 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package de.jwic.controls;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.jwic.base.IControlContainer;
 import de.jwic.events.SelectionEvent;
 import de.jwic.events.SelectionListener;
 import de.jwic.util.IHTMLElement;
 
 /**
  * Represents a standard SELECT element on the page.
  *  
  * @author lordsam
  */
 public class ListBox extends AbstractSelectListControl implements IHTMLElement {
 
 	protected List<SelectionListener> listeners = null;
 
 	protected String confirmMsg = null;
 	
 	protected String cssClass = "ui-widget ui-widget-content j-listbox";
 	protected boolean fillWidth = false;
 	protected int width = 0;	// 0 = not set
 	protected int height = 0;	// 0 = not set
 	
	protected int size = 1; 
 	
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public ListBox(IControlContainer container, String name) {
 		super(container, name);
 
 	}
 
 	/**
 	 * Register a listener that will be notified when the listbox has been
 	 * selected (i.e. doubleClick). The event is only triggered if the
 	 * selectionMode property is not NO_SELECTION.
 	 * @param listener
 	 */
 	public void addSelectionListener(SelectionListener listener) {
 		if (listeners == null) {
 			listeners = new ArrayList<SelectionListener>();
 		}
 		listeners.add(listener);
 	}
 	
 	/**
 	 * Removes the specified listener.
 	 * @param listener
 	 */
 	public void removeSelectionListener(SelectionListener listener) {
 		if (listeners != null) {
 			listeners.remove(listener);
 		}
 	}
 
 	
 	/**
 	 * Invoked when the value has been changed.
 	 *
 	 */
 	public void actionValuechanged() {
 		// nothing to do, as the valueChanged is triggered directly by the field.
 		// but we must leave this method as it is invoked by the onChanged event.
 	}
 	
 	/**
 	 * The action method that handles if the user selects the control, depending on
 	 * how the selectionMode is set. 
 	 */
 	public void actionSelected(String type) {
 		sendSelectionEvent("dblClick".equals(type));
 	}
 	
 	/**
 	 * Send the element selected event to the registered listeners.
 	 * Defaults dblClick to false.
 	 */
 	protected void sendSelectionEvent() {
 		sendSelectionEvent(false);
 	}
 	
 	/**
 	 * Send the element selected event to the registerd listeners.
 	 * @param dblClick
 	 */
 	protected void sendSelectionEvent(boolean dblClick) {
 		
 		if (listeners != null) {
 			SelectionEvent e = new SelectionEvent(this, dblClick);
 			for (Iterator<SelectionListener> it = listeners.iterator(); it.hasNext(); ) {
 				SelectionListener osl = it.next();
 				osl.objectSelected(e);
 			}
 		}
 
 	}
 
 	/**
 	 * @return the confirmMsg
 	 */
 	public String getConfirmMsg() {
 		return confirmMsg;
 	}
 
 	/**
 	 * @param confirmMsg the confirmMsg to set
 	 */
 	public void setConfirmMsg(String confirmMsg) {
 		this.confirmMsg = confirmMsg;
 		requireRedraw();
 	}
 
 	/**
 	 * @return the cssClass
 	 */
 	public String getCssClass() {
 		return cssClass;
 	}
 
 	/**
 	 * @param cssClass the cssClass to set
 	 */
 	public void setCssClass(String cssClass) {
 		this.cssClass = cssClass;
 		requireRedraw();
 	}
 
 	/**
 	 * @return the fillWidth
 	 */
 	public boolean isFillWidth() {
 		return fillWidth;
 	}
 
 	/**
 	 * @param fillWidth the fillWidth to set
 	 */
 	public void setFillWidth(boolean fillWidth) {
 		this.fillWidth = fillWidth;
 		requireRedraw();
 	}
 
 	/**
 	 * @return the width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @param width the width to set
 	 */
 	public void setWidth(int width) {
 		this.width = width;
 		requireRedraw();
 	}
 
 	/**
 	 * @return the height
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * @param height the height to set
 	 */
 	public void setHeight(int height) {
 		this.height = height;
 		requireRedraw();
 	}
 
 	/**
 	 * @return the size
 	 */
 	public int getSize() {
 		return size;
 	}
 
 	/**
 	 * @param size the size to set
 	 */
 	public void setSize(int size) {
 		if (this.size != size) {
 			this.size = size;
 			requireRedraw();
 		}
 	}
 
 }
