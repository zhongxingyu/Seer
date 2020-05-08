 package de.uni_leipzig.imise.visualization.view;
 
 import java.awt.Component;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 public class DiffTable extends JTable {
 
 	
 	private static final Logger log = Logger.getLogger(DiffTable.class.getName());
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 	public DiffTable(){
 		super();
 		
 	
 	}
 	
 	public DiffTable(CRFTableModel dtm){
 		super(dtm);
 		ListCellRenderer lcr = new ListCellRenderer();
 		int ind =this.getColumnModel().getColumnIndex(CellConstants.NEW_VALUE);
 		int ind2= this.getColumnModel().getColumnIndex(CellConstants.OLD_VALUE);
 		this.getColumnModel().getColumn(ind).setCellRenderer(lcr);
 		this.getColumnModel().getColumn(ind2).setCellRenderer(lcr);
 	}
 	
 	public DiffTable(DefaultTableModel dtm){
 		super(dtm);		
 	}
 	/*
 	public void setModel(DiffTableModel m){
 		this.setModel(tableModel);
 		DefaultTableColumnModel dcm = new DefaultTableColumnModel();
 		for (int c = 0; c<m.getColumnCount();c++){
 			TableColumn tc = new TableColumn();
 			tc.setHeaderValue(m.getColumnName(c));
 			dcm.addColumn(tc);
 		}
 		this.setColumnModel(dcm);
 	}*/
 	
 	public static void main (String[] args){
 		CRFTableModel dtm = new CRFTableModel(CellConstants.VERSION_COL,CellConstants.ITEM_COL,
 				CellConstants.MOD_TYPE);
 	
 		
 		DiffTable dt = new DiffTable();
 		int r = dtm.addRow();
 		dtm.setValueAt(new Integer(1), r, 0);
 		dtm.setValueAt("test", r, 1);
 		dtm.setValueAt("g", r, 2);
 		
 		//dt.setFillsViewportHeight(true);
 		JFrame f= new JFrame();
 		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		JPanel p = new JPanel();
 		p.add(new JScrollPane(dt));
 		f.setContentPane(p);
 		f.setVisible(true);
 		dt.setModel(dtm);
 		
 	}
 	
 	private class ListCellRenderer implements TableCellRenderer{
 
 		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			String selColName = table.getColumnName(column);
 			String prop =(String) table.getValueAt(
 					row,table.getColumnModel().getColumnIndex(CellConstants.PROPERTY_COL));
 			String valueString = value.toString();
 			String [] valueArray = valueString.split(",");
 			StringBuffer sb = new StringBuffer("<html><body>");
 			
 			if (valueArray.length>1&& prop.startsWith("RESPONSE")){
 				JLabel lab =new JLabel();
 				int otherIndex;
 				if (selColName.equals(CellConstants.OLD_VALUE))
 					otherIndex = table.getColumnModel().getColumnIndex(CellConstants.NEW_VALUE);
 				else 
 					otherIndex = table.getColumnModel().getColumnIndex(CellConstants.OLD_VALUE);
 				
 				if (selColName.equals(CellConstants.OLD_VALUE)||
 						selColName.equals(CellConstants.NEW_VALUE)){
 						String otherValue = (String) table.getModel().getValueAt(row, otherIndex);
 						String[] otherArray= otherValue.split(",");
 						
 						HashSet<String> newSet = new HashSet<String> ();
 						for (String v: otherArray) newSet.add(v);
 						for (int i = 0; i< valueArray.length;i++){
 							String v = valueArray[i];
							if (newSet.contains(v)){
								sb.append(v);
							}else{
 								sb.append("<b>"+v+"</b>");
 							}
 							if (i!= valueArray.length-1){
 								sb.append("<br/>");
 							}
 						}
 						sb.append("</body></html>");
 						lab.setText(sb.toString());
						table.setRowHeight(row, (int) lab.getPreferredSize().getHeight());
 						return lab;
 					}else{
 						return dtcr.getTableCellRendererComponent(
 								table, value, isSelected, hasFocus, row, column);
 					}
 				
 			}else {
 				return dtcr.getTableCellRendererComponent(
 						table, value, isSelected, hasFocus, row, column);
 			}
 			
 		}
 		
 	}
 }
