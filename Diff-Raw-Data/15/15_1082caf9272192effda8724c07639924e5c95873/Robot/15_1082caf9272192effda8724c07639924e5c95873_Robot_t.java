 import pbox2d.*;
 import org.jbox2d.collision.shapes.*;
 import org.jbox2d.common.*;
 import org.jbox2d.dynamics.*;
 
 public class Robot {
 	public PBox2D box2d;
 	public Body body;
 	protected float w, h;
 	protected Motor leftMotor, rightMotor;
 
 	public Robot(PBox2D b2d, float x, float y) {
 		box2d = b2d;
 
 		w = 16;
 		h = 16;
 
 		leftMotor = new Motor(0, -h/2);
 		rightMotor = new Motor(0, h/2);
 
 		//make the robot's body and add to Box2D world
 		makeBody(new Vec2(x, y), w, h);
 	}
 
 	//remove from world
 	void killBody() {
 		box2d.destroyBody(body);
 	}
 
 	//check if particle is ready to be removed
 	boolean done() {
 		//we're a robot so we don't magically disappear
 		return false;
 	}
 
 	public void update() {
 		leftMotor.affect(body);
 		rightMotor.affect(body);	
 	}
 
 	public void left(float val) {
 			leftMotor.setSpeed(val);
 	}
 
 	public void right(float val) {
 			rightMotor.setSpeed(val);
 	}
 
 	private void makeBody(Vec2 center, float _w, float _h) {
 		// Define a polygon (this is what we use for a rectangle)
 		PolygonShape sd = new PolygonShape();
 		float box2dW = box2d.scalarPixelsToWorld(_w/2);
 		float box2dH = box2d.scalarPixelsToWorld(_h/2);
 		sd.setAsBox(box2dW, box2dH);
 
 		// Define a fixture
 		FixtureDef fd = new FixtureDef();
 		fd.shape = sd;
 		// Parameters that affect physics
 		fd.density = 1;
 		fd.friction = 0.3f;
 		fd.restitution = 0.5f;
 
 		// Define the body and make it from the shape
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set(box2d.coordPixelsToWorld(center));
 
 		body = box2d.createBody(bd);
 		body.createFixture(fd);
		body.setLinearDamping(0.1f);
		body.setAngularDamping(0.5f);
 	}
 
 	public float getWidth() { return w; }
 	public float getHeight() { return h; }
 
 	//really models a motor connected to a wheel
 	//directly impacts movement
 	class Motor {
 		private float x, y;
 		private float torque, maxspeed;
 		private float speed;
 
 		public Motor(float _x, float _y) {
 			x = _x;
 			y = _y;
 
 			torque = 1;
 			maxspeed = 4;
 			speed = 0;
 		}
 
 		public void affect(Body b) {
 			if(speed>0.001) {
				/*float a = b.getAngle();
 				Vec2 force = new Vec2((float)(speed*Math.cos(a)), (float)(speed*Math.sin(a)));
 
 				float rotx = (float)(x*Math.cos(a)-y*Math.sin(a));
 				float roty = (float)(x*Math.sin(a)+y*Math.cos(a));
				//if(y<0) System.out.println(force.x+", "+force.y);
				Vec2 point = new Vec2(b.getWorldCenter().x+rotx, b.getWorldCenter().y+roty);
				b.applyForce(force, point);*/

				Vec2 force = b.getWorldVector(new Vec2(1, 0));
				Vec2 point = b.getWorldPoint(new Vec2(x, y));
 				b.applyForce(force, point);
 			}
 		}
 
 		public void setSpeed(float val) {
 			if(Math.abs(val)<maxspeed)
 				speed = val;
 		}
 
 		public float getTorque() { return torque; }
 		public float getMaxSpeed() { return maxspeed; }
 	}
 }
