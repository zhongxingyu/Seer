 package com.mobi.iou.client.view;
 
 import java.util.List;
 
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.HasKeyPressHandlers;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.mobi.iou.client.presenter.IouMasterListPresenter;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 
 public class IouMasterListView extends Composite implements
 		IouMasterListPresenter.Display {
 	
 	private Button btnAddIOU;
 	private TextBox txtName;
 	private TextBox txtEmail;
 	private TextBox txtDescription;
 	private TextBox txtAmount;
 	private FlexTable SummaryTable;
 	
 	public HasClickHandlers getBtnAddIOU() {
 		return btnAddIOU;
 	}
 
 
 	public HasClickHandlers getTxtName() {
 		return txtName;
 	}
 
 
 	public HasClickHandlers getTxtEmail() {
 		return txtEmail;
 	}
 
 
 	public HasClickHandlers getTxtDescription() {
 		return txtDescription;
 	}
 
 
 	public HasKeyPressHandlers getTxtAmount() {
 		return txtAmount;
 	}
 
 
 	public void setData(List<String> data) {
		int rowCount = SummaryTable.getRowCount() + 1;
 		
 		SummaryTable.setText(rowCount, 0, "register entry");
		SummaryTable.setText(rowCount, 1, "register entry2");
 	}
 	
 
 	public IouMasterListView() {
 		
 		VerticalPanel AddIOUPanel = new VerticalPanel();
 		initWidget(AddIOUPanel);
 		AddIOUPanel.setWidth("664px");
 		
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		AddIOUPanel.add(horizontalPanel);
 		horizontalPanel.setWidth("700px");
 		
 		VerticalPanel verticalPanel_1 = new VerticalPanel();
 		horizontalPanel.add(verticalPanel_1);
 		
 		Label lblName_1 = new Label("Name");
 		verticalPanel_1.add(lblName_1);
 		
 		txtName = new TextBox();
 		verticalPanel_1.add(txtName);
 		
 		VerticalPanel verticalPanel_2 = new VerticalPanel();
 		horizontalPanel.add(verticalPanel_2);
 		
 		Label lblEmail = new Label("Email");
 		verticalPanel_2.add(lblEmail);
 		
 		txtEmail = new TextBox();
 		verticalPanel_2.add(txtEmail);
 		
 		VerticalPanel verticalPanel_3 = new VerticalPanel();
 		horizontalPanel.add(verticalPanel_3);
 		
 		Label lblDescription_1 = new Label("Description");
 		verticalPanel_3.add(lblDescription_1);
 		
 		txtDescription = new TextBox();
 		verticalPanel_3.add(txtDescription);
 		
 		VerticalPanel verticalPanel_4 = new VerticalPanel();
 		horizontalPanel.add(verticalPanel_4);
 		
 		Label lblAmount_1 = new Label("Amount");
 		verticalPanel_4.add(lblAmount_1);
 		
 		txtAmount = new TextBox();
 		verticalPanel_4.add(txtAmount);
 		txtAmount.setWidth("90px");
 		
 		btnAddIOU = new Button("New button");
 		btnAddIOU.setText("Add IOU");
 		horizontalPanel.add(btnAddIOU);
 		btnAddIOU.setSize("78px", "28px");
 		horizontalPanel.setCellVerticalAlignment(btnAddIOU, HasVerticalAlignment.ALIGN_BOTTOM);
 		
 		SummaryTable = new FlexTable();
 		AddIOUPanel.add(SummaryTable);
 		
 		Label lblName = new Label("Name");
 		SummaryTable.setWidget(0, 1, lblName);
 		
 		Label lblDescription = new Label("Description");
 		SummaryTable.setWidget(0, 2, lblDescription);
 		
 		Label lblAmount = new Label("Is Owed");
 		SummaryTable.setWidget(0, 3, lblAmount);
 		
 		Label lblOwesMe = new Label("Owes Me");
 		SummaryTable.setWidget(0, 4, lblOwesMe);
 
 	}
 
 	
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 }
