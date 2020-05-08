 public class Percolation {
 
     public int size;
     public int virtualTop;
     public int virtualBottom;
 	public int[] openSites;    
     public WeightedQuickUnionUF grid;
 
     public static void main(String args[]) {
		Percolation myPerc = new Percolation(1);
 		myPerc.open(1,1);
		// myPerc.open(1,2);
		// myPerc.open(2,1);
 		System.out.println(myPerc.isFull(1,1));
		System.out.println(myPerc.percolates());
		// System.out.println(myPerc.isFull(2,1));
     }
     
     // create N-by-N grid, with all sites blocked
     public Percolation(int N) {
       	size = N;
   	  	virtualTop = 0;
     	virtualBottom = 1;
     	openSites = new int[N*(N+1)+1];
     	grid = new WeightedQuickUnionUF(N*(N+1)+1);
 
     	for (int i=0; i < N*(N+1)+1; i++) {
         	openSites[i] = 0;
         }
 
         //connect grid[virtualTop]
         for (int i = N; i < 2*N+1; i++) {
         	grid.union(virtualTop,i);
         }
 
         // connect grid[virtualBottom]
         for (int i = N*N; i < N*N+1; i++) {
         	grid.union(virtualBottom,i);
         }
     }
 
     // 2d to 1d conversion fun
     private int xyTo1D(int x, int y) {
     	int newSize;
     	return newSize = x*size + y;
     }
 
     private void validIndex(int x, int y) {
     	if (x < 1 || x > size || y < 1 || y > size) {
     		throw new IndexOutOfBoundsException();
     	}
     }
 
 	// open site (row i, column j) if it is not already
     public void open(int i, int j) {
 
     	int self = xyTo1D(i,j);
 
    	// What if you are already at the very top of the array?
    	// Then this doesn't make sense.
     	int left = xyTo1D(i-1, j);
     	int right = xyTo1D(i+1, j);
     	int bottom = xyTo1D(i, j-1);
     	int top = xyTo1D(i, j+1);
 
     	int[] neighbors = {left, right, top, bottom};
 
     	validIndex(i, j);
     	openSites[self] = 1;
 
     	//Unnecessary for loop?
     	for (int k=0; k < neighbors.length; k++) {
     		if (openSites[neighbors[k]] == 1) {
     			grid.union(self, neighbors[k]);
     		}
         }
 
     }
 
     // is site (row i, column j) open?
     public boolean isOpen(int i, int j) {
     	validIndex(i, j);
     	if (openSites[xyTo1D(i,j)] == 1) {
     		return true;
     	} else {
     		return false;
     	}
     }
 
     // is site (row i, column j) full?
     public boolean isFull(int i, int j) {
     	validIndex(i, j);
     	int self = xyTo1D(i,j);
 
     	if (isOpen(i,j) && grid.connected(virtualTop, self)) {
     		return true;
     	} else {
 	    	return false;
 	    }
     }
 
 
    public boolean percolates() {
    	if (grid.connected(virtualTop, virtualBottom)) {
    		return true;
    	} else {
    		return false;
    	}
    }      
 }
