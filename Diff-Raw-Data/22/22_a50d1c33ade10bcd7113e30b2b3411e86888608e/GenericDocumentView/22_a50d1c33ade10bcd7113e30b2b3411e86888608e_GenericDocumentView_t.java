 package com.duggan.workflow.client.ui.document;
 
 import static com.duggan.workflow.client.ui.document.GenericDocumentPresenter.ACTIVITY_SLOT;
 import static com.duggan.workflow.client.ui.document.GenericDocumentPresenter.ATTACHMENTS_SLOT;
 import static com.duggan.workflow.client.ui.util.DateUtils.DATEFORMAT;
 import static com.duggan.workflow.client.ui.util.DateUtils.TIMEFORMAT12HR;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import com.duggan.workflow.client.ui.component.CommentBox;
 import com.duggan.workflow.client.ui.document.form.FormPanel;
 import com.duggan.workflow.client.ui.upload.custom.Uploader;
 import com.duggan.workflow.client.ui.util.DateUtils;
 import com.duggan.workflow.client.ui.wfstatus.ProcessState;
 import com.duggan.workflow.shared.model.Actions;
 import com.duggan.workflow.shared.model.Delegate;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.HTUser;
 import com.duggan.workflow.shared.model.NodeDetail;
 import com.duggan.workflow.shared.model.Priority;
 import com.duggan.workflow.shared.model.Value;
 import com.duggan.workflow.shared.model.form.Form;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 
 public class GenericDocumentView extends ViewImpl implements
 		GenericDocumentPresenter.MyView {
 
 	private final Widget widget;
 
 	public interface Binder extends UiBinder<Widget, GenericDocumentView> {
 	}
 
 	@UiField
 	SpanElement spnCreated;
 	@UiField
 	SpanElement spnDocType;
 	@UiField
 	SpanElement spnSubject;
 	
 	@UiField SpanElement spnDate;
 
 	@UiField
 	SpanElement spnValue;	
 	@UiField
 	SpanElement spnPartner;
 	@UiField
 	SpanElement spnDescription;
 
 	@UiField Anchor aSimulate;	
 	@UiField Anchor aEdit;	
 	@UiField Anchor aSave;
 	@UiField Anchor aDelete;
 	@UiField Anchor aClaim;
 	@UiField Anchor aStart;
 	@UiField Anchor aSuspend;
 	@UiField Anchor aResume;
 	@UiField Anchor aComplete;
 	@UiField Anchor aDelegate;
 	@UiField Anchor aRevoke;
 	@UiField Anchor aStop;
 	@UiField Anchor aForward;
 	@UiField Anchor aProcess;
 	@UiField Anchor aApprove;	
 	@UiField Anchor aReject;
 	@UiField HTMLPanel statusContainer;
 	@UiField HTMLPanel divProcess;
 	@UiField Element eOwner;
 	@UiField Element eTitle;
 	//@UiField Element eDelegate;
 	@UiField HTMLPanel spnPriority;
 	@UiField SpanElement spnAttachmentNo;
 	@UiField SpanElement spnActivityNo;
 	@UiField DivElement divAttachment;
 	@UiField SpanElement spnStatusBody;
 	@UiField HTMLPanel panelActivity;
 	@UiField Uploader uploader;
 	@UiField HTMLPanel panelAttachments;
 	@UiField Anchor aAttach1;
 	@UiField Anchor aAttach2;
 	@UiField Anchor aShowProcess;
 	@UiField CommentBox commentPanel;
 	
 	@UiField DivElement btnGroup;
 	@UiField DivElement divDate;
 	@UiField DivElement divDesc;
 	@UiField DivElement divPartner;
 	@UiField DivElement divValue;
 	@UiField DivElement divContent;
 	
 	@UiField HTMLPanel fldForm;
 	
 	FormPanel formPanel;
 	
 	String url=null;
 	
 	List<Actions> validActions = null;
 	private String timeDiff;
 	
 	@Inject
 	public GenericDocumentView(final Binder binder) {
 		widget = binder.createAndBindUi(this);
 		UIObject.setVisible(aEdit.getElement(), false);
 		aEdit.getElement().setAttribute("type","button");
 		aEdit.getElement().setAttribute("data-toggle","tooltip");
 		aSimulate.getElement().setAttribute("type","button");
 		UIObject.setVisible(aForward.getElement(), false);
 		aApprove.getElement().setAttribute("type", "button");
 		aReject.getElement().setAttribute("type", "button");
 		aForward.getElement().setAttribute("type", "button");
 		panelActivity.getElement().setAttribute("id", "panelactivity");
 		aForward.getElement().setAttribute("alt", "Forward for Approval");
 		aShowProcess.setVisible(false);
 		
 		showDefaultFields(false);
 		disableAll();//Hide all buttons
 		
		showOneButton();
		
 		
 		aShowProcess.removeStyleName("gwt-Anchor");
 		
 		aShowProcess.addClickHandler(new ClickHandler() {
 		@Override
 			public void onClick(ClickEvent event) {
 				if(url!=null)
 				Window.open(url, "Business Process", null);
 			}
 		});
 		
 		aEdit.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				formPanel.setReadOnly(false);
 				UIObject.setVisible(btnGroup, false);
 				UIObject.setVisible(aForward.getElement(), false);
 				UIObject.setVisible(aEdit.getElement(), false);
 				UIObject.setVisible(aSave.getElement(), true);
 				
 			}
 		});
 		
 		aProcess.addClickHandler(new ClickHandler() {
 			private boolean isClicked=true;
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if(isClicked){
 					aProcess.addStyleName("disabled");
 					divProcess.removeStyleName("hidden");
 					divContent.removeClassName("span12");
 					divContent.addClassName("span9");
 					isClicked=false;
 				}else{
 					aProcess.removeStyleName("disabled");
 					divProcess.addStyleName("hidden");
 					divContent.removeClassName("span9");
 					divContent.addClassName("span12");
 					isClicked=true;
 				}
 			}
 		});
 	
 		aSave.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				UIObject.setVisible(btnGroup, true);
 				UIObject.setVisible(aForward.getElement(),true);
 				UIObject.setVisible(aEdit.getElement(), true);
 				UIObject.setVisible(aSave.getElement(), false);
 			}
 		});
 		
 		UIObject.setVisible(aSave.getElement(), false);
 		statusContainer.add(new InlineLabel("Nothing to show"));
 	}
 
	private void showOneButton() {

	}

 	private void disableAll() {
 		show(aClaim, false);
 		show(aStart, false);
 		show(aSuspend, false);
 		show(aResume, false);
 		show(aComplete, false);
 		show(aDelegate, false);
 		show(aReject, false);
 		show(aRevoke, false);
 		show(aStop, false);
 		show(aForward, false);
 		show(aApprove, false);
		show(aAttach1, false);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 	
 	public void setForm(Form form){
 		fldForm.clear();
 		
 		if(form==null || form.getFields()==null)
 			return;
 		
 		//hide static fields		
 		showDefaultFields(false);
 		
 		formPanel = new FormPanel(form);
 		
 		if(validActions!=null){
 			if(validActions.contains(Actions.COMPLETE)){
 				formPanel.setReadOnly(false);
 			}else{
 				formPanel.setReadOnly(true);
 			}
 		}
 		fldForm.add(formPanel);
 	}
 
 	public void showDefaultFields(boolean show) {
 		UIObject.setVisible(divDate, show);
 		UIObject.setVisible(divDesc, show);
 		UIObject.setVisible(divPartner, show);
 		UIObject.setVisible(divValue, show);
 	}
 
 	public void setValues(HTUser createdBy, Date created, String type, String subject,
 			Date docDate, String value, String partner, String description, 
 			Integer priority,DocStatus status, Long id, String taskDisplayName) {
 		disableAll();
 		if(createdBy!=null){
 			if(createdBy.getName()!=null)
 				eOwner.setInnerText(createdBy.getName());
 			else
 				eOwner.setInnerText(createdBy.getUserId());
 		}
 		
 		if (created!= null)
 			timeDiff =  DateUtils.getTimeDifferenceAsString(created);
 			if(timeDiff != null)
 			spnCreated.setInnerText(TIMEFORMAT12HR.format(created)+" ("+timeDiff+" )");
 
 		if(!(taskDisplayName==null || taskDisplayName.equals(""))){
 			spnDocType.setInnerText(taskDisplayName);
 		}else if (type != null){
 			spnDocType.setInnerText(type);
 		}
 
 		if (subject != null){
 			spnSubject.setInnerText(subject);
 		}
 
 		if (docDate != null){
 			spnDate.setInnerText(DATEFORMAT.format(docDate));
 		}
 
 		if(value!=null){
 			spnValue.setInnerText(value);
 			//UIObject.setVisible(divValue, true);
 		}
 		
 		if (partner != null){
 			//UIObject.setVisible(divPartner, true);
 			spnPartner.setInnerText(partner);
 		}			
 
 		if (description != null)
 			spnDescription.setInnerText(description);
 		
 		if(status==DocStatus.DRAFTED){
 			showForward(true);
 		}else{
 			showForward(false);
 		}
 		
 		if(status!=null){
 			spnStatusBody.setInnerText(status.name());
 			if(status==DocStatus.APPROVED){
 				spnStatusBody.setClassName("label label-success");
 			}
 			
 			if(status==DocStatus.REJECTED){
 				spnStatusBody.setClassName("label label-danger");
 			}
 			
 		}
 		
 		if(priority!=null){
 			Priority prty = Priority.get(priority);
 			
 			switch (prty) {
 			case CRITICAL:
 				spnPriority.addStyleName("label-important");
 				//spnPriority.setInnerText("Urgent");
 				break;
 
 			case HIGH:
 				spnPriority.addStyleName("label-warning"); //
 				//spnPriority.setInnerText("Important");
 				break;
 
 			default:
 				spnPriority.addStyleName("hide");
 				break;
 			}
 		}
 		
 		this.url=null;
 		if(id!=null){
 			String root = GWT.getModuleBaseURL();
 			root = root.replaceAll("/gwtht", "");
 			this.url = root+"getreport?did="+id+"&ACTION=GETDOCUMENTPROCESS";
 			aShowProcess.setVisible(true);
 		}
 		
 	}
 		
 	public void setValidTaskActions(List<Actions> actions){
 		this.validActions = actions;
 		if(actions!=null)
 		for(Actions action : actions){		
 			Anchor target=null;
 			switch(action){
 			case CLAIM:
 				target=aClaim;
 				break;
 			case COMPLETE:
 				//target=aComplete;
 				show(aApprove);
 				show(aReject);
 				break;
 			case DELEGATE:
 				target=aDelegate;
 				break;
 			case FORWARD:
 				target=aForward;
 				break;
 			case RESUME:
 				target=aResume;
 				break;
 			case REVOKE:
 				target=aRevoke;
 				break;
 			case START:
 				target=aStart;
 				break;
 			case STOP:
 				target=aStop;
 				break;
 			case SUSPEND:
 				target=aSuspend;
 				break;
 			}
 			
 			if(target!=null){
 				show(target);
 			}
 		}
 		
 	}
 	
 	@Override
 	public void showForward(boolean show) {
 		show(aForward, show);
 	}
 
 	@Override
 	public void show(boolean IsShowapprovalLink, boolean IsShowRejectLink) {
 		
 		show(aApprove, IsShowapprovalLink);
 		show(aReject, IsShowRejectLink);
 	}
 	
 
 	public HasClickHandlers getSimulationBtn(){
 		return aSimulate;
 	}
 	
 	
 	public HasClickHandlers getSaveButton(){
 		return aSave;
 	}
 	
 	public HasClickHandlers getDeleteButton(){
 		return aDelete;
 	}
 	
 	@Override
 	public void showEdit(boolean displayed) {
 		UIObject.setVisible(aEdit.getElement(), displayed);
 		UIObject.setVisible(aDelete.getElement(), displayed);
 	}
 
 	@Override
 	public void setStates(List<NodeDetail> states) {
 		statusContainer.clear();
 		if(states!=null){
 			NodeDetail detail = null;
 			for(NodeDetail state:states){
 				if(state.isEndNode())
 					detail = state;
 				else
 					statusContainer.add(new ProcessState(state));
 				
 			}
 			
 			//ensure end node always comes last
 			if(detail!=null){
 				statusContainer.add(new ProcessState(detail));
 			}
 		}
 	}
 	
 	public void show(Anchor target){
 		show(target,true);
 	}
 	public void show(Anchor target, boolean isvisible){
 		if(isvisible){
 			target.removeStyleName("hidden");
 		}
 		UIObject.setVisible(target.getElement(), isvisible);
 		
 	}
 
 	public HasClickHandlers getClaimLink(){
 		return aClaim;
 	}
 	
 	public HasClickHandlers getStartLink(){
 		return aStart;
 	}
 	
 	public HasClickHandlers getSuspendLink(){
 		return aSuspend;
 	}
 	
 	public HasClickHandlers getResumeLink(){
 		return aResume;
 	}
 	
 	public HasClickHandlers getCompleteLink(){
 		return aComplete;
 	}
 	
 	public HasClickHandlers getDelegateLink(){
 		return aDelegate;
 	}
 	
 	public HasClickHandlers getRevokeLink(){
 		return aRevoke;
 	}
 	
 	public HasClickHandlers getStopLink(){
 		return aStop;
 	}
 	
 	public HasClickHandlers getForwardForApproval(){
 		return aForward;
 	}
 
 	public HasClickHandlers getApproveLink(){
 		return aApprove;
 	}
 	
 	public HasClickHandlers getRejectLink(){
 		return aReject;
 	}
 	
 	public Anchor getSaveCommentButton(){
 		return commentPanel.getaSaveComment();
 	}
 	
 	@Override
 	public void setInSlot(Object slot, Widget content) {
 
 		if(slot==ACTIVITY_SLOT){
 			panelActivity.clear();
 			if(content!=null){
 				panelActivity.add(content);
 			}
 		}if(slot==ATTACHMENTS_SLOT){
 			panelAttachments.clear();	
 			if(content!=null){
 				panelAttachments.add(content);
 			}
 		}else{		
 			super.setInSlot(slot, content);
 		}
 	}
 
 	@Override
 	public void addToSlot(Object slot, Widget content) {
 		
 		if(slot==ACTIVITY_SLOT){
 			
 			if(content!=null){
 				panelActivity.add(content);
 			}
 		}if(slot==ATTACHMENTS_SLOT){
 			if(content!=null){
 				panelAttachments.add(content);
 			}
 		}else{
 			super.addToSlot(slot, content);
 		}
 		
 	}
 
 	@Override
 	public Uploader getUploader() {
 		return uploader;
 	}
 
 	@Override
 	public String getComment() {
 		
 		return commentPanel.getCommentBox().getValue();
 	}
 
 	@Override
 	public void setComment(String string) {
 		commentPanel.getCommentBox().setText("");
 	}
 	
 	public HasClickHandlers getUploadLink(){
 		return aAttach1;
 	}
 	
 	public HasClickHandlers getUploadLink2(){
 		return aAttach2;
 	}
 	
 	public SpanElement getSpnAttachmentNo() {
 		return spnAttachmentNo;
 	}
 	
 	public SpanElement getSpnActivityNo() {
 		return spnActivityNo;
 	}
 	
 	public DivElement getDivAttachment() {
 		return divAttachment;
 	}
 
 	@Override
 	public boolean isValid() {
 		if(formPanel==null){
 			return true;
 		}
 		return formPanel.isValid();
 	}
 
 	@Override
 	public Map<String, Value> getValues() {
 		if(formPanel==null){
 			return null;
 		}
 		
 		return formPanel.getValues();
 	}
 
 	@Override
 	public void setDelegate(Delegate delegate) {
 		delegate.getCreated();
 		delegate.getDelegateTo();
 		delegate.getUserId();
 		//eDelegate.setInnerText(delegate.getDelegateTo());
 	}
 }
