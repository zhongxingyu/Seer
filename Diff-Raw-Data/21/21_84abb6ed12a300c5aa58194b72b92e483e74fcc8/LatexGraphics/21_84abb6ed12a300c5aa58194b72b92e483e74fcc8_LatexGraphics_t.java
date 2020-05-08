 /*
  * Paints latex on a Graphics.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/LatexGraphics.java,v 2.0 2011/09/25 12:37:13 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.MediaTracker;
 
 import teo.isgci.util.Latex;
 import teo.isgci.util.LatexGlyph;
 
 public class LatexGraphics extends Latex {
 
     /**
      * The font used for drawing latex strings.
      */
     private Font font;
 
     /**
      * The singleton instance of this class.
      */
     private static LatexGraphics latexgraphics;
     
     /**
      * Indicates whether this class was initialized already.
      */
     private static boolean isInitialized;
 
     /**
      * Creates a new instance of LatexGraphics. It's private because
      * access will be handled over {@link #getInstance()} as part of
      * the singleton design.
      */
     private LatexGraphics() {
         font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
     }
 
     /**
      * Returns the current instance of LatexGraphics or creates a new
      * one if none exists yet.
      * 
      * @return
      *          The singleton LatexGraphics.
      */
     public static LatexGraphics getInstance() {
         
        if (latexgraphics == null) {
            latexgraphics = new LatexGraphics();
        }
        
        return latexgraphics;
     }
     
     /**
      * Load the images for the glyphs.
      * 
      * @param loader
      *          The loader used to initialize this class.
      */
     public static void init(teo.Loader loader) {
         // can only be initialized once
         if (isInitialized) {
             return;
         }
         
         isInitialized = true;
         
         MediaTracker tracker = null;
         LatexGlyph[] glyphs = LatexGlyph.getGlyphs();
         try {
             for (int i = 0; i < glyphs.length; i++) {
                 if (glyphs[i].getImage() == null  
                         && !glyphs[i].getImageName().equals("")) {
                     glyphs[i].setImage(loader.getImage(
                                 "images/" + glyphs[i].getImageName()));
                     if (tracker == null) {
                         tracker = new MediaTracker(ISGCIMainFrame.tracker);
                     }
                     tracker.addImage(glyphs[i].getImage(), 1);
                 }
             }
             if (tracker != null) {
                 tracker.waitForID(1);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public Font getFont() {
         return font;
     }
 
     /**
      * Paints a string that contains latexcodes and returns its width.
      * @param g the graphics context
      * @param f the font metrics
      * @param str the string to draw
      * @param x where to paint
      * @param y where to paint
      */
     public int drawLatexString(Graphics g, String str, int x, int y) {
         if (str == null) {
             return 0;
         }
         GraphicsState state = new GraphicsState(str, g, x, y, true);
         drawLatexPart(state, true);
         return state.x - x;
     }
 
     /**
      * Returns the width of a latexstring.
      * @param str the string
      * @param g the graphics context to measure in
      */
     public int getLatexWidth(Graphics g, String str) {
         if (str == null) {
             return 0;
         }
         GraphicsState state = new GraphicsState(str, g, 0, 0, false);
         drawLatexPart(state, true);
         return state.x;
     }
 
     @Override
     protected State startSuper(State s) {
         GraphicsState state = (GraphicsState) s;
         if (state.s.charAt(state.i) == '*')     // * is superscript already
             return super.startSuper(s);
 
         Font f = state.graphics.getFont();
         GraphicsState substate = (GraphicsState) super.startSuper(state);
 
         substate.graphics.setFont(f.deriveFont((float) (f.getSize() * 0.75)));
         substate.y = state.y -
                 substate.graphics.getFontMetrics().getMaxDescent();
         return substate;
     }
 
     @Override
     protected void endSuper(State s) {
         GraphicsState state = (GraphicsState) s;
         state.graphics.dispose();
         ((GraphicsState) state.parent).x = state.x;
         super.endSuper(s);
     }
 
     @Override
     protected State startSub(State s) {
         GraphicsState state = (GraphicsState) s;
         Font f = state.graphics.getFont();
         GraphicsState substate = (GraphicsState) super.startSub(state);
 
         substate.graphics.setFont(f.deriveFont((float)(f.getSize()*0.75)));
         substate.y = state.y +
                 substate.graphics.getFontMetrics().getMaxDescent();
         return substate;
     }
 
     @Override
     protected void endSub(State s) {
         GraphicsState state = (GraphicsState) s;
         state.graphics.dispose();
         ((GraphicsState) state.parent).x = state.x;
         super.endSub(s);
     }
 
     @Override
     protected State startCo(State s) {
         return super.startCo(s);
     }
 
     @Override
     protected void endCo(State s) {
         GraphicsState state = (GraphicsState) s;
         if (state.dopaint) {
             FontMetrics m = state.graphics.getFontMetrics();
             state.graphics.drawLine(
                     ((GraphicsState) state.parent).x, state.y - m.getAscent(),
                     state.x, state.y - m.getAscent());
         }
         state.graphics.dispose();
         ((GraphicsState) state.parent).x = state.x;
         super.endCo(s);
     }
 
     @Override
     protected void drawPlainString(State s, String str) {
         GraphicsState state = (GraphicsState) s;
         if (str != null  &&  str.length() > 0) {
             if (state.dopaint) {
                 state.graphics.drawString(str, state.x, state.y);
             }
             state.x += state.graphics.getFontMetrics().stringWidth(str);
         }
     }
 
     @Override
     protected void drawGlyph(State s, LatexGlyph glyph) {
         GraphicsState state = (GraphicsState) s;
         Graphics g = state.graphics;
 
         if (g instanceof PSGraphics) {
             Font font = g.getFont();
             Font newfont = new Font("ISGCIFont",
                     font.getStyle(), font.getSize());
             if (state.dopaint) {
                 g.setFont(newfont);
                 g.drawString(glyph.getIsgcichar(), state.x, state.y);
                 g.setFont(font);
             }
             state.x += g.getFontMetrics(newfont).stringWidth(
                     glyph.getIsgcichar());
         } else {
             String unicode = glyph.getUnicode();
             if (!unicode.isEmpty() 
                     && g.getFont().canDisplayUpTo(unicode) == -1) {
                if (state.dopaint) {
                     g.drawString(unicode, state.x, state.y);
                }
                 state.x += g.getFontMetrics().stringWidth(unicode);
             } else if (g instanceof SVGGraphics) { // We don't do SVG imgs
                 if (state.dopaint) {
                     g.drawString(glyph.getName(), state.x, state.y);
                 }
                 state.x += g.getFontMetrics().stringWidth(glyph.getName());
             } else {
                 FontMetrics f = g.getFontMetrics();
                 Image image = glyph.getImage();
                 float scale = f.getHeight() / image.getHeight(null);
 
                 int dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2;
                 sx1 = 0;
                 sy1 = 0;
                 sx2 = image.getWidth(null);
                 sy2 = image.getHeight(null);
 
                 dx1 = state.x;
                 dy2 = state.y;
                 dy1 = dy2 - (int) (sy2 * scale);
                 dx2 = dx1 + (int) (sx2 * scale);
 
                 /*System.err.println( f.getHeight());
                 System.err.print(x); System.err.print(" ");
                 System.err.println(y); System.err.print(" ");
                 System.err.print(sx1); System.err.print(" ");
                 System.err.print(sy1); System.err.print(" ");
                 System.err.print(sx2); System.err.print(" ");
                 System.err.println(sy2);
                 System.err.print(dx1); System.err.print(" ");
                 System.err.print(dy1); System.err.print(" ");
                 System.err.print(dx2); System.err.print(" ");
                 System.err.println(dy2);*/
 
                 if (state.dopaint) {
                     g.drawImage(image, dx1, dy1, dx2, dy2, 
                             sx1, sy1, sx2, sy2, null);
                 }
                 state.x += dx2 - dx1;
             }
         }
     }
 
 
     protected class GraphicsState extends Latex.State {
         /**
          * Graphics context where we're painting.
          */
         public Graphics graphics;
 
         /**
          * Whether we're really painting or just pretending.
          */
         public boolean dopaint;
 
         /**
          * Graphics location where we're painting.
          */
         public int x, y;
 
         public GraphicsState(String s,
                 Graphics g, int x, int y, boolean dopaint) {
             super(s);
             graphics = g;
             this.x = x;
             this.y = y;
             this.dopaint = dopaint;
         }
 
         public GraphicsState(State parent) {
             super(parent);
             this.graphics = ((GraphicsState) parent).graphics.create();
             this.x = ((GraphicsState) parent).x;
             this.y = ((GraphicsState) parent).y;
             this.dopaint = ((GraphicsState) parent).dopaint;
         }
 
         /**
          * Derive a new state and return it.
          */
         public State deriveStart() {
             return new GraphicsState(this);
         }
     }
 }
 
 
 /* EOF */
