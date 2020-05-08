 package org.emoseman.beagle.bot;
 
 import org.apache.log4j.Logger;
 import org.emoseman.beagle.io.Analog;
 import org.emoseman.beagle.io.AnalogFactory;
 import org.emoseman.beagle.io.LED;
 import org.emoseman.beagle.io.LEDFactory;
 import org.emoseman.beagle.io.LEDTrigger;
import org.emoseman.beagle.sensor.LPY510ALGyro;
 
 public class Bot
 {
   private static final Logger log = Logger.getLogger(Bot.class);
 
   public static void main(final String[] args)
     throws Exception
   {
     log.info("Starting Bot...");
 
     log.info("Initializing IO Objects...");
     LED led0 = LEDFactory.getLED(0);
     LED led1 = LEDFactory.getLED(1);
     LED led2 = LEDFactory.getLED(2);
     LED led3 = LEDFactory.getLED(3);
    // Analog a1 = AnalogFactory.getAnalog(1);
     Analog a2 = AnalogFactory.getAnalog(2);
     Analog a3 = AnalogFactory.getAnalog(3);
     Analog a4 = AnalogFactory.getAnalog(4);
     Analog a5 = AnalogFactory.getAnalog(5);
     Analog a6 = AnalogFactory.getAnalog(6);
     Analog a7 = AnalogFactory.getAnalog(7);
     Analog a8 = AnalogFactory.getAnalog(8);
 
     led0.setTrigger(LEDTrigger.mmc0);
     led1.setTrigger(LEDTrigger.heartbeat);
     led2.setTrigger(LEDTrigger.mmc0);
     led3.setTrigger(LEDTrigger.heartbeat);
 
    LPY510ALGyro gyro = LPY510ALGyro.build(1);

     // GPIO gpio0 = GPIOFactory.getGPIO(10);
     // // gpio0.activatePin();
     // gpio0.setPinDirection(PINDirection.out);
     // gpio0.setValueHigh();
 
    // StatsArray accelZ = new StatsArray(30);
    // StatsArray accelX = new StatsArray(30);

 
     int counter = 0;
     while (true)
     {
      gyro.update();

      if (counter == 200)
       {
        System.out.println("Gyro: " + gyro.toString());
         counter = 0;
       }
 
       Thread.sleep(10);
       counter++;
     }
 
   }
 }
