 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wojciech Galanciak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     wojciech.galanciak@gmail.com - initial API and implementation
  *******************************************************************************/
 package org.zend.usagedata.internal.swt.adapters;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Widget;
 import org.zend.usagedata.internal.swt.ComponentType;
 import org.zend.usagedata.internal.swt.EventMessage;
 
 /**
  * Represents adapter for SWT {@link Widget} component.
  * 
  * @author wojciech.galanciak@gmail.com
  * 
  */
 public class WidgetAdapter extends AbstractAdapter {
 
 	private static final String PREFIX = ComponentType.WIDGET.getPrefix();
 
 	public static final String EVENT_TYPE = PREFIX + "e"; //$NON-NLS-1$
 	public static final String STYLE = PREFIX + "s"; //$NON-NLS-1$
 	public static final String CLASS = PREFIX + "t"; //$NON-NLS-1$
 	public static final String SHELL_TITLE = PREFIX + "st"; //$NON-NLS-1$
 
 	protected ComponentType componentType;
 
 	private Widget widget;
 
 	private int eventType;
 
 	public WidgetAdapter(Widget widget, int eventType) {
 		this.widget = widget;
 		this.eventType = eventType;
 		this.message = new EventMessage();
 		this.componentType = ComponentType.WIDGET;
 	}
 
 	/**
 	 * @return component type
 	 */
 	public ComponentType getComponentType() {
 		return componentType;
 	}
 
 	/**
 	 * @return component type name
 	 */
 	public String getWidgetType() {
 		return componentType.getName();
 	}
 
 	@Override
 	public String getShell() {
 		Display display = widget.getDisplay();
 		if (display != null) {
 			Shell shell = display.getActiveShell();
 			if (shell != null) {
 				return shell.getText();
 			}
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * @return widget style
 	 */
 	public int getStyle() {
		return widget.getStyle();
 	}
 
 	@Override
 	protected void buildMessage() {
 		message.addMessage(EVENT_TYPE, eventType);
 		message.addMessage(STYLE, getStyle());
 		message.addMessage(CLASS, getWidgetType());
 		message.addMessage(SHELL_TITLE, getShell());
 	}
 
 }
