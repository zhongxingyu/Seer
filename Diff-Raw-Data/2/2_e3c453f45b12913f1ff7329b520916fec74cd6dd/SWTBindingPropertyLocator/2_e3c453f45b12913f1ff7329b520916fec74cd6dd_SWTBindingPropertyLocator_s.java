 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.utils;
 
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Helper class to get the ID of a SWT UI control used for binding.
  */
public class SWTBindingPropertyLocator implements IBindingPropertyLocator {
 
 	public final static String BINDING_PROPERTY = "binding_property"; //$NON-NLS-1$
 	private static SWTBindingPropertyLocator locator;
 
 	private SWTBindingPropertyLocator() {
 
 	}
 
 	/**
 	 * Returns an instance of this class.
 	 * 
 	 * @return
 	 */
 	public static SWTBindingPropertyLocator getInstance() {
 		if (locator == null) {
 			locator = new SWTBindingPropertyLocator();
 		}
 		return locator;
 	}
 
 	public String locateBindingProperty(Object uiControl) {
 		if (uiControl instanceof Widget) {
 			Widget control = (Widget) uiControl;
 			if (control.isDisposed()) {
 				return null;
 			}
 			return (String) control.getData(BINDING_PROPERTY);
 		}
 
 		if (uiControl instanceof IPropertyNameProvider) {
 			return ((IPropertyNameProvider) uiControl).getPropertyName();
 		}
 
 		return null;
 	}
 
 	public void setBindingProperty(Object uiControl, String id) {
 
 		if (uiControl instanceof Widget) {
 			Widget control = (Widget) uiControl;
 			if (control.isDisposed()) {
 				return;
 			}
 			control.setData(BINDING_PROPERTY, id);
 		} else if (uiControl instanceof IPropertyNameProvider) {
 			((IPropertyNameProvider) uiControl).setPropertyName(id);
 		}
 	}
 }
