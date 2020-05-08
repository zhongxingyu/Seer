 package no.hist.gruppe5.pvu;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 8/28/13
  * Time: 10:06 AM
  */
 public class Assets {
 
     public static TextureRegion testRegion;
     public static BitmapFont primaryFont;
     public static void load() {
         Texture testTexture = new Texture(Gdx.files.internal("data/square.png"));
         testTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
         primaryFont = new BitmapFont(
                Gdx.files.internal("data/lucidaBitmap.fnt"),
                Gdx.files.internal("data/lucidaBitmap_0.png"), false);
         
         testRegion = new TextureRegion(testTexture, 0, 0, 100, 100);
     }
 
     public static void dispose() {
     }
 }
