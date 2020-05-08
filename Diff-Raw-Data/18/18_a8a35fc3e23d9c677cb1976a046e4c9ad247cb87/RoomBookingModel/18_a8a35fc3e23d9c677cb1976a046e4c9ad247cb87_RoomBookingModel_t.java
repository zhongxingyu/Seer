 package client.model;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.DefaultListModel;
 
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
 		for (int i = 0; i < view.getRoomPanel().getModel().getSize();) {
 			((DefaultListModel<Room>) view.getRoomPanel().getModel()).remove(0);
 		}
 
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
 		fireNetworkEvent(new QueryEvent(QueryType.ROOMS).addCalEvent(new CalEvent(view.getFrame().getEventModel().getStartDate(), view.getFrame().getEventModel().getDuration())));
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
 
 	public Room getBestRoom() {
 		int participants;
 		try {
 			participants = view.getFrame().getEventModel().getParticipants().size() + 1;
 		} catch (NullPointerException e) {
 			participants = 1;
 		}
 
		// a little ghetto i knowz.
 		Room best = rooms.get(0);
 		double bestDiv = rooms.get(0).getCapacity() / participants;
		boolean storNok = (bestDiv > 1 ? true : false);
 		for (Room r : rooms) {

			double div = r.getCapacity() / participants;
 			if (r.getCapacity() >= participants) {
				storNok = true;
 				if (div < bestDiv)
					bestDiv = div;
				best = r;
			} else {
				if (!storNok && div > bestDiv) {
					bestDiv = div;
 					best = r;
				}
 			}
 		}
 		return best;
 	}
 }
