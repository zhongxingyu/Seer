 package ex3.render.raytrace;
 
 import java.awt.Color;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import math.Point3D;
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
 	}
 
 	public void init(Map<String, String> attributes) {
 		if (attributes.containsKey("background-col"))
 			backgroundCol = new Vec(attributes.get("background-col"));
 		if (attributes.containsKey("ambient-light"))
 			ambientLight = new Vec(attributes.get("ambient-light"));
 	}
 
     public void setCamera(Camera camera) {
         this.camera = camera;
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
 
 		Point3D intersection = new Point3D(ray.origin);
 		intersection.mac(minDistance, ray.direction);
 
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
 		
         // start from the ambient light color value
         Vec lightSum = new Vec(hit.surface.material.emission);
 
         // add the material ambient using the global ambient
         lightSum.add(Vec.scale(hit.surface.material.ambient, ambientLight));
 
         for (Light light : lights) {
             Vec lightDirection =  new Vec(hit.intersection, light.pos);
 
             // it's more efficient to get both distance types
             double lightDistance = lightDirection.length();
             double lightDistanceSquared = lightDirection.lengthSquared();
 
             lightDirection.normalize();
 
             Ray lightRay = new Ray(light.pos, lightDirection);
             Vec lightHitNormal = hit.surface.normalAt(hit.intersection, lightRay);
 
             // calculate the dot product for diffusive shading
             Vec diffuse = hit.surface.material.diffuse.clone();
             Vec specular = hit.surface.material.specular.clone();
 
 			double lightDistanceAttenuation = 1 / (light.kc + light.kl * lightDistance + light.kq * lightDistanceSquared);
 
             // Lambert diffusion
             // multiply by the light color, diffusion amount by angle, and inverse square (3d distance) light attenuation
 			// we negate the result because the light direction is facing the normal of the plane
             double diffusionAmount = -lightHitNormal.dotProd(lightDirection);
 
             if (diffusionAmount < 0) {
                 diffusionAmount = 0;
             }
 
             diffuse.scale(light.color);
             diffuse.scale(diffusionAmount);
 
             lightSum.add(diffuse);
             
             // Phong specular model
             // first, calculate the reflection direction vector
             Vec reflection = lightDirection.reflect(lightHitNormal);
 
             // first we multiply by the light color
             specular.scale(light.color);
 
             // then, we multiply by the cosine of the angle between the reflected ray from the light and the viewport.
             // we then multiply the scalar with the shininess factor to achieve greater shininiess.
			double specularAmount = Math.pow(-reflection.dotProd(ray.direction), hit.surface.material.shininess);
 
            specular.scale(specularAmount);
            lightSum.add(specular);
            
 
 			lightSum.scale(lightDistanceAttenuation);
 
         }
 
 		return lightSum.toColor();
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
 
         if (name.equals("trimesh")) {
             Object3D surface = new TriMesh();
             surface.init(attributes);
             surfaces.add(surface);
         }
 
 		if (name.equals("omni-light")) {
 			Light light = new OmniLight();
 			light.init(attributes);
 			lights.add(light);
 		}
 
         if (name.equals("spot-light")) {
             Light light = new SpotLight();
             light.init(attributes);
             lights.add(light);
         }
 
         if (name.equals("directional-light")) {
             Light light = new DirectionalLight();
             light.init(attributes);
             lights.add(light);
         }
 	}
 }
