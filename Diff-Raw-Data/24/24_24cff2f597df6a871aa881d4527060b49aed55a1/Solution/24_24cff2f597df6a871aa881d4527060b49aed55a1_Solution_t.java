 public class Solution {
     private int getInt( char c){
         return c-'0';
     }
     public boolean isValidSudoku(char[][] board) {
         if(board == null || board.length < 9 ||
                 board.length > 9 ||  board[0].length !=9)
             return false;
 
         for (int i = 0; i< 9; ++i){
             for(int j = 0; j<9; ++j){
                 if(board[i][j] == '.'){
                    int [] rowflag = new int [9];
                    int [] colflag = new int [9];
                    int [] subflag = new int [9];
 
                     //row
                     for(int k = 0; k<9; ++k){
                         if(board[i][k] != '.'){
                            if(rowflag[getInt(board[i][k])-1] == 1)
                                return false;
                            rowflag[getInt(board[i][k])-1] = 1; 
                         }
                     }
 
                     //col 
                     for(int k = 0; k<9; ++k){
                         if(board[k][j] != '.'){
                            if(colflag[getInt(board[k][j])-1] == 1)
                                return false;
                            colflag[getInt(board[k][j])-1] = 1; 
                         }
                     }
                     
                     // sub
                     int m = (i/3)*3;
                     int n = (j/3)*3;
                     for(int x = m; x < m+3; ++x)
                         for(int y = n; y < n+3; ++y)
                             if(board[x][y] != '.'){
                                if(subflag[getInt(board[x][y])-1] == 1)
                                    return false;
                                subflag[getInt(board[x][y])-1] =1;
                             }

                     for(int k = 0; k < 9; ++k){
                        if( (rowflag[k] | colflag[k] | subflag[k]) != 0)
                             continue;
                         board[i][j] = (char) ('0'+(k+1));
                         //call with possible solution, if recursion 
                         //call succeeded, return true
                         if(isValidSudoku(board))
                             return true;
                     }
                     board[i][j] = '.';
                     return false;
                 }
             }
         }
         return true;
     }
 }
