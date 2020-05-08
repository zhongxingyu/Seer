 package gruppe19.gui;
 
 import gruppe19.client.ktn.ServerAPI;
 import gruppe19.model.Appointment;
 import gruppe19.model.Room;
 import gruppe19.model.User;
 
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.toedter.calendar.JCalendar;
 
 public class MainScreen extends JFrame {
	private final User loggedInUser;
 	private final MainMenu menu;
 	private final JCalendar miniCalendar;
 	private final CalendarView calendar;
 	private final JButton logOut;
 	
 	private final JLabel calendarHeader;
 	private final JLabel leftArrow;
 	private final JLabel rightArrow;
 	private final JLabel week;
 	private final JLabel selectWeek;
 	
 	public CalendarView getCalendar() {
 		return calendar;
 	}
 	
 	/**
 	 * Gets the user logged into the system.
 	 */
	public User getUser() {
 		return loggedInUser;
 	}
 	
 	/**
 	 * Called when the logged in user is invited to the specified appointment.
 	 */
 	public void invite(Appointment a) {
 		
 	}
 	
 	public MainScreen(User user) {
 		setLayout(null);
 		setSize(1280, 720);
 		
 		loggedInUser = user;
 		menu = new MainMenu(this);
 		miniCalendar = new JCalendar();
 		calendar = new CalendarView(new Date());
 		logOut = new JButton("Logg ut");
 		selectWeek = new JLabel("Velg uke:");
 		week = new JLabel("Uke " + calendar.getCurrentWeek());
 		leftArrow = new JLabel(new ImageIcon(getClass().getResource("/gruppe19/client/images/LeftArrow.png")));
 		rightArrow = new JLabel(new ImageIcon(getClass().getResource("/gruppe19/client/images/RightArrow.png")));
 		calendarHeader = new JLabel(new ImageIcon(getClass().getResource("/gruppe19/client/images/CalendarHead.png")));
 		
 		week.setFont(new Font(Font.SERIF, Font.BOLD, 26));
 		selectWeek.setFont(new Font(Font.SERIF, Font.BOLD, 22));
 		
 		leftArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		rightArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 		miniCalendar.setSize(250, 250);
 		leftArrow.setSize(leftArrow.getPreferredSize());
 		rightArrow.setSize(rightArrow.getPreferredSize());
 		logOut.setSize(logOut.getPreferredSize());
 		week.setSize(week.getPreferredSize());
 		selectWeek.setSize(selectWeek.getPreferredSize());
 		calendarHeader.setSize(calendarHeader.getPreferredSize());
 		
 		miniCalendar.setLocation(20, getHeight() - 300);
 		selectWeek.setLocation(miniCalendar.getX(), miniCalendar.getY() - 40);
 		
 		setLocationsAndSizes();
 		
 		add(miniCalendar);
 		add(selectWeek);
 		add(week);
 		add(logOut);
 		add(menu);
 		add(calendarHeader);
 		add(calendar);
 		add(leftArrow);
 		add(rightArrow);
 		
 		ServerAPI.setListener(this);
 		
 		//Add all appointments you have started to the calendar view
 		for (Appointment a : ServerAPI.getAppointmentsStarted(loggedInUser)) {
 			calendar.addAppointment(a);
 		}
 		
 		//Add all appointments where you are a participant to the calendar view
 		for (Appointment a : ServerAPI.getAppointments(loggedInUser)) {
 			calendar.addAppointment(a);
 		}
 		
 		setTitle("Kalendersystem - Hovedskjerm");
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 		
 		//Called after the window is resized
 		getContentPane().addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent e) {
 				setLocationsAndSizes();
 			}
 		});
 		
 		//Called every time the mini calendar is clicked
 		miniCalendar.addPropertyChangeListener(new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (evt.getPropertyName().equals("calendar")) {
 					calendar.setDate(miniCalendar.getDate());
 					week.setText("Uke " + calendar.getCurrentWeek());
 				}
 			}
 		});
 		
 		//Called when the left arrow is clicked
 		leftArrow.addMouseListener(new MouseAdapter() {
 			public void mousePressed(java.awt.event.MouseEvent e) {
 				GregorianCalendar cal = new GregorianCalendar();
 				
 				cal.setTime(calendar.getDate());
 				cal.add(Calendar.WEEK_OF_YEAR, -1);
 				
 				calendar.setDate(cal.getTime());
 				miniCalendar.setDate(calendar.getDate());
 				week.setText("Uke " + calendar.getCurrentWeek());
 			}
 		});
 		
 		//Called when the right arrow is clicked
 		rightArrow.addMouseListener(new MouseAdapter() {
 			public void mousePressed(java.awt.event.MouseEvent e) {
 				GregorianCalendar cal = new GregorianCalendar();
 				
 				cal.setTime(calendar.getDate());
 				cal.add(Calendar.WEEK_OF_YEAR, 1);
 				
 				calendar.setDate(cal.getTime());
 				miniCalendar.setDate(calendar.getDate());
 				week.setText("Uke " + calendar.getCurrentWeek());
 			}
 		});
 		
 		//Called when logout is clicked
 		logOut.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MainScreen.this.dispose();
 			}
 		});
 	}
 	
 	/**
 	 * Sets all non-constant location and size attributes of all the
 	 *  elements in the JFrame according to the current frame size.
 	 */
 	private void setLocationsAndSizes() {
 		logOut.setLocation(this.getWidth() - logOut.getWidth() - 20, 20);
 		
 		calendar.setSize(calendarHeader.getWidth() + 18, 
 							this.getHeight() - 140);
 		
 		calendar.setLocation(this.getWidth() - calendar.getWidth() - 20, 
 								calendarHeader.getHeight() +
 								week.getHeight() + logOut.getHeight() + 10);
 		
 		week.setLocation(calendar.getX() + 
 							calendarHeader.getWidth() / 2 -
 							week.getWidth() / 2, 30);
 		
 		calendarHeader.setLocation(calendar.getX() + 1, 
 									week.getHeight() + logOut.getHeight() + 10);
 		
 		leftArrow.setLocation(week.getX() - leftArrow.getWidth() - 40, 
 								week.getY());
 		
 		rightArrow.setLocation(week.getX() + week.getWidth() + 40, 
 								week.getY());
 	}
 	
 	public static void main(String[] args) {
 		new MainScreen(new User("Hans", "Hansen"));
 	}
 }
