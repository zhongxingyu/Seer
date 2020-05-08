 package erekspeed;
 
 import java.io.Serializable;
 import java.util.BitSet;
 
 /**
  * Created by IntelliJ IDEA.
  * User: espeed
  * Date: Jul 11, 2010
  * Time: 2:38:44 PM
  * Map Wrapper which contains Direction as well
  */
 public class MapDirTimeWrapper extends MapWrapper implements Serializable {
 
 
 	private final boolean left;
 	private final int timeLeft;
 
	public MapDirTimeWrapper(byte[][] map, boolean left, int tl) {
		super(map);
		cachedHash += (left ? 27017 : 48299);
		cachedHash += tl * 27357;
		this.left = left;
		this.timeLeft = tl;
	}
	
 	public MapDirTimeWrapper(BitSet map, boolean left, int tl) {
 		super(map);
 		cachedHash += (left ? 27017 : 48299);
 		cachedHash += tl * 27357;
 		this.left = left;
 		this.timeLeft = tl;
 	}
 
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 
 		if (o == null || getClass() != o.getClass())
 			return false;
 
 		if (!super.equals(o)) {
 			return false;
 		}
 
 		MapDirTimeWrapper other = (MapDirTimeWrapper) o;
 
 		return (left == other.left) && (timeLeft == other.timeLeft);
 	}
 
 //    protected void generateHash() {
 //        cachedHash += (left?27017:48299);
 //        System.out.println(cachedHash);
 //    }
 }
