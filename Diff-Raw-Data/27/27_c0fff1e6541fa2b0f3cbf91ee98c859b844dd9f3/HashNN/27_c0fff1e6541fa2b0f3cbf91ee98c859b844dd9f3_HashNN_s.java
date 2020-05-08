 /**
  * 
  */
 package graphrep;
 
 import java.util.HashMap;
 
 import com.carrotsearch.hppc.IntArrayList;
 
 /**
  * @author nino
  * 
  */
 public class HashNN implements NNSearcher {
 
 	GraphRep graphRep;
 	HashMap<Long, Object> hashMap;
 	NNSearcher dumpNN = new DumbNN(graphRep);
 
 	public HashNN(GraphRep graphRep) {
 		this.graphRep = graphRep;
 		hashMap = new HashMap<Long, Object>();
 		for (int i = 0; i < graphRep.getNodeCount(); i++) {
			int tempLat = (int) (graphRep.getNodeLat(i) * 10);
			int tempLon = (int) (graphRep.getNodeLon(i) * 10);
 
 			long key = tempLat << 32 | tempLon;
 			IntArrayList tempValues = (IntArrayList) hashMap.get(key);
 			if (tempValues == null) {
 				tempValues = new IntArrayList(10);
 				tempValues.add(i);
 				hashMap.put(key, tempValues);
 			} else {
 				tempValues.add(i);
 			}
 		}
 		for (Long key : hashMap.keySet()) {
 			int[] arr = ((IntArrayList) hashMap.get(key)).toArray();
 			hashMap.put(key, arr);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see graphrep.NNSearcher#getIDForCoordinates(double, double)
 	 */
 	@Override
 	public int getIDForCoordinates(double lat, double lon) {
		int keyLat = (int) (lat * 10);
		int keyLon = (int) (lon * 10);
 		int pos = -1;
 		long key = keyLat << 32 | keyLon;
 		double dist = Long.MAX_VALUE;
 		double tempDist = Long.MAX_VALUE;
 		if (hashMap.containsKey(key)) {
			int[] arr = ((IntArrayList) hashMap.get(key)).toArray();
 			for (int nodeID : arr) {
 				tempDist = Math.sqrt((graphRep.getNodeLat(nodeID) - lat)
 						* (graphRep.getNodeLat(nodeID) - lat)
 						+ (graphRep.getNodeLon(nodeID) - lon)
 						* (graphRep.getNodeLon(nodeID) - lon));
 				if (tempDist < dist) {
 					dist = tempDist;
 					pos = nodeID;
 				}
 
 			}
 		}
 		// test Ring
 		long ringkey = 0;
 		for (int i = -1; i < 2; i++) {
 			for (int j = -1; i < 2; i++) {
 				ringkey = keyLat + i << 32 | keyLon + j;
 				if (hashMap.containsKey(ringkey)) {
					int[] ringArr = ((IntArrayList) hashMap.get(key)).toArray();
 					for (int nodeID : ringArr) {
 						tempDist = Math
 								.sqrt((graphRep.getNodeLat(nodeID) - lat)
 										* (graphRep.getNodeLat(nodeID) - lat)
 										+ (graphRep.getNodeLon(nodeID) - lon)
 										* (graphRep.getNodeLon(nodeID) - lon));
 						if (tempDist < dist) {
 							dist = tempDist;
 							pos = nodeID;
 						}
 
 					}
 				}
 			}
 		}
 
 		if (pos == -1) {
 			pos = dumpNN.getIDForCoordinates(lat, lon);
 		}
 		return pos;
 	}

 }
