 import java.util.LinkedList;
 import java.util.Arrays;
 
 /*
  * A tray is a representation of a board state. 
  * Trays will be stored inside a graph.
  * Tray is a vertex in the graph of tray states.
  * 
  * Knows:
  * 
  * 1. Position of blocks
  * 2. position of spaces
  * 3. Weight which represents its closeness to another tray
  * 
  * Does:
  * 
  * 
  * 
  * 
  * */
 
 
 public class Tray {
 	//myBoardState is a doubleArray showing position of blocks
 	//myBlockList contains all block objects on the tray
 	//myPreviousTray points back to the Tray preceding this one
 	
 	public int[][] myBoardState;
 	public Tray myPreviousTray;
 	public Block[] myBlockList;
 
 	public Tray(String[] config, String size) {
 		//takes in a String Array 
 		// of format [x y x y,x y x y, etc] where each cell is the points for one block
 		// as well as a string size of format "x y" 
 		myPreviousTray = null;
 		myBlockList = new Block[config.length];
 		String[] holder = size.split(" ");
 		myBoardState = new int[Integer.parseInt(holder[1])][Integer
 				.parseInt(holder[0])];
 		for (int n = 0; n < myBoardState.length; n++) {
 			Arrays.fill(myBoardState[n], -1);
 		}
 		for (int i = 0; i < myBlockList.length; i++) {
 			String[] block = config[i].split(" ");
 			myBlockList[i] = new Block(Integer.parseInt(block[0]),
 					Integer.parseInt(block[1]), Integer.parseInt(block[2]),
 					Integer.parseInt(block[3]));
 			for (int j = myBlockList[i].leftCol; j <= myBlockList[i].rightCol; j++) {
 				Arrays.fill(myBoardState[j], myBlockList[i].topRow,
 						myBlockList[i].bottomRow + 1, i);
 			}
 		}
 	}
 	
 	//direction is represented as clockwise positive integers from 1-4 inclusive
 	public Tray(Tray previousTray, int moveBlockId, int direction)
 	{
 		//makes new tray off of playing one move on previous Tray
 		myPreviousTray = previousTray;
 		myBlockList = previousTray.myBlockList;
 		myBoardState = previousTray.move(moveBlockId,direction).myBoardState;
 	}
 
 	public Tray(Tray previousTray)
 	{
 		//clones previous tray
 		myPreviousTray = previousTray;
 
 		myBlockList = new Block[previousTray.myBlockList.length];
 		
 		for (int b = 0;b < previousTray.myBlockList.length;b++)
 		{
 			myBlockList[b] = new Block( previousTray.myBlockList[b].leftCol,
 										previousTray.myBlockList[b].topRow,
 										previousTray.myBlockList[b].rightCol,
 										previousTray.myBlockList[b].bottomRow
 									  );
 							
 		}
 		
 		myBoardState = new int[previousTray.myBoardState.length][];
 
 		for (int i = 0;i < previousTray.myBoardState.length;i++)
 		{
 			myBoardState[i] = previousTray.myBoardState[i].clone();
 		}
 	}
 
 	//only used for testing right now
 		public Tray(int [][] inBoardState, Block[] inBlockList)
 		{
 			myBoardState = inBoardState;
 			myBlockList = inBlockList;
 			myPreviousTray = null;
 		}
 
 	//assumes move is possible / legal
 	public Tray move(int moveBlockId, int direction) {
 
 		Tray clone = new Tray(this);
 
 		//change the block representation in new tray
 		switch(direction){
 			case 1: 
 					for (int i = 0;i < myBlockList[moveBlockId].myLength;i++)
 					{
 						clone.myBoardState[clone.myBlockList[moveBlockId].leftCol+i]
 								[clone.myBlockList[moveBlockId].topRow-1] = moveBlockId;
 
 						clone.myBoardState[clone.myBlockList[moveBlockId].leftCol+i]
 								[clone.myBlockList[moveBlockId].bottomRow] = -1;
 					}
 
 					clone.myBlockList[moveBlockId].topRow = clone.myBlockList[moveBlockId].topRow-1;
 					clone.myBlockList[moveBlockId].bottomRow = clone.myBlockList[moveBlockId].bottomRow-1;
 
 					break;
 
 			case 2: 
 					for (int i = 0;i < myBlockList[moveBlockId].myHeight;i++)
 					{
 						clone.myBoardState[clone.myBlockList[moveBlockId].rightCol+1]
 								[clone.myBlockList[moveBlockId].topRow+i] = moveBlockId;
 
 						clone.myBoardState[clone.myBlockList[moveBlockId].leftCol]
 								[clone.myBlockList[moveBlockId].topRow+i] = -1;
 					}
 
 					clone.myBlockList[moveBlockId].leftCol = clone.myBlockList[moveBlockId].leftCol+1;
 					clone.myBlockList[moveBlockId].rightCol = clone.myBlockList[moveBlockId].rightCol+1;
 					break;
 
 			case 3: 
 					for (int i = 0;i < myBlockList[moveBlockId].myLength;i++)
 					{
 						clone.myBoardState[clone.myBlockList[moveBlockId].leftCol+i]
 								[clone.myBlockList[moveBlockId].bottomRow+1] = moveBlockId;
 
 						clone.myBoardState[clone.myBlockList[moveBlockId].leftCol+i]
 								[clone.myBlockList[moveBlockId].topRow] = -1;
 					}
 
 					clone.myBlockList[moveBlockId].topRow = clone.myBlockList[moveBlockId].topRow+1;
 					clone.myBlockList[moveBlockId].bottomRow = clone.myBlockList[moveBlockId].bottomRow+1;
 					break;
 
 			case 4: 
 					for (int i = 0;i < myBlockList[moveBlockId].myHeight;i++)
 					{
 						clone.myBoardState[clone.myBlockList[moveBlockId].leftCol-1]
 								[clone.myBlockList[moveBlockId].topRow+i] = moveBlockId;
 
 						clone.myBoardState[clone.myBlockList[moveBlockId].rightCol]
 								[clone.myBlockList[moveBlockId].topRow+i] = -1;
 					}
 
 					clone.myBlockList[moveBlockId].leftCol = clone.myBlockList[moveBlockId].leftCol-1;
 					clone.myBlockList[moveBlockId].rightCol = clone.myBlockList[moveBlockId].rightCol-1;
 					break;
 		}
 
 		return clone;
 	}
 
 
 	//given input ArrayList, adds all viable moves to the ArrayList as trays
 	public void getMoves(LinkedList<Tray> fringe)
 	{
 	for (int i = 0;i<myBlockList.length;i++){
 		for (int j=1;j<=4;j++)
 		{
 			Label1:
 			switch (j)
 			{
 			
 			case 1: //up
 				if (myBlockList[i].topRow-1<0)
 				{
 					break;
 				}
 				for(int k =myBlockList[i].leftCol;k<=myBlockList[i].rightCol;k++)
 				{
 					if (myBoardState[k][myBlockList[i].topRow-1]!=-1)
 					{
 						break Label1;
 					}
 				}
 				fringe.add(this.move(i, j));
 				break;
 			case 2: //right
 				if (myBlockList[i].rightCol+1>=myBoardState.length)
 				{
 					break;
 				}
 				for(int k =myBlockList[i].topRow;k<=myBlockList[i].bottomRow;k++)
 				{
 					if (myBoardState[myBlockList[i].rightCol+1][k]!=-1)
 					{
 						break Label1;
 					}
 				}
 				fringe.add(this.move(i, j));
 				break;
 			case 3: //down
 				if (myBlockList[i].bottomRow+1>=myBoardState[0].length)
 				{
 					break;
 				}
 				for(int k =myBlockList[i].leftCol;k<=myBlockList[i].rightCol;k++)
 				{
 					if (myBoardState[k][myBlockList[i].bottomRow+1]!=-1)
 					{
 						break Label1;
 					}
 				}
 				fringe.add(this.move(i, j));
 				break;
 			case 4: //right
 				if (myBlockList[i].leftCol-1<0)
 				{
 					break;
 				}
 				for(int k =myBlockList[i].topRow;k<=myBlockList[i].bottomRow;k++)
 				{
 					if (myBoardState[myBlockList[i].leftCol-1][k]!=-1)
 					{
 						break Label1;
 					}
 				}
 				fringe.add(this.move(i, j));
 				break;
 			}
 			}
 		}
 	}
 
	//Update This: Make refernce top position/size
 	public String moveMade(Tray next)
 	{
 		String prevPos = null;
 		String nextPos = null;
		
 
 		boolean prevPosFound = false;
 		boolean nextPosFound = false;
 
 		for(int col = 0; col < myBoardState[0].length; col++)
 		{
 			for(int row = 0; row < myBoardState.length; row++)
 			{
				if(this.myBoardState[row][col] != next.myBoardState[row][col])
 				{
					if(this.myBoardState[row][col]!=-1&&!prevPosFound)
					{
						prevPos = col + " " + row;
						prevPosFound = true;
					}
					if(next.myBoardState[row][col]!=-1&&!nextPosFound)
					{
						nextPos = col + " " + row;
						nextPosFound = true;
					}
 				}
 			}
 		}
 
 		return prevPos + " " + nextPos;
 	}
 	
 	public boolean equals(Tray other)
 	{
 		for(Block block : this.myBlockList)
 		{
 			if(other.myBoardState[block.leftCol][block.topRow]==-1)
 			{
 				return false;
 			}
 			if(!block.equals(other.myBlockList[other.myBoardState[block.leftCol][block.topRow]]))
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
