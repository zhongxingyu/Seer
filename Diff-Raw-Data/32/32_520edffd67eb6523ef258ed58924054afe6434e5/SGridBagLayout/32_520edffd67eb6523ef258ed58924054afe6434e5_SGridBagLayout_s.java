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
 
 import org.wings.event.SComponentEvent;
 import org.wings.event.SComponentListener;
 
 import java.awt.*;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * Tries to simulate swing GridBag behaviour as close as possible.
  * <p/>
  * This layout is similar to Swing's GridBagLayout, though it can't
  * implement all functionalities because of the limitations of
  * HTML-table. It probably doesn't work exactly like its
  * Swing-counterpart - as a general hint: don't be too clever...
  * <p/>
  * <p/>
  * SComponents are usually added using an instance of
  * java.awt.GridBagConstraints which is copied while adding it (so you
  * might reuse it to add other SComponents). There are basically two
  * ways of adding: explicitly setting gridx and gridy or leaving
  * those at the default (RELATIVE) and let SGridBagLayout decide where
  * to put them. Normally they will be added horizontally, unless you
  * explicitly set gridx, which will add the SComponents
  * vertically. With setting gridy you can choose a row in which the
  * SComponents will be added. If you want to finish a row/column, you
  * can set gridwidth/gridheight to REMAINDER or RELATIVE - REMAINDER
  * marks the row/column to be finished while RELATIVE tells
  * SGridBagLayout that the <em>next</em> added SComponent will be the
  * last cell of the row/column which will always be placed at the end
  * (while the 'RELATIVE'-SComponent will be expanded to fill the gap).
  * <p/>
  * <p/>
  * <em>Important:</em> When choosing a new row/column, the next
  * gridx/gridy-value that SGridLayout will choose will always be 0,
  * even if there is already a SComponent at that position. If you
  * really need to be clever, explicitly set gridx and gridy,
  * especially if you plan to dynamically add and remove SComponents.
  * <p/>
  * <p/>
  * The size of a cell can be influenced in two ways: either set
  * gridwidth/gridheight to a value larger than 1 to say how many
  * regular cells this cell should span or use weightx/weighty to tell
  * the browser how much of the empty space this cell should eat up
  * (e.g. if there are 3 cells with each weight=1, they will all get
  * 33%). The last method has two disadvantages: firstly, it uses the
  * deprecated width/height-parameters of the HTML-td statement and
  * secondly, it must be carefully used to get the correct result: all
  * cells of a row/column should have the same weighty/weightx or 0, so
  * it might be easier to set these values only in the first
  * column/row.
  * <p/>
  * <p/>
  * GridBagConstraints has many more options than those described
  * above, but the current implementation can't use them.
  *
  * @author <a href="mailto:js@trollhead.net">Jochen Scharrlach</a>
  * @version $Revision $
  */
 public class SGridBagLayout
         extends SAbstractLayoutManager implements SComponentListener {
     /**
      * Map of all managed components (key: component, value: constraint)
      */
     private HashMap components = new HashMap();
 
     /**
      * @see #getBorder
      */
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
      * @see #getHeader
      */
     protected boolean header = false;
 
     /**
      * The defaults to use if the addComponent()-call does not give
      * the constraints.
      */
     protected GridBagConstraints defaultConstraints =
             new GridBagConstraints();
 
     /**
      * Contains a pre-calculated grid (or null)
      */
     protected transient Grid currentGrid;
 
     /**
      * Indicates that the corresponding SComponent should be at the
      * end of the row/column. This value is only for internal use and
      * cannot be used with addComponent.
      */
     public static final int LAST_CELL = -1;
 
     /**
      * creats a new gridbag layout
      */
    public SGridBagLayout() {}
 
     /**
      * Add the given component with the given constraints to the
      * layout.
      *
      * @param comp       the component to add
      * @param constraint instance of GridBagConstraints or null
      * @param index      ignored
      */
     public void addComponent(SComponent comp, Object constraint, int index) {
         // The grid has to be rebuilt
         currentGrid = null;
 
         GridBagConstraints c = (GridBagConstraints) constraint;
         if (c == null) {
             c = defaultConstraints;
         }
         c = (GridBagConstraints) c.clone();
 
         comp.addComponentListener(this);
         components.put(comp, c);
     }
 
     public void removeComponent(SComponent c) {
         // The grid has to be rebuilt
         currentGrid = null;
         components.remove(c);
         c.removeComponentListener(this);
     }
 
     public void componentHidden(SComponentEvent e) {
         // The grid has to be rebuilt
         currentGrid = null;
     }
 
     public void componentMoved(SComponentEvent e) {
         // ignored
     }
 
     public void componentResized(SComponentEvent e) {
         // ignored
     }
 
     public void componentShown(SComponentEvent e) {
         // The grid has to be rebuilt
         currentGrid = null;
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
 
     /**
      * Set the border width.
      *
      * @param pixel the new border width in pixels
      */
     public void setBorder(int pixel) {
         border = pixel;
     }
 
     /**
      * Get the border width.
      *
      * @return the border width in pixels
      */
     public int getBorder() {
         return border;
     }
 
     /**
      * Specify if the first row should be printed as header
      *
      * @param b true=the first row is used as header
      */
     public void setHeader(boolean b) {
         header = b;
     }
 
     /**
      * Query if the first row will be printed as header
      *
      * @return true=the first row is used as header
      */
     public boolean getHeader() {
         return header;
     }
 
     // Some helper functions for CGs
 
     /**
      * This class prepares all information necessary to plot the
      * layout to the output device. The information will be outdated as
      * soon as components will be added or removed from the layout.
      */
     public class Grid {
         /**
          * Number of columns
          */
         public int cols;
 
         /**
          * Number of rows
          */
         public int rows;
 
         /**
          * The matrix with all known SComponents. A SComponent might
          * appear in more than one cell, indicating that it spans more
          * than one cell - usually it will only be plotted if its
          * value for gridx/gridy matches the current cell (exception:
          * gridx/gridy might also be set to LAST_CELL).
          */
         public SComponent[][] grid;
 
         /**
          * The total column-weight of a row(!). The cumulated weightx
          * of all cells of a row..
          */
         public double[] colweight;
 
         /**
          * The total row-weight of a column(!). The cumulated weighty
          * of all cells of a column..
          */
         public double[] rowweight;
 
         /**
          * The first row that contains cells
          */
         public int firstRow;
 
         /**
          * The first column that contains cells
          */
         public int firstCol;
 
         /**
          * Row for the next horizontal add (gridx=RELATIVE). If gridy is
          * not RELATIVE and does not match nextHorRow, the SComponent will
          * be added at gridx=0.
          */
         private int nextHorRow = 0;
 
         /**
          * Column for the next horizontal add (gridx=RELATIVE)
          */
         private int nextHorCol = 0;
 
         /**
          * Row for the next vertical add (gridx != RELATIVE, gridy =
          * RELATIVE).
          */
         private int nextVertRow = 0;
 
         /**
          * Column for the next vertical add (gridx != RELATIVE, gridy =
          * RELATIVE). If gridx does not match nextVertCol, the SComponent
          * will be added at gridy=0.
          */
         private int nextVertCol = 0;
 
         private final HashMap modifiedConstraints = new HashMap(components.size());
 
         /**
          * Initialize all members
          */
         public Grid() {
             cols = 0;
             rows = 0;
 
             for (Iterator i = getContainer().getComponentList().iterator(); i.hasNext();) {
                 SComponent comp = (SComponent) i.next();
                 if (!comp.isVisible()) {
                     continue;
                 }
 
                 GridBagConstraints c = (GridBagConstraints) components.get(comp);
                 c = (GridBagConstraints) c.clone();
                 modifiedConstraints.put(comp, c);
 
                 if (c.gridx >= 0) {
                     if (c.gridx != nextVertCol) {
                         nextVertRow = 0;
                     }
 
                     if (c.gridy < 0) {
                         c.gridy = nextVertRow;
                     }
                 } else {
                     if (c.gridy >= 0 && c.gridy != nextHorRow) {
                         nextHorCol = 0;
                     }
 
                     if (c.gridy < 0) {
                         c.gridy = nextHorRow;
                     } else if (c.gridy != nextHorRow) {
                         nextHorCol = 0;
                     }
                     c.gridx = nextHorCol;
                 }
                 if (c.gridx == LAST_CELL) {
                     if (c.gridy == LAST_CELL) {
                         nextHorRow = 0;
                         nextVertRow = 0;
                     } else {
                         nextHorRow = c.gridy + 1;
                         nextVertRow = c.gridy + 1;
                     }
                     nextHorCol = 0;
                     nextVertCol = 0;
                 } else {
                     if (c.gridy == LAST_CELL) {
                         nextHorRow = 0;
                         nextVertRow = 0;
                         nextHorCol = c.gridx + 1;
                         nextVertCol = c.gridx + 1;
                     } else {
                         nextHorCol = c.gridx;
                         nextVertCol = c.gridx;
                         nextHorRow = c.gridy;
                         nextVertRow = c.gridy;
 
                         if (c.gridwidth == GridBagConstraints.RELATIVE) {
                             nextHorCol = LAST_CELL;
                         } else if (c.gridwidth == GridBagConstraints.REMAINDER) {
                             nextHorCol = 0;
                             nextHorRow++;
                         } else {
                             if (c.gridwidth > 0) {
                                 nextHorCol += c.gridwidth;
                             } else {
                                 nextHorCol++;
                             }
                         }
 
                         if (c.gridheight == GridBagConstraints.RELATIVE) {
                             nextVertRow = LAST_CELL;
                         } else if (c.gridheight == GridBagConstraints.REMAINDER) {
                             nextVertRow = 0;
                             nextVertCol++;
                         } else {
                             if (c.gridheight > 0) {
                                 nextVertRow += c.gridheight;
                             } else {
                                 nextVertRow++;
                             }
                         }
                     }
                 }
 
 
                 if (c.gridx != SGridBagLayout.LAST_CELL) {
                     int col = c.gridx;
                     if (c.gridwidth == GridBagConstraints.RELATIVE) {
                         col++;
                     } else if (c.gridwidth > 1) {
                         col += c.gridwidth - 1;
                     }
 
                     int row = c.gridy;
                     if (c.gridheight == GridBagConstraints.RELATIVE) {
                         row++;
                     } else if (c.gridheight > 1) {
                         row += c.gridheight - 1;
                     }
 
                     if (col >= cols) {
                         cols = col + 1;
                     }
                     if (row >= rows) {
                         rows = row + 1;
                     }
                 }
             }
 
             grid = new SComponent[cols][rows];
             rowweight = new double[cols];
             colweight = new double[rows];
 
             for (Iterator i = getContainer().getComponentList().iterator();
                  i.hasNext();) {
                 SComponent comp = (SComponent) i.next();
                 if (!comp.isVisible()) {
                     continue;
                 }
                 GridBagConstraints c = (GridBagConstraints)
                         modifiedConstraints.get(comp);
 
                 int maxcol = c.gridx + c.gridwidth;
                 int maxrow = c.gridy + c.gridheight;
 
                 if (c.gridwidth == GridBagConstraints.RELATIVE) {
                     maxcol = cols - 1;
                 } else if (c.gridwidth == GridBagConstraints.REMAINDER) {
                     maxcol = cols;
                 }
                 if (c.gridheight == GridBagConstraints.RELATIVE) {
                     maxrow = rows - 1;
                 } else if (c.gridheight == GridBagConstraints.REMAINDER) {
                     maxrow = rows;
                 }
                 int col = c.gridx;
                 if (col == SGridBagLayout.LAST_CELL) {
                     col = cols - 1;
                     maxcol = cols;
                 }
                 int row = c.gridy;
                 if (row == SGridBagLayout.LAST_CELL) {
                     row = rows - 1;
                     maxrow = rows;
                 }
                 colweight[row] += c.weightx;
                 rowweight[col] += c.weighty;
 
                 for (; col < maxcol; col++) {
                     for (int r = row; r < maxrow; r++) {
                         grid[col][r] = comp;
                     }
                 }
             }
             for (firstRow = 0; firstRow < rows; firstRow++) {
                 int col;
                 for (col = 0; col < cols; col++) {
                     if (grid[col][firstRow] != null) {
                         break;
                     }
                 }
                 if (col < cols) {
                     break;
                 }
             }
             for (firstCol = 0; firstCol < cols; firstCol++) {
                 int row;
                 for (row = 0; row < rows; row++) {
                     if (grid[firstCol][row] != null) {
                         break;
                     }
                 }
                 if (row < rows) {
                     break;
                 }
             }
         }
     }
 
     /**
      * Build a grid from the current configuration. Make sure the
      * layout is not altered while using the Grid!
      *
      * @return the Grid-instance
      */
     public Grid getGrid() {
         if (currentGrid == null) {
             currentGrid = new Grid();
         }
         return currentGrid;
     }
 
     /**
      * Retrieve the constraint of a SComponent. The constraint must
      * not be altered!
      *
      * @param comp the component
      * @return the constraint or null if the component is unknown
      */
     final public GridBagConstraints getConstraints(SComponent comp) {
         // It might be better to return a copy of the constraint,
         // but that would hurt the performance
         return (GridBagConstraints) getGrid().modifiedConstraints.get(comp);
     }
 }
 
 
