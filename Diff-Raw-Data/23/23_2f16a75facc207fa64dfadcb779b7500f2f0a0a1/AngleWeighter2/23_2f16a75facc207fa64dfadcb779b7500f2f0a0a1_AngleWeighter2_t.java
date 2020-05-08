 package huisken.projection;
 
 import ij.ImageJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.process.FloatProcessor;
 import ij.process.ImageProcessor;
 
 import javax.vecmath.Point3f;
 
 public class AngleWeighter2 implements FusionWeight {
 
 	public static final int X_AXIS = 0;
 	public static final int Y_AXIS = 1;
 	public static final int Z_AXIS = 2;
 
 	private final int aperture;
 	private final Point3f center;
 	private final int angle;
 	private final int axis;
 	private boolean negativeAxis;
 	public static final double overlap = 20;
	public static final double overlap2 = overlap / 2;
 
 	public AngleWeighter2(int axis, boolean negativeAxis, int angle, int aperture, Point3f center) {
 		this.axis = axis;
 		this.angle = angle;
 		this.aperture = aperture;
 		this.center = center;
 		this.negativeAxis = negativeAxis;
 	}
 
 	public double getAngle(double dx, double dy, double dz) {
 		double d1, d2;
 		switch(axis) {
 		case Z_AXIS: d1 =  dx; d2 =  dy; break;
 		case Y_AXIS: d1 =  dx; d2 = -dz; break;
 		case X_AXIS: d1 = -dz; d2 =  dy; break;
 		default: throw new IllegalArgumentException();
 		}
 		double a = 180.0 * Math.atan2(d2, d1) / Math.PI;
 		if(negativeAxis)
 			a = -a;
 		a -= angle;
 		if(a > 180)
 			return a - 360;
 		if(a < -180)
 			return a + 360;
 		return a;
 	}
 
 	public boolean inLowerOverlap(double angle) {
		return angle > -aperture/2.0 - overlap2 && angle < -aperture/2.0 + overlap2;
 	}
 
 	public boolean inUpperOverlap(double angle) {
		return angle > aperture/2.0 - overlap2 && angle < aperture/2.0 + overlap2;
 	}
 
 
 	/*
 	 *          Camera
 	 *            |
 	 *            |
 	 *
 	 *            x
 	 *         _________
 	 *      y /        /|
 	 *       /        / |
 	 *      /________/  |
 	 *      |        |  /
 	 *    z |        | /
 	 *      |________|/
 	 */
 	@Override
 	public float getWeight(float px, float py, float pz) {
 
 		double dx = px - center.x;
 		double dy = py - center.y;
 		double dz = pz - center.z;
 
 		double angle = getAngle(dx, dy, dz);
 
 		// inside
		if(angle > -aperture/2.0 + overlap2 && angle < aperture/2.0 - overlap2)
 			return 1f;
 
 		// outside
		if(angle < -aperture/2.0 - overlap2 || angle > aperture/2.0 + overlap2)
 			return 0f;
 
 
 		// within the overlap
		double eps = 0.0001;
		double k = 1 / overlap2 * Math.log(1.0 / eps - 1.0);
 		if(angle < 0) {
 			double t = angle + aperture/2.0;
			return (float)(1.0 / (1 + Math.exp(-k * t)));
 		} else {
 			double t = angle - aperture/2.0;
			return 1f - (float)(1.0 / (1 + Math.exp(-k * t)));
 		}
 	}
 
 	public static void main(String[] args) {
 		new ImageJ();
 		AngleWeighter2 aw = new AngleWeighter2(X_AXIS, false, 135, 90, new Point3f(50, 50, 50));
 		int w = 100, h = 100, d = 100;
 		ImageStack stack = new ImageStack(w, h);
 		for(int z = 0; z < d; z++) {
 			ImageProcessor p = new FloatProcessor(w, h);
 			for(int y = 0; y < h; y++) {
 				for(int x = 0; x < w; x++) {
 					p.setf(x, y, aw.getWeight(x, y, z));
 				}
 			}
 			stack.addSlice("", p);
 		}
 		new ImagePlus("aw", stack).show();
 	}
 }
