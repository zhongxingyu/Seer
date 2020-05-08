// TODO Merge all collision detections into one master method

 import java.util.ArrayList;
 
 class Board {
 	// for modeling/display 
 	// should be 10x20
 	
 	/*
 	 0  1  2  3  4  5  6  7  8  9  10
 	[ ][ ][ ][ ][T][ ][ ][ ][ ][ ][ ]0
 	[ ][ ][ ][T][T][T][ ][ ][ ][ ][ ]1
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]2
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]3
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]4
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]5
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]6
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]7
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]8
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]9
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]10
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]11
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]12
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]13
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]14
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]15
 	[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]16
 	[L][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]17
 	[L][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]18
 	[L][L][ ][ ][ ][ ][ ][ ][ ][ ][ ]19
 	*/
 	
 	
 	private String matrix[][];
 	
 	// the current bricks X coord
 	private int activeX;
 	// Y coord
 	private int activeY;
 	
 	private Piece active;
 	
	// these are the bricks that are already laid.
	// bricks contain their own rotation, so that should be fine.
	// TODO Answer Question: Is it necessary to keep track of dead pieces with ArrayList:bricks or does String:matrix take care of that?
	private ArrayList<Piece> bricks;
	private ArrayList coords;
	
 	public Board() {
 		// make a new board with nothing on it.
 		matrix = new String[10][20];
 	}
 	
 	public void add(Piece p) {
 		// add the brick to the list
 		active = p;
 		// position it at the top middle of the frame
 		activeX=2;
 		activeY=0;
 	}
 	
 	public void add() {
		// adds a completely random piece
		bricks.add(new Piece());
 		// position it at the top middle of the frame
 		activeX=2;
 		activeY=0;
 	}
 	
 	public void checkRows() {
 
 		ArrayList<Integer> rowsToClear = new ArrayList<Integer>();
 
 		for(int y = 0; y < matrix.length; y++)
 		{
 			boolean clearQ = true;
 			for(int x = 0; x < matrix[y].length; x++)
 			{
 				if(matrix[x][y] == null)
 					clearQ = false;
 			}
 			if(clearQ == true)
 				rowsToClear.add(y);
 		}
 
 		if(rowsToClear.size() != 0)
 			clearRows(rowsToClear);
 		else
 			System.out.println("none here boss");
 	}
 
 	public void clearRows(ArrayList<Integer> rows) {
 		// Clears necessary rows
 		for(int r : rows)
 		{	
 			for(int x = 0; x < matrix[r].length; x++)
 			{
 				matrix[x][r] = null;
 			}	
 		}
 		settle(rows);
 	}
 
 	public void settle(ArrayList<Integer> rows) {
 		// Settles the bricks
 		for(int r : rows)
 		{
 			for(int y = r; y > 0; y--)
 			{
 				for(int x = 0; x < matrix[y].length; x++)
 				{
 					matrix[x][y] = matrix[x][y-1];
 				}
 			}
 		}
 	}
 	
 	public boolean collisionDetection(int dx, int dy, Piece p) {
 
 		int[][] pMatrix = p.getPoints();
 		
 		boolean collision = false;
 		for(int y = 0; y < pMatrix.length; y++)
 		{
 			for(int x = 0; x < pMatrix[y].length; x++)
 			{
 				if(pMatrix[x][y] == 1)
 				{
 					if(activeX + x + dx < 0			// Left Board Collision Detection
					|| activeX + x + dx > matrix[y].length  // Right Board Collision Detection
 					|| matrix[activeX + x + dx][activeY + y + dy] != null) //Piece Collision Detection
 					{
 						collision = true;
 					}
 				}
 			}
 		}
 
 		return collision;
 	}
 	
 	public void left() {
 		// moves the brick to left
 		
 		if(!collisionDetection(-1, 0, active))
 			activeX--;
 	}
 	
 	public void right() {
 		// moves brick to right
 
 		if(!collisionDetection(1, 0, active))
 			activeX++;
 	}
 
 	public void rotate() {
 		// Rotates the piece
 		Piece temp = new Piece(active);
 		temp.rotateR();
 		if(!collisionDetection(0,0,temp))
 			active.rotateR();
 	}
 	
 	public void gravity() {
                 // Gravitates the active piece downward
 
                 if(!collisionDetection(0,1,active))
                         activeY++;
                 else
                         lockPiece();
         }
 
         public void lockPiece() {
                 // Locks the piece to the matrix then adds a new piece to the board
                 int[][] pMatrix = active.getPoints();
                 for(int y = 0; y < pMatrix.length; y++)
                 {
                         for(int x = 0; x < pMatrix[y].length; x++)
                         {
                                 if(pMatrix[x][y] == 1)
                                 {
                                         matrix[activeX + x][activeY + y] = active.getType();
                                 }
                         }
                 }
 
                 this.add();
         }
 
 		
 	public void display() {
 		String output = "";
 		// TODO implement this graphically
 		for (int y=0;y<20 ;y++ ) {
 			for (int x=0;x<10 ;x++ ) {
 				if (matrix[x][y]== null) {
 					output+="[ ]";
 				}
 				else {
 					output+=("["+matrix[x][y]+"]");
 				}
 			}
 			output+=("\n");
 		}
 		output+=("\n\n");
 		System.out.print(output);
 	}
 
 	// TODO What's 'refresh()' supposed to do?	
	public void refresh() {
 		for (int i=0;i<bricks.size() ;i++ ) {
 			
 		}
	}
 	
 }
