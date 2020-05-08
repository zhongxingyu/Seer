 import java.awt.*;
 import java.util.Random;
 import java.util.Arrays;
 
 public class Grid {
   boolean alive = true;
 
   Square[][] squares = new Square[Globals.SIDE_LENGTH][Globals.SIDE_LENGTH];
 
   //with structure [x,y]
   int[][] border  = new int[Globals.BORDER_COUNT] [2];
   int[][] inner   = new int[Globals.INNER_SIZE]   [2];
   int[][] all     = new int[Globals.ALL_SIZE]     [2];
 
   int[][] mhoList = new int[Globals.MHO_COUNT]    [2];
 
   int[] you       = new int[2];
 
 Random random   = new Random();
 
 public Grid() {
 int bcount = 0;
 int icount = 0;
 int acount = 0;
 for (int y = 0; y < Globals.SIDE_LENGTH; y++) {
   for (int x = 0; x < Globals.SIDE_LENGTH; x++) {
     all[acount] = new int[]{x, y};
     acount++;
     if (x == Globals.SIDE_LENGTH-1 || x == 0 || y == Globals.SIDE_LENGTH-1 || y == 0) {
       border[bcount] = new int[]{x,y};
       bcount++;
     }
     else {
       inner[icount] = new int[]{x,y};
       icount++;
     }
   }
 }
 //iterates over all, creating squares
 for (int i = 0; i < all.length; i++) {
     squares[all[i][0]][all[i][1]] = new Square(all[i][0], all[i][1]);
 }
 
 //iterates over border squares, placing fences
 for (int i = 0; i < border.length; i++) {
     squares[border[i][0]][border[i][1]].setValue(Globals.FENCE_VALUE);
 }
 
 //randomly generates fences, mhos, and you
 generateBlocks(Globals.FENCE_VALUE, Globals.FENCE_COUNT);
 generateBlocks(Globals.MHO_VALUE, Globals.MHO_COUNT);
 generateBlocks(Globals.YOU_VALUE, Globals.YOU_COUNT);
 }
     
     public void paint(Graphics g) {   
 	for (int i = 0; i < all.length; i++) {
 	    squares[all[i][0]][all[i][1]].paint(g);
 	}
     }
     
   public void generateBlocks(int blockValue, int count) {
     for(int i = 0; i < count; i++) {
       while(true) {
         int x = random.nextInt(Globals.INNER_LENGTH) + 1;
         int y = random.nextInt(Globals.INNER_LENGTH) + 1;
         if(blockValue == Globals.MHO_VALUE) {
           mhoList[i] = new int[]{x, y};
         }
         if(blockValue == Globals.YOU_VALUE) {
           you = new int[]{x, y};
         }
         if(squares[x][y].getValue() == Globals.BLANK_VALUE) {
           squares[x][y].setValue(blockValue);
           break;
         }
       }
     }
   }
 
   public int abs(int n) {
     if(n > 0) return n;
     else return -n;
   }
 
   public int getSign(int n) {
     return n/abs(n);
   }
 
   public void forcedMoveMho(int x, int y, int n) {
     if(squares[x][y].getValue() == Globals.BLANK_VALUE) {
       squares[x][y].setValue(Globals.MHO_VALUE);
       mhoList[n][0] = x;
       mhoList[n][1] = y;
     }
     else if(squares[x][y].getValue() == Globals.YOU_VALUE) {
       squares[x][y].setValue(Globals.MHO_VALUE);
       mhoList[n][0] = x;
       mhoList[n][1] = y;
       endGameLoss();
     }
     else {
       mhoList[n][0] = -1;
     }
   }
 
   public boolean unforcedMoveMho(int x, int y, int n, boolean isFenceSquareValid) {
     if(squares[x][y].getValue() == Globals.BLANK_VALUE) {
       squares[x][y].setValue(Globals.MHO_VALUE);
       mhoList[n][0] = x;
       mhoList[n][1] = y;
       return true;
     }
     else if(squares[x][y].getValue() == Globals.YOU_VALUE) {
       squares[x][y].setValue(Globals.MHO_VALUE);
       mhoList[n][0] = x;
       mhoList[n][1] = y;
       endGameLoss();
       return true;
     }
     else if(isFenceSquareValid && squares[x][y].getValue() == Globals.FENCE_VALUE) {
       mhoList[n][0] = -1;
       return true;
     }
     else {
       return false;
     }
 
   }
 
   public void moveMho(int x, int y, int n) {
       boolean toContinue = true;
       squares[x][y].setValue(Globals.BLANK_VALUE);
       //if in line with you
       if(x == you[0]) {
 	  if(getSign(y - you[1]) == 1) {
 	      forcedMoveMho(x, y-1, n);
 	  }
 	  else {
 	      forcedMoveMho(x, y+1, n);
 	  }
       }
       else if(y == you[1]) {
 	  if(getSign(x - you[0]) == 1) {
 	      forcedMoveMho(x-1, y, n);
 	  }
 	  else {
 	      forcedMoveMho(x+1, y, n);
 	  }
       }
       //not in line with you
       else {
 	  if(unforcedMoveMho(x - getSign(x-you[0]), y - getSign(y-you[1]), n, false)) {
 	      toContinue = false;
 	  }
 	  else if(abs(x-you[0]) > abs(y-you[1])) {
 	      if(unforcedMoveMho(x - getSign(x-you[0]), y, n, false)) {
 		  toContinue = false;
 	      }
 	      else if(unforcedMoveMho(x, y - getSign(y-you[1]), n, false)) {
 		  toContinue = false;
 	      }
 	  }
 	  else {
 	      if(unforcedMoveMho(x, y - getSign(y - you[1]), n, false)) {
 		  toContinue = false;
 	      }
 	      else if(unforcedMoveMho(x - getSign(x-you[0]), y, n, false)) {
 		  toContinue = false;
 	      }
 	  }
 
 	  if(toContinue) {
 	      if(unforcedMoveMho(x - getSign(x-you[0]), y - getSign(y-you[1]), n, true));
 	      else if(abs(x-you[0]) > abs(y-you[1])) {
 		  if(unforcedMoveMho(x - getSign(x-you[0]), y, n, true));
 		  else if(unforcedMoveMho(x, y - getSign(y-you[1]), n, true));
 	      }
 	      else {
 		  if(unforcedMoveMho(x, y - getSign(y - you[1]), n, true));
 		  else if(unforcedMoveMho(x - getSign(x-you[0]), y, n, true));
 	      }
 	  }
       }
   }
     
     public void moveMhos() {
 	int totalMhoCount = mhoList.length;
 	for (int i = 0; i < mhoList.length; i++) {
 	    int x = mhoList[i][0];
 	    int y = mhoList[i][1];
 	    if(x >= 0) {
 		moveMho(x, y, i);
 	    }
 	    else {
 		totalMhoCount--;
 	    }
 	}
 
 	if(totalMhoCount == 0) {
 	    endGameWin();
 	}	    
     }
 
     public void moveYou(int x2, int y2) {
 	int x = you[0] + x2;
 	int y = you[1] + y2;
 	squares[you[0]][you[1]].setValue(Globals.BLANK_VALUE);
 	if(squares[x][y].getValue() == Globals.BLANK_VALUE) {
 	    squares[x][y].setValue(Globals.YOU_VALUE);
 	    you[0] = x;
 	    you[1] = y;
 	}
 	else {
 	    endGameLoss();
 	}
     }
     
     public void endGameLoss() {
 	System.out.println("Loss");
     }
 
     public void endGameWin() {
 	System.out.println("Win");
     }
 
     public void keyAction(char c) {
 	switch(c) {
 	case 's': {
 	    break;
 	}
 	case 'j': {
 	    // jump
	    int x = random.nextInt(Globals.INNER_LENGTH) + 1;
	    int y = random.nextInt(Globals.INNER_LENGTH) + 1;
	    if(squares[x][y].getValue() != Globals.FENCE_VALUE) {
		moveYou(x-you[0],y-you[1]);
 	    }
 	    break;
 	}
 	case 'w': {
 	    // up
 	    moveYou(0,-1);
 	    break;
 	}
 	case 'x': {
 	    // down
 	    moveYou(0,1);
 	    break;
 	}
 	case 'd': {
 	    // right
 	    moveYou(1,0);
 	    break;
 	}
 	case 'a': {
 	    //left
 	    moveYou(-1,0);
 	    break;
 	} 
 	case 'e': {
 	    // up and right
 	    moveYou(1,-1);
 	    break;
 	}
 	case 'q': {
 	    // up and left
 	    moveYou(-1,-1);
 	    break;
 	}
 	case 'c': {
 	    // down and right
 	    moveYou(1,1);
 	    break;
 	}
 	case 'z': {
 	    // down and left
 	    moveYou(-1,1);
 	    break;
 	}
 	default: {
 	    break;
 	}
 	}
 	moveMhos();
 	
     }
 }
