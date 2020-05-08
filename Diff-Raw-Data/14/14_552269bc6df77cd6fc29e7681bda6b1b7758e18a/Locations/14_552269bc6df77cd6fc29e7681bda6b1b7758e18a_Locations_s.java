 package uonlineeditor;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author houjing
  */
 public class Locations {
 
 	public ArrayList<Location> locs = new ArrayList<>();
 
 	public int areaId;
 	public Integer lastId = 0;
 	public int selected = -1;
 
 	Locations (int areaId) {
 		this.areaId = areaId;
 	}
 
 	Locations (List<String[]> locs) {
 		for (Iterator<String[]> it = locs.iterator(); it.hasNext();) {
 			Location l = new Location(it.next());
 			lastId = Math.max(lastId, l.id);
 			this.locs.add(l);
 		}
 	}
 
 	static List<String[]> asData(List<Location> ll) {
 		List<String[]> csv = new ArrayList<>();
 		for(Location l: ll) {
 			csv.add(l.asData());
 		}
 		return csv;
 	}
 
 	int getIndexOf(Object obj, int param) {
 		for (int i = 0; i < locs.size(); i++) {
 			if (locs.get(i).getParameter(param) == obj) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	Location getSelected() {
 		if (selected == -1 || selected > locs.size() - 1) return null;
 		return locs.get(selected);
 	}
 
 	void addLocation() {
 		if (selected == -1 || selected > locs.size() - 1) {
			locs.add(new Location(areaId, lastId++));
 			selected = 0;
 		}
 		else if (locs.size() == selected + 1) {
			locs.add(new Location(areaId, lastId++));
 			selected = locs.size()-1;
 		}
 		else {
			locs.add(selected + 1, new Location(areaId, lastId++));
 			selected = selected + 1;
 		}
 	}
 
 	void removeLocation() {
 		if(selected == -1) return;
 		locs.remove(selected);
 		selected--;
 		if(selected == -1 && !locs.isEmpty()) selected=0;
 	}
 
 }
