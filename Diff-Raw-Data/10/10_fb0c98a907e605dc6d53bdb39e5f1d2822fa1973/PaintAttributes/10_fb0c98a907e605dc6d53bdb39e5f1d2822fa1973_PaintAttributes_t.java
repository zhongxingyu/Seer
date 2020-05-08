 package vooga.towerdefense.action.actionlist;
 
import java.awt.Color; 
 import java.awt.Graphics2D;
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.attributes.Attribute;
 import vooga.towerdefense.gameelements.GameElement;
 
 /**
  * This is a test paint attribute action. Not a core game element component, but
  * should be worked on further given more time.
  * 
  * @author Matthew Roy
  * 
  */
 
 public class PaintAttributes extends Action {
 
 	private Graphics2D myPen;
 	private GameElement myElement;
 	private Attribute myAttribute;
 
 	/**
      * 
      */
 	public PaintAttributes(Graphics2D pen, GameElement element,
 			Attribute toPaint) {
 		myPen = pen;
 		myElement = element;
 		myAttribute = toPaint;
 	}
 
 	/**
 	 * Paints health bar or equivalent Needs to figure out a way to paint bar
 	 * without pen. Currently hardcoded.
 	 * 
 	 * @param elapseTime
 	 */
 	@Override
 	public void executeAction(double elapseTime) {
 		if (myPen != null) {
 			myPen.setColor(Color.red);
 			// TODO: THIS IS VERY SPECIFIC FOR TESTING, WE WILL FIGURE OUT
 			// BETTER WAY TO FIT THE BAR
 			myPen.fillRect((int) myElement.getX(), (int) myElement.getY()
 					- (int) myElement.getHeight() / 2, (int) (myElement
 					.getWidth() * (myAttribute.getValue() / myAttribute
 					.getOriginalValue())), (int) myElement.getHeight() / 10);
 		}
 	}
 }
