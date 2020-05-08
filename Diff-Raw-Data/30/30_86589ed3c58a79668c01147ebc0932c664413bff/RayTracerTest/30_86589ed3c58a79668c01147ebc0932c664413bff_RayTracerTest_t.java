 package suite.rt;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.junit.Test;
 
 import suite.math.Vector;
 import suite.rt.Planar.Triangle;
 import suite.rt.RayTracer.LightSource;
 import suite.rt.RayTracer.Material;
 import suite.rt.RayTracer.RayTraceObject;
 
 public class RayTracerTest {
 
 	private Vector cr = v(1f, 0f, 0f);
 	private Vector cg = v(0f, 1f, 0f);
 	private Vector cb = v(0f, 0f, 1f);
 	private Vector cw = v(1f, 1f, 1f);
 
 	@Test
 	public void testBlank() throws IOException {
 		RayTracer rayTracer = new RayTracer(Collections.<LightSource> emptySet(),
 				new Scene(Collections.<RayTraceObject> emptySet()));
 		raster(rayTracer, "/tmp/ray-tracer-blank.png");
 	}
 
 	@Test
 	public void testSphere() throws IOException {
 		RayTraceObject sphere = Sphere.c(v(0f, 0f, 3f), 1f, reflective(cr, 0.4f));
 		Scene scene = new Scene(Arrays.asList(sphere));
 
 		LightSource light = new PointLightSource(v(10000f, 10000f, -10000f), gray(1f));
 		List<LightSource> lights = Arrays.asList(light);
 
 		RayTracer rayTracer = new RayTracer(lights, scene);
 		raster(rayTracer, "/tmp/ray-tracer-sphere.png");
 	}
 
 	@Test
 	public void testSphereReflection() throws IOException {
 		RayTraceObject sphere = Sphere.c(v(0f, 0f, 3f), 1f, reflective(cr, 0.4f));
 		RayTraceObject mirror = new Plane(v(1f, 0f, 0f), -0.3f, reflective(cw, 0.4f));
 		Scene scene = new Scene(Arrays.asList(sphere, mirror));
 
 		LightSource light = new PointLightSource(v(10000f, 10000f, -10000f), gray(1f));
 		List<LightSource> lights = Arrays.asList(light);
 
 		RayTracer rayTracer = new RayTracer(lights, scene);
 		raster(rayTracer, "/tmp/ray-tracer-sphere-reflection.png");
 	}
 
 	@Test
 	public void testSpheres() throws IOException {
 		RayTraceObject sphere0 = Sphere.c(v(-2f, 0f, 5f), 1f, reflective(cr, 0.4f));
 		RayTraceObject sphere1 = Sphere.c(v(2f, 0f, 5f), 1f, reflective(cr, 0.4f));
 		Scene scene = new Scene(Arrays.asList(sphere0, sphere1));
 
 		LightSource light = new PointLightSource(v(10000f, 10000f, -10000f), gray(1f));
 		List<LightSource> lights = Arrays.asList(light);
 
 		RayTracer rayTracer = new RayTracer(lights, scene);
 
 		raster(rayTracer, "/tmp/ray-tracer-spheres.png");
 	}
 
 	@Test
 	public void testMess() throws IOException {
		Material silver = reflective(gray(1f), 0.75f);
 
 		RayTraceObject sphere0 = Sphere.c(v(1f, -1f, 4f), 1f, reflective(cr, 0.4f));
 		RayTraceObject sphere1 = Sphere.c(v(0f, 0f, 6f), 1f, reflective(cg, 0.4f));
 		RayTraceObject sphere2 = Sphere.c(v(-1f, 1f, 8f), 1f, reflective(cb, 0.4f));
 		RayTraceObject plane = new Plane(v(0f, 1f, 0f), -3f, silver);
 		RayTraceObject triangle = new Triangle(v(0.2f, 0.2f, 3f), v(0.2f, 0f, 0f), v(0f, 0.2f, 0f), silver);
 		Scene scene = new Scene(Arrays.asList(sphere0, sphere1, sphere2, plane, triangle));
 
		LightSource light0 = new PointLightSource(v(10000f, 10000f, -10000f), gray(0.6f));
		LightSource light1 = new PointLightSource(v(-10000f, 10000f, -10000f), gray(0.6f));
		List<LightSource> lights = Arrays.asList(light0, light1);
 
 		RayTracer rayTracer = new RayTracer(lights, scene);
 
 		raster(rayTracer, "/tmp/ray-tracer.png");
 	}
 
 	private void raster(RayTracer rayTracer, String filename) throws IOException {
 		BufferedImage bufferedImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
 		rayTracer.trace(bufferedImage, 640);
 		ImageIO.write(bufferedImage, "png", new File(filename));
 	}
 
 	private Material reflective(final Vector color, final float index) {
 		return new Material() {
 			public Vector filter() {
 				return color;
 			}
 
 			public float diffusionIndex() {
				return 1f - index;
 			}
 
 			public float reflectionIndex() {
 				return index;
 			}
 
 			public float refractionIndex() {
 				return 0f;
 			}
 		};
 	}
 
 	private Vector gray(float f) {
 		return v(f, f, f);
 	}
 
 	private Vector v(float x, float y, float z) {
 		return new Vector(x, y, z);
 	}
 
 }
