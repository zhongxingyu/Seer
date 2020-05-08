 package suite.rt;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.Collection;
 
 import suite.math.Vector;
 import suite.util.LogUtil;
 
 public class RayTracer {
 
 	public static final float negligibleAdvance = 0.0001f;
 
	private Vector ambient = new Vector(0.8f, 0.8f, 0.8f);
 	private float refractiveIndexRatio = 1.1f;
 
 	private Collection<LightSource> lightSources;
 	private RayTraceObject scene;
 
 	public interface RayTraceObject {
 
 		/**
 		 * Calculates hit point with a ray. Assumes direction is normalized.
 		 */
 		public RayHit hit(Ray ray);
 	}
 
 	public interface RayHit {
 		public float advance();
 
 		public RayHitDetail detail();
 	}
 
 	public interface RayHitDetail {
 		public Vector hitPoint();
 
 		public Vector normal();
 
 		public Material material();
 	}
 
 	public interface Material {
 		public Vector surfaceColor();
 
 		public float reflectionIndex();
 
 		public float refractionIndex();
 	}
 
 	public static class Ray {
 		public Vector startPoint;
 		public Vector dir;
 
 		public Ray(Vector startPoint, Vector dir) {
 			this.startPoint = startPoint;
 			this.dir = dir;
 		}
 
 		public Vector hitPoint(float advance) {
 			return Vector.add(startPoint, Vector.mul(dir, advance));
 		}
 	}
 
 	public interface LightSource {
 		public Vector source();
 
 		public Vector lit(Vector point);
 	}
 
 	public RayTracer(Collection<LightSource> lightSources, RayTraceObject scene) {
 		this.lightSources = lightSources;
 		this.scene = scene;
 	}
 
 	public void test() {
 		traceRay(4, new Ray(Vector.origin, new Vector(0f, 0f, 1f)));
 	}
 
 	public void trace(BufferedImage bufferedImage, int viewDistance) {
 		int width = bufferedImage.getWidth(), height = bufferedImage.getHeight();
 		int centreX = width / 2, centreY = height / 2;
 		int depth = 4;
 
 		for (int x = 0; x < width; x++)
 			for (int y = 0; y < height; y++) {
 				Color color;
 
 				try {
 					Vector startPoint = Vector.origin;
 					Vector dir = Vector.norm(new Vector(x - centreX, y - centreY, viewDistance));
 					Vector lit = limit(traceRay(depth, new Ray(startPoint, dir)));
 					color = new Color(lit.getX(), lit.getY(), lit.getZ());
 				} catch (Exception ex) {
 					LogUtil.error(new RuntimeException("at (" + x + ", " + y + ")", ex));
 					color = new Color(1f, 1f, 1f);
 				}
 
 				bufferedImage.setRGB(x, y, color.getRGB());
 			}
 	}
 
 	private Vector traceRay(int depth, Ray ray) {
 		float mix = 0.1f;
 		RayHit rayHit = scene.hit(ray);
 		Vector color1;
 
 		if (rayHit != null) {
 			RayHitDetail d = rayHit.detail();
 			Vector hitPoint = d.hitPoint();
 			Vector normal = Vector.norm(d.normal());
 
 			Material material = d.material();
 			float reflectionIndex = material.reflectionIndex();
 			float refractionIndex = material.refractionIndex();
 			Vector color;
 
 			if (depth > 0 && (reflectionIndex > 0f || refractionIndex > 0f)) {
 
 				// Reflection
 				float dot = Vector.dot(ray.dir, normal);
 				boolean isInside = dot > 0f;
 
 				if (isInside) {
 					normal = Vector.neg(normal);
 					dot = -dot;
 				}
 
 				Vector reflectDir = Vector.add(ray.dir, Vector.mul(normal, -2f * dot));
 				float cos = -dot / (float) Math.sqrt(Vector.normsq(ray.dir));
 
 				float fresnel = mix + ((float) Math.pow(1 - cos, 3)) * (1 - mix);
 				Vector reflectPoint = Vector.add(hitPoint, Vector.mul(normal, negligibleAdvance));
 				Vector reflectColor = traceRay(depth - 1, new Ray(reflectPoint, reflectDir));
 
 				// Refraction
 				Vector refractColor;
 
 				if (refractionIndex > 0f) {
 					float eta = isInside ? refractiveIndexRatio : 1f / refractiveIndexRatio;
 					float k = 1 - eta * eta * (1 - cos * cos);
 					Vector refractDir = Vector.add(Vector.mul(ray.dir, eta), Vector.mul(normal, eta * cos - (float) Math.sqrt(k)));
 					Vector refractPoint = Vector.add(hitPoint, Vector.mul(normal, -negligibleAdvance));
 					refractColor = traceRay(depth - 1, new Ray(refractPoint, refractDir));
 				} else
 					refractColor = Vector.origin;
 
 				color = Vector.add(Vector.mul(reflectColor, fresnel),
 						Vector.mul(Vector.mul(refractColor, refractionIndex), 1 - fresnel));
 			} else {
 				color = Vector.origin;
 
 				for (LightSource lightSource : lightSources) {
 					Vector lightDir = Vector.sub(lightSource.source(), hitPoint);
 					float dot = Vector.dot(lightDir, normal);
 
 					if (dot > 0) { // Facing the light
						RayHit lightRayHit = scene.hit(new Ray(hitPoint, lightDir));
 
 						if (lightRayHit == null || lightRayHit.advance() > 1f) {
 							Vector lightColor = lightSource.lit(hitPoint);
 							float cos = dot / (float) Math.sqrt(Vector.normsq(lightDir));
 							color = Vector.add(color, Vector.mul(lightColor, cos));
 						}
 					}
 				}
 			}
 
 			color1 = mc(color, material.surfaceColor());
 		} else
			color1 = ambient; // Ambient
 
 		return color1;
 	}
 
 	/**
 	 * Multiply vector components.
 	 */
 	private static Vector mc(Vector u, Vector v) {
 		return new Vector(u.getX() * v.getX(), u.getY() * v.getY(), u.getZ() * v.getZ());
 
 	}
 
 	/**
 	 * Limit vector components between 0 and 1.
 	 */
 	private static Vector limit(Vector u) {
 		return new Vector(limit(u.getX()), limit(u.getY()), limit(u.getZ()));
 	}
 
 	private static float limit(float f) {
 		return Math.min(1f, Math.max(0f, f));
 	}
 
 }
