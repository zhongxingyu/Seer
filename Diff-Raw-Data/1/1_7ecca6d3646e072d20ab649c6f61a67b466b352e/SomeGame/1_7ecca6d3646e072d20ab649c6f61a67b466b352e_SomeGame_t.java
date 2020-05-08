 package net;
 
 import org.lwjgl.opengl.Display;
 import static org.lwjgl.opengl.GL11.*;
 
 import chu.engine.Game;
 import chu.engine.Stage;
 
 public class SomeGame extends Game {
 	
 	private Stage currentStage;
 
 
 	public static void main(String[] args) {
		System.out.println("asdf");
 		SomeGame game = new SomeGame();
 		game.init(640,480);
 		game.loop();
 	}
 	
 	public void init(int width, int height) {
 		super.init(width, height);
 		currentStage = new Stage();
 		currentStage.addEntity(new Merc(currentStage, 100, 100));
 	}
 	
 	public void loop() {
 		
 		while(!Display.isCloseRequested()) {
 			if(timeDelta > 16666667) {			//60 FPS
 				time = System.nanoTime();
 				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 				
 				getInput();
 				processInput();
 				
 				if(!paused) {
 					currentStage.update();
 				}
 				currentStage.render();
 				Display.update();
 			}
 			
 			timeDelta = System.nanoTime()-time;
 		}
 		
 		Display.destroy();
 	}
 
 }
