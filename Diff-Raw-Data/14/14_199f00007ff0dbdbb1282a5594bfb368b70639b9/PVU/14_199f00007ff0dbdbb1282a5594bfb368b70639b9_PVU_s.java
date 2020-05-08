 package no.hist.gruppe5.pvu;
 
import no.hist.gruppe5.pvu.dialogdrawer.TestScreen;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import no.hist.gruppe5.pvu.coderacer.CoderacerScreen;
 import no.hist.gruppe5.pvu.visionshooter.VisionScreen;
 
 public class PVU extends Game {
 
     public static float SCREEN_WIDTH = 960f;
     public static float SCREEN_HEIGHT = 580f;
     public static float GAME_WIDTH = 192f;
     public static float GAME_HEIGHT = 116f;
 
     @Override
     public void create() {
         Assets.load();
        setScreen(new VisionScreen(this));
     }
 
     @Override
     public void dispose() {
     }
 
     @Override
     public void render() {
         super.render();
     }
 
     @Override
     public void resize(int width, int height) {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     public static void main(String[] args) {
         LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
         cfg.width = (int) PVU.SCREEN_WIDTH;
         cfg.height = (int) PVU.SCREEN_HEIGHT;
         cfg.fullscreen = false;
         cfg.resizable = false;
 
         new LwjglApplication(new PVU(), cfg);
     }
 }
