 import javax.swing.table.AbstractTableModel;
 
 public class YearDataModel extends AbstractTableModel {
 
     private static final long serialVersionUID = 1L;
     private String[] names = new String[1];
     private int offset;
     private int days;
 
     public YearDataModel(String name, int off, int d) {
         offset = off;
         days = d;
         names[0] = name;
     }
 
     public String getColumnName(int col) {
        return col == 0 ? names[0] : null;
     }
 
     public int getOffset() {
         return offset;
     }
 
     public int getDays() {
         return days;
     }
 
     public void setOffset(int i) {
         offset = i;
     }
 
     public void setDays(int i) {
         days = i;
     }
 
     public int getColumnCount() {
         return 7;
     }
 
     public int getRowCount() {
         int result = (days + offset) / 7;
         if ((days + offset) % 7 != 0)
             result++;
         return result;
     }
 
     public Object getValueAt(int row, int col) {
         int i = ((7 * row) + (col + 1) - offset);
         return (i <= days && i > 0) ? i : null;
     }
 
 }
