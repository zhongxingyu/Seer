 package com.duggan.workflow.client.ui.save.form;
 
 import com.duggan.workflow.client.service.TaskServiceCallback;
 import com.duggan.workflow.client.ui.events.AfterSaveEvent;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.form.Form;
 import com.duggan.workflow.shared.requests.CreateDocumentRequest;
 import com.duggan.workflow.shared.requests.GetFormModelRequest;
 import com.duggan.workflow.shared.responses.CreateDocumentResult;
 import com.duggan.workflow.shared.responses.GetFormModelResponse;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.PopupView;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.annotations.ContentSlot;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
 
 public class GenericFormPresenter extends
 		PresenterWidget<GenericFormPresenter.ICreateDocView> {
 
 	public interface ICreateDocView extends PopupView {
 		HasClickHandlers getSave();
 
 		HasClickHandlers getCancel();
 
 		HasClickHandlers getForward();
 
 		Document getDocument();
 
 		boolean isValid();
 
 		void setForm(Form form);
 				
 	}
 
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> UPLOAD_SLOT = new Type<RevealContentHandler<?>>();
 
 	@Inject
 	DispatchAsync requestHelper;
 
 	private Long Id;
 
 	@Inject
 	PlaceManager placeManager;
 
 	@Inject
 	public GenericFormPresenter(final EventBus eventBus, final ICreateDocView view) {
 		super(eventBus, view);
 	}
 	
 	@Override
 	protected void onBind() {
 		super.onBind();
 
 		getView().getCancel().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				getView().hide();
 			}
 		});
 		
 		getView().getSave().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				Document document = getView().getDocument();
 				document.setStatus(DocStatus.DRAFTED);
 				document.setId(Id);
 
 				// document.setDescription(null);
 				if (getView().isValid()) {
 					requestHelper.execute(new CreateDocumentRequest(document),
 							new TaskServiceCallback<CreateDocumentResult>() {
 								@Override
 								public void processResult(
 										CreateDocumentResult result) {
 
 									Document saved = result.getDocument();
 									assert saved.getId() != null;
 									fireEvent(new AfterSaveEvent());
 									getView().hide();
 								}
 							});
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onReveal() {
 		super.onReveal();
 		if(Id!=null)
			loadForm(1L);
 	}
 	
 	protected void loadForm(Long id) {
 		GetFormModelRequest request = new GetFormModelRequest(Form.FORMMODEL, id, true);
		
 		requestHelper.execute(request, new TaskServiceCallback<GetFormModelResponse>() {
 			@Override
 			public void processResult(GetFormModelResponse result) {
 				assert result.getFormModel().size()==1;
 				
 				Form form = (Form)result.getFormModel().get(0);
 				getView().setForm(form);
 				
 				getView().center();
 			}
 		});
 	}
 
 	public void setFormId(Long formId) {
 		this.Id = formId;
 	}
 
 }
