 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class World {
   private final List<Room> rooms;
 
   private final Player player;
 
   public World (List<Room> rooms, Player player) {
     this.rooms = Collections.unmodifiableList(new ArrayList<>(rooms));
     this.player = player;
   }
 
   public List<Room> getRooms () {
     return this.rooms;
   }
 
   public Player getPlayer () {
     return this.player;
   }
 
   public Room getCurrentRoom () {
     for (Room room : this.rooms) {
      if (room.getId().equals(this.player.getRoomId())) {
         return room;
       }
     }
 
     return null;
   }
 
   public World apply (Transform transform) {
     List<Room> rooms = new ArrayList<>(this.rooms.size());
 
     for (Room room : this.rooms) {
       rooms.add(room.apply(transform));
     }
 
     return new World(rooms, this.player.apply(transform));
   }
 }
