 /*----------------------------------------------------------------------------
  This file is part of BasicJ.
 
  BasicJ is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  BasicJ is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with BasicJ; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  ----------------------------------------------------------------------------*/
 package basicj;
 
 import java.util.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 import javax.swing.*;
 import basicj.util.*;
 
 /**
  * Handles the visual and event based BasicJ commands.
  * 
  * The print, println, point, line, circle, color, screen, width, height, text, 
  * flush, input, zoom, keypressed, and mouseclicked commands are implemented in
  * this class.
  * 
  * The commands input, zoom, keypressed, and mouseclicked are not 
  * implmented yet.
  * 
  * @author Ronald Chen
  */
 final class Screen extends JComponent {	
 	
 	/**
 	 * The back buffer of the screen.
 	 * The final image is composed in this buffer from the print commands and
      * the draw buffers.  If there were no updates, the paint() method just 
      * paints this buffer.
 	 */
 	private BufferedImage backBuffer;
 	
 	/**
 	 * The back buffer for draw commands.
 	 * This layer is where point, line, circle, and text commands are rendered.
 	 * This buffer is only cleared when the clear command is called.  Even when
 	 * a screen command is given, the draw buffer is copied onto the new draw 
 	 * buffer.
 	 */
 	private BufferedImage drawBuffer;
     
     /**
      * The Graphics for the drawBuffer.
      * This is updated automatically by the resetBuffers method.
      * @see #drawBuffer
      */
     private Graphics2D drawGraphics;
 	
 	/**
 	 * Holds the input from print commands.
 	 * This list also stores the current foreground color, for rendering 
 	 * later.  The clear command empties this buffer.
 	 */
	private java.util.List<Pair<Color, StringBuffer>> printBuffer;
 	
 	/**
 	 * Holds the last print command.
 	 * This Pair is always the last element in the printBuffer.  
 	 * It is replaced with a new one if the foreground color has changed 
 	 * since the last print command.
 	 * @see #printBuffer
 	 */
 	private Pair<Color, StringBuffer> lastPrint;
 	
 	/**
 	 * Holds formated version of the printBuffer.
 	 * Elements in this list are the same elements in the printBuffer, except
 	 * that '\n' characters are converted into hardBreaks and long
 	 * lines are broken up to the screen width using softBreaks.
 	 * This list is cleared when the clear command is called.
 	 * @see #printBuffer
      * @see #hardBreak
      * @see #softBreak
 	 */
 	private java.util.List<Pair<Color, StringBuffer>> formatedPrintBuffer;
 	
 	/**
 	 * Represents a hard new line break.
 	 * This Pair is found in the formatedPrintBuffer.  It 
 	 * separates lines of text where '\n' characters are found.
 	 * @see #softBreak
      * @see #formatedPrintBuffer
 	 */
 	private static final Pair<Color, StringBuffer> hardBreak = new Pair<Color, StringBuffer>(null, null);
 	
 	/**
 	 * Represents a soft new line break.
 	 * This Pair is found in the formatedPrintBuffer.  It 
 	 * separates long lines of text on to new lines.
 	 * @see #softBreak
      * @see #formatedPrintBuffer
 	 */
 	private static final Pair<Color, StringBuffer> softBreak = null;
 	
 	/**
 	 * The inital width of the screen.
 	 */
 	private static final int INITAL_WIDTH = 500;
 	
 	/**
 	 * The inital height of the screen.
 	 */
 	private static final int INITAL_HEIGHT = 500;
 	
 	/**
 	 * The inital foreground color.
 	 */
 	private static final Color INITAL_FOREGROUND = Colors.toColor(Colors.white);
 	
 	/**
 	 * The inital background color.
 	 */
 	private static final Color INITAL_BACKGROUND = Colors.toColor(Colors.black);
 	
 	/**
 	 * A transparent color.
 	 * It is set to a transparent blue, just in case this is painted to an
 	 * image that does not support transparency.  Thus the blue will show
 	 * the error.
 	 */
 	private static final Color TRANSPARENT = new Color(0, 0, 255, 0);
 	
 	/**
 	 * The fixed-width font used to render print and text commands.
 	 */
 	private static final Font f = new Font("Courier New", Font.PLAIN, 12);
 
     /**
      * Inital zoom factor.
      */
     private static final int INITAL_ZOOMFACTOR = 1;
 	
 	/**
 	 * The character width of f.
 	 */
 	private final int charWidth;
 	
 	/**
 	 * The character height of f.
 	 */
 	private final int charHeight;
 	
 	/**
 	 * The character ascent of f.
 	 */
 	private final int charAscent;
 	
 	/**
 	 * The current foreground color.
 	 * This variable should not be set directly in any method other than 
 	 * color().
 	 */
 	private Color fgColor;
 	
 	/**
 	 * The current background color.
 	 */
 	private Color bgColor;
 
     /**
      * The current zoom factor.
      */
     private int zoomFactor;
 
     /**
      * The current width.
      */
     private int bufferWidth;
 
     /**
      * The current height.
      */
     private int bufferHeight;
 
 	 
 	
 	/**
 	 * Creates a new screen.
 	 * The new screen has a default size of INITAL_WIDTH by INITAL_HEIGHT with 
 	 * the foreground color INITAL_FOREGROUND and background color 
 	 * INITAL_BACKGROUND.
 	 */
 	public Screen() {
 		super();
 		setLayout(null);
 		fgColor = INITAL_FOREGROUND;
 		bgColor = INITAL_BACKGROUND;
 		
 		FontMetrics fm = getFontMetrics(f);
 		charWidth = fm.charWidth('a');
 		charHeight = fm.getHeight();
 		charAscent = fm.getAscent();
 		
 		printBuffer = new LinkedList<Pair<Color, StringBuffer>>();
 		lastPrint = new Pair<Color, StringBuffer>(fgColor, new StringBuffer());
 		printBuffer.add(lastPrint);
 		formatedPrintBuffer = new LinkedList<Pair<Color, StringBuffer>>();
 		
         zoomFactor = INITAL_ZOOMFACTOR;
 		screen(INITAL_WIDTH, INITAL_HEIGHT);
 	}
 	
 	/**
 	 * Creates a new buffer set.
 	 * The fields backBuffer and drawBuffer are initalized with
 	 * new BufferedImage objects.  IE. The old buffers are lost.  drawGraphics
      * is also updated to the new drawBuffer's Graphics as a side affect.
      * 
      * @see #backBuffer
      * @see #drawBuffer
      * @see #drawGraphics
 	 */
 	private void resetBuffers() {
 		backBuffer = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
 		drawBuffer = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
         
         drawGraphics = (Graphics2D) drawBuffer.getGraphics();
 		clearBuffers();
 	}
 	
 	/**
 	 * Clears all buffers.
 	 * The backBuffer is reset to bgColor.
      * 
      *  @see #backBuffer
      *  @see #bgColor
 	 */
 	private void clearBuffers() {
 		Graphics g = backBuffer.getGraphics();
 		g.setColor(bgColor);
 		g.fillRect(0, 0, bufferWidth, bufferHeight);
 
         drawGraphics.setColor(TRANSPARENT);
         drawGraphics.fillRect(0, 0, bufferWidth, bufferHeight);
         drawGraphics.setColor(fgColor);
 	}
 
 	/**
 	 * Paints the visual commands.
 	 */
 	public void paint(Graphics g) {
 		updateBackBuffer();
         if(zoomFactor > 1) {
             g.drawImage(backBuffer.getScaledInstance(zoomFactor*bufferWidth, zoomFactor*bufferHeight, Image.SCALE_REPLICATE), 0, 0, null);
         } else {
             g.drawImage(backBuffer, 0, 0, null);
         }
 	}
 	
 	/**
 	 * Updates the back buffer.
 	 * Here is where print commands and drawBuffer is composed on the 
      * backBuffer.
      * 
      * @see #drawBuffer
      * @see #backBuffer
 	 */
 	public void updateBackBuffer() {
         formatPrintBuffer();
         
 		Graphics g = backBuffer.getGraphics();
         int x = 0;
         int y = 0;
         
         g.setColor(bgColor);
         g.fillRect(0, 0, bufferWidth, bufferHeight);
         g.setFont(f);
         Pair<Color, StringBuffer> p;
         synchronized(formatedPrintBuffer) {
             Iterator<Pair<Color, StringBuffer>> it = formatedPrintBuffer.iterator();
             while(it.hasNext()) {
                 p = it.next();
                 if(p != hardBreak && p != softBreak) {
                     g.setColor(p.getX());
                     g.drawString(p.getY().toString(), x, y + charAscent);
                     x += charWidth*p.getY().length();                       
                 } else {
                     x = 0;
                     y += charHeight;                        
                 }
             }
         }
 		g.drawImage(drawBuffer, 0, 0, null);
 	}
     
     /**
      * Just calls repaint.
      * 
      * @see #paint
      */
     public void flush() {
         repaint();
     }
 	
 	/**
 	 * Formats printBuffer into formatedPrintBuffer.
 	 * Any Pair in the printBuffer with the character '\n' in the String is
 	 * separated into two Pairs and a hardBreak between them.  If the String in
 	 * the Pair is longer than the width of the screen minus the current 
 	 * position of the cursor, it is separated into Pairs, so not to exceede 
 	 * the screen width.  Between every new pair is a softBreak.  Otherwise, 
 	 * Pair is just copied.  The result is stored in the formatedPrintBuffer.
 	 * If the total number of lines in the formatedPrintBuffer exceedes the 
 	 * current height of the screen, then the lines at the top are trucated 
 	 * until the total number of lines is equal to the current height of the 
 	 * screen.  If this height trunction occurs, printBuffer is updated 
 	 * accordingly.  IE. The oldest data in the printBuffer is lost.
      * 
 	 * @see #formatedPrintBuffer
      * @see #printBuffer
      * @see #hardBreak
      * @see #softBreak
 	 */
 	private void formatPrintBuffer() {
 		final int charsPerWidth = bufferWidth/charWidth;
 		synchronized(formatedPrintBuffer) {
 			formatedPrintBuffer.clear();
 			
 			Iterator<Pair<Color, StringBuffer>> it = printBuffer.iterator();
 			StringTokenizer st;
 			String s;
 			int nLines = 0;
 			Pair<Color, StringBuffer> curr;
 			int cursorX = 0;
 			while(it.hasNext()) {
 				curr = it.next();
 				st = new StringTokenizer(curr.getY().toString(), "\n", true);
 				while(st.hasMoreTokens()) {
 					s = st.nextToken();
 					if(s.equals("\n")) {
 						formatedPrintBuffer.add(hardBreak);
 						cursorX = 0;
 						nLines++;
 					} else {
     					while(charWidth*(s.length() + cursorX) > bufferWidth) {
     						formatedPrintBuffer.add(new Pair<Color, StringBuffer>(curr.getX(), new StringBuffer(s.substring(0, charsPerWidth - cursorX))));
     						s = s.substring(charsPerWidth - cursorX);
     						formatedPrintBuffer.add(softBreak);
     						cursorX = 0;
     						nLines++;
     					}
     					formatedPrintBuffer.add(new Pair<Color, StringBuffer>(curr.getX(), new StringBuffer(s)));
     					cursorX += s.length();
                     }
 				}
 			}
 
 			if(nLines > 0 && nLines*charHeight >= bufferHeight) {
 				while(nLines*charHeight >= getHeight()) {
 					while(formatedPrintBuffer.get(0) != hardBreak && formatedPrintBuffer.get(0) != softBreak) {
 						formatedPrintBuffer.remove(0);
 					}
 					formatedPrintBuffer.remove(0);
 					nLines--;
 				}
 				printBuffer.clear();
 				it = formatedPrintBuffer.iterator();
 				Pair<Color, StringBuffer> last = new Pair<Color, StringBuffer>(fgColor, new StringBuffer());
 				printBuffer.add(last);
 				while(it.hasNext()) {
 					curr = it.next();
 					if(curr == hardBreak) {
 						last.getY().append("\n");
 					} else if(curr == softBreak) {
 						if(it.hasNext()) {
 							curr = it.next();
 							last.getY().append(curr.getY().toString());
 						}
 					} else {
 						printBuffer.add(curr);
 						last = curr;
 					}
 				}
 				lastPrint = last;
 			}
 		}
 	}
 
 	/**
 	 * Implements the clear command.
 	 * All print and draw commands are lost.  The foreground and background
 	 * colors are unchanged.
 	 */
 	public void clear() {
 		printBuffer.clear();
 		lastPrint = new Pair<Color, StringBuffer>(fgColor, new StringBuffer());
 		printBuffer.add(lastPrint);
 		formatedPrintBuffer.clear();
		clearBuffers();
         flush();
 	}
 	
 	/**
 	 * Implements the color command, with optinal background.
 	 * Sets the background color to c.
 	 */
 	public void clear(Color c) {
 		bgColor = c;
 		clear();
 	}
 	
 	/**
 	 * Implements the color command.
 	 * The foreground color is set to c.
 	 */
 	public void color(Color c) {
 		fgColor = c;
         drawGraphics.setColor(fgColor);
 	}
     
     /**
      * Implements the thickness command.
      */
     public void thickness(int width) {
         drawGraphics.setStroke(new BasicStroke(width));
     }
     
 	/**
 	 * Implements the width command.
      * 
 	 * @return the current width of the screen
 	 */
 	public int width() {
         return bufferWidth;
 	}
 	
 	/**
 	 * Implements the height command.
      * 
 	 * @return the current height of the screen
 	 */
 	public int height() {
         return bufferHeight;
 	}
 	
 	/**
 	 * Implements the screen command.
      * The screen is resized to the new width and height.  If there was
      * anything drawn, then it is copied to the new drawBuffer.
      * 
 	 * @param width the new width
 	 * @param height the new height
 	 */
 	public void screen(int width, int height) {
         bufferWidth = width;
         bufferHeight = height;
         zoom(zoomFactor);
 		
 		BufferedImage tempDrawBuffer = drawBuffer;
 		resetBuffers();
         drawGraphics.drawImage(tempDrawBuffer, 0, 0, null);
 	}
 
 	/**
 	 * Implements the print command.
 	 * All other types can be casted to type String.
 	 */
 	public void print(String s) {
 		if(lastPrint.getX() == fgColor) {
 			lastPrint.getY().append(s);
 		} else {
 			lastPrint = new Pair<Color, StringBuffer>(fgColor, new StringBuffer(s));
 			printBuffer.add(lastPrint);
 		}
 	}
 	
 	/**
 	 * Implements the point command.
 	 */
 	public void point(int x, int y) {
         drawGraphics.drawLine(x, y, x, y);
 	}
 	
 	/**
 	 * Implements the line command.
 	 */
 	public void line(int x0, int y0, int x1, int y1) {
         drawGraphics.drawLine(x0, y0, x1, y1);
 	}
 	
 	/**
 	 * Implements the circle command.
 	 */
 	public void circle(int x, int y, int r) {
 		if(r == 0) {
             point(x, y);
 		} else {
             drawGraphics.drawOval(x - r, y - r, 2*r, 2*r);
 		}
 	}
 	
 	/**
 	 * Implements the text command.
 	 */
 	public void text(int x, int y, String s) {
         drawGraphics.drawString(s, x, y + charAscent);
 	}
     
     /**
      * Implments the zoom command.
      * @param factor must be greater than 0
      */
     public void zoom(int factor) {
         if(zoomFactor > 0) {
             zoomFactor = factor;
             setPreferredSize(new Dimension(zoomFactor*bufferWidth, zoomFactor*bufferHeight));
             invalidate();
         }
     }
 	
 }
