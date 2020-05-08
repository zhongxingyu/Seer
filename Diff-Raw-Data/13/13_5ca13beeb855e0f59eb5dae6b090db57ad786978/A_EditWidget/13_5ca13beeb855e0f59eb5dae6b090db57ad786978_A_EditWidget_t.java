 /*
  * This library is part of OpenCms -
  * the Open Source Content Management System
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
  * For further information about OpenCms, please see the
  * project website: http://www.opencms.org
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.forms.client.widgets;
 
 import com.google.gwt.event.dom.client.DomEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.EventListener;
 import com.google.gwt.user.client.ui.UIObject;
 
 /**
  * Abstract editing widget class.<p>
  */
 public abstract class A_EditWidget extends UIObject implements I_EditWidget, EventListener {
 
     /** The handler manager. */
     private HandlerManager m_handlerManager;
 
     /** The previous value. */
     private String m_previousValue;
 
     /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public abstract HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler);

    /**
      * Adds a native event handler to the widget and sinks the corresponding
      * native event. If you do not want to sink the native event, use the generic
      * addHandler method instead.<p>
      *
      * @param <H> the type of handler to add
      * @param type the event key
      * @param handler the handler
      * @return {@link HandlerRegistration} used to remove the handler
      */
     public final <H extends EventHandler> HandlerRegistration addDomHandler(final H handler, DomEvent.Type<H> type) {
 
         assert handler != null : "handler must not be null";
         assert type != null : "type must not be null";
         int typeInt = Event.getTypeInt(type.getName());
         if (typeInt == -1) {
             sinkBitlessEvent(type.getName());
         } else {
             sinkEvents(typeInt);
         }
         return ensureHandlers().addHandler(type, handler);
     }
 
     /**
      * Adds this handler to the widget.
      *
      * @param <H> the type of handler to add
      * @param type the event type
      * @param handler the handler
      * @return {@link HandlerRegistration} used to remove the handler
      */
     public final <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {
 
         return ensureHandlers().addHandler(type, handler);
     }
 
     /**
      * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
      */
     public void fireEvent(GwtEvent<?> event) {
 
         if (m_handlerManager != null) {
             m_handlerManager.fireEvent(event);
         }
 
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#getValue()
      */
     public String getValue() {
 
         return getElement().getInnerText();
     }
 
     /**
      * @see com.google.gwt.user.client.EventListener#onBrowserEvent(com.google.gwt.user.client.Event)
      */
     public void onBrowserEvent(Event event) {
 
         switch (DOM.eventGetType(event)) {
             case Event.ONMOUSEOVER:
                 // Only fire the mouse over event if it's coming from outside this
                 // widget.
             case Event.ONMOUSEOUT:
                 // Only fire the mouse out event if it's leaving this
                 // widget.
                 Element related = event.getRelatedEventTarget().cast();
                 if ((related != null) && getElement().isOrHasChild(related)) {
                     return;
                 }
                 break;
         }
         DomEvent.fireNativeEvent(event, this, this.getElement());
 
     }
 
     /**
      * Creates the {@link HandlerManager} used by this Widget. You can override
      * this method to create a custom {@link HandlerManager}.
      *
      * @return the {@link HandlerManager} you want to use
      */
     protected HandlerManager createHandlerManager() {
 
         return new HandlerManager(this);
     }
 
     /**
      * Fires the value change event, if the value has changed.<p>
      */
     protected void fireValueChange() {
 
         String currentValue = getValue();
         if (!currentValue.equals(m_previousValue)) {
             m_previousValue = currentValue;
         }
         ValueChangeEvent.fire(this, currentValue);
     }
 
     /**
      * Returns the previous value.<p>
      *
      * @return the previous value
      */
     protected String getPreviousValue() {
 
         return m_previousValue;
     }
 
     /**
      * @see com.google.gwt.user.client.ui.UIObject#setElement(com.google.gwt.user.client.Element)
      */
     @Override
     protected void setElement(Element element) {
 
         super.setElement(element);
         DOM.setEventListener(getElement(), this);
     }
 
     /**
      * Sets the previous value.<p>
      *
      * @param previousValue the previous value to set
      */
     protected void setPreviousValue(String previousValue) {
 
         m_previousValue = previousValue;
     }
 
     /**
      * Ensures the existence of the handler manager.<p>
      *
      * @return the handler manager
      * */
     HandlerManager ensureHandlers() {
 
         return m_handlerManager == null ? m_handlerManager = createHandlerManager() : m_handlerManager;
     }
 
     /**
      * Returns the handler manager.<p>
      * 
      * @return the handler manager
      */
     HandlerManager getHandlerManager() {
 
         return m_handlerManager;
     }
 }
