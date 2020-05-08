 package org.infuse.hexview;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.swing.table.AbstractTableModel;
 
 @SuppressWarnings("serial")
 public class HexViewTableModel extends AbstractTableModel {
   
   private byte[] _data;
   private final String[] _names = new String[] { "Offset", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "Ascii" };
   
   public HexViewTableModel(File f) throws IOException {
     setFile(f);
   }
   
   public void setFile(File f) throws IOException {
     if (f == null) {
       _data = new byte[0];
     } else {
       _data = new byte[(int)f.length()];
       FileInputStream input = new FileInputStream(f);
       input.read(_data);
       input.close();
     }
   }
   
   @Override
   public int getRowCount() {
    return (_data.length / 16) + ((_data.length % 16) > 1 ? 1 : 0);
   }
   
   @Override
   public int getColumnCount() {
     return _names.length;
   }
   
   @Override
   public String getColumnName(int columnIndex) {
     if (columnIndex >= _names.length) {
       return null;
     } else {
       return _names[columnIndex];
     }
   }
   
   @Override
   public boolean isCellEditable(int rowIndex, int columnIndex) {
     return false;
   }
   
   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
     if (columnIndex == 0) {
      return String.format("%08X", rowIndex);
     } else if (columnIndex == 17) {
      int offset = rowIndex * 16;
      int count = _data.length - offset >= 16 ? 16 : _data.length - offset;
       String ret = "";
       for (int i = 0; i < count; i++) {
         int value = _data[offset+i] & 0xFF;
         if (value < 32 || value > 126) {
           ret += ".";
         } else {
           ret += String.format("%c", value);
         }
       }
       return ret;
     } else {
      int offset = (rowIndex*16)+(columnIndex-1);
       if (offset >= _data.length) {
         return "";
       } else {
        return String.format("%02X", _data[(rowIndex*16)+(columnIndex-1)] & 0xFF);
       }
     }
   }
   
 }
