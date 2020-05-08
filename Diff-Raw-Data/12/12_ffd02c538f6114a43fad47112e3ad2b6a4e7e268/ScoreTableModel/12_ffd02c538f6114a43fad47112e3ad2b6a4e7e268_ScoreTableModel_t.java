 package org.joedog.pinochle.model;
 
 import javax.swing.table.*;
 
 public class ScoreTableModel extends AbstractTableModel {
   private String[] headers = {"Us", "Them"};
   Object[][] data = {
     {new Integer(0), new Integer(0)}, // meld
     {new Integer(0), new Integer(0)}, // tricks
     {new Integer(0), new Integer(0)}, // hand total 
     {new Integer(0), new Integer(0)}, // grand total
   };
 
   public ScoreTableModel()  {
 
   }
 
   public ScoreTableModel(String us, String them) {
     headers[0] = us;
     headers[1] = them;
   }
 
   public ScoreTableModel(String ua, String ub, String ta, String tb) {
    if (ua == null || ua.length() < 1 || ub == null || ub.length() < 1) {
      headers[0] = "Us";  
    } else {
      headers[0] = ua.substring(0, 1)+"/"+ub.substring(0,1);  
    }
    if (ua == null || ua.length() < 1 || ub == null || ub.length() < 1) {
      headers[1] = "Them";
    } else {
      headers[1] = ta.substring(0, 1)+"/"+tb.substring(0,1);
    }
   }
 
   public void resetScore() {
     System.out.println("resetting score....");
     for (int x = 0; x < headers.length; x++) {
       for (int y = 0; y < data.length; y++) {
         this.setValueAt((Integer)0, x, y); 
       }
     }
   }
 
   public int getColumnCount() {
     return headers.length;
   }
 
   public int getRowCount() {
     return data.length;
   }
 
   public String getColumnName(int col) {
     return headers[col];
   }
 
   public Object getValueAt(int row, int col) {
     return data[row][col];
   }
 
   public Class getColumnClass(int c) {
     return getValueAt(0, c).getClass();
   }
 
   public void setValueAt(Object o, int row, int col) {
     data[row][col] = o;
     fireTableDataChanged();
   }
 }
 
