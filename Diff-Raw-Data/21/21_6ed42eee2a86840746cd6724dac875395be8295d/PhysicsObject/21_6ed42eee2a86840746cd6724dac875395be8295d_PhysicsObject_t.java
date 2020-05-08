 package co.devrandom.model.objects;
 
 import java.util.ArrayList;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.collision.shapes.ShapeType;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.FixtureDef;
 
 import co.devrandom.main.GameState;
 import co.devrandom.model.Model;
 import co.devrandom.util.Vector;
 import co.devrandom.vc.view.TextureAttributes;
 
 public class PhysicsObject {
 	private Model model;
 	private TextureAttributes texAttributes;
 	private BodyDef bd;
 	private Body body;
 	private ArrayList<Shape> shapeList;
 
 	public PhysicsObject(Model model, BodyDef bd, FixtureDef[] fixtures, TextureAttributes texAttributes) {
 		this.model = model;
 
 		body = model.getWorld().createBody(bd);
 		
 		Vector c1 = new Vector(Float.MAX_VALUE, Float.MAX_VALUE);
 		Vector c2 = new Vector(Float.MIN_VALUE, Float.MIN_VALUE);
 		
 		shapeList = new ArrayList<Shape>();
 		
 		for (FixtureDef fd : fixtures) {
 			body.createFixture(fd);
 			shapeList.add(fd.shape);
 			Vector[] corners = getCorners(fd.shape);
 			c1.x = Math.min(corners[0].x, c1.x);
 			c1.y = Math.min(corners[0].y, c1.y);
 			c2.x = Math.max(corners[1].x, c2.x);
 			c2.y = Math.max(corners[1].y, c2.y);
 		}
 		
 		this.texAttributes = texAttributes;
 		texAttributes.setSize(c2.minus(c1).scale(GameState.SCALE));
 	}
 	
 	public Vec2[] getVertices() {
		Vec2[] vertices = ((PolygonShape) body.getFixtureList().getShape()).getVertices();
		// Why the hell do I need to do this.
		Vec2[] actualVertices = new Vec2[vertices.length / 2];
		for (int i = 0; i < actualVertices.length; i++)
			actualVertices[i] = vertices[i];
		return actualVertices;
 	}
 
 	public PhysicsObject(Model model, BodyDef bd, FixtureDef fixtures, TextureAttributes texAttributes) {
 		this(model, bd, new FixtureDef[] {fixtures}, texAttributes);
 	}
 
 	public TextureAttributes getTexAttributes() {
 		return texAttributes;
 	}
 
 	/**
 	 * Translates Box2D coordinates to on screen coordinates using
 	 * GameState.SCALE
 	 */
 	public Vector getPosition() {
 		Vec2 position = body.getPosition();
 		return new Vector(position.x, position.y).scale(GameState.SCALE);
 	}
 
 	public ArrayList<Shape> getShapeList() {
 		return shapeList;
 	}
 	
 	private Vector[] getCorners(Shape shape) {
 		if (shape.m_type == ShapeType.POLYGON) {
 			// If the shape is a polygon, find the minimum bounding box.
 			PolygonShape polyShape = (PolygonShape) shape;
 			
 			Vec2 vertex0 = polyShape.getVertex(0);
 			Vector c1 = new Vector(vertex0.x, vertex0.y);
 			Vector c2 = new Vector(c1);
 
 			for (Vec2 vertex : polyShape.getVertices()) {
 				c1.x = Math.min(c1.x, vertex.x);
 				c2.x = Math.max(c2.x, vertex.x);
 				c1.y = Math.min(c1.y, vertex.y);
 				c2.y = Math.max(c2.y, vertex.y);
 			}
 
 			return new Vector[] {c1, c2};
 		} else {
 			float rad = shape.getRadius();
 			return new Vector[] { new Vector(-rad, -rad), new Vector(rad, rad) };
 		}
 	}
 
 	public Model getModel() {
 		return this.model;
 	}
 
 	public float getRotation() {
 		return (float) (body.getAngle() * 180 / Math.PI);
 	}
 	
 	public Body getBody() {
 		return body;
 	}
 
 	public BodyDef getBD() {
 		return bd;
 	}
 
 	/**
 	 * Creates a new box with the specified width and height
 	 */
 	public static Shape makeBoxShape(Vector size) {
 		PolygonShape shape = new PolygonShape();
 		shape.setAsBox(size.x, size.y);
 		return shape;
 	}
 
 	/**
 	 * Creates a new box with the specified width and height
 	 */
 	public static Shape makeCircle(float r) {
 		CircleShape shape = new CircleShape();
 		shape.m_radius = r;
 		return shape;
 	}
 }
