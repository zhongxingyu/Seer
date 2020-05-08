 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.helix.mobile.component.contextmenuitem;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 /**
  *
  * @author shallem
  */
 public class ContextMenuItemRenderer extends CoreRenderer {
     /*@Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ContextMenuItem item = (ContextMenuItem) component; 
         writer.startElement("li", item);
         writer.startElement("a", null);
         writer.writeAttribute("href", "javascript:void(0);", null);
         writer.writeAttribute("onclick", item.getOntap(), null);
         writer.write((String)item.getValue());
     }*/
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ContextMenuItem item = (ContextMenuItem) component; 
 
         /*writer.endElement("a");
         writer.endElement("li");*/
         writer.write("{");
         writer.write("'display' : '" + item.getValue() + "'");
         writer.write(",'action' : " + item.getOntap());
        writer.write(",'enabled' : " + item.getEnabled());
         if (item.getGroup() != null) {
             writer.write(",'group' : '" + item.getGroup() + "'");
         }
         writer.write("}");
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
