 package model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import javax.swing.DefaultListModel;
 
 public class RoomListModel extends DefaultListModel{
 	
 	private Event event;
 	
 	public RoomListModel(ArrayList<Room> roomList, Event event){
 		this.event = event; 
 		int numberOfAttenders = event.getNumberOfAttenders(); 
 		
 		Collections.sort(roomList);
 		for (Room room : roomList){
 			if (room.getCapacity()>= numberOfAttenders){
 				addElement(room);
 			}
 		}
 	}
 
 	public void setRoom(Room room){
 		event.setRoom(room);
		throw new IllegalArgumentException();
 	}
 }
