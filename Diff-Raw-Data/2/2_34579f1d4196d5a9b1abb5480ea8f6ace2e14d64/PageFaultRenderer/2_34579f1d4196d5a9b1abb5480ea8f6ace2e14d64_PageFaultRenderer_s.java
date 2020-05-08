 package com.github.pageallocation.gui.table;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableModel;
 
 import com.github.pageallocation.util.Util;
 
 /**
  * Renderer used to depict a Page Fault. If the current reference was not in the
  * previous column then a Page Fault occurred.
  * 
  * @author Victor J.
  * 
  */
 public class PageFaultRenderer extends DefaultTableCellRenderer {
 
 	@Override
 	public Component getTableCellRendererComponent(JTable table, Object value,
 			boolean isSelected, boolean hasFocus, int row, int column) {
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
 
 		if (column == 0) {// Frames column
 			return false;
 		}
 		if (column == 1) {// First reference and its not in the initial state
			return !Util.isInteger(columnName);
 
 		} else if (column > 1) {
 
 			if (!Util.isInteger(columnName)) {
 				return false;
 			}
 
 			int rows = model.getRowCount();
 			if (isColumnEmpty(model, column, rows)) {
 				return false;
 			}
 			int searchColumn = column - 1;
 			for (int i = 0; i < rows; i++) {// Search the value
 				Object value = model.getValueAt(i, searchColumn);
 				if (value == null) {
 					continue;
 				} else {
 					String v = (String) value;
 					if (columnName.equals(v)) {// found it, no page fault
 						return false;
 					}
 				}
 			}
 		}
 
 		return true;
 
 	}
 
 	/**
 	 * Verifies if a column is empty. Either all columns are "" or null.
 	 * @param model
 	 * @param column
 	 * @param rows
 	 * @return
 	 */
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
