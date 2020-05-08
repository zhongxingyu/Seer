 package com.sff.report_performance;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.JTable;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 
 public class SelectionTable extends JTable {
 	private static final long serialVersionUID = 1L;
 	private boolean headerClick;
 
 	public SelectionTable(MyTableModel model){
 		super(model);
 	}
 	
 	@Override
 	public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
 		Component c = super.prepareRenderer(renderer, row, column);
 		Color color = (boolean) this.getValueAt(row, 0) ? Color.BLUE : Color.BLACK;
 		c.setForeground(color);
 		return c;
 	}
 	
 	public void synchronizeHeader(){
 		boolean checked = true;
 		if(this.getRowCount() == 0)((CheckBoxHeader)this.getColumnModel().getColumn(0).getHeaderRenderer()).setSelected(false);
 		else{ 
 			for(int x = 0; x < this.getRowCount(); x++){
 				if(!(boolean) this.getValueAt(x, 0)){
 					checked = false;
 				}
 			}
 			headerClick = false;
 			((CheckBoxHeader)this.getColumnModel().getColumn(0).getHeaderRenderer()).setSelected(checked);
			this.getParent().getParent().repaint();
 			headerClick = true;
 		}
 	}
 	
 	public boolean isHeaderClicked(){
 		return headerClick;
 	}
 	
 	@Override
 	public void setModel(TableModel tableModel){
 		super.setModel(tableModel);
 //		if(this.getRowSorter()!=null)((TableRowSorter<?>) this.getRowSorter()).setSortable(0, false); //TODO: combine with synch header?
 	}
 }
