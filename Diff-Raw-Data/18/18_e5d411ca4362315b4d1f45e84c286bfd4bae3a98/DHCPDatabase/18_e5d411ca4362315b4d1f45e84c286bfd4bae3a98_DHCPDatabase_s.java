 package dhcpserver;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.swing.table.AbstractTableModel;
 
 public class DHCPDatabase extends AbstractTableModel {
 
     public static DHCPDatabase model = new DHCPDatabase();
 
     private String[] columnNames = {"Client Address", "IP Address", "Acked Time"};
     static ArrayList<DHCPRecord> data = new ArrayList<DHCPRecord>();
 
     public static DHCPRecord getRecord(byte[] mac)
     {
         int i = 0;
         Iterator<DHCPRecord> itr = data.iterator();
         while (itr.hasNext())
         {
             DHCPRecord record = itr.next();
 
            for (i = 0; i < 16; i++)
                 if (record.chaddr[i] != mac[i])
                     break;
            if (i == 16)
                 return record;
         }
         return null;
     }
 
     // model functions
     public int getColumnCount() {
         return columnNames.length;
     }
 
     public int getRowCount() {
         return data.size();
     }
 
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     public Object getValueAt(int row, int col) {
 
         DHCPRecord record = data.get(row);
         switch (col)
         {
             case 0:
                return '1'; //record.chaddr;
             case 1:
                return '2'; //record.ip;
             case 2:
                return '3'; //record.ackTime;
         }
 
         return null;
     }
 
     public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
     }
 }
