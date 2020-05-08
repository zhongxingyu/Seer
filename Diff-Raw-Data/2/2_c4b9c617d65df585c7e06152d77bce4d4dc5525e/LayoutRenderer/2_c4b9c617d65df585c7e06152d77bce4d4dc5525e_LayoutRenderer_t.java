 /**
  * 
  */
 package org.richfaces.renderkit.html;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
 import org.ajax4jsf.renderkit.RendererBase;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.LayoutPosition;
 import org.richfaces.component.LayoutStructure;
 import org.richfaces.component.UILayout;
 import org.richfaces.component.UILayoutPanel;
 
 /**
  * @author asmirnov
  * 
  */
 public class LayoutRenderer extends
 		RendererBase {
 	
 	public static final String LAYOUT_STRUCTURE_ATTRIBUTE = UILayout.class.getName()+".structure";
 	private static final Object[] LAYOUT_EXCLUSIONS = {HTML.id_ATTRIBUTE,HTML.style_ATTRIBUTE};
 	@Override
 	protected void doEncodeBegin(ResponseWriter writer, FacesContext context,
 			UIComponent component) throws IOException {
 		writer.startElement(HTML.DIV_ELEM, component);
 		getUtils().encodeCustomId(context, component);
 		getUtils().encodePassThruWithExclusionsArray(context, component, LAYOUT_EXCLUSIONS);
 		Object style = component.getAttributes().get("style");
 		writer.writeAttribute(HTML.style_ATTRIBUTE,null==style?"":(style.toString()+";")+"zoom:1;","style");
 	}
 
 	
 	@Override
 	protected void doEncodeChildren(ResponseWriter writer,
 			FacesContext context, UIComponent component) throws IOException {
 		renderLayout(writer,context, (UILayout) component);
 	}
 	
 	public void renderLayout(ResponseWriter writer,FacesContext context, UILayout layout)
 			throws IOException {
 		LayoutStructure structure = new LayoutStructure(layout);
 		structure.calculateWidth();
 		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
 		Object oldLayout = requestMap.get(LAYOUT_STRUCTURE_ATTRIBUTE);
 		requestMap.put(LAYOUT_STRUCTURE_ATTRIBUTE, structure);
 		// Detect layout content;
 		if (null != structure.getTop()) {
 			structure.getTop().encodeAll(context);
 		}
 		if (structure.getColumns() > 0) {
 			// Reorder panels to fill ordeg left->center->right.
 			if (null != structure.getLeft()) {
 				structure.getLeft().encodeAll(context);
 			} 
 			if (null != structure.getCenter()) {
 				structure.getCenter().encodeAll(context);
 			} 
 			if (null != structure.getRight()) {
 				structure.getRight().encodeAll(context);
 			} 
 		}
 		// line separator.
 		writer.startElement(HTML.DIV_ELEM, layout);
		writer.writeAttribute(HTML.style_ATTRIBUTE, "display: block; height: 0;line-height:0px; font-size:0px; clear: both; visibility: hidden;", null);
 		writer.writeText(".", null);
 		writer.endElement(HTML.DIV_ELEM);
 		if (null != structure.getBottom()) {
 			renderChild(context, structure.getBottom());
 		}
 		requestMap.put(LAYOUT_STRUCTURE_ATTRIBUTE, oldLayout);
 	}
 	
 	@Override
 	protected void doEncodeEnd(ResponseWriter writer, FacesContext context,
 			UIComponent component) throws IOException {
 		writer.endElement(HTML.DIV_ELEM);
 	}
 
 	@Override
 	public boolean getRendersChildren() {
 		return true;
 	}
 	
 	@Override
 	protected Class<? extends UIComponent> getComponentClass() {
 		return UILayout.class;
 	}
 }
