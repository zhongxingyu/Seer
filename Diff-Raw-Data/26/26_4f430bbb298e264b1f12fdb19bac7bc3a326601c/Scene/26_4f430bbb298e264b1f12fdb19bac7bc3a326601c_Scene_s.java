 package ex3.render.raytrace;
 
 import java.awt.Color;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import math.Ray;
 import math.Vec;
 
 
 /**
  * A Scene class containing all the scene objects including camera, lights and
  * surfaces.
  * 
  */
 public class Scene implements IInitable {
 
 	protected Vec backgroundCol;
 	protected Vec ambientLight;
 	protected List<Object3D> surfaces;
 	protected List<Light> lights;
 	protected Camera camera;
 
 	public Scene() {
 		backgroundCol = new Vec(0.5, 0.5, 0.5);
 		ambientLight = new Vec(0.5, 0.5, 0.5);
 		surfaces = new LinkedList<Object3D>();
 		lights = new LinkedList<Light>();
 		camera = new Camera();
 	}
 
 	public void init(Map<String, String> attributes) {
 		if (attributes.containsKey("background-col"))
 			backgroundCol = new Vec(attributes.get("background-col"));
 		if (attributes.containsKey("ambient-light"))
 			ambientLight = new Vec(attributes.get("ambient-light"));
 	}
 
 	/**
 	 * Send ray return the nearest intersection. Return null if no intersection
 	 * 
 	 * @param ray
 	 * @return
 	 */
 	public Hit findIntersection(Ray ray) {
 
 		double minDistance = Double.POSITIVE_INFINITY;
 		Object3D minSurface = null;
 		for (Object3D surface : surfaces) {
 			double d = surface.nearestIntersection(ray);
 			if (minDistance > d) {
 				minDistance = d;
 				minSurface = surface;
 			}
 		}
 
 		if (Double.isInfinite(minDistance))
 			return null;
 
		Vec intersection = new Vec(ray.p);
		intersection.mac(minDistance, ray.v);
 
 		return new Hit(intersection, minSurface);
 	}
 
 	/**
 	 * Calculates the color at a given intersection point
 	 * 
 	 * @param hit
 	 *            The intersection point and surface
 	 * @param ray
 	 *            Hitting ray
 	 * @return
 	 */
 	public Color calcColor(Hit hit, Ray ray) {
 
 		if (hit == null)
 			return backgroundCol.toColor();
 
 		// TODO: Implement color calculation
 		return Color.GRAY;
 	}
 
 	/**
 	 * Add objects to the scene by name
 	 * 
 	 * @param name
 	 *            Object's name
 	 * @param attributes
 	 *            Object's attributes
 	 */
 	public void addObjectByName(String name, Map<String, String> attributes) {
 		if (name.equals("sphere")) {
 			Object3D surface = new Sphere();
 			surface.init(attributes);
 			surfaces.add(surface);
 		}
 		if (name.equals("light-point")) {
 			Light light = new Light();
 			light.init(attributes);
 			lights.add(light);
 		}
 	}
 }
