 
 public class Percolation {
 
 	private boolean[][] opened;
 	private WeightedQuickUnionUF wqUF;
 	private int vTop = 0; //virtual top site, its value indicates its root
 	private int vButtom;  //virtual bottom site
 	private int N;
 	
 	public Percolation(int N){
 
 		opened = new boolean[N][N];//default value false
 		wqUF = new WeightedQuickUnionUF(N*N + 2); 
 		vButtom = N * N + 1;
 		this.N = N;
 	}
 	
 	public void open(int i, int j){
 		
 		if(i < 1 || i > N || j < 1 || j > N)
 			throw new IndexOutOfBoundsException();
 		
 		if(isOpen(i, j)) return;
 		
 		connectNeighbor(i, j);//connect it with its neighbors
 		opened[i-1][j-1] = true;
 	}
 	
 	
 	public boolean isOpen(int i,int j){
 		if(i < 1 || i > N || j < 1 || j > N)
 			throw new IndexOutOfBoundsException();
 		
 		return opened[i-1][j-1];
 	}
 	
 	public boolean isFull(int i, int j){
 		if(i < 1 || i > N || j < 1 || j > N)
 			throw new IndexOutOfBoundsException();
 		
 		//corresponding index in wqUF
 		int pos = (i-1) * N + j; 
 		return wqUF.connected(0, pos);
 	}
 	
 	
 	public boolean percolates(){
 		return wqUF.connected(vTop, vButtom);
 	}
 	
 	
 	/**
 	 * union site(i,j) with its neighbors
 	 * requirement: this is site is open
 	 * @param i row index, start from 1
 	 * @param j column index, start from 1
 	 */
 	private void connectNeighbor(int i,int j){
 		int pos = (i-1)*N + j;
 		
 		int upPos = pos - N; // upper site index in wqUF
		if(upPos <= 0) upPos = 0;
		wqUF.union(pos,upPos);
 		
 		if(j>1){//left site exists and is open
 			if(isOpen(i, j-1)){
 				int leftPos = pos - 1;
 				wqUF.union(pos, leftPos);
 			}
 		}
 		
 		if(j<N){//right site exists and is open
 			if(isOpen(i, j+1)){
 				int rightPos = pos + 1;
 				wqUF.union(pos, rightPos);
 			}
 		}
 		
 		int downPos = pos + N; // down site index in wqUF
		if(downPos >= N*N) downPos = N*N + 1;
		wqUF.union(pos, downPos);
 	}
 }
