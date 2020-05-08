 import java.awt.*;
 import javax.swing.*;
 import javax.swing.border.Border;
 import java.awt.Font;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 /**
  * Controls the Session. Shows all the important information and data about the
  * current run session.
  *
  * <p>Note that in this version, the QuickStart button just adds 10s to the time
  * run, it doesn't actually do anything with a timer or anything.
  *
  * @version 1.1
  */
 public class ControlTab extends JPanel {
 	/* Buttons */
 	private JButton quickStart_Resume, pause_Stop, goal_Run_Start, speedUp, speedDown, inclineUp, inclineDown;
 	private JRadioButton[] radioButtonsGoalRun;
 	private ButtonGroup myButtonGroup;
 
 	/* Panels */
 	private JPanel panelMessage, panelInputOutput, panelOutputs, panelInputs;
 	private JPanel panelBasicFunc, panelGoalStart;
 	private JPanel panelSpeedIncline;
 	private JPanel panelTime, panelSpeed, panelIncline, panelDistance, panelCalories;
 	private JPanel panelDistanceRadio, panelDurationRadio, panelCaloriesRadio;
 	private JPanel panelGoals; ///
 	private JPanel[] panelSpeedArray, panelTimeArray, panelInclinationArray, panelDistanceArray, panelCalorieArray;
 
 	/* Labels */
 	private JLabel labelTimeCurVal, labelTimeElapsedVal, labelSpeedCurVal, labelSpeedAvgVal;
 	private JLabel labelInclineCurVal, labelDistanceCurVal, labelDistanceTargVal, labelCaloriesCurVal, labelCaloriesTargVal;
 	private JLabel message;
 	private JLabel time, speed, inclination, distance, calories;		///
 
 	/* Misc */
 	private Border blackline;
 	private Font currentFont;
 	private JTextField textFieldGoalDistance, textFieldGoalDuration, textFieldGoalCalories;
 	private JTextField goalTextField; 	///
 	private JSeparator mySeparator;		///
 
 	private int distanceTarget, caloriesTarget;
 	private Session myTreadmill;
 	private int age, weight;
 	private int timeMultiplier;
 	private Timer timer;
 	private Goal goal;
 
 
 	/* XXX: Should these be here? */
 	/** Maximum speed in tenths of a mile per hour */
 	private static final int MAX_SPEED = 150;
 	/** Maximum incline in percent */
 	private static final int MAX_INCLINE = 15;
 	private static final int FPS = 100;
 	private static final String DEFAULT_MESSAGE = "Welcome to the PiTech Treadmill Simulator!";
 
 	
 	public ControlTab() {
 		ButtonListener bl = new ButtonListener();
 
 		/* Set up buttons */
 		quickStart_Resume = new JButton("QuickStart");
 		quickStart_Resume.addActionListener(bl);
 		pause_Stop = new JButton("Stop");
 		pause_Stop.addActionListener(bl);
 		goal_Run_Start = new JButton("Goal Run Start");
 		speedUp = new JButton("Speed Up");
 		speedUp.addActionListener(bl);
 		speedDown = new JButton("Speed Down");
 		speedDown.addActionListener(bl);
 		inclineUp = new JButton("Incline Up");
 		inclineUp.addActionListener(bl);
 		inclineDown = new JButton("Incline Down");
 		inclineDown.addActionListener(bl);
 
 		radioButtonsGoalRun = new JRadioButton[3];
 		radioButtonsGoalRun[0] = new JRadioButton("distance", true);
 		radioButtonsGoalRun[1] = new JRadioButton("duration");
 		radioButtonsGoalRun[2] = new JRadioButton("calories");
 		myButtonGroup = new ButtonGroup();
 		myButtonGroup.add(radioButtonsGoalRun[0]);
 		myButtonGroup.add(radioButtonsGoalRun[1]);
 		myButtonGroup.add(radioButtonsGoalRun[2]);
 		
 		
 		/* Set up labels */
 		labelTimeCurVal  = new JLabel("00:00:00", JLabel.CENTER);
 		labelTimeElapsedVal = new JLabel("00:00:00", JLabel.CENTER);
		labelSpeedCurVal = new JLabel("0.000 mph", JLabel.CENTER);
		labelSpeedAvgVal = new JLabel("0.000 mph", JLabel.CENTER);
		labelInclineCurVal = new JLabel("0 %", JLabel.CENTER); 
		labelDistanceCurVal = new JLabel("0.000 mi", JLabel.CENTER); 
		labelDistanceTargVal = new JLabel("0.000 mi", JLabel.CENTER); 
 		labelCaloriesCurVal = new JLabel("0 cal", JLabel.CENTER); 
 		labelCaloriesTargVal = new JLabel("0 cal", JLabel.CENTER);
 
 		message = new JLabel(DEFAULT_MESSAGE);
 		message.setForeground(Color.blue);
 		currentFont = message.getFont();
 		message.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), 15));
 		
 		textFieldGoalDistance = new JTextField(30);
 		textFieldGoalDuration = new JTextField(30);
 		textFieldGoalCalories = new JTextField(30);
 
 
 		
 		/* Set up all the panels, top to bottom, left to right: */
 
 		/* Message */
 		panelMessage = new JPanel(); 
 		panelMessage.setBorder(BorderFactory.createLineBorder(Color.black));
 		panelMessage.add(message);
 
 
 		/* Outputs */		
 		panelTime = new JPanel();
 ///		panelTime.setBorder(BorderFactory.createLineBorder(Color.black));
 		panelTime.setLayout(new GridLayout(5,1));
 		panelTimeArray = new JPanel[5];
 		for(int i =0; i< 5; i++){
 			panelTimeArray[i] = new JPanel();
 			panelTimeArray[i].setLayout(new BorderLayout());
 			panelTime.add(panelTimeArray[i]);
 		}
 		panelTimeArray[0].add(new JLabel("TIME", JLabel.CENTER), BorderLayout.CENTER);
 		panelTimeArray[1].add(new JLabel("Elapsed", JLabel.CENTER), BorderLayout.SOUTH);
 		panelTimeArray[2].add(labelTimeElapsedVal, BorderLayout.NORTH);
 		panelTimeArray[3].add(new JLabel("Target", JLabel.CENTER), BorderLayout.SOUTH);
 		panelTimeArray[4].add(labelTimeCurVal, BorderLayout.NORTH);
 		
 		panelSpeed = new JPanel();
 		panelSpeed.setLayout(new GridLayout(5,1));
 		panelSpeedArray = new JPanel[5];
 		for(int i =0; i< 5; i++){
 			panelSpeedArray[i] = new JPanel();
 			panelSpeedArray[i].setLayout(new BorderLayout());
 			panelSpeed.add(panelSpeedArray[i]);
 		}
 		panelSpeedArray[0].add(new JLabel("SPEED", JLabel.CENTER), BorderLayout.CENTER);
 		panelSpeedArray[1].add(new JLabel("Current", JLabel.CENTER), BorderLayout.SOUTH);
 		panelSpeedArray[2].add(labelSpeedCurVal, BorderLayout.NORTH);
 		panelSpeedArray[3].add(new JLabel("Average", JLabel.CENTER), BorderLayout.SOUTH);
 		panelSpeedArray[4].add(labelSpeedAvgVal, BorderLayout.NORTH);
 		
 		panelIncline = new JPanel();
 		panelIncline.setLayout(new GridLayout(5,1));
 		panelInclinationArray = new JPanel[5];
 		for(int i =0; i< 5; i++){
 			panelInclinationArray[i] = new JPanel();
 			panelInclinationArray[i].setLayout(new BorderLayout());
 			panelIncline.add(panelInclinationArray[i]);
 		}
 		panelInclinationArray[0].add(new JLabel("INCLINATION", JLabel.CENTER), BorderLayout.CENTER);
 		panelInclinationArray[1].add(new JLabel("Current", JLabel.CENTER), BorderLayout.SOUTH);
 		panelInclinationArray[2].add(labelInclineCurVal, BorderLayout.NORTH);
 
 		panelDistance = new JPanel();
 		panelDistance.setLayout(new GridLayout(5,1));
 		panelDistanceArray = new JPanel[5];
 		for(int i =0; i< 5; i++){
 			panelDistanceArray[i] = new JPanel();
 			panelDistanceArray[i].setLayout(new BorderLayout());
 			panelDistance.add(panelDistanceArray[i]);
 		}
 		panelDistanceArray[0].add(new JLabel("DISTANCE", JLabel.CENTER), BorderLayout.CENTER);
 		panelDistanceArray[1].add(new JLabel("Current", JLabel.CENTER), BorderLayout.SOUTH);
 		panelDistanceArray[2].add(labelDistanceCurVal, BorderLayout.NORTH);
 		panelDistanceArray[3].add(new JLabel("Target", JLabel.CENTER), BorderLayout.SOUTH);
 		panelDistanceArray[4].add(labelDistanceTargVal, BorderLayout.NORTH);
 		
 		panelCalories = new JPanel();
 		panelCalories.setLayout(new GridLayout(5,1));
 		panelCalorieArray = new JPanel[5];
 		for(int i =0; i< 5; i++){
 			panelCalorieArray[i] = new JPanel();
 			panelCalorieArray[i].setLayout(new BorderLayout());
 			panelCalories.add(panelCalorieArray[i]);
 		}
 		panelCalorieArray[0].add(new JLabel("CALORIES BURNED", JLabel.CENTER), BorderLayout.CENTER);
 		panelCalorieArray[1].add(new JLabel("Current", JLabel.CENTER), BorderLayout.SOUTH);
 		panelCalorieArray[2].add(labelCaloriesCurVal, BorderLayout.NORTH);
 		panelCalorieArray[3].add(new JLabel("Target", JLabel.CENTER), BorderLayout.SOUTH);
 		panelCalorieArray[4].add(labelCaloriesTargVal, BorderLayout.NORTH);
 		
 		
 //		mySeparator = new JSeparator(SwingConstants.VERTICAL);
 //		mySeparator.setSize(30, 40);
 		panelOutputs = new JPanel(); 
 		panelOutputs.setLayout(new GridLayout(1,5));
 		panelOutputs.add(panelTime);
 //		panelOutputs.add(new JSeparator(SwingConstants.VERTICAL));
 		panelOutputs.add(panelSpeed);
 //		panelOutputs.add(new JSeparator(SwingConstants.VERTICAL));
 		panelOutputs.add(panelIncline);
 //		panelOutputs.add(new JSeparator(SwingConstants.VERTICAL));
 		panelOutputs.add(panelDistance);
 //		panelOutputs.add(new JSeparator(SwingConstants.VERTICAL));
 		panelOutputs.add(panelCalories);
 		
 
 		/* Inputs */
 		/* Basic functions: left */
 		panelBasicFunc = new JPanel();
 		panelBasicFunc.setLayout(new GridLayout(2,1));
 		panelBasicFunc.add(quickStart_Resume);
 		panelBasicFunc.add(pause_Stop);
 		
 		/* Goals & speed/incline controls: right */
 		goalTextField = new JTextField(20);
 		panelGoals = new JPanel();
 		panelGoals.setLayout(new GridLayout(5,1));
 		panelGoals.add(goal_Run_Start);
 		panelGoals.add(radioButtonsGoalRun[0]);
 		panelGoals.add(radioButtonsGoalRun[1]);
 		panelGoals.add(radioButtonsGoalRun[2]);
 		panelGoals.add(goalTextField);
 
 		panelSpeedIncline = new JPanel();
 		panelSpeedIncline.setLayout(new GridLayout(4,1));
 		panelSpeedIncline.add(speedUp);
 		panelSpeedIncline.add(speedDown);
 		panelSpeedIncline.add(inclineUp);
 		panelSpeedIncline.add(inclineDown);
 		
 		/* All inputs */
 		panelInputs = new JPanel(); 
 		panelInputs.setLayout(new GridLayout(1,2));
 		panelInputs.add(panelBasicFunc);
 ///		panelInputs.add(panelGoalStart);
 		panelInputs.add(panelGoals);
 		panelInputs.add(panelSpeedIncline);
 		
 		
 		/* Bring it all together */
 		panelInputOutput = new JPanel();
 		panelInputOutput.setLayout(new GridLayout(2,1));
 		panelInputOutput.add(panelOutputs);
 		panelInputOutput.add(panelInputs);
 		
 		setLayout(new BorderLayout());
 		add(BorderLayout.NORTH, panelMessage);
 		add(BorderLayout.CENTER, panelInputOutput);
 
 
 		/* Set up the current variables. */
 		distanceTarget = 0;
 		caloriesTarget = 0;
 		myTreadmill = new Session();
 		age = 21;
 		weight = 180;
 		timeMultiplier = 4;
 
 		goal = new DistanceGoal(0.5);
 
 		timer = new Timer(1000 / FPS, new TimerListener());
 		timer.setInitialDelay(0);
 		timer.start();
 
 		/* And update the initial labels. */
 		updateLabels();
 	}
 
 
 	/**
 	 * Updates all the labels based on session.
 	 */
 	private void updateLabels() {
 		labelTimeElapsedVal.setText(String.format("%02d:%02d:%02d", (int)(myTreadmill.getTimeElapsed() / 3600),
 				(int)(myTreadmill.getTimeElapsed() / 60) % 60, (int)(myTreadmill.getTimeElapsed()) % 60));
		labelSpeedCurVal.setText(String.format("%5.3f mph", (double)myTreadmill.getSpeed() / 10.0));
 		labelSpeedAvgVal.setText(String.format("%5.3f mph", myTreadmill.getAverageSpeed()));
 		labelInclineCurVal.setText(Integer.toString(myTreadmill.getIncline()) + " %"); 
		labelDistanceCurVal.setText(String.format("%5.3f mi", myTreadmill.getDistance()));
		labelCaloriesCurVal.setText(Integer.toString(myTreadmill.getCalories(age, weight)) + " cal");
 	}
 	
 	/**
 	 * Writes a message to the messagebox.
 	 *
 	 * @param msg the message to write
 	 */
 	private void writeMessage(String msg) {
 		message.setText(msg);
 	}
 
 
 
 	/** Listener for button events */
 	private class ButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent event) {
 			Object src = event.getSource();
 			if (src == quickStart_Resume) {
 				quickStart_Resume.setText("QuickStart");
 				pause_Stop.setText("Pause");
 				if (myTreadmill.getState() == Session.State.STOPPED) {
 					myTreadmill.start();
 				} else {
 					myTreadmill.resume();
 				}
 			} else if (src == pause_Stop) {
 				pause_Stop.setText("Stop");
 				if (myTreadmill.getState() == Session.State.RUNNING) {
 					quickStart_Resume.setText("Resume");
 					myTreadmill.pause();
 				} else {
 					quickStart_Resume.setText("QuickStart");
 					myTreadmill.stop();
 				}
 			} else if (src == speedUp) {
 				int cursp = myTreadmill.getSpeed();
 				cursp += 1;
 				if (cursp >= MAX_SPEED) {
 					cursp = MAX_SPEED;
 				}
 				myTreadmill.setSpeed(cursp);
 			} else if (src == speedDown) {
 				int cursp = myTreadmill.getSpeed();
 				cursp -= 1;
 				if (cursp <= 0) {
 					cursp = 0;
 				}
 				myTreadmill.setSpeed(cursp);
 			} else if (src == inclineUp) {
 				int curinc = myTreadmill.getIncline();
 				curinc += 1;
 				if (curinc >= MAX_INCLINE) {
 					curinc = MAX_INCLINE;
 				}
 				myTreadmill.setIncline(curinc);
 			} else if (src == inclineDown) {
 				int curinc = myTreadmill.getIncline();
 				curinc -= 1;
 				if (curinc <= 0) {
 					curinc = 0;
 				}
 				myTreadmill.setIncline(curinc);
 			}
 			updateLabels();
 		}
 	}
 
 	/** Listener for timer events. */
 	private class TimerListener implements ActionListener {
 		private long lastCall;
 		
 		public TimerListener() {
 			super();
 			lastCall = System.currentTimeMillis();
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			long curTime = System.currentTimeMillis();
 			myTreadmill.update((double)(curTime - lastCall) / 1000.0 * timeMultiplier);
 			updateLabels();
 
 			if (goal.checkIfDone(myTreadmill)) {
 				writeMessage("Goal has been met!");
 			} else {
 				writeMessage(DEFAULT_MESSAGE);
 			}
 			lastCall = curTime;
 		}
 	}
 }
 
