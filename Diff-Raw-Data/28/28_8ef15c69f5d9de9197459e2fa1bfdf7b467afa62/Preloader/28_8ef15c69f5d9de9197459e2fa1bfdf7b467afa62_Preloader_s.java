 package eu32k.ludumdare.ld26;
 
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 
 import eu32k.libgdx.common.Assets;
 
 public class Preloader {
 
    public static void preLoad() {
       AssetManager am = Assets.MANAGER;
 
       Assets.loadTexture("textures/sound.png");
       Assets.loadTexture("textures/tiles2.png");
       Assets.loadTexture("textures/tiles3.png");
       Assets.loadTexture("textures/square.png");
       Assets.loadTexture("textures/square2.png");
       Assets.loadTexture("textures/circle.png");
       Assets.loadTexture("textures/circle2.png");
       Assets.loadTexture("textures/title.png");
 
      // am.load("skins/uiskin.png", Texture.class);
      // am.load("skins/default.png", Texture.class);

       am.load("fonts/consolas.fnt", BitmapFont.class);
       am.load("fonts/calibri.fnt", BitmapFont.class);
 
       wait(am);
    }
 
    private static void wait(AssetManager am) {
       while (true) {
          am.update();
          if (am.getProgress() == 1.0f) {
             return;
          }
          try {
             Thread.sleep(10);
          } catch (InterruptedException e) {
             e.printStackTrace();
          }
       }
    }
 
 }
