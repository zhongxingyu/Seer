 package operator;
 
 
 import java.awt.Point;
 import javax.swing.JFrame;
 
 import maze.Maze;
 import draw.StdDraw;
 import controller.ServerQueue;
 import controller.SimpleServerQueue;
 
 public class OperatorGui {
 	public static final int WINDOW_LENGTH = 600;
 	public static final int WINDOW_HEIGHT = WINDOW_LENGTH;
 	
 	private Point robotPositions[] = new Point[4];
 	
 	private int tenMinuteTimerValue = 600;
 	private int threeSecTimerValue = 3;
 	
 	private boolean missionFailed = false;
 	private boolean twoMinWarning = false;
 	private boolean threeSecTimerWarning = false;
 	
 	private KeySender keySender;
 	private MazeReceiver mazeReceiver;
 	private RobotPositionReceiver[] robotPositionReceivers;
 	private WarningAndCounterReceiver wacr;
 	private Maze maze;
 	
 	private ServerQueue<Integer> sq;
 	private SimpleServerQueue<Maze> ssq;
 	
 	public OperatorGui(int robotId) {
 		sq = new ServerQueue<Integer>();
 		ssq = new SimpleServerQueue<Maze>();
 		robotPositionReceivers = new RobotPositionReceiver[4]; //for four robots
 		
 		StdDraw.setCanvasSize(WINDOW_HEIGHT, WINDOW_LENGTH);
 		
 		JFrame guiJFrame = StdDraw.getFrame();
 		
 		int keySenderPort = 10000 + 1000 * (robotId - 1);
 		keySender = new KeySender(keySenderPort);
 		guiJFrame.addKeyListener(keySender);
 		keySender.start();
 		
 		guiJFrame.setTitle("Robot operator " + robotId + " GUI");
 		
 		//Start a new thread for Maze Receiver
 		mazeReceiver = new MazeReceiver(ssq);
 		mazeReceiver.start();
 		maze = ssq.get(); //this will block until we receive a maze
 		
 		StdDraw.setXscale(0, maze.size + 2);
 		StdDraw.setYscale(0, maze.size + 2);
 		
		//add some stuff
		//JButton b1 = new JButton("Oh hai");
		//JButton b2 = new JButton("Hello");
		//JButton b3 = new JButton("Hows it going");
		//JPanel buttonPanel = new JPanel(); // use FlowLayout
		//buttonPanel.add(b1);
		//buttonPanel.add(b2);
		//buttonPanel.add(b3);
		//guiJFrame.add(buttonPanel);
		
		//draw(); //draw the frame before continuing
		
 		//Start a new thread for WACR
 		int port = 10003 + (robotId - 1) * 1000;
 		wacr = new WarningAndCounterReceiver(port, sq);
 		wacr.start();	
 
 		//Start 4 new threads, one for each robot
 		//This could be upgrade to use message passing
 		for (int i = 0; i < 4; i++) {
 			int portNumber = 10001 + (i * 1000);
 			robotPositionReceivers[i] = new RobotPositionReceiver(portNumber);
 			robotPositionReceivers[i].start();
 		}
 	}
 	
 	/**
 	 * Fetch data from the servers
 	 */
 	private void update() {
 		//update robot positions
 		for (int i = 0; i < 4; i++) {
 			robotPositions[i] = robotPositionReceivers[i].getRobotPosition();
 		}
 		
 		//fetch the counter data and warnings from the server queue
 		if (sq.get() == 1) missionFailed = true;
 		else missionFailed = false;
 		
 		int elapsedTime = sq.get();
 		if (elapsedTime <= 600) {
 			tenMinuteTimerValue = 600 - elapsedTime;
 		}
 		else {
 			tenMinuteTimerValue = 0;
 		}
 		
 		
 		threeSecTimerValue = sq.get();
 		
 		if (sq.get() == 1) twoMinWarning = true;
 		else twoMinWarning = false;
 		
 		if (sq.get() == 1) threeSecTimerWarning = true;
 		else threeSecTimerWarning = false;
 	}
 	
 	
 	/*********************************************************
 	 * Draw Methods
 	 *********************************************************/
 	private void draw() {
 		StdDraw.clear();
 		
 		if (maze != null) {
 			maze.draw();
 		}
 		
 		drawRobots();
 		drawCounters();
 		drawWarnings();
 		
 		StdDraw.show(100);
 	}
 
 	private void drawWarnings() {
 		//Draw the warnings
 		StdDraw.setPenColor(StdDraw.BLACK);
 
 		if (missionFailed) {
 			StdDraw.text(18, 0.5, "Mission Failed!");
 		}
 		
 		if (threeSecTimerWarning) {
 			StdDraw.textLeft(1, 0.5, "Not all switches have been turned!");
 		}
 		
 		if (twoMinWarning) {
 			StdDraw.textLeft(10, -0.5, "Please get to the Switch zone!");
 		}
 		
 	}
 
 	private void drawRobots() {
 		// Draw the four robots
 		StdDraw.setPenColor(StdDraw.RED);
 		for (int i = 0; i < 4; i++) {
 			if (robotPositions[i] != null) {
 				StdDraw.filledCircle(robotPositions[i].getX() + 0.5, robotPositions[i].getY() + 0.5, 0.375);
 			}
 		}
 	}
 	
 	private void drawCounters() {
 		// Draw the 10min & 3s counter
		StdDraw.setPenColor(StdDraw.BLACK);
		// Draw the 10min & 3s counter
		StdDraw.setPenColor(StdDraw.BLACK);
 		
		if(tenMinuteTimerValue<600) 
		{StdDraw.textLeft(0,-0.5, "Seconds left:" + String.valueOf(600-tenMinuteTimerValue));}
		else
		{
 			StdDraw.setPenColor(StdDraw.RED);
			StdDraw.textLeft(0,-0.5, "Seconds left:" + String.valueOf(0));
 		}
 		
 		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.textLeft(6,-0.5, "3sec timer:" + String.valueOf(threeSecTimerValue));
 	}
 
 	
 	/**
 	 * Creates the GUI for a single robot operator
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		int robotId = 1;
 		if(args.length > 0) {
 			robotId = Integer.parseInt(args[0]);
 		}
 		
 		OperatorGui operatorGui = new OperatorGui(robotId);
 		StdDraw.show(0);
 
 		while (true) {
 			operatorGui.update();
 			operatorGui.draw();
 		}
 	}
 
 }
