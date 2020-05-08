 package org.jtrace;
 
 import static java.util.Arrays.asList;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jtrace.cameras.Camera;
 import org.jtrace.geometry.GeometricObject;
 import org.jtrace.lights.Light;
 import org.jtrace.primitives.ColorRGB;
 import org.jtrace.shader.Shader;
 
 /**
  * Abstract class containing the common operation and fields of a Tracer.
  */
 public class Tracer {
 
     private List<TracerListener> listeners = new LinkedList<TracerListener>();
     private List<TracerInterceptor> interceptors = new LinkedList<TracerInterceptor>();
     
     private List<Shader> shaders = new LinkedList<Shader>();
 
     /**
      * Traces the given {@link Jay}.
      * 
      * @param scene
      *            the {@link Scene} containing the objects to rendered.
      * @param jay
      *            the {@link Jay} to be casted.
      * @return the color of the object hit by the {@link Jay} or the
      *         {@link Scene}'s background color if there was no hit.
      */
     public ColorRGB trace(Scene scene, Jay jay) {
         Hit hit = cast(scene, jay);
 
         // if there was a collision, calculate illumination
         if (hit.isHit()) {
           return shade(scene, jay, hit);
         }
 
        return scene.getBackgroundColor();
     }
 
     public Hit cast(Scene scene, Jay jay) {
         double tMin = Double.MAX_VALUE;
         GeometricObject hitObject = null;
         Hit hitMin = new Hit();
 
         for (GeometricObject object : scene.getObjects()) {
             Hit hit = object.hit(jay);
 
             if (hit.isHit() && hit.getT() < tMin) {
                 tMin = hit.getT();
                 hitObject = object;
                 hitMin = hit;
             }
         }
 
 
         hitMin.setObject(hitObject);
         return hitMin;
     }
 
     public ColorRGB shade(Scene scene, Jay jay, Hit hit) {
        ColorRGB color = ColorRGB.BLACK;
 
         for (Light light : scene.getLigths()) {
         	beforeShade(light, color);
             for (Shader shader : shaders) {
             	if (shouldShade(shader, light, hit, jay, hit.getObject())) {
             		ColorRGB shaderColor = shader.shade(light, hit, jay, hit.getObject());
                 
             		color = color.add(shaderColor);
             	}
             }
             afterShade(light, color);
         }
 
         return color;
     }
 
     private boolean shouldShade(Shader shader, Light light, Hit hit, Jay jay, GeometricObject object) {
 		boolean shouldShade = true;
     	
 		for (TracerInterceptor interceptor : interceptors) {
 			shouldShade = shouldShade && interceptor.shouldShade(shader, light, hit, jay, object);
 		}
 		
     	return shouldShade;
 	}
 
 	private void afterShade(Light light, ColorRGB color) {
     	for (TracerInterceptor interceptor : interceptors) {
 			interceptor.afterShade(light, color);
 		}
 		
 	}
 
 	private void beforeShade(Light light, ColorRGB color) {
 		for (TracerInterceptor interceptor : interceptors) {
 			interceptor.beforeShade(light, color);
 		}
 	}
 
 	/**
      * Renders the scene.
      * The {@link Jay} casting strategy is defined by the {@link Camera} used in the scene.
      * @param scene the {@link Scene} to be rendered.
      * @param viewPlane this should change =p.
      */
     public void render(Scene scene, ViewPlane viewPlane) {
         final int hres = viewPlane.getHres();
         final int vres = viewPlane.getVres();
         final Camera camera = scene.getCamera();
 
         fireStart(viewPlane);
         initInterceptors(scene);
         
         for (int r = 0; r < vres; r++) {
             for (int c = 0; c < hres; c++) {
                 final Jay jay = camera.createJay(r, c, vres, hres);
 
                 final ColorRGB color = trace(scene, jay);
 
                 fireAfterTrace(color, c, r);
             }
         }
 
         fireFinish();
     }
 
     protected void initInterceptors(Scene scene) {
 		for (TracerInterceptor interceptor : interceptors) {
 			interceptor.init(this, scene);
 		}
 	}
 
 	protected void fireFinish() {
         for (TracerListener listener : listeners) {
             listener.finish();
         }
     }
 
     protected void fireAfterTrace(ColorRGB color, int c, int r) {
         for (TracerListener listener : listeners) {
             listener.afterTrace(color, c, r);
         }
     }
 
     protected void fireStart(ViewPlane viewPlane) {
         for (TracerListener listener : listeners) {
             listener.start(viewPlane);
         }
     }
 
     /**
      * Adds a {@link TracerListener} to the {@link Tracer}.
      * 
      * @param paramListeners one or more listeners.
      */
     public void addListeners(TracerListener... paramListeners) {
         listeners.addAll(asList(paramListeners));
     }
 
 
     /**
      * Adds a {@link Shader} to the {@link Tracer}.
      * 
      * @param paramShaders one or more shaders.
      */
     public void addShaders(Shader... paramShaders) {
         shaders.addAll(asList(paramShaders));
     }
     
     public void addInterceptors(TracerInterceptor... paramInterceptors) {
     	interceptors.addAll(asList(paramInterceptors));
     }
 
     public void clearListeners() {
         listeners.clear();
     }
 }
