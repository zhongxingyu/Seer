 /*
  *  FatFind
  *  A Diamond application for adipocyte image exploration
  *  Version 1
  *
  *  Copyright (c) 2006, 2010 Carnegie Mellon University
  *  All Rights Reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.fatfind;
 
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.util.List;
 
 import org.freedesktop.cairo.Context;
 import org.gnome.gdk.InterpType;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.gdk.Window;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.Widget;
 import org.gnome.pango.FontDescription;
 import org.gnome.pango.Layout;
 
 class ProcessedImage {
     public Pixbuf getOriginal() {
         return original;
     }
 
     public Pixbuf getScaled() {
         return scaled;
     }
 
     public List<Circle> getCircles() {
         return circles;
     }
 
     public double getScale() {
         return scale;
     }
 
     public boolean deleteCircleAtPoint(int x, int y) {
         int index = getCircleIndexAtPoint(x, y);
 
         if (index == -1) {
             return false;
         } else {
             circles.remove(index);
             updateHitmap();
             widget.queueDraw();
             return true;
         }
     }
 
     public Circle getCircleAtPoint(int x, int y) {
         int index = getCircleIndexAtPoint(x, y);
         if (index == -1) {
             return null;
         } else {
             return circles.get(index);
         }
     }
 
     private int getCircleIndexAtPoint(int x, int y) {
        if (hitmap == null) {
            return -1;
        }
         int index = hitmap[y * allocW + x];
         return index;
     }
 
     final private Pixbuf original;
 
     final private Widget widget;
 
     // delete when https://bugzilla.gnome.org/show_bug.cgi?id=628348 fixed
     private Window window;
 
     private Pixbuf scaled;
 
     private List<Circle> circles;
 
     private int hitmap[];
 
     double scale;
 
     int allocW;
 
     int allocH;
 
     boolean showCircles;
 
     public static ProcessedImage createBusyImage(Widget widget) {
         return new ProcessedImage(widget, null, null);
     }
 
     public ProcessedImage(Widget widget, Pixbuf original, List<Circle> circles) {
         this.widget = widget;
         this.original = original;
         this.circles = circles;
 
         rescale();
     }
 
     public void rescale() {
         Allocation a = widget.getAllocation();
 
         if ((allocW == a.getWidth()) && (allocH == a.getHeight())) {
             return;
         }
 
         allocW = a.getWidth();
         allocH = a.getHeight();
 
         if (original == null) {
             return;
         }
 
         double aspect = (double) original.getWidth()
                 / (double) original.getHeight();
 
         double windowAspect = (double) allocW / (double) allocH;
 
         int w = allocW;
         int h = allocH;
 
         // is window wider than pixbuf?
         if (aspect < windowAspect) {
             // calc width from height
             w = (int) (h * aspect);
             scale = (double) allocH / (double) original.getHeight();
         } else {
             // calc height from width
             h = (int) (w / aspect);
             scale = (double) allocW / (double) original.getWidth();
         }
 
         scaled = original.scale(w, h, InterpType.BILINEAR);
 
         // generate hitmap
         updateHitmap();
 
         widget.queueDraw();
     }
 
     private void updateHitmap() {
         BufferedImage img = new BufferedImage(allocW, allocH,
                 BufferedImage.TYPE_INT_RGB);
 
         Graphics2D g = img.createGraphics();
 
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_OFF);
 
         // clear
         g.setComposite(AlphaComposite.Src);
         g.setColor(new Color(-1, true));
         g.fillRect(0, 0, allocW, allocH);
 
         // draw circles
         Shape circle = new Ellipse2D.Double(-1, -1, 2, 2);
 
         int color = 0;
         for (Circle c : circles) {
             g.setColor(new Color(color, true));
 
             AffineTransform at = new AffineTransform();
             at.scale(scale, scale);
 
             at.translate(c.getX(), c.getY());
             at.rotate(c.getT());
             at.scale(c.getA(), c.getB());
 
             Shape s = at.createTransformedShape(circle);
             g.fill(s);
             g.draw(s);
 
             color++;
         }
 
         g.dispose();
 
         hitmap = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
     }
 
     private void drawCircles(Context cr, Circle.Filter filter) {
         for (Circle c : circles) {
             c.draw(cr, scale, filter.filter(c) ? Circle.Fill.SOLID
                     : Circle.Fill.DASHED);
         }
     }
 
     public void setShowCircles(boolean state) {
         if (state != showCircles) {
             showCircles = state;
             widget.queueDraw();
         }
     }
 
     public void setCircles(List<Circle> circles) {
         this.circles = circles;
         updateHitmap();
         widget.queueDraw();
     }
 
     public void drawToWidget(Circle.Filter filter) {
         rescale();
 
         // https://bugzilla.gnome.org/show_bug.cgi?id=628348
         if (window == null) {
             window = widget.getWindow();
         }
 
         Context cr = new Context(widget.getWindow());
 
         if (original == null) {
             // "busy" image
             Layout layout = new Layout(cr);
             layout.setFontDescription(new FontDescription("Sans"));
             layout.setText("Processing...");
             int w = layout.getPixelWidth();
             int h = layout.getPixelHeight();
 
             cr.moveTo(allocW / 2 - w / 2, allocH / 2 - h / 2);
             cr.showLayout(layout);
         } else {
             cr.setSource(getScaled(), 0, 0);
             cr.paint();
 
             if (showCircles) {
                 drawCircles(cr, filter);
             }
         }
     }
 
     public void toggleCircleAtPoint(int x, int y) {
         Circle c = circles.get(getCircleIndexAtPoint(x, y));
         c.setInResult(!c.isInResult());
         widget.queueDraw();
     }
 
     public void addCircle(Circle c) {
         circles.add(c);
         updateHitmap();
         widget.queueDraw();
     }
 }
