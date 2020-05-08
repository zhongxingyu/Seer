 package cx.it.hyperbadger.spacezombies.explorer;
 
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glVertex2d;
 
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 import cx.it.hyperbadger.spacezombies.Game;
 import cx.it.hyperbadger.spacezombies.TextureName;
 import cx.it.hyperbadger.spacezombies.Vector2d;
 
 public class Ship extends Mass implements Drawable, Collidable, Moveable{
 	private TextureName textureName = null;
 	private double rotation = 0.0;
 	private boolean first = true;
 	private int h,w;
 	private double force = 100000000000.0;
 	private Vector2d velocity = new Vector2d();
 	private boolean auto = false;
 	private double autoVelocity = 50;
 	private ArrayList<Mass> attracts = null;
 	public Ship(int mass, double x, double y, String textureName){
 		super(mass,x,y,"Ship");
 		this.textureName = new TextureName(textureName);
 		this.textureName.loadTexture();
 		velocity.set(0,0);
 	}
 	@Override
 	public void draw() {
 		//calculateAngle();
 		//if(this.scale<0.6){
 		//	this.scale = 0.6;
 		//}
 		calculateAngle();
 		//GL11.glPushMatrix();
 		textureName.getTexture().bind();
 		GL11.glTranslated((Display.getWidth()/2), (Display.getHeight()/2), 0);
 		GL11.glRotated(rotation, 0f, 0f, 1f);
 		GL11.glTranslated(-(Display.getWidth()/2), -(Display.getHeight()/2), 0);
 		h=30;
 		w=60;
 		glBegin(GL_QUADS);
 		glTexCoord2f(0,0);
 		glVertex2d((Display.getWidth()/2-w/2),(Display.getHeight()/2-h/2)); //topleft
 		glTexCoord2f(1,0);
 		glVertex2d((Display.getWidth()/2+w/2),(Display.getHeight()/2-h/2)); //top right
 		glTexCoord2f(1,1);
 		glVertex2d((Display.getWidth()/2+w/2),(Display.getHeight()/2+h/2)); //bottom right
 		glTexCoord2f(0,1);
 		glVertex2d((Display.getWidth()/2-w/2),(Display.getHeight()/2+h/2)); //bottom left
 		glEnd();
 		//GL11.glPopMatrix();/*
 		GL11.glTranslated((Display.getWidth()/2), (Display.getHeight()/2), 0);
 		GL11.glRotated(-rotation, 0f, 0f, 1f);
 		GL11.glTranslated(-(Display.getWidth()/2), -(Display.getHeight()/2), 0);
 	}
 	public void applyForce(Vector2d force){
 		calculateRelativity();
 		velocity.x = velocity.x + (force.x*Game.delta/1000)/this.mass;
 		velocity.y = velocity.y + (force.y*Game.delta/1000)/this.mass;
 		calculateRelativity();
 	}
 
 	public void processCollisions(ArrayList<Collidable> toCheck){
 		for(Collidable c: toCheck){
 			if(c.collisionCheck(this)){
 				this.collision(c);
 				c.collision(this);
 			}
 		}
 	}
 	public void setAttracts(ArrayList<Mass> a){
 		this.attracts = a;
 	}
 	@Override
 	public void move(){
 		if(attracts!=null){
 			Vector2d planetForce = new Vector2d(0,0);
 			ArrayList<Collidable> a = new ArrayList<Collidable>();
 			for(Mass m: attracts){
 				if(m!=this){
 					Vector2d.add(planetForce, getVectorForce(this,m), planetForce);
 					a.add(m);
 				}
 			}
 			//we dont apply force initially, shit fucks up
 			if(!first){
 				this.applyForce(planetForce);
 			}
 			this.processCollisions(a);
 		}
 		first = false;
 		if(auto){
 			Vector2d a = new Vector2d(0,0);
 			double speed = velocity.length();
 			double over = (autoVelocity - speed)*force/1000;
 			if(over>force/1000){
 				over = force;
 			}
 			if(over<-force/1000){
 				over = -force;
 			}
 			a = velocity.unitVector();
 			a.scale(over);
 			applyForce(a);
 		}
 		//
 		if(velocity.length()>(2.997*Math.pow(10,8))){
 			velocity = velocity.unitVector();
 			velocity = velocity.scale(2.997*Math.pow(10,8));
 		}
 		this.setX(this.x + velocity.x*Game.delta/1000);
 		this.setY(this.y + velocity.y*Game.delta/1000);
 	}
 	private void calculateRelativity(){
 		//calculate relativity
 		//mass = originalMass/Math.sqrt(1-Math.pow(velocity.length()/Game.C,2));
 	}
 	private Vector2d getVectorForce(Mass from, Mass to){
 		//from ship
 		//to planet
 		Vector2d ship = from.getLocation();
 		Vector2d mass = to.getLocation();
 		Vector2d force = new Vector2d();
 		Vector2d.sub(mass, ship, force);
 		Vector2d unitVector = new Vector2d();
 		unitVector = force.unitVector();
 		double magnitude = this.getForceMagnitude(to);
 		Vector2d magVec = unitVector.scale(magnitude);
 		return magVec;
 	}
 	private void calculateAngle(){
 		Vector2d velocityUnit = new Vector2d();
 		double velocityMag = Math.sqrt(Math.pow(velocity.x, 2)+Math.pow(velocity.y, 2));
 		if(velocityMag!=0){
 			velocityUnit.set(velocity.x/velocityMag,velocity.y/velocityMag);
 			rotation = velocityUnit.getDegrees()+180;
 		}
 	}
 	public double getVelocity(){
 		return velocity.length();
 	}
 	@Override
 	public void setTexture(TextureName t) {
 		textureName = t;
 	}
 	@Override
 	public TextureName getTexture() {
 		return textureName;
 	}
 	public double getForce() {
 		return force;
 	}
 	public void setForce(double force) {
 		this.force = force;
 	}
 	public boolean isAuto() {
 		return auto;
 	}
 	public void setAuto(boolean auto) {
 		this.auto = auto;
 	}
 	public double getAutoVelocity() {
 		return autoVelocity;
 	}
 	public void setAutoVelocity(double autoVelocity) {
 		this.autoVelocity = autoVelocity;
 	}
 	@Override
 	public double getCollisionRadius() {
 		return (h+w)/4;
 	}
 	@Override
 	public void collision(Collidable object) {
 		System.out.println(this.getName()+" has collided with "+object.getName());
 	}
 }
