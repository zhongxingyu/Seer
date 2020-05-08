 /**
  * 
  */
 package test;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Random;
 
 import utils.dataStructures.trees.thirdGenKD.KdTree;
 import utils.dataStructures.trees.thirdGenKD.NearestNeighborIterator;
 import utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
 
 /**
  * @author dutech
  *
  */
 public class TestKdTree {
 	
 	static DecimalFormat df3_2 = new DecimalFormat( "0.00" );
 	Random rnd = new Random();
 
 	/**
 	 * Creation and all the tests
 	 */
 	public TestKdTree() {
 		System.out.println("***** TestKdTree *****");
 
 	}
 		
 	public void run() {	
 		boolean res;
 		int nbTest = 0;
 		int nbPassed = 0;
 		
 
 			nbTest ++;
 			res = testCreateAdd();
 			if (res) {
 				System.out.println("testCreateAdd >> "+res);
 				nbPassed ++;
 			}
 			else {
 				System.err.println("testCreateAdd >> "+res);
 			}
 			nbTest ++;
 			res = testNeighbors();
 			if (res) {
 				System.out.println("testNeighbors >> "+res);
 				nbPassed ++;
 			}
 			else {
 				System.err.println("testNeighbors >> "+res);
 			}
 
 			if (nbTest > nbPassed) {
 				System.err.println("FAILURE : only "+nbPassed+" success out of "+nbTest);
 				System.exit(1);
 			}
 			else {
 				System.out.println("SUCCESS : "+nbPassed+" success out of "+nbTest);
 				System.exit(0);
 			}
 		
 	}
 	/**
 	 * Create points are get the 20 closest to 'query' by SORTING according
 	 * to their distance to query or by using a KdTree.
 	 * @return
 	 */
 	boolean testNeighbors() {
 		// A list of points
 		double[] query = {0.5, 0.5};
 		
 		ArrayList<double[]> lpt = new ArrayList<double[]>();
 		for (int i = 0; i < 100000; i++) {
 			lpt.add(rndPoint(2));
 		}
 		
 		// Add the list to KdTree
 		KdTree<double[]> kdtree = new KdTree<double[]>(2, 5);
 		for (double[] dx : lpt) {
 			kdtree.addPoint(dx, dx);
 		}
 		
 		// Sort the list according to the distance to query
 		long m_starttime = System.currentTimeMillis();
 		Collections.sort(lpt, new Comparator<double[]>() {
 			double[] query = {0.5, 0.5};
 		    public int compare(double[] o1, double[] o2) {
 		    	double dist1 = (o1[0]-query[0])*(o1[0]-query[0])+(o1[1]-query[1])*(o1[1]-query[1]);
 		    	double dist2 = (o2[0]-query[0])*(o2[0]-query[0])+(o2[1]-query[1])*(o2[1]-query[1]);
 		        return (int) Math.round(Math.signum((dist1 - dist2)));
 		    }});
 		List<double[]> sublist = lpt.subList(0, 20);
 		// OUTPUT from sort
 		String strLpt = "";
 		for (int i = 0; i < 20; i++) {
 			double[] dx = sublist.get(i);
 			strLpt += "("+df3_2.format(dx[0])+", "+df3_2.format(dx[1])+") ";
 		}
 		System.out.println("SORT = "+strLpt);
 		long m_curtime = System.currentTimeMillis();
 		System.out.println("Sorting out took t="+(m_curtime-m_starttime));
 		m_starttime = m_curtime;
 		
 		// Using the kdtree, retrieve closest 'data' of points close to query
 		// (here the trick is that data=key in the kdtree)
 		SquareEuclideanDistanceFunction distF = new SquareEuclideanDistanceFunction();
 		String strTree = "";
 		for (NearestNeighborIterator<double[]> it = kdtree.getNearestNeighborIterator(query, 20, distF); it.hasNext();) {
 			double[] dx = (double[]) it.next();
 			strTree += "("+df3_2.format(dx[0])+", "+df3_2.format(dx[1])+") ";
 			boolean rm = sublist.remove(dx);
 			if (rm == false) {
 				System.err.println("testNeighbors kdtree nearest = "+"("+df3_2.format(dx[0])+", "+df3_2.format(dx[1])+")");
 				System.err.println("              NOT in SORT !!");
 				return false;
 			}
 		}
 		System.out.println("TREE = "+strTree);
 		m_curtime = System.currentTimeMillis();
 		System.out.println("KdTree took t="+(m_curtime-m_starttime));
 		//MaxHeap<double[]> near = kdtree.findNearestNeighbors(query, 20, distF);
 		
 		if (sublist.isEmpty()) {
 			System.out.println("testNeighbors : ALL nearest found");
 			return true;
 		}
 		else {
 			System.err.println("testNeighbors  : SOME neartest NOT FOUND");
 			
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Create Points, dump them on screen and on files so as to
 	 * be plotted by R using plotKdTree.
 	 */
 	boolean testCreateAdd() {
 		// KdTree avec des points en 2 dimensions
		KdTree<Double> kdtree = new KdTree<Double>(2, 7);
 		System.out.println("-------------");
 		kdtree.dumpDisplay("");
 		
 		for (int i = 0; i < 30; i++) {
 			kdtree.addPoint(rndPoint(2), rnd.nextGaussian());
 		}
 		System.out.println("-------------");
 		kdtree.dumpDisplay("");
 		
 		kdtree.addPoint(rndPoint(2), rnd.nextGaussian());
 		kdtree.addPoint(rndPoint(2), rnd.nextGaussian());
 		System.out.println("-------------");
 		kdtree.dumpDisplay("");
 		
 		try {
 			kdtree.dumpFile("src/test/kdtree");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 		
 	}
 	// Generate random point of dimension 'dim'.
 	double[] rndPoint(int dim) {
 		double[] pt = new double[dim];
 		for (int i = 0; i < pt.length; i++) {
 			pt[i] = rnd.nextDouble();
 		}
 		return pt;
 	}
 	
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		TestKdTree app = new TestKdTree();
 		app.run();
 
 	}
 
 }
