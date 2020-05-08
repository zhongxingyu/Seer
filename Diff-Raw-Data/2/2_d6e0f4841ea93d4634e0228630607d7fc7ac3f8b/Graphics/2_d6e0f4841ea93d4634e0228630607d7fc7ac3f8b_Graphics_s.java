 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2009-2010  BlinzProject <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.graphics;
 
 import com.sun.opengl.util.j2d.TextRenderer;
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureCoords;
 import java.util.ArrayList;
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import org.blinz.util.Position;
 import org.blinz.util.Bounds;
 import org.blinz.util.Size;
 
 /**
  * Graphics class contains methods for drawing to the screen.
  * @author Blinz Project
  */
 public class Graphics {
 
     private final static ArrayList<Viewport> excessViewports = new ArrayList<Viewport>();
     private boolean viewPortOn = false;
     private Viewport viewport;
     private final Bounds screenBounds = new Bounds();
     private final Color color = new Color();
     private Font font;
     private GL gl;
 
     /**
      * Constructor
      */
     Graphics() {
     }
 
     //PUBLIC METHODS------------------------------------------------------------
     /**
      * Moves the point of origin for drawing over the specified amount.
      * @param x the x coordinate of the translation
      * @param y the y coordinate of the translation
      */
     public final void translate(final int x, final int y) {
         gl.glTranslated(x, y, 0);
     }
 
     /**
      * Draws the rectangle with point (x1, y1) representing the top left corner,
      * and point (x2, y2) representing the bottom right corner.
      * @param x1 the x1 coordinate of the translation
      * @param y1 the y1 coordinate of the translation
      * @param x2 the x2 coordinate of the translation
      * @param y2 the y2 coordinate of the translation
      */
     public final void fillRect(final int x1, final int y1, final int x2, final int y2) {
         gl.glRecti(x1, y1, x2, y2);
     }
 
     /**
      * Draws the outline of the given Polygon with the first point in the Polygon
      * as the point of origin.
      * @param polygon the Polygon to be filled 
      */
     public final void fillPolygon(final Polygon polygon) {
         gl.glBegin(GL.GL_POLYGON);
         {
             for (int i = 0; i < polygon.size(); i++) {
                 gl.glVertex2i(polygon.get(i).x, polygon.get(i).y);
             }
         }
         gl.glEnd();
     }
 
     /**
      * Draws a line from point (x1, y1) to point (x2, y2).
      * @param x1 the x1 coordinate of the line 
      * @param y1 the y1 coordinate of the line
      * @param x2 the x2 coordinate of the line
      * @param y2 the y2 coordinate of the line
      */
     public final void drawLine(final int x1, final int y1, final int x2, final int y2) {
         gl.glBegin(GL.GL_LINES);
         {
             gl.glVertex2i(x1, y1);
             gl.glVertex2i(x2, y2);
         }
         gl.glEnd();
     }
 
     /**
      * Draws a line from loc1 to loc2.
      * @param loc1 a Position object representing the starting point of the line
      * @param loc2 a Position object representing the end of the line
      */
     public final void drawLine(final Position loc1, final Position loc2) {
         drawLine(loc1.x, loc1.y, loc2.x, loc2.y);
     }
 
     /**
      * Draws the outline of a rectangle with point (x1, y1) representing the top
      * left corner, and point (x2, y2) representing the bottom right corner.
      * @param x1 the x1 coordinate of the rectangle
      * @param y1 the y1 coordinate of the rectangle
      * @param x2 the x2 coordinate of the rectangle
      * @param y2 the y2 coordinate of the rectangle
      */
     public final void drawRect(final int x1, final int y1, final int x2, final int y2) {
         gl.glBegin(GL.GL_LINE_STRIP);
         {
             gl.glVertex2i(x1, y1);
             gl.glVertex2i(x2, y1);
             gl.glVertex2i(x2, y2);
             gl.glVertex2i(x1, y2);
             gl.glVertex2i(x1, y1);
         }
         gl.glEnd();
 
     }
 
     /**
      * Draws the outline of the given Polygon with the first point in the Polygon
      * as the point of origin.
      * @param polygon the Polygon to be filled
      */
     public final void drawPolygon(final Polygon polygon) {
         gl.glBegin(GL.GL_LINE_STRIP);
         for (int i = 0; i < polygon.size(); i++) {
             gl.glVertex2i(polygon.get(i).x, polygon.get(i).y);
         }
         gl.glEnd();
     }
 
     /**
      * Sets the drawing color to the color represented by the passed Color object.
      * @param color a Color object representing the color with which primites will be drawn
      */
     public final void setColor(final Color color) {
         setColor(color.red, color.green, color.blue);
     }
 
     /**
      * Sets the drawing color to the mix of the provided RGB values.
      * @param red the red in the RGB
      * @param green the green in the RGB
      * @param blue the blue in the RGB
      */
     public final void setColor(final int red, final int green, final int blue) {
         color.setColor(red, green, blue);
         gl.glColor3b(color.red, color.green, color.blue);
     }
 
     /**
      * Draws the current Image of the given Animation on the screen at the
      * given coordinates.
      *
      * Note: x1 and y1 refer to the top left corner of the image, and x2 and y2
      * refer to the bottom right.
      * @param animation the Animation to be drawn
      * @param x1 the x1 coordinate of the Animation
      * @param y1 the y1 coordinate of the Animation
      * @param x2 the x2 coordinate of the Animation
      * @param y2 the y2 coordinate of the Animation
      */
     public final void drawAnimation(final Animation animation, final int x1, final int y1, final int x2, final int y2) {
         drawImage(animation.getImage(), x1, y1, x2, y2);
     }
 
     /**
      * Draws the current Image of the given Animation on the screen at the
      * given coordinates.
      *
      * Note: x1 and y1 refer to the top left corner of the image, and x2 and y2
      * refer to the bottom right.
      * @param animation the Animation to be drawn
      * @param bounds the bounds in which to draw the given animation
      */
     public final void drawAnimation(final Animation animation, final Bounds bounds) {
         drawImage(animation.getImage(), bounds.x, bounds.y, bounds.x2(), bounds.y2());
     }
 
     /**
      * Draws the current Image of the given Animation on the screen at the
      * given coordinates.
      *
      * Note: x1 and y1 refer to the top left corner of the image, and x2 and y2
      * refer to the bottom right.
      * @param animation the Animation to be drawn
      * @param x1 the x1 coordinate of the Animation
      * @param y1 the y1 coordinate of the Animation
      * @param x2 the x2 coordinate of the Animation
      * @param y2 the y2 coordinate of the Animation
      */
     public final void draw(final Animation animation, final int x1, final int y1, final int x2, final int y2) {
         drawImage(animation.getImage(), x1, y1, x2, y2);
     }
 
     /**
      * Draws the current Image of the given Animation on the screen at the
      * given coordinates.
      *
      * Note: x1 and y1 refer to the top left corner of the image, and x2 and y2
      * refer to the bottom right.
      * @param animation the Animation to be drawn
      * @param bounds the bounds in which to draw the given animation
      */
     public final void draw(final Animation animation, final Bounds bounds) {
         drawImage(animation.getImage(), bounds.x, bounds.y, bounds.x2(), bounds.y2());
     }
 
     /**
      * Draws the current Image of the given Animation on the screen at the
      * given coordinates.
      *
      * Note: loc1 refers to the top left corner of the image and loc2
      * refers to the bottom right.
      * @param animation the Animation to be drawn
      * @param loc1 top left coordinate of the draw space
      * @param loc2 bottom right coordinate of the draw space
      */
     public final void drawAnimation(final Animation animation, final Position loc1, final Position loc2) {
         drawImage(animation.getImage(), loc1.x, loc1.y, loc2.x, loc2.y);
     }
 
     /**
      * Draws the current Image of the given Animation on the screen at the
      * given coordinates.
      *
      * Note: loc1 refers to the top left corner of the image and loc2
      * refers to the bottom right.
      * @param animation the Animation to be drawn
      * @param loc1 top left coordinate of the draw space
      * @param loc2 bottom right coordinate of the draw space
      */
     public final void draw(final Animation animation, final Position loc1, final Position loc2) {
         drawImage(animation.getImage(), loc1.x, loc1.y, loc2.x, loc2.y);
     }
 
     /**
      * Draws the given Image on the screen at the given coordinates.
      *
      * Note: loc1 refers to the top left corner of the image and loc2
      * refers to the bottom right.
      * @param image the image to draw
      * @param loc1 top left coordinate of the draw space
      * @param loc2 bottom right coordinate of the draw space
      */
     public final void drawImage(final Image image, final Position loc1, final Position loc2) {
         drawImage(image, loc1.x, loc1.y, loc2.x, loc2.y);
     }
 
     /**
      * Draws the given Image on the screen across the given bounds.
      *
      * @param image the image to draw
      * @param bounds the bounds in which to draw the image
      */
     public final void drawImage(final Image image, final Bounds bounds) {
         drawImage(image, bounds.x, bounds.y, bounds.x2(), bounds.y2());
     }
 
     /**
      * Draws the given Image on the screen at the given coordinates.
      *
      * Note: loc1 refers to the top left corner of the image and loc2
      * refers to the bottom right.
      * @param image the image to draw
      * @param loc1 top left coordinate of the draw space
      * @param loc2 bottom right coordinate of the draw space
      */
     public final void draw(final Image image, final Position loc1, final Position loc2) {
         drawImage(image, loc1.x, loc1.y, loc2.x, loc2.y);
     }
 
     /**
      * Draws the given Image on the screen across the given bounds.
      *
      * @param image the image to draw
      * @param bounds the bounds in which to draw the image
      */
     public final void draw(final Image image, final Bounds bounds) {
         drawImage(image, bounds.x, bounds.y, bounds.x2(), bounds.y2());
     }
 
     /**
      * Draws the given Image on the screen at the given coordinates.
      *
      * Note: x1 and y1 refer to the top left corner of the image, and x2 and y2
      * refer to the bottom right.
      * @param image the Image to be drawn
      * @param x1 the x1 coordinate of the Animation
      * @param y1 the y1 coordinate of the Animation
      * @param x2 the x2 coordinate of the Animation
      * @param y2 the y2 coordinate of the Animation
      */
     public final void draw(final Image image, final int x1, final int y1, final int x2, final int y2) {
         drawImage(image, x1, y1, x2, y2);
     }
 
     /**
      * Draws the given Image on the screen at the given coordinates.
      *
      * Note: x1 and y1 refer to the top left corner of the image, and x2 and y2
      * refer to the bottom right.
      * @param image the Image to be drawn
      * @param x1 the x1 coordinate of the Animation
      * @param y1 the y1 coordinate of the Animation
      * @param x2 the x2 coordinate of the Animation
      * @param y2 the y2 coordinate of the Animation
      */
     public final void drawImage(final Image image, final int x1, final int y1, final int x2, final int y2) {
         final Texture texture = image.getImageStub().getTexture();
         TextureCoords t = texture.getImageTexCoords();
         gl.glPushMatrix();
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
         gl.glEnable(GL.GL_ALPHA_TEST);
         gl.glAlphaFunc(GL.GL_GREATER, 0);
 
         gl.glEnable(GL.GL_TEXTURE_2D);
         gl.glTexEnvf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
         gl.glColor3d(1, 1, 1);
         texture.bind();
 
         gl.glBegin(GL.GL_QUADS);
         {
             //Top Left
             gl.glTexCoord2f(t.left(), t.top());
             gl.glVertex3i(x1, y1, 0);
             //Top Right
             gl.glTexCoord2f(t.right(), t.top());
             gl.glVertex3i(x2, y1, 0);
             //Bottom Right
             gl.glTexCoord2f(t.right(), t.bottom());
             gl.glVertex3i(x2, y2, 0);
             //Bottom Left
             gl.glTexCoord2f(t.left(), t.bottom());
             gl.glVertex3i(x1, y2, 0);
         }
         gl.glEnd();
 
         gl.glDisable(GL.GL_TEXTURE_2D);
         gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
 
         gl.glDisable(GL.GL_ALPHA);
         gl.glDisable(GL.GL_BLEND);
 
 
         gl.glPopMatrix();
 
         gl.glColor3d(color.red, color.green, color.blue);
     }
 
     /**
      * Draws a rectangle, with loc representing the top left corner of the
      * rectangle.
      * @param loc Position object representing the top left corner of the rectangle
      * @param size Size object specifying how far to the right and down the rectangle will extend
      */
     public final void fillRect(final Position loc, final Size size) {
         fillRect(loc.x, loc.y, loc.x + size.width, loc.y + size.height);
     }
 
     /**
      * Draws a rectangle, with the bounds of the given Bounds object.
      * @param bounds the bounds of the rectangle to be drawn
      */
     public final void fillRect(final Bounds bounds) {
         fillRect(bounds.x, bounds.y, bounds.x2(), bounds.y2());
     }
 
     /**
      * Fills a rectangle, with loc representing the top left corner of the
      * rectangle.
      * @param loc Position object representing the top left corner of the rectangle
      * @param size Size object specifying how far to the right and down the rectangle will extend
      */
     public final void drawRect(final Position loc, final Size size) {
         drawRect(loc.x, loc.y, loc.x + size.width, loc.y + size.height);
     }
 
     /**
      * Draws a rectangle, with loc representing the top left corner of the
      * rectangle.
      * @param bounds the bounds of the rectangle to be drawn
      */
     public final void drawRect(final Bounds bounds) {
         drawRect(bounds.x, bounds.y, bounds.x2(), bounds.y2());
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string the String to be drawn
      * @param x the x coordinate of the String drawn
      * @param y the x coordinate of the String drawn
      * @param font the Font of the String when drawn
      */
     public final void drawString(final String string, int x, int y, final Font font) {
         TextRenderer r = font.stub.getRenderer();
         r.setColor(color.getRedf(), color.getGreenf(), color.getBluef(), 1f);
         if (viewPortOn) {
             y = viewport.getHeight() - y;
             r.beginRendering(viewport.getWidth(), viewport.getHeight());
         } else {
             y = screenBounds.getHeight() - y;
             r.beginRendering(screenBounds.getWidth(), screenBounds.getHeight());
         }
         r.draw(string, x, y - font.getSize());
         r.endRendering();
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param loc Position object with the coordinates string will be drawn at
      * @param font Font the String will be drawn with
      */
     public final void drawString(final String string, final Position loc, final Font font) {
         drawString(string, loc.x, loc.y, font);
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param x x location that string will be drawn at
      * @param y y location that string will be drawn at
      */
     public final void drawString(final String string, final int x, final int y) {
         drawString(string, x, y, font);
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param loc Position object with the coordinates string will be drawn at
      */
     public final void drawString(final String string, final Position loc) {
         drawString(string, loc.x, loc.y, font);
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param x x location that string will be drawn at
      * @param y y location that string will be drawn at
      * @param font Font the String will be drawn with
      */
     public final void draw(final String string, int x, int y, final Font font) {
         TextRenderer r = font.stub.getRenderer();
         r.setColor(color.getRedf(), color.getGreenf(), color.getBluef(), 1f);
         if (viewPortOn) {
             y = viewport.getHeight() - y;
             r.beginRendering(viewport.getWidth(), viewport.getHeight());
         } else {
             y = screenBounds.getHeight() - y;
             r.beginRendering(screenBounds.getWidth(), screenBounds.getHeight());
         }
         r.draw(string, x, y - font.getSize());
         r.endRendering();
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param loc Position object with the coordinates string will be drawn at
      * @param font Font the String will be drawn with
      */
     public final void draw(final String string, final Position loc, final Font font) {
         drawString(string, loc.x, loc.y, font);
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param x the x location that string will be drawn at
      * @param y the y location that string will be drawn at
      */
     public final void draw(final String string, final int x, final int y) {
         drawString(string, x, y, font);
     }
 
     /**
      * Draws the specified String with the specified font with the the bottom
      * left corner as the point of origin.
      * @param string String to be drawn
      * @param loc Position object with the coordinates string will be drawn at
      */
     public final void draw(final String string, final Position loc) {
         drawString(string, loc.x, loc.y, font);
     }
 
     /**
      * Gets the width of the given char.
      * Note: this number is rounded from a floating point number.
      * @param character the char who's width the will be measured
      * @return the width of the given char
      */
     public final int getCharWidth(final char character) {
         return font.getCharWidth(character);
     }
 
     /**
      * Gets the width of the given String when drawn on the screen with the current font.
      * @param string the String to be measured
      * @return the width of the given String when drawn on the screen with the current font
      */
     public final long getStringWidth(final String string) {
         return font.getStringWidth(string);
     }
 
     /**
      * Sets the Font to be used when drawing text.
      * @param font the Font this Graphics object will use to draw text
      */
     public final void setFont(final Font font) {
         this.font = font;
     }
 
     /**
      * Gets the Font this currently uses.
      * @return the Font this Graphics instance uses
      */
     public final Font getFont() {
         return font;
     }
 
     /**
      * Gets the size of this Graphics instances current Font.
      * @return the size of this Graphics instances current Font 
      */
     public final int getFontSize() {
         return font.getSize();
     }
 
     /**
      * Gets the width of the Screen and/or viewport in which this currently draws.
     * @return the width of the drawing
      */
     public final int getPaneWidth() {
         if (viewport != null) {
             return viewport.getWidth();
         }
         return screenBounds.getWidth();
     }
 
     /**
      * Gets the height of the Screen and/or viewport in which this currently draws.
      * @return the height of the drawing area
      */
     public final int getPaneHeight() {
         if (viewport != null) {
             return viewport.getHeight();
         }
         return screenBounds.getHeight();
     }
 
     /**
      * Sets the bounds within the current screen which things will be drawn,
      * outside of it everything will be cut off.
      *
      * IMPORTANT: Nesting viewports is not currently supported but may be in the
      * future, please exit the viewport when you're done.
      *
      * @param x the x location of the viewport
      * @param y the y location of the viewport
      * @param width the width of the viewport
      * @param height the height of the viewport
      */
     public final void enterViewport(final int x, final int y, final int width, final int height) {
         Viewport v = viewport;
         viewport = fetchViewport();
         if (v == null) {
             viewport.parent = screenBounds;
         } else {
             viewport.parent = v;
         }
         viewport.setViewport(x, y, width, height);
         viewport.fixViewport();
         gl.glViewport(viewport.getX(), viewport.getY(),
                 viewport.getWidth(), viewport.getHeight());
         gl.glLoadIdentity();
         gl.glOrtho(0.0f, viewport.getWidth(), viewport.getHeight(),
                 0.0f, -1.0f, 1.0f);
         gl.glTranslated(viewport.getXTranslation(), viewport.getYTranslation(), 0);
         viewPortOn = true;
     }
 
     /**
      * Leaves the viewport and returns the scope to the whole screen.
      */
     public final void exitViewport() {
         excessViewports.add(viewport);
         if (viewport.parent instanceof Viewport) {
             viewport = (Viewport) viewport.parent;
         } else {
             viewport = null;
             viewPortOn = false;
         }
         load();
     }
 
     /**
      * Sets the bounds of the Screen this Graphics object belongs to and draws for.
      * @param bounds a Bounds object representing the bounds of this Screen within the window
      */
     final void setScreenBounds(final Bounds bounds) {
         screenBounds.setBounds(bounds);
     }
 
     /**
      * Sets the GL context to be used for drawing.
      * @param drawable the GLAutoDrawable object used to get the lower level drawing utility
      */
     final void setContext(final GLAutoDrawable drawable) {
         gl = drawable.getGL();
     }
 
     /**
      * Loads this Graphics objects settings to the OpenGL context.
      */
     final void load() {
         gl.glLoadIdentity();
         if (viewPortOn) {
             viewport.fixViewport();
 
             gl.glViewport(viewport.getX(), viewport.getY(),
                     viewport.getWidth(), viewport.getHeight());
             gl.glOrtho(0.0f, viewport.getWidth(), viewport.getHeight(),
                     0.0f, -1.0f, 1.0f);
         } else {
             gl.glViewport(screenBounds.getX(), screenBounds.getY(),
                     screenBounds.getWidth(), screenBounds.getHeight());
             gl.glOrtho(0.0f, screenBounds.getWidth(), screenBounds.getHeight(), 0.0f,
                     -1.0f, 1.0f);
         }
         gl.glColor3b(color.red, color.green, color.blue);
     }
 
     /**
      * Cleans out excess data being used, reduces memory use.
      */
     final void clean() {
         excessViewports.trimToSize();
         excessViewports.clear();
     }
 
     /**
      * Gets a recycled Viewport object.
      * @return a Viewport object, reuses old Position objects when possible
      */
     private final Viewport fetchViewport() {
         if (excessViewports.isEmpty()) {
             return new Viewport();
         } else {
             return excessViewports.remove(0);
         }
     }
 }
