 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: ValidatingEContentAdapter.java,v 1.10 2009/09/07 13:16:30 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.internal.validation;
 
 import java.util.Properties;
 import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.pde.emfforms.editor.EmfFormEditor;
 import org.eclipse.pde.emfforms.editor.ValidatingService;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.ui.forms.IMessageManager;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.osgi.framework.*;
 
 public class ValidatingEContentAdapter extends EContentAdapter {
	private DataBindingContext _dataBindingContext;
 	private EmfFormEditor<?> _formEditor;
	private IObservableValue _observedValue;
 	private Diagnostician _diagnostician;
 
 	private ValidatingService validatingService;
 
	public ValidatingEContentAdapter(IObservableValue observedValue, DataBindingContext dataBindingContext, EmfFormEditor<?> formEditor) {
 		_formEditor = formEditor;
		_dataBindingContext = dataBindingContext;
		_observedValue = observedValue;
 
 		// if SCR is not present, we want to register the Eclipse 3.4 validator manually ...
 		forceValidatingService34Registration();
 
 		// Subclass the default Diagnostician to customize the String
 		// representation of each validated EObject
 		_diagnostician = new Diagnostician() {
 			@Override
 			public String getObjectLabel(EObject eObject) {
 				if (!eObject.eIsProxy()) {
 					IItemLabelProvider itemLabelProvider = (IItemLabelProvider) _formEditor.getAdapterFactory().adapt(eObject, IItemLabelProvider.class);
 					if (itemLabelProvider != null) {
 						return itemLabelProvider.getText(eObject);
 					}
 				}
 
 				return super.getObjectLabel(eObject);
 			}
 		};
 
 	}
 
 	private void forceValidatingService34Registration() {
 		if (getValidatorService() == null) {
 			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
 			Properties serviceProperties = new Properties();
 			serviceProperties.put(Constants.SERVICE_RANKING, 10);
 			context.registerService(ValidatingService.class.getName(), new ValidatingService34(), serviceProperties);
 		}
 	}
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		super.notifyChanged(notification);
 		if (notification.getEventType() != Notification.REMOVING_ADAPTER) {
 			validate();
 		}
 	}
 
 	public void validate() {
 		IFormPage activePageInstance = _formEditor.getActivePageInstance();
 		if (_formEditor.getActivePageInstance() == null) {
 			return;
 		}
 
 		IMessageManager messageManager = activePageInstance.getManagedForm().getMessageManager();
 		messageManager.removeAllMessages();
 		messageManager.setAutoUpdate(false);
 
		Diagnostic diagnostics = validate((EObject) _observedValue.getValue());
 
 		for (Diagnostic diagnostic : diagnostics.getChildren()) {
 			getValidatorService().analyzeDiagnostic(_dataBindingContext, diagnostic, messageManager);
 		}
 
 		messageManager.update();
 	}
 
 	public Diagnostic validate(EObject obj) {
 		return _diagnostician.validate(obj);
 	}
 
 	private ValidatingService getValidatorService() {
 		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
 		if (validatingService == null) {
 			ServiceReference validatingServiceRef = context.getServiceReference(ValidatingService.class.getName());
 			if (validatingServiceRef != null)
 				validatingService = (ValidatingService) context.getService(validatingServiceRef);
 		}
 		return validatingService;
 	}
 
 }
