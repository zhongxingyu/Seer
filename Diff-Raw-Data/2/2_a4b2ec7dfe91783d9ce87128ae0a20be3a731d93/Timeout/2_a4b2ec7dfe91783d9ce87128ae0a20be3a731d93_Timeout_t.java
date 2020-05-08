 package edu.drexel.cs544.mcmuc.actions;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONObject;
 
 import edu.drexel.cs544.mcmuc.Channel;
 import edu.drexel.cs544.mcmuc.Controller;
 
 /**
  * The timeout action is used by clients to find channels clients are no longer
  * actively using to chat, and thus whose allocated resources can be freed.
  * The timeout is used to ask the other clients which of the provided rooms they
  * are using.
  * 
  * The JSON format of a Timeout is {'uid':'<uid>','action':'timeout','rooms':'<rooms>'}
  * 
  */
 public class Timeout extends RoomAction {
 	public static final String action = "timeout";
 	
     /**
      * The timeout action must carry a list of rooms that receiving clients may
      * reply with a preserve action to indicate continuing use of the room
      * @param rooms List<Integer> the list of the rooms
      */
     public Timeout(List<Integer> rooms) {
         super(rooms, Timeout.action);
     }
 
     /**
      * Deserializes JSON into a Timeout object
      * @param json the JSON to deserialize
      */
     public Timeout(JSONObject json) {
         super(json, Timeout.action);
     }
 
     @Override
     public void process(Channel channel) {
         class Runner implements Runnable {
             Timeout message;
 
             Runner(Timeout m) {
                 message = m;
             }
 
             public void run() {
             	Set<Integer> roomPortsInUse = Controller.getInstance().roomPortsInUse;
             	roomPortsInUse.retainAll(message.getRooms());
            	//TODO Reset secondary timer for each room in roomPortsInUse
                 if (!roomPortsInUse.isEmpty()) {
                     Preserve reply = new Preserve(new ArrayList<Integer>(roomPortsInUse));
                     Controller.getInstance().send(reply);
                 }
             }
         }
         Thread t = new Thread(new Runner(this));
         t.start();
     }
 }
