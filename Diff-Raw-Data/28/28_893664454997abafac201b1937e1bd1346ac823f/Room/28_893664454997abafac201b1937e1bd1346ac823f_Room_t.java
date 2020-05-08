 import java.util.*;
 
 /**
  * Class Room - a room in an adventure game.
  *
  * This class is part of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  *
  * A "Room" represents one location in the scenery of the game.  It is 
  * connected to other rooms via exits.  The exits are labelled north, 
  * east, south, west.  For each direction, the room stores a reference
  * to the neighboring room, or null if there is no exit in that direction.
  * 
  * @author  Michael Kölling and David J. Barnes
  * @version 2011.07.31
  */
 public class Room 
 {
     private String description;
     private Map<String,Room> exits;
     private Map<String, Item> items;
 
     /**
      * Create a room described "description". Initially, it has
      * no exits. "description" is something like "a kitchen" or
      * "an open court yard".
      * @param description The room's description.
      */
     public Room(String description) 
     {
         exits = new HashMap<String, Room>();
         items = new HashMap<String, Item>();
         this.description = description;
     }
     
     public void setDescription(String description) {
         this.description = description;
     }
     
     public Room getExit(String direction) {
         return exits.get(direction);
     }
     
     public boolean hasExit(String direction) {
         return exits.get(direction)!=null;
     }
     
     
     public void addExit(String direction, Room nextRoom) {
         exits.put(direction, nextRoom);
     }
     
     public void addItem(Item item) {
         items.put(item.getName(), item);
     }
     
     /**
      * removes the item from the room, returns for the player's inventory
      */
     public Item getItem(String itemToGet) {
         Item itemToReturn = items.get(itemToGet);
        //item = null;
         return itemToReturn;
     }
     
     
     public String directionList() {
         StringBuffer buff = new StringBuffer();
         for(String exitDirection : exits.keySet()) {
             buff.append(exitDirection+" ");
         }
         return buff.toString();
     }
 
     /**
      * @return The description of the room.
      */
     public String getDescription()
     {
         return description;
     }
 }
