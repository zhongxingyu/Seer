 package tim.model;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 public class Calendar extends Element {
 	
 	private Color color;
 	private ArrayList<Appointment> appointments;
 
 	public Calendar(long id, Color color) {
 		super(id);
 		this.color = color;
 		appointments = new ArrayList<Appointment>();
 	}
 	
 	public Calendar(long id, Color color, ArrayList<Appointment> appointments) {
 		super(id);
 		this.color = color;
 		this.appointments = appointments;
 	}
 	
 	public ArrayList<Appointment> getAppointments() {
 		return appointments;
 	}
 	
 	public Color getColor() {
 		return color;
 	}
 
 }
