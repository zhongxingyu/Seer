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
 
		
 	Button play;
		
	Button settings;
 	
 	public MainMenu(int ID){
 		this.ID = ID;
 	}
 	
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
		play = new Button(new Image("graphics/buttons/playbutton.png"), new Image("graphics/buttons/playbuttonhover.png"), Button.MID(), Button.MID(), gc);
		settings = new Button(new Image("graphics/buttons/textsettingsbutton.png"), new Image("graphics/buttons/textsettingsbuttonhover.png"), Button.MID(), 0, Button.MID(), 90, gc);
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		play.getGraphics().draw(play.getX(), play.getY());
		settings.getGraphics().draw(settings.getX(), settings.getY());
 		
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 	}
 	
 	public int getID(){
 		return ID;
 	}
 	
 }
