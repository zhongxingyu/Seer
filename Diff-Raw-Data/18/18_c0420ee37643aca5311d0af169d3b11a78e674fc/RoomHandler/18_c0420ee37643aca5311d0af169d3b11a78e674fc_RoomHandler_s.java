 package no.ntnu.fp.common.handlers;
 
 import no.ntnu.fp.client.Connection;
 import no.ntnu.fp.common.Constants;
 import no.ntnu.fp.common.model.Room;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.Console;
 import java.io.IOException;
 
 public class RoomHandler {
 
     private int id;
 
     public RoomHandler(int id){
         this.id = id;
     }
 
     public static Room getRoom(int id){
         if (Constants.USE_SERVER) return getRoomFromServer("id=" + id);
         return new Room(1, "Drivhuset", "its", 100);
     }
 
     private static Room getRoomFromServer(String arg) {
         JSONObject json = new JSONObject();
         try {
             Connection conn = new Connection();
             try {
                 json.put("key", "room");
                 json.put("action", "get");
                 json.put("argument", arg);
                 conn.send(json);
                 JSONObject object = new JSONObject(conn.receive());
                Room room = new Room(object);
                 conn.close();
                 return room;
 
             } catch (JSONException e) {
                 e.printStackTrace();
                 conn.close();
             }
         } catch (IOException e) {
             e.printStackTrace();
 
         }
         return null;
     }
 }
