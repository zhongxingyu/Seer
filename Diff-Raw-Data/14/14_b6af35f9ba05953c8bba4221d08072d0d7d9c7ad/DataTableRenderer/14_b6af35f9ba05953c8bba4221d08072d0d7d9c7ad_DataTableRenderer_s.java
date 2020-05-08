 /*
  * Copyright 2009-2012 Prime Teknoloji.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.primefaces.mobile.component.datatable;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.mobile.component.column.Column;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class DataTableRenderer extends CoreRenderer {
 
     public static final String CONTAINER_CLASS = "ui-datatable ui-widget";
     public static final String COLUMN_HEADER_CLASS = "ui-state-default";
     public static final String COLUMN_CONTENT_WRAPPER = "ui-dt-c";
     public static final String COLUMN_FOOTER_CLASS = "ui-state-default";
     public static final String DATA_CLASS = "ui-datatable-data ui-widget-content";
     public static final String SORTABLE_COLUMN_ICON_CLASS = "ui-sortable-column-icon ui-icon ui-icon-carat-2-n-s";
     public static final String HEADER_CLASS = "ui-datatable-header ui-widget-header";
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         DataTable table = (DataTable) component;
         encodeMarkup(context, table);
         encodeScript(context, table);
     }
 
     protected void encodeScript(FacesContext context, 
             DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = table.getClientId(context);
 
         startScript(writer, clientId);
 
         writer.write("PrimeFaces.cw('DataTable','" + table.resolveWidgetVar() + "',{");
         writer.write("id:'" + clientId + "'");
 
         writer.write(",grouped: " + Boolean.toString(table.isGrouped()));
         
         // Getter for group name.
         if (table.getGroupName() != null) {
             writer.write(",groupName: " + table.getGroupName());
         }
         // Getter for group members.
         if (table.getGroupMembers() != null) {
             writer.write(",groupMembers: " + table.getGroupMembers());
         }
         
         // The row style class.
         if (table.getRowStyleClass() != null) {
             writer.write(",rowStyleClass: '" + table.getRowStyleClass() + "'");
         }
         
         // Rendered condition.
         if (table.getCondition() != null) {
             writer.write(",condition: function() {" + table.getCondition() + "}");
         }
         
         // Pagination
         if (table.getItemsPerPage() != null) {
             writer.write(",itemsPerPage: " + table.getItemsPerPage());
             if (table.getPaginatorTemplate() != null) {
                 writer.write(",paginatorTemplate: '" + table.getPaginatorTemplate() + "'");
             }
         }
 
         // Selection
         if (table.getSelectable().equals("true")) {
             writer.write(",selectable: true");
             
             if (table.getSelectAction() != null) {
                writer.append(",selectAction: function(row,col) {" + table.getSelectAction() + "}");
             }
         }
 
         // Default field to sort by.
         if (table.getSortBy() != null) {
             writer.write(",sortBy:'" + table.getSortBy() + "'");
         }
 
         // Sort order for the default sort.
         if (table.getSortOrder() != null) {
             writer.write(",sortOrder:'" + table.getSortOrder() + "'");
         }
         
         // The data list.
         writer.write(",itemList: " + table.getItemList());
         
         // The column renderer
         writer.write(",columnRenderer: " + table.getColumnRenderer());
         
         // The columns.
         StringBuilder colsString = new StringBuilder();
         colsString.append("[");
         for (Column column : getColumns(table)) {
             colsString.append("{");
             colsString.append("id: '").append(column.getClientId()).append("'");
             colsString.append(",name: '").append(column.getName()).append("'");
             if (column.getSortBy() != null) {
                 colsString.append(",sortby: '").append(column.getSortBy()).append("'");
             }
             if (column.getSortOrder() != null) {
                 colsString.append(",sortorder: '").append(column.getSortOrder()).append("'");
             }
             if (column.getStyle() != null) {
                 colsString.append(",style: '").append(column.getStyle()).append("'");
             }
             if (column.getStyleClass() != null) {
                 colsString.append(",styleClass: '").append(column.getStyleClass()).append("'");
             }
             if (column.getWidth() != null) {
                 colsString.append(",width: '").append(column.getWidth()).append("'");
             }
             
             colsString.append("}");
         }
         colsString.append("]");
         writer.write(",cols: " + colsString.toString());
         
         writer.write("});");
 
         endScript(writer);
     }
 
     protected void encodeMarkup(FacesContext context, DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = table.getClientId(context);
         String style = table.getStyle();
         
         //style class
         String containerClass = DataTableRenderer.CONTAINER_CLASS;
         containerClass = table.getStyleClass() != null ? containerClass + " " + table.getStyleClass() : containerClass;
 
         writer.startElement("div", table);
         writer.writeAttribute("id", clientId, "id");
         writer.writeAttribute("class", containerClass, "styleClass");
         if (style != null) {
             writer.writeAttribute("style", style, "style");
         }
 
         encodeRegularTable(context, table);
 
         writer.endElement("div");
     }
 
     protected void encodeRegularTable(FacesContext context, 
             DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement("table", null);
         writer.writeAttribute("role", "grid", null);
         if (table.getTableStyle() != null) {
             writer.writeAttribute("style", table.getTableStyle(), null);
         }
         if (table.getTableStyleClass() != null) {
             writer.writeAttribute("class", table.getTableStyleClass(), null);
         }
 
         encodeThead(context, table);
         // encodeTFoot(context, table);
         encodeTbody(context, table, false);
         writer.endElement("table");
     }
 
     protected void encodeColumnHeader(FacesContext context, 
             DataTable table, 
             Column column) throws IOException {
         if (!column.isRendered()) {
             return;
         }
 
         ResponseWriter writer = context.getResponseWriter();
         String clientId = column.getClientId();
         boolean isSortable = column.getSortBy() != null;
         String sortIcon = isSortable ? DataTableRenderer.SORTABLE_COLUMN_ICON_CLASS : null;
         
         String columnClass = DataTableRenderer.COLUMN_HEADER_CLASS;
         columnClass = column.getStyleClass() != null ? columnClass + " " + column.getStyleClass() : columnClass;
 
         writer.startElement("th", null);
         writer.writeAttribute("id", clientId, null);
         writer.writeAttribute("class", columnClass, null);
         writer.writeAttribute("role", "columnheader", null);
 
         if (column.getStyle() != null) {
             writer.writeAttribute("style", column.getStyle(), null);
         }
 
         //column content wrapper
         writer.startElement("div", null);
         writer.writeAttribute("class", DataTableRenderer.COLUMN_CONTENT_WRAPPER, null);
         if (column.getWidth() != null) {
             writer.writeAttribute("style", "width:" + column.getWidth(), null);
         }
         encodeColumnHeaderContent(context, column, sortIcon);
 
         writer.endElement("div");
 
         writer.endElement("th");
     }
 
     protected void encodeColumnHeaderContent(FacesContext context, 
             Column column, 
             String sortIcon) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         
         if (sortIcon != null) {
             writer.startElement("span", null);
             writer.writeAttribute("class", sortIcon, null);
             writer.endElement("span");
         }
 
         writer.startElement("span", null);
         renderChildren(context, column);
         writer.endElement("span");
     }
 
     /**
      * Render column headers either in single row or nested if a columnGroup is
      * defined
      */
     protected void encodeThead(FacesContext context, DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         
         writer.startElement("thead", null);
 
         UIComponent tableHeader = getHeader(table);
         if (tableHeader != null) {
             writer.startElement("th", null);
             writer.startElement("div", null);
             writer.writeAttribute("class", DataTableRenderer.HEADER_CLASS, null);
             tableHeader.encodeAll(context);
             writer.endElement("div");
             writer.endElement("th");
         }
         
         writer.startElement("tr", null);
         writer.writeAttribute("role", "row", null);
 
         for (Column column : getColumns(table)) {
             encodeColumnHeader(context, table, column);
         }
 
         writer.endElement("tr");
 
         writer.endElement("thead");
     }
     
     public void encodeTbody(FacesContext context, DataTable table, boolean dataOnly) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = table.getClientId(context);
 
         writer.startElement("tbody", null);
         writer.writeAttribute("id", clientId + "_data", null);
         writer.writeAttribute("class", DataTableRenderer.DATA_CLASS + " pm-scroller", null);
         
         writer.endElement("tbody");
     }
 
     private UIComponent getHeader(DataTable table) {
         return table.getFacet("header");
     }
     
     public List<Column> getColumns(DataTable table) {
         List<Column> columns = new ArrayList<>();
 
         for(UIComponent child : table.getChildren()) {
             if(child instanceof Column) {
                 columns.add((Column) child);
             }
         }
         
         return columns;
     }
     
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Rendering happens on encodeEnd
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
