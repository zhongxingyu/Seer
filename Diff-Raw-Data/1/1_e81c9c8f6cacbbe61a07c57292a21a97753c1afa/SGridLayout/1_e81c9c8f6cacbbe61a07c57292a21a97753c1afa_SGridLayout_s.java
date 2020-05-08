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
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Swing-like grid layout.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SGridLayout extends SAbstractLayoutManager {
     protected ArrayList components = new ArrayList(2);
     protected int rows = 1;
     protected int cols = 1;
     protected int border = 0;
     protected boolean renderFirstLineAsHeader = false;
 
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
      * creates a new grid layout with 1 row and 1 column extent
      */
     public SGridLayout() {
        this.setPreferredSize(SDimension.FULLAREA);
     }
 
     /**
      * creats a new grid layout with the given number of columns
      *
      * @param cols number of columns
      */
     public SGridLayout(int cols) {
         this();
         setColumns(cols);
     }
 
     /**
      * creats a new grid layout with the given number of columns and rows
      *
      * @param rows number of rows
      * @param cols number of columns
      */
     public SGridLayout(int rows, int cols) {
         this(cols);
         setRows(rows);
     }
     
     /**
      * creats a new grid layout with the given number of columns and rows and the given gaps
      *
      * @param rows number of rows
      * @param cols number of columns
      * @param hgap horizontal gap
      * @param vgap vertical gap
      */
     public SGridLayout(int rows, int cols, int hgap, int vgap ) {
         this( rows, cols );
         setHgap( hgap );
         setVgap( vgap );
     }
 
     /**
      * sets the number of columns
      *
      * @param c number of columns
      */
     public void setColumns(int c) {
         cols = c;
     }
 
     /**
      * returns the number of columns
      *
      * @return number of columns
      */
     public int getColumns() {
         return cols;
     }
 
     /**
      * sets the number of rows
      *
      * @param r number of rows
      */
     public void setRows(int r) {
         rows = r;
     }
 
     /**
      * returns the number of rows
      *
      * @return number of rows
      */
     public int getRows() {
         return rows;
     }
 
     public void addComponent(SComponent c, Object constraint, int index) {
         components.add(index, c);
     }
 
     public void removeComponent(SComponent c) {
         components.remove(c);
     }
 
     /**
      * returns a list of all components
      *
      * @return all components
      */
     public List getComponents() {
         return components;
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
 
 //    /**
 //     * The paddding between the layout cells in pixel. Some PLAFs might ignore this property.
 //     * @param cellPadding cell padding in pixel
 //     */
 //    public void setCellPadding(int cellPadding) {
 //        this.cellPadding = cellPadding;
 //    }
 //
 //    /**
 //     * The paddding between the layout cells in pixel. Some PLAFs might ignore this property.
 //     * @return cell padding in pixel
 //     */
 //    public int getCellPadding() {
 //        return cellPadding;
 //    }
 //
 //    /**
 //     * The paddding between the layout cells in pixel. Some PLAFs might ignore this property.
 //     * @param cellSpacing The spacing between the layout cells. pixel
 //     */
 //    public void setCellSpacing(int cellSpacing) {
 //        this.cellSpacing = cellSpacing;
 //    }
 //
 //    /**
 //     * The paddding between the layout cells in pixel. Some PLAFs might ignore this property.
 //     * @return The spacing between the layout cells. pixel
 //     */
 //    public int getCellSpacing() {
 //        return cellSpacing;
 //    }
 
 
     /**
      * Typical PLAFs will render this layout as invisible table (border = 0). Use this property to make it visible
      *
      * @param borderWidth The rendered border with in pixel
      */
     public void setBorder(int borderWidth) {
         border = borderWidth;
     }
 
     /**
      * Typical PLAFs will render this layout as invisible table (border = 0). Use this property to make it visible
      *
      * @return The rendered border with in pixel
      */
     public int getBorder() {
         return border;
     }
 
     /**
      * Renders the first line as HTML <code>&lt;th&gt;</code> instead regular <code>&lt;tr&gt;</code>.
      *
      * @param renderAsTH true if first line should be rendered as header
      */
     public void setRenderFirstLineAsHeader(boolean renderAsTH) {
         renderFirstLineAsHeader = renderAsTH;
     }
 
     /**
      * {@link #setRenderFirstLineAsHeader(boolean)}
      */
     public boolean getRenderFirstLineAsHeader() {
         return renderFirstLineAsHeader;
     }
 }
 
 
