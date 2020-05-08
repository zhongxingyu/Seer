 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.swing.table.AbstractTableModel;
 
 import edu.bu.cs673.AwesomeAlphabet.value.ThemeViewData;
 
 public class ThemeTableModel extends AbstractTableModel {
 	private static final long serialVersionUID = -224567130880087561L;
 	
 	private static ArrayList<ThemeViewData> m_themeTable = new ArrayList<ThemeViewData>();
 	
 	@Override
 	public String getColumnName(int column) {
 		if (column == 0)
 			return "Theme Name";
 		else if (column == 1)
 			return "No. Words in Theme";
 		else
 			return "Col. " + column;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 2;
 	}
 
 	@Override
 	public int getRowCount() {
 		return m_themeTable.size();
 	}
 
 	@Override
 	public Object getValueAt(int row, int column) {
 		if (column == 0) {
 			return m_themeTable.get(row).name();
 		} else if (column == 1) {
 			return "" + m_themeTable.get(row).count();
 		} else {
 			return "(* " + column + ", " + row + " *)";
 		}
 	}
 
 	public void removeAllElements() {
 		m_themeTable.clear();
 	}
 
 	public void addElement(ThemeViewData next) {
 		m_themeTable.add(next);
 	}
 	
 	public void sort() {
 		Collections.sort(m_themeTable, new Comparator<ThemeViewData>() {
 			public int compare(ThemeViewData a, ThemeViewData b) {
 				return a.name().compareTo(b.name());
 			}
 		});
 	}
 
 	public String getThemeName(int selectedRow) {
		if (selectedRow >= 0)
			return m_themeTable.get(selectedRow).name();
		return null;
 	}
 	
 	public boolean rowIsEditable(int selectedRow) {
 		if (selectedRow < 0 || selectedRow >= m_themeTable.size())
 			return false;
 		
 		return m_themeTable.get(selectedRow).editable();
 	}
 }
