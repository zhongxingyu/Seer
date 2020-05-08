 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.opengl.Display;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.gen.MapGenerator;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.GuiRotation;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.ProgressBar;
 import com.vloxlands.ui.Spinner;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.RenderAssistant;
 
 
 public class SceneNewGame extends Scene
 {
 	ProgressBar progress;
 	Spinner xSize, zSize, radius;
 	
 	@Override
 	public void init()
 	{
		if (!Game.client.isConnected()) Game.client.connectToServer(Game.IP);
 		
 		setBackground();
 		// setUserZone();
 		
 		setTitle(Tr._("newGame"));
 		
 		content.add(new Container(0, 115, Display.getWidth() - TextButton.WIDTH - 90, Display.getHeight() - 220 - TextButton.HEIGHT - 30)); // lobby
 		
 		Container lobbyButtons = new Container(0, 115 + Display.getHeight() - 220 - TextButton.HEIGHT - 30, Display.getWidth() - TextButton.WIDTH - 90, TextButton.HEIGHT + 30, false, false);
 		
 		content.add(lobbyButtons);
 		
 		content.add(new Container(Display.getWidth() - TextButton.WIDTH - 90, 115, TextButton.WIDTH + 90, Display.getHeight() - 220));
 		
 		progress = new ProgressBar(Display.getWidth() / 2, Display.getHeight() / 2 - ProgressBar.HEIGHT / 2, 400, 0, true);
 		progress.setVisible(false);
 		content.add(progress);
 		
 		TextButton back = new TextButton(Display.getWidth() / 2 - TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("back"));
 		back.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.currentGame.removeScene(SceneNewGame.this);
 			}
 		});
 		content.add(back);
 		
 		TextButton skip = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("start"));
 		skip.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.mapGenerator = new MapGenerator(xSize.getValue(), zSize.getValue(), 20, 24);
 				Game.mapGenerator.start();
 				lockScene();
 				progress.setVisible(true);
 			}
 		});
 		content.add(skip);
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 130, (TextButton.WIDTH + 70) / 2, 25, "X-" + Tr._("islands") + ":", false));
 		xSize = new Spinner(Display.getWidth() - TextButton.WIDTH - 80 + (TextButton.WIDTH + 70) / 2, 125, (TextButton.WIDTH + 70) / 2, 1, 4, 1, 1, GuiRotation.HORIZONTAL);
 		content.add(xSize);
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 175, (TextButton.WIDTH + 70) / 2, 25, "Z-" + Tr._("islands") + ":", false));
 		zSize = new Spinner(Display.getWidth() - TextButton.WIDTH - 80 + (TextButton.WIDTH + 70) / 2, 170, (TextButton.WIDTH + 70) / 2, 1, 4, 1, 1, GuiRotation.HORIZONTAL);
 		content.add(zSize);
 		
 		
 	}
 	
 	@Override
 	public void onTick()
 	{
 		super.onTick();
 		
 		if (Game.currentMap != null) Game.currentGame.setScene(new SceneGame());
 	}
 	
 	@Override
 	public void render()
 	{
 		super.render();
 		
 		if (Game.mapGenerator != null)
 		{
 			glEnable(GL_BLEND);
 			glColor4f(0.4f, 0.4f, 0.4f, 0.6f);
 			glBindTexture(GL_TEXTURE_2D, 0);
 			RenderAssistant.renderRect(0, 0, Display.getWidth(), Display.getHeight());
 			glColor4f(1, 1, 1, 1);
 			progress.setValue(Game.mapGenerator.progress);
 			progress.render();
 			glDisable(GL_BLEND);
 		}
 	}
 }
