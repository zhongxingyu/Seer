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
 
 package org.icemobile.jsp.tags;
 
import org.icefaces.mobi.utils.HTML;

 import java.io.IOException;
 
 import static org.icemobile.util.HTML.*;
 
 /**
  *
  */
 public class CommandButtonTag extends BaseSimpleTag {
 
     // Default button types.
     public static final String BUTTON_TYPE_UNIMPORTANT = "unimportant";
     public static final String BUTTON_TYPE_IMPORTANT = "important";
     public static final String BUTTON_TYPE_ATTENTION = "attention";
     public static final String BUTTON_TYPE_BACK = "back";
 
     // button styles.
     public static final String BASE_STYLE_CLASS = "mobi-button";
     public static final String DISABLED_STYLE_CLASS = " mobi-button-dis";
     public static final String IMPORTANT_STYLE_CLASS = " mobi-button-important";
     public static final String UNIMPORTANT_STYLE_CLASS = " mobi-button-unimportant";
     public static final String BACK_STYLE_CLASS = " mobi-button-back";
     public static final String ATTENTION_STYLE_CLASS = " mobi-button-attention";
     public static final String SELECTED_STYLE_CLASS = " mobi-button-selected";
 
     private String id;
     private String name;
     private String style;
     private String styleClass;
     private String buttonType;
 
     private boolean disabled;
     private String value;
     private String type = "submit";
     private boolean selected;
 
     public void doTag() throws IOException {
 
         TagWriter writer = new TagWriter(getContext());
         StringBuilder baseClass = new StringBuilder(BASE_STYLE_CLASS);
         if (selected) {
             baseClass.append(SELECTED_STYLE_CLASS);
         }
         if (disabled) {
             baseClass.append(" ").append(DISABLED_STYLE_CLASS);
         }
         if( buttonType != null && !"".equals(buttonType)){
             if (BUTTON_TYPE_UNIMPORTANT.equals(buttonType)) {
                 baseClass.append(UNIMPORTANT_STYLE_CLASS);
             } else if (BUTTON_TYPE_BACK.equals(buttonType)) {
                 baseClass.append(BACK_STYLE_CLASS);
             } else if (BUTTON_TYPE_ATTENTION.equals(buttonType)) {
                 baseClass.append(ATTENTION_STYLE_CLASS);
             } else if (BUTTON_TYPE_IMPORTANT.equals(buttonType)) {
                 baseClass.append(IMPORTANT_STYLE_CLASS);
             } 
         }
         
         if (styleClass != null) {
             baseClass.append(" ").append(styleClass);
         }
         
         if (BUTTON_TYPE_BACK.equals(buttonType)){
            writer.startElement(HTML.DIV_ELEM);
             if( id != null ){
                writer.writeAttribute(HTML.ID_ATTR, id+"_ctr");
             }
            writer.writeAttribute(HTML.CLASS_ATTR, baseClass.toString());
             // should be auto base though
             if (style != null ) {
                writer.writeAttribute(HTML.STYLE_ATTR, style);
             }
            writer.startElement(HTML.SPAN_ELEM);
            writer.endElement(HTML.SPAN_ELEM);
         }
         
         writer.startElement(INPUT_ELEM);
         if (id != null && !"".equals(id)) {
             writer.writeAttribute(ID_ATTR, id);
         }
         if (name != null && !"".equals(name)) {
             writer.writeAttribute(NAME_ATTR,name);
         }
         // button type for styling purposes
         writer.writeAttribute(TYPE_ATTR, type);
         
         //style and class written to ctr div when back button
         if (!BUTTON_TYPE_BACK.equals(buttonType)){
            writer.writeAttribute(HTML.CLASS_ATTR, baseClass.toString());
             // should be auto base though
             if (style != null ) {
                writer.writeAttribute(HTML.STYLE_ATTR, style);
             }
         }
         
        
         if (isDisabled()) {
             writer.writeAttribute(DISABLED_ATTR,DISABLED_ATTR);
         }
 
         if (null != value) {
             writer.writeAttribute(VALUE_ATTR, value);
         }
         writer.endElement();
         
       //end ctr div for back button
         if (BUTTON_TYPE_BACK.equals(buttonType)){
             writer.startElement("b");
            writer.writeAttribute(HTML.CLASS_ATTR, "mobi-button-placeholder");
             if (value != null) {
                 writer.writeText(value);
             }
             writer.endElement("b");
            writer.endElement(HTML.DIV_ELEM);
         }
 
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getStyle() {
         return style;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
 
     public String getStyleClass() {
         return styleClass;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     public String getButtonType() {
         return buttonType;
     }
 
     public void setButtonType(String buttonType) {
         this.buttonType = buttonType;
     }
 
     public boolean isDisabled() {
         return disabled;
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
 
 }
