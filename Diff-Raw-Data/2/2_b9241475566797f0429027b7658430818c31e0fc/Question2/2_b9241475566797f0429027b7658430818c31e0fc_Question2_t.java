 package ch.wurmlo.week2;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jgrapht.UndirectedGraph;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.*;
 
 public class Question2 {
 
 	@SuppressWarnings("UnusedDeclaration")
 	private static Logger log = LoggerFactory.getLogger(Question2.class);
 
 	public static void main(String[] args) {
 
 		// translating file into list of jobs
         HammingReader reader = null;
 		try {
 			reader = new HammingReader("clustering2_small2.txt");
 		} catch (IOException e) {
 			System.err.println("Could not read file");
 			System.exit(1);
 		}
		UndirectedGraph<LongPoint,Distance> graph = reader.getGraph();
 
 		// sort edges in increasing cost
 //		Set<Distance> distances = graph.edgeSet();
 //		List<Distance> edgeList = new ArrayList<Distance>(distances);
 //		Collections.sort(edgeList, new Comparator<Distance>() {
 //			@Override
 //			public int compare(Distance o1, Distance o2) {
 //				if(o1.getDistance() < o2.getDistance()) {
 //					return -1;
 //				} else if(o1.getDistance() > o2.getDistance()) {
 //					return 1;
 //				} else {
 //					return 0;
 //				}
 //			}
 //		});
 
 		// compute max-spaced 4-clustering
 //		ClusteringUnionFind<Point> unionFind = new ClusteringUnionFind<Point>(graph.vertexSet());
 //		for (Distance distance : edgeList) {
 //			Point source = graph.getEdgeSource(distance);
 //			Point target = graph.getEdgeTarget(distance);
 //			if (!unionFind.find(source).equals(unionFind.find(target))) {
 //				int numberOfClusters = unionFind.numberOfClusters();
 //				if(numberOfClusters <= 4) {
 //					break;
 //				}
 //				unionFind.union(source, target);
 //			}
 //		}
 
 		// get maximum spacing
 //		List<Integer> crossingDistanceInts = new ArrayList<Integer>();
 //		for (Point pointA : graph.vertexSet()) {
 //			for (Point pointB : graph.vertexSet()) {
 //				if(!unionFind.find(pointA).equals(unionFind.find(pointB))) {
 //					Distance distance = graph.getEdge(pointA, pointB);
 //					if(distance != null) {
 //						crossingDistanceInts.add(distance.getDistance());
 //					}
 //				}
 //
 //			}
 //		}
 
 		// answer to question
 //		log.info("Collections.min(crossingDistanceInts) == {}", Collections.min(crossingDistanceInts));
 //
 //		// log all clusters
 //		Set<Point> clusters = unionFind.getClusters();
 //		for (Point cluster : clusters) {
 //			List<Point> nodesForCluster = unionFind.getNodesForCluster(cluster);
 //			log.info("In cluster {}, nodes = {}", cluster, StringUtils.join(nodesForCluster, ","));
 //		}
 
 	}
 
 }
