 package platformer.core.model.systems.impl.physics.bodies;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 import platformer.core.model.systems.PhysicsBody;
 
 public class Square implements PhysicsBody {
 
 	@Override
 	public Body create(World world, Vector3 position, Rectangle bounds) {
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.StaticBody;
 		bodyDef.position.set(position.x, position.y);
 
 		Body body = world.createBody(bodyDef);
 
 		PolygonShape tileShape = new PolygonShape();
 		tileShape.setAsBox(bounds.width / 2, bounds.height / 2);
 		
 		Fixture fixture = body.createFixture(tileShape, 70);
		fixture.setFriction(0.8f);
		fixture.setRestitution(0.25f);
 		
 		return body;
 	}
 
 }
