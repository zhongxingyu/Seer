 package engine.physics.objects;
 
 import java.util.Vector;
 
 import engine.helper.MathHelper;
 import engine.physics.FunctionLinear;
 import engine.render.Color;
 import engine.render.Point2D;
 import engine.render.Rectangle2D;
 import engine.render.RenderEngine;
 
 
 public class Square extends PhysicalObject {
 	
 	private Rectangle2D rect;
 	private float rotation = 0.0f;
 	private Color color;
 	private Point2D middlePoint;
 	private boolean isAffectedByGravity;
 	
 	public Square(float length, Color color) {
 		this(length, 0.0f, color);
 	}
 	
 	public Square(float length, Color color, boolean isAffectedByGravity) {
 		this(length, 0.0f, color, isAffectedByGravity);
 	}
 	
 	public Square(float length, float weight, Color color) {
 		this(length, weight, color, true);
 	}
 	
 	public Square(float length, float weight, Color color, boolean isAffectedByGravity) {
 		super(weight);
 		float position = length/2.0f;
 		middlePoint = new Point2D(position, position);
 		this.rect = new Rectangle2D(new Point2D(-position, position), length, length);
 		this.color = color;
 		this.isAffectedByGravity = isAffectedByGravity;
 	}
 
 	@Override
 	public void rotateZ(float angle) {
 		this.rotation = angle;
 	}
 
 	@Override
 	public void render() {
 		RenderEngine.setColor(color);
 		RenderEngine.rotateZ(rotation);
 		RenderEngine.drawRectangle(rect);
 	}
 
 	@Override
 	public Vector<Point2D> getCorners() {
 		Vector<Point2D> corners = new Vector<Point2D>();
 		corners.add(MathHelper.getPointRotated(rect.topLeft, rotation));
 		corners.add(MathHelper.getPointRotated(rect.getTopRight(), rotation));
 		corners.add(MathHelper.getPointRotated(rect.getBottomLeft(), rotation));
 		corners.add(MathHelper.getPointRotated(rect.getBottomRight(), rotation));
 		return corners;
 	}
 
 	@Override
 	public Point2D getMiddlePoint() {
 		return middlePoint;
 	}
 	
 	@Override
 	public boolean isAffectedByGravity() {
 		return isAffectedByGravity;
 	}
 
 	@Override
 	public float getRotation() {
 		return rotation;
 	}
 
 	@Override
 	public Vector<FunctionLinear> getSides() {
 		Vector<FunctionLinear> sides = new Vector<FunctionLinear>();
		sides.add(MathHelper.getLinearFunction(rect.topLeft, rect.getTopRight()));
		sides.add(MathHelper.getLinearFunction(rect.getTopRight(), rect.getBottomRight()));
		sides.add(MathHelper.getLinearFunction(rect.getBottomRight(), rect.getBottomLeft()));
		sides.add(MathHelper.getLinearFunction(rect.getBottomLeft(), rect.topLeft));
 		return sides;
 	}
 	
 }
