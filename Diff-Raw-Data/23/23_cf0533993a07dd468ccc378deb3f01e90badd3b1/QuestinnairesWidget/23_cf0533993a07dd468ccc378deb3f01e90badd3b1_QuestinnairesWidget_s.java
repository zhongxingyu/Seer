 package com.szas.server.gwt.client.widgets;
 
 import java.util.ArrayList;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.szas.data.FieldDataTuple;
 import com.szas.data.FieldTuple;
 import com.szas.data.FilledQuestionnaireTuple;
 import com.szas.data.QuestionnaireTuple;
 import com.szas.server.gwt.client.sync.StaticGWTSyncer;
 import com.szas.server.gwt.client.universalwidgets.SimpleTupleList;
 import com.szas.server.gwt.client.universalwidgets.UniversalWidget;
 import com.szas.sync.local.LocalDAO;
 
 public class QuestinnairesWidget extends UniversalWidget<QuestionnaireTuple> {
 
 	public static String NAME = "questionnaire";
 
 	static class StringUrlValues {
 		String value = "";
 		String url;
 	}
 	
 	private static class FilledQuestionnaireFiledColumn extends Column<FilledQuestionnaireTuple, String> {
 
 		private String fieldName;
 
 		public FilledQuestionnaireFiledColumn(String fieldName) {
 			super(new TextCell());
 			this.fieldName = fieldName;
 		}
 
 		@Override
 		public String getValue(FilledQuestionnaireTuple object) {
 			for (FieldTuple fieldTuple : object.getFilledFields()) {
 				if (!fieldTuple.getName().equals(fieldName))
 					continue;
 				return fieldTuple.getText();
 			}
 			return "";
 		}
 		
 	}
 
 	static class MyTupleList extends SimpleTupleList<FilledQuestionnaireTuple> {
 
 		private final QuestionnaireTuple questionnaireTuple;
 
 		public MyTupleList(QuestionnaireTuple questionnaireTuple) {
 			this.questionnaireTuple = questionnaireTuple;
 		}
 
 		@Override
 		protected LocalDAO<FilledQuestionnaireTuple> getLocalDAO() {
 			return StaticGWTSyncer.getFilledquestionnairedao();
 		}
 
 		@Override
 		protected String getListName() {
 			return EditFilledQuestionnaireWidget.NAME;
 		}
 
 		@Override
 		protected boolean filter(FilledQuestionnaireTuple filledTuple) {
 			if (filledTuple.getName().equals(questionnaireTuple.getName()))
 				return true;
 			return false;
 		}
 
 		@Override
 		protected void addColumns(CellTable<FilledQuestionnaireTuple> cellTable) {
 			ArrayList<FieldDataTuple> fields = questionnaireTuple.getFields();
 			for (FieldDataTuple field : fields) {
 				if (field.isOnList()) {
 					FilledQuestionnaireFiledColumn column = new FilledQuestionnaireFiledColumn(field.getName());
 					column.setSortable(true);
 					cellTable.addColumn(column,field.getName());
 				}
 			}
 		}
 	};
 
 
 	@UiField(provided=true)
 	MyTupleList cellTable;
 	@UiField Button deleteButton;
 	@UiField Label questionnaireName;
 	@UiField Button editButton;
 	@UiField Button fillButton;
 
 	private static QuestinnairesWidgetUiBinder uiBinder = GWT
 	.create(QuestinnairesWidgetUiBinder.class);
 
 	interface QuestinnairesWidgetUiBinder extends
 	UiBinder<Widget, QuestinnairesWidget> {
 	}
 
 
 	public QuestinnairesWidget(QuestionnaireTuple questionnaireTuple) {
 		super(questionnaireTuple);
 		cellTable = new MyTupleList(tuple);
 		initWidget();
 	}
 
 	@UiHandler("deleteButton")
 	void onDeleteButtonClick(ClickEvent event) {
 		onDelete();
 	}
 
 	@Override
 	protected LocalDAO<QuestionnaireTuple> getLocalDAO() {
 		return StaticGWTSyncer.getQuestionnairedao();
 	}
 
 	@Override
 	protected void updateWidgets() {
 		questionnaireName.setText(tuple.getName());
 	}
 
 	@Override
 	protected void initWidget() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	protected void updateTuple() {
 		tuple.setName(questionnaireName.getText());
 	}
 
 	@Override
 	protected void setDeleteable(boolean deletable) {
 		deleteButton.setVisible(update);
 	}
 
 	@UiHandler("editButton")
 	void onEditButtonClick(ClickEvent event) {
 		History.newItem(EditQuesionnaireWidget.NAME+"," + tuple.getId(),true);
 	}
 
 	@UiHandler("fillButton")
 	void onFillButtonClick(ClickEvent event) {
 		History.newItem(EditFilledQuestionnaireWidget.NAME_NEW + "," + tuple.getId(),true);
 	}
 }
