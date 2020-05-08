 import javax.swing.*;
 
 import org.poly2tri.*;
 import org.poly2tri.geometry.polygon.*;
 import org.poly2tri.geometry.polygon.Polygon;
 import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 
 public class PolygonInputFrame extends JFrame implements ActionListener {
 
 	PlotArea drawPanel;
 	ControlArea buttonPanel;
 	Point2D.Double[] polyP;
 	Point2D.Double[] polyQ;
 
 	public PolygonInputFrame() {
     	super("Frechet Distance - Simple Polygons");
     	drawPanel = new PlotArea();
     	this.getContentPane().add(drawPanel);
     	this.addTestPolygons();
     	Point2D.Double start = new Point2D.Double((polyQ[0].x+polyQ[1].x)/2,
                 (polyQ[0].y+polyQ[1].y)/2);
     	Point2D.Double end = new Point2D.Double((polyQ[2].x+polyQ[3].x)/2,
                 (polyQ[2].y+polyQ[3].y)/2);
 
     	/*polyQ = this.insertPointIntoPolygon(this.insertPointIntoPolygon(polyQ, start), end);
 
     	ShortestPath spCalculator = new ShortestPath(polyQ, start, end);
     	drawPanel.spPath = spCalculator.getPath();
 
 
     	ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
     	for (int i = 0; i < polyP.length; i++) {
     		Point2D.Double point = polyP[i];
     		points.add(new PolygonPoint(point.x, point.y));
     	}
 
     	Polygon poly = new Polygon(points);
     	Poly2Tri.triangulate(poly);
 
     	ArrayList<DelaunayTriangle> triangulation = new ArrayList<DelaunayTriangle>();
     	triangulation = (ArrayList<DelaunayTriangle>) poly.getTriangles();
 
     	drawPanel.triangulation = triangulation;    */
 
    	ReachabilityStructure graph = new ReachabilityStructure(polyP, polyQ, 5);
         Point2D.Double[] path = graph.getFirstReachablePath();
 
         System.out.println("Path: ");
         if (path != null)
         {
             for (Point2D.Double point : path) {
                 System.out.println(point);
             }
         }
 	}
 
 	private Point2D.Double[] insertPointIntoPolygon(Point2D.Double[] poly, Point2D.Double point) {
 		//Iterate through all edges
 		for (int i = 0; i < poly.length; i++) {
 			int j = i + 1;
 			if (j >= poly.length) {
 				j = 0;
 			}
 			//if distance from point to edge is small, insert point between the vertices making up that edge
 			double denominator = Math.sqrt((poly[j].x - poly[i].x)*(poly[j].x - poly[i].x) + (poly[j].y - poly[i].y)*(poly[j].y - poly[i].y));
 			double numerator = Math.abs((poly[j].x - poly[i].x)*(poly[i].y - point.y) - (poly[i].x - point.x)*(poly[j].y - poly[i].y));
 
 			Point2D.Double[] newPolygon = new Point2D.Double[poly.length + 1];
 			if (getZero(numerator/denominator) == 0) {
 				System.arraycopy(poly, 0, newPolygon, 0, j);
 				newPolygon[j] = point;
 				System.arraycopy(poly, j, newPolygon, j+1, poly.length-j);
 				return newPolygon;
 
 			}
 		}
 		return poly;
 	}
 
 	private double getZero(double x) {
 		double testPositiveZero = 0.00000000001;
 		double testNegativeZero = -0.00000000001;
 		if (x >= testNegativeZero && x <= testPositiveZero) {
 			x = 0;
 		}
 		return x;
 	}
 
 	private void addTestPolygons() {
 		polyP = new Point2D.Double[4];
 		polyP[0] = new Point2D.Double(-1, -1);
 		polyP[1] = new Point2D.Double(0, 1);
 		polyP[2] = new Point2D.Double(1, -1);
 		polyP[3] = new Point2D.Double(0, 0.6);
 
 		drawPanel.polyP = polyP;
 
 		polyQ = new Point2D.Double[4];
 		polyQ[0] = new Point2D.Double(-1, 1);
 		polyQ[1] = new Point2D.Double(0, -1);
 		polyQ[2] = new Point2D.Double(1, 1);
 		polyQ[3] = new Point2D.Double(0, -0.6);
 
     	drawPanel.polyQ = polyQ;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		PolygonInputFrame me = new PolygonInputFrame();
     	me.setDefaultCloseOperation(EXIT_ON_CLOSE);
     	me.setSize(400,400);
     	me.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e){
 
     }
 
 	public void mouseClicked(MouseEvent e) {
 
 	}
 }
 
 class ControlArea extends JPanel{
 	public ControlArea(){
 
 	}
 }
 
 class PlotArea extends JPanel{
 	public Point2D.Double[] polyP;
 	public Point2D.Double[] polyQ;
 	public Point2D.Double[] spPath;
 	private double xmin = -2;
 	private double xmax = 2;
 	private double ymin = -2;
 	private double ymax = 2;
 	public ArrayList<DelaunayTriangle> triangulation;
 
 	public PlotArea(){
 		super();
 		this.setSize(400, 400);
 	}
 	public void paint(Graphics g)
 	{
 		g.setColor(Color.blue);
 		drawPolygon(polyP, g);
 		g.setColor(Color.red);
 		drawPolygon(polyQ, g);
 		if (spPath != null) {
 			drawPath(spPath, g);
 		}
 		g.setColor(Color.orange);
 		if (triangulation != null) {
 			Iterator<DelaunayTriangle> itr = triangulation.iterator();
 			while(itr.hasNext()) {
 				DelaunayTriangle t = (DelaunayTriangle) itr.next();
 				Point2D.Double[] converted = {new Point2D.Double(t.points[0].getX(), t.points[0].getY()), new Point2D.Double(t.points[1].getX(), t.points[1].getY()), new Point2D.Double(t.points[2].getX(), t.points[2].getY())};
 	    	    drawPolygon(converted, g);
 	    	    System.out.println("Triangulation Triangle: " + Arrays.toString(converted));
 			}
 		}
     	super.paintComponents(g);
 	}
 
 	public void drawPolygon (Point2D.Double[] points, Graphics g) {
 		Point[] screenPoints = new Point[points.length];
 		for (int i = 0; i<points.length; i++) {
 			screenPoints[i] = new Point(toScreenX(points[i].x), toScreenY(points[i].y));
 		}
 		for (int i = 0; i<screenPoints.length; i++) {
 			int j = i + 1;
 			if (i == screenPoints.length - 1) {
 				j = 0;
 			}
 			g.drawLine(screenPoints[i].x, screenPoints[i].y, screenPoints[j].x, screenPoints[j].y);
 		}
 	}
 
 	public void drawPath(Point2D.Double[] points, Graphics g) {
 		g.setColor(Color.green);
 		for (int i = 0; i< points.length - 1; i++) {
 			g.drawLine(toScreenX(points[i].x), toScreenY(points[i].y), toScreenX(points[i+1].x), toScreenY(points[i+1].y));
 		}
 	}
 
 	public double toMathX(int x)
 	{
 	    return (x - (this.getWidth()) / 2) * ((xmax - xmin) / (this.getWidth())) + ((xmax + xmin) / 2);
 	}
 	public double toMathY(int y)
 	{
 	    return ((this.getHeight()) / 2 - y) * ((ymax - ymin) / (this.getHeight())) + ((ymax + ymin) / 2);
 	}
 	public int toScreenX(double x)
 	{
 	    return (int)((this.getWidth() / 2) + (x - (xmax + xmin) / 2) * (this.getWidth() / (xmax - xmin)));
 	}
 	public int toScreenY(double y)
 	{
 	    return (int)((this.getHeight() / 2) - (y - (ymax + ymin) / 2) * (this.getHeight() / (ymax - ymin)));
 	}
 }
