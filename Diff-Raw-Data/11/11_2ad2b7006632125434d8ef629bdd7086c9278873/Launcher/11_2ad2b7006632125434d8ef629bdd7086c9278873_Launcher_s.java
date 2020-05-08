 package zwapi;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import zwapi.nodes.ZWEZMotionSensor;
 import zwapi.nodes.ZWNetworkController;
 
 /**
  * @author David Lemvigh
  * @author Andreas Møller
  */
 public class Launcher {
 	
 
     public static void main(String[] args)
     throws InterruptedException
     {
 
         ZWNetworkController trickle;
         ZWNetwork network;
         try {
             trickle = new ZWNetworkController((byte) 16, "");
             //aeon = new ZWNetworkController((byte) 1, "");
             
             network = new ZWNetwork(trickle);
 
 //            ZWLampModule lamp = new ZWLampModule(13);
 //            network.addNode(lamp);
 //
 //            ZWLampModule lamp2 = new ZWLampModule(4);
 //            network.addNode(lamp2);
             
             ZWSQLLogger logger = new ZWSQLLogger();
            args = new String[]{"30"};
             Map<Integer, ZWEZMotionSensor> sensors = new HashMap<Integer, ZWEZMotionSensor>();
             if (args.length > 0) {
                 for(String s : args) {
                     try {
                         int id = Integer.parseInt(s);
                         ZWEZMotionSensor sensor = new ZWEZMotionSensor(id);
                         network.addNode(sensor);
                         sensor.addZWBasicListener(logger);
                         sensors.put(id, sensor);
                     }catch(NumberFormatException ex) {
                     }
                 }
             } else {
                 ZWEZMotionSensor sensor20 = new ZWEZMotionSensor(20);
                 network.addNode(sensor20);
                 sensor20.addZWBasicListener(logger);
     
                 ZWEZMotionSensor sensor21 = new ZWEZMotionSensor(21);
                 network.addNode(sensor21);
                 sensor21.addZWBasicListener(logger);            
             }
             
 //            ZWEZMotionSensor sensor = new ZWEZMotionSensor(30);
 //            network.addNode(sensor);
             
 //            sensor.setAssociation((byte) 1, (byte) 1);
 //            sensor.setAssociation((byte) 16, (byte) 2);
 
 //            sensor.removeAssociation((byte) 1, (byte) 1);
 //            sensor.removeAssociation((byte) 42, (byte) 4);
 //            sensor.setOnTime(0);
 
 //            trickle.sendNodeInfo();
 //            lamp.on();
 //            Thread.sleep(1000);
 //            lamp.off();
 //            Thread.sleep(1000);
 //            lamp2.on();
 //            Thread.sleep(1000);
 //            lamp2.off();
 //            trickle.sendNodeInfo();
 //            lamp.off();
 //            trickle.sendNodeInfo();
 //            lamp.off();
 
 //           	Thread.sleep(1000);
 //           	System.exit(0);
 
 //            ZWSensor sensor = new ZWEZMotionSensor(49);
 //            network.addNode(sensor);
 
 //            lamp.setLevel(20);
 //            sensor.getConfigurationParameter((byte) 2);
 //            sensor.getAssociation((byte) 6);
             
 //            ZWFrame zm = new ZWFrame(ZWFrame.REQUEST, (byte) 0x53, null);
 //            trickle.send(zm.getBytes());
 //            trickle.getVersion();
 //            trickle.sendNodeInfo();
 
 
         } catch (Exception ex) {
             System.err.println("Could open connection to Network Controller");
             ex.printStackTrace();
             System.exit(-1);
         }
 
         try {
             System.in.read();
             System.err.println("Program terminated");
             System.exit(0);
         } catch (IOException ex) {
             System.err.println("Failed waiting for input, ending program now!");
             System.exit(-2);
         }
 
     }
 }
