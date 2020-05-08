 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 public class GUI
 {
 	static int padDisplaceX = 370;
 	static int padDisplaceY = 20;
 	static String newline = "\n";
 
 	private ArrayList<String> keysCurrentlyPressed;
 
 	JPanel data1, data2, buttons1, buttons2;
 	JTabbedPane data3;
 	JLabel batterylabel, signallabel, xposlabel, yposlabel, touch1label, touch2label, lightlabel, soundlabel, ultralabel, speedlabel;
 	JTextField batterydata, signaldata, xposdata, yposdata, touch1data, touch2data, lightdata, sounddata, ultradata, speed;
 	JButton connect, home, requestdata, debug, speedup, speeddown, stop, forward, backward, left, right;
 
 	JTextArea commlog, commlogSent, commlogReceived;
 
 	MainControl control;
 
 	int varspeed = 5;
 	static int varspeedMIN = 0;
 	static int varspeedMAX = 20;
 
 	public GUI(MainControl m)
 	{
 		control = m;
 		keysCurrentlyPressed = new ArrayList<String>();
 	}
 
 	public JPanel createContentPane (){  //Initialize the objects in the GUI, including buttons, labels, and 
 		//action listeners
 
 		//panels
 		final JPanel basepane = new JPanel();
 		basepane.setLayout(null);
 
 		//To be used to display internal sensor data (battery level, signal strength)
 		data1 = new JPanel();
 		data1.setLayout(null);
 		data1.setLocation(30,30);
 		data1.setSize(200, 300);
 		data1.setFocusable(false);
 		basepane.add(data1);
 
 		//Used to contain display objects for external sensor data values (touch, sound, light, ultrasonic)
 		data2 = new JPanel();
 		data2.setLayout(null);
 		data2.setLocation(280,30);
 		data2.setSize(200, 300);
 		data2.setFocusable(false);
 		basepane.add(data2);
 
 		//Contains the communication log
 		data3 = new JTabbedPane();
 		//data3.setLayout(null);
 		data3.setLocation(600,30);
 		data3.setSize(300, 300);
 		data3.setFocusable(false);
 		basepane.add(data3);
 
 		//Contains the connect, home, and request data buttons
 		buttons1 = new JPanel();
 		buttons1.setLayout(null);
 		buttons1.setLocation(30,400);
 		buttons1.setSize(320, 200);
 		buttons1.setFocusable(false);
 		basepane.add(buttons1);
 
 		//Contains the stop, forward, backward, left, and right buttons
 		buttons2 = new JPanel();
 		buttons2.setLayout(null);
 		buttons2.setLocation(350,400);
 		buttons2.setSize(620, 200);
 		buttons2.setFocusable(false);
 		basepane.add(buttons2);
 
 		//system status display
 		batterylabel = new JLabel("Battery");
 		batterylabel.setSize(100,50);
 		batterylabel.setLocation(0,0);
 		batterylabel.setHorizontalAlignment(0);
 		data1.add(batterylabel);
 		batterydata = new JTextField();
 		batterydata.setSize(100,50);
 		batterydata.setLocation(100,0);
 		batterydata.setEditable(false);
 		batterydata.setHorizontalAlignment(JTextField.RIGHT);
 		batterydata.setText("0");
 		data1.add(batterydata);
 
 		signallabel = new JLabel("Signal Strength");
 		signallabel.setSize(100,50);
 		signallabel.setLocation(0,60);
 		signallabel.setHorizontalAlignment(0);
 		data1.add(signallabel);
 		signaldata = new JTextField();
 		signaldata.setSize(100,50);
 		signaldata.setLocation(100,60);
 		signaldata.setEditable(false);
 		signaldata.setHorizontalAlignment(JTextField.RIGHT);
 		signaldata.setText("0");
 		data1.add(signaldata);
 
 		xposlabel = new JLabel("X-pos");
 		xposlabel.setSize(100,50);
 		xposlabel.setLocation(0,180);
 		xposlabel.setHorizontalAlignment(0);
 		data1.add(xposlabel);
 		xposdata = new JTextField();
 		xposdata.setSize(100,50);
 		xposdata.setLocation(100,180);
 		xposdata.setEditable(false);
 		xposdata.setHorizontalAlignment(JTextField.RIGHT);
 		xposdata.setText("0");
 		data1.add(xposdata);
 
 		yposlabel = new JLabel("Y-pos");
 		yposlabel.setSize(100,50);
 		yposlabel.setLocation(0,240);
 		yposlabel.setHorizontalAlignment(0);
 		data1.add(yposlabel);
 		yposdata = new JTextField();
 		yposdata.setSize(100,50);
 		yposdata.setLocation(100,240);
 		yposdata.setEditable(false);
 		yposdata.setHorizontalAlignment(JTextField.RIGHT);
 		yposdata.setText("0");
 		data1.add(yposdata);
 
 		//sensor data display
 		touch1label = new JLabel("Touch 1");
 		touch1label.setSize(100,50);
 		touch1label.setLocation(0,0);
 		touch1label.setHorizontalAlignment(0);
 		data2.add(touch1label);
 		touch1data = new JTextField();
 		touch1data.setSize(100,50);
 		touch1data.setLocation(100,0);
 		touch1data.setEditable(false);
 		touch1data.setHorizontalAlignment(JTextField.RIGHT);
 		touch1data.setText("0");
 		data2.add(touch1data);
 
 		//Display for touch sensor 2
 		touch2label = new JLabel("Touch 2");
 		touch2label.setSize(100,50);
 		touch2label.setLocation(0,60);
 		touch2label.setHorizontalAlignment(0);
 		data2.add(touch2label);
 		touch2data = new JTextField();
 		touch2data.setSize(100,50);
 		touch2data.setLocation(100,60);
 		touch2data.setEditable(false);
 		touch2data.setHorizontalAlignment(JTextField.RIGHT);
 		touch2data.setText("0");
 		data2.add(touch2data);
 
 		//Display for the light sensor data
 		lightlabel = new JLabel("Light");
 		lightlabel.setSize(100,50);
 		lightlabel.setLocation(0,120);
 		lightlabel.setHorizontalAlignment(0);
 		data2.add(lightlabel);
 		lightdata = new JTextField();
 		lightdata.setSize(100,50);
 		lightdata.setLocation(100,120);
 		lightdata.setEditable(false);
 		lightdata.setHorizontalAlignment(JTextField.RIGHT);
 		lightdata.setText("0");
 		data2.add(lightdata);
 
 		//Display for the sound sensor data
 		soundlabel = new JLabel("Sound");
 		soundlabel.setSize(100,50);
 		soundlabel.setLocation(0,180);
 		soundlabel.setHorizontalAlignment(0);
 		data2.add(soundlabel);
 		sounddata = new JTextField();
 		sounddata.setSize(100,50);
 		sounddata.setLocation(100,180);
 		sounddata.setEditable(false);
 		sounddata.setHorizontalAlignment(JTextField.RIGHT);
 		sounddata.setText("0");
 		data2.add(sounddata);
 
 		//Display for the ultrasonic sensor data
 		ultralabel = new JLabel("Ultrasonic");
 		ultralabel.setSize(100,50);
 		ultralabel.setLocation(0,240);
 		ultralabel.setHorizontalAlignment(0);
 		data2.add(ultralabel);
 		ultradata = new JTextField();
 		ultradata.setSize(100,50);
 		ultradata.setLocation(100,240);
 		ultradata.setEditable(false);
 		ultradata.setHorizontalAlignment(JTextField.RIGHT);
 		ultradata.setText("0");
 		data2.add(ultradata);
 
 		//command log
 		commlog = new JTextArea();
 		commlog.setSize(300,300);
 		//command log display
 		commlog.setLocation(0,0);
 		commlog.setEditable(false);
 		commlog.setFocusable(false);
 		//commlog.setHorizontalAlignment(0);
 		commlog.setText(" --Control Station online-- " + newline);
 		JScrollPane commlogpane = new JScrollPane(commlog);
 		commlogpane.setSize(300,300);
 		commlogpane.setFocusable(false);
 		commlogpane.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
 		data3.addTab("All", commlogpane);
 
 		//command log
 		commlogSent = new JTextArea();
 		commlogSent.setSize(300,300);
 		//command log display
 		commlogSent.setLocation(0,0);
 		commlogSent.setEditable(false);
 		commlogSent.setFocusable(false);
 		//commlog.setHorizontalAlignment(0);
 		commlogSent.setText(" --Control Station online-- " + newline);
 		JScrollPane commlogpaneSent = new JScrollPane(commlogSent);
 		commlogpaneSent.setSize(300,300);
 		commlogpaneSent.setFocusable(false);
 		commlogpaneSent.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
 		data3.addTab("Sent", commlogpaneSent);
 
 		//command log
 		commlogReceived = new JTextArea();
 		commlogReceived.setSize(300,300);
 		//command log display
 		commlogReceived.setLocation(0,0);
 		commlogReceived.setEditable(false);
 		commlogReceived.setFocusable(false);
 		//commlog.setHorizontalAlignment(0);
 		commlogReceived.setText(" --Control Station online-- " + newline);
 		JScrollPane commlogpaneReceived = new JScrollPane(commlogReceived);
 		commlogpaneReceived.setSize(300,300);
 		commlogpaneReceived.setFocusable(false);
 		commlogpaneReceived.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
 		data3.addTab("Received", commlogpaneReceived);
 
 		//buttons
 		connect = new JButton("Connect");
 		connect.setSize(120,110);
 		connect.setLocation(0,0);
 		connect.setFocusable(false);
 		connect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//commlog.append("Connecting..." + newline);
 				control.getController().connect();
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons1.add(connect);
 
 		home = new JButton("Home");
 		home.setSize(160,30);
 		home.setLocation(150,0);
 		home.setFocusable(false);
 		home.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				control.getController().goHome();
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons1.add(home);
 
 		requestdata = new JButton("Request System Data");
 		requestdata.setSize(160,30);
 		requestdata.setLocation(150,40);
 		requestdata.setFocusable(false);
 		requestdata.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				control.getController().requestSystemStatusData();
 				//commlog.append("Requesting data..." + newline);
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons1.add(requestdata);
 
 		debug = new JButton("Launch Debugger");
 		debug.setSize(160,30);
 		debug.setLocation(150,80);
 		debug.setFocusable(false);
 		debug.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//control method goes here once debugger code is added
 				//commlog.append("Launched debugger" + newline);
 			}
 		});
 		buttons1.add(debug);
 
 		speedlabel = new JLabel("Motor Speed");
 		speedlabel.setSize(100,50);
 		speedlabel.setLocation(110,0);
 		speedlabel.setHorizontalAlignment(0);
 		buttons2.add(speedlabel);
 
 		speed = new JTextField();
 		speed.setSize(60,25);
 		speed.setLocation(130,40);
 		speed.setEditable(false);
 		speed.setFocusable(false);
 		speed.setHorizontalAlignment(JTextField.CENTER);
 		speed.setText(varspeed+"");
 		buttons2.add(speed);
 
 		speedup = new JButton("+");
 		speedup.setSize(41,41);
 		speedup.setLocation(160,75);
 		speedup.setFocusable(false);
 		speedup.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				if(varspeed < varspeedMAX-1)
 					varspeed += 1;
 				else
 					varspeed = varspeedMAX;
 				speed.setText(varspeed+"");
 				control.getController().setSpeed(varspeed);
 			}
 		});
 		buttons2.add(speedup);
 
 		speeddown = new JButton("-");
 		speeddown.setSize(41,41);
 		speeddown.setLocation(120,75);
 		speeddown.setFocusable(false);
 		speeddown.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{				
 				if(varspeed > varspeedMIN+1)
 					varspeed -= 1;
 				else
 					varspeed = varspeedMIN;
 				speed.setText(varspeed+"");
 			//	control.getController().setSpeed(varspeed);
 			}
 		});
 		buttons2.add(speeddown);
 
 		stop = new JButton("Stop");
 		stop.setSize(60,50);
 		stop.setLocation(270,30);
 		stop.setFocusable(false);
 		stop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				control.getController().stop();
 			//	commlog.append("Stopped" + newline);
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons2.add(stop);
 
 		forward = new JButton(" ^ ");
 		forward.setSize(50,50);
 		forward.setLocation(60+padDisplaceX, 0+padDisplaceY);
 		forward.setFocusable(false);
 		forward.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				control.getController().moveForward(false, false);
 			//	commlog.append("Moving forward..." + newline);
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons2.add(forward);
 
 		backward = new JButton(" v ");
 		backward.setSize(50,50);
 		backward.setLocation(60+padDisplaceX, 60+padDisplaceY);
 		backward.setFocusable(false);
 		backward.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				control.getController().moveBackward(false, false);
 			//	commlog.append("Moving backward..." + newline);
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons2.add(backward);
 
 		left = new JButton(" < ");
 		left.setSize(50,50);
 		left.setLocation(0+padDisplaceX, 60+padDisplaceY);
 		left.setFocusable(false);
 		left.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				control.getController().moveLeft();
 			//	commlog.append("Turning left..." + newline);
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons2.add(left);
 
 		right = new JButton(" > ");
 		right.setSize(50,50);
 		right.setLocation(120+padDisplaceX, 60+padDisplaceY);
 		right.setFocusable(false);
 		right.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				control.getController().moveRight();
 			//	commlog.append("Turning right..." + newline);
 				basepane.requestFocusInWindow();
 			}
 		});
 		buttons2.add(right);
 
 		basepane.addKeyListener(new KeyListener() { //Listener allowing the user to use keyboard buttons 
 			//instead of on screen buttons
 			public void keyPressed(KeyEvent e) {
 				int keyCode = e.getKeyCode();
 				if(keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
 					if(!keysCurrentlyPressed.contains("up")) {
 						control.getController().moveForward(false, false);
 			//			commlog.append("Moving forward..." + newline);
 						keysCurrentlyPressed.add("up");
 					}
 				}
 				else if(keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
 					if(!keysCurrentlyPressed.contains("down")) {
 						control.getController().moveBackward(false, false);
 			//			commlog.append("Moving backward..." + newline);
 						keysCurrentlyPressed.add("down");
 					}
 				}
 				else if(keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
 					if(!keysCurrentlyPressed.contains("left")) {
 						keysCurrentlyPressed.add("left");						
 						control.getController().moveLeft();
						commlog.append("Turning left..." + newline);
 
 					}
 				}
 				else if(keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
 					if(!keysCurrentlyPressed.contains("right")) {
 						keysCurrentlyPressed.add("right");
 						control.getController().moveRight();
 			//			commlog.append("Turning right..." + newline);
 					}
 				}
 				else if(keyCode == KeyEvent.VK_Q) {
 					if(!keysCurrentlyPressed.contains("curveForwardLeft")) {
 						keysCurrentlyPressed.add("curveForwardLeft");
 						control.getController().moveForward(true, false);
 			//			commlog.append("Curving forward and left..." + newline);
 					}
 				}
 				else if(keyCode == KeyEvent.VK_E) {
 					if(!keysCurrentlyPressed.contains("curveForwardRight")) {
 						keysCurrentlyPressed.add("curveForwardRight");
 						control.getController().moveForward(false, true);
 			//			commlog.append("Curving forward and right..." + newline);
 					}
 				}
 				else if(keyCode == KeyEvent.VK_Z) {
 					if(!keysCurrentlyPressed.contains("curveBackwardLeft")) {
 						keysCurrentlyPressed.add("curveBackwardLeft");
 						control.getController().moveBackward(true, false);
 				//		commlog.append("Curving backward and left..." + newline);
 					}
 				}
 				else if(keyCode == KeyEvent.VK_C) {
 					if(!keysCurrentlyPressed.contains("curveBackwardRight")) {
 						keysCurrentlyPressed.add("curveBackwardRight");
 						control.getController().moveBackward(false, true);
 				//		commlog.append("Curving backward and right..." + newline);
 					}
 				}
 			}
 			public void keyReleased(KeyEvent e) {
 				String command = "";
 				int keyCode = e.getKeyCode();
 				if(keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
 					command = "up";
 				}
 				else if(keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
 					command = "down";
 				}
 				else if(keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
 					command = "left";
 				}
 				else if(keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
 					command = "right";
 				}
 				else if(keyCode == KeyEvent.VK_Q) {
 					command = "curveForwardLeft";
 				}
 				else if(keyCode == KeyEvent.VK_E) {
 					command = "curveForwardRight";
 				}
 				else if(keyCode == KeyEvent.VK_Z) {
 					command = "curveBackwardLeft";
 				}
 				else if(keyCode == KeyEvent.VK_C) {
 					command = "curveBackwardRight";
 				}
 				keysCurrentlyPressed.remove(command);
 				//System.out.println(keysCurrentlyPressed);
 				control.getController().stop();
 		//		commlog.append("Stopped" + newline);
 			}
 			public void keyTyped(KeyEvent e) {
 
 			}
 		});
 		basepane.setFocusable(true);
 		basepane.requestFocusInWindow();
 
 		Timer timer = new Timer(50, new ActionListener() { //Timer which updates the sensor displays on screen
 			public void actionPerformed(ActionEvent e) {
 				sounddata.setText("" + control.getExtSensor().getSound().getValue());
 				touch1data.setText("" + control.getExtSensor().getTouch().getValue());
 				ultradata.setText("" + control.getExtSensor().getUltrasonic().getValue());
 				lightdata.setText("" + control.getExtSensor().getLight().getValue());
 			}
 		});
 		timer.start();
 
		Timer systemStatusTimer = new Timer(1000, new ActionListener() { //Timer which requests sensor updates
 			//once per second
 			public void actionPerformed(ActionEvent e) {
 				control.getController().requestSystemStatusData();
 		//		commlog.append("Requesting data..." + newline);
 			}
 		});
 		systemStatusTimer.start();
 
 		basepane.setOpaque(true);
 		return basepane;
 	}
 	public void updateCommLog(ArrayList<Message> messageList) {
 		commlog.setText("");
 		commlogSent.setText("");
 		commlogReceived.setText("");
 		for(int i = 0; i < messageList.size(); i++) {
 			Message message = messageList.get(i);
 			commlog.append(message.getMessageContent() + newline);
 			if(message.getTypeOfMessage().equals("Received")) {
 				commlogReceived.append(message.getMessageContent());
 			}
 			if(message.getTypeOfMessage().equals("Sent")) {
 				commlogSent.append(message.getMessageContent());
 			}
 		}
 		
 	}
 	public void updateCommLog(Message message) {
		commlog.append(message + newline);
 		if(message.getTypeOfMessage().equals("Received")) {
			commlogReceived.append(message.getMessageContent());
 		}
 		if(message.getTypeOfMessage().equals("Sent")) {
			commlogSent.append(message.getMessageContent());
 		}
 	}
 	/*
 	public static void init()
 	{
 		JFrame frame = new JFrame("Control Station");
 		GUI controlPanel = new GUI();
 		frame.setContentPane(controlPanel.createContentPane());
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocation(20,20);
 		frame.setSize(1000,600);
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				init();
 			}
 		});
 	}
 	 */
 }
