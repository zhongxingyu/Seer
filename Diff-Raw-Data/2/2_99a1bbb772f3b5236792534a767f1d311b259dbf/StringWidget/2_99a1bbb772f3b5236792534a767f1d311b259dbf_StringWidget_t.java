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
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
 import com.google.gwt.user.client.Element;
 
 /**
  * The string edit widget.<p>
  */
 public class StringWidget extends A_EditWidget {
 
     /** Indicating if the widget is active. */
     private boolean m_active;
 
     /** The value changed handler initialized flag. */
     private boolean m_valueChangeHandlerInitialized;
 
     /**
      * Constructor.<p>
      */
     public StringWidget() {
 
         this(DOM.createDiv());
     }
 
     /**
      * Constructor wrapping a specific DOM element.<p>
      * 
      * @param element the element to wrap
      */
     public StringWidget(Element element) {
 
         super(element);
         init();
     }
 
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
 
                     // schedule the change event, so the key press can take effect
                     Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
                         public void execute() {
 
                             fireValueChange(false);
                         }
                     });
                 }
             }, KeyPressEvent.getType());
             addDomHandler(new ChangeHandler() {
 
                 public void onChange(ChangeEvent event) {
 
                     fireValueChange(false);
 
                 }
             }, ChangeEvent.getType());
             addDomHandler(new BlurHandler() {
 
                 public void onBlur(BlurEvent event) {
 
                     fireValueChange(false);
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
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#isActive()
      */
     public boolean isActive() {
 
         return m_active;
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
      */
     public void setActive(boolean active) {
 
         if (m_active == active) {
             return;
         }
         m_active = active;
         if (m_active) {
             getElement().setAttribute("contentEditable", "true");
             getElement().removeClassName(I_LayoutBundle.INSTANCE.form().inActive());
             getElement().focus();
             fireValueChange(true);
         } else {
             getElement().setAttribute("contentEditable", "false");
             getElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
         }
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
      */
     public void setName(String name) {
 
        // nothing to do
 
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
             fireValueChange(false);
         }
     }
 
     /**
      * Initializes the widget.<p>
      */
     private void init() {
 
         getElement().setAttribute("contentEditable", "true");
         addStyleName(I_LayoutBundle.INSTANCE.form().input());
         m_active = true;
     }
 }
