 package ca.etsmtl.capra.purifier.implementations.PositionManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.AffineTransform;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JToolBar;
 import javax.swing.filechooser.FileFilter;
 
 import ca.etsmtl.capra.datas.OrientedPosition;
 import ca.etsmtl.capra.datas.Position;
 import ca.etsmtl.capra.datas.RobotPosition;
 import ca.etsmtl.capra.digitizer.datas.IMUData;
 import ca.etsmtl.capra.digitizer.services.PositionSimulator;
 import ca.etsmtl.capra.purifier.services.PurifierServiceAbstract;
 import ca.etsmtl.capra.purifier.services.ReportService;
 import ca.etsmtl.capra.purifier.services.RobotPositionService;
 import ca.etsmtl.capra.util.Math2;
 import ca.etsmtl.capra.util.Parsing;
 
 public abstract class AbstractPositionManager extends PurifierServiceAbstract
 		implements RobotPositionService, ActionListener {
 	protected static final long REFRESH_TIME = 60;
 
 	/**
 	 * Estimated frequency of the position stored (hz)
 	 */
 	private static final int ESTIMATED_FREQUENCY = 20;
 	/**
 	 * About 2 hours of buffer
 	 */
 	private static final int INITIAL_POSITION_BUFFER_SIZE = 3600 * ESTIMATED_FREQUENCY;
 	private static final String EXT_POS = ".positions";
 	protected volatile RobotPosition lastRobotPosition = new RobotPosition(0,
 			0, 0, 0, 0, 0);
 	protected final ArrayList<RobotPosition> positions = new ArrayList<RobotPosition>(
 			INITIAL_POSITION_BUFFER_SIZE);
 	protected volatile long firstTimestamp = lastRobotPosition.getTimeStamp();
 	protected volatile long lastTimestamp = lastRobotPosition.getTimeStamp();
 	protected PositionSimulator positionSimulator;
 	protected ReportService reportService;
 	private final OrientedPosition positionDiff = new OrientedPosition();
 	private final JButton savePositions = new JButton("Save Positions");
 	private final JButton loadPositions = new JButton("Load Positions");
 
 	protected IMUData initialImuData = null;
 	protected List<CallBack> callBacks = new Vector<CallBack>();
 
 	public AbstractPositionManager(long minDelay, long maxDelay) {
 		super(minDelay, maxDelay);
 	}
 
 	public RobotPosition getRobotPosition() {
 		return lastRobotPosition;
 	}
 
 	public RobotPosition getRobotPosition(long time) {
 		RobotPosition afterPos;
 		RobotPosition beforePos;
 		synchronized (positions) {
 			// Check for not enough positions
 			if (positions.size() <= 1)
 				return lastRobotPosition;
 			long firstTime = firstTimestamp;
 			long lastTime = lastTimestamp;
 			// Check equal and under
 			if (time <= firstTime)
 				return positions.get(0);
 			// Check equal and over
 			if (time >= lastTime)
 				return lastRobotPosition;
 			// Linear approximation of index
 			float timeRatio = (float) (time - firstTime)
 					/ (float) (lastTime - firstTime);
 			float timeIndex = interpolate(timeRatio, 0, positions.size() - 1);
 			int index = Math.round(timeIndex);
 			// Check for negative index
 			if (index < 0)
 				index = 0;
 			// Check for last or over index
 			if (index >= positions.size() - 1)
 				index = positions.size() - 2;
 			// Find the good lower position assuming the upper is the next
 			int beforeIndex = index;
 			{
 				long beforeTime = positions.get(beforeIndex).getTimeStamp();
 				long afterTime = positions.get(beforeIndex + 1).getTimeStamp();
 				// Go after that time
 				while (beforeTime >= time || afterTime <= time) {
 					// Lucky shots
 					if (beforeTime == time)
 						return positions.get(beforeIndex);
 					if (afterTime == time)
 						return positions.get(beforeIndex + 1);
 					if (beforeTime > time)
 						beforeIndex--;
 					else
 						beforeIndex++;
 					beforeTime = positions.get(beforeIndex).getTimeStamp();
 					afterTime = positions.get(beforeIndex + 1).getTimeStamp();
 				}
 			}
 			beforePos = positions.get(beforeIndex);
 			afterPos = positions.get(beforeIndex + 1);
 		}
 		// Interpolate linearly between the 2 positions based on time (assumes
 		// constant speed)
 		long afterTime = afterPos.getTimeStamp();
 		long beforeTime = beforePos.getTimeStamp();
 		float ratio = (float) (time - beforeTime)
 				/ (float) (afterTime - beforeTime);
 		// Interpolate each parameter lineary
 		float x = interpolate(ratio, beforePos.getX(), afterPos.getX());
 		float y = interpolate(ratio, beforePos.getY(), afterPos.getY());
 		float theta = interpolateTheta(ratio, beforePos.getTheta(),
 				afterPos.getTheta());
 		float speed = interpolate(ratio, beforePos.getSpeed(),
 				afterPos.getSpeed());
 		float omega = interpolate(ratio, beforePos.getOmega(),
 				afterPos.getOmega());
 		float acceleration = interpolate(ratio, beforePos.getAcceleration(),
 				afterPos.getAcceleration());
 		RobotPosition robotPosition = new RobotPosition(x, y, theta, speed,
 				omega, acceleration);
 		robotPosition.setTimeStamp(time);
 		return robotPosition;
 	}
 
 	private float interpolate(float ratio, float start, float end) {
 		return start + ratio * (end - start);
 	}
 	
 	private float interpolateTheta(float ratio, float start, float end )
 	{
 		float diff = end - start;
		// TODO CHANGE ME PLZ K THX
		if ( diff > 1 )
			diff = end - ( start + 2 * Math2.PI );
		else if ( diff < - 1 )
			diff = (end + 2 * Math2.PI ) - start;
 		
		/*
 		if ( Math2.cos(start) > 0 && Math2.cos(end) > 0 )
 			if ((Math2.sin(end) > 0) != (Math2.sin(start) > 0 ) )
 				if ( Math2.sin(end) > 0 )
 					diff = (end - start) + 2 * Math2.PI ;
 				else
 					diff = (end - start) - 2 * Math2.PI;
		*/
 		return start + ratio * diff;
 	}
 
 	public void reset() {
 		synchronized (positions) {
 			positions.clear();
 			lastRobotPosition = new RobotPosition(0, 0, 0, 0, 0, 0);
 			firstTimestamp = lastRobotPosition.getTimeStamp();
 			updatePosition(lastRobotPosition);
 		}
 	}
 
 	public AffineTransform getRelToAbsTransform(long time) {
 		RobotPosition dataPos = getRobotPosition(time);
 		// Create the transform that convert from relative to absolute position
 		AffineTransform rel2abs = new AffineTransform();
 		rel2abs.setToTranslation(dataPos.getX(), dataPos.getY());
 		rel2abs.rotate(dataPos.getTheta());
 		return rel2abs;
 	}
 
 	@Override
 	public Position transposeRelToAbsPosition(long time, Position position) {
 		AffineTransform rel2abs = getRelToAbsTransform(time);
 		float xyRel[] = new float[2];
 		float xyAbs[] = new float[2];
 		xyRel[0] = position.getX();
 		xyRel[1] = position.getY();
 		rel2abs.transform(xyRel, 0, xyAbs, 0, 1);
 		Position transposed = new Position(xyAbs[0], xyAbs[1]);
 		return transposed;
 	}
 
 	@Override
 	public List<Position> transposeRelToAbsPositions(long time,
 			List<Position> positions_) {
 		List<Position> transposedPositions = new ArrayList<Position>(
 				positions_.size());
 		AffineTransform rel2abs = getRelToAbsTransform(time);
 		float xyRel[] = new float[2];
 		float xyAbs[] = new float[2];
 		for (Position position : positions_) {
 			xyRel[0] = position.getX();
 			xyRel[1] = position.getY();
 			rel2abs.transform(xyRel, 0, xyAbs, 0, 1);
 			Position transposed = new Position(xyAbs[0], xyAbs[1]);
 			transposedPositions.add(transposed);
 		}
 		return transposedPositions;
 	}
 
 	final protected void updatePosition(RobotPosition robotPosition) {
 		synchronized (positions) {
 			lastRobotPosition = new RobotPosition(robotPosition);
 			lastTimestamp = lastRobotPosition.getTimeStamp();
 			positions.add(lastRobotPosition);
 			// System.out.println("lastRobotPosition x: " +
 			// lastRobotPosition.getX() + ", y: " + lastRobotPosition.getY());
 		}
 		// RobotPosition rp = robotPosition;
 		// positionSimulator.setSimulatedPosition(rp, rp.theta, rp.speed,
 		// rp.omega, rp.acceleration);
 	}
 
 	public void resolveSummons() {
 		positionSimulator = getDigitizerServiceFor(PositionSimulator.class);
 		reportService = getPurifierServiceFor(ReportService.class);
 	}
 
 	public void resolveSummonsDone() {
 		updatePosition(lastRobotPosition);
 		if (reportService != null)
 			reportService.addToolbar(createToolBar());
 	}
 
 	private JToolBar createToolBar() {
 		JToolBar bar = new JToolBar();
 		bar.add(savePositions);
 		savePositions.addActionListener(this);
 		bar.add(loadPositions);
 		loadPositions.addActionListener(this);
 		return bar;
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		Object source = ae.getSource();
 		if (source == savePositions) {
 			JFileChooser fc = new JFileChooser(new File("."));
 			fc.setFileFilter(new PosFileFilter());
 			int result = fc.showSaveDialog(null);
 			if (result == JFileChooser.APPROVE_OPTION) {
 				File selectedFile = fc.getSelectedFile();
 				if (!selectedFile.getAbsolutePath().endsWith(EXT_POS)) {
 					selectedFile = new File(selectedFile.getAbsolutePath()
 							+ EXT_POS);
 				}
 				try {
 					if (selectedFile.exists())
 						selectedFile.delete();
 					selectedFile.createNewFile();
 				} catch (IOException e) {
 					log().warn("Can't create file " + selectedFile, e);
 				}
 				synchronized (positions) {
 					Parsing.saveRobotPosition(selectedFile, positions, this);
 				}
 			}
 		} else if (source == loadPositions) {
 			JFileChooser fc = new JFileChooser(new File("."));
 			fc.setFileFilter(new PosFileFilter());
 			int result = fc.showDialog(null, "Load");
 			if (result == JFileChooser.APPROVE_OPTION) {
 				File selectedFile = fc.getSelectedFile();
 				synchronized (positions) {
 					positions.clear();
 					Parsing.loadRobotPosition(selectedFile, positions, this);
 					if (!positions.isEmpty()) {
 						firstTimestamp = positions.get(0).getTimeStamp();
 						lastTimestamp = positions.get(positions.size() - 1)
 								.getTimeStamp();
 					}
 				}
 			}
 		}
 	}
 
 	public void moveRobot(Position diff) {
 		if (diff instanceof OrientedPosition) {
 			moveRobot((OrientedPosition) diff);
 		} else {
 			positionDiff.add(diff);
 			lastRobotPosition.add(diff);
 			// System.out.println("diff x: " + diff.getX() + ", y: " +
 			// diff.getY());
 		}
 	}
 
 	public void moveRobot(OrientedPosition diff) {
 		positionDiff.add(diff);
 		synchronized (positions) {
 			for (RobotPosition rp : positions) {
 				rp.add(diff);
 			}
 			lastRobotPosition.add(diff);
 		}
 	}
 
 	class PosFileFilter extends FileFilter {
 		@Override
 		public boolean accept(File f) {
 			if (f.isDirectory())
 				return true;
 			return f.getAbsolutePath().endsWith(EXT_POS);
 		}
 
 		@Override
 		public String getDescription() {
 			return "Position File (*.positions)";
 		}
 	}
 
 	public float getAverageOrientation(float beenTime, float deltaTime) {
 		ArrayList<RobotPosition> pos = new ArrayList<RobotPosition>(
 				(int) (beenTime / deltaTime + 1));
 		RobotPosition ourPos = getRobotPosition();
 		long now = ourPos.getTimeStamp();
 		long beenTimeMili = (long) (beenTime * 1000);
 		long deltaTimeMili = (long) (deltaTime * 1000);
 		for (long time = now - beenTimeMili; time < now; time += deltaTimeMili) {
 			RobotPosition robotPosition = getRobotPosition(time);
 			pos.add(robotPosition);
 		}
 		pos.add(ourPos);
 		//
 		float x = 0;
 		float y = 0;
 		for (int index = 1; index < pos.size(); index++) {
 			Position i = pos.get(index - 1);
 			Position f = pos.get(index);
 			Position diff = Position.substract(f, i);
 			x += diff.getX();
 			y += diff.getY();
 		}
 		x /= pos.size() - 1;
 		y /= pos.size() - 1;
 		return Math2.atan2(y, x);
 	}
 
 	public float getAverageOrientation(float beenDistance, float deltaTime,
 			boolean fromCurrentPosition) {
 		ArrayList<RobotPosition> pos = new ArrayList<RobotPosition>();
 		RobotPosition ourPos = getRobotPosition();
 		long now = ourPos.getTimeStamp();
 		long deltaTimeMili = (long) (deltaTime * 1000);
 		pos.add(ourPos);
 		float distanceSq = 0;
 		float beenDistanceSq = beenDistance * beenDistance;
 		// Filled from now to past
 		for (long time = now; distanceSq < beenDistanceSq; time -= deltaTimeMili) {
 			RobotPosition robotPosition = getRobotPosition(time);
 			distanceSq = robotPosition.distanceSq(ourPos);
 			pos.add(robotPosition);
 			if (robotPosition.getTimeStamp() == firstTimestamp)
 				break;
 		}
 		// Flip to list so it will be chronological
 		Collections.reverse(pos);
 		//
 		float x = 0;
 		float y = 0;
 		for (int index = 1; index < pos.size(); index++) {
 			Position i = pos.get(index - 1);
 			Position f;
 			if (fromCurrentPosition)
 				f = ourPos;
 			else
 				f = pos.get(index);
 			Position diff = Position.substract(f, i);
 			x += diff.getX();
 			y += diff.getY();
 		}
 		x /= pos.size() - 1;
 		y /= pos.size() - 1;
 		return Math2.atan2(y, x);
 	}
 
 	protected void notifyCallback() {
 		for (CallBack c : callBacks)
 			c.gotNewData(this);
 	}
 
 	@Override
 	public void registerCallBack(CallBack cb) {
 		callBacks.add(cb);
 	}
 }
