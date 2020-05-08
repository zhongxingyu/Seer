 package graphrep;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Serializable;
 
 /**
  * Class Graphrep
  */
 public class Graphrep implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private final NNSearcher searcher;
 
 	private final int nodeCount;
 	private final int edgeCount;
 
 	// note: edgeNum often refers to the relative number of an outgoing edge
 	// from a node:
 	// (first outgoing edge = 0, second outgoing edge = 1, ...)
 
 	// (deprecated) File Format:
 	// TODO: Ask Frederic for updated description (#7)
 	// #N -> Number of Nodes
 	// #X -> Number of Edges:
 	// #N * (ID oldOsmID lat lon height)
 	// #M * (source destination multiplier distance elevationDelta)
 
 	// new fileformat is probably:
 	// nodecount
 	// edgecount
 	// (nodecount *) ID lat lon height
 	// (edgecount *) source dest dist mult
 
 	// TODO: at the moment a simplistic solution for the inedges: create the
 	// file with inedges with this script (assuming it is in new file format and
 	// has no leading and no trailing newline):
 	//
 	// #!/bin/bash
 	// INFILE="$1"
 	// [[ -z $INFILE ]] && echo -e
 	// "USAGE:\n\t$0 FILENAME\n\twill create FILENAME_inedges.txt" && exit 1
 	// OUTFILE="$1_inedges.txt"
 	// EDGENUM=$(head -2 ${INFILE} | tail -1)
 	// tail -$EDGENUM ${INFILE} | sort -b -k2n,2 > ${OUTFILE}
 	// exit 0
 
 	// nodes
 	// why no osm id??
 	// private final int[] osmIDs;
 	protected final float[] lat;
 	protected final float[] lon;
 	private final int[] height;
 
 	// edges
 	private final int[] source_out;
 	private final int[] dest_out;
 	private final float[] mult_out;
 	private final int[] dist_out;
 	// private final float[] elev;
 
 	final int[] offsetOut;
 
 	private final int[] source_in;
 	private final int[] dest_in;
 	private final float[] mult_in;
 	private final int[] dist_in;
 
 	final int[] offsetIn;
 
 	/**
 	 * Constructor loads graph from disk
 	 * 
 	 * @throws IOException
 	 * 
 	 */
 	public Graphrep() throws IOException {
 
 		// TODO: get filename from config and replace the following
 		String filename = System.getProperty("user.home") + "/germany.txt";
 
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(filename));
 
 		} catch (FileNotFoundException e) {
 			// TODO: what happens here?
 			e.printStackTrace();
 		}
 
 		String line;
 
 		// exception should happen when file format is wrong
 		line = in.readLine();
 
 		while (line != null && line.trim().startsWith("#")) {
 			line = in.readLine();
 		}
 		if (line != null) {
 			nodeCount = Integer.parseInt(line);
 		} else {
 			nodeCount = 0;
 		}
 
 		line = in.readLine();
 		if (line != null) {
 			edgeCount = Integer.parseInt(line);
 		} else {
 			edgeCount = 0;
 		}
 
 		// TODO osmIDs = new int[nodeCount];
 		lat = new float[nodeCount];
 		lon = new float[nodeCount];
 		height = new int[nodeCount];
 
 		offsetOut = new int[nodeCount + 1];
 
 		source_out = new int[edgeCount];
 		dest_out = new int[edgeCount];
 		mult_out = new float[edgeCount];
 		// TODO elev = new float[edgeCount];
 		dist_out = new int[edgeCount];
 
 		// used for splitted lines in 1. nodes 2. edges
 		String[] splittedLine;
 		System.out.println("Reading " + nodeCount + " nodes and " + edgeCount
 				+ " edges...");
 		for (int i = 0; i < nodeCount; i++) {
 			splittedLine = in.readLine().split(" ");
 			// TODO osmIDs[i] = Integer.parseInt(linesplitted[1]);
 			lat[i] = Float.parseFloat(splittedLine[1]);
 			lon[i] = Float.parseFloat(splittedLine[2]);
 			height[i] = Integer.parseInt(splittedLine[3]);
 		}
 
 		System.out.println("successfully read nodes");
 		int currentSource;
 		int prevSource = -1;
 		for (int i = 0; i < edgeCount; i++) {
 			splittedLine = in.readLine().split(" ");
 			currentSource = Integer.parseInt(splittedLine[0]);
 			source_out[i] = currentSource;
 			dest_out[i] = Integer.parseInt(splittedLine[1]);
 			dist_out[i] = Integer.parseInt(splittedLine[2]);
 			mult_out[i] = Float.parseFloat(splittedLine[3]);
 			// TODO mult[i] = Integer.parseInt(splittedLine[4]);
 
 			if (currentSource != prevSource) {
 				for (int j = currentSource; j > prevSource; j--) {
 					offsetOut[j] = i;
 				}
 				prevSource = currentSource;
 			}
 		}
 		in.close();
 		offsetOut[nodeCount] = edgeCount;
 		// assuming we have at least one edge
 		for (int cnt = nodeCount - 1; offsetOut[cnt] == 0; cnt--) {
 			offsetOut[cnt] = offsetOut[cnt + 1];
 		}
 
 		System.out.println("successfully read outedges");
 
 		// //// inedges
 		filename = System.getProperty("user.home") + "/germany.txt_inedges.txt";
 		try {
 			in = new BufferedReader(new FileReader(filename));
 
 		} catch (FileNotFoundException e) {
 			// TODO: what happens here?
 			e.printStackTrace();
 		}
 		offsetIn = new int[nodeCount + 1];
 
 		source_in = new int[edgeCount];
 		dest_in = new int[edgeCount];
 		mult_in = new float[edgeCount];
 		// TODO elev = new float[edgeCount];
 		dist_in = new int[edgeCount];
 		// shamelessly reuse variables
 		int prevDest = -1;
 		int currentDest;
 		for (int i = 0; i < edgeCount; i++) {
 			splittedLine = in.readLine().split(" ");
			currentDest = Integer.parseInt(splittedLine[1]);
			source_in[i] = Integer.parseInt(splittedLine[0]);
			dest_in[i] = currentDest;
 			dist_in[i] = Integer.parseInt(splittedLine[2]);
 			mult_in[i] = Float.parseFloat(splittedLine[3]);
 			// TODO mult[i] = Integer.parseInt(splittedLine[4]);
 			if (currentDest != prevDest) {
				for (int j = currentDest; j > prevDest; j--) {
 					offsetIn[j] = i;
 				}
				prevDest = currentDest;
 			}
 		}
 		in.close();
 		offsetIn[nodeCount] = edgeCount;
 		// assuming we have at least one edge
 		for (int cnt = nodeCount - 1; offsetIn[cnt] == 0; cnt--) {
 			offsetIn[cnt] = offsetIn[cnt + 1];
 		}
 		System.out.println("successfully read inedges");
 
 		// choose the NNSearcher here
 		// DumbNN uses linear search and is slow.
 		// KDTreeNN should be faster
 		searcher = new DumbNN(this);
 	}
 
 	/**
 	 * @param lat
 	 * @param lon
 	 * @return
 	 */
 	public final int getIDForCoordinates(float lat, float lon) {
 		return searcher.getIDForCoordinates(lat, lon);
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 */
 	public final float getNodeLat(int nodeId) {
 		return lat[nodeId];
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 */
 	public final float getNodeLon(int nodeId) {
 		return lon[nodeId];
 	}
 
 	/**
 	 * @return long
 	 * @param nodeId
 	 */
 	public final long getNodeOsmId(int nodeId) {
 		// TODO: is not in the graph txt
 		return -1;
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 */
 	public final float getNodeHeight(int nodeId) {
 		return height[nodeId];
 	}
 
 	/**
 	 * @param nodeId
 	 */
 	public final int getOutEdgeCount(int nodeId) {
 		return (offsetOut[nodeId + 1] - offsetOut[nodeId]);
 	}
 
 	/**
 	 * @return int
 	 * @param nodeId
 	 */
 	public final int getInEdgeCount(int nodeId) {
 		return offsetIn[nodeId + 1] - offsetIn[nodeId];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final int getOutTarget(int nodeId, int edgeNum) {
 		return dest_out[offsetOut[nodeId] + edgeNum];
 	}
 
 	/**
 	 * @return int
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final int getInSource(int nodeId, int edgeNum) {
 		return source_in[offsetIn[nodeId] + edgeNum];
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final int getOutDist(int nodeId, int edgeNum) {
 		return dist_out[offsetOut[nodeId] + edgeNum];
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final float getOutMult(int nodeId, int edgeNum) {
 		return mult_out[offsetOut[nodeId] + edgeNum];
 	}
 
 	/**
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final float getOutEleDelta(int nodeId, int edgeNum) {
 		// TODO
 		return -1;
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final int getInDist(int nodeId, int edgeNum) {
 		return dist_in[offsetIn[nodeId] + edgeNum];
 	}
 
 	/**
 	 * @return float
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final float getInMult(int nodeId, int edgeNum) {
 		return mult_in[offsetIn[nodeId] + edgeNum];
 	}
 
 	/**
 	 * @param nodeId
 	 * @param edgeNum
 	 */
 	public final int getInEleDelta(int nodeId, int edgeNum) {
 		// TODO
 		return -1;
 	}
 
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
 
 }
