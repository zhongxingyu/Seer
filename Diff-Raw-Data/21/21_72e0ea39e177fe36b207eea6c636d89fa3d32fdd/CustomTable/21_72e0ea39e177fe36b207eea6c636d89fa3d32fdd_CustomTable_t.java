 package gui;
 
 import java.util.ArrayList;
 
 import javax.swing.table.*;
 
 /** This class represents a table model
  * @author David Wille
  */
 public class CustomTable extends AbstractTableModel {
 
 	static final long serialVersionUID = -3667258249137827980L;
 	private String[] columnNames;
 	private ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
 	private boolean[] editable;
 
 	/**
 	 * Constructs a custom table with column names and sets if columns are editable.
 	 * @param head Names for the columns
 	 * @param editable Editable columns
 	 */
 	public CustomTable(String[] head, boolean[] editable) {
 		this.editable = editable;
 		columnNames = head;
 	}
 	
 	@Override
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 
 	@Override
 	public int getRowCount() {
 		return data.size();
 	}
 	
 	@Override
 	public Object getValueAt(int i, int j) {
 		return data.get(i).get(j);
 	}
 
 	@Override
 	public String getColumnName(int i) {
 		return columnNames[i];
 	}
 
 	@Override
 	public void setValueAt(Object value, int row, int col) {
 		data.get(row).set(col, value);
 		fireTableCellUpdated(row, col);
 	}
 	
 	@Override
 	public Class<?> getColumnClass(int col) {
 		return data.get(0).get(col).getClass();
 	}
 
 	@Override
 	public boolean isCellEditable(int row, int col) {
 		if (editable[col]) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	/**
 	 * Add row to the table.
 	 * @param values Values to be inserted
 	 */
 	public void addRow(Object[] values) {
 		ArrayList<Object> row = new ArrayList<Object>();
 		for (int i = 0; i < values.length; i++) {
 			row.add(values[i]);
 		}
 		data.add(row);
 		fireTableRowsInserted(data.size()-1, data.size()-1);
 	}
 	
 	/**
 	 * Updates a row with the given values.
 	 * @param values Values to set
 	 * @param row Row that should be updated
 	 */
 	public void updateRow(Object[] values, int row) {
 		for (int i = 0; i < values.length; i++) {
 			setValueAt(values[i], row, i);
 			fireTableCellUpdated(row, i);
 		}
 	}
 
 	/**
 	 * Delete a row.
 	 * @param row Row which should be deleted
 	 */
 	public void deleteRow(int row) {
 		data.remove(row);
 		fireTableRowsDeleted(row, row);
 	}
 }
