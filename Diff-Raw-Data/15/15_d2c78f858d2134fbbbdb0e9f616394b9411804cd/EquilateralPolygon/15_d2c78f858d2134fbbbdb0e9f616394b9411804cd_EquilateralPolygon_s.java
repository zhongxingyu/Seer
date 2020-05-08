 package engine.physics.objects;
 
import java.awt.geom.Rectangle2D;
 import java.util.Vector;
 
import src.RenderDisplay;
 
 import engine.helper.MathHelper;
 import engine.physics.FunctionLinear;
 import engine.render.Color;
 import engine.render.Point2D;
 import engine.render.RenderEngine;
 import engine.render.Triangle2D;
 
 public class EquilateralPolygon extends PhysicalObject{
 	
 	private int corners;
 	private float radius;
 	private Color color;
 	private boolean isAffectedByGlobalForces;
 	private Vector<Point2D> cornersPoints;
 	private Vector<Triangle2D> triangles;
 	private Point2D p1;
 	private Point2D middlePoint;
 
 	
 	public EquilateralPolygon(int corners, float radius, Color color){
 		this(corners, radius, 0.0f, color);
 	}
 	
 	public EquilateralPolygon(int corners, float radius, float weight, Color color){
 		this(corners, radius, weight, color, true);
 	}
 	
 	public EquilateralPolygon(int corners, float radius, float weight, Color color, boolean isAffectedByGlobalForces){
 		super(weight);
 		this.color = color;
 		this.isAffectedByGlobalForces = isAffectedByGlobalForces;
 		this.radius = radius;
 		this.corners = corners;
 		calculateCorners();
 		calculateTriangles();
 	}
 
 	private void calculateCorners() {
 		cornersPoints = new Vector<Point2D>();
 		float angleToRotate = 180-MathHelper.getInteriorAngleSumOfAnEquilateralPolygon(corners)/corners;
 		p1 = new Point2D(0,radius);
 		cornersPoints.add(p1);
 		
		System.out.println("Ecke: P0(" + 0 + "/" + radius + ")");
 		
 		middlePoint = new Point2D(p1.x, p1.y-radius);
 		for(int i = 1; i < corners; i++){
 			Point2D cornerToAdd = MathHelper.getPointRotatedAroundAnotherPoint(	cornersPoints.elementAt(i-1), middlePoint, angleToRotate);
 			cornersPoints.add(cornerToAdd);
 		}
 	}
 
 	@Override
 	public void render() {
 		RenderEngine.setColor(color);
 		for(int i = 0; i < triangles.size(); i++){
 			RenderEngine.drawTriangle(triangles.elementAt(i));
 		}
 	
 	}
 
 	@Override
 	public Vector<Point2D> getCorners() {
 		return cornersPoints;
 	}
 
 	@Override
 	public Vector<FunctionLinear> getSidesFromCorners(Vector<Point2D> corners) {
 		Vector<FunctionLinear> sides = new Vector<FunctionLinear>();
 		
 		for(int i = 0; i < this.corners-1; i++){
 			sides.add(MathHelper.getLinearFunctionWithLimitation(cornersPoints.get(i), cornersPoints.get(i+1)));
 		}
 		sides.add(MathHelper.getLinearFunctionWithLimitation(cornersPoints.lastElement(), cornersPoints.firstElement()));
 		return sides;
 	}
 	
 	private void calculateTriangles(){
 		triangles = new Vector<Triangle2D>();
 		for(int i = 0; i < corners-1; i++){
 			triangles.add(new Triangle2D(middlePoint, cornersPoints.get(i), cornersPoints.get(i+1)));
 		}
 			triangles.add(new Triangle2D(middlePoint, cornersPoints.lastElement(), cornersPoints.firstElement()));
 	}
 	
 }
