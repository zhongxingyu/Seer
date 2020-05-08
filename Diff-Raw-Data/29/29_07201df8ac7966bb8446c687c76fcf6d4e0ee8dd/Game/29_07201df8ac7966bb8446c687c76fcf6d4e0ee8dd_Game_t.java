 package game;
 
 import graphics.Camera;
 import graphics.Model;
 import graphics.RenderMaster;
 import graphics.RenderMasterFactory;
 import input.Controller;
 import input.KeyboardController;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import sound.Emitter;
 import sound.SoundMaster;
 
 public class Game {
 	
 	public static void main(String[] args)
 	{
 
 		
 		
 		RenderMaster renderMaster = RenderMasterFactory.getRenderMaster();
 
 		SoundMaster soundMaster = new SoundMaster();
 		soundMaster.loadSound("temp/conti.wav");
 		soundMaster.loadSound("temp/conti2.wav");
 		
 		
 		Emitter emitter = new Emitter(soundMaster);
 		emitter.update();
 		
 		Controller controller = new KeyboardController();
 		
 		Camera camera = renderMaster.getCamera();
 		float rotation = 0.0f;
 		float height = 0.0f;
 		float lent = 15.0f;
 		float fov = 90.0f;
 		
 		
 		Model last = renderMaster.addModel("whatever");
 		Model root = last;
 		
         Model current = null;
         for(int i = 0; i < 80; i++)
         {
         	current = renderMaster.addModel("whatever");
         	current.setPosition(new Vector3f(0.0f, 2.25f, -1.0f));
         	
         	last.addChild(current);
         	
         	last = current;
         	
         }
 		
         current.setPosition(new Vector3f(0.0f,0.0f,7.5f));
 		
 		while(!Display.isCloseRequested())
 		{
 			//close if escape is hit
 			if(controller.isPressed("QUIT")){
 				Display.destroy();
 				break;
 			}
 			
 			if(controller.isPressed("RIGHT"))
 			{
 				rotation += 0.015f;	
 			}else if(controller.isPressed("LEFT"))
 			{
 				rotation -= 0.015f;
 			}
 			
 			if(controller.isPressed("MOVEUP"))
 			{
 				height += 0.05f;	
 
 				
 			}else if(controller.isPressed("MOVEDOWN"))
 			{
 				height -= 0.05f;
 			}
 			
 			if(controller.isPressed("PLAY")){
 				emitter.stopAll();
 			}
 			
 			if(controller.isPressed("DOLLYOUT"))
 			{
 				fov *= 1.01f;
 			    emitter.playSound("temp/conti.wav",fov/90.0f);
 			}
 			else if(controller.isPressed("DOLLYIN"))
 			{
 				fov *= 0.99f;
 			    emitter.playSound("temp/conti.wav",fov/90.0f);
 			}
 			
 			
 			
 			if(controller.isPressed("OBJUP"))
 			{
				root.addPosition(new Vector3f(0.0f, 0.0f, 0.05f));
 			}
 			else if(controller.isPressed("OBJDOWN"))
 			{
				root.addPosition(new Vector3f(0.0f, 0.0f, -0.05f));
 			}
 			
 			if(controller.isPressed("OBJLEFT"))
 			{
				root.addPosition(new Vector3f(-0.05f, 0.0f, 0.0f));
 			}
 			else if(controller.isPressed("OBJRIGHT"))
 			{
				root.addPosition(new Vector3f(0.05f, 0.0f, 0.0f));
 			}
 			
 			fov = fov > 180.0f ? 180.0f : fov <= 0.0f ? 0.0f : fov;
 			
 			lent = 15f / (float)Math.atan(fov * 0.0349066);
 			
 			camera.setPosition(new Vector3f(lent * (float)Math.sin(rotation), 
 											height, 
 											lent * (float)Math.cos(rotation)));
 			
 			camera.setFOV(fov);
 			
 			
 			
 			renderMaster.render();
 			
 		}
 
 		soundMaster.exit();
 		
 	}
 
 }
