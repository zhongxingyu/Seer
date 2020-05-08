 
 import java.util.*;
 
 public class EvaluationFunctions {
     //This class are the list of heuristics that will be used for the AI Agent
     //State s = Current State
     //move = Action
 
     private static int[][] cloneField(int[][] source) {
         //Clone a field
         int[][] clonedField = new int[source.length][source[0].length];
         for (int i = 0; i < source.length; i++) {
             System.arraycopy(source[i], 0, clonedField[i], 0, source[i].length);
         }
         return clonedField;
     }
 
     //Compute c' from c and a..
     private static int[][] computeHeuristicField(State s, int[] move) {
         int nextPiece = s.getNextPiece();
         int orientation = move[0];
         int slot = move[1];
 
         int newHt = s.getTop()[slot] - State.getpBottom()[nextPiece][orientation][0];
         int[][] clonedField = cloneField(s.getField());
         //for the tromino width, do the same thing
         for (int w = 1; w < State.getpWidth()[nextPiece][orientation]; w++) {
             //favor clearing the top rows first
             newHt = Math.max(s.getTop()[slot + w] - State.getpBottom()[nextPiece][orientation][w], newHt);
         }
         for (int i = 0; i < State.getpWidth()[nextPiece][orientation]; i++) {
             for (int h = newHt + State.getpBottom()[nextPiece][orientation][i];
                     h < newHt + State.getpTop()[nextPiece][orientation][i]; h++) {
 
                 if (h >= State.ROWS - 1) {
                     continue; //GGPOK already, dont consider -.-
                 }
                 clonedField[h][i + slot] = s.getTurnNumber();
             }
         }
         return clonedField;
     }
 
 
     private static int computelinesClearScore(State s, int[] move) {
         int nextPiece = s.getNextPiece();
         int orientation = move[0];
         int slot = move[1];
         //new height, the top of that particular row - the bottom of that particular piece
         //- becos 1 = a hole at the bottom
         int newHt = s.getTop()[slot] - State.getpBottom()[nextPiece][orientation][0];
 
         //for the tromino width, do the same thing
         for (int w = 1; w < State.getpWidth()[nextPiece][orientation]; w++) {
             //favor clearing the top rows first
             newHt = Math.max(s.getTop()[slot + w] - State.getpBottom()[nextPiece][orientation][w], newHt);
         }
 
         int[][] resultantField = computeHeuristicField(s, move);
 
         int linesCleared = 0;
         //from the new height + the next piece height (-1 cos index starts from 0)
         //down to newHt
         for (int h = newHt + State.getpHeight()[nextPiece][orientation] - 1; h >= newHt; h--) {
             if (h >= State.ROWS - 1) {// index start from 0
                 continue; //GGPOK already, dont consider -.-
             }
             boolean completeRow = true;
             for (int c = 0; c < State.COLS; c++) {
                 if (resultantField[h][c] == 0) { //if got hole
                     completeRow = false;
                     break;
                 }
             }
             if (completeRow) {
                 linesCleared += 1;
             }
         }
 
         return linesCleared;
     }
 
     private static int findMinHeight(State s) {
         int[] top = s.getTop();
         int lowest = Integer.MAX_VALUE;
         for (int i : top) {
             lowest = Math.min(i, lowest);
         }
 
         return lowest;
     }
     
     //Maximum Altitude - The height of the tallest column on the game board
     //note that it does not care about whether the rows get clear anot
     public static int maximumAltitude(State s, int[] move) {
         int highest = -1;
         int nextPiece = s.getNextPiece();
         int orientation = move[0];
         int slot = move[1];
         int pieceHt = State.getpHeight()[nextPiece][orientation];
 
         for (int w = 0; w < State.getpWidth()[nextPiece][orientation]; w++) {
             highest = Math.max(s.getTop()[slot + w] + pieceHt, highest);
         }
 
         return highest;
     }
 
     //The difference in height between the tallest column and the shortest column
     public static int altitudeDelta(State s, int[] move) {
         return EvaluationFunctions.maximumAltitude(s, move) - EvaluationFunctions.findMinHeight(s);
     }
 
     //Total lines clear so far -_-
     public static int linesCleared(State s, int[] move) {
         return s.getRowsCleared() + computelinesClearScore(s, move);
     }
 
     //total number of filled spot count after executing a move using the new 
     public static int filledSpotCount(State s, int[] move) {
 
         int count = 0;
         int[][] resultantField = computeHeuristicField(s, move);
         for (int y = 0; y < resultantField.length; y++) {
             for (int x = 0; x < resultantField[y].length; x++) {
                 int blockint = resultantField[y][x];
                 if (blockint > 0) {
                     count++;
                 }
             }
         }
 
         return count;
     }
 
     //same as filled spot count but multiplied by the row it is in
     public static int weightedFilledSpotCount(State s, int[] move) {
         int count = 0;
         int[][] resultantField = computeHeuristicField(s, move);
 
         for (int y = 0; y < resultantField.length; y++) {
             for (int x = 0; x < resultantField[y].length; x++) {
                 int blockint = resultantField[y][x];
                 if (blockint > 0) {
                     count = count * (y + 1);
                 }
             }
         }
         return count;
     }
 
     //Highest hole after executing a move
     public static int highestHole(State s, int[] move) {
         int max = 0;
         int[][] resultantField = computeHeuristicField(s, move);
 
         for (int x = 0; x < resultantField[0].length; x++) {
             for (int y = s.getTop()[x] - 2; y >= 0; y--) {
                 int cell = resultantField[y][x];
                 int topcell = resultantField[y + 1][x];
                 if (cell == 0 && topcell > 0) {
                     Math.max(y + 1, max);
                     break;
                 }
             }
         }
 
         return max;
     }
 
     //no. of connected holes after executing a move
     public static int connectedHoles(State s, int[] move) {
         Set<String> holes = new HashSet<String>();
         int count = 0;
         int[][] resultantField = computeHeuristicField(s, move);
         for (int x = 0; x < resultantField[0].length; x++) {
             for (int y = s.getTop()[x] - 2; y >= 0; y--) {
                 int cell = resultantField[y][x];
                 if (cell == 0) {//is empty  //is hole
                     String cur = x + ":" + y;
                     holes.add(cur);
                     String top = x + ":" + (y + 1);
                     String btm = x + ":" + (y - 1);
                     String left = (x - 1) + ":" + (y);
                     String right = (x + 1) + ":" + (y);
 
                     if (!holes.contains(top) && !holes.contains(btm) && !holes.contains(left) && !holes.contains(right)) {
                         count++;
                         int tempx = (x + 1);
                         while (tempx < s.getTop().length && resultantField[y][tempx] == 0) {
                             int tempy = y;
                             while (s.getTop()[tempx] > tempy && resultantField[tempy][tempx] == 0) {
                                 holes.add(tempx + ":" + tempy);
                                 tempy++;
                             }
                             tempx++;
                         }
                     }
                 }
             }
         }
         return count;
     }
 
     //no. of holes after executing a move
     public static int holesCount(State s, int[] move) {
         int count = 0;
         int[][] resultantField = computeHeuristicField(s, move);
         for (int x = 0; x < resultantField[0].length; x++) {
             for (int y = s.getTop()[x] - 2; y >= 0; y--) {
                 int cell = s.getField()[y][x];
                 if (cell == 0) { //is hole
                     count++;
                 }
             }
         }
         return count;
     }
 
     //same as holesCount but multipled by the row it is in
     public static int weightedHolesCount(State s, int[] move) {
         int count = 0;
         int[][] resultantField = computeHeuristicField(s, move);
         for (int x = 0; x < resultantField[0].length; x++) {
             for (int y = s.getTop()[x] - 2; y >= 0; y--) {
                 int cell = s.getField()[y][x];
                 if (cell == 0) { //is hole
                     count += count * (y + 1);
                 }
             }
         }
         return count;
     }
 
     //No. of blocks above highest hole
     public static int blocksAboveHighestHole(State s, int[] move) {
         int holex = 0;
         int holey = 0;
         int[][] resultantField = computeHeuristicField(s, move);
         for (int x = 0; x < resultantField[0].length; x++) {
             for (int y = s.getTop()[x] - 2; y >= 0; y--) {
                 int cell = s.getField()[y][x];
                 int topcell = resultantField[y + 1][x];
                 if (cell == 0 && topcell > 0) {
                     if ((y + 1) > holey) {
                         holex = x;
                         holey = (y + 1);
                     }
                     break;
                 }
             }
         }
         return s.getTop()[holex] - holey;
     }
 
     //maximum contact area of a piece after executing a move
     public static int maximumContactArea(State s, int[] move) {
         int count = 0;
         int nextPiece = s.getNextPiece();
         int orientation = move[0];
         int slot = move[1];
 
         int newHt = s.getTop()[slot] - State.getpBottom()[nextPiece][orientation][0];
         int[][] currField = s.getField();
         //for the tromino width, do the same thing
         for (int w = 1; w < State.getpWidth()[nextPiece][orientation]; w++) {
             //favor clearing the top rows first
             newHt = Math.max(s.getTop()[slot + w] - State.getpBottom()[nextPiece][orientation][w], newHt);
         }
 
         for (int c = 0; c < State.getpWidth()[nextPiece][orientation]; c++) {
             for (int h = newHt + State.getpBottom()[nextPiece][orientation][c];
                     h < newHt + State.getpTop()[nextPiece][orientation][c]; h++) {
 
                 if (h >= State.ROWS - 1) {
                     continue; //GGPOK already, dont consider -.-
                 }
                 //get the left, right and bottom sides
                 int bottom = h - 1;
                 int leftSide = c - 1 + slot;
                 int rightSide = c + 1 + slot;
 
                 //bottom check, to see if it touches
                 if ((currField[bottom][c + slot] != 0) && (bottom >= 0)) {
                     count += 1;
                 }
                 //left side check to see if it touches
                 if ((currField[h][leftSide] != 0) && (leftSide >= 0)) {
                     count += 1;
                 }
                 if (leftSide < 0) { //touch wall check
                     count += 1;
                 }
                 //Right side check to see if it touches
                 if ((currField[h][rightSide] != 0) && (rightSide < State.COLS)) {
                     count += 1;
                 }
                 if (rightSide >= State.COLS) { //touch wall check
                     count += 1;
                 }
             }
         }
         return count;
     }
 
 	//maximum depth of a well after executing a move
 	public static int maxWellDepth(State s, int[] move) {
 		int maxWellDep = 0;
 		int col = State.COLS - 1;
 		int[][] resultantField = computeHeuristicField(s, move);
		int[] top=computeTop(resultantField)
 		
 		for (int x = 0; x < State.COLS; x++) {
 			//if check if the side of the column to see if it is enclosed and does not have 
 			if ((x == 0 || top[x - 1] > top[x]) && (x == col || top[x + 1] > top[x])) {
 				int wellDepth = 0;
 				if(x==0)//first column
 				{
 					wellDepth = top[x+1] -top[x];
 				}
 				else if(x==col)//last column
 				{
 					wellDepth = top[x-1] - top[x];
 				}
 				else
 				{
 					wellDepth=Math.min(top[x - 1], top[x + 1])- top[x];
 				}
 				maxWellDep = Math.max(wellDepth, maxWellDep);
 			}
 		}
 		
 		
 		return maxWellDep;
 	}
 
 
 	//maximum depth of a well after executing a move
 	public static int TotalWellDepth(State s, int[] move) {
 		int totalWellDep = 0;
 		int col = State.COLS - 1;
 		int[][] resultantField = computeHeuristicField(s, move);
		int[] top=computeTop(resultantField)
 		
 		for (int x = 0; x < State.COLS; x++) {
 			//if check if the side of the column to see if it is enclosed and does not have 
 			if ((x == 0 || top[x - 1] > top[x]) && (x == col || top[x + 1] > top[x])) {
 				int wellDepth = 0;
 				if(x==0)//first column
 				{
 					wellDepth = top[x+1] - top[x];
 				}
 				else if(x==col)//last column
 				{
 					wellDepth = top[x-1] - top[x];
 				}
 				else
 				{
 					wellDepth=Math.min(top[x - 1], top[x + 1])- top[x];
 				}
 				totalWellDep += wellDepth;
 			}
 		}
 		
 		
 		return totalWellDep;
 	}	
 
 	//no. of wells after executing a move
 	public static int wellCount(State s, int[] move) {
 		int count = 0;
 		int[][] resultantField = computeHeuristicField(s, move);
 		for (int x = 0; x < State.COLS; x++) {
 			int[] top = resultantField[x];
 			int startCount=0;
 			int depth=0;
 			int wellSpotted=0;
 			
 			for(int y = top.length-1; y>=0; y--)
 			{
 				int cell=s.getField()[y][x];
 				if(cell!=0)//if the top hole is not filled start to count
 				{
 					startCount=1;
 				}
 				
 				if(startCount==1)
 					depth++;
 
 				if(depth>3)
 				{
 					if(cell!=0)
 					{
 						wellSpotted=1;
 					}
 					
 					if(wellSpotted==1)
 					{
 						if(cell==0)
 						{
 							wellSpotted=0;
 							count++;
 						}
 					}
 				}
 			}
 		}
 		return count;
 	}
 
 	//Surface area roughness after executing a move
 	public static int surfaceAreaRoughness(State s, int[] move) {
 		int roughness = 0;
 		int[][] resultantField = computeHeuristicField(s, move);
 		int[] top=computeTop(resultantField);
 		
		for(int c=0; c<top.length-1; c++)//by column
 		{
			roughness+=Math.abs(top[c] - top[c+1]);
 		}
 		return roughness;
 	}
 	
 	//Compute top after executing a move
 	private static int[] computeTop(int[][] resultField)
 	{
 		int col=State.COLS;
 		int height=State.ROWS;
 		int[]  top=new int[col];
 		for (int c=0; c<col; c++)
 		{
 			top[c]=0;
 			for(int h=height-1; h>=0; h--)
 			{
 				int cell=resultField[h][c];
 				if(cell!=0)
 				{
 					top[c]=h+1;
 					break;
 				}
 			}
 		}	
 		return top;
 	}
 }
 
