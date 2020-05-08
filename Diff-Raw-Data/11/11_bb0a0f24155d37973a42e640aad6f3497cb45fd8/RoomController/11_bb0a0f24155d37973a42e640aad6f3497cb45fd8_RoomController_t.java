 package sse.client.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import sse.ejb.dao.RoomDAO;
 import sse.model.Room;
 
 @ManagedBean(name="roomCtrl")
 @SessionScoped
 public class RoomController {
 	
 	@EJB
 	private RoomDAO roomDAO;
 	private String filterRoomTxt;
 	
 	public RoomController() {
 		rooms = new ArrayList<Room>();
 
 	}
 
 	private List<Room> rooms;
 	private Room selectedRoom;
 
 	public List<Room> getRooms() {
 		return rooms;
 	}
 
 	public void setRooms(List<Room> rooms) {
 		this.rooms = rooms;
 	}
 
 	public Room getSelectedRoom() {
 		return selectedRoom;
 	}
 
 	public void setSelectedRoom(Room selectedRoom) {
 		this.selectedRoom = selectedRoom;
 	}
 
 	public void create() {
 		this.selectedRoom = new Room();
 	}
 
 	public void save() {
 		roomDAO.save(selectedRoom);
		load();
 	}
 
 	public void delete() {
 		roomDAO.delete(selectedRoom);
 		this.selectedRoom = new Room();
		load();
 	}
 	
 	public List<Room> filterRooms() {
 		if (!filterRoomTxt.equals("")) {
 			Integer txt = Integer.valueOf(filterRoomTxt).intValue();
 			return rooms = roomDAO.findByNamedQuery("filterRooms", txt, txt);
 		} else {
 			return rooms = roomDAO.findAll();
 		}
 	}
 	
 	public String load() {
 		
 		rooms = roomDAO.findAll();
 		
 		return "room.jsf";
 	}
 
 	public void setFilterRoomTxt(String filterRoomTxt) {
 		this.filterRoomTxt = filterRoomTxt;
 	}
 
 	public String getFilterRoomTxt() {
 		return filterRoomTxt;
 	}
 
 }
