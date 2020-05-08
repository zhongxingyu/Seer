 package tim.view.calendar.test;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.util.Date;
 
 import javax.swing.JButton;
 
 import tim.application.utils.DateHelper;
 import tim.model.Appointment;
 import tim.model.Employee;
 
 public class EventButton extends JButton {
 	private Color color = null;
 	private String title;
 	private int duration;
 	private Date begin, end;
 	private Appointment appointment;
 	
 	private Employee employee;
 
 	public EventButton(String title, Date begin, Date end, int duration, Color color) {
 		
 		this.duration = duration;
 		this.title = title;
 		this.color = color;
 		this.begin = begin;
 		this.end = end;
 
 		this.setMargin(new Insets(0, 2, 0, 2));
 		this.setOpaque(true);
 		this.setRolloverEnabled(false);
 		this.setFocusPainted(false);
 		this.setBackground(color);
 		this.setText(title);
 
 	}
 	
 	public EventButton(Appointment appointment) {
 
 		this.appointment = appointment;
 		this.duration = DateHelper.DateDiff(appointment.getBegin(), appointment.getEnd());
 		this.title = "<html>" + appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName() + "</html>";
 		Employee e = (Employee) appointment.getEmployee();
 		this.color = e.getCalendar().getColor();
 		this.begin = appointment.getBegin();
 		this.end = appointment.getEnd();
 
 		this.setMargin(new Insets(0, 2, 0, 2));
 		this.setOpaque(true);
 		this.setRolloverEnabled(false);
 		this.setFocusPainted(false);
 		this.setBackground(color);
 		this.setText(title);
 		this.setToolTipText("adsfasd");
 
 	}
 	
 	public EventButton(Employee employee, Date begin, Date end){
 		this.employee = employee;
 		this.begin = begin;
 		this.end = end;
 		this.duration = DateHelper.DateDiff(begin, end);
 		this.title = "";
 		this.color = null;
 		this.appointment = null;
 		
 		this.setBorderPainted(false);
         this.setBackground(new Color(1f,1f,1f,0));
         this.setOpaque(false);
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 	public int getDuration() {
 		return duration;
 	}
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 	
 	public Color getColor() {
 		return color;
 	}
 	public void setColor(Color color) {
 		this.color = color;
 	}
 	
 	public Date getBegin() {
 		return begin;
 	}
 	public Date getEnd() {
 		return end;
 	}
 	public Appointment getAppointment() {
 		return appointment;
 	}
 	public Employee getEmployee() {
 		return employee;
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		
 	}
 	
 }
