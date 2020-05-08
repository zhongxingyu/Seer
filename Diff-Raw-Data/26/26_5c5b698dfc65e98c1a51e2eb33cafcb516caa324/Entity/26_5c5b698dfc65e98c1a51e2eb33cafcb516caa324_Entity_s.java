 package Entities;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Shape;
 
 public class Entity {
 	
 	public final static int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
 	private Body body;
	private float force = 100000.f;
 	
 
 	Image img;
 	Shape sh;
 	String name;
 	
 	public Entity(int x, int y, String name, Image img, World world){
 		this.name = name;
 		this.img = img;
 		BodyDef bd = new BodyDef();
 		bd.position = new Vec2(x+50, y+50);
 		
 		bd.type = BodyType.DYNAMIC;
 		CircleShape cs = new CircleShape();
 		cs.m_radius = 0.5f;
 		FixtureDef fd = new FixtureDef();
 		fd.shape = cs;
 		fd.density = 0.5f;
 		fd.friction = 0.3f;        
 		fd.restitution = 0.5f;
 		
 		body = world.createBody(bd);
 		body.createFixture(fd);
 		body.setLinearDamping(0.0015f);
 	}
 	public Entity(int x, int y, String name, Shape sh, World world){
 		this.name = name;
 		this.sh = sh;
 		BodyDef bd = new BodyDef();
 		bd.position = new Vec2(x+50, y+50);
 		
 		bd.type = BodyType.DYNAMIC;
 		CircleShape cs = new CircleShape();
 		cs.m_radius = 0.5f;
 		FixtureDef fd = new FixtureDef();
 		fd.shape = cs;
 		fd.density = 0.5f;
 		fd.friction = 0.3f;        
 		fd.restitution = 0.5f;
 		
 		body = world.createBody(bd);
 		body.createFixture(fd);
 		body.setLinearDamping(0.0015f);
 	}
 
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void draw(Graphics arg1){
 		Vec2 pos = body.getPosition();
		System.out.println("Draw");
 		if(img != null){
 			System.out.println("img draw");
 			img.draw(pos.x , pos.y);
 		}else if(sh != null){
 			sh.setLocation(pos.x, pos.y);
 			arg1.draw(sh);
			System.out.println("X: " + pos.x + " Y: " + pos.y);
 		}
 		
 	}
 	
 	public Vec2 getPos() {
 		return body.getPosition();
 	}
 	
 	public void addForce(int[] acc, int ms){
 		//System.out.println(acc[0] + " " + acc[1] + " " + acc[2] + " " + acc[3]);
 		//System.out.println(body.getMass() + " ");
 		Vec2 f = new Vec2( (force * ms) * (acc[3]-acc[2]), (force * ms) * (acc[1] - acc[0]));
		//System.out.println("x: " + f.x + " y: " + f.y);
 		Vec2 curr_vec = body.getLinearVelocity();
		body.applyLinearImpulse(f, getPos());
 	}
 	
 }
