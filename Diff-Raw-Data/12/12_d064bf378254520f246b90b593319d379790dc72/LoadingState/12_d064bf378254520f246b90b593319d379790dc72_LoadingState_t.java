 package menu;
 
 import java.util.List;
 
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
 			List<Object> options = Config.loadOptions();
			Sounds.get().setSoundEnabled(options.isEmpty() ? true : (Boolean) options.get(3));
			Sounds.get().setMusicEnabled(options.isEmpty() ? true : (Boolean) options.get(4));
 			Game.get().setCurrentState(MenuState.name);
 		}
 		startLoading = true;
 	}
 
 }
