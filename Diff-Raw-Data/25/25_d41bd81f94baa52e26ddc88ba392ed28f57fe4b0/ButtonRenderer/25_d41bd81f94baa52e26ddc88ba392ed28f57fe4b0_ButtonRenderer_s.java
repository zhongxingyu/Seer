 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.helix.mobile.component.button;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 /**
  *
  * @author shallem
  */
 public class ButtonRenderer extends CoreRenderer {
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Button button = (Button) component;
         
         writer.startElement("a", button);
         writer.writeAttribute("data-icon", button.getIcon(), null);
         writer.writeAttribute("href", button.getHref(), null);
         if (button.getTheme() != null) {
             writer.writeAttribute("data-theme", button.getTheme(), null);
         }
         
         String onclick = button.getOnclick();
         if (!isValueBlank(onclick)) {
             writer.writeAttribute("onclick", onclick, "onclick");
         }
 
         if (button.getValue() != null) {
             writer.write(button.getValue().toString());
         }
 
         writer.endElement("a");
     }
 }
