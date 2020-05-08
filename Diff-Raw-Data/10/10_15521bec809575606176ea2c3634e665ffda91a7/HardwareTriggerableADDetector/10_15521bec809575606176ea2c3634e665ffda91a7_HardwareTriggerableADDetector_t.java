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
 
 package gda.device.detector.addetector;
 
 import static org.apache.commons.io.FileUtils.waitFor;
 import gda.data.nexus.tree.NexusTreeProvider;
 import gda.device.DeviceException;
 import gda.device.continuouscontroller.HardwareTriggerProvider;
 import gda.device.detector.NexusDetector;
 import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
 import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
 import gda.device.scannable.PositionCallableProvider;
 import gda.jython.InterfaceProvider;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.concurrent.Callable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HardwareTriggerableADDetector extends ADDetector implements HardwareTriggerableDetector,
 		PositionCallableProvider<NexusTreeProvider> {
 
 	private static Logger logger = LoggerFactory.getLogger(ADDetector.class);
 	
 	private static int SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES = 5;
 	
 	private HardwareTriggerProvider triggerProvider;
 
 	private NXCollectionStrategyPlugin hardwareTriggeredCollectionStrategy;
 	
 	private NXCollectionStrategyPlugin nonHardwareTriggeredCollectionStrategy;
 	
 	private boolean exposureCompleteWhenFileIsVisible = false;
 
 	private boolean exposureCompleteWhenArrayCounterSaysSo = false;
 
 	private double exposureCompletionTimeoutS = 60.;
 
 	private boolean hardwareTriggering;
 
 	private String baseFilepathForMultipleExposure;
 	
 	private Object createFileNameGuard = new Object();
 
 	
 	/**
 	 * The exposure number to put in the next positionCallable when hardware triggering. These start at 0 and are
 	 * initialised in atScanLineStart() (arm() is too late).
 	 */
 	private int nextPositionCallableExposureNumberToReturn; // start at 0
 
 	private boolean integratesBetweenPoints;
 
 	private int numberImagesToCollect;
 
 	public void setIntegratesBetweenPoints(boolean integratesBetweenPoints) {
 		this.integratesBetweenPoints = integratesBetweenPoints;
 	}
 
 	public void setHardwareTriggerProvider(HardwareTriggerProvider triggerProvider) {
 		this.triggerProvider = triggerProvider;
 	}
 
 	@Override
 	public HardwareTriggerProvider getHardwareTriggerProvider() {
 		return triggerProvider;
 	}
 
 
 	public void setExposureCompleteWhenFileIsVisible(boolean exposureCompleteWhenFileIsVisible) {
 		this.exposureCompleteWhenFileIsVisible = exposureCompleteWhenFileIsVisible;
 	}
 
 	public boolean isExposureCompleteWhenFileIsVisible() {
 		return exposureCompleteWhenFileIsVisible;
 	}
 
 	public void setExposureCompleteWhenArrayCounterSaysSo(boolean exposureCompleteWhenArrayCounterSaysSo) {
 		this.exposureCompleteWhenArrayCounterSaysSo = exposureCompleteWhenArrayCounterSaysSo;
 	}
 
 	public boolean isExposureCompleteWhenArrayCounterSaysSo() {
 		return exposureCompleteWhenArrayCounterSaysSo;
 	}
 
 	public double getExposureCompletionTimeoutS() {
 		return exposureCompletionTimeoutS;
 	}
 
 	public void setExposureCompletionTimeoutS(double exposureCompletionTimeoutS) {
 		this.exposureCompletionTimeoutS = exposureCompletionTimeoutS;
 	}
 
 	@Override
 	public boolean integratesBetweenPoints() {
 		return integratesBetweenPoints;
 	}
 
 	@Override
 	public void setHardwareTriggering(boolean hardwareTriggering) throws DeviceException {
 		this.hardwareTriggering = hardwareTriggering;
 		if (hardwareTriggering) {
 			if (getHardwareTriggeredCollectionStrategy() == null) {
 				throw new IllegalStateException("No hardwareTriggeredCollectionStrategy configured");
 			}
 			setCollectionStrategy(getHardwareTriggeredCollectionStrategy());
 		} else {
 			if (getNonHardwareTriggeredCollectionStrategy() == null) {
 				throw new IllegalStateException("No nonHardwareTriggeredCollectionStrategy configured");
 			}
 			setCollectionStrategy(getNonHardwareTriggeredCollectionStrategy());
 		}
 	}
 	
 	@Override
 	public void setNumberImagesToCollect(int numberImagesToCollect) {
 		this.numberImagesToCollect = numberImagesToCollect;
 	}
 
 	public int getNumberImagesToCollect() {
 		return numberImagesToCollect;
 	}
 	
 	
 	@Override
 	public void atScanStart() throws DeviceException 
 	{
 		if (!isHardwareTriggering()) {
 			super.atScanStart();
 		}
 		
 	}
 	
 	@Override
 	public boolean isHardwareTriggering() {
 		return hardwareTriggering;
 	}
 
 	/**
 	 * Read settings of filewriter for later use when predicting filenames to put into callables
 	 */
 	@Override
 	public void atScanLineStart() throws DeviceException {
 		try {
 			int fileNumber_RBV = getNdFile().getFileNumber_RBV();
 			String fileTemplate_RBV = getNdFile().getFileTemplate_RBV();
 			String filePath_RBV = getNdFile().getFilePath_RBV();
 			String fileName_RBV = getNdFile().getFileName_RBV();
 			
 			baseFilepathForMultipleExposure = String.format(fileTemplate_RBV, filePath_RBV, fileName_RBV, fileNumber_RBV);
 			getAdBase().setArrayCounter(0);
 		} catch (Exception e) {
 			throw new DeviceException(e.getClass() + " while creating filepath", e);
 		}
 
 	}
 
 	@Override
 	public void collectData() throws DeviceException {
 		if (!isHardwareTriggering()) {
 			super.collectData();
 		} else {
 			// only called when in hardware triggering mode
 			try {
 				getCollectionStrategy().prepareForCollection(getCollectionTime(), getNumberImagesToCollect(), null);
 				// Set number of images: the last trigger to end the exposure is superfluous
 				getCollectionStrategy().collectData();
 				nextPositionCallableExposureNumberToReturn = 0;
 			} catch (Exception e) {
 				throw new DeviceException(MessageFormat.format("{0} during {1} arm: {2}", e.getClass(), getName(),
 						e.getMessage()), e);
 			}
 		}
 	}
 
 	@Override
 	protected String createFileName() throws Exception {
 		if (isHardwareTriggering()) {
 			synchronized (createFileNameGuard) {
 				String fileName = createFileNameForExposure(nextPositionCallableExposureNumberToReturn);
 				nextPositionCallableExposureNumberToReturn++;
 				return fileName;
 			}
 		}
 		return super.createFileName();
 	}
 
 	protected String createFileNameForExposure(int exposureNumber) {
 		String base = baseFilepathForMultipleExposure;
 		// e.g. p124124.tif --> p124124_00004.tif
 		String s1 = base.split("\\.")[0];
 		String s2 = String.format("_%05d.", exposureNumber);
 		String s3 = base.split("\\.")[1];
 		return s1 + s2 + s3;
 	}
 
 	@Override
 	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
 		try {
 			if (isHardwareTriggering()) {
 				if (isComputeStats()) {
 					throw new IllegalStateException(
 							"Cannot currently return stats while hardware triggering (computeStats is true)");
 				}
 				if (isComputeCentroid()) {
 					throw new IllegalStateException(
 							"Cannot currently return a centroid while hardware triggering (computeStats is true)");
 				}
 				MultipleExposurePositionCallable callable = new MultipleExposurePositionCallable(super.getPositionCallable().call(),
 						nextPositionCallableExposureNumberToReturn, this);
				clearCachedCallable();
 				return callable;
 			}
 			return new CompletedExposurePositionCallable(super.getPositionCallable().call());
 		} catch (DeviceException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new DeviceException(e.getMessage(),e);
 		}
 	}
 
 	public void waitWhileExposureCompletes(int exposureNumber) throws InterruptedException, Exception {
 		if (isExposureCompleteWhenArrayCounterSaysSo()) {
 			getAdBase().waitForArrayCounterToReach(exposureNumber, (int) (getExposureCompletionTimeoutS() * 1000.));
 		}
 
 		if (isExposureCompleteWhenFileIsVisible()) {
 			waitForFileToBeVisible(createFileNameForExposure(exposureNumber), (int) (getExposureCompletionTimeoutS()));
 
 		}
 	}
 
 	private void waitForFileToBeVisible(final String pathname, final int timeoutS) throws IOException {
 
 		long startTimeMillis = System.currentTimeMillis();
 		long remainingMillis;
 		boolean firstTimeRoundLoop = true;
 		
 		while ((remainingMillis = timeoutS*1000 - (System.currentTimeMillis() - startTimeMillis)) >= 0) {
 			if (!firstTimeRoundLoop) {
 				// message if no file yet
 				int totalTimeWaiting = (int) (timeoutS - (remainingMillis / 1000.));
 				String msg = getName() + " waiting for file '" + pathname + " to appear: " + totalTimeWaiting + "/"
 						+ timeoutS + "s";
 				logger.info(msg);
 				InterfaceProvider.getTerminalPrinter().print(msg);
 			}
 			firstTimeRoundLoop = false;
 			// wait for file for a bit
 			logger.info("remainingMillis:" + remainingMillis);
 			int nextWaitTimeS = ((remainingMillis * 1000) > SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES) ? SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES :
 				(int) remainingMillis * 1000;
 			if (waitFor(new File(pathname), nextWaitTimeS)) {
 				return;
 			}
 			
 		}
 		// error if no file after timoutS
 		throw new IOException("The file '" + pathname + "' did not exist despite waiting " + timeoutS
 				+ "s for it to appear");
 
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		super.afterPropertiesSet();
 		if (this.isHardwareTriggering() && hardwareTriggeredCollectionStrategy == null)
 			throw new IllegalStateException("hardwareTriggeredCollectionStrategy is not defined");
 		if (!this.isHardwareTriggering() && nonHardwareTriggeredCollectionStrategy == null)
 			throw new IllegalStateException("nonHardwareTriggeredCollectionStrategy is not defined");
 	}
 	
 	public NXCollectionStrategyPlugin getHardwareTriggeredCollectionStrategy() {
 		return hardwareTriggeredCollectionStrategy;
 	}
 
 	public void setHardwareTriggeredCollectionStrategy(NXCollectionStrategyPlugin hardwareTriggeredCollectionStrategy) {
 		this.hardwareTriggeredCollectionStrategy = hardwareTriggeredCollectionStrategy;
 	}
 
 	public NXCollectionStrategyPlugin getNonHardwareTriggeredCollectionStrategy() {
 		return nonHardwareTriggeredCollectionStrategy;
 	}
 
 	public void setNonHardwareTriggeredCollectionStrategy(NXCollectionStrategyPlugin nonHardwareTriggeredCollectionStrategy) {
 		this.nonHardwareTriggeredCollectionStrategy = nonHardwareTriggeredCollectionStrategy;
 		setCollectionStrategy(nonHardwareTriggeredCollectionStrategy);
 	}
 
 }
 
 /**
  * A Callable suitable for return by {@link PositionCallableProvider#getPositionCallable()} for {@link NexusDetector}s
  * while operated in a mode where a NexusTreeProvider can be generated in the main scan thread. The image acquisition
  * associated with an instance is assumed to be complete when the instance is created.
  */
 class CompletedExposurePositionCallable implements Callable<NexusTreeProvider> {
 
 	private final NexusTreeProvider preloadedPosition;
 
 	public CompletedExposurePositionCallable(NexusTreeProvider preloadedPosition) {
 		this.preloadedPosition = preloadedPosition;
 	}
 
 	@Override
 	public NexusTreeProvider call() throws Exception {
 		return getPreloadedPosition();
 	}
 
 	public NexusTreeProvider getPreloadedPosition() {
 		return preloadedPosition;
 	}
 }
 
 /**
  * A Callable suitable for return by {@link PositionCallableProvider#getPositionCallable()} for {@link NexusDetector}s
  * while operated in a mode where a NexusTreeProvider can be generated in the main scan thread. The image acquisition
  * associated with an instance is _not_ assumed not be complete when the instance is created. Instead when
  * {@link #call()} will block until {@link HardwareTriggerableADDetector#waitWhileExposureCompletes(int exposureNumber)} returns and then
  * return the preloadedPosition.
  */
 
 class MultipleExposurePositionCallable extends CompletedExposurePositionCallable {
 
 	private final int exposureNumber;
 
 	private final HardwareTriggerableADDetector detector;
 
 	public MultipleExposurePositionCallable(NexusTreeProvider preloadedPosition, int exposureNumber, HardwareTriggerableADDetector detector) {
 		super(preloadedPosition);
 		this.exposureNumber = exposureNumber;
 		this.detector = detector;
 	}
 
 	@Override
 	public NexusTreeProvider call() throws Exception {
 		detector.waitWhileExposureCompletes(exposureNumber);
 		return getPreloadedPosition();
 	}
 }
 
