 package core;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a single Polygon within a model
  * @author Oliver Greenaway
  *
  */
 public class Polygon {
 
 	private List<Point3D> points;
 	private int rRef,gRef,bRef;
 	private List<PolygonEdge> edges;
 
 	/**
 	 * Constructs a new Polygon using the given color and points
 	 * @param r Red reflectance
 	 * @param g Green reflectance
 	 * @param b Blue reflectance
 	 * @param points Points of the polygon
 	 */
 	public Polygon(int r, int g, int b, Point3D... points){
 		rRef = r;
 		gRef = g;
 		bRef = b;
 		this.points = new ArrayList<Point3D>();
 		this.edges = new ArrayList<PolygonEdge>();
 		Point3D previous = null;
 		if(points.length > 0){
 			previous = points[0];
 			this.points.add(previous);
 		}
 		for(int i=1; i<points.length; i++){
 			this.points.add(points[i]);
 			this.edges.add(new PolygonEdge(previous, points[i]));
 			previous = points[i];
 		}
 		if(points.length > 2){
 			this.edges.add(new PolygonEdge(previous, points[0]));
 		}
 	}
 
 	/**
 	 * Returns a list of corner points for the polygon
 	 * @return List of corner points
 	 */
 	public List<Point3D> getPoints(){
 		return points;
 	}
 
 	/**
 	 * Returns the normal Vector of the polygon
 	 * @return The normal Vector
 	 */
 	public Point3D getNormal(){
 		double ax = points.get(1).getX()-points.get(0).getX();
 		double ay = points.get(1).getY()-points.get(0).getY();
 		double az = points.get(1).getZ()-points.get(0).getZ();
 		double bx = points.get(2).getX()-points.get(1).getX();
 		double by = points.get(2).getY()-points.get(1).getY();
 		double bz = points.get(2).getZ()-points.get(1).getZ();
 		double x = (ay*bz - az*by);
 		double y = (az*bx - ax*bz);
 		double z = (ax*by - ay*bx);
 		return new Point3D(x, y, z);
 
 	}
 
 	/**
 	 * Takes a vector and returns the unit vector
 	 * @param p The Vector to be converted
 	 * @return The Unit Vector
 	 */
 	private Point3D toUnitVector(Point3D p){
 		double length = Math.sqrt(Math.pow(p.getX(), 2)+Math.pow(p.getY(), 2)+Math.pow(p.getZ(), 2));
 		return new Point3D(p.getX()/length, p.getY()/length, p.getZ()/length);
 	}
 
 	/**
 	 * Returns a list of where the edge are located down the y axis
 	 * @return An array of edge points
 	 */
 	public double[][] getEdgeList(){
 		int minY = Integer.MAX_VALUE;
 		int maxY = Integer.MIN_VALUE;
 		for(Point3D p : points){
 			minY = Math.min(minY, (int)Math.round(p.getY()));
 			maxY = Math.max(maxY, (int)Math.round(p.getY()));
 		}
 		double[][] edgeList = new double[maxY-minY+1][4];
 		for(int i=0; i<edgeList.length; i++){
 			edgeList[i][0] = Double.MAX_VALUE;
 			edgeList[i][1] = Double.MAX_VALUE;
 			edgeList[i][2] = Double.MIN_VALUE;
 			edgeList[i][3] = Double.MAX_VALUE;
 		}
 		for(PolygonEdge edge : edges){
 			Point3D v1 = edge.getPoint1();
 			Point3D v2 = edge.getPoint2();
 			if(v2.getY() < v1.getY()){
 				Point3D temp = v2;
 				v2 = v1;
 				v1 = temp;
 			}
 			double gradientX = ((v2.getX()-v1.getX())/(v2.getY()-v1.getY()));
 			double gradientZ = ((v2.getZ()-v1.getZ())/(v2.getY()-v1.getY()));
 			double curX = v1.getX();
 			double curZ = v1.getZ();
 			for(int i=(int)(Math.round(v1.getY())-minY); i<(int)(Math.round(v2.getY())-minY); i++){
 				if(curX < edgeList[i][0]){
 					edgeList[i][0] = curX;
 					edgeList[i][1] = curZ;
 				}
 				if(curX > edgeList[i][2]){
 					edgeList[i][2] = curX;
 					edgeList[i][3] = curZ;
 				}
 				curX += gradientX;
 				curZ += gradientZ;
 			}
			int i=(int)(Math.round(v2.getY())-minY);
 			if(v2.getX() < edgeList[i][0]){
 				edgeList[i][0] = v2.getX();
 				edgeList[i][1] = v2.getZ();
 			}
 			if(v2.getX() > edgeList[i][2]){
 				edgeList[i][2] = v2.getX();
 				edgeList[i][3] = v2.getZ();
 			}
 		}
 		return edgeList;
 	}
 
 	/**
 	 * Returns the color that the polygon should be displayed as given the light details
 	 * @param lightDirection The direction of the light source
 	 * @param ambient The ambiant light level
 	 * @param incedentIntensity The intensity of the light
 	 * @return The polygons current color
 	 */
 	public Color getColor(Point3D lightDirection, Light ambient, Light incedentIntensity){
 		double cost = toUnitVector(getNormal()).dotProduct(toUnitVector(lightDirection));
 
 		int red = (int)((ambient.getR() + incedentIntensity.getR() * cost) * rRef);
 		int green = (int)((ambient.getG() + incedentIntensity.getG() * cost) * gRef);
 		int blue = (int)((ambient.getB() + incedentIntensity.getB() * cost) * bRef);
 		red = (red >= 0) ? (red <= 255) ? red : 255 : 0;
 		green = (green >= 0) ? (green <= 255) ? green : 255 : 0;
 		blue = (blue >= 0) ? (blue <= 255) ? blue : 255 : 0;
 		return new Color(red,green,blue);
 	}
 
 }
