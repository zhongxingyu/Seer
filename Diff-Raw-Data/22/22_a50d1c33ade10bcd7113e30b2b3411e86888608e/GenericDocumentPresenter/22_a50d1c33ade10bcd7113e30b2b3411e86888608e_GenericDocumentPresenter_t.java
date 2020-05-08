 package com.duggan.workflow.client.ui.document;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.duggan.workflow.client.model.MODE;
 import com.duggan.workflow.client.model.TaskType;
 import com.duggan.workflow.client.model.UploadContext;
 import com.duggan.workflow.client.model.UploadContext.UPLOADACTION;
 import com.duggan.workflow.client.service.ServiceCallback;
 import com.duggan.workflow.client.service.TaskServiceCallback;
 import com.duggan.workflow.client.ui.AppManager;
 import com.duggan.workflow.client.ui.OnOptionSelected;
 import com.duggan.workflow.client.ui.comments.CommentPresenter;
 import com.duggan.workflow.client.ui.delegate.DelegateTaskView;
 import com.duggan.workflow.client.ui.delegate.msg.DelegationMessageView;
 import com.duggan.workflow.client.ui.events.ActivitiesLoadEvent;
 import com.duggan.workflow.client.ui.events.ActivitiesLoadEvent.ActivitiesLoadHandler;
 import com.duggan.workflow.client.ui.events.AfterAttachmentReloadedEvent;
 import com.duggan.workflow.client.ui.events.AfterDocumentLoadEvent;
 import com.duggan.workflow.client.ui.events.AfterSaveEvent;
 import com.duggan.workflow.client.ui.events.CompleteDocumentEvent;
 import com.duggan.workflow.client.ui.events.DeleteLineEvent;
 import com.duggan.workflow.client.ui.events.DeleteLineEvent.DeleteLineHandler;
 import com.duggan.workflow.client.ui.events.ExecTaskEvent;
 import com.duggan.workflow.client.ui.events.ProcessingCompletedEvent;
 import com.duggan.workflow.client.ui.events.ProcessingEvent;
 import com.duggan.workflow.client.ui.events.ReloadAttachmentsEvent;
 import com.duggan.workflow.client.ui.events.ReloadAttachmentsEvent.ReloadAttachmentsHandler;
 import com.duggan.workflow.client.ui.events.ReloadDocumentEvent;
 import com.duggan.workflow.client.ui.events.ReloadDocumentEvent.ReloadDocumentHandler;
 import com.duggan.workflow.client.ui.events.ReloadEvent;
 import com.duggan.workflow.client.ui.events.WorkflowProcessEvent;
 import com.duggan.workflow.client.ui.notifications.note.NotePresenter;
 import com.duggan.workflow.client.ui.popup.GenericPopupPresenter;
 import com.duggan.workflow.client.ui.save.CreateDocPresenter;
 import com.duggan.workflow.client.ui.upload.UploadDocumentPresenter;
 import com.duggan.workflow.client.ui.upload.attachment.AttachmentPresenter;
 import com.duggan.workflow.client.ui.upload.custom.Uploader;
 import com.duggan.workflow.client.util.AppContext;
 import com.duggan.workflow.shared.model.Actions;
 import com.duggan.workflow.shared.model.Activity;
 import com.duggan.workflow.shared.model.Attachment;
 import com.duggan.workflow.shared.model.BooleanValue;
 import com.duggan.workflow.shared.model.Comment;
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.DateValue;
 import com.duggan.workflow.shared.model.Delegate;
 import com.duggan.workflow.shared.model.Doc;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentLine;
 import com.duggan.workflow.shared.model.DocumentType;
 import com.duggan.workflow.shared.model.GridValue;
 import com.duggan.workflow.shared.model.HTSummary;
 import com.duggan.workflow.shared.model.HTUser;
 import com.duggan.workflow.shared.model.HTask;
 import com.duggan.workflow.shared.model.NodeDetail;
 import com.duggan.workflow.shared.model.Notification;
 import com.duggan.workflow.shared.model.StringValue;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.Field;
 import com.duggan.workflow.shared.model.form.Form;
 import com.duggan.workflow.shared.model.form.FormModel;
 import com.duggan.workflow.shared.requests.ApprovalRequest;
 import com.duggan.workflow.shared.requests.CreateDocumentRequest;
 import com.duggan.workflow.shared.requests.DeleteDocumentRequest;
 import com.duggan.workflow.shared.requests.DeleteLineRequest;
 import com.duggan.workflow.shared.requests.GetActivitiesRequest;
 import com.duggan.workflow.shared.requests.GetAttachmentsRequest;
 import com.duggan.workflow.shared.requests.GetCommentsRequest;
 import com.duggan.workflow.shared.requests.GetDocumentRequest;
 import com.duggan.workflow.shared.requests.GetFormModelRequest;
 import com.duggan.workflow.shared.requests.GetProcessStatusRequest;
 import com.duggan.workflow.shared.requests.GetUsersRequest;
 import com.duggan.workflow.shared.requests.MultiRequestAction;
 import com.duggan.workflow.shared.requests.SaveCommentRequest;
 import com.duggan.workflow.shared.responses.ApprovalRequestResult;
 import com.duggan.workflow.shared.responses.CreateDocumentResult;
 import com.duggan.workflow.shared.responses.DeleteDocumentResponse;
 import com.duggan.workflow.shared.responses.DeleteLineResponse;
 import com.duggan.workflow.shared.responses.GetActivitiesResponse;
 import com.duggan.workflow.shared.responses.GetAttachmentsResponse;
 import com.duggan.workflow.shared.responses.GetCommentsResponse;
 import com.duggan.workflow.shared.responses.GetDocumentResult;
 import com.duggan.workflow.shared.responses.GetFormModelResponse;
 import com.duggan.workflow.shared.responses.GetProcessStatusRequestResult;
 import com.duggan.workflow.shared.responses.GetUsersResponse;
 import com.duggan.workflow.shared.responses.MultiRequestActionResult;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.gwtplatform.common.client.IndirectProvider;
 import com.gwtplatform.common.client.StandardProvider;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 
 public class GenericDocumentPresenter extends
 		PresenterWidget<GenericDocumentPresenter.MyView> 
 		implements ReloadDocumentHandler, ActivitiesLoadHandler,
 		ReloadAttachmentsHandler,DeleteLineHandler{
 
 	public interface MyView extends View {
 		void setValues(HTUser createdBy, Date created, String type, String subject,
 				Date docDate, String value, String partner, String description, 
 				Integer priority,DocStatus status, Long id, String taskDisplayName);
 		
 		void showForward(boolean show);
 		void setValidTaskActions(List<Actions> actions);
 		void show(boolean IsShowapprovalLink, boolean IsShowRejectLink);
 		void showEdit(boolean displayed);
 		void setStates(List<NodeDetail> states);
 		HasClickHandlers getSimulationBtn();
 		HasClickHandlers getSaveButton();
 		HasClickHandlers getDeleteButton();
 		HasClickHandlers getForwardForApproval();		
 		HasClickHandlers getClaimLink();
 		HasClickHandlers getStartLink();
 		HasClickHandlers getSuspendLink();
 		HasClickHandlers getResumeLink();
 		HasClickHandlers getCompleteLink();
 		HasClickHandlers getDelegateLink();
 		HasClickHandlers getRevokeLink();
 		HasClickHandlers getStopLink();
 		HasClickHandlers getApproveLink();
 		HasClickHandlers getRejectLink();
 		HasClickHandlers getSaveCommentButton();
 		String getComment();
 		Uploader getUploader();
 		void setComment(String string);
 		HasClickHandlers getUploadLink();
 		SpanElement getSpnAttachmentNo();
 		SpanElement getSpnActivityNo();
 		DivElement getDivAttachment();
 		void setForm(Form form);
 		boolean isValid();
 		Map<String,Value> getValues(); //Task Data
 
 		void showDefaultFields(boolean b);
 
 		void setDelegate(Delegate delegate);
 
 		HasClickHandlers getUploadLink2();
 	}
 	
 	Long taskId;
 	
 	Long documentId;
 	
 	Doc doc;
 	
 	Form form;
 	
 	private Integer activities=0;
 	
 	@Inject DispatchAsync requestHelper;
 	
 	@Inject PlaceManager placeManager;
 	
 	private IndirectProvider<CreateDocPresenter> createDocProvider;
 	private IndirectProvider<CommentPresenter> commentPresenterFactory;
 	private IndirectProvider<AttachmentPresenter> attachmentPresenterFactory;
 	private IndirectProvider<NotePresenter> notePresenterFactory;
 	private IndirectProvider<UploadDocumentPresenter> uploaderFactory;
 	
 	//@Inject static MainPagePresenter mainPagePresenter;
 	@Inject static GenericPopupPresenter popupPresenter;
 	
 	public static final Object ACTIVITY_SLOT = new Object();
 	public static final Object ATTACHMENTS_SLOT = new Object();
 	
 	@Inject
 	public GenericDocumentPresenter(final EventBus eventBus, final MyView view,
 			Provider<CreateDocPresenter> docProvider,
 			Provider<CommentPresenter> commentProvider,
 			Provider<AttachmentPresenter> attachmentProvider,
 			Provider<NotePresenter> noteProvider,
 			Provider<UploadDocumentPresenter> uploaderProvider) {
 		super(eventBus, view);		
 		createDocProvider = new StandardProvider<CreateDocPresenter>(docProvider);
 		commentPresenterFactory = new StandardProvider<CommentPresenter>(commentProvider);
 		attachmentPresenterFactory = new StandardProvider<AttachmentPresenter>(attachmentProvider);
 		notePresenterFactory = new StandardProvider<NotePresenter>(noteProvider);
 		uploaderFactory = new StandardProvider<UploadDocumentPresenter>(uploaderProvider);
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();		
 		
 		addRegisteredHandler(ReloadDocumentEvent.TYPE, this);
 		addRegisteredHandler(ActivitiesLoadEvent.TYPE, this);
 		addRegisteredHandler(ReloadAttachmentsEvent.TYPE, this);
 		addRegisteredHandler(DeleteLineEvent.TYPE, this);
		
 		getView().getUploadLink().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				showUpload();
 			  }
 		});
 		
 		getView().getUploadLink2().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				showUpload();
 			  }
 		});
 		
 		getView().getForwardForApproval().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				fireEvent(new ProcessingEvent());
 				requestHelper.execute(new ApprovalRequest(AppContext.getUserId(), (Document)doc), new TaskServiceCallback<ApprovalRequestResult>(){
 					@Override
 					public void processResult(ApprovalRequestResult result) {
 						GenericDocumentPresenter.this.getView().asWidget().removeFromParent();
 						fireEvent(new ProcessingCompletedEvent());
 						//clear selected document
 						fireEvent(new AfterSaveEvent());
 						fireEvent(new WorkflowProcessEvent(doc.getSubject(), "You have forwarded for Approval",doc));
 					}
 				});
 				
 			}
 		});
 		
 		getView().getSaveButton().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				if(doc instanceof Document)
 				if(((Document)doc).getStatus()==DocStatus.DRAFTED){
 					//showEditForm(MODE.EDIT);
 					fireEvent(new ProcessingEvent());
 					save((Document)doc);
 				}
 			}
 		});
 		
 		getView().getDeleteButton().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				if(doc instanceof Document)
 					if(((Document)doc).getStatus()==DocStatus.DRAFTED){
 						//showEditForm(MODE.EDIT);
 						delete((Document)doc);
 					}
 			}
 		});
 		
 		getView().getSaveCommentButton().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				String comment = getView().getComment();
 				save(comment);
 			}
 		});
 		
 		//testing code
 		getView().getSimulationBtn().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 //				Document doc = new Document();
 //				doc.setCreated(new Date());
 //				doc.setDateDue(new Date());
 //				doc.setSubject("CNT/B&C/01/2013");
 //				doc.setDescription("Contract for the constrution of Hall6");
 //				doc.setPartner("B&C Contactors");
 //				doc.setValue("5.5Mil");
 //				doc.setType(DocumentType.CONTRACT);
 				
 				requestHelper.execute(new ApprovalRequest(AppContext.getUserId(), (Document)doc), new TaskServiceCallback<ApprovalRequestResult>(){
 					@Override
 					public void processResult(ApprovalRequestResult result) {						
 						PlaceRequest request = new PlaceRequest("home").
 								with("type", TaskType.APPROVALREQUESTNEW.getURL());
 						
 						placeManager.revealPlace(request);
 						
 					}
 				});
 			}
 		});
 
 		getView().getClaimLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				//submitRequest(Actions.CLAIM);
 				fireEvent(new ExecTaskEvent(taskId, Actions.CLAIM));
 			}
 		});
 		
 		getView().getStartLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				//submitRequest(Actions.START);
 				assert taskId!=null;
 				fireEvent(new ExecTaskEvent(taskId, Actions.START));
 			}
 		});
 		
 		getView().getSuspendLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				//submitRequest(Actions.SUSPEND);
 				fireEvent(new ExecTaskEvent(taskId, Actions.SUSPEND));
 			}
 		});
 		
 		getView().getResumeLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				//submitRequest(Actions.RESUME);
 				fireEvent(new ExecTaskEvent(taskId, Actions.RESUME));
 			}
 		});
 		
 		getView().getCompleteLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				if(getView().isValid()){
 					
 					Map<String, Value> values = getView().getValues();
 					if(values==null){
 						values = new HashMap<String, Value>();
 					}
 					fireEvent(new CompleteDocumentEvent(taskId, values));
 				}
 				fireEvent(new ExecTaskEvent(taskId, Actions.COMPLETE));
 			}
 		});
 		
 		getView().getDelegateLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				//submitRequest(Actions.DELEGATE);
 				//fireEvent(new ExecTaskEvent(taskId, Actions.DELEGATE));
 				requestHelper.execute(new GetUsersRequest(), new TaskServiceCallback<GetUsersResponse>() {
 					@Override
 					public void processResult(GetUsersResponse result) {
 						showDelegatePopup(result.getUsers());
 					}
 				});
 				
 				//mainPagePresenter.addToPopupSlot(popupPresenter, false);
 			}
 		});
 		
 		getView().getRevokeLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				//submitRequest(Actions.REVOKE);
 				fireEvent(new ExecTaskEvent(taskId, Actions.REVOKE));
 			}
 		});
 		
 		getView().getStopLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				fireEvent(new ExecTaskEvent(taskId, Actions.STOP));				
 			}
 		});
 		
 		getView().getApproveLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				if(getView().isValid()){
 					
 					Map<String, Value> values = getView().getValues();
 					if(values==null){
 							values = new HashMap<String, Value>();
 					}
 					values.put("isApproved", new BooleanValue(true));
 					fireEvent(new CompleteDocumentEvent(taskId, values));
 				}
 				
 			}
 		});
 		
 		getView().getRejectLink().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {				
 				Map<String, Value> values = getView().getValues();
 				if(values==null){
 						values = new HashMap<String, Value>();
 				}
 				values.put("isApproved", new BooleanValue(false));
 				fireEvent(new CompleteDocumentEvent(taskId, values));
 			}
 		});
 		
 	}
 
 
 	protected void showUpload() {
 		uploaderFactory.get(new ServiceCallback<UploadDocumentPresenter>() {
 			@Override
 			public void processResult(UploadDocumentPresenter result) {
 				
 				UploadContext context = new UploadContext();
 				context.setAction(UPLOADACTION.ATTACHDOCUMENT);
 				context.setContext("documentId", documentId+"");
 				context.setContext("userid", AppContext.getUserId());
 				result.setContext(context);
 				addToPopupSlot(result,false);
 			}
 		});
 		
 	}
 
 	protected void delete(Document document) {
 		AppManager.showPopUp("Confirm Delete", 
 				new InlineLabel("Do you want to delete document '"+document.getSubject()+"'"),
 				new OnOptionSelected() {
 					
 					@Override
 					public void onSelect(String name) {
 						if(name.equals("Yes")){
 							requestHelper.execute(new DeleteDocumentRequest(documentId),
 									new TaskServiceCallback<DeleteDocumentResponse>() {
 										@Override
 										public void processResult(
 												DeleteDocumentResponse aResponse) {
 											if(aResponse.isDelete()){
 												fireEvent(new ReloadEvent());
 											}
 											
 										}
 									});
 						}
 					}
 				}, "Yes","Cancel");
 	}
 
 	private void showDelegatePopup(final List<HTUser> users) {
 		final DelegateTaskView view = new DelegateTaskView(users);
 		showDelegatePopup(view);
 		
 	}
 	
 	private void showDelegatePopup(final DelegateTaskView view) {
 		AppManager.showPopUp("Delegate Task", view,
 				new OnOptionSelected() {
 					
 					@Override
 					public void onSelect(String name) {
 						if(name.equals("Ok")){
 							final HTUser user = view.getSelectedUser();
 							if(user!=null && user.getUserId()!=null){
 								
 								DelegationMessageView msgView = new DelegationMessageView(user, doc.getSubject());
 								
 								AppManager.showPopUp("Delegation Message",
 										msgView,
 										new OnOptionSelected() {
 											
 											@Override
 											public void onSelect(String name) {
 												
 												if(name.equals("Back")){
 													showDelegatePopup(view);
 												}else{
 													ExecTaskEvent event = new ExecTaskEvent(taskId, Actions.DELEGATE);
 													
 													Map<String, Value> values = new HashMap<String, Value>();
 													
 													StringValue userValue = new StringValue(null, "targetUserId",user.getUserId());
 													values.put(userValue.getKey(), userValue);
 													event.setValues(values);
 													fireEvent(event);
 												}
 												
 											}
 										}, "Back", "Done");
 								
 							}
 						}
 					}
 				}, "Ok", "Cancel");
 	}
 
 	protected void save(Document document) {
 		document.setValues(getView().getValues());
 		document.getDetails().clear();
 		for(Value val: document.getValues().values()){
 			if(val instanceof GridValue){
 				GridValue gridVal = (GridValue)val;
 				Collection<DocumentLine> lines = gridVal.getValue();
 				for(DocumentLine line: lines){
 					line.setName(gridVal.getKey());
 					document.addDetail(line);
 				}				
 			}
 		}
 		
 //		List<DocumentLine> lnes=document.getDetails().get("lines");
 //		for(DocumentLine l: lnes){
 //			System.err.println(l);
 //		}
 		
 		if (getView().isValid()) {
 			requestHelper.execute(new CreateDocumentRequest(document),
 					new TaskServiceCallback<CreateDocumentResult>() {
 						@Override
 						public void processResult(CreateDocumentResult result) {
 							fireEvent(new ProcessingCompletedEvent());
 							Document saved = result.getDocument();
 							assert saved.getId() != null;
 							bindForm(form, saved);
 						}
 					});
 		}
 		
 	}
 
 	protected void save(String commenttxt) {
 		if(commenttxt==null || commenttxt.trim().isEmpty())
 			return;
 		
 		getView().setComment("");
 		Comment comment = new Comment();
 		comment.setComment(commenttxt);
 		comment.setDocumentId(documentId);
 		comment.setParentId(null);
 		comment.setUserId(AppContext.getUserId());
 		//comment.setCreatedBy(AppContext.getUserId());
 		
 		MultiRequestAction action = new MultiRequestAction();
 		action.addRequest(new SaveCommentRequest(comment));
 		action.addRequest(new GetActivitiesRequest(documentId));
 		
 		requestHelper.execute(action,
 				 new TaskServiceCallback<MultiRequestActionResult>(){
 			@Override
 			public void processResult(MultiRequestActionResult result) {
 				result.get(0);
 				bindActivities((GetActivitiesResponse)result.get(1));
 			}
 		});
 		
 	}
 
 	protected void showEditForm(final MODE mode) {
 		
 		createDocProvider.get(new ServiceCallback<CreateDocPresenter>() {
 			@Override
 			public void processResult(CreateDocPresenter result) {
 				if(mode.equals(MODE.EDIT)){
 					result.setDocumentId(documentId);
 				}
 				
 				addToPopupSlot(result, true);				
 			}
 		});
 	}
 
 	private void clear() {
 		getView().showEdit(false);
 	}
 	
 	@Override
 	protected void onReveal() {
 		super.onReveal();
 		
 		loadData();
 	}
 	
 	private void loadData() {
 		MultiRequestAction requests = new MultiRequestAction();
 		requests.addRequest(new GetDocumentRequest(documentId, taskId));
 		requests.addRequest(new GetCommentsRequest(documentId));
 		requests.addRequest(new GetAttachmentsRequest(documentId));
 		requests.addRequest(new GetActivitiesRequest(documentId));
 		requests.addRequest(new GetFormModelRequest(FormModel.FORMMODEL,taskId,documentId));
 		
 		fireEvent(new ProcessingEvent());
 		if(documentId != null){
 			requestHelper.execute(requests, 
 					new TaskServiceCallback<MultiRequestActionResult>() {
 				
 				public void processResult(MultiRequestActionResult results) {					
 					
 					GetDocumentResult result = (GetDocumentResult)results.get(0);
 					bindDocumentResult(result);
 					
 					GetCommentsResponse commentsResult = (GetCommentsResponse)results.get(1);
 					bindCommentsResult(commentsResult);
 					
 					GetAttachmentsResponse attachmentsresponse = (GetAttachmentsResponse)results.get(2);
 					bindAttachments(attachmentsresponse);
 					
 					GetActivitiesResponse getActivities = (GetActivitiesResponse)results.get(3);
 					bindActivities(getActivities);
 					
 					GetFormModelResponse response = (GetFormModelResponse)results.get(4);					
 					
 					if(!response.getFormModel().isEmpty()){
 						bindForm((Form)response.getFormModel().get(0), result.getDoc());
 						
 					}else{
 						getView().showDefaultFields(true);
 					}
 					
 					fireEvent(new ProcessingCompletedEvent());
 					
 				}	
 			});
 		}
 	}
 
 	protected void bindForm(Form form, Doc doc) {
 		this.doc = doc;
 		this.form = form;
 		if(form.getFields()==null){
 			getView().showDefaultFields(true);
 			return;
 		}
 			
 		Map<String, Value> values = doc.getValues();
 		//System.err.println("Details >>>>> "+doc.getDetails().size());
 		for(Field field: form.getFields()){
 			String name = field.getName();
 			if(name==null || name.isEmpty()){
 				continue;
 			}
 			
 			if(field.getType()==DataType.GRID){
 				List<DocumentLine> lines=doc.getDetails().get(field.getName());
 				if(lines!=null){
 					GridValue value = new GridValue();
 					value.setKey(field.getName());
 					value.setCollectionValue(lines);
 					//System.err.println(">>"+lines.size());
 					field.setValue(value);
 				}
 				continue;
 			}
 			
 			Value value = values.get(name);
 			field.setValue(value);
 			
 			if(value==null){
 				if(name.equals("subject")){
 					value = new StringValue(doc.getSubject());
 				}
 				
 				if(name.equals("description")){
 					value = new StringValue(doc.getDescription());
 				}
 				
 				if(name.equals("docDate")){
 					value = new DateValue(doc.getCreated());
 				}
 				field.setValue(value);
 			}
 				
 		}
 		getView().setForm(form);
 	}
 
 	protected void bindActivities(GetActivitiesResponse response) {
 		Map<Activity, List<Activity>> activitiesMap = response.getActivityMap();
 		bindActivities(activitiesMap);		
 	}
 	
 	public void bindActivities(Map<Activity, List<Activity>> activitiesMap){
 		setInSlot(ACTIVITY_SLOT, null);
 		
 		Set<Activity> keyset = activitiesMap.keySet();
 		List<Activity> activities= new ArrayList<Activity>();
 		activities.addAll(keyset);
 		Collections.sort(activities);
 		Collections.reverse(activities);
 		
 		for(Activity activity: activities){
 			bind(activity,false);	
 			List<Activity> children = activitiesMap.get(activity);	
 			if(children!=null){
 				for(Activity child: children){
 					bind(child, true);
 				}
 				
 			}
 		}		
 		this.activities +=activitiesMap.size();
 	}
 
 	private void bind(final Activity child, boolean isChild) {
 				
 		if(child instanceof Comment)
 		commentPresenterFactory.get(new ServiceCallback<CommentPresenter>() {
 			@Override
 			public void processResult(CommentPresenter result) {
 				result.setComment((Comment)child);
 				
 				addToSlot(ACTIVITY_SLOT,result);
 			}
 		});
 		
 		
 		if(child instanceof Notification)
 			notePresenterFactory.get(new ServiceCallback<NotePresenter>() {				
 				@Override
 				public void processResult(NotePresenter result) {
 					result.setNotification((Notification)child, false);
 					addToSlot(ACTIVITY_SLOT, result);
 				}
 			});
 			
 		
 	}
 
 	protected void bindAttachments(GetAttachmentsResponse attachmentsresponse) {
 		
 		List<Attachment> attachments = attachmentsresponse.getAttachments();
 		
 		if(attachments.size()>0){
 			getView().getDivAttachment().removeClassName("hidden");
 			getView().getSpnAttachmentNo().setInnerText("Attachments (" + attachments.size() +")");
 			fireEvent(new AfterAttachmentReloadedEvent(documentId));
 		}
 		
 		setInSlot(ATTACHMENTS_SLOT, null);//clear
 		for(final Attachment attachment: attachments){
 			attachmentPresenterFactory.get(new ServiceCallback<AttachmentPresenter>() {
 				@Override
 				public void processResult(AttachmentPresenter result) {
 					result.setAttachment(attachment);
 					addToSlot(ATTACHMENTS_SLOT, result);
 				}
 			});
 		}
 		
 	}
 
 	protected void bindCommentsResult(GetCommentsResponse commentsResult) {
 		setInSlot(ACTIVITY_SLOT, null);
 		List<Comment> comments = commentsResult.getComments();
 		
 		this.activities += comments.size();
 		
 		getView().getSpnActivityNo().setInnerText("Activity("+this.activities+")");
 		for(final Comment comment: comments){
 			commentPresenterFactory.get(new ServiceCallback<CommentPresenter>() {
 				@Override
 				public void processResult(CommentPresenter result) {
 					result.setComment(comment);
 					addToSlot(ACTIVITY_SLOT,result);
 				}
 			});
 		}
 	}
 
 	protected void bindDocumentResult(GetDocumentResult result) {
 
 		this.doc = result.getDoc();
 		Map<String, Value> vals= doc.getValues();
 		
 		long docId=0l;
 		
 		String taskDisplayName="";
 		if(doc instanceof Document){
 			docId = (Long)doc.getId();
 			
 		}else{
 			HTSummary task = ((HTSummary)doc); 
 			docId = task.getDocumentRef();
 			this.taskId = task.getId();
 			
 			if(task.getName()!=null)
 				taskDisplayName = task.getName();
 			
 			HTask humantask = (HTask)task;
 			Delegate delegate = humantask.getDelegate();
 			if(delegate!=null){
 				getView().setDelegate(delegate);
 			}
 		}
 		
 		this.documentId = docId;
 		
 		Date created = doc.getCreated();
 		String subject = doc.getSubject();
 		
 		Date docDate = doc.getDocumentDate();					
 		String description = doc.getDescription();
 		Integer priority = doc.getPriority();	
 		
 		String partner = null;
 		String value= null;
 		DocumentType docType = null;
 		DocStatus status = null;
 		
 		String type=null;
 		
 		if(doc instanceof Document){
 			Document d = (Document)doc;
 			value = d.getValue();
 			partner = d.getPartner();
 			docType= d.getType();
 			type = docType.getDisplayName();
 			
 			status = d.getStatus();
 		}else{
 			HTask task = (HTask)doc;
 			type = task.getName();	
 			status = task.getDocStatus();
 		}
 		
 		if(value==null){
 			Value val = vals.get("value");
 			if(val!=null)
 				value = ((StringValue)val).getValue();
 		}
 		
 		if(partner==null){
 			Value val = vals.get("partner");
 			if(val!=null)
 				value = ((StringValue)val).getValue();
 		}
 		
 		
 		getView().setValues(doc.getOwner(),created,
 				type, subject, docDate,  value, partner, description, priority,status, documentId,
 				taskDisplayName);
 		
 		if(status==DocStatus.DRAFTED){
 			getView().showEdit(true);
 		}else{
 			getView().showEdit(false);
 			clear();
 		}
 		
 		//get document actions - if any
 		AfterDocumentLoadEvent e = new AfterDocumentLoadEvent(documentId, taskId);
 		fireEvent(e);		
 		if(e.getValidActions()!=null){
 			getView().setValidTaskActions(e.getValidActions());
 		}	
 		
 		Long processInstanceId = doc.getProcessInstanceId();
 		
 		if(processInstanceId!=null){
 			//System.err.println("Loading activities for ProcessInstanceId = "+processInstanceId);
 			requestHelper.execute(new GetProcessStatusRequest(processInstanceId),
 					new TaskServiceCallback<GetProcessStatusRequestResult>() {
 				@Override
 				public void processResult(
 						GetProcessStatusRequestResult result) {
 					List<NodeDetail> details = result.getNodes();
 					setProcessState(details);
 				}
 			});
 		}
 	}
 
 	public void setProcessState(List<NodeDetail> states){
 		getView().setStates(states);
 	}
 
 	public void setDocId(Long documentId, Long taskId) {
 		this.documentId=documentId;
 		this.taskId = taskId;
 	}
 	
 	@Override
 	protected void onHide() {
 		super.onHide();
 		this.unbind();
 	}
 
 	@Override
 	public void onReloadDocument(ReloadDocumentEvent event) {
 		if(event.getDocumentId()==this.documentId){
 			loadData();
 		}
 	}
 
 	@Override
 	public void onActivitiesLoad(ActivitiesLoadEvent event) {
 		bindActivities(event.getActivitiesMap());
 	}
 
 	@Override
 	public void onReloadAttachments(ReloadAttachmentsEvent event) {
 		reloadAttachments();
 	}
 
 	private void reloadAttachments() {
 		requestHelper.execute(new GetAttachmentsRequest(documentId),
 				new TaskServiceCallback<GetAttachmentsResponse>() {
 			@Override
 			public void processResult(GetAttachmentsResponse result) {
 				bindAttachments(result);
 			}
 		});
 	}
 	
 	/**
 	 * Runtime - Delete Row
 	 * -Enable save/ edit mode
 	 */
 	@Override
 	public void onDeleteLine(DeleteLineEvent event) {
 		DocumentLine line = event.getLine();
 		if(line.getId()==null){
 			return;
 		}
 		AppContext.fireEvent(new ProcessingEvent("Deleting ..."));
 		requestHelper.execute(new DeleteLineRequest(line), 
 				new TaskServiceCallback<DeleteLineResponse>() {
 			@Override
 			public void processResult(DeleteLineResponse result) {
 				AppContext.fireEvent(new ProcessingCompletedEvent());
 			}
 		});
 	}
 }
