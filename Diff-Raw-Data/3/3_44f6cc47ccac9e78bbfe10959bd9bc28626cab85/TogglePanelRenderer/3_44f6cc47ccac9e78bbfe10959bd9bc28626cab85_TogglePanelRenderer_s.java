 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
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
 
 /*
  * Created on 04.07.2006
  */
 package org.richfaces.renderkit.html;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.renderkit.RendererBase;
 import org.richfaces.component.UITogglePanel;
 import org.richfaces.event.SwitchablePanelSwitchEvent;
 
 /**
  * @author igels
  *
  */
 public class TogglePanelRenderer extends RendererBase {
 
 	/* (non-Javadoc)
 	 * @see org.ajax4jsf.renderkit.RendererBase#getComponentClass()
 	 */
 	protected Class<? extends UIComponent> getComponentClass() {
 		return UITogglePanel.class;
 	}
 
 	public void doDecode(FacesContext context, UIComponent component) {
 		super.doDecode(context, component);
 		Map<String, String> rqMap = context.getExternalContext().getRequestParameterMap();
 		Object clnId = rqMap.get(component.getClientId(context));
 		UITogglePanel panel = (UITogglePanel)component;
 		if (clnId != null) {
 			if (UITogglePanel.CLIENT_METHOD.equals(panel.getSwitchType())) {
 				new SwitchablePanelSwitchEvent(panel, clnId, null).queue();
 			}
 		}
 	}
 
 	private UIComponent getFacet(UITogglePanel togglePanel, String facetName) {
 		UIComponent child = (UIComponent) togglePanel.getFacet(facetName);
 
 		if (child == null) {
 			throw new FacesException("Facet with name: " + facetName + " not found!");
 		}
 
 		return child;
 	}
 
 	//xxx by nick - denis - do not catch exception, rethrow them
 	public void handleFacets(FacesContext context, UITogglePanel component) throws IOException  {
 		UITogglePanel panel = (UITogglePanel)component;
 		List<String> stateOrderList = component.getStateOrderList();
 		String state = (String) component.getValue();
 		if (state == null) {
 			String initialState = component.getInitialState();
 			if (initialState != null) {
 				state = initialState;
 			} else {
 			    if (!stateOrderList.isEmpty()) {
 					state = (String) stateOrderList.get(0);
 			    } else {
 			    	throw new FacesException("The \"initialState\" attribute of the togglePanel component should be set if \"stateOrder\" attribute is empty!");
 			    }
 			}
 		}
 		
 		ResponseWriter out = context.getResponseWriter();
 		String switchType = panel.getSwitchType();
 		if (UITogglePanel.CLIENT_METHOD.equals(switchType)) {
 			// Client
 			String panelId = panel.getClientId(context);
 			StringBuffer divIds = new StringBuffer();
 			boolean first = true;
 
 			for (Iterator<String> iterator = stateOrderList.iterator(); iterator.hasNext();) {
 				String stateName = (String) iterator.next();
 
 				UIComponent child = getFacet(component, stateName);
 				if(!first) {
 					divIds.append("\", \"");
 				}
 				divIds.append(stateName);
 				first = false;
 
 				String id = panel.getClientId(context) + "_" + stateName;
 
 				out.startElement("div", component);
 				out.writeAttribute("id", id, null);
 				out.writeAttribute("style", "display: " + (stateName.equals(state) ? "inherit": "none"), null);
 				renderChild(context, child);
 				out.endElement("div");
 			}
 
 			String idInput = panel.getClientId(context) + "_input";
 			out.startElement("div", component);
 			out.writeAttribute("style", "display: none;", null);
 			out.startElement("input", component);
 			out.writeAttribute("type", "hidden", null);
 			out.writeAttribute("id", idInput, null);
 			out.writeAttribute("name", panel.getClientId(context), null);
 			out.writeAttribute("value", state, null);
 			out.endElement("input");
 			out.endElement("div");
 
 			
 			String script =  MessageFormat.format(CLIENT_SCRIPT, 
 			        panelId, divIds.toString(), state);
 			out.write(script);
 
 //			} else if(UITogglePanel.AJAX_METHOD.equals(switchType)) {
 //			// Ajax
 //			UIComponent child = getFacet(component, state);
 //			if(child != null) {
 //			out.startElement("div", component);
 //			renderChild(context, child);
 //			out.endElement("div");
 //			}
 		} else {
 			// Server or AJAX
 			UIComponent child = getFacet(component, state);
 			if(child != null) {
 				out.startElement("div", component);
 				renderChild(context, child);
 				out.endElement("div");
 			}
 		}
 		panel.setValue(state);
 	}
 
 	private static final String CLIENT_SCRIPT = "<script type=\"text/javascript\">TogglePanelManager.add(new TogglePanel(\"{0}\", $A([\"{1}\"]), \"{2}\"));</script>";
 
 	/* (non-Javadoc)
 	 * @see javax.faces.render.Renderer#getRendersChildren()
 	 */
 	public boolean getRendersChildren() {
 		return true;
 	}
     
 //    public void setValue(FacesContext context, UIComponent component) {
 //        UITogglePanel panel = (UITogglePanel) component;
 //        Object value = panel.getValue();
 //        if (value == null) {
 //            value = panel.getStateOrderList().size() > 0 
 //            ? panel.getStateOrderList().get(0) : "";
 //            panel.setValue(value);
 //        }
 //    }
 }
