 package framePackage;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.LineBorder;
 
 import data.CalendarModel;
 import data.Meeting;
 import data.Notification;
 import data.Person;
 
 public class AppointmentView implements PropertyChangeListener {
 
 	private ArrayList<Meeting> meetings;
 	private ArrayList<Notification> notifications;
 	private Person user;
 	private JPanel meetingPanel;
 	private JPanel mainPanel;
 	private JPanel legendPanel;
 	private JPanel headerPanel;
 	private GridBagConstraints mc;
 	private CalendarModel calendarModel;
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"dd.MMM.yyyy");
 	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(
 			"HH:mm");
 	private static final int[] sizes = { 50, 100, 50, 300, 100, 90, 90 };
 
 	public AppointmentView(CalendarModel calendarModel) {
 		this.calendarModel = calendarModel;
 		calendarModel.addPropertyChangeListener(this);
 		this.notifications = notifications;
 		this.user = user;
 		mainPanel = new JPanel();
 		meetingPanel = new JPanel();
 		meetingPanel.setLayout(new GridBagLayout());
 		mainPanel.add(meetingPanel, BorderLayout.CENTER);
 
 		JScrollPane jsp = new JScrollPane(meetingPanel);
 		mainPanel.setLayout(new BorderLayout());
 		createHeaders();
 		createLegend();
 		mainPanel.add(headerPanel, BorderLayout.NORTH);
 		mainPanel.add(jsp, BorderLayout.CENTER);
 		mainPanel.add(legendPanel, BorderLayout.SOUTH);
		mainPanel.setBorder(new LineBorder(Color.black));
 		mainPanel.setPreferredSize(new Dimension(814, 457));
 		// refreshMeetings();
 
 	}
 
 	private void createLegend() {
 		legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
 		JLabel leader = new JLabel(": Mteleder");
 		leader.setIcon(new ImageIcon("res/icons/icon_star.png"));
 		JLabel participates = new JLabel(": Deltar");
 		participates.setIcon(new ImageIcon("res/icons/icon_check.png"));
 		JLabel notParticipates = new JLabel(": Deltar ikke");
 		notParticipates.setIcon(new ImageIcon("res/icons/icon_cross.png"));
 		JLabel unanswered = new JLabel(": Venter svar");
 		unanswered.setIcon(new ImageIcon("res/icons/icon_question.png"));
 		legendPanel.add(leader);
 		legendPanel.add(participates);
 		legendPanel.add(notParticipates);
 		legendPanel.add(unanswered);
 		legendPanel.setPreferredSize(new Dimension(800, 30));
 		legendPanel.setBorder(new LineBorder(Color.black));
 	}
 
 	private void createHeaders() {
 		headerPanel = new JPanel(new GridBagLayout());
 		JLabel[] headers = new JLabel[7];
 		headers[0] = new JLabel("status");
 		headers[1] = new JLabel("dato");
 		headers[2] = new JLabel("tid");
 		headers[3] = new JLabel("avtale");
 		headers[4] = new JLabel("sted");
 		headers[5] = new JLabel("endre");
 		headers[6] = new JLabel("mer info");
 
 		mc = new GridBagConstraints();
 		mc.gridy = 0;
 		mc.anchor = GridBagConstraints.LINE_END;
 		for (int i = 0; i < headers.length; i++) {
 			headers[i].setPreferredSize(new Dimension(sizes[i], 20));
			mc.gridx += i;
 			headerPanel.add(headers[i]);
 		}
 	}
 
 	private void refreshMeetings() {
 //		 for (int i = 0; i < notifications.size(); i++) {
 //		 Notification n = notifications.get(i);
 //		 	if(n.getApproved() == 'w') {
 //		 		
 //		 	}
 //		 }
 		
 		mc.anchor = GridBagConstraints.NORTHWEST;
 		for (int i = 0; i < meetings.size(); i++) {
 			if (i == meetings.size()-1) {
 				mc.weighty = 1;
 			}
 			Meeting meeting = meetings.get(i);
 			JComponent[] items = new JComponent[7];
 			items[0] = new JLabel(new ImageIcon("res/icons/icon_check.png"));
 			items[1] = new JLabel();
 			items[2] = new JLabel();
 			items[3] = new JLabel(meeting.getTitle());
 			items[4] = new JLabel(meeting.getLocation());
 			items[5] = new JButton("endre");
 			items[6] = new JButton("mer info...");
 
 			GregorianCalendar startDate = new GregorianCalendar();
 			startDate.setTimeInMillis(meeting.getStartTime());
 			((JLabel) items[1]).setText(dateFormat.format(startDate.getTime()));
 			((JLabel) items[2]).setText(timeFormat.format(startDate.getTime()));
 
 			mc.gridy = i + 1;
 			for (int j = 0; j < items.length; j++) {
 				JComponent item = items[j];
 				item.setPreferredSize(new Dimension(sizes[j], 20));
 				mc.gridx = j;
 				meetingPanel.add(item, mc);
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		JFrame frame = new JFrame("APPointmensTest");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		CalendarModel cm = new CalendarModel();
 		frame.setContentPane(new AppointmentView(cm).getPanel());
//		cm.init();
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public JPanel getPanel() {
 		return mainPanel;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		switch (evt.getPropertyName()) {
 		case CalendarModel.CALENDAR_LOADED_Property:
 			user = calendarModel.getUser();
 			meetings = calendarModel.getMeetings(user);
 //			notifications = calendarModel.getNotifications(user);
 			refreshMeetings();
 			break;
 		}
 
 	}
 }
