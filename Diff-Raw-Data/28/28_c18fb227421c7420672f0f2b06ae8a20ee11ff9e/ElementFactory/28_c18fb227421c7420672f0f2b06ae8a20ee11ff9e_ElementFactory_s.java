 import processing.core.PApplet;
 import processing.core.PVector;
 
 /**
  * Factory class to generate Actor objects
  */
 public class ElementFactory {
 	public enum ElementType {
   		GAME_BALL, FORCE_OBJECT, PORTALS, SCATTERER
 	}
 
 	private static ElementType currentType = ElementType.FORCE_OBJECT;
 	private static PApplet parent;
 
 	public static void setParent(PApplet p) {
 		parent = p;
 	}
 
 	public static void setType(ElementType t) {
 		currentType = t;
 	}
 
 	public static ElementType getType() {
 		return currentType;
 	}
 
 	public static void spawnElement() {
 		switch(currentType) {
 			case GAME_BALL:
 				GameBall tempgb = new GameBall(parent, "ballgame.GameBall#" + GameBall.getNumber(), new PVector(parent.mouseX, parent.mouseX), new PVector(0,0));
 				break;
 	
 			case FORCE_OBJECT:
 				ForceObject tempfo = new ForceObject(parent, "ballgame.ForceObject#" + ForceObject.getNumber(), new PVector(parent.mouseX, parent.mouseY), 0, 0);
 				break;
 
 			case PORTALS:
 				Portals temppo = new Portals(parent, "ballgame.Portals#" + Portals.getNumber(), new PVector(parent.mouseX, parent.mouseY));
 				break;
 
 			case SCATTERER:
				Scatterer tempsc = new Scatterer(parent, "ballgame.Scatterer#" + Scatterer.getNumber(), new PVector(parent.mouseX, parent.mouseX));
 				break;			
 		}
 	}
 }
