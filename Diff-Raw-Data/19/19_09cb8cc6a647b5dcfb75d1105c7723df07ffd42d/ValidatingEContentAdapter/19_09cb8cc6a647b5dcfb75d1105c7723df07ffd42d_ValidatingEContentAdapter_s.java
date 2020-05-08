 package org.eclipse.pde.emfforms.internal.validation;
 
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
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
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
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		if (notification.getEventType() != Notification.REMOVING_ADAPTER) {
 			validate();
 		}
 	}
 
 	public void validate() {
 		IMessageManager messageManager = _formEditor.getActivePageInstance().getManagedForm().getMessageManager();
 		messageManager.removeAllMessages();
 		messageManager.setAutoUpdate(false);
 
		Diagnostic diagnostics = _diagnostician.validate((EObject) _observedValue.getValue());
 
 		for (Diagnostic diagnostic : diagnostics.getChildren()) {
 			getValidatorService().analyzeDiagnostic(_dataBindingContext, diagnostic, messageManager);
 		}
 
 		messageManager.update();
 	}
 
 	private ValidatingService getValidatorService() {
 		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
 		if (validatingService == null) {
 			ServiceReference validatingServiceRef = context.getServiceReference(ValidatingService.class.getName());
 			validatingService = (ValidatingService) context.getService(validatingServiceRef);
 		}
 		return validatingService;
 	}
 
 }
