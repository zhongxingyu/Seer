 package org.java_websocket;
 
 import java.net.UnknownHostException;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 /**
  *
  * @author Matteo Ciman
  * 
  * @version 0.1
  * 
  * v 0.1 First specification of the class
  */
 public class EyeTrackerManager extends WebSocketWithOffsetCalc {
     
     public EyeTrackerManager(int port) throws UnknownHostException {
         super("EyeTrackerClient", port);
     }
     
     /*@Override
     public long managerReady() {
         return serverManager.eyeTrackerReady();
     }*/
     
     @Override
     public boolean onMessage(WebSocket conn, String message) {
         
         try {
             boolean alreadyManaged = super.onMessage(conn, message);
             JSONObject packet = (JSONObject)JSONValue.parse(message);
 
            //System.out.println(packet);
             if (packet.get("TYPE").equals("IDENTIFICATION") && clientConnected != null) {
                 BaseManager.eyeTrackerManager = this;
             }
 
             if (!alreadyManaged) {
 
                 if (packet.get("TYPE").equals("EYE_TRACKER_DATA")) {
 
                     synchronized(messageManager.bufferSynchronizer) {
                         messageManager.messagesEyeTrackerBuffer.add(packet);
                         messageManager.bufferSynchronizer.notifyAll();
                     }
                 }
                 else if (packet.get("TYPE").equals("READY_TO_PLAY")) {
 
                     System.out.println("Eye_Tracker_Manager READY TO START");
                 }
                 else if (packet.get("TYPE").equals("TRAINING_POSITIONS")) {
 
                     patientManager.sendPacket(packet);
                 }
                 else if (packet.get("TYPE").equals("TRAINING_SESSION")) {
                     doctorManager.sendPacket(packet);
                 }
                else {
                    System.out.println("PACCHETTO NON CORRETTO eyeTrackerManager");
                    System.out.println(packet);
                    System.out.println("****************************");
                }
             }
             return true;
         }
         catch(Exception err) {
             System.out.println("***** EXCEPTION ON MESSAGE *****");
             System.out.println("** Error onMessage eyeTrackerManager ** ");
             System.out.println(err.toString());
             err.printStackTrace();
             System.out.println("***** END EXCEPTION MESSAGE  ***");
             return true;
         }
     }
     
 }
