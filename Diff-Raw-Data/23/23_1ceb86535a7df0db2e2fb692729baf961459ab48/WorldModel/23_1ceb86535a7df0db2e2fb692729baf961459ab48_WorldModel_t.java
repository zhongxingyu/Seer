 package raisa.domain;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import raisa.comms.SampleParser;
 import raisa.comms.SensorListener;
 import raisa.util.CollectionUtil;
 import raisa.util.Vector2D;
 
 
 public class WorldModel implements Serializable, SensorListener {
 	private static final long serialVersionUID = 1L;
 
 	private List<Sample> samples = new ArrayList<Sample>();
 	private List<Robot> states = new ArrayList<Robot>();
 	private List<SampleFixer> sampleFixers = new ArrayList<SampleFixer>();
 
 	private Grid grid = new Grid();
 	
 	private List<SampleListener> sampleListeners = new ArrayList<SampleListener>();
 	private List<RobotListener> robotListeners = new ArrayList<RobotListener>();
 	private String latestMapFilename;
 	public WorldModel() {
 		addState(new Robot());
 		sampleFixers.add(new AveragingSampleFixer(10, 10.0f));
 	}
 	
 	public List<Sample> getSamples() {
 		return samples;
 	}
 	
 	public List<Robot> getStates() {		
 		List<Robot> copy = new ArrayList<Robot>();
 		synchronized(states) {
 			copy.addAll(states);
 		}
 		return copy;
 	}
 	
 	@Override
 	public void sampleReceived(String message) {
 		Sample sample = new SampleParser().parse(message);
 		for (SampleFixer fixer : sampleFixers) {
 		 sample = fixer.fix(sample);
 		}
 		addSample(sample);
 	}
 	
 	public void addSample(Sample sample) {
 		samples.add(sample);	
 		notifySampleListeners(sample);
 	}
 
 	public void addState(Robot state) {
 		synchronized(states) {
 			states.add(state);
 		}
 		calculateSpeed();
 		notifyRobotListeners(state);
 	}
 	
 	/**
 	 * Calculate speed for tracks based on locations and timestamps of the previous states.
 	 */
 	private void calculateSpeed() {
 		float currentSpeedLeftTrack = 0.0f;
 		float currentSpeedRightTrack = 0.0f;
 		List<Robot> pastStates = CollectionUtil.takeLast(states, 5);
 		if (pastStates.size() > 1) {
 			boolean isFirst = true;
 			Vector2D previousPositionLeftTrack = new Vector2D(), previousPositionRightTrack = new Vector2D();
 			long accumulatedTime = 0, previousTimestamp = 0;
 			float accumulatedDistanceLeftTrack = 0.0f, accumulatedDistanceRightTrack = 0.0f;
 			for (Robot r : pastStates) {
 				if (isFirst) {
 					isFirst = false;
 				} else {
 					accumulatedDistanceLeftTrack += (r.isDirectionLeftTrackForward() ? 1.0f : -1.0f) * previousPositionLeftTrack.distance(r.getPositionLeftTrack());
 					accumulatedDistanceRightTrack += (r.isDirectionRightTrackForward() ? 1.0f : -1.0f) * previousPositionRightTrack.distance(r.getPositionRightTrack());
 					accumulatedTime += r.getTimestampMillis() - previousTimestamp;
 				}
 				previousPositionLeftTrack = r.getPositionLeftTrack();
 				previousPositionRightTrack = r.getPositionRightTrack();
 				previousTimestamp = r.getTimestampMillis();				
 			}
 			currentSpeedLeftTrack = accumulatedDistanceLeftTrack / (accumulatedTime / 10.0f);
 			currentSpeedRightTrack = accumulatedDistanceRightTrack / (accumulatedTime / 10.0f);			
 		}
 		getLatestState().setSpeedLeftTrack(currentSpeedLeftTrack);
 		getLatestState().setSpeedRightTrack(currentSpeedRightTrack);		
 	}
 
 	public Robot getLatestState() {
 		return states.get(states.size() - 1);
 	}
 
 	public void reset() {
 		samples = new ArrayList<Sample>();
 		states = new ArrayList<Robot>();
 		if(latestMapFilename != null) {
 			loadMap(latestMapFilename);
 		} else {
 			grid = new Grid();
 		}
		addState(new Robot(new Vector2D(100.0f, 0), 0.5f));
 	}
 	
 	public void removeOldSamples(int preserveLength) {
 		samples = CollectionUtil.takeLast(samples, preserveLength);
 	}
 	
 	public void clearSamples() {
 		samples = new ArrayList<Sample>();		
 	}
 	
 	public List<Sample> getLastSamples(int numberOfSamples) {
 		return CollectionUtil.takeLast(samples, numberOfSamples);
 	}
 
 	public void setGridPosition(Vector2D position, boolean isBlocked) {
 		grid.setGridPosition(position, isBlocked);
 	}	
 	
 	public void setUserPosition(Vector2D position, boolean isBlocked) {
 		grid.setUserPosition(position, isBlocked);
 	}
 	
 	public void pushUserEditUndoLevel() {
 		grid.pushUserUndoLevel();
 	}
 	
 	public void popUserEditUndoLevel() {
 		grid.popUserUndoLevel();
 	}
 
 	public void redoUserEditUndoLevel() {
 		grid.redoUserUndoLevel();
 	}
 
 	public boolean isUserEditUndoable() {
 		return grid.isUserEditUndoable();
 	}
 
 	public boolean isUserEditRedoable() {
 		return grid.isUserEditRedoable();
 	}
 
 	public int getUserUndoLevels() {
 		return grid.getUserUndoLevels();
 	}
 
 	public int getUserRedoLevels() {
 		return grid.getUserRedoLevels();
 	}
 
 	public void saveMap(String fileName) {
 		try {
 			ImageIO.write(grid.getUserImage(), "PNG", new File(fileName));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadMap(String fileName) {
 		try {
 			BufferedImage mapImage = ImageIO.read(new File(fileName));
 			grid.pushUserUndoLevel();
 			grid.setUserImage(mapImage);
 			latestMapFilename = fileName;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void resetMap() {
 		grid.resetUserImage();
 	}
 
 	public Image getUserImage() {
 		return grid.getUserImage();
 	}
 
 	public Image getBlockedImage() {
 		return grid.getBlockedImage();
 	}
 	
 	public float traceRay(Vector2D from, float angle) {
 		return grid.traceRay(from, angle);
 	}
 
 	public float getWidth() {
 		return grid.getWidth();
 	}
 
 	public float getHeight() {
 		return grid.getHeight();
 	}
 
 	public void addSampleListener(SampleListener listener) {
 		this.sampleListeners.add(listener);
 	}
 	
 	public void removeSampleListener(SampleListener listener) {
 		this.sampleListeners.remove(listener);
 	}	
 
 	public void addRobotListener(RobotListener listener) {
 		this.robotListeners.add(listener);
 	}
 	
 	private void notifySampleListeners(Sample sample) {
 		for (SampleListener listener : sampleListeners) {
 			listener.sampleAdded(sample);
 		}
 	}
 	
 	private void notifyRobotListeners(Robot robot) {
 		for (RobotListener listener : robotListeners) {
 			listener.robotStateChanged(robot);
 		}
 	}
 
 	public boolean isClear(Vector2D position) {
 		return grid.isClear(position);
 	}
 }
