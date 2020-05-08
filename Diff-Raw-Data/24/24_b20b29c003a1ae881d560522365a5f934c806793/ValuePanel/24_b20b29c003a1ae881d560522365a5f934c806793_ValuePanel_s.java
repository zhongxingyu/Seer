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
 
 package com.alkacon.acacia.client.ui;
 
 import com.alkacon.acacia.client.css.I_LayoutBundle;
 import com.alkacon.geranium.client.dnd.DNDHandler.Orientation;
 import com.alkacon.geranium.client.dnd.I_Draggable;
 import com.alkacon.geranium.client.dnd.I_DropTarget;
 import com.alkacon.geranium.client.util.DomUtil;
 
 import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.FlowPanel;
 
 /**
  * The attribute values panel.<p>
  */
public class ValuePanel extends FlowPanel implements I_DropTarget, HasMouseOverHandlers {
 
     /** The current place holder. */
     protected Element m_placeholder;
 
     /** The placeholder position index. */
     protected int m_placeholderIndex = -1;
 
     /**
      * Constructor.<p>
      */
     public ValuePanel() {
 
         setStyleName(I_LayoutBundle.INSTANCE.form().attribute());
        // to stop propagation of mouse over events, this is done to stop flickering effect during highlighting
        addMouseOverHandler(new MouseOverHandler() {

            public void onMouseOver(MouseOverEvent event) {

                event.stopPropagation();
            }
        });
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
     }
 
     /**
      * @see com.alkacon.geranium.client.dnd.I_DropTarget#checkPosition(int, int, com.alkacon.geranium.client.dnd.DNDHandler.Orientation)
      */
     public boolean checkPosition(int x, int y, Orientation orientation) {
 
         return true;
     }
 
     /**
      * @see com.alkacon.geranium.client.dnd.I_DropTarget#getPlaceholderIndex()
      */
     public int getPlaceholderIndex() {
 
         return m_placeholderIndex;
     }
 
     /**
      * @see com.alkacon.geranium.client.dnd.I_DropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, com.alkacon.geranium.client.dnd.DNDHandler.Orientation)
      */
     public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {
 
         m_placeholder = placeholder;
         repositionPlaceholder(x, y, orientation);
     }
 
     /**
      * @see com.alkacon.geranium.client.dnd.I_DropTarget#onDrop(com.alkacon.geranium.client.dnd.I_Draggable)
      */
     public void onDrop(I_Draggable draggable) {
 
         // nothing to do
     }
 
     /**
      * @see com.alkacon.geranium.client.dnd.I_DropTarget#removePlaceholder()
      */
     public void removePlaceholder() {
 
         if (m_placeholder == null) {
             return;
         }
         m_placeholder.removeFromParent();
         m_placeholder = null;
         m_placeholderIndex = -1;
     }
 
     /**
      * @see com.alkacon.geranium.client.dnd.I_DropTarget#repositionPlaceholder(int, int, com.alkacon.geranium.client.dnd.DNDHandler.Orientation)
      */
     public void repositionPlaceholder(int x, int y, Orientation orientation) {
 
         switch (orientation) {
             case HORIZONTAL:
                 m_placeholderIndex = DomUtil.positionElementInside(
                     m_placeholder,
                     getElement(),
                     m_placeholderIndex,
                     x,
                     -1);
                 break;
             case VERTICAL:
                 m_placeholderIndex = DomUtil.positionElementInside(
                     m_placeholder,
                     getElement(),
                     m_placeholderIndex,
                     -1,
                     y);
                 break;
             case ALL:
             default:
                 m_placeholderIndex = DomUtil.positionElementInside(
                     m_placeholder,
                     getElement(),
                     m_placeholderIndex,
                     x,
                     y);
                 break;
         }
     }
 }
