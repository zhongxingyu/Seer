 /*
  * Created On: Jun 11, 2006 1:28:06 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.platform.application.window;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.util.Stack;
 
 import javax.swing.border.AbstractBorder;
 
 import com.thinkparity.ophelia.browser.Constants.Images;
 
 /**
  * The thinkParity window border.
  * 
  * @author raymond@thinkparity.com
  * @version $Revision$
  */
 public class WindowBorder extends AbstractBorder {
 
     /** The border insets. */
     private static final Insets BORDER_INSETS;
 
     /** A colour stack used when modifying the graphics object. */
     private static final Stack<Color> COLOUR_STACK;
 
     /** The border colours. */
     private static final Color[] COLOURS;
 
     /** @see java.io.Serializable */
     private static final long serialVersionUID = 1;
 
     static {
        //BORDER_INSETS = new Insets(2, 2, 1, 2);
        BORDER_INSETS = new Insets(6, 6, 5, 6);
         COLOURS = new Color[] {
                 new Color(139, 142, 143, 255),
                 new Color(228, 229, 233, 255),
                 new Color(112, 113, 117, 255)
         };
         COLOUR_STACK = new Stack<Color>();
     }
 
     private static void popColour(final Graphics g) {
         g.setColor(COLOUR_STACK.pop());
     }
 
     private static void pushColour(final Graphics g) {
         COLOUR_STACK.push(g.getColor());
     }
 
     /** Create WindowBorder. */
     public WindowBorder() { super(); }
 
     /**
      * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
      * 
      */
     public Insets getBorderInsets(Component c) {
         return (Insets) BORDER_INSETS.clone();
     }
 
     /**
      * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component,
      *      java.awt.Insets)
      * 
      */
     public Insets getBorderInsets(Component c, Insets insets) {
         insets.top = BORDER_INSETS.top;
         insets.left = BORDER_INSETS.left;
         insets.bottom = BORDER_INSETS.bottom;
         insets.right = BORDER_INSETS.right;
         return insets;
     }
 
     /**
      * @see javax.swing.border.AbstractBorder#getInteriorRectangle(java.awt.Component,
      *      int, int, int, int)
      * 
      */
     public Rectangle getInteriorRectangle(Component c, int x, int y, int width, int height) {
         return super.getInteriorRectangle(c, x, y, width, height);
     }
 
     /**
      * @see javax.swing.border.AbstractBorder#isBorderOpaque()
      * 
      */
     public boolean isBorderOpaque() { return true; }
 
     /**
      * @see javax.swing.border.AbstractBorder#paintBorder(java.awt.Component,
      *      java.awt.Graphics, int, int, int, int)
      * 
      */
     public void paintBorder(final Component c, final Graphics g, final int x,
             final int y, final int width, final int height) {
         pushColour(g);
         
         g.setColor(COLOURS[0]);
         g.drawLine(0, 0, width - 1, 0);                     // top line 1
         g.drawLine(1, 1, 1, height - 2);                    // left line 2
         g.drawLine(width - 2, 1, width - 2, height - 2);    // right line 1
 
         g.setColor(COLOURS[1]);
         g.drawLine(2, 1, width - 3, 1);                     // top line 2
 
         g.setColor(COLOURS[2]);
         g.drawLine(0, 0, 0, height - 1);                    // left line 1
         g.drawLine(width - 1, 0, width - 1, height - 1);    // right line 2
         g.drawLine(0, height - 1, width - 1, height - 1);   // bottom
         
         // These images put borders on rounded corners.
         g.drawImage(Images.BrowserTitle.DIALOG_TOP_LEFT_OUTER,
                 0,
                 0,
                 Images.BrowserTitle.DIALOG_TOP_LEFT_OUTER.getWidth(),
                 Images.BrowserTitle.DIALOG_TOP_LEFT_OUTER.getHeight(), c);
         g.drawImage(Images.BrowserTitle.DIALOG_BOTTOM_LEFT_OUTER,
                 0,
                 height - Images.BrowserTitle.DIALOG_BOTTOM_LEFT_OUTER.getHeight(),
                 Images.BrowserTitle.DIALOG_BOTTOM_LEFT_OUTER.getWidth(),
                 Images.BrowserTitle.DIALOG_BOTTOM_LEFT_OUTER.getHeight(), c);
         g.drawImage(Images.BrowserTitle.DIALOG_TOP_RIGHT_OUTER,
                 width - Images.BrowserTitle.DIALOG_TOP_RIGHT_OUTER.getWidth(),
                 0,
                 Images.BrowserTitle.DIALOG_TOP_RIGHT_OUTER.getWidth(),
                 Images.BrowserTitle.DIALOG_TOP_RIGHT_OUTER.getHeight(), c);
         g.drawImage(Images.BrowserTitle.DIALOG_BOTTOM_RIGHT_OUTER,
                 width - Images.BrowserTitle.DIALOG_BOTTOM_RIGHT_OUTER.getWidth(),
                 height - Images.BrowserTitle.DIALOG_BOTTOM_RIGHT_OUTER.getHeight(),
                 Images.BrowserTitle.DIALOG_BOTTOM_RIGHT_OUTER.getWidth(),
                 Images.BrowserTitle.DIALOG_BOTTOM_RIGHT_OUTER.getHeight(), c);
         
         popColour(g);
     }
 }
