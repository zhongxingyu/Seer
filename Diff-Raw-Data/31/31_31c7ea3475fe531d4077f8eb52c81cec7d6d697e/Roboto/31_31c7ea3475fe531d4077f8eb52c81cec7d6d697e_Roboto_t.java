 package balle.brick;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 import lejos.nxt.SensorPort;
 import lejos.nxt.Sound;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.comm.BTConnection;
 import lejos.nxt.comm.Bluetooth;
 import balle.bluetooth.messages.AbstractMessage;
 import balle.bluetooth.messages.MessageDecoder;
 import balle.bluetooth.messages.MessageKick;
 import balle.bluetooth.messages.MessageMove;
 import balle.bluetooth.messages.MessageRotate;
 import balle.bluetooth.messages.MessageStop;
 import balle.controller.Controller;
 
 class ListenerThread extends Thread {
     DataInputStream input;
     boolean         shouldStop;
     int             command;
     boolean         commandConsumed;
 
     ListenerThread(DataInputStream input) {
         this.input = input;
         this.shouldStop = false;
         this.commandConsumed = true;
     }
 
     @Override
     public void run() {
 
         while (!shouldStop) {
             try {
                 int command = input.readInt();
                 setCommand(command);
             } catch (IOException e) {
                 shouldStop = true;
             }
         }
     }
 
     private synchronized void setCommand(int command) {
         this.command = command;
         commandConsumed = false;
     }
 
     public synchronized int getCommand() {
         commandConsumed = true;
         return command;
     }
 
     public synchronized boolean available() {
         return !commandConsumed;
     }
 
     public void cancel() {
         shouldStop = true;
     }
 
 }
 
 /**
  * Create a connection to Roboto from the computer. execute commands send from
  * the computer test out movements of Roboto.
  * 
  * @author s0815695
  */
 public class Roboto {
 
     /**
      * Processes the decoded message and issues correct commands to controller
      * 
      * @param decodedMessage
      *            the decoded message
      * @param controller
      * @return true, if successful
      */
     public static boolean processMessage(AbstractMessage decodedMessage,
             Controller controller) {
         String name = decodedMessage.getName();
 
         if (name.equals(MessageKick.NAME)) {
             MessageKick messageKick = (MessageKick) decodedMessage;
             if (messageKick.isPenalty()) {
                 controller.penaltyKick();
             } else {
                 controller.kick();
             }
         } else if (name.equals(MessageMove.NAME)) {
             MessageMove messageMove = (MessageMove) decodedMessage;
             controller.setWheelSpeeds(messageMove.getLeftWheelSpeed(),
                     messageMove.getRightWheelSpeed());
         } else if (name.equals(MessageStop.NAME)) {
             MessageStop messageStop = (MessageStop) decodedMessage;
             if (messageStop.floatWheels())
                 controller.floatWheels();
             else
                 controller.stop();
         } else if (name.equals(MessageRotate.NAME)) {
             MessageRotate messageRotate = (MessageRotate) decodedMessage;
             controller.rotate(messageRotate.getAngle(),
                     messageRotate.getSpeed());
         } else {
             return false;
         }
         return true;
     }
 
     /**
      * Main program
      * 
      * @param args
      */
     public static void main(String[] args) {
 
         TouchSensor touchRight = new TouchSensor(SensorPort.S1);
         TouchSensor touchLeft = new TouchSensor(SensorPort.S2);
 
         while (true) {
             // Enter button click will halt the program
             if (Button.ENTER.isPressed())
                 break;
 
             drawMessage("Connecting...");
            Sound.twoBeeps();
 
             BTConnection connection = Bluetooth.waitForConnection();
 
             drawMessage("Connected");
             Sound.beep();
 
             DataInputStream input = connection.openDataInputStream();
             ListenerThread listener = new ListenerThread(input);
 
             Controller controller = new BrickController();
             MessageDecoder decoder = new MessageDecoder();
 
             listener.start();
 
             while (true) {
                 // Enter button click will halt the program
                 if (Button.ENTER.isPressed()) {
                    controller.stop();
                    listener.cancel();
                     break;
                 }
                if (Button.ESCAPE.isPressed()) {
                    return;
                }
                 try {
                     // Check for sensors when idle
                     if (touchLeft.isPressed() || touchRight.isPressed()) {
                         controller.backward(controller.getMaximumWheelSpeed());
                         drawMessage("Obstacle detected. Backing up");
                         Thread.sleep(400);
                         controller.stop();
                     }
 
                     if (!listener.available())
                         continue;
 
                     int hashedMessage = listener.getCommand();
                     AbstractMessage message = decoder
                             .decodeMessage(hashedMessage);
                     if (message == null) {
                         drawMessage("Could not decode: " + hashedMessage);
                         break;
                     }
                     String name = message.getName();
                     drawMessage(name);
 
                     boolean successful = processMessage(message, controller);
                     if (!successful) {
                         drawMessage("Unknown message received: "
                                 + hashedMessage);
                         break;
                     }
 
                 } catch (Exception e1) {
                     drawMessage("Error in MainLoop: " + e1.getMessage());
                 }
             }
 
             connection.close();
         }
     }
 
     private static void drawMessage(String message) {
         LCD.clear();
         LCD.drawString(message, 0, 0);
         LCD.refresh();
     }
 
 }
