 package tetrix.view;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import tetrix.sound.GameMusic;
 
 /**
  * Class managing the loading of the different states
  * @author Linus Karlsson
  *
  */
 public class StateHandler extends StateBasedGame {
 	
 	public enum States {
 		INTROVIEW(1),
 		MAINMENUVIEW(2),
 		LEVELSVIEW(3),
 		GAMEPLAYVIEW(4),
 		SETTINGSVIEW(5),
 		HIGHSCOREVIEW(6),
 		PAUSEDGAMEVIEW(7);
 		
 		private final int stateID;  
 		  
 		States(int stateID) {  
 			this.stateID = stateID;  
 		} 
 		
 		public int getID() {
 		    return stateID;
 		  }
 	}
 	
 	public StateHandler() {
 		super("Tetrix");
 	}
 
 	@Override
     public void initStatesList(GameContainer gameContainer) throws SlickException {
 		this.addState(new IntroView(States.INTROVIEW.getID()));
		this.addState(new MainMenuView(States.MAINMENUVIEW.getID()));
		this.addState(new LevelsView(States.LEVELSVIEW.getID()));
        this.addState(new GameplayView(States.GAMEPLAYVIEW.getID()));
        this.addState(new SettingsView(States.SETTINGSVIEW.getID()));
        this.addState(new HighscoreView(States.HIGHSCOREVIEW.getID()));
        this.addState(new PausedGameView(States.PAUSEDGAMEVIEW.getID()));
     }
 }
 	
