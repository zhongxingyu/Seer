 package com.duggan.workflow.client.ui.document;
 
 import static com.duggan.workflow.client.ui.document.GenericDocumentPresenter.ACTIVITY_SLOT;
 import static com.duggan.workflow.client.ui.document.GenericDocumentPresenter.ATTACHMENTS_SLOT;
 import static com.duggan.workflow.client.ui.util.DateUtils.CREATEDFORMAT;
 
 import java.util.Date;
 import java.util.List;
 
 import com.duggan.workflow.client.ui.upload.Uploader;
 import com.duggan.workflow.client.ui.wfstatus.ProcessState;
 import com.duggan.workflow.shared.model.Actions;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.DocType;
 import com.duggan.workflow.shared.model.HTUser;
 import com.duggan.workflow.shared.model.NodeDetail;
 import com.duggan.workflow.shared.model.Priority;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.TextArea;
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
 
 	@UiField
 	SpanElement spnValue;	
 	@UiField
 	SpanElement spnPartner;
 	@UiField
 	SpanElement spnDescription;
 
 	@UiField Anchor aSimulate;	
 	@UiField Anchor aEdit;	
 	@UiField Anchor aClaim;
 	@UiField Anchor aStart;
 	@UiField Anchor aSuspend;
 	@UiField Anchor aResume;
 	@UiField Anchor aComplete;
 	@UiField Anchor aDelegate;
 	@UiField Anchor aRevoke;
 	@UiField Anchor aStop;
 	@UiField Anchor aForward;	
 	@UiField Anchor aApprove;	
 	@UiField Anchor aReject;
 	@UiField HTMLPanel statusContainer;
 	@UiField Element eOwner;
 	@UiField SpanElement spnPriority;
 	@UiField SpanElement spnStatus;
 	@UiField HTMLPanel panelActivity;
 	@UiField Uploader uploader;
 	@UiField HTMLPanel panelAttachments;
 	@UiField Anchor aSaveComment;
	@UiField TextArea txtComments;
 	
 	
 	@Inject
 	public GenericDocumentView(final Binder binder) {
 		widget = binder.createAndBindUi(this);
 		UIObject.setVisible(aEdit.getElement(), false);
 		aEdit.getElement().setAttribute("type","button");
 		aSimulate.getElement().setAttribute("type","button");
 		UIObject.setVisible(aForward.getElement(), false);
 		aApprove.getElement().setAttribute("type", "button");
 		aReject.getElement().setAttribute("type", "button");
 		aForward.getElement().setAttribute("type", "button");
 		aForward.getElement().setAttribute("alt", "Forward for Approval");
		txtComments.getElement().setAttribute("placeholder","write comments, Clarifications and Questions ...");
 		disableAll();
 		statusContainer.add(new InlineLabel("Nothing to show"));
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
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	public void setValues(HTUser createdBy, Date created, DocType type, String subject,
 			Date docDate, String value, String partner, String description, Integer priority,DocStatus status) {
 		disableAll();
 		if(createdBy!=null){
 			eOwner.setInnerText(createdBy.getName());
 		}
 		if (created != null)
 			spnCreated.setInnerText(CREATEDFORMAT.format(created));
 
 		if (type != null)
 			spnDocType.setInnerText(type.getDisplayName());
 
 		if (subject != null)
 			spnSubject.setInnerText(subject);
 
 		if (docDate != null)
 			//spnDocDate.setInnerText(DATEFORMAT.format(docDate));
 
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
 		
 		if(status==null || status==DocStatus.DRAFTED){
 			showForward(true);
 		}else{
 			showForward(false);
 		}
 		
 		if(status!=null)
 			spnStatus.setInnerText(status.name());
 		
 		if(priority!=null){
 			Priority prty = Priority.get(priority);
 			
 			switch (prty) {
 			case CRITICAL:
 				spnPriority.addClassName("label-important");
 				spnPriority.setInnerText("Urgent");
 				break;
 
 			case HIGH:
 				spnPriority.addClassName("label-warning"); //
 				spnPriority.setInnerText("Important");
 				break;
 
 			default:
 				spnPriority.addClassName("hide");
 				break;
 			}
 		}
 		
 	}
 		
 	public void setValidTaskActions(List<Actions> actions){
 		
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
 	
 	
 	public HasClickHandlers getEditButton(){
 		return aEdit;
 	}
 
 	@Override
 	public void showEdit(boolean displayed) {
 		UIObject.setVisible(aEdit.getElement(), displayed);
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
 		return aSaveComment;
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
 		
		return txtComments.getValue();
 	}
 	
 }
