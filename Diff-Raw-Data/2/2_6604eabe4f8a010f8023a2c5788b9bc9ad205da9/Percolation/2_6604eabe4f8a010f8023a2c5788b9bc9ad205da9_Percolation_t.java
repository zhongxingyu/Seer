 public class Percolation {
 	private WeightedQuickUnionUF uf;
 	private int n;
 	private int headIndex;
 	private int tailIndex;
 	private boolean[] open;
 	// create N-by-N grid, with all sites blocked
 	public Percolation(int N) {
 		n = N;
 		int nn = n*n;
 		headIndex = nn;
 		tailIndex = nn + 1;
 		uf = new WeightedQuickUnionUF(nn +2);
 		open = new boolean[nn];
 		
 		for(int i = 0; i < n ; i++){
 			uf.union(i, headIndex);
 		}
 		
 		for(int i = n * (n-1); i < nn ; i++){
 			uf.union(i, tailIndex);
 		}
 	}
 
 	private void unionNeighbor(int ij, int i2, int j2){
		if(softValidate(i2) && softValidate(j2)){
 			int ij2 = getArrayIndex(i2, j2);
 			if(open[ij2]){
 				uf.union(ij, ij2);
 			}
 		}
 	}
 	// open site (row i, column j) if it is not already
 	// i and j starts from 1
 	public void open(int i, int j) {
 		hardValidate(i);
 		hardValidate(j);
 		int ij = getArrayIndex(i, j);
 		open[ij] = true;
 		//up
 		unionNeighbor(ij, i-1, j);
 		//right
 		unionNeighbor(ij, i, j+1);
 		//down
 		unionNeighbor(ij, i+1, j);
 		//left
 		unionNeighbor(ij, i, j-1);
 	}
 
 	// is site (row i, column j) open?
 	public boolean isOpen(int i, int j) {
 		hardValidate(i);
 		hardValidate(j);
 		int ij = getArrayIndex(i, j);
 		return open[ij];
 	}
 
 	// is site (row i, column j) full?
 	public boolean isFull(int i, int j) {
 		return (isOpen(i, j) && uf.connected(getArrayIndex(i, j), tailIndex) && uf.connected(getArrayIndex(i, j), headIndex));
 	}
 
 	// does the system percolate?
 	public boolean percolates() {
 		return uf.connected(headIndex, tailIndex);
 	}
 	
 	private int getArrayIndex(int i, int j){
 		hardValidate(i);
 		hardValidate(j);
 		return (i-1)*n + j-1;
 	}
 	
 	private void hardValidate(int i){
 		if(i <= 0 || i > n){
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	private boolean softValidate(int i){
 		if(i <= 0 || i > n){
 			return false;
 		}else{
 			return true;
 		}
 	}
 }
