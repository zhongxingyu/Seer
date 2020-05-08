 package edu.ncsu.uhp.escape.engine.utilities;
 
 import edu.ncsu.uhp.escape.engine.utilities.math.Point;
 
 public class EulerAngles implements IRotation {
 	private float yaw;
 	private float roll;
 	private float pitch;
 
 	public EulerAngles(float yaw, float pitch, float roll) {
 		this.yaw = yaw;
 		this.roll = roll;
 		this.pitch = pitch;
 	}
 
 	public EulerAngles rotate(IRotation rotation) {
 		float newYaw, newRoll, newPitch;
 		if (rotation instanceof EulerAngles) {
 			EulerAngles otherAngles = (EulerAngles) rotation;
 			newYaw = yaw + otherAngles.getYaw();
 			newRoll = roll + otherAngles.getRoll();
 			newPitch = pitch + otherAngles.getPitch();
 		} else {
 			newYaw = 0;
 			newRoll = 0;
 			newPitch = 0;
 		}
 		return new EulerAngles(newYaw, newPitch, newRoll);
 	}
 
 	public float[] toGlMatrix() {
 
 		float[] matrix = new float[16];
 		// http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToMatrix/index.htm
 		float sa = (float) Math.sin(pitch);
 		float ca = (float) Math.cos(pitch);
 		float sb = (float) Math.sin(yaw);
 		float cb = (float) Math.cos(yaw);
 		float sh = (float) Math.sin(roll);
 		float ch = (float) Math.cos(roll);
 		matrix[0] = ch * ca;
 		matrix[1] = -ch * sa * cb + sh * sb;
 		matrix[2] = ch * sa * sb + sh * cb;
 
 		matrix[3] = sa;
 		matrix[4] = ca * cb;
 		matrix[5] = -ca * sb;
 
 		matrix[6] = -sh * ca;
 		matrix[7] = sh * sa * cb + ch * sb;
 		matrix[8] = -sh * sa * sb + ch * cb;
 		return matrix;
 	}
 
 	public float getYaw() {
 		return yaw;
 	}
 
 	public float getRoll() {
 		return roll;
 	}
 
 	public float getPitch() {
 		return pitch;
 	}
 
 	public IRotation negative() {
		return new EulerAngles(yaw == 0 ? yaw : -yaw, pitch == 0 ? pitch
				: -pitch, roll == 0 ? roll : -roll);
 	}
 
 	public Point apply(Point point) {
		float newX = point.getX() * ((float) Math.cos(yaw));
		float newY = point.getY() * ((float) Math.sin(yaw));
		float newZ = point.getZ();

		newY = newY * (float) Math.cos(pitch);
		newZ = newZ * (float) Math.sin(pitch);

		newX = newX * (float) Math.cos(roll);
		newZ = newZ * (float) Math.sin(roll);
 		return new Point(newX, newY, newZ);
 	}
 }
