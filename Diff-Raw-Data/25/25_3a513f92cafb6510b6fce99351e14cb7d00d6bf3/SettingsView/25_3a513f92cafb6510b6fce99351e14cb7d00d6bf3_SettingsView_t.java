 package tetrix.view;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * Class responsible for viewing different settings for the user to control.
  * @author Linus Karlsson
  *
  */
 public class SettingsView extends BasicGameState {
 
 	private int stateID;
	private Image background;
 	
 	public SettingsView(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		// TODO Auto-generated method stub
		background = new Image("img/settingsbackg.png");
 		
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 			throws SlickException {
 		// TODO Auto-generated method stub
		background.draw(0,0);
 		
 	}
 
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
