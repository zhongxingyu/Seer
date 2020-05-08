 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.components.input;
 
 import com.flexive.faces.FxJsfComponentUtils;
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.model.FxJSFSelectItem;
 import com.flexive.shared.ObjectWithColor;
 import com.flexive.shared.SelectableObjectWithLabel;
 import com.flexive.shared.SelectableObjectWithName;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.el.ValueExpression;
 import javax.faces.component.*;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.model.SelectItem;
 import javax.faces.model.SelectItemGroup;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A special renderer for SelectOneListbox and SelectManyListbox based on MenuRenderer from the JSF RI 1.2
  * Changed behaviour: if readonly or disabled on the text is rendered and colors are taken from
  * the SelectItem's if their value class extends ObjectWithColor or a style is provided in FxJSFSelectItem's
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @see com.flexive.shared.ObjectWithColor
  */
 public class FxSelectRenderer extends Renderer {
 
     private static final Log LOG = LogFactory.getLog(FxSelectRenderer.class);
 
     /**
      * constant if no value is assigned
      */
     public static final Object NO_VALUE = "";
 
     /**
      * Attributed to be passed through
      */
     private final static String[] ATTRIBUTES = {
             "disabled",
             "readonly",
             "accesskey",
             "dir",
             "lang",
             "onblur",
             "onchange",
             "onclick",
             "ondblclick",
             "onfocus",
             "onkeydown",
             "onkeypress",
             "onkeyup",
             "onmousedown",
             "onmousemove",
             "onmouseout",
             "onmouseover",
             "onmouseup",
             "onselect",
             "style",
             "tabindex",
             "title"
     };
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String convertClientId(FacesContext context, String clientId) {
         return clientId;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void decode(FacesContext context, UIComponent component) {
         if (!isEditable(component)) {
             //dont decode a readonly or disabled listbox
             return;
         }
         String clientId = component.getClientId(context);
         if (component instanceof UISelectMany) {
             Map<String, String[]> requestParameterValuesMap =
                     context.getExternalContext().
                             getRequestParameterValuesMap();
             if (requestParameterValuesMap.containsKey(clientId)) {
                 String newValues[] = requestParameterValuesMap.
                         get(clientId);
                 ((UIInput) component).setSubmittedValue(newValues);
             } else {
                 // Use the empty array, not null, to distinguish
                 // between an deselected UISelectMany and a disabled one
                 ((UIInput) component).setSubmittedValue(new String[0]);
             }
         } else {
             Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
             if (requestParameterMap.containsKey(clientId)) {
                 String newValue = requestParameterMap.get(clientId);
                 ((UIInput) component).setSubmittedValue(newValue);
             } else {
                 // there is no value, but this is different from a null value.
                 ((UIInput) component).setSubmittedValue(NO_VALUE);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         //do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         if (isEditable(component))
             renderSelect(context, component);
         else
             renderText(context, component);
     }
 
     /**
      * Convert a SelectOne value to the correct value class
      *
      * @param context     faces context
      * @param uiSelectOne the select one component
      * @param newValue    the value to convert
      * @return converted value
      * @throws ConverterException on errors
      */
     public Object convertSelectOneValue(FacesContext context, UISelectOne uiSelectOne, String newValue) throws ConverterException {
         if (NO_VALUE.equals(newValue) || newValue == null)
             return null;
         return FxJsfComponentUtils.getConvertedValue(context, uiSelectOne, newValue);
     }
 
     /**
      * Convert SelectManys value to the correct value classes
      * @param context faces context
      * @param uiSelectMany the select many component
      * @param newValues the new values to convert
      * @return converted values
      * @throws ConverterException on errors
      */
     public Object convertSelectManyValue(FacesContext context, UISelectMany uiSelectMany, String[] newValues)
             throws ConverterException {
         // if we have no local value, try to get the valueExpression.
         ValueExpression valueExpression = uiSelectMany.getValueExpression("value");
 
         Object result = newValues; // default case, set local value
         boolean throwException = false;
 
         // If we have a ValueExpression
         if (null != valueExpression) {
             Class modelType = valueExpression.getType(context.getELContext());
             // Does the valueExpression resolve properly to something with
             // a type?
             if (modelType != null)
                 result = convertSelectManyValuesForModel(context, uiSelectMany, modelType, newValues);
             // If it could not be converted, as a fall back try the type of
             // the valueExpression's current value covering some edge cases such
             // as where the current value came from a Map.
             if (result == null) {
                 Object value = valueExpression.getValue(context.getELContext());
                 if (value != null)
                     result = convertSelectManyValuesForModel(context, uiSelectMany, value.getClass(), newValues);
             }
             if (result == null)
                 throwException = true;
         } else {
             // No ValueExpression, just use Object array.
             result = convertSelectManyValues(context, uiSelectMany, Object[].class, newValues);
         }
         if (throwException) {
             StringBuffer values = new StringBuffer();
             if (null != newValues) {
                 for (int i = 0; i < newValues.length; i++) {
                     if (i == 0)
                         values.append(newValues[i]);
                     else
                         values.append(' ').append(newValues[i]);
                 }
             }
             throw new ConverterException("Error converting expression [" + valueExpression.getExpressionString() + "] of " + String.valueOf(values));
         }
         return result;
     }
 
     /*
     * Converts the provided string array and places them into the correct provided model type.
     */
     protected Object convertSelectManyValuesForModel(FacesContext context, UISelectMany uiSelectMany, Class modelType, String[] newValues) {
         Object result = null;
         if (modelType.isArray())
             result = convertSelectManyValues(context, uiSelectMany, modelType, newValues);
         else if (List.class.isAssignableFrom(modelType)) {
             Object[] values = (Object[]) convertSelectManyValues(context, uiSelectMany, Object[].class, newValues);
             // perform a manual copy as the Array returned from
             // Arrays.asList() isn't mutable.  It seems a waste
             // to also call Collections.addAll(Arrays.asList())
             List<Object> l = new ArrayList<Object>(values.length);
             //noinspection ManualArrayToCollectionCopy
             for (Object v : values)
                 l.add(v);
             result = l;
         }
         return result;
     }
 
     /**
      * Convert select many values to given array class
      * @param context faces context
      * @param uiSelectMany select many component
      * @param arrayClass the array class
      * @param newValues new values to convert
      * @return converted values
      * @throws ConverterException on errors
      */
     protected Object convertSelectManyValues(FacesContext context, UISelectMany uiSelectMany, Class arrayClass, String[] newValues)
             throws ConverterException {
 
         Object result;
         Converter converter;
         int len = (null != newValues ? newValues.length : 0);
 
         Class elementType = arrayClass.getComponentType();
 
         // Optimization: If the elementType is String, we don't need
         // conversion.  Just return newValues.
         if (elementType.equals(String.class))
             return newValues;
 
         try {
             result = Array.newInstance(elementType, len);
         } catch (Exception e) {
             throw new ConverterException(e);
         }
 
         // bail out now if we have no new values, returning our
         // oh-so-useful zero-length array.
         if (null == newValues)
             return result;
 
         // obtain a converter.
 
         // attached converter takes priority
         if (null == (converter = uiSelectMany.getConverter())) {
             // Otherwise, look for a by-type converter
             if (null == (converter = FxJsfComponentUtils.getConverterForClass(elementType, context))) {
                 // if that fails, and the attached values are of Object type,
                 // we don't need conversion.
                 if (elementType.equals(Object.class))
                     return newValues;
                 StringBuffer valueStr = new StringBuffer();
                 for (int i = 0; i < len; i++) {
                     if (i == 0)
                         valueStr.append(newValues[i]);
                     else
                         valueStr.append(' ').append(newValues[i]);
                 }
                 throw new ConverterException("Could not get a converter for " + String.valueOf(valueStr));
             }
         }
 
         if (elementType.isPrimitive()) {
             for (int i = 0; i < len; i++) {
                 if (elementType.equals(Boolean.TYPE)) {
                     Array.setBoolean(result, i, ((Boolean) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Byte.TYPE)) {
                     Array.setByte(result, i, ((Byte) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Double.TYPE)) {
                     Array.setDouble(result, i, ((Double) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Float.TYPE)) {
                     Array.setFloat(result, i, ((Float) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Integer.TYPE)) {
                     Array.setInt(result, i, ((Integer) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Character.TYPE)) {
                     Array.setChar(result, i, ((Character) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Short.TYPE)) {
                     Array.setShort(result, i, ((Short) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 } else if (elementType.equals(Long.TYPE)) {
                     Array.setLong(result, i, ((Long) converter.getAsObject(context, uiSelectMany, newValues[i])));
                 }
             }
         } else {
             for (int i = 0; i < len; i++)
                 Array.set(result, i, converter.getAsObject(context, uiSelectMany, newValues[i]));
         }
         return result;
     }
 
 
     /**
      * Should the component be rendered as editable?
      * Checks the disabled and reaonly attributes.
      *
      * @param component the component to check
      * @return editable
      */
     protected boolean isEditable(UIComponent component) {
         return !(Boolean.valueOf(String.valueOf(component.getAttributes().get("disabled"))) ||
                 Boolean.valueOf(String.valueOf(component.getAttributes().get("readonly"))));
     }
 
     /**
      * Render the component as static text (when readonly or disabled)
      *
      * @param context   faces context
      * @param component the component
      * @throws IOException on errors
      */
     protected void renderText(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Object[] items = getCurrentSelectedValues(component);
         List<SelectItem> selectedItems = FxJsfComponentUtils.getSelectItems(context, component);
         for (Object item : items) {
             writer.startElement("div", component);
             for (SelectItem currItem : selectedItems) {
                 if (currItem.getValue().equals(item)) {
                     item = currItem;
                     break;
                 }
             }
 
             String color = null;
             if (item instanceof FxJSFSelectItem) {
                 writer.writeAttribute("style", ((FxJSFSelectItem) item).getStyle(), "style");
             } else if (item instanceof ObjectWithColor) {
                 ObjectWithColor oc = (ObjectWithColor) item;
                 if (!StringUtils.isEmpty(oc.getColor()))
                     color = oc.getColor();
             } else if (item instanceof SelectItem) {
                 if (((SelectItem) item).getValue() instanceof ObjectWithColor) {
                     ObjectWithColor oc = (ObjectWithColor) ((SelectItem) item).getValue();
                     if (!StringUtils.isEmpty(oc.getColor()))
                         color = oc.getColor();
                 }
             }
             if (color != null)
                 writer.writeAttribute("style", "color:" + color, "style");
 
             if (item instanceof SelectableObjectWithLabel)
                 writer.write(((SelectableObjectWithLabel) item).getLabel().getBestTranslation());
             else if (item instanceof SelectableObjectWithName)
                 writer.write(((SelectableObjectWithName) item).getName());
             else if (item instanceof SelectItem)
                 writer.write(((SelectItem) item).getLabel());
             else
                 writer.write(String.valueOf(item)); //last exit ...
             writer.endElement("div");
         }
 
     }
 
     /**
      * Render the component as select list box
      *
      * @param context   faces context
      * @param component the component
      * @throws IOException on errors
      */
     protected void renderSelect(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement("select", component);
         String id;
         if (null != (id = component.getId()) && !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
             //noinspection UnusedAssignment
             writer.writeAttribute("id", id = component.getClientId(context), "id");
 
         writer.writeAttribute("name", component.getClientId(context), "clientId");
         // render styleClass attribute if present.
         String styleClass;
         if (null != (styleClass = (String) component.getAttributes().get("styleClass"))) {
             writer.writeAttribute("class", styleClass, "styleClass");
         }
 
         if (component instanceof UISelectMany)
             writer.writeAttribute("multiple", true, "multiple");
 
         List<SelectItem> items = FxJsfComponentUtils.getSelectItems(context, component);
 
         // If "size" is *not* set explicitly, we have to default it correctly
         Integer size = (Integer) component.getAttributes().get("size");
         if (size == null || size == Integer.MIN_VALUE) {
             // Determine how many option(s) we need to render, and update
             // the component's "size" attribute accordingly;  The "size"
             // attribute will be rendered as one of the "pass thru" attributes
             int itemCount;
             if ("javax.faces.Listbox".equals(component.getRendererType()))
                 itemCount = 1; //only 1 item if we are a listbox
             else
                 itemCount = getOptionNumber(items);
             size = itemCount;
         }
         writer.writeAttribute("size", size, "size");
 
         //render the components default attributes if present
         Map attrMap = component.getAttributes();
         for (String att : ATTRIBUTES)
             if (attrMap.get(att) != null)
                 writer.writeAttribute(att, attrMap.get(att), att);
 
         //render each option
         renderOptions(context, component, items);
         writer.endElement("select");
 
     }
 
     /**
      * Render all present options
      *
      * @param context   faces context
      * @param component the component
      * @param items     all items to render
      * @throws IOException on errors
      */
     protected void renderOptions(FacesContext context, UIComponent component, List<SelectItem> items) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         assert (writer != null);
 
         Converter converter = null;
         if (component instanceof ValueHolder)
             converter = ((ValueHolder) component).getConverter();
 
         if (!items.isEmpty()) {
             Object currentSelections = getCurrentSelectedValues(component);
             Object[] submittedValues = getSubmittedSelectedValues(component);
             for (SelectItem item : items) {
                 if (item instanceof SelectItemGroup) {
                     // render OPTGROUP
                     writer.startElement("optgroup", component);
                     writer.writeAttribute("label", item.getLabel(), "label");
 
                     // if the component is disabled, "disabled" attribute would be rendered
                     // on "select" tag, so don't render "disabled" on every option.
                     boolean componentDisabled = Boolean.TRUE.equals(component.getAttributes().get("disabled"));
                     if ((!componentDisabled) && item.isDisabled())
                         writer.writeAttribute("disabled", true, "disabled");
 
                     // render options of this group.
                     SelectItem[] itemsArray = ((SelectItemGroup) item).getSelectItems();
                     for (SelectItem currentOption : itemsArray)
                         renderOption(context, component, converter, currentOption, currentSelections, submittedValues);
                     writer.endElement("optgroup");
                 } else
                     renderOption(context, component, converter, item, currentSelections, submittedValues);
             }
         }
     }
 
     /**
      * Render a single option
      *
      * @param context           faces context
      * @param component         the current component
      * @param converter         the converter
      * @param curItem           the item to render
      * @param currentSelections the current selections
      * @param submittedValues   all submitted values
      * @throws IOException on errors
      */
     protected void renderOption(FacesContext context, UIComponent component, Converter converter, SelectItem curItem,
                                 Object currentSelections, Object[] submittedValues) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.writeText("\t", component, null);
         writer.startElement("option", component);
 
         String valueString = getFormattedValue(context, component, curItem.getValue(), converter);
         writer.writeAttribute("value", valueString, "value");
         if (curItem instanceof FxJSFSelectItem) {
             //apply the style attribute if it is available
             writer.writeAttribute("style", ((FxJSFSelectItem) curItem).getStyle(), null);
         } else if (curItem.getValue() instanceof ObjectWithColor) {
             //apply the color to the style attribute
             String color = ((ObjectWithColor) (curItem.getValue())).getColor();
             if (!StringUtils.isEmpty(color))
                 writer.writeAttribute("style", "color:" + color, null);
         }
 
         Object valuesArray;
         Object itemValue;
         boolean containsValue;
         if (submittedValues != null) {
             containsValue = containsaValue(submittedValues);
             if (containsValue) {
                 valuesArray = submittedValues;
                 itemValue = valueString;
             } else {
                 valuesArray = currentSelections;
                 itemValue = curItem.getValue();
             }
         } else {
             valuesArray = currentSelections;
             itemValue = curItem.getValue();
         }
 
         if (isSelected(context, component, itemValue, valuesArray, converter)) {
             writer.writeAttribute("selected", true, "selected");
         }
 
         Boolean disabledAttr = (Boolean) component.getAttributes().get("disabled");
         boolean componentDisabled = disabledAttr != null && disabledAttr.equals(Boolean.TRUE);
 
         // if the component is disabled, "disabled" attribute would be rendered
         // on "select" tag, so don't render "disabled" on every option.
         if ((!componentDisabled) && curItem.isDisabled())
             writer.writeAttribute("disabled", true, "disabled");
 
         String labelClass;
         if (componentDisabled || curItem.isDisabled())
             labelClass = (String) component.getAttributes().get("disabledClass");
         else
             labelClass = (String) component.getAttributes().get("enabledClass");
 
         if (labelClass != null)
             writer.writeAttribute("class", labelClass, "labelClass");
 
         if (curItem.isEscape()) {
             String label = curItem.getLabel();
             if (label == null)
                 label = valueString;
             writer.writeText(label, component, "label");
         } else
             writer.write(curItem.getLabel());
 
         writer.endElement("option");
         writer.writeText("\n", component, null);
     }
 
     /**
      * Get the values currently selected
      *
      * @param component the component
      * @return selected values
      */
     protected Object[] getCurrentSelectedValues(UIComponent component) {
         if (component instanceof UISelectMany) {
             UISelectMany select = (UISelectMany) component;
             Object value = select.getValue();
             if (value instanceof Collection)
                 return ((Collection) value).toArray();
             else if (value != null && !value.getClass().isArray())
                 LOG.warn("The UISelectMany value should be an array or a collection type, the actual type is " + value.getClass().getName());
 
             return (Object[]) value;
         }
         //select one
         UISelectOne select = (UISelectOne) component;
         Object val = select.getValue();
         if (val != null)
             return new Object[]{val};
        return null;
     }
 
     /**
      * Get the submitted values that are selected
      *
      * @param component the component
      * @return submitted values that are selected
      */
     protected Object[] getSubmittedSelectedValues(UIComponent component) {
         if (component instanceof UISelectMany) {
             UISelectMany select = (UISelectMany) component;
             return (Object[]) select.getSubmittedValue();
         }
 
         UISelectOne select = (UISelectOne) component;
         Object val = select.getSubmittedValue();
         if (val != null)
             return new Object[]{val};
         return null;
     }
 
     /**
      * Get the number of options to render depending on the type of the items (eg item groups require more items to be rendered)
      *
      * @param selectItems the select items to inspect
      * @return number of options to be rendered
      */
     protected int getOptionNumber(List<SelectItem> selectItems) {
         int itemCount = 0;
         if (!selectItems.isEmpty()) {
             for (Object selectItem : selectItems) {
                 itemCount++;
                 if (selectItem instanceof SelectItemGroup)
                     itemCount += ((SelectItemGroup) selectItem).getSelectItems().length;
             }
         }
         return itemCount;
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
         if (component instanceof UISelectMany)
             return convertSelectManyValue(context, ((UISelectMany) component), (String[]) submittedValue);
         else
             return convertSelectOneValue(context, ((UISelectOne) component), (String) submittedValue);
     }
 
     /**
      * Overloads getFormattedValue to take a advantage of a previously
      * obtained converter.
      *
      * @param context      the FacesContext for the current request
      * @param component    UIComponent of interest
      * @param currentValue the current value of <code>component</code>
      * @param converter    the component's converter
      * @return the currentValue after any associated Converter has been
      *         applied
      * @throws ConverterException if the value cannot be converted
      */
     protected String getFormattedValue(FacesContext context, UIComponent component, Object currentValue, Converter converter)
             throws ConverterException {
 
         // formatting is supported only for components that support
         // converting value attributes.
         if (!(component instanceof ValueHolder)) {
             if (currentValue != null)
                 return currentValue.toString();
             return null;
         }
 
         if (converter == null) {
             // If there is a converter attribute, use it to to ask application
             // instance for a converter with this identifer.
             converter = ((ValueHolder) component).getConverter();
         }
 
         if (converter == null) {
             // if value is null and no converter attribute is specified, then
             // return a zero length String.
             if (currentValue == null) {
                 return "";
             }
             // Do not look for "by-type" converters for Strings
             if (currentValue instanceof String) {
                 return (String) currentValue;
             }
 
             // if converter attribute set, try to acquire a converter
             // using its class type.
             try {
                 converter = FxJsfUtils.getApplication().createConverter(currentValue.getClass());
             } catch (Exception e) {
                 converter = null;
             }
 
             // if there is no default converter available for this identifier,
             // assume the model type to be String.
             if (converter == null) {
                 return currentValue.toString();
             }
         }
         return converter.getAsString(context, component, currentValue);
     }
 
     /**
      * Check if the given array contains an actual value
      *
      * @param valueArray value array
      * @return if an actual value is set in the array
      */
     protected boolean containsaValue(Object valueArray) {
         if (null != valueArray) {
             int len = Array.getLength(valueArray);
             for (int i = 0; i < len; i++) {
                 Object value = Array.get(valueArray, i);
                 if (value != null && !(value.equals(NO_VALUE))) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Check if an item is selected
      *
      * @param context    faces context
      * @param component  our component
      * @param itemValue  the value to check for selection
      * @param valueArray all values to compare against
      * @param converter  the converter
      * @return selected
      */
     protected boolean isSelected(FacesContext context, UIComponent component, Object itemValue, Object valueArray, Converter converter) {
         if (itemValue == null && valueArray == null)
             return true;
         if (null != valueArray) {
             if (!valueArray.getClass().isArray()) {
                 LOG.warn("valueArray is not an array, the actual type is " + valueArray.getClass());
                 return valueArray.equals(itemValue);
             }
             int len = Array.getLength(valueArray);
             for (int i = 0; i < len; i++) {
                 Object value = Array.get(valueArray, i);
                 if (value == null && itemValue == null) {
                     return true;
                 } else {
                     if ((value == null) ^ (itemValue == null))
                         continue;
 
                     Object compareValue;
                     if (converter == null) {
                         compareValue = FxJsfComponentUtils.coerceToModelType(context, itemValue, value.getClass());
                     } else {
                         compareValue = itemValue;
                         if (compareValue instanceof String && !(value instanceof String)) {
                             // type mismatch between the time and the value we're
                             // comparing.  Invoke the Converter.
                             compareValue = converter.getAsObject(context, component, (String) compareValue);
                         }
                     }
 
                     if (value.equals(compareValue))
                         return true; //selected
                 }
             }
         }
         return false;
     }
 }
