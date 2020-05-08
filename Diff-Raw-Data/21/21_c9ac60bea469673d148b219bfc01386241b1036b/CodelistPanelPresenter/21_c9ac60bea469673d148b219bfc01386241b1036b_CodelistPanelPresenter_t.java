 package org.cotrix.web.manage.client.codelist;
 
 import org.cotrix.web.common.client.Presenter;
 import org.cotrix.web.common.client.error.ManagedFailureCallback;
 import org.cotrix.web.common.client.feature.AsyncCallBackWrapper;
 import org.cotrix.web.common.client.feature.FeatureBinder;
 import org.cotrix.web.common.client.feature.HasFeature;
 import org.cotrix.web.common.client.widgets.HasEditing;
 import org.cotrix.web.common.shared.feature.FeatureCarrier;
 import org.cotrix.web.manage.client.ManageServiceAsync;
 import org.cotrix.web.manage.client.codelist.CodelistToolbar.Action;
 import org.cotrix.web.manage.client.codelist.CodelistToolbar.ToolBarListener;
 import org.cotrix.web.manage.client.data.CodeAttributeCommandGenerator;
 import org.cotrix.web.manage.client.data.CodeModifyCommandGenerator;
 import org.cotrix.web.manage.client.data.DataSaverManager;
 import org.cotrix.web.manage.client.data.MetadataAttributeModifyGenerator;
 import org.cotrix.web.manage.client.data.MetadataModifyCommandGenerator;
 import org.cotrix.web.manage.shared.ManagerUIFeature;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodelistPanelPresenter implements Presenter {
 
 	protected CodelistPanelView view;
 	protected String codelistId;
 	protected ManageServiceAsync service;
 	
 	@Inject
 	protected DataSaverManager saverManager;
 	
 	protected ManagedFailureCallback<FeatureCarrier.Void> callBack = new ManagedFailureCallback<FeatureCarrier.Void>() {
 
 		@Override
 		public void onSuccess(FeatureCarrier.Void result) {
 			loadState();
 		}
 	};
 	
 	protected AsyncCallBackWrapper<String> updateStateBack = AsyncCallBackWrapper.wrap(new ManagedFailureCallback<String>() {
 
 		@Override
 		public void onSuccess(String result) {
 			loadState();
 		}
 	});
 
 	@Inject
 	public CodelistPanelPresenter(CodelistPanelView view, @CodelistId String codelistId, ManageServiceAsync service, DataSaverManager saverManager) {
 		this.view = view;
 		this.codelistId = codelistId;
 		this.service = service;
 		this.saverManager = saverManager;
 		
 		bind();
 		bindFeatures();
 		bindSavers();
 		
 		loadState();
 	}
 
 	protected void bindSavers() {
 		saverManager.register(new CodeModifyCommandGenerator());
 		saverManager.register(new CodeAttributeCommandGenerator());
 		saverManager.register(new MetadataModifyCommandGenerator());
 		saverManager.register(new MetadataAttributeModifyGenerator());
 	}
 	
 	protected void bind()
 	{
 		// TOOLBAR
 		CodelistToolbar toolbar = view.getToolBar();
 		
 		toolbar.setListener(new ToolBarListener() {
 			
 			@Override
 			public void onAction(Action action) {
 				Log.trace("toolbar onAction "+action);
 				switch (action) {
 					case ALL_COLUMN: view.getCodeListEditor().showAllAttributesAsColumn(); break;
 					case ALL_NORMAL: view.getCodeListEditor().showAllAttributesAsNormal(); break;
 					case LOCK: lock(); break;
 					case FINALIZE: finalizeCodelist(); break;
 					case UNLOCK: unlock(); break;
 				}
 			}
 		});
 	}
 	
 	protected void loadState() {
 		view.getToolBar().showStateLoader(true);
 		service.getCodelistState(codelistId, AsyncCallBackWrapper.wrap(new ManagedFailureCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 				updateState(result);
 			}
 		}));
 	}
 	
 	protected void lock()
 	{
 		view.getToolBar().showStateLoader(true);
 		service.lock(codelistId, callBack);
 	}
 	
 	protected void unlock()
 	{
 		view.getToolBar().showStateLoader(true);
 		service.unlock(codelistId, callBack);
 	}
 	
 	protected void finalizeCodelist()
 	{
 		view.getToolBar().showStateLoader(true);
 		service.seal(codelistId, callBack);
 	}
 	
 	protected void updateState(String state) {
 		view.getToolBar().setState(state);
 		view.getToolBar().showStateLoader(false);
 	}
 
 	
 	protected void bindFeatures()
 	{
 		// TOOLBAR
 		CodelistToolbar toolbar = view.getToolBar();
 		
 		FeatureBinder.bind(new ActionEnabler(Action.LOCK, toolbar), codelistId, ManagerUIFeature.LOCK_CODELIST);
 		FeatureBinder.bind(new ActionEnabler(Action.UNLOCK, toolbar), codelistId, ManagerUIFeature.UNLOCK_CODELIST);
 		FeatureBinder.bind(new ActionEnabler(Action.FINALIZE, toolbar), codelistId, ManagerUIFeature.SEAL_CODELIST);
 		
 		// CODELIST EDITOR
 		FeatureBinder.bind((HasEditing)view.getCodeListEditor(), codelistId, ManagerUIFeature.EDIT_CODELIST);
 		
 		//METADATA EDITOR
 		FeatureBinder.bind(view.getMetadataEditor(), codelistId, ManagerUIFeature.EDIT_METADATA);
 		
 		//ATTRIBUTES EDITOR
 		FeatureBinder.bind(view.getAttributesEditor(), codelistId, ManagerUIFeature.EDIT_CODELIST);
 	}
 	
 	protected class ActionEnabler implements HasFeature {
 		protected Action action;
 		protected CodelistToolbar toolbar;
 
 		/**
 		 * @param action
 		 * @param toolbar
 		 */
 		public ActionEnabler(Action action, CodelistToolbar toolbar) {
 			this.action = action;
 			this.toolbar = toolbar;
 		}
 
 		@Override
 		public void setFeature() {
 			toolbar.setEnabled(action, true);
 		}
 
 		@Override
 		public void unsetFeature() {
 			toolbar.setEnabled(action, false);
 		}
 		
 	}
 	
 	public void go(HasWidgets container) {
 		container.add(view.asWidget());
 	}
 
 	public CodelistPanelView getView() {
 		return view;
 	}
 }
