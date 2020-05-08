 package cx.it.hyperbadger.spacezombies.explorer;
 
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import cx.it.hyperbadger.spacezombies.Game;
 
 public class Ship extends Mass implements Drawable{
 	private Texture texture = null;
 	private float rotation = 0;
 	private boolean first = true;
 	private Vector2f velocity = new Vector2f();
 	public Ship(int mass, int x, int y, String texture){
 		super(mass,x,y,"Ship");
 		try {
 			this.texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/"+texture));
 		} catch (IOException e) {
 			System.err.println("Could not load texture: "+texture);
 		}
 		velocity.set(0,0);
 	}
 	@Override
 	public void draw() {
 		texture.bind();
 		int h = texture.getImageHeight()/20;
 		int w = texture.getImageWidth()/20;
 		//int h = 20;
 		//int w = 20;
 		calculateAngle();
		GL11.glTranslatef((float)x+w/2, (float)y+h/2, 0);
 		GL11.glRotatef(rotation, 0f, 0f, 1f);
		GL11.glTranslatef(-(float)x-w/2, -(float)y-h/2, 0);
 		glBegin(GL_QUADS);
 		glTexCoord2f(0,0);
     	glVertex2f((int)x-w,(int)y-h); //topleft
     	glTexCoord2f(1,0);
     	glVertex2f((int)x+w,(int)y-h); //top right
     	glTexCoord2f(1,1);
     	glVertex2f((int)x+w,(int)y+h); //bottom right
     	glTexCoord2f(0,1);
     	glVertex2f((int)x-w,(int)y+h); //bottom left
     	glEnd();
    	GL11.glTranslatef((float)x+w/2, (float)y+h/2, 0);
		GL11.glRotatef(-rotation, 0f, 0f, 1f);
		GL11.glTranslatef(-(float)x-w/2, -(float)y-h/2, 0);
 	}
 	public void applyForce(Vector2f force){
 		velocity.x = velocity.x + (force.x*Game.delta/1000)/this.mass;
 		velocity.y = velocity.y + (force.y*Game.delta/1000)/this.mass;
 	}
 	public void move(ArrayList<Mass> attracts){
 		Vector2f planetForce = new Vector2f(0,0);
 		for(Mass m: attracts){
 			Vector2f.add(planetForce, getVectorForce(this,m), planetForce);
 		}
 		//we dont apply force initially, shit fucks up
 		if(!first){
 			this.applyForce(planetForce);
 		}
 		first = false;
 		this.x = this.x + velocity.x*Game.delta/1000;
 		this.y = this.y + velocity.y*Game.delta/1000;
 	}
 	private Vector2f getVectorForce(Mass from, Mass to){
 		//from ship
 		//to planet
 		Vector2f ship = new Vector2f();
 		ship.x = (float)this.getX();
 		ship.y = (float)this.getY();
 		Vector2f mass = new Vector2f();
 		mass.x = (float)to.getX();
 		mass.y = (float)to.getY();
 		Vector2f force = new Vector2f();
 		Vector2f.sub(mass, ship, force);
 		Vector2f unitVector = new Vector2f();
 		float divisor = (float) Math.sqrt(Math.pow(force.x, 2)+Math.pow(force.y, 2));
 		unitVector.set(force.x/divisor,force.y/divisor);
 		float magnitude = (float) this.getForceMagnitude(to);
 		Vector2f magVec = new Vector2f();
 		magVec.set(unitVector.x*magnitude,unitVector.y*magnitude);
 		return magVec;
 	}
 	private void calculateAngle(){
 		Vector2f velocityUnit = new Vector2f();
 		Vector2f up = new Vector2f(0,1);
 		float velocityMag = (float) Math.sqrt(Math.pow(velocity.x, 2)+Math.pow(velocity.y, 2));
 		if(velocityMag!=0){
 			velocityUnit.set(velocity.x/velocityMag,velocity.y/velocityMag);
 			rotation = (float) Math.toDegrees(Vector2f.angle(up, velocityUnit))-90;
 		}
 	}
 	public float getVelocity(){
 		return (float) Math.sqrt(Math.pow(velocity.x, 2)+Math.pow(velocity.y, 2));
 	}
 }
