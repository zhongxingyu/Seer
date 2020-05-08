 package client.gui.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import client.ClientMain;
 import client.gui.week.WeekView;
 import client.model.CalendarModel;
 import client.model.MeetingModel;
 import client.model.NotificationType;
 
 
 public class MainPanel extends JPanel implements PropertyChangeListener {
 
 	private static final long serialVersionUID = -6453034572305492442L;
 
 	private final JTabbedPane optionTabbedPane;
 	private NewAppointmentPanel newAppointmentPane = null;
 	private int unreadNotifications;
 	private WeekView weekView;
 
 	public MainPanel() {
 		super(new BorderLayout());
		
 		unreadNotifications = 0;
 		
 		optionTabbedPane = new JTabbedPane();
 		JTabbedPane calendarTabbedPane = new JTabbedPane();
 		
 		HovedPanel hp = new HovedPanel();
 		
 		VarselPanel vp = new VarselPanel();
 
 		AndrePanel akp = new AndrePanel();
 		
 		optionTabbedPane.addTab("Hoved", hp); //TODO
 		optionTabbedPane.addTab("Andre Kalendre", akp); //TODO
 		optionTabbedPane.addTab("Varsler (0)", vp); //TODO
 		
 		weekView = new WeekView();
		weekView.addPropertyChangeListener(this);
 		calendarTabbedPane.addTab("Uke", weekView);
 		calendarTabbedPane.addTab("Mned", new JPanel()); //TODO
 		
 		//TODO This should probably be done in a better manner
 		optionTabbedPane.setPreferredSize(new Dimension(330,calendarTabbedPane.getPreferredSize().height));
 		
 		this.add(optionTabbedPane,BorderLayout.CENTER);
 		this.add(calendarTabbedPane, BorderLayout.EAST);
 
 		// Initialize notifications
 		vp.initializeList(ClientMain.getActiveUser().getNotifications());
 		
 		//Listeners
 		ActionListener listener = new NewAppointmentListener();
 		hp.getNewAppointmentButton().addActionListener(listener);
 		akp.getNewAppointmentButton().addActionListener(listener);
 		vp.getNewAppointmentButton().addActionListener(listener);
 		vp.addPropertyChangeListener(this);
 		
 		
 	}
 	
 	private void OpenNewAppointment() {
 		OpenAppointment(MeetingModel.newDefaultInstance());
 	}
 	
 	private void OpenAppointment(MeetingModel meeting) {
 		if (newAppointmentPane == null) {
 			newAppointmentPane = new NewAppointmentPanel(meeting);
 			optionTabbedPane.addTab("NAVN?", newAppointmentPane);
 		}
 		optionTabbedPane.setSelectedComponent(newAppointmentPane);
 	}
 	
 	
 	class NewAppointmentListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			OpenNewAppointment();
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		System.out.println(evt.getPropertyName());
 		if (evt.getPropertyName() == NotificationList.NOTIFICATION_COUNT) {
 			optionTabbedPane.setTitleAt(2, "Varsel (" + ((Integer)evt.getNewValue()) + ")");
 		} else if (evt.getPropertyName() == NotificationList.NOTIFICATION_CLICKED) {
 			OpenAppointment((MeetingModel)evt.getNewValue());
 		} else if (evt.getPropertyName() == WeekView.WEEKCLICK) {
 			CalendarModel calMod = weekView.getCalendarModel();
 		}
	}
	
	public boolean calModHasAppointmentAtClickTime(PropertyChangeEvent evt, CalendarModel calMod) {
		return false;
	}
 }
