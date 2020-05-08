 package menu;
 
 import util.Config;
 import assets.Fonts;
 import assets.Sounds;
 import assets.Textures;
 import engine.Game;
 import engine.IGameState;
 
 public class LoadingState implements IGameState {
 	public static final String name = "STATE_LOADING";
 
 	private boolean startLoading;
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void init() {
 		startLoading = false;
		Config.createConfig();
 	}
 
 	@Override
 	public void start() {
 
 	}
 
 	@Override
 	public void stop() {
 
 	}
 
 	@Override
 	public void render(int delta) {
 		System.out.println("loading...");
 	}
 
 	@Override
 	public void update(int delta) {
 		if (startLoading) {
 			Sounds.get();
 			Textures.get();
 			Fonts.get();
 			Sounds.get().setSoundEnabled(false);
 			Sounds.get().setMusicEnabled(false);
 			Game.get().setCurrentState(MenuState.name);
 		}
 		startLoading = true;
 	}
 
 }
