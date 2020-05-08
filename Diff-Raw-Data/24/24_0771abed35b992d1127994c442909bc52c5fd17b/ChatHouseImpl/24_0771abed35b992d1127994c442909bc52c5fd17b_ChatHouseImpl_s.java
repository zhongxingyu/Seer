 package co.speechat.data;
 
 import java.util.HashMap;
 
 /**
  * TODO: Document me
  * Author: Iurii Lytvynenko
  */
 public class ChatHouseImpl implements ChatHouse {
     private HashMap<String, ChatRoom> rooms = new HashMap<String, ChatRoom>();
     private static final ChatHouseImpl instance = new ChatHouseImpl();
     
     public static ChatHouse get() {
         return instance;
     }
     
     @Override
     public ChatRoom getRoom(String room) {
         return rooms.get(room);
     }
 
     @Override
     public void createRoom(String room) {
         rooms.put(room, new ChatRoomImpl(room, new PasswordNameGenerator()));
     }
 }
