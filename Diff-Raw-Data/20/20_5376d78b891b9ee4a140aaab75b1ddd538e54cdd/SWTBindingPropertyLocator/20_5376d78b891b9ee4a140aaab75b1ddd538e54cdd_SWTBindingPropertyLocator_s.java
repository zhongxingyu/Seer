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

 import org.eclipse.swt.widgets.Control;
 
 public class SWTBindingPropertyLocator implements IBindingPropertyLocator {
 
 	public final static String BINDING_PROPERTY = "binding_property"; //$NON-NLS-1$
 
 	public String locateBindingProperty(Object uiControl) {
 		if (uiControl instanceof Control) {
 			Control control = (Control) uiControl;
 			return (String) control.getData(BINDING_PROPERTY);
 		}
 
 		if (uiControl instanceof IPropertyNameProvider) {
 			IPropertyNameProvider nameProvider = (IPropertyNameProvider) uiControl;
 			return nameProvider.getPropertyName();
 		}
 
 		return null;
 	}
 }
