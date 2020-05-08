 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.primefaces.mobile.component.scrollingdiv;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 /**
  *
  * @author shallem
  */
 public class ScrollingDivRenderer extends CoreRenderer {
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ScrollingDiv sdiv = (ScrollingDiv) component;
         String id = sdiv.getClientId(context);
 
         String styleStr =
                 MessageFormat.format("width: {0};",
                     new Object[]{ sdiv.getWidth(), sdiv.getHeight() });
         
         String customStyleClass =
                 sdiv.getStyleClass();
         if (customStyleClass == null) {
             customStyleClass = "";
         }
         if (sdiv.getOrientation().equals("horizontal")) {
             customStyleClass = customStyleClass + " pm-scroller-horizontal";
         }
        if (!sdiv.isZoom()) {
            customStyleClass = customStyleClass + " pm-scroller-nozoom";
        }
         
         // Output a full-width div enclosing the child. This is the wrapper.
         writer.startElement("div", sdiv);
         writer.writeAttribute("id", id, "id");
         writer.writeAttribute("style", styleStr, null);
         writer.writeAttribute("class", "pm-scroller " + customStyleClass, null);
         
         // Also enclose the child in a div because we don't know what is in that child ...
         // we need everything in capsulated in 1 DOM element.
         writer.startElement("div", sdiv);
         writer.writeAttribute("id", id + "_inner", "id");
         writer.writeAttribute("style", "padding-bottom: 7px", null);
         for (UIComponent c : sdiv.getChildren()) {
             c.encodeAll(context);
         }
         writer.endElement("div");
         
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
