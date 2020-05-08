 /**
  * @author ardobryn
  * @file   Percolation.java
  */
 
 
 /**
  * Weighted quick-union implementation for percolation problem.
  */
 public class Percolation {
 
 	private int gridSize;
 	private boolean[] siteStates;
 	private WeightedQuickUnionUF quickUf;
 
 	public Percolation(int n) {
 		gridSize = n;
 		// +2 top and bottom virtual sites
 		quickUf = new WeightedQuickUnionUF(n*n + 2);
 		siteStates = new boolean[n*n + 2];
 		for (int i = 1; i < (n*n + 1); i++) {
 			siteStates[i] = false;
 		}
 		// open top and bottom virtual sites
 		siteStates[0] = true;
 		siteStates[n*n + 1] = true;
 	}
 	
 	public void open(int i, int j) {
 		checkValid(i, j);
 		int thisSite = flatIndex(i, j);
 		siteStates[thisSite] = true;
 
 		int neighbours[] = new int[]{flatIndex(i-1, j), flatIndex(i, j+1),
 									 flatIndex(i+1, j), flatIndex(i, j-1)};
 		for (int index : neighbours) {
 			if ((index != -1) && (siteStates[index] == true)) {
 				quickUf.union(thisSite, index);
 			}
 		}
 	}
 	
 	public boolean isOpen(int i, int j) {
 		checkValid(i, j);
 		return siteStates[flatIndex(i, j)];
 	}
 
 	public boolean isFull(int i, int j) {
 		checkValid(i, j);
		return quickUf.connected(flatIndex(i, j), 0);
 	}
 
 	public boolean percolates() {
 		return quickUf.connected(0, gridSize*gridSize + 1);
 	}
 
 	private int flatIndex(int i, int j) {
 		if (i == 0) {
 			return 0;
 		} else if (i == (gridSize + 1)) {
 			return gridSize * gridSize + 1;
 		} else if ((i > 0) && (i <= gridSize) && (j > 0) && (j <= gridSize)) {
 			return (i - 1) * gridSize + j;
 		} else {
 			return -1;
 		}
 	}
 	
 	private void checkValid(int i, int j) {
 		if ((i < 1) || (i > gridSize)) throw new IndexOutOfBoundsException("Row index is out of bounds");
 		if ((j < 1) || (j > gridSize)) throw new IndexOutOfBoundsException("Column index is out of bounds");
 	}
 }
