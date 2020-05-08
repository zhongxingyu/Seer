 package de.uniluebeck.itm.icontrol.gui.controller;
 
 //Lizenz
 
 import java.util.LinkedList;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 import de.uniluebeck.itm.icontrol.PluginiControl2iShell;
 import de.uniluebeck.itm.icontrol.communication.listener.FeatureListener;
 import de.uniluebeck.itm.icontrol.communication.listener.MessageListener;
 import de.uniluebeck.itm.icontrol.gui.model.VRobot;
 import de.uniluebeck.itm.icontrol.gui.view.Gui;
 
 /**
  * The <code>VController</code> class controls the graphical user interface and updates the
  * <code>VRobot</code> models.
  * 
  * @author Johannes Kotzerke
  */
 public class VController implements FeatureListener, MessageListener {
 
 	/**
 	 * The communication gateway
 	 */
 	private PluginiControl2iShell control;
 
 	/**
 	 * List of all available robots
 	 */
 	private LinkedList<VRobot> robotList;
 
 	/**
 	 * The list position of the currently displayed robot
 	 */
 	private int displayedRobot;
 	
 	private Display display;
 
 	private Gui gui;
 
 	public VController(final PluginiControl2iShell control, final Composite container) {
 		this.control = control;
 		this.display = container.getDisplay();
 		this.robotList = new LinkedList<VRobot>();
 		this.gui = new Gui(this, container);
 	}
 
 	/**
 	 * Checks if a robot with the given id is in the <code>robotList</code>
 	 * 
 	 * @param id
 	 *            the robot's id
 	 * @return -1 if there isn't a robot having that id, else the robot's position in the
 	 *         <code>robotList</code>
 	 */
 	private int robotInList(final int id) {
 		for (final VRobot robot : robotList) {
 			if (robot.getId() == id) {
 				return robotList.indexOf(robot);
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Sets which robot the GUI currently displays
 	 * 
 	 * @param id
 	 *            the robot's id
 	 */
 	public void setDisplayedRobot(final int id) {
 		displayedRobot = id;
 		// gui.updateRobot();
 	}
 
 	/**
 	 * Returns all parameters of the given feature of the currently displayed robot
 	 * 
 	 * @param featureNumber
 	 *            the feature number in the list
 	 * @return the parameters' names in a <code>String[]</code>
 	 */
 	public String[] getFeatureParameters(final int featureNumber) {
 		return robotList.get(robotInList(displayedRobot)).getFeatureParameters(featureNumber);
 	}
 
 	/**
 	 * Returns the ids of all currently known robots
 	 * 
 	 * @return all robots' ids in an <code>int[]</code>
 	 */
 	public int[] getAllRobotNames() {
 		int[] thatNames = new int[robotList.size()];
 		for (int i = 0; i < robotList.size(); i++) {
 			thatNames[i] = robotList.get(i).getId();
 		}
 		return thatNames;
 	}
 
 	/**
 	 * Returns all features' names of the currently displayed robot
 	 * 
 	 * @return the names of all features in a <code>String[]</code>
 	 */
 	public String[] getAllFeaturesNames() {
 		return robotList.get(robotInList(displayedRobot)).getAllFeatureNames();
 	}
 
 	/**
 	 * Gives the chosen task to the communication gateway and sets this task in the
 	 * <code>VRobot</code> status
 	 * 
 	 * @param taskName
 	 *            the name of the chosen task
 	 * @param parameters
 	 *            the given parameters
 	 */
 	public void doTask(final String taskName, final int[] parameters) {
 		control.getGateway().doTask(robotInList(displayedRobot), taskName, parameters.length, parameters);
 		robotList.get(robotInList(displayedRobot)).setTask(taskName);
 		// gui.updateStatus();
 	}
 
 	/**
 	 * Deletes all used stuff by setting to null
 	 */
 	public void killAll() {
 		gui = null;
 		robotList = null;
 	}
 
 	// FeatureListener
 	@Override
	public synchronized void onAction(final int robotId, final int taskListLength, final String[] taskList, final int[] paramListLength, final String[][] paramList) {
 		if (robotList.isEmpty()) {
			robotList.add(new VRobot(robotId, taskListLength, taskList, paramListLength, paramList));
 			if (Display.getCurrent() != null) {
 				gui.run();
 			} else {
 				if (!display.isDisposed()) {
 					display.asyncExec(new Runnable() {
 						public void run() {
 							if (display.isDisposed()) {
 								return;
 							}
 							gui.run();
 						}
 					});
 				}
 			}
 		}
 		// if the list is empty, add the robot, else check if the robot already exists. If he exists
 		// ignore the given features, else add him.
 		else if (robotInList(robotId) == -1) {
 			robotList.add(new VRobot(robotId, taskListLength, taskList, paramListLength, paramList));
 		}else
 			return;
 		
 		if (Display.getCurrent() != null) {
 			gui.gotNewRobot(getAllRobotNames());
 		} else {
 			if (!display.isDisposed()) {
 				display.asyncExec(new Runnable() {
 					public void run() {
 						if (display.isDisposed()) {
 							return;
 						}
 						gui.gotNewRobot(getAllRobotNames());
 					}
 				});
 			}
 		}
 	}
 	
 
 	// MessageListener
 	@Override
 	public void onMessage(final int robotId, final String taskName, final int valueLength, final int[] values) {
 		if (taskName.equals("status")) {
 			final int position = robotInList(robotId);
 			if (position == -1) {
 				System.err.println("I don't know this robot!");
 			} else {
 				robotList.get(position).updateStatus(values);
 				if (position == displayedRobot) {
 					// gui.updateStatus
 					// remove if-{}
 				}
 			}
 		} else if (taskName.equals("linkStatus")) {
 			final int position = robotInList(robotId);
 			if (position == -1) {
 				System.err.println("I don't know this robot!");
 			} else {
 				robotList.get(position).updateLinkStatus(values);
 				if (position == displayedRobot) {
 					// gui.updateLinkStatus
 					// remove if-{}
 				}
 			}
 		}
 
 	}
 
 }
