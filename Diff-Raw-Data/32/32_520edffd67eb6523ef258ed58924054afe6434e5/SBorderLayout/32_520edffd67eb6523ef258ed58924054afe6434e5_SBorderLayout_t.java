 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Swing-like border layout.
  * <p/>
  * You can add up to 5 components to a
  * container with this layout at the following positions:
  * <code>NORTH</code>, <code>SOUTH</code>, <code>EAST</code>,
  * <code>WEST</code> and <code>CENTER</code>.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SBorderLayout
         extends SAbstractLayoutManager {
 
     public static final String NORTH = "North";
     public static final String SOUTH = "South";
     public static final String EAST = "East";
     public static final String WEST = "West";
     public static final String CENTER = "Center";
 
     protected Map components = new HashMap(5);
     protected int border = 0;
 
     /**
      * The horizontal gap (in pixels) specifiying the space
      * between columns.  They can be changed at any time.
      * This should be a non-negative integer.
      */
     protected int hgap = 0;
 
     /**
      * The vertical gap (in pixels) which specifiying the space
      * between rows.  They can be changed at any time.
      * This should be a non negative integer.
      */
     protected int vgap = 0;
 
 
     /**
      * creates a new border layout
      */
    public SBorderLayout() {
        this(0,0);
    }
 
     /**
      * creates a new border layout
      */
     public SBorderLayout(int hgap, int vgap) {
         setHgap(hgap);
         setVgap(vgap);
        // like swing
        setPreferredSize(SDimension.FULLWIDTH);
     }
 
 
     public void addComponent(SComponent c, Object constraint, int index) {
         if (constraint == null)
             constraint = CENTER;
 
         components.put(constraint, c);
     }
 
     /**
      * Removes the component from the layout manager
      * @param c the component to be removed
      */
     public void removeComponent(SComponent c) {
         if (c == null)
             return;
 
         Iterator iterator = components.entrySet().iterator();
         while (iterator.hasNext()) {
             Map.Entry entry = (Map.Entry) iterator.next();
             if (c.equals(entry.getValue())) {
                 iterator.remove();
                 break;
             }
         }
     }
 
     /**
      * Returns a map of all components.
      * @return the components contained by the layout
      */
     public Map getComponents() {
         return components;
     }
 
     /**
      * Set the thickness of the border.
      * Default is 0, which means no border.
      *
      * @param pixel thickness of the border
      */
     public void setBorder(int pixel) {
         border = pixel;
     }
 
     /**
      * Returns the thickness of the border.
      *
      * @return thickness of the border
      */
     public int getBorder() {
         return border;
     }
 
     /**
      * Gets the horizontal gap between components in pixel. Rendered half as margin left and margin right
      * Some PLAFs might ignore this property.
      *
      * @return the horizontal gap between components
      */
     public int getHgap() {
         return hgap;
     }
 
     /**
      * Sets the horizontal gap between components to the specified value in pixe. Rendered half as margin left and margin right
      * Some PLAFs might ignore this property.
      *
      * @param hgap the horizontal gap between components
      */
     public void setHgap(int hgap) {
         this.hgap = hgap;
     }
 
     /**
      * Gets the vertical gap between components in pixel. Rendered half as margin top and margin bottom
      * Some PLAFs might ignore this property.
      *
      * @return the vertical gap between components
      */
     public int getVgap() {
         return vgap;
     }
 
     /**
      * Sets the vertical gap between components to the specified value in pixel.
      * Rendered half as margin top and margin bottom. Some PLAFs might ignore this property.
      *
      * @param vgap the vertical gap between components
      */
     public void setVgap(int vgap) {
         this.vgap = vgap;
     }
 }
 
 
