 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.autocompleteentry;
 
 import org.icefaces.ace.renderkit.InputRenderer;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.ace.util.JSONBuilder;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 
 import org.icefaces.impl.util.DOMUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.el.ELContext;
 import javax.el.ValueExpression;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
@MandatoryResourceComponent(tagName="autoCompleteEntry", value="org.icefaces.ace.component.autocompleteentry.autoCompleteEntry")
 public class AutoCompleteEntryRenderer extends InputRenderer {
 
     private static final String AUTOCOMPLETE_DIV = "_div";
     static final String AUTOCOMPLETE_INDEX = "_idx";
 
     public boolean getRendersChildren() {
         return true;
     }
 	
 	public void decode(FacesContext facesContext, UIComponent uiComponent) {
 		decodeBehaviors(facesContext, uiComponent);
 	}
 	
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 
 		ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         AutoCompleteEntry autoCompleteEntry = (AutoCompleteEntry) uiComponent;
 		
 		// root
         writer.startElement("div", null);
 		writer.writeAttribute("id", clientId + "_container", null);
 		writer.writeAttribute("class", autoCompleteEntry.getStyleClass(), null);
 
         Map paramMap = facesContext.getExternalContext().getRequestParameterMap();
         Map<String, Object> labelAttributes = getLabelAttributes(uiComponent);
 
         String inFieldLabel = (String) labelAttributes.get("inFieldLabel"), inFieldLabelStyleClass = "";
         String iceFocus = (String) paramMap.get("ice.focus");
         String value = (String) autoCompleteEntry.getValue();
         boolean labelIsInField = false;
         if (isValueBlank(value)) value = null;
         if (value == null && !isValueBlank(inFieldLabel) && !clientId.equals(iceFocus)) {
             value = inFieldLabel;
             inFieldLabelStyleClass = " " + IN_FIELD_LABEL_STYLE_CLASS;
             labelIsInField = true;
         }
 
         writeLabelAndIndicatorBefore(labelAttributes);
         // text field
 		writer.startElement("input", null);
         writer.writeAttribute("type", "text", null);
 		//setRootElementId(facesContext, input, uiComponent);
 		writer.writeAttribute("id", clientId, null);
 		writer.writeAttribute("name", clientId, null);
 		//writer.writeAttribute("class", autoCompleteEntry.getInputTextClass(), null);
 		String mousedownScript = (String) uiComponent.getAttributes().get("onmousedown");
 		mousedownScript = mousedownScript == null ? "" : mousedownScript;
 		//input.setAttribute(HTML.ONMOUSEDOWN_ATTR, combinedPassThru(mousedownScript, "this.focus();"));
 		writer.writeAttribute("onmousedown", mousedownScript + "this.focus();", null);
 		int width = autoCompleteEntry.getWidth();
 		writer.writeAttribute("style", "width: " + width + "px;", null);
         writer.writeAttribute("class", "ui-inputfield ui-widget ui-state-default ui-corner-all" + getStateStyleClasses(autoCompleteEntry) + inFieldLabelStyleClass, null);
 		writer.writeAttribute("autocomplete", "off", null);
         String onfocusCombinedValue = "setFocus(this.id);";
         Object onfocusAppValue = uiComponent.getAttributes().get("onfocus");
         if (onfocusAppValue != null) {
             onfocusCombinedValue += onfocusAppValue.toString();
 		}
         writer.writeAttribute("onfocus", onfocusCombinedValue, null);
         String onblurCombinedValue = "setFocus('');";
         Object onblurAppValue = uiComponent.getAttributes().get("onblur");
         if (onblurAppValue != null) {
             onblurCombinedValue += onblurAppValue.toString();
 		}
         writer.writeAttribute("onblur", onblurCombinedValue, null);
         Object onchangeAppValue = uiComponent.getAttributes().get("onchange");
         if (onchangeAppValue != null) {
             writer.writeAttribute("onchange", onchangeAppValue.toString(), null);
 		}
 		// this would prevent, when first valueChangeListener fires with null value
         if (value != null) {
 			writer.writeAttribute("value", value, null);
 		}
 		writer.endElement("input");
         writeLabelAndIndicatorAfter(labelAttributes);
 
 		// index
 		writer.startElement("input", null);
 		writer.writeAttribute("type", "hidden", null);
 		String indexId = clientId + AUTOCOMPLETE_INDEX;
 		writer.writeAttribute("name", indexId, null);
 		writer.endElement("input");
 		
 		// div
 		writer.startElement("div", null);
 		String divId = clientId + AUTOCOMPLETE_DIV;
 		writer.writeAttribute("id", clientId + AUTOCOMPLETE_DIV, null);
 		//String listClass = autoCompleteEntry.getListClass(); // TODO: check for list class and use it instead
 		writer.writeAttribute("class", "ui-widget ui-widget-content ui-corner-all", null);
 		writer.writeAttribute("style", "display:none;z-index:500;", null);
 		writer.endElement("div");
 
 		// script
 		writer.startElement("script", null);
 		//writer.writeAttribute("id", clientId + "script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		boolean partialSubmit = false; // TODO: remove
 		String direction = autoCompleteEntry.getDirection();
 		direction = direction != null ? ("up".equalsIgnoreCase(direction) || "down".equalsIgnoreCase(direction) ? direction : "auto" ) : "auto";
 		if (!autoCompleteEntry.isDisabled() && !autoCompleteEntry.isReadonly()) {
 			JSONBuilder jb = JSONBuilder.create();
 			jb.beginFunction("ice.ace.Autocompleter")
 				.item(clientId)
 				.item(divId)
 				.item(null, false) // TODO: re-add autoCompleteEntry.getOptions()
 				.item("ui-widget-content")
 				.item("ui-state-active")
 				.item(partialSubmit)
 				.item(autoCompleteEntry.getDelay())
 				.item(autoCompleteEntry.getMinChars())
 				.item(autoCompleteEntry.getHeight())
 				.item(direction)
 				.beginMap()
 				.entry("p", ""); // dummy property
 				encodeClientBehaviors(facesContext, autoCompleteEntry, jb);
 			jb.endMap();
             jb.beginMap()
                 .entryNonNullValue("inFieldLabel", inFieldLabel)
                 .entry("inFieldLabelStyleClass", IN_FIELD_LABEL_STYLE_CLASS)
                 .entry("labelIsInField", labelIsInField);
             jb.endMap().endFunction();
 			writer.writeText("new " + jb.toString(), null);
 		}
 		
 		writer.endElement("script");
 		writer.endElement("div");
     }
 
     public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
         AutoCompleteEntry autoCompleteEntry = (AutoCompleteEntry) uiComponent;
 		String clientId = autoCompleteEntry.getClientId(facesContext);
 		
 		if (autoCompleteEntry.getValue() != null) {
             if (autoCompleteEntry.hasChanged()) {
                 populateList(facesContext, autoCompleteEntry);
                 autoCompleteEntry.setChangedComponentId(null);
             }
         } else {
             writer.startElement("div", null);
 			writer.writeAttribute("id", clientId + "update", null);
 			encodeDynamicScript(facesContext, autoCompleteEntry, "");
 			writer.endElement("div");
 		}
     }
 
 
     public void populateList(FacesContext facesContext, AutoCompleteEntry autoCompleteEntry) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		String clientId = autoCompleteEntry.getClientId(facesContext);
         autoCompleteEntry.populateItemList();
         Iterator matches = autoCompleteEntry.getItemList();
 		String filter = ((String) autoCompleteEntry.getValue());
 		FilterMatchMode filterMatchMode = getFilterMatchMode(autoCompleteEntry);
         int rows = autoCompleteEntry.getRows();
         if (rows == 0) rows = Integer.MAX_VALUE;
         int rowCounter = 0;
             writer.startElement("div", null);
 			writer.writeAttribute("id", clientId + "update", null);
         if (autoCompleteEntry.getSelectFacet() != null) {
 
             UIComponent facet = autoCompleteEntry.getSelectFacet();
 			ValueExpression filterBy = autoCompleteEntry.getValueExpression("filterBy");
 			ELContext elContext = facesContext.getELContext();
 			String listVar = autoCompleteEntry.getListVar();
 
             writer.startElement("div", null);
 			//writer.writeAttribute("id", clientId + "content", null);
 			writer.writeAttribute("style", "display: none;", null);
 			writer.startElement("div", null);
             Map requestMap =
                     facesContext.getExternalContext().getRequestMap();
             //set index to 0, so child components can get client id from autoComplete component
             autoCompleteEntry.setIndex(0);
             while (matches.hasNext() && rowCounter <= rows) {
 
 				requestMap.put(listVar, matches.next());
 				String value = (String) filterBy.getValue(elContext);
 			
 				if (satisfiesFilter(value, filter, filterMatchMode, autoCompleteEntry)) {
 					rowCounter++;
 					writer.startElement("div", null);
 					//SelectItem item = (SelectItem) matches.next();
 					//requestMap.put(autoCompleteEntry.getListVar(), item.getValue());
 					
 					// When HTML is display we still need a selected value. Hidding the value in a hidden span
 					// accomplishes this.
 					writer.startElement("span", null); // span to display
 					writer.writeAttribute("class", "informal", null);
 					encodeParentAndChildren(facesContext, facet);
 					writer.endElement("span");
 					writer.startElement("span", null); // span to select
 					writer.writeAttribute("style", "visibility:hidden;display:none;", null);
 					String itemLabel = value;
 					if (itemLabel == null) {
 						/*itemLabel = converterGetAsString(
 								facesContext, autoCompleteEntry, item.getValue());*/
 						itemLabel = value;
 					}
 					writer.writeText(itemLabel, null);
 					writer.endElement("span");
 					autoCompleteEntry.resetId(facet);
 					writer.endElement("div");
 				}
 				
 				requestMap.remove(listVar);
             }
             autoCompleteEntry.setIndex(-1);
 
             //String nodeValue =
                     //DOMUtils.nodeToString(listDiv).replaceAll("\n", "");
 					// ice.ace.jq(ice.ace.escapeClientId(id)).get(0).innerHTML
 			writer.endElement("div");
             String call = "ice.ace.Autocompleters[\"" +
                     autoCompleteEntry.getClientId(facesContext) +
                     "\"].updateNOW(ice.ace.jq(ice.ace.escapeClientId('" + clientId + "update')).get(0).firstChild.innerHTML);";
             encodeDynamicScript(facesContext, autoCompleteEntry, call);
 			writer.endElement("div");
         } else {
             if (matches.hasNext()) {
                 StringBuffer sb = new StringBuffer("<div>");
                 SelectItem item = null;
                 while (matches.hasNext() && rowCounter <= rows) {
                     item = (SelectItem) matches.next();
                     String itemLabel = item.getLabel();
                     if (itemLabel == null) {
                         /*itemLabel = converterGetAsString(
                                 facesContext, autoCompleteEntry, item.getValue());*/ // TODO: add converter support
 						itemLabel = item.getValue().toString();
                     }
 					if (satisfiesFilter(itemLabel, filter, filterMatchMode, autoCompleteEntry)) {
                     sb.append("<div>").append(itemLabel)
                             .append("</div>");
 							rowCounter++;
 					}
                 }
                 sb.append("</div>");
                 String call = "ice.ace.Autocompleters[\"" + autoCompleteEntry.getClientId(facesContext) + "\"]" +
                         ".updateNOW('" + escapeSingleQuote(sb.toString()) + "');";
                 encodeDynamicScript(facesContext, autoCompleteEntry, call);
             }
         }
 		writer.endElement("div");
     }
 
     public void encodeDynamicScript(FacesContext facesContext, UIComponent uiComponent, String call) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
 		
 		writer.startElement("span", null);
 		//writer.writeAttribute("id", clientId + "dynamic_script", null);
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		writer.writeText(call, null);
 		writer.endElement("script");
 		writer.endElement("span");
 	}
 		
 	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 
 	}
 
     private static String escapeSingleQuote(String text) {
         if (null == text) {
             return "";
         }
         char[] chars = text.toCharArray();
         StringBuilder buffer = new StringBuilder(chars.length);
         for (int index = 0; index < chars.length; index++) {
             char ch = chars[index];
             if (ch == '\'') {
                 buffer.append("&#39;");
             } else {
                 buffer.append(ch);
             }
         }
 
         return buffer.toString();
     }
 	
     // taken from com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer
 	public static void encodeParentAndChildren(FacesContext facesContext, UIComponent parent) throws IOException {
         parent.encodeBegin(facesContext);
         if (parent.getRendersChildren()) {
             parent.encodeChildren(facesContext);
         } else {
             if (parent.getChildCount() > 0) {
                 Iterator children = parent.getChildren().iterator();
                 while (children.hasNext()) {
                     UIComponent nextChild = (UIComponent) children.next();
                     if (nextChild.isRendered()) {
                         encodeParentAndChildren(facesContext, nextChild);
                     }
                 }
             }
         }
         parent.encodeEnd(facesContext);
     }
 	
 	private FilterMatchMode getFilterMatchMode(AutoCompleteEntry autoCompleteEntry) {
 		String filterMatchMode = autoCompleteEntry.getFilterMatchMode();
 		if ("contains".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.contains;
 		if ("exact".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.exact;
 		if ("endsWith".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.endsWith;
 		if ("none".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.none;
 		return FilterMatchMode.startsWith;
 	}
 	
 	private enum FilterMatchMode {
 		contains,
 		exact,
 		startsWith,
 		endsWith,
 		none
 	}
 	
 	private boolean satisfiesFilter(String string, String filter, FilterMatchMode filterMatchMode, AutoCompleteEntry autoCompleteEntry) {
 		
 		if (string != null) {
 			if (!autoCompleteEntry.isCaseSensitive()) {
 				string = string.toLowerCase();
 				filter = filter.toLowerCase();
 			}
 			switch (filterMatchMode) {
 				case contains:
 					if (string.indexOf(filter) >= 0) return true;
 					break;
 				case exact:
 					if (string.equals(filter)) return true;
 					break;
 				case startsWith:
 					if (string.startsWith(filter)) return true;
 					break;
 				case endsWith:
 					if (string.endsWith(filter)) return true;
 					break;
 				default:
 					return true;
 			}
 		}
 		
 		return false;
 	}
 }
