 /**
  * Copyright 2012 A24Group
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 
 /**
  * Package for all input control elements and components.
  */
 package org.ssgwt.client.ui;
 
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.user.client.ui.TextBox;
 
 /**
  * AdvancedTextbox
  * 
  * A text box that allows the developer to set a placeholder label inside it.
  * When in focus, the placeholder text is removed, when the focus fades, the
  * placeholder text will be replaced if no value was supplied.
  * 
  * @author Jaco Nel <jaco.nel@a24group.com>
  * @since 05 June 2012
  */
 public class AdvancedTextbox extends TextBox implements AdvancedInputField<String> {
 
     /**
      * The text to be used as place holder inside the textbox.
      */
     protected String placeholderText = "";
 
     /**
      * Style name to be applied to the textbox when the placeholder is being
      * displayed. Defaults to 'placeholder'.
      */
     protected String placeholderStyleName = "placeholder";
 
     /**
      * Indicates whether the value for the textbox is required to be filled in
      * when displayed on screen or not.
      */
     protected boolean required = false;
 
     /**
      * The style name to be added to the element if the element is marked as
      * required,
      */
     protected String requiredStyleName = "required";
 
     /**
      * Class constructor
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      */
     public AdvancedTextbox() {
         this.attacheHanlders();
     }
 
     /**
      * Sets the place holder text to be displayed when no value is given in the
      * text input.
      * 
      * @param placeholderText
      *            The text to be used as the placeholder
      */
     public void setPlaceholder(String placeholderText) {
         this.placeholderText = placeholderText.trim();
         this.displayPlaceholder();
     }
 
     /**
      * Retrieves the place holder text to be displayed when no value is given in
      * the text input.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      * 
      * @return The text to be used as the placeholder
      */
     public String getPlaceholderText() {
         return this.placeholderText;
     }
 
     /**
      * Changes the style name for the placeholder text in the textbox.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      */
     public void setPlaceholderStyleName(String placeholderStyleName) {
         this.placeholderStyleName = placeholderStyleName.trim();
     }
 
     /**
      * Returns the placeholder style name set on the textbox.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      * 
      * @return The placeholder styleName
      */
     public String getPlaceholderStyleName() {
         return this.placeholderStyleName;
     }
 
     /**
      * Returns whether the field is required or not
      * 
      * @return required or not
      */
     public boolean isRequired() {
         return required;
     }
 
     /**
      * Set the field to be either required or not. Will apply or remove the
      * required style name from the element dependant on the value provided.
      * 
      * @param required
      *            Whether the field is required or not
      */
     public void setRequired(boolean required) {
         this.required = required;
         if (this.required) {
             this.addStyleName(this.getRequiredStyleName());
         } else {
             this.removeStyleName(this.getRequiredStyleName());
         }
 
     }
 
     /**
      * Retrieve the required style name
      * 
      * @return the requiredStyleName
      */
     public String getRequiredStyleName() {
         return requiredStyleName;
     }
 
     /**
      * Set the required style name
      * 
      * @param requiredStyleName
      *            the required style name to set
      */
     public void setRequiredStyleName(String requiredStyleName) {
         this.removeStyleName(this.getRequiredStyleName());
 
         this.requiredStyleName = requiredStyleName;
         if (this.required) {
             this.addStyleName(this.getRequiredStyleName());
         }
     }
 
     /**
      * Displays the placeholder text only if no other value was provided. It
      * also adds the placeholder style name to the textbox when adding the
      * placeholder text.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      */
     public void displayPlaceholder() {
         if ((super.getText().equals(""))
                 && (!this.getPlaceholderText().equals(""))) {
             super.setText(this.getPlaceholderText());
             super.addStyleName(this.getPlaceholderStyleName());
         }
     }
 
     /**
      * Hides the placeholder by removing the placeholder text from the textbox
      * and also removing the placeholder style name from the textbox.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      */
     public void hidePlaceholder() {
         if (super.getText().equals(this.getPlaceholderText())) {
             super.setText("");
            super.removeStyleName(this.getPlaceholderStyleName());
         }
     }
 
     /**
      * Attach all handlers to the input textbox. Current handlers include @see
      * FocusHandler and @see BlurHandler. These handlers are responsible for
      * displaying and hiding the placeholder text at the right time.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      */
     public void attacheHanlders() {
 
         // hide the placeholder text on focus
         addFocusHandler(new FocusHandler() {
             public void onFocus(FocusEvent event) {
                 hidePlaceholder();
             }
         });
 
         // display the placeholder text when focus is lost.
         addBlurHandler(new BlurHandler() {
             public void onBlur(BlurEvent event) {
                 displayPlaceholder();
             }
         });
     }
 
     /**
      * Override the getText method to trim text before returning it.
      * 
      * @author Jaco Nel <jaco.nel@a24group.com>
      * @since 05 June 2012
      */
     @Override
     public String getText() {
         if (super.getText().trim().equals(this.getPlaceholderText().trim())) {
             return "";
         } else {
             return super.getText().trim();
         }
     }
 
     /**
      * Used to get the type of the input
      * 
      * @return The class type 
      */
     @Override
     public Class<String> getReturnType() {
         return String.class;
     }
 }
