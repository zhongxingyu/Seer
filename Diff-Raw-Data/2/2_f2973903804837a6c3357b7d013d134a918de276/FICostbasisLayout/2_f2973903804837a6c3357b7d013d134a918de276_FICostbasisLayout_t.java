 /**
  * 
  */
 package com.grimesco.gcocentralapp.Fidelity;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 
 import com.grimesco.gcocentral.FImanageFileUpload;
 import com.grimesco.gcocentral.act.dao.account.ACTAccountDao;
 import com.grimesco.gcocentral.fidelity.dao.*;
 import com.grimesco.gcocentralapp.GCOCentralSetting;
 import com.grimesco.translateFidelity.model.POJO.FIcostbasis;
 import com.grimesco.translateFidelity.model.POJO.FIreconciliation;
 import com.grimesco.translateFidelity.model.POJO.FItransaction;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * @author jaeboston
  *
  */
 public class FICostbasisLayout extends VerticalLayout {
 
 	
 	private static Logger logger = LoggerFactory.getLogger(FICostbasisLayout.class);
 	
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	static public java.util.Date workingDate;
 	
 	
 	public FIcostbasisDao costbasisDao;
 	public FItransactionDao transactionDao;
 	public ACTAccountDao actAccountDao;
 	
 	
 	public FItransaction workingTransaction;
 	public FIcostbasis workingCostbasis;
 	
 	public String workingShortName;
 	
 	public ArrayList<FIreconciliation> reconciliationList;
 	public ArrayList<FIreconciliation> selectedCostbasisList;
 	
 	public Table 	reconsiliationTable;
 	
 	public 	Label 	selected;
 	public 	Label 	manCostbasisLabel;
     private Label 	spacer = new Label("          ");
     
     public 	IndexedContainer reconsiliationTableSource;		//-- source for the reconsiliation table
 	
     public  Button manResolvetButton  	= new Button("Submit costbasis");
     public  Button rejectButton 		= new Button("Reject this costbasis");
	public 	Button cancelButton 			= new Button("Cancel");
 	
 	public TextField manCostbasisField;
 	public HorizontalLayout manCostbasisLayout;
 	
 	
 	//-- constructor
 	public FICostbasisLayout() {
 		
 	}	
 	
 	//-- constructor
 	public void init(String caption, FIcostbasisDao _costbasisDao, String _shortName, final long _transactionID, 	ArrayList<FIreconciliation> _reconciliationList, FItransactionDao _transactionDao, ACTAccountDao _actaccountDao, final java.util.Date _workingdate) {
 		
 		//-- clear out the layout
 		this.removeAllComponents();
 		
 		this.workingDate 		= _workingdate;
 		this.transactionDao 	= _transactionDao;
 		this.costbasisDao 		= _costbasisDao;
 		this.actAccountDao 		= _actaccountDao;
 		this.reconciliationList = _reconciliationList;
 		this.workingShortName 	= _shortName;
 		
 		this.setWidth("1150px");
 		this.setHeight("600px");
 		
 		//-- get the working FItransaction
 		this.workingTransaction = transactionDao.get(_transactionID);
 		this.workingCostbasis 	= costbasisDao.getByTransactionID(_transactionID);
 		
 		this.setMargin(true);
         this.setSpacing(true);
         
         //-- make it fill the whole window
         setSizeFull();
 		
         // Add some content; a label and a close-button
         Label message = new Label(
         							"<h1> Working on the following transaction</h1>"
         									+ "<table cellspacing='20'>"
         									+ "<tr><th>Short Name</th><th>File Date</th><th>Symbol</th><th>Trade Date</th><th>Quantity</th><th>Net Amount</th></tr>" 
         									+ "<tr><td>" + this.workingShortName + "</td>"
         									+ "<td>" + this.workingTransaction.getSOURCE_DATE_display() + "</td>" 
         									+ "<td>" + String.valueOf(this.workingTransaction.getSYMBOL()) + "</td>"
         									+ "<td>" + this.workingTransaction.getTRANSACTION_DATE_display() + "</td>"
         									+ "<td>" + String.valueOf(this.workingTransaction.getTRANSACTION_QUANTITY()) + "</td>"
         									+ "<td>" + String.valueOf(this.workingTransaction.getAMOUNT()) + "</td></tr></table><hr>");
         message.setContentMode(Label.CONTENT_XHTML);
         
         this.addComponent(message);
         
         //-- label configuration
         manCostbasisLabel = new Label("Selected Costbasis Quantity");
         
         //-- costbasis text field configuration
         manCostbasisField = new TextField();
         manCostbasisField.setInputPrompt("Enter costbasis");
         manCostbasisField.setImmediate(true);
         
         //-- costbasis manual sumbit button configuration
         manResolvetButton.setVisible(false);
         manResolvetButton.setImmediate(true);
         
         //-- costbasis reject button configuration
         rejectButton.setVisible(true);
         rejectButton.setImmediate(true);
         //rejectButton.setStyleName(Runo.BUTTON_LINK);
         
         //-- addNewRow button configuration
         cancelButton.setVisible(true);
         cancelButton.setImmediate(true);
         
         
         //-- Man costbasis layout configuration
         manCostbasisLayout = new HorizontalLayout();
         manCostbasisLayout.setMargin(true);
         manCostbasisLayout.setSpacing(true);
         manCostbasisLayout.addComponent(manCostbasisLabel);
         manCostbasisLayout.addComponent(manCostbasisField);
         manCostbasisLayout.addComponent(manResolvetButton);
         manCostbasisLayout.addComponent(cancelButton);
         this.spacer.setWidth("200px");
         manCostbasisLayout.addComponent(this.spacer);
         manCostbasisLayout.addComponent(rejectButton);
         
         //-- Add the layout to window
         this.addComponent(manCostbasisLayout);
         
         //--Add reconciliation list table
         reconsiliationTable = new Table();
         reconsiliationTable.setHeight("400px");
         //-- selectable
         reconsiliationTable.setSelectable(true);
         reconsiliationTable.setMultiSelect(true);
         reconsiliationTable.setImmediate(true); // react at once when something is selected
 
         this.addComponent(reconsiliationTable);
         
         
         //-- add selected label
         selected = new Label("No selection");
         
         this.addComponent(selected);
         
         //-- Populate the content in the table
         reconsiliationTableSource 		= GCOCentralSetting.updateFIReconciliationContainer(this.reconciliationList);
         reconsiliationTable.setContainerDataSource(reconsiliationTableSource);
 		//-- set the fotters
         reconsiliationTable.setFooterVisible(true);
         reconsiliationTable.setColumnFooter("Source Date", "Total");
         reconsiliationTable.setColumnFooter("Current Quantity", 	String.valueOf(getTotalQuantity()));
         reconsiliationTable.setColumnFooter("Cost Basis", 			String.valueOf(getTotalCostBasis()));
         reconsiliationTable.setColumnFooter("Adjusted Cost Basis", String.valueOf(getTotalAdjCostBasis()));
         
         //-------------------------------------------
   		//-- // listen for valueChange, a.k.a 'select' and update the label
   		//-------------------------------------------
         reconsiliationTable.addValueChangeListener(new Table.ValueChangeListener() {
         	/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
         	public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                 // in multiselect mode, a Set of itemIds is returned,
                 // in singleselect mode the itemId is returned directly
                 Set<?> value = (Set<?>) event.getProperty().getValue();
 
                 double selectedQuantity = 0.0;
 
                 if (null == value || value.size() == 0) {
                     selected.setValue("No selection");
                 } else {
         
                 	//-- work through the selected rows of the table
                 	
                 	Set<?> itemids = (Set<?>)reconsiliationTable.getValue();
                 	
                 	int reconciliationID;
                 	
                 	selectedCostbasisList = new ArrayList<FIreconciliation>();
                 	
                 	Iterator iterator = itemids.iterator();
                 	while(iterator.hasNext()) {
                 		Object item = iterator.next();
                 		//selectedQuantity = selectedQuantity + Double.parseDouble(String.valueOf(reconsiliationTable.getContainerProperty(item, "Current Quantity")));
                 		selectedQuantity = selectedQuantity + Double.parseDouble((String)(reconsiliationTable.getContainerProperty(item, "Current Quantity").getValue()));
                 		
                 		//reconciliationID = Integer.parseInt(String.valueOf(reconsiliationTable.getContainerProperty(item, "ID")));
                 		reconciliationID = Integer.parseInt((String)reconsiliationTable.getContainerProperty(item, "ID").getValue());
                 		selectedCostbasisList.add(findReconciliation(reconciliationID));
                 	}
                 	
                 	//-- set the value in the field
                 	manCostbasisField.setValue(String.valueOf(round(selectedQuantity)));
                     
                 	selected.setValue("Selected Transactions: <b>" + itemids.size() + "</b>");
                 	selected.setContentMode(Label.CONTENT_XHTML);
                 	
                 	manResolvetButton.setVisible(true);
                 }
             }
 
         });        
         
         
         
         //-------------------------------------------
   		//-- listerner for export button : update the database done in FIView.java
   		//-------------------------------------------
         manResolvetButton.addClickListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 			}
         });
       		
         
 	    //-------------------------------------------
 		//-- listerner for rejectButton : database update done on FIView.java using executeReject() method						
 		//-------------------------------------------
 	    rejectButton.addClickListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 			}
 	    });
 	
 	    
 		
 	}  //-- end of init
 	
 	
 
 	//-- public method
 	public void executeResolution() {
 		
 		int insertedRows = 0;
 		boolean dateValid = true;
 		
 		ArrayList<FItransaction> costbase_transactionList = new ArrayList<FItransaction>();
 		
 		//-- Build the data
 		//-- create a new transaction for each of the selected reconciliation record with cost basis
 		for (FIreconciliation rec : selectedCostbasisList) {
 		
 			
 			FItransaction transaction = new FItransaction();
 
 			//-- populate new transaction
 			transaction.setID(rec.getID());
 			transaction.setACCOUNT_NUMBER(rec.getACCOUNT_NUMBER());
 			transaction.setAMOUNT(rec.getCOST_BASIS_AMOUNT());
 			transaction.setBROKER_CODE(workingTransaction.getBROKER_CODE());
 			transaction.setCOMMISSION(workingTransaction.getCOMMISSION());
 			transaction.setKICK_OUT_CODE(workingTransaction.getKICK_OUT_CODE());
 			transaction.setOPTION_SYMBOL(workingTransaction.getOPTION_SYMBOL());
 			transaction.setSETTLEMENT_DATE(rec.getLOT_ACQUIRED_DATE());
 			transaction.setSOURCE(workingTransaction.getSOURCE());
 			transaction.setSYMBOL(workingTransaction.getSYMBOL());
 			transaction.setTRANSACTION_DATE(workingTransaction.getTRANSACTION_DATE());
 			transaction.setTRANSACTION_QUANTITY(rec.getLOT_QUANTITY());
 			transaction.setTRANSACTION_SECURITY_TYPE_CODE(workingTransaction.getTRANSACTION_SECURITY_TYPE_CODE());
 			transaction.setTRANSACTION_TYPE_CODE(workingTransaction.getTRANSACTION_TYPE_CODE());
 			transaction.setSOURCE_DATE(workingTransaction.getSOURCE_DATE());
 			
 			//-- Data Validation
 			//-- make sure resolved date is later than the file date
 			if (workingDate.before(transaction.getDATE_SOURCE_DATE())){
 				dateValid = false;
 				break;
 			}
 			costbase_transactionList.add(transaction);
 			
 		}
 		
 
 		if (dateValid) {
 			//-- upload the data to database
 			try {
 				//-- insert new transaction to transaction table
 				insertedRows = costbasisDao.insertBatchReconcilTransaction(workingCostbasis.getID(), costbase_transactionList, workingDate);
 									
 			} catch (DataAccessException e) {
 					
 				//-- Error and exception statements.
 				Notification.show("error updating: ", e.getMessage(), Notification.Type.ERROR_MESSAGE); 	//-- Notification
 			}
 			
 			//-- when it is done close window
 			if (insertedRows == selectedCostbasisList.size()) {
 			
 				//-- remove the parent's panel content done it on FIView.java
 				//((Panel)getParent()).setContent(null);
 				
 			} else {
 				Notification.show("updated rows = " + insertedRows +  "COSTBASIS trnasction NOT updated correctly: ", Notification.Type.ERROR_MESSAGE);
 			}
 		
 		} else {			
 			Notification.show("Resolution Date should be later than the File date", Notification.Type.ERROR_MESSAGE); 	//-- Notification
 		}
 		
 		
 	}
 	
 
 	
 	public void executeReject() {
 			
 		int updatedRows = 0;
 		
 		//-- upload the data to database
 		try {
 			//-- insert new transaction to transaction table
 			updatedRows = costbasisDao.updateRejectUnresolvedCostBasis(this.workingTransaction.ID);
 									   //updateBatchRejectUnresolvedCostBasis
 			
 		} catch (DataAccessException e) {		
 			//-- Error and exception statements.
 			Notification.show("error updating: ", e.getMessage(), Notification.Type.ERROR_MESSAGE); 	//-- Notification
 		}
 		
 		//-- when it is done close window
 		if (updatedRows == 1) {
 			//-- refresh parent windows's table.
 			
 		} else {
 			//-- show error message to make sure
 			Notification.show("updated rows = " + updatedRows +  "COSTBASIS transction NOT updated correctly: ", Notification.Type.ERROR_MESSAGE);
 		}
 		
 
 	}
 
 	
 	
 	//-- private method
 	private double getTotalQuantity() {
 		
 		double sum = 0;
 		
 		for (FIreconciliation rec : reconciliationList) {
 			sum = sum + Double.parseDouble(String.valueOf(rec.getLOT_QUANTITY()));
 		}
 		
 		//-- round
 		return round(sum);
 		
 	}
 	
 	//-- private method
 	private double getTotalCostBasis() {
 		
 		double sum = 0;
 		
 		for (FIreconciliation rec : reconciliationList) {
 			sum = sum + Double.parseDouble(String.valueOf(rec.getCOST_BASIS_AMOUNT()));
 		}
 		
 		//-- round
 		return round(sum);
 	}
 	
 	//-- private method
 	private double getTotalAdjCostBasis() {
 		
 		double sum = 0;
 		
 		for (FIreconciliation rec : reconciliationList) {
 			sum = sum + Double.parseDouble(String.valueOf(rec.getTOTAL_ADJUSTED_COST_BASIS_AMOUNT()));
 		}
 		return round(sum);
 	}
 	
 	//-- private method
 	private double round(double num) {
 
 		double factor = 1e4; // = 1 * 10^5 = 10000.
 		double result = Math.round(num * factor) / factor;
 		return result;
 	
 	}
 	
 	//-- private method
 	private FIreconciliation findReconciliation(int id) {
 		
 		for(FIreconciliation recond : this.reconciliationList) {
 			if (recond.getID() == id)
 				return recond;
 		}
 		
 		//-- if nothing is found, return NULL
 		return null;
 		
 	}
 
 
 
 }
