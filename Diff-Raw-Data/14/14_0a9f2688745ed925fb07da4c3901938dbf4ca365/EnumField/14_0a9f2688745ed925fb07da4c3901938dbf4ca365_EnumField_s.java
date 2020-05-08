 /*---
    Copyright 2006-2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 /*
  * 
  */
 package com.googlecode.wicketwebbeans.fields;
 
 import java.io.Serializable;
 import java.util.List;
 
 
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.string.Strings;
 
 import com.googlecode.wicketwebbeans.model.ElementMetaData;
 import com.googlecode.wicketwebbeans.model.NonJavaEnum;
 
 
 /**
  * A field for enumerated types. Presents the values as a drop-down list.
  * Accepts a parameter of "default" indicating the default choice if the current selection is null.
  * 
  * @author Dan Syrstad
  */
 abstract public class EnumField extends AbstractField
 {
     private DropDownChoice choice;
     private IChoiceRenderer choiceRenderer;
     
     /**
      * Construct a new EnumField.
      *
      * @param id the Wicket id for the editor.
      * @param model the model.
      * @param metaData the meta data for the property.
      * @param viewOnly true if the component should be view-only.
      * @param values a List of values to be selected from. The element's toString() is used to 
      *  produce the value displayed to the user.
      */
     public EnumField(String id, IModel model, ElementMetaData metaData, boolean viewOnly, List<?> values)
     {
         this(id, model, metaData, viewOnly, new Model((Serializable)values));
     }
     
     /**
      * Construct a new EnumField.
      *
      * @param id the Wicket id for the editor.
      * @param model the model.
      * @param metaData the meta data for the property.
      * @param viewOnly true if the component should be view-only.
      * @param valueModel an IModel that returns a List of values to be selected from. The element's toString() is used to 
      *  produce the value displayed to the user.
      */
     public EnumField(String id, IModel model, ElementMetaData metaData, boolean viewOnly, IModel valueModel)
     {
         this(id, model, metaData, viewOnly, valueModel, null);
     }
     
     /**
      * Construct a new EnumField.
      *
      * @param id the Wicket id for the editor.
      * @param model the model.
      * @param metaData the meta data for the property.
      * @param viewOnly true if the component should be view-only.
      * @param valueModel an IModel that returns a List of values to be selected from. The element's toString() is used to 
      *  produce the value displayed to the user.
      * @param choiceRenderer a renderer that produces the value displayed to the user. May be null to use the default rendering.
      */
     public EnumField(String id, IModel model, ElementMetaData metaData, boolean viewOnly, IModel valueModel, final IChoiceRenderer choiceRenderer)
     {
         super(id, model, metaData, viewOnly);
         
         this.choiceRenderer = choiceRenderer;
         Fragment fragment;
         if (viewOnly) {
             fragment = new Fragment("frag", "viewer");
             fragment.add(new LabelWithMinSize("component", model) {
                 private static final long serialVersionUID = 1L;
                 @Override
                 protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
                 {
                     if (choiceRenderer == null) {
                         super.onComponentTagBody(markupStream, openTag);
                     }
                     else {
                         String value = choiceRenderer.getDisplayValue(getDefaultModelObject()).toString();
                         if (Strings.isEmpty(value)) {
                             value = "&nbsp;";
                             setEscapeModelStrings(false);
                         }
                         replaceComponentTagBody(markupStream, openTag, value);
                     }
                 }
             });
         }
         else {
             fragment = new Fragment("frag", "editor");
             choice = new DropDownChoice("component", model, valueModel, choiceRenderer);
             // Always allow the null choice.
             choice.setNullValid(true);
             fragment.add(choice);
 
             initDefault();
         }
 
         add(fragment);
     }
     
     /**
      * Sets the list of possible values. 
      *
      * @param values the values.
      */
     public void setValues(List<?> values)
     {
         setValuesModel( new Model((Serializable)values) );
     }
     
     /**
      * Sets the model of the list of possible values. This model should return a List. 
      *
      * @param valuesModel the values model.
      */
     public void setValuesModel(IModel valuesModel)
     {
         choice.setChoices(valuesModel);
         initDefault();
     }
     
     private void initDefault()
     {
         // Handle default selection if no model is provided
         if (choice.getModelObject() == null) {
            String defaultChoice = getElementMetaData().getParameter("default");
            if (defaultChoice != null)
                setupDefault(defaultChoice);
         }
     }
     
     private void setupDefault(String defaultChoice)
     {
         List<?> values = choice.getChoices();
         if (values == null || values.isEmpty()) {
             return;
         }
 
         Object firstValue = values.get(0);
 
         // Figure out what type of enum were dealing with
         boolean isJavaEnum = false;
         boolean isNonJavaEnum = false;
         boolean useRenderer = false;
         if (firstValue instanceof Enum) {
             isJavaEnum = true;
         }
         else if (firstValue instanceof NonJavaEnum) {
             isNonJavaEnum = true;
         }
         else if (choiceRenderer != null) {
             useRenderer = true;
         }
         else {
             throw new RuntimeException("Unexpected choice type. Must be a Java language Enum, a custom enumeration that implements NonJavaEnum, or must supply an IChoiceRenderer");
         }
 
         // Iterate over the values and find the one that matches the default choice
         for (int i = 0; i < values.size(); i++) {
             Object value = values.get(i);
             String valueStr = "";
             if (isJavaEnum) {
                 valueStr = ((Enum<?>)value).name();
             }
             else if (isNonJavaEnum) {
                 valueStr = ((NonJavaEnum)value).name();
             }
             else if (useRenderer) {
                 valueStr = choiceRenderer.getIdValue(value, i);
             }
             
             if (defaultChoice.equals(valueStr)) {
                 choice.setModelObject(value);
                 break;
             }
         }
     }
 }
