 package org.fruct.oss.russianriddles.elements;
 
 import javax.microedition.lcdui.CustomItem;
 import javax.microedition.lcdui.Graphics;
 
 import org.fruct.oss.russianriddles.Menu;
 
 import com.nokia.mid.ui.gestures.GestureEvent;
 import com.nokia.mid.ui.gestures.GestureInteractiveZone;
 import com.nokia.mid.ui.gestures.GestureListener;
 import com.nokia.mid.ui.gestures.GestureRegistrationManager;
 
 //import com.nokia.example.composedui.views.elements.GridLayout;
 
 public class MenuItem extends CustomItem implements GestureListener {
 
     private static final int HILIGHT_COLOR = 0x29a7cc;
     private static final int BACKGROUND_COLOR = 0xf4f4f4;
     private static final int TEXT_COLOR_NOT_SELECTED = 0x595959;
     private static final int TEXT_COLOR_SELECTED = 0xfdfefe;
     public static final int JITTER_THRESHOLD = 10;
 
 
     protected int width = 0;
     protected int height = 0;
     protected String text = null;
     protected boolean highlight = false;
     
    private int lastX = 0;
    private int lastY = 0;
    
     private Menu mainMenu = null;
 
     private MenuItem() {
     	super(null);
     }
     
     public MenuItem(String text, final int width, final int height)
     {
     	this();
     	this.text = text;
     	this.width = width;
     	this.height = height;
     	
     	GestureInteractiveZone gis = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP | 256);
     	GestureRegistrationManager.register(this, gis);
     	GestureRegistrationManager.setListener(this, this);
     }
     
     public void setMenu(Menu newMenu) {
     	this.mainMenu = newMenu;
     }
 
 	protected int getMinContentHeight() {
 		return height;
 	}
 
 	protected int getMinContentWidth() {
 		return width;
 	}
 
 	protected int getPrefContentHeight(int width) {
 		return height;
 	}
 
 	protected int getPrefContentWidth(int height) {
 		return width;
 	}
 
     public String getString() {
         return text;
     }
 
     public void setString(String text) {
         this.text = text;
     }
 
     public boolean getHighlight() {
         return highlight;
     }
 
     public void setHighlight(boolean highlight) {
         this.highlight = highlight;
     }
 
     public void setSize(final int width, final int height) {
         this.width = width;
         this.height = height;
     }
 
 
 	protected void paint(Graphics graphics, int w, int h) {
 
         // Paint the background
         if (highlight) {
             graphics.setColor(HILIGHT_COLOR);
         }
         else {
             graphics.setColor(BACKGROUND_COLOR);
         }
         
         graphics.fillRect(0, 0, width, height);
 
         // Paint the text
         if (highlight) {
             graphics.setColor(TEXT_COLOR_SELECTED);
         }
         else {
             graphics.setColor(TEXT_COLOR_NOT_SELECTED);
         }
 
         graphics.drawString(text,
                 width / 2,
                 (height - graphics.getFont().getHeight()) / 2,
                 Graphics.TOP | Graphics.HCENTER);
 
 	}
 
 	public void gestureAction(Object container,
 			GestureInteractiveZone gestureInteractiveZone,
 			GestureEvent gestureEvent) {
 
 		System.err.println("gestureAction: " + String.valueOf(gestureEvent.getType()));
 			this.mainMenu.itemClicked(this);
 		
 	}
 
 //    /**
 //     * @see javax.microedition.lcdui.CustomItem#pointerPressed(int, int)
 //     */
 //    protected void pointerPressed(int x, int y) {
 //        lastX = x;
 //        lastY = y;
 //        highlight = true;
 //        repaint();
 //    }
 //
 //    /**
 //     * @see javax.microedition.lcdui.CustomItem#pointerDragged(int, int)
 //     */
 //    protected void pointerDragged(int x, int y) {
 //        if (Math.abs(lastX - x) > JITTER_THRESHOLD
 //            || Math.abs(lastY - y) > JITTER_THRESHOLD)
 //        {
 //            // Too much jitter. Lose the focus.
 //            highlight = false;
 //            repaint();
 //        }
 //    }
 //
 //    /**
 //     * @see javax.microedition.lcdui.CustomItem#pointerReleased(int, int)
 //     */
 //    protected void pointerReleased(int x, int y) {
 //        if (highlight) {
 //            this.notifyStateChanged();
 //            highlight = false;
 //            repaint();
 //            
 //            if (this.mainMenu != null) {
 //            	this.mainMenu.itemClicked(this);
 //            	this.pointerReleased(x, y);
 //            }
 //        }
 //    }
 
 }
