 package se2.e.utilities.path;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se2.e.utilities.PathInterpolator;
 import se2.e.utilities.Vector2D;
 import se2.e.utilities.Where;
 
 /**
  * A very simple implementation of an interpolatable path.
  * 
  * @author anders
  *
  */
 public class LinearPathInterpolator extends BasePathInterpolator {
 
 	/**
 	 * 
 	 * @param originPoint
 	 * @param positions
 	 */
 	public LinearPathInterpolator(Vector2D... positions) {
 		assert(positions != null && positions.length >= 2);
 		for (int i = 0; i < positions.length-1; i++) {
 			addStep(positions[i], positions[i+1]);
 			}
 		}
 	
 	}
