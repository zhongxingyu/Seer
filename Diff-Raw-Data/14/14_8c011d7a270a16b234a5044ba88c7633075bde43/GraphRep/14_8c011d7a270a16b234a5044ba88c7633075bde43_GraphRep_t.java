 package graphrep;
 
 import java.io.Serializable;
 
 /**
  * Class GraphRep
  */
 public class GraphRep implements Serializable {
 	private static final long serialVersionUID = 5L;
 
 	protected NNSearcher searcher;
 
 	protected int nodeCount;
 	protected int edgeCount;
 
 	// note: edgeNum refers to the relative number of an outgoing edge
 	// from a node:
 	// (first outgoing edge = 0, second outgoing edge = 1, ...)
 
 	// nodes
 	protected double[] lat;
 	protected double[] lon;
 	protected int[] height;
 	protected int[] rank;
 
 	// edges
 	protected int[] src;
 	protected int[] dest;
 	protected int[] multipliedDist;
 	protected int[] dist;
 	// protected double[] elev_out;
 	protected int[] shortedID;
 	protected int[] outEdgeSourceNum;
 	protected int[] outEdgeShortedNum;
 
 	protected int[] offsetOut;
 	protected int[] offsetIn;
 
 	/**
 	 * Index = "virtual" id for in edges
 	 * 
 	 * content = position in the edge array
 	 */
 	protected int[] mapping_InToOut;
 
 	// protected int[] src_in;
 	// protected int[] dest_in;
 	// protected int[] multipliedDist_in;
 	// protected int[] dist_in;
 	// // protected double[] elev_in;
 	// protected int[] shortedID_in;
 	// protected int[] outEdgeSourceNum_in;
 	// protected int[] outEdgeShortedNum_in;
 
 	/**
 	 * A graphrep is the representation of a graph used to perform several
 	 * algorithms on. All its protected fields must be be set from the outside
 	 * (which is not nice)
 	 */
 	public GraphRep() {
 	}
 
 	/**
 	 * @param srclat
 	 * @param srclon
 	 * @return
 	 */
 	public final int getIDForCoordinates(double srclat, double srclon) {
 		return searcher.getIDForCoordinates(srclat, srclon);
 	}
 
 	/**
 	 * @return float
 	 * @param nodeID
 	 */
 	public final double getNodeLat(int nodeID) {
 		return lat[nodeID];
 	}
 
 	/**
 	 * @return float
 	 * @param nodeID
 	 */
 	public final double getNodeLon(int nodeID) {
 		return lon[nodeID];
 	}
 
 	/**
 	 * @return float
 	 * @param nodeID
 	 */
 	public final float getNodeHeight(int nodeID) {
 		return height[nodeID];
 	}
 
 	/**
 	 * @param nodeID
 	 */
 	public final int getOutEdgeCount(int nodeID) {
 		return offsetOut[nodeID + 1] - offsetOut[nodeID];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 */
 	public final int getInEdgeCount(int nodeID) {
 		return offsetIn[nodeID + 1] - offsetIn[nodeID];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 * @param edgeNum
 	 */
 	public final int getOutTarget(int nodeID, int edgeNum) {
 		return dest[offsetOut[nodeID] + edgeNum];
 	}
 
 	/**
 	 * @return int
 	 * @param edgeID
 	 */
 	public final int getSource(int edgeID) {
 		return src[edgeID];
 	}
 
 	/**
 	 * @return int
 	 * @param edgeID
 	 */
 	public final int getTarget(int edgeID) {
 		return dest[edgeID];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 */
 	public final int getInSource(int nodeID, int edgeNum) {
 		// return src_in[offsetIn[nodeID] + edgeNum];
 		return src[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 * @param edgeNum
 	 */
 	public final int getOutDist(int nodeID, int edgeNum) {
 		return dist[offsetOut[nodeID] + edgeNum];
 	}
 
 	/**
 	 * @return int
 	 * @param edgeID
 	 * @param edgeNum
 	 */
 	public final int getDist(int edgeID) {
 		return dist[edgeID];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 * @param edgeNum
 	 */
 	public final int getOutMultipliedDist(int nodeID, int edgeNum) {
 		return multipliedDist[offsetOut[nodeID] + edgeNum];
 	}
 
 	// /**
 	// * @param nodeID
 	// * @param edgeNum
 	// */
 	// public final float getOutEleDelta(int nodeID, int edgeNum) {
 	// // TODO
 	// return -1;
 	// }
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 * @param edgeNum
 	 */
 	public final int getInDist(int nodeID, int edgeNum) {
 		// return dist_in[offsetIn[nodeID] + edgeNum];
 		return dist[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeID
 	 * @param edgeNum
 	 */
 	public final int getInMultipliedDist(int nodeID, int edgeNum) {
 		// return multipliedDist_in[offsetIn[nodeID] + edgeNum];
 		return multipliedDist[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
 	}
 
 	/**
 	 * @return int
 	 * @param edgeID
 	 */
 	public final int getInMultipliedDist(int edgeID) {
 		return multipliedDist[edgeID];
 	}
 
 	// /**
 	// * @param nodeID
 	// * @param edgeNum
 	// */
 	// public final float getInEleDelta(int nodeID, int edgeNum) {
 	// // TODO
 	// return -1;
 	// }
 
 	/**
 	 * @return int
 	 */
 	public final int getNodeCount() {
 		return nodeCount;
 	}
 
 	/**
 	 * @return int
 	 */
 	public final int getEdgeCount() {
 		return edgeCount;
 	}
 
 	/**
 	 * @param nodeID
 	 * @return
 	 */
 	public int getRank(int nodeID) {
 		return rank[nodeID];
 	}
 
 	/**
 	 * @param nodeID
 	 * @param edgeNum
 	 * @return
 	 */
 	public int getShortedID_out(int nodeID, int edgeNum) {
 		return shortedID[offsetOut[nodeID] + edgeNum];
 	}
 
 	/**
 	 * @param nodeID
 	 * @param edgeNum
 	 * @return
 	 */
 	public int getOutEdgeSourceNum_out(int nodeID, int edgeNum) {
 		return outEdgeSourceNum[offsetOut[nodeID] + edgeNum];
 	}
 
 	/**
 	 * @param nodeID
 	 * @param edgeNum
 	 * @return
 	 */
 	public int getOutEdgeShortedNum_out(int nodeID, int edgeNum) {
 		return outEdgeShortedNum[offsetOut[nodeID] + edgeNum];
 	}
 
 	/**
 	 * @param nodeID
 	 * @param edgeNum
 	 * @return
 	 */
 	public int getShortedID_in(int nodeID, int edgeNum) {
 		// return shortedID_in[offsetIn[nodeID] + edgeNum];
 		return shortedID[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
 	}
 
 	/**
 	 * @param nodeID
 	 * @param edgeNum
 	 * @return
 	 */
 	public int getOutEdgeSourceNum_in(int nodeID, int edgeNum) {
 		// return outEdgeSourceNum_in[offsetIn[nodeID] + edgeNum];
 		return outEdgeSourceNum[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
 	}
 
 	/**
 	 * @param nodeID
 	 * @param edgeNum
 	 * @return
 	 */
 	public int getOutEdgeShortedNum_in(int nodeID, int edgeNum) {
 		// return outEdgeShortedNum_in[offsetIn[nodeID] + edgeNum];
 		return outEdgeShortedNum[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
 	}
 
 	public int getOutEdgeID(int nodeID, int edgeNum) {
		return offsetOut[nodeID] + edgeNum;
 	}
 
 	public int getInEdgeID(int nodeID, int edgeNum) {
		return mapping_InToOut[offsetIn[nodeID] + edgeNum];
 	}
 
 }
