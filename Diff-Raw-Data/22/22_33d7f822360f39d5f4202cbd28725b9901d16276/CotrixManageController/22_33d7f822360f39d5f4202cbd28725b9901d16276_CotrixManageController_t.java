 package org.cotrix.web.manage.client;
 
 import org.cotrix.web.common.client.CotrixModule;
 import org.cotrix.web.common.client.CotrixModuleController;
 import org.cotrix.web.common.client.Presenter;
 import org.cotrix.web.common.client.error.ErrorManager;
 import org.cotrix.web.common.client.error.ManagedFailureCallback;
 import org.cotrix.web.common.client.error.ManagedFailureLongCallback;
 import org.cotrix.web.common.client.event.CodeListImportedEvent;
 import org.cotrix.web.common.client.event.CotrixBus;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog;
 import org.cotrix.web.common.client.widgets.dialog.ProgressDialog;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.ConfirmDialogListener;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.DialogButton;
 import org.cotrix.web.common.shared.codelist.UICodelist;
 import org.cotrix.web.common.shared.exception.Exceptions;
 import org.cotrix.web.manage.client.ManageServiceAsync;
 import org.cotrix.web.manage.client.codelist.common.attribute.AttributesGridResources;
 import org.cotrix.web.manage.client.codelist.event.CreateNewVersionEvent;
 import org.cotrix.web.manage.client.codelist.event.RemoveCodelistEvent;
 import org.cotrix.web.manage.client.event.CloseCodelistEvent;
 import org.cotrix.web.manage.client.event.CodelistCreatedEvent;
 import org.cotrix.web.manage.client.event.CodelistRemovedEvent;
 import org.cotrix.web.manage.client.event.CreateNewCodelistEvent;
 import org.cotrix.web.manage.client.event.ManagerBus;
 import org.cotrix.web.manage.client.event.OpenCodelistEvent;
 import org.cotrix.web.manage.client.event.RefreshCodelistsEvent;
 import org.cotrix.web.manage.client.manager.CodelistManagerPresenter;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.shared.CodelistRemoveCheckResponse;
 import org.cotrix.web.manage.shared.CodelistGroup;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class CotrixManageController implements Presenter, ValueChangeHandler<String>, CotrixModuleController {
 	
 	interface CotrixManageControllerEventBinder extends EventBinder<CotrixManageController> {}
 	
 	@Inject
 	private ManageServiceAsync service;
 	
 	@Inject
 	private ProgressDialog progressDialog;
 	
 	@Inject
 	private ConfirmDialog confirmDialog;
 	
 	@Inject
 	private ErrorManager errorManager;
 
 	@Inject
 	@ManagerBus
 	private EventBus managerBus;
 	
 	@Inject
 	private CodelistManagerPresenter codeListManagerPresenter;
 	
 	@Inject
 	private void init(AttributesGridResources resources) {
 		resources.dataGridStyle().ensureInjected();
 	}
 	
 	@Inject
 	private void bind(@CotrixBus EventBus cotrixBus) {
 		cotrixBus.addHandler(CodeListImportedEvent.TYPE, new CodeListImportedEvent.CodeListImportedHandler() {
 			
 			@Override
 			public void onCodeListImported(CodeListImportedEvent event) {
 				refreshCodeLists();
 			}
 		});
 	}
 	
 	@Inject
 	private void bind(CotrixManageControllerEventBinder binder) {
 		binder.bindEventHandlers(this, managerBus);
 	}
 	
 	@Inject
 	private void setupCss(CotrixManagerResources resources) {
 		resources.css().ensureInjected();
 		resources.propertyGrid().ensureInjected();
 		resources.detailsPanelStyle().ensureInjected();
		resources.attributeRow().ensureInjected();
 	}
 	
 	@EventHandler
 	protected void onCreateNewVersion(CreateNewVersionEvent event) {
 		createNewVersion(event.getCodelistId(), event.getNewVersion());
 	}
 	
 	@EventHandler
 	protected void onCreateNewCodelist(CreateNewCodelistEvent event) {
 		createNewCodelist(event.getName(), event.getVersion());
 	}
 	
 	@EventHandler
 	protected void onRemoveCodelist(RemoveCodelistEvent event) {
 		removeCodelist(event.getCodelist());
 	}
 	
 	public void go(HasWidgets container) {
 		this.codeListManagerPresenter.go(container);
 	}
 	
 	public void onValueChange(ValueChangeEvent<String> event) {
 	}
 
 	private void refreshCodeLists() {
 		Log.trace("refreshCodeLists");
 		managerBus.fireEvent(new RefreshCodelistsEvent());
 	}
 	
 	private void createNewVersion(String codelistId, String newVersion)
 	{
 		Log.trace("createNewVersion codelistId: " + codelistId+" newVerions: "+newVersion);
 		progressDialog.showCentered();
 		service.createNewCodelistVersion(codelistId, newVersion, new ManagedFailureCallback<CodelistGroup>() {
 		
 			/** 
 			 * {@inheritDoc}
 			 */
 			@Override
 			public void onCallFailed() {
 				progressDialog.hide();
 			}
 
 			@Override
 			public void onSuccess(CodelistGroup result) {
 				Log.trace("created "+result);
 				managerBus.fireEvent(new OpenCodelistEvent(result.getVersions().get(0).toUICodelist()));
 				managerBus.fireEvent(new CodelistCreatedEvent(result));
 				progressDialog.hide();
 			}
 		});
 	}
 	
 	private void createNewCodelist(String name, String version)
 	{
 		Log.trace("createNewVersion name: "+name+" version: "+version);
 		service.createNewCodelist(name, version, new AsyncCallback<CodelistGroup>() {
 			
 			@Override
 			public void onSuccess(CodelistGroup result) {
 				Log.trace("created "+result);
 				managerBus.fireEvent(new OpenCodelistEvent(result.getVersions().get(0).toUICodelist()));
 				managerBus.fireEvent(new CodelistCreatedEvent(result));
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Creation of a new codelist failed", caught);
 				progressDialog.hide();
 				errorManager.showError(Exceptions.toError(caught));
 			}
 		});
 	}
 	
 	private void removeCodelist(final UICodelist codelist) {
 		Log.trace("deleteCodelist codelist: "+codelist);
 		progressDialog.showCentered();
 		service.canUserRemove(codelist.getId(), new AsyncCallback<CodelistRemoveCheckResponse>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Checking user rights failed", caught);
 				progressDialog.hide();
 				errorManager.showError(Exceptions.toError(caught));
 			}
 
 			@Override
 			public void onSuccess(CodelistRemoveCheckResponse result) {
 				progressDialog.hide();
 				askUserConfirm(codelist, result);
 			}
 		});
 	}
 	
 	private void askUserConfirm(final UICodelist codelist, CodelistRemoveCheckResponse checkResult) {
 		Log.trace("askUserConfirm codelistId: "+codelist+" checkResult: "+checkResult);
 		if (checkResult.isCanRemove()) {
 			
 			confirmDialog.center("The operation can't be reverted. Do you want to continue?", new ConfirmDialogListener() {
 				
 				@Override
 				public void onButtonClick(DialogButton button) {
 					if (button == DialogButton.CONTINUE) doRemoveCodelist(codelist);
 				}
 			});
 		} else {
 			String message = "You can't delete the selected codelist for this reason: "+checkResult.getCause();
 			confirmDialog.warning(message);
 		}
 	}
 	
 	private void doRemoveCodelist(final UICodelist codelist) {
 		service.removeCodelist(codelist.getId(), new ManagedFailureLongCallback<Void>() {
 
 			@Override
 			public void onCallSuccess(Void result) {
 				managerBus.fireEvent(new CodelistRemovedEvent(CodelistGroup.fromCodelist(codelist)));
 				managerBus.fireEvent(new CloseCodelistEvent(codelist));
 			}
 		});
 	}
 
 	@Override
 	public CotrixModule getModule() {
 		return CotrixModule.MANAGE;
 	}
 
 	@Override
 	public void activate() {
 		refreshCodeLists();	
 	}
 
 	@Override
 	public void deactivate() {
 	}
 
 }
