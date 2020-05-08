 package de.dakror.tube.game.tube;
 
 import static org.lwjgl.opengl.GL11.*;
 import de.dakror.tube.util.RenderAssistant;
 
 public class Field
 {
 	public static final float SIZE = 1;
 	
 	int pos;
 	boolean block;
 	
 	public Field(int pos, boolean block)
 	{
 		this.pos = pos;
 		this.block = block;
 	}
 	
 	public void render()
 	{
 		glTranslatef(0, 0, pos);
 		glRotatef(90, 1, 0, 0);
		glEnable(GL_TEXTURE_2D);
		if (System.getProperty("os.version").equals("6.0")) glColor3f(1, 0, 0);
		RenderAssistant.bindTexture("/img/field.png");
		
 		float size = 0.85f;
 		
		RenderAssistant.renderRect(0, 0, 1, 1, 0, 0, size, size);
 	}
 }
