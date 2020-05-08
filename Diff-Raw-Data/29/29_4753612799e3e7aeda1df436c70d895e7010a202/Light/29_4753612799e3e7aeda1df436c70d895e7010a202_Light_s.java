 package initial3d.engine;
 
 import initial3d.Initial3D;
 import initial3d.linearmath.Matrix;
 import static initial3d.Initial3D.*;
 
 public abstract class Light {
 
 	protected ReferenceFrame rf = ReferenceFrame.SCENE_ROOT;
 
 	protected double[] lightpos = new double[4];
 	protected double[] spotdir = new double[4];
 	protected Color color_d = Color.WHITE, color_s = Color.WHITE, color_a = Color.BLACK;
 	protected float spot_cutoff = (float) Math.PI;
 	protected float[] coltemp = new float[3];
 	protected float atten_const = 1f;
 	protected float atten_lin = 0f;
 	protected float atten_quad = 0f;
 	protected float effect_radius = Float.MAX_VALUE;
 
 	private final double[][] xformtemp = Matrix.create(4, 4);
 
 	public Light(ReferenceFrame rf_) {
 		trackReferenceFrame(rf_);
 	}
 
 	public void trackReferenceFrame(ReferenceFrame rf_) {
 		rf = rf_ == null ? ReferenceFrame.SCENE_ROOT : rf_;
 	}
 
 	void loadTo(Initial3D i3d, long light) {
 		update();
 		i3d.matrixMode(MODEL);
 		i3d.pushMatrix();
 		i3d.loadIdentity();
 		// load the transforms back to the scene root (world space)
 		for (ReferenceFrame r = rf; r != ReferenceFrame.SCENE_ROOT; r = r.getParent()) {
 			r.getOrientation().toOrientationMatrix(xformtemp);
 			i3d.multMatrix(xformtemp);
 			r.getPosition().toTranslationMatrix(xformtemp);
 			i3d.multMatrix(xformtemp);
 		}
 
 		i3d.lightdv(light, POSITION, lightpos);
 		i3d.lightdv(light, SPOT_DIRECTION, spotdir);
 
 		i3d.popMatrix();
 
 		i3d.lightf(light, SPOT_CUTOFF, spot_cutoff);
 		i3d.lightfv(light, DIFFUSE, color_d.toArray(coltemp));
 		i3d.lightfv(light, SPECULAR, color_s.toArray(coltemp));
 		i3d.lightfv(light, AMBIENT, color_a.toArray(coltemp));
 		i3d.lightf(light, CONSTANT_ATTENUATION, atten_const);
 		i3d.lightf(light, LINEAR_ATTENUATION, atten_lin);
 		i3d.lightf(light, QUADRATIC_ATTENUATION, atten_quad);
 		i3d.lightf(light, EFFECT_RADIUS, effect_radius);
 
 	}
 
 	protected void update() {
 		// override as necessary
 	}
 	
 	/* package-private */
 	double getPriority(Vec3 campos) {
 		return 0;
 	}
 
 	public static class DirectionalLight extends Light {
 
 		private volatile Vec3 n = Vec3.j;
 
 		public DirectionalLight(ReferenceFrame rf_, Color c, Vec3 norm) {
 			super(rf_);
 			lightpos[3] = 0;
 			color_d = c;
 			color_s = c;
 			n = norm;
 		}
 		
 		public void setColor(Color c) {
 			color_d = c;
 			color_s = c;
 		}
 
 		public void setNormal(Vec3 n_) {
 			n = n_;
 		}
 
 		protected void update() {
 			// no synchro, just ensure consistency
 			Vec3 n = this.n;
 			lightpos[0] = n.x;
 			lightpos[1] = n.y;
 			lightpos[2] = n.z;
 		}
 
 	}
 
 	public static class SphericalPointLight extends Light {
 		
 		private volatile float radius = 1;
 
 		public SphericalPointLight(ReferenceFrame rf_, Color c, float radius_) {
 			super(rf_);
 			lightpos[3] = 1;
 			color_d = c;
 			color_s = c;
 			radius = radius_;
 			atten_const = 1f;
 		}
 		
 		public void setColor(Color c) {
 			color_d = c;
 			color_s = c;
 		}
 		
 		public void setRadius(float radius_) {
 			radius = radius_;
 		}
 		
 		protected void update() {
			atten_lin = 2f / radius;
			atten_quad = 1f / (radius * radius);
 		}
 
 	}
 
 	public static class SpotLight extends Light {
 
 		public SpotLight(ReferenceFrame rf_) {
 			super(rf_);
 			throw new AssertionError("SpotLight is not implemented.");
 		}
 
 	}
 
 	public static class SunLight extends DirectionalLight {
 
 		public SunLight(ReferenceFrame rf_, Color c, Vec3 norm) {
 			super(rf_, c, norm);
 			throw new AssertionError("SunLight is not implemented.");
 		}
 
 	}
 
 }
