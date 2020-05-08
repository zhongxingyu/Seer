 package iballs;
 
 import java.awt.EventQueue;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Main {
 
 	public static final int nRows = 5;
 	public static final int nBits = 15;
 	
 	int[][] indexMatrix = new int[nRows][nRows];
 	List<Integer> testMasks = new LinkedList<Integer>();
 	List<Integer> solutions = new LinkedList<Integer>();
 	
 	int[][][] patterns  = {
 		{{0,0}, {0,1}, {1,1}}, //triangle
 		{{0,0}, {0,1}, {0,2}}, //vertical row
 		{{0,0}, {1,0}, {2,0}}, //horizontal row
 		{{0,0}, {1,1}, {2,2}}, //diagonal			
 	};	
 	
 	private void buildMasks(int[][] p) {			
 		for (int y = 0; y < nRows; ++y) {
 			for (int x = 0; x < nRows; ++x) {			
 				int mask = 0;
 				for (int[] xy : p) {
 					int tx = x + xy[0];
 				    int ty = y + xy[1];				    
 				    if ((tx >= nRows) || (ty >= nRows) || (indexMatrix[tx][ty] == -1)) {   				    						    	
 				    	mask = -1;
 				    	break;
 				    }				    
 				    mask |= (1 << indexMatrix[tx][ty]);
 				}								
 				if (mask != -1) {
 					testMasks.add(mask);
 				}
 			}
 		}
 	}
 	
 	private void printMask(int mask) {
 		for (int i = 0; i < nBits; ++i) {
 			if ((mask & 1) != 0) {
 				System.out.print(String.format("% 3d ", i));				
 			}			
 			mask >>= 1;
 		}
 		System.out.println();
 	}
 	
 	private boolean checkState(int state) {
 		//check solid ball at center
 		if ((state & (1 << 4)) == 0) {
 			return false;
 		}
 		
 		//count number of solid balls, should be 8
 		int cnt = 0;
 		for (int i = 0; i < nBits; ++i) {
 			if ((state & (1 << i)) != 0) {
 				++cnt;
 			}
 		}
 		if (cnt != 8) {
 			return false;
 		}
 		
 		//check against mask set
 		for (Integer m : testMasks) {
 			if (((state & m) == m) || ((~state & m) == m)) {										
 				return false;
 			}
 		}		
 		
 		return true;
 	}
 	
 	public void printSolution(int state) {		
 		for (int r = 0; r < nRows; ++r) {
 			for (int n = 0; n < nRows - r - 1; ++n) {
 				System.out.print(" ");
 			}
 			for (int n = 0; n <= r; ++n) {
 				System.out.print(((state & 1) != 0)?"X ":"O ");
 				state >>= 1;
 			}
 			System.out.println();
 		}
 		System.out.println();
 	}
 	
 	private void buildIndexMatrix() {
 		int num = 0;
 		for (int y = 0; y < nRows; ++y) {
 			for (int x = 0; x < nRows; ++x) {			
 				indexMatrix[x][y] = (x <= y)?num++:-1;
 			}
 		}		
 	}
 	
 	private void printIndexMatrix() {
 		for (int y = 0; y < nRows; ++y) {
 			for (int x = 0; x < nRows; ++x) {
 				System.out.print(String.format("% 3d ", indexMatrix[x][y]));
 			}
 			System.out.println();
 		}	
 	}
 	
 	private void solve() {
 		for (int state = 0; state < 32768; ++state) {
 			if (checkState(state)) {
 				solutions.add(state);								
 			}
 		}		
 	}
 	
 	
 	public void run() {				
 		buildIndexMatrix();
 		
 		System.out.println("index matrix: ");
 		printIndexMatrix();
 		System.out.println();
 		
 		for (int[][] p : patterns) {			
 			buildMasks(p);
 		}
 		
 		System.out.println("check mask list: ");
 		for (Integer mask : testMasks) {			
 			printMask(mask);
 		}
 		System.out.println();
 		
 		solve();
 		
 		System.out.println("solutions: ");
 		for (Integer s : solutions) {
 			printSolution(s);
 		}		
 
 		System.out.println();
 		System.out.println("total count: " + solutions.size());
 
 		View v = new View();
 		v.setSolutions(solutions);
 		v.setVisible(true);		
 	}
 	
 	public static void main(String[] args) {
 		new Main().run();
 	}
 }
