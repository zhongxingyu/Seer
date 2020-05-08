 public class Board {
     
     private int[][] tiles; //immutable data
     private int hammingValue = -1;
     private int manhattanValue = -1;
     
     public Board(int[][] blocks) {
         
         this.tiles = blocks;
         for (int i = 0; i < blocks.length; i++) {
             for (int j = 0; j < blocks.length; j++) {
                 this.tiles[i][j] = blocks[i][j];
             }
         }
     }
     
     public int dimension() {
         return this.tiles.length;
     }
     
     public int hamming() {
         
         if (hammingValue == -1) {
         
             int referenceNum = 1;
             int hammingNumber = 0;
             int lastIndex = this.dimension() - 1;
             
             
             for (int i = 0; i < this.dimension(); i++) {
                 for (int j = 0; j < this.dimension(); j++) {
                     if (this.tiles[i][j] != referenceNum) {
                         hammingNumber++;
                     }
                     referenceNum++;
                 }
             }
             
             //Since we are not considering the last element which should be '0'
             hammingNumber = hammingNumber - 1; 
             this.hammingValue = hammingNumber;
         }
         return this.hammingValue;
         
     }
     
     public int manhattan() {
         
         if (this.manhattanValue == -1) {
             int manhattanResult = 0;
             int tempNumber = 0;
             for (int i = 0; i < this.dimension(); i++) {
                 for (int j = 0; j < this.dimension(); j++) {
                     tempNumber = this.tiles[i][j];
                     if (tempNumber != 0) {
                         
                         manhattanResult += 
                             Math.abs(i - this.getXForNumber(tempNumber)) 
                             + Math.abs(j - this.getYForNumber(tempNumber)); 
                     }
                 }
             }
             this.manhattanValue =  manhattanResult;
         } 
         return this.manhattanValue;
     }
     
     // return x index for goal position of a number
     private int getXForNumber(int number) {
         int result = number / this.dimension();
         if (number == result * this.dimension())
             result--;
         return result;    
     }
     
     // return y index for goal position of a number
     private int getYForNumber(int number) {
         int remainder = number % this.dimension();
         if (remainder == 0)
             return this.dimension() - 1;
         else
             return remainder - 1; // since index start from 0 thus minus 1
     }
     
     public boolean isGoal() {
         int startNum = 1;
         int lastIndex = this.dimension() - 1;
         
         for (int i = 0; i < this.dimension() - 1; i++) {
             for (int j = 0; j < this.dimension(); j++) {
                 if (this.tiles[i][j] != startNum) 
                     return false;
                 startNum++;
             }
         }
         
         for (int k = 0; k < this.dimension() - 1; k++) {
             if (this.tiles[lastIndex][k] != startNum)
                 return false;
             startNum++;
         }
         
         if (this.tiles[lastIndex][lastIndex] != 0) return false;
         return true;
     }
     
     public Board twin() {
         
         int[][] newArray = new int[this.dimension()][this.dimension()];
         for (int i = 0; i < this.dimension(); i++) {
             for (int j = 0; j < this.dimension(); j++) {
                 newArray[i][j] = this.tiles[i][j];
             }
         }
         if (newArray.length > 1) {
             
             // Fixed a bug for twin function
             if (newArray[0][0] == 0 || newArray[0][1] == 0) {
                 int temp = newArray[1][0];
                 newArray[1][0] = newArray[1][1];
                 newArray[1][1] = temp;   
             } else {
                 int temp = newArray[0][0];
                 newArray[0][0] = newArray[0][1];
                 newArray[0][1] = temp;
                 
             }
         }
         
         return new Board(newArray);   
     }
     
     public boolean equals(Object y) {
         if (this == y) return true;
         if (y == null) return false;
         if (this.getClass() != y.getClass()) return false;
         Board that = (Board) y;
        
        for (int i = 0; i < this.dimension(); i++) {
            for (int j = 0; j < this.dimension(); j++) {
                if (this.tiles[i][j] != that.tiles[i][j])
                    return false;
            }
        }
        
        return true;
     }
     
     public Iterable<Board> neighbors() {
         Queue<Board> queue = new Queue<Board>();
         for (int i = 0; i < this.dimension(); i++) {
             for (int j = 0; j < this.dimension(); j++) {
 
                 if (this.tiles[i][j] == 0) {
                     
                     if (i > 0) {
                         int[][] newArray = this.cloneTiles();
                         newArray[i][j] = newArray[i - 1][j];
                         newArray[i - 1][j] = 0;
                         queue.enqueue(new Board(newArray));
                     }
                     
                     if (i < this.dimension() - 1) {
                         int[][] newArray = this.cloneTiles();
                         newArray[i][j] = newArray[i + 1][j];
                         newArray[i + 1][j] = 0;
                         queue.enqueue(new Board(newArray));
                     }
                     
                     if (j > 0) {
                         int[][] newArray = this.cloneTiles();
                         newArray[i][j] = newArray[i][j - 1];
                         newArray[i][j - 1] = 0;
                         queue.enqueue(new Board(newArray));
                     }
                     
                     if (j < this.dimension() - 1) {
                         int[][] newArray = this.cloneTiles();
                         newArray[i][j] = newArray[i][j + 1];
                         newArray[i][j + 1] = 0;
                         queue.enqueue(new Board(newArray));
                     }
                     break;
                 } //end if
             } //end inner for
         } // end outer for
         return queue;
     }
     
     private int[][] cloneTiles () {
         int length = this.dimension();
         int[][] newArray =  new int[length][length];
         for (int i = 0; i < length; i++) {
             for (int j = 0; j < length; j++) {
                 newArray[i][j] = this.tiles[i][j];
             }
         }
         return newArray;
     }
     
     public String toString() {
         int N = this.dimension();
         StringBuilder s = new StringBuilder();
         s.append(N + "\n");
         for (int i = 0; i < N; i++) {
             for (int j = 0; j < N; j++) {
                 s.append(String.format("%2d ", this.tiles[i][j]));
             }
             s.append("\n");
         }
         return s.toString();    
     }
     
     public static void main(String[] args) {
         int[][] goalArray = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
         
         // test isGoal()
         Board b1 = new Board(goalArray);
         assert b1.isGoal() : "array is not a goal board";
         
         // test equals() function
         Board b3 = new Board(goalArray);
         assert b1.equals(b3) : "Boards are not equal";
         
         int[][] testArray = {{1, 4, 3}, {2, 0, 6}, {8, 7, 5}};
         Board b2 = new Board(testArray);
         assert !b2.isGoal() : "board should not be a goal board";
         
         // test dimension() and hamming() function
         assert b2.dimension() == 3 : "Dimension for the current board is 3";
         assert b2.hamming() == 5 : "hamming is not correctly calculated.";
         
         int[][] testArray2 = {{1, 2, 3}, {4, 5, 6}, {8, 7, 0}};
         Board b4 = new Board(testArray2);
         assert b4.hamming() == 2 : "hamming is not correctly calculated for b4";
         
         // test manhattan result
         assert b1.manhattan() == 0 : "manhattan not equal to 0 for goal board";
         assert b2.manhattan() == 8 : "manhattan not equal to 0 for goal board";
         assert b4.manhattan() == 2 : "manhattan not equal to 0 for goal board";
         
         StdOut.println(b1);StdOut.println(b1.twin());
         StdOut.println(b2);StdOut.println(b2.twin());
         StdOut.println(b3);StdOut.println(b3.twin());
         StdOut.println(b4);StdOut.println(b4.twin());
         
         
         /*
         // test getXForNumber(int number) function, this will be removed
         assert b4.getXForNumber(3) == 0 : "wrong x calculation";
         assert b4.getXForNumber(2) == 0 : "wrong x calculation";        
         assert b4.getXForNumber(4) == 1 : "wrong x calculation";
         assert b4.getXForNumber(6) == 1 : "wrong x calculation";       
         assert b4.getXForNumber(8) == 2 : "wrong x calculation";
         assert b4.getXForNumber(7) == 2 : "wrong x calculation";
         
         // test getYForNumber(int number) function, this will be removed
         assert b4.getYForNumber(8) == 1 : "wrong y calculation";
         assert b4.getYForNumber(3) == 2 : "wrong y calculation";
         assert b4.getYForNumber(6) == 2 : "wrong y calculation";
         assert b4.getYForNumber(4) == 0 : "wrong y calculation";
         assert b4.getYForNumber(7) == 0 : "wrong y calculation";
         assert b4.getYForNumber(1) == 0 : "wrong y calculation";
         */
         
         // test neighbors
         
         StdOut.println("==========The following are neighbots of b1==========");
         //StdOut.println(b1);
         Queue<Board> queue = (Queue<Board>) b2.neighbors();
         while (!queue.isEmpty()) {
             Board board = queue.dequeue();
             StdOut.println(board);
         }
     }
 }
