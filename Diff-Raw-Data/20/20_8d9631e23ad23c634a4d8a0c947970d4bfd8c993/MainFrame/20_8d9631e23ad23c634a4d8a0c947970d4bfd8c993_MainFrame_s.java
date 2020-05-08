 package client.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import chronos.Singleton;
 import client.ClientController;
 import client.gui.view.ParticipantsWindow;
 import client.gui.view.CalendarWindow;
 import client.gui.view.ChronosWindow;
 import client.gui.view.EventConfigWindow;
 import client.gui.view.InvitationWindow;
 import client.gui.view.LoginWindow;
 import client.gui.view.RoomBookingWindow;
 import client.gui.view.NotificationWindow;
 import client.model.ParticipantsModel;
 import client.model.CalendarModel;
 import client.model.ChronosModel;
 import client.model.EventConfigModel;
 import client.model.InvitationModel;
 import client.model.LoginModel;
 import client.model.RoomBookingModel;
 import events.QueryEvent;
 import events.QueryEvent.QueryType;
 
 /**
  * The main GUI frame. Holds a layeredPane, each filled with it's individual
  * view
  * 
  * @author anon
  * 
  */
 public class MainFrame extends JFrame {
 
 	private ArrayList<ChronosWindow> panels;
 	private final ClientController controller;
 
 	private ChronosWindow calendarWindow;
 	private ChronosModel calendarModel;
 
 	private ChronosModel loginModel;
 	private ChronosWindow loginWindow;
 
 	private ChronosWindow invitationWindow;
 	private InvitationModel invitationModel;
 
 	private ChronosWindow eventConfigWindow;
 	private ChronosWindow notificationWindow;
 	private ChronosModel eventConfigModel;
 
 	private ChronosWindow roomBookingWindow;
 	private RoomBookingModel roomBookingModel;
 
 	private ChronosWindow addParticipantWindow;
 	private ParticipantsModel addParticipantModel;
 
 	private int frameWidth = 1150;
 	private int frameHeight = 620;
 	private JFrame loginFrame;
 
 	public MainFrame(ClientController controller) {
 		this.controller = controller;
 		setTitle(Singleton.APP_NAME);
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 
 		panels = new ArrayList<ChronosWindow>();
 
 		calendarModel = new CalendarModel(controller);
 		calendarWindow = new CalendarWindow(calendarModel, this);
 
 		invitationModel = new InvitationModel(controller);
 		invitationWindow = new InvitationWindow(invitationModel, this);
 
 		eventConfigModel = new EventConfigModel(controller);
 		eventConfigWindow = new EventConfigWindow(eventConfigModel, this);
		notificationWindow = new NotificationWindow(eventConfigModel, this);
 
 		roomBookingModel = new RoomBookingModel(controller);
 		roomBookingWindow = new RoomBookingWindow(roomBookingModel, this);
 
 		addParticipantModel = new ParticipantsModel(controller);
 		addParticipantWindow = new ParticipantsWindow(addParticipantModel, this);
 
 		// Timer t = new Timer(10, new ActionListener() {
 		//
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// repaint();
 		//
 		// }
 		// });
 		// t.start();
 	}
 
 	private JLayeredPane buildLayeredPane() {
 		JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBackground(Color.black);
 
 		// ADD COMPONENTS
 		layeredPane.add(calendarWindow, new Integer(0));
 		layeredPane.add(eventConfigWindow, new Integer(2));
		layeredPane.add(notificationWindow, new Integer(2));
 		layeredPane.add(invitationWindow, new Integer(4));
 		layeredPane.add(roomBookingWindow, new Integer(14));
 		layeredPane.add(addParticipantWindow, new Integer(16));
 
 		// SET BOUNDS ON EVERY COMPONENT ADDED DIRECTLY TO A LAYER
 		calendarWindow.setBounds(0, 0, frameWidth, frameHeight);
 
 		int eventConfigWidth = 500;
 		int eventConfigHeight = frameHeight / 2;
 		eventConfigWindow.setBounds((frameWidth - eventConfigWidth) / 2, (frameHeight - eventConfigHeight) / 2, eventConfigWidth, eventConfigHeight);
		notificationWindow.setBounds((frameWidth - eventConfigWidth) / 2, (frameHeight - eventConfigHeight) / 2, eventConfigWidth, eventConfigHeight);
 
 		int roomBookingWidth = frameWidth / 4;
 		int roomBookingHeight = frameHeight / 4;
 		roomBookingWindow.setBounds((frameWidth - roomBookingWidth) / 2, (frameHeight - roomBookingHeight) / 2, roomBookingWidth, roomBookingHeight);
 
 		int addParticipantWidth = frameWidth / 6;
 		int addParticipantHeight = frameHeight / 2;
 		addParticipantWindow.setBounds((frameWidth + eventConfigWidth) / 2, (frameHeight - addParticipantHeight) / 2, addParticipantWidth, addParticipantHeight);
 
 		layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));
 		layeredPane.setOpaque(true);
 
 		return layeredPane;
 	}
 
 	private void buildFrame(JFrame frame) {
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setLocationRelativeTo(getRootPane());
 		frame.setVisible(true);
 		frame.setResizable(false);
 		frame.setExtendedState(Frame.NORMAL);
 	}
 
 	// initialize the GUI here
 	public void buildGui() {
 		if (loginFrame != null) {
 			loginFrame.dispose();
 			JLabel currentUser = new JLabel(Singleton.getInstance().getSelf().toString());
 			calendarWindow.add(currentUser, new GBC(0, 100).setSpan(10, 1));
 			calendarModel.fireNetworkEvent(new QueryEvent(QueryType.CALEVENTS, Singleton.getInstance().getSelf()));
 			calendarModel.fireNetworkEvent(new QueryEvent(QueryType.PERSONS));
 		}
 		JLayeredPane layeredPane = buildLayeredPane();
 		getContentPane().add(layeredPane);
 
 		buildFrame(this);
 	}
 
 	public void loginPrompt() {
 		loginFrame = new JFrame();
 		int frameWidth = 300;
 		int frameHeight = 200;
 
 		loginFrame.setSize(frameWidth, frameHeight);
 
 		loginModel = new LoginModel(controller);
 		loginWindow = new LoginWindow(loginModel, this);
 		loginWindow.setBounds(0, 0, frameWidth, frameHeight);
 		loginFrame.add(loginWindow);
 
 		buildFrame(loginFrame);
 	}
 
 	public ChronosWindow getEventConfigWindow() {
 		return eventConfigWindow;
 	}
 
 	public ChronosWindow getRoomBookingWindow() {
 		return roomBookingWindow;
 	}
 
 	public ChronosWindow getAddParticipantWindow() {
 		System.out.println(addParticipantWindow.isVisible());
 		System.out.println(addParticipantWindow.getSize());
 		return addParticipantWindow;
 	}
 
 	public ChronosWindow getNotificationWindow() {
 		return notificationWindow;
 	}
 }
