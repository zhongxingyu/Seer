 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.font.GlyphVector;
 
 
 /**
  * Class extending Tool which defines how to draw text on the canvas.
  *
  * The user selects the starting point with the mouse and then types in the
  * desired text.  Currently, the font is not selectable.  A new text box will
  * begin when the user clicks on a different canvas location.  Changing the
  * current tool, in effect, also stops current text entry.
  */
 public class TextTool extends Tool {
 
     /** Associated Drawing canvas */
     protected DrawingCanvas canvas;
     /** Starting position for text to be drawn */
     protected Point startingPosition;
     /** StringBuffer containing currently drawn text */
     protected StringBuffer text;
     /** Font being used */
     protected Font font;
     /** Graphics used to draw text*/
     protected Graphics iBGraphics;
     /** */
     protected GlyphVector vect;
     /** Font metrics of graphics being used*/
     protected FontMetrics fm;  
     protected boolean started = false; 
 
     /**
      * < Constructor >
      * Constructor for class TextTool
      * @param c The associated drawing canvas
      */
     public TextTool(DrawingCanvas c) {
         if (c != null) {
             canvas = c;
             font = new Font("Serif", Font.BOLD, 24);
             iBGraphics = canvas.getImageBufferGraphics(); // ITER 4
         } else {
             throw new IllegalArgumentException();
         }
     }
     
     /**
      * Event Handler method for mouse pressed
      * 
      * Returns focus to the drawing canvas and stores the starting location for
      * the text display.
      * @param e The mouse event
      * @see tools.Tool#mousePressed(java.awt.event.MouseEvent)
      */
     public void mousePressed(MouseEvent e) {
 
         if (started == true) { // ITER 4
             Rectangle dims = new Rectangle(startingPosition.x,
                     startingPosition.y,
                     (int) fm.getStringBounds(text.toString(), iBGraphics).getWidth(),
                     (int) fm.getStringBounds(text.toString(), iBGraphics).getHeight());
             if (dims.contains(e.getPoint()) == false && text.length() != 0) {
                 //fm = iBGraphics.getFontMetrics(font); //Removed/Changed ITER 4
                 drawText();
             }
 
         } // ITER 4
         //ITER 4
         else {
             focus();
             startingPosition = e.getPoint();
             iBGraphics = canvas.getImageBufferGraphics();
             iBGraphics.setFont(font);
             text = new StringBuffer();
             started = true; // ITER 4
         }
         // ITER 4
     }
 
   /**
    * Event handler method for key pressed
    *
    * Adds a character to the string buffer
    * @param e The key event
    * @see tools.Tool#keyPressed(java.awt.event.KeyEvent)
    */
     public void keyPressed(KeyEvent e) {
         if (e.getKeyChar() == e.VK_ENTER) {
             //canvas.transferFocusBackward();
             if (text.length() != 0) { // ITER 4
                 fm = iBGraphics.getFontMetrics(font); //Removed/Changed ITER 4
                 drawText();
             } // ITER 4
         } else if (e.getKeyChar() == e.VK_BACK_SPACE && text.length() != 0) {
             fm = iBGraphics.getFontMetrics(font);
             canvas.clearImageBuffer();
             text.deleteCharAt(text.length() - 1);
             iBGraphics.drawString(text.toString(), startingPosition.x, //Removed/Changed ITER 4
                     startingPosition.y + fm.getHeight() - 11);
             canvas.repaint();
        } else if (started == true) {
             fm = iBGraphics.getFontMetrics(font);
             char nextChar = e.getKeyChar();
             text.append(nextChar);
             iBGraphics.drawString(text.toString(), startingPosition.x, //Removed/Changed ITER 4
                     startingPosition.y + fm.getHeight() - 11); // Changed/Removed ITER 4
             canvas.repaint();
         }
     }
 
     /**
      * Method to add text shape to the object stack when it is finished being drawn
      */
     public void drawText() {
 
         ShapeClass textShape = new ShapeClass(startingPosition.x,
                 startingPosition.y,
                 (int) fm.getStringBounds(text.toString(), iBGraphics).getWidth(),
                 (int) fm.getStringBounds(text.toString(), iBGraphics).getHeight(),
                 canvas.getPenColor(),
                 canvas.getFillColor(),
                 new Textangle(text.toString(),
                 startingPosition.x,
                 startingPosition.y,
                 (int) fm.getStringBounds(text.toString(), iBGraphics).getWidth(),
                 (int) fm.getStringBounds(text.toString(), iBGraphics).getHeight()),
                 true, font.getSize()); // ITER 4
         textShape.setText(text.toString());
         canvas.addShape(textShape);
         started = false;
     }
 
     
     /**
      * Method to set the font size being used
      * @param size the new font size
      */
     public void setFontSize(int size) {
         font = new Font("Serif", Font.BOLD, size); // Set font size - Tiny = 16, Small = 20, Medium = 24, Large = 28, Extra Large = 32
     }
 
     /**
      * Method to request focus from the drawing canvas
      */
     public void focus() {
         canvas.requestFocus();
     }
 }// end public class TextTool extends Tool
