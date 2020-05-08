 package erekspeed;
 
 import ch.idsia.benchmark.mario.environments.MarioEnvironment;
 
 import java.io.Serializable;
 import java.util.BitSet;
 
 /**
  * User: espeed
  * Date: Aug 16, 2009
  * Time: 2:26:06 AM
  * We wrap the arrays so that I can use them in the hash map.
  */
 public class MapWrapper implements Serializable {
 	private  byte[][] map = new byte[0][0];;
 	private BitSet mapB = new BitSet();
 	public static int rfheight = 0;
 	public static int rfwidth = 0;
 
 	// TODO: Possibly include other variables besides map, like mario status and time left as well.
 	protected int cachedHash;
 	static final long serialVersionUID = -1534403799001948431L;
 
 	public MapWrapper(BitSet mapB) {
 		this.mapB = mapB;
 		generateHashB();
 	}
 	
 	public MapWrapper(byte[][] map) {
 		//TODO: Check to see if this calculation is accurate
 		//this.map = new byte[rfheight][rfwidth];
 		//rfwidth and height aren't being set
 		this.map = new byte[map.length][map[0].length];
 
 		for (int i = 0; i < map.length; i++) {
 			for (int j = 0; j < map[i].length; j++) {
 				switch (map[i][j]) {
 /*                    case (25):
                     case (14):
                     case (15):
                         this.map[i][j] = 0;
                         break;
                     case (-11):
                     case (20):
                     case (16):
                     case (21):
                         this.map[i][j] = -10;
                         break;
                     case (3):
                     case (4):
                     case (5):
                     case (6):
                     case (7):
                     case (8):
                         this.map[i][j] = 2;
                         break;
                     case (10):
                     case (12):
                         this.map[i][j] = 9;
                         break;*/
 					default:
 						this.map[i][j] = map[i][j];
 				}
 			}
 		}
 
 		generateHash();
 	}
 
 	public byte[][]
 	getMap() {
 		return map;
 	}
 
 	@Override
 	public boolean
 	equals(Object o) {
 		if (this == o)
 			return true;
 
 		if (o == null || getClass() != o.getClass())
 			return false;
 
 		MapWrapper other = (MapWrapper) o;
 
 		if (other.cachedHash != this.cachedHash)
 			return false;
 		
		if(other.mapB != this.mapB)
 			return false;
 
 		byte[][] otherMap = other.getMap();
 		for (int i = 0; i < map.length; i++) {
 			for (int j = 0; j < map[i].length; j++) {
 				if (map[i][j] != otherMap[i][j]) {
 					//System.out.println("Collision");
 					return false;
 
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public int
 	hashCode() {
 		return cachedHash;
 	}
 
 	// don't change this after starting evolution
 
 	protected void
 	generateHash() {
 		int hash = 17;
 
 		for (int i = 0; i < map.length; i++) {
 			for (int j = 0; j < map[i].length; j++) {
 				hash += (Math.abs(map[i][j]) + 3 * 9613) * ((31 + i) * 13291) + (j + 27) * 22567;
 			}
 		}
 
 		cachedHash = hash;
 	}
 	
 	protected void
 	generateHashB() {
 		cachedHash = mapB.hashCode();
 	}
 
 	public double
 	getDistance(MapWrapper o) {
 		double dist = 0.0;
 
 		for (int i = 7; i <= 13; i++) {
 			for (int j = 0; j < 22; j++) {
 				switch (map[i][j]) {
 					case (-10):
 						switch (o.map[i][j]) {
 							case (-10):
 								break;
 							case (0):
 								dist += 1;
 								break;
 							case (2):
 								dist += 1;
 								break;
 							case (9):
 								dist += 2;
 								break;
 						}
 						break;
 					case (2):
 						switch (o.map[i][j]) {
 							case (-10):
 								dist += 1;
 								break;
 							case (0):
 								dist += 5;
 								break;
 							case (2):
 								break;
 							case (9):
 								dist += 2;
 								break;
 						}
 						break;
 					case (9):
 						switch (o.map[i][j]) {
 							case (-10):
 								dist += 4;
 								break;
 							case (0):
 								dist += 6;
 								break;
 							case (2):
 								dist += 3;
 								break;
 							case (9):
 								break;
 						}
 						break;
 					case (0):
 						switch (o.map[i][j]) {
 							case (0):
 								break;
 							default:
 								dist += 1;
 						}
 						break;
 					default:
 				}
 			}
 		}
 
 		return dist;
 	}
 }
