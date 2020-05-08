 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.continuouscontroller;
 
 import static java.text.MessageFormat.format;
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.ScannableMotion;
 import gda.device.scannable.ContinuouslyScannableViaController;
 import gda.device.scannable.scannablegroup.ScannableGroup;
 import gda.factory.FactoryException;
 import gda.util.OutOfRangeException;
 import gov.aps.jca.CAException;
 import gov.aps.jca.TimeoutException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EpicsTrajectoryMoveControllerAdapter extends DeviceBase implements TrajectoryMoveController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryMoveControllerAdapter.class);
 
 	private EpicsTrajectoryScanController controller;
 	
 	private String[] axisNames;
 	
 	private int[] axisMotorOrder;
 	
 	private FutureTask<Void> moveFuture;
 	
 	private Double triggerPeriod = 1.;
 	
 	private Scannable scannableForMovingGroupToStart;
 	
 	List<Double[]> points = new ArrayList<Double[]>();
 	
 	private boolean verifyEpicsReadbackEnabled = true;
 
 	private final Double[] triggerDeltas = null; // final as not yet used
 	
 	public EpicsTrajectoryScanController getController() {
 		return controller;
 	}
 
 	public void setController(EpicsTrajectoryScanController controller) {
 		this.controller = controller;
 	}
 
 	public String[] getAxisNames() {
 		return axisNames;
 	}
 
 	public void setAxisNames(String[] axisNames) {
 		this.axisNames = axisNames;
 	}
 
 	public int[] getAxisMotorOrder() {
 		return axisMotorOrder;
 	}
 	
 	/**
 	 * Set the order of axes exposed here. For example if on the controller:
 	 * <p>
 	 * 1=c 2=b 3=a (indexed from 1)
 	 * <p>
 	 * And we want to expose:
 	 * <p>
 	 * 0=a, 1=b, c=2 (indexed from 0)
 	 * <p>
 	 * we would call setAxisMotorOrder(3,2,1) (indexed from 1)
 	 * 
 	 * @param axisMotorOrder
 	 */
 	public void setAxisMotorOrder(int[] axisMotorOrder) {
 		this.axisMotorOrder = axisMotorOrder;
 	}
 	
 	public Scannable getScannableForMovingGroupToStart() {
 		return scannableForMovingGroupToStart;
 	}
 
 	/**
 	 * Set the Scannable used for moving the group to the start position for a move. This is *not required*, however it
 	 * should be set if either (a) the non-coordinated move Epics uses to move the motors to the start position might
 	 * result in collision, or (b) the time it takes for motors to move the start position when executing the trajectory
 	 * is may exceed the timeout a detector waits for it first trigger..
 	 */
 	public void setScannableForMovingGroupToStart(Scannable scannableForMovingGroupToStart) {
 		this.scannableForMovingGroupToStart = scannableForMovingGroupToStart;
 	}
 	
 	/**
 	 * 
 	 * @param index starting from 0
 	 * @return motor number starting from 1 and possibly reordered.
 	 */
 	public int motorFromIndex(int index) {
 		return (axisMotorOrder == null) ? index + 1 :axisMotorOrder[index];
 	}
 	
 	@Override
 	public int getNumberAxes() {
 		return axisNames.length;
 	}
 
 	@Override
 	public void setTriggerPeriod(double seconds) throws DeviceException {
 		triggerPeriod = seconds;
 	}
 	
 	@Override
 	public void addPoint(Double[] point) throws DeviceException {
 		if (point.length != getNumberAxes()) {
 			throw new DeviceException(format("{0} expected {1} dimension point not a {2} dimension one.", getName(),
 					getNumberAxes(), point.length));
 		}
 		points.add(point);
 	}
 
 	@Override
 	public Double[] getLastPointAdded() {
 		if (points.size() == 0) {
 			return null;
 		}
 		return points.get(points.size() - 1);
 	}
 
 	@Override
 	public void setAxisTrajectory(int axisIndex, double[] trajectory) throws DeviceException, InterruptedException {
 		controller.setMTraj(motorFromIndex(axisIndex), trajectory);	
 	}
 
 	@Override
 	public void setTriggerDeltas(double[] triggerDeltas) throws DeviceException {
 		throw new RuntimeException("Non-even time triggering not supported yet. See 'TimeTraj' pv to implement");
 	}
 
 	@Override
 	public void prepareForMove() throws DeviceException, InterruptedException {
 		// note: these methods all work with no points (they clear in this case)
 		
 		pushNumberOfElementsAndPulses();
 		pushPathsFromPoints();
 		controller.setTrajectoryTime(getTotalTime());
 				
 		List<Integer> motorsToMove = pushEnableAxisSettings();
 		
 		// Verify Epics settings. Getting this wrong could be expensive.
 		if(verifyEpicsReadbackEnabled){
 			verifyEpicsSettingsForNumberOfElementsAndPulses();
 			verifyEpicsSettingsForPathTrajectories();
 			verifyEpicsSettingsForTrajectoryTime();
 			try {
 				verifyEpicsSettingsAxisEnabled(motorsToMove);
 			} catch (CAException e) {
 				throw new DeviceException(e);
 			} catch (TimeoutException e) {
 				throw new DeviceException(e);
 			}
 		}
 		
 		controller.build(); // will throw an exception with no points (this is okay)
 		
 		// move to first position
 		Scannable scannableForMovingGroupToStart = getScannableForMovingGroupToStart();
 		if (scannableForMovingGroupToStart != null) {
 			
 			// We have broken encapsulation early on with this design. As a repercussion we should make sure
 			// that an offset is not applied twice when moving to the start. TODO: Fix encapsulation issue.
 			if (scannableForMovingGroupToStart instanceof ScannableGroup) {
				ArrayList<Scannable> groupMembers = ((ScannableGroup) scannableForMovingGroupToStart).getGroupMembers();
 				for (Scannable member : groupMembers) {
 					if (member instanceof ScannableMotion) {
 						Double[] offset = ((ScannableMotion) member).getOffset();
 						if ((offset != null) && (!zeroLength(offset))) {
 							throw new IllegalStateException("The scannable used to move motors to the start of a continuous move must have no gda offset!");
 						}
 					}
 				}
 			}
 			boolean wasOperatingContinously=false;
 			if (scannableForMovingGroupToStart instanceof ContinuouslyScannableViaController) {
 				wasOperatingContinously = ((ContinuouslyScannableViaController) scannableForMovingGroupToStart).isOperatingContinously();
 				((ContinuouslyScannableViaController) scannableForMovingGroupToStart).setOperatingContinuously(false);
 			}
 			Double[] firstPosition = points.get(0);
 			logger.info("Moving axes to start (" + Arrays.toString(firstPosition) + ") via configured Scannable " + scannableForMovingGroupToStart.getName());
 			scannableForMovingGroupToStart.moveTo(firstPosition);
 			logger.info("Move to start complete");
 			if (scannableForMovingGroupToStart instanceof ContinuouslyScannableViaController) {
 				((ContinuouslyScannableViaController) scannableForMovingGroupToStart).setOperatingContinuously(wasOperatingContinously);
 			}
 		}
 		
 	}
 
 	private boolean zeroLength(Double[] offset) {
 		for (int i = 0; i < offset.length; i++) {
 			Double offseti = offset[i];
 			if ((offseti != null) && (offseti != 0)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	void verifyEpicsSettingsForNumberOfElementsAndPulses() throws DeviceException, InterruptedException {
 		logger.info("Epics NumberOfElements:" + controller.getNumberOfElements());
 		if (controller.getNumberOfElements() != points.size()) {
 			throw new IllegalStateException(format("controller.getNumberOfElements() [{0}] != points.size() [{1}]",
 					controller.getNumberOfElements(), points.size()));
 		}
 		logger.info("Epics StartPulseElement:" + controller.getStartPulseElement());
 		if (controller.getStartPulseElement() != 1) {
 			throw new IllegalStateException(format("controller.getStartPulseElement() [{0}] != 1",
 					controller.getStartPulseElement()));
 		}
 		logger.info("Epics StopPulseElement:" + controller.getStopPulseElement());
 		if (controller.getStopPulseElement() != points.size()) {
 			throw new IllegalStateException(format("controller.getStopPulseElement() [{0}] != points.size() [{1}]",
 					controller.getStopPulseElement(), points.size()));
 		}
 		
 	}
 
 	private void verifyEpicsSettingsForPathTrajectories() throws DeviceException, InterruptedException {
 		
 		int numberOfElementsFromEpics = controller.getNumberOfElements();
 		logger.info("----------------------------------------------------------------------------------------------------");	
 		logger.info("Showing epics trajectories for the number of elements reported by Epics:" + numberOfElementsFromEpics);
 		for (int motorIndex = 1; motorIndex < controller.getMaximumNumberMotors() + 1; motorIndex++) {
 			double[] mTraj = controller.getMTraj(motorIndex);
 			mTraj = Arrays.copyOf(mTraj, numberOfElementsFromEpics);
 			try {
 				String enabledFlag = controller.isMMove(motorIndex) ? "**ENABLED**" : "  DISABLED";
 				logger.info(format("Epics trajectory for motor{0}: {1}\n{2}", motorIndex, enabledFlag, Arrays.toString(mTraj)));
 			} catch (Exception e) {
 				throw new DeviceException(e);
 			}
 		}
 		logger.info("----------------------------------------------------------------------------------------------------");	
 		
 		ArrayList<Integer> motorsToMove = new ArrayList<Integer>();
 		// check trajectories to move
 		if (points.size() > 0) {
 			for (int axisIndex = 0; axisIndex < axisNames.length; axisIndex++) {
 				double[] path = new double[points.size()];
 				for (int i = 0; i < points.size(); i++) {
 					Double val = points.get(i)[axisIndex];
 					if (val != null){
 						path[i] = val;
 						motorsToMove.add(motorFromIndex(axisIndex));
 					} else {
 						path = null;
 						break;
 					}
 				}
 				if (path != null) {
 					double[] setPath = controller.getMTraj(motorFromIndex(axisIndex));
 					String msgIfDiffer = almostEqual(setPath, Arrays.copyOf(path, controller.getMaximumNumberElements()), .0000001);
 					if (msgIfDiffer != null) {
 						throw new IllegalStateException(format("Epics set path for motor{0} differs from expected value: {1}", motorFromIndex(axisIndex), msgIfDiffer));
 					}
 				}
 			}
 		}
 		for (int motorIndex = 1; motorIndex < controller.getMaximumNumberMotors() + 1; motorIndex++) {
 			double[] zeros = new double[controller.getMaximumNumberElements()];
 			Arrays.fill(zeros, 0);
 			if (!motorsToMove.contains(motorIndex)) {
 				double[] setPath = controller.getMTraj(motorIndex);
 				String msgIfDiffer = almostEqual(setPath, zeros, .0000001);
 				if (msgIfDiffer != null) {
 					throw new IllegalStateException(format("Epics set path for motor{0} differs from expected value: {1}", motorIndex, msgIfDiffer));
 				}
 			}
 		}
 		
 	}
 
 	private void verifyEpicsSettingsForTrajectoryTime() throws DeviceException, InterruptedException {
 		logger.info("Epics TrajectoryTime:" + controller.getTrajectoryTime());
 		if (Math.abs(controller.getTrajectoryTime() - getTotalTime()) > .00001) {
 			throw new IllegalStateException(format("controller.getTrajectoryTime() [{0}] != getTotalTime() [{1}]",
 					controller.getTrajectoryTime(), getTotalTime()));
 		}
 		
 	}
 
 	private void verifyEpicsSettingsAxisEnabled(List<Integer> motorsToMove) throws CAException, TimeoutException, InterruptedException {
 		for (int motorIndex = 1; motorIndex < controller.getMaximumNumberMotors() + 1; motorIndex++) {
 			logger.info("Epics MMove motor" + motorIndex + ": " + controller.isMMove(motorIndex));
 		}
 		for (int motorIndex = 1; motorIndex < controller.getMaximumNumberMotors() + 1; motorIndex++) {
 			boolean motorShouldMove = controller.isMMove(motorIndex);
 			if (motorShouldMove != motorsToMove.contains(motorIndex)) {
 				throw new IllegalStateException(format("Epics MMove({0}) was found to be ''{1}'' but should be ''{2}'']",
 						motorIndex, motorShouldMove, motorsToMove.contains(motorIndex)));
 			}
 		}
 		logger.info("Epics MMove set as expected");
 		
 	}
 
 	
 	private List<Integer> pushEnableAxisSettings() throws DeviceException, InterruptedException {
 		ArrayList<Integer> motorsToMove = new ArrayList<Integer>();
 		if (points.size() > 0) {
 			Double[] representativePoint = (getLastPointAdded() == null) ? new Double[getNumberAxes()]
 					: getLastPointAdded();
 			// enable each axis with a non-null value in point
 			// (if any point has null component, all must be null)
 			for (int axisIndex = 0; axisIndex < axisNames.length; axisIndex++) {
 				if (representativePoint[axisIndex] != null) {
 					controller.setMMove(motorFromIndex(axisIndex), true);
 					motorsToMove.add(motorFromIndex(axisIndex));
 				}
 			}
 			// disable axes not enabled
 		}
 		for (int motorIndex = 1; motorIndex < controller.getMaximumNumberMotors() + 1; motorIndex++) {
 			if (!motorsToMove.contains(motorIndex)) {
 				controller.setMMove(motorIndex, false);
 			}
 		}
 		return motorsToMove;
 	}
 
 	private void pushNumberOfElementsAndPulses() throws DeviceException, InterruptedException {
 		try {
 			controller.setNumberOfElements(points.size());
 			controller.setStartPulseElement(1);
 			controller.setStopPulseElement(points.size()); 
 		} catch (OutOfRangeException e) {
 			throw new DeviceException("Problem setting number of elements in Epics", e);
 		}
 		try {
 			controller.setNumberOfPulses(points.size());
 		} catch (OutOfRangeException e) {
 			throw new DeviceException("Problem setting number of triggers in Epics", e);
 		}
 	}
 
 	private void pushPathsFromPoints() throws DeviceException, InterruptedException {
 		ArrayList<Integer> motorsToMove = new ArrayList<Integer>();
 		// set trajectories to move
 		if (points.size() > 0) {
 			for (int axisIndex = 0; axisIndex < axisNames.length; axisIndex++) {
 				double[] path = new double[points.size()];
 				for (int i = 0; i < points.size(); i++) {
 					Double val = points.get(i)[axisIndex];
 					if (val != null){
 						path[i] = val;		
 					} else {
 						// don't move motor
 						// (if any point has null component, all must be null)
 						path = null;
 						break;
 					}
 				}
 				if (path != null) {
 					controller.setMTraj(motorFromIndex(axisIndex), path);
 					motorsToMove.add(motorFromIndex(axisIndex));
 				}
 			}
 		}
 		// clear trajectories not to move
 		for (int motorIndex = 1; motorIndex < controller.getMaximumNumberMotors() + 1; motorIndex++) {
 			if (! motorsToMove.contains(motorIndex)) {
 				controller.setMTraj(motorIndex, new double[]{});
 			}
 		}
 		
 	}
 	@Override
 	public String toString() {
 		String s = getName() + ":\n";
 		s += "names: " + Arrays.toString(getAxisNames()) + "\n";
 		try {
 			s += "epics: " + Arrays.toString(getMotorNamesFromEpics()) + "\n";
 		} catch (DeviceException e1) {
 			logger.error(getName() + " could not read epics motor names", e1);
 			s += "epics: COULD NOT READ PV NAMES FROM EPICS";
 		} catch (InterruptedException e1) {
 			logger.error(getName() + " InterruptedException while reading epics motor names", e1);
 			s += "epics: COULD NOT READ PV NAMES FROM EPICS";
 		}
 		if ((triggerPeriod == null) && (triggerDeltas == null)) {
 			s += "NO TRIGGER PERIOD OR DELTAS CONFIGURED\n";
 		}
 		if (triggerPeriod != null) {
 			s += "triggerPeriod = " + triggerPeriod +"\n";
 		}
 		for (int p = 0; p < points.size(); p++) {
 			String t =  "";
 			if (triggerDeltas != null) {
 			try {
 				t = triggerDeltas[p] + "s";
 			} catch (ArrayIndexOutOfBoundsException e) {
 				t = "NO TRIGGER DELTA FOR THIS POINT";
 			}
 			}
 			 s += Arrays.toString(points.get(p)) + " " + t + "\n";
 		}
 		return s;
 	}
 	
 	private String[] getMotorNamesFromEpics() throws DeviceException, InterruptedException {
 		String[] names = new String[getNumberAxes()];
 		for (int i = 0; i < getNumberAxes(); i++) {
 			String pv = controller.getMName(motorFromIndex(i));
 			names[i] = pv.substring(pv.lastIndexOf(":")+1,pv.length());
 		}
 		return names;
 	}
 	
 	@Override
 	public boolean isMoving() throws DeviceException {
 		return !((moveFuture == null) || (moveFuture.isDone()));
 	}
 
 
 	@Override
 	public void startMove() throws DeviceException {
 		moveFuture = new FutureTask<Void>(new ExecuteMoveTask());
 		new Thread(moveFuture, getName() + "_execute_move").start(); // FutureTask implements Runnable
 	}
 
 	@Override
 	public void stopAndReset() throws DeviceException, InterruptedException {
 		controller.stop();
 		points = new ArrayList<Double[]>();
 		triggerPeriod = null;
 		pushDisableAllMotorMovement();
 	}
 
 	private void pushDisableAllMotorMovement() throws DeviceException, InterruptedException {
 		for (int i = 0; i < controller.getMaximumNumberMotors(); i++) {
 			controller.setMMove( i+1, false);
 		}
 	}
 
 	@Override
 	public void waitWhileMoving() throws DeviceException, InterruptedException {
 		if (moveFuture == null) {
 			return;
 		}
 		try {
 			moveFuture.get();
 		} catch (ExecutionException e) {
 			moveFuture = null;
 			throw new DeviceException(getName() + " problem executing move: ...\n   " + e.getMessage(), e.getCause());
 		}
 	}
 
 	@Override
 	public int getNumberTriggers() {
 		return points.size();
 	}
 
 	@Override
 	public double getTotalTime() throws DeviceException {
 		return (getNumberTriggers() == 0) ? 0 : triggerPeriod * (getNumberTriggers() -1);
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 	}
 
 	/**
 	 * Blocks while reading the actual positions from hardware
 	 * @return list of points
 	 * @throws InterruptedException 
 	 * @throws DeviceException 
 	 */
 	public List<double[]>readActualPositionsFromHardware() throws DeviceException, InterruptedException {
 		controller.read();
 		ArrayList<double[]> points = new ArrayList<double[]>();
 		ArrayList<double[]> axisProfiles = new ArrayList<double[]>(getNumberAxes()); // in the controllers order
 		for (int motor = 1; motor < getNumberAxes() + 1; motor++) {
 			axisProfiles.add(controller.getMActual(motor));
 		}
 		for (int pointNumber = 0; pointNumber < axisProfiles.get(0).length; pointNumber++) {
 			double[] point = new double[getNumberAxes()];
 			for (int i = 0; i < point.length; i++) {
 				point[i] = axisProfiles.get(motorFromIndex(i)-1)[pointNumber];
 			}
 			points.add(point);
 		}
 		return points;
 	}
 	
 	public static String almostEqual(double[] a, double[] b, double eps){
 		if (a == null) {
 			return "first is null";
 		}
 		if (b == null) {
 			return "second is null";
 		}
 		if (a.length != b.length) {
 			return "Lengths differ. First=" + a.length + " Second:" + b.length;
 		}
 		for (int i = 0; i < a.length; i++) {
 			if (Math.abs(a[i] - b[i]) > eps) {
 				return "Element " + i + " differs. First=" + a[i] + " Second:" + b[i];
 			}
 		}
 		return null;
 	}
 	
 	public class ExecuteMoveTask implements Callable<Void> {
 		@Override
 		public Void call() throws DeviceException, InterruptedException{
 			try {
 				controller.execute();
 			} catch (DeviceException e) {
 				logger.error("Problem in trajectory move Thread (will be thrown in waitWhileMoving()): \n", e.getMessage());
 				throw e;
 			} catch (InterruptedException e) {
 				logger.error("Interupted in trajectory move Thread (will be thrown in waitWhileMoving()): \n", e.getMessage());
 				throw e;
 			}
 			return null;
 		}
 	}
 	
 	public List<Double[]> getPointsList() {
 		return points;
 	}
 	
 	public boolean isVerifyEpicsReadbackEnabled() {
 		return verifyEpicsReadbackEnabled;
 	}
 
 	public void setVerifyEpicsReadbackEnabled(boolean verifyEpicsReadbackEnabled) {
 		this.verifyEpicsReadbackEnabled = verifyEpicsReadbackEnabled;
 	}
 }
