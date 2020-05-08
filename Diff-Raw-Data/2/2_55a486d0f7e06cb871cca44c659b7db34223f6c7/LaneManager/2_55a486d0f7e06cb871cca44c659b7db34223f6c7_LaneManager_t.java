 package manager;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import manager.util.OverlayPanel;
 import DeviceGraphicsDisplay.CameraGraphicsDisplay;
 import DeviceGraphicsDisplay.DeviceGraphicsDisplay;
 import DeviceGraphicsDisplay.FeederGraphicsDisplay;
 import DeviceGraphicsDisplay.GantryGraphicsDisplay;
 import DeviceGraphicsDisplay.LaneGraphicsDisplay;
 import DeviceGraphicsDisplay.NestGraphicsDisplay;
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 public class LaneManager extends Client implements ActionListener {
 
 	// JFrame dimensions
 	private static final int WINDOW_WIDTH = 400;
 	private static final int WINDOW_HEIGHT = 700;
 
 	// Create a new timer
 	private Timer timer;
 
 	// Variables for displaying an error message when user clicks outside of a
 	// lane
 	private int timeElapsed;
 	private boolean messageDisplayed;
 
 	// Swing components
 	private OverlayPanel messagePanel;
 	private JLabel currentMessage;
 
 	// This arrayList holds 8 panels - one located over each lane with a
 	// mouseListener
 	private ArrayList<JPanel> lanePanels;
 
 	// This JPanel lies over the entire window. The panels that correspond to
 	// each lane are added to
 	// this panel. This panel also has its own mouse listener - the purpose of
 	// this is so we can
 	// know when the user clicks on the lane (good click) or when the user
 	// clicks outside of the
 	// lane (bad click)
 	private JPanel windowPanel;
 
 	/**
 	 * Constructor
 	 */
 	public LaneManager() {
 		super();
 		clientName = Constants.LANE_MNGR_CLIENT;
 		offset = -540;
 
 		timeElapsed = 0;
 		messageDisplayed = false;
 
 		initStreams();
 		initGUI();
 		initDevices();
 
 	}
 
 	/**
 	 * Initialize the GUI and start the timer.
 	 */
 	public void initGUI() {
 
 		// Initialize and add the windowPanel that lies over the whole window
 		windowPanel = new JPanel();
 		windowPanel.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		windowPanel.setLayout(null);
 		windowPanel.setVisible(true);
 		windowPanel.setOpaque(false);
 		windowPanel.addMouseListener(new OutPanelMouseListener());
 		add(windowPanel);
 
 		// Initialize the arrayList of JPanels and add each one to the screen
 		// with a mouse listener
 		lanePanels = new ArrayList<JPanel>();
 		for (int i = 0; i < 8; i++) {
 			lanePanels.add(new JPanel());
 			lanePanels.get(i).setOpaque(false);
 			lanePanels.get(i).setName("" + i + "");
 			windowPanel.add(lanePanels.get(i));
 			lanePanels.get(i).setBounds(Constants.LANE_END_X - 540,
 					53 + i * 75, 210, 50);
 			lanePanels.get(i).addMouseListener(
 					new JamPanelMouseListener(lanePanels.get(i)));
 		}
 
 		messagePanel = new OverlayPanel();
 		messagePanel.setPanelSize(WINDOW_WIDTH, 30);
 		add(messagePanel, BorderLayout.SOUTH);
 
 		currentMessage = new JLabel(
 				"Click anywhere on the lane to produce a jam at that location.");
 		currentMessage.setForeground(Color.WHITE);
 		currentMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
 		currentMessage.setHorizontalAlignment(JLabel.CENTER);
 		currentMessage.setVisible(true);
 		messagePanel.add(currentMessage);
 
 		messagePanel.setVisible(true);
 
 		timer = new Timer(Constants.TIMER_DELAY, this);
 		timer.start();
 	}
 
 	/**
 	 * Initialize the devices
 	 */
 	public void initDevices() {
 		for (int i = 0; i < Constants.LANE_COUNT; i++) {
 			addDevice(Constants.LANE_TARGET + i, new LaneGraphicsDisplay(this,
 					i));
 		}
 
 		for (int i = 0; i < Constants.NEST_COUNT; i++) {
 			addDevice(Constants.NEST_TARGET + i, new NestGraphicsDisplay(this,
 					i));
 		}
 
 		for (int i = 0; i < Constants.FEEDER_COUNT; i++) {
 			addDevice(Constants.FEEDER_TARGET + i, new FeederGraphicsDisplay(
 					this, i));
 		}
 
 		addDevice(Constants.CAMERA_TARGET, new CameraGraphicsDisplay(this));
 
 		addDevice(Constants.GANTRY_ROBOT_TARGET,
 				new GantryGraphicsDisplay(this));
 
 	}
 
 	/**
 	 * Main method sets up the JFrame
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT, "Lane Manager");
 
 		LaneManager mngr = new LaneManager();
 		frame.add(mngr);
 		mngr.setVisible(true);
 		frame.validate();
 	}
 
 	/**
 	 * This function handles painting of graphics
 	 */
 	@Override
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
 
 		synchronized (devices) {
 			for (DeviceGraphicsDisplay device : devices.values()) {
 				device.draw(this, g);
 			}
 		}
 	}
 
 	/**
 	 * Forward network requests to devices processing
 	 * 
 	 * @param req
 	 *            incoming request
 	 */
 	@Override
 	public void receiveData(Request req) {
 		devices.get(req.getTarget()).receiveData(req);
 	}
 
 	/**
 	 * This function intercepts requests and drops them if the request is a
 	 * "DONE" request
 	 * 
 	 * @req Request to be sent.
 	 */
 	@Override
 	public void sendData(Request req) {
 		if (!req.getCommand().endsWith(Constants.DONE_SUFFIX)) {
 			super.sendData(req);
 		}
 	}
 
 	/**
 	 * This function handles action events.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		repaint();
 
 		// If user clicked a bad location, remove error message after 5 seconds
 		if (messageDisplayed) {
 			if (timeElapsed == 75) {
 				currentMessage
 						.setText("Click anywhere on the lane to produce a jam at that location.");
 				timeElapsed = 0;
 				messageDisplayed = false;
 			} else {
 				timeElapsed++;
 			}
 		}
 	}
 
 	/**
 	 * This function gets called when a user clicks outside of a lane.
 	 */
 	public void clickOutOfBounds() {
 		displayMessage("Cannot produce a jam here! Click on a lane.");
 	}
 
 	public void displayMessage(String message) {
 		messageDisplayed = true;
 		currentMessage.setText(message);
 		timeElapsed = 0;
 	}
 
 	private class JamPanelMouseListener implements MouseListener {
 		JPanel panel;
 
 		public JamPanelMouseListener(JPanel p) {
 			panel = p;
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			// this will represent which lane (0-7) was clicked
 			int laneNumber;
 
 			// this is necessary to change lane number from string to integer so
 			// that
 			// it can be multiplied by the y-coordinate for proper locations for
 			// each lane
 			laneNumber = Integer.valueOf(e.getComponent().getName());
 
 			// pass this location AND laneNumber to FCS once appropriate method
 			// is there
 			Location location = new Location(e.getX(), e.getY());
 			displayMessage("Jam at lane " + laneNumber);
 
			sendData(new Request(Constants.LANE_SET_JAM_COMMAND,
 					Constants.LANE_TARGET + laneNumber, location));
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			panel.setOpaque(true);
 			panel.setBackground(new Color(255, 255, 255, 70));
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			panel.setOpaque(false);
 		}
 	}
 
 	private class OutPanelMouseListener implements MouseListener {
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			// this means the user clicked on the screen but not in lane
 			clickOutOfBounds();
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 		}
 	}
 }
