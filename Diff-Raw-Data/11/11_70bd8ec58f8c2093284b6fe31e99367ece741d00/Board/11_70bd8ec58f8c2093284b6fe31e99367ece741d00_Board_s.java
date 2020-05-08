 import java.util.Vector;
 public class Board {
     
     private int N;
     private int[][] tiles;
     
     private int freeRow;
     private int freeCol;
     
     private int manhattanValue;
     private int hammingValue;
     
     // construct a board from an N-by-N array of blocks
     // (where blocks[i][j] = block in row i, column j)
     public Board(int[][] blocks) {
         N = blocks.length;
         tiles = new int[N][N];
     
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N; j++) {
                 tiles[i][j] = blocks[i][j];
                 if (tiles[i][j] == 0) {
                     freeRow = i;
                     freeCol = j;
                 }
 
             }
         
         manhattanValue = -1;
         
         hammingValue = -1;
     }
     
     // board dimension N
     public int dimension() {
         return N;
     }
     
     // number of blocks out of place
     public int hamming() {
         
         if (hammingValue != -1)
             return hammingValue;
         int out = 0;
         
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N; j++) {
                 int idealNum = i*N + j +1;
                 if (tiles[i][j] != idealNum 
                         && tiles[i][j] != 0)
                     out++;
 
             }
         
         hammingValue = out;
         
         return out;
     }
     
     // sum of Manhattan distances between blocks and goal
     public int manhattan() {
         
         if (manhattanValue != -1)
             return manhattanValue;
         int out = 0;
 
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N; j++) {
                 int idealNum = i*N + j +1;
                 if (tiles[i][j] != idealNum 
                         && tiles[i][j] != 0) {
                    int row = tiles[i][j] / N;
                    int col = tiles[i][j] % N;
                     out += Math.abs(row - i) + Math.abs(col -j);
                     
                 }
             }
         
         manhattanValue = out;
         
         return out;
     }
     
     // is this board the goal board?
     public boolean isGoal() {
         boolean out = true;
         
         for (int i = 0; i < N*N -1; i++) {
             int row = i / N;
             int col = i % N;
             if (tiles[row][col] != i+1)
                 return false;
         }
         
         return out;
     }
     
     // a board obtained by exchanging two adjacent blocks in the same row
     public Board twin() {
         Board twinBoard = new Board(tiles);
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N -1; j++) {
                 if (twinBoard.tiles[i][j] != 0 
                         && twinBoard.tiles[i][j+1] != 0)
                 {
                     int t = twinBoard.tiles[i][j];
                     twinBoard.tiles[i][j] = twinBoard.tiles[i][j+1];
                     twinBoard.tiles[i][j+1] = t;
                     return twinBoard;
                 }
             }
         return null;
     }
     
     // does this board equal y?
     public boolean equals(Object y) {
         if (this == y)
             return true;
         if (!(y instanceof Board))
             return false;
         
         Board Y = (Board) y;
         
         if (N != Y.N)
             return false;
         
         if (freeRow != Y.freeRow || freeCol != Y.freeCol)
             return false;
         
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N; j++) {
                 if (tiles[i][j] != Y.tiles[i][j])
                     return false;
 
             }
         
         return true;
     }
     
     // all neighboring boards
     public Iterable<Board> neighbors() {
         Vector<Board> vector = new Vector<Board>();
         Board next;
         
         // Move the top one into the empty slot
         if (freeRow != 0) {
             next = new Board(this.tiles);
             next.tiles[freeRow][freeCol] = next.tiles[freeRow-1][freeCol];
             next.tiles[freeRow-1][freeCol] = 0;
             next.freeRow -= 1;
             vector.add(next);
         }
         
         // Move the left one into the empty slot
         if (freeCol != 0) {
             next = new Board(this.tiles);
             next.tiles[freeRow][freeCol] = next.tiles[freeRow][freeCol-1];
             next.tiles[freeRow][freeCol-1] = 0;
             next.freeCol -= 1;
             vector.add(next);            
         }
         
         // Move the bottom one into the empty slot
         if (freeRow != N-1) {
             next = new Board(this.tiles);
             next.tiles[freeRow][freeCol] = next.tiles[freeRow+1][freeCol];
             next.tiles[freeRow+1][freeCol] = 0;
             next.freeRow += 1;
             vector.add(next);            
         }      
         
         // Move the right one into the empty slot
         if (freeCol != N-1) {
             next = new Board(this.tiles);
             next.tiles[freeRow][freeCol] = next.tiles[freeRow][freeCol+1];
             next.tiles[freeRow][freeCol+1] = 0;
             next.freeCol += 1;
             vector.add(next);            
         }            
         return vector;
     }
     
     // string representation of the board 
     public String toString() {
         StringBuilder s = new StringBuilder();
         s.append(N + "\n");
         for (int i = 0; i < N; i++) {
             for (int j = 0; j < N; j++) {
                 s.append(String.format("%2d ", tiles[i][j]));
             }
             s.append("\n");
         }
         return s.toString();
     }
 }
