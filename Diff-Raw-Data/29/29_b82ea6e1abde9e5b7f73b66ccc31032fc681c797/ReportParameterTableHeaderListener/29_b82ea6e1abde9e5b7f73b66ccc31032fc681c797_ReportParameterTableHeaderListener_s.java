 package com.sff.report_performance;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.JTable;
 
 import com.sff.report_performance.GUI.Active;
 
 public class ReportParameterTableHeaderListener implements ItemListener{
 	
 	private SelectionTable selectionTable;
 	
 	public ReportParameterTableHeaderListener(SelectionTable selectionTable){
 		this.selectionTable = selectionTable;
 	}
 	
 	public void itemStateChanged(ItemEvent e){
 		MyTableModel model = Active.getActiveDisplayModel();
		if(e.getStateChange() == ItemEvent.SELECTED && model != null){
 			JTable display = Active.getActiveDisplayTable();
 			if(model.getRowData().isEmpty()){
 				((CheckBoxHeader) display.getColumnModel().getColumn(0).getHeaderRenderer()).setSelected(true);
 			}
 			else model.clearSelection(selectionTable);
 			Active.getActiveDisplayTable().synchronizeHeader();
 			selectionTable.synchronizeHeader();
 		}
 	}
 }
