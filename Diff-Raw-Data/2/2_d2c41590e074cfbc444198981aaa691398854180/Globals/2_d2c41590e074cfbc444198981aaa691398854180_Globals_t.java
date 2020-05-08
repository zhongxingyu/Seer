 package balle.misc;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 
 import balle.main.Config;
 import balle.memory.FolderReader;
 import balle.memory.PowersConfigFile;
 import balle.world.AngularVelocity;
 import balle.world.Orientation;
 import balle.world.Velocity;
 import balle.world.objects.Pitch;
 import balle.world.objects.Robot;
 
 public class Globals {
 
 	// ----------
 	// Constants.
 
     public static final float BALL_RADIUS = 0.02135f;
     public static final float ROBOT_WIDTH = 0.15f;
     public static final float ROBOT_LENGTH = 0.2f;
     public static final float ROBOT_TRACK_WIDTH = 0.155f; // Meters
     public static final float ROBOT_WHEEL_DIAMETER = 0.0816f; // Meters
 	
 	public static final Vec2 ROBOT_LEFT_WHEEL_POS = new Vec2(0,
 			-ROBOT_TRACK_WIDTH / 2);
 
 	public static final Vec2 ROBOT_RIGHT_WHEEL_POS = new Vec2(0,
 			ROBOT_TRACK_WIDTH / 2);
 
     public static final float ROBOT_MAX_KICK_DISTANCE = Globals.PITCH_WIDTH; // Meters
     // TODO
     // CHECK
     // THIS
 
     // For defining near corner, and near wall areas.
     public final static double DISTANCE_TO_WALL = 0.2;
     public final static double DISTANCE_TO_CORNER = 0.2;
 
     public static final float PITCH_WIDTH = 2.4384f; // Metres
     public static final float PITCH_HEIGHT = 1.2192f; // Metres
     public static final float GOAL_POSITION = 0.31f; // Metres
 
     public static final float ROBOT_POSSESS_DISTANCE = 0.025f;
 
     public static final float METERS_PER_PIXEL = PITCH_WIDTH / 605f;
 
 	public static final float VISION_COORD_NOISE_SD = 0.29f * METERS_PER_PIXEL; // in
                                                                                 // meters
 	public static final float VISION_ANGLE_NOISE_SD = 0.53f; // in
                                                              // degrees
     public static final float SIMULATED_VISON_FRAMERATE = 25f;
 	public static final long SIMULATED_VISON_DELAY = 450;
 
     public static final int MAXIMUM_MOTOR_SPEED = 900;
 
     // Camera Info
 	public static final float P1_CAMERA_HEIGHT = 2.285f; // 2.386f; // Meters
 	public static final float P0_CAMERA_HEIGHT = 2.37f; // 2.421f; // Meters
 
     public static final float ROBOT_HEIGHT = 0.19f; // Meters
 
     // TODO: SAULIUS this is just a temp fix for M3, change back to 50 for
     public static final double OVERSHOOT_ANGLE_EPSILON = 0; // Degrees
 
     public static final long BALL_POSITION_ESTIMATE_MAX_STEP = 1000; // ms
     public static final long BALL_POSITION_ESTIMATE_DRAW_STEP = 100; // ms
 
 	// static final
 	// Vec2
 	// ROBOT_LEFT_WHEEL_POS
 	// = new
 	// Vec2(-ROBOT_TRACK_WIDTH/2,
 	// 0);
 
 	public static final float MaxWheelAccel = 10f;// 0.019f; // m/s^2 good value
 													// around
 	// 0.005 ish
 	public static final float SlipWheelAccel = MaxWheelAccel * 0.6f;
 	public static final float MAX_ROBOT_LINEAR_ACCEL = 40f; // m/s^2
 	public static final float MAX_ROBOT_ANG_ACCEL = 700f; // r/s^2
 	public static final float MAX_MOTOR_POWER_ACCEL = 500f; // p/s^2
	public static final float ARBITRARY_BALL_VEL_SCALING = 1;
     public static final double VELOCITY_NOISE_THRESHOLD = 1e-8;
 
 	public static final FolderReader resFolder = new FolderReader("res");
 	public static final String configFolder = "initConfig.txt";
 
 
 	// --------
 	// Methods
 
 	public static float powerToVelocity(float p) {
 		Powers powerAbove = null;
 		Powers powerBelow = null;
 		float velo = 0;
 		if (p > MAXIMUM_MOTOR_SPEED) {
 			p = MAXIMUM_MOTOR_SPEED;
 
         }
         if (p < -MAXIMUM_MOTOR_SPEED) {
 			p = -MAXIMUM_MOTOR_SPEED;
 
         }
 		boolean isNegative = p < 0;
 		p = Math.abs(p);
 
 		for (int i = 1; i < powervelo.size(); i++) {
 			powerBelow = powervelo.get(i - 1);
 			powerAbove = powervelo.get(i);
 			if (powerBelow.getPower() <= p && powerAbove.getPower() >= p) {
                 break;
 			}
 		}
 
 		float m = powerAbove.getVelocity() - powerBelow.getVelocity();
 		m /= (powerAbove.getPower() - powerBelow.getPower());
 		velo = m * (p - powerBelow.getPower()) + powerBelow.getVelocity();
 		return isNegative ? -velo : velo;
 	}
 
     public static float velocityToPower(float v) {
 
         Powers powerAbove = null;
         Powers powerBelow = null;
         float power = 0;
 
         float maxVelocity = powerToVelocity(MAXIMUM_MOTOR_SPEED);
         if (v > maxVelocity) {
             v = maxVelocity;
 
         }
         if (v < -maxVelocity) {
             v = -maxVelocity;
 
         }
 		boolean isNegative = v < 0;
 		v = Math.abs(v);
 
 		for (int i = 1; i < powervelo.size(); i++) {
 			powerBelow = powervelo.get(i - 1);
 			powerAbove = powervelo.get(i);
 			if (powerBelow.getVelocity() <= v && powerAbove.getVelocity() >= v) {
 				break;
 			}
 		}
 
         float m = powerAbove.getPower() - powerBelow.getPower();
 		m /= (powerAbove.getVelocity() - powerBelow.getVelocity());
         power = m * (v - powerBelow.getVelocity()) + powerBelow.getPower();
 
 		return isNegative ? -power : power;
 
     };
 
     public static Pitch getPitch() {
         return new Pitch(0, PITCH_WIDTH, 0, PITCH_HEIGHT);
     }
     
 	@SuppressWarnings("unchecked")
 	public static <E> E[] cast(Object[] array) {
 		return (E[]) array;
     }
 
 	public static double[] getWheelVels(Robot r) {
 		return getWheelVels(r.getVelocity(), r.getAngularVelocity(),
 				r.getOrientation());
 	}
 
 	public static double[] getWheelVels(Velocity lv, AngularVelocity av,
 			Orientation forward) {
 		// this uses the angular and linear velocity of the robot to
 		// find the estimated powers to the wheels
 		// basicV is the velocity of each wheel assuming the robot is just
 		// spinning
 		double curentLeftV;
 		double curentRightV;
 		if (lv != null && av != null) {
 			double basicV = av.radians() * Globals.ROBOT_TRACK_WIDTH / 2;
 			int flipper = lv.dot(forward.getUnitCoord()) <= 0 ? -1 : 1;
 			curentLeftV = (flipper * lv.abs()) - basicV;
 			curentRightV = (flipper * lv.abs()) + basicV;
 
 		} else {
 			curentLeftV = curentRightV = 0;
 		}
 		return new double[] { curentLeftV, curentRightV };
 	}
 
 	// ----------------
 	// Variable Fields.
 
 
 	public static List<Powers> powervelo;
 	static {
 		Powers[] data = VtoPData.getData();
 		powervelo = new ArrayList<Powers>();
 		for (Powers p : data)
 			powervelo.add(p);
 	}
 
     
     
     
     // -------------------------------------
 	// Methods dealing with variable fields.
 
 	public static void initGlobals(Config config) {
 		setPowerVelo(config.get(Config.POWER_VELO_DATA));
 	}
 
 	public static void setPowerVelo(String filename) {
 		if (filename == null || filename.equals("[DEFAULT]"))
 			return;
 
 		try {
 			PowersConfigFile pcf = new PowersConfigFile(resFolder, filename);
 			powervelo = pcf.readArray();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 
 }
