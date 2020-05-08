 /*Soduku.java - Daniel Moore
 100% Working - needs formatting*/
 import java.util.*;
 
 public class sudoku {
 	
 	public static void main(String[] args){
 		
 		//create scanner
 		Scanner inFile = new Scanner(System.in);
 		
 		//create main program counter
 		int games_i = inFile.nextInt();
 		
 		//loop through puzzles
 		for(int main_game_i = 1; main_game_i <= games_i; main_game_i ++){
 			
 			
 			//create new board
 			Board b = new Board();
 
 			//fill board with available data
 			b.init(inFile);
 
 			//create flag to stop iterative solution
 			boolean go_again = false;
 
 			//attempt to solve puzzle interatively
 			//updates each cell possible numbers in the process
 			do{
 
 				b.calc_all_pos_nums();
 
 				go_again = b.update_board();
 
 			}while(go_again);
 
 			//check if puzzle is solved
 			//if not, recursively solve using backtracking
 			if(b.solve_rec(0, 0)){
 			
 				//output board and case number
 				b.print_board(main_game_i);
 			
 			}else{
 				
 				//output that there is no solution
 				System.out.println("Test case "+ main_game_i +":");
 				System.out.println();
				System.out.println("No solution possible.");
 				System.out.println();
 				System.out.println();
 			}
 
 		}
 
 		
 	}//end main method
 }
 
 //Cells carry values when set.  When not set they carry 0
 //and an ArrayList of all possible values that need to be tried
 class Cell {
 	boolean is_set;
 	int row;
 	int col;
 	public ArrayList<Integer> pos_nums;
 	int value;
 	
 	public Cell(int row, int col, int val){
 		this.row = row;
 		this.col = col;
 		if(val == 0){
 			this.is_set = false;
 			this.pos_nums = new ArrayList<Integer>();
 			for (int i = 1; i <= 9; i++){
 				this.pos_nums.add(new Integer(i));
 			}
 			
 		}else{
 			this.is_set = true;
 			this.value = val;
 		}
 		
 		
 	}//end cell constructor
 	
 	//returns value of cell
 	public int get_val(){
 		return this.value;
 	}
 	
 }//end cell class
 
 //Board is a 2D array of cells
 class Board {
 	Cell [][] theBoard;
 	boolean is_solved;
 	
 	public Board(){
 		this(9, 9);
 	}
 	
 	public Board(int rows, int cols){
 		theBoard = new Cell [rows][cols];
 		this.is_solved = false;
 	}
 	
 	//outputs board and case number
 	public void print_board(int k){
 		System.out.println("Test case "+ k +":");
 		System.out.println();
 		for (int i = 0; i < 9; i ++){
 			for (int j = 0; j < 9; j ++){
 				System.out.print(theBoard[i][j].value+" ");
 			}
 			System.out.printf("\n");
 		}
 		System.out.println();
 		System.out.println();
 	}
 	
 	//loops through input file and populates board
 	public void init(Scanner s){
 		for(int i = 0; i < 9; i++){
 			for(int j = 0; j < 9; j ++){
 				theBoard[i][j] = new Cell(i, j, s.nextInt());
 			}
 		}
 	}
 	
 	//updates cell by checking against row conflicts
 	public void check_row(int row, int col){
 		for (int i = 1; i < 9; i ++){
 			if(theBoard[row][col].pos_nums.contains(theBoard[(row+i)%9][col].get_val())){
 				theBoard[row][col].pos_nums.remove(Integer.valueOf(theBoard[(row+i)%9][col].get_val()));
 				
 			}
 		}
 	}
 	
 	//updates cell by checking against column conflicts
 	public void check_col(int row, int col){
 		for (int i = 1; i < 9; i ++){
 			if(theBoard[row][col].pos_nums.contains(theBoard[row][(col+i)%9].get_val())){
 				theBoard[row][col].pos_nums.remove(Integer.valueOf(theBoard[row][(col+i)%9].get_val()));
 				
 			}
 		}
 	}
 	
 	//updates cell by checking against square conflicts
 	public void check_square(int row, int col){
 		int sq_row = (row/3) * 3;
 		int sq_col = (col/3) * 3;
 		int row_limit = sq_row + 3;
 		int col_limit = sq_col + 3;
 		for(int i = sq_row; i < row_limit; i ++){
 			for(int j = sq_col; j < col_limit; j ++){
 				if(!((i == row) && (j == col))){
 					if(theBoard[row][col].pos_nums.contains(theBoard[i][j].get_val())){
 						theBoard[row][col].pos_nums.remove(Integer.valueOf(theBoard[i][j].get_val()));
 					}
 				}
 			}
 		}
 	}
 	
 	//updates all cell pos_nums array by checking against conflicts
 	public void calc_all_pos_nums(){
 		for(int i = 0; i < 9; i ++){
 			for(int j = 0; j < 9; j++){
 				if(theBoard[i][j].is_set == false){
 					this.check_row(i, j);
 					this.check_col(i, j);
 					this.check_square(i, j);
 				}
 			}
 		}
 	}
 	
 	//if a cell has only one possible number, then set the value to that number
 	//returns true if any cell in board was updated
 	public boolean update_board(){
 		boolean gets_updated = false;
 		for(int i = 0; i < 9; i ++){
 			for(int j = 0; j < 9; j++){
 				if(theBoard[i][j].is_set == false){
 					if(theBoard[i][j].pos_nums.size() == 1){
 						
 						theBoard[i][j].value = theBoard[i][j].pos_nums.get(0);
 						
 						theBoard[i][j].is_set = true;
 						gets_updated = true;
 					}
 				}	
 			}
 		}
 		return gets_updated;
 	}
 	
 	//Solves puzzle using backtracking
 	//return true if a verified solution is found,
 	//false if board cannot be solved
 	public boolean solve_rec(int row, int col){
 		int next_row = row;
 		int next_col = col;
 
 		//base case: board is solved
 		if(this.solved()){
 			return true;
 		}else{
 			
 			//find the next empty cell
 			while(theBoard[row][col].value != 0){
 				if(!(row == 8 && col == 8)){
 					col ++;
 					if(col > 8){
 						col = 0;
 						row ++;
 					}
 				}
 			}
 			
 			//loop through all possible values of a cell
 			//if value is valid, try next cell
 			for(int loop_i = 0; loop_i < theBoard[row][col].pos_nums.size(); loop_i ++){
 				//set cell value to next value in possible numbers array
 				theBoard[row][col].value = theBoard[row][col].pos_nums.get(loop_i);
 				theBoard[row][col].is_set = true;
 				//check if valid
 				if(this.valid_move(row, col)){
 					
 					//move to next cell
 					if(!(row == 8 && col == 8)){
 						next_col = col + 1;
 						if(next_col > 8){
 							next_col = 0;
 							next_row = row + 1;
 						}
 					}else{
 						//check if board is at the last cell
 						//returns false if valid move is made, and board is not solved
 						if(!this.solved()){
 							return false;
 						}
 					}
 					//recursive step, if returns true, send true up call stack
 					if(this.solve_rec(next_row, next_col)){
 						return true;
 					//if false, un set value in current cell
 					}else{
 						theBoard[row][col].value = 0;
 						theBoard[row][col].is_set = false;
 					}
 				//if not valid move, un set value in current cell
 				}else{
 					
 					theBoard[row][col].value = 0;
 					theBoard[row][col].is_set = false;
 				}
 			}
 			
 			//if no value in possible number array works, un set value in cell
 			theBoard[row][col].value = 0;
 			theBoard[row][col].is_set = false;
 			return false;
 		}
 		
 	}
 
 	//checks if board is complete and free of conflicts
 	//returns true if board is solved
 	public boolean solved(){
 		for(int i = 0; i < 9; i ++){
 			for(int j = 0; j < 9; j ++){
 				if(theBoard[i][j].value == 0){
 					return false;
 				}
 				if(!this.valid_row(i, j)){
 					return false;
 				}
 				if(!this.valid_col(i, j)){
 					return false;
 				}
 				if(!this.valid_square(i, j)){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	//checks if row has conflicts
 	//returns false as soon as conflict is found
 	public boolean valid_row(int row, int col){
 		for(int i = 1; i < 9; i ++){
 			if(theBoard[row][col].value == theBoard[(row + i)%9][col].value){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	//checks if column has conflicts
 	//returns false as soon as conflict is found
 	public boolean valid_col(int row, int col){
 		for(int i = 1; i < 9; i ++){
 			if(theBoard[row][col].value == theBoard[row][(col + i)%9].value){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	//checks if square has conflicts
 	//returns false as soon as conflict is found
 	public boolean valid_square(int row, int col){
 		
 		int sq_row = (row/3) * 3;
 		int sq_col = (col/3) * 3;
 		int row_limit = sq_row + 3;
 		int col_limit = sq_col + 3;
 		for(int i = sq_row; i < row_limit; i ++){
 			for(int j = sq_col; j < col_limit; j ++){
 				if(!((i == row) && (j == col))){
 					if(theBoard[row][col].value == theBoard[i][j].value){
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 
 	}
 
 	//checks if last move was valid
 	//returns false as soon as conflict is found
 	public boolean valid_move(int row, int col){
 		if(!this.valid_row(row, col)){
 			return false;
 		}
 		if(!this.valid_col(row, col)){
 			return false;
 		}
 		if(!this.valid_square(row, col)){
 			return false;
 		}
 
 		return true;
 	}
 }//end board class
