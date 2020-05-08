 package org.infuse.hexview;
 
 import java.awt.Font;
 import java.io.IOException;
 
 import javax.swing.JTable;
 import javax.swing.table.TableColumnModel;
 
 @SuppressWarnings("serial")
 public class HexViewTable extends JTable {
   
   public HexViewTable() throws IOException {
     setModel(new HexViewTableModel(null));
     setFont(new Font("Courier New", getFont().getStyle(), getFont().getSize()));
     TableColumnModel cm = getColumnModel();
    fixWidth(cm, 0, 80);
    for (int i = 1; i < 17; i++) { fixWidth(cm, i, 22); }
    cm.getColumn(17).setMinWidth(80);
     setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
     setFillsViewportHeight(true);
   }
   
   private void fixWidth(TableColumnModel cm, int index, int width) {
     cm.getColumn(index).setMinWidth(width);
     cm.getColumn(index).setPreferredWidth(width);
     cm.getColumn(index).setMaxWidth(width);
   }
   
 }
