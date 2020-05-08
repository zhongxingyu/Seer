 
 public class PhysicsEngine {
 	
 	public PhysicsEngine() {
 		
 	}
 	
 	public static void affectObject(GraphicsObject go, Vector force) {
 		
 	}
 	
 	public static boolean isColliding(GraphicsObject go1, GraphicsObject go2) {		
 		double go1x1 = go1.getReferencePoint()[0];
 		double go1x2 = go1x1 + go1.getWidth();
 		double go1y1 = go1.getReferencePoint()[1];
 		double go1y2 = go1y1 + go1.getHeight();
 		
 		double go2x1 = go2.getReferencePoint()[0];
 		double go2x2 = go2x1 + go2.getWidth();
 		double go2y1 = go2.getReferencePoint()[1];
 		double go2y2 = go2y1 + go2.getHeight();
 		
		System.out.println(go1y1 + " - " + go1y2);
		System.out.println(go2y1 + " - " + go2y2);
		System.out.println();
		
 		// If go2.x1 is IN BETWEEN of go1x1 and go1x2 OR go2x2 is IN BETWEEN go1x1 and go1x2
		if( (go2x1 >= go1x1 && go2x1 <= go1x2) || (go2x2 >= go1x1 && go2x2 <= go1x2) ) {
 			// If go2.y1 OR go2.y2 is IN BETWEEN go1.y1 AND go1.y2
			if( (go2y1 >= go1y1 && go2y1 <= go1y2) || (go2y2 >= go1y1 && go2y2 <= go1y2) ) {
 				return true;
 			} else {
 				return false;
 			}		
 		} else {
 			return false;
 		}
 	}
 }
