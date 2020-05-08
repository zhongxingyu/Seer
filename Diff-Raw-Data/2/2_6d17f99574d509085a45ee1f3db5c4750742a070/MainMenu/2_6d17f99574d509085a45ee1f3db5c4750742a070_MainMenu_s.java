 package projectmayhem;
 
 import misc.Button;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MainMenu extends BasicGameState{
 
 	public static int ID;
 
 	int playButtonState;	
 	Button play;
 	
 	
 	public MainMenu(int ID){
 		this.ID = ID;
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		playButtonState = 0;
		play = new Button(new Image("graphics/buttons/playbutton.png"), new Image("graphics/buttons/playbuttonhover.png"), Button.LEFTBOT(), Button.LEFTBOT(), gc);
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		play.getGraphics().draw(play.getX(), play.getY());
 		
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 	}
 	
 	public int getID(){
 		return ID;
 	}
 	
 }
