 package suite.rt;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.Collection;
 
 import suite.math.Vector;
 import suite.util.LogUtil;
 
 public class RayTracer {
 
	public static final float negligibleAdvance = 0.00001f;
 
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
 		public Vector filter(); // Filters the light color
 
 		public float diffusionIndex();
 
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
 		Vector color;
 
 		if (depth > 0) {
 			RayHit rayHit = scene.hit(ray);
 
 			if (rayHit != null)
 				color = traceRayHit(depth, ray, rayHit);
 			else
 				color = traceLightSources(ray);
 		} else
 			color = traceLightSources(ray);
 
 		return color;
 	}
 
 	private Vector traceRayHit(int depth, Ray ray, RayHit rayHit) {
 		RayHitDetail d = rayHit.detail();
 		Vector hitPoint = d.hitPoint();
 		Vector normal = d.normal();
 		float nsn = Vector.normsq(normal);
 
 		// Cast from light sources
 		Vector lightColor = Vector.origin;
 
 		for (LightSource lightSource : lightSources) {
 			Vector lightDir = Vector.sub(lightSource.source(), hitPoint);
 			float dot0 = Vector.dot(lightDir, normal);
 
 			if (dot0 > 0) { // Facing the light
 				Ray lightRay = new Ray(hitPoint, lightDir);
 				RayHit lightRayHit = scene.hit(lightRay);
 				Vector lightColor1 = Vector.origin;
 
 				if (lightRayHit != null)
 					lightColor1 = traceRayHit(depth, lightRay, lightRayHit);
 
 				if (lightRayHit == null || lightRayHit.advance() > 1f)
 					lightColor1 = Vector.add(lightColor, lightSource.lit(hitPoint));
 
 				float cos = dot0 / (float) Math.sqrt(Vector.normsq(lightDir) * nsn);
 				lightColor = Vector.add(lightColor, Vector.mul(lightColor1, cos));
 			}
 		}
 
 		// Reflection
 		Vector reflectDir = Vector.add(ray.dir, Vector.mul(normal, -2f * Vector.dot(ray.dir, normal) / nsn));
 		Vector reflectColor = traceRay(depth - 1, new Ray(hitPoint, reflectDir));
 
 		// TODO refraction
 
 		Material material = d.material();
 		float diffusionIndex = material.diffusionIndex();
 		float reflectionIndex = material.reflectionIndex();
 
 		Vector sum = Vector.add(Vector.mul(lightColor, diffusionIndex), Vector.mul(reflectColor, reflectionIndex));
 		return mc(sum, material.filter());
 	}
 
 	private Vector traceLightSources(Ray ray) {
 		Vector color = Vector.origin;
 
 		for (LightSource lightSource : lightSources) {
 			Vector lightDir = Vector.sub(lightSource.source(), ray.startPoint);
 			float dot = Vector.dot(lightDir, ray.dir);
 
 			if (dot > 0) {
 				float cos = dot / (float) Math.sqrt(Vector.normsq(lightDir) * Vector.normsq(ray.dir));
 				color = Vector.add(color, Vector.mul(lightSource.lit(ray.startPoint), cos));
 			}
 		}
 
 		return color;
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
