 package projectmayhem;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MainMenu extends BasicGameState{
 
 	public static int ID;
 
 	int playButtonState;	
 	Image play[];
 	
 	
 	public MainMenu(int ID){
 		this.ID = ID;
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		playButtonState = 0;
 		play = new Image[2];
 		play[0] = new Image("res/playbutton.png");
 		play[1] = new Image("res/playbuttonhover.png");
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		play[playButtonState].draw(400,300);
		
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 	}
 	
 	public int getID(){
 		return ID;
 	}
 	
 }
