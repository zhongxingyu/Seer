 package uk.ac.gla.dcs.tp3.w.ui;
 
 import java.util.HashMap;
 
 import javax.swing.table.AbstractTableModel;
 
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 public class TableModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = 1L;
 	private HashMap<String, Division> data;
 	private String current = "National West";
 	private String[] names = { "Team", "Points", "Games Played", "Eliminated?" };
 
 	public TableModel(HashMap<String, Division> d) {
 		data = d;
 	}
 
 	public String getCurrent() {
 		return current;
 	}
 
 	public void setCurrent(String s) {
 		current = s;
 		this.fireTableDataChanged();
 	}
 
 	public int getColumnCount() {
 		return names.length;
 	}
 
 	public String getColumnName(int col) {
 		return names[col];
 	}
 
 	public int getRowCount() {
 		Division temp = data.get(current);
 		return (temp == null) ? 0 : temp.getTeams().size();
 	}
 
	
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case (1):
			return Integer.class;
		case (2):
			return Integer.class;
		default:
			return String.class;
		}
	}

 	public Object getValueAt(int row, int col) {
 		Division temp = data.get(current);
 		if (temp == null)
 			return null;
 		Team[] t = temp.teamsToArray();
 		switch (col) {
 		case (0):
 			return t[row];
 		case (1):
 			return t[row].getPoints();
 		case (2):
 			return t[row].getGamesPlayed();
 		case (3):
 			return t[row].isEliminated();
 		}
 		return null;
 	}
 
 }
