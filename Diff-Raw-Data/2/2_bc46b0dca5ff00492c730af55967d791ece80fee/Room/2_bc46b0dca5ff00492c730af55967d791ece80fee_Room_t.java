 package model;
 
 public class Room implements Comparable<Room>{
 	
 	private String roomNr; 
 	private int capacity; 
 	
 	public Room(String roomNr, int capacity){
 		if (roomNr.equals(null)){
 			throw new IllegalArgumentException("roomNr can not be null");
 		}else if (capacity<1){
 			throw new IllegalArgumentException("capacity must be a positive integer");
 		}
 		this.roomNr = roomNr; 
 		this.capacity = capacity;
 	}
 
 	public String getRoomNr() {
 		return roomNr;
 	}
 
 	public int getCapacity() {
 		return capacity;
 	}
 
 	@Override
 	public int compareTo(Room room) {
		return (room.capacity-this.capacity);
 	}
 	
 	public String toString(){
 		return "Room " + roomNr + " - Capacity " + capacity;
 	}
 
 }
