 package projectrts.global.utils;
 
 import java.util.HashMap;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.render.NiftyRenderEngine;
 
 /**
  * Handles import of icons for abilities
  * @author Filip Brynfors
  *
  */
 
 
 public enum ImageManager {
 	INSTANCE;
 	private HashMap<String, NiftyImage> images = new HashMap<String, NiftyImage>();
 	
 
 	public void initializeImages(Nifty nifty){
 		NiftyRenderEngine engine = nifty.getRenderEngine();

		images.put("Move", engine.createImage("assets/gui/MoveAbility.bmp", true));
		images.put("Attack", engine.createImage("/assets/gui/AttackAbility.bmp", false));
		
 	}
 	
 	
 	
	public NiftyImage getImage(String name){
 		return images.get(name);
 	}
 
 
 }
 
