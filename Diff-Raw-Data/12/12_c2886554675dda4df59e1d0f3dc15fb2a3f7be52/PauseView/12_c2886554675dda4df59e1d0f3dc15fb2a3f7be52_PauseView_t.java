 package view;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PauseView {
 	private int worldWidth;
 	private int worldHeight;
 	
 	public PauseView(int worldWidth, int worldHeight){
 		this.worldWidth = worldWidth;
 		this.worldHeight = worldHeight;
 	}
 
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		//if(gc.isPaused()){
			/*Create image of game TODO*/
 			//draw(sbg.getState(Game.IN_GAME));
 			//gather contents from the play screen and make a new Image
 			//get graphics and render your scene in the new Image
 			//set Image as background
 			//create a fill rect for opacity and color if needed
 
 			/*if you pause, you keep the last state id in memory and you do a transition to your paused state
 			 * if you resume you get the last state back by id and everyting "resumes" automatically without 
 			 * doing noting, the state is paused (not rendered or updated) until you go back to that state*/
 
 			/*Create pause menu*/
			Rectangle pauseMenu = new Rectangle(worldWidth/4, worldHeight/4, worldWidth/2, worldHeight/2); //x, y, width, height
 			g.draw(pauseMenu);
 			g.setColor(Color.green);
 			/*Create "buttons" in menu*/
 			for(int i = 0; i < 4; i++){
 				//buttonList = 
 			}
 		//}
 	}
 }
