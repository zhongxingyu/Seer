 import java.util.HashMap;
 import java.util.Map.Entry;
 
 
 
 /*
  * Class Room - a room in an adventure game.
  *
  * This class is the main class of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  *
  * A "Room" represents one location in the scenery of the game.  It is 
  * connected to other rooms via exits.  The exits are labelled north, 
  * east, south, west.  For each direction, the room stores a reference
  * to the neighboring room, or null if there is no exit in that direction.
  * 
  * @version 2.0 (1/28/13)
  */
 
 class Room 
 {
     private String description;
     private HashMap<Direction, Room> exits;
     
 
     /**
      * Create a room described "description". Initially, it has
      * no exits. "description" should be able to follow the words 
      * "You are in" and is something like "a kitchen" or "an 
      * open court yard".
      */
     public Room(String description){
         setDescription(description);
         exits = new HashMap<Room.Direction, Room>();
     }
     
     /**
      * An empty Room object, for use instead of 'null' in the setExits()
      * method. Since a hashmap is used, this will help avoid NPEs.
      * @return An empty Room object.
      */
     public static Room empty(){
    	return new Room(""); 
     }
 
     /**
      * @returns the description of the room
      */
     public String getDescription(){
         return description;
     }
     
     /** This method creates a string returnable, concatenates all exit's directions and returns the variable. 
      * 
      * @return String representation of a list of existing exit *directions*
      */
     public String getExitLocationsString(){
     	String str = "Exits: ";
     	for(Direction exitDir : exits.keySet()){
     		str += exitDir.toString()+" ";
     	}
     	return str;
     }
     
     public String getLongDescription(){
     	return "You are "+description+".\n"+getExitLocationsString();
     }
     
     /**
      * This may return a null value. Use hasExit() to verify if an
      * exit has been defined already.
      */
     public Room getExit(Direction dir){
     	return exits.get(dir);
     }
     
     public boolean hasExit(Direction dir){
     	return !(exits.get(dir)==null||exits.get(dir).isEmpty());
     }
     
     public boolean isEmpty(){
     	return description==""&&(exits.isEmpty()||exits==null);
     }
     
     public Direction isAnExit(Room room){
     	if(exits.containsValue(room)){
     		for(Entry<Direction, Room> entry : exits.entrySet()){
     			if(entry.getValue().equals(room)) return entry.getKey();
     		}
     	}
     	return Direction.NULL; 
     	/* returns null in case I want to read the result of this method 
     	 * in a situation where I don't want an actual null value.*/
     }
 	
     public void setDescription(String description){
     	this.description = description;
     }
     
     public void setExits(Room north, Room east, Room south, Room west, Room up, Room down){
		if(north!=null) setExit(north, Direction.NORTH);
		if(east!=null) setExit(east, Direction.EAST);
		if(south!=null) setExit(south, Direction.SOUTH);
		if(west!=null) setExit(west, Direction.WEST);
		if(up!=null) setExit(up, Direction.UP);
		if(down!=null) setExit(down, Direction.DOWN);
 	}
     
     /**
      * This calls the Hashmap.put() method, meaning it will
      * override any existing exit in the same key (direction).
      * 
      * Nulls should be okay as parameters since they will be
      * caught here and replaced by Room.empty()
      */
 	public void setExit(Room exit, Direction dir){
 		exits.put(dir, exit==null ? Room.empty() : exit);
 	}
 	
 	public enum Direction{
 		NORTH, EAST, SOUTH, WEST, UP, DOWN, NULL;
 		
 		@Override
 		public String toString(){
 			return super.toString().toLowerCase();
 		}
 	}
 
 }
