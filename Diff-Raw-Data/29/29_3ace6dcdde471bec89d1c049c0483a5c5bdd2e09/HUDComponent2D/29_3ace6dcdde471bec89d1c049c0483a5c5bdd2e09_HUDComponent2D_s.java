 /*
  * Project Wonderland
  * 
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  * 
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  * 
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  * 
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.hud.client;
 
 import java.awt.Dimension;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 
 /**
  * A HUDComponent2D is a 2D object that can be displayed on the HUD.
  * It has a position (x, y), a width and a height.
  *
  * A HUDComponent2D can be visible or invisible. It can also be enabled,
  * in which case it responds to mouse and keyboard events, or disabled.
  *
  * @author nsimpson
  */
 public class HUDComponent2D extends HUDObject2D implements HUDComponent {
 
     private static final Logger logger = Logger.getLogger(HUDComponent2D.class.getName());
     private Cell cell;
     protected JComponent component;
     protected Window2D window;
 
     public HUDComponent2D() {
         super();
     }
 
     /**
      * Creates a new HUD component instance for a Swing component.
      *
      * @param component the Swing component
      */
     public HUDComponent2D(JComponent component) {
         this();
         setComponent(component);
         Dimension size = component.getPreferredSize();
         setBounds(0, 0, size.width, size.height);
         JmeClientMain.getFrame().getCanvas3DPanel().add(component);
     }
 
     /**
      * Creates a new HUD component instance for a Swing component, bound to
      * a Cell.
      *
      * @param component the Swing component
      * @param cell the Cell
      */
     public HUDComponent2D(JComponent component, Cell cell) {
         this(component);
         this.cell = cell;
     }
 
     /**
      * Creates a new HUD component instance for a Window2D.
      *
      * @param component the Swing component
      */
     public HUDComponent2D(Window2D window) {
         this.window = window;
         setBounds(getX(), getY(), window.getWidth(), window.getHeight());
     }
 
     /**
      * Creates a new HUD component instance for a Window2D, bound to
      * a Cell.
      *
      * @param component the Swing component
      * @param cell the Cell
      */
     public HUDComponent2D(Window2D window, Cell cell) {
         this(window);
         this.cell = cell;
     }
 
     /**
      * Sets the Swing component of this HUD component.
      *
      * @param component the Swing component
      */
     public void setComponent(JComponent component) {
         this.component = component;
         setBounds(0, 0, (int) component.getPreferredSize().getWidth(), (int) component.getPreferredSize().getHeight());
         component.addComponentListener(new ComponentListener() {
 
             public void componentShown(ComponentEvent e) {
             }
 
             public void componentHidden(ComponentEvent e) {
             }
 
             public void componentMoved(ComponentEvent e) {
             }
 
             public void componentResized(ComponentEvent e) {
                 // TODO: generate a resize event so the HUD frame can adapt
                 logger.fine("swing component resized: " + e);
                notifyEventListeners(HUDEventType.RESIZED);
             }
         });
     }
 
     /**
      * Gets the Swing component associated with this HUD component.
      *
      * @return the Swing component
      */
     public JComponent getComponent() {
         return component;
     }
 
     /**
      * Sets the Window2D of this HUD component.
      *
      * @param window the Window2D to associate with this HUD component
      */
     public void setWindow(Window2D window) {
         this.window = window;
         setBounds(getX(), getY(), window.getWidth(), window.getHeight());
     }
 
     /**
      * Gets the Window2D of this HUD component.
      *
      * @return the Window2D
      */
     public Window2D getWindow() {
         return window;
     }
 
     /**
      * Associates a cell with this HUD component for in-world display
      * @param cell the cell to associate with this HUD component
      */
     public void setCell(Cell cell) {
         this.cell = cell;
     }
 
     /**
      * Gets the cell associated with this HUD component
      * @return the associated cell
      */
     public Cell getCell() {
         return cell;
     }
 
     @Override
     public String toString() {
         return "HUDComponent2D: " +
                 "cell: " + cell + " " +
                 ((component != null) ? component.getClass().getName() : "") +
                 "window: " + window + " " +
                 super.toString();
     }
 }
