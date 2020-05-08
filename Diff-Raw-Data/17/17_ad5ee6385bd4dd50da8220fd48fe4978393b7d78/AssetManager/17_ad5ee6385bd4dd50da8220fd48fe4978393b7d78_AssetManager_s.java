 package org.unbiquitous.ubiengine.engine.asset;
 
 import java.awt.Font;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 import org.unbiquitous.ubiengine.engine.core.GameComponents;
import org.unbiquitous.ubiengine.engine.core.UbiGame.Settings;
 
 /**
  * Class to manage assets of a game state.
  * @author Pimenta
  *
  */
 public final class AssetManager {
   /**
    * Load a texture.
    * @param fn Texture path.
    * @return Texture loaded.
    */
   public Texture getTexture(String fn) {
     Texture asset = (Texture)assets.get(fn);
     if (asset != null)
       return asset;
     
     try {
       asset = TextureLoader.getTexture(getFormat(fn),
         ResourceLoader.getResourceAsStream(
          GameComponents.get(Settings.class).get("root_path") + "/" + fn
         )
       );
     } catch (IOException e) {
       throw new Error(e);
     }
     
     assets.put(fn, asset);
     return asset;
   }
   
   /**
    * Load audio.
    * @param fn Audio path.
    * @return Audio loaded.
    */
   public Audio getAudio(String fn) {
     Audio asset = (Audio)assets.get(fn);
     if (asset != null)
       return asset;
     
     try {
       asset = AudioLoader.getAudio(getFormat(fn),
         ResourceLoader.getResourceAsStream(
          GameComponents.get(Settings.class).get("root_path") + "/" + fn
         )
       );
     } catch (IOException e) {
       throw new Error(e);
     }
     
     assets.put(fn, asset);
     return asset;
   }
   
   /**
    * Load streaming audio.
    * @param fn Audio path.
    * @return Audio loaded.
    */
   public Audio getStreamingAudio(String fn) {
     Audio asset = (Audio)assets.get(fn);
     if (asset != null)
       return asset;
     
     try {
       asset = AudioLoader.getStreamingAudio(getFormat(fn),
         ResourceLoader.getResource(
          GameComponents.get(Settings.class).get("root_path") + "/" + fn
         )
       );
     } catch (IOException e) {
       throw new Error(e);
     }
     
     assets.put(fn, asset);
     return asset;
   }
   
   /**
    * Load font.
    * @param fn Font path.
    * @return Font loaded.
    */
   public Font getFont(String fn) {
     Font asset = (Font)assets.get(fn);
     if (asset != null)
       return asset;
     
     try {
       asset = Font.createFont(Font.TRUETYPE_FONT,
         ResourceLoader.getResourceAsStream(
          GameComponents.get(Settings.class).get("root_path") + "/" + fn
         )
       );
     } catch (Exception e) {
       throw new Error(e);
     }
     
     assets.put(fn, asset);
     return asset;
   }
   
   /**
    * Load map from a text file.
    * @param fn Text file path.
    * @return Map loaded.
    */
   public int[][] getMap(String fn) {
     int[][] asset = (int[][])assets.get(fn);
     if (asset != null)
       return asset;
     
     Scanner sc;
     try {
       sc = new Scanner(new FileReader(
        GameComponents.get(Settings.class).get("root_path") + "/" + fn
       ));
     } catch (FileNotFoundException e) {
       throw new Error(e);
     }
     sc.useDelimiter(",");
     int rows = sc.nextInt();
     int cols = sc.nextInt();
     sc.nextLine();
     asset = new int[rows][cols];
     for (int i = 0; i < rows; i++) {
       for (int j = 0; j < cols; j++)
         asset[i][j] = sc.nextInt();
       sc.nextLine();
     }
     sc.close();
     
     assets.put(fn, asset);
     return asset;
   }
 //==============================================================================
 //nothings else matters from here to below
 //==============================================================================
   private static String getFormat(String fn) {
     int i;
     for (i = fn.length() - 1; i >= 0 && fn.charAt(i) != '.'; i--);
     if (i < 0)
       throw new Error("Invalid path for asset: " + fn);
     String fmt = "";
     for (int j = i + 1; j < fn.length(); j++)
       fmt += fn.charAt(j);
     return fmt.toUpperCase();
   }
   
   private HashMap<String, Object> assets = new HashMap<String, Object>();
 }
