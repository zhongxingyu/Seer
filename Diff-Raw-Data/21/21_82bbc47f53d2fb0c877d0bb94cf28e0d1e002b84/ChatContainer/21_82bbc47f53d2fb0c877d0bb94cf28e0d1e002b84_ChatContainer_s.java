 package com.vloxlands.ui;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.awt.Font;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.util.FontAssistant;
 import com.vloxlands.util.RenderAssistant;
 
 /**
  * @author Dakror
  */
 public class ChatContainer extends Container
 {
 	int lastY;
 	
 	public Font font = FontAssistant.GAMEFONT.deriveFont(Font.PLAIN, 24f);
 	
 	public ChatContainer(int x, int y, int width, int height)
 	{
 		super(x, y, width, height, false, true);
 		lastY = 15;
 		FontAssistant.getFont(font);
 	}
 	
 	@Override
 	public void render()
 	{
 		if (border)
 		{
 			if (filled) RenderAssistant.renderContainer(x, y, width, height, doubled);
 			else RenderAssistant.renderOutline(x, y, width, height, doubled);
 		}
 		glEnable(GL_SCISSOR_TEST);
 		glScissor(x + 15, (Display.getHeight() - y) - height + 15, width - 30, height - 30);
		int dif = height - 30 - lastY;
		if (lastY > height - 30) glTranslatef(x, y + dif, 0);
 		else glTranslatef(x, y, 0);
 		for (IGuiElement g : components)
 		{
 			if (g.isVisible() && g.wantsRender()) g.render();
 		}
 		
		if (lastY > height - 30) glTranslatef(-x, -y - dif, 0);
 		else glTranslatef(-x, -y, 0);
 		glDisable(GL_SCISSOR_TEST);
 	}
 	
 	public void addMessage(String message, Vector3f color)
 	{
 		int lineHeight = 25;
 		String[] lines = RenderAssistant.wrap(message, font, width - 30);
 		for (int i = 0; i < lines.length; i++)
 		{
 			Label label = new Label(20, lastY + i * lineHeight, width, lineHeight, lines[i], false);
 			label.font = font;
 			label.color = color;
 			components.add(label);
 		}
 		lastY += lines.length * lineHeight;
 	}
 }
