 package client.gui.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import client.ClientMain;
 import client.gui.week.WeekView;
 import client.model.MeetingModel;
 import client.model.NotificationModel;
 
 
 public class MainPanel extends JPanel implements PropertyChangeListener {
 
 	private static final long serialVersionUID = -6453034572305492442L;
 
 	private final JTabbedPane optionTabbedPane;
 	private NewAppointmentPanel newAppointmentPane = null;
 	private WeekView weekView;
 
 	public MainPanel() {
 		super(new BorderLayout());
 				
 		optionTabbedPane = new JTabbedPane();
 		JTabbedPane calendarTabbedPane = new JTabbedPane();
 		
 		HovedPanel hp = new HovedPanel();
 		
 		VarselPanel vp = new VarselPanel();
 
 		AndrePanel akp = new AndrePanel();
 		
		optionTabbedPane.setFont(new Font("Sans serif", Font.PLAIN, 10));
 		optionTabbedPane.addTab("Hoved", hp); //TODO
 		optionTabbedPane.addTab("Andre Kalendre", akp); //TODO
 		optionTabbedPane.addTab("Varsler (0)", vp); //TODO
 		
 		weekView = new WeekView();
 		weekView.addPropertyChangeListener(this);
 		calendarTabbedPane.addTab("Uke", weekView);
 		calendarTabbedPane.addTab("M�ned", new JPanel()); //TODO
 		
 		//TODO This should probably be done in a better manner
 		optionTabbedPane.setPreferredSize(new Dimension(330,calendarTabbedPane.getPreferredSize().height));
 		
 		this.add(optionTabbedPane,BorderLayout.CENTER);
 		this.add(calendarTabbedPane, BorderLayout.EAST);
 
 		
 		//Listeners
 		ActionListener listener = new NewAppointmentListener();
 		hp.getNewAppointmentButton().addActionListener(listener);
 		akp.getNewAppointmentButton().addActionListener(listener);
 		vp.getNewAppointmentButton().addActionListener(listener);
 		vp.addPropertyChangeListener(this);
 		
 		// Initialize notifications
 		vp.initializeList(ClientMain.getActiveUser().getNotifications());		
 	}
 	
 	/**
 	 * Creates and opens a new appointment at the given time
 	 * @param startTime
 	 */
 	private void OpenNewAppointment(Calendar startTime) {
 		OpenAppointment(MeetingModel.newDefaultInstance(startTime));
 	}
 	
 	
 	private void OpenAppointment(MeetingModel meeting) {
 		if (newAppointmentPane != null)
 			CloseAppointment();
 		
 		newAppointmentPane = new NewAppointmentPanel(meeting);
 		optionTabbedPane.addTab("Avtale", newAppointmentPane); //TODO navn?
 		newAppointmentPane.addPropertyChangeListener(this);
 		optionTabbedPane.setSelectedComponent(newAppointmentPane);
 	}
 	
 	private void CloseAppointment() {
 		newAppointmentPane.removePropertyChangeListener(this);
 		newAppointmentPane = null;
 		optionTabbedPane.removeTabAt(optionTabbedPane.indexOfTab("NAVN?"));
 	}
 	
 	
 	class NewAppointmentListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			OpenNewAppointment(Calendar.getInstance());
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName() == VarselPanel.NOTIFICATION_COUNT_CHANGED) {
 			optionTabbedPane.setTitleAt(2, "Varsel (" + ((Integer)evt.getNewValue()) + ")");
 		} else if (evt.getPropertyName() == VarselPanel.NOTIFICATION_W_MEETING_CLICKED) {
 			OpenAppointment(((NotificationModel)evt.getNewValue()).getRegardsMeeting());
 		} else if (evt.getPropertyName() == WeekView.WEEKCLICK) {
 			int[] dayAndHour = (int[]) evt.getNewValue();
 			int weekNumber = weekView.getWeekNumber();
 			Calendar clickTime = Calendar.getInstance();
 			clickTime.set(Calendar.WEEK_OF_YEAR, weekNumber);
 			clickTime.set(Calendar.DAY_OF_WEEK, dayAndHour[0] + 1);
 			clickTime.set(Calendar.HOUR_OF_DAY, dayAndHour[1]);
 			clickTime.set(Calendar.MINUTE, 0);
 			clickTime.set(Calendar.SECOND, 0);
 			OpenNewAppointment(clickTime);
 		} else if (evt.getPropertyName() == WeekView.APPOINTMENTCLICEKD){
 			OpenAppointment((MeetingModel)evt.getNewValue());
 		} else if (evt.getPropertyName() == NewAppointmentPanel.CLOSE) {
 			CloseAppointment();
 		}
 	}
 }
