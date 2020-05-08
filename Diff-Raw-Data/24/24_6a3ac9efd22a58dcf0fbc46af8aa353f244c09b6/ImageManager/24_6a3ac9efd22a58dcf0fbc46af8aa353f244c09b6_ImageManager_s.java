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
		images.put("MoveAbility", engine.createImage("assets/interface/move.bmp", false));
		images.put("AttackAbility", engine.createImage("assets/interface/attack.bmp", false));
 	}
 	
 	
 	
	public NiftyImage getTexture(String name){
 		return images.get(name);
 	}
 
 
 }
 
