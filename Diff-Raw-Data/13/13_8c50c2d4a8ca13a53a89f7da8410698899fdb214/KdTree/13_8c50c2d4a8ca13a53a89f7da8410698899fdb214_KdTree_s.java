 
 
 import Jcg.Fenetre;
 import Jcg.geometry.*;
 import Jcg.viewer.MeshViewer;
 
 import java.util.*;;
 
 /**
  * A class implementing a Kd-tree data structure
  * Exercice 1
  */
 public class KdTree implements NearestNeighborSearch {
 
 	private int 		pointDimension, cutDimension;
 	private double 		cutValue;
 	private int 		numPoints;
 	private PointCloud 	points;
 	private KdTree 		lowerHalf, upperHalf;
 
 
 
 	//--------------------------------------------
 	//--- Computing the cut value ----------------
 	//--- Ex 1.1a: median in linear time
 	//--- Ex 1.1b: median with sorting (nlog n time)
 	//--- Ex 1.1c: use the mean (instead of median)
 	//--------------------------------------------
 
 
 
 	/**
 	 * Select the i-th element in time O(nlog n) (after sorting)
 	 */    
 	private static Point_D selectWithSorting (Point_D[] buffer, int index, int cutDim){
 		Arrays.sort(buffer, new CompareCoordinate(cutDim));
 		return buffer[index];
 	}
 
 	/**
 	 * Compute the median of a set of point cloud in time O(nlog n) (after sorting)
 	 */    
 	private static Point_D findMedianWithSorting (PointCloud N, int cutDim){
 		Point_D[] pts = N.toArray();
 		Arrays.sort(pts, new CompareCoordinate(cutDim));
 		return pts[pts.length/2];
 	}
 
 
 	/**
 	 * Compute the median of a set of points (just for small arrays)
 	 */    
 	private static Point_D findMedianArray (Point_D[] buffer, int max, int cutDim) {
 		throw new Error("Exercice 1.1a: a' completer");
 	}
 
 
 	/**
 	 * Compute the median of a set of points (linear median)
 	 */    
 	private static Point_D findMedianLinear (PointCloud N, int cutDim) {
 		throw new Error("Exercice 1.1a: a' completer");
 	}
 
 	/**
 	 * Split a point cloud into two sub-point clouds (lower an upper point clouds)
 	 */    
 	public static PointCloud[] split(PointCloud N, double cutValue, int cutDim) {
 		PointCloud[] subN = new PointCloud[2]; 
 		subN[0] = null; subN[1] = null;
 		for (PointCloud n = N; n != null; n = n.next) {
 			if (n.p.Cartesian(cutDim) < cutValue) 
 				subN[0] = new PointCloud(n.p, subN[0], false);
 			else 
 				subN[1] = new PointCloud(n.p, subN[1], false);
 		}
 		return subN;
 	}
 
 
 	//-------------------------------    
 	//--- Constructing the Kd-Tree --
 	//-------------------------------
 
 	/**
 	 * Construct a Kd-Tree from a point cloud (in dimension pDim)
 	 */    
 	public static KdTree constructDataStructure(PointCloud N, int pDim) {
 		return new KdTree(N, pDim, 0);
 	}
 
 	/**
 	 * Constructor -- builds entire kd-tree at once recursively
 	 */    
 	public KdTree (PointCloud N, int pDim, int cutDim) {
 //		private int 		numPoints;
 		this.points         = N;
 		this.cutDimension   = cutDim;
 		this.pointDimension = pDim;
 		this.cutValue = findMedianWithSorting(N, cutDim).Cartesian(cutDim);
 		PointCloud[] subN = split(N,cutValue,cutDim);
 		cutDim = (cutDim+1)%pDim;
 		if (subN[0] == null) {  // No lower point
 			this.lowerHalf = null;
 			this.upperHalf = null;
 		} else {
 			this.lowerHalf = new KdTree(subN[0], pDim, cutDim);
 			this.upperHalf = new KdTree(subN[1], pDim, cutDim);
 		}
 		this.numPoints = (this.lowerHalf == null) ? 1 : (this.lowerHalf.numPoints + this.upperHalf.numPoints);
 	}
 
 
 
 	//-----------------------------------
 	//--- Implementation of NN search ---
 	//-----------------------------------
 
 
 
 	/**
 	 * Range search: return the list of nearest point to a given query point q.
 	 * The output is the set of points at distance at most sqRad from q.
 	 */    
 	public PointCloud NearestNeighbor (Point_D q, double sqRad) {
 		throw new Error("Exercice 1.3: a' completer");
 
 	}
 
 
 
 	//-------------------------------    
 	//------ Testing  Kd-Trees ------
 	//-------------------------------
 
 	public static void testMedianComputing(PointCloud N) {
		System.out.println("exercice 1.1: computing the median (or the mean)");
 		if(N==null) return;
 		int cutDim=1;
 
 		Point_D p;
 		p=findMedianLinear(N, cutDim);
 		//p=findMedianWithSorting(N, cutDim);
 		//p=Point_D.mean(N);
 		double value=p.Cartesian(cutDim);
 		PointCloud[] subN=split(N, value, cutDim);
 
 		System.out.println("size original point cloud: "+PointCloud.size(N));
 		System.out.println("size lower half: "+PointCloud.size(subN[0]));
 
 		Draw.draw2D(subN[0], "lower points");
 		Draw.draw2D(subN[1], "upper points");
 	}
 
 	public static void testConstructionKdTree(PointCloud N) {
 		System.out.println("exercice 1.2: constructing Kd-Tree");
 		if(N==null) return;
 
 		KdTree kdtree=KdTree.constructDataStructure(N, N.p.dimension());
 
 		System.out.println("size original point cloud: "+PointCloud.size(N));
 		System.out.println("size Kd-Tree (total number of nodes): "+KdTree.size(kdtree));
 		System.out.println("number of leaves in the tree: "+leavesNumber(kdtree));
 		KdTree.checkBalance(kdtree);
 
 	}
 
 	public static void main(String[ ] args) throws Exception {
 		System.out.println("Exercice 1: testing Kd-trees");
 		PointCloud N;
 
 		if (args.length < 1) {
 			N=PointCloud.randomPoints(3000, 3);
 		}
 		else { 
 			N=Clustering.readFile(args[0]);
 		}
 		Draw.draw2D(N, "original point cloud");
 		Draw.draw3D(N);
 
 //		testMedianComputing(N);
 		testConstructionKdTree(N);
 
 		System.out.println("end");
 	}
 
 
 	//-----------------------------------
 	//--- Methods on binary trees ---
 	//-----------------------------------
 
 
 
 	static public int size(KdTree t) {
 		int result=0;
 		if(t==null) return 0;
 		else {
 			result=size(t.lowerHalf);
 			result=result+size(t.upperHalf);
 		}
 		return result+1;
 	}
 
 	static public boolean checkBalance(KdTree t) {
 		if (t.lowerHalf==null && t.upperHalf==null)
 			return true;
 		if (t.numPoints < 200) 
 			return true;
 
 		boolean result=true;
 		if ((double)t.lowerHalf.numPoints/(double)t.numPoints < 0.25) {
 			System.out.println("Bad balance in kd-tree! (" + 
 					t.lowerHalf.numPoints + "/" + t.numPoints +
 					"=" + (double)t.lowerHalf.numPoints/(double)t.numPoints + ")");
 			result=false;
 		}
 		if ((double)t.lowerHalf.numPoints/(double)t.numPoints > 0.75) {
 			System.out.println("Bad balance in kd-tree! (" + 
 					t.lowerHalf.numPoints + "/" + t.numPoints +
 					"=" + (double)t.lowerHalf.numPoints/(double)t.numPoints + ")");
 			result=false;
 		}
 		result=result && checkBalance(t.lowerHalf);
 		result=result && checkBalance(t.upperHalf);
 
 		return result;
 	}
 
 	static public int leavesNumber(KdTree t) {
 		int result=0;
 		if(t==null) return 0;
 		if(t.upperHalf==null && t.lowerHalf==null)
 			return 1;
 		else {
 			result=leavesNumber(t.lowerHalf);
 			result=result+leavesNumber(t.upperHalf);
 		}
 		return result;
 	}
 
 	static public String toString(KdTree t) {
 		String result="";
 		if(t==null) return result;
 		if(t.upperHalf==null && t.lowerHalf==null)
 			return ""+t.points+"\n";
 		else {
 			result=toString(t.lowerHalf);
 			result=result+toString(t.upperHalf);
 		}
 		return result;
 	}
 
 	static public ArrayList<Point_D> toList(KdTree t, ArrayList<Point_D> list) {
 		if(t==null) return list;
 		if(t.upperHalf==null && t.lowerHalf==null)
 			list.add(t.points.p);
 		else {
 			toList(t.lowerHalf, list);
 			toList(t.upperHalf, list);
 		}
 		return list;
 	}
 
 }
