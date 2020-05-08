 package AP2DX.coordinator;
 
 import java.net.*;
 import java.util.ArrayList;
 import java.io.*;
 import java.util.Map;
 
 import AP2DX.*;
 import AP2DX.specializedMessages.*;
 import AP2DX.usarsim.*;
 import AP2DX.usarsim.specialized.*;
 
 /**
  * This is the class that will take care of receiving messages from UsarSim, and forwarding them to 
  * the Sensor module. 
  * @author Maarten de Waard
  */
 public class UsarMessageParser extends Thread
 {
     /** An UsarSimMessageReader to read the messages USARSim sends us. */
     private UsarSimMessageReader in;
     /** The module this parser relates to (For message sending purposes) */
     private Module IAM;
     /** The module this parser sends to */
     private Module send;
     /** The connectionHandler that handles the send connection */
     private ConnectionHandler sendConnection;
     /** The base class (supposed to be the AP2DXBase of coordinator */
     private AP2DXBase base;
     /**
      * This constructor configures the connection with usarsim, 
      * with the data that is specified in the config file.
      * @param base The base (should be coordinator)
      * @param IAM The module from which this parser will send messages
      * @param send The module this parser will send to
      */
     public UsarMessageParser(AP2DXBase base, Module IAM, Module send, Socket socket)
     {
         this.base = base;
         this.IAM = IAM;
         this.send = send;
         try 
         {
             this.sendConnection = base.getSendConnection(send);
             in = new UsarSimMessageReader(socket.getInputStream());
         }
         catch (Exception ex) 
         {
             ex.printStackTrace();
         }
     }
 
     public void run()
     {
         while (true) 
         {
             Message messageIn = null;
             try 
             {
                 System.out.println("\tReading message in UsarMessageParser.run()");
                 messageIn = in.readMessage();
                 //System.out.println("Read message to string: " + messageIn.toString());
             } 
             catch (Exception e) 
             {
                 e.printStackTrace();
                 base.logger
                     .severe("Error in UsarMessageParser.run, attempted to retrieve item out of messageReader");
             }
             //Initialize the message we will return. This shouldn't stay null.
             AP2DXMessage message = null;
             switch (messageIn.getMsgType())
             {
                 case USAR_SENSOR:
                     System.out.println("Parsing usar sensor message ...");
                     UsarSimSensorMessage sensorMessage = null;
                     try
                     {
                         sensorMessage = new UsarSimSensorMessage((UsarSimMessage) messageIn);
                         switch (sensorMessage.getSensorType())
                         {
                             case USAR_ODOMETRY: 
                                 System.out.print("Ignoring odometry message");
                                 System.out.println(" as we are not using those.");
                                 break;
                             case USAR_INS:
                                 UInsSensorMessage insMessage = 
                                     new UInsSensorMessage(sensorMessage);
                                 message = (InsSensorMessage) 
                                     insMessage.toAp2dxMessage();
                                 break;
                             case USAR_SONAR:
                                 USonarSensorMessage sonarMessage = 
                                     new USonarSensorMessage(sensorMessage);
                                     // Put the right values in the message
                                 message = (SonarSensorMessage) 
                                     sonarMessage.toAp2dxMessage();
                                 break;
                             case USAR_RANGE:
                                 URangeSensorMessage rangeMessage = 
                                     new URangeSensorMessage(sensorMessage);
                                 message = (RangeScannerSensorMessage) 
                                     rangeMessage.toAp2dxMessage();
                                 break;
 
                             default: 
                                 System.out.print("Ignoring unimportant or ");
                                 System.out.print(" unknown sensor message, OR "); 
                                 System.out.println("message was incorrectly parsed"); 
                         };
                     }
                     catch (Exception e)
                     {
                         System.err.println("Some exception occured while making a specialized Usar sensor message, or perhaps even at casting to usarsimsensormessage");
                         System.err.println("Message.getSensorType() : " + sensorMessage.getSensorType());
                         System.err.println("exception.getMessage(): " + e.getMessage());
                         System.err.println("exception.printing stacktrace.");
                         e.printStackTrace();
                     }
                     break;
                 default:
                     System.out.println("Unexpected message type in ap2dx.coordonator.UsarMessageParser: " + messageIn.getMsgType());
             };
             //Try to send the message to the right connection.
             if(message != null && message.getMsgType() != Message.MessageType.UNKNOWN)
             {
                 try 
                 {
                	System.out.println("Attempting to send a message");
                     sendConnection.sendMessage(message);
                 } 
                 catch (Exception e) 
                 {
                     e.printStackTrace();
                     base.logger
                         .severe("Error in UsarMessageParser.run, attempted get the connection of message: "
                                 + message);
                 }
             System.out.printf("End of UsarMessageParser.run while loop, message %s may be sent.\n", message);
             }
             else
                 System.out.printf("End of UsarMessageParser.run while loop. No messages sent\n");
         }
     } 
 }
 
 
