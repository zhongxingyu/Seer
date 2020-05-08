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
 
 package uk.ac.gda.client.tomo.alignment.view.handlers.impl;
 
 import gda.data.NumTracker;
 import gda.data.PathConstructor;
 import gda.device.DeviceException;
 import gda.factory.Findable;
 import gda.jython.IScanDataPointObserver;
 import gda.jython.InterfaceProvider;
 import gda.jython.JythonServerFacade;
 import gda.observable.IObservable;
 import gda.observable.IObserver;
 import gda.util.Sleep;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.client.tomo.DoublePointList;
 import uk.ac.gda.client.tomo.ExternalFunction;
 import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
 import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentCommands;
 import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
 import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleMotorHandler;
 import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleStageMotorHandler;
 import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltBallLookupTableHandler;
 import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltController;
 import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
 import uk.ac.gda.client.tomo.view.handlers.exceptions.ExternalProcessingFailedException;
 
 /**
  *
  */
 public class TiltController implements ITiltController {
 
 	private static final String MATLAB_OUTPUT_PREFIX = "output =";
 	private static final String NAN_VALUE = "NaN";
 	private Pattern doublePattern = Pattern.compile("\\-?[0-9]*\\.?[0-9]*");
 	private static final String SUBDIRECTORY = "Subdirectory:";
 	private ExternalFunction externalProgram1;
 	private ExternalFunction externalProgram2;
 	private ITiltBallLookupTableHandler lookupTableHandler;
 
 	private ISampleStageMotorHandler sampleStageMotorHandler;
 
 	private ICameraModuleMotorHandler cameraModuleMotorHandler;
 
 	private ICameraHandler cameraHandler;
 
 	private IObservable tomoScriptController;
 
 	private String result;
 
 	private boolean test = false;
 
 	private double preTiltSs1RxVal = 0.2;
 
 	private double preTiltCam1RollVal = 0.5;
 
 	public void setPreTiltCam1RollVal(double preTiltCam1RollVal) {
 		this.preTiltCam1RollVal = preTiltCam1RollVal;
 	}
 
 	public void setPreTiltSs1RxVal(double preTiltSs1RxVal) {
 		this.preTiltSs1RxVal = preTiltSs1RxVal;
 	}
 
 	public String getResult() {
 		return result;
 	}
 
 	public void setTomoScriptController(IObservable tomoScriptController) {
 		this.tomoScriptController = tomoScriptController;
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(TiltController.class);
 
 	public void setCameraModuleMotorHandler(ICameraModuleMotorHandler cameraModuleMotorHandler) {
 		this.cameraModuleMotorHandler = cameraModuleMotorHandler;
 	}
 
 	public void setLookupTableHandler(ITiltBallLookupTableHandler lookupTableHandler) {
 		this.lookupTableHandler = lookupTableHandler;
 	}
 
 	public int getMinY(CAMERA_MODULE selectedCameraModule) throws DeviceException {
 		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
 			return Integer.MIN_VALUE;
 		}
 		return lookupTableHandler.getMinY(selectedCameraModule.getValue());
 	}
 
 	public int getMaxY(CAMERA_MODULE selectedCameraModule) throws DeviceException {
 		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
 			return Integer.MIN_VALUE;
 		}
 		return lookupTableHandler.getMaxY(selectedCameraModule.getValue());
 	}
 
 	@Override
 	public TiltPlotPointsHolder doTilt(IProgressMonitor monitor, CAMERA_MODULE module, double exposureTime)
 			throws Exception {
 		SubMonitor progress = SubMonitor.convert(monitor);
 		progress.beginTask("Preparing for Tilt alignment", 100);
 
 		// 1. set roi minY and maxY to values in the lookuptable so that the scanned images are cropped.
 		int minY = getMinY(module);
 		int maxY = getMaxY(module);
 
 		int minX = getMinX(module);
 		int maxX = getMaxX(module);
 
 		String firstScanFolder = null;
 		String secondScanFolder = null;
 		try {
 			// Move the cam1_roll and the ss1_rx to preset values so that the matlab tilt procedure evaluates good
 			// values.
 			cameraModuleMotorHandler.moveCam1Roll(progress, preTiltCam1RollVal);
 			sampleStageMotorHandler.moveSs1RxBy(progress, preTiltSs1RxVal);
 			//
 
 			cameraHandler.setUpForTilt(minY, maxY, minX, maxX);
 			logger.debug("Set the camera minY at:" + minY + " and maxY at:" + maxY);
 			double txOffset = getTxOffset(module);
 			String subDir = getSubDir();
 			try {
 				changeSubDir("tmp");
 				// Move tx by offset
 				logger.debug("the tx offset is:{}", txOffset);
 				if (!monitor.isCanceled()) {
 					// Relative to where it was - conclusion from testing with Mike
 					sampleStageMotorHandler.moveSs1TxBy(progress, txOffset);
 					// 2. scan 0 to 340 deg in steps of 20
 					logger.debug("will run scan command next");
 					if (!monitor.isCanceled()) {
						//scanThetha(progress, exposureTime);
 						if (!monitor.isCanceled()) {
 
 							// 3. call matlab - first time
 							firstScanFolder = runExternalProcess(progress, 1);
 							String result = getResult();
 							// 4. read output from matlab and move motors
 							// output= x,yz,z
 							Double[] motorsToMove = getTiltMotorPositions(result);
 							logger.debug("motorsto move:{}", motorsToMove);
 							if (!progress.isCanceled()) {
 								if (motorsToMove != null) {
 									logger.debug("Current cam1_roll is :{}",
 											cameraModuleMotorHandler.getCam1RollPosition());
 									logger.debug("Current rx is :{}", sampleStageMotorHandler.getSs1RxPosition());
 									double rz = motorsToMove[0];// roll
 									double rx = motorsToMove[1];// pitch
 
 									// sampleStageMotorHandler.moveSs1RzBy(progress, -rz);
 									cameraModuleMotorHandler.moveCam1Roll(progress,
 											cameraModuleMotorHandler.getCam1RollPosition() - rz);
 									sampleStageMotorHandler.moveSs1RxBy(progress, -rx);
 
 									logger.debug("After move cam1 roll is :{}",
 											cameraModuleMotorHandler.getCam1RollPosition());
 									logger.debug("After move ss1_rx is :{}", sampleStageMotorHandler.getSs1RxPosition());
 									if (!monitor.isCanceled()) {
 										// 5. scan 0 to 340 deg in steps of 20
 										scanThetha(progress, exposureTime);
 										if (!monitor.isCanceled()) {
 											// 6. call matlab
 											secondScanFolder = runExternalProcess(progress, 2);
 										}
 									}
 								} else {
 									throw new IllegalArgumentException("Matlab returned no values");
 								}
 							}
 
 						}
 					}
 				}
 			} finally {
 				sampleStageMotorHandler.moveSs1TxBy(progress, -txOffset);
 				changeSubDir(subDir);
 			}
 		} finally {
 			// - move the motor back
 			// motorHandler.moveSs1TxBy(progress, -txOffset);
 			cameraHandler.resetAfterTilt();
 			sampleStageMotorHandler.moveRotationMotorTo(progress, 0);
 			// Return the plottable points
 			progress.done();
 		}
 
 		return getPlottablePoint(firstScanFolder, secondScanFolder);
 	}
 
 	private String getSubDir() {
 		final boolean[] subdirChanged = new boolean[1];
 		final String[] subdir = new String[1];
 		IObserver observer = null;
 		if (tomoScriptController != null) {
 			observer = new IObserver() {
 
 				@Override
 				public void update(Object source, Object arg) {
 					if (arg instanceof String) {
 						String msg = (String) arg;
 						if (msg.startsWith(SUBDIRECTORY)) {
 							subdir[0] = msg.substring(SUBDIRECTORY.length());
 							subdirChanged[0] = true;
 						}
 
 					}
 				}
 			};
 			tomoScriptController.addIObserver(observer);
 		}
 
 		InterfaceProvider.getCommandRunner().evaluateCommand(TomoAlignmentCommands.GET_SUBDIR);
 		while (!subdirChanged[0]) {
 			Sleep.sleep(5);
 		}
 
 		if (tomoScriptController != null && observer != null) {
 			tomoScriptController.deleteIObserver(observer);
 		}
 		logger.debug("Subdir is {}", subdir[0]);
 		return subdir[0];
 	}
 
 	private void changeSubDir(final String subdir) {
 		final boolean[] subdirChanged = new boolean[1];
 		IObserver observer = null;
 		if (tomoScriptController != null) {
 			observer = new IObserver() {
 
 				@Override
 				public void update(Object source, Object arg) {
 					if (arg instanceof String) {
 						String msg = (String) arg;
 						if (msg.equals("Subdirectory set to " + subdir)) {
 							subdirChanged[0] = true;
 						}
 
 					}
 				}
 			};
 			tomoScriptController.addIObserver(observer);
 		}
 
 		InterfaceProvider.getCommandRunner()
 				.evaluateCommand(String.format(TomoAlignmentCommands.CHANGE_SUBDIR, subdir));
 		while (!subdirChanged[0]) {
 			Sleep.sleep(5);
 		}
 		if (tomoScriptController != null && observer != null) {
 			tomoScriptController.deleteIObserver(observer);
 		}
 
 		logger.debug("Subdirectory set to {}", subdir);
 	}
 
 	private void scanThetha(final IProgressMonitor progress, double exposureTime) {
 		SubMonitor subMonitor = SubMonitor.convert(progress);
 		subMonitor.beginTask("Scan", 10);
 
 		String scanCmd = String.format("scan %1$s 0 340 20 %2$s %3$f", sampleStageMotorHandler.getThethaMotorName(),
 				cameraHandler.getCameraName(), exposureTime);
 		logger.debug("Scan command being executed:{}", scanCmd);
 		PrepareTiltSubProgressMonitor prepareTiltSubProgressMonitor = new PrepareTiltSubProgressMonitor(subMonitor, 80);
 
 		prepareTiltSubProgressMonitor.subTask(String.format("Command:%1$s", scanCmd));
 		JythonServerFacade.getInstance().addIObserver(prepareTiltSubProgressMonitor);
 
 		JythonServerFacade.getInstance().evaluateCommand(scanCmd);
 
 		JythonServerFacade.getInstance().deleteIObserver(prepareTiltSubProgressMonitor);
 
 		prepareTiltSubProgressMonitor.done();
 		subMonitor.done();
 	}
 
 	private static class PrepareTiltSubProgressMonitor extends SubProgressMonitor implements IScanDataPointObserver,
 			Findable {
 
 		private String name;
 
 		public PrepareTiltSubProgressMonitor(IProgressMonitor monitor, int ticks) {
 			super(monitor, ticks);
 		}
 
 		@Override
 		public void setName(String name) {
 			this.name = name;
 
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public void update(Object source, Object arg) {
 			logger.debug("PrepareTiltSubProgressMonitor#update#{}", source);
 			String msg = arg.toString();
 			if (msg.startsWith("point")) {
 
 				subTask(String.format("Scan data information: %s", arg));
 				worked(1);
 			}
 		}
 
 	}
 
 	protected String runExternalProcess(IProgressMonitor monitor, int count) throws Exception {
 		SubMonitor progress = SubMonitor.convert(monitor);
 		result = null;
 		progress.beginTask("Matlab Processing", 10);
 		ArrayList<String> cmdAndArgs = new ArrayList<String>();
 		cmdAndArgs.add(externalProgram1.getCommand());
 		String path = PathConstructor.createFromDefaultProperty();
 		long filenumber = new NumTracker("i12").getCurrentFileNumber();
 		//
 		String imagesPath = path + File.separator + filenumber + File.separator + "projections" + File.separator;
 
 		String lastImageFilename = "p_00017.tif";
 		if (!externalProgram1.getArgs().isEmpty()) {
 			cmdAndArgs.add(externalProgram1.getArgs().get(0));
 		}
 		if (test) {
 			cmdAndArgs.add("'/dls_sw/i12/software/tomoTilt/images/projections/p_00017.tif'" + ",1,true");
 		} else {
 			String lastPartOfCmd = "'" + imagesPath + lastImageFilename + "',1,true";
 			cmdAndArgs.add(lastPartOfCmd);
 			logger.info("imageLastFileName:{}", lastPartOfCmd);
 		}
 		logger.info("CommandAndArgs1:{}", cmdAndArgs);
 		runExtProcess(progress, cmdAndArgs);
 
 		cmdAndArgs.clear();
 		cmdAndArgs.add(externalProgram2.getCommand());
 		if (!externalProgram2.getArgs().isEmpty()) {
 			cmdAndArgs.add(externalProgram2.getArgs().get(0));
 			if (test) {
 				String cmdArgs = externalProgram2.getArgs().get(1)
 						+ "'/dls_sw/i12/software/tomoTilt/images/projections/p_00017.tif','/dls_sw/i12/software/tomoTilt/images/projections/calculated_flatfield.tif'"
 						+ "," + count;
 				logger.info("Test cmd:{}", cmdArgs);
 				cmdAndArgs.add(cmdArgs);
 			} else {
 				String cmdArgs = externalProgram2.getArgs().get(1) + "'" + imagesPath + lastImageFilename + "','"
 						+ imagesPath + "calculated_flatfield.tif'," + count;
 				logger.info("External program being run:{}", cmdArgs);
 				cmdAndArgs.add(cmdArgs);
 			}
 		}
 		logger.info("CommandAndArgs2:{}", cmdAndArgs);
 		runExtProcess(progress, cmdAndArgs);
 		progress.done();
 		return imagesPath;
 
 	}
 
 	protected void runExtProcess(IProgressMonitor monitor, List<String> cmdAndArgs) throws Exception {
 		ProcessBuilder pb = new ProcessBuilder();
 		pb.redirectErrorStream(true);
 		pb.command(cmdAndArgs);
 		final Process p = pb.start();
 		try {
 			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String line;
 			while ((line = output.readLine()) != null) {
 				logger.info(line);
 				if (!line.equals("")) {
 					monitor.subTask(line);
 					if (line.startsWith(MATLAB_OUTPUT_PREFIX)) {
 						result = line;
 					}
 				}
 			}
 			int exitValue = p.waitFor();
 
 			closeStream(p.getInputStream(), "output");
 			if (exitValue != 0) {
 				throw new ExternalProcessingFailedException("External Processing Failed" + cmdAndArgs);
 			}
 
 		} catch (Exception ex) {
 			logger.error(ex.getMessage());
 			throw ex;
 		} finally {
 			p.destroy();
 		}
 	}
 
 	private static void closeStream(Closeable stream, String name) {
 		try {
 			stream.close();
 		} catch (IOException ioe) {
 			logger.warn(String.format("Unable to close process %s stream", name), ioe);
 		}
 	}
 
 	public void setExternalProgram1(ExternalFunction externalProgram1) {
 		this.externalProgram1 = externalProgram1;
 	}
 
 	public void setExternalProgram2(ExternalFunction externalProgram2) {
 		this.externalProgram2 = externalProgram2;
 	}
 
 	public double getTxOffset(CAMERA_MODULE selectedCameraModule) throws DeviceException {
 		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
 			return Double.NaN;
 		}
 		return lookupTableHandler.getTxOffset(selectedCameraModule.getValue());
 	}
 
 	protected TiltPlotPointsHolder getPlottablePoint(String firstScanFolder, String secondScanFolder)
 			throws IOException {
 		TiltPlotPointsHolder tiltPlotPointsHolder = new TiltPlotPointsHolder();
 
 		tiltPlotPointsHolder.setCenters1(getDoublePointList(firstScanFolder + File.separator + "centers_1.csv"));
 		tiltPlotPointsHolder.setEllipse1(getDoublePointList(firstScanFolder + File.separator + "ellipse_1.csv"));
 		tiltPlotPointsHolder.setCenters2(getDoublePointList(secondScanFolder + File.separator + "centers_2.csv"));
 		tiltPlotPointsHolder.setEllipse2(getDoublePointList(secondScanFolder + File.separator + "ellipse_2.csv"));
 		tiltPlotPointsHolder.setLine2(getDoublePointList(secondScanFolder + File.separator + "line_2.csv"));
 		return tiltPlotPointsHolder;
 	}
 
 	private DoublePointList getDoublePointList(String fileName) throws IOException {
 		File firstScanCentersFile = new File(fileName);
 		DoublePointList pointList = new DoublePointList();
 		if (firstScanCentersFile.exists()) {
 			FileInputStream fis = new FileInputStream(firstScanCentersFile);
 			InputStreamReader inpStreamReader = new InputStreamReader(fis);
 			BufferedReader br = new BufferedReader(inpStreamReader);
 			String rl = null;
 			rl = br.readLine();
 			while (rl != null) {
 				StringTokenizer strTokenizer = new StringTokenizer(rl, ",");
 				if (strTokenizer.countTokens() != 2) {
 					fis.close();
 					br.close();
 					throw new IllegalArgumentException("Invalid row in the table");
 				}
 				double x = Double.parseDouble(strTokenizer.nextToken());
 				double y = Double.parseDouble(strTokenizer.nextToken());
 				pointList.addPoint(x, y);
 				rl = br.readLine();
 			}
 			fis.close();
 			br.close();
 			inpStreamReader.close();
 		} else {
 			logger.error("File {} does not exist", fileName);
 		}
 		return pointList;
 	}
 
 	public int getMinX(CAMERA_MODULE selectedCameraModule) throws DeviceException {
 		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
 			return Integer.MIN_VALUE;
 		}
 		return lookupTableHandler.getMinX(selectedCameraModule.getValue());
 	}
 
 	public int getMaxX(CAMERA_MODULE selectedCameraModule) throws DeviceException {
 		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
 			return Integer.MIN_VALUE;
 		}
 		return lookupTableHandler.getMaxX(selectedCameraModule.getValue());
 	}
 
 	private Double[] getTiltMotorPositions(String result) throws Exception {
 		if (result != null) {
 			String values = result.substring(MATLAB_OUTPUT_PREFIX.length());
 			StringTokenizer tokenizer = new StringTokenizer(values, ",");
 			int count = 0;
 			Double[] motorsToMove = new Double[tokenizer.countTokens()];
 			while (tokenizer.hasMoreElements()) {
 				String token = tokenizer.nextElement().toString().trim();
 				// Matcher doublePatternMatcher = doublePattern.matcher(token);
 				// if (!doublePatternMatcher.matches()) {
 				// return null;
 				// }
 				try {
 					if (NAN_VALUE.equals(token)) {
 						throw new Exception("Unable to get correct tilt values");
 					}
 
 					motorsToMove[count] = Double.parseDouble(token);
 					count = count + 1;
 					// Only interested in the first two decimals
 					if (count == 2) {
 						break;
 					}
 				} catch (NumberFormatException nfe) {
 					logger.error("Not a number", nfe);
 					throw new Exception("Values received are not numbers", nfe);
 				}
 			}
 			return motorsToMove;
 		}
 		return null;
 	}
 
 	public void setCameraHandler(ICameraHandler cameraHandler) {
 		this.cameraHandler = cameraHandler;
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setSampleStageMotorHandler(ISampleStageMotorHandler sampleStageMotorHandler) {
 		this.sampleStageMotorHandler = sampleStageMotorHandler;
 	}
 }
