 package com.warrenfalk.jbox2d;
 
 import java.util.LinkedList;
 
 import org.jbox2d.callbacks.RayCastCallback;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.joints.Joint;
 import org.jbox2d.dynamics.joints.RevoluteJointDef;
 import org.jbox2d.dynamics.joints.RopeJointDef;
 import org.jbox2d.testbed.framework.TestbedTest;
 
 public class RopeTest extends TestbedTest {
 	
 	class Rope {
 		public LinkedList<Body> segments = new LinkedList<Body>();
 		public LinkedList<Joint> joints = new LinkedList<Joint>();
 	}
 	
 	Rope rope;
 	float thickness = 0.2f;
 	float resolution = 0.6f;
 	Body base;
 	Body weight;
 	
 	final static int ROPE = 0x2; // ropes collide with the world, but ropes don't collide with ropes
 
 	@Override
 	public void initTest(boolean deserialized) {
 		setTitle("Rope test");
 		
 		rope = null;
 		
 		getWorld().setGravity(new Vec2(0, -10f));
 		
 		PolygonShape baseShape = new PolygonShape();
 		baseShape.setAsBox(4f, 2f);
 		PolygonShape weightShape = new PolygonShape();
 		weightShape.setAsBox(3f, 1f);
 		
 		BodyDef fixed = new BodyDef();
 		fixed.type = BodyType.STATIC;
 		
 		BodyDef dynamic = new BodyDef();
 		dynamic.type = BodyType.DYNAMIC;
 		
 		FixtureDef fd = new FixtureDef();
 		fd.shape = baseShape;
 		fd.density = 1.0f;
 		fd.filter.maskBits = 0; // doesn't collide with anything
 
 		fixed.position.set(0, 0);
 		base = m_world.createBody(fixed);
 		base.createFixture(fd);
 		
 		fixed.position.set(0, -5);
 		Body obstacle = getWorld().createBody(fixed);
 		fd.filter.maskBits = 0xFFFF; // collide with everything
 		obstacle.createFixture(fd);
 		
 		dynamic.position = new Vec2(-20f, 19f);
 		weight = getWorld().createBody(dynamic);
 		fd.shape = weightShape;
 		fd.density = 20f;
 		fd.filter.maskBits = 0xFFFF & ~ROPE;
 		fd.filter.categoryBits = ROPE; // the thing on the end of the rope collides like the rope
 		weight.createFixture(fd);
 		
 	}
 	
 	@Override
 	public void keyPressed(char keyChar, int keyCode) {
 		switch (keyChar) {
 		case 'j':
 			if (rope == null) {
 				//rope = createRope(getWorld(), thickness, resolution, base, weight, new Vec2(0, 0), weight.getWorldPoint(new Vec2(3f, 0f)));
 				Vec2 localFrom = new Vec2(3f, 0f);
 				Vec2 worldFrom = weight.getWorldPoint(localFrom);
 				Vec2 ray = new Vec2(0, 0).sub(worldFrom);
 				float angle = (float)Math.atan2(ray.y, ray.x);
 				rope = shootRope(getWorld(), null, thickness, resolution, weight, localFrom, angle);
 			}
 			else {
 				for (Joint joint : rope.joints)
 					if (joint != null)
 						getWorld().destroyJoint(joint);
 				for (Body segment : rope.segments)
 					getWorld().destroyBody(segment);
 				rope = null;
 			}
 		}
 	}
 	
 	static class RopeRaycast implements RayCastCallback {
 		Fixture fixture;
 		Vec2 strikePoint;
 		Vec2 normal;
 		float fraction;
 		
		public RopeRaycast(World world, Vec2 from, float angle, float maxLength) {
 			Vec2 to = from.add(new Vec2((float)Math.cos(angle), (float)Math.sin(angle)).mul(17f));
 			world.raycast(this, from, to);
 			if (strikePoint == null)
 				strikePoint = to;
 		}
 
 		@Override
 		public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
 			this.fixture = fixture;
 			this.strikePoint = point;
 			this.normal = normal;
 			this.fraction = fraction;
 			return fraction;
 		}
 	}
 	
 	private Rope shootRope(World world, Rope rope, float thickness, float resolution, Body fromBody, Vec2 localFrom, float angle) {
 		// for now this is instant, find a place for the rope to strike, then strike there.
 		// find a strike point for the rope
 		Vec2 from = fromBody.getWorldPoint(localFrom);
		RopeRaycast rrc = new RopeRaycast(world, from, angle, 60f);
 		
		return createRope(world, thickness, resolution, fromBody, rrc.fixture.getBody(), from, rrc.strikePoint);
 	}
 	
 	private Rope createRope(World world, float thickness, float resolution, Body fromBody, Body toBody, Vec2 from, Vec2 to) {
 		Rope rope = new Rope();
 		
 		Vec2 vector = to.sub(from);
 		float length = vector.length();
 		int count = (int)Math.floor(length / resolution);
 		resolution = length / (float)count;
 		
 		Vec2 direction = new Vec2(vector);
 		direction.normalize();
 		
 		float angle = (float)Math.atan2(direction.y, direction.x);
 		
 		PolygonShape segmentShape = new PolygonShape();
 		segmentShape.setAsBox(resolution / 2f, thickness / 2f);
 		
 		BodyDef segmentBodyDef = new BodyDef();
 		segmentBodyDef.type = BodyType.DYNAMIC;
 		segmentBodyDef.linearDamping = 0.999f;
 		segmentBodyDef.angularDamping = 0.999f;
 		segmentBodyDef.angle = angle;
 
 		FixtureDef segmentFixture = new FixtureDef();
 		segmentFixture.shape = segmentShape;
 		segmentFixture.restitution = 0f;
 		segmentFixture.density = 9f;
 		segmentFixture.filter.categoryBits = ROPE;
 		segmentFixture.filter.maskBits = 0xFFFF & ~ROPE; // collide with anything, except other ropes
 		
 		int i;
 		Body prev = fromBody;
 		for (i = 0; i < count; i++) {
 			segmentBodyDef.position = from.add(direction.mul(resolution * (i + 0.5f)));
 			
 			Body segment = world.createBody(segmentBodyDef);
 			rope.segments.addLast(segment);
 			segment.createFixture(segmentFixture);
 			
 			RevoluteJointDef jd = new RevoluteJointDef();
 			jd.initialize(segment, prev, from.add(direction.mul(resolution * i)));
 			jd.collideConnected = false;
 			rope.joints.addLast(world.createJoint(jd));
 			
 			if (i % 2 == 1) {
 				// create a rope joint from each body to every other segment
 				// necessary to prevent stretching even when middle of rope is obstructed
 				RopeJointDef rj = new RopeJointDef();
 				rj.bodyA = segment;
 				rj.localAnchorA.set(0, 0);
 				rj.collideConnected = false;
 				
 				rj.bodyB = fromBody;
 				rj.localAnchorB.set(fromBody.getLocalPoint(from));
 				rj.maxLength = rj.bodyA.getWorldPoint(rj.localAnchorA).sub(from).length();
 				rope.joints.addLast(world.createJoint(rj));
 				
 				rj.bodyB = toBody;
 				rj.localAnchorB.set(toBody.getLocalPoint(to));
 				rj.maxLength = rj.bodyA.getWorldPoint(rj.localAnchorA).sub(to).length();
 				rope.joints.addLast(world.createJoint(rj));
 			}
 			
 			prev = segment;
 		}
 		
 		RevoluteJointDef jd = new RevoluteJointDef();
 		jd.initialize(toBody, prev, from.add(direction.mul(resolution * i)));
 		jd.collideConnected = false;
 		rope.joints.add(world.createJoint(jd));
 		
 		// create the final rope joint between the from and to
 		RopeJointDef rj = new RopeJointDef();
 		rj.bodyA = fromBody;
 		rj.bodyB = toBody;
 		rj.localAnchorA.set(fromBody.getLocalPoint(from));
 		rj.localAnchorB.set(toBody.getLocalPoint(to));
 		rj.maxLength = from.sub(to).length();
 		rj.collideConnected = false;
 		rope.joints.add(world.createJoint(rj));
 		
 		return rope;
 	}
 
 	@Override
 	public String getTestName() {
 		return "Rope";
 	}
 
 }
