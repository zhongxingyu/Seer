 package uonlineeditor;
 
import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * @author houjing
  */
 public class Areas extends DefaultTableModel {
 
 	Areas() {}
 
 	Areas (List<String[]> areas) {
 		//System.out.println("Areas constructor");
 		for (Iterator<String[]> it = areas.iterator(); it.hasNext();) {
 			Area ar = new Area(it.next());
 			lastId = lastId > (int) ar.getParameter(1) ? lastId : (int) ar.getParameter(1);
 			this.areas.add(ar);
 		}
 	}
 
 	List<String[]> asData() {
 		List<String[]> areas = new ArrayList<>();
 		for (Iterator<Area> it = this.areas.iterator(); it.hasNext();) {
 			Area i = it.next();
 			areas.add(i.asData());
			System.out.println(ExtString.join(i.asData(), ", "));
 		}
 		return areas;
 	}
 
 	int addNewArea(int index) {
 		if (index == -1) {
 			areas.add(new Area(++lastId));
 			return 0;
 		}
 		else if (areas.size() == index + 1) {
 			areas.add(new Area(++lastId));
 			return areas.size()-1;
 		}
 		else {
 			areas.add(index + 1, new Area(++lastId));
 			return  index + 1;
 		}
 	}
 
 	void removeArea(int index) {
 		areas.remove(index);
 	}
 
 	int removeAreas(int[] index) {
 		if (this.getRowCount() == 1) return 0;
 		for (int i = index.length - 1; i >= 0 ; i--) {
 			this.removeArea(index[i]);
 		}
 		return index[index.length-1] == areas.size() + 1 - index.length ? areas.size() - 1 : index[0];
 	}
 
 	public Class<?> getColumnClass(int columnIndex) {
 		return String.class;
 	}
 
 	public int getColumnCount() {
 		return 2;
 	}
 
 	public String getColumnName(int columnIndex) {
 		switch (columnIndex) {
 			case 0:
 				return "Зона";
 			case 1:
 				return "id";
 			default:
 				return "";
 		}
 	}
 
 	public int getRowCount() {
 		return areas == null ? 0 : areas.size();
 	}
 
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		return areas.get(rowIndex).getParameter(columnIndex);
 	}
 
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return true;
 	}
 
 	public void addTableModelListener(TableModelListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeTableModelListener(TableModelListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void setValueAt(Object value, int rowIndex, int columnIndex) {
 		areas.get(rowIndex).setParameter(value, columnIndex);
 	}
 
 	private Set<TableModelListener> listeners = new HashSet<>();
 	private ArrayList<Area> areas = new ArrayList<>();
 	private int lastId = 0;
 
 }
