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
 import org.wings.style.Style;
 import org.wings.io.Device;
 import org.wings.io.StringBuilderDevice;
 import org.wings.plaf.CGManager;
 import org.wings.plaf.Update;
 import org.wings.session.SessionManager;
 import org.wings.table.*;
 
 import java.awt.*;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 // Please do NOT reformat this class!
 public class TableCG
     extends AbstractComponentCG
     implements org.wings.plaf.TableCG
 {
     private static final long serialVersionUID = 1L;
     protected String fixedTableBorderWidth;
     protected SIcon editIcon;
     protected String selectionColumnWidth = "30";
 
     int horizontalOversize = 4;
 
     /**
      * Initialize properties from config
      */
     public TableCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
         setFixedTableBorderWidth((String)manager.getObject("TableCG.fixedTableBorderWidth", String.class));
         setEditIcon(manager.getIcon("TableCG.editIcon"));
         selectionColumnWidth = (String)manager.getObject("TableCG.selectionColumnWidth", String.class);
     }
 
     public int getHorizontalOversize() {
         return horizontalOversize;
     }
 
     public void setHorizontalOversize(int horizontalOversize) {
         this.horizontalOversize = horizontalOversize;
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
 
         if (Utils.isMSIE(table))
             table.putClientProperty("horizontalOversize", new Integer(horizontalOversize));
     }
 
     public void uninstallCG(SComponent component) {
         super.uninstallCG(component);
         final STable table = (STable)component;
         table.setHeaderRenderer(null);
         table.setDefaultRenderer(null);
         table.setRowSelectionRenderer(null);
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
         if (table.isEditable() && editableCell)
             parameter = table.getEditParameter(row, col);
         else if (selectableCell)
             parameter = table.getToggleSelectionParameter(row, col) + ";shiftKey='+event.shiftKey+';ctrlKey='+event.ctrlKey+'";
 
         String style = component.getStyle(STable.SELECTOR_CELL);
         if (style == null)
             style = table.getStyle(STable.SELECTOR_CELL);
         if (style == null)
             style = "cell";
 
         if (parameter != null && (selectableCell || editableCell) && !isClickable) {
             printClickability(device, table, parameter, table.getShowAsFormComponent());
             device.print(isEditingCell ? " editing=\"true\"" : " editing=\"false\"");
             device.print(isEditingCell ? " class=\"" + style + "\"" : " class=\"" + style + " clickable\"");
         }
         else
             device.print(" class=\"" + style + "\"");
 
         Style inlineStyle = component.getDynamicStyle(STable.SELECTOR_CELL);
         if (inlineStyle == null)
             inlineStyle = table.getDynamicStyle(STable.SELECTOR_CELL);
         Utils.optAttribute(device, "style", Utils.inlineStyles(inlineStyle));
 
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
         final STable table = (STable)_c;
 
         device.print("<table");
         Utils.writeAllAttributes(device, table);
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
 
        if (endY >= startY) {
             device.print("<tbody>");
             writeBody(device, table, startX, endX, startY, endY, emptyIndex);
             device.print("</tbody>");
         }
         device.print("</table>");
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
 
     protected void writeHeader(Device device, STable table, int startX, int endX) throws IOException {
         if (!table.isHeaderVisible())
             return;
 
         final SCellRendererPane rendererPane = table.getCellRendererPane();
         STableColumnModel columnModel = table.getColumnModel();
 
         String headerAreaStyle = table.getStyle(STable.SELECTOR_HEADER);
         if (headerAreaStyle != null)
             device.print("<tr class=\"" + headerAreaStyle + "\"");
         else
             device.print("<tr class=\"header\"");
         StringBuilder headerAreaInline = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_HEADER));
         Utils.optAttribute(device, "style", headerAreaInline);
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
 
     protected void writeBody(Device device, STable table,
             int startX, int endX, int startY, int endY, int emptyIndex) throws IOException {
         final SListSelectionModel selectionModel = table.getSelectionModel();
 
         String selectedAreaStyle = table.getStyle(STable.SELECTOR_SELECTED);
         String evenAreaStyle = table.getStyle(STable.SELECTOR_EVEN_ROWS);
         String oddAreaStyle = table.getStyle(STable.SELECTOR_ODD_ROWS);
         StringBuilder selectedAreaInline = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_SELECTED));
         StringBuilder evenAreaInline = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_EVEN_ROWS));
         StringBuilder oddAreaInline = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_ODD_ROWS));
         final SCellRendererPane rendererPane = table.getCellRendererPane();
         STableColumnModel columnModel = table.getColumnModel();
 
         for (int r = startY; r < endY; ++r) {
             writeTableRow(device, table, columnModel, selectionModel, rendererPane, r,
                     startX, endX, emptyIndex, selectedAreaInline, oddAreaInline, evenAreaInline, selectedAreaStyle, oddAreaStyle, evenAreaStyle);
         }
     }
 
     protected void writeTableRow(
         Device device, STable table, STableColumnModel columnModel,
         SListSelectionModel selectionModel, SCellRendererPane rendererPane,
         final int rowIndex, int startX, int endX,
         int emptyIndex, StringBuilder selectedAreaInline,
         StringBuilder oddAreaInline, StringBuilder evenAreaInline,
         String selectedAreaStyle, String oddAreaStyle, String evenAreaStyle) throws IOException {
         if (rowIndex >= emptyIndex) {
             int colspan = endX - startX;
             device.print("<tr class=\"empty\">\n");
             if (isSelectionColumnVisible(table)) {
                 device.print("  <td></td>\n");
             }
             device.print("  <td class=\"empty\" colspan=\"" + colspan + "\">&nbsp;</td>\n");
             device.print("</tr>\n");
             return;
         }
 
         String rowStyle = table.getRowStyle(rowIndex);
         StringBuilder rowClass = new StringBuilder(rowStyle != null ? rowStyle + " " : "");
         device.print("<tr");
         if (selectionModel.isSelectedIndex(rowIndex)) {
             Utils.optAttribute(device, "style", selectedAreaInline);
             rowClass.append(selectedAreaStyle != null ? selectedAreaStyle + " " : "selected ");
         }
         else if (rowIndex % 2 != 0)
             Utils.optAttribute(device, "style", oddAreaInline);
         else
             Utils.optAttribute(device, "style", evenAreaInline);
 
         rowClass.append(rowIndex % 2 != 0
             ? oddAreaStyle != null ? oddAreaStyle + " " : "odd"
             : evenAreaStyle != null ? evenAreaStyle + " " : "even");
         Utils.optAttribute(device, "class", rowClass);
         device.print(">");
 
         writeSelectionBody(device, table, rendererPane, rowIndex);
 
         for (int c = startX; c < endX; ++c) {
             STableColumn column = columnModel.getColumn(c);
             if (!column.isHidden())
                 renderCellContent(device, table, rendererPane, rowIndex, c);
             else
                 ++endX;
         }
 
         device.print("</tr>");
         Utils.printNewline(device, table);
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
         if (width != null) {
             device.print(" width=\"");
             device.print(width);
             device.print("\"");
         }
         device.print("/>");
     }
 
     /**
      * Renders the row sometimes needed to allow row selection.
      */
     protected void writeSelectionBody(final Device device, final STable table, final SCellRendererPane rendererPane,
                                          final int row)
         throws IOException
     {
         if (isSelectionColumnVisible(table)) {
             final SComponent comp = getRowSelectionRenderer(table, row);
             final String columnStyle = Utils.joinStyles(comp, "num");
 
             device.print("<td valign=\"top\" align=\"right\"");
             Utils.optAttribute(device, "width", selectionColumnWidth);
 
             String value = table.getToggleSelectionParameter(row, -1) + ";shiftKey='+event.shiftKey+';ctrlKey='+event.ctrlKey+'";
             if (table.getSelectionMode() != SListSelectionModel.NO_SELECTION) {
                 printClickability(device, table, value, table.getShowAsFormComponent());
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
 
             // Renders the content of the row selection row
             rendererPane.writeComponent(device, comp, table);
 
             device.print("</td>");
         }
     }
 
     private SComponent getRowSelectionRenderer(final STable table, final int row) {
         final STableCellRenderer rowSelectionRenderer = table.getRowSelectionRenderer();
         final SComponent comp = rowSelectionRenderer.getTableCellRendererComponent(table,
                                                                                    table.getToggleSelectionParameter(row, -1) + ";shiftKey='+event.shiftKey+';ctrlKey='+event.ctrlKey+'",
                                                                                    table.isRowSelected(row),
                                                                                    row, -1);
         return comp;
     }
 
     public static void printClickability(final Device device, final SComponent component, final String eventValue,
                                          final boolean formComponent) throws IOException {
         device.print(" onclick=\"return wingS.table.cellClick(");
         device.print("event,this,");
         device.print(formComponent + ",");
         device.print(!component.isReloadForced() + ",'");
         device.print(Utils.event(component));
         device.print("','");
         device.print(eventValue == null ? "" : eventValue);
         device.print("'");
         device.print(");\"");
     }
 
     protected boolean isSelectionColumnVisible(STable table) {
         if (table.getRowSelectionRenderer() != null && table.getSelectionModel().getSelectionMode() != SListSelectionModel.NO_SELECTION)
             return true;
         return false;
     }
 
 
     public Update getTableScrollUpdate(STable table, Rectangle newViewport, Rectangle oldViewport) {
         //return new TableScrollUpdate(table);
         return new ComponentUpdate(this, table);
     }
 
     public Update getSelectionUpdate(STable table, List deselectedIndices, List selectedIndices) {
         return new SelectionUpdate(table, deselectedIndices, selectedIndices);
     }
 
     public Update getEditCellUpdate(STable table, int row, int column) {
         return new EditCellUpdate(table, row, column);
     }
 
     public Update getRenderCellUpdate(STable table, int row, int column) {
         return new RenderCellUpdate(table, row, column);
     }
 
     protected class TableScrollUpdate
         extends AbstractUpdate {
 
         public TableScrollUpdate(SComponent component) {
             super(component);
         }
 
         public Handler getHandler() {
             STable table = (STable) component;
 
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
 
             String htmlCode = "";
             String exception = null;
 
             try {
                 StringBuilderDevice htmlDevice = new StringBuilderDevice();
                 writeBody(htmlDevice, table, startX, endX, startY, endY, emptyIndex);
                 htmlCode = htmlDevice.toString();
             } catch (Throwable t) {
                 exception = t.getClass().getName();
             }
 
             UpdateHandler handler = new UpdateHandler("tableScroll");
             handler.addParameter(table.getName());
             handler.addParameter(htmlCode);
             if (exception != null) {
                 handler.addParameter(exception);
             }
             return handler;
         }
     }
 
     protected class SelectionUpdate extends AbstractUpdate<STable> {
         private List<Integer> deselectedIndices;
         private List<Integer> selectedIndices;
 
         public SelectionUpdate(STable component, List<Integer> deselectedIndices, List<Integer> selectedIndices) {
             super(component);
             this.deselectedIndices = deselectedIndices;
             this.selectedIndices = selectedIndices;
         }
 
         public Handler getHandler() {
             int indexOffset = 0;
             if (component.isHeaderVisible()) {
                 ++indexOffset;
             }
 
             UpdateHandler handler = new UpdateHandler("selectionTable");
             handler.addParameter(component.getName());
             handler.addParameter(new Integer(indexOffset));
             handler.addParameter(Utils.listToJsArray(deselectedIndices));
             handler.addParameter(Utils.listToJsArray(selectedIndices));
             if (isSelectionColumnVisible(component)) {
                 String exception = null;
                 List<String> deselectedBodies = new ArrayList<String>();
                 List<String> selectedBodies = new ArrayList<String>();
                 exception = writeSelectionBodies(deselectedIndices, deselectedBodies);
                 exception = writeSelectionBodies(selectedIndices, selectedBodies);
                 handler.addParameter(Utils.listToJsArray(deselectedBodies));
                 handler.addParameter(Utils.listToJsArray(selectedBodies));
                 if (exception != null) {
                     handler.addParameter(exception);
                 }
             }
             return handler;
         }
 
         private String writeSelectionBodies(List<Integer> indices, List<String> bodies) {
             try {
                 final StringBuilderDevice htmlDevice = new StringBuilderDevice(512);
                 final SCellRendererPane rendererPane = component.getCellRendererPane();
                 Rectangle currentViewport = component.getViewportSize();
                 int viewportOffset = 0;
                 if (currentViewport != null) {
                     viewportOffset += currentViewport.y;
                 }
                 for (Integer index : indices) {
                     SComponent rowSelectionRenderer = getRowSelectionRenderer(component, index + viewportOffset);
                     rendererPane.writeComponent(htmlDevice, rowSelectionRenderer, component);
                     bodies.add(htmlDevice.toString());
                     htmlDevice.reset();
                 }
             } catch (Throwable t) {
                 return t.getClass().getName();
             }
             return null;
         }
     }
 
     private class EditCellUpdate
         extends AbstractUpdate<STable>
     {
         private int row;
         private int column;
 
         public EditCellUpdate(STable table, int row, int column) {
             super(table);
             this.row = row;
             this.column = column;
         }
 
         public Handler getHandler() {
             STable table = this.component;
 
             String htmlCode = "";
             String exception = null;
 
             try {
                 StringBuilderDevice device = new StringBuilderDevice();
                 /*
                 Utils.printTableCellAlignment(device, component, SConstants.LEFT, SConstants.TOP);
                 Utils.optAttribute(device, "oversize", horizontalOversize);
                 device.print(" class=\"cell\">");
                 */
                 SComponent component = table.getEditorComponent();
                 table.getCellRendererPane().writeComponent(device, component, table);
                 htmlCode = device.toString();
             }
             catch (Throwable t) {
                 exception = t.getClass().getName();
             }
 
             row = table.isHeaderVisible() ? this.row + 1 : this.row;
             column = columnInView(table, column);
             column = isSelectionColumnVisible(table) ? column + 1 : column;
 
             Rectangle currentViewport = table.getViewportSize();
             if (currentViewport != null) {
                 row -= currentViewport.y;
                 column -= currentViewport.x;
             }
 
             UpdateHandler handler = new UpdateHandler("tableCell");
             handler.addParameter(table.getName());
             handler.addParameter(row);
             handler.addParameter(column);
             handler.addParameter(true);
             handler.addParameter(htmlCode);
             if (exception != null) {
                 handler.addParameter(exception);
             }
             return handler;
         }
     }
 
     private class RenderCellUpdate
         extends AbstractUpdate<STable>
     {
         private int row;
         private int column;
 
         public RenderCellUpdate(STable table, int row, int column) {
             super(table);
             this.row = row;
             this.column = column;
         }
 
         public Handler getHandler() {
             STable table = this.component;
 
             String htmlCode = "";
             String exception = null;
 
             try {
                 StringBuilderDevice device = new StringBuilderDevice(256);
                 /*
                 Utils.printTableCellAlignment(device, component, SConstants.LEFT, SConstants.TOP);
                 Utils.optAttribute(device, "oversize", horizontalOversize);
                 device.print(" class=\"cell\">");
                 */
                 SComponent component = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                 table.getCellRendererPane().writeComponent(device, component, table);
                 htmlCode = device.toString();
             }
             catch (Throwable t) {
                 exception = t.getClass().getName();
             }
 
             row = table.isHeaderVisible() ? this.row + 1 : this.row;
             column = columnInView(table, column);
             column = isSelectionColumnVisible(table) ? column + 1 : column;
 
             Rectangle currentViewport = table.getViewportSize();
             if (currentViewport != null) {
                 row -= currentViewport.y;
                 column -= currentViewport.x;
             }
 
             UpdateHandler handler = new UpdateHandler("tableCell");
             handler.addParameter(table.getName());
             handler.addParameter(row);
             handler.addParameter(column);
             handler.addParameter(false);
             handler.addParameter(htmlCode);
             if (exception != null) {
                 handler.addParameter(exception);
             }
             return handler;
         }
 
         public boolean equals(Object object) {
             if (!super.equals(object))
                 return false;
 
             RenderCellUpdate other = (RenderCellUpdate) object;
 
             if (this.row != other.row)
                 return false;
             if (this.column != other.column)
                 return false;
 
             return true;
         }
 
         public int hashCode() {
             int hashCode = super.hashCode();
             int dispersionFactor = 37;
 
             hashCode = hashCode * dispersionFactor + row;
             hashCode = hashCode * dispersionFactor + column;
 
             return hashCode;
         }
     }
 
     private int columnInView(STable table, int column) {
         int viewColumn = 0;
         for (int i=0; i < column; i++) {
             STableColumn tableColumn = table.getColumnModel().getColumn(i);
             if (!tableColumn.isHidden())
                 viewColumn++;
         }
         return viewColumn;
     }
 }
