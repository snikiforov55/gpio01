package stnik.pi.gpio01


import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.util.ConsoleColor
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.util.CommandArgumentParser
import io.javalin.Context
import java.util.concurrent.TimeUnit
import io.javalin.Javalin
import io.javalin.staticfiles.Location


fun main(args: Array<String>){
    println("Hello Gpio!")

    val app = Javalin.create()
    //app.get("/") { ctx : Context -> ctx.result("Hello World") }
    app.enableStaticFiles("gpio01web", Location.EXTERNAL)
    app.start(7000)

    val gpio = GpioFactory.getInstance()

    // by default we will use gpio pin #01; however, if an argument
    // has been provided, then lookup the pin by address
    val pin = CommandArgumentParser.getPin(
        RaspiPin::class.java, // pin provider class to obtain pin instance from
        RaspiPin.GPIO_02, // default pin if no pin argument found
        args.toString())             // argument array to search in

    // by default we will use gpio pin PULL-UP; however, if an argument
    // has been provided, then use the specified pull resistance
    val pull = CommandArgumentParser.getPinPullResistance(
        PinPullResistance.PULL_UP, // default pin pull resistance if no pull argument found
        args.toString())                      // argument array to search in

    // provision gpio pin as an input pin
    val input = gpio.provisionDigitalInputPin(pin, "MyInput", pull)

    // set shutdown state for this pin: unexport the pin
    input.setShutdownOptions(true)

    // prompt user that we are ready
    println("Successfully provisioned [$pin] with PULL resistance = [$pull]")
    println()
    println("The GPIO input pin states will be displayed below.")
    println()

    // display pin state
    println()
    println(
        " [" + input.toString() + "] digital state is: " + ConsoleColor.conditional(
            input.state.isHigh, // conditional expression
            ConsoleColor.GREEN, // positive conditional color
            ConsoleColor.RED, // negative conditional color
            input.state
        )
    )
    println()


    val myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, PinPullResistance.PULL_DOWN)
    // set shutdown state for this input pin
    myButton.setShutdownOptions(true)

    app.get("/gpio") { ctx : Context -> ctx.result("{\"id\" : \"none\",  \"name\" : " +
            "\"GPIO7\"" +
            ", \"pin\": \"" + myButton.state + "\" " + "}") }

    while (true) {
        println(
            " [" + myButton.toString() + "] digital state is: " + ConsoleColor.conditional(
                myButton.isHigh, // conditional expression
                ConsoleColor.GREEN, // positive conditional color
                ConsoleColor.RED, // negative conditional color
                myButton.state
            )
        )
        TimeUnit.MILLISECONDS.sleep(500L)
    }

    // stop all GPIO activity/threads by shutting down the GPIO controller
    // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
    gpio.shutdown()

    /*
    app.get("/gpio") { ctx : Context -> ctx.result("{\"id\" : \"none\",  \"name\" : " +
            "\"GPIO7\"" +
            ", \"pin\": \"" + "OFF" + "\" " + "}") }

    println("Running")
    while (true) {
        print(".")
        TimeUnit.MILLISECONDS.sleep(1000L)
    }
    */

}