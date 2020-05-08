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
     
     public static JSONObject packetWithScreenDimension = null;
     
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
             
             Object packetType = packet.get("TYPE");
 
             //System.out.println(packet);
             if (packetType.equals("IDENTIFICATION") && clientConnected != null) {
                 BaseManager.eyeTrackerManager = this;
             }
 
             if (!alreadyManaged) {
 
                 if (packetType.equals("GAZE_DATA")) {
 
                     synchronized(messageManager.bufferSynchronizer) {
                         messageManager.messagesEyeTrackerBuffer.add(packet);
                         messageManager.bufferSynchronizer.notifyAll();
                     }
                 }
                 /*
                  * Packet with the coordinates for the calibration
                  */
                 else if (packetType.equals("CAL_POINT")) {
 
                     patientManager.sendPacket(packet);
                 }
                 else if (packetType.equals("READY_TO_PLAY")) {
 
                     System.out.println("Eye_Tracker_Manager READY TO START");
                     
                     if (waitingForTracker) {
                         waitingForTracker = false;
                         
                         clientConnected.send(packetWithScreenDimension.toJSONString());
                         
                         JSONObject packetForTraining = new JSONObject();
                         packetForTraining.put("TYPE", "TRAINING_SESSION");
                         packetForTraining.put("PATIENT_ID", patientID);
                         
                         clientConnected.send(packetForTraining.toJSONString());
                     }
                 }
                /*else if (packet.get("TYPE").equals("TRAINING")) {
                    
                    JSONObject packetForDoctor = new JSONObject();
                    packetForDoctor.put("TYPE", "EYE_TRACKER_READY");
                    packetForDoctor.put("DATA", packet.get("DATA"));
                    
                    doctorManager.sendPacket(packetForDoctor);
                }*/
                 else if (packetType.equals("TRAINING_SESSION") || 
                         packetType.equals("TRAINING_RESULT") || 
                         packetType.equals("CAL_END") || 
                         packetType.equals("CAL_QUAL")) {
                     
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
  
     @Override
     public void onClose(WebSocket client, int code, String reason, boolean remote) {
         System.out.println("EYE_TRACKER MANAGER Closed");
         clientConnected = null;
         eyeTrackerManager = null;
     }
 }
