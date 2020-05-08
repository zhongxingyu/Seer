 package manager;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JSlider;
 import javax.swing.Timer;
 
 import manager.util.OverlayPanel;
 import manager.util.WhiteLabel;
 import DeviceGraphicsDisplay.CameraGraphicsDisplay;
 import DeviceGraphicsDisplay.ConveyorGraphicsDisplay;
 import DeviceGraphicsDisplay.DeviceGraphicsDisplay;
 import DeviceGraphicsDisplay.InspectionStandGraphicsDisplay;
 import DeviceGraphicsDisplay.KitRobotGraphicsDisplay;
 import DeviceGraphicsDisplay.NestGraphicsDisplay;
 import DeviceGraphicsDisplay.PartsRobotDisplay;
 import DeviceGraphicsDisplay.StandGraphicsDisplay;
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 
 public class KitAssemblyManager extends Client implements ActionListener {
 	// Window dimensions
 	private static final int WINDOW_WIDTH = 600;
 	private static final int WINDOW_HEIGHT = 700;
 
 	// JSlider parameters
 	private final int CHANCE_MIN = 0;
 	private final int CHANCE_MAX = 100;
 	private final int CHANCE_INIT = 0;
 
 	// Swing Components
 	private final int DEFECT_PANEL_HEIGHT = 70;
 
 	private OverlayPanel defectPanel;
 	private JSlider dropChanceSlider;
 	private JButton okButton;
 
 	// Stores chance of part robot dropping a kit selected by user
 	private Float selectedChance;
 
 	// Create a timer
 	private Timer timer;
 
 	/**
 	 * Constructor
 	 */
 	public KitAssemblyManager() {
 		super();
 		clientName = Constants.KIT_ASSEMBLY_MNGR_CLIENT;
 		offset = 0;
 
 		initStreams();
 		initGUI();
 		initDevices();
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
 	 * Initialize the GUI and start the timer.
 	 */
 	public void initGUI() {
 		defectPanel = new OverlayPanel();
 		defectPanel.setPanelSize(WINDOW_WIDTH, DEFECT_PANEL_HEIGHT);
 		add(defectPanel, BorderLayout.SOUTH);
 
 		WhiteLabel chanceLabel = new WhiteLabel("Part Drop Percentage");
 
 		defectPanel.add(chanceLabel);
 
 		dropChanceSlider = new JSlider(JSlider.HORIZONTAL, CHANCE_MIN,
 				CHANCE_MAX, CHANCE_INIT);
 		dropChanceSlider.setMajorTickSpacing(50);
 		dropChanceSlider.setMinorTickSpacing(5);
 		dropChanceSlider.setPaintTicks(true);
 		dropChanceSlider.setPaintLabels(true);
 		dropChanceSlider.setFont(new Font("Arial", Font.PLAIN, 14));
 		dropChanceSlider.setForeground(Color.WHITE);
 		dropChanceSlider.setOpaque(false);
 		defectPanel.add(dropChanceSlider);
 
 		okButton = new JButton("OK >");
 		okButton.addActionListener(this);
 		defectPanel.add(okButton);
 
 		timer = new Timer(Constants.TIMER_DELAY, this);
 		timer.start();
 	}
 
 	/**
 	 * Initialize the devices
 	 */
 	public void initDevices() {
 		// TODO - add KitAssemblyManager devices here
 
 		for (int i = 0; i < Constants.NEST_COUNT; i++) {
 			addDevice(Constants.NEST_TARGET + i, new NestGraphicsDisplay(this,
 					i));
 		}
 
 		addDevice(Constants.STAND_TARGET + 0,
 				new InspectionStandGraphicsDisplay(this));
 
 		for (int i = 1; i < Constants.STAND_COUNT; i++) {
 			addDevice(Constants.STAND_TARGET + i, new StandGraphicsDisplay(
 					this, i));
 		}
 
 		addDevice(Constants.CONVEYOR_TARGET, new ConveyorGraphicsDisplay(this));
 		addDevice(Constants.KIT_ROBOT_TARGET, new KitRobotGraphicsDisplay(this));
 		addDevice(Constants.CAMERA_TARGET, new CameraGraphicsDisplay(this));
 		addDevice(Constants.PARTS_ROBOT_TARGET, new PartsRobotDisplay(this));
 	}
 
 	/**
 	 * Main method sets up the JFrame
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT,
 				"Kit Assembly Manager");
 
 		KitAssemblyManager mngr = new KitAssemblyManager();
 		frame.add(mngr);
 		mngr.setVisible(true);
 		frame.validate();
 	}
 
 	/**
 	 * This function intercepts requests and calls client's sendData if the
 	 * request is a DONE request.
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
 	 * This function handles action events.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		repaint();
 
 		if (ae.getSource() == okButton) {
			selectedChance = Float.valueOf(dropChanceSlider.getValue() / 100);
 
 			this.sendData(new Request(Constants.FCS_SET_DROP_CHANCE,
 					Constants.FCS_TARGET, selectedChance));
 		}
 	}
 }
