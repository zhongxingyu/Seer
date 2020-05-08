 package com.fastquake.textbasedgame.room;
 
 import java.util.ArrayList;
 
 
 import com.fastquake.textbasedgame.Direction;
 import com.fastquake.textbasedgame.gameobject.GameObject;
 
 public class Room {
 	private Door[] doors;
 	private int doorCount;
 	protected int id;
	protected ArrayList<GameObject> objects;
 	
 	protected Room(){
 		doors = new Door[4]; //Initialize the Door array
 		objects = new ArrayList<GameObject>();
 		doorCount = 0;
 	}
 	
 	/**
 	 * Prints the description of the room, specifically the items, doors, and other dynamic elements.
 	 * Room subclasses will override this to add their own static descriptive text.
 	 */
 	public void describe(){
 		System.out.println();
 		String doorString = "There is a door to the "; //String to describe the doors
 		for(int i=0,k=0;i<4;i++){
 			if(doors[i] != null) //If a door occupies this slot (direction)
 				k++; //Increment k
 			if(k>1){ //If k is greater than 1, then there are multiple doors, so change the door string
 				doorString = "There are doors to the ";
 				break;
 			}
 		}
 		for(int i=0,appendedDoors=0;i<4;i++){
 			String iDir = Direction.values()[i].toString(); //Human-readable text representing the compass direction
 			if(doors[i] != null){
 				doorString += iDir;
 				appendedDoors++;
 				if(appendedDoors == doorCount){
 					doorString += ".";
 					break;
 				}else if(doorCount != 2) //no commas if there are only 2 doors
 					doorString += ", ";
 			}
 			if(appendedDoors == doorCount-1 && doors[i+1] != null && doorCount != 1){
 				if(doorCount == 2) //Without commas there are no spaces, so put one
 					doorString += " ";
 				doorString += "and ";
 			}
 		}
 		System.out.println(doorString);
 		//TODO: Print items
 	}
 	
 	public int getId(){
 		return id;
 	}
 	
 	/**
 	 * Adds a door to the Room
 	 * @param targetId The ID of the target room that the door leads to
 	 * @param direction The compass direction of the door
 	 */
 	protected void addDoor(Direction direction, int targetId){
 		this.doors[direction.ordinal()] = new Door(targetId,false);
 		this.doorCount++;
 	}
 	
 	/**
 	 * Same as addDoor, but with the option to lock the door upon adding
 	 * @param direction
 	 * @param targetId
 	 * @param locked
 	 */
 	protected void addDoor(Direction direction, int targetId, boolean locked){
 		this.doors[direction.ordinal()] = new Door(targetId,locked);
 		this.doorCount++;
 	}
 	
 	protected Door getDoor(Direction direction){
 		return this.doors[direction.ordinal()];
 	}
 	
 	/**
 	 * Gets the target of a door in a specified direction, if one exists
 	 * @param direction The direction of the door
 	 * @return A Room object if there is a door in the specified direction, otherwise null
 	 */
 	public Room getDoorTarget(Direction direction, RoomManager rm){
 		if(doors[direction.ordinal()] != null)
 			return rm.getRoomById(doors[direction.ordinal()].getTargetId());
 		else
 			return null;
 	}
 	
 	/**
 	 * Gets an object in the room by its name(s)
 	 * @param requestedName The player-input name that is to be searched for
 	 * @return A GameObject in the room of that name
 	 */
 	public GameObject getObjectByName(String requestedName){
 		for(int i=0;i<objects.size();i++){
 			GameObject currentObject = objects.get(i);
 			for(int k=0;k<currentObject.getNames().length;k++){
 				if(currentObject.getNames()[k].equals(requestedName))
 					return currentObject;
 			}
 		}
 		return null;
 	}
 }
