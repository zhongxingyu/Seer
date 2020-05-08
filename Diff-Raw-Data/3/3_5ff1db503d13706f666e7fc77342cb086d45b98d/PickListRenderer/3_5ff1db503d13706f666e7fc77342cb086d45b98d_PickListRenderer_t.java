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
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javax.faces.component.EditableValueHolder;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.model.SelectItem;
 import javax.faces.model.SelectItemGroup;
 
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.javascript.JSFunctionDefinition;
 import org.ajax4jsf.javascript.JSReference;
 import org.ajax4jsf.javascript.ScriptString;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.ajax4jsf.util.SelectUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.richfaces.component.UIPickList;
 import org.richfaces.component.util.HtmlUtil;
 
 public class PickListRenderer extends HeaderResourcesRendererBase {
 
 	private static  Log logger = LogFactory.getLog(PickListRenderer.class);
 	
 	private static final String HIDDEN_SUFFIX = "valueKeeper";
    
     private static final String MESSAGE_BUNDLE_NAME = PickListRenderer.class.getPackage().getName() + ".pickList";
     
     private static final String SHOW_LABELS_ATTRIBUTE_NAME = "showButtonsLabel";
     
     private static final OrderingComponentRendererBase.ControlsHelper[] SHUTTLE_HELPERS = PickListControlsHelper.HELPERS;
     
     protected static final class ListStateFlags {
 
     	public ListStateFlags() {
     		super();
     	}
     	
     	protected boolean isSelectedList;
 
     	protected boolean isAvailableList;
     }
     
     protected List<SelectItem> selectItemsForSelectedList(FacesContext facesContext, UIComponent uiComponent, List<SelectItem> selectItemList, List<Object> lookupList) {
 		
     	List<SelectItem> selectItemForSelectedValues = new ArrayList<SelectItem>();
 	
 		for (Object lookupItem: lookupList) {
 		    
 			for (SelectItem selectItem: selectItemList) {
 		    	
 				if(selectItem instanceof SelectItemGroup) {
 
 					SelectItem[] items = ((SelectItemGroup) selectItem).getSelectItems();
 					for (int j = 0; j < items.length; j++) {
 						if(lookupItem.equals(items[j].getValue())) {
 							selectItemForSelectedValues.add(items[j]);
 						}
 					}
 					
 				} else {
 					
 					if (lookupItem.equals(selectItem.getValue())) {
 						selectItemForSelectedValues.add(selectItem);
 					}
 					
 				}
 				
 		    }
 		    
 		}
 		
 		return selectItemForSelectedValues;
     }
     
     protected List<Object> getValuesList(UIPickList pickList) {
     	
     	List <Object> valuesList = new ArrayList<Object>();
     	
     	Object value = getCurrentValue(FacesContext.getCurrentInstance(), pickList);
 
     	if(null == value || "".equals(value)) {
     		return valuesList;
     	}
     	
     	if(value.getClass().isArray()) {
     		int len = Array.getLength(value);
  		   
     		for (int i = 0; i < len; i++) {
     			
     			Object localValue = Array.get(value, i);
     			if(localValue != null) {
     				valuesList.add(localValue);
     			}
     			
  		    }
     		
  		    return valuesList;
     	} else if(value instanceof List) {
     		List <?> list = (List<?>) value;
    		
     		for (Object item: list) {
     			valuesList.add(item);
 		    }
 		    	
 		   	return valuesList;
 		
     	} else {
 			throw new IllegalArgumentException("Error: value of UIPickList component is not of type Array or List");
 		}
     	
     }
 
     /**
      * @param context the FacesContext for the current request
      * @param component the UIComponent whose value we're interested in
      *
      * @return the value to be rendered and formats it if required. Sets to
      *  empty string if value is null.
      */
     protected Object getCurrentValue(FacesContext context,
                                      UIComponent component) {
 
         if (component instanceof UIInput) {
             Object submittedValue = ((UIInput) component).getSubmittedValue();
             if (submittedValue != null) {
                 return submittedValue;
             }
         }
         Object currentValue = ((UIPickList)component).getValue();
         return currentValue;
 
     }
     
     protected List <SelectItem> selectItemsForAvailableList(FacesContext facesContext, UIComponent uiComponent, List<SelectItem> selectItemList,
 	    List<SelectItem> selectItemsForSelectedList) {
     	    	
     	  List <SelectItem> processItems = new ArrayList<SelectItem>();	
     	  
     	  for(SelectItem item: selectItemList) {
     		  
     		  if(item instanceof SelectItemGroup) {
     			
     			SelectItem items[] = ((SelectItemGroup)item).getSelectItems();  
     			for (int i = 0; i < items.length; i++) {
 					processItems.add(items[i]);
 				}
     			
     		  } else {
     			  processItems.add(item);
     		  }
     		  
     	  }
     	
     	  for (SelectItem selectItem: selectItemsForSelectedList) {
               processItems.remove(selectItem);
           }
           
     	  return processItems;
       	
     }
 
     protected List <SelectItem> getSelectItemsList(FacesContext context, UIComponent component) {
     	return SelectUtils.getSelectItems(context, component);
     }
 
     public PickListRenderer() {
     	super();
     }
 
     public void decode(FacesContext context, UIComponent component) {
 
 		UIPickList picklist = (UIPickList) component;
 		if (!(picklist instanceof EditableValueHolder)) {
 		    throw new IllegalArgumentException("Component " + picklist.getClientId(context) + " is not an EditableValueHolder");
 		}
 	
 		String hiddenClientId = picklist.getClientId(context) + HIDDEN_SUFFIX;
 		Map <String, String> paramMap = context.getExternalContext().getRequestParameterMap();
 	
 		if (picklist.isDisabled()) {
 		    return;
 		}
 	
 		String value = paramMap.get(hiddenClientId);
 		if (value != null) {
 		    if (value.trim().equals("")) {
 		    	
 		    	((EditableValueHolder) picklist).setSubmittedValue(new String[] {});
 		    
 		    } else {
 		    	String[] reqValues = value.split(",");
 		    	((EditableValueHolder) picklist).setSubmittedValue(reqValues);
 		    }
 		    
 		} else {
 		    ((EditableValueHolder) picklist).setSubmittedValue(new String[] {});
 		}
 		
     }
 
     private static boolean isTrue(Object obj) {
 		
     	if (!(obj instanceof Boolean)) {
 		    return false;
 		}
 		
 		return ((Boolean) obj).booleanValue();
 		
     }
 
     @Override
     public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
     	
     	Object convertedValue = null;
     	
     	if(component instanceof UIPickList) {
     		UIPickList pickList = (UIPickList)component;
     		convertedValue = SelectUtils.getConvertedUISelectManyValue(context, pickList, (String[]) submittedValue); 
     	}
     	
       	return convertedValue;
     }
     
     private void encodeRows(FacesContext context, UIPickList pickList, boolean source, ListStateFlags flags) throws IOException {
     	
     	List <SelectItem> selectItemsList = SelectUtils.getSelectItems(context, pickList);
 
     	Converter converter = pickList.getConverter();
     	
 		List <Object> values = getValuesList(pickList); // PickListUtils.getValuesList(context, pickList, converter);
 		
 		List <SelectItem> selectItemsForSelectedValues = selectItemsForSelectedList(context, pickList, selectItemsList, values);
 		List <SelectItem> selectItemsForAvailableList = selectItemsForAvailableList(context, pickList, selectItemsList, selectItemsForSelectedValues);
 		
 		flags.isSelectedList = !selectItemsForSelectedValues.isEmpty();
 		flags.isAvailableList = !selectItemsForAvailableList.isEmpty();
 	
 		List <SelectItem> selectItemList = null;
 		
 		if (source) {
 		    selectItemList = selectItemsForAvailableList;
 		} else {
 		    selectItemList = selectItemsForSelectedValues;
 		}
 	
 		if(selectItemList != null) {
 			
 			SelectItem [] itemsList =  selectItemList.toArray(new SelectItem[selectItemList.size()]);
 			
 			for (int i = 0; i < itemsList.length; i++) {
 			    
 				SelectItem selectItem = itemsList[i];
 			   
 				if (selectItem instanceof SelectItemGroup) {
 					
 			    	SelectItem[] items = ((SelectItemGroup) selectItem).getSelectItems();
 					for (int j = 0; j < items.length; j++) {
 					    encodeItem(context, pickList, converter, items[j], source, "group:" + j);
 					}
 					
 			    } else {
 			    	
 			    	encodeItem(context, pickList, converter, selectItem, source, Integer.toString(i));
 			    	
 			    }
 				
 			}
 		
 		}
 		
     }
 
     public void encodeItem(FacesContext context, UIComponent component, Converter converter, SelectItem selectItem, boolean source, String suff)
 	    throws IOException {
 
     	ResponseWriter writer = context.getResponseWriter();
     	writer.startElement(HTML.TR_ELEMENT, component);
     	
     	String clientId = component.getClientId(context);
     	
     	if (source) {
     		clientId += ":source:";
     	}
     	
     	String id = clientId + ":" + suff;
 	
     	writer.writeAttribute("id", id, null);
 
 		StringBuffer rowClassName = new StringBuffer();
 		StringBuffer cellClassName = new StringBuffer();
 
 		if (source) {
 			
 			rowClassName.append("rich-picklist-source-row");
 			cellClassName.append("rich-picklist-source-cell");
 			
 		} else {
 			
 			rowClassName.append("rich-picklist-target-row");
 			cellClassName.append("rich-picklist-target-cell");
 			
 		}
 
 		writer.writeAttribute("class", rowClassName.toString(), null);
 		writer.startElement(HTML.td_ELEM, component);
 		writer.writeAttribute(HTML.class_ATTRIBUTE, cellClassName, null);
 		
 		Object width = component.getAttributes().get("width");
 		if (width != null) {
 			writer.writeAttribute("style", "width: " + HtmlUtil.qualifySize(width.toString()), null);
 		}
 
 		encodeSpacer(context, component, writer);
 
 		boolean escape = isTrue(component.getAttributes().get("escape"));
 		if (escape) {
 			writer.writeText(selectItem.getLabel(), null);
 		} else {
 			writer.write(selectItem.getLabel());
 		}
 		
 		String itemValue;
 		if (converter != null) {
 			itemValue = converter.getAsString(context, component, selectItem.getValue());
 		} else {
 			Object valueObject = selectItem.getValue();
 			itemValue = (valueObject != null ? valueObject.toString() : "");
 		}
 				
 		encodeItemValue(context, component, writer, id, itemValue);
 		
 		writer.endElement(HTML.td_ELEM);
 		writer.endElement(HTML.TR_ELEMENT);
 
     }
 
     public void encodeTargetRows(FacesContext context, UIPickList pickList, ListStateFlags flags) throws IOException {
     	encodeRows(context, pickList, false, flags);
     }
 
     public void encodeSourceRows(FacesContext context, UIPickList pickList, ListStateFlags flags) throws IOException {
     	encodeRows(context, pickList, true, flags);
     }
 
     private void encodeItemValue(FacesContext context, UIComponent component, ResponseWriter writer, String id, String itemValue) throws IOException { //rowKey = i
 
 		writer.startElement(HTML.INPUT_ELEM, component);
 		writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
		writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
 		writer.writeAttribute(HTML.NAME_ATTRIBUTE, id, null);
 
 		StringBuffer value = new StringBuffer();
 
 		value.append(itemValue);
 		
 		writer.writeAttribute(HTML.value_ATTRIBUTE, value.toString(), null);
 		writer.writeAttribute(HTML.id_ATTRIBUTE, id + "StateInput", null);
 		writer.endElement(HTML.INPUT_ELEM);
 		
     }
 
     protected void encodeSpacer(FacesContext context, UIComponent component, ResponseWriter writer) throws IOException {
 		writer.startElement(HTML.IMG_ELEMENT, component);
 		
 		writer.writeAttribute(HTML.src_ATTRIBUTE, getResource("/org/richfaces/renderkit/html/images/spacer.gif").getUri(context, null), null);
 		writer.writeAttribute(HTML.alt_ATTRIBUTE, " ", null);
 		writer.writeAttribute(HTML.style_ATTRIBUTE, "width:1px;height:1px;", null);
 		
 		writer.endElement(HTML.IMG_ELEMENT);
     }
 
     public void encodeHiddenField(FacesContext context, UIPickList pickList) throws IOException {
     
     	Converter converter = pickList.getConverter();
 		List <String> lookupList = new ArrayList<String>();
 		List <Object> values = getValuesList(pickList);
 
 		for (Object value: values) {
     		if(converter != null) {
         		lookupList.add(converter.getAsString(context, pickList, value));
         	} else {
         		lookupList.add(value != null ? value.toString() : "");
         	}
     	}
 
 		encodeHiddenField(context, pickList, lookupList);
     	
     }
 
     private void encodeHiddenField(FacesContext context, UIComponent component, List <String> lookupList) throws IOException {
 
     	String hiddenFieldCliendId = component.getClientId(context) + HIDDEN_SUFFIX;
     	
     	ResponseWriter writer = context.getResponseWriter();
 
     	writer.startElement(HTML.INPUT_ELEM, component);
     	writer.writeAttribute(HTML.TYPE_ATTR, "hidden", "type");
    	writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
     	writer.writeAttribute(HTML.id_ATTRIBUTE, hiddenFieldCliendId, "id");
     	writer.writeAttribute("name", hiddenFieldCliendId, null);
     	
     	StringBuffer sb = new StringBuffer();
     	
     	int n = 0;
 
     	for (Iterator <String> i = lookupList.iterator(); i.hasNext();) {
     		
     		if (n > 0) {
     			sb.append(",");
     		}
     	
     		String value = i.next();
     		sb.append(value);
     		n++;
     		
     	}
 
 		writer.writeAttribute(HTML.value_ATTRIBUTE, sb.toString(), null);
 		writer.endElement(HTML.INPUT_ELEM);
 		
     }
 
     protected Class<? extends UIComponent> getComponentClass() {
     	return UIPickList.class;
     }
 
     public String getAsEventHandler(FacesContext context, UIComponent component, String attributeName) {
 		
     	String event = (String) component.getAttributes().get(attributeName);
 		ScriptString result = JSReference.NULL;
 
 		if (event != null) {
 	    
 			event = event.trim();
 		
 			if (event.length() != 0) {
 				
 		    	JSFunctionDefinition function = new JSFunctionDefinition();
 				function.addParameter("event");
 				function.addToBody(event);
 				
 				result = function;
 		    }
 			
 		}
 
 		return ScriptUtils.toScript(result);
 		
     }
 
     public String getColumnClassesAsJSArray(FacesContext context, UIComponent component) {
     	return ScriptUtils.toScript(getClassesAsList(context, component, "columnClasses"));
     }
 
     public String getRowClassesAsJSArray(FacesContext context, UIComponent component) {
     	return ScriptUtils.toScript(getClassesAsList(context, component, "rowClasses"));
     }
 
     protected List <String> getClassesAsList(FacesContext context, UIComponent component, String attr) {
     	String value = (String) ((UIComponent) component).getAttributes().get(attr);
     	
     	if (value != null && (value.length() != 0)) {
     		return Arrays.asList(value.split(","));
     	}
     	
     	return null;
     }
 
     public void encodePickListControlsFacets(FacesContext context, UIComponent component, ListStateFlags listStateFlags) throws IOException {
     	boolean needsStrut = true;
     	
     	String clientId = component.getClientId(context);
     	boolean enable = false;
     	
     	ResponseWriter writer = context.getResponseWriter();
     	boolean componentDisabled = ((UIPickList) component).isDisabled();
 	
     	for (int i = 0; i < SHUTTLE_HELPERS.length; i++) {
     		//Conditionally render the control based on the return value of the control's corresponding helper.isRendered method
     		if (SHUTTLE_HELPERS[i].isRendered(context, component)){
     			needsStrut = false;
     			
 	    		OrderingComponentRendererBase.ControlsHelper helper = SHUTTLE_HELPERS[i];
 	    		
 	    		boolean isDisabled = helper.getButtonStyleClass().equals(PickListControlsHelper.DISABLED_STYLE_PREF);
 	    		
 	    		if (helper.getBundlePropertyName().equals(PickListControlsHelper.BUNDLE_REMOVE_ALL_LABEL)) {
 	    			
 	    			enable = listStateFlags.isSelectedList;
 	    			enable = (enable != isDisabled) || (isDisabled && componentDisabled) ? true : false;
 	    			
 	    		} else if (helper.getBundlePropertyName().equals(PickListControlsHelper.BUNDLE_COPY_ALL_LABEL)) {
 	    			
 	    			enable = listStateFlags.isAvailableList;
 	    			enable = (enable != isDisabled) || (isDisabled && componentDisabled) ? true : false;
 	    			
 	    		} else {
 	    			
 	    			if (helper.getButtonStyleClass().equals(PickListControlsHelper.DISABLED_STYLE_PREF)) {
 	    				enable = true;
 	    			} else {
 	    				enable = false;
 	    			}
 	    			
 	    		}
     		
     			encodeControlFacet(context, component, SHUTTLE_HELPERS[i], clientId, writer, enable, "rich-list-picklist-button", " rich-picklist-control");
     		}
     	}
     	
     	if (needsStrut) {
     		writer.startElement(HTML.SPAN_ELEM, component);
     		writer.endElement(HTML.SPAN_ELEM);
     	}
     }
 
     protected void encodeControlFacet(FacesContext context, UIComponent component, OrderingComponentRendererBase.ControlsHelper helper, String clientId,
     		ResponseWriter writer, boolean enabled, String baseStyle, String baseControlStyle) throws IOException {
     	
     	renderDefaultControl(context, component, writer, helper, clientId, enabled, baseStyle, baseControlStyle);
     	
     }
 
     protected ClassLoader getCurrentLoader(Object fallbackClass) {
         
     	ClassLoader loader = Thread.currentThread().getContextClassLoader();
         
     	if (loader == null) {
             loader = fallbackClass.getClass().getClassLoader();
         }
     	
         return loader;
         
     }
     
     protected String findLocalisedLabel(FacesContext context, String propertyId, String bundleName) {
     	String label = null;
     	Locale locale = null;
     	String userBundleName = null;
     	ResourceBundle bundle = null;
 
     	UIViewRoot viewRoot = context.getViewRoot();
     	if ( viewRoot != null) {
     		locale = viewRoot.getLocale(); 
     	} else {
     		locale = Locale.getDefault();
     	}
 	
     	if(locale != null) {
     	
     		try {
     		
     			if( null != (userBundleName = context.getApplication().getMessageBundle())) {
     				bundle = ResourceBundle.getBundle(userBundleName,locale, getCurrentLoader(userBundleName));
     			
     				if (bundle != null) {
     					label = bundle.getString(propertyId);
     				}    
     			}
     			
     		} catch (MissingResourceException e) {
     			
     			if (logger.isDebugEnabled()) {
 					logger.debug("Can't find bundle properties file " + userBundleName + " " + locale.getLanguage() + " " + locale.getCountry()) ;
     			}
     			
     		} 
     		
 		if(label == null && bundleName != null) {
 		
 			try {
 			
 				bundle = ResourceBundle.getBundle(bundleName ,locale, getCurrentLoader(bundleName));
 				if (bundle != null) { 
 				    label = bundle.getString(propertyId);
 				}
 				
 				} catch (MissingResourceException e) {
 				
 			    	if (logger.isDebugEnabled()) {
 						logger.debug("Can't find bundle properties file " + bundleName + " " + locale.getLanguage() + " " + locale.getCountry()) ;
 					}
 	
 			    }
 			}
 		
     	}	
 	    	
     	return label; 
     }
     
     protected void renderDefaultControl(FacesContext context, UIComponent component, ResponseWriter writer, 
     		OrderingComponentRendererBase.ControlsHelper helper, String clientId, boolean enabled, String baseStyle,
     		String baseControlStyle) throws IOException {
 	
     	UIComponent facet = component.getFacet(getAltAttribbute(helper));
     	
     	boolean useFacet = (facet != null && facet.isRendered());
 
     	String customEvent = null;
     	
     	Map<String,Object> attributes = component.getAttributes();
 
     	if (helper.customEvent != null) {
     		customEvent = (String) attributes.get(helper.customEvent);
     	}
 
 		String styleFromAttribute = (String) attributes.get(helper.styleFromAttribute);
 		String baseStyleLight = baseStyle.concat("-light");
 		String baseStylePress = baseStyle.concat("-press");
 		String currentStyle = baseControlStyle + helper.getStyleClassName();
 
 		if (styleFromAttribute != null) {
 		    currentStyle = styleFromAttribute.concat(currentStyle);
 		}
 		
 		String controlId = clientId + helper.getIdSuffix();
 		writer.startElement(HTML.DIV_ELEM, component);
 		writer.writeAttribute(HTML.id_ATTRIBUTE, controlId, null); // FIXME:
 		writer.writeAttribute(HTML.class_ATTRIBUTE, currentStyle, null);
 		
 		String style = enabled ? "display:block;" : "display:none;"; 
 		
 		writer.writeAttribute(HTML.style_ATTRIBUTE, style, null);
 
 		if (!useFacet) {
 			
 			writer.startElement(HTML.DIV_ELEM, component);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle + helper.getButtonStyleClass(), null);
 
 			if (helper.enable) {
 				writer.writeAttribute(HTML.onmouseover_ATTRIBUTE, "this.className='" + baseStyleLight + "'", null);
 				writer.writeAttribute(HTML.onmousedown_ATTRIBUTE, "this.className='" + baseStylePress + "'", null);
 				writer.writeAttribute(HTML.onmouseup_ATTRIBUTE, "this.className='" + baseStyle + "'", null);
 				writer.writeAttribute(HTML.onmouseout_ATTRIBUTE, "this.className='" + baseStyle + "'", null);
 			}
 
 			if (!helper.enable) {
 				//writer.writeAttribute(HTML.DISABLED_ATTR, "disabled", null);
 				//writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle + "-a-disabled", null);
 				//writer.startElement(HTML.a_ELEMENT, component);
 			} else {
 				writer.startElement(HTML.a_ELEMENT, component);
 				writer.writeAttribute(HTML.id_ATTRIBUTE, controlId + "link", null); // FIXME:
 				writer.writeAttribute(HTML.onclick_ATTRIBUTE, "return false;", null);
 		    
 				writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle + "-selection", null);
 				writer.writeAttribute(HTML.onblur_ATTRIBUTE, "Richfaces.Control.onblur(this);", null);
 				writer.writeAttribute(HTML.onfocus_ATTRIBUTE, "Richfaces.Control.onfocus(this);", null);
 			}
 
 			writer.startElement(HTML.DIV_ELEM, component);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle + "-content", null);
 		}
 
 		if (customEvent != null) {
 		    writer.writeAttribute(HTML.onclick_ATTRIBUTE, customEvent, null);
 		}
 
 		if (useFacet) {
 		    renderChild(context, facet);
 		} else {
 		    
 			writer.startElement(HTML.IMG_ELEMENT, component);
 		    writer.writeAttribute(HTML.class_ATTRIBUTE, "rich-picklist-control-img", null);
 		    writer.writeAttribute(HTML.alt_ATTRIBUTE, component.getAttributes().get(helper.getTitle()), null);
 		    writer.writeAttribute(HTML.src_ATTRIBUTE, getResource(helper.getImageURI()).getUri(context, null), null);
 		    writer.endElement(HTML.IMG_ELEMENT);
 
 		    if (getUtils().isBooleanAttribute(component, SHOW_LABELS_ATTRIBUTE_NAME)) {
 		    	String label = (String) attributes.get(helper.getLabelAttributeName());
 		    	if (label == null || label.equals("")) {
 		    		label = findLocalisedLabel(context, helper.getBundlePropertyName(), MESSAGE_BUNDLE_NAME);
 		    	}
 
 		    	if (label == null || label.equals("")) {
 		    		label = helper.getDefaultText();
 		    	}
 		    	
 		    	writer.write(label);
 		    }
 		}
 
 		if (!useFacet) {
 		    writer.endElement(HTML.DIV_ELEM);
 		   
 		    if (helper.enable) {
 		    	writer.endElement(HTML.a_ELEMENT);
 		    }
 		    
 		    writer.endElement(HTML.DIV_ELEM);
 		}
 			
 		writer.endElement(HTML.DIV_ELEM);
 	}
 
 	private String getAltAttribbute(
 			OrderingComponentRendererBase.ControlsHelper helper) {
 			
 		return helper.getFacetName() != null ? helper.getFacetName() : " ";
 		
 	}
     
     public void reRenderScript(FacesContext context, UIComponent component) throws IOException {
     	
     	AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
 	
     	Set <String> areas = ajaxContext.getAjaxRenderedAreas();
     	String clientId = component.getClientId(context);
 	
     	if (ajaxContext.isAjaxRequest() && areas.contains(clientId)){
     		areas.add(clientId + "script");
     	}
     	
     }
     
 }
