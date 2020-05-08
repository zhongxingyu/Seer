 /*
  * Copyright (C) 2012 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package example.swingclient;
 
 import java.util.ArrayList;
 import javax.swing.table.AbstractTableModel;
 import org.javnce.upnp.RemoteServerInfo;
 
 /**
  * The JTable model for showing remote Javnce servers.
  */
 public class RemoteServerInfoTableModel extends AbstractTableModel {
 
     /**
      * The column names.
      */
     static final private String[] columnNames = {"Name", "Address"};
     /**
      * The array of Javnce servers.
      */
     final private ArrayList<RemoteServerInfo> data;
 
     /**
      * Instantiates a new remote server info table model.
      */
     public RemoteServerInfoTableModel() {
         data = new ArrayList<>();
     }
 
     /* (non-Javadoc)
      * @see javax.swing.table.AbstractTableModel#getColumnName(int)
      */
     @Override
     public String getColumnName(int column) {
         return columnNames[column];
     }
 
     /* (non-Javadoc)
      * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
      */
     @Override
     public boolean isCellEditable(int row, int column) {
         return false;
     }
 
     /* (non-Javadoc)
      * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
      */
     @Override
    public Class getColumnClass(int column) {
         return String.class;
     }
 
     /* (non-Javadoc)
      * @see javax.swing.table.TableModel#getValueAt(int, int)
      */
     @Override
     public Object getValueAt(int row, int column) {
         RemoteServerInfo info = data.get(row);
         switch (column) {
             case 0:
                 return info.getName();
             case 1:
                 return info.getAddress().getAddress().getCanonicalHostName();
             default:
                 return new Object();
         }
     }
 
     /* (non-Javadoc)
      * @see javax.swing.table.TableModel#getRowCount()
      */
     @Override
     public int getRowCount() {
         return data.size();
     }
 
     /* (non-Javadoc)
      * @see javax.swing.table.TableModel#getColumnCount()
      */
     @Override
     public int getColumnCount() {
         return columnNames.length;
     }
 
     /**
      * Adds the remote server info.
      *
      * @param info the info
      */
     public void addRemoteServerInfo(RemoteServerInfo info) {
         data.add(info);
         fireTableRowsInserted(data.size() - 1, data.size() - 1);
     }
 
     /**
      * Gets the remote server info.
      *
      * @param i the index in table
      * @return the remote server info
      */
     public RemoteServerInfo getRemoteServerInfo(int i) {
         return data.get(i);
     }
 }
