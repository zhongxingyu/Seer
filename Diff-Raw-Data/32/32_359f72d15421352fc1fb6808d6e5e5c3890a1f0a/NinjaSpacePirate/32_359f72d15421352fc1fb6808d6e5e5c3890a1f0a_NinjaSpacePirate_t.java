 package com.punchline.NinjaSpacePirate;
 
 import java.io.IOException;
 
 import com.badlogic.gdx.Gdx;
 import com.punchline.NinjaSpacePirate.gameplay.Stats;
 import com.punchline.NinjaSpacePirate.screens.MainMenuScreen;
 import com.punchline.javalib.Game;
 import com.punchline.javalib.utils.Convert;
 import com.punchline.javalib.utils.LogManager;
import com.punchline.javalib.utils.LogManager.LogType;
 import com.punchline.javalib.utils.SoundManager;
 
 /**
  * The game class.
  * @author Natman64
  * @created Oct 17, 2013
  */
 public class NinjaSpacePirate extends Game {
 	
 	@Override
 	public void create() {
 		Convert.init(8f);
		LogManager.init(Gdx.app, LogType.ERROR);
 		Stats.init();
 		
 		title = "Ninja Space Pirate";
 		
 		landscapeMode = false;
 		
 		super.create();
 		
 		Settings.loadSettings();
 		
 		getScreenManager().addScreen(new MainMenuScreen(this));
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		
 		Stats.saveStats();
 		Settings.saveSettings();
 	}
 
 	@Override
 	protected void loadSounds() {
 		try {
 			SoundManager.loadContent(Gdx.files.internal("data/audio.xml"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
