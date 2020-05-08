 package admin.playertab;
 
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 
 import data.Contestant;
 import data.InvalidFieldException;
 import data.User;
 
 import admin.AdminUtils;
 import admin.data.*;
 
 
 public class PlayerTableModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = 1L;
 	private String[] columnNames;
 	private List<User> data;
 	private boolean frozen = false;
 	
 	//public static final int INDEX_SELECT = 0;
 	public static final int INDEX_ID = 0;
 	public static final int INDEX_LASTNAME = 1;
 	public static final int INDEX_FIRSTNAME = 2;
 	public static final int INDEX_POINTS	= 3;
 	public static final int INDEX_WEEKLY_PICK = 4;
 	public static final int INDEX_ULT_PICK = 5;
 	
 	private int sortColumn = INDEX_ID;
 	private List globalData;
 	
 	/**
 	 * Creates the table model which controls the table's actions and data.
 	 * @param users The global reference to the actual GameData.
 	 */
 	public PlayerTableModel(List<User> users) {
 		
 		globalData = AdminUtils.castListToUncast(users);
 		data = users;
 		
		columnNames = new String[] { "ID", "Last", "First", "Points", 
 				"Weekly Pick", "Ultimate Pick" };
 	}
 	
 	@Override
 	public String getColumnName(int col) {
         return columnNames[col].toString();
     }
 	
 	@Override
     public int getRowCount() {
 		return data.size(); 
 	}
 	
 	@Override
     public int getColumnCount() {
 		return columnNames.length; 
 	}
     
 	@Override
 	public Object getValueAt(int row, int col) {
         User user = (User)data.get(row);
         switch (col) {
         case INDEX_ID:
         	return user.getID();
         
         case INDEX_FIRSTNAME:
         	return user.getFirstName();
         
         case INDEX_LASTNAME:
         	return user.getLastName();
         
         case INDEX_POINTS:
         	return user.getPoints();
         	
         case INDEX_WEEKLY_PICK:
         	return user.getWeeklyPick().toString();
         
         case INDEX_ULT_PICK:
         	return user.getUltimatePick().toString();
         
         default:
         	return null;
         }
     }
 	
 	@Override
     public boolean isCellEditable(int row, int col) { 
 		return false; // never?
 	}
     
 	@Override
 	public void setValueAt(Object value, int row, int col) {
 		User user = (User)data.get(row);
         
         switch (col) {
         case INDEX_ID:
         	// this can't be changed..
         	break;
         
         case INDEX_FIRSTNAME:
         	try {
 				user.setFirstName((String)value);
 			} catch (InvalidFieldException e1) {}
         	break;
         
         case INDEX_LASTNAME:
         	try {
 				user.setLastName((String)value);
 			} catch (InvalidFieldException e) { }
         	break;
         
         default:
         	// can't change here!
         	break;
         }
         
         fireTableCellUpdated(row, col);
     }
 
 	/**
 	 * Gets a User based on the row passed, this is used to read clicks.
 	 * @param row The row to gather from
 	 * @return Data contained in the Row in a User form
 	 */
     protected User getByRow(int row) {
     	return data.get(row);
     }
     
     /**
      * Gets the row number of a User's ID
      * @param u The user to find
      * @return Row number, -1 if not found
      */
     protected int getRowByUser(User u) {
     	if (u == null) 
     		return -1;
     	
     	String id = u.getID();
     	
     	for (int i = 0; i < data.size(); i++) {
     		if (data.get(i).getID().equals(id)) {
     			return i;
     		}
     	}
     	
     	return -1;
     }
 	
     /**
 	 * Sorts the table by the column specified, will update the table.
 	 * @param col -1 for stored value, else the column passed. Default
 	 * to no sorting otherwise.
 	 */
 	protected void sortTableBy(int col) {
 		Comparator<User> comp;
 		
 		// use the stored column if -1 is passed.
 		col = (col == -1 ? sortColumn : col);
 		
 		switch (col) {
         case INDEX_ID:
         	comp = AdminUtils.getUserComparator(AdminUtils.CompType.USER_ID);
         	break;
         
         case INDEX_FIRSTNAME:
         	comp = AdminUtils.getUserComparator(AdminUtils.CompType.USER_FIRST_NAME);
         	break;
         
         case INDEX_LASTNAME:
         	comp = AdminUtils.getUserComparator(AdminUtils.CompType.USER_LAST_NAME);
         	break;
         
         case INDEX_POINTS:
         	comp = AdminUtils.getUserComparator(AdminUtils.CompType.USER_POINTS);
         	break;
         	
         case INDEX_ULT_PICK:
         	comp = AdminUtils.getUserComparator(AdminUtils.CompType.USER_ULT_PICK);
         	break;
         	
         case INDEX_WEEKLY_PICK:
         	comp = AdminUtils.getUserComparator(AdminUtils.CompType.USER_WEEKLY_PICK);
         	break;
 
         // others aren't valid to sort by (too ambiguous)
         default:
         	return;
         }
 		
 		Collections.sort(data, comp);
 		fireTableDataChanged();
 		
 		sortColumn = col;
 	}
 	
 	/**
 	 * Sorts the table using sortTableBy with the current sorted column.
 	 */
 	protected void sortTable() {
 		sortTableBy(-1);
 	}
 	
 	/**
 	 * Adds a contestant, resorts the table. Updates the stored game data.
 	 * @param c 
 	 */
 	protected void addUser(User u) {
 		data.add(u);
 		sortTable();
 		
 		GameData g = GameData.getCurrentGame();
 		g.addUser(u);
 	}
 	
 	protected void removeUser(User u) {
 		data.remove(u);
 		sortTable();
 		
 		GameData g = GameData.getCurrentGame();
 		g.removeUser(u);
 	}
 	
 	/**
 	 * Updates the stored contestant with the same ID to hold the currently
 	 * stored information. Overwrites everything in the current position, also
 	 * will resort the data table.
 	 * <br>
 	 * If the ID is not present, it WILL create a new entry (no sort).
 	 * @param c New contestant data.
 	 */
 	protected void updateUser(User u) {
 		User updateUser = null;
 		
 		for (int i = 0; i < globalData.size(); i++) {
 			updateUser = (User)globalData.get(i);
 			if (updateUser.getID().equalsIgnoreCase(u.getID())) {
 				break;
 			}
 			updateUser = null;
 		}
 		
 		if (updateUser != null) {
 			try { 
 				updateUser.update(u); 
 			} catch (InvalidFieldException e) { }
 		} else {
 			addUser(u);
 		}
 		
 		sortTable();
 	}
 	
 	protected class SortColumnAdapter extends MouseAdapter {
 		
 		public void mouseClicked(MouseEvent e) {
 	        JTable table = ((JTableHeader)e.getSource()).getTable();
 	        TableColumnModel colModel = table.getColumnModel();
 
 	        // The index of the column whose header was clicked
 	        int vIndex = colModel.getColumnIndexAtX(e.getX());
 
 	        // Return if not clicked on any column header
 	        if (vIndex == -1) {
 	            return;
 	        }
 	        
 	        int mIndex = table.convertColumnIndexToModel(vIndex);
 	        
 	        // we have the column index, sort the data
 	        sortTableBy(mIndex);
 	    }
 	}
 }
