 package ua.org.dector.moon_lander;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import ua.org.dector.moon_lander.managers.GameManagers;
 import ua.org.dector.moon_lander.models.Level;
 import ua.org.dector.moon_lander.models.Rocket;
 import ua.org.dector.moon_lander.screens.AbstractScreen;
 import ua.org.dector.moon_lander.screens.EditorScreen;
 import ua.org.dector.moon_lander.screens.GameScreen;
 import ua.org.dector.moon_lander.screens.SplashScreen;
 import ua.org.dector.moon_lander.utils.LevelRenderer;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class LanderGame extends Game {
     private GameScreen gameScreen;
     private SplashScreen splashScreen;
     private EditorScreen editorScreen;
 
     private AbstractScreen currentScreen;
 
     private GameManagers gameManagers;
 
     private Rocket rocket;
     private Level[] levels;
 
     public void create() {
         rocket = new Rocket();
         levels = ResourceLoader.loadLevelSet("levelset.json");
 
         gameManagers = new GameManagers();
 
         splashScreen = new SplashScreen(gameManagers, this);
         switchScreen(splashScreen);
     }
 
     public void play() {
         if (gameScreen == null)
             gameScreen = new GameScreen(gameManagers, rocket, levels, this);
         switchScreen(gameScreen);
     }
 
     public void play(Level level) {
         if (gameScreen == null)
             gameScreen = new GameScreen(gameManagers, rocket, new Level[] {level}, this);
 
         gameScreen.setLevelSet(new Level[] {level});
 
         gameScreen.playLevel(0);
         switchScreen(gameScreen);
     }
 
     public void switchScreen(AbstractScreen screen) {
         if (screen != null) {
             if (currentScreen != null) {
                 currentScreen.dispose();
             }
 
             setScreen(screen);
             Gdx.input.setInputProcessor(screen);
         }
     }
 
     public void openEditor(Level level, Rocket rocket) {
         if (editorScreen == null)
             editorScreen = new EditorScreen(gameManagers, level, rocket, this);
 
        editorScreen.editLevel(level, null);
         switchScreen(editorScreen);
     }
 }
