 import javax.swing.table.AbstractTableModel;
 
 public class MonthDataModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = 1L;
 	private int offset;
	private int days;
 
	public MonthDataModel(int off, int d) {
		offset = off;
		days = d;
 	}
 
 	private String[] names = { "Monday", "Tuesday", "Wednesday", "Thursday",
 			"Friday", "Saturday", "Sunday" };
 
 	public String getColumnName(int col) {
 		return names[col];
 	}
 
 	public int getColumnCount() {
 		return 7;
 	}
 
 	public int getRowCount() {
 		return 6;
 	}
 
 	public Object getValueAt(int row, int col) {
 		int i = ((7 * row) + (col + 1) - offset);
		return (i <= days && i > 0) ? i : null;
 	}
 
 }
