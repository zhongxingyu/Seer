 package tim.view.calendar.test;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 
 import javax.swing.JPanel;
 
 import tim.application.Config;
 import tim.application.GlobalRegistry;
 import tim.application.Resizer;
 import tim.application.exception.PersistanceException;
 import tim.application.exception.ResourceNotFoundException;
 import tim.model.Appointment;
 import tim.model.AppointmentModel;
 import tim.model.Element;
 import tim.model.Employee;
 import tim.view.ChildView;
 import tim.view.ParentView;
 import tim.view.calendar.test.EventButton;
 import tim.view.dialog.appointment.AppointmentDialog;
 
 public class UserCalendar extends JPanel implements ChildView {
 
 	private ArrayList<EventButton> eventButtons;
 	private ArrayList<Appointment> appointments;
 	private AppointmentDialog appointmentDialog;
 	private ParentView parentView;
 	private Dimension dimension;
 	private Employee employee;
 	private Date date;
 	
 	public UserCalendar() {
 		GlobalRegistry.resizer.addObserver(this);
 		try {
 			GlobalRegistry.mvcLinker.addObserverToModel("AppointmentModel", this);
 		} catch (ResourceNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		FlowLayout flowLayout = new FlowLayout();
 		flowLayout.setAlignment(FlowLayout.LEFT);
 		flowLayout.setHgap(0);
 		flowLayout.setVgap(0);
 		setLayout(flowLayout);
 		setOpaque(false);
 	}
 	
 	public void load() {
 		eventButtons = (ArrayList<EventButton>) ((CalendarContainer) parentView).getButtonsForCalendar(appointments);
 
 		
 		for (EventButton eventButton : eventButtons) {
 			final Appointment appointment = eventButton.getAppointment();
 			final Date begin = eventButton.getBegin();
 			final Date end = eventButton.getEnd();
 			
 			eventButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (appointment != null) {
 						showDialog(appointment);
 					}
 					else {
 						showDialog(new Appointment(begin, end, null, employee, null));
 					}
 				}
 			});
 			add(eventButton);
 		}
 	}
 	
 	public void eventSizing(){
 
 		Dimension btnDimension;
 		for(EventButton eventButton : eventButtons){
 			int x, y;
 
 			x = (int) ((eventButton.getDuration()*dimension.getWidth())/((Config.CALENDAR_DAY_END - Config.CALENDAR_DAY_START) * 60));
 			y = (int)dimension.getHeight();
 			
 			btnDimension = new Dimension(x, y);
 			
 			eventButton.setSize(btnDimension);
 		    eventButton.setPreferredSize(btnDimension);
 		}
 	}
 	
 	
 	public void showDialog(Appointment appointment) {
 		appointmentDialog = new AppointmentDialog(appointment);
 		appointmentDialog.setModal(true);
 		appointmentDialog.setResizable(false);
 		appointmentDialog.pack();
 		appointmentDialog.setLocationRelativeTo((Component) parentView);
 		appointmentDialog.setVisible(true);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void update(Observable o, Object arg) {
 		if (o instanceof Resizer) {
 			this.dimension = (Dimension) arg;
 			repaint();
 		}
 		else if (o instanceof AppointmentModel) {
 			reload(date);
 		}
 	}
 	
 	public void reload(Date date) {
 		this.date = date;
 		this.removeAll();
 		employee = ((CalendarContainer) parentView).getData(employee, date);
 		appointments = employee.getCalendar().getAppointments();
 		load();
 		repaint();
 	}
 
 	@Override
 	public void setParentView(ParentView view) {
 		this.parentView = view;
 	}
 
 	@Override
 	public Object getData() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setData(Object value) {
 		Employee employee = (Employee) value;
 		this.employee = employee;
 		appointments = employee.getCalendar().getAppointments();
 	}
 	
 	public void validate(){
 		dimension = getSize();
 	    setSize(dimension);
 	    setPreferredSize(dimension);
 	    eventSizing();
 	}
 	public void paintComponent(Graphics g) {
 	    super.paintComponent(g);
 	    validate();
 	}
 
 }
