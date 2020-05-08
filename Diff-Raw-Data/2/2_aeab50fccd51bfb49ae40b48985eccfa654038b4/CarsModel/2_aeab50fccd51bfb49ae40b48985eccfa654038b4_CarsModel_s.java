 package model;
 
 import static java.lang.Double.parseDouble;
 import static javax.swing.JOptionPane.ERROR_MESSAGE;
 import static javax.swing.JOptionPane.showMessageDialog;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import bean.Car;
 
 public class CarsModel extends AbstractTableModel {
   private static final long serialVersionUID = 1L;
 
   private final String[] ColumnsName = { "Producer", "Modification", "Manifacture Date", "Color", "Price for Day" };
 
   private List<Car> tableData = new ArrayList<Car>();
 
   @Override
   public String getColumnName(int column) {
     return this.ColumnsName[column];
   }
 
   @Override
   public int getColumnCount() {
     return this.ColumnsName.length;
   }
 
   @Override
   public int getRowCount() {
     return this.tableData.size();
   }
 
   @Override
   public Object getValueAt(int rowIndex, int colIndex) {
     Object result;
     SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
     Car car = getRow(rowIndex);
 
     switch (colIndex) {
     case 0:
       result = car.getProducer();
       break;
     case 1:
       result = car.getModification();
       break;
     case 2:
       result = sdf.format(car.getManifactureDate());
       break;
     case 3:
       result = car.getColor();
       break;
     case 4:
       result = car.getPriceForDay();
       break;
     default:
       throw new RuntimeException("Invalid column index");
     }
     return result;
 
   }
 
   @Override
   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
     String valueStr = (String) aValue;
     boolean changeDone = true;
     Car car = getRow(rowIndex);
     if (columnIndex == 2) {
       car.setColor(valueStr);
     } else {
       try {
         car.setPriceForDay(parseDouble(valueStr));
       } catch (NumberFormatException e) {
        showMessageDialog(null, " !", "", ERROR_MESSAGE);
         changeDone = false;
       }
 
     }
     if (changeDone) {
       fireTableDataChanged();
     }
   }
 
   @Override
   public boolean isCellEditable(int row, int col) {
     if (col == 2 || col == 4) {
       return true;
     }
     return false;
   }
 
   public void setTableData(List<Car> tableData) {
     this.tableData = tableData;
   }
 
   public Car getRow(int rowIndex) {
     Car car = this.tableData.get(rowIndex);
     return car;
   }
 
 }
