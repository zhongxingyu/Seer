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
 
            for (i = 0; i < 6; i++)
                 if (record.chaddr[i] != mac[i])
                     break;
            if (i == 6)
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
                return String.format("%2s:%2s:%2s:%2s:%2s:%2s", Integer.toHexString(record.chaddr[0]), Integer.toHexString(record.chaddr[1]), Integer.toHexString(record.chaddr[2]), Integer.toHexString(record.chaddr[3]), Integer.toHexString(record.chaddr[4]), Integer.toHexString(record.chaddr[5])).replace(" ", "0");
             case 1:
                return String.format("%d.%d.%d.%d", DHCPController.byteToInt(record.ip[0]), DHCPController.byteToInt(record.ip[1]), DHCPController.byteToInt(record.ip[2]), DHCPController.byteToInt(record.ip[3]));
             case 2:
                if (record.ackTime != null)
                    return record.ackTime.toString();
         }
 
         return null;
     }
 
     public Class getColumnClass(int c) {
        return ("").getClass();
     }
 }
