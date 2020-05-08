 package com.vloxlands.ui;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.newdawn.slick.Color;
 
 import com.vloxlands.util.FontAssistant;
 import com.vloxlands.util.RenderAssistant;
 
 public class Button extends Component
 {
 	String title;
 	
 	/**
 	 * 0 = unselected 1 = hovered 2 = active
 	 */
 	int state;
 	
 	public Button(int x, int y, int width, int height, String title)
 	{
 		super(x, y, width, height);
 		this.title = title;
 		this.state = 0;
 	}
 	
 	public void setState(int s)
 	{
 		state = s;
 	}
 	
 	public int getState()
 	{
 		return state;
 	}
 	
 	@Override
 	public void draw()
 	{
		RenderAssistant.disable(GL_LIGHTING);
 		RenderAssistant.bindTexture("graphics/textures/ui/button.png");
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		if (state != 1) RenderAssistant.glColorHex("222222", 0.6f);
 		else RenderAssistant.glColorHex("ff9933", 0.6f);
 		RenderAssistant.renderRect(x, y, width, height);
 		glDisable(GL_BLEND);
 		
 		int tx = FontAssistant.getFont(font).getWidth(title);
 		int mx = width / 2 - tx / 2;
 		RenderAssistant.renderText(x + mx, y + height / 4f, title, Color.white, font);
 	}
 	
 	@Override
 	public void mouseEvent(int posX, int posY, byte b, boolean c)
 	{
 		if (!c) state = 0;
 		else if ((b & 1) == 1) state = 2;
 		else state = 1;
 	}
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 }
