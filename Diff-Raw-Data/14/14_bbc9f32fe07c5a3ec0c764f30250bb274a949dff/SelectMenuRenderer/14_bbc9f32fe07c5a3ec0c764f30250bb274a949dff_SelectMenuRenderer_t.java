 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.ace.component.selectmenu;
 
 import org.icefaces.ace.renderkit.InputRenderer;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.util.EnvUtils;
 import org.icefaces.ace.event.TextChangeEvent;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.el.ELContext;
 import javax.el.ValueExpression;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.IOException;
 
 @MandatoryResourceComponent(tagName="selectMenu", value="org.icefaces.ace.component.selectmenu.SelectMenu")
 public class SelectMenuRenderer extends InputRenderer {
 
     private static final String AUTOCOMPLETE_DIV = "_div";
 
     public boolean getRendersChildren() {
         return true;
     }
 
 	public void decode(FacesContext facesContext, UIComponent uiComponent) {
 		SelectMenu selectMenu = (SelectMenu) uiComponent;
 		selectMenu.setItemList(null);
         Map requestMap = facesContext.getExternalContext().getRequestParameterMap();
         String clientId = selectMenu.getClientId(facesContext);
         String value = (String) requestMap.get(clientId + "_input");
 		String oldValue = (String) selectMenu.getValue();
 		
 		boolean isEventSource = false;
 		Object sourceId = requestMap.get("ice.event.captured");
         if (sourceId != null && (sourceId.toString().equals(clientId) || sourceId.toString().equals(clientId + "_input"))) {
 			isEventSource = true;
         }
         if (isEventSource) selectMenu.setSubmittedValue(value);
 		
 		decodeBehaviors(facesContext, selectMenu);
 	}
 	
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         SelectMenu selectMenu = (SelectMenu) uiComponent;
         int width = selectMenu.getWidth();
         boolean ariaEnabled = EnvUtils.isAriaEnabled(facesContext);
         Map paramMap = facesContext.getExternalContext().getRequestParameterMap();
         Map<String, Object> labelAttributes = getLabelAttributes(uiComponent);
         String inFieldLabel = (String) labelAttributes.get("inFieldLabel");
         String inFieldLabelStyleClass = "";
         String iceFocus = (String) paramMap.get("ice.focus");
         String mousedownScript = (String) uiComponent.getAttributes().get("onmousedown");
         String onfocusCombinedValue = "setFocus(this.id);";
         String onblurCombinedValue = "";
         Object onfocusAppValue = uiComponent.getAttributes().get("onfocus");
         Object onblurAppValue = uiComponent.getAttributes().get("onblur");
         Object onchangeAppValue = uiComponent.getAttributes().get("onchange");
 
 		String inputClientId = clientId + "_input";
 /*
         if (isValueBlank(text)) text = null;
         String inputClientId = clientId + "_input";
         boolean labelIsInField = false;
 
         if (text == null && !isValueBlank(inFieldLabel) && !inputClientId.equals(iceFocus)) {
             text = inFieldLabel;
             inFieldLabelStyleClass = " " + IN_FIELD_LABEL_STYLE_CLASS;
             labelIsInField = true;
         }
 */
 
 		// root
         writer.startElement("div", null);
 		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("class", "ui-select" + selectMenu.getStyleClass(), null);
 
 		writer.startElement("span", null);
		writer.writeAttribute("class", "ui-widget ui-corner-all ui-state-default ui-select-value", null);
         writer.writeAttribute("style", "display: inline-block; width:100px; height:20px; padding-bottom: 2px;", null);
 		writer.writeAttribute("tabindex", "0", null);
 		
 		// text span
 		writer.startElement("span", null);
		writer.writeAttribute("style", "display: inline-block; overflow: hidden;", null);
 		writer.endElement("span");
 		
 		// down arrow span
 		writer.startElement("div", null);
 		writer.writeAttribute("class", "ui-state-default ui-corner-all", null);
 		writer.writeAttribute("style", "float:right; width:17px; height:100%;", null);
 		writer.startElement("div", null);
 		writer.writeAttribute("class", "ui-icon ui-icon-triangle-1-s", null);
 		writer.endElement("div");
 		writer.endElement("div");
 		
 		writer.endElement("span");
 
 		writer.startElement("input", null);
         writer.writeAttribute("type", "hidden", null);
 		writer.writeAttribute("name", inputClientId, null);
 		writer.endElement("input");
         //writeLabelAndIndicatorBefore(labelAttributes);
 
         // text field
 		/*
 		writer.startElement("input", null);
         writer.writeAttribute("type", "text", null);
 		writer.writeAttribute("name", inputClientId, null);
         if (ariaEnabled)
             writer.writeAttribute("role", "textbox", null);
 
 		mousedownScript = mousedownScript == null ? "" : mousedownScript;
 		writer.writeAttribute("onmousedown", mousedownScript + "this.focus();", null);
 		writer.writeAttribute("style", "width: " + width + "px;" + selectMenu.getStyle(), null);
         writer.writeAttribute("class", "ui-inputfield ui-widget ui-state-default ui-corner-all"
                 + getStateStyleClasses(selectMenu) + inFieldLabelStyleClass, null);
 		writer.writeAttribute("autocomplete", "off", null);
 
         if (onfocusAppValue != null)
             onfocusCombinedValue += onfocusAppValue.toString();
 
         writer.writeAttribute("onfocus", onfocusCombinedValue, null);
 
         if (onblurAppValue != null)
             onblurCombinedValue += onblurAppValue.toString();
 
         writer.writeAttribute("onblur", onblurCombinedValue, null);
 
         if (onchangeAppValue != null)
             writer.writeAttribute("onchange", onchangeAppValue.toString(), null);
 
 		if (ariaEnabled) {
             final SelectMenu component = (SelectMenu) uiComponent;
             Map<String, Object> ariaAttributes = new HashMap<String, Object>() {{
                 put("autocomplete", "list");
                 put("readonly", component.isReadonly());
                 put("required", component.isRequired());
                 put("disabled", component.isDisabled());
                 put("invalid", !component.isValid());
             }};
             writeAriaAttributes(ariaAttributes, labelAttributes);
         }
 
         writer.endElement("input");
 		*/
 
         //writeLabelAndIndicatorAfter(labelAttributes);
 
         String divId = clientId + AUTOCOMPLETE_DIV;
 
         writer.startElement("div", null);
         writer.writeAttribute("id", divId, null);
        writer.writeAttribute("class", "ui-widget ui-widget-content ui-corner-all ui-select-list", null);
         writer.writeAttribute("style", "display:none;z-index:500;", null);
         writer.endElement("div");
 
         encodeScript(facesContext, writer, clientId, selectMenu,
                 paramMap, inFieldLabel, inputClientId, false/*labelIsInField*/);
 
 		writer.endElement("div");
     }
 
     private void encodeScript(FacesContext facesContext, ResponseWriter writer, String clientId, SelectMenu selectMenu, Map paramMap, String inFieldLabel, String inputClientId, boolean labelIsInField) throws IOException {
         String divId = clientId + AUTOCOMPLETE_DIV;
         Object sourceId = paramMap.get("ice.event.captured");
         boolean isEventSource = sourceId != null && sourceId.toString().equals(inputClientId);
         //Object event = paramMap.get("javax.faces.behavior.event");
         //boolean isBlurEvent = 
         //boolean focus = isEventSource && !isBlurEvent;
 
         // script
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
 
         //if (!selectMenu.isDisabled() && !selectMenu.isReadonly()) {
 			JSONBuilder jb = JSONBuilder.create();
 
             jb.beginFunction("ice.ace.create")
             .item("SelectMenu")
 			
             .beginArray()
             .item(clientId)
             .item(divId)
             .item("ui-widget-content")
            .item("ui-state-hover")
			.item("ui-state-active")
             .item(selectMenu.getHeight())
 			.item(selectMenu.getRows())
             .beginMap()
             .entry("p", ""); // dummy property
             encodeClientBehaviors(facesContext, selectMenu, jb);
 
             jb.endMap();
 
             /*jb.beginMap()
             .entryNonNullValue("inFieldLabel", inFieldLabel)
             .entry("inFieldLabelStyleClass", IN_FIELD_LABEL_STYLE_CLASS)
             .entry("labelIsInField", labelIsInField);
 
             jb.endMap();*/
 
 			jb.endArray();
             jb.endFunction();
 
             writer.writeText(jb.toString(), null);
 		//}
 
         writer.endElement("script");
 
         // field update script
         /*writer.startElement("span", null);
         writer.writeAttribute("id", clientId + "_fieldupdate", null);
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
         writer.writeText("(function() {", null);
         writer.writeText("var instance = ice.ace.SelectMenus[\"" + clientId + "\"];", null);
         writer.writeText("instance.updateField('" + escapeBackslashes(***) + "', " + focus + ");", null);
         writer.writeText("})();", null);
         writer.endElement("script");
         writer.endElement("span");*/
     }
 
     public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {
         SelectMenu selectMenu = (SelectMenu) uiComponent;
 		String clientId = selectMenu.getClientId(facesContext);
 		populateList(facesContext, selectMenu);
     }
 
     public void populateList(FacesContext facesContext, SelectMenu selectMenu) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		String clientId = selectMenu.getClientId(facesContext);
         selectMenu.populateItemList();
         Iterator matches = selectMenu.getItemListIterator();
         int rows = selectMenu.getRows();
         if (rows == 0) rows = Integer.MAX_VALUE;
         int rowCounter = 0;
         writer.startElement("div", null);
 		writer.writeAttribute("id", clientId + "_update", null);
         if (selectMenu.getSelectFacet() != null) {
 
             UIComponent facet = selectMenu.getSelectFacet();
 			ValueExpression filterBy = selectMenu.getValueExpression("filterBy");
 			ELContext elContext = facesContext.getELContext();
 			String listVar = selectMenu.getListVar();
 
             writer.startElement("div", null);
 			writer.writeAttribute("style", "display: none;", null);
 			writer.startElement("div", null);
             Map requestMap = facesContext.getExternalContext().getRequestMap();
             //set index to 0, so child components can get client id from autoComplete component
             selectMenu.setIndex(0);
             while (matches.hasNext() && rowCounter < rows) {
 
 				requestMap.put(listVar, matches.next());
 				String value = (String) filterBy.getValue(elContext);
 			
 				rowCounter++;
 				writer.startElement("div", null);
 				writer.writeAttribute("style", "border: 0;", null);
 				
 				// When HTML is display we still need a selected value. Hidding the value in a hidden span
 				// accomplishes this.
 				writer.startElement("span", null); // span to display
 				writer.writeAttribute("class", "informal", null);
 				encodeParentAndChildren(facesContext, facet);
 				writer.endElement("span");
 				writer.startElement("span", null); // span to select
 				writer.writeAttribute("style", "visibility:hidden;display:none;", null);
 				String itemLabel;
 				try {
 					itemLabel = (String) getConvertedValue(facesContext, selectMenu, value, true);
 				} catch (Exception e) {
 					itemLabel = value;
 				}
 				writer.writeText(itemLabel, null);
 				writer.endElement("span");
 				selectMenu.resetId(facet);
 				writer.endElement("div");
 				
 				requestMap.remove(listVar);
             }
             selectMenu.setIndex(-1);
 
 			writer.endElement("div");
             String call = "ice.ace.SelectMenus[\"" + clientId +
                     "\"].setContent(ice.ace.jq(ice.ace.escapeClientId('" + clientId + "_update')).get(0).firstChild.innerHTML);";
             encodeDynamicScript(facesContext, selectMenu, call);
 			writer.endElement("div");
         } else {
             if (matches.hasNext()) {
                 StringBuffer sb = new StringBuffer("<div>");
                 SelectItem item = null;
                 while (matches.hasNext() && rowCounter < rows) {
                     item = (SelectItem) matches.next();
                     String itemLabel = item.getLabel();
                     if (itemLabel == null) {
 						try {
 							itemLabel = (String) getConvertedValue(facesContext, selectMenu, item.getValue(), true);
 						} catch (Exception e) {
 							itemLabel = item.getValue().toString();
 						}
                     }
                     sb.append("<div style=\"border: 0;\">").append(itemLabel).append("</div>");
 					rowCounter++;
                 }
                 sb.append("</div>");
                 String call = "ice.ace.SelectMenus[\"" + clientId + "\"]" +
                         ".setContent('" + escapeSingleQuote(sb.toString()) + "');";
                 encodeDynamicScript(facesContext, selectMenu, call);
             }
         }
 		writer.endElement("div");
     }
 	
     public void encodeDynamicScript(FacesContext facesContext, UIComponent uiComponent, String call) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
 		
 		writer.startElement("span", null);
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
 	
 	private static String escapeBackslashes(String str) {
 		if (str == null) return "";
 		return str.replace("\\", "\\\\");
 	}
 	
 	private String getTimestamp(FacesContext facesContext, SelectMenu selectMenu) {
 		Map requestMap = facesContext.getExternalContext().getRequestParameterMap();
 		Object sourceId = requestMap.get("ice.event.captured");
 		String clientId = selectMenu.getClientId(facesContext);
 		String timestamp = "";
 		// only include a timestamp if the user pressed the up or down arrow keys to introduce a difference from previous dom
 		// so that the domdiff sends the call to update the list of options
 		timestamp = "" + System.currentTimeMillis();
 		return timestamp;
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
 	
 	@Override
 	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
 		return getConvertedValue(context, component, submittedValue, false);
 	}
 	
 	private Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue, boolean force) throws ConverterException {
 		SelectMenu selectMenu = (SelectMenu) component;
 		String value = (String) submittedValue;
 		Converter converter = selectMenu.getConverter();
 		
 			if(converter != null) {
 				return converter.getAsObject(context, selectMenu, value);
 			}
 			else {
 				ValueExpression ve = selectMenu.getValueExpression("value");
 
 				if(ve != null) {
 					Class<?> valueType = ve.getType(context.getELContext());
 					Converter converterForType = context.getApplication().createConverter(valueType);
 
 					if(converterForType != null) {
 						return converterForType.getAsObject(context, selectMenu, value);
 					}
 				}
 			}
 		
 		return value;	
 	}
 }
