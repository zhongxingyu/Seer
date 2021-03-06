 package ca.nengo.ui.lib.world.handlers;
 
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 
 import edu.umd.cs.piccolo.PCamera;
 import edu.umd.cs.piccolo.event.PInputEvent;
 import edu.umd.cs.piccolo.event.PPanEventHandler;
 import edu.umd.cs.piccolo.util.PBounds;
 import edu.umd.cs.piccolo.util.PDimension;
 
 /**
  * Extend PPanEventHandler so that panning direction can be inverted
  * 
  * @author Shu Wu
  */
 public class PanEventHandler extends PPanEventHandler {
 
 	private boolean isInverted = false;
 
 	public PanEventHandler() {
 		super();
 	}
 
 	/**
 	 * Do auto panning even when the mouse is not moving.
 	 */
 	protected void dragActivityStep(PInputEvent aEvent) {
 		if (!getAutopan())
 			return;
 
 		PCamera c = aEvent.getCamera();
 		PBounds b = c.getBoundsReference();
 		Point2D l = aEvent.getPositionRelativeTo(c);
 		int outcode = b.outcode(l);
 		PDimension delta = new PDimension();
 
 		if ((outcode & Rectangle.OUT_TOP) != 0) {
 			delta.height = validatePanningSpeed(-1.0
 					- (0.5 * Math.abs(l.getY() - b.getY())));
 		} else if ((outcode & Rectangle.OUT_BOTTOM) != 0) {
 			delta.height = validatePanningSpeed(1.0 + (0.5 * Math.abs(l.getY()
 					- (b.getY() + b.getHeight()))));
 		}
 
 		if ((outcode & Rectangle.OUT_RIGHT) != 0) {
 			delta.width = validatePanningSpeed(1.0 + (0.5 * Math.abs(l.getX()
 					- (b.getX() + b.getWidth()))));
 		} else if ((outcode & Rectangle.OUT_LEFT) != 0) {
 			delta.width = validatePanningSpeed(-1.0
 					- (0.5 * Math.abs(l.getX() - b.getX())));
 		}
 
 		c.localToView(delta);
 
 		if (delta.width != 0 || delta.height != 0) {
 			if (isInverted) {
 				c.translateView(-1 * delta.width, -1 * delta.height);
 			} else {
 				c.translateView(delta.width, delta.height);
 			}
 		}
 	}
 
 	public void setInverted(boolean isInverted) {
 		this.isInverted = isInverted;
 	}
 
 	public boolean isInverted() {
 		return isInverted;
 	}
 }
