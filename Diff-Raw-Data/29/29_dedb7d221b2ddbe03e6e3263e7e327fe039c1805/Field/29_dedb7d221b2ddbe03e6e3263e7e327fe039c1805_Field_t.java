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
 		float size = 0.85f;
 		
		if (System.getProperty("os.version").equals("6.0")) // vista fix
		{
			glLineWidth(5f);
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glColor3f(0, 0, 0);
			
			RenderAssistant.renderRect(0, 0, 1, 1, 0, 0, size, size);
			glLineWidth(1f);
			
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			
			glColor3f(1, 1, 1);
			RenderAssistant.renderRect(0.01f, 0.01f, 0.98f, 0.98f, 0, 0, size, size);
		}
		else
		{
			glEnable(GL_TEXTURE_2D);
			RenderAssistant.bindTexture("/img/field.png");
			
			glColor3f(1, 1, 1);
			RenderAssistant.renderRect(0, 0, 1, 1, 0, 0, size, size);
		}
		
 	}
 }
