 package org.icemobile.renderkit;
 
import static org.icemobile.util.HTML.*;
 
 import java.io.IOException;
 
 import org.icemobile.component.IContactList;
import org.icemobile.util.CSSUtils;
 
 public class ContactListCoreRenderer {
     
     public void encode(IContactList component, IResponseWriter writer)
             throws IOException {
         
         String clientId = component.getClientId();
         
         writer.startElement(DIV_ELEM);
         writer.writeAttribute(ID_ATTR, clientId);
 
         writer.startElement(INPUT_ELEM);
         writer.writeAttribute(TYPE_ATTR, "button");
        writer.writeAttribute(CLASS_ATTR, CSSUtils.STYLECLASS_BUTTON);
 
         StringBuilder args = new StringBuilder();
 
         String pattern = component.getPattern();
         if (pattern != null && !"".equals(pattern)) {
             args.append("pattern=").append(pattern);
         }
 
         if (component.isMultipleSelect()) {
             if (args.length() > 0) {
                 args.append("&");
             }
             args.append("select=multiple");
         }
 
         String fields = component.getFields();
         if (fields != null && !"".equals(fields)) {
             if (args.length() > 0) {
                 args.append("&");
             }
             args.append("fields=").append(fields);
         }
 
         if (args.length() > 0) {
             writer.writeAttribute(ONCLICK_ATTR, "ice.fetchContacts('" + clientId + "', '" + args.toString() + "' );");
         } else {
             writer.writeAttribute(ONCLICK_ATTR, "ice.fetchContacts('" + clientId + "' );");
         }
         writer.writeAttribute(VALUE_ATTR, component.getLabel());
         writer.endElement(INPUT_ELEM);
         writer.endElement(DIV_ELEM);
     }
 
 }
