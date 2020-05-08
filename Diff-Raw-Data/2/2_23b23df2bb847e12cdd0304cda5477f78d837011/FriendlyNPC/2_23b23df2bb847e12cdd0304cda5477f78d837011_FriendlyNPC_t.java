 package main;
 
 import main.interfaces.Interface;
 import main.interfaces.NPCInterface;
 import org.lwjgl.opengl.GL11;
 import util.gl.Color;
 
 public class FriendlyNPC extends Entity {
 
 	Color c;
 	
 	Interface npcInterface;
 	
 	public FriendlyNPC(String name, int x, int y, int sceneX, int sceneY) {
 		
 		super(name);
 		
 		setCoordinates(x, y);
 		setScene(sceneX, sceneY);
 		
 		c = new Color(33,100,148);
 		
 		npcInterface = new NPCInterface(this);
 	}
 
 	public void interact() {
 		
 		npcInterface.showInterface();
 	}
 	
 	@Override
 	public void draw() {
 		
		Main.BLANK_TEXTURE.bind();
		
 		GL11.glBegin(GL11.GL_QUADS);
 		{
 			GL11.glColor3d(c.r, c.g, c.b);
 			
 			GL11.glVertex2f(getX(), getY()); // top left
 			GL11.glVertex2f(getX(), getY()+getH()); // bottom left
 			GL11.glVertex2f(getX()+getW(), getY()+getH()); // bottom right
 			GL11.glVertex2f(getX()+getW(), getY()); // top right
 		}
 		GL11.glEnd();
 		
 	}
 
 	@Override
 	public void notifyDeath() {
 		
 		/* We don't really need to implement this method.
 		 * In the event that we decide that we can attack
 		 * all of the NPCs in the game then we can go ahead
 		 * and stick some code here
 		 */
 	}
 
 	@Override
 	public void collided(Entity x) {
 
 		/* When we come back to tidy up the collision detection,
 		 * this will probably be a method used in that code.
 		 */
 	}
 
 }
