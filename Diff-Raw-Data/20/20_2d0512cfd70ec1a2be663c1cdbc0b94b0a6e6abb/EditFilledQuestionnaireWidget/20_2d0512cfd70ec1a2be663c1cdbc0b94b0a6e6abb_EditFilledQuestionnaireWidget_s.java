 package com.szas.server.gwt.client.widgets;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Label;
 import com.szas.data.FieldDataTuple;
 import com.szas.data.FieldTextAreaDataTuple;
 import com.szas.data.FieldTextAreaTuple;
 import com.szas.data.FieldTextBoxDataTuple;
 import com.szas.data.FieldTextBoxTuple;
 import com.szas.data.FieldTuple;
 import com.szas.data.FilledQuestionnaireTuple;
 import com.szas.data.QuestionnaireTuple;
 import com.szas.server.gwt.client.sync.StaticGWTSyncer;
 import com.szas.server.gwt.client.universalwidgets.FieldWidget;
 import com.szas.server.gwt.client.universalwidgets.UniversalWidget;
 import com.szas.sync.local.LocalDAO;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 public class EditFilledQuestionnaireWidget extends UniversalWidget<FilledQuestionnaireTuple> {
 
 	
 	public EditFilledQuestionnaireWidget(FilledQuestionnaireTuple tuple, boolean edit) {
 		super(tuple);
 		this.edit = edit;
 		initWidget();
 	}
 
 
 	private final boolean edit;
 	public static final String NAME = "filledQuestionnaire";
 	public static final String NAME_EDIT = "editFilledQuestionnaire";
 	public static final String NAME_NEW = "newFilledQuestionnaire";
 	private static EditFilledQuestionnaireWidgetUiBinder uiBinder = GWT
 			.create(EditFilledQuestionnaireWidgetUiBinder.class);
 	@UiField Label questionnaireName;
 	@UiField Button deleteButton;
	@UiField Button saveButton;
 	@UiField Button editButton;
 	@UiField VerticalPanel verticalPanel;
 	
 	private ArrayList<FieldWidget> fieldDataWidgets;
 	private ArrayList<FieldTuple> fields;
 	private ArrayList<FieldDataTuple> dataFields = 
 		new ArrayList<FieldDataTuple>();
 
 	interface EditFilledQuestionnaireWidgetUiBinder extends
 			UiBinder<Widget, EditFilledQuestionnaireWidget> {
 	}
 
 	@Override
 	protected void initWidget() {
 		initWidget(uiBinder.createAndBindUi(this));
		saveButton.setVisible(edit);
		editButton.setVisible(!edit);
 	}
 
 	@Override
 	protected LocalDAO<FilledQuestionnaireTuple> getLocalDAO() {
 		return StaticGWTSyncer.getFilledquestionnairedao();
 	}
 
 	@Override
 	protected void updateWidgets() {
 		questionnaireName.setText(tuple.getName());
 		fields = tuple.getFilledFields();
 		Collection<QuestionnaireTuple> quastionnaires = StaticGWTSyncer.getQuestionnairedao().getAll();
 		for (QuestionnaireTuple questionnaire : quastionnaires) {
 			if (questionnaire.getName().equals(tuple.getName())) {
 				dataFields = questionnaire.getFields();
 			}
 		}
 		updateList();
 	}
 	
 	private FieldDataTuple findDataTuple(FieldTuple fieldTuple) {
 		for (FieldDataTuple fieldDataTuple : dataFields) {
 			if (fieldDataTuple.getName().equals(fieldTuple.getName())) {
 				return fieldDataTuple;
 			}
 		}
 		return null;
 	}
 	
 	private void updateList() {
 		verticalPanel.clear();
 		fieldDataWidgets = new ArrayList<FieldWidget>();
 		for (FieldTuple field : fields) {
 			FieldWidget widget;
 			FieldDataTuple fieldData = findDataTuple(field);
 			
 			if (field instanceof FieldTextBoxTuple) {
 				if (fieldData != null && fieldData instanceof FieldTextBoxDataTuple && edit) {
 					widget = new FieldTextBoxWidget(
 							(FieldTextBoxTuple) field,
 							(FieldTextBoxDataTuple) fieldData);
 				} else {
 					widget = new FieldUniversalWidget(field);
 				}
 			} else if (field instanceof FieldTextAreaTuple) {
 				if (fieldData != null && fieldData instanceof FieldTextAreaTuple && edit) {
 					widget = new FieldTextAreaWidget(
 							(FieldTextAreaTuple) field,
 							(FieldTextAreaDataTuple) fieldData);
 				} else {
 					widget = new FieldUniversalWidget(field);
 				}
 			} else {
 				widget = new FieldUniversalWidget(field);
 			}
 			widget.updateWidget();
 			
 			fieldDataWidgets.add(widget);
 			if (fieldDataWidgets.size() % 2 == 0)
 				widget.setStyleName("even");
 			else
 				widget.setStyleName("odd");
 			verticalPanel.add(widget);
 		}
 	}
 
 	@Override
 	protected void updateTuple() {
 		for (FieldWidget dataWidget : fieldDataWidgets) {
 			dataWidget.updateField();
 		}
 	}
 
 	@Override
 	protected void setDeleteable(boolean deletable) {
 		deleteButton.setVisible(deletable);
 	}
 
 	@UiHandler("editButton")
 	void onEditButtonClick(ClickEvent event) {
		History.newItem(NAME_EDIT+"," + tuple.getId(),true);;
	}
	@UiHandler("saveButton")
	void onSaveButtonClick(ClickEvent event) {
		onSave();
 	}
 	@UiHandler("deleteButton")
 	void onDeleteButtonClick(ClickEvent event) {
 		onDelete();
 	}
 }
