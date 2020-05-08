 package mud.network.server.input.interpreter;
 
 import java.util.ArrayList;
 import mud.AreaManager;
 import mud.Player;
 import mud.geography.Area;
 import mud.geography.Direction;
 import mud.geography.Room;
 import mud.network.server.Connection;
 
 /**
  * World shaper commands allow a user to directly modify the structure of an
  * area, create new rooms, and modify the content and exits of those rooms.
  *
  * A world shaper can change data about the area or room that they are in. If
  * they create a new room, it must be adjacent to the room they are in somehow.
  *
  * Only adjacent rooms can be deleted.
  *
  * @author Japhez
  */
 public class WorldShaperInterpreter implements Interpretable {
 
     private AreaManager areaManger;
 
     public WorldShaperInterpreter(AreaManager areaManager) {
         this.areaManger = areaManager;
     }
 
     @Override
     public boolean interpret(Connection sender, ParsedInput input) {
         int wordCount = input.getWordCount();
         ArrayList<String> words = input.getWords();
         Room currentRoom = sender.getPlayer().getCurrentRoom();
         //More than 2 word commands
         if (wordCount > 2) {
             //Change current area's name - "area" "name" "new name"
             if (input.getFirstWord().equalsIgnoreCase("area")) {
                 if (words.get(1).equalsIgnoreCase("name")) {
                     String areaName = input.getWordsStartingAtIndex(2);
                     sender.getPlayer().getCurrentRoom().getArea().setName(areaName);
                     sender.sendMessage("This area is now know as " + areaName + ".");
                     return true;
                 }
             }
             if (input.getFirstWord().equalsIgnoreCase("room")) {
                 //Change current room's name - "room" "name" "new name"
                 if (words.get(1).equalsIgnoreCase("name")) {
                     String newName = "";
                     for (int i = 2; i < wordCount; i++) {
                         newName += words.get(i) + " ";
                     }
                     newName = newName.trim();
                     currentRoom.setName(newName);
                     sender.sendMessage("Room title updated.");
                     return true;
                 }
                 //Change current room's description - "room" "description" "new description"
                 if (words.get(1).equalsIgnoreCase("description")) {
                     String newDescription = input.getWordsStartingAtIndex(2);
                     newDescription = newDescription.trim();
                     currentRoom.setDescription(newDescription);
                     sender.sendMessage("Room description updated.");
                     return true;
                 }
             }
             //Link this room to an existing room - "link" "direction" "roomID"
             if (input.getFirstWord().equalsIgnoreCase("link") && (Direction.getDirectionFromString(words.get(1)) != null)) {
                 Direction direction = Direction.getDirectionFromString(words.get(1));
                 int roomID;
                 try {
                     roomID = Integer.parseInt(words.get(2));
                 } catch (NumberFormatException ex) {
                     sender.sendMessage("That's not a number.");
                     return true;
                 }
                 Room targetRoom = areaManger.getRoom(roomID);
                 if (currentRoom.linkToRoom(direction, targetRoom)) {
                     if (direction.equals(Direction.UP)) {
                         sender.sendMessage(targetRoom.getName() + " is now above you.");
                     } else if (direction.equals((Direction.DOWN))) {
                         sender.sendMessage(targetRoom.getName() + " now lies below you.");
                     } else {
                         sender.sendMessage(targetRoom.getName()+ " now lies to the " + direction + " of here.");
                     }
                     return true;
                 } else {
                     sender.sendMessage("Unable to link rooms.  Are both room exists available?");
                     return true;
                 }
             }
         }
         //Two word commands
         if (wordCount == 2) {
             //All other commands take a direction as their second argument, make sure it is valid
             Direction direction = Direction.getDirectionFromString(words.get(1));
             if (direction != null) {
                 //Create a new room and link to this - "create" "direction"
                 if (input.getFirstWord().equalsIgnoreCase("create")) {
                     //First check to make sure direction is clear
                     if (currentRoom.getRoomInDirection(direction) != null) {
                         sender.sendMessage("There's already a room in that direction.");
                         return true;
                     }
                     //Create the new room
                     Room newRoom = new Room(sender.getPlayer().getCurrentRoom().getArea(), areaManger);
                     //Link the room to this one
                     currentRoom.linkToRoom(direction, newRoom);
                     if (direction.equals(Direction.UP)) {
                         sender.sendMessage("Something appears above you.");
                     } else if (direction.equals(Direction.DOWN)) {
                         sender.sendMessage("Something appears below you.");
                     } else {
                         sender.sendMessage("Something interesting just appeared to the " + direction + "." + " (#" + newRoom.getRoomID() + ")");
                     }
                     return true;
                 }
                 //Unlink the passed direction - "unlink" "direction"
                 if (input.getFirstWord().equalsIgnoreCase("unlink")) {
                     currentRoom.unlinkExits(new Direction[]{direction});
                     if (direction.equals(Direction.UP)) {
                         sender.sendMessage("There's nothing overhead anymore.");
                     } else if (direction.equals(Direction.DOWN)) {
                         sender.sendMessage("There's nothing below you anymore.");
                     } else {
                         sender.sendMessage("Nothing to the " + direction + " of here seems interesting anymore.");
                     }
                     return true;
                 }
                 //Delete the room in the passed direction - "delete" "direction"
                 if (input.getFirstWord().equalsIgnoreCase("delete")) {
                     Room doomedRoom = currentRoom.getRoomInDirection(direction);
                     //Check to make sure that the room exists
                     if (doomedRoom != null) {
                         areaManger.deleteRoom(doomedRoom);
                         sender.sendMessage(doomedRoom.getName() + " vanishes into nothingness.");
                         return true;
                     } else {
                         sender.sendMessage("There's already nothing over there.  Mission accomplished I guess?");
                         return true;
                     }
                 }
             }
             //Info
             if (input.getWordAtIndex(1).equalsIgnoreCase("info")) {
                 //Get info about the room - "room" "info"
                 if (input.getFirstWord().equalsIgnoreCase("room")) {
                     sender.sendMessage("Room ID: #" + currentRoom.getRoomID());
                     return true;
                 }
                 //Get info about the area - "area" "info"
                 if (input.getFirstWord().equalsIgnoreCase("area")) {
                     Area area = currentRoom.getArea();
                    sender.sendMessage("Area" + "#" + area.getAreaID() + ": " + currentRoom.getArea());
                     return true;
                 }
             }
         }
         System.out.println(input.getOriginalInput() + input.getWordCount());
         return false;
     }
 
     /**
      * Creates a new room connected to the room the player is in. The direction
      * is the direction to the new room.
      *
      * @param player
      * @param direction the exit to the new room
      */
     public void createRoom(Player player, Direction direction) {
         //Verify that the exit of the current room doesn't already exist
         Room roomInDirection = player.getCurrentRoom().getRoomInDirection(direction);
         if (roomInDirection != null) {
             player.sendMessage("There's already a room to the " + direction);
         } else {
             Room newRoom = new Room(player.getCurrentRoom().getArea(), areaManger);
             player.getCurrentRoom().setRoomInDirection(direction, newRoom);
         }
     }
 }
