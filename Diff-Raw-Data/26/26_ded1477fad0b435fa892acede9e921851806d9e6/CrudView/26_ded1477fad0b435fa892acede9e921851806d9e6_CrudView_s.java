 package com.tda.presentation.client.view;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 import com.smartgwt.client.types.DSOperationType;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.tda.presentation.client.datasource.CrudGwtRPCDS;
 import com.tda.presentation.client.presenter.CrudPresenter;
 
 public class CrudView<T> extends Composite implements CrudPresenter.Display<T> {
 
 	@UiTemplate("CrudView.ui.xml")
 	interface ItemViewUiBinder extends UiBinder<Widget, CrudView> {
 	}
 
 	private static ItemViewUiBinder uiBinder = GWT.create(ItemViewUiBinder.class);
 
 	@UiField
 	Panel parent;
 	
 	@UiField
 	Panel listContainer;
 
 	@UiField
 	Button addButton;
 
 	@UiField
 	Button deleteButton;
 	
 	@UiField
 	Button editButton;
 
 	@UiField
 	Panel formContainer;
 
 	Button submitButton;
 
 	private ListGrid listGrid;
 	private DynamicForm form;
 
 	public CrudView() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	
 	private void createForm(CrudGwtRPCDS<T> ds) {
 		form = new DynamicForm();
         form.setLayoutAlign(VerticalAlignment.CENTER);
         form.setSaveOperationType(DSOperationType.ADD);
         form.setDataSource(ds);
         form.setUseAllDataSourceFields(true);
 
         submitButton = new Button();
         submitButton.setText("Enviar");
 	}
 
 	private void createList(CrudGwtRPCDS<T> ds) {
 		listGrid = new ListGrid();
 		listGrid.setWidth(750);
 		listGrid.setHeight(224);
		listGrid.setHeaderHeight(40);
		listGrid.setDataSource(ds);
		listGrid.setAutoFetchData(true);
 		listGrid.setShowFilterEditor(true);
 		
 	}
 
 	public HasClickHandlers getAddButton() {
 		return addButton;
 	}
 
 	public HasClickHandlers getEditButton() {
 		return editButton;
 	}
 
 	public HasClickHandlers getDeleteButton() {
 		return deleteButton;
 	}
 
 	public ListGrid getGrid() {
 		return listGrid;
 	}
 
 	public ListGridRecord getClickedRow() {
 		return listGrid.getSelectedRecord();
 	}
 
 	public ListGridRecord[] getSelectedRows() {
 		return listGrid.getSelection();
 	}
 
 	public DynamicForm getForm() {
 		return form;
 	}
 
 	public Widget asWidget() {
 		return this;
 	}
 
 	public Panel getListContainer() {
 		return listContainer;
 	}
 
 	public Panel getParentContainer() {
 		return parent;
 	}
 
 	public Panel getFormContainer() {
 		return formContainer;
 	}
 
 	public HasClickHandlers getSubmitButton() {
 		return submitButton;
 	}
 
 	public void setDataSource(CrudGwtRPCDS<T> ds) {
 
 		createList(ds);
 		createForm(ds);
 
		listContainer.add(listGrid);
 		formContainer.setVisible(false);
 		formContainer.add(form);
         formContainer.add(submitButton);
         
 	}
 }
