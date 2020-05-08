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
 package org.primefaces.mobile.component.datagrid;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.TreeMap;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.DataRenderer;
 
 public class DataGridRenderer extends DataRenderer {
 
     public static final String DATAGRID_CLASS_MOBILE = "ui-datagrid ui-widget pm-layout-full-height";
     public static final String CONTENT_CLASS_MOBILE = "ui-datagrid-content ui-widget-content pm-scroller";
     public static final String CONTEXT_MENU_ACTIONS_KEY = "CONTEXT_MENU_ACTIONS";
     public static final String CONTEXT_MENU_ITEM_INDEX_KEY = "CONTEXT_MENU_ITEM_INDEX";
     public static final String HEADER_CLASS = "ui-datagrid-header ui-widget-header ui-corner-top";
     
     private Map<String, String> contextActionMap;
     private String contextMenuID;
     private String itemMenuID;
     
     public DataGridRenderer() {
         this.contextMenuID = null;
         this.itemMenuID = null;
         this.contextActionMap = new TreeMap<String, String>();
     }
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         DataGrid grid = (DataGrid) component;
         
         encodeMarkup(context, grid);
         encodeScript(context, grid);        
     }
 
     protected void encodeMarkup(FacesContext context, DataGrid grid) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = grid.getClientId();
         String styleClass = 
                 grid.getStyleClass() == null ? 
                     DataGridRenderer.DATAGRID_CLASS_MOBILE : 
                     DataGridRenderer.DATAGRID_CLASS_MOBILE + " " + grid.getStyleClass();
         
         if (grid.getFacet("defaultmenu") != null) {
             contextMenuID = clientId + "_menu";
             renderContextMenu(context, 
                     writer, 
                     grid, 
                     grid.getFacet("defaultmenu"), 
                     contextMenuID, 
                     "window");
         }
         
         if (grid.getFacet("contextmenu") != null) {
             itemMenuID = clientId + "_item_menu";
             renderContextMenu(context, 
                     writer, 
                     grid, 
                     grid.getFacet("contextmenu"), 
                     itemMenuID, 
                     "origin");
         }
         
         StringBuilder styleStrBuilder = new StringBuilder();
         if (grid.getWidth() != null) {
             styleStrBuilder.append("width: ").append(grid.getWidth()).append(";");
         }
         if (grid.getHeight() != null) {
             styleStrBuilder.append("height: ").append(grid.getHeight()).append(";");
         }
         
         writer.startElement("div", grid);
         writer.writeAttribute("id", clientId, "id");
         writer.writeAttribute("class", styleClass, "styleClass");
         if (styleStrBuilder.length() > 0) {
             writer.writeAttribute("style", styleStrBuilder.toString(), null);
         }
         
         // The body of the grid is rendered 100% on the client side.
         
         writer.endElement("div");
     }
 
     protected void encodeScript(FacesContext context, DataGrid grid) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = grid.getClientId();
 
         startScript(writer, clientId);
 
        writer.write("\n(function() {");
         
         // Define the data list and condition, handling the case where a data widget
         // is specified and that widget may be undefined.
         if (grid.getDataWidget() != null) { 
             writer.write("var dataList;");
             writer.write("if (" + grid.getDataWidget() + ") {");
             writer.write("dataList=" + grid.getItemList() + ";");
             writer.write("}");
         } else {
             writer.write("var dataList = " + grid.getItemList() + ";");
         }
         
         if (grid.getCondition() != null) {
             writer.write("\nvar renderCondition;");
             if (grid.getDataWidget() != null) { 
                 writer.write("if (" + grid.getDataWidget() + ") {");
                 writer.write("renderCondition=" + grid.getCondition() + ";");
                 writer.write("}");
             }
         }
         
         // Setup the widget.
        writer.write("\nPrimeFaces.cw('MobileDataGrid','" + grid.resolveWidgetVar() + "',{");
        writer.write("id:'" + clientId + "',");
         if (grid.getPaginatorTemplate() != null) {
             writer.write("paginatorTemplate: '" + grid.getPaginatorTemplate() + "',");
         }
         if (grid.getCondition() != null) {
             writer.write("condition: renderCondition,");
         }
         if (this.contextMenuID != null) {
             writer.write("defaultContextMenu: '" + this.contextMenuID + "',");
         }
         if (this.itemMenuID != null) {
             writer.write("itemContextMenu: '" + this.itemMenuID + "',");
         }
         if (grid.getEmptyMessage() != null) {
             writer.write("emptyMessage: '" + grid.getEmptyMessage() + "',");
         }
         if (grid.getStrings() != null) {
             writer.write("strings: '" + grid.getStrings() + "',");
         }
         writer.write("itemList: dataList,");
         writer.write("renderer: " + grid.getRenderer() + ",");
         writer.write("rows: '" + grid.getRows() + "',");
         writer.write("cols: '" + grid.getCols() + "'");
        writer.write("});");
 
        writer.write("})();\n");
         
         endScript(writer);
     }
 
     protected void renderContextMenu(FacesContext context, 
             ResponseWriter writer, 
             DataGrid grid, 
             UIComponent contextMenu,
             String menuID,
             String positionTo) throws IOException {
         writer.startElement("div", null);
         writer.writeAttribute("data-role", "popup", null);
         writer.writeAttribute("id", menuID, null);
         writer.writeAttribute("data-theme", "a", null);
         if (positionTo != null) {
             writer.writeAttribute("data-position-to", positionTo, null);
         }
             writer.startElement("ul", null);
             writer.writeAttribute("data-role", "listview", null);
             writer.writeAttribute("data-inset", "true", null);
             writer.writeAttribute("data-theme", "b", null);
             renderChildren(context, contextMenu);
             writer.endElement("ul");
         writer.endElement("div");
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
