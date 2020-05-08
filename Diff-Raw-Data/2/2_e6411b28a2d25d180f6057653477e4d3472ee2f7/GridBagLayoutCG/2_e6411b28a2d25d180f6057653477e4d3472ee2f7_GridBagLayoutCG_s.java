 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 import org.wings.*;
 import org.wings.plaf.css.PaddingVoodoo;
 import org.wings.io.Device;
 
 import java.awt.*;
 import java.io.IOException;
 
 public class GridBagLayoutCG extends AbstractLayoutCG {
     private static final long serialVersionUID = 1L;
 
     /**
      * Renders a gridbag layout using invisible layouter tables.
      *
      * @param d the device to write the code to
      * @param l the layout manager
      */
     public void write(Device d, SLayoutManager l)
             throws IOException {
         final SGridBagLayout layout = (SGridBagLayout) l;
         final SContainer container = layout.getContainer();
         final boolean header = layout.getHeader();
         final boolean useCellInsets = layout.getVgap() == -1 && layout.getHgap() == -1;
         final SGridBagLayout.Grid grid = layout.getGrid();
         final TableCellStyle cellStyle = cellLayoutStyle(layout);
         final TableCellStyle origCellStyle = cellStyle.makeACopy();
 
         SDimension preferredSize = container.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
         boolean clientLayout = isMSIE(container) && height != null && !"auto".equals(height);
         int vertivalOversize = layoutOversize(layout);
 
         if (grid.cols == 0) {
             return;
         }
 
         openLayouterBody(d, layout);
 
         for (int row = grid.firstRow; row < grid.rows; row++) {
             Utils.printNewline(d, container);
             if (clientLayout) {
                 d.print("<tr");
                 Utils.optAttribute(d, "yweight", determineRowHeight(layout, row));
                 if (useCellInsets) {
                     vertivalOversize = 0;
                     for (int col = grid.firstCol; col < grid.cols; col++) {
                         final SComponent comp = grid.grid[col][row];
                         if (comp != null) {
                             GridBagConstraints c = layout.getConstraints(comp);
                             Insets insets = c.insets;
                             if (insets != null)
                                 vertivalOversize = Math.max(vertivalOversize, cellOversize(layout, insets));
                         }
                     }
                 }
                 Utils.optAttribute(d, "oversize", vertivalOversize);
                 d.print(">");
             }
             else {
                 openLayouterRow(d, determineRowHeight(layout, row) + "%");
             }
             for (int col = grid.firstCol; col < grid.cols; col++) {
                 final SComponent comp = grid.grid[col][row];
                 Utils.printNewline(d, container);
 
                 cellStyle.renderAsTH = row == grid.firstRow && header;
                 cellStyle.defaultLayoutCellHAlignment = SConstants.CENTER;
                 cellStyle.defaultLayoutCellVAlignment = SConstants.CENTER;
 
                 if (comp == null) {
                     cellStyle.colspan = -1;
                     cellStyle.rowspan = -1;
                     cellStyle.width = null;
 
                     openLayouterCell(d, null, cellStyle);
                     closeLayouterCell(d, null, cellStyle.renderAsTH);
                 } else {
                     GridBagConstraints c = layout.getConstraints(comp);
 
                     if ((c.gridx == SGridBagLayout.LAST_CELL || c.gridx == col) &&
                             (c.gridy == SGridBagLayout.LAST_CELL || c.gridy == row)) {
 
                         int gridwidth = c.gridwidth;
                         if (gridwidth == GridBagConstraints.RELATIVE) {
                             gridwidth = grid.cols - col - 1;
                         } else if (gridwidth == GridBagConstraints.REMAINDER) {
                             gridwidth = grid.cols - col;
                         }
                         if (gridwidth < 2) {
                             gridwidth = -1;
                         }
 
                         int gridheight = c.gridheight;
                         if (gridheight == GridBagConstraints.RELATIVE) {
                             gridheight = grid.cols - col - 1;
                         } else if (gridheight == GridBagConstraints.REMAINDER) {
                             gridheight = grid.cols - col;
                         }
                         if (gridheight < 2) {
                             gridheight = -1;
                         }
 
                         cellStyle.width = null;
                         if (c.weightx > 0 && grid.colweight[row] > 0) {
                             cellStyle.width = (int)(100 * c.weightx / grid.colweight[row]) + "%";
                         }
 
                         if (useCellInsets) {
                             cellStyle.setInsets((Insets) c.insets.clone());
                         }
                         cellStyle.colspan = gridwidth;
                         cellStyle.rowspan = gridheight;
 
                         if (PaddingVoodoo.hasPaddingInsets(container)) {
                            final Insets patchedInsets = (Insets) origCellStyle.getInsets().clone();
                             final boolean isFirstRow = row == grid.firstRow;
                             final boolean isLastRow = row == grid.rows - 1;
                             final boolean isFirstCol = col == grid.firstCol;
                             final boolean isLastCol = col == grid.cols - 1;
                             PaddingVoodoo.doBorderPaddingsWorkaround(container.getBorder(), patchedInsets, isFirstRow, isFirstCol, isLastCol, isLastRow);
                             cellStyle.setInsets(patchedInsets);
                         }
 
                         openLayouterCell(d, comp, cellStyle);
 
                         Utils.printNewline(d, comp);
                         comp.write(d); // Render component
 
                         closeLayouterCell(d, comp, cellStyle.renderAsTH);
                     }
                 }
             }
             Utils.printNewline(d, container);
             closeLayouterRow(d);
         }
         closeLayouterBody(d, layout);
     }
 
     /**
      * Copy and paste extracted method to determine an optional row height of the passed row.
      * Was necessary to avoid a rendering bug with Gecko engines leading to a messed up layout.
      * Refer to http://jira.j-wings.org/browse/WGS-120
      *
      * @param layout The gridbaglayout
      * @param row    The row
      * @return Row height percentage as int or 0
      */
     protected int determineRowHeight(SGridBagLayout layout, int row) {
         final SGridBagLayout.Grid grid = layout.getGrid();
         int rowHeight = 0;
 
         for (int col = grid.firstCol; col < grid.cols; col++) {
             SComponent comp = grid.grid[col][row];
             if (comp != null) {
                 GridBagConstraints c = layout.getConstraints(comp);
                 if ((c.gridx == SGridBagLayout.LAST_CELL || c.gridx == col)
                         && (c.gridy == SGridBagLayout.LAST_CELL || c.gridy == row)) {
                     if (c.weighty > 0 && grid.rowweight[col] > 0) {
                         int cellHeight = (int) (100 * c.weighty / grid.rowweight[col]);
                         if (cellHeight > rowHeight) {
                             rowHeight = cellHeight;
                         }
                     }
                 }
             }
         }
         return rowHeight;
     }
 
     protected int getLayoutHGap(SLayoutManager layout) {
         return ((SGridBagLayout)layout).getHgap();
     }
 
     protected int getLayoutVGap(SLayoutManager layout) {
         return ((SGridBagLayout)layout).getVgap();
     }
 
     protected int getLayoutBorder(SLayoutManager layout) {
         return layout.getBorder();
     }
 
     protected int layoutOversize(SLayoutManager layout) {
         return ((SGridBagLayout)layout).getVgap() + layout.getBorder();
     }
 
     public int getDefaultLayoutCellHAlignment() {
         return SConstants.NO_ALIGN;  // Don't knoff.
     }
 
     public int getDefaultLayoutCellVAlignment() {
         return SConstants.NO_ALIGN;  // Don't knoff.
     }
 
 }
