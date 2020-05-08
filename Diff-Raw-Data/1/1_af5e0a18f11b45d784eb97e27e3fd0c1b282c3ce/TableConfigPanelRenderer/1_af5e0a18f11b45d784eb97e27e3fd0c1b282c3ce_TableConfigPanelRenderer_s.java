 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.tableconfigpanel;
 
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.component.datatable.DataTableConstants;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.List;
 
 public class TableConfigPanelRenderer extends CoreRenderer {
     @Override
     public void decode(FacesContext context, UIComponent component) {
         decodeBehaviors(context, component);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         TableConfigPanel panel = (TableConfigPanel)component;
 
         encodePopup(context, panel);
         super.encodeEnd(context, component);
     }
     private void encodePopup(FacesContext context, TableConfigPanel component) throws IOException {
         DataTable table = (DataTable)component.getTargetedDatatable();
         String tableId = table.getClientId(context);
         String clientId = component.getClientId(context);
         String jsId = this.resolveWidgetVar(component);
         ResponseWriter writer = context.getResponseWriter();
         List<Column> columns = table.getColumns();
         int i;
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, clientId, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-tableconf ui-widget", null);
         writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", null);
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-tableconf-header ui-widget-header ui-corner-tr ui-corner-tl", null);
         writer.writeText("Column Settings", null);
 
         writeConfigPanelOkButton(writer, clientId);
         writeConfigPanelCloseButton(writer, clientId, jsId);
 
         writer.endElement(HTML.DIV_ELEM);
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR,  "ui-tableconf-body ui-widget-content ui-corner-br ui-corner-bl", null);
 
         writer.startElement(HTML.TABLE_ELEM, null);
 
         writeHeaderRow(writer, component);
 
         writer.startElement(HTML.TBODY_ELEM, null);
 
         writeColumnConfigRows(writer, component, clientId, columns);
 
         writer.endElement(HTML.TBODY_ELEM);
         writer.endElement(HTML.TABLE_ELEM);
 
         writer.endElement(HTML.DIV_ELEM);
 
         writeJavascript(writer, clientId, tableId, component);
 
         writer.endElement(HTML.DIV_ELEM);
 
         if (component.isModal()) {
             writer.startElement(HTML.DIV_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR,  "ui-tableconf-modal", null);
             writer.endElement(HTML.DIV_ELEM);
         }
     }
 
     private void writeColumnConfigRows(ResponseWriter writer, TableConfigPanel component, String tableId, List<Column> columns) throws IOException {
         int i;
         boolean ordering = component.isColumnOrderingConfigurable();
         boolean naming = component.isColumnNameConfigurable();
         boolean sizing = false; //component.isColumnSizingConfigurable();
         boolean visibility = component.isColumnVisibilityConfigurable();
         boolean sorting = component.isColumnSortingConfigurable();
         boolean firstCol = component.getType().equals("first-col") ;
         boolean lastCol = component.getType().equals("last-col");
         List<Integer> columnOrdering = component.getTargetedDatatable().getColumnOrdering();
 
         for (i = 0; i < columns.size(); i++) {
             Column column = columns.get(i);
 
             String rowClass = "ui-tableconf-row-"+columnOrdering.get(i);
             if (!column.isConfigurable()) rowClass += " ui-disabled ui-opacity-40";
 
             writer.startElement(HTML.TR_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR,  rowClass, null);
             if (!column.isConfigurable() && component.isHideDisabledRows())
                 writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", null);
 
             boolean disableVisibilityControl = (firstCol && i == 0) || ((lastCol && i == columns.size() - 1));
 
             if (ordering) writeColumnOrderingControl(writer, column, i, tableId);
             writeColumnNameControl(writer, column, i, tableId, naming);
             if (sizing) writeColumnSizingControl(writer, column, i, tableId);
             if (visibility) writeColumnVisibilityControl(writer, column, i, tableId, disableVisibilityControl);
             if (sorting) writeSortControl(writer, column);
 
             writer.endElement(HTML.TR_ELEM);
         }
     }
 
     private void writeHeaderRow(ResponseWriter writer, TableConfigPanel component) throws IOException {
         writer.startElement(HTML.THEAD_ELEM, null);
         writer.startElement(HTML.TR_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-state-default", null);
         writer.writeAttribute(HTML.STYLE_ATTR, "border:0;", null);
 
         if (component.isColumnOrderingConfigurable()) {
             writer.startElement(HTML.TH_ELEM, null);
             writer.writeText("Ordering", null);
             writer.endElement(HTML.TH_ELEM);
         }
 
         writer.startElement(HTML.TH_ELEM, null);
         writer.writeText("Name", null);
         writer.endElement(HTML.TH_ELEM);
 
         //if (component.isColumnSizingConfigurable()) {
         //    writer.startElement(HTML.TH_ELEM, null);
         //    writer.writeText("Sizing", null);
         //    writer.endElement(HTML.TH_ELEM);
         //}
 
         if (component.isColumnVisibilityConfigurable()) {
             writer.startElement(HTML.TH_ELEM, null);
             writer.writeText("Visibility", null);
             writer.endElement(HTML.TH_ELEM);
         }
         if (component.isColumnSortingConfigurable()) {
             writer.startElement(HTML.TH_ELEM, null);
             writer.writeText("Sorting", null);
             writer.endElement(HTML.TH_ELEM);
         }
 
         writer.endElement(HTML.TR_ELEM);
         writer.endElement(HTML.THEAD_ELEM);
     }
 
     private void writeConfigPanelOkButton(ResponseWriter writer, String clientId) throws IOException {
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.STYLE_ATTR, "float:right;", null);
 
         writer.startElement(HTML.ANCHOR_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-state-default ui-corner-all ui-tableconf-head-button", null);
         writer.writeAttribute(HTML.HREF_ATTR, "#", null);
         writer.writeAttribute(HTML.ID_ATTR, clientId +"_tableconf_ok", null);
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-icon ui-icon-check", null);
 
         writer.writeText("table", null);
 
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.ANCHOR_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private void writeConfigPanelCloseButton(ResponseWriter writer, String clientId, String jsId) throws IOException {
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.STYLE_ATTR, "float:right;", null);
 
         writer.startElement(HTML.ANCHOR_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-state-default ui-corner-all ui-tableconf-head-button", null);
         writer.writeAttribute(HTML.HREF_ATTR, "#", null);
         writer.writeAttribute(HTML.ID_ATTR, clientId +"_tableconf_close", null);
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-icon ui-icon-close", null);
 
         writer.writeText("table", null);
 
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.ANCHOR_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private void writeJavascript(ResponseWriter writer, String clientId, String tableId, TableConfigPanel component) throws IOException {
         String jsId = this.resolveWidgetVar(component);
         boolean isSortable = component.isColumnSortingConfigurable();
         boolean isReorderable = component.isColumnOrderingConfigurable();
         boolean isSingleSort = ((DataTable)component.getTargetedDatatable()).isSingleSort();
         String handle = component.getDragHandle();
         Integer left = component.getOffsetLeft();
         Integer top = component.getOffsetTop();
 
         writer.startElement(HTML.SCRIPT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
 
         JSONBuilder json = new JSONBuilder()
                 .initialiseVar(jsId).beginFunction("ice.ace.create")
                 .item("TableConf").beginArray().item(clientId)
                 .beginMap();
 
         if (handle != null && handle.length() > 0)
             json.entry("handle", handle);
 
         if (isReorderable)
             json.entry("reorderable", true);
 
         if (isSortable)
             json.entry("sortable", isSortable);
 
         if (isSingleSort)
             json.entry("singleSort", isSingleSort);
 
         if (left != null)
             json.entry("left", left);
 
         if (top != null)
             json.entry("top", top);
 
         json.entry("tableId", tableId);
 
         encodeClientBehaviors(FacesContext.getCurrentInstance(), component, json);
 
         json.endMap().endArray().endFunction();
 
         writer.write(json.toString());
 
         writer.endElement(HTML.SCRIPT_ELEM);
     }
 
     private void writeSortControl(ResponseWriter writer, Column column) throws IOException {
         writer.startElement(HTML.TD_ELEM, null);
 
         if (column.getValueExpression("sortBy") != null) {
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, "ui-tableconf-sort-cont", null);
 
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_CONTROL_CLASS, null);
 
             // Write carats
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_CONTAINER, null);
 
             writer.startElement(HTML.ANCHOR_ELEM, null);
             if (column.getSortPriority() != null && column.isSortAscending())
                 writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_UP_CLASS + " ui-toggled", null);
             else writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_UP_CLASS, null);
             writer.writeAttribute(HTML.TABINDEX_ATTR, 0, null);
             writer.endElement(HTML.ANCHOR_ELEM);
 
             writer.startElement(HTML.ANCHOR_ELEM, null);
             if (column.getSortPriority() != null && !column.isSortAscending())
                 writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_DOWN_CLASS + " ui-toggled", null);
             else writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_DOWN_CLASS, null);
             writer.writeAttribute(HTML.TABINDEX_ATTR, 0, null);
             writer.endElement(HTML.ANCHOR_ELEM);
 
             writer.endElement(HTML.SPAN_ELEM);
 
             // Write Sort Order Integer
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ORDER_CLASS, null);
             if (column.getSortPriority() != null) writer.writeText(column.getSortPriority(), null);
             else writer.write(HTML.NBSP_ENTITY);
             writer.endElement(HTML.SPAN_ELEM);
 
             writer.endElement(HTML.SPAN_ELEM);
             writer.endElement(HTML.SPAN_ELEM);
         }
         writer.endElement(HTML.TD_ELEM);
     }
     private void writeColumnOrderingControl(ResponseWriter writer, Column column, int i, String tableId) throws IOException {
         writer.startElement(HTML.TD_ELEM, null);
         writer.startElement(HTML.ANCHOR_ELEM, null);
 
         String style = "display:inline-block; padding:0 1px 0 0; margin:0px 10px; text-align:left;";
         String styleClass = "ui-state-default ui-corner-all ui-sortable-handle";
 
         if (!column.isConfigurable()) styleClass += " ui-disabled";
 
         writer.writeAttribute(HTML.STYLE_ELEM, style, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
         writer.writeAttribute(HTML.HREF_ATTR, "#", null);
        writer.writeAttribute(HTML.ID_ATTR, tableId +"_tableconf_close", null);
 
         writer.startElement(HTML.SPAN_ELEM, null);
 
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-icon ui-icon-arrow-2-n-s", null);
         writer.writeText("table", null);
 
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.ANCHOR_ELEM);
         writer.endElement(HTML.TD_ELEM);
     }
     private void writeColumnSizingControl(ResponseWriter writer, Column column, int i, String clientId) throws IOException {
         writer.writeText("DataTable Settings", null);
     }
     private void writeColumnVisibilityControl(ResponseWriter writer, Column column, int i, String clientId, boolean disable) throws IOException {
         writer.startElement(HTML.TD_ELEM, null);
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "checkbox", null);
         if (disable || !column.isConfigurable())
             writer.writeAttribute(HTML.DISABLED_ATTR, "disabled", null);
         writer.writeAttribute(HTML.NAME_ATTR, clientId+"_colvis_"+i, null);
         if (column.isRendered()) writer.writeAttribute(HTML.CHECKED_ATTR, "checked", null);
         writer.endElement(HTML.INPUT_ELEM);
         writer.endElement(HTML.TD_ELEM);
     }
     private void writeColumnNameControl(ResponseWriter writer, Column column, int i, String clientId, boolean naming) throws IOException {
         writer.startElement(HTML.TD_ELEM, null);
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "text", null);
         if (!naming || !column.isConfigurable())
             writer.writeAttribute(HTML.DISABLED_ATTR, "disabled", null);
         writer.writeAttribute(HTML.NAME_ATTR, clientId+"_head_"+i, null);
         writer.writeAttribute(HTML.VALUE_ATTR, column.getHeaderText(), null);
         writer.endElement(HTML.INPUT_ELEM);
         writer.endElement(HTML.TD_ELEM);
     }
 
 
 }
