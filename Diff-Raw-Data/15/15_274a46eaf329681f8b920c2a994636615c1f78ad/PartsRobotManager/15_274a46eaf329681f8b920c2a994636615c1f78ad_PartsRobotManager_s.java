 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.Timer;
 
 import DeviceGraphicsDisplay.CameraGraphicsDisplay;
 import DeviceGraphicsDisplay.DeviceGraphicsDisplay;
 import DeviceGraphicsDisplay.KitGraphicsDisplay;
 import DeviceGraphicsDisplay.NestGraphicsDisplay;
 import DeviceGraphicsDisplay.PartsRobotDisplay;
 import GUI.NetworkingButtonListener;
 import GUI.OverlayPanel;
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 
 public class PartsRobotManager extends Client implements ActionListener{
 	// Temp values. Feel free to change
 	private static final int WINDOW_WIDTH = 800;
 	private static final int WINDOW_HEIGHT = 600;
 	
 	private Timer timer;
 	
 	public PartsRobotManager() {
 		super();
 		clientName = Constants.PARTS_ROBOT_MNGR_CLIENT;
 		
 		initStreams();
 		initGUI();
 		initDevices();
 	}
 	
 	public void initGUI() {
 		JLabel label = new JLabel("Parts Robot Manager");
 		label.setForeground(Color.WHITE);
 		label.setFont(new Font("SansSerif", Font.PLAIN, 40));
 		label.setHorizontalAlignment(JLabel.CENTER);
 		add(label);
 		
 		OverlayPanel panel = new OverlayPanel();
 		add(panel, BorderLayout.SOUTH);
 		panel.setVisible(true);
 		
 		JButton nest1 = new JButton("Nest1");
 		nest1.addActionListener(new NetworkingButtonListener(Constants.PARTS_ROBOT_MOVE_TO_NEST1_COMMAND, Constants.PARTS_ROBOT_TARGET, writer));
 		panel.add(nest1);
 		
 		JButton nest2 = new JButton("Nest2");
 		nest2.addActionListener(new NetworkingButtonListener(Constants.PARTS_ROBOT_MOVE_TO_NEST2_COMMAND, Constants.PARTS_ROBOT_TARGET, writer));
 		panel.add(nest2);
 		
 		JButton gohome = new JButton("Go Home");
 		gohome.addActionListener(new NetworkingButtonListener(Constants.PARTS_ROBOT_GO_HOME_COMMAND, Constants.PARTS_ROBOT_TARGET, writer));
 		panel.add(gohome);
 		
 		JButton gokit = new JButton("Go Kit");
 		gokit.addActionListener(new NetworkingButtonListener(Constants.PARTS_ROBOT_GO_KIT_COMMAND, Constants.PARTS_ROBOT_TARGET, writer));
 		panel.add(gokit);
 		
 		JButton pickup = new JButton("Pick Up");
 		pickup.addActionListener(new NetworkingButtonListener(Constants.PARTS_ROBOT_PICKUP_COMMAND, Constants.PARTS_ROBOT_TARGET, writer));
 		panel.add(pickup);
 		
 		JButton give = new JButton("Give to Kit");
 		give.addActionListener(new NetworkingButtonListener(Constants.PARTS_ROBOT_GIVE_COMMAND, Constants.PARTS_ROBOT_TARGET, writer));
 		panel.add(give);
 		
 		JButton picNest = new JButton("Take pic of nest");
 		picNest.addActionListener(new NetworkingButtonListener(Constants.CAMERA_TAKE_NEST_PHOTO_COMMAND, Constants.CAMERA_TARGET, writer));
 		panel.add(picNest);
 		
 		JButton removeNest = new JButton("remove from nest");
		removeNest.addActionListener(new NetworkingButtonListener(Constants.NEST_RECEIVE_PART_COMMAND, Constants.NEST_TARGET+":"+0, writer));
 		panel.add(removeNest);
 		
 		timer = new Timer(Constants.TIMER_DELAY, this);
 		timer.start();
 	}
 	
 	public void initDevices() {
 		// example:
 //		addDevice(Constants.LANE_TARGET, new LaneGraphicsDisplay(this, new Location(400, 100), 0));
 //		addDevice(Constants.LANE_TARGET, new LaneGraphicsDisplay(this, new Location(400, 100), 1));
 		addDevice(Constants.PARTS_ROBOT_TARGET, new PartsRobotDisplay(this, new Location(250,450)));
 		addDevice(Constants.NEST_TARGET+":"+0, new NestGraphicsDisplay(this, 0));
 		addDevice(Constants.NEST_TARGET+":"+1, new NestGraphicsDisplay(this, 1));
 		addDevice(Constants.KIT_TARGET, new KitGraphicsDisplay(this, new Location (20, 200)));
 		addDevice(Constants.CAMERA_TARGET, new CameraGraphicsDisplay(this, new Location(5,5)));
 	}
 	
 	@Override
 	public void receiveData(Request req) {
 		devices.get(req.getTarget()).receiveData(req);
 	}
 
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT);
 		
 		PartsRobotManager mngr = new PartsRobotManager();
 		frame.add(mngr);
 		mngr.setVisible(true);
 		frame.validate();
 	}
 	
 	@Override
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
 		
 		for(DeviceGraphicsDisplay device : devices.values()) {
 			device.draw(this, g);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		repaint();
 		
 	}
 }
