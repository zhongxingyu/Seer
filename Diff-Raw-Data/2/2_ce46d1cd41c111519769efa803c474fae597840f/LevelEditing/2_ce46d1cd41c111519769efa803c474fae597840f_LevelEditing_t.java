 package games.rts.myRTS;
 
 import vooga.rts.leveleditor.gui.Canvas;
 import vooga.rts.resourcemanager.ImageLoader;
 import vooga.rts.resourcemanager.ResourceManager;
 
 /**
  * level editor class for XML
  * @author Eunsu (Joe) Ryu - jesryu
  *
  */
 public class LevelEditing {
	private static final String RESOURCE_LOCATION = "/vooga/rts/leveleditor/resource/";
 
     public static void main(String[] args) {
     	ImageLoader il = new ImageLoader();
         ResourceManager.getInstance().registerResourceLoader(il);
         ResourceManager.getInstance().setResourceBase(RESOURCE_LOCATION);
         
         // get rid of it at the end - won't use this
         Canvas c = new Canvas();
     }
 }
