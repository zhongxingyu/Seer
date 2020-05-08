 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.IClickEvent;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.RenderAssistant;
 
 public class SceneGame extends Scene
 {
 	boolean paused;
 	
 	@Override
 	public void init()
 	{
 		glClearColor(0.5f, 0.8f, 0.85f, 1);
 		paused = false;
 		TextButton mainmenu = new TextButton(Display.getWidth() / 2, Display.getHeight() / 2 - 65, Tr._("title.mainmenu"));
 		mainmenu.setClickEvent(new IClickEvent()
 		{
 			@Override
 			public void onClick()
 			{
 				Game.currentMap = null;
 				Game.currentGame.setScene(new SceneMainmenu());
 			}
 		});
 		content.add(mainmenu);
 		
 		TextButton quit = new TextButton(Display.getWidth() / 2, Display.getHeight() / 2 + 10, Tr._("title.quitGame"));
 		quit.setClickEvent(new IClickEvent()
 		{
 			@Override
 			public void onClick()
 			{
 				System.exit(0);
 			}
 		});
 		content.add(quit);
 	}
 	
 	@Override
 	public void update()
 	{
 		content.get(0).setVisible(paused);
 		content.get(1).setVisible(paused);
 		super.update();
 		if (!paused) return;
 		
 		glEnable(GL_BLEND);
 		glColor4f(0.4f, 0.4f, 0.4f, 0.6f);
 		glBindTexture(GL_TEXTURE_2D, 0);
 		RenderAssistant.renderRect(0, 0, Display.getWidth(), Display.getHeight());
 		glColor4f(1, 1, 1, 1);
 		RenderAssistant.renderContainer(Display.getWidth() / 2 - 170, Display.getHeight() / 2 - 100, 340, 200, true);
 		super.render();
 		
 		glDisable(GL_BLEND);
 	}
 	
 	@Override
 	public void handleMouseWorld(int x, int y, int flag)
 	{
 		super.handleMouseWorld(x, y, flag);
		if (Mouse.isButtonDown(1))
 		{
			if (!Mouse.isGrabbed()) Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
			
 			Mouse.setGrabbed(true);
			if (Mouse.isButtonDown(1)) Game.currentGame.rotateCamera();
			else Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 		}
 		else Mouse.setGrabbed(false);
 	}
 	
 	@Override
 	public void handleKeyboard(int key, boolean down)
 	{
 		if (key == Keyboard.KEY_ESCAPE && down)
 		{
 			paused = !paused;
 		}
 	}
 }
