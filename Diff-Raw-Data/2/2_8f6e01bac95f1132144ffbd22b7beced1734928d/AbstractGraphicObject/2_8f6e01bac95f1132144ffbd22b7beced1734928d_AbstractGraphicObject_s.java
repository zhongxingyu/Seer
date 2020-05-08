 package model;
 
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2d;
 
 import java.awt.Rectangle;
 
 import org.newdawn.slick.opengl.Texture;
 
 public abstract class AbstractGraphicObject implements IGraphicObject {
 
 	protected double x, y, width, height;
 	protected float rotation;
 	protected Rectangle hitbox = new Rectangle();
 	protected Texture[] textures = new Texture[1];
 	// The current shown image
 	protected int imageIndex;
 	
 	public AbstractGraphicObject(double x, double y, double width, double height, String type, String model) {
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		
 		rotation = 0;
 		imageIndex = 0;
 		textures = GameContainer.getContainer().getTextureList(type, model);
 	}
 	
 	// Used for invisible tiles
 	public AbstractGraphicObject(double x, double y, double width, double height) {
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 	}
 	
 	
 	@Override
 	public void draw() {
 		glPushMatrix();
 		
 		textures[imageIndex].bind();
 		
 		glTranslatef((int) x, (int) y, 0);
 		glRotatef(rotation, 0f, 0f, 1f);
 		glTranslatef((int) -x, (int) -y, 0);
 		
 		glBegin(GL_QUADS);
 			glTexCoord2f(0, 0);
 			glVertex2d(x - width/2, y - width/2);
 			glTexCoord2f(textures[imageIndex].getWidth(), 0);
 			glVertex2d(x + width/2, y - width/2);
 			glTexCoord2f(textures[imageIndex].getWidth(), textures[imageIndex].getHeight());
 			glVertex2d(x + width/2, y + height/2);
 			glTexCoord2f(0, textures[imageIndex].getHeight());
 			glVertex2d(x - width/2, y + height/2);
 		glEnd();
 		glPopMatrix();
 
 	}
 
 	@Override
 	public double getX() {
 		return x;
 	}
 
 	@Override
 	public float getRotation() {
 		return rotation;
 	}
 	
 	@Override
 	public double getWidth() {
 		return width;
 	}
 
 	@Override
 	public double getHeight() {
 		return height;
 	}
 
 	@Override
 	public double getY() {
 		return y;
 	}
 	
 	@Override
 	public void setLocation(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	@Override
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	@Override
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	@Override
 	public void setWith(double width) {
 		this.width = width;
 	}
 
 	@Override
 	public void setHeight(double height) {
 		this.height = height;
 	}
 
 	@Override
 	public void setRotation(float rotation) {
 		this.rotation = rotation;
 	}
 	
 	@Override
 	public int getCurrentImage() {
 		return imageIndex;
 	}
 
 	@Override
 	public void setCurrentImage(int index) {
 		imageIndex = index;
 	}
 
 	@Override
 	public boolean intersects(IGraphicObject other) {
		hitbox.setBounds((int) (x-(width/2)), (int) (y-(height/2)), (int) width, (int) height);
 		
 		return hitbox.intersects(other.getX() - (other.getWidth()/2), other.getY() - (other.getHeight()/2), other.getWidth(), other.getHeight());
 	}
 }
