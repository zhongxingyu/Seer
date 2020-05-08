 package game;
 
import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 
 import graphics.*;
 
 public class Game {
 	
 	public static void main(String[] args)
 	{
 		RenderMaster renderMaster = RenderMasterFactory.getRenderMaster();
 		
 		while(!Display.isCloseRequested())
 		{
 			renderMaster.render();
			
			if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
				Display.destroy();
				break;
			}
 		}
 		
 	}
 
 }
