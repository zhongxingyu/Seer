 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.tr0llhoehle.buschtrommel.gui;
 
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.RemoteShare;
 import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.swing.table.AbstractTableModel;
 
 /**
  * 
  * @author benjamin
  */
 public class LocalSharesTableModel extends AbstractTableModel {
 
 	private Vector<String[]> shares = new Vector<String[]>();;
 
 	private String[] names = new String[] { "Filename", "Meta-Information", "Path", "Size", "TTL", "activated" };
 	private boolean[] canEdit = new boolean[] { true, true, false, false, true, false };
 
 	public String getColumnName(int col) {
 		return names[col].toString();
 	}
 
 	public void setValueAt(Object value, int row, int column) {
 		// TODO validity check and so on
 		// System.out.println("changing meta");
 		String string = (String) value;
 		if (column == 4) {
 			// sanity check
 
 			Long ttl = Long.valueOf(string);
 			
 			if (ttl > 0 || ttl == -1) {
 				shares.get(row)[column] = (String) string;
 			} else {
 				return;
 			}
 		} else {
 			shares.get(row)[column] = (String) string;
 		}
 		this.fireTableCellUpdated(row, column);
 	}
 
 	public Class getColumnClass(int columnIndex) {
 		// return types [columnIndex];
 		return java.lang.String.class;
 	}
 
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return canEdit[columnIndex];
 
 	}
 
 	@Override
 	public int getRowCount() {
 		return shares.size();
 		// throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public int getColumnCount() {
 		return names.length;
 		// throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public String getValueAt(int rowIndex, int columnIndex) {
 		if (rowIndex < shares.size()) {
 			return shares.get(rowIndex)[columnIndex];
 			// throw new UnsupportedOperationException("Not supported yet.");
 		} else
 			return null;
 	}
 	public void setActive(int index, String active){
 		shares.get(index)[5] = active;
 		this.fireTableCellUpdated(index, 5);
 	}
 
 	public synchronized void addShare(String name, String path, String size, String ttl) {
 		if (path == null) {
 			System.out.print("path is null");
 			return;
 		}
 		String eintrag[] = new String[names.length];
 		// "Filename", "Meta-Information", "Path", "Size","TTL"
 		eintrag[0] = name;
 		eintrag[1] = "";
 		eintrag[2] = path;
 		eintrag[3] = size;
 		eintrag[4] = ttl;
 		eintrag[5] = "false";
 		if (ttl == "") {
 			ttl = "-1";
 		}
 
 		this.shares.add(eintrag);
 		this.fireTableDataChanged();
 	}
 	
 	public synchronized void addShareMeta(String name, String meta, String path, String size, String ttl, String active) {
 		if (path == null) {
 			System.out.print("path is null");
 			return;
 		}
 		String eintrag[] = new String[names.length];
 		// "Filename", "Meta-Information", "Path", "Size","TTL"
 		eintrag[0] = name;
 		eintrag[1] = meta;
 		eintrag[2] = path;
 		eintrag[3] = size;
 		eintrag[4] = ttl;
 		eintrag[5] = active;
 		if (ttl == "") {
 			ttl = "-1";
 		}
 
 		this.shares.add(eintrag);
 		this.fireTableDataChanged();
 	}
 
 	public synchronized void addMeta(int index, String meta) {
 		shares.get(index)[1] = meta;
 		shares.get(index)[5] = "meta outdated";
 		this.fireTableDataChanged();
 	}
 
 	synchronized void removeShare(int index) {
 		if (index < shares.size()) {
 			shares.remove(index);
			this.fireTableDataChanged();
 		}
 	}
 
 }
