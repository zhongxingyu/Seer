 package ca.etsmtl.capra.digitizer.implementations;
 
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Path2D;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import ca.etsmtl.capra.archie.services.ResolveService;
 import ca.etsmtl.capra.datas.OrientedPosition;
 import ca.etsmtl.capra.datas.Position;
 import ca.etsmtl.capra.datas.RobotPosition;
 import ca.etsmtl.capra.digitizer.services.DigitizerServiceAbstract;
 import ca.etsmtl.capra.digitizer.services.RobotService;
 import ca.etsmtl.capra.util.Math2;
 
 public class Minotaurus extends DigitizerServiceAbstract implements
 		RobotService {
 	private static RobotService theRobot = null;
 	protected static RangeFinder theRangeFinder = null;
 	protected static Camera theCamera = null;
 	protected static Gps theGps = null;
 
 	public Minotaurus() {
 		theRobot = this;
 		mowerMounted = false;
 		// All in meters
 		nose_Length = 0.81f;
 		butt_Length = 0.25f;
 		length = nose_Length + butt_Length;
 		width = 0.77f;
 		half_Width = width / 2;
 		turning_Radius = nose_Length;
 		height = 0.51f;
 		/**
 		 * Mesure en m d'un pulse sur les encodeurs
 		 */
 		enc_Pulse_To_Distance = 0.000192f; // pas update pour mino
 		robot_Size_From_Center_Angular_Resolution = Math2.toRadians(1.0f); // pas
 																			// update
 																			// pour
 																			// mino
 		// Compute robot size from center
 		unmounted_Area = new Area(new Rectangle2D.Float(-butt_Length,
 				-half_Width, length, width));
 		unmounted_Robot_Size_From_Center_Mounted = computeRobotSizesFromCenter(
 				unmounted_Area, new Point2D.Float());
 	}
 
 	public static RobotService getRobot() {
 		if (theRobot == null) {
 			theRobot = new Minotaurus();
 		}
 		return theRobot;
 	}
 
 	public Camera getCamera() {
 		if (theCamera == null) {
 			theCamera = new Camera();
 		}
 		return theCamera;
 	}
 
 	public final Gps getGps() {
 		if (theGps == null) {
 			theGps = new Gps();
 		}
 		return theGps;
 	}
 
 	public final RangeFinder getRangeFinder() {
 		if (theRangeFinder == null) {
 			theRangeFinder = new RangeFinder();
 		}
 		return theRangeFinder;
 	}
 
 	volatile boolean mowerMounted;
 	private float nose_Length;
 	private float butt_Length;
 	private float length;
 	private float width;
 	private float half_Width;
 	private float turning_Radius;
 	private float height;
 	private float enc_Pulse_To_Distance;
 	private float robot_Size_From_Center_Angular_Resolution;
 	private Area mounted_Area;
 	private Area unmounted_Area;
 	private float[] unmounted_Robot_Size_From_Center_Mounted;
	
 	@ResolveService
 	private ca.etsmtl.capra.digitizer.sensors.RangeFinder rangeFinderService;
 
 	public class RangeFinder implements RobotService.RangeFinder {
 		private Position position;
 		private Area sight;
 		private float max_Sight;
 
 		protected RangeFinder() {
 			theRangeFinder = this;
 			position = new Position(0.68f, 0f, 0.32f);
 			max_Sight = 8f;
 			// Create shape representing the sight of the range finder
 			int width = -270 + rangeFinderService.degreesToHide();
 			sight = new Area(new Arc2D.Float(-max_Sight, -max_Sight,
 					2 * max_Sight, 2 * max_Sight, Math.abs(width / 2), width,
 					Arc2D.PIE));
 			System.out.println(rangeFinderService.degreesToHide());
 
 		}
 
 		public Area getSight(float x, float y, float theta, float scale) {
 			return getArea(x, position.getX(), y, position.getY(), theta, 0,
 					scale, sight);
 		}
 
 		public Area getSight(OrientedPosition pos) {
 			return getArea(pos.getX(), position.getX(), pos.getY(),
 					position.getY(), pos.getTheta(), 0, 1.0f, sight);
 		}
 
 		public Position getPosition() {
 			return position;
 		}
 
 		public Area getSight() {
 			return sight;
 		}
 
 		public float getMaxSight() {
 			return max_Sight;
 		}
 	}
 
 	public class Camera implements RobotService.Camera {
 		private Position position;
 		private Area sight;
 		private Area fade_Sight;
 
 		protected Camera() {
 			theCamera = this;
 			position = new Position(0.1f, 0f, 1.4f);
 			// Calibration  40 degree
 			Path2D.Float path = new Path2D.Float();
 			// Rebencher les valeurs pour 35deg.cal
 			path.moveTo(0f, 1f);
 			path.lineTo(3.5f, 1.5f);
 			path.lineTo(3.5f, -1.5f);
 			path.lineTo(0f, -1f);
 			path.closePath();
 			fade_Sight = new Area(path);
 			// Calibration  40 degree
 			path = new Path2D.Float();
 			// Rebencher les valeurs pour 35deg.cal
 			path.moveTo(0.7f, 0.7f);
 			path.lineTo(2.9f, 1.4f);
 			path.lineTo(2.9f, -1.4f);
 			path.lineTo(0.7f, -0.7f);
 			path.closePath();
 			sight = new Area(path);
 		}
 
 		public Area getSight(float x, float y, float theta, float scale) {
 			return getArea(x, position.getX(), y, position.getY(), theta, 0,
 					scale, sight);
 		}
 
 		public Area getSight(OrientedPosition pos) {
 			return getArea(pos.getX(), position.getX(), pos.getY(),
 					position.getY(), pos.getTheta(), 0, 1.0f, sight);
 		}
 
 		public Area getFadeSight(OrientedPosition pos) {
 			return getArea(pos.getX(), position.getX(), pos.getY(),
 					position.getY(), pos.getTheta(), 0, 1.0f, fade_Sight);
 		}
 
 		public Position getPosition() {
 			return position;
 		}
 	}
 
 	public class Gps implements RobotService.Gps {
 		private Position position;
 
 		protected Gps() {
 			theGps = this;
 			position = Position.substract(new RS3().getRangeFinder()
 					.getPosition(), new Position(0.1f, 0f));
 		}
 
 		public Position getPosition() {
 			return position;
 		}
 	}
 
 	public Area getArea(float x, float y, float theta) {
 		return getArea(x, y, theta, 1);
 	}
 
 	public float[] computeRobotSizesFromCenter(Area area, Point2D.Float center) {
 		Rectangle2D.Float line = new Rectangle2D.Float(center.x, center.y,
 				length, length / 1000f);
 		Area lineArea = new Area(line);
 		float[] coords = new float[10];
 		int i = 0;
 		float[] robotSizeFromCenter = new float[361];
 		for (double theta = 0; theta < Math.PI * 2; theta += robot_Size_From_Center_Angular_Resolution) {
 			AffineTransform at = new AffineTransform();
 			at.setToRotation(theta, center.x, center.y);
 			Area diffArea = lineArea.createTransformedArea(at);
 			diffArea.subtract(area);
 			at.setToRotation(-theta, center.x, center.y);
 			Area diffArea2 = diffArea.createTransformedArea(at);
 			PathIterator pathIterator = diffArea2.getPathIterator(null);
 			robotSizeFromCenter[i] = Integer.MAX_VALUE;
 			while (!pathIterator.isDone()) {
 				pathIterator.currentSegment(coords);
 				robotSizeFromCenter[i] = Math.min(robotSizeFromCenter[i],
 						coords[0]);
 				pathIterator.next();
 			}
 			i++;
 		}
 		return robotSizeFromCenter;
 	}
 
 	@Override
 	public Area getArea(OrientedPosition oriPos) {
 		return getArea(oriPos, 1.0f);
 	}
 
 	@Override
 	public Area getArea(OrientedPosition oriPos, float scale) {
 		return getArea(oriPos.getX(), oriPos.getY(), oriPos.getTheta(), scale);
 	}
 
 	@Override
 	public Area getArea(float x, float y, float theta, float scale) {
 		return getArea(x, y, theta, scale, getArea());
 	}
 
 	@Override
 	public Area getArea(float x, float y, float theta, float scale, Area area) {
 		return getArea(x, 0, y, 0, theta, 0, scale, area);
 	}
 
 	@Override
 	public Area getArea(float x, double dx, float y, double dy, float theta,
 			double dtheta, float scale, Area area) {
 		AffineTransform t = new AffineTransform();
 		t.setToTranslation(x, y);
 		t.rotate(theta);
 		t.translate(dx, dy);
 		t.rotate(dtheta);
 		t.scale(scale, scale);
 		return area.createTransformedArea(t);
 	}
 
 	@Override
 	public Area getArea(RobotPosition robotPosition) {
 		return getArea(robotPosition, 1f);
 	}
 
 	@Override
 	public Area getArea(RobotPosition robotPosition, float scale) {
 		return getArea(robotPosition.getX(), robotPosition.getY(),
 				robotPosition.getTheta(), scale);
 	}
 
 	public float getRobotSizeAt(float theta) {
 		int index = getIndex(theta);
 		return unmounted_Robot_Size_From_Center_Mounted[index];
 	}
 
 	public int getIndex(float theta) {
 		return Math.round(Math2.limitTheta(theta)
 				/ robot_Size_From_Center_Angular_Resolution);
 	}
 
 	public Area getArea() {
 		return unmounted_Area;
 	}
 
 	public void setMowerMounted(boolean mowerMounted) {
 		this.mowerMounted = mowerMounted;
 	}
 
 	public float getNoseLength() {
 		return this.nose_Length;
 	}
 
 	public float getButtLength() {
 		return this.butt_Length;
 	}
 
 	public float getLength() {
 		return this.length;
 	}
 
 	public float getWidth() {
 		return this.width;
 	}
 
 	// 2;
 	public float getHalfWidth() {
 		return this.half_Width;
 	}
 
 	public float getTurningRadius() {
 		return this.turning_Radius;
 	}
 
 	public float getHeight() {
 		return this.height;
 	}
 
 	// Mesure en m d'un pulse sur les encodeurs
 	public float getEncPulseToDistance() {
 		return this.enc_Pulse_To_Distance;
 	}
 
 	public float getRobotSizeFromCenterAngularResolution() {
 		return this.robot_Size_From_Center_Angular_Resolution;
 	}
 
 	// Compute robot size from center
 	public Area getMountedArea() {
 		return this.mounted_Area;
 	}
 
 	public Area getUnmountedArea() {
 		return this.unmounted_Area;
 	}
 
 	public float[] getUnmountedRobotSizeFromCenterMounted() {
 		return this.unmounted_Robot_Size_From_Center_Mounted;
 	}
 
 	public void resolveSummons() {
 	}
 
 	public void resolveSummonsDone() {
 	}
 
 	@Override
 	public void reset() {
 		System.out.println("Herbinator.reset");
 	}
 }
