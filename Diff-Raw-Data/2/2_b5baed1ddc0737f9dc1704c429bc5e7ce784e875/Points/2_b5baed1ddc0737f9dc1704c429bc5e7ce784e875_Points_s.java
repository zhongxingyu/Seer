 /**
  * $$\\ToureNPlaner\\$$
  */
 package algorithms;
 
 import com.carrotsearch.hppc.IntArrayList;
 
 public class Points {
 	private IntArrayList points;
 
 	public Points() {
 		points = new IntArrayList();
 	}
 
 	public void addPoint(int lat, int lon) {
 		points.add(lat);
 		points.add(lon);
 	}
 
 	public void addEmptyPoints(int num) {
		points.resize(this.size() + num*2);
 	}
 
 	public int getPointLat(int index) {
 		return points.get(index * 2);
 	}
 
 	public int getPointLon(int index) {
 		return points.get(index * 2 + 1);
 	}
 
 	public void setPointLat(int index, int lat) {
 		points.set(index * 2, lat);
 	}
 
 	public void setPointLon(int index, int lon) {
 		points.set(index * 2 + 1, lon);
 	}
 
 	public int size() {
 		return points.size() / 2;
 	}
 
 }
