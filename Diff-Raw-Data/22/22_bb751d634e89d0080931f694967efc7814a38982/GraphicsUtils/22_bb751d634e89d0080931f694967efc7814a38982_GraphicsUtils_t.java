 /*
  * Copyright (c) 2000
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.util.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Window;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

 import java.util.Collection;
 import java.util.Iterator;

 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  * graphics functions that should exist in java, but don't
  *
  * @author Jon Schewe
  * 
 * @version $Revision: 1.9 $
  */
 public class GraphicsUtils {
 
   /**
      a basic main for testing a graphical class.  Takes the component and puts 
      * in in a JFrame or JDialog and shows the window.
      @param c the component to display
      @param dialog if <code>true</code> put in a JDialog, otherwise put in a
      * JFrame
      **/
   public static Window basicGUIMain(final Component c, final boolean dialog) {
     final Container container;
     final Window window;
     if(dialog) {
       window = new JDialog();
       container = ((JDialog)window).getContentPane();
     }
     else {
       window = new JFrame();
       container = ((JFrame)window).getContentPane();      
     }
 
     window.setSize(c.getPreferredSize());
     window.addWindowListener(new BasicWindowMonitor());
     container.setLayout(new BorderLayout());
     container.add(c, BorderLayout.CENTER);
     centerWindow(window);
     window.pack();    
     window.show();
 
     return window;
   }
 
   /**
    * Centers the window on the screen.
    */
   public static void centerWindow(final Window window) {
    final Dimension2D screenSize = window.getToolkit().getScreenSize();
    final Point2D screenCenter = new Point2D.Double(screenSize.getWidth()/2,
                                                    screenSize.getHeight()/2);
    final Dimension2D windowSize = window.getSize();
    final Point location = new Point();
    location.setLocation(new Point2D.Double(screenCenter.getX() - windowSize.getWidth()/2,
                                            screenCenter.getY() - windowSize.getHeight()/2));
    window.setLocation(location);
   }
 
   
   /**
      draw the points on the Graphics Context.  Uses Graphics.drawPolyLine.
      @param g the graphics context
      @param points a vector of points, anything other than a point in this
      vector is simply skipped
   **/
   public static void drawPolyLine(final Graphics g, final Collection points) {
     final int[] xpoints = new int[points.size()];
     final int[] ypoints = new int[points.size()];
     int npoints = 0;
     final Iterator iter = points.iterator();
     while(iter.hasNext()) {
       Object obj = iter.next();
       if(obj instanceof Point) {
         final Point p = (Point)obj;
         xpoints[npoints] = p.x;
         ypoints[npoints] = p.y;
         npoints++;
       }
     }
 
     g.drawPolyline(xpoints, ypoints, npoints);
   }
 
   /**
      draw a bunch of polygons
      @param g the graphics context
      @param v a Container of polygons, other classes are ignored
   **/
   public static void drawPolygons(final Graphics g, final Collection v) {
     drawPolygons(g, v.iterator());
   }
 
   /**
      draw a bunch of polygons
      @param g the graphics context
      @param iter an Enumeration of polygons, other classes are ignored
   **/
   public static void drawPolygons(final Graphics g, final Iterator iter) {
     while(iter.hasNext()) {
       final Object obj = iter.next();
       if(obj instanceof Polygon) {
         g.drawPolygon((Polygon)obj);
       }
     }
   }
 
   /**
      fill a bunch of polygons
      @param g the graphics context
      @param v a Container of polygons, other classes are ignored
   **/
   public static void fillPolygons(final Graphics g, final Collection v) {
     fillPolygons(g, v.iterator());
   }
 
 
   /**
      fill a bunch of polygons
      @param g the graphics context
      @param iter an Enumeration of polygons, other classes are ignored
   **/
   public static void fillPolygons(final Graphics g, final Iterator iter) {
     while(iter.hasNext()) {
       final Object obj = iter.next();
       if(obj instanceof Polygon) {
         g.fillPolygon((Polygon)obj);
       }
     }
   }
 
   public static int getMaxWidth(final JComboBox combo, final FontMetrics fm) {
     int maxLen = 0;
     for(int i=0; i<combo.getItemCount(); i++) {
       String str = combo.getItemAt(i).toString();
       int wi = fm.stringWidth(str);
       if(wi > maxLen) {
         maxLen = wi;
       }
     }
     return maxLen + 20; // leave room for the scroll bar
   }
 
   /**
      Create an icon from the resource at path.
 
      @pre (path != null)
   **/
   static public ImageIcon getIcon(final String path) {
     return new ImageIcon(ClassLoader.getSystemClassLoader().getResource(path));
   }
 
   /**
      Popup a warning dialog displaying <tt>message</tt>.
   **/
   static public void notImplemented(final String message) {
     JOptionPane.showMessageDialog(null, message, "Not Implemented", JOptionPane.WARNING_MESSAGE);
   }
 
   /**
      Popup an error dialog with <tt>message</tt> in it.
   **/
   static public void error(final String message) {
     JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.WARNING_MESSAGE);
   }
   
 }
