 /**
  * License Agreement.
  *
  * Ajax4jsf 1.1 - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.omidbiz.renderkit.html;
 
 
 // 
 // Imports
 //
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.Map;
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import javax.faces.context.FacesContext;
 import javax.faces.component.UIComponent;
 import org.ajax4jsf.resource.InternetResource;
 import org.ajax4jsf.resource.InternetResource;
 //
 //
 //
 
 
 import org.omidbiz.renderkit.WindowPopupRendererBase;
 
 
 
 /**
  * Renderer for component class org.omidbiz.renderkit.html.WindowPopupRenderer
  */
 public class WindowPopupRenderer extends WindowPopupRendererBase {
 
 	public WindowPopupRenderer () {
 		super();
 	}
 
 	// 
 	// Declarations
 	//
 	private final InternetResource[] scripts = {
 						getResource("/org/richfaces/renderkit/html/scripts/jquery/jquery.js")
 						,
 				new org.ajax4jsf.javascript.PrototypeScript()
 						,
 				new org.ajax4jsf.javascript.AjaxScript()
 						,
 				getResource("/org/omidbiz/renderkit/html/script/colorboxUtil.js")
 						,
 				getResource("/org/omidbiz/renderkit/html/script/colorbox.js")
 	};
 
 private InternetResource[] scriptsAll = null;
 
 protected InternetResource[] getScripts() {
 	synchronized (this) {
 		if (scriptsAll == null) {
 			InternetResource[] rsrcs = super.getScripts();
 			boolean ignoreSuper = rsrcs == null || rsrcs.length == 0;
 			boolean ignoreThis = scripts == null || scripts.length == 0;
 			
 			if (ignoreSuper) {
 				if (ignoreThis) {
 					scriptsAll = new InternetResource[0];	
 				} else {
 					scriptsAll = scripts;
 				}
 			} else {
 				if (ignoreThis) {
 					scriptsAll = rsrcs;
 				} else {
 					java.util.Set rsrcsSet = new java.util.LinkedHashSet();
 
 					for (int i = 0; i < rsrcs.length; i++ ) {
 						rsrcsSet.add(rsrcs[i]);
 					}
 
 					for (int i = 0; i < scripts.length; i++ ) {
 						rsrcsSet.add(scripts[i]);
 					}
 
 					scriptsAll = (InternetResource[]) rsrcsSet.toArray(new InternetResource[rsrcsSet.size()]);
 				}
 			}
 		}
 	}
 	
 	return scriptsAll;
 }
 	private final InternetResource[] styles = {
 						getResource("/org/omidbiz/renderkit/html/css/colorbox.css")
 	};
 
 private InternetResource[] stylesAll = null;
 
 protected InternetResource[] getStyles() {
 	synchronized (this) {
 		if (stylesAll == null) {
 			InternetResource[] rsrcs = super.getStyles();
 			boolean ignoreSuper = rsrcs == null || rsrcs.length == 0;
 			boolean ignoreThis = styles == null || styles.length == 0;
 			
 			if (ignoreSuper) {
 				if (ignoreThis) {
 					stylesAll = new InternetResource[0];	
 				} else {
 					stylesAll = styles;
 				}
 			} else {
 				if (ignoreThis) {
 					stylesAll = rsrcs;
 				} else {
 					java.util.Set rsrcsSet = new java.util.LinkedHashSet();
 
 					for (int i = 0; i < rsrcs.length; i++ ) {
 						rsrcsSet.add(rsrcs[i]);
 					}
 
 					for (int i = 0; i < styles.length; i++ ) {
 						rsrcsSet.add(styles[i]);
 					}
 
 					stylesAll = (InternetResource[]) rsrcsSet.toArray(new InternetResource[rsrcsSet.size()]);
 				}
 			}
 		}
 	}
 	
 	return stylesAll;
 }
 	// 
 	// 
 	//
 
 
 	private String convertToString(Object obj ) {
 		return ( obj == null ? "" : obj.toString() );
 	}
 	private String convertToString(boolean b ) {
 		return String.valueOf(b);
 	}
 	private String convertToString(int b ) {
 		return b!=Integer.MIN_VALUE?String.valueOf(b):"";
 	}
 	private String convertToString(long b ) {
 		return b!=Long.MIN_VALUE?String.valueOf(b):"";
 	}
 	
 	private boolean isEmpty(Object o) {
 		if (null == o) {
 			return true;
 		}
 		if (o instanceof String ) {
 			return (0 == ((String)o).length());
 		}
 		if (o instanceof Collection) {
 			return (0 == ((Collection)o).size());
 		}
 		if (o instanceof Map) {
 			return (0 == ((Map)o).size());
 		}
 		if (o.getClass().isArray()) {
 			return (0 == ((Object [])o).length);
 		}
 		return false;
 	}
 	
 	/**
 	 * Get base component class, targetted for this renderer. Used for check arguments in decode/encode.
 	 * @return
 	 */
 	protected Class getComponentClass() {
 		return org.omidbiz.component.UIWindowPopup.class;
 	}
 
 
 	public void doEncodeEnd(ResponseWriter writer, FacesContext context, org.omidbiz.component.UIWindowPopup component, ComponentVariables variables) throws IOException {
 	  java.lang.String clientId = component.getClientId(context);
 variables.setVariable("bg", getResource( "/org/omidbiz/renderkit/html/css/images/loading_background.png" ).getUri(context, component) );
 
 variables.setVariable("loader", getResource( "/org/omidbiz/renderkit/html/css/images/loading.gif" ).getUri(context, component) );
 
 variables.setVariable("controls", getResource( "/org/omidbiz/renderkit/html/css/images/controls.png" ).getUri(context, component) );
 
 variables.setVariable("border", getResource( "/org/omidbiz/renderkit/html/css/images/border.png" ).getUri(context, component) );
 
 variables.setVariable("view", component.getAttributes().get("view") );
 variables.setVariable("type", component.getAttributes().get("type") );
 variables.setVariable("useImage", component.getAttributes().get("useImage") );
 variables.setVariable("imageSrc", component.getAttributes().get("imageSrc") );
 variables.setVariable("id", component.getAttributes().get("id") );
 variables.setVariable("iframe", component.getAttributes().get("iframe") );
 
  
 			String type = (String) variables.getVariable("type");
 			Boolean useImage = (Boolean) variables.getVariable("useImage");
 
 
  if (! "".equals(type.trim()) && type.equalsIgnoreCase("dialog") ) { 
 writer.startElement("a", component);
 			getUtils().writeAttribute(writer, "class", convertToString(variables.getVariable("id")) + "popupClass" );
 						getUtils().writeAttribute(writer, "href", variables.getVariable("view") );
 						getUtils().writeAttribute(writer, "rel", "rel" + convertToString(variables.getVariable("id")) );
 			
  if (useImage) { 
 writer.startElement("img", component);
 			getUtils().writeAttribute(writer, "border", "0" );
 						getUtils().writeAttribute(writer, "src", variables.getVariable("imageSrc") );
 						getUtils().writeAttribute(writer, "title", component.getAttributes().get("title") );
 			
 writer.endElement("img");
  } else { 
 writer.writeText(convertToString(component.getAttributes().get("openText")),null);
 
  } 
 writer.endElement("a");
 writer.startElement("script", component);
 			getUtils().writeAttribute(writer, "type", "text/javascript" );
 			
 writer.writeText(convertToString("jQuery(document).ready(function(){	\n			jQuery(\"." + convertToString(variables.getVariable("id")) + "popupClass\").colorbox({width:\"80%\", height:\"80%\", iframe:" + convertToString(variables.getVariable("iframe")) + ", rel : 'nofollow'});\n			jQuery('a[rel=rel" + convertToString(variables.getVariable("id")) + "]').colorbox(jQuery.extend({width:'80%', height:'80%', speed:0, iframe:" + convertToString(variables.getVariable("iframe")) + "}, Richfaces.colorboxControl.getParameters()));\n			\n		});"),null);
 
 writer.endElement("script");
  } 
  if (! "".equals(type.trim()) && type.equalsIgnoreCase("link") ) { 
 writer.startElement("a", component);
 			getUtils().writeAttribute(writer, "href", "#" );
 						getUtils().writeAttribute(writer, "onclick", "parent.jQuery.fn.colorbox.close();" );
 			
 writer.writeText(convertToString(component.getAttributes().get("closeText")),null);
 
 writer.endElement("a");
  } 
  if (! "".equals(type.trim()) && type.equalsIgnoreCase("button") ) { 
 writer.startElement("a", component);
 			getUtils().writeAttribute(writer, "class", convertToString(variables.getVariable("id")) + "popupClass" );
 						getUtils().writeAttribute(writer, "href", variables.getVariable("view") );
 						getUtils().writeAttribute(writer, "rel", "rel" + convertToString(variables.getVariable("id")) );
 			
 writer.startElement("button", component);
			getUtils().writeAttribute(writer, "class", "wpopupBtn" );
						getUtils().writeAttribute(writer, "onclick", getOnClick(context,component) );
			
writer.startElement("span", component);
			getUtils().writeAttribute(writer, "class", "wpopupspan" );
 			
 writer.writeText(convertToString(component.getAttributes().get("openText")),null);
 
writer.endElement("span");
 renderChildren(context, component);
 
 writer.endElement("button");
 writer.endElement("a");
 writer.startElement("script", component);
 			getUtils().writeAttribute(writer, "type", "text/javascript" );
 			
 writer.writeText(convertToString("jQuery(document).ready(function(){	\n				jQuery(\"." + convertToString(variables.getVariable("id")) + "popupClass\").colorbox({width:\"80%\", height:\"80%\", iframe:" + convertToString(variables.getVariable("iframe")) + ", rel : 'nofollow'});\n				jQuery('a[rel=rel" + convertToString(variables.getVariable("id")) + "]').colorbox(jQuery.extend({width:'80%', height:'80%', speed:0, iframe:" + convertToString(variables.getVariable("iframe")) + "}, Richfaces.colorboxControl.getParameters()));\n				\n			});"),null);
 
 writer.endElement("script");
  } 
 
 	}		
 	
 	public void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, component);
 		doEncodeEnd(writer, context, (org.omidbiz.component.UIWindowPopup)component, variables );
 
 		ComponentsVariableResolver.removeVariables(this, component);
 	}		
 	
 
 }
