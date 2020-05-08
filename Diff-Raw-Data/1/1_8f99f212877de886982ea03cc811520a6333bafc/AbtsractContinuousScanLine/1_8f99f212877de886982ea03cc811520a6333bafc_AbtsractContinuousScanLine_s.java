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
 
 package gda.scan;
 
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.continuouscontroller.ContinuousMoveController;
 import gda.device.continuouscontroller.HardwareTriggerProvider;
 import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
 import gda.device.scannable.ContinuouslyScannableViaController;
 import gda.device.scannable.PositionConvertorFunctions;
 import gda.factory.FactoryException;
 import gda.jython.InterfaceProvider;
 import gda.observable.IObserver;
 
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.FutureTask;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.math.linear.MatrixUtils;
 import org.apache.commons.math.linear.RealVector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  */
 public abstract class AbtsractContinuousScanLine extends ConcurrentScan {
 
 	static class PositionGrabbingAdapter implements ContinuouslyScannableViaController {
 		
 		private final ContinuouslyScannableViaController delegate;
 		
 		private ScanPositionRecorder recorder;
 
 		public PositionGrabbingAdapter(ContinuouslyScannableViaController delegate) {
 			this.delegate = delegate;
 		}
 
 		public void setRecorder(ScanPositionRecorder recorder) {
 			this.recorder = recorder;
 			
 		}
 		
 		@Override
 		public void asynchronousMoveTo(Object position) throws DeviceException {
 			if (recorder != null) {
 				recorder.addPositionToCurrentPoint(delegate, position);
 			}
 			delegate.asynchronousMoveTo(position);
 		}
 //
 		@Override
 		public void setName(String name) {
 			delegate.setName(name);
 		}
 
 		@Override
 		public void reconfigure() throws FactoryException {
 			delegate.reconfigure();
 		}
 
 		@Override
 		public Object getPosition() throws DeviceException {
 			return delegate.getPosition();
 		}
 
 		@Override
 		public String getName() {
 			return delegate.getName();
 		}
 
 		@Override
 		public void addIObserver(IObserver observer) {
 			delegate.addIObserver(observer);
 		}
 
 		@Override
 		public void setAttribute(String attributeName, Object value) throws DeviceException {
 			delegate.setAttribute(attributeName, value);
 		}
 
 		@Override
 		public String toString() {
 			return delegate.toString();
 		}
 
 		@Override
 		public void setOperatingContinuously(boolean b) throws DeviceException {
 			delegate.setOperatingContinuously(b);
 		}
 
 		@Override
 		public void deleteIObserver(IObserver observer) {
 			delegate.deleteIObserver(observer);
 		}
 
 		@Override
 		public void deleteIObservers() {
 			delegate.deleteIObservers();
 		}
 
 		@Override
 		public void moveTo(Object position) throws DeviceException {
 			delegate.moveTo(position);
 		}
 
 		@Override
 		public Object getAttribute(String attributeName) throws DeviceException {
 			return delegate.getAttribute(attributeName);
 		}
 
 		@Override
 		public ContinuousMoveController getContinuousMoveController() {
 			return delegate.getContinuousMoveController();
 		}
 
 		@Override
 		public void close() throws DeviceException {
 			delegate.close();
 		}
 
 		@Override
 		public void setProtectionLevel(int newLevel) throws DeviceException {
 			delegate.setProtectionLevel(newLevel);
 		}
 
 		@Override
 		public String checkPositionValid(Object position) throws DeviceException {
 			return delegate.checkPositionValid(position);
 		}
 
 		@Override
 		public int getProtectionLevel() throws DeviceException {
 			return delegate.getProtectionLevel();
 		}
 
 		@Override
 		public void stop() throws DeviceException {
 			delegate.stop();
 		}
 
 		@Override
 		public boolean isBusy() throws DeviceException {
 			return delegate.isBusy();
 		}
 
 		@Override
 		public void waitWhileBusy() throws DeviceException, InterruptedException {
 			delegate.waitWhileBusy();
 		}
 
 		@Override
 		public boolean isAt(Object positionToTest) throws DeviceException {
 			return delegate.isAt(positionToTest);
 		}
 
 		@Override
 		public void setLevel(int level) {
 			delegate.setLevel(level);
 		}
 
 		@Override
 		public int getLevel() {
 			return delegate.getLevel();
 		}
 
 		@Override
 		public String[] getInputNames() {
 			return delegate.getInputNames();
 		}
 
 		@Override
 		public void setInputNames(String[] names) {
 			delegate.setInputNames(names);
 		}
 
 		@Override
 		public String[] getExtraNames() {
 			return delegate.getExtraNames();
 		}
 
 		@Override
 		public void setExtraNames(String[] names) {
 			delegate.setExtraNames(names);
 		}
 
 		@Override
 		public void setOutputFormat(String[] names) {
 			delegate.setOutputFormat(names);
 		}
 
 		@Override
 		public String[] getOutputFormat() {
 			return delegate.getOutputFormat();
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		public void atStart() throws DeviceException {
 			delegate.atStart();
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		public void atEnd() throws DeviceException {
 			delegate.atEnd();
 		}
 
 		@Override
 		public void atScanStart() throws DeviceException {
 			delegate.atScanStart();
 		}
 
 		@Override
 		public void atScanEnd() throws DeviceException {
 			delegate.atScanEnd();
 		}
 
 		@Override
 		public void atScanLineStart() throws DeviceException {
 			delegate.atScanLineStart();
 		}
 
 		@Override
 		public void atScanLineEnd() throws DeviceException {
 			delegate.atScanLineEnd();
 		}
 
 		@Override
 		public void atPointStart() throws DeviceException {
 			delegate.atPointStart();
 		}
 
 		@Override
 		public void atPointEnd() throws DeviceException {
 			delegate.atPointEnd();
 		}
 
 		@Override
 		public void atLevelMoveStart() throws DeviceException {
 			delegate.atLevelMoveStart();
 		}
 
 		@Override
 		public void atCommandFailure() throws DeviceException {
 			delegate.atCommandFailure();
 		}
 
 		@Override
 		public String toFormattedString() {
 			return delegate.toFormattedString();
 		}
 
 		@Override
 		public boolean isOperatingContinously() {
 			return delegate.isOperatingContinously();
 		}
 		
 		
 	}
 	
 	class ScanPositionRecorder {
 		
 		LinkedList<Map<Scannable, RealVector>> points = 
 			new LinkedList<Map<Scannable, RealVector>>();
 
 		void startNewPoint() {
 			points.add(new HashMap<Scannable, RealVector>());
 		}
 		
 		void addPositionToCurrentPoint(Scannable scannable, Object demandPosition) {
 			double[] doublePosition = ArrayUtils.toPrimitive(PositionConvertorFunctions.toDoubleArray(demandPosition));
 			RealVector positionVector = MatrixUtils.createRealVector(doublePosition);
 			points.getLast().put(scannable, positionVector);
 		}
 		
 		List<Map<Scannable, RealVector>> getPoints() {
 			return points;
 		}
 
 	}
 	
 	private static final Logger logger = LoggerFactory.getLogger(TrajectoryScanLine.class);
 
 	// TODO: handle requirement for repeating a line
 
 	private ContinuousMoveController controller;
 
 	protected Vector<HardwareTriggerableDetector> detectors = new Vector<HardwareTriggerableDetector>();
 
 	private Vector<ContinuouslyScannableViaController> scannablesToMove = new Vector<ContinuouslyScannableViaController>();
 
 	ScanPositionRecorder scanPositionRecorder;
 
 	private boolean detectorsIntegrateBetweenTriggers;
 	
 	public AbtsractContinuousScanLine(Object[] args) throws IllegalArgumentException {
 		super(wrapContinuouslyScannables(args));
 		extractScannablesToScan();
 		extractDetectors();
 		if (detectors.size() == 0) {
 			throw new IllegalArgumentException("At least one (HardwareTriggerableDetector) detector must be specified (to provide a trigger period or profile).");
 		}
 		extractContinuousMoveController(scannablesToMove);
 		checkDetectorsAllUseTheScanController();
 		determineIfDetectorsIntegrateBetweenTriggers();
 		// TODO: if anything is a PositionCallableProvider then check the pipeline length is unbounded.
 	}
 
 	@Override
 	public boolean isReadoutConcurrent() {
 		return false;  // should be false even if enabled for beamline
 	}
 	
 	private static Object[] wrapContinuouslyScannables(Object[] args) {
 		for (int i = 0; i < args.length; i++) {
 			if (args[i] instanceof ContinuouslyScannableViaController) {
 				args[i] = new PositionGrabbingAdapter((ContinuouslyScannableViaController)args[i]);
 			}
 		}
 		return args;
 	}
 
 	protected void extractScannablesToScan() {
 		for (Scannable scn : allScannables) {
 			if ((scn.getInputNames().length + scn.getExtraNames().length) != 0 ) {
 				// ignore zero input-output name devices
 				if (!(scn instanceof ContinuouslyScannableViaController)) {
 					throw new IllegalArgumentException("Scannable " + scn.getName()
 							+ " is not ContinouselyScannable so cannot be used in a TrajectoryScanLine");
 				}
 				scannablesToMove.add((ContinuouslyScannableViaController) scn);
 			}
 		}
 	}
 
 	private void extractDetectors() {
 		for (Detector det : allDetectors) {
 			if (!(det instanceof HardwareTriggerableDetector)) {
 				throw new IllegalArgumentException("Detector " + det.getName()
 						+ " is not a HardwareTriggerableDetector so cannot be used in a TrajectoryScanLine");
 			}
 			detectors.add((HardwareTriggerableDetector) det);
 		}
 	}
 
 	private void extractContinuousMoveController(Vector<ContinuouslyScannableViaController> scannables) {
 		for (ContinuouslyScannableViaController scn : scannables) {
 			ContinuousMoveController scnsController;
 			try {
 				scnsController = scn.getContinuousMoveController();
 			} catch (ClassCastException e) {
 				throw new IllegalArgumentException(scn.getName()
 						+ " has a continuous move controller that does not support trajectory scanning");
 			}
 			if (scnsController == null) {
 				throw new IllegalArgumentException(scn.getName() + " has no Trajectory scan controller configured.");
 			}
 			if (getController() == null) {
 				setController(scnsController);
 			} else {
 				if (getController() != scnsController) {
 					throw new IllegalArgumentException(scn.getName()
 							+ " has a different trajectory scan controller than another scannable to be scanned over");
 				}
 			}
 		}
 	}
 
 	private void checkDetectorsAllUseTheScanController() {
 		for (HardwareTriggerableDetector det : detectors) {
 			HardwareTriggerProvider triggerProvider = det.getHardwareTriggerProvider();
 			if (triggerProvider == null) {
 				throw new IllegalArgumentException("Detector " + det.getName()
 						+ " has no HardwareTriggerProvider configured.");
 			}
 			if (triggerProvider != getController()) {
 				throw new IllegalArgumentException(MessageFormat.format(
 						"Detector {0} is configured with a different continous move controller ({1}) than that of the specified Scannable ({2}).",
 						det.getName(), triggerProvider.getName(), getController().getName()));
 			}
 		}
 	}
 
 	private void determineIfDetectorsIntegrateBetweenTriggers() {
 		Iterator<HardwareTriggerableDetector> detectorIterator = detectors.iterator();
 		detectorsIntegrateBetweenTriggers = detectorIterator.next().integratesBetweenPoints();
 		while (detectorIterator.hasNext()) {
 			if (detectorIterator.next().integratesBetweenPoints() != detectorsIntegrateBetweenTriggers) {
 				throw new IllegalArgumentException(
 						"Detectors are inconsistently configured: some are set to integrateBetweenPoints(), but not all");
 			}
 		}
 	}
 	
 	@Override
 	protected void prepareDevicesForCollection() throws Exception {
 		try {
 			// 1. Prepare the Scannables and Detectors to be continuously operated
 			for (ContinuouslyScannableViaController scn : scannablesToMove) {
 				scn.setOperatingContinuously(true);
 			}
 			logger.info(MessageFormat.format(
 					"Requests to move Scannables ({0}) will be collected by the TrajectoryMoveController ({1})",
 					scannablesToString(scannablesToMove), getController().getName()));
 
 			for (HardwareTriggerableDetector det : detectors) {
 				det.setHardwareTriggering(true);
 			}
 			logger.info(MessageFormat
 					.format("Requests to collect data on Detectors ({0}) will be collected by the TrajectoryMoveController ({1})",
 							scannablesToString(detectors), getController().getName()));
 
 		} catch (Exception e) {
 			logger.info("problem in prepareDevicesForCollection()");
 			for (ContinuouslyScannableViaController scn : scannablesToMove) {
 				scn.setOperatingContinuously(false);
 			}
 			for (HardwareTriggerableDetector det : detectors) {
 				det.setHardwareTriggering(false);
 			}
 			throw e;
 		}
 		super.prepareDevicesForCollection();
 	}
 	
 	@Override
 	public void doCollection() throws Exception {
 
 		logger.info("Starting TrajectoryScanLine for scan: '" + getName() + "' (" + getCommand() + ")" );
 		
 		getController().stopAndReset(); // should be ready anyway
 
 		try {
 
 			// TODO: Check the controller is not moving
 
 			if (detectorsIntegrateBetweenTriggers) {
 				scanPositionRecorder = new ScanPositionRecorder();
 				for (ContinuouslyScannableViaController scn : scannablesToMove) {
 					((PositionGrabbingAdapter) scn).setRecorder(scanPositionRecorder);
 				}
 			}
 			
 			// 2. Perform the 'scan'. Scannables must direct their asynchronousMoveTo methods to the controller,
 			// and detectors should ingorec alls made to collectData. Detectors will have their collectionTimes set.
 			// ScanDataPoints will be created although will likely be incomplete awaiting results from
 			// Scannables/Detectors that implement PositionCallableProvider.
 
 			super.doCollection();
 
 			configureControllerPositions(detectorsIntegrateBetweenTriggers);
 
 			configureControllerTriggerTimes();
 
 			// 4a. Prepare the controller and move to the start position
 			// (some detectors timeout waiting for a first trigger once armed)
 			getController().prepareForMove();
 			// 4b. Prepare hardware in parallel and wait for it all to be ready
 			armDetectors();
 
 			// 5. Start the move which will result in hardware triggers to the already armed Detectors.
 			getController().startMove();
 
 			// 6. Wait for completion (Scannables obtain their status from the controller)
 			getController().waitWhileMoving();
 			for (HardwareTriggerableDetector det : detectors) {
 				det.waitWhileBusy();
 			}
 		} catch (Exception e) {
 			String msg = "problem in doCollection() so calling " + getController().getName() + "stopAndReset";
 			logger.info(msg);
 			InterfaceProvider.getTerminalPrinter().print(msg);
 			getController().stopAndReset();
 			throw e;
 		} finally {
 			for (ContinuouslyScannableViaController scn : scannablesToMove) {
 				scn.setOperatingContinuously(false);
 			}
 			for (HardwareTriggerableDetector det : detectors) {
 				det.setHardwareTriggering(false);
 			}
 		}
 	}
 
 	abstract protected void configureControllerPositions(boolean detectorsIntegrateBetweenTriggers) throws DeviceException, InterruptedException, Exception;
 
 	final protected double extractCommonCollectionTimeFromDetectors() throws DeviceException {
 		double period = detectors.get(0).getCollectionTime();
 		for (HardwareTriggerableDetector det : detectors.subList(1, detectors.size())) {
 			double detsPeriod = det.getCollectionTime();
 			if ((Math.abs(detsPeriod - period) / period) > .1 / 100) {
 				throw new DeviceException(
 						MessageFormat
 								.format("Requested trigger time ({0}) is more than .1% different from the time already requested for this point ({1}).",
 										detsPeriod, period));
 			}
 			period = (period + detsPeriod) / 2.; // average away differences less than .1% to be pedantic
 		}
 		return period;
 	}
 	
 	protected abstract void configureControllerTriggerTimes() throws DeviceException ;
 
 	// TODO: Consider this change. It would make writing HTDs simpler
 //	@Override
 //	protected void triggerDetectorsAndWait() throws InterruptedException, DeviceException {
 //		// Don't trigger detectors through Detector.collectData();
 //	}
 	
 
 	List<Map<Scannable,double[]>> generateTrajectoryForDetectorsThatIntegrateBetweenTriggers() {
 		List<Map<Scannable, double[]>> triggers = 
 			new LinkedList<Map<Scannable, double[]>>();
 		List<Map<Scannable, RealVector>> binCentres = 
 			scanPositionRecorder.getPoints();
 		
 		HashMap<Scannable, double[]> pointToAdd;
 		
 		Set<Scannable> scannables = binCentres.get(0).keySet();
 		
 		// Add first trigger: xtrig[0] = bincentre[0] - (bincentre[1] - bincentre[0]) / 2
 		pointToAdd = new HashMap<Scannable, double[]>();
 		for (Scannable scannable : scannables) {
 			RealVector first = binCentres.get(0).get(scannable);
 			RealVector second = binCentres.get(1).get(scannable);
 			double[] firstTrigger = first.subtract(second.subtract(first).mapDivide(2.)).toArray();
 			pointToAdd.put(scannable, firstTrigger );
 		}
 		triggers.add(pointToAdd);	
 		
 		// Add middle triggers: xtrig[i] = (bincentre[i] + bincentre[i+1]) / 2
 		for (int i = 0; i < binCentres.size() -1 ; i++) { // not the last one
 			pointToAdd = new HashMap<Scannable, double[]>();
 			for (Scannable scannable : scannables) {
 				RealVector current = binCentres.get(i).get(scannable);
 				RealVector next = binCentres.get(i+1).get(scannable);
 				double[] trigger = current.add(next).mapDivide(2.).toArray();
 				pointToAdd.put(scannable, trigger);
 			}
 			triggers.add(pointToAdd);	
 		}
 		
 		// Add last trigger: xtrig[n+1] = bincentre[n] + (bincentre[n] - bincentre[n-1]) / 2
 		pointToAdd = new HashMap<Scannable, double[]>();
 		for (Scannable scannable : scannables) {
 			int lastIndex = binCentres.size() - 1;
 			RealVector last = binCentres.get(lastIndex).get(scannable);
 			RealVector secondLast = binCentres.get(lastIndex-1).get(scannable);
 			double[] lastTrigger = last.add(last.subtract(secondLast).mapDivide(2.)).toArray();
 			pointToAdd.put(scannable, lastTrigger );
 		}
 		triggers.add(pointToAdd);	
 		
 		return triggers;
 	}
 
 
 	@Override
 	protected void callAtPointStartHooks() throws DeviceException {
 		super.callAtPointStartHooks();
 		if (scanPositionRecorder != null) {
 			scanPositionRecorder.startNewPoint();
 		}
 	}
 	
 	private String scannablesToString(Vector<? extends Scannable> scannables) {
 		Vector<String> names = new Vector<String>();
 		for (Scannable scn : scannables) {
 			names.add(scn.getName());
 		}
 		return Arrays.toString(names.toArray());
 
 	}
 
 	//@SuppressWarnings("null")
 	private void armDetectors() throws DeviceException, InterruptedException {
 
 		class ArmDetector implements Callable<Void> {
 			public final HardwareTriggerableDetector det;
 
 			public ArmDetector(HardwareTriggerableDetector det) {
 				this.det = det;
 			}
 
 			@Override
 			public Void call() throws Exception {
 				try {
 					det.arm();
 				} catch (Exception e) {
 					throw new Exception("Problem arming " + det.getName(), e);
 				}
 				return null;
 			}
 		}
 
 		// Arm each detector in a new thread
 		LinkedList<FutureTask<Void>> futureTasks = new LinkedList<FutureTask<Void>>();
 		for (HardwareTriggerableDetector det : detectors) {
 			futureTasks.add(new FutureTask<Void>(new ArmDetector(det)));
 			(new Thread(futureTasks.getLast(), "TrajectoryScanLine.ArmDetector-" + det.getName())).start();
 		}
 		
 		// Wait for each detector to arm (cancelling other arm-tasks and stopping all detectors on a failure.
 		try {
 			while(!futureTasks.isEmpty()) {
 				FutureTask<Void> task = futureTasks.pop();
 				task.get();
 			}
 		} catch (ExecutionException e) {
 			logger.error(e.getClass() + " while arming detectors:", e.getCause());
 			cancelRemainingTasks(futureTasks);
 			stopDetectors();
 			throw new DeviceException("Problem arming detectors: "+ e.getMessage(), e.getCause());
 		} catch (InterruptedException e) {
 			logger.error("Interrupted while arming detectors", e);
 			cancelRemainingTasks(futureTasks);
 			stopDetectors();
 			throw e;
 		}
 	}
 
 	private void cancelRemainingTasks(List<FutureTask<Void>> futures) {
 		logger.info("cancelling remaining detector preparation tasks");
 		for (Future<Void> future : futures) {
 			future.cancel(true);
 		}
 	}
 
 	protected void stopDetectors() throws DeviceException {
 		logger.info("stopping detector(s)");
 		for (HardwareTriggerableDetector det : detectors) {
 			det.stop();
 		}
 	}
 
 	protected ContinuousMoveController getController() {
 		return controller;
 	}
 
 	private void setController(ContinuousMoveController controller) {
 		this.controller = controller;
 	}
 }
