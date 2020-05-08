 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
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
 package org.richfaces.renderkit;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 
 import org.ajax4jsf.component.UIDataAdaptor;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UIListShuttle;
 import org.richfaces.component.UIOrderingBaseComponent;
 import org.richfaces.component.UIOrderingBaseComponent.ItemState;
 import org.richfaces.component.util.HtmlUtil;
 import org.richfaces.model.ListShuttleRowKey;
 
 /**
  * @author Nick Belaevski
  *
  */
 public abstract class ListShuttleRendererBase extends OrderingComponentRendererBase {
 	
 	protected static final String SELECTION_STATE_VAR_NAME = "selectionState";
 	
 	public final static String FACET_SOURCE_CAPTION = "sourceCaption";
 	
 	public final static String FACET_TARGET_CAPTION = "targetCaption";
 	
 	protected static final OrderingComponentRendererBase.ControlsHelper[] SHUTTLE_HELPERS = ListShuttleControlsHelper.HELPERS;
 	
 	protected static final OrderingComponentRendererBase.ControlsHelper[] TL_HELPERS = OrderingComponentControlsHelper.HELPERS;
 	
 	private static final String MESSAGE_BUNDLE_NAME = OrderingListRendererBase.class.getPackage().getName() + ".listShuttle";
 
 	private static class ListShuttleRendererTableHolder extends TableHolder {
 
 		private boolean source;
 		private Converter converter;
 		
 		public ListShuttleRendererTableHolder(UIDataAdaptor table, Converter converter, boolean source) {
 			super(table);
 		
 			this.converter = converter;
 			this.source = source;
 		}
 		
 		public boolean isSource() {
 			return source;
 		}
 		
 		public Converter getConverter() {
 			return converter;
 		}
 	}
 	
 	public ListShuttleRendererBase() {
 		super(MESSAGE_BUNDLE_NAME);
 	}
 
 	public void encodeSLCaption(FacesContext context, UIOrderingBaseComponent shuttle) throws IOException {
 		encodeCaption(context, shuttle, FACET_SOURCE_CAPTION, "rich-shuttle-source-caption",
 				ListShuttleControlsHelper.ATTRIBUTE_SOURCE_CAPTION_LABEL);
 	}
 
 	public void encodeTLCaption(FacesContext context, UIComponent shuttle) throws IOException {
 		encodeCaption(context, shuttle, FACET_TARGET_CAPTION, "rich-shuttle-target-caption",
 				ListShuttleControlsHelper.ATTRIBUTE_TARGET_CAPTION_LABEL);
 	}
 
 	public void encodeSLHeader(FacesContext context, UIOrderingBaseComponent shuttle) throws IOException {
 		encodeHeader(context, shuttle, "rich-table-header", "rich-shuttle-header-tab-cell", "sourceHeaderClass");
 	}
 	
 	public void encodeTLHeader(FacesContext context, UIOrderingBaseComponent shuttle) throws IOException {
 		encodeHeader(context, shuttle, "rich-table-header", "rich-shuttle-header-tab-cell", "sourceHeaderClass");
 	}
 	
 	public boolean isHeaderExists(FacesContext context, UIOrderingBaseComponent component) {
 		return isHeaderExists(context, component, "header");
 	}
 
 	protected String encodeRows(FacesContext context, UIOrderingBaseComponent shuttle, boolean source) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		StringWriter stringWriter = new StringWriter();
 		context.setResponseWriter(writer.cloneWithWriter(stringWriter));
 		encodeRows(context, shuttle, new ListShuttleRendererTableHolder(shuttle, getConverter(context, shuttle, true), source));
 		context.getResponseWriter().flush();
 		context.setResponseWriter(writer);
 
 		return stringWriter.getBuffer().toString();
 	}
 	
 	public void encodeOneRow(FacesContext context, TableHolder holder)
 	throws IOException {
 		UIListShuttle table = (UIListShuttle) holder.getTable();
 		ListShuttleRendererTableHolder shuttleRendererTableHolder = (ListShuttleRendererTableHolder) holder;
 		
 		ListShuttleRowKey listShuttleRowKey = (ListShuttleRowKey) table.getRowKey();
 		if (listShuttleRowKey != null) {
 			boolean source = shuttleRendererTableHolder.isSource();
 			if (source == listShuttleRowKey.isFacadeSource()) {
 				
 				ResponseWriter writer = context.getResponseWriter();
 				String clientId = table.getClientId(context);
 				writer.startElement(HTML.TR_ELEMENT, table);
 				writer.writeAttribute("id",  clientId, null);
 
 				StringBuffer rowClassName = new StringBuffer();
 				StringBuffer cellClassName = new StringBuffer();
 				if (source) {
 					rowClassName.append("rich-shuttle-source-row");
 					cellClassName.append("rich-shuttle-source-cell");
 				} else {
 					rowClassName.append("rich-shuttle-target-row");
 					cellClassName.append("rich-shuttle-target-cell");
 				}
 
 				String rowClass = holder.getRowClass();
 				if (rowClass != null) {
 					rowClassName.append(' ');
 					rowClassName.append(rowClass);
 				}
 
 				ComponentVariables variables = ComponentsVariableResolver.getVariables(this, table);
 				SelectionState selectionState = (SelectionState) variables.getVariable(SELECTION_STATE_VAR_NAME);
 				ItemState itemState = getItemState(context, table, variables);
 				
 				boolean active = itemState.isActive();
 				boolean selected = itemState.isSelected();
 				selectionState.addState(selected);
 				if (selected) {
 					if (source) {
 						rowClassName.append(" rich-shuttle-source-row-selected");
 						cellClassName.append(" rich-shuttle-source-cell-selected");
 					} else {
 						rowClassName.append(" rich-shuttle-target-row-selected");
 						cellClassName.append(" rich-shuttle-target-cell-selected");
 					}
 				}
 				
 				writer.writeAttribute("class", rowClassName.toString(), null);
 
 				int colCounter = 0;
 				boolean columnRendered = false;
 
 				for (Iterator iterator = table.columns(); iterator.hasNext();) {
 					UIComponent component = (UIComponent) iterator.next();
 
 					if (component.isRendered()) {
 						writer.startElement(HTML.td_ELEM, table);
 
 						Object width = component.getAttributes().get("width");
 						if (width != null) {
 							writer.writeAttribute("style", "width: " + HtmlUtil.qualifySize(width.toString()), null);
 						}
 
 						String columnClass = holder.getColumnClass(colCounter);
 						if (columnClass != null) {
 							writer.writeAttribute("class", cellClassName.toString().concat(" " + columnClass), null);
 						} else {
 							writer.writeAttribute("class", cellClassName.toString(), null);
 						}
 
 						writer.startElement(HTML.IMG_ELEMENT, table);
 						writer.writeAttribute(HTML.src_ATTRIBUTE, getResource("/org/richfaces/renderkit/html/images/spacer.gif").getUri(context, null), null);
 						writer.writeAttribute(HTML.style_ATTRIBUTE, "width:1px;height:1px;", null);
 						writer.writeAttribute(HTML.alt_ATTRIBUTE, " ", null);
 						writer.endElement(HTML.IMG_ELEMENT);
 						
 						renderChildren(context, component);
 
 						if (!columnRendered) {
 							writer.startElement(HTML.INPUT_ELEM, table);
 							writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
							writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
 							writer.writeAttribute(HTML.NAME_ATTRIBUTE, table.getBaseClientId(context), null);
 
 							StringBuffer value = new StringBuffer();
 							if (selected) {
 								value.append('s');
 							}
 							
 							if (active) {
 								value.append('a');
 							}
 
 							value.append(table.getRowKey());
 							value.append(':');
 							value.append(shuttleRendererTableHolder.getConverter().getAsString(context, table, table.getRowData()));
 							
 							writer.writeAttribute(HTML.value_ATTRIBUTE, value.toString(), null);
 							
 							writer.writeAttribute(HTML.id_ATTRIBUTE, clientId + "StateInput", null);
 							
 							writer.endElement(HTML.INPUT_ELEM);
 							
 							columnRendered = true;
 						}
 
 						writer.endElement(HTML.td_ELEM);
 					}
 
 					colCounter++;
 				}
 
 				writer.endElement(HTML.TR_ELEMENT);
 			}
 		}
 	}
 	
 	public void encodeChildren(FacesContext context, UIComponent component)
 			throws IOException {
         if (component.isRendered()) {
 			ResponseWriter writer = context.getResponseWriter();
 			doEncodeChildren(writer, context, component);
 		}
 	}
 	
 	public void encodeShuttleControlsFacets(FacesContext context, UIOrderingBaseComponent component, 
 			SelectionState sourceSelectionState, SelectionState targetSelectionState) 
 		throws IOException {
 		
 		boolean needsStrut = true;
 		String clientId = component.getClientId(context);
 
 		ResponseWriter writer = context.getResponseWriter();
 		
 		int divider = SHUTTLE_HELPERS.length / 2;
 		
 		for (int i = 0; i < SHUTTLE_HELPERS.length; i++) {
 			SelectionState state = (i < divider ? sourceSelectionState : targetSelectionState);
 			
 			boolean enabled;
 			if (i <= 1 || i >= SHUTTLE_HELPERS.length - 2) {
 				enabled = state.isItemExist();
 			} else {
 				enabled = state.isSelected();
 			}
 			
 			if (i % 2 == 1) {
 				enabled = !enabled;
 			}
 			
 			if (SHUTTLE_HELPERS[i].isRendered(context, component)) {
 				needsStrut = false;
 				
 				//proper assumption about helpers ordering
 				encodeControlFacet(context, component, SHUTTLE_HELPERS[i], clientId, writer, enabled, 
 						"rich-list-shuttle-button", " rich-shuttle-control");
 			}
 		}
 		
 		if (needsStrut) {
 			writer.startElement(HTML.SPAN_ELEM, component);
 			writer.endElement(HTML.SPAN_ELEM);
 		}
 	}
 	
 	public void encodeTLControlsFacets(FacesContext context, UIOrderingBaseComponent component, SelectionState selectionState) 
 		throws IOException {
 		String clientId = component.getClientId(context);
 
 		ResponseWriter writer = context.getResponseWriter();
 		
 		int divider = TL_HELPERS.length / 2;
 		
 		for (int i = 0; i < TL_HELPERS.length; i++) {
 			boolean boundarySelection = i < divider ? selectionState.isFirstSelected() : selectionState.isLastSelected();
 			boolean enabled = selectionState.isSelected() && !boundarySelection;
 			if (i % 2 == 1) {
 				enabled = !enabled;
 			}
 			
 			if (TL_HELPERS[i].isRendered(context, component)) {
 				//proper assumption about helpers ordering
 				encodeControlFacet(context, component, TL_HELPERS[i], clientId, writer, enabled, 
 						"rich-list-shuttle-button", " rich-shuttle-control");
 			}
 		}
 	}
 	
 	private boolean isEmpty(String s) {
 		return s == null || s.length() == 0;
 	}
 	
 	public void doDecode(FacesContext context, UIComponent component) {
 		UIListShuttle listShuttle = (UIListShuttle) component;
 		
 		String clientId = listShuttle.getBaseClientId(context);
 		ExternalContext externalContext = context.getExternalContext();
 		Map<String, String[]> requestParameterValuesMap = externalContext
         								 .getRequestParameterValuesMap();
         
 		String[] strings = (String[]) requestParameterValuesMap.get(clientId);
 		
 		if (strings != null && strings.length != 0) {
 			Set sourceSelection = new HashSet();
 			Set targetSelection = new HashSet();
 			Object activeItem = null;
         	Map map = new LinkedHashMap();
         	
         	boolean facadeSource = true;
         	
         	Converter converter = getConverter(context, listShuttle, false);
         	
         	for (int i = 0; i < strings.length; i++) {
 				String string = strings[i];
 				
 				if (":".equals(string)) {
 					facadeSource = false;
 					continue;
 				}
 				
 				int idx = string.indexOf(':');
 				Object value = converter.getAsObject(context, listShuttle, string.substring(idx + 1));
 				String substring = string.substring(0, idx);
 				
 				idx = 0;
 				boolean source = true;
 
 				boolean selected = false;
 				
 				if (substring.charAt(idx) == 's') {
 					(facadeSource ? sourceSelection : targetSelection).add(value);
 					idx++;
 				}
 
 				if (substring.charAt(idx) == 'a') {
 					activeItem = value;
 					idx++;
 				}
 
 				if (substring.charAt(idx) == 't') {
 					source = false;
 					idx++;
 				}
 
 				substring = substring.substring(idx);
 				
 				Object key = new ListShuttleRowKey(new Integer(substring), source, facadeSource);
 				map.put(key, value);
         	}
         	listShuttle.setSubmittedStrings(map, sourceSelection, targetSelection, activeItem);
         }
 	}
 	
 	public String getCaptionDisplay(FacesContext context, UIComponent component) {
 		UIListShuttle shuttle = (UIListShuttle)component;
 		if ((shuttle.getSourceCaptionLabel() != null && !"".equals(shuttle.getSourceCaptionLabel())) ||
 				(shuttle.getTargetCaptionLabel() != null && !"".equals(shuttle.getTargetCaptionLabel())) ||
 				(shuttle.getFacet("sourceCaption") != null && shuttle.getFacet("sourceCaption").isRendered()) ||
 				(shuttle.getFacet("targetCaption") != null && shuttle.getFacet("targetCaption").isRendered())) {
 			
 			return "";
 		}
 		
 		return "display: none;";
 	}
 	
 }
