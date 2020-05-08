 package AP2DX.sensor;
 
 import java.util.ArrayList;
 
 import AP2DX.AP2DXBase;
 import AP2DX.AP2DXMessage;
 import AP2DX.Message;
 import AP2DX.Module;
 import AP2DX.specializedMessages.*;
 import AP2DX.sensor.*;
 
 // Imports for the interface
 import javax.swing.*;
 import java.awt.Graphics;
 import java.lang.Thread;
 
 public class Program extends AP2DXBase 
 {
 
     /** The array with the range data from the Sonar Sensor */
     //private double[] rangeArray;
 
     private Drawer drawer;
 
     /**
      * Entrypoint of mapper
      */
     public static void main (String[] args){
         new Program();
     }
 
 
     /**
      * constructor
      */
     public Program() 
     {
         super(Module.SENSOR); // explicitly calls base constructor
         System.out.println(" Running Sensor... ");
     }
 
     /**
      * In this override, the program sets up a new JFrame. Then it creates a ``Drawer'', our class that draws lines of the sensor data.
      * It does enough to add the drawer to the frame, sets the size of the frame to 400x400 and draws a standard line
      * This standard line is just to see that it works
      * TODO: If everything works, stop drawing this line.
      */
     @Override
         protected void doOverride() 
         {
             JFrame frame = new JFrame("Sonar");
             drawer = new Drawer();
             frame.getContentPane().add(drawer);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setSize(500,500);
             frame.setVisible(true);
             System.out.println("Ended override");
 
             // probably for testin' purposes? Removed either way :P
             // (In hindsight I discovered it wasn't for testing purposes but values could not be 
             // null... )
             //double[] sonarDataSample = {2.0, 3.0, 4.0, 5.0, 5.0, 4.0, 3.0, 2.0};
             //// test data to draw range scanner data
             //double[] rangeScannerDataSample = {2.1924,2.1961,2.1961,2.1964,2.2010,2.2031,2.2042,2.2107,2.2165,2.2196,2.2300,2.2375,2.2456,2.2519,2.2614,2.2730,2.2809,2.2926,2.3073,2.3196,2.3340,2.3498,2.3682,2.3838,2.4021,2.4207,2.4418,2.4625,2.4869,2.5099,2.5354,2.5593,2.5882,2.6187,2.6492,2.6776,1.4046,1.3873,1.3544,1.3256,1.2973,1.2717,1.2460,1.2234,1.1989,1.1908,1.1702,1.1508,1.1305,1.1130,1.0971,1.0818,1.0656,1.0523,1.0370,1.0244,1.0131,1.0016,0.9890,0.9799,0.9689,0.9493,0.9408,0.9325,0.9235,0.9167,0.9095,0.9014,0.8948,0.8886,0.8824,0.8774,0.8728,0.8673,0.8622,0.8587,0.8550,0.8512,0.8387,0.8430,0.9502,1.0554,1.1908,1.4281,1.6740,1.8226,1.8269,1.8251,1.8257,1.8237,1.8212,1.8244,1.8229,1.8252,1.8260,1.8288,1.8340,1.8350,1.8424,1.8441,1.8506,1.8562,1.8653,1.8715,1.8786,1.8894,1.8983,1.9062,1.9193,1.9272,1.9407,1.9523,1.9663,1.9828,1.9963,2.0142,2.0307,2.0497,2.0655,2.0853,2.1070,2.1273,2.1546,2.1783,2.2044,2.2274,2.2578,2.2877,2.3175,2.1972,2.2519,2.3132,2.3789};
 
             //drawer.paintSonarLines(sonarDataSample); 
             //drawer.paintRangeScannerLines(rangeScannerDataSample);
         }
 
     @Override
         public ArrayList<AP2DXMessage> componentLogic(Message message) 
         {
             //System.out.println("\tReceived a message in the component logic of the sensor module. The message was: " + message.getMessageString());
             ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage>();
 
             
             try 
             {
                 switch (message.getMsgType())
                 {
                     case AP2DX_SENSOR_ODOMETRY:
                         //System.out.print("Parsing an odometry message, but we do");
                         //System.out.println(" with this data.. so pass.");
                         // Turns out odometry is something different than we had expected {{{
                         //OdometrySensorMessage odometrySensorMessage = 
                         //    (OdometrySensorMessage) message;
                         //odometrySensorMessage.setSourceModuleId(IAM);
                         //odometrySensorMessage.setDestinationModuleId(Module.MAPPER);
                         //messageList.add(odometrySensorMessage);
 
                         //// TO THE REFLEX! 
                         //// I'm not sure if this little piece of magic works
                         //OdometrySensorMessage message2 = new OdometrySensorMessage((AP2DXMessage) odometrySensorMessage.clone());
                         //message2.setDestinationModuleId(Module.REFLEX);
                         //messageList.add(message2); // }}}
                         break;
                     case AP2DX_SENSOR_INS:
                         //System.out.println("Parsing an INS message: " + message.getMessageString());
                         // TO THE MAPPER! Said Batman
                         InsSensorMessage insSensorMessage = (InsSensorMessage) message;
                         insSensorMessage.parseMessage();
                         insSensorMessage.setSourceModuleId(IAM);
                         insSensorMessage.setDestinationModuleId(Module.MAPPER);
                         insSensorMessage.compileMessage();
                         messageList.add(insSensorMessage);
 
 
                         // TO THE REFLEX! 
                         //insSensorMessage.compileMessage();
                         InsSensorMessage message2 = new InsSensorMessage(
                         //insSensorMessage);
                                (AP2DXMessage) insSensorMessage.clone());
                         message2.setDestinationModuleId(Module.REFLEX);
                         message2.compileMessage();
                         messageList.add(message2);
                         break;
                     case AP2DX_SENSOR_SONAR:
                         //create the SonarSensorMessage instance
                         SonarSensorMessage sonarSensorMessage = (SonarSensorMessage) message;
                         //System.out.println("Sonar data: " + sonarSensorMessage.getRangeArray());
                         if (sonarSensorMessage.getRangeArray() == null)
                             System.out.println("ERROR in AP2DX.sensor.Program.ComponentLogic(), SonarSensorMessage array is null");
                         else
                         {
                             //System.out.println("Updating the sonar lines");
                             try{
                                 drawer.paintSonarLines(sonarSensorMessage.getRangeArray());
                             }
                             catch (NullPointerException e)
                             {
                                 System.out.println("Drawer does not exist yet");
                             }
                             // Clone it to send it to reflex
                            SonarSensorMessage sonarSensorMessage2 = new SonarSensorMessage( (AP2DXMessage) sonarSensorMessage.clone());
                            sonarSensorMessage2.parseMessage();
                             sonarSensorMessage2.setSourceModuleId(Module.SENSOR);
                            sonarSensorMessage2.setDestinationModuleId(Module.REFLEX);  
                            sonarSensorMessage2.compileMessage();
                             messageList.add(sonarSensorMessage2);
                         }
                         break;
                     case AP2DX_SENSOR_RANGESCANNER:
                         //System.out.println("RangeScanner message detected");
                         RangeScannerSensorMessage rangeScannerSensorMessage = 
                             (RangeScannerSensorMessage) message;
                         try{
                             drawer.setRangeScannerFov(rangeScannerSensorMessage.getFov());
                             drawer.setRangeScannerResolution(rangeScannerSensorMessage.getResolution());
                             drawer.paintRangeScannerLines(rangeScannerSensorMessage.getDataArray());
                         }
                         catch(NullPointerException e)
                         {
                             System.out.println("Drawer does not exist");
                         }
                         break;
 
                     default:
                         //System.out.println("Unexpected message type in ap2dx.sensor.Program: " + message.getMsgType());
 
                 }  
             }
             catch (Exception e)
             {
                 System.out.println("Something went wrong in the parsing of a message in sensor.Program.compontentLogic");
                 System.out.println("message.getMessageString() " + message.getMessageString());
                 System.out.println("message.getMsgType() " + message.getMsgType());
                 System.out.println("e.getMessage() " + e.getMessage());
                 System.out.println("Printing stacktrace:");
                 e.printStackTrace(); 
             } 
         
 
            //for (int i = 0; i < messageList.size(); i ++)
            //    System.out.println("!!!! " + i + " message: " + messageList.get(i).getMessageString());
         
             return messageList;
         }
 }
 
 
 
 
 
 
