 package com.github.pageallocation.gui.table;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableModel;
 
 import com.github.pageallocation.util.Util;
 
 public class PageFaultRenderer extends DefaultTableCellRenderer {
 
 	@Override
 	public Component getTableCellRendererComponent(JTable table, Object value,
 			boolean isSelected, boolean hasFocus, int row, int column) {
 		// TODO Auto-generated method stub
 		Component renderer = super.getTableCellRendererComponent(table, value,
 				isSelected, hasFocus, row, column);
 		boolean pageFault = pageFaultOccurred(table.getModel(), column);
 		if (pageFault) {
 			renderer.setBackground(Color.CYAN);
 		} else {
 			renderer.setBackground(Color.WHITE);
 		}
 		return renderer;
 	}
 
 	private boolean pageFaultOccurred(final TableModel model, final int column) {
 		String columnName = model.getColumnName(column);
 
 		if (column == 0) {
 			return false;
 		}
 		if (column == 1) {
			return !columnName.equalsIgnoreCase("A");
 
 		} else if (column > 1) {
 
 			if (!Util.isInteger(columnName)) {
 				return false;
 			}
 
 			int rows = model.getRowCount();
 			if (isColumnEmpty(model, column, rows)) {
 				return false;
 			}
 			int searchColumn = column - 1;
 			for (int i = 0; i < rows; i++) {
 				Object value = model.getValueAt(i, searchColumn);
 				if (value == null) {
 					continue;
 				} else {
 					String v = (String) value;
 					if (columnName.equals(v)) {
 						return false;
 
 					}
 
 				}
 			}
 		}
 
 		return true;
 
 	}
 
 	private boolean isColumnEmpty(TableModel model, int column, int rows) {
 		for (int i = 0; i < rows; i++) {
 			Object val = model.getValueAt(i, column);
 			if (val != null) {
 				if (!((String) val).isEmpty()) {
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 }
