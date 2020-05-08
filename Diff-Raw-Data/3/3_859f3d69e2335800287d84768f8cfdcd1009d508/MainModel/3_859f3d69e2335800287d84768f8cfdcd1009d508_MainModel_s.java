 package ee.ttu.kinect.model;
 
 import java.io.File;
 import java.util.List;
 
 import ee.ttu.kinect.controller.MainController;
 
 public class MainModel {
 	
 	private Runner activeRunner;
 	
 	private final SensorRunner sensorRunner;
 	
 	private final FileRunner fileRunner;
 	
 	private File fileToPlay;
 	
 	private final CoordinateCorrection coordinateCorrection;
 	
 	private final MovementProcessor processor;
 	
 	private boolean movementAnalysisMode;
 		
 	public MainModel(MainController controller) {
 		sensorRunner = new SensorRunner(controller);
 		fileRunner = new FileRunner(controller);
 		coordinateCorrection = new CoordinateCorrection();
 		processor = new MovementProcessor();
 	}
 
 	public void setFileToPlay(File file) {
 		fileToPlay = file;
 	}
 	
 	public File getFileToPlay() {
 		return fileToPlay;
 	}
 	
 	public void startRecord() {
 		stopFileRun();
 		
 		sensorRunner.startRecord();
 	}
 
 	public void stopRecord() {
 		stopFileRun();
 		
 		sensorRunner.stopRecord();
 	}
 	
 	public void startFileRun() {
 		fileRunner.readFile(fileToPlay);
 		if (!fileRunner.isRunning()) {
 			fileRunner.start();
 			
 			activeRunner = fileRunner;
 		}
 	}
 
 	public void pauseFileRun() {
 		if (fileRunner.isRunning()) {
 			fileRunner.pause();
 		}
 	}
 	
 	public void unpauseFileRun() {
 		if (fileRunner.isRunning()) {
 			fileRunner.unpause();		}
 	}
 	
 	public void stopFileRun() {
 		if (fileRunner.isRunning()) {
 			fileRunner.stop();
 		}
 	}
 	
 	public void startSensorRun() {
 		if (!sensorRunner.isRunning()) {
 			sensorRunner.start();
 			
 			activeRunner = sensorRunner;
 		}
 	}
 	
 	public void stopSensorRun() {
 		if (sensorRunner.isRunning()) {
 			sensorRunner.stop();
 		}
 	}
 	
 	public boolean isFileRunPaused() {
 		return fileRunner.isPaused();
 	}
 
 	public void setSeatedMode() {
 		activeRunner.setSeatedMode();
 	}
 	
 	public void setDefaultMode() {
 		activeRunner.setDefaultMode();
 	}
 
 	public boolean isSeatedMode() {
 		return activeRunner.isSeatedMode();
 	}
 
 	public List<Body> getFileData() {
 		fileRunner.readFile(fileToPlay);
 		return fileRunner.getData();
 	}
 
 	public CoordinateCorrection getCoordinateCorrection() {
 		return coordinateCorrection;
 	}
 	
 	public void calculateSittingCorrection() {
 		coordinateCorrection.calculateSittingCorrection(activeRunner.body);
 	}
 	
 	public void calculateStandingCorrection() {
 		coordinateCorrection.calculateStandingCorrection(activeRunner.body);
 	}
 	
 	public void turnStandingCorrectionOff() {
 		coordinateCorrection.turnStandingCorrectionOff();
 	}
 	
 	public void turnSittingCorrectionOff() {
 		coordinateCorrection.turnSittingCorrectionOff();
 	}
 
 	public void setMovementAnalysisMode(boolean value) {
 		movementAnalysisMode = value;
 	}
 	
 	public boolean isMovementAnalysisMode() {
 		return movementAnalysisMode;
 	}
 	
 	public boolean isMovementEnded(Body body, JointType type) {
 		boolean isProcessed = false;
 		boolean isMovementEnded = false;
 		if (body != null && body.isBodyReady()) {
 			isProcessed = processor.process(body, type);
 			if (isProcessed) {
 				isMovementEnded = processor.isMovementEnded();
 //				if (isMovementEnded) {
 //					processor.outputSummaryToConsole(type);
 //				}
 //				processor.clean();
 			}
 		}
 		
 		return (isProcessed && isMovementEnded);
 	}
 
 	public List<Body> getMovementData() {
 		return processor.getDataSummary();
 	}
 
 	public double getTrajectoryMassSummary() {
 		return processor.getTrajectoryMassSummary();
 	}
 
 	public double getAccelerationMassSummary() {
 		return processor.getAccelerationMassSummary();
 	}
 		
 }
