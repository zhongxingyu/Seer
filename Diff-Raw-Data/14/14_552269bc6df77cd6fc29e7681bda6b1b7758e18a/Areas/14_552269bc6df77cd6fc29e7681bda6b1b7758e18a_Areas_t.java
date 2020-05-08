 package uonlineeditor;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author houjing
  */
 public class Areas {
 
 	Areas() {}
 
 	Areas (List<String[]> areas) {
 		for (Iterator<String[]> it = areas.iterator(); it.hasNext();) {
 			Area ar = new Area(it.next());
 			lastId = Math.max (lastId, (int) ar.getParameter(Area.ID));
 			this.areas.add(ar);
 		}
 	}
 
 	List<Location> getLocations() {
 		ArrayList<Location> ll = new ArrayList<>();
 		for (Area a: areas) {
 				ll.addAll(a.locs.locs);
 		}
 		return ll;
 	}
 
 	void attachLocations(List<Location> ll) {
 		for(Location l: ll) {
 			for(Area a: areas) {
 				if (a.getParameter(Area.ID) == l.area) {
 					a.addLocation(l);
 					break;
 				}
 			}
 		}
 	}
 
 	int getRowNumberOf(Object obj, int col) {
 		for (int i = 0; i < areas.size(); i++) {
 			if (areas.get(i).getParameter(col) == obj) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	List<String[]> asData() {
 		List<String[]> areas = new ArrayList<>();
 		for (Iterator<Area> it = this.areas.iterator(); it.hasNext();) {
 			Area i = it.next();
 			areas.add(i.asData());
 		}
 		return areas;
 	}
 
 	int addArea(int index) {
 		if (index == -1 || index > areas.size() - 1) {
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
 		if (index.length == 0 || areas.size() == -1) return -1;
 		for (int i = index.length - 1; i >= 0 ; i--) {
 			if (index[i] >= areas.size()) continue;
 			removeArea(index[i]);
 		}
 		return index[index.length-1] == areas.size() + 1 - index.length ? areas.size() - 1 : index[0];
 	}
 
 
 	public ArrayList<Area> areas = new ArrayList<>();
 	private int lastId = 0;
	public static int lastLocId;
 
 }
