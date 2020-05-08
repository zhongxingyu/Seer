 package conicio;
 
 import conicio.shapes.*;
 import conicio.util.*;
 
 import java.awt.Toolkit;
 import java.awt.Image;
 import java.awt.image.*;
 import java.util.*;
 
 /**
  *
  **/
 public class Renderer<C extends Color<C>> implements Runnable {
 	protected final int maxDepth;
 
 	/** The scene description. **/
 	protected final Scene<C> scene;
 
 	/** The perspective of the rendered image. **/
 	protected final Camera camera;
 
 	/** The viewing plane pixel width. **/
 	protected final int xSize;
 
 	/** The viewing plane pixel height. **/
 	protected final int ySize;
 
 	/** The backing pixel matrix. **/
 	protected final int[] pixels;
 
 	// Memory mapped images
 	private final MemoryImageSource source;
 	private final Image buffer;
 
 	/**
 	 * Constructs a Renderer which renders an image as described by the given
 	 * Scene with perspective as given by the Camera.
 	 *
 	 * @param scene the scene description
 	 **/
 	public Renderer(Scene<C> scene) { 
 		this.maxDepth = 3;
 	
 		this.scene    = scene;
 		this.camera   = scene.camera();
 		this.xSize    = scene.xSize();
 		this.ySize    = scene.ySize();
 
 		/* setup image background buffer */
 		this.pixels   = new int[xSize * ySize];
 		this.source   = new MemoryImageSource(xSize, ySize, pixels, 0, xSize);
 		this.source.setAnimated(true);
 		//this.source.setFullBufferUpdates(true);
 		this.buffer = Toolkit.getDefaultToolkit().createImage(source);
 
 		/* setup the background color */
 		final int background = 0xFF000000;
 		for(int y = 0; y < ySize; y++) {
 			for(int x = 0; x < xSize; x++) {
 				pixels[y * xSize + x] = background;
 			}
 		}
 	}
 
 	/**
 	 *
 	 **/
 	public void run() {
 		// clean this up?
 		Vector3 direction = camera.center.sub(camera.eye);
 		Vector3 up = camera.up;
 		Vector3 du = direction.mul(up).neg().normalize();
 		Vector3 dv = direction.mul(du).neg().normalize();
 
 		double fovAdjust = xSize / (2.0 * Math.tan(camera.fov / 2.0));
 		Vector3 base = direction.normalize().mul(fovAdjust);
 
 		int samples = 1; // becomes multisampler
 		for(int y = 0; y < ySize; y++) {
 			for(int x = 0; x < xSize; x++) {
 				C color = scene.factory.black();
 
 				// multisampling
 				Vector3 normal = base;
 				normal = normal.sub(du.mul(x - xSize / 2.0));
 				normal = normal.add(dv.mul(y - ySize / 2.0));
 
 				Ray ray = new Ray(camera.eye, normal);
 				color = color.add(castRay(ray).scale(1.0 / samples));
 
 				pixels[y * xSize + x] = color.asRGB();
 			}
 		}
 	}
 	
 	/**
 	 *
 	 **/
 	public Image result() {
 		source.newPixels();
 		return buffer;
 	}
 
 	/**
 	 *
 	 **/
 	/*
 	protected Set<Vector3> photonMap() {
 		// As the scene is never mutated during the generation of the photon map, this process may be parallelized.
 		// Note however that each thread should probably have its own set of points and they should be merged after the parallel portion.
 		Set<Vector3> photonMap = new HashSet<Vector3>();
 		for(Light light : scene.lights) {
 			for(Ray photon : light.photons()) {
 				
 			}
 		}
 
 		return photonMap;
 	}
 	*/
 
 	/**
 	 *
 	 **/
 	/*
 	protected void castPrimaryPhoton(Set<Vector3> photonMap, Ray ray, Color color, int depth) {
 		if(depth >= 3) { return; } // off in to nothingness
 
 		SortedMap<Double, Shape> intersections = new TreeMap<Double, Shape>();
 		for(Shape shape : shapes) {
 			double[] candidates = ray.intersect(shape);
 			if(candidates.length != 0) {
 				intersections.put(candidates[0], shape);
 			}
 		}
 
 		if(intersections.isEmpty()) { return; } // off in to nothingness again
 		else {
 			Shape   intersected = intersections.get(intersections.firstKey());
 			Vector3 intersect   = ray.cast(intersections.firstKey());
 
 			if(intersected.material.alpha   < 1.00) { castRefractPhoton(photonMap, new Ray(intersect, ray.normal), color, depth); }
 			if(intersected.material.reflect > 0.00) { castReflectPhoton(photonMap, new Ray(intersect, ray.normal), color, depth); }
 		}
 	}
 	*/
 
 
 
 	/**
 	 *
 	 **/
 	protected C castRay(Ray ray) {
 		return castPrimary(ray, 0);
 	}
 
 	/**
 	 *
 	 **/
 	protected C castPrimary(Ray ray, int depth) {
 		if(depth >= maxDepth) { return scene.factory.white(); }
 
 		/* find ray-object intersection points */
 		SortedMap<Double, Shape<C>> intersections = new TreeMap<Double, Shape<C>>();
 		for(Shape<C> shape : scene.shapes) {
 			double[] candidates = ray.intersect(shape);
 			if(candidates.length != 0) { 
 				intersections.put(candidates[0], shape); 
 			}
 		}
 
 		/* assign a color to the ray */
 		if(intersections.isEmpty()) { return scene.factory.black(); }
 		else {
 			Shape<C> intersected  = intersections.get(intersections.firstKey());
 			Vector3  intersection = ray.cast(intersections.firstKey());
 
 			C accum = scene.factory.black(); // color dynamics?
 			accum = accum.add(castReflect(ray, intersected, intersection, depth));
 			accum = accum.add(castRefract(ray, intersected, intersection, depth));
 			accum = accum.add(castShadows(ray, intersected, intersection));
 
 			return accum;
 		}
 	}
 
 	/**
 	 *
 	 **/
 	protected C castReflect(Ray ray, Shape<C> intersected, Vector3 intersection, int depth) {
 		double reflectIndex = intersected.material.reflect;
 		if(reflectIndex <= 0.0) { return scene.factory.black(); }
 
 		Ray reflection = new Ray(
 			intersection, 
 			(ray.normal).reflect(intersected.normal(intersection))
 		);
 		
 		C masked = intersected.material.ambient;
 		  masked = masked.mask (castPrimary(reflection, depth + 1));
 		  masked = masked.scale(reflectIndex);
 		return masked;
 	}
 
 	/**
 	 *
 	 **/
 	// Model Total Internal Reflection!
 	protected C castRefract(Ray ray, Shape<C> intersected, Vector3 intersection, int depth) {
 		double refractIndex = intersected.material.refract;
 		if(refractIndex <= 0.0) { return scene.factory.black(); }
 
 		/* refract ray in to the object */
		Vector3 inner = ray.normal.refract(intersected.normal(intersection), 1.0, refractIndex);
		if(inner == null) { return scene.factory.white(); }
		Ray internal = new Ray(intersection, inner);
 
 		Ray external = null;
 		double[] candidates = internal.intersect(intersected);
 		if(candidates.length != 0) { 
 			intersection = internal.cast(candidates[0]);
 
 			/* refract ray out of the object */
			Vector3 outer = internal.normal.refract(intersected.normal(intersection).neg(), refractIndex, 1.0);
			if(outer == null) { external = internal;                     }
			else              { external = new Ray(intersection, outer); }
 		}
 
 		// beer's law?
 		//return intersected.material.ambient.mask(castPrimary(external, depth + 1)).scale(1 - intersected.material.alpha);
 		// model of beer's law with 1 - a as a density metric
 		
 		//double dist = (candidates.length != 0 ? candidates[0] : 10000.0);
 		C masked = intersected.material.ambient;
 		  masked = masked.mask(castPrimary(external, depth + 1));
 		//  masked.scale(Math.pow(Math.E, -(intersected.material.refract * dist));
 		
 		return masked;
 	}
 
 	/**
 	 *
 	 **/
 	protected C castShadows(Ray ray, Shape<C> intersected, Vector3 intersect) {
 		//Set<Light> active = new HashSet<Light>();
 		//for(Light light : scene.lights) {
 		//	if(light.illuminated(intersect, scene)) { active.add(light); }
 		//}
 		
 		//Material material = intersected.material();
 		//Vector3  normal   = intersected.normal(intersect);
 		
 		//Color intensity = Color.BLACK;
 		//for(Light light : active) {
 		//
 		//}
 	
 	
 	
 		Set<Light<C>> active = new HashSet<Light<C>>();
 
 		for(Light<C> light : scene.lights) {
 			if(light.illuminated(intersect, scene)) { active.add(light); }
 		}
 
 		Material<C> material = intersected.material();
 		Vector3 normal       = intersected.normal(intersect);
 		
 		C intensity = scene.factory.black();
 
 		for(Light<C> light : active) {
 			Vector3 displacement = (((PointLight)light).origin).sub(intersect).normalize();
 			Vector3 reflection   = normal.mul(displacement.dot(normal)).mul(2.0).sub(displacement).normalize();
 
 
 			C diffuse  = (material.diffuse ).scale(2 * displacement.dot(normal));
 			C specular = (material.specular).scale(Math.pow(reflection.dot((ray.normal).mul(-1)), material.shine));
 
 			//specular.scale(material.reflect)
 			intensity = intensity.add(diffuse.scale(material.alpha).add(specular).mask(light.color));
 		}
 		
 		return intensity;
 	}
 	
 	/**
 	 *
 	 **/
 	public static <C extends Color<C>> Renderer<C> create(Scene<C> scene) {
 		return new Renderer<C>(scene);
 	}
 }
