 package org.icefaces.mobi.component.contentnavbar;
 
 
 import org.icefaces.mobi.component.contentpane.ContentPane;
 import org.icefaces.mobi.component.contentstack.ContentStack;
 import org.icefaces.mobi.renderkit.BaseLayoutRenderer;
 import org.icefaces.mobi.utils.HTML;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ContentNavBarRenderer extends BaseLayoutRenderer {
     private static Logger logger = Logger.getLogger(ContentNavBarRenderer.class.getName());
 
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         ContentNavBar navbar = (ContentNavBar) uiComponent;
         UIComponent parent = uiComponent.getParent();
         if (!(parent instanceof ContentPane) &&
             logger.isLoggable(Level.FINER)){
                  logger.finer("all children must be of type ContentPane");
                  return;
         }
         ContentPane cp = (ContentPane)parent;
         boolean client = cp.isClient();
         writer.startElement(HTML.DIV_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         StringBuilder styleClass = new StringBuilder(ContentNavBar.CONTENTNAVBAR_BASE_CLASS);
         StringBuilder menubuttonClass = new StringBuilder(ContentNavBar.CONTENTNAVBAR_BUTTON_MENU_CLASS);
         StringBuilder buttonClass = new StringBuilder (ContentNavBar.CONTENTNAVBAR_BUTTON_CLASS);
         // user specified style class
         String userDefinedClass = navbar.getStyleClass();
         if (userDefinedClass != null && userDefinedClass.length() > 0){
             styleClass.append(" ").append(userDefinedClass);
             buttonClass.append(" ").append(userDefinedClass);
             menubuttonClass.append(" ").append(userDefinedClass);
         }
         writer.writeAttribute("class", styleClass.toString(), "styleClass");
         // write out any users specified style attributes.
         if (navbar.getStyle() != null) {
             writer.writeAttribute(HTML.STYLE_ATTR, navbar.getStyle(), "style");
         }
         if (navbar.getMenuButtonLabel() !=null){
             //need to get info for onclick ..returns null if no layoutMenu though
             //also need to get menuTargetId
             if (navbar.getMenuButtonTargetId() !=null){
                 String targetId = navbar.getMenuButtonTargetId();
                 StringBuilder sb = getOnClickString(parent, facesContext, targetId, client);
                 writer.startElement(HTML.ANCHOR_ELEM, uiComponent);
                 writer.writeAttribute("class",menubuttonClass , "class");
                 if (sb !=null){
                    writer.writeAttribute("onclick", sb.toString(), null);
                 }
                 writer.write(navbar.getMenuButtonLabel());
                 writer.endElement(HTML.ANCHOR_ELEM);
             }
         }
     }
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         writer.endElement(HTML.DIV_ELEM);
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
     public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException{
         super.renderChildren(facesContext, uiComponent);
     }
 
     private StringBuilder getOnClickString(UIComponent parent, FacesContext facesContext,
                                           String targetId, boolean client){
         UIComponent grandparent = parent.getParent();
         if (! (grandparent instanceof ContentStack) ){
             return null;
         }
         ContentStack stack = (ContentStack)grandparent;
        if (null == stack.getContentMenuId()) {
             return null;
         }
         String clientId = stack.getClientId(facesContext);
         //probably just need the clientId, but if wanting server side, may need ice.se
         StringBuilder sb = new StringBuilder("mobi.layoutMenu.showMenu('").append(clientId);
         sb.append("', {selectedId: '").append(targetId).append("'");
         sb.append(", singleSubmit: true");
         sb.append(", client: ").append(client);
         sb.append(", single: ").append(true); //assume single for now
         sb.append("});") ;
         return sb;
     }
 
 }
