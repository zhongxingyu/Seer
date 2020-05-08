 /**
  *
  *	Board represents a single game state. It knows the 
  *	position of each chip, and enforces the rules of the 
  *	game, throwing an exception when a violation is reached.
  *	
  *	Invariants:
  *  	1. Will throw an exception if a move is illegal.
  *  	2. No two chips in any given place.
  *  	3. Cannot be three chips adjacent to each other.
  *  	4. Cannot place more than 10 chips of the same color.
  *  	5. No chips in wrong goals.
  *  	6. No chips in corners.
  *  	7. The board will NOT CHANGE in the event of an exception
  *	
 **/
 
 package Board;
 
 import Constants.Constants;
 import DList.*;
 import java.io.*;
 
 
 public class Board{	
 
 
 	/**
 	 *
 	 *	Constructs a new Board.
 	 *
 	**/
 	protected Chip[][] grid;
 	
 	public Board() {
 		grid = new Chip[Constants.BOARDWIDTH][Constants.BOARDHEIGHT];
 	}
 
 
 	/**
 	 *
 	 *	Moves chip c to point x,y
 	 *	@param x1,x2 the coordinates of the chip to be moved.
 	 *	@param x1,x2 the coordinates of the chip's destination.
 	 *
 	**/
 	public void moveChip(int x1, int y1, int x2, int y2) throws InvalidMoveException{
 		//Make sure there's a chip at (x1,y1)
 		if (!hasChip(x1,y1)){
 			throw new InvalidMoveException("No chip to move from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ").");
 		}
 
 		//Verify that the chip is only being moved 1 step orthogonally or diagonally
 		if (Math.abs(x1-x2)!=1 || Math.abs(y1-y2)!=1){
 			throw new InvalidMoveException("No chip to move from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ").");
 		}
 
 		//Get the chip at (x1,y1)'s color
 		int currColor = getChip(x1,x2).getColor();
 
 		//Copy the chip to (x2,y2). If doing so throws an exception 
 		//(x2,y2 is occupied, for instace), the exception is passed 
 		//to caller.
 		addChip(currColor,x2,y2);
 		grid[x1][y1]=null;
 	}
 
 
 	/**
 	 *
 	 *	Creates chip at x,y.
 	 *	@param color the color of the chip to be
 	 *		   created
 	 *	@param x the destination x coordinate
 	 *	@param x the destination x coordinate
 	 *	@param y the destination y coorinate
 	 *
 	**/
 	public void addChip(int color, int x, int y) throws InvalidMoveException{
 		//Enforce chip placement rule 3
 		if (hasChip(x,y)){
 			throw new InvalidMoveException("There is already a chip at (" + x+","+y+").");
 		}
 
 		//Create the new chip
 		grid[x][y]=new Chip(color);
 
 		//Enforce chip placement rules 1, 2, and 4. These are rules dependent on board state.
 		if (!isValid()){
 			grid[x][y]=null; //Revert the board if the move invalidates the board.
 			throw new InvalidMoveException("Placement rule violated.");
 		}
 	}
 
 
 
 	/**
 	 *
 	 *	Gets the chip at x,y.
 	 *	@param x the target x coordinate
 	 *	@param y the target y coordinate
 	 *	@return the Chip at x,y, or null if there is no chip.
 	 *
 	**/
 	public Chip getChip(int x, int y){
 		return grid[x][y];
 	}
 
 
 	/**
 	 *
 	 *	Tells whether a chip at x,y exists
 	 *	@param x the target x coordinate
 	 *	@param y the target y coordinate
 	 *	@return whether there is a chip at x,y
 	 *
 	**/
 	public boolean hasChip(int x, int y){
 		return grid[x][y]!=null;
 	}
 	/**
 	 *
 	 *	Returns whether this Chip a is line of sight with Chip b
 	 *	@param x1,y1 are the coordinates of the chip of reference.
 	 *	@param x2,y2 are the coordinates of the chip for comparison
 	 *	@return whether the chip is line of sight with b
 	 *
 	**/
 	public boolean isLOS(int x1,int x2,int y1,int y2) {
 		return !(x1==x2 || y1==y2 || Math.abs((y2-y1)/(x2-x1))==1);
 	}
 	
 	/**
 	 *
 	 *	Returns whether chip 1 interrupts the path between chips 2 and 3.
 	 *	@param x1, y1 the reference chip
 	 *	@param x2, y2 the second chip
 	 *	@param x3, y3 the third chip
 	 *	@return whether this chip is between 2 and 3.
 	 *
 	**/
 	public boolean isInterruptPath(int x1, int y1, int x2, int y2, int x3, int y3) {
 		//If 2 and 3 do not form a valid Network path, then no interruption occurs.
 		if (!isLOS(x2,y2,x3,y3)){
 			return false;
 		}
 
 		//Check if 1 is in the rectangle bounded by 2 and 3.
 		if (!(((x1 > x2 && x1 < x3) || (x1 > x3 && x1 < x2)) && (y1 > y2 && y1 < y3) || (y1 > y3 && y1 < y2))){
 			return false;
 		}
 
 		//Check 1 is on the path between 2 and 3.
 		return y1==y2+(Math.abs((y2-y3)/(x2-x3)))*(x1-x2);
 	}
 
 	public boolean isValid(){
 		//Enforce chip placement rule 1. Cannot place in corners.
 		if (hasChip(0,0) || hasChip(Constants.BOARDWIDTH-1,0) || hasChip(0,Constants.BOARDHEIGHT-1) || hasChip(Constants.BOARDWIDTH-1,Constants.BOARDHEIGHT-1)){
 			return false;
 		}
 		//Enforce chip placement rule 2. Cannot place in wrong goal.
 		for (int x = 0; x < Constants.BOARDWIDTH; x++){
 			if (hasChip(x,0) && getChip(x,0).color==Constants.WHITE){
 				return false;
 			}
 			if (hasChip(x,Constants.BOARDHEIGHT-1) && getChip(x,Constants.BOARDHEIGHT-1).color==Constants.WHITE){
 				return false;
 			}
 		}
 		for (int y = 0; y < Constants.BOARDHEIGHT; y++){
 			if (hasChip(0,y) && getChip(0,y).color==Constants.BLACK){
 				return false;
 			}
 			if (hasChip(Constants.BOARDWIDTH-1,y) && getChip(Constants.BOARDWIDTH-1,y).color==Constants.BLACK){
 				return false;
 			}
 		}
 
 		//Enforce chip placement rule 4. Cannot place three in contact.
 		for (int x = 0; x < Constants.BOARDWIDTH; x++){
 			for (int y = 0; y < Constants.BOARDHEIGHT; y++){
 				if (getSameColorNeighbors(x,y)>=2){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 *
 	 *	Returns the number of same-color neighbors this chip has.
 	 *	@returns the number of neighbors of this chip's color.
 	 *
 	**/
 	private int getSameColorNeighbors(int x, int y) {
 		if (!hasChip(x,y)){
 			return 0;
 		}
 		int total = 0;
 		int currColor = grid[x][y].getColor();
 		//Check the top left corner
 		if (x!=0 && y!=0 && hasChip(x-1,y-1) && grid[x-1][y-1].getColor()==currColor){
 			total+=1;
 		}
 		//Check left
 		if (x!=0 && hasChip(x-1,y) && grid[x-1][y].getColor()==currColor){
 			total+=1;
 		}
 		//Check up
 		if (y!=0 && hasChip(x,y-1) && grid[x][y-1].getColor()==currColor){
 			total+=1;
 		}
 		//Check the bottom right corner
 		if (x!=Constants.BOARDWIDTH-1 && y!=Constants.BOARDHEIGHT-1 && hasChip(x+1,y+1) && grid[x+1][y+1].getColor()==currColor){
 			total+=1;
 		}
 		//Check right
 		if (x!=Constants.BOARDWIDTH-1 && hasChip(x+1,y) && grid[x+1][y].getColor()==currColor){
 			total+=1;
 		}
 		//Check down
 		if (y!=Constants.BOARDHEIGHT-1 && hasChip(x,y+1) && grid[x][y+1].getColor()==currColor){
 			total+=1;
 		}
 		//Check the bottom left corner
 		if (x!=0 && y!=Constants.BOARDHEIGHT-1 && hasChip(x-1,y+1) && grid[x-1][y+1].getColor()==currColor){
 			total+=1;
 		}
 		//Check the top right corner
 		if (x!=Constants.BOARDWIDTH-1 && y!=0 && hasChip(x+1,y-1) && grid[x+1][y-1].getColor()==currColor){
 			total+=1;
 		}
 		return total;
 	}
 
 	/**
 	 *
 	 * Returns whether the game is over.
 	 * @return the color of the winning player, or Constants.NULL_PLAYER if the game is not over.
 	 *
 	**/
 	public int getWinner(){
 		DList<Net> networks = getLongestNetworks();
 		DListNode<Net> curr = networks.getFront();
 		while (curr!=null){
 			if (curr.item().complete){
 				return curr.item().player;
 			}
 			curr=curr.next();
 		}
 		return Constants.NULL_PLAYER;
 	}
 
 	public String toString(){
 		String out="";
 		for (int y = 0; y < Constants.BOARDHEIGHT; y++){
 			for (int x = 0; x < Constants.BOARDWIDTH; x++){
 				if (!hasChip(x,y)){
 					out+=".";
 				}else if (getChip(x,y).color==Constants.BLACK){
 					out+="X";
 				}else if (getChip(x,y).color==Constants.WHITE){
 					out+="O";
 				}else{
 					Constants.print("This should never happen (at Board.toString)");
 				}
 			}
 			out+="\n";
 		}
 		return out;
 	}
 
 	/**
 	 *
 	 * Returns a list of networks
 	 * @return a DList of networks
 	 *
 	**/
 
 	public DList<Net> getLongestNetworks(){
 		DList<Net> out = new DList<Net>();
 		for (int x = 0; x < Constants.BOARDWIDTH;x++){
 			if (hasChip(x,0)){
 				out.append(expandLongestNetFromChip(x,0,0,0,x,0));
 			}
 			if (hasChip(x,Constants.BOARDWIDTH-1)){
 				out.append(expandLongestNetFromChip(x,Constants.BOARDWIDTH-1,0,0,x,Constants.BOARDWIDTH-1));
 			}
 		}
 		for (int y = 0; y < Constants.BOARDHEIGHT;y++){
 			if (hasChip(0,y)){
 				out.append(expandLongestNetFromChip(0,y,0,0,0,y));
 			}
 			if (hasChip(Constants.BOARDHEIGHT-1,y)){
 				out.append(expandLongestNetFromChip(Constants.BOARDHEIGHT-1,y,0,0,Constants.BOARDHEIGHT-1,y));
 			}
 		
 		}
 		return out;
 	}
 
 	private DList<Net> expandLongestNetFromChip(int x, int y, int last_direction, int depth, int origin_x, int origin_y){
 		//Base cases:
 		//	1. x,y is in the origin goal, return empty.
 		//	2. x,y is in the target goal, return path.
 		//Recursive cases:
 		//	1. x,y is not any of the base cases.
 		//	2. x,y is a dead end. append path.
 		
 		Chip c = getChip(x,y);
 		DList<Net> out = new DList<Net>();
 		boolean isDeadEnd=true;
 
 		//Check Base Cases
 		if (!(x==origin_x && y==origin_y)) //Add exception for first node
 		{
 			if (c.getColor() == Constants.WHITE && (x==0 || x==Constants.BOARDWIDTH-1)){
 				if (x==origin_x){
 					return out;
 				}else{
 					out.push(new Net(c.getColor(),depth+1,depth+1>=6));
 					return out;
 				}
 			}
 			if (c.getColor() == Constants.BLACK && (y==0 || y==Constants.BOARDHEIGHT-1)){
 				if (y==origin_y){
 					return out;
 				}else{
 					out.push(new Net(c.getColor(),depth+1,depth+1>=6));
 					return out;
 				}
 			}
 
 		}
 
 		c.visited=true;
 		//Left-right, direction code 1
 		if (last_direction!=1){ //Make sure the network turns
 			for (int s = 1; s <= Constants.BOARDWIDTH-1-x; s++){
 				if (hasChip(x+s,y)){
 					if (getChip(x+s,y).color==c.color && !getChip(x+s,y).visited){
 						out.append(expandLongestNetFromChip(x+s,y,1,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 			for (int s = -1; s >= -x; s--){
 				if (hasChip(x+s,y)){
 					if (getChip(x+s,y).color==c.color && !getChip(x+s,y).visited){
 						out.append(expandLongestNetFromChip(x+s,y,1,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 		}
 
 		//Up-down, direction code 2
 		if (last_direction!=2){ //Make sure the network turns
 			for (int s = 1; s <= Constants.BOARDHEIGHT-1-y; s++){
 				if (hasChip(x,y+s)){
 					if (getChip(x,y+s).color==c.color && !getChip(x,y+s).visited){
 						out.append(expandLongestNetFromChip(x,y+s,2,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 			for (int s = -1; s >= -y; s--){
 				if (hasChip(x,y+s)){
 					if (getChip(x,y+s).color==c.color && !getChip(x,y+s).visited){
 						out.append(expandLongestNetFromChip(x,y+s,2,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 		}
 
 		//Ascending diagonal, direction code 3
 		if (last_direction!=3){ //Make sure the network turns
 			for (int s = 1; s <= Math.min(Constants.BOARDHEIGHT-1-y,Constants.BOARDWIDTH-1-x); s++){
 				if (hasChip(x+s,y+s)){
 					if (getChip(x+s,y+s).color==c.color && !getChip(x+s,y+s).visited){
 						out.append(expandLongestNetFromChip(x+s,y+s,3,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 			for (int s = -1; s >= Math.max(-x,-y); s--){
 				if (hasChip(x+s,y+s)){
 					if (getChip(x+s,y+s).color==c.color && !getChip(x+s,y+s).visited){
 						out.append(expandLongestNetFromChip(x+s,y+s,3,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 		}
 
 		//Descending diagonal, direction code 4
 		if (last_direction!=4){ //Make sure the network turns
 			for (int s = 1; s <= Math.min(y,Constants.BOARDWIDTH-1-x); s++){
 				if (hasChip(x+s,y-s)){
 					if (getChip(x+s,y-s).color==c.color && !getChip(x+s,y-s).visited){
 						out.append(expandLongestNetFromChip(x+s,y-s,4,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 			for (int s = -1; s >= Math.min(x,Constants.BOARDHEIGHT-1-y); s--){
 				if (hasChip(x+s,y-s)){
 					if (getChip(x+s,y-s).color==c.color && !getChip(x+s,y-s).visited){
 						out.append(expandLongestNetFromChip(x+s,y-s,4,depth+1,origin_x,origin_y));
 						isDeadEnd=false;
 						break;
 					}else{
 						break;
 					}
 				}
 			}
 		}
 		c.visited=false;
 		if (isDeadEnd){
 			out.push(new Net(c.getColor(),depth+1,false));
 		}
 		return out;
 	}
 
 	public static void main(String[] args){
 		testNetworkGetterInteractive();
 	}
 
 	//Test packages
 	
 	private static void testNetworkGetterInteractive(){
 		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
 		int input=0;
 		Board b = new Board();
 
 		while (input<1000){
 			Constants.print(b);
 			Constants.print("Enter three integers in the format cxy, where c is color, or '1000' to quit:");
 			try{
 				input=Integer.parseInt(keyboard.readLine());
 			}
 			catch (IOException e){}
 			Constants.print("");
 			testAdd(b,(input/100)%10,(input/10)%10,input%10);
 			Constants.print(b.getLongestNetworks());
 		}
 	}
 
 	private static void testNetworkGetter(){
 		Board b=new Board();
 		testAdd(b,0,3,0);
 		testAdd(b,0,3,1);
 		testAdd(b,0,3,3);
 		testAdd(b,0,3,4);
 		testAdd(b,0,3,5);
 		testAdd(b,0,3,6);
 		testAdd(b,0,5,6);
 		Constants.print(b);
 		Constants.print(b.getLongestNetworks());
 	}
 
 	private static void testRuleImplementation(){
 		Board board;
 		Constants.print("");
 		Constants.print("Testing Board.getSameColorNeighbors...");
 		Constants.print("======================================");
 		board = new Board();
 		board.grid[5][5]=new Chip(Constants.BLACK);
 		board.grid[5][6]=new Chip(Constants.BLACK);
 		board.grid[5][7]=new Chip(Constants.BLACK);
 		board.grid[6][6]=new Chip(Constants.WHITE);
 		Constants.printTest(1,board.getSameColorNeighbors(5,5));
 		board.grid[4][4]=new Chip(Constants.BLACK);
 		board.grid[4][5]=new Chip(Constants.BLACK);
 		board.grid[4][6]=new Chip(Constants.BLACK);
 		board.grid[5][4]=new Chip(Constants.BLACK);
 		board.grid[6][4]=new Chip(Constants.BLACK);
 		board.grid[6][5]=new Chip(Constants.BLACK);
 		Constants.printTest(7,board.getSameColorNeighbors(5,5));
 		board.grid[0][0]=new Chip(Constants.WHITE);
 		Constants.printTest(0,board.getSameColorNeighbors(0,0));
 		board.grid[0][1]=new Chip(Constants.WHITE);
 		Constants.printTest(1,board.getSameColorNeighbors(0,0));
 		board.grid[1][1]=new Chip(Constants.WHITE);
 		board.grid[1][0]=new Chip(Constants.WHITE);
 		Constants.printTest(3,board.getSameColorNeighbors(0,0));
 		Constants.print("");
 		Constants.print("Testing Board.addChip...");
 		Constants.print("========================");
 		board = new Board();
 		Constants.print(board);
 		Constants.print("Testing enforcement of Placement Rule 1...");
 		Constants.print("--------------------------------");
 		Constants.print("\t The following tests");
 		Constants.print("\t should all throw");
 		Constants.print("\t exceptions.");
 		Constants.print("--------------------------------");
 		testAdd(board,Constants.BLACK,0,0);
 		testAdd(board,Constants.WHITE,0,0);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,0);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,0);
 		testAdd(board,Constants.BLACK,0,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,0,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,Constants.BOARDHEIGHT-1);
 		Constants.print("");
 		Constants.print("Board State after tests:");
 		Constants.print(board);
 		Constants.print("");
 		Constants.print("Testing enforcement of Placement Rule 2...");
 		Constants.print("------------------------------------------");
 		Constants.print("\t The following tests");
 		Constants.print("\t should all throw");
 		Constants.print("\t exceptions.");
 		Constants.print("------------------------------------------");
 		testAdd(board,Constants.WHITE,1,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,2,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,3,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,4,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,5,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,6,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.WHITE,1,0);
 		testAdd(board,Constants.WHITE,2,0);
 		testAdd(board,Constants.WHITE,3,0);
 		testAdd(board,Constants.WHITE,4,0);
 		testAdd(board,Constants.WHITE,5,0);
 		testAdd(board,Constants.WHITE,6,0);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,1);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,2);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,3);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,4);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,5);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-1,6);
 		testAdd(board,Constants.BLACK,0,1);
 		testAdd(board,Constants.BLACK,0,2);
 		testAdd(board,Constants.BLACK,0,3);
 		testAdd(board,Constants.BLACK,0,4);
 		testAdd(board,Constants.BLACK,0,5);
 		testAdd(board,Constants.BLACK,0,6);
 		Constants.print(board);
 		Constants.print("");
 		Constants.print("");
 		Constants.print("------------------------------------------");
 		Constants.print("\t The following tests");
 		Constants.print("\t should not throw");
 		Constants.print("\t exceptions.");
 		Constants.print("------------------------------------------");
 		testAdd(board,Constants.BLACK,1,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,2,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,4,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,5,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,1,0);
 		testAdd(board,Constants.BLACK,2,0);
 		testAdd(board,Constants.BLACK,4,0);
 		testAdd(board,Constants.BLACK,5,0);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,1);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,2);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,4);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,5);
 		testAdd(board,Constants.WHITE,0,1);
 		testAdd(board,Constants.WHITE,0,2);
 		testAdd(board,Constants.WHITE,0,4);
 		testAdd(board,Constants.WHITE,0,5);
 		Constants.print(board);
 		Constants.print("");
 		Constants.print("");
 		Constants.print("Testing enforcement of Placement Rule 3...");
 		Constants.print("------------------------------------------");
 		Constants.print("\t The following tests");
 		Constants.print("\t should all throw");
 		Constants.print("\t exceptions.");
 		Constants.print("------------------------------------------");
 		testAdd(board,Constants.BLACK,1,0);
 		testAdd(board,Constants.BLACK,2,0);
 		testAdd(board,Constants.BLACK,4,0);
 		testAdd(board,Constants.BLACK,5,0);
 		Constants.print(board);
 		Constants.print("");
 		Constants.print("");
 		Constants.print("Testing enforcement of Placement Rule 4...");
 		Constants.print("------------------------------------------");
 		Constants.print("\t The following tests");
 		Constants.print("\t should all throw");
 		Constants.print("\t exceptions.");
 		Constants.print("------------------------------------------");
 		testAdd(board,Constants.BLACK,3,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,6,Constants.BOARDHEIGHT-1);
 		testAdd(board,Constants.BLACK,3,0);
 		testAdd(board,Constants.BLACK,6,0);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,3);
 		testAdd(board,Constants.WHITE,Constants.BOARDWIDTH-1,6);
 		testAdd(board,Constants.WHITE,0,3);
 		testAdd(board,Constants.WHITE,0,6);
 		Constants.print(board);
 		Constants.print("");
 		Constants.print("");
 		Constants.print("------------------------------------------");
 		Constants.print("\t The following tests");
 		Constants.print("\t should not throw");
 		Constants.print("\t exceptions.");
 		Constants.print("------------------------------------------");
 		testAdd(board,Constants.WHITE,2,Constants.BOARDHEIGHT-2);
 		testAdd(board,Constants.WHITE,4,Constants.BOARDHEIGHT-2);
 		testAdd(board,Constants.WHITE,2,1);
 		testAdd(board,Constants.WHITE,4,1);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-2,2);
 		testAdd(board,Constants.BLACK,Constants.BOARDWIDTH-2,4);
 		testAdd(board,Constants.BLACK,1,2);
 		testAdd(board,Constants.BLACK,1,4);
 		Constants.print(board);
 		Constants.print("");
 		Constants.print("");
 		Constants.print("Testing random placements...");
 		for (int x = 0; x < Constants.BOARDWIDTH; x++){
 			for (int y = 0; y < Constants.BOARDHEIGHT; y++){
 				testAdd(board,Constants.WHITE,x,y);
 			}
 		}
 		for (int x = 0; x < Constants.BOARDWIDTH; x++){
 			for (int y = 0; y < Constants.BOARDHEIGHT; y++){
 				testAdd(board,Constants.BLACK,x,y);
 			}
 		}
 		Constants.print(board);
 
 	}
 
 
 	//Util
 	private static void testAdd(Board b,int color, int x,int y){
 		String colorStr="";
 		if (color == Constants.BLACK){
 			colorStr="black";
 		}else if (color == Constants.WHITE){
 			colorStr="white";
 		}
 		Constants.print("Adding " + colorStr +" chip to ("+x+","+y+")");
 		try{
 			b.addChip(color,x,y);
 		}catch(InvalidMoveException e){
 			Constants.print(e);
 		}
 
 	}
 
 	private static String colorStr(int c){
 		if (c==Constants.BLACK){
 			return "black";
 		}else if (c==Constants.WHITE){
 			return "white";
 		}else{
 			return "null";
 		}
 	}
 }
