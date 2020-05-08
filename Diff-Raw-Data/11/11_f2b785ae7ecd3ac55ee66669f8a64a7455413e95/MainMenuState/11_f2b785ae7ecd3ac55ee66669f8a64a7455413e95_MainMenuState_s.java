 package pl.spaceshooters.state;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.lwjgl.input.Keyboard;
 
 import pl.blackburn.base.Game;
 import pl.blackburn.base.Input;
 import pl.blackburn.graphics.Texture;
 import pl.spaceshooters.aurora.plugin.Plugin;
 import pl.spaceshooters.aurora.plugin.PluginLoader;
 import pl.spaceshooters.gui.Button;
 import pl.spaceshooters.gui.ButtonSize;
 import pl.spaceshooters.level.MenuLevel;
 import pl.spaceshooters.main.Spaceshooters;
 import pl.spaceshooters.util.Font;
 import pl.spaceshooters.util.ResourceLoader;
 import pl.spaceshooters.util.Translator;
 
 /**
  * Main menu state.
  * 
  * @author Mat
  * 
  */
 public class MainMenuState implements pl.blackburn.base.GameState {
 	
 	public static MenuLevel lvl;
 	
	private Translator translator = Translator.getTranslator();
 	private String splash;
 	private Texture logo;
 	private Button playSingle, plugins, options, quit;
 	private List<Button> buttons;
 	
 	@Override
 	public void init(Game game) {
 		buttons = new ArrayList<>();
 		logo = ResourceLoader.getTexture("menu/logo.png", 1f);
 		// Adding 70 to y coord.
 		playSingle = new Button(buttons, 148, 233, ButtonSize.BIG, translator.getTranslated("play.title"));
 		options = new Button(buttons, 148, 303, ButtonSize.BIG, translator.getTranslated("options.title"));
 		plugins = new Button(buttons, 148, 373, ButtonSize.SMALL, translator.getTranslated("plugins.title"));
 		quit = new Button(buttons, 410, 373, ButtonSize.SMALL, translator.getTranslated("quit.title"));
 	}
 	
 	@Override
 	public void enter(Game game) {
 		this.setSplash();
 		lvl = new MenuLevel();
 		lvl.init();
 	}
 	
 	float xPos = 10;
 	float yPos = 10;
 	boolean shouldMove = true;
 	
 	@Override
 	public void render(Game game) {
 		lvl.render();
 		
 		for (Button b : buttons) {
 			b.render();
 		}
 		
 		if (shouldMove) {
 			xPos += 2f;
 			if (xPos + Font.getFont().getWidth(splash) + 10 >= game.getWidth()) {
 				shouldMove = false;
 			}
 		} else {
 			xPos -= 2f;
 			if (xPos <= 10) {
 				shouldMove = true;
 			}
 		}
 		
 		Font.getFont().renderString(splash, xPos, yPos, 0.19f, 0.19f, 1);
 		
 		logo.render(Spaceshooters.WIDTH / 2 - logo.getWidth() / 2, 50);
 	}
 	
 	@Override
 	public void update(Game game, int delta) {
 		Input input = Input.getInput();
 		
 		lvl.update(delta);
 		
 		if (playSingle.isPressed()) {
 			game.enterState(State.SELECTOR);
 		} else if (plugins.isPressed()) {
 			game.enterState(State.PLUGINS);
 		} else if (options.isPressed()) {
 			game.enterState(State.OPTIONS);
 		} else if (quit.isPressed()) {
 			for (Plugin p : PluginLoader.getActivePlugins()) {
 				p.disable();
 			}
 			game.exit();
 		} else if (input.isKeyPressed(Keyboard.KEY_TAB)) {
 			game.enterState(State.TEST);
 		}
 	}
 	
 	@Override
 	public void leave(Game game) {
 		lvl = null;
 	}
 	
 	@Override
 	public void dispose(Game game) {
 		
 	}
 	
 	public void setSplash() {
 		InputStreamReader s = new InputStreamReader(this.getClass().getResourceAsStream("/data/vanilla/splashes.txt"), Charset.forName("UTF-8"));
 		BufferedReader r = new BufferedReader(s);
 		Random random = new Random();
 		ArrayList<String> splashes = new ArrayList<String>();
 		try {
 			String string;
 			while ((string = r.readLine()) != null) {
 				string.trim();
 				
 				if (string.length() > 0 && !string.startsWith("#")) {
 					splashes.add(string);
 				}
 			}
 			
 			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
 			Calendar cal = Calendar.getInstance();
 			Date now = sdf.parse(cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1));
 			
 			if (now.equals(sdf.parse("17/06"))) {
 				splash = "Happy Birthday Matterross!";
 			} else if (now.equals(sdf.parse("24/12"))) {
 				splash = "Merry Christmas!";
 			} else if (now.equals(sdf.parse("01/01"))) {
 				splash = "Happy New Year!";
 			} else {
 				splash = splashes.size() > 0 ? splashes.get(random.nextInt(splashes.size())) : "You destroyed me! :(";
 			}
 		} catch (IOException | ParseException e) {
 			System.err.println("I either cannot read the splashes or I cannot parse them.");
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public int getID() {
 		return State.MAIN_MENU;
 	}
 }
