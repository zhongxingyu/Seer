 package spaceshooters.main;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import spaceshooters.states.GameplayState;
 import spaceshooters.states.MainMenuState;
 import spaceshooters.states.OptionsState;
 import spaceshooters.util.config.Configuration;
 import spaceshooters.util.config.EnumConfig;
 
 public class Game extends StateBasedGame {
 	
	public String version = "Indev 0.2";
 	
 	public Game() throws SlickException {
 		super("Spaceshooters 2");
 		
 		start();
 	}
 	
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.addState(new MainMenuState());
 		this.addState(new OptionsState());
 		this.addState(new GameplayState());
 	}
 	
 	public Configuration getConfig() {
 		return Launcher.config;
 	}
 	
 	public void start() throws SlickException {
 		AppGameContainer game = new AppGameContainer(this, 800, 600, this.getConfig().getBoolean(EnumConfig.FULLSCREEN));
 		game.setVerbose(false);
 		game.setShowFPS(false);
 		game.setMinimumLogicUpdateInterval(1);
 		game.setMaximumLogicUpdateInterval(1);
 		game.start();
 	}
 }
