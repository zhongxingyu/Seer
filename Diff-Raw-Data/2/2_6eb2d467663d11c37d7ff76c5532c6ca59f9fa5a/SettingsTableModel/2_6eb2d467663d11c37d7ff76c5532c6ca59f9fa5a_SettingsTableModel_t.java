 package ui;
 import javax.swing.table.AbstractTableModel;
 
 /*
  * @author Paul Grigoras
  */
 
 public class SettingsTableModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = 1L;
 
 	String[] columnNames = { "option name", "value" };
 
 	Object[][] data = { { "Functions", "" }, { "Colour pattern", "" },
 			{ "Display grid", new Boolean(true) },
 			{ "Display axis values", new Boolean(true) },
 			{ "Show vertical asymptote", new Boolean(false) },
 			{ "Line thickness", new Integer(1) },
 			{ "Accuracy Level", new String("auto") },
 			{ "Increased accuracy", new String("auto") } };
 
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 
 	public int getRowCount() {
 		return data.length;
 	}
 
 	public String getColumnName(int col) {
 		return columnNames[col];
 	}
 
 	public Object getValueAt(int row, int col) {
 		return data[row][col];
 	}
 
 	public boolean isCellEditable(int row, int col) {
 		if (col == 0)
 			return false;
 		return true;
 	}
 
 	public void setValueAt(Object value, int row, int col) {
 		data[row][col] = value;
 		fireTableCellUpdated(row, col);
 	}
 
	public Class<?> getColumnClass(int c) {
 		return getValueAt(0, c).getClass();
 	}
 }
