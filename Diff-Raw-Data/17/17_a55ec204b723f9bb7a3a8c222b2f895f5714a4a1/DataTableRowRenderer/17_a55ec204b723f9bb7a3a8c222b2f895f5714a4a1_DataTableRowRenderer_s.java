 package org.icefaces.ace.component.datatable;
 
 import org.icefaces.ace.component.celleditor.CellEditor;
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.panelexpansion.PanelExpansion;
 import org.icefaces.ace.component.row.Row;
 import org.icefaces.ace.component.rowexpansion.RowExpansion;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.model.table.TreeDataModel;
 import org.icefaces.ace.util.HTML;
 
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Copyright 2010-2011 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils
  * Date: 12-06-01
  * Time: 2:41 PM
  */
 public class DataTableRowRenderer {
     protected static boolean encodeRow(FacesContext context, DataTable table, List<Column> columns, Map<Object, List<String>> rowToSelectedFieldsMap, String clientId, int rowIndex, String parentIndex, String rowIndexVar, boolean topVisibleRowRendered) throws IOException {
         table.setRowIndex(rowIndex);
         if (!table.isRowAvailable()) return false;
         if (rowIndexVar != null) context.getExternalContext().getRequestMap().put(rowIndexVar, rowIndex);
 
         RowState rowState = table.getStateMap().get(table.getRowData());
 
         boolean selected = rowState.isSelected();
         boolean unselectable = !rowState.isSelectable();
         boolean expanded = rowState.isExpanded();
         boolean visible = rowState.isVisible();
 
         List<String> selectedCellExpressions = (rowToSelectedFieldsMap != null)
                 ? (List<String>)(rowToSelectedFieldsMap.get(table.getRowData()))
                 : null;
 
         context.getExternalContext().getRequestMap().put(table.getRowStateVar(), rowState);
 
         if (visible) {
             ResponseWriter writer = context.getResponseWriter();
 
             // Add leading conditional row for this row object if required
             List<Row> leadingRows = table.getConditionalRows(rowIndex, true);
             for (Row r : leadingRows) encodeConditionalRow(context, r);
 
             String userRowStyleClass = table.getRowStyleClass();
             String expandedClass = expanded ? DataTableConstants.EXPANDED_ROW_CLASS : "";
             String unselectableClass = unselectable ? DataTableConstants.UNSELECTABLE_ROW_CLASS : "";
             String rowStyleClass = rowIndex % 2 == 0 ? DataTableConstants.ROW_CLASS + " " + DataTableConstants.EVEN_ROW_CLASS : DataTableConstants.ROW_CLASS + " " + DataTableConstants.ODD_ROW_CLASS;
 
             if (selected && table.getSelectionMode() != null) rowStyleClass = rowStyleClass + " ui-selected ui-state-active";
             if (userRowStyleClass != null) rowStyleClass = rowStyleClass + " " + userRowStyleClass;
 
             writer.startElement(HTML.TR_ELEM, null);
             parentIndex = (parentIndex != null) ? parentIndex + "." : "";
             writer.writeAttribute(HTML.ID_ATTR, clientId + "_row_" + parentIndex + rowIndex, null);
             writer.writeAttribute(HTML.CLASS_ATTR, rowStyleClass + " " + expandedClass + " " + unselectableClass, null);
             writer.writeAttribute(HTML.TABINDEX_ATTR, "0", null);
 
             boolean innerTdDivRequired = ((table.isScrollable() || table.isResizableColumns()) & !topVisibleRowRendered);
 
             for (Column kid : columns) {
                 if (kid.isRendered()) {
                     boolean cellSelected = false;
                     if (selectedCellExpressions != null) {
                         ValueExpression ve = kid.getValueExpression("selectBy") != null ? kid.getValueExpression("selectBy") : kid.getValueExpression("value");
                         if (ve != null)
                             cellSelected = selectedCellExpressions.contains(ve.getExpressionString());
                     }
 
                     encodeRegularCell(context, columns, kid, cellSelected, innerTdDivRequired);
                 }
             }
 
             if (rowIndexVar != null) context.getExternalContext().getRequestMap().put(rowIndexVar, rowIndex);
             writer.endElement(HTML.TR_ELEM);
 
             if (expanded) {
                 context.getExternalContext().getRequestMap().put(clientId + "_expandedRowId", ""+rowIndex);
                 boolean isPanel = table.getPanelExpansion() != null;
                 boolean isRow = table.getRowExpansion() != null;
 
                 // Ensure that table.getTableId returns correctly for request map look
                 table.setRowIndex(-1);
 
                 if (isPanel && isRow) {
                     if (rowState.getExpansionType() == RowState.ExpansionType.ROW) {
                         encodeRowExpansion(context, table, columns, writer);
                     }
                     else if (rowState.getExpansionType() == RowState.ExpansionType.PANEL) {
                         encodeRowPanelExpansion(context, table);
                     }
                 } else if (isPanel) {
                     encodeRowPanelExpansion(context, table);
                 } else if (isRow) {
                     encodeRowExpansion(context, table, columns, writer);
                 }
 
                 // Row index will have come back different from row expansion.
                 table.setRowIndex(rowIndex);
             }
 
             // Add tailing conditional row for this row object if required
             List<Row> tailingRows = table.getConditionalRows(rowIndex, false);
             for (Row r : tailingRows) encodeConditionalRow(context, r);
 
             return innerTdDivRequired;
         }
 
         return false;
     }
 
     private static void encodeConditionalRow(FacesContext context, Row r) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement(HTML.TR_ELEM, null);
        if (r.getStyle() != null) writer.writeAttribute(HTML.STYLE_ATTR, r.getStyle(), null);
        if (r.getStyleClass() != null) writer.writeAttribute(HTML.CLASS_ATTR, "dt-cond-row " + r.getStyleClass(), null);
 
         List<UIComponent> children = r.getChildren();
         List<Column> rowColumns = new ArrayList<Column>(children.size());
         for (UIComponent kid : children)
             if (kid instanceof Column)
                 rowColumns.add((Column)kid);
 
         for (Column kid : rowColumns)
             if (kid.isRendered())
                 encodeConditionalRowCell(context, kid);
 
         writer.endElement(HTML.TR_ELEM);
     }
 
     private static void encodeConditionalRowCell(FacesContext context, Column c) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement(HTML.TD_ELEM, null);
         writer.writeAttribute(HTML.COLSPAN_ATTR, c.getColspan(), null);
         if (c.getStyle() != null) writer.writeAttribute(HTML.STYLE_ATTR, c.getStyle(), null);
         if (c.getStyleClass() != null) writer.writeAttribute(HTML.CLASS_ATTR, c.getStyleClass(), null);
 
         c.encodeAll(context);
         writer.endElement(HTML.TD_ELEM);
     }
 
     private static void encodeRegularCell(FacesContext context, List columnSiblings, Column column, boolean selected, boolean resizable) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         Column nextColumn = DataTableRendererUtil.getNextColumn(column, columnSiblings);
         boolean isCurrStacked = DataTableRendererUtil.isCurrColumnStacked(columnSiblings, column);
         boolean isNextStacked = (nextColumn == null) ? false
                 : (nextColumn.isRendered() && nextColumn.isStacked());
         boolean isCurrGrouped = column.getCurrGroupLength() > 0;
         boolean isNextGrouped = isCurrGrouped ? false // No need to calculate next group if grouped
                 : column.isNextColumnGrouped();
 
         if (isCurrGrouped) {
             column.setCurrGroupLength(column.getCurrGroupLength()-1);
         } else {
             if (!isCurrStacked) {
                 writer.startElement(HTML.TD_ELEM, null);
 
                 if (column.getStyle() != null) writer.writeAttribute(HTML.STYLE_ELEM, column.getStyle(), null);
 
                 if (isNextGrouped) writer.writeAttribute(HTML.ROWSPAN_ATTR, column.findCurrGroupLength()+1, null);
 
                 CellEditor editor = column.getCellEditor();
 
                 String columnStyleClass = column.getStyleClass();
                 if (editor != null) {
                     columnStyleClass = columnStyleClass == null ? DataTableConstants.EDITABLE_COLUMN_CLASS : DataTableConstants.EDITABLE_COLUMN_CLASS + " " + columnStyleClass;
                 }
                 // Add alternating styling, except when last group is the same value, split by an expansion or conditional row
                 if (column.getValueExpression("groupBy") != null) {
                     if (column.isLastGroupDifferent()) column.setOddGroup(!column.isOddGroup());
                     if (columnStyleClass == null) columnStyleClass = "";
                     columnStyleClass += column.isOddGroup() ? " ui-datatable-group-odd" : " ui-datatable-group-even";
                 }
                 if (selected) columnStyleClass = columnStyleClass == null ? "ui-state-active ui-selected" : columnStyleClass + " ui-state-active ui-selected";
                 if (columnStyleClass != null) writer.writeAttribute(HTML.CLASS_ATTR, columnStyleClass, null);
 
                 if (resizable) writer.startElement(HTML.DIV_ELEM, null);
             }
             else {
                 writer.startElement("hr", null);
                 writer.endElement("hr");
             }
 
             column.encodeAll(context);
 
             if (!isNextStacked) {
                 if (resizable) writer.endElement(HTML.DIV_ELEM);
                 writer.endElement(HTML.TD_ELEM);
             }
         }
     }
 
     private static void encodeRowExpansion(FacesContext context, DataTable table, List<Column> columns, ResponseWriter writer) throws IOException {
         String rowVar = table.getVar();
         String rowIndexVar = table.getRowIndexVar();
         String clientId = table.getClientId(context);
 
         String expandedRowId = context.getExternalContext().getRequestParameterMap().get(clientId + "_expandedRowId");
         if (expandedRowId == null) {
             expandedRowId = (String) context.getExternalContext().getRequestMap().get(clientId + "_expandedRowId");
         }
 
         Object model = table.getDataModel();
 
         if (!(table.hasTreeDataModel())) throw new FacesException("DataTable : \"" + clientId + "\" must be bound to an instance of TreeDataModel when using sub-row expansion.");
         TreeDataModel rootModel = (TreeDataModel)model;
         rootModel.setRootIndex(expandedRowId);
         table.getStateMap().get(rootModel.getRootData()).setExpanded(true);
         table.setRowIndex(0);
 
         if (rootModel.getRowCount() > 0)
             while (rootModel.getRowIndex() < rootModel.getRowCount()) {
 //            System.out.println("----------");
 //            System.out.println(rootModel.getRootIndex());
 //            System.out.println(rootModel.getRowIndex());
 //            System.out.println("----------");
 
                 if (rowVar != null) context.getExternalContext().getRequestMap().put(rowVar, rootModel.getRowData());
                 if (rowIndexVar != null) context.getExternalContext().getRequestMap().put(rowIndexVar, rootModel.getRowIndex());
 
                 RowState rowState = table.getStateMap().get(rootModel.getRowData());
                 boolean selected = rowState.isSelected();
                 boolean expanded = rowState.isExpanded();
                 boolean unselectable = !rowState.isSelectable();
                 boolean visible = rowState.isVisible();
                 Map<Object, List<String>> rowToSelectedFieldsMap = table.getRowToSelectedFieldsMap();
                 List<String> selectedCellExpressions = null;
                 if (rowToSelectedFieldsMap != null)
                     selectedCellExpressions = (List<String>)(rowToSelectedFieldsMap.get(table.getRowData()));
                 context.getExternalContext().getRequestMap().put(table.getRowStateVar(), rowState);
 
                 String expandedClass = expanded ? DataTableConstants.EXPANDED_ROW_CLASS : "";
                 String alternatingClass = (rootModel.getRowIndex() % 2 == 0) ? DataTableConstants.EVEN_ROW_CLASS : DataTableConstants.ODD_ROW_CLASS;
                 String selectionClass = (selected && table.getSelectionMode() != null) ? "ui-selected ui-state-active" : "";
                 String unselectableClass = unselectable ? DataTableConstants.UNSELECTABLE_ROW_CLASS : "";
 
                 if (visible) {
                     writer.startElement(HTML.TR_ELEM, null);
                     writer.writeAttribute(HTML.ID_ATTR, clientId + "_row_" + expandedRowId + "." + rootModel.getRowIndex(), null);
                     writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.ROW_CLASS + " " + alternatingClass + " " + selectionClass + " " + expandedClass + " " + unselectableClass, null);
 
                     for (Column kid : columns) {
                         if (kid.isRendered()) {
                             boolean cellSelected = false;
                             if (selectedCellExpressions != null) {
                                 ValueExpression ve = kid.getValueExpression("selectBy") != null ? kid.getValueExpression("selectBy") : kid.getValueExpression("value");
                                 if (ve != null)
                                     cellSelected = selectedCellExpressions.contains(ve.getExpressionString());
                             }
 
                             encodeRegularCell(context, columns, kid, cellSelected, false);
                         }
                     }
                     writer.endElement(HTML.TR_ELEM);
 
                     if (expanded) {
                         int rowIndex = rootModel.getRowIndex();
                         context.getExternalContext().getRequestMap().put(clientId + "_expandedRowId", expandedRowId+"."+rowIndex);
 
                         PanelExpansion panelExpansion = table.getPanelExpansion();
                         RowExpansion rowExpansion = table.getRowExpansion();
                         boolean isPanel = panelExpansion != null;
                         boolean isRow = rowExpansion != null;
 
                         // Ensure that table.getTableId returns correctly for request map look
                         table.setRowIndex(-1);
 
                         if (isPanel && isRow) {
                             if (rowState.getExpansionType() == RowState.ExpansionType.ROW) {
                                 encodeRowExpansion(context, table, columns, writer);
                             }
                             else if (rowState.getExpansionType() == RowState.ExpansionType.PANEL) {
                                 encodeRowPanelExpansion(context, table);
                             }
                         } else if (isPanel) {
                             encodeRowPanelExpansion(context, table);
                         } else if (isRow) {
                             encodeRowExpansion(context, table, columns, writer);
                         }
 
                         rootModel = (TreeDataModel) table.getDataModel();
                         rootModel.setRootIndex(expandedRowId);
                         table.setRowIndex(rowIndex); // Row index will have come back different from row expansion.
                         context.getExternalContext().getRequestMap().put(clientId + "_expandedRowId", expandedRowId);
                     }
                 }
 
                 table.setRowIndex(rootModel.getRowIndex() + 1);
                 if (rowIndexVar != null) context.getExternalContext().getRequestMap().remove(rowIndexVar);
                 if (rowVar != null) context.getExternalContext().getRequestMap().remove(rowVar);
             }
 
         rootModel.setRootIndex(null);
         table.setRowIndex(-1);
     }
 
     private static void encodeRowPanelExpansion(FacesContext context, DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Map<String,String> params = context.getExternalContext().getRequestParameterMap();
         String clientId = table.getClientId(context);
         Object model = table.getDataModel();
 
 
         String expandedRowId = params.get(clientId + "_expandedRowId");
         if (expandedRowId == null) {
             expandedRowId = (String) context.getExternalContext().getRequestMap().get(clientId + "_expandedRowId");
         }
 
         int sepIndex = expandedRowId.lastIndexOf('.');
         String rootIndex = null;
         if (sepIndex >= 0) {
             rootIndex = expandedRowId.substring(0,sepIndex);
             expandedRowId = expandedRowId.substring(sepIndex+1);
         }
 
         if (rootIndex != null) ((TreeDataModel)model).setRootIndex(rootIndex);
         table.setRowIndex(Integer.parseInt(expandedRowId));
 
         table.getStateMap().get(table.getRowData()).setExpanded(true);
 
         writer.startElement(HTML.TR_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.EXPANDED_ROW_CONTENT_CLASS + " ui-widget-content " + DataTableConstants.UNSELECTABLE_ROW_CLASS , null);
 
         writer.startElement(HTML.TD_ELEM, null);
 
         int enabledColumns = 0;
         for (Column c : table.getColumns()) if (c.isRendered() && !c.isStacked()) enabledColumns++;
 
         writer.writeAttribute(HTML.COLSPAN_ATTR, enabledColumns, null);
         table.getPanelExpansion().encodeAll(context);
 
         writer.endElement(HTML.TD_ELEM);
         writer.endElement(HTML.TR_ELEM);
         table.setRowIndex(-1);
     }
 }
