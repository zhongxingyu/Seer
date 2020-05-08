 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.opengl.Texture;
 
 
 public class Bullet extends Entity {
 	
 	public int moveSpeed = 12;
 	public int damage = 12;
 	private Texture bull;
 	private boolean used;
 
 	public Bullet(Game ingame) {
 		game = ingame;
 		bull = loadTexture("bullet.png");
 		this.dx = moveSpeed;
 		this.dy = moveSpeed;
		width = bull.getImageWidth()-5;
        height = bull.getImageHeight()-5;
 		halfSize = width/2;
 	}
 	
 	public int getMoveSpeed() {
 		return moveSpeed;
 	}
 	
 	public void move(long delta) {
 		super.move(delta);
 	}
 	
 	public void reinitialize(float x, float y,float dx,float dy) {
 		this.x = x;
 		this.y = y;
 		this.dx = dx;
 		this.dy = dy;
 		used = false;
 	}
 	
 	public void draw() {
 		bull.bind();
 		super.draw();
 	}
 
 	@Override
 	public void collidedWith(Entity other) {
 		
 	}
 
 }
