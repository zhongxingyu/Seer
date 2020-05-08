 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>
  * 
  *  This file is part of EvoPaint.
  * 
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint.gui.util;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JComponent;
 
 /**
  * The <code>WrappingScalableCanvas</code> class is used to create a parallax
  * surface which can be moved into two directions and be scaled indefinetly.
  * <p>
  * Keep in mind two spaces which need transformation from one to another. The
  * user space in which the user performs clicks and the image is scaled into
  * and the image space in which operations are performed on the image.<br>
  * As a rule of thumb remember to transform any points from mouse clicks
  * performed on the canvas to image space before processing them.
  * </p>
  * <p>
  * Implementing <code>IOverlayable</code>, this canvas supports alpha overlays,
  * which it will also wrap and scale to user space accordingly.
  * </p>
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  */
 public class WrappingScalableCanvas extends JComponent implements IOverlayable {
 
     private BufferedImage image;
     private Graphics2D imageG2;
     private int imageWidth;
     private int imageHeight;
     private int integerScale;
     private double scale;
     private Point translation;
     private AffineTransform scaleTransform;
     private AffineTransform transform;
     private List<IOverlay> overlays;
 
     /**
      * Creates a new WrappingScalableCanvas using the image supplied.
      * @param image
      */
     public WrappingScalableCanvas(BufferedImage image) {
         this.image = image;
         this.imageWidth = image.getWidth();
         this.imageHeight = image.getHeight();
         this.imageG2 = (Graphics2D)image.getGraphics();
         this.integerScale = 10;
         this.scale = 1;
         this.translation = new Point(0, 0);
         this.overlays = new ArrayList<IOverlay>();
         updateScale();
         updateComponentSize();
     }
     
     /**
      * Magnifies the display of the image
      */
     public void scaleUp() {
         integerScale++;
         scale = integerScale / 10d;
         updateScale();
     }
 
     /**
      * Shrinks the display of the image
      */
     public void scaleDown() {
         if (integerScale <= 1) {
             return;
         }
         integerScale--;
         updateScale();
     }
 
     private void updateScale() {
         scale = integerScale / 10d;
         scaleTransform = AffineTransform.getScaleInstance(scale, scale);
         transform = new AffineTransform(scaleTransform);
         transform.translate(translation.x, translation.y);
         updateComponentSize();
         revalidate();
     }
 
     private void updateComponentSize() {
         setPreferredSize(new Dimension((int)Math.ceil(imageWidth * scale),
                 (int)Math.ceil(imageHeight * scale)));
     }
 
     /**
      * Translates the display of the underlying image using user space
      * coordinates.
      * @param origin the origin eg. of a user space drag operation
      * @param destination the destination eg. of a user space drag operation
      */
     public void translateInUserSpace(Point origin, Point destination) {
        // transform points to image space before translation so they scale
        // correctly
        origin = transformToImageSpace(new Point(origin));
        destination = transformToImageSpace(new Point(destination));

         translation.x += destination.x - origin.x;
         if (translation.x < (-1) * imageWidth) {
             translation.x += imageWidth;
         } else if (translation.x > imageWidth) {
             translation.x -= imageWidth;
         }
         translation.y += destination.y - origin.y;
         if (translation.y < (-1) * imageHeight) {
             translation.y += imageHeight;
         } else if (translation.y > imageHeight) {
             translation.y -= imageHeight;
         }
         transform = new AffineTransform(scaleTransform);
         transform.translate(translation.x, translation.y);
     }
 
     /**
      * Transforms a <code>Point</code> from user space to image space. It will
      * translate the <code>Point</code> back to its origin in user space and
      * rescale it to the original image space scale.
      * @param point The <code>Point</code> used to create the
      * @return A new Point in image space corresponding to the passed
      * <code>Point</code>
      */
     public Point transformToImageSpace(Point point) {
         AffineTransform invertedTransform = new AffineTransform(transform);
         try {
             invertedTransform.invert();
         } catch (NoninvertibleTransformException ex) {
             ex.printStackTrace();
             System.exit(1);
         }
         Point2D.Float floatPoint = (Point2D.Float)invertedTransform.transform(point, null);
         Point ret = new Point((int)floatPoint.x, (int)floatPoint.y);
 
         // the transformed point may have coordinates wich lie out of our
         // image, so we have to wrap them
         if (ret.x < 0) {
             ret.x += imageWidth;
         }
         else if (ret.x > imageWidth) {
             ret.x -= imageWidth;
         }
         if (ret.y < 0) {
             ret.y += imageHeight;
         }
         else if (ret.y > imageHeight) {
             ret.y -= imageHeight;
         }
         return ret;
     }
 
     /**
      * Scales an arbitrary <code>Shape</code> from image space to user space
      * @param shape The <code>Shape</code> you wish to scale
      * @return A new <code>Shape</code> scaled to user space
      * @see Shape
      */
     public Shape scaleToUserSpace(Shape shape) {
         return scaleTransform.createTransformedShape(shape);
     }
 
     /**
      * Scales an arbitrary <code>Shape</code> from user space to image space
      * @param shape The <code>Shape</code> you wish to scale
      * @return A new <code>Shape</code> scaled to image space
      * @see Shape
      */
     public Shape scaleToImageSpace(Shape shape) {
         try {
             return scaleTransform.createInverse().createTransformedShape(shape);
         } catch (NoninvertibleTransformException ex) {
             ex.printStackTrace();
             System.exit(1);
         }
         assert (false);
         return null;
     }
 
     /**
      * Transforms a <code>Point</code> from image space to user space the
      * resulting <code>Point</code> will be translated and scaled to match
      * user space coordinates.
      * @param point The <code>Point</code> you wish to transform
      * @return a new <code>Point</code> in user space corresponding to the
      * argument point
      */
     public Point transformToUserSpace(Point point) {
         return (Point)transform.transform(point, null);
     }
 
     /**
      * Transforms an arbitrary <code>Shape</code> from image space to user space, translates
      * and scales.
      * @param shape The <code>Shape</code> you wish to transform
      * @return A new <code>Shape</code> corresponding the the argument
      * @see Shape
      */
     public Shape transformToUserSpace(Shape shape) {
         return transform.createTransformedShape(shape);
     }
 
     /**
      * Adds an overlay to the canvas
      * @param overlay The Overlay you wish to subscribe
      * @see IOverlayable
      * @see IOverlay
      */
     public void subscribe(IOverlay overlay) {
         overlays.add(overlay);
     }
 
     /**
      * Removes an overlay from the subscribed overlays
      * @param overlay The Overlay you wish to unsubscribe
      * @see IOverlayable
      * @see IOverlay
      */
     public void unsubscribe(IOverlay overlay) {
         overlays.remove(overlay);
     }
 
     /**
      * Paints an overlay using the given parameters. This is a callback called
      * by subscribed overlays which wish to paint onto the canvas.
      * @param shape The <code>Shape</code> to be painted
      * @param color The color in which the <code>Shape</code> shall be painted
      * @param alpha An alpha value to make the <code>Shape</code> transparent
      * @see IOverlayable
      * @see IOverlay
      * @see Shape
      * @see Color
      */
     public void paintOverlay(Shape shape, Color color, float alpha) {
         imageG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
         imageG2.setColor(color);
         imageG2.fill(shape);
 
         Rectangle bounds = shape.getBounds();
 
         // wrapping horizontal
         // west
         if (bounds.x < 0) {
             Rectangle wrappedRest = new Rectangle(bounds.x + imageWidth, bounds.y, bounds.width + bounds.x, bounds.height);
             imageG2.fill(wrappedRest);
 
             // corner NW
             if (bounds.y < 0) {
                 wrappedRest = new Rectangle(bounds.x + imageWidth, bounds.y + imageHeight, bounds.width + bounds.x, bounds.height + bounds.y);
                 imageG2.fill(wrappedRest);
             }
 
             // corner SW
             else if (bounds.y + bounds.height > imageHeight) {
                 wrappedRest = new Rectangle(bounds.x + imageWidth, 0, bounds.width + bounds.x, bounds.y + bounds.height - imageHeight);
                 imageG2.fill(wrappedRest);
             }
         }
 
         // east
         else if (bounds.x + bounds.width > imageWidth) {
             Rectangle wrappedRest = new Rectangle(0, bounds.y, bounds.x + bounds.width - imageWidth, bounds.height);
             imageG2.fill(wrappedRest);
 
             // corner NE
             if (bounds.y < 0) {
                 wrappedRest = new Rectangle(0, bounds.y + imageHeight, bounds.x + bounds.width - imageWidth, bounds.height + bounds.y);
                 imageG2.fill(wrappedRest);
             }
 
             // corner SE
             else if (bounds.y + bounds.height > imageHeight) {
                 wrappedRest = new Rectangle(0, 0, bounds.x + bounds.width - imageWidth, bounds.y + bounds.height - imageHeight);
                 imageG2.fill(wrappedRest);
             }
         }
 
         // wrapping vertical (corners already painted)
         // north
         if (bounds.y < 0) {
             Rectangle wrappedRest = new Rectangle(bounds.x, bounds.y + imageHeight, bounds.width, bounds.height + bounds.y);
             imageG2.fill(wrappedRest);
         }
 
         // south
         else if (bounds.y + bounds.height > imageHeight) {
             Rectangle wrappedRest = new Rectangle(bounds.x, 0, bounds.width, bounds.y + bounds.height - imageHeight);
             imageG2.fill(wrappedRest);
         }
     }
     
     /**
      * Paints the canvas
      * @param g The graphics context to paint on
      */
     @Override
     public void paintComponent(Graphics g) {
 
         for (IOverlay overlay : overlays) {
             overlay.paint();
         }
 
         Graphics2D g2 = (Graphics2D)g;
 
         g2.clip(scaleToUserSpace(new Rectangle(imageWidth, imageHeight)));
         
         // paint NW
         transform.translate((-1) * imageWidth, (-1) * imageHeight);
         g2.drawRenderedImage(image, transform);
         // paint N
         transform.translate(imageWidth, 0);
         g2.drawRenderedImage(image, transform);
         // paint NE
         transform.translate(imageWidth, 0);
         g2.drawRenderedImage(image, transform);
         // paint E
         transform.translate(0, imageHeight);
         g2.drawRenderedImage(image, transform);
         // paint SE
         transform.translate(0, imageHeight);
         g2.drawRenderedImage(image, transform);
         // paint S
         transform.translate((-1) * imageWidth, 0);
         g2.drawRenderedImage(image, transform);
         // paint SW
         transform.translate((-1) * imageWidth, 0);
         g2.drawRenderedImage(image, transform);
         // paint W
         transform.translate(0, (-1) * imageHeight);
         g2.drawRenderedImage(image, transform);
         // back to normal
         transform.translate(imageWidth, 0);
         g2.drawRenderedImage(image, transform);
     }
 }
