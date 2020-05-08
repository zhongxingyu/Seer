 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class Polygon {
	private static int last_id_ = 0;
	
	private int id_ = last_id_++;
 	private ArrayList<Point> points_;
	
 	public Polygon() {
		this(new ArrayList<Point>());
 	}
 	
 	public Polygon(ArrayList<Point> points) {
 		points_ = points;
 	}
 	
 	public void addPoint(Point point) {
 		points_.add(point);
 	}
 	
 	public void addPoint(double x, double y) {
 		points_.add(new Point(x, y));
 	}
 	
 	@Override
 	public String toString() {
 		return points_.toString();
 	}
 
 	public String toScript() {
		DecimalFormat df = new DecimalFormat("#.##");
 
 		double area = MathHelper.polygonArea(points_);
		double centroid_x = MathHelper.centroidX(points_, area) - SVGMapParser.X_OFFSET;
		double centroid_y = (MathHelper.centroidY(points_, area) - SVGMapParser.Y_OFFSET) * -1;
 		
		// TODO: Dirty hack. Needs fix. 
		StringBuilder script = new StringBuilder(id_ + "/" + /*df.format(centroid_x)*/ 0 + " " + /*df.format(centroid_y)*/ 0 + ";");
 		
 		boolean first = true;
 		for (Point point : points_) {
 			if (first) {
 				first = false;
 			} else {
 				script.append(" ");
 			}
 			
			script.append(df.format(point.x - SVGMapParser.X_OFFSET)).append(" ").append(df.format((point.y - SVGMapParser.Y_OFFSET) * -1));
 		}

 		return script.toString();
 	}
 }
