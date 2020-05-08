 public class Block {
 	//holds the index of:
 	//leftmost column, rightmost colum, top row, bottom row
 	//also contains the length (row) and the Height(column)
 
 	public int leftCol; //upper left x position
 	public int topRow;  //upper left y position
 	public int rightCol;
 	public int bottomRow;
 	public int myLength;
 	public int myHeight;
 	public int myWeight;
 	public int simmilarStartBlocks;
 	//public Block[] simmilarEndBlocks = new Block[3]; for tests
 	public Block[] simmilarEndBlocks;
 	
 	public Block(int x1,int y1, int x2, int y2)
 	{
 		//takes in (x,y) of top left corner, then (x,y) of bottom right corner
 		leftCol = x1;
 		topRow = y1;
 		rightCol = x2;
 		bottomRow = y2;
 		myLength = (rightCol - leftCol) + 1;
 		myHeight = (bottomRow - topRow) + 1;
 
 	}
 
 	public Block(Block previous)
 	{
 		//takes in a block, makes a clone of it
 		leftCol = previous.leftCol;
 		topRow = previous.topRow;
 		rightCol = previous.rightCol;
 		bottomRow = previous.bottomRow;
 		myLength = previous.myLength;
 		myHeight = previous.myHeight;
 		myWeight = previous.myWeight;
 		simmilarStartBlocks=previous.simmilarStartBlocks;
 		simmilarEndBlocks=previous.simmilarEndBlocks;
 	}
 
 	public boolean equals(Block b)
 	{
 		//compares two blocks, returns true if all variables are equal
 		if ((leftCol == b.leftCol) && 
 			(topRow == b.topRow) &&
 			(rightCol == b.rightCol) &&
 			(bottomRow == b.bottomRow) &&
 			(myLength == b.myLength) &&
 			(myHeight == b.myHeight))
 		{
 			return true;
 		}
 
 		return false;
 	}
 	
 	public int distanceFromClosestEndBlock() {
 		int leastDistance = 1000000;
 		
 		for (Block b: this.simmilarEndBlocks) 
 		{
 			int cur = (int) Math.sqrt((this.leftCol-b.leftCol)*(this.leftCol-b.leftCol) + 
 					 			   (this.topRow-b.topRow)*(this.topRow-b.topRow));
 			if (cur < leastDistance)
 			{
 				leastDistance = cur;
 			}
 			
 		} 
 		return leastDistance;
 	}
 	
 	public void calibrateWeight() {
 		//Will add more considerations
 		myWeight = this.distanceFromClosestEndBlock();
 	}
 
 }
