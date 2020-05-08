 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twosnakes;
 
 import java.awt.event.KeyEvent;
 
 /**
  *
  * @author jovan
  */
 public class Update
 {
 	public Update(GameState state)
 	{
 		this.state = state;
 	}
 	
 	void processKeyPress(KeyEvent e)
 	{
 		//get the new direction vector based on which key (left or right) is pressed.
 		Vector currentDirection = state.snake1.getDirection();
 		double x = currentDirection.x;
 		double y = currentDirection.y;
 		Vector delta;
		if(e.equals(KeyEvent.VK_LEFT)){
 			if(x >= 0.0 && y >= 0.0){
 				x = x - y/10.0;
 				y = y + x/10.0;
 			}
 			else if(x >= 0.0 && y < 0.0){
 				x = x - y/10.0;
 				y = y + x/10.0;
 			}
 			else if(x < 0.0 && y >= 0.0){
 				x = x - y/10.0; 
 				y = y + x/10.0;
 			}
 			else if(x < 0.0 && y < 0.0){
 				x = x - y/10.0;
 				y = y + x/10.0;
 			}
 		}
		else if (e.equals(KeyEvent.VK_RIGHT)){
 			
 			if(x >= 0.0 && y >= 0.0){
 				x = x + y/10.0;
 				y = y - x/10.0;
 			}
 			else if(x >= 0.0 && y < 0.0){
 				x = x + y/10.0;
 				y = y - x/10.0;
 			}
 			else if(x < 0.0 && y >= 0.0){
 				x = x - y/10.0; 
 				y = y - x/10.0;
 			}
 			else if(x < 0.0 && y < 0.0){
 				x = x + y/10.0;
 				y = y + x/10.0;
 			}
 		}
 		state.snake1.setDirection(new Vector(x,y));
 	}
 
 	void processKeyRelease(KeyEvent e)
 	{
 		
 	}
 
 	void gameUpdate(long timePassed)
 	{
 		state.snake1.move(timePassed);
 	}
 	
 	private GameState state;
 }
