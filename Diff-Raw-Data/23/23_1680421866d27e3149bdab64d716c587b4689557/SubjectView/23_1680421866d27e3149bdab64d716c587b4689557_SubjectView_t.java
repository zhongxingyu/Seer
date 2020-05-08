 package org.seamoo.webapp.client.admin.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.seamoo.entities.Subject;
 import org.seamoo.webapp.client.admin.SubjectListPresenter.SubjectDisplay;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SubjectView extends Composite implements SubjectDisplay {
 	interface SubjectViewUiBinder extends UiBinder<Widget, SubjectView> {
 	}
 
 	private static final SubjectViewUiBinder binder = GWT.create(SubjectViewUiBinder.class);
 
 	@UiField
 	Hidden hiddenAutoId;
 
 	@UiField
 	Image imgLogo;
 	@UiField
 	Button buttonSelectLogo;
 
 	@UiField
 	HTMLPanel divView;
 	@UiField
 	Anchor anchorName;
 	@UiField
 	Anchor buttonEdit;
 	@UiField
 	Anchor buttonRemove;
 	@UiField
 	Anchor buttonPreview;
 	@UiField
 	Label labelDescription;
 	@UiField
 	Label labelEnabled;
 
 	@UiField
 	HTMLPanel divEdit;
 	@UiField
 	TextBox textboxName;
 	@UiField
 	TextArea textareaDescription;
 	@UiField
 	CheckBox checkboxEnabled;
 	@UiField
 	Button buttonCreate;
 	@UiField
 	Button buttonSave;
 	@UiField
 	Button buttonCancel;
 	@UiField
 	HTMLPanel buttonContainer;
 
 	List<SubjectDisplayEventListener> listeners;
 
 	private Subject subject;
 
 	public SubjectView() {
 		initWidget(binder.createAndBindUi(this));
 		listeners = new ArrayList<SubjectDisplayEventListener>();
 		final SubjectView me = this;
 		buttonEdit.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent clickevent) {
 				// TODO Auto-generated method stub
 				for (SubjectDisplayEventListener l : listeners)
 					l.edit(me);
 			}
 		});
 
 		buttonCancel.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent clickevent) {
 				// TODO Auto-generated method stub
 				for (SubjectDisplayEventListener l : listeners)
 					l.cancelEdit(me);
 			}
 		});
 
 		buttonSave.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent clickevent) {
 				// TODO Auto-generated method stub
 				for (SubjectDisplayEventListener l : listeners)
 					l.update(me, me.getEditedSubject());
 			}
 		});
 
 		buttonCreate.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent clickevent) {
 				// TODO Auto-generated method stub
 				for (SubjectDisplayEventListener l : listeners)
 					l.create(me, me.getEditedSubject());
 			}
 		});
 
 		buttonRemove.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent clickevent) {
 				// TODO Auto-generated method stub
 				for (SubjectDisplayEventListener l : listeners)
 					l.delete(me, me.getSubject());
 			}
 		});

		buttonSelectLogo.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent clickevent) {
				// TODO Auto-generated method stub
				for (SubjectDisplayEventListener l : listeners)
					l.selectLogo(me, me.getLogoUrl());
			}
		});
 	}
 
 	public Subject getEditedSubject() {
 		Long autoId = null;
 		String autoIdStr = hiddenAutoId.getValue();
 		if (autoIdStr != null && !autoIdStr.equals(""))
 			autoId = Long.parseLong(hiddenAutoId.getValue());
 
 		return new Subject(autoId, textboxName.getText(), imgLogo.getUrl(), textareaDescription.getText(),
 				checkboxEnabled.getValue());
 
 	}
 
 	public void setMode(SubjectDisplayMode mode) {
 		boolean isView = mode == SubjectDisplayMode.VIEW;
 		boolean isCreate = mode == SubjectDisplayMode.CREATE;
 		boolean isEdit = mode == SubjectDisplayMode.EDIT;
 		buttonContainer.setVisible(isEdit || isCreate);
 		buttonCreate.setVisible(isCreate);
 		buttonSave.setVisible(isEdit);
 		buttonCancel.setVisible(isEdit);
 		divView.setVisible(isView);
 		divEdit.setVisible(isEdit || isCreate);
 		buttonSelectLogo.setVisible(isCreate || isEdit);
 	}
 
 	public Subject getSubject() {
 		return subject;
 	}
 
 	public void setSubject(Subject subject) {
 		this.subject = subject;
 		hiddenAutoId.setValue(subject.getAutoId() != null ? subject.getAutoId().toString() : "");
 		textboxName.setText(subject.getName());
 		anchorName.setText(subject.getName());
 		imgLogo.setUrl(subject.getLogoUrl());
 		textareaDescription.setText(subject.getDescription());
 		labelDescription.setText(subject.getDescription());
 		labelEnabled.setText(subject.isEnabled() ? "Enabled" : "Disabled");
 
 		checkboxEnabled.setValue(subject.isEnabled());
 	}
 
 	@Override
 	public void addEventListener(SubjectDisplayEventListener listener) {
 		// TODO Auto-generated method stub
 		listeners.add(listener);
 	}
 
 	@Override
 	public String getLogoUrl() {
 		// TODO Auto-generated method stub
 		return imgLogo.getUrl();
 	}
 
 	@Override
 	public void setLogoUrl(String url) {
 		// TODO Auto-generated method stub
 		imgLogo.setUrl(url);
 	}
 }
