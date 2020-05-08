 /*
  * Copyright 2009-2011 Prime Technology.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.helix.mobile.component.formfield;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.helix.mobile.component.fieldlabel.FieldLabel;
 import org.helix.mobile.component.iconbutton.IconButton;
 import org.helix.mobile.component.pickitem.PickItem;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class FormFieldRenderer extends CoreRenderer {
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         FormField ffield = (FormField) component;
         writer.write("{");
         writer.write("'id' : '" + ffield.getName() + "',");
         writer.write("'name' : '" + ffield.getName() + "',");
         writer.write("'type' : '" + ffield.getType() + "',");
         if (ffield.getWidthMap() != null) {
             writer.write("'width' : " + ffield.getWidthMap() + ",");
         } else if (ffield.getWidth() != null) {
             writer.write("'width' : '" + ffield.getWidth() + "',");
         }
         if (ffield.getStyleMap() != null) {
             writer.write("'style' : " + ffield.getStyleMap() + ",");
         } else if (ffield.getStyle() != null) {
             writer.write("'style' : '" + ffield.getStyle() + "',");
         }
         
         if (ffield.getStyleClassMap() != null) {
             writer.write("'styleClass' : " + ffield.getStyleClassMap() + ",");
         } else if (ffield.getStyleClass() != null) {
            writer.write("'styleClass' : '" + ffield.getStyleClass() + "',");
         }
         
         if (ffield.getValueText() != null) {
             writer.write("'value' : '" + ffield.getValueText() + "'");
         } else {
             writer.write("'value' : ''");
         }
         if (ffield.getTitleStyleClass() != null) {
             writer.write(", 'titleStyleClass' : '" + ffield.getTitleStyleClass() + "'");
         }        
         if (ffield.getCondition() != null) {
             writer.write(", 'condition' : '" + ffield.getCondition() + "'");
         }
         if (ffield.getTitle() != null) {
             writer.write(",'fieldTitle' : '" + ffield.getTitle() + "'");
         } else {
             if (ffield.getChildCount() > 0) {
                 for (UIComponent c : ffield.getChildren()) {
                     if (c instanceof FieldLabel) {
                         FieldLabel fl = (FieldLabel)c;
                         writer.write(",'fieldTitle' : $($.parseHTML('" );
                         writeFieldTitleMarkup(context, ffield);
                         writer.write("'))");
                         writer.write(",'fieldTitleType' : '" + fl.getType() + "'");
                     }
                 }
             }
         }
         if (ffield.getType().equals("buttonGroup")) {
             writer.write(",'buttons' : [");
             boolean firstButton = true;
             for (UIComponent c : ffield.getChildren()) {
                 if (firstButton) {
                     firstButton = false;
                 } else {
                     writer.write(",");
                 }
                 if (c instanceof IconButton) {
                     IconButton ic = (IconButton)c;
                     writer.write("{");
                     writer.write("'iconClass' : '" + ic.getImage() + "'");
                     if (ic.getHref() != null) {
                         writer.write(",'href' : '" + ic.getHref() + "'");
                     }
                     if (ic.getOnclick() != null) {
                         writer.write(",'onclick' : function(tgt,ev) {" + ic.getOnclick() + "}");
                     }
                     if (ic.getValue() != null) {
                         writer.write(",'title' : '" + ic.getValue() + "'");
                     }
                     writer.write(",'theme' : '" + ic.getTheme() + "'");
                     writer.write("}");
                 }
             }
             writer.write("]");
         }
         if (ffield.getType().equals("pickList")) {
             writer.write(",'options' : [");
             boolean firstOption = true;
             for (UIComponent c : ffield.getChildren()) {
                 if (firstOption) {
                     firstOption = false;
                 } else {
                     writer.write(",");
                 }
                 if (c instanceof PickItem) {
                     c.encodeAll(context);
                 }
             }
             writer.write("]");
         }
         if (ffield.getOnblur() != null) {
             writer.write(",'onblur' : function(elem) {" + ffield.getOnblur() + "(elem); }");
         }
         
         writer.write("}");
     }
     
     private void writeFieldTitleMarkup(FacesContext context, FormField ffield) throws IOException {
         /* Write the markup to a string. */
         ResponseWriter origWriter = context.getResponseWriter();
         StringWriter sw = new StringWriter();
         ResponseWriter newWriter = origWriter.cloneWithWriter(sw);
         context.setResponseWriter(newWriter);
         
         UIComponent c = ffield.getChildren().get(0);
         c.encodeAll(context);
 
         /* Restore the original writer. */
         context.setResponseWriter(origWriter);
         
         StringBuffer labelMarkup = sw.getBuffer();
         String markupString = labelMarkup.toString();
         /* Escape single quotes. */
         markupString = markupString.replace("'", "\\'");
         markupString = markupString.trim();
         
         ResponseWriter writer = context.getResponseWriter();
         writer.writeText(markupString, null);
     }
     
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Rendering happens on encodeEnd
     }
     
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
