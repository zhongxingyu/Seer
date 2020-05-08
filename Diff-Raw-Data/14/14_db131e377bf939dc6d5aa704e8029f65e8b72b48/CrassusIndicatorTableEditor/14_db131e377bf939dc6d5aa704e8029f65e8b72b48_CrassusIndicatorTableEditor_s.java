 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.AbstractCellEditor;
 import javax.swing.JTable;
 import javax.swing.table.TableCellEditor;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 
 @SuppressWarnings("serial")
 public class CrassusIndicatorTableEditor extends AbstractCellEditor implements TableCellEditor {
 
 	private CrassusCheckBoxEye cb;
 	private Stock stock;
 	
 	public CrassusIndicatorTableEditor(){
 		cb = new CrassusCheckBoxEye();
 	}
 	
 	@Override
 	public Object getCellEditorValue() {
 		return cb.isSelected();
 	}
 
 	@Override
 	public Component getTableCellEditorComponent(JTable table, Object value,
 			boolean isSelected, int row, int column) {
 
 		if (value instanceof Boolean) {
             boolean selected = (boolean) value;
             cb.setSelected(selected);
         }
 
 		if(row%5==0)//(stocks.getStockList().get(row).isTriggered() == StockEventType.BUY)
 			cb.setBackground(new Color(170,230,170));
 		else if(row%5==1)//(stocks.getStockList().get(row).isTriggered() == StockEventType.SELL)
 			cb.setBackground(new Color(255,150,150));
 		else
 			cb.setBackground(Color.WHITE);
 
 		
         return cb;
 	}
 
 
 	public void changeToStock(Stock stock){
 		this.stock = stock;
 	}
 }
