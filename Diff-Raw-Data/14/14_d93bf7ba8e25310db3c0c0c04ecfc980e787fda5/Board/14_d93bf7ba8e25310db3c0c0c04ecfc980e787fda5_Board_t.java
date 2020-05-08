 public class Board {
 	
 	int[] [] board = new int [9] [9];
 	boolean[] [] fixedBoard = new boolean [9] [9];
 	
 	public Board(String[] data){
 		this.insert(data);
 		this.isFixed(); //Too bad we aren't gambling
 	}
 	
 	public void insert(String[] data){
 		int n = 0;
 		for(int i=0; i<9; i++){
 			java.util.StringTokenizer str = new java.util.StringTokenizer(data[i]);
 			while(str.hasMoreTokens()){
 				for(int j=0; j<9; j++){
 					n=Integer.parseInt(str.nextToken());
 					board [i] [j] = n;
 				}
 			}
 		}
 	}
 	
 	public int[][] getBoard(){
 		return board;
 	}
 	
 	public int[][] getFixedBoard(){
 		int[][] fixedBoardNums = new int[9][9];
 		for(int i=0; i<9; i++){
 			for(int j=0; j<9; j++){
 				if(board [i] [j] != 0){
 					fixedBoardNums[i][j] = board [i] [j];
 				}
 			}
 		}
 		return fixedBoardNums;
 	}
 	
 	public String toString(){
 		String s = " ";
 		for(int i=0; i<9; i++){
 			for(int j=0; j<9; j++){
 				System.out.println(board [i] [j]);
 			}
 			System.out.println(" ");
 		}
 		return s;
 	}
 	
 	public void isFixed(){
 		for(int i=0; i<9; i++){
 			for(int j=0; j<9; j++){
 				if(board [i] [j] != 0){
 					fixedBoard [i] [j] = true;
 				}
 			}
 		}
 	}
 	
 	public boolean isBad(int r, int c, int posb){
 		//returns true if bad
 		boolean check=false;
 		for(int row=0; row<9; row++){
 			if(board[row][c]==posb){
 				check=true;
 			}
 		}
 		for(int col=0; col<9; col++){
 			if(board[r][col]==posb){
 				check=true;
 			}
 		}
 		
 		return true;
 	}
 	
	/*
 	public void move(Move m){// throws BadMoveException{
 		
	}*/
 }
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 /*	
 	public int returnBoard(int r, int c){
 		return board[r][c];
 	}
 	public boolean returnFixedBoard(int r, int c){
 		return fixedBoard[r][c];
 	}
 	
 	public int[] returnFixed(int r){
 		int[] fixedNums = new int [9];
 		for(int col=0; col<9; col++){
 			if(this.returnFixedBoard(r, col)==true){
 				fixedNums[col] = this.returnBoard(r, col);
 			}
 			else{
 				fixedNums[col] = 0;
 			}
 		}
 		java.util.Arrays.sort(fixedNums);
 		/*for(int col=0; col<9; col++){
 			System.out.println(fixedNums[col]);
 		}
 		return fixedNums;
 	}
 }
 */
