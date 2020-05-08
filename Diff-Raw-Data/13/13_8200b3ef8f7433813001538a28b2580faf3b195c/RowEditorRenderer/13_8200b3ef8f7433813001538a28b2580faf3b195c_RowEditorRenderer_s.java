 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
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
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.roweditor;
 
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.component.datatable.DataTableConstants;
 import org.icefaces.ace.event.RowEditCancelEvent;
 import org.icefaces.ace.event.RowEditEvent;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 @MandatoryResourceComponent(tagName="rowEditor", value="org.icefaces.ace.component.roweditor.RowEditor")
 public class RowEditorRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         Map<String,String> params = context.getExternalContext().getRequestParameterMap();
         RowEditor editor = (RowEditor) component;
         String clientId = editor.getClientId(context);
         
         //Decode row edit request triggered by this editor
         if(params.containsKey(clientId)) {
             DataTable table = findParentTable(context, editor);
             RowState state = (RowState)(context.getExternalContext().getRequestMap().get(table.getRowStateVar()));
             String tableId = table.getClientId(context);
             tableId = tableId.substring(0, tableId.lastIndexOf(NamingContainer.SEPARATOR_CHAR));
             List<String> activeCellEditors = state.getActiveCellEditorIds();
 
             if (params.containsKey(tableId + "_editSubmit")) {
                 component.queueEvent(new RowEditEvent(component, table.getRowData()));
                 for (Column c : table.getColumns()) {
                    activeCellEditors.remove(c.getCellEditor().getId());
                 }
             }
             else if (params.containsKey(tableId + "_editCancel")) {
                 component.queueEvent(new RowEditCancelEvent(component, table.getRowData()));
                 for (Column c : table.getColumns()) {
                    activeCellEditors.remove(c.getCellEditor().getId());
                 }
             }
             else if (params.containsKey(tableId + "_editShow")) {
                 //component.queueEvent(new RowEditCancelEvent(component, table.getRowData()));
                for (Column c : table.getColumns()) {
                   activeCellEditors.add(c.getCellEditor().getId());
                }
             }               
         }
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         RowEditor editor = (RowEditor) component;
         DataTable table = findParentTable(context, editor);
         RowState state = (RowState)(context.getExternalContext().getRequestMap().get(table.getRowStateVar()));
         boolean hasActiveEditors = state.getActiveCellEditorIds().size() > 0;
 
         if (state.isEditable()) {
             ResponseWriter writer = context.getResponseWriter();
             writer.startElement(HTML.DIV_ELEM, null);
             writer.writeAttribute("id", component.getClientId(context), null);
             if (hasActiveEditors) writer.writeAttribute("class", DataTableConstants.ROW_EDITOR_CLASS + " ui-state-highlight", null);
             else writer.writeAttribute("class", DataTableConstants.ROW_EDITOR_CLASS, null);
 
             if (!hasActiveEditors) {
                 writer.startElement("a", null);
                 writer.writeAttribute("class", "ui-icon ui-icon-pencil", null);
                 writer.writeAttribute("tabindex", "0", null);
                 writer.endElement("a");
             }
 
             if (hasActiveEditors) {
                 writer.startElement("a", null);
                 writer.writeAttribute("class", "ui-icon ui-icon-check", null);
                 writer.writeAttribute("tabindex", "0", null);
                 writer.endElement("a");
 
                 writer.startElement("a", null);
                 writer.writeAttribute("class", "ui-icon ui-icon-close", null);
                 writer.writeAttribute("tabindex", "0", null);
                 writer.endElement("a");
             }
     
             writer.endElement(HTML.DIV_ELEM);
         }
     }
 
     protected DataTable findParentTable(FacesContext context, RowEditor editor) {
 		UIComponent parent = editor.getParent();
 
 		while(parent != null)
             if (parent instanceof DataTable) return (DataTable) parent;
             else parent = parent.getParent();
 
 		return null;
 	}
 }
