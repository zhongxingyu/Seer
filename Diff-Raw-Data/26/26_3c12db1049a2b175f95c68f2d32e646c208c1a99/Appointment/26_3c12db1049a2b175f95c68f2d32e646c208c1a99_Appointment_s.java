 package no.ntnu.fp.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import no.ntnu.fp.Client;
 import no.ntnu.fp.model.Answer;
 
 public class Appointment {
 	private int id;
 	private String title, place, description;
 	// private Date startDate, endDate;
 	private Room room;
 	private boolean isRoom;
	public static GregorianCalendar starting;
	public static GregorianCalendar ending;
 
 	public Appointment(String title, String description, GregorianCalendar startDate,
 			GregorianCalendar endDate) {
 		this.title = title;
 		this.description = description;
 
 		 this.starting = startDate;
 		 this.ending = endDate;
 	}
 
 	public Appointment(String title, String place, String description,
 			GregorianCalendar startDate, GregorianCalendar endDate) {
 		this(title, description, startDate, endDate);
 		this.place = place;
 	}
 
 	public Appointment(String title, Room room, String description,
 			GregorianCalendar startDate, GregorianCalendar endDate) {
 		this(title, description, startDate, endDate);
 		this.room = room;
 	}
 
 	public static Appointment loadAppointment(int id) {
 		return Client.app1;
 	}
 
 	public static ArrayList<Appointment> loadAppointments(User user,
 			GregorianCalendar startDate, GregorianCalendar endDate) {
 		ArrayList<Appointment> appointments = new ArrayList<Appointment>();
 		appointments.add(Client.app1);
 		appointments.add(Client.app2);
 		return appointments;
 	}
 
 	public void saveAppointment() {
 
 	}
 
 	private int[] getStatus() {
 		int[] status = new int[3];
 
 		List<Invitation> invitations = Invitation.loadInvitations(this.id);
 
 		for (Invitation invitation : invitations) {
 			switch (invitation.getAnswer()) {
 			case YES:
 				status[0] += 1;
 			case NO:
 				status[1] += 1;
 			case AWAITING_REPLY:
 				status[2] += 1;
 			}
 		}
 		return status;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getPlace() {
 		return place;
 	}
 
 	public void setPlace(String place) {
 		this.place = place;
 	}
 
 	public Room getRoom() {
 		return room;
 	}
 
 	public void setRoom(Room room) {
 		this.room = room;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public GregorianCalendar getStartDate() {
 		return starting;
 	}
 	public void setStartDate(GregorianCalendar gregCalendar){
 		this.starting=gregCalendar;
 	}
 	public void setStartDate(Date startDate) {
 		this.starting =  new GregorianCalendar(startDate.getYear(),
 				startDate.getMonth() + 1, startDate.getDate());
 		;
 	}
 
 	public GregorianCalendar getEndDate() {
 		return ending;
 	}
 	public void setEndDate(GregorianCalendar gregCalendar){
 		this.ending= gregCalendar;
 	}
 	public void setEndDate(Date endDate) {
 		this.ending = new GregorianCalendar(endDate.getYear(),
 				endDate.getMonth() + 1, endDate.getDate());
 	}
 }
