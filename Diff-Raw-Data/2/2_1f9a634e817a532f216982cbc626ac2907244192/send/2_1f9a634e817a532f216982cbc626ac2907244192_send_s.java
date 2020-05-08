 /*
 * Source code of the receiving Sun SPOT. This source code is part of our Cobot
  * project (for the robotics course on the University of Amsterdam).
  *
  * Authors: Lucas Swartsenburg, Harm Dermois
  **/
 package org.sunspotworld.demo;
 
 import com.sun.spot.service.BootloaderListenerService;
 import com.sun.spot.resources.Resources;
 import com.sun.spot.resources.transducers.*;
 import com.sun.spot.sensorboard.*;
 
 import com.sun.spot.util.Utils;
 
 import java.io.IOException;
 
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.Datagram;
 import javax.microedition.io.DatagramConnection;
 
 import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
 
 /**
  * A class that receives directions from another sunspot and sends them by
  * bitbanging to a Cobot.
  */
 public class send extends javax.microedition.midlet.MIDlet {
 
     private ITriColorLEDArray leds =
         (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
     IIOPin pins[] = EDemoBoard.getInstance().getIOPins();
     IIOPin send_pin = pins[EDemoBoard.D0];
     IIOPin send_data_pin = pins[EDemoBoard.D1];
     IIOPin pin_ack = pins[EDemoBoard.D2];
     IIOPin pin_rst = pins[EDemoBoard.D3];
     int error = 0;
     private static final int DRIVE = 0;
     private static final int REVERSE = 1;
     private static final int LEFT = 2;
     private static final int RIGHT = 3;
     private static final int STOP = 4;
 
     private static final int BITS = 4;
 
     /**
      * Start the sunspot, initiate all pins, the leds, make sure that there is
      * a handshake and after the handshake initiate a receiving thread.
      */
     protected void startApp() {
 
         /*
          * Listen for downloads/commands over USB connection
          */
         new com.sun.spot.service.BootloaderListenerService()
                 .getInstance().start();
 
         System.out.println("I'm about to rock that Cobot !");
         /*
          * Show we are waiting for the Cobot
          */
         leds.setColor(LEDColor.RED);
         leds.setOn();
 
         send_pin.setAsOutput(true);
         send_data_pin.setAsOutput(true);
         pin_ack.setAsOutput(false);
         pin_rst.setAsOutput(false);
 
         /*
          * Set pins high so that the Cobot can see this device is ready.
          */
         send_pin.setHigh();
         send_data_pin.setHigh();
         /*
          * Check if Cobot is ready.
          */
         while (!(pin_ack.isHigh() && pin_rst.isHigh())) {
         }
         /**
          * There has been a handshake.
          */
         show_handshake();
 
         send_pin.setLow();
         send_data_pin.setLow();
         startReceiverThread();
     }
 
     /**
      * Display on the leds as well as on the std out that there has been a
      * handshake.
      */
     public void show_handshake(){
         System.out.println("Hands have been shaken, but not stirred");
         leds.setColor(LEDColor.YELLOW);
         leds.getLED(0).setOff();
         leds.getLED(7).setOff();
     }
 
     /**
      * Receive what has to be done in a thread.
      */
     public void startReceiverThread() {
         new Thread() {
             public void run() {
                 System.out.println("Start receiving");
                 int direction = STOP;
                 /*
                  * Connect...
                  */
                 RadiogramConnection dgConnection = null;
                 Datagram dg = null;
 
                 try {
                     dgConnection = (RadiogramConnection) Connector.open("radiogram://:37");
                     // Then, we ask for a datagram with the maximum size allowed
                     dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                 } catch (IOException e) {
                     System.out.println("Could not open radiogram receiver connection");
                     return;
                 }
 
                 /*
                  * The receiving variables are ready. Start receiving.
                  */
                 while (true) {
                     try {
                         dg.reset();
                         dgConnection.receive(dg);
                         direction = dg.readInt();
                         /*
                          * Show on the leds which direction has been received.
                          */
                         set_Leds(direction);
                         /*
                          * Bitbang the received direction to the Cobot
                          */
                         bitbang_send(direction);
                     } catch (IOException e) {
                         System.out.println("Nothing received");
                     }
                 }
             }
         }.start();
     }
 
     /**
      * This function uses bitbanging to send data to the Cobot. We send a 6 bit
      * code. The first three bits contain the data, the other three contain the
      * same data so that the Cobot can check the data.
      */
     public int bitbang_send(int data) {
         System.out.println("---- Start send ----");
         int i;
         int data_cop = data;
         /*
          * Start by setting both pins low. This is to prevent confusing the
          * Cobot.
          */
         send_pin.setLow();
         send_data_pin.setLow();
 
         /*
          * Loop for every bit to be send.
          */
         for (i = 0; i < 6; i++) {
             /*
              * Wait until the Cobot is done handling the last received bit.
              */
             while (!(pin_rst.isLow()&&pin_ack.isLow())) {
                 error++;
                 if (error == 20000) {
                     error = 0;
                     print_state("In while waiting for rst off");
                 }
             }
             error = 0;
 
             /*
              * If the first three bits have been send (by bitshift), restore the
              * data var to what is was before bitshifting.
              */
             if (i == 3) {
                 data = data_cop;
                 System.out.println("DATA: " + data);
             }
 
             /*
              * Bitwise AND on most left bit (the data is 3 bits in total).
              */
             if ((data & 0x4) > 0) {
                 send_data_pin.setHigh();
                 System.out.print("Set data: 1");
             } else {
                 send_data_pin.setLow();
                 System.out.print("Set data: 0");
             }
 
             /*
              * Tell the Cobot that there is new data.
              */
             send_pin.setHigh();
             System.out.println("");
 
             /*
              * Wait until the cobot says it has received the bit.
              */
             while (!(pin_ack.isHigh())) {
                 error++;
                 if (error == 20000) {
                     error = 0;
                     print_state("In while waiting for ack on");
                 }
             }
             error = 0;
 
             /*
              * Make sure that the ack pin is off before sending new data.
              */
             send_pin.setLow();
 
             /*
              * The reset pin is on means that the Cobot didn't receive the data
              * properly.
              */
             if (pin_rst.isHigh()) {
                 print_state("ERROR!");
                 bitbang_send(data_cop);
                 return -1;
             }
 
             data <<= 1;
         }
 
         /*
          * Cleanup the datapin.
          */
         send_data_pin.setLow();
         print_state("Finished");
         return 1;
     }
 
     /**
      * Put a appropriate combination on the leds according to the received
      * direction.
      * @param drive
      */
     public void set_Leds(int drive) {
         switch (drive) {
             case DRIVE:
                 for (int i = 0; i < 2; i++) {
                     leds.getLED(i).setOff();
                 }
                 for (int i = 6; i < 8; i++) {
                     leds.getLED(i).setOff();
                 }
                 for (int i = 2; i < 6; i++) {
                     leds.getLED(i).setColor(LEDColor.GREEN);
                     leds.getLED(i).setOn();
                 }
                 break;
             case REVERSE:
                 for (int i = 0; i < 2; i++) {
                     leds.getLED(i).setOff();
                 }
                 for (int i = 6; i < 8; i++) {
                     leds.getLED(i).setOff();
                 }
                 for (int i = 2; i < 6; i++) {
                     leds.getLED(i).setColor(LEDColor.ORANGE);
                     leds.getLED(i).setOn();
                 }
                 break;
             case LEFT:
                 for (int i = 0; i < 4; i++) {
                     leds.getLED(i).setOff();
                 }
                 for (int i = 4; i < 8; i++) {
                     leds.getLED(i).setColor(LEDColor.BLUE);
                 }
                 for (int i = 4; i < 8; i++) {
                     leds.getLED(i).setOn();
                 }
                 break;
 
             case RIGHT:
                 for (int i = 4; i < 8; i++) {
                     leds.getLED(i).setOff();
                 }
 
                 for (int i = 0; i < 4; i++) {
                     leds.getLED(i).setColor(LEDColor.WHITE);
                 }
                 for (int i = 0; i < 4; i++) {
                     leds.getLED(i).setOn();
                 }
                 break;
 
             case STOP:
                 for (int i = 0; i < 3; i++) {
                     leds.getLED(i).setOff();
                 }
                 for (int i = 5; i < 8; i++) {
                     leds.getLED(i).setOff();
                 }
                 leds.getLED(3).setColor(LEDColor.RED);
                 leds.getLED(3).setOn();
                 leds.getLED(4).setColor(LEDColor.RED);
                 leds.getLED(4).setOn();
                 break;
         }
 
     }
 
     /**
      * Print the state in words to std out.
      *
      * @param dir
      */
     public void print_dir(int dir){
        switch (dir) {
             case DRIVE:
                 System.out.println("Drive");
                 break;
             case REVERSE:
                 System.out.println("Reverse");
                 break;
             case LEFT:
                 System.out.println("Left");
                 break;
 
             case RIGHT:
                 System.out.println("Right");
                 break;
             case STOP:
                 System.out.println("Stop");
                 break;
         }
     }
 
     /**
      * Used in while loops...
      * Print the current state of the pins and print the message.
      *
      * @param event
      */
     public void print_state(String event) {
         System.out.println(event);
         System.out.println("Pin_ack: " + pin_ack.isHigh() + " | "
                 + "Pin_rst: " + pin_rst.isHigh() + "");
         System.out.println("Send_ack: " + send_pin.isHigh() + " | "
                 + "Send_data: " + send_data_pin.isHigh() + "");
     }
 
     protected void pauseApp() {
     }
 
     /**
      * Called if the MIDlet is terminated by the system.
      * I.e. if startApp throws any exception other than MIDletStateChangeException,
      * if the isolate running the MIDlet is killed with Isolate.exit(), or
      * if VM.stopVM() is called.
      *
      * It is not called if MIDlet.notifyDestroyed() was called.
      *
      * @param unconditional If true when this method is called, the MIDlet must
      *    cleanup and release all resources. If false the MIDlet may throw
      *    MIDletStateChangeException  to indicate it does not want to be destroyed
      *    at this time.
      */
     protected void destroyApp(boolean unconditional) {
     }
 }
