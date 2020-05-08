 package com.sff.report_performance;
 
 import javax.swing.JTextField;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 public class ReportParameterTableModelListener implements TableModelListener {
 	private JTextField textField = null;
 	private MyTableModel displayModel = null;
 	private MyTableModel selectionModel;
 	private DefaultTableModel defaultModel;
 	private ReportParameterTable displayTable;
 	
 	public ReportParameterTableModelListener(JTextField textField, DefaultTableModel defaultModel){
 		this.defaultModel = defaultModel;
 		this.textField = textField;
 	}
 	
 	public ReportParameterTableModelListener(MyTableModel displayModel, MyTableModel selectionModel, ReportParameterTable displayTable){
 		this.displayModel = displayModel;
 		this.selectionModel = selectionModel;
 		this.displayTable = displayTable;
 	}
 	
 	@Override
 	public void tableChanged(TableModelEvent evt) {
 		if(textField!=null) {
 			textField.setText("ALL");
 			defaultModel.setRowCount(0);
 		}
 		if(displayModel!=null) {
 			selectionModel.removeAllRows();
 			displayModel.removeAllRows();
			CheckBoxHeader checkBoxHeader = (CheckBoxHeader) displayTable.getColumnModel().getColumn(0).getHeaderRenderer();
 //			System.out.println(checkBoxHeader.isSelected());
			checkBoxHeader.setSelected(true);
 			displayTable.getColumnModel().getColumn(0).setHeaderValue("ALL");
 			displayTable.getParent().getParent().repaint();
 		}
 	}
 }
