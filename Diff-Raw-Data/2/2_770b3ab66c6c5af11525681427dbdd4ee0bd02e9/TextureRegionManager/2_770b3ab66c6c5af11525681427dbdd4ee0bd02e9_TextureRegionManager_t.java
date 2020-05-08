 package org.rubenrr.walkeitor.manager;
 
 import android.util.Log;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.bitmap.BitmapTexture;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.util.adt.io.in.IInputStreamOpener;
 import org.rubenrr.walkeitor.config.ElementConfig;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 /**
  *
  * This class preload all the bitmaps that will be converted to
  * TextureRegion following the AndEngine conventions
  *
  * User: Ruben Rubio Rey
  * Date: 1/04/13
  * Time: 6:58 PM
  */
 public class TextureRegionManager extends HashMap<String, TextureRegion> {
 
     private static TextureRegionManager instance = null;
 
     public static TextureRegionManager getInstance() {
         if (instance == null) {
            synchronized(TextureRegionManager .class) {
                 if (instance == null) {
                     instance = new TextureRegionManager();
                 }
             }
         }
         return instance;
     }
 
     /**
      * Load bitmaps and keeps it in a NavigableMap.
      * The bitmap will be stored in memory as a TextureRegionManager
      *
      * @param element Element config item
      */
     //public void put (String key, String path) {
     public void put (final ElementConfig element) {
         final String key = element.toString();
         Log.d("TextureRegionManager", "PUT: " + key);
         if (this.get(key) == null) {
             TextureRegion value = this.getTexttureRegionFromBitmap(element);
             Log.d("TextureRegionManager", "Put " + key + " is empty, storing value " + value);
             super.put(key, value);
         }
     }
 
     public TextureRegion get (ElementConfig element) {
         final String key = element.toString();
         TextureRegion value = (TextureRegion)super.get(key);
         Log.d("TextureRegionManager", "GET: key " + key + " value " + value);
         if ( value == null ) {
             Log.e("TextureRegionManager", "Getting " + key + " not loaded");
         }
         return value;
     }
 
     private TextureRegion getTexttureRegionFromBitmap( final ElementConfig element) {
 
         final String path = element.getSpriteNormalPath();
         ITexture mTexture;
 
         TextureRegion texttureregion = null;
 
         // get the city
         try {
             mTexture = new BitmapTexture(SceneManager.getInstance().getTextureManager(), new IInputStreamOpener() {
                 @Override
                 public InputStream open() throws IOException {
                     return SceneManager.getInstance().getAssetManager().open(path);
                 }
             });
 
             mTexture.load();
             texttureregion = TextureRegionFactory.extractFromTexture(mTexture);
 
         } catch (IOException e) {
             Log.e("TextureRegionManager", "Error loading texture " + path, e);
         }
 
         return texttureregion;
 
     }
 }
