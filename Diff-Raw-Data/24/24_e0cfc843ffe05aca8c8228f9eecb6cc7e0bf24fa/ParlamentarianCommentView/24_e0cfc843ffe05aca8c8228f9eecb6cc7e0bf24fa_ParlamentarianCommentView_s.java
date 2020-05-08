 package cl.votainteligente.inspector.client.views;
 
 import cl.votainteligente.inspector.client.presenters.ParlamentarianCommentPresenter;
 import cl.votainteligente.inspector.client.uihandlers.ParlamentarianCommentUiHandlers;
 import cl.votainteligente.inspector.model.Parlamentarian;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.*;
 
 public class ParlamentarianCommentView extends ViewWithUiHandlers<ParlamentarianCommentUiHandlers> implements ParlamentarianCommentPresenter.MyView {
 	private static ParlamentarianCommentViewUiBinder uiBinder = GWT.create(ParlamentarianCommentViewUiBinder.class);
 	interface ParlamentarianCommentViewUiBinder extends UiBinder<Widget, ParlamentarianCommentView> {}
 	private final Widget widget;
 
 	@UiField ListBox parlamentarianList;
 	@UiField TextBox commentSubject;
 	@UiField TextArea commentBody;
 	@UiField Button commentSubmit;
 	@UiField Button commentClear;
 
 	public ParlamentarianCommentView() {
 		widget = uiBinder.createAndBindUi(this);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public Long getSelectedParlamentarianId() {
 		return new Long(parlamentarianList.getValue(parlamentarianList.getSelectedIndex()));
 	}
 
 	@Override
	public void setSelectedParlamentarian(Long parlamentarianId) {
		if (parlamentarianId < Integer.MAX_VALUE) {
			parlamentarianList.setSelectedIndex(parlamentarianId.intValue());
		}
 	}
 
 	@Override
 	public void setupParlamentarianList() {
 		parlamentarianList.clear();
 		parlamentarianList.addItem("Seleccione un parlamentario");
 	}
 
 	@Override
 	public void addParlamentarian(Parlamentarian parlamentarian) {
 		parlamentarianList.addItem(parlamentarian.getLastName() + ", " + parlamentarian.getFirstName(), parlamentarian.getId().toString());
 	}
 
 	@Override
 	public String getCommentSubject() {
 		return commentSubject.getText();
 	}
 
 	@Override
 	public String getCommentBody() {
 		return commentBody.getText();
 	}
 
 	@Override
 	public void clearForm() {
 		parlamentarianList.setSelectedIndex(0);
 		commentSubject.setText("");
 		commentBody.setText("");
 	}
 
 	@UiHandler("commentSubmit")
 	public void onCommentSubmitClick(ClickEvent event) {
 		getUiHandlers().saveParlamentarianComment();
 	}
 
 	@UiHandler("commentClear")
 	public void onCommentClearClick(ClickEvent event) {
 		clearForm();
 	}
 }
