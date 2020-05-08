 // XXX TODO add in error correction
 
 package edu.cmu.ri.mrpl.example;
 /*
 
  * Sample code for the 16x62 class taught at the Robotics Institute
  * of Carnegie Mellon University
  *
  * written from scratch by Martin Stolle in 2006
  *
  * inspired by code started by Illah Nourbakhsh and used
  * for many years.
  */
 
 import java.io.*;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 
 import javax.swing.*;
 
 import edu.cmu.ri.mrpl.*;
 import edu.cmu.ri.mrpl.Robot;
 import edu.cmu.ri.mrpl.kinematics2D.Angle;
 import edu.cmu.ri.mrpl.kinematics2D.LineSegment;
 import edu.cmu.ri.mrpl.kinematics2D.RealPoint2D;
 import edu.cmu.ri.mrpl.kinematics2D.RealPose2D;
 import edu.cmu.ri.mrpl.kinematics2D.Vector2D;
 import edu.cmu.ri.mrpl.util.AngleMath;
 import static java.lang.Math.*;
 
 public class SampleRobotApp extends JFrame implements ActionListener, TaskController {
 
 	private Robot robot;
 	private SonarConsole sc;
 
 	private JFrame scFrame;
 
 	private JButton connectButton;
 	private JButton disconnectButton;
 
 	private JButton pauseButton;
 	private JButton waitButton;
 	private JButton turnToButton;
 	private JButton goToButton;
 	private JButton poseToButton;
 	private JFormattedTextField argumentField;
 	private JFormattedTextField remainingField;
 	private JFormattedTextField xField;
 	private JFormattedTextField yField;
 	private JFormattedTextField thField;
 
 	private JButton stopButton;
 	private JButton quitButton;
 	private JButton executeButton;
 
 	private java.util.LinkedList<Task> upcomingTasks;
 
 	static final int DEFAULT_ROOM_SIZE = 4;
 	
 	final double DISTANCE_TOLERANCE = 0.01;
 	final double ANGLE_TOLERANCE = 0.01;
 	final double SPEED_TOLERANCE = 0.01;
 
 	private Task curTask = null;
 
 	public SampleRobotApp() {
 		super("Kick Ass Sample Robot App");
 
 		upcomingTasks = new LinkedList<Task>();
 
 		connectButton = new JButton("connect");
 		disconnectButton = new JButton("disconnect");
 
 		pauseButton = new JButton("Pause!");
 		waitButton = new JButton("Wait!");
 		turnToButton = new JButton("Turn to angle!");
 		goToButton = new JButton("Go to distance!");
 		poseToButton = new JButton("Go to pose!");
 		argumentField = new JFormattedTextField(NumberFormat.getInstance());
 		argumentField.setValue(1);
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMaximumFractionDigits(2);
 		remainingField = new JFormattedTextField(nf);
 		remainingField.setEditable(false);
 		xField = new JFormattedTextField(NumberFormat.getInstance());
 		xField.setValue(1);
 		yField = new JFormattedTextField(NumberFormat.getInstance());
 		yField.setValue(1);
 		thField = new JFormattedTextField(NumberFormat.getInstance());
 		thField.setValue(1);
 		stopButton = new JButton(">> stop <<");
 		quitButton = new JButton(">> quit <<");
 		executeButton = new JButton("Execute File");
 
 		connectButton.addActionListener(this);
 		disconnectButton.addActionListener(this);
 
 		pauseButton.addActionListener(this);
 		waitButton.addActionListener(this);
 		turnToButton.addActionListener(this);
 		goToButton.addActionListener(this);
 		poseToButton.addActionListener(this);
 		stopButton.addActionListener(this);
 		quitButton.addActionListener(this);
 		executeButton.addActionListener(this);
 
 		Container main = getContentPane();
 		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
 		Box box;
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(connectButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(disconnectButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(pauseButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(waitButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(turnToButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(goToButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(poseToButton);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(executeButton);
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(argumentField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(remainingField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(xField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(yField);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(thField);
 		box.add(Box.createHorizontalStrut(30));
 
 		main.add(Box.createVerticalStrut(30));
 
 		box = Box.createHorizontalBox();
 		main.add(box);
 		box.add(stopButton);
 		box.add(Box.createHorizontalStrut(30));
 		box.add(quitButton);
 
 		main.add(Box.createVerticalStrut(30));
 
 		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				quit();
 			}
 		});
 
 
 		this.setLocationByPlatform(true);
 		this.pack();
 		// moved this line to the bottom to make the controls show up on top
 		//this.setVisible(true);
 
 		// construct the SonarConsole, but don't make it visible
 		scFrame = new JFrame("Sonar Console");
 		scFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		sc = new SonarConsole();
 		scFrame.add(sc);
 		scFrame.pack();
 
 		this.setVisible(true);
 
 		String message = "Is this running on the robot?";
 		int isRobot = JOptionPane.showConfirmDialog(null, message, message, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 		if (isRobot == JOptionPane.YES_OPTION) {
 			robot = new ScoutRobot();
 		}
 		else {
 			robot = new SimRobot();
 		}
 
 		robot.setAccel(0.25, 0.25);
 	}
 
 	// call from GUI thread
 	public void connect() {
 		try {
 			robot.connect();
 		} catch(IOException ioex) {
 			System.err.println("couldn't connect to robot:");
 			ioex.printStackTrace();
 		}
 	}
 
 	// call from GUI thread
 	public void disconnect() {
 		try {
 			robot.disconnect();
 		} catch(IOException ioex) {
 			System.err.println("couldn't disconnect from robot:");
 			ioex.printStackTrace();
 		}
 	}
 
 	// call from GUI thread
 	public synchronized void stop() {
 		upcomingTasks.clear();
 		if (curTask!=null)
 			curTask.pleaseStop();
 	}
 
 	// call from GUI thread
 	public void quit() {
 
 		synchronized(this) {
 			if (curTask!=null) {
 				curTask.pleaseStop();
 
 				try {
 					this.wait(1000);
 				} catch(InterruptedException iex) {
 					System.err.println("interrupted while waiting for worker termination signal");
 				}
 			}
 
 			if (curTask!=null) {
 				System.err.println("exiting even though robot is running!");
 			} else {
 				disconnect();
 			}
 		}
 
 		System.exit(0);
 	}
 
 	// invoked from non-GUI thread to hide/show SonarConsole
 	public void showSC() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				scFrame.setLocationByPlatform(true);
 				scFrame.setVisible(true);
 			}
 		});
 	}
 
 	// invoked from non-GUI thread to hide/show SonarConsole
 	public void hideSC() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				scFrame.setVisible(false);
 			}
 		});
 	}
 
 	// XXX this doesn't work quite right if called multiple times when the task list is non-empty
 	// something to do with how canStart works vs checking for a null curTask
 	private void startUpcomingTasks () {
 		System.err.println("startUpcomingTasks");
 		if (!upcomingTasks.isEmpty() && curTask == null) {
 			Task next = upcomingTasks.remove();
 			System.err.println("attempting to start " + next);
 			//if (canStart(next)) {
 			new Thread(next).start();
 			//}
 		}
 		else {
 			System.err.println("no more tasks left or task is currently running");
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 
 		double argument = Double.parseDouble(argumentField.getText());
 		double x = Double.parseDouble(xField.getText());
 		double y = Double.parseDouble(yField.getText());
 		double th = Double.parseDouble(thField.getText());
 
 		if (source==connectButton) {
 			connect();
 		} else if ( source==disconnectButton ) {
 			disconnect();
 		} else if ( source==stopButton ) {
 			stop();
 		} else if ( source==quitButton ) {
 			quit();
 		} else if ( source==pauseButton ) {
 			upcomingTasks.add(new PauseTask(this));
 			startUpcomingTasks();
 		} else if ( source==waitButton ) {
 			upcomingTasks.add(new WaitTask(this, argument));
 			startUpcomingTasks();
 		} else if ( source==turnToButton ) {
 			upcomingTasks.add(new TurnToTask(this, argument));
 			startUpcomingTasks();
 		} else if ( source==goToButton ) {
 			upcomingTasks.add(new GoToTask(this, argument));
 			startUpcomingTasks();
 		} else if (source == poseToButton) {
 			upcomingTasks.add(new PoseToTask(this, x, y, th));
 			startUpcomingTasks();
 		} else if ( source==executeButton ) {
 			addFileTasksToQueue();
 		}
 	}
 
 	public void addFileTasksToQueue() {
 		JFileChooser chooser = new JFileChooser();
 		int returnVal = chooser.showOpenDialog(this);
 		if(returnVal == JFileChooser.APPROVE_OPTION) {
 			System.out.println("You chose to open this file: " +
 					chooser.getSelectedFile().getName());
 		}
 
 		CommandSequence commandSequence = new CommandSequence();
 
 		try {
 			commandSequence.readFile(chooser.getSelectedFile().getPath());
 		} catch (IOException e) {
 			System.err.println("Couldn't read file");
 		}
 
 		executeActionList(commandSequence);
 	}
 
 	public synchronized boolean canStart(Task t) {
 		if (curTask!=null)
 			return false;
 
 		curTask = t;
 		return true;
 	}
 
 	public synchronized void finished(Task t) {
 		if (curTask!=t) {
 			System.err.println("ignoring finished() from unknown task "+t+"!");
 			return;
 		}
 		curTask=null;
 		startUpcomingTasks();
 		this.notifyAll();
 	}
 
 	public static void main(String[] argv) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new SampleRobotApp();
 			}
 		});
 	}
 
 	public void executeActionList (ArrayList<Command> commands) {
 		for (Command cmd : commands) {
 			System.err.println(cmd.type);
 			switch (cmd.type) {
 			case PAUSE:
 				upcomingTasks.add(new PauseTask(this));
 				break;
 
 			case TURNTO:
 				upcomingTasks.add(new TurnToTask(this,
 						((Command.AngleArgument) cmd.argument).angle.angleValue()));
 				break;
 
 			case GOTO:
 				upcomingTasks.add(new GoToTask(this,
 						((Command.LengthArgument) cmd.argument).d));
 				break;
 
 			case WAIT:
 				upcomingTasks.add(new WaitTask(this,
 						((Command.LengthArgument) cmd.argument).d));
 				break;
 				
 			case POSETO:
 				RealPose2D pose = ((Command.PoseArgument) cmd.argument).pose;
 				upcomingTasks.add(new PoseToTask(this,
 						pose.getX(),
 						pose.getY(),
 						pose.getTh()));
 				break;
 				
 			case FOLLOWPATH:
 				Path path = ((Command.PathArgument) cmd.argument).path;
 				upcomingTasks.add(new FollowPathTask(this, path, 0.1));
 				break;
 
 			default:
 				System.err.println("unimplemented Command type: " + cmd.type);
 				new Speech().speak("unimplemented Command type: " + cmd.type);
 				break;
 			}
 		}
 
 		startUpcomingTasks();
 	}
 
 	// here we implement the different robot controllers
 	// as Tasks
 	//
 
 	class PauseTask extends Task implements KeyListener, MouseListener {
 
 		Speech speech;
 		boolean done;
 
 		PauseTask(TaskController tc) {
 			super(tc);
 		}
 
 		public void taskRun() {
 			speech = new Speech();
 			speech.speak("Waiting for you");
 			done = false;
 			long startTime = System.currentTimeMillis();
 
 			// these only catch events on the specific object
 			pauseButton.addKeyListener(this);
 			SampleRobotApp.this.addKeyListener(this);
 			SampleRobotApp.this.addMouseListener(this);
 
 			while(!shouldStop() && !done) {
 				try {
 					Thread.sleep(50);
 				} catch(InterruptedException iex) {
 					System.err.println("pause sleep interrupted");
 				}
 			}
 
 			long endTime = System.currentTimeMillis();
 			double elapsedTime = (endTime - startTime) / 1000.0;
 			remainingField.setValue(elapsedTime);
 		}
 
 		public String toString() {
 			return "pause task";
 		}
 
 		public void keyPressed(KeyEvent e) {
 			done = true;
 			pauseButton.removeKeyListener(this);
 			SampleRobotApp.this.removeKeyListener(this);
 		}
 
 		public void mouseClicked(MouseEvent arg0) {
 			done = true;
 			SampleRobotApp.this.removeMouseListener(this);
 		}
 
 		public void keyReleased(KeyEvent e) {}
 		public void keyTyped(KeyEvent e) {}
 		public void mouseEntered(MouseEvent arg0) {}
 		public void mouseExited(MouseEvent arg0) {}
 		public void mousePressed(MouseEvent arg0) {}
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 
 	class WaitTask extends Task {
 
 		Speech speech;
 		double duration;
 		private long startTime;
 
 		WaitTask(TaskController tc, double d) {
 			super(tc);
 			setDuration(d);
 		}
 
 		public void setDuration (double d) {
 			duration = d;
 		}
 
 		public void taskRun() {
 			speech = new Speech();
 			speech.speak("Waiting " + AngleMath.roundTo2(duration) + " seconds");
 			startTime = System.currentTimeMillis();
 			double elapsed = elapsedTime();
 
 			while(!shouldStop() && elapsed < duration) {
 				elapsed = elapsedTime();
 				if (elapsed >= duration) {
 					break;
 				}
 				try {
 					remainingField.setValue(duration - elapsed);
 
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("wait sleep interrupted");
 				}
 			}
 			// final update
 			remainingField.setText(duration - elapsed + "");
 		}
 
 		private double elapsedTime() {
 			return (System.currentTimeMillis() - startTime) / 1000.0;
 		}
 
 		public String toString() {
 			return "wait task: " + duration;
 		}
 	}
 
 	class TurnToTask extends Task {
 
 		Speech speech;
 		double desiredAngle;
 		Controller controller;
 
 		TurnToTask(TaskController tc, double a) {
 			super(tc);
 			setDesiredAngle(a);
 		}
 
 		public void setDesiredAngle (double a) {
 			desiredAngle = a;
 			//System.out.println("desiredAngle: " + desiredAngle);
 		}
 
 		public void taskRun() {
 			speech = new Speech();
 			controller = new Controller(robot);
 			final double Kp = .4;
 			final double Kd = 95;
 			double u = 0;
 			robot.updateState();
 			double curAngle = Angle.normalize(robot.getHeading());
 			double lastAngle = curAngle;
 			double curTime = System.nanoTime();
 			double lastTime = curTime;
 			double destAngle = Angle.normalize(curAngle + desiredAngle);
 			//System.out.println("destAngle: " + destAngle);
 			double angleErr = Angle.normalize(destAngle - curAngle);
 
 			final double ANGLE_TOLERANCE = 0.01;
 			final double SPEED_TOLERANCE = 0.01;
 
 			while(!shouldStop() &&
 					(abs(angleErr) > ANGLE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				//System.out.println("angleErr = " + angleErr);
 				robot.updateState();
 				lastAngle = curAngle;
 				lastTime = curTime;
 				curAngle = Angle.normalize(robot.getHeading());
 				//curTime = System.nanoTime();
 				curTime = System.currentTimeMillis();
 				angleErr = Angle.normalize(destAngle - curAngle);
 
 				double angleTraveled = Angle.normalize(curAngle - lastAngle);
 				boolean progressMade = signum(angleTraveled) == signum(angleErr);
 
 				double pterm = Kp*abs(angleErr);
 				double dterm = Kd*(abs(angleTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 				u = pterm + dterm;
 				double direction = signum(angleErr);
 				//System.err.println(angleErr + " off, speed = " + u*direction);
 				double speed = direction*u;
 				controller.setVel(-speed, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("turn-to sleep interrupted");
 				}
 			}
 
 			speech.speak("error " + AngleMath.roundTo2(angleErr*1000) + " milliradians");
 			//System.out.println("curAngle: " + curAngle);
 			controller.stop();
 		}
 
 		public String toString() {
 			return "turn-to task: " + desiredAngle;
 		}
 	}
 
 	class GoToTask extends Task {
 
 		double desiredDistance;
 		RealPose2D robotStartedHere;
 		Controller controller;
 		Perceptor perceptor;
 		Speech speech;
 
 		GoToTask(TaskController tc, double d) {
 			super(tc);
 			setDesiredDistance(d);
 		}
 
 		public void setDesiredDistance (double d) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			desiredDistance = d;
 		}
 
 		// setDesiredDistance should be called before calling this method
 		public void taskRun() {
 			speech = new Speech();
 			robot.updateState();
 			final double Kp = 1.5;
 			final double Kd = 15;
 			double u = 0;
 			robotStartedHere = perceptor.getWorldPose();
 			RealPose2D curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			RealPose2D lastPoseRelStart = curPoseRelStart;
 			double curTime = System.nanoTime();
 			double lastTime = curTime;
 			final Point2D destPointRelStart = new Point2D.Double(desiredDistance, 0);
 			double distanceErr = destPointRelStart.getX() - curPoseRelStart.getX();
 
 			final double DISTANCE_TOLERANCE = 0.005;
 			final double SPEED_TOLERANCE = 0.01;
 
 			while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				robot.updateState();
 				lastPoseRelStart = curPoseRelStart;
 				lastTime = curTime;
 				curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 				curTime = System.currentTimeMillis();
 				distanceErr = destPointRelStart.getX() - curPoseRelStart.getX();
 
 				double distanceTraveled = curPoseRelStart.getX() - lastPoseRelStart.getX();
 				boolean progressMade = signum(distanceTraveled) == signum(distanceErr);
 
 				double pterm = Kp*distanceErr;
 				double dterm = Kd*(abs(distanceTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 				u = pterm + dterm;
 				double speed = u;
 				controller.setVel(speed, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("go-to sleep interrupted");
 				}
 			}
 
 			speech.speak("error " + AngleMath.roundTo2(distanceErr*1000) + " millimeters");
 			controller.stop();
 		}
 
 		public String toString() {
 			return "go-to task: " + desiredDistance;
 		}
 	}
 	
 	private static double calculateArcLength (Point2D dest, double radius) {
 		double x = dest.getX();
 		double y = dest.getY();
 		double angle;
 		if (radius > 0) {
 			angle = Math.atan2(x, radius - y);
 		}
 		else {
 			angle = Math.atan2(x, y - radius);
 		}
 		double arcLength = radius*angle;
 		if (radius < 0) {
 			arcLength *= -1;
 		}
 		return arcLength;
 	}
 
 	// returns the radius and arc length to get to a given point relative to the robot
 	private static double[] calculateArc (Point2D dest) {
 		double x = dest.getX();
 		double y = dest.getY();
 		if (y == 0) {
 			return new double[]{Double.MAX_VALUE, x};
 		}
 		double radius = (x*x + y*y) / (2*y);
 		double arcLength = calculateArcLength(dest, radius);
 		if (arcLength < 0) {
 			System.err.println("NEGATIVE ARC LENGTH");
 		}
 		return new double[]{radius, arcLength};
 	}
 	
 	class PoseToTask extends Task {
 		RealPose2D destPoseRelStart;
 		Controller controller;
 		Perceptor perceptor;
 		Speech speech;
 		TaskController tc;
 
 		PoseToTask(TaskController tc, double x, double y, double th) {
 			super(tc);
 			this.tc = tc;
 			setDesiredPose(x,y,th);
 		}
 
 		public void setDesiredPose (double x, double y, double th) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			destPoseRelStart = new RealPose2D(x,y,th);
 		}
 
 		// setDesiredPose should be called before calling this method
 		public void taskRun() {
 			speech = new Speech();
 			robot.updateState();
 			
 			final double Kp = 2;
 			final double Kd = 10;
 
 			RealPose2D robotStartedHere = perceptor.getWorldPose();
 			RealPose2D curPoseRelStart = new RealPose2D();
             RealPose2D lastPoseRelStart;
             double curTime = System.nanoTime();
             double lastTime;
             
 			// calculate arc info
             double[] arcInfo = calculateArc(destPoseRelStart.getPosition());
 			double radius = arcInfo[0];
 			double distanceErr = arcInfo[1];
 			System.err.printf("arc: radius=%.2f, distanceErr=%.2f\n", radius, distanceErr);
 			
 			do {
 				// update everything
 				lastPoseRelStart = curPoseRelStart;
 				lastTime = curTime;
 				robot.updateState();
 				curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 				curTime = System.currentTimeMillis();
 				
 				// figure out how far we've been
 				RealPose2D curPoseRelLast = RealPose2D.multiply(
 						lastPoseRelStart.inverse(), curPoseRelStart);
 				double distanceTraveled = calculateArcLength(curPoseRelLast.getPosition(), radius);
 				
 				// compute whether progress has been made
 				RealPose2D destPoseRelCur = RealPose2D.multiply(
 						curPoseRelStart.inverse(), destPoseRelStart);
 				distanceErr = calculateArcLength(destPoseRelCur.getPosition(), radius);
 		        boolean progressMade = signum(distanceTraveled) == signum(distanceErr);
 
 				// calculate dterm
 		        double dterm = Kd*(abs(distanceTraveled))/(curTime-lastTime);
 		        if (progressMade) {
 		        	dterm *= -1;
 		        }
 		        
 				// set wheel speeds
 				double pterm = Kp*distanceErr;
 				double speed = pterm;// + dterm;
 				controller.setCurvVel(1.0/radius, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("go-to sleep interrupted");
 				}
 			} while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE));
 
 			System.out.println("Current x = "+curPoseRelStart.getX());
 			System.out.println("Current y = "+curPoseRelStart.getY());
 			System.out.println("FINISHED X/Y. STARTING THETA");
 
 			speech.speak("remaining error " + AngleMath.roundTo2(distanceErr*1000)
 					+ " millimeters");
 			
 			upcomingTasks.add(0, new TurnToTask(tc, Angle.normalize(
 					destPoseRelStart.getTh() + robotStartedHere.getTh() - robot.getHeading())));
 			controller.stop();
 		}
 
 		public String toString() {
 			return "pose-to task: " + destPoseRelStart;
 		}
 	}
 
 	class FollowPathTask extends Task {
 		ArrayList<RealPose2D> desiredPath;
 		ArrayList<Line2D> pathSegments;
 		double maxDeviation;
 		RealPose2D robotStartedHere;
 		Controller controller;
 		Perceptor perceptor;
 		Speech speech;
 		
 		private final double LOOKAHEAD_DISTANCE = 0.25;
 		private Line2D closestSegment;
		
		private TaskController tc;
 
 		FollowPathTask(TaskController tc, ArrayList<RealPose2D> poses, double maxDeviation) {
 			super(tc);
			this.tc = tc;
 			setDesiredPath(poses, maxDeviation);
 		}
 
 		public void setDesiredPath (ArrayList<RealPose2D> poses, double maxDeviation) {
 			controller = new Controller(robot);
 			perceptor = new Perceptor(robot);
 			desiredPath = new ArrayList<RealPose2D>(poses);
 			this.maxDeviation = maxDeviation;
 			
 			// construct list of line segments
 			pathSegments = new ArrayList<Line2D>();
 			for (int i = 1; i < desiredPath.size(); i++) {
 				pathSegments.add(new Line2D.Double(desiredPath.get(i-1).getPosition(),
 						desiredPath.get(i).getPosition()));
 			}
 			System.err.println(desiredPath.size() + " PATH SEGMENTS");
 		}
 		
 		private RealPoint2D findClosestPoint () {
 			RealPoint2D closestPoint = new RealPoint2D();
 			double minDistSquared = Double.MAX_VALUE;
 			Point2D robotRelWorld = perceptor.getWorldPose().getPosition();
 			Line2D oldClosestSegment = closestSegment;
 			for (int i = 0; i < pathSegments.size(); i++) {
 				Line2D segment = pathSegments.get(i);
 				RealPoint2D tmp = new RealPoint2D();
 				double tmpDistSquared = LineSegment.closestPointOnLineSegment(segment, robotRelWorld, tmp);
 				if (tmpDistSquared <= minDistSquared) {
 					minDistSquared = tmpDistSquared;
 					closestPoint.setLocation(tmp);
 					closestSegment = segment;
 				}
 			}
 			if (oldClosestSegment != null && !closestSegment.equals(oldClosestSegment)) {
 				System.out.printf("CHANGING SEGMENTS FROM %d to %d\n",
 						pathSegments.indexOf(oldClosestSegment), pathSegments.indexOf(closestSegment));
 			}
 			return closestPoint;
 		}
 		
 		// TODO make this work properly. wtf
 		private Point2D findLookaheadPoint () {
 			Point2D closestPoint = findClosestPoint();
 			ArrayList<Vector2D> points = new ArrayList<Vector2D>();
 			double lookaheadDistance = LOOKAHEAD_DISTANCE;
 			Vector2D lookaheadPoint = new Vector2D(closestPoint.getX(), closestPoint.getY());
 			for (int i = pathSegments.indexOf(closestSegment); i < pathSegments.size(); i++) {
 				points.clear();
 				Line2D segment = new Line2D.Double(closestPoint, pathSegments.get(i).getP2());
 				Point2D segmentEnd = segment.getP2();
 				double segmentLength = closestPoint.distance(segmentEnd);
 				
 				if (segmentLength < lookaheadDistance) {
 					// if it's not on this segment, need to check next segment
 					// adjust closest point and lookahead distance accordingly
 					lookaheadDistance -= segmentLength;
 					closestPoint = segmentEnd;
 					System.err.println("LOOKAHEAD ON SEGMENT AFTER " + i);
 					continue;
 				}
 				else if (LineSegment.radialPointsOnLineSegment(segment, lookaheadDistance, closestPoint, points)) {
 					lookaheadPoint = points.get(0);
 				}
 			}
 			return new RealPoint2D(lookaheadPoint.x, lookaheadPoint.y);
 		}
 
 		// setDesiredPath should be called before calling this method
 		public void taskRun() {
 			robot.updateState();
			RealPose2D context = new RealPose2D();
			for (int i = 0; i < desiredPath.size(); i++) {
				RealPose2D nextPose = desiredPath.get(i);
				RealPose2D transformed = RealPose2D.multiply(context, nextPose);
				double x, y, th;
				x = transformed.getX();
				y = transformed.getY();
				th = transformed.getTh();
				upcomingTasks.add(i, new PoseToTask(tc, x, y, th));
				context = nextPose.inverse();
			}
			/* XXX need to make this actually work
			speech = new Speech();
 			double Kp = 2;
 			//double Kd = 10;
 			//double u = 0;
 
 			robotStartedHere = perceptor.getWorldPose();
 			RealPose2D curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			Point2D destPointRelStart = findLookaheadPoint();
 
 			double[] arcInfo = calculateArc(destPointRelStart);
 			double distanceErr = arcInfo[1];
 
 			while(!shouldStop() &&
 					(abs(distanceErr) > DISTANCE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				robot.updateState();
 				curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 				
 				// remove the old lookahead point
 				if (robot instanceof SimRobot) {
 					((SimRobot) robot).deleteObstacle(destPointRelStart.getX(), destPointRelStart.getY());
 				}
 				
 				// recalculate destination (code copied from before loop)
 				destPointRelStart = findLookaheadPoint();
 				
 				// draw the lookahead point
 				if (robot instanceof SimRobot) {
 					if (destPointRelStart.distance(perceptor.getWorldPose().getPosition()) > .25) {
 						((SimRobot) robot).addObstacle(destPointRelStart.getX(), destPointRelStart.getY(), 0.01);
 					}
 				}
 				
 				arcInfo = calculateArc(destPointRelStart);
 				controller.setCurvVel(1.0/arcInfo[0], Kp*arcInfo[1]);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("go-to sleep interrupted");
 				}
 			}
 			System.out.println("Current x = "+curPoseRelStart.getX());
 			System.out.println("Current y = "+curPoseRelStart.getY());
 
 			// TODO figure out what to do at the end of the path (turnto, etc)
 			/* XXX copied code from TurnToTask.
 			// Check whether the "rapid transition" rule is broken here.
 			System.out.println("FINISHED X/Y. STARTING THETA");
 			Kp = .4;
 			Kd = 95;
 			u = 0;
 			robot.updateState();
 			double curAngle = Angle.normalize(robot.getHeading());
 			double lastAngle = curAngle;
 			curTime = System.nanoTime();
 			lastTime = curTime;
 			curPoseRelStart = perceptor.getRelPose(robotStartedHere);
 			double desiredAngle = Angle.normalize(desiredPose.getTh() - curPoseRelStart.getTh());
 			double destAngle = Angle.normalize(curAngle + desiredAngle);
 			//System.out.println("destAngle: " + destAngle);
 			double angleErr = Angle.normalize(destAngle - curAngle);
 
 			final double ANGLE_TOLERANCE = 0.01;
 			SPEED_TOLERANCE = 0.01;
 
 			while(!shouldStop() &&
 					(abs(angleErr) > ANGLE_TOLERANCE
 							|| abs(robot.getVelLeft()) > SPEED_TOLERANCE))
 			{
 				//System.out.println("angleErr = " + angleErr);
 				robot.updateState();
 				lastAngle = curAngle;
 				lastTime = curTime;
 				curAngle = Angle.normalize(robot.getHeading());
 				//curTime = System.nanoTime();
 				curTime = System.currentTimeMillis();
 				angleErr = Angle.normalize(destAngle - curAngle);
 
 				double angleTraveled = Angle.normalize(curAngle - lastAngle);
 				boolean progressMade = signum(angleTraveled) == signum(angleErr);
 
 				double pterm = Kp*abs(angleErr);
 				double dterm = Kd*(abs(angleTraveled))/(curTime-lastTime);
 				if (progressMade) {
 					dterm *= -1;
 				}
 				//System.err.println("pterm = " + pterm + "\ndterm = " + dterm + "\n");
 				u = pterm + dterm;
 				double direction = signum(angleErr);
 				//System.err.println(angleErr + " off, speed = " + u*direction);
 				double speed = direction*u;
 				controller.setVel(-speed, speed);
 
 				try {
 					Thread.sleep(5);
 				} catch(InterruptedException iex) {
 					System.err.println("pose-to (turn) sleep interrupted");
 				}
 			}
			
 
 			speech.speak("remaining error " + AngleMath.roundTo2(distanceErr*1000)
 					+ " millimeters");
			//*/
 			controller.stop();
 		}
 
 		public String toString() {
 			return "follow-path task: " + desiredPath;
 		}
 	}
 	
 	private static final long serialVersionUID = 0;
 }
