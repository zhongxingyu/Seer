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
 package org.eclipse.riena.ui.ridgets.uibinding;
 
 import java.beans.IntrospectionException;
 import java.beans.PropertyDescriptor;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.util.ReflectionFailure;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.ridgets.Activator;
 import org.eclipse.riena.ui.ridgets.IComplexComponent;
 import org.eclipse.riena.ui.ridgets.IComplexRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.UIBindingFailure;
 import org.eclipse.riena.ui.ridgets.util.beans.BeanUtils;
 import org.osgi.service.log.LogService;
 
 /**
  * This class manages the binding between UI-control and ridget.
  */
 public class DefaultBindingManager implements IBindingManager {
 
 	// cache for PropertyDescriptors
 	private Map<String, PropertyDescriptor> binding2PropertyDesc;
 	private IBindingPropertyLocator propertyStrategy;
 	private IControlRidgetMapper mapper;
 
	private static final Logger LOGGER = Activator.getDefault().getLogger(DefaultBindingManager.class.getName());
 
 	/**
 	 * Creates the managers of all bindings of a view.
 	 * 
 	 * @param propertyStrategy
 	 * 		- strategy to get the property for the binding from the UI-control.
 	 * @param mapper
 	 * 		- mapping for UI control-classes to ridget-classes
 	 */
 	public DefaultBindingManager(IBindingPropertyLocator propertyStrategy, IControlRidgetMapper mapper) {
 		this.propertyStrategy = propertyStrategy;
 		this.mapper = mapper;
 		binding2PropertyDesc = new HashMap<String, PropertyDescriptor>();
 	}
 
 	/**
 	 * @see
 	 * 	org.eclipse.riena.ui.ridgets.uibinding.IBindingManager#injectRidgets(
 	 * 	org.eclipse.riena.ui.ridgets.IRidgetContainer, java.util.List)
 	 */
 	public void injectRidgets(IRidgetContainer ridgetContainer, List<Object> uiControls) {
 		for (Object control : uiControls) {
 			String bindingProperty = propertyStrategy.locateBindingProperty(control);
 			if (bindingProperty != null) {
 				try {
 					IRidget ridget = createRidget(control);
 					ridgetContainer.addRidget(bindingProperty, ridget);
 					injectIntoController(ridget, ridgetContainer, bindingProperty);
 					if (control instanceof IComplexComponent) {
 						IComplexRidget complexRidget = (IComplexRidget) ridget;
 						IComplexComponent complexComponent = (IComplexComponent) control;
 						injectRidgets(complexRidget, complexComponent.getUIControls());
 					}
 				} catch (ReflectionFailure e) {
 					UIBindingFailure ee = new UIBindingFailure("Cannot create ridget for ridget property '" //$NON-NLS-1$
 							+ bindingProperty + "' of ridget container " + ridgetContainer, e); //$NON-NLS-1$
 					LOGGER.log(LogService.LOG_ERROR, ee.getMessage(), ee);
 					throw ee;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates for the given UI-control the appropriate ridget.
 	 * 
 	 * @param control
 	 * 		- UI-control
 	 * @return ridget
 	 * @throws ReflectionFailure
 	 */
 	private IRidget createRidget(Object control) throws ReflectionFailure {
 		Class<? extends IRidget> ridgetClass = mapper.getRidgetClass(control);
 		return ReflectionUtils.newInstance(ridgetClass);
 	}
 
 	private void injectIntoController(IRidget ridget, IRidgetContainer controller, String bindingProperty) {
 		PropertyDescriptor desc = getPropertyDescriptor(bindingProperty, controller);
 		BeanUtils.setValue(controller, desc, ridget);
 	}
 
 	private PropertyDescriptor getPropertyDescriptor(String bindingProperty, IRidgetContainer ridgetContainer) {
 		PropertyDescriptor desc = binding2PropertyDesc.get(bindingProperty);
 		if (desc != null) {
 			return desc;
 		}
 		return createPropertyDescriptor(bindingProperty, ridgetContainer);
 	}
 
 	private PropertyDescriptor createPropertyDescriptor(String bindingProperty, IRidgetContainer ridgetContainer) {
 		try {
 			PropertyDescriptor desc = BeanUtils.getPropertyDescriptor(ridgetContainer, bindingProperty);
 			binding2PropertyDesc.put(bindingProperty, desc);
 			return desc;
 		} catch (IntrospectionException e) {
 			UIBindingFailure bindingFailure = new UIBindingFailure("Cannot access ridget property '" + bindingProperty //$NON-NLS-1$
 					+ "' of ridget container " + ridgetContainer, e); //$NON-NLS-1$
 			LOGGER.log(LogService.LOG_ERROR, bindingFailure.getMessage(), bindingFailure);
 		}
 		return null;
 	}
 
 	private IRidget getRidget(String bindingProperty, IRidgetContainer controller) {
 		PropertyDescriptor desc = getPropertyDescriptor(bindingProperty, controller);
 		return (IRidget) BeanUtils.getValue(controller, desc);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.uibinding.IBindingManager#bind(
 	 * 	IRidgetContainer, java.util.List)
 	 */
 	public void bind(IRidgetContainer controller, List<Object> uiControls) {
 		updateBindings(controller, uiControls, false);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.uibinding.IBindingManager#unbind(
 	 * 	IRidgetContainer, java.util.List)
 	 */
 	public void unbind(IRidgetContainer controller, List<Object> uiControls) {
 		updateBindings(controller, uiControls, true);
 	}
 
 	private void updateBindings(IRidgetContainer controller, List<Object> uiControls, boolean unbind) {
 		for (Object control : uiControls) {
 			if (control instanceof IComplexComponent) {
 				IComplexComponent complexComponent = (IComplexComponent) control;
 				String bindingProperty = propertyStrategy.locateBindingProperty(control);
 				IComplexRidget complexRidget = (IComplexRidget) getRidget(bindingProperty, controller);
 				updateBindings(complexRidget, complexComponent.getUIControls(), unbind);
 				if (complexRidget instanceof IRidget) {
 					bindRidget((IRidget) complexRidget, complexComponent, unbind);
 				}
 
 			} else {
 				String bindingProperty = propertyStrategy.locateBindingProperty(control);
 				if (bindingProperty != null) {
 					IRidget ridget = getRidget(bindingProperty, controller);
 					bindRidget(ridget, control, unbind);
 				}
 			}
 		}
 	}
 
 	private void bindRidget(IRidget ridget, Object uiControl, boolean unbind) {
 		if (unbind) {
 			ridget.setUIControl(null);
 		} else {
 			ridget.setUIControl(uiControl);
 		}
 	}
 
 }
