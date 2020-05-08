 package domobi.IOIO.heli;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.SystemClock;
 
 //Need Gyro for rotation, now just manually select rotation power...
 public class Autopilot {
 	private Helicopter heliModel;
 	private Orientation desiredOrientation;
 	public float pitchError;
 	public float yawError;
 	public float rotPower;
 	public float pitPower;
 	private long lastTime;
 	private long dt;
 	private FIR diffFilterPitch;
 	private FIR diffFilterYaw;
 
 	public float kPt = 0.3f, kIt = 0.001f, kDt = 2.5f;
 	// public float kPr = 0.3f, kIr = 0.0f, kDr = 0.0f;
	public float kPr = 0.01f, kIr = 0.0f, kDr = 0.5f;
 
 	public float uPt = 0, uIt = 0, uDt = 0;
 	public float uPr = 0, uIr = 0, uDr = 0;
 
 	public Autopilot() {
 		// Savitzky-Golay Derivative
 		float[] sg = { 3, 2, 1, 0, -1, -2, -3 };
 		heliModel = new Helicopter();
 		desiredOrientation = new Orientation();
 		diffFilterPitch = new FIR(sg);
 		diffFilterYaw = new FIR(sg);
 		lastTime = 0;
 	}
 
 	public void setOrientation(float pitch, float yaw, float mainPwr) {
 		desiredOrientation.pitch = pitch;
 		desiredOrientation.yawVel = yaw;
 		desiredOrientation.mainPwr = mainPwr;
 	}
 
 	public void setOrientation(JSONObject setpoint) throws JSONException {
 		double pitch = setpoint.getDouble("pitch");
 		double yaw = setpoint.getDouble("yaw");
 		double mainPwr = 0.5 * setpoint.getDouble("mainPwr");
 
 		desiredOrientation.pitch = (float) pitch;
 		desiredOrientation.yawVel = (float) yaw;
 		desiredOrientation.mainPwr = (float) mainPwr;
 	}
 
 	public Orientation getSetPoint() {
 		return desiredOrientation;
 	}
 
 	public Helicopter getNextState(Odometry odom) {
 		// getControl should be called at some rate.
 		// the values of odom should reflect this
 		// rate if there's a difference (i.e. use an
 		// anti-aliasing filter).
 		dt = SystemClock.elapsedRealtime() - lastTime;
 		if (dt > 1000) {
 			// starting up or something went wrong
 			dt = 0;
 			lastTime = SystemClock.elapsedRealtime();
 		} else {
 			lastTime = lastTime + dt;
 		}
 
 		Orientation error = desiredOrientation.getDifference(odom
 				.getOrientation());
 		pitchError = error.pitch;
 		yawError = error.yawVel;
 
 		heliModel.setMainPwr(desiredOrientation.mainPwr);
 		// Replace with Gyro
 		heliModel.setRotationPwr(desiredOrientation.getYaw());
 		if (heliModel.getMainPwr() > 0.4f) {
 			uPt = pitchError;
 
 			uIt = (uIt + pitchError * dt);
 
 			uDt = (diffFilterPitch.getNextOutput(pitchError) / dt);
 
 			pitPower = kPt * uPt + kIt * uIt + kDt * uDt;
 			heliModel.setTailPwr(pitPower);
 
 		} else if (heliModel.getMainPwr() > 0.2f) {
 
 			uPr = yawError;
 
 			uIr = (uIr + yawError * dt);
 
 			uDr = (diffFilterYaw.getNextOutput(yawError) / dt);
 
 			rotPower = rotPower + (kPr * uPr + kIr * uIr + kDr * uDr);
 			if (rotPower > 1) {
 				rotPower = 1;
 			}
 			if (rotPower < -1) {
 				rotPower = -1;
 			}
 			heliModel.setRotationPwr(rotPower);
 		} else {
 			heliModel.setTailPwr(0f);
 			// Negative values turn it clockwise
 		}
 
 		return heliModel;
 	}
 
 }
