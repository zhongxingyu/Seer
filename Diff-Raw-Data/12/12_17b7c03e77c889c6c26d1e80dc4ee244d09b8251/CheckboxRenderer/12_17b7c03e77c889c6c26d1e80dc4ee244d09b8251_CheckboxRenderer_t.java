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
 import org.ajax4jsf.resource.InternetResource;
 import org.ajax4jsf.resource.InternetResource;
 //
 //
 //
 
 
 import org.omidbiz.renderkit.CheckboxRenderBase;
 
 
 
 /**
  * Renderer for component class org.omidbiz.renderkit.html.CheckboxRenderer
  */
 public class CheckboxRenderer extends CheckboxRenderBase {
 
 	public CheckboxRenderer () {
 		super();
 	}
 
 	// 
 	// Declarations
 	//
 	private final InternetResource[] styles = {
 						getResource("/org/omidbiz/renderkit/html/css/checkBox.css")
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
 	private final InternetResource[] scripts = {
 						getResource("/org/richfaces/renderkit/html/scripts/jquery/jquery.js")
 						,
 				new org.ajax4jsf.javascript.PrototypeScript()
 						,
 				new org.ajax4jsf.javascript.AjaxScript()
 						,
 				getResource("/org/omidbiz/renderkit/html/script/checkBox.js")
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
 		return org.omidbiz.component.UICheckbox.class;
 	}
 
 
 	public void doEncodeEnd(ResponseWriter writer, FacesContext context, org.omidbiz.component.UICheckbox component, ComponentVariables variables) throws IOException {
 	  writer.startElement("script", component);
 			getUtils().writeAttribute(writer, "type", "text/javascript" );
 			
 writer.writeText(convertToString("jQuery.noConflict();"),null);
 
 writer.endElement("script");
 java.lang.String clientId = component.getClientId(context);
variables.setVariable("$variable", getResource( "/org/omidbiz/renderkit/html/css/switch.gif" ).getUri(context, component) );
 
 variables.setVariable("value", component.getAttributes().get("value") );
 variables.setVariable("checked", component.getAttributes().get("checked") );
 variables.setVariable("change", component.getAttributes().get("onchange") );
 
  
 			String value = (String) variables.getVariable("value");
 			Boolean checked = (Boolean) variables.getVariable("checked");
 
 
 writer.startElement("p", component);
 			getUtils().writeAttribute(writer, "class", "field switch" );
 			
 writer.startElement("label", component);
 			getUtils().writeAttribute(writer, "class", "cb-enable " + convertToString(clientId) );
 			
 writer.startElement("span", component);
 
 writer.writeText(convertToString("On"),null);
 
 writer.endElement("span");
 writer.endElement("label");
 writer.startElement("label", component);
 			getUtils().writeAttribute(writer, "class", "cb-disable " + convertToString(clientId) + " selected" );
 			
 writer.startElement("span", component);
 
 writer.writeText(convertToString("Off"),null);
 
 writer.endElement("span");
 writer.endElement("label");
  if (value != null && !"".equals(value.trim()) && value.equalsIgnoreCase("Y") || checked ) { 
 writer.startElement("input", component);
 			getUtils().writeAttribute(writer, "checked", "checked" );
 						getUtils().writeAttribute(writer, "class", convertToString(clientId) + "-chk" );
 						getUtils().writeAttribute(writer, "id", convertToString(clientId) + "-chk" );
 						getUtils().writeAttribute(writer, "name", convertToString(clientId) + "-chk" );
 						getUtils().writeAttribute(writer, "onchange", variables.getVariable("change") );
 						getUtils().writeAttribute(writer, "type", "checkbox" );
 						getUtils().writeAttribute(writer, "value", "Y" );
 			
 writer.endElement("input");
  } else { 
 writer.startElement("input", component);
 			getUtils().writeAttribute(writer, "class", convertToString(clientId) + "-chk" );
 						getUtils().writeAttribute(writer, "id", convertToString(clientId) + "-chk" );
 						getUtils().writeAttribute(writer, "name", convertToString(clientId) + "-chk" );
 						getUtils().writeAttribute(writer, "onchange", variables.getVariable("change") );
 						getUtils().writeAttribute(writer, "onclick", "Richfaces.checkboxControl.setYvalue('" + convertToString(clientId) + "');" );
 						getUtils().writeAttribute(writer, "type", "checkbox" );
 						getUtils().writeAttribute(writer, "value", "N" );
 			
 writer.endElement("input");
  } 
 writer.endElement("p");
 writer.startElement("script", component);
 			getUtils().writeAttribute(writer, "type", "text/javascript" );
 			
 writer.writeText(convertToString("jQuery(document).ready( function(){ \n		jQuery(\".cb-enable " + convertToString(clientId) + "\").click(function(){\n			var parent = jQuery(this).parents('.switch');\n			jQuery(\".cb-disable " + convertToString(clientId) + "\",parent).removeClass('selected');\n			jQuery(this).addClass('selected');\n			jQuery(\"." + convertToString(clientId) + "-chk\",parent).attr('checked', true);\n			jQuery( \"." + convertToString(clientId) + "-chk\" ).click();\n	      \n	              \n		});\n		jQuery(\".cb-disable " + convertToString(clientId) + "\").click(function(){\n			var parent = jQuery(this).parents('.switch');\n			jQuery('.cb-enable " + convertToString(clientId) + "',parent).removeClass('selected');\n			jQuery(this).addClass('selected');\n			jQuery('." + convertToString(clientId) + "-chk',parent).attr('checked', false);\n			jQuery( \"." + convertToString(clientId) + "-chk\" ).click();\n		});\n	});"),null);
 
 writer.endElement("script");
 
 	}		
 	
 	public void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, component);
 		doEncodeEnd(writer, context, (org.omidbiz.component.UICheckbox)component, variables );
 
 		ComponentsVariableResolver.removeVariables(this, component);
 	}		
 	
 
 }
