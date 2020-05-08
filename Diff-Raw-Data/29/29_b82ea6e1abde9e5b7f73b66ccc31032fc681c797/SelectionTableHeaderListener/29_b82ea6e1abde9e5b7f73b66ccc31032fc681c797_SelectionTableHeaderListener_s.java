 package com.sff.report_performance;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.AbstractButton;
 
 import com.sff.report_performance.GUI.Active;
 
 public class SelectionTableHeaderListener implements ItemListener{
 	
 	private SelectionTable table;
 	
 	public SelectionTableHeaderListener(SelectionTable table){
 		this.table = table;
 	}
 	
 	public void itemStateChanged(ItemEvent e){
 		if(table.isHeaderClicked()){
 			
 			int[] selection = new int [table.getRowCount()];
 			
 			for(int i = 0; i < table.getRowCount(); i++){
 				selection[i] = table.convertRowIndexToModel(i);
 			}
 
 			if(e.getStateChange() == ItemEvent.SELECTED
 					&& (e.getSource() instanceof AbstractButton)){
 				Active.getActiveDisplayModel().addRowInterval(selection, table);
 			}
 			else if(e.getStateChange() == ItemEvent.DESELECTED
 					&& (e.getSource() instanceof AbstractButton)){
 				Active.getActiveDisplayModel().partialRemoval(selection, table);
 			}
 			Active.getActiveDisplayTable().synchronizeHeader();
 		}
 	}
 }
