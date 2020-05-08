 /**
  * 
  */
 package entities;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Point;
 
 import config.Config;
 
 /**
  * Base class for in-game objects and agents. Handles physics updates
  * @author Mullings
  * 
  */
 public abstract class Entity extends Sprite {
 	private PolygonShape physicsShape;
 	private BodyDef physicsDef;
 	private FixtureDef physicsFixture;
 	private Body physicsBody;
 
 	public Entity(float x, float y, float width, float height) {
 		super(x, y, width, height);
 		physicsDef = new BodyDef();
 		physicsDef.type = BodyType.DYNAMIC;
 		physicsDef.position.set(x / Config.PIXELS_PER_METER,
 		                        y / Config.PIXELS_PER_METER);
 		
 		physicsShape = new PolygonShape();
 		physicsShape.setAsBox(width / 2 / Config.PIXELS_PER_METER,
 		                     height / 2 / Config.PIXELS_PER_METER);
 		
 		physicsFixture = new FixtureDef();
 		physicsFixture.shape = physicsShape;
 		physicsFixture.density = Config.DEFAULT_DENSITY;
 		physicsFixture.friction = Config.DEFAULT_FRICTION;
 	}
 	
 	@Override
 	public abstract void render(Graphics graphics);
 
 	@Override
 	public abstract void update();
 	
 	public final void addToWorld(World world) {
 		physicsBody = world.createBody(physicsDef);
 		physicsBody.createFixture(physicsFixture);
 	}
 	
 	@Override
 	public final Point getPosition() {
 		if (physicsBody == null) return super.getPosition();
 		Vec2 v = physicsBody.getPosition();
 		return new Point(v.x * Config.PIXELS_PER_METER,
 		                 v.y * Config.PIXELS_PER_METER);
 	}
 	
 	@Override
 	public final void setPosition(float x, float y) {
 		if (physicsBody == null) {
			super.getPosition();
 			return;
 		}
 		physicsBody.setTransform(new Vec2(x / Config.PIXELS_PER_METER,
 		                                  y / Config.PIXELS_PER_METER), 0);
 	}
 	
 	public final Body getPhysicsBody() {
 		return physicsBody;
 	}
 	
 	public final BodyDef getPhysicsBodyDef() {
 		return physicsDef;
 	}
 
 }
