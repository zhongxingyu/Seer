 package com.vloxlands.game;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.settings.CFG;
 import com.vloxlands.util.RenderAssistant;
 
 /**
  * @author Dakror
  */
 public class UpdateThread extends Thread
 {
 	public static UpdateThread currentUpdateThread;
 	public static boolean requestStop = false;
 	int ticks;
 	
 	public UpdateThread()
 	{
 		currentUpdateThread = this;
 		ticks = 0;
 		setDaemon(true);
 		setName("Update Thread");
 		start();
 	}
 	
 	@Override
 	public void run()
 	{
 		try
 		{
			while (Display.isCreated() && !Display.isCloseRequested() && Mouse.isCreated())
 			{
 				if (requestStop) break;
 				
 				if (Game.currentGame.scene != null && Game.currentGame.scene.initialized) Game.currentGame.scene.onTick();
 				
 				while (Keyboard.isCreated() && Keyboard.next())
 				{
 					if (Keyboard.getEventKey() == Keyboard.KEY_F11 && !Keyboard.getEventKeyState()) Game.currentGame.fullscreenToggled = true;
 					if (Keyboard.getEventKey() == Keyboard.KEY_F4 && !Keyboard.getEventKeyState()) CFG.SHOW_DEBUG = !CFG.SHOW_DEBUG;
 					if (Keyboard.getEventKey() == Keyboard.KEY_L && !Keyboard.getEventKeyState()) CFG.LIGHTING = !CFG.LIGHTING;
 					if (Keyboard.getEventKey() == Keyboard.KEY_B && !Keyboard.getEventKeyState()) CFG.SHOW_CHUNK_BOUNDRIES = !CFG.SHOW_CHUNK_BOUNDRIES;
 					if (Keyboard.getEventKey() == Keyboard.KEY_T && Keyboard.getEventKeyState()) Game.currentGame.rerender = true;
 					if (Keyboard.getEventKey() == Keyboard.KEY_Z && Keyboard.getEventKeyState()) Game.currentGame.regenerate = true;
 					if (Keyboard.getEventKey() == Keyboard.KEY_V && !Keyboard.getEventKeyState())
 					{
 						CFG.SHOW_DIRECTIONS = !CFG.SHOW_DIRECTIONS;
 						Game.currentGame.directionalArrowsPos = new Vector3f(Game.currentGame.camera.getPosition());
 					}
 					if (Game.currentGame.scene != null) Game.currentGame.scene.handleKeyboard(Keyboard.getEventKey(), Keyboard.getEventKeyState());
 				}
 				
 				
 				if (Keyboard.isKeyDown(Keyboard.KEY_R))
 				{
 					Game.currentGame.lightPos.x = Game.currentGame.camera.position.x;
 					Game.currentGame.lightPos.y = Game.currentGame.camera.position.y;
 					Game.currentGame.lightPos.z = Game.currentGame.camera.position.z;
 					RenderAssistant.setUniform3f("lightPosition", Game.currentGame.lightPos);
 				}
 				if (Keyboard.isKeyDown(Keyboard.KEY_UP))
 				{
 					Game.currentGame.lightPos.z++;
 				}
 				if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
 				{
 					Game.currentGame.lightPos.z--;
 				}
 				if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
 				{
 					Game.currentGame.lightPos.x++;
 				}
 				if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
 				{
 					Game.currentGame.lightPos.x--;
 				}
 				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) Game.currentGame.cameraSpeed = 0.5f;
 				else Game.currentGame.cameraSpeed = 0.3f;
 				
 				
 				try
 				{
 					ticks++;
 					Thread.sleep(20);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
 	}
 	
 	public int getTicksPS()
 	{
 		return Math.round(ticks / ((System.currentTimeMillis() - Game.currentGame.start) / 1000f));
 	}
 }
