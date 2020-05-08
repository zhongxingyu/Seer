 package org.icefaces.ace.component.listcontrol;
 
 import org.icefaces.ace.component.list.ACEList;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UINamingContainer;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 @MandatoryResourceComponent(tagName="listControl", value="org.icefaces.ace.component.listcontrol.ListControl")
 public class ListControlRenderer extends CoreRenderer {
     public static final String containerStyleClass = "if-list-nctrls ui-widget";
     public static final String dualListContainerStyleClass = "if-list-dl-cnt ui-widget";
     public static final String controlStyleClass = "if-list-nctrl";
     public static final String controlsSpacerStyleClass = "if-list-ctrl-spcr";
     public static final String dualListStyleClass = "if-list-dl";
     public static final String listControlHeaderClass = "if-list-nctrl-head";
     public static final String listControlFooterClass = "if-list-nctrl-foot";
     public static final String firstStyleClass = "if-list-dl-1";
     public static final String secondStyleClass = "if-list-dl-2";
 
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ListControl control = (ListControl)component;
         ListControlRenderContext renderContext = new ListControlRenderContext();
 
         control.setRenderContext(renderContext);
 
         String style = control.getStyle();
         DualListPosition position = control.getPosition();
         String styleClass = control.getStyleClass();
         UIComponent facet = control.getFacet("header");
         renderContext.dualListMode = hasTwoListChildren(control);
 
         if (styleClass == null) styleClass = "";
         else styleClass += " ";
         if (renderContext.dualListMode)
             styleClass = styleClass + dualListContainerStyleClass;
         else
             styleClass = styleClass + containerStyleClass;
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, control.getClientId(context), "id");
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "styleClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "style");
 
         encodeHeader(context, writer, control, facet);
 
         if (!renderContext.dualListMode || position.equals(DualListPosition.TOP) || position.equals(DualListPosition.BOTH) || position.equals(DualListPosition.ALL)) {
             if (renderContext.dualListMode) {
                 writer.startElement(HTML.DIV_ELEM, null);
                 writer.writeAttribute(HTML.CLASS_ATTR, containerStyleClass.substring(0, containerStyleClass.indexOf(" ")), null);
             }
             encodeControls(context, writer, control);
             if (renderContext.dualListMode) {
                 writer.endElement(HTML.DIV_ELEM);
             }
         }
     }
 
     private boolean hasTwoListChildren(ListControl control) {
         List<UIComponent> children = control.getChildren();
         List<ACEList> lists = new ArrayList<ACEList>();
 
         for (UIComponent c : children)
             if (c instanceof ACEList) lists.add((ACEList)c);
 
         if (lists.size() == 2) {
            ListControlRenderContext context = ((ListControlRenderContext)control.getRenderContext());
             context.one = lists.get(0);
             context.two = lists.get(1);
             return true;
         } else if (lists.size() > 2 || lists.size() == 1) {
             throw new FacesException("ListControl " + control + " : " + "Cannot have fewer or greater than 2 ace:list children. The nested list mode only supports dual lists.");
         }
 
         return false;
     }
 
     private void encodeHeader(FacesContext context, ResponseWriter writer, ListControl control, UIComponent facet) throws IOException {
         if (facet == null) return;
 
         String styleClass = control.getHeaderClass();
         if (styleClass == null)  styleClass = "";
         else styleClass += " ";
 
         styleClass = styleClass + listControlHeaderClass;
 
         String style = control.getHeaderStyle();
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "headerClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "headerStyle");
 
         facet.encodeAll(context);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeControls(FacesContext context, ResponseWriter writer, ListControl control) throws IOException {
         String styleClass = control.getControlClass();
         styleClass = styleClass == null ? controlStyleClass : styleClass + " " + controlStyleClass;
 
         for (String code : control.getFormat().split(" "))
             encodeControl(context, writer, control, code, styleClass);
     }
 
     private void encodeControl(FacesContext context, ResponseWriter writer, ListControl control, String code, String containerClass) throws IOException {
         String styleClass;
         String property;
         UIComponent facet = null;
 
         if (code.equals("alll")) {
             styleClass = control.getAllLeftClass();
             facet = control.getFacet("allLeft");
             property = "allLeftClass";
         } else if (code.equals("lft")) {
             styleClass = control.getLeftClass();
             facet = control.getFacet("left");
             property = "leftClass";
         } else if (code.equals("rgt")) {
             styleClass = control.getRightClass();
             facet = control.getFacet("right");
             property = "rightClass";
         } else if (code.equals("allr")) {
             styleClass = control.getAllRightClass();
             facet = control.getFacet("allRight");
             property = "allRightClass";
         } else return;
 
         if (facet != null) {
             facet.encodeAll(context);
         } else {
             String spacerClass = control.getSpacerClass();
             spacerClass = spacerClass == null ? controlsSpacerStyleClass : spacerClass + " " + controlsSpacerStyleClass;
 
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, spacerClass, null);
 
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, "if-list-nctrl-" + code + " " + containerClass, "controlClass");
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, styleClass, property);
             writer.endElement(HTML.SPAN_ELEM);
             writer.endElement(HTML.SPAN_ELEM);
 
             writer.endElement(HTML.SPAN_ELEM);
         }
     }
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ListControl control = (ListControl) component;
        ListControlRenderContext renderContext = ((ListControlRenderContext)control.getRenderContext());
 
         if (renderContext.dualListMode)
             renderDualListLayout(context, writer, control, renderContext.one, renderContext.two);
     }
 
     private void renderDualListLayout(FacesContext context, ResponseWriter writer, ListControl control, ACEList one, ACEList two) throws IOException {
         DualListPosition position = control.getPosition();
        ListControlRenderContext renderContext = ((ListControlRenderContext)control.getRenderContext());
         Boolean middleMode = position.equals(DualListPosition.MIDDLE) || position.equals(DualListPosition.ALL);
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, dualListStyleClass, null);
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR,  firstStyleClass, null);
         writer.startElement(HTML.DIV_ELEM, null);
         if (middleMode) {
             writer.writeAttribute(HTML.STYLE_ATTR, "margin-right:1.1em;", null);
         }
 
         renderContext.one.encodeAll(context);
 
         writer.endElement(HTML.DIV_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
 
         if (middleMode) {
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, containerStyleClass.substring(0, containerStyleClass.indexOf(" "))+" ui-corner-all", null);
             encodeControls(context, writer, control);
             writer.endElement(HTML.SPAN_ELEM);
         }
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, secondStyleClass, null);
         writer.startElement(HTML.DIV_ELEM, null);
         if (middleMode) {
             writer.writeAttribute(HTML.STYLE_ATTR, "margin-right:1.4em;", null);
         }
 
         renderContext.two.encodeAll(context);
 
         writer.endElement(HTML.DIV_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         UIComponent facet = component.getFacet("footer");
         ListControl control = (ListControl) component;
        ListControlRenderContext renderContext = ((ListControlRenderContext)control.getRenderContext());
         DualListPosition position = control.getPosition();
 
         if (renderContext.dualListMode && position.equals(DualListPosition.BOTTOM)
                 || position.equals(DualListPosition.BOTH)
                 || position.equals(DualListPosition.ALL)) {
 
             if (renderContext.dualListMode) {
                 writer.startElement(HTML.DIV_ELEM, null);
                 writer.writeAttribute(HTML.CLASS_ATTR, containerStyleClass.substring(0, containerStyleClass.indexOf(" ")), null);
             }
 
             encodeControls(context, writer, control);
 
             if (renderContext.dualListMode) {
                 writer.endElement(HTML.DIV_ELEM);
             }
         }
 
         encodeFooter(context, writer, control, facet);
 
         encodeScript(context, writer, control);
 
         writer.endElement(HTML.DIV_ELEM);
         control.setRenderContext(null);
     }
 
     private void encodeScript(FacesContext context, ResponseWriter writer, ListControl control) throws IOException {
         String widgetVar = resolveWidgetVar(control);
         String clientId = control.getClientId(context);
        ListControlRenderContext renderContext = ((ListControlRenderContext)control.getRenderContext());
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, clientId+"_script", null);
         writer.startElement(HTML.SCRIPT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
 
         JSONBuilder cfgBuilder = new JSONBuilder();
 
         cfgBuilder.beginMap();
         cfgBuilder.entry("separator", UINamingContainer.getSeparatorChar(context));
         cfgBuilder.entry("selector", control.getSelector(clientId.replace(":","\\:"), renderContext.dualListMode));
         cfgBuilder.endMap();
 
         writer.write("var " + widgetVar + " = new ice.ace.ListControl('" + clientId + "', " + cfgBuilder +");");
         writer.endElement(HTML.SCRIPT_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private void encodeFooter(FacesContext context, ResponseWriter writer, ListControl control, UIComponent facet) throws IOException {
         if (facet == null) return;
 
         String styleClass = control.getFooterClass();
         if (styleClass == null)  styleClass = "";
         else styleClass += " ";
 
         styleClass = styleClass + listControlFooterClass;
 
         String style = control.getFooterStyle();
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "footerClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "footerStyle");
 
         facet.encodeAll(context);
         writer.endElement(HTML.DIV_ELEM);
     }
 }
