 /**
  * @class TrainModelUI
  * 
  * @version 1.0
  * 
  * @date 04/25/2013
  * 
  * @author Chris Paskie
  */
 
 /*
 XXX
 change resolution of screen so that both popups fully appear
 handle case if train starts rolling backwards while attempting to go uphill
 	train position may needchanged to prevBlock and routeIndex--
 when train moves out of block, don't want to necessarily change isOccupied to false since there may be another train in the block
 handle crashing of trains?...
 when train goes onto switch circuuit to avoid crash and the switched block is not in the route list...
 XXX
 */
 
 
 package TNM;
 
 import TKM.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.BorderFactory; 
 import javax.swing.border.Border;
 
 /**
  * This file contains the main class(es) used for the train module GUI.
  * It can created by the CTC in which case it will run with other modules, 
  * or it may be run solo from the command line.
  */
 public class TrainModelUI {
 	private static boolean isSolo = false;			// Is the train module being run individually?
 	private static int soloNumTrains = 0;
 	private static double soloTime = 0;
 	private static Date soloDate = new Date();		// Used with soloTime when TNM run solo.
 	private static int soloDelta = 100;				// Timestep in milliseconds.
 	private static int refreshUI = 0;				// Used to refresh GUI only once a second.
 	
 	protected static ArrayList<Train> trainList;
 	protected static int selectedId;				// ID of the currently selected train.
 	protected static String[] idArray;				// Contains string IDs of trains.
 	protected static boolean isPaused;
 	protected static boolean isVisible;				// Is the main window visible?
 	protected static boolean isVisibleStatic;		// Is the static values window visible?
 	
 	// Main JFrames
 	protected static JFrame dynamicWindow;
 	protected static JFrame staticWindow;
 	Border borderline = BorderFactory.createLineBorder(Color.black, 2);
 	
 	// dynamicWindow JButtons
 	protected static JButton btnShowStaticValues;
 	protected static JButton btnSelectTrain;
 	protected static JButton btnPauseResume;
 	protected static JButton btnSetManRecPower;
 	protected static JButton btnToggleManRecPower;
 	protected static JButton btnSetManDesSpdLmt;
 	protected static JButton btnToggleManDesSpdLmt;
 	protected static JButton btnToggleSignalPickupFailure;
 	protected static JButton btnToggleEngineFailure;
 	protected static JButton btnToggleBrakeFailure;
 	protected static JButton btnToggleServiceBrake;
 	protected static JButton btnToggleEmergencyBrake;
 	protected static JButton btnSetManLights;
 	protected static JButton btnToggleManLights;
 	protected static JButton btnSetManDoors;
 	protected static JButton btnToggleManDoors;
 	protected static JButton btnSetManTarTemperature;
 	protected static JButton btnToggleManTarTemperature;
 	
 	// dynamicWindow JLabels
 	protected static JLabel jlTime;
 	protected static JLabel jlCurVel;
 	protected static JLabel jlCurAccel;
 	protected static JLabel jlRecPowerTNC;
 	protected static JLabel jlManRecPower;
 	protected static JLabel jlToggleManRecPower;
 	protected static JLabel jlPostedSpdLmt;
 	protected static JLabel jlManDesSpdLmt;
 	protected static JLabel jlToggleManDesSpdLmt;
 	protected static JLabel jlGrade;
 	protected static JLabel jlTotalMass;
 	protected static JLabel jlPassengerCount;
 	protected static JLabel jlCrewCount;
 	protected static JLabel jlPosition;
 	protected static JLabel jlToggleSignalPickupFailure;
 	protected static JLabel jlToggleEngineFailure;
 	protected static JLabel jlToggleBrakeFailure;
 	protected static JLabel jlToggleServiceBrake;
 	protected static JLabel jlToggleEmergencyBrake;
 	protected static JLabel jlLights;
 	protected static JLabel jlManLights;
 	protected static JLabel jlToggleManLights;
 	protected static JLabel jlDoors;
 	protected static JLabel jlManDoors;
 	protected static JLabel jlToggleManDoors;
 	protected static JLabel jlCurTemperature;
 	protected static JLabel jlTarTemperature;
 	protected static JLabel jlManTarTemperature;
 	protected static JLabel jlToggleManTarTemperature;
 	protected static JLabel jlAnnouncement;
 	
 	// staticWindow JLabels
 	protected static JLabel jlLength;
 	protected static JLabel jlWidth;
 	protected static JLabel jlHeight;
 	protected static JLabel jlNumCars;
 	protected static JLabel jlMotorPower;
 	protected static JLabel jlMaxSpeed;
 	protected static JLabel jlServiceBrakeDecel;
 	protected static JLabel jlEmergencyBrakeDecel;
 	protected static JLabel jlFrictionCoeff;
 	protected static JLabel jlEmptyTrainMass;
 	protected static JLabel jlPersonMass;
 	protected static JLabel jlMaxSeatedCount;
 	protected static JLabel jlMaxStandingCount;
 	protected static JLabel jlMaxCrewCount;
 	
 	/**
 	 * Create the train module GUI (the dynamic and static windows).
 	 * Both windows are HIDE_ON_CLOSE so that closing them does not cause the 
 	 * train module to close.
 	 */
 	public TrainModelUI() {
 		try {
 			JPanel emptyJPanel = new JPanel();
 			emptyJPanel.add(new JLabel("                                                                                                                                                                                                                                                                                                                                                          "));
 			isPaused = true;
 			
 			
 			// Setup the dynamicWindow.
 			
 			btnShowStaticValues = buildJButton("Show Static Values");
 			btnSelectTrain = buildJButton("Select Train");
 			btnPauseResume = buildJButton("Pause");
 			btnPauseResume.setEnabled(false);
 			btnSetManRecPower = buildJButton("Set Manual Received Power");
 			btnToggleManRecPower = buildJButton("Toggle Manual Received Power");
 			btnSetManDesSpdLmt = buildJButton("Set Manual Desired Speed Limit");
 			btnToggleManDesSpdLmt = buildJButton("Toggle Manual Desired Speed Limit");
 			btnToggleSignalPickupFailure = buildJButton("Toggle Signal Pickup Failure");
 			btnToggleEngineFailure = buildJButton("Toggle Engine Failure");
 			btnToggleBrakeFailure = buildJButton("Toggle Brake Failure");
 			btnToggleServiceBrake = buildJButton("Toggle Service Brake");
 			btnToggleEmergencyBrake = buildJButton("Toggle Emergency Brake");
 			btnSetManLights = buildJButton("Set Manual Lights Status");
 			btnToggleManLights = buildJButton("Toggle Manual Lights Status");
 			btnSetManDoors = buildJButton("Set Manual Doors Status");
 			btnToggleManDoors = buildJButton("Toggle Manual Doors Status");
 			btnSetManTarTemperature = buildJButton("Set Manual Target Temp.");
 			btnToggleManTarTemperature = buildJButton("Toggle Manual Target Temp.");
 			
 			jlTime = new JLabel("XX:XX:XX", JLabel.CENTER);
 			jlCurVel = new JLabel("", JLabel.CENTER);
 			jlCurAccel = new JLabel("", JLabel.CENTER);
 			jlRecPowerTNC = new JLabel("", JLabel.CENTER);
 			jlManRecPower = new JLabel("", JLabel.CENTER);
 			jlToggleManRecPower = new JLabel("", JLabel.CENTER);
 			jlPostedSpdLmt = new JLabel("", JLabel.CENTER);
 			jlManDesSpdLmt = new JLabel("", JLabel.CENTER);
 			jlToggleManDesSpdLmt = new JLabel("", JLabel.CENTER);
 			jlGrade = new JLabel("", JLabel.CENTER);
 			jlTotalMass = new JLabel("", JLabel.CENTER);
 			jlPassengerCount = new JLabel("", JLabel.CENTER);
 			jlCrewCount = new JLabel("", JLabel.CENTER);
 			jlPosition = new JLabel("", JLabel.CENTER);
 			jlToggleSignalPickupFailure = new JLabel("", JLabel.CENTER);
 			jlToggleEngineFailure = new JLabel("", JLabel.CENTER);
 			jlToggleBrakeFailure = new JLabel("", JLabel.CENTER);
 			jlToggleServiceBrake = new JLabel("", JLabel.CENTER);
 			jlToggleEmergencyBrake = new JLabel("", JLabel.CENTER);
 			jlLights = new JLabel("", JLabel.CENTER);
 			jlManLights = new JLabel("", JLabel.CENTER);
 			jlToggleManLights = new JLabel("", JLabel.CENTER);
 			jlDoors = new JLabel("", JLabel.CENTER);
 			jlManDoors = new JLabel("", JLabel.CENTER);
 			jlToggleManDoors = new JLabel("", JLabel.CENTER);
 			jlCurTemperature = new JLabel("", JLabel.CENTER);
 			jlTarTemperature = new JLabel("", JLabel.CENTER);
 			jlManTarTemperature = new JLabel("", JLabel.CENTER);
 			jlToggleManTarTemperature = new JLabel("", JLabel.CENTER);
 			jlAnnouncement = new JLabel("", JLabel.CENTER);
 			
 			JPanel dwPanel1 = new JPanel();
 			dwPanel1.setLayout(new GridLayout(18, 2));
 			dwPanel1.add(buildJPanel(new JLabel("Current Velocity (m/s)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlCurVel));
 			dwPanel1.add(buildJPanel(new JLabel("Current Acceleration (m/s^2)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlCurAccel));
 			dwPanel1.add(buildJPanel(new JLabel("Received Power from TNC (W)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlRecPowerTNC));
 			dwPanel1.add(buildJPanel(btnSetManRecPower));
 			dwPanel1.add(buildJPanel(jlManRecPower));
 			dwPanel1.add(buildJPanel(btnToggleManRecPower));
 			dwPanel1.add(buildJPanel(jlToggleManRecPower));
 			dwPanel1.add(buildJPanel(new JLabel("Speed Limit Posted on Signs (km/hr)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlPostedSpdLmt));
 			dwPanel1.add(buildJPanel(btnSetManDesSpdLmt));
 			dwPanel1.add(buildJPanel(jlManDesSpdLmt));
 			dwPanel1.add(buildJPanel(btnToggleManDesSpdLmt));
 			dwPanel1.add(buildJPanel(jlToggleManDesSpdLmt));
 			dwPanel1.add(buildJPanel(new JLabel("Relative Grade from TKM (%)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlGrade));
 			dwPanel1.add(buildJPanel(new JLabel("Total Mass (including passengers/crew) (kg)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlTotalMass));
 			dwPanel1.add(buildJPanel(new JLabel("Passenger Count", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlPassengerCount));
 			dwPanel1.add(buildJPanel(new JLabel("Crew Count", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlCrewCount));
 			dwPanel1.add(buildJPanel(new JLabel("Position from Onboard GPS ([current_block], m)", JLabel.CENTER)));
 			dwPanel1.add(buildJPanel(jlPosition));
 			dwPanel1.add(buildJPanel(btnToggleSignalPickupFailure));
 			dwPanel1.add(buildJPanel(jlToggleSignalPickupFailure));
 			dwPanel1.add(buildJPanel(btnToggleEngineFailure));
 			dwPanel1.add(buildJPanel(jlToggleEngineFailure));
 			dwPanel1.add(buildJPanel(btnToggleBrakeFailure));
 			dwPanel1.add(buildJPanel(jlToggleBrakeFailure));
 			dwPanel1.add(buildJPanel(btnToggleServiceBrake));
 			dwPanel1.add(buildJPanel(jlToggleServiceBrake));
 			dwPanel1.add(buildJPanel(btnToggleEmergencyBrake));
 			dwPanel1.add(buildJPanel(jlToggleEmergencyBrake));
 			JPanel dwPanel2 = new JPanel();
 			dwPanel2.setLayout(new GridLayout(18, 2));
 			dwPanel2.add(buildJPanel(new JLabel("Lights Status", JLabel.CENTER)));
 			dwPanel2.add(buildJPanel(jlLights));
 			dwPanel2.add(buildJPanel(btnSetManLights));
 			dwPanel2.add(buildJPanel(jlManLights));
 			dwPanel2.add(buildJPanel(btnToggleManLights));
 			dwPanel2.add(buildJPanel(jlToggleManLights));
 			dwPanel2.add(buildJPanel(new JLabel("Doors Status", JLabel.CENTER)));
 			dwPanel2.add(buildJPanel(jlDoors));
 			dwPanel2.add(buildJPanel(btnSetManDoors));
 			dwPanel2.add(buildJPanel(jlManDoors));
 			dwPanel2.add(buildJPanel(btnToggleManDoors));
 			dwPanel2.add(buildJPanel(jlToggleManDoors));
 			dwPanel2.add(buildJPanel(new JLabel("Current Temperature (degrees Celsius)", JLabel.CENTER)));
 			dwPanel2.add(buildJPanel(jlCurTemperature));
 			dwPanel2.add(buildJPanel(new JLabel("Target Temperature from TNC (degrees Celsius)", JLabel.CENTER)));
 			dwPanel2.add(buildJPanel(jlTarTemperature));
 			dwPanel2.add(buildJPanel(btnSetManTarTemperature));
 			dwPanel2.add(buildJPanel(jlManTarTemperature));
 			dwPanel2.add(buildJPanel(btnToggleManTarTemperature));
 			dwPanel2.add(buildJPanel(jlToggleManTarTemperature));
 			dwPanel2.add(buildJPanel(new JLabel("Announcement", JLabel.CENTER)));
 			dwPanel2.add(buildJPanel(jlAnnouncement));
 			
 			JPanel primaryButtons = new JPanel();
 			primaryButtons.setLayout(new GridLayout(1, 4));
 			primaryButtons.add(btnShowStaticValues);
 			primaryButtons.add(btnSelectTrain);
 			primaryButtons.add(btnPauseResume);
 			primaryButtons.add(buildJPanel(jlTime));
 			
 			JPanel jp = new JPanel();
 			jp.add(primaryButtons);
 			jp.add(emptyJPanel);
 			jp.add(dwPanel1);
 			jp.add(new JLabel("        "));
 			jp.add(dwPanel2);
 			
 			dynamicWindow = new JFrame();
 			dynamicWindow.setTitle("Train Model (Chris Paskie)   -   UI   (Train ID:   --)");
 			dynamicWindow.setSize(1300, 700);
 			dynamicWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 			dynamicWindow.add(jp);
 			isVisible = false;
 			dynamicWindow.setVisible(isVisible);
 			
 			
 			// Setup the staticWindow.
 			
 			jlLength = new JLabel("", JLabel.CENTER);
 			jlWidth = new JLabel("", JLabel.CENTER);
 			jlHeight = new JLabel("", JLabel.CENTER);
 			jlNumCars = new JLabel("", JLabel.CENTER);
 			jlMotorPower = new JLabel("", JLabel.CENTER);
 			jlMaxSpeed = new JLabel("", JLabel.CENTER);
 			jlServiceBrakeDecel = new JLabel("", JLabel.CENTER);
 			jlEmergencyBrakeDecel = new JLabel("", JLabel.CENTER);
 			jlFrictionCoeff = new JLabel("", JLabel.CENTER);
 			jlEmptyTrainMass = new JLabel("", JLabel.CENTER);
 			jlPersonMass = new JLabel("", JLabel.CENTER);
 			jlMaxSeatedCount = new JLabel("", JLabel.CENTER);
 			jlMaxStandingCount = new JLabel("", JLabel.CENTER);
 			jlMaxCrewCount = new JLabel("", JLabel.CENTER);
 			
 			JPanel swPanel = new JPanel();
 			swPanel.setLayout(new GridLayout(14, 2));
 			swPanel.add(buildJPanel(new JLabel("Length (m)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlLength));
 			swPanel.add(buildJPanel(new JLabel("Width (m)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlWidth));
 			swPanel.add(buildJPanel(new JLabel("Height (m)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlHeight));
 			swPanel.add(buildJPanel(new JLabel("Number of Cars", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlNumCars));
 			swPanel.add(buildJPanel(new JLabel("Motor Power (W)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlMotorPower));
 			swPanel.add(buildJPanel(new JLabel("Maximum Speed (m/s)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlMaxSpeed));
 			swPanel.add(buildJPanel(new JLabel("Service Brake Deceleration (m/s^2)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlServiceBrakeDecel));
 			swPanel.add(buildJPanel(new JLabel("Emergency Brake Deceleration (m/s^2)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlEmergencyBrakeDecel));
 			swPanel.add(buildJPanel(new JLabel("Coefficient of Friction", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlFrictionCoeff));
 			swPanel.add(buildJPanel(new JLabel("Train Mass (not including passengers/crew) (kg)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlEmptyTrainMass));
 			swPanel.add(buildJPanel(new JLabel("Mass Per Passenger/Crew (kg)", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlPersonMass));
 			swPanel.add(buildJPanel(new JLabel("Maximum Seated Passenger Count", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlMaxSeatedCount));
 			swPanel.add(buildJPanel(new JLabel("Maximum Standing Passenger Count", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlMaxStandingCount));
 			swPanel.add(buildJPanel(new JLabel("Maximum Crew Count", JLabel.CENTER)));
 			swPanel.add(buildJPanel(jlMaxCrewCount));
 			
 			JScrollPane staticJSP = new JScrollPane(swPanel);
 			
 			staticWindow = new JFrame();
 			staticWindow.setTitle("Train Model (Chris Paskie)   -   Static Values   (Train ID:   --)");
 			staticWindow.setSize(700, 600);
 			staticWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 			staticWindow.add(staticJSP);
 			isVisibleStatic = false;
 			staticWindow.setVisible(isVisibleStatic);
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 			JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	/**
 	 * Create a standard JButton with the provided text.
 	 */
 	public JButton buildJButton(String text) {
 		JButton jb = new JButton(text);
 		jb.addActionListener(new TrainModelButtonListener());
 		jb.setEnabled(true);
 		return jb;
 	}
 	
 	/**
 	 * Create a standard JPanel containing the provided JComponent (i.e. JLabel, JButton, etc.).
 	 */
 	public JPanel buildJPanel(JComponent jc) {
 		JPanel jp = new JPanel(new GridLayout(1, 1), false);
 		jp.add(jc);
 		jp.setBorder(borderline);
 		
 		JPanel container  = new JPanel();
 		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
 		container.add(jp);
 		return container;
 	}
 	
 	/**
 	 * What is the list of trains that the application is currently using?
 	 */
 	public ArrayList<Train> getTrainList() {
 		return trainList;
 	}
 	
 	/**
 	 * Is the application currently paused?
 	 */
 	public boolean getIsPaused() {
 		return isPaused;
 	}
 	
 	/**
 	 * Is the main (dynamic) window currently visible?
 	 */
 	public boolean getIsVisible() {
 		return isVisible;
 	}
 	
 	/**
 	 * Is the static window currently visible?
 	 */
 	public boolean getIsVisibleStatic() {
 		return isVisibleStatic;
 	}
 	
 	/**
 	 * Load the list of trains which the module will use.
 	 */
 	public void setTrainList(ArrayList<Train> trainList) {
 		this.trainList = trainList;
 		if ((trainList != null) && (trainList.size() > 0)) {
 			idArray = new String[trainList.size()];
 			for (int i = 0; i < trainList.size(); i++) {
 				idArray[i] = new String(trainList.get(i).stringId);
 			}
 		} else {
 			idArray = null;
 		}
 	}
 	
 	/**
 	 * Pause/resume the module.
 	 */
 	public void setIsPaused(boolean isPaused) {
 		this.isPaused = isPaused;
 		if (isPaused) {
 			btnPauseResume.setText("Resume");
 		} else {
 			btnPauseResume.setText("Pause");
 		}
 	}
 	
 	/**
 	 * Show/hide the main (dynamic) window.
 	 */
 	public void setIsVisible(boolean isVisible) {
 		this.isVisible = isVisible;
 		dynamicWindow.setVisible(isVisible);
 	}
 	
 	/**
 	 * Show/hide the static window.
 	 */
 	public void setIsVisibleStatic(boolean isVisibleStatic) {
 		this.isVisibleStatic = isVisibleStatic;
 		staticWindow.setVisible(isVisibleStatic);
 	}
 	
 	/**
 	 * Update the GUI (both the dynamic and static windows) so that it displays the 
 	 * data of the newly selected train.
 	 */
 	public static void setSelectedId(int selId) {
 		selectedId = selId;
 		dynamicWindow.setTitle("Train Model (Chris Paskie)   -   UI   (Train ID:   " + trainList.get(selectedId - 1).stringId + ")");
 		staticWindow.setTitle("Train Model (Chris Paskie)   -   Static Values   (Train ID:   " + trainList.get(selectedId - 1).stringId + ")");
 		int hrs = (int) soloTime / (60 * 60);
 		int min = ((int) soloTime / 60) % 60;
 		int sec = (int) soloTime - (hrs * 60 * 60 + min * 60);
 		jlTime.setText(((hrs < 10) ? "0" : "") + hrs + ":" + ((min < 10) ? "0" : "") + min + ":" + ((sec < 10) ? "0" : "") + sec);
 		
 		jlCurVel.setText("" + trainList.get(selectedId - 1).curVelocity);
 		jlCurAccel.setText("" + trainList.get(selectedId - 1).curAccel);
 		jlRecPowerTNC.setText("" + trainList.get(selectedId - 1).receivedPower);
 		jlManRecPower.setText("" + trainList.get(selectedId - 1).manualPower);
 		jlToggleManRecPower.setText("" + trainList.get(selectedId - 1).issetManualPower);
 		jlPostedSpdLmt.setText("" + trainList.get(selectedId - 1).postedSpeedLimit);
 		jlManDesSpdLmt.setText("" + trainList.get(selectedId - 1).manualSpeedLimit);
 		jlToggleManDesSpdLmt.setText("" + trainList.get(selectedId - 1).issetManualSpeedLimit);
 		jlGrade.setText("" + trainList.get(selectedId - 1).positionBlock.grade);
 		jlTotalMass.setText("" + trainList.get(selectedId - 1).totalMass);
 		jlPassengerCount.setText("" + trainList.get(selectedId - 1).numPassengers);
 		jlCrewCount.setText("" + trainList.get(selectedId - 1).numCrew);
 		jlPosition.setText("" + ((trainList.get(selectedId - 1).issetSignalPickupFailure) 
 				? "???????" 
 				: ("[ " + trainList.get(selectedId - 1).positionBlock.id + " , " + trainList.get(selectedId - 1).positionMeters + " ]")));
 		jlToggleSignalPickupFailure.setText("" + trainList.get(selectedId - 1).issetSignalPickupFailure);
 		jlToggleEngineFailure.setText("" + trainList.get(selectedId - 1).issetEngineFailure);
 		jlToggleBrakeFailure.setText("" + trainList.get(selectedId - 1).issetBrakeFailure);
 		jlToggleServiceBrake.setText("" + trainList.get(selectedId - 1).issetServiceBrake);
 		jlToggleEmergencyBrake.setText("" + trainList.get(selectedId - 1).issetEmerBrake);
 		jlLights.setText("" + ((trainList.get(selectedId - 1).issetLightsOn) ? "On" : "Off"));
 		jlManLights.setText("" + ((trainList.get(selectedId - 1).issetLightsOnManual) ? "On" : "Off"));
 		jlToggleManLights.setText("" + trainList.get(selectedId - 1).issetLightsOnUseManual);
 		jlDoors.setText("" + ((trainList.get(selectedId - 1).issetDoorsOpen) ? "Open" : "Closed"));
 		jlManDoors.setText("" + ((trainList.get(selectedId - 1).issetDoorsOpenManual) ? "Open" : "Closed"));
 		jlToggleManDoors.setText("" + trainList.get(selectedId - 1).issetDoorsOpenUseManual);
 		jlCurTemperature.setText("" + trainList.get(selectedId - 1).curTemperature);
 		jlTarTemperature.setText("" + trainList.get(selectedId - 1).targetTemperatureTNC);
 		jlManTarTemperature.setText("" + trainList.get(selectedId - 1).targetTemperatureManual);
 		jlToggleManTarTemperature.setText("" + trainList.get(selectedId - 1).issetTargetTemperatureManual);
 		jlAnnouncement.setText("" + trainList.get(selectedId - 1).announcement);
 		
 		jlLength.setText("" + trainList.get(selectedId - 1).length);
 		jlWidth.setText("" + trainList.get(selectedId - 1).width);
 		jlHeight.setText("" + trainList.get(selectedId - 1).height);
 		jlNumCars.setText("" + trainList.get(selectedId - 1).numCars);
 		jlMotorPower.setText("" + trainList.get(selectedId - 1).motorPower);
 		jlMaxSpeed.setText("" + trainList.get(selectedId - 1).maxSpeed);
 		jlServiceBrakeDecel.setText("" + trainList.get(selectedId - 1).serviceBrakeDecel);
 		jlEmergencyBrakeDecel.setText("" + trainList.get(selectedId - 1).emerBrakeDecel);
 		jlFrictionCoeff.setText("" + trainList.get(selectedId - 1).frictionCoeff);
 		jlEmptyTrainMass.setText("" + trainList.get(selectedId - 1).emptyTrainMass);
 		jlPersonMass.setText("" + trainList.get(selectedId - 1).personMass);
 		jlMaxSeatedCount.setText("" + trainList.get(selectedId - 1).maxCapacitySeated);
 		jlMaxStandingCount.setText("" + trainList.get(selectedId - 1).maxCapacityStanding);
 		jlMaxCrewCount.setText("" + trainList.get(selectedId - 1).maxCapacityCrew);
 	}
 	
 	/**
 	 * Return a list containing all trains which are in the Block with given id.
 	 */
 	public ArrayList<Train> getTrainsInBlock(int id) {
 		ArrayList<Train> tempAL = new ArrayList<Train>();
 		for (int i = 0; i < trainList.size(); i++) {
			if (trainList.get(i).positionBlock.id == id) {
 				tempAL.add(trainList.get(i));
 			}
 		}
 		return tempAL;
 	}
 	
 	/**
 	 * Updates all train data and refreshes the GUI.
 	 */
 	public static void timeTick(Date date, int delta) {
 		if (!isPaused) {
 			refreshUI += delta;
 			double time = date.getHours() * 60 * 60 + date.getMinutes() * 60 + date.getSeconds();
 			
 			for (int i = 0; i < trainList.size(); i++) {
 				// Update the data for each train.
 				trainList.get(i).timeTick(time, ((double) (delta)) / 1000.0, isSolo);
 			}
 			
 			if (refreshUI >= 1000) {
 				// Refresh the train module GUI.
 				
 				if (isSolo) {
 					// If TNM is running solo, figure out the new time.
 					soloTime += 1;
 					if (soloTime >= 24 * 60 * 60) {
 						soloTime = soloTime % (24 * 60 * 60);
 					}
 					int hrs = (int) soloTime / (60 * 60);
 					int min = ((int) soloTime / 60) % 60;
 					int sec = (int) soloTime - (hrs * 60 * 60 + min * 60);
 					soloDate = new Date(93, 2, 2, hrs, min, sec);
 				} else {
 					soloTime = time;
 				}
 				
 				refreshUI = refreshUI % 1000;
 				setSelectedId(selectedId);
 			}
 		}
 	}
 	
 	/**
 	 * Called when run from the command line.  The train module will run (mostly) individually.
 	 */
 	public static void main(String[] args) {
 		try {
 			isSolo = true;
 			
 			// Get the number of trains.
 			if (args.length != 1) {
 				System.out.println("Invalid number of arguments.");
 				System.exit(1);
 			}
 			soloNumTrains = Integer.parseInt(args[0], 10);
 			if ((soloNumTrains < 1) || (soloNumTrains > 9)) {
 				System.out.println("Invalid argument. The number of trains must be greater than 0 and less than 10.");
 				System.exit(1);
 			}
 			
 			// Create the test block loop.
 			Block bYard = new Block(0, "A", "I", 0.0, 0.0, 5.0, true, false, true, false, false, "", false, false, false);
 			Block b01 = new Block(1, "B", "II", 500.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b02 = new Block(2, "C", "II", 500.0, -22.2, 1.0, true, false, false, false, false, "", false, false, false);
 			Block b03 = new Block(3, "D", "II", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b04 = new Block(4, "E", "II", 500.0, 11.1, 30.0, true, true, false, false, false, "", false, false, false);
 			Block b05 = new Block(5, "F", "III", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b06 = new Block(6, "G", "III", 100.0, 0.0, 5.0, true, false, false, true, false, "Alpha", false, false, false);
 			Block b07 = new Block(7, "H", "III", 500.0,  - 11.1, 30.0, true, false, false, false, false, "", false, false, false);
 			Block b08 = new Block(8, "I", "III", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b09 = new Block(9, "J", "IV", 500.0, 22.2, 1.0, true, false, false, false, false, "", false, false, false);
 			Block b10 = new Block(10, "K", "IV", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b11 = new Block(11, "L", "IV", 100.0, 0.0, 5.0, true, false, false, true, false, "Beta", false, false, false);
 			Block b12 = new Block(12, "M", "IV", 50.0, 11.1, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b13 = new Block(13, "N", "V", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b14 = new Block(14, "O", "V", 50.0,  - 11.1, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b15 = new Block(15, "P", "V", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			Block b16 = new Block(16, "Q", "V", 100.0, 0.0, 5.0, true, false, false, true, false, "Gamma", false, false, false);
 			Block b17 = new Block(17, "R", "V", 50.0, 0.0, 15.0, true, false, false, false, false, "", false, false, false);
 			
 			bYard.connect(b17, b01);
 			b01.connect(bYard, b07);
 			b02.connect(b01, b03);
 			b03.connect(b02, b04);
 			b04.connect(b03, b05);
 			b05.connect(b04, b06);
 			b06.connect(b05, b07);
 			b07.connect(b08, b01);
 			b08.connect(b09, b07);
 			b09.connect(b10, b08);
 			b10.connect(b11, b09);
 			b11.connect(b12, b10);
 			b12.connect(b11, b13);
 			b13.connect(b14, b12);
 			b14.connect(b13, b15);
 			b15.connect(b16, b14);
 			b16.connect(b15, b17);
 			b17.connect(bYard, b16);
 			
 			ArrayList<Block> route = new ArrayList<Block>();
 			route.add(bYard);
 			route.add(b01);
 			route.add(b02);
 			route.add(b03);
 			route.add(b04);
 			route.add(b05);
 			route.add(b06);
 			route.add(b07);
 			route.add(b08);
 			route.add(b09);
 			route.add(b10);
 			route.add(b11);
 			route.add(b12);
 			route.add(b13);
 			route.add(b14);
 			route.add(b15);
 			route.add(b16);
 			route.add(b17);
 			route.add(bYard);
 			route.add(b01);
 			route.add(b02);
 			route.add(b03);
 			route.add(b04);
 			route.add(b05);
 			route.add(b06);
 			route.add(b07);
 			route.add(b08);
 			route.add(b09);
 			route.add(b10);
 			route.add(b11);
 			route.add(b12);
 			route.add(b13);
 			route.add(b14);
 			route.add(b15);
 			route.add(b16);
 			route.add(b17);
 			route.add(bYard);
 			
 			/*
  			 * Create the trains.
 			 * The first will depart at 8:00 AM, the second at 8:30 AM, the third at 9:00 AM, etc.
 			 * The first will depart at 8:00 AM, the second at 8:15 AM, the third at 8:30 AM, etc.
  			 * All will have break time set to 4 hours after their departure times.
  			 */
  			trainList = new ArrayList<Train>();
  			idArray = new String[soloNumTrains];
  			for (int i = 0; i < soloNumTrains; i++) {
 				trainList.add(new Train(i + 1, "T" + (i + 1), "Test", (8 * 60 * 60 + i * 15 * 60) % (24 * 60 * 60), 
  						route, new Engineer(true, false, 0.0, (8 * 60 * 60 + i * 15 * 60 + 4 * 60 * 60) % (24 * 60 * 60)), bYard));
  				idArray[i] = new String("T" + (i + 1));
  			}
 			
 			// Create the UI.
 			TrainModelUI tnmUI = new TrainModelUI();
 			tnmUI.setTrainList(trainList);
 			
 			// Setup the timer.
 			soloTime = 7 * 60 * 60 + 59 * 60 + 55;
 			soloDate = new Date(93, 2, 2, 7, 59, 55);
 			ActionListener taskPerformer = new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					timeTick(soloDate, soloDelta);
 				}
 			};
 			new javax.swing.Timer(20, taskPerformer).start();
 			
 			// Disable all "Toggle" buttons that are associated with a manual entry.
 			btnToggleManRecPower.setEnabled(false);
 			btnToggleManDesSpdLmt.setEnabled(false);
 			btnToggleManLights.setEnabled(false);
 			btnToggleManDoors.setEnabled(false);
 			btnToggleManTarTemperature.setEnabled(false);
 			
 			// Make it so only manual values are used.
 			for (int i = 0; i < soloNumTrains; i++) {
 				Train t = trainList.get(i);
 				t.issetManualPower = true;
 				t.issetManualSpeedLimit = true;
 				t.issetLightsOnUseManual = true;
 				t.issetDoorsOpenUseManual = true;
 				t.issetTargetTemperatureManual = true;
 			}
 			
 			btnPauseResume.setEnabled(true);
 			tnmUI.setSelectedId(trainList.get(0).id);
 			tnmUI.setIsPaused(tnmUI.getIsPaused());
 			tnmUI.setIsVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 			JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	
 	/**
 	 * Handles all button clicks in the main (dynamic) window and all popups.
 	 */
 	private class TrainModelButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent aEvent) {
 			try {
 				if (aEvent.getActionCommand() == "Show Static Values") {
 					// Show Static Values
 					staticWindow.setVisible(true);
 				} else if (aEvent.getActionCommand() == "Select Train") {
 					// Select Train
 					try {
 						int intId = 1;
 						String tempId = (String)JOptionPane.showInputDialog(null, "", "Train Model - Select Train", 
 										JOptionPane.QUESTION_MESSAGE, null, idArray, null);
 						if (tempId != null) {
 							int i;
 							for (i = 0; i < trainList.size(); i++) {
 								if (trainList.get(i).stringId.equals(tempId)) {
 									break;
 								}
 							}
 							setSelectedId(i+1);
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						JOptionPane.showMessageDialog(null, "Invalid input.", "Train Model - Warning", 
 								JOptionPane.WARNING_MESSAGE);
 					}
 				} else if ((aEvent.getActionCommand() == "Pause") || (aEvent.getActionCommand() == "Resume")) {
 					// Pause / Resume
 					setIsPaused(!isPaused);
 				} else if (aEvent.getActionCommand() == "Set Manual Received Power") {
 					// Set Manual Received Power
 					try {
 						String tempManRecPower = (String)JOptionPane.showInputDialog(null, 
 													"Enter the received power (W) (number only):", 
 													"Train Model - Set Received Power", JOptionPane.QUESTION_MESSAGE);
 						if (tempManRecPower != null) {
 							trainList.get(selectedId - 1).manualPower = Double.parseDouble(tempManRecPower);
 							jlManRecPower.setText("" + trainList.get(selectedId - 1).manualPower);
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						JOptionPane.showMessageDialog(null, "Invalid received power value entered.", 
 								"Train Model - Warning", JOptionPane.WARNING_MESSAGE);
 					}
 				} else if (aEvent.getActionCommand() == "Toggle Manual Received Power") {
 					// Toggle Manual Received Power
 					trainList.get(selectedId - 1).issetManualPower = !trainList.get(selectedId - 1).issetManualPower;
 					jlToggleManRecPower.setText("" + trainList.get(selectedId - 1).issetManualPower);
 				} else if (aEvent.getActionCommand() == "Set Manual Desired Speed Limit"){
 					// Set Manual Desired Speed Limit
 					try {
 						String tempManDesSpdLmt = (String)JOptionPane.showInputDialog(null, "Enter the desired speed limit (km/hr) (number only):", 
 													"Train Model - Set Desired Speed Limit", JOptionPane.QUESTION_MESSAGE);
 						if (tempManDesSpdLmt != null) {
 							trainList.get(selectedId - 1).manualSpeedLimit = Double.parseDouble(tempManDesSpdLmt);
 							jlManDesSpdLmt.setText("" + trainList.get(selectedId - 1).manualSpeedLimit);
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						JOptionPane.showMessageDialog(null, "Invalid desired speed limit entered.", "Train Model - Warning", 
 								JOptionPane.WARNING_MESSAGE);
 					}
 				} else if (aEvent.getActionCommand() == "Toggle Manual Desired Speed Limit") {
 					// Toggle Manual Desired Speed Limit
 					trainList.get(selectedId - 1).issetManualSpeedLimit = !trainList.get(selectedId - 1).issetManualSpeedLimit;
 					jlToggleManDesSpdLmt.setText("" + trainList.get(selectedId - 1).issetManualSpeedLimit);
 				} else if (aEvent.getActionCommand() == "Toggle Signal Pickup Failure") {
 					// Toggle Signal Pickup Failure
 					trainList.get(selectedId - 1).issetSignalPickupFailure = !trainList.get(selectedId - 1).issetSignalPickupFailure;
 					jlToggleSignalPickupFailure.setText("" + trainList.get(selectedId - 1).issetSignalPickupFailure);
 					jlPosition.setText("" + ((trainList.get(selectedId - 1).issetSignalPickupFailure) ? "???????" 
 							: ("[ " + trainList.get(selectedId - 1).positionBlock.id + " , " + trainList.get(selectedId - 1).positionMeters + " ]")));
 				} else if (aEvent.getActionCommand() == "Toggle Engine Failure") {
 					// Toggle Engine Failure
 					trainList.get(selectedId - 1).issetEngineFailure = !trainList.get(selectedId - 1).issetEngineFailure;
 					jlToggleEngineFailure.setText("" + trainList.get(selectedId - 1).issetEngineFailure);
 				} else if (aEvent.getActionCommand() == "Toggle Brake Failure") {
 					// Toggle Brake Failure
 					trainList.get(selectedId - 1).issetBrakeFailure = !trainList.get(selectedId - 1).issetBrakeFailure;
 					jlToggleBrakeFailure.setText("" + trainList.get(selectedId - 1).issetBrakeFailure);
 				} else if (aEvent.getActionCommand() == "Toggle Service Brake") {
 					// Toggle Service Brake
 					trainList.get(selectedId - 1).issetServiceBrake = !trainList.get(selectedId - 1).issetServiceBrake;
 					jlToggleServiceBrake.setText("" + trainList.get(selectedId - 1).issetServiceBrake);
 				} else if (aEvent.getActionCommand() == "Toggle Emergency Brake") {
 					// Toggle Emergency Brake
 					trainList.get(selectedId - 1).issetEmerBrake = !trainList.get(selectedId - 1).issetEmerBrake;
 					jlToggleEmergencyBrake.setText("" + trainList.get(selectedId - 1).issetEmerBrake);
 				} else if (aEvent.getActionCommand() == "Set Manual Lights Status") {
 					// Set Manual Lights Status
 					try {
 						String[] optionsManLights = new String[2];
 						optionsManLights[0] = "On";
 						optionsManLights[1] = "Off";
 						String tempManLights = (String)JOptionPane.showInputDialog(null, "", "Train Model - Set Lights Status", 
 												JOptionPane.QUESTION_MESSAGE, null, optionsManLights, null);
 						if (tempManLights != null) {
 							trainList.get(selectedId - 1).issetLightsOnManual = tempManLights.equals(optionsManLights[0]);
 							jlManLights.setText("" + ((trainList.get(selectedId - 1).issetLightsOnManual) ? "On" : "Off"));
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						JOptionPane.showMessageDialog(null, "Invalid input.", "Train Model - Warning", JOptionPane.WARNING_MESSAGE);
 					}
 				} else if (aEvent.getActionCommand() == "Toggle Manual Lights Status") {
 					// Toggle Manual Lights Status
 					trainList.get(selectedId - 1).issetLightsOnUseManual = !trainList.get(selectedId - 1).issetLightsOnUseManual;
 					jlToggleManLights.setText("" + trainList.get(selectedId - 1).issetLightsOnUseManual);
 				} else if (aEvent.getActionCommand() == "Set Manual Doors Status") {
 					// Set Manual Doors Status
 					try {
 						String[] optionsManDoors = new String[2];
 						optionsManDoors[0] = "Open";
 						optionsManDoors[1] = "Closed";
 						String tempManDoors = (String)JOptionPane.showInputDialog(null, "", "Train Model - Set Doors Status", 
 												JOptionPane.QUESTION_MESSAGE, null, optionsManDoors, null);
 						if (tempManDoors != null) {
 							trainList.get(selectedId - 1).issetDoorsOpenManual = tempManDoors.equals(optionsManDoors[0]);
 							jlManDoors.setText("" + ((trainList.get(selectedId - 1).issetDoorsOpenManual) ? "Open" : "Closed"));
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						JOptionPane.showMessageDialog(null, "Invalid input.", "Train Model - Warning", JOptionPane.WARNING_MESSAGE);
 					}
 				} else if (aEvent.getActionCommand() == "Toggle Manual Doors Status") {
 					// Toggle Manual Doors Status
 					trainList.get(selectedId - 1).issetDoorsOpenUseManual = !trainList.get(selectedId - 1).issetDoorsOpenUseManual;
 					jlToggleManDoors.setText("" + trainList.get(selectedId - 1).issetDoorsOpenUseManual);
 				} else if (aEvent.getActionCommand() == "Set Manual Target Temp.") {
 					// Set Manual Target Temp.
 					try {
 						String tempManTarTemperature = (String)JOptionPane.showInputDialog(null, "Enter the target temperature (degrees Celsius) (number only):", 
 														"Train Model - Set Target Temperature", JOptionPane.QUESTION_MESSAGE);
 						if (tempManTarTemperature != null) {
 							trainList.get(selectedId - 1).targetTemperatureManual = Double.parseDouble(tempManTarTemperature);
 							jlManTarTemperature.setText("" + trainList.get(selectedId - 1).targetTemperatureManual);
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						JOptionPane.showMessageDialog(null, "Invalid target temperature entered.", "Train Model - Warning", 
 								JOptionPane.WARNING_MESSAGE);
 					}
 				} else if (aEvent.getActionCommand() == "Toggle Manual Target Temp.") {
 					// Toggle Manual Target Temp.
 					trainList.get(selectedId - 1).issetTargetTemperatureManual = !trainList.get(selectedId - 1).issetTargetTemperatureManual;
 					jlToggleManTarTemperature.setText("" + trainList.get(selectedId - 1).issetTargetTemperatureManual);
 				} else {
 					JOptionPane.showMessageDialog(null, "Invalid action event.", "Error", JOptionPane.ERROR_MESSAGE);
 				}
 			} catch (Exception e) {
 				e.printStackTrace(System.err);
 				JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	} 
 }
