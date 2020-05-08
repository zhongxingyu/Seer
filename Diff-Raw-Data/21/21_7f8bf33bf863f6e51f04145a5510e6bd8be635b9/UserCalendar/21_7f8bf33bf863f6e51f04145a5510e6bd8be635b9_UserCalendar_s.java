 package tim.view.calendar;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Insets;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import sun.security.jca.GetInstance.Instance;
 import tim.application.Config;
 import tim.application.exception.ExceptionFormatter;
 import tim.application.exception.PersistanceException;
 import tim.application.exception.ResourceNotFoundException;
 import tim.application.utils.DateHelper;
 import tim.application.utils.ErrorHandler;
 import tim.controller.UserCalendarController;
 import tim.model.Appointment;
 import tim.model.AppointmentModel;
 import tim.model.Element;
 import tim.model.Employee;
 import tim.model.EmployeeModel;
 
 
 public class UserCalendar extends JPanel{
 	
 	private ArrayList<EventButton> eventButtons;
 	private int minutesInDay;
 	private Date beginFindDate;
 	private Date endFindDate;
 		
 	public UserCalendar(Employee employee){
 		//Layout du calendrier
 		FlowLayout layout = new FlowLayout();
 		layout.setAlignment(FlowLayout.LEFT);
 		layout.setHgap(0);
 		layout.setVgap(0);
 		setLayout(layout);
 		this.setOpaque(false);
 		
 		this.minutesInDay = (Config.CALENDAR_DAY_END - Config.CALENDAR_DAY_START) * 60;
 		
 		//Initialisation des collections
 		this.eventButtons = new ArrayList<EventButton>();
 		
 		//Initialisation du controller
 		UserCalendarController controller = new UserCalendarController();
 
 		
 		Date begin = null, end = null;
 		try {
 			this.beginFindDate = DateHelper.StringToDate("2011-01-01", Config.DATE_FORMAT_SHORT);
 			this.endFindDate = DateHelper.StringToDate("2011-06-10", Config.DATE_FORMAT_SHORT);
 		} catch (ParseException ex) {
			ExceptionFormatter.format(ex, this.getClass().getName(), "constructor");
 		}
 		
 		//eventButtons = controller.getEventButtons(employee, beginFindDate, endFindDate);
 		try {
 			eventButtons = controller.getAllButtons(controller.getEventButtons(employee, beginFindDate, endFindDate));
 		} catch (PersistanceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		for (EventButton btn : eventButtons) {
 			add(btn);
 		}
 		
 	}
 	
 	public void eventSizing(Dimension d){
 
 		for(EventButton btn : eventButtons){
 			
 			Dimension btnDimension = new Dimension((int) (/*-33+*/(btn.getDuration()*d.getWidth())/minutesInDay), (int) d.getHeight());
 			
 			//il faut faire les deux op√©ration pour qu'il n'y ait pas de bug d'affichage
 			btn.setSize(btnDimension);
 		    btn.setPreferredSize(btnDimension);
 
 		}
 	}
 
 	public void paintComponent(Graphics g) {
 	    // Appel de la m√©thode de la classe JPanel
 	    super.paintComponent(g);
 	    
 	    eventSizing(new Dimension(this.getWidth(),this.getHeight()));
 	    //System.out.println("Event: " + this.getWidth() + "x" + this.getHeight());
 	}
 	
 	public void validate(){
 		
 		
 		this.setSize(CalendarContainer.getJLayerPaneDimension());
 		System.out.println("1.1.1= " + this.getWidth() + "x" + this.getHeight());
 	}
 }
