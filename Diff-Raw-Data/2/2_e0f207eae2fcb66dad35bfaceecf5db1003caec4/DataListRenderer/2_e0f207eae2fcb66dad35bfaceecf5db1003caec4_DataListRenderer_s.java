 /*
  * Copyright 2009-2011 Prime Technology.
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
 package org.primefaces.mobile.component.datalist;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class DataListRenderer extends CoreRenderer {
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         DataList dataList = (DataList) component;
         UIComponent header = dataList.getFacet("header");
         String type = dataList.getType();
         Object filterValue = dataList.getAttributes().get("filter");
 
         writer.startElement("ul", dataList);
         writer.writeAttribute("id", dataList.getClientId(context), "id");
         writer.writeAttribute("data-role", "listview", null);
         
         if(filterValue != null && Boolean.valueOf(filterValue.toString())) {
             writer.writeAttribute("data-filter", "true", null);
         }
         if(type != null && type.equals("inset")) {
             writer.writeAttribute("data-inset", true, null);
         }
         if(dataList.getStyle() != null) {
             writer.writeAttribute("style", dataList.getStyle(), null);
         }
         if(dataList.getStyleClass() != null) {
             writer.writeAttribute("class", dataList.getStyleClass(), null);
         }
 
         if(header != null) {
             writer.startElement("li", null);
             writer.writeAttribute("data-role", "list-divider", null);
             header.encodeAll(context);
             writer.endElement("li");
         }
 
         // Leave the list empty because we fill it in on the client side.
         
         writer.endElement("ul");
         encodeScript(context, dataList);
     }
     
     protected void encodeScript(FacesContext context, 
             DataList dlist) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = dlist.getClientId(context);
 
         startScript(writer, clientId);
 
         writer.write("PrimeFaces.cw('DataList','" + dlist.resolveWidgetVar() + "',{");
         writer.write("id:'" + clientId + "'");
 
         writer.write(",grouped: " + Boolean.toString(dlist.isGrouped()));
         
         // Getter for group name.
         if (dlist.getGroupName() != null) {
             writer.write(",groupName: " + dlist.getGroupName());
         }
         // Getter for group members.
         if (dlist.getGroupMembers() != null) {
             writer.write(",groupMembers: " + dlist.getGroupMembers());
         }
         
         // The row style class.
         if (dlist.getRowStyleClass() != null) {
             writer.write(",rowStyleClass: '" + dlist.getRowStyleClass() + "'");
         }
         
         // Rendered condition.
         if (dlist.getCondition() != null) {
            writer.write(",condition: function() {" + dlist.getCondition() + "}");
         }
         
         // Pagination
         if (dlist.getItemsPerPage() != null) {
             writer.write(",itemsPerPage: " + dlist.getItemsPerPage());
             if (dlist.getPaginatorTemplate() != null) {
                 writer.write(",paginatorTemplate: '" + dlist.getPaginatorTemplate() + "'");
             }
         }
 
         // Selection
         if (dlist.getSelectable().equals("true")) {
             writer.write(",selectable: true");
             
             if (dlist.getSelectAction() != null) {
                 writer.append(",selectAction: function(row,rowIndex,strings) {" + dlist.getSelectAction() + "}");
             }
         }
 
         // Default field to sort by.
         if (dlist.getSortBy() != null) {
             writer.write(",sortBy:'" + dlist.getSortBy() + "'");
         }
 
         // Sort order for the default sort.
         if (dlist.getSortOrder() != null) {
             writer.write(",sortOrder:'" + dlist.getSortOrder() + "'");
         }
         
         // The data list.
         writer.write(",itemList: " + dlist.getItemList());
         
         // The row renderer
         writer.write(",rowRenderer: " + dlist.getRowRenderer());
         
         writer.write("});");
 
         endScript(writer);
     } 
     
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Do Nothing
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
