 package entities.ships;
 
 import game.Variables;
 
 import java.io.IOException;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.joints.DistanceJointDef;
 import org.jbox2d.dynamics.joints.Joint;
 
 public class Player extends Ship {
 	
 	public Player() throws IOException {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void compute() {
 		Vec2 position = getScreenPostion();
 		if(position.y>Variables.SCREEN_HEIGHT/3){
			setVelocity(0, 0);
 		}		
 	}
 
 	@Override
 	protected String getImageURL() {
 		return "images/ship.png";
 	}
 
 	
 	
 	@Override
 	public void init(World world, float x, float y){
 		super.init(world, Variables.SCREEN_WIDTH/2, Variables.SCREEN_HEIGHT/5);
 		body.setFixedRotation(true);
 		
 		/*
 		Joint joint;
 		Body ground = world.createBody(new BodyDef());
 		
 		DistanceJointDef jd = new DistanceJointDef();
 		Vec2 p1 = new Vec2();
 		Vec2 p2 = new Vec2();
 		Vec2 d = new Vec2();
 		
 		jd.frequencyHz = Variables.LINK_FREQUENCY;
 		jd.dampingRatio = Variables.LINK_DAMPING;
 		
 		jd.bodyA = ground;
 		jd.bodyB = body;
 		jd.localAnchorA.set(toWorldSize(Variables.SCREEN_WIDTH/2), toWorldSize(Variables.SCREEN_HEIGHT/5));
 		jd.localAnchorB.set(0,0);
 		p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
 		p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
 		d = p2.sub(p1);
 		jd.length = 0;//d.length();
 		joint = world.createJoint(jd);
 		*/
 		
 	}
 }
