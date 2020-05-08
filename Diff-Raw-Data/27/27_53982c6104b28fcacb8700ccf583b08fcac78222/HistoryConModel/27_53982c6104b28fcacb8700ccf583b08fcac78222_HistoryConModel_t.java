 package admin.panel.general;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 import admin.Utils;
 import data.Contestant;
 import data.GameData;
 import data.GameData.UpdateTag;
 
 public class HistoryConModel extends AbstractTableModel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	private String[] columnNames = {
 			"ID", "Last Name", "First Name"
 	};
 
 	private List<Contestant> data;
 	
 	private boolean active;
 	
 	public static final int INDEX_ID = 0,
 			INDEX_LASTNAME = 1,
 			INDEX_FIRSTNAME = 2,
 			INDEX_ACTIVE = 3;
 	
 	/**
 	 * Creates the table model which controls the table's actions and data.
 	 */
 	public HistoryConModel(JTable table, boolean castTable) {
 		GameData g = GameData.getCurrentGame();
 		
 		active = !castTable;
 		data = new ArrayList<Contestant>(g.getActiveContestants(active));
 		
 		if (castTable) {
 			columnNames = Arrays.copyOf(columnNames, columnNames.length+1);
 			columnNames[columnNames.length-1] = "Week Cast";
 		}
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
 		switch (col) {
 		case INDEX_ID:
 			return data.get(row).getID();
 		case INDEX_FIRSTNAME:
 			return data.get(row).getFirstName();
 		case INDEX_LASTNAME:
 			return data.get(row).getLastName();
 		case INDEX_ACTIVE:
 			int date = data.get(row).getCastDate();
 			if (date > 0) {
 				return date;
 			}
 			return "Active";
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public boolean isCellEditable(int row, int col) {
 		// Always uneditable
 		return false;
 	}
 
 	@Override
 	public void setValueAt(Object value, int row, int col) {
 		return; // overridden to stop super.
 	}
 	
 	protected void loadContestantByWeek(int week) {
 		GameData g = GameData.getCurrentGame();
 		List<Contestant> temp = new ArrayList<Contestant>(g.getAllContestants());
 		data = new ArrayList<Contestant>(temp.size());
 		
 		Collections.sort(temp, Utils.getContComparator(Utils.CompType.CONTNT_DATE));
 		
 		//if (!active) // count backwards. :)
 		//	Collections.reverse(temp);
 		
 		for (Contestant c: temp) {
 			if (active) {
 				if ( !c.isCastOff() || c.getCastDate() > week) {
 					data.add(c);
 				} else {
 					break;
 				}
 			} else {
 				if (c.isCastOff() && c.getCastDate() <= week) {
 					data.add(c);
 				} 
 			}
 		}
 		
 		//if (!active) {
 		//	Collections.sort(data, Utils.getContComparator(Utils.CompType.CONTNT_DATE));
 		//}
 		
 		fireTableDataChanged();
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		GameData g = (GameData)o;
 		
 		@SuppressWarnings("unchecked")
 		EnumSet<UpdateTag> up = (EnumSet<UpdateTag>)arg;
 		
 		if (up.contains(UpdateTag.ADD_CONTESTANT) || 
 				up.contains(UpdateTag.REMOVE_CONTESTANT) ||
 				up.contains(UpdateTag.CONTESTANT_CAST_OFF)) {
 			data = new ArrayList<Contestant>(g.getActiveContestants(active));
 			
 			fireTableDataChanged();
 		}
 	}
 }
