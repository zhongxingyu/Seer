 package distributed;
 
 import image.Image;
 
 import org.gridgain.grid.GridException;
 import org.gridgain.grid.GridFactory;
 import org.gridgain.grid.gridify.Gridify;
 
 import raytracing.RayTracer;
 import scene.Scene;
 
 public class GridRenderer {
 	
 	public static void render(Scene scene) throws GridException {
 		GridFactory.start();
 		int[][] colors = null;
 		try {
 			long begin = System.currentTimeMillis();
 			colors = executeRayTracer(scene, 0, scene.getHeight());
 			System.out.println("Distributed execution time: " + (System.currentTimeMillis() - begin));
 		} finally {
			GridFactory.stop(false);
 			Image.generateImage(colors).save("distributedRender.jpg", "jpeg");
 		}
 	}
 	
 	@Gridify(taskClass=RayTracerTask.class)
 	public static int[][] executeRayTracer(Scene scene, int fromRow, int toRow) {
 		return new RayTracer(scene).execute(fromRow, toRow);
 	}
 
 }
