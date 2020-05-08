 package client.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Label;
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
 import client.gui.view.LoginWindow;
 import client.gui.view.RoomBookingWindow;
 import client.gui.view.eventConfig.EventWindowAdmin;
 import client.gui.view.eventConfig.EventWindowInvite;
 import client.gui.view.eventConfig.EventWindowNew;
 import client.gui.view.eventConfig.EventWindowOther;
 import client.gui.view.eventConfig.EventWindowParticipant;
 import client.gui.view.eventConfig.EventWindowUpdate;
 import client.model.ParticipantsModel;
 import client.model.CalendarModel;
 import client.model.ChronosModel;
 import client.model.EventConfigModel;
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
 	private ChronosModel invitationModel;
 
 	private ChronosWindow eventWindowAdmin;
 	private ChronosWindow eventWindowInvite;
 	private ChronosWindow eventWindowOther;
 	private ChronosWindow eventWindowParticipant;
 	private ChronosWindow eventWindowNew;
 	private ChronosWindow eventWindowUpdate;
 
 	private ChronosModel eventConfigModel;
 
 	private ChronosWindow roomBookingWindow;
 	private ChronosModel roomBookingModel;
 
 	private ChronosWindow addParticipantWindow;
 	private ChronosModel addParticipantModel;
 
	private int frameWidth = 1202;
	private int frameHeight = 622;
 	private JFrame loginFrame;
 
 	public MainFrame(ClientController controller) {
 		this.controller = controller;
 		setTitle(Singleton.APP_NAME);
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 
 		panels = new ArrayList<ChronosWindow>();
 
 		calendarModel = new CalendarModel(controller);
 		calendarWindow = new CalendarWindow(calendarModel, this);
 
 		eventConfigModel = new EventConfigModel(controller);
 
 		eventWindowAdmin = new EventWindowAdmin(eventConfigModel, this);
 		eventWindowInvite = new EventWindowInvite(eventConfigModel, this);
 		eventWindowOther = new EventWindowOther(eventConfigModel, this);
 		eventWindowParticipant = new EventWindowParticipant(eventConfigModel, this);
 		eventWindowNew = new EventWindowNew(eventConfigModel, this);
 		eventWindowUpdate = new EventWindowUpdate(eventConfigModel, this);
 
 		roomBookingModel = new RoomBookingModel(controller);
 		roomBookingWindow = new RoomBookingWindow(roomBookingModel, this);
 
 		addParticipantModel = new ParticipantsModel(controller);
 		addParticipantWindow = new ParticipantsWindow(addParticipantModel, this);
 
 	}
 
 	private JLayeredPane buildLayeredPane() {
 		JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBackground(Color.black);
 
 		// ADD COMPONENTS
 		layeredPane.add(calendarWindow, new Integer(0));
 		layeredPane.add(eventWindowAdmin, new Integer(2));
 		layeredPane.add(eventWindowInvite, new Integer(2));
 		layeredPane.add(eventWindowOther, new Integer(2));
 		layeredPane.add(eventWindowParticipant, new Integer(2));
 		layeredPane.add(eventWindowUpdate, new Integer(2));
 		layeredPane.add(eventWindowNew, new Integer(2));
 
 		layeredPane.add(roomBookingWindow, new Integer(14));
 		layeredPane.add(addParticipantWindow, new Integer(16));
 
 		// SET BOUNDS ON EVERY COMPONENT ADDED DIRECTLY TO A LAYER
 		calendarWindow.setBounds(0, 0, frameWidth, frameHeight);
 
 		int roomBookingWidth = frameWidth / 4;
 		int roomBookingHeight = frameHeight / 4;
 		roomBookingWindow.setBounds((frameWidth - roomBookingWidth) / 2, (frameHeight - roomBookingHeight) / 2, roomBookingWidth, roomBookingHeight);
 
 		int addParticipantWidth = frameWidth / 6;
 		int addParticipantHeight = 300;
 		addParticipantWindow.setBounds((frameWidth + eventWindowAdmin.getWidth()) / 2, (frameHeight - addParticipantHeight) / 2, addParticipantWidth, addParticipantHeight);
 
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
 			calendarModel.fireNetworkEvent(new QueryEvent(QueryType.CALEVENTS, Singleton.getInstance().getSelf()));
 			calendarModel.fireNetworkEvent(new QueryEvent(QueryType.PERSONS));
 			Label currentUser = new Label("Username: " + Singleton.getInstance().getSelf().getUsername() + " - Name: " + Singleton.getInstance().getSelf().getName());
 			calendarWindow.add(currentUser, new GBC(3, 0).setSpan(2, 1));
 		}
 		JLayeredPane layeredPane = buildLayeredPane();
 		getContentPane().add(layeredPane);
 
 		buildFrame(this);
 	}
 
 	public void loginPrompt() {
 		loginFrame = new JFrame("Chronos login");
 		int frameWidth = 600;
 		int frameHeight = 300;
 
 		loginFrame.setSize(frameWidth, frameHeight);
 
 		loginModel = new LoginModel(controller);
 		loginWindow = new LoginWindow(loginModel, this);
 		loginWindow.setBounds(0, 0, frameWidth, frameHeight);
 		loginFrame.add(loginWindow);
 
 		buildFrame(loginFrame);
 	}
 
 	public int getFrameHeight() {
 		return frameHeight;
 	}
 
 	public int getFrameWidth() {
 		return frameWidth;
 	}
 
 	public EventConfigModel getEventModel() {
 		return (EventConfigModel) eventConfigModel;
 	}
 
 	public ChronosWindow getRoomBookingWindow() {
 		return roomBookingWindow;
 	}
 
 	public ChronosWindow getAddParticipantWindow() {
 		return addParticipantWindow;
 	}
 
 	public ChronosWindow getNotificationWindow() {
 		return invitationWindow;
 	}
 }
