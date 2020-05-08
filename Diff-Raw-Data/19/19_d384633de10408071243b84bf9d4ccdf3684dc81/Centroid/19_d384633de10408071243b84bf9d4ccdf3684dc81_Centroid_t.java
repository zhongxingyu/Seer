 package clustering;
 
 import java.util.ArrayList;
 
 public class Centroid extends Point {
 	ArrayList<Point> grouping = new ArrayList<Point>();
 	private boolean hasMoved = true;
 	Centroid(double x, double y) {
 		super(x, y);
 	}
 	
 	public void addPoint(Point p) {
 		if(!grouping.contains(p))
 			grouping.add(p);
 		
 		reposition();
 	}
 	
 	public void reposition(){
 		double[] coords = getAverageCoordinates();
 		setX(coords[0]);
 		setY(coords[1]);
 	}
 	
 	public boolean hasMoved(){
 		boolean moved = hasMoved;
 		hasMoved = false;
 		return moved;
 	}
 	
 	public double[] getAverageCoordinates() {
 		double[] co = {
 				x,
 				y
 		};
 		
 		for(Point p : grouping){
 			co[0] += p.getX();
 			co[1] += p.getY();
 		}
 		int size = grouping.size();
 		
		double factor = 1e5;
 		
 		co[0] = (Math.round(co[0] * factor) / factor) /size;
 		co[1] = (Math.round(co[1] * factor) / factor) /size;
 		
 		hasMoved = (co[0] == x && co[1] == y) ? false : true;
 		
 		return co;
 	}
 }
