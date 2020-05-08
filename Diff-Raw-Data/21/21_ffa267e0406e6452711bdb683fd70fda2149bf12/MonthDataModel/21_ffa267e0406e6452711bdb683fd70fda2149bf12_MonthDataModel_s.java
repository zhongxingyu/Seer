 import javax.swing.table.AbstractTableModel;
 
 public class MonthDataModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
 	private int offset;
 
	public MonthDataModel(int n) {
		offset = n;
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
		int offset = 3;
 		int i = ((7 * row) + (col + 1) - offset);
		return (i < 32 && i > 0) ? i : null;
 	}
 
 }
