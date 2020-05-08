 package com.scholastic.sbam.client.uiobjects.uiapp;
 
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.HtmlEditor;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.UpdateProxyService;
 import com.scholastic.sbam.client.services.UpdateProxyServiceAsync;
 import com.scholastic.sbam.client.uiobjects.foundation.FieldFactory;
 import com.scholastic.sbam.client.uiobjects.foundation.PortletMaskDialog;
 import com.scholastic.sbam.shared.objects.ProxyInstance;
 import com.scholastic.sbam.shared.objects.UpdateResponse;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class CreateProxyDialog extends PortletMaskDialog {
 	
 	protected final UpdateProxyServiceAsync		updateProxyService		= GWT.create(UpdateProxyService.class);
 	
 	/**
 	 * Implement this interface to respond to the dialog's request to create a site -- and apply it to the target
 	 * @author Bob Lacatena
 	 *
 	 */
 	public interface CreateProxyDialogSaver {
 		public void onCreateProxySave(ProxyInstance instance);
 		
 		public void lockTrigger();
 		
 		public void unlockTrigger();
 	}
 	
 	protected CreateProxyDialogSaver			saver;
 	
 	protected TextField<String>				descriptionField;
 	protected TextField<String>				searchKeysField;
 	protected HtmlEditor					noteField;
 	
 	public CreateProxyDialog(LayoutContainer container, CreateProxyDialogSaver saver) {
 		super(container);
 		this.saver = saver;
 	}
 	
 	@Override
 	public void init() {
 		super.init();
 		setHeading("Create Proxy");
 	}
 
 	@Override
 	public void addFields(FormPanel formPanel) {
 		
 		if (descriptionField == null) 
 			descriptionField = FieldFactory.getStringTextField("Description", "A clear description of the proxy.");
 		
 		if (searchKeysField == null) 
 			searchKeysField = FieldFactory.getStringTextField("Search Keys", "Any key values that might be used to find this proxy.");
 		
 		if (noteField == null) {
 			noteField = new HtmlEditor();
 			FieldFactory.setStandard(noteField, "Notes");
 		}
 		
 		descriptionField.setAllowBlank(false);
 		descriptionField.setMinLength(4);
 		descriptionField.setMaxLength(255);
 		
 		searchKeysField.setAllowBlank(true);
 		searchKeysField.setMinLength(0);
 		searchKeysField.setMaxLength(255);
 		
 		
 		FormData fd = new FormData("-24");
 		
 		formPanel.add(descriptionField,		fd);
 		formPanel.add(searchKeysField,		fd);
 		formPanel.add(noteField,			fd);
 		
 		formPanel.enable();
 	}
 
 	@Override
 	public void destroyFields() {
 
 		if (descriptionField != null) {
 			descriptionField.removeFromParent();
 			descriptionField = null;
 		}
 
 		if (searchKeysField != null) {
 			searchKeysField.removeFromParent();
 			searchKeysField = null;
 		}
 
 		if (noteField != null) {
 			noteField.removeFromParent();
 			noteField = null;
 		}
 	}
 
 	@Override
 	protected void onSave() {
 	
 		// Set field values from form fields
 		ProxyInstance proxyInstance = new ProxyInstance();
 		proxyInstance.setNewRecord(true);
 		proxyInstance.setDescription(descriptionField.getValue());
 		proxyInstance.setSearchKeys(searchKeysField.getValue());
 		proxyInstance.setNote(noteField.getValue());
 		proxyInstance.setStatus(AppConstants.STATUS_ACTIVE);
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateProxyService.updateProxy(proxyInstance,
 				new AsyncCallback<UpdateResponse<ProxyInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Proxy update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(UpdateResponse<ProxyInstance> updateResponse) {
 						if (saver != null)
 							saver.onCreateProxySave(updateResponse.getInstance());
 				}
 			});
 	}
 
 	@Override
 	public void lockTrigger() {
 		saver.lockTrigger();
 	}
 
 	@Override
 	public void unlockTrigger() {
 		saver.unlockTrigger();
 	}
 
 }
