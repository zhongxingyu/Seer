 package huisken.projection;
 
 import ij.process.ImageProcessor;
 
 import java.io.File;
 
 import javax.vecmath.Point3f;
 
 import meshtools.IndexedTriangleMesh;
 import vib.FastMatrix;
 
 public class MultiViewSphericalMaxProjection {
 
 	private final String outputdir;
 	private final int timepointStart, timepointInc, nTimepoints;
 	private final int angleStart, angleInc, nAngles;
 	private final int nPlanes;
 
 	private final SphericalMaxProjection[][] smp;
 	private final boolean saveSingleViews;
 	private final AngleWeighter aw;
 	private Iterator iterator;
 
 	public static final int LEFT  = Opener.LEFT;
 	public static final int RIGHT = Opener.RIGHT;
 
 	public MultiViewSphericalMaxProjection(String outputdir,
 			int timepointStart, int timepointInc, int nTimepoints,
 			int angleStart, int angleInc, int nAngles,
 			int w, int h, int d,
 			double pw, double ph, double pd,
 			Point3f[] centers, float radius,
 			boolean saveSingleViews) {
 
 		if(!outputdir.endsWith(File.separator))
 			outputdir += File.separator;
 
 		this.outputdir = outputdir;
 		this.timepointStart = timepointStart;
 		this.timepointInc = timepointInc;
 		this.nTimepoints = nTimepoints;
 		this.angleStart = angleStart;
 		this.angleInc = angleInc;
 		this.nAngles = nAngles;
 		this.nPlanes = d;
 		this.saveSingleViews = saveSingleViews;
 
 
 		// calculate sphere transformations for each angle
 		FastMatrix[] transforms = new FastMatrix[nAngles];
 		for(int a = 1; a < nAngles; a++) {
 			double angle = angleStart + angleInc * a;
 			angle = angle * Math.PI / 180.0;
 			transforms[a] = FastMatrix.translate(
 						centers[a].x - centers[0].x,
 						centers[a].y - centers[0].y,
 						centers[a].z - centers[0].z)
 					.times(rotateY(-angle, centers[0]));
 		}
 
 
 		// initialize the maximum projections
 		smp = initSphericalMaximumProjection(
 				transforms,
 				centers[0], radius,
 				angleStart, angleInc, nAngles,
 				w, h, d, pw, ph, pd);
 
 		// save the sphere geometry
 		String spherepath = new File(outputdir, "Sphere.obj").getAbsolutePath();
 		try {
 			smp[0][0].saveSphere(spherepath);
 		} catch(Exception e) {
 			throw new RuntimeException("Cannot save sphere: " + spherepath, e);
 		}
 
 		aw = new AngleWeighter(nAngles);
 		iterator = new Iterator();
 	}
 
 	class Iterator implements java.util.Iterator<Iterator> {
 		public int timepoint, angle, angleIndex, plane;
 
 		public Iterator() {
 			reset();
 		}
 
 		public void reset() {
 			angle = angleStart;
 			timepoint = timepointStart;
 			plane = -1;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return (plane + 1) < nPlanes ||
 				(angle + angleInc) < (angleStart + angleInc * nAngles) ||
 				(timepoint + timepointInc) < (timepointStart + timepointInc * nTimepoints);
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Iterator next() {
 			plane ++;
 			if(plane >= nPlanes) {
 				plane = 0;
 				angle += angleInc;
 				angleIndex++;
 				if(angle >= (angleStart + angleInc * nAngles)) {
 					angleIndex = 0;
 					angle = angleStart;
 					timepoint += timepointInc;
 					if(timepoint >= (timepointStart + timepointInc * nTimepoints))
 						return null;
 				}
 			}
 			return this;
 		}
 	}
 
 	public void process(ImageProcessor left, ImageProcessor right) {
 		iterator = iterator.next();
 		if(iterator == null)
 			throw new RuntimeException("Finished");
 
 		int a = iterator.angleIndex;
 		int angle = iterator.angle;
 		int tp = iterator.timepoint;
 		int p = iterator.plane;
 
 		// Start of stack
 		if(p == 0) {
 			smp[a][LEFT].startProjectStack();
 			smp[a][RIGHT].startProjectStack();
 		}
		// Not end of stack
		if(p < nPlanes) {
			smp[a][LEFT].projectPlane(p, left);
			smp[a][RIGHT].projectPlane(p, right);
 			return;
		}
 
 		// End of stack, further process the projections
 		smp[a][LEFT].finishProjectStack();
 		smp[a][RIGHT].finishProjectStack();
 
 		// sum up left and right illumination
 		smp[a][LEFT].addMaxima(smp[a][RIGHT].getMaxima());
 
 		// if specified, save the single views in separate folders
 		if(saveSingleViews) {
 			String filename = String.format("tp%04d.tif", tp, angle);
 			String subfolder = String.format("angle%3d/", angle);
 			String vpath = new File(outputdir + subfolder, filename + ".vertices").getAbsolutePath();
 			File subf = new File(outputdir, subfolder);
 			if(!subf.exists()) {
 				subf.mkdir();
 				try {
 					smp[0][0].saveSphere(outputdir + subfolder + "Sphere.obj");
 				} catch(Exception e) {
 					throw new RuntimeException("Cannot save sphere: " + outputdir + subfolder + "Sphere.obj", e);
 				}
 			}
 
 			try {
 				smp[a][0].saveMaxima(vpath);
 			} catch(Exception e) {
 				throw new RuntimeException("Cannot save " + vpath);
 			}
 		}
 
 		// scale the resulting maxima according to angle
 		smp[a][0].scaleMaxima(aw);
 
 		// sum them all up
 		if(a > 0) {
 			smp[0][0].addMaxima(smp[a][0].getMaxima());
 		}
 
 		// if it's the last angle, save the result
 		if(a == nAngles - 1) {
 			String filename = String.format("tp%04d.tif", tp);
 			String vpath = new File(outputdir, filename + ".vertices").getAbsolutePath();
 			try {
 				smp[0][0].saveMaxima(vpath);
 			} catch(Exception e) {
 				throw new RuntimeException("Cannot save " + vpath);
 			}
 		}
 	}
 
 	private static FastMatrix rotateY(double rad, Point3f center) {
 		FastMatrix rot = FastMatrix.rotate(rad, 1);
 		rot.apply(center.x, center.y, center.z);
 		return FastMatrix.translate(center.x - rot.x, center.y - rot.y, center.z - rot.z).times(rot);
 	}
 
 	/**
 	 * @param transform Array with one transformation for each angle; the first entry
 	 *                  in this array is ignored (it is assumed to be the identity matrix.
 	 */
 	private SphericalMaxProjection[][] initSphericalMaximumProjection(
 			FastMatrix[] transform,
 			Point3f center, float radius,
 			int angleStart, int angleInc, int nAngles,
 			int w, int h, int d,
 			double pw, double ph, double pd) {
 
 		if(transform.length != nAngles)
 			throw new IllegalArgumentException("Need one transformation for each angle");
 
 		int subd = (int)Math.round(radius / (Math.min(pw, Math.min(ph, pd))));
 		subd /= 4;
 
 		SphericalMaxProjection[][] smp = new SphericalMaxProjection[nAngles][2];
 
 		// 0 degree, left illumination
 		smp[0][0] = new SphericalMaxProjection(center, radius, subd);
 		smp[0][0].prepareForProjection(w, h, d, pw, ph, pd, new SimpleLeftWeighter(center.x));
 
 		IndexedTriangleMesh sphere = smp[0][0].getSphere();
 
 		// 0 degree, right handed illumination
 		smp[0][1] = new SphericalMaxProjection(sphere, center, radius);
 		smp[0][1].prepareForProjection(w, h, d, pw, ph, pd, new SimpleRightWeighter(center.x));
 
 		// all other angles
 		for(int a = 1; a < nAngles; a++) {
 			// left illumination
 			smp[a][0] = new SphericalMaxProjection(sphere, center, radius, transform[a]);
 			smp[a][0].prepareForProjection(w, h, d, pw, ph, pd, new SimpleLeftWeighter(center.x));
 
 			// right illumination
 			smp[a][1] = new SphericalMaxProjection(sphere, center, radius, transform[a]);
 			smp[a][1].prepareForProjection(w, h, d, pw, ph, pd, new SimpleRightWeighter(center.x));
 		}
 
 		return smp;
 	}
 }
