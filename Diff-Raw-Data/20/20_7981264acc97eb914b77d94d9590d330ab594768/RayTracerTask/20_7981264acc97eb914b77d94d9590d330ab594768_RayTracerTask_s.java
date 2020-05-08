 package distributed;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
import org.apache.commons.lang.ArrayUtils;
 import org.gridgain.grid.GridException;
 import org.gridgain.grid.GridJob;
 import org.gridgain.grid.GridJobAdapterEx;
 import org.gridgain.grid.GridJobResult;
 import org.gridgain.grid.GridTaskSplitAdapter;
 import org.gridgain.grid.gridify.GridifyArgument;
 
 import scene.Scene;
 
 public class RayTracerTask extends GridTaskSplitAdapter<GridifyArgument, int[][]> {
 

 	@Override
 	protected Collection<? extends GridJob> split(int gridSize, GridifyArgument arg)
 			throws GridException {
 		Scene scene = (Scene)arg.getMethodParameters()[0];
 		int delta = scene.getHeight() / gridSize;
 		List<GridJobAdapterEx> jobs = new ArrayList<GridJobAdapterEx>(gridSize);
 		for (int i = 0; i < scene.getHeight(); i += delta) {
 			jobs.add(new GridJobAdapterEx(scene, i, Math.min(i + delta, scene.getHeight())) {
 
 				@Override
 				public int[][] execute() throws GridException {					
					int[][] result = GridRenderer.executeRayTracer((Scene)argument(0),
																	(Integer)argument(1), (Integer)argument(2));
					return result;
 				}
 				
 			});
 		}
 		return jobs;
 	}
 	
 	@Override
 	public int[][] reduce(List<GridJobResult> arg) throws GridException {
 		int[][] sample = (int[][])arg.get(0).getData();
 		int[][] result = new int[sample.length * arg.size()][sample[0].length];
 		int pos = 0;
 		for (GridJobResult jobResult : arg) {
 			int[][] data = (int[][])jobResult.getData();
 			for (int i = 0; i < data.length; i++) {
 				result[pos++] = data[i];
				/*for (int j = 0; j < data[0].length; j++) 
					result[pos][j] = data[i][j];
				pos++;*/
 			}
 		}
 		return result;
 	}
 
 }
