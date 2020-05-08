 package client.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import chronos.Person;
 import chronos.Room;
 import client.ClientController;
 import client.gui.view.ChronosWindow;
 import client.gui.view.RoomBookingWindow;
 import client.gui.view.calendarWindowHelper.RoomCheckBox;
 import events.CalEvent;
 import events.NetworkEvent;
 import events.QueryEvent;
 import events.QueryEvent.QueryType;
 
 public class RoomBookingModel extends ChronosModel {
 	private String roomName;
 	private RoomBookingWindow view;
 	private ArrayList<Room> rooms;
 
 	public void recieveNetworkEvent(NetworkEvent event) {
 		addRooms((QueryEvent) event);
 	}
 
 	private void addRooms(QueryEvent event) {
		view.getRoomPanel().removeAll();
 		
 		for (Room room : (ArrayList<Room>) event.getResults()) {
 			System.out.println(room.toString());
 			rooms.add(view.addRoom(room));
 		}
 		view.getFrame().revalidate();
 	}
 
 	public RoomBookingModel(ClientController controller) {
 		super(controller, ChronosType.ROOM_BOOK);
 		rooms = new ArrayList<Room>();
 		// TODO Auto-generated constructor stub
 	}
 
 	public void getRooms() {
 		fireNetworkEvent(new QueryEvent(QueryType.ROOMS).addCalEvent(new CalEvent("", new Date(), 1829, new Person("penis"), "")));
 	}
 
 	@Override
 	public void receiveNetworkEvent(NetworkEvent event) {
 		addRooms((QueryEvent) event);
 	}
 
 	@Override
 	public void setView(ChronosWindow view) {
 		this.view = (RoomBookingWindow) view;
 	}
 
 	
 	public String getRoomName() {
 		return roomName;
 	}
 
 	public void setRoomName(String roomName) {
 		this.roomName = roomName;
 	}
 
 }
