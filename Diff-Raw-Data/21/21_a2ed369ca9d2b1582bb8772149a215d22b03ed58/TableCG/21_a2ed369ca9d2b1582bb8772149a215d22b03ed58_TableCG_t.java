 /*
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
 package org.wings.plaf.css;
 
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.plaf.CGManager;
 import org.wings.session.SessionManager;
 import org.wings.table.*;
 import org.wings.util.SStringBuilder;
 
 import java.awt.*;
 import java.io.IOException;
 
 public final class TableCG
     extends AbstractComponentCG
     implements org.wings.plaf.TableCG
 {
     private static final long serialVersionUID = 1L;
     protected String fixedTableBorderWidth;
     protected SIcon editIcon;
    protected String selectionColumnWidth = "60";
 
     int horizontalOversize = 4;
 
     public int getHorizontalOversize() {
         return horizontalOversize;
     }
 
     public void setHorizontalOversize(int horizontalOversize) {
         this.horizontalOversize = horizontalOversize;
     }
 
     /**
      * Initialize properties from config
      */
     public TableCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
         setFixedTableBorderWidth((String)manager.getObject("TableCG.fixedTableBorderWidth", String.class));
         setEditIcon(manager.getIcon("TableCG.editIcon"));
         selectionColumnWidth = (String)manager.getObject("TableCG.selectionColumnWidth", String.class);
     }
 
     /**
      * Tweak property. Declares a deprecated BORDER=xxx attribute on the HTML TABLE element.
      */
     public String getFixedTableBorderWidth() {
         return fixedTableBorderWidth;
     }
 
     /**
      * Tweak property. Declares a deprecated BORDER=xxx attribute on the HTML TABLE element.
      */
     public void setFixedTableBorderWidth(String fixedTableBorderWidth) {
         this.fixedTableBorderWidth = fixedTableBorderWidth;
     }
 
     /**
      * Sets the icon used to indicated an editable cell (if content is not direct clickable).
      */
     public void setEditIcon(SIcon editIcon) {
         this.editIcon = editIcon;
     }
 
     /**
      * @return Returns the icon used to indicated an editable cell (if content is not direct clickable).
      */
     public SIcon getEditIcon() {
         return editIcon;
     }
 
     /**
      * @return The width of the (optional) row selection column in px
      */
     public String getSelectionColumnWidth() {
         return selectionColumnWidth;
     }
 
     /**
      * The width of the (optional) row selection column in px
      *
      * @param selectionColumnWidth The width of the (optional) row selection column with unit
      */
     public void setSelectionColumnWidth(String selectionColumnWidth) {
         this.selectionColumnWidth = selectionColumnWidth;
     }
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
 
         final STable table = (STable)comp;
         final CGManager manager = table.getSession().getCGManager();
         Object value;
 
         value = manager.getObject("STable.defaultRenderer", STableCellRenderer.class);
         if (value != null) {
             table.setDefaultRenderer((STableCellRenderer)value);
             if (value instanceof SDefaultTableCellRenderer) {
                 SDefaultTableCellRenderer cellRenderer = (SDefaultTableCellRenderer)value;
                 cellRenderer.setEditIcon(editIcon);
             }
         }
 
         value = manager.getObject("STable.headerRenderer", STableCellRenderer.class);
         if (value != null) {
             table.setHeaderRenderer((STableCellRenderer)value);
         }
 
         value = manager.getObject("STable.rowSelectionRenderer", org.wings.table.STableCellRenderer.class);
         if (value != null) {
             table.setRowSelectionRenderer((org.wings.table.STableCellRenderer)value);
         }
 
         /*
         InputMap inputMap = new InputMap();
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK, false), "left");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK, false), "right");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK, false), "up");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK, false), "down");
         table.setInputMap(SComponent.WHEN_IN_FOCUSED_FRAME, inputMap);
 
         Action action = new AbstractAction()
         {
             public void actionPerformed(ActionEvent e) {
                 if (!table.isEditing())
                     return;
                 if (table.getEditingRow() > 0 && "up".equals(e.getActionCommand()))
                     table.setEditingRow(table.getEditingRow() - 1);
                 if (table.getEditingRow() < table.getRowCount() - 1 && "down".equals(e.getActionCommand()))
                     table.setEditingRow(table.getEditingRow() + 1);
                 if (table.getEditingColumn() > 0 && "left".equals(e.getActionCommand()))
                     table.setEditingColumn(table.getEditingColumn() - 1);
                 if (table.getEditingColumn() < table.getColumnCount() - 1 && "right".equals(e.getActionCommand()))
                     table.setEditingColumn(table.getEditingColumn() + 1);
                 table.requestFocus();
             }
         };
         ActionMap actionMap = new ActionMap();
         actionMap.put("up", action);
         actionMap.put("down", action);
         actionMap.put("left", action);
         actionMap.put("right", action);
         table.setActionMap(actionMap);
         */
 
         if (isMSIE(table))
             table.putClientProperty("horizontalOversize", new Integer(horizontalOversize));
     }
 
     public void uninstallCG(SComponent component) {
         super.uninstallCG(component);
         final STable table = (STable)component;
         table.setHeaderRenderer(null);
         table.setDefaultRenderer(null);
         table.setRowSelectionRenderer(null);
         table.setActionMap(null);
         table.setInputMap(null);
     }
 
     /**
      * write a specific cell to the device
      */
     protected void renderCellContent(final Device device, final STable table, final SCellRendererPane rendererPane,
                                      final int row, final int col)
         throws IOException
     {
         final boolean isEditingCell = table.isEditing() && row == table.getEditingRow() && col == table.getEditingColumn();
         final boolean editableCell = table.isCellEditable(row, col);
         final boolean selectableCell = table.getSelectionMode() != SListSelectionModel.NO_SELECTION && !table.isEditable() && table.isSelectable();
 
         final SComponent component;
         if (isEditingCell) {
             component = table.getEditorComponent();
         }
         else {
             component = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
         }
 
         final boolean isClickable = component instanceof SClickable;
 
         device.print("<td col=\"");
         device.print(col);
         device.print("\"");
 
         if (component == null) {
             device.print("></td>");
             return;
         }
         Utils.printTableCellAlignment(device, component, SConstants.LEFT, SConstants.TOP);
         Utils.optAttribute(device, "oversize", horizontalOversize);
 
         String parameter = null;
         if (table.isEditable() && !isEditingCell && editableCell)
             parameter = table.getEditParameter(row, col);
         else if (selectableCell)
             parameter = table.getToggleSelectionParameter(row, col);
 
         if (parameter != null && !isEditingCell && (selectableCell || editableCell) && !isClickable) {
             Utils.printClickability(device, table, parameter, true, table.getShowAsFormComponent());
             device.print(" class=\"cell clickable\"");
         }
         else
             device.print(" class=\"cell\"");
         device.print(">");
 
         rendererPane.writeComponent(device, component, table);
 
         device.print("</td>");
         Utils.printNewline(device, component);
     }
 
     protected void writeHeaderCell(final Device device, final STable table,
                                    final SCellRendererPane rendererPane,
                                    final int col)
         throws IOException
     {
         final SComponent comp = table.prepareHeaderRenderer(table.getHeaderRenderer(col), col);
 
         device.print("<th col=\"");
         device.print(col);
         device.print("\" class=\"head\"");
 
         Utils.printTableCellAlignment(device, comp, SConstants.CENTER, SConstants.CENTER);
         device.print(">");
 
         rendererPane.writeComponent(device, comp, table);
 
         device.print("</th>");
         Utils.printNewline(device, comp);
     }
 
     public final void writeInternal(final Device device, final SComponent _c) throws IOException {
         RenderHelper.getInstance(_c).forbidCaching();
 
         final STable table = (STable)_c;
 
         device.print("<table");
         writeAllAttributes(device, table);
         writeTableAttributes(device, table);
         device.print("><thead>");
         Utils.printNewline(device, table);
 
         Rectangle currentViewport = table.getViewportSize();
         Rectangle maximalViewport = table.getScrollableViewportSize();
         int startX = 0;
         int endX = table.getVisibleColumnCount();
         int startY = 0;
         int endY = table.getRowCount();
         int emptyIndex = maximalViewport != null ? maximalViewport.height : endY;
 
         if (currentViewport != null) {
             startX = currentViewport.x;
             endX = startX + currentViewport.width;
             startY = currentViewport.y;
             endY = startY + currentViewport.height;
         }
 
         writeColumnWidths(device, table, startX, endX);
 
         writeHeader(device, table, startX, endX);
 
         device.print("</thead>");
         Utils.printNewline(device, table);
         device.print("<tbody>");
 
         writeBody(device, table, startX, endX, startY, endY, emptyIndex);
 
         device.print("</tbody></table>");
         RenderHelper.getInstance(_c).allowCaching();
     }
 
     private void writeTableAttributes(Device device, STable table) throws IOException {
         final SDimension intercellPadding = table.getIntercellPadding();
         final SDimension intercellSpacing = table.getIntercellSpacing();
         Utils.writeEvents(device, table, null);
 
         // TODO: border="" should be obsolete
         // TODO: cellspacing and cellpadding may be in conflict with border-collapse
         /* Tweaking: CG configured to have a fixed border="xy" width */
         Utils.optAttribute(device, "border", fixedTableBorderWidth);
         Utils.optAttribute(device, "cellspacing", ((intercellSpacing != null) ? "" + intercellSpacing.getWidthInt() : null));
         Utils.optAttribute(device, "cellpadding", ((intercellPadding != null) ? "" + intercellPadding.getHeightInt() : null));
     }
 
     private void writeColumnWidths(Device device, STable table, int startX, int endX) throws IOException {
         STableColumnModel columnModel = table.getColumnModel();
         if (columnModel != null && atLeastOneColumnWidthIsNotNull(columnModel)) {
             device.print("<colgroup>");
             if (isSelectionColumnVisible(table))
                 writeCol(device, selectionColumnWidth);
 
             for (int i = startX; i < endX; ++i) {
                 STableColumn column = columnModel.getColumn(i);
                 if (!column.isHidden())
                     writeCol(device, column.getWidth());
                 else
                     ++endX;
             }
             device.print("</colgroup>");
             Utils.printNewline(device, table);
         }
     }
 
     private void writeHeader(Device device, STable table, int startX, int endX) throws IOException {
         if (!table.isHeaderVisible())
             return;
 
         final SCellRendererPane rendererPane = table.getCellRendererPane();
         STableColumnModel columnModel = table.getColumnModel();
 
         SStringBuilder headerArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_HEADER));
         device.print("<tr class=\"header\"");
         Utils.optAttribute(device, "style", headerArea);
         device.print(">");
 
         Utils.printNewline(device, table, 1);
         writeSelectionHeader(device, table);
 
         for (int i = startX; i < endX; ++i) {
             STableColumn column = columnModel.getColumn(i);
             if (!column.isHidden())
                 writeHeaderCell(device, table, rendererPane, i);
             else
                 ++endX;
         }
         device.print("</tr>");
         Utils.printNewline(device, table);
     }
 
     private void writeBody(Device device, STable table,
             int startX, int endX, int startY, int endY, int emptyIndex) throws IOException {
         final SListSelectionModel selectionModel = table.getSelectionModel();
 
         SStringBuilder selectedArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_SELECTED));
         SStringBuilder evenArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_EVEN_ROWS));
         SStringBuilder oddArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_ODD_ROWS));
         final SCellRendererPane rendererPane = table.getCellRendererPane();
         STableColumnModel columnModel = table.getColumnModel();
 
         int backupEndX = endX; // Backup endX because we have to restore it after each inner loop.
 
         for (int r = startY; r < endY; ++r) {
             if (r >= emptyIndex) {
                 int colspan = endX - startX;
                 device.print("<tr>\n");
                 if (isSelectionColumnVisible(table)) {
                     device.print("  <td class=\"empty\"></td>\n");
                 }
                 device.print("  <td class=\"empty\" colspan=\"" + colspan + "\">&nbsp;</td>\n");
                 device.print("</tr>\n");
                 continue;
             }
 
             String rowStyle = table.getRowStyle(r);
             SStringBuilder rowClass = new SStringBuilder(rowStyle != null ? rowStyle + " " : "");
             device.print("<tr");
             if (selectionModel.isSelectedIndex(r)) {
                 Utils.optAttribute(device, "style", selectedArea);
                 rowClass.append("selected ");
             }
             else if (r % 2 != 0)
                 Utils.optAttribute(device, "style", oddArea);
             else
                 Utils.optAttribute(device, "style", evenArea);
 
             rowClass.append(r % 2 != 0 ? "odd" : "even");
             Utils.optAttribute(device, "class", rowClass);
             device.print(">");
 
             writeSelectionBody(device, table, rendererPane, r);
 
             for (int c = startX; c < endX; ++c) {
                 STableColumn column = columnModel.getColumn(c);
                 if (!column.isHidden())
                     renderCellContent(device, table, rendererPane, r, c);
                 else
                     ++endX;
             }
             endX = backupEndX;
 
             device.print("</tr>");
             Utils.printNewline(device, table);
         }
     }
 
     protected void writeSelectionHeader(Device device, STable table) throws IOException {
         if (isSelectionColumnVisible(table)) {
             device.print("<th valign=\"middle\" class=\"num\"");
             Utils.optAttribute(device, "width", selectionColumnWidth);
             device.print("></th>");
         }
     }
 
     private boolean atLeastOneColumnWidthIsNotNull(STableColumnModel columnModel) {
         int columnCount = columnModel.getColumnCount();
         for (int i = 0; i < columnCount; i++)
             if (columnModel.getColumn(i).getWidth() != null)
                 return true;
         return false;
     }
 
     private void writeCol(Device device, String width) throws IOException {
         device.print("<col");
         Utils.optAttribute(device, "width", width);
         device.print("/>");
     }
 
     /**
      * Renders the row sometimes needed to allow row selection.
      */
     protected void writeSelectionBody(final Device device, final STable table, final SCellRendererPane rendererPane,
                                          final int row)
         throws IOException
     {
         final STableCellRenderer rowSelectionRenderer = table.getRowSelectionRenderer();
         if (isSelectionColumnVisible(table)) {
             final String columnStyle = Utils.joinStyles((SComponent) rowSelectionRenderer, "num");
 
             device.print("<td valign=\"top\" align=\"right\"");
             Utils.optAttribute(device, "width", selectionColumnWidth);
 
             String value = table.getToggleSelectionParameter(row, -1);
             if (table.getSelectionMode() != SListSelectionModel.NO_SELECTION) {
                 Utils.printClickability(device, table, value, true, table.getShowAsFormComponent());
                 device.print(" class=\"clickable ");
                 device.print(columnStyle);
                 device.print("\"");
             }
             else {
                 device.print(" class=\"");
                 device.print(columnStyle);
                 device.print("\"");
             }
             device.print(">");
 
             renderSelectionColumnContent(device, row, table, rendererPane);
 
             device.print("</td>");
         }
     }
 
     /**
      * Renders the <b>content</b> of the row selection row.
      */
     private void renderSelectionColumnContent(final Device device, int row, final STable table, final SCellRendererPane rendererPane)
         throws IOException
     {
         final STableCellRenderer rowSelectionRenderer = table.getRowSelectionRenderer();
         // use row selection renderer component
         final SComponent comp = rowSelectionRenderer.getTableCellRendererComponent(table,
                                                                                    table.getToggleSelectionParameter(row, -1),
                                                                                    table.isRowSelected(row),
                                                                                    row, -1);
         rendererPane.writeComponent(device, comp, table);
     }
     
     private boolean isSelectionColumnVisible(STable table) {
         if (table.getRowSelectionRenderer() != null && table.getSelectionModel().getSelectionMode() != SListSelectionModel.NO_SELECTION)
             return true;
         return false;
     }
 }
