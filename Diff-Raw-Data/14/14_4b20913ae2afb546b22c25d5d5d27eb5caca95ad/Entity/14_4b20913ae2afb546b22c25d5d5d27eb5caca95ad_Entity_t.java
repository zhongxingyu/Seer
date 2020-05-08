 package de.schaf.apco;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.Texture;
 
 public class Entity {
 	private Vector3f Position = new Vector3f();
 
 	private Texture Texture = null;
 
 	public Entity() {
 
 	}
 
 	public Vector3f getPosition() {
 		return Position;
 	}
 
 	public void setPosition(Vector3f position) {
 		Position = position;
 	}
 
 	public Texture getTexture() {
 		return Texture;
 	}
 
 	public void setTexture(Texture tex) {
 		Texture = tex;
 	}
 
 	public void Render() throws CorruptTextureException {
 		if (Texture == null)
 			throw new CorruptTextureException("Nullpointer");
 		GL11.glPushMatrix();
 		{
 			GL11.glTranslatef(getPosition().x,getPosition().y,0f);
 			Texture.bind();
 			doQuad();
 		}
		GL11.glPopMatrix();
 	}
 
	protected void doQuad() {
 		GL11.glPushMatrix();
 		{
 			GL11.glTranslatef(-.5f, -.5f, 0f);
 			GL11.glScalef(getTexture().getImageWidth(),getTexture().getImageHeight(),1f);
 			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
 			{
 				GL11.glNormal3f(0f, 0f, 1f);
 
 				GL11.glTexCoord2f(0, 0);
 				GL11.glVertex2f(0, 0);
 
 				GL11.glTexCoord2f(0, getTexture().getHeight());
 				GL11.glVertex2f(0, 1);
 
				GL11.glTexCoord2f(getTexture().getWidth(), 0);
				GL11.glVertex2f(1, 0);
				
 				GL11.glTexCoord2f(getTexture().getWidth(), getTexture()
 						.getHeight());
 				GL11.glVertex2f(1, 1);
 			}
			GL11.glEnd();
 		}
 		GL11.glPopMatrix();
 	}
 
 }
