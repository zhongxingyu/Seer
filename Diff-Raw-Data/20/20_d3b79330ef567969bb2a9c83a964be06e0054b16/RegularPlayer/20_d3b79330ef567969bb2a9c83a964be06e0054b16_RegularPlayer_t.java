 package platformer.core.model.systems.impl.physics.bodies;
 
 import platformer.core.model.systems.PhysicsBody;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class RegularPlayer implements PhysicsBody {
 	public Body create(World world, Vector3 position, Rectangle bounds) {
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(position.x, position.y);
 
 		Body body = world.createBody(bodyDef);
 		body.setFixedRotation(true);
 		body.setBullet(true);
 
 		float quarterSize = bounds.height / 4;
 		
 		CircleShape circShape = new CircleShape();
 		circShape.setRadius(quarterSize);
 		circShape.setPosition(new Vector2(0, quarterSize));;
 		
 		Fixture head = body.createFixture(circShape, 1);
 		head.setRestitution(0);
 		head.setFriction(0);
 		
 		circShape.setPosition(new Vector2(0, -(bounds.height / 4)));
 		
		Fixture legs = body.createFixture(circShape, 1);
 		head.setRestitution(0);
		head.setFriction(0.2f);
 		
 		return body;
 	}
 	
 }
