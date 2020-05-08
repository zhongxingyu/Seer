 package com.sff.report_performance;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.table.TableColumn;
 
 import com.sff.report_performance.GUI.Active;
 import com.sff.report_performance.GUI.State;
 
 public class EnableSelectionAction extends AbstractAction{
 	private static final long serialVersionUID = 1L;
 	private GUI gui;
 	private State state;
 
 	public EnableSelectionAction(GUI gui, State state){
 		this.gui = gui;
 		this.state = state;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
		
 		Active.setState(state);
 		
 		boolean isCustomerEnabled = Active.getState() == State.CUSTOMER;
 		boolean isProjectEnabled = Active.getState() == State.PROJECT;
 		boolean isStatusEnabled = Active.getState() == State.STATUS;
 		
 		if(isCustomerEnabled)gui.getSelectionHeadline().setText("Select Customers");
 		else if(isProjectEnabled)gui.getSelectionHeadline().setText("Select Projects");
 		else gui.getSelectionHeadline().setText("Select Statuses");
 		
 		if(isStatusEnabled)gui.getLblName().setVisible(false);
 		else gui.getLblName().setVisible(true);
 		
 		if(isCustomerEnabled)gui.getLblName().setText("Customer Name");
 		else gui.getLblName().setText("Project Name");
 		
 		if(isCustomerEnabled)gui.getLblID().setVisible(true);
 		else gui.getLblID().setVisible(false);
 		
 		if(isStatusEnabled)gui.getNameField().setVisible(false);
 		else gui.getNameField().setVisible(true);
 		
 		if(isCustomerEnabled)gui.getIdField().setVisible(true);
 		else gui.getIdField().setVisible(false);
 		
 		if(isStatusEnabled)gui.getBtnSearch().setVisible(false);
 		else gui.getBtnSearch().setVisible(true);
 
 		gui.getSelectionTable().setModel(Active.getActiveSelectModel());
 		TableColumn tc = gui.configureTableColumns(gui.getSelectionTable());		
 		tc.setHeaderRenderer(gui.getHeader());
 
 		if(!isCustomerEnabled)gui.getCustomerTable().disable();
 		if(!isProjectEnabled)gui.getProjectTable().disable();
 		if(!isStatusEnabled)gui.getStatusTable().disable();
 		
 		Active.getActiveDisplayTable().enable();
 		
 		gui.getNameField().setText("");
 		gui.getNameField().requestFocusInWindow();
 
 		if(isCustomerEnabled)gui.getbCustomers().setSelected(true);
 		else gui.getbCustomers().setSelected(false);
 		if(isProjectEnabled)gui.getbProjects().setSelected(true);
 		else gui.getbProjects().setSelected(false);
 		if(isStatusEnabled)gui.getbStatuses().setSelected(true);
 		else gui.getbStatuses().setSelected(false);
 		
 		gui.synchronizeHeader();
		
 	}
 }
