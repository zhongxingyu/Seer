 /**
  * 
  */
 package org.richfaces.renderkit;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UIPage;
 import org.richfaces.skin.SkinFactory;
 import org.richfaces.skin.Theme;
 
 /**
  * @author asmirnov
  * 
  */
 public abstract class AbstractPageRenderer extends HeaderResourcesRendererBase {
 
 	public static final String RENDERER_TYPE = "org.richfaces.PageRenderer";
 
 	private static final Map<String, String[]> doctypes;
 
 	static {
 		// Fill doctype, content-type and namespace map for different formats.
 		doctypes = new HashMap<String, String[]>();
 		doctypes
 				.put(
 						"html-transitional",
 						new String[] {
 								"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n",
 								"text/html", null });
 		doctypes.put("html", new String[] {
 				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n"
 						+ "\"http://www.w3.org/TR/html4/strict.dtd\">\n",
 				"text/html", null });
 		doctypes.put("html-frameset", new String[] {
 				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"\n"
 						+ "\"http://www.w3.org/TR/html4/frameset.dtd\">\n",
 				"text/html", null });
 		doctypes
 				.put(
 						"xhtml",
 						new String[] {
 								"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
 										+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n",
 								"application/xhtml+xml",
 								"http://www.w3.org/1999/xhtml" });
 		doctypes
 				.put(
 						"xhtml-transitional",
 						new String[] {
 								"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
 										+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n",
 								"application/xhtml+xml",
 								"http://www.w3.org/1999/xhtml" });
 		doctypes
 				.put(
 						"xhtml-frameset",
 						new String[] {
 								"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\"\n"
 										+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">\n",
 								"application/xhtml+xml",
 								"http://www.w3.org/1999/xhtml" });
 		doctypes.put("html-3.2", new String[] {
 				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n",
 				"text/html", null });
 	}
 
 	public String prolog(FacesContext context, UIComponent component)
 			throws IOException {
 		ResponseWriter out = context.getResponseWriter();
 		Map<String, Object> attributes = component.getAttributes();
 		String format = (String) attributes.get("markupType");
 		String contentType = null;
 		String namespace = null;
 		// String characterEncoding = out.getCharacterEncoding();
 		String[] docType = null;
 		if (null != format) {
 			docType = (String[]) doctypes.get(format);
 		} else {
 			contentType = out.getContentType();
 			for (Iterator<String[]> iterator = doctypes.values().iterator(); iterator
 					.hasNext();) {
 				String[] types = (String[]) iterator.next();
 				if (types[1].equals(contentType)) {
 					docType = types;
 					break;
 				}
 			}
 		}
 		if (null != docType) {
 			contentType = docType[1];
 			namespace = docType[2];
 			out.write(docType[0]);
 		}
 		if (null == contentType) {
 			contentType = (String) attributes.get("contentType");
 		}
 		if (null != contentType) {
 			// response.setContentType(contentType /*+ ";charset=" +
 			// characterEncoding*/);
 		}
 		return namespace;
 	}
 
 
 	public Theme getTheme(FacesContext context, UIPage page) {
 		Theme theme = null;
 		String themeName = page.getTheme();
 		if(null != themeName && themeName.length()>0){
 			theme = SkinFactory.getInstance().getTheme(context, themeName);
 		}
 		return theme;
 	}
 	
 	public void themeStyle(FacesContext context, UIPage component) throws IOException{
 		Theme theme = getTheme(context, component);
 		if(null != theme){
 			String style = theme.getStyle();
 			if(null != style){
 				ResponseWriter writer = context.getResponseWriter();
 				writer.startElement(HTML.LINK_ELEMENT, component);
 				writer.writeAttribute(HTML.TYPE_ATTR, "text/css", null);
 				writer.writeAttribute(HTML.REL_ATTR, "stylesheet", null);
 				writer.writeAttribute(HTML.class_ATTRIBUTE, "component", null);
 				style = context.getApplication().getViewHandler().getResourceURL(context, style);
 				style= context.getExternalContext().encodeResourceURL(style);
 				writer.writeAttribute(HTML.HREF_ATTR, style, null);
 				writer.endElement(HTML.LINK_ELEMENT);
 			}
 		}
 		
 	}
 
 	public void themeScript(FacesContext context, UIPage component) throws IOException{
 		Theme theme = getTheme(context, component);
 		if(null != theme){
 			String script = theme.getScript();
 			if(null != script){
 				ResponseWriter writer = context.getResponseWriter();
 				writer.startElement(HTML.SCRIPT_ELEM, component);
 				writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
 				script = context.getApplication().getViewHandler().getResourceURL(context, script);
 				script= context.getExternalContext().encodeResourceURL(script);
 				writer.writeAttribute(HTML.src_ATTRIBUTE, script, null);
 				writer.endElement(HTML.SCRIPT_ELEM);
 			}
 		}
 		
 	}
 
 	
 	
 	public void pageStyle(FacesContext context, UIComponent component)
 			throws IOException {
 		// Write body class.
 		ResponseWriter writer = context.getResponseWriter();
 		Map<String, Object> attributes = component.getAttributes();
 		writer.startElement("style", component);
 		writer.writeAttribute(HTML.TYPE_ATTR, "text/css", null);
 		// Calculate page width
 		Integer width = (Integer) attributes.get("width");
 		if (null != width && width.intValue() > 0) {
 			float nonIeWidth = (width.floatValue() / 13.0f);
 			float ieWidth = (width.floatValue() / 13.333f);
 			StringBuilder format = new StringBuilder(
 					".rich-page{margin:auto;text-align:left;");
 			format.append("width:").append(nonIeWidth).append("em;");
 			format.append("*width:").append(ieWidth).append("em;}\n");
 			writer.write(format.toString());
 		} else {
 			writer.write(".rich-page{margin:auto 10px;width:auto;}\n");
 		}
 		// Calculate sidebar width
 		if (component.getFacet("sidebar") != null) {
 			Object sidebarPosition = attributes.get("sidebarPosition");
 			String position;
 			if ("right".equals(sidebarPosition)) {
 				writer
 						.write(".rich-page-main{float:left;margin-right:-30em;}\n");
 				position = "right";
 			} else {
 				writer
 						.write(".rich-page-main{float:right;margin-left:-30em;}\n");
 				position = "left";
 			}
 			Integer sidebarWidth = (Integer) attributes.get("sidebarWidth");
 			if (null != sidebarWidth && sidebarWidth.intValue() > 0) {
 				float nonIeWidth = (sidebarWidth.floatValue() / 13.0f);
 				float ieWidth = (sidebarWidth.floatValue() / 13.333f);
 				StringBuilder format = new StringBuilder(
 						".rich-page-sidebar{float:");
 				format.append(position).append(";");
 				format.append("width:").append(nonIeWidth).append("em;");
 				format.append("*width:").append(ieWidth).append("em;}\n");
 				format.append(".rich-page-body{margin-");
 				format.append(position).append(":").append(nonIeWidth + 1.0f)
 						.append("em;");
 				;
 				format.append("*margin").append(position).append(":").append(
 						ieWidth + .975f).append("em;}\n");
 				writer.write(format.toString());
 			}
 
 		} // Cleanup
 		writer.write(".rich-page-body{float:none;width:auto;}\n");
 		writer.endElement("style");
 	}
 
 	public boolean hasFacet(UIComponent component, String facet) {
 		return null != component.getFacet(facet);
 	}
 
	public boolean hasTitle(FacesContext context, UIComponent component) {
		return component.getAttributes().get("pageTitle") != null &&  !component.getAttributes().get("pageTitle").toString().trim().equals("");
	}
 }
