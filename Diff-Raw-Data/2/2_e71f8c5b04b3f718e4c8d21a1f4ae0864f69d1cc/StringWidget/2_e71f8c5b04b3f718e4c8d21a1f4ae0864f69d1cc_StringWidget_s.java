 /*
  * This library is part of the Acacia Editor -
  * an open source inline and form based content editor for GWT.
  *
  * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.acacia.client.widgets;
 
 import com.alkacon.acacia.client.css.I_LayoutBundle;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 
 /**
  * The string edit widget.<p>
  */
 public class StringWidget extends A_EditWidget {
 
     /** The value changed handler initialized flag. */
     private boolean m_valueChangeHandlerInitialized;
 
     /**
      * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
      */
     @Override
     public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
 
         // Initialization code
         if (!m_valueChangeHandlerInitialized) {
             m_valueChangeHandlerInitialized = true;
             addDomHandler(new KeyPressHandler() {
 
                 public void onKeyPress(KeyPressEvent event) {
 
                     fireValueChange();
 
                 }
             }, KeyPressEvent.getType());
             addDomHandler(new ChangeHandler() {
 
                 public void onChange(ChangeEvent event) {
 
                     fireValueChange();
 
                 }
             }, ChangeEvent.getType());
             addDomHandler(new BlurHandler() {
 
                 public void onBlur(BlurEvent event) {
 
                     fireValueChange();
                 }
             }, BlurEvent.getType());
         }
         return addHandler(handler, ValueChangeEvent.getType());
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#getValue()
      */
     @Override
     public String getValue() {
 
         return getElement().getInnerText();
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#initWidget(com.google.gwt.dom.client.Element)
      */
     public I_EditWidget initWidget(Element element) {
 
         setElement(element);
         DOM.setEventListener(getElement(), this);
         setPreviousValue(getValue());
        getElement().setAttribute("contenteditable", "true");
         getElement().addClassName(I_LayoutBundle.INSTANCE.form().input());
         return this;
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#setConfiguration(java.lang.String)
      */
     public void setConfiguration(String confuguration) {
 
         // TODO: Auto-generated method stub
 
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
      */
     public void setValue(String value) {
 
         setValue(value, true);
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
      */
     public void setValue(String value, boolean fireEvents) {
 
         getElement().setInnerText(value);
         if (fireEvents) {
             fireValueChange();
         }
     }
 }
