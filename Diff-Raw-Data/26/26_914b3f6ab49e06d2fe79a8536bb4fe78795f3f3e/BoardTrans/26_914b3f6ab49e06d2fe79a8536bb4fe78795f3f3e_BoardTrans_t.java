 /*
  * First assignment for the Path Planning part of the Robotics practical.
  *
  * You'll be introduced to the problem of planning a path for the 
  * gripper of the robot arm. You will write some functions which are essential
  * for this task.
  *
  * The assignment consists of three parts:
  * - access some data of the board to get acquainted with it
  * - write a class to convert positions ("e3") to locations (column 4, row 2)
  * - write a function convert locations to cartesian coordinates (x,y,z)
  *
  * Just start reading the comments and fill in the ???? stuff. The latter two
  * parts of the assignment and the data are contained in separate class called
  * StudentBoardTrans, check the bottom of this file.
  * A pointer to documentation can be found in
  * /opt/stud/robotics/hints/DOCUMENTATION .
  *
  * You can test your answers yourself, if they are correct ask one of the
  * practical assistents to verify them.
  *
  * This Java introduction was written for the 2001/2002 course.
  * Matthijs Spaan <mtjspaan@science.uva.nl>
  * $Id: Week1.java,v 1.9 2008/06/10 10:21:36 obooij Exp $
  */
 
 import java.io.*;
 import java.lang.*;
 
 class BoardTrans
 {
   /*
    * BoardTrans takes one optional argument, specifying the position on the field
    * it should use. It defaults to b7.
    */
   public static void main(String[] args)
   {
     String position;
 
     try { position=args[0]; }
     catch(ArrayIndexOutOfBoundsException e) { position="b7"; }
 
     StudentBoardTrans boardTrans = new StudentBoardTrans(position);
 
     // set up the board in starting position
     boardTrans.board.setup();
     
     // draw the board state
     boardTrans.board.print();
     
     /*
      * You are now asked to access some data in the board structure.
      * Please print them and check your answers with the chess_board eitor
      * and the chess_piece editor from SCIL.
      */
     try {  
       System.out.println("The dimensions of the squares on the board are " +
          // ????
 boardTrans.getX() +
          " by " + 
 boardTrans.getY() +
          // ????
          "mm");
   
       System.out.println("The x,y coordinates of the board are " + boardTrans.getCoordX() +
          // ????
          "," + boardTrans.getCoordY()
          // ????
          );
   
       System.out.println("The height of the piece at " + boardTrans.pos + " is " + boardTrans.getHeight() +
          // ????
          " mm");
     
       System.out.println("The color of the piece at " + boardTrans.pos + " is " + boardTrans.getSide()
              // ????
              );
     } catch (Exception e) {
       System.out.println(e);
       System.exit(1);
     }
     
     /*
      * Next you should write a small class called BoardLocation which
      * converts a position like "a1" to a column and a row.
      * Finish the class BoardLocation in StudentBoardTrans started below.
      */
         
     StudentBoardTrans.BoardLocation location = boardTrans.boardLocation;
     BoardLocation realLocation = new BoardLocation(boardTrans.pos);
 
     System.out.println("You think position " + boardTrans.pos + " is at (" +
                        location.column + "," + location.row +
                        "), the correct answer is (" + realLocation.column +
                        "," + realLocation.row + ")");
 
     /*
      * In order to be able to plan a path to certain position on the board
      * you have to know where this position is in Cartesian (i.d. real world)
      * coordinates: (x,y,z) in mm relative to the origin.
      * Look at the picture of the chess board in the practical manual.
      * Finish the method toCartesian() in StudentBoardTrans started below.
      */
 
     Point cartesian = new Point();
     cartesian=boardTrans.toCartesian(location.column, location.row);
 
 //System.out.println(location.column +" "+ location.row);
     System.out.println("You think " + boardTrans.pos + " is at " + cartesian +
                        ", the correct answer is " +
                        boardTrans.board.toCartesian(boardTrans.pos));
 
     // Let's turn the board 45 degrees
     boardTrans.board.theta=45;
 
     // recalculate cartesian
     cartesian=boardTrans.toCartesian(location.column, location.row);
 
     System.out.println("You think " + boardTrans.pos + " is at " + cartesian +
                        ", the correct answer is " +
                        boardTrans.board.toCartesian(boardTrans.pos));
 
     // Let's move the position of the board and turn it again
     boardTrans.board.coords.x=100;
     boardTrans.board.coords.y=200;
     boardTrans.board.theta=-60;
 
     // recalculate cartesian
     cartesian=boardTrans.toCartesian(location.column, location.row);
 
     System.out.println("You think " + boardTrans.pos + " is at " + cartesian +
                        ", the correct answer is " +
                        boardTrans.board.toCartesian(boardTrans.pos));
   }
 }
 
 class StudentBoardTrans
 {
   public ChessBoard board; // our board
   public String pos; // the position we're going to examine
   public BoardLocation boardLocation;
 
   public StudentBoardTrans(String position)
   {
     board = new ChessBoard();
     pos = position;
     boardLocation = new BoardLocation(position);
   }
 
 	public double getX() {
 		return this.board.delta_x;
 	}
 	public double getY() {
 		return this.board.delta_y;
 	}
 	public double getHeight() {
 		double height;
 		try {
 			height = this.board.getHeight(this.pos);
 		} catch(ChessBoard.NoPieceAtPositionException e) {
 			height = 0.0;
 		}
 		return height;
 	}
 
 	double getCoordX() {
 		return this.board.coords.x;
 	}
 
 	double getCoordY() {
 		return this.board.coords.y;
 	}
 
 	double getCoordZ() {
 		return this.board.board_thickness;
 	}
 
 	Point getCoords() {
 		return this.board.coords;
 	}
 
 	String getSide() {
 		String side;
 		try {
 			side = this.board.getSide(this.pos);
 		} catch(ChessBoard.NoPieceAtPositionException e) {
 			side = "none";
 		}
 		return side;
 	}
   
 	public Point toCartesian(int column, int row)
   {
     // write this function
 
     /* You can ignore the normal of the board, i.e. we assume the chess
      * board always lies flat on the table.
      */
 
     /*Point result = new Point ();
     
     result.x = 23; // ????
     result.y = 23; // ????
     result.z = 23; // ????
     */
 /*double[][] m = { { 5.0, 0.0, 0.0 },
 			 { 0.0, 5.0, 0.0 },
 			 { 0.0, 0.0, 1.0} };
 	double t = 90.0;
 	double[][] r_z = { { Math.cos(t), -Math.sin(t), 0.0 },
 			 { Math.sin(t), Math.cos(t), 0.0 },
 			 { 0.0, 0.0, 1.0} };
 	Matrix xyz = new Matrix(m);
 	Matrix rot = new Matrix(r_z);
 	Matrix rotated_xyz = xyz.times(rot);
 	rotated_xyz.show();*/
 	//System.out.println(this.board.toCartesian(row, column));
	double x = /*this.getCoordX() -*/ this.board.sur_x + ((7-row) * this.board.delta_x) + (this.board.delta_x / 2); 
	double y = /*this.getCoordY() +*/ this.board.sur_y + ((7-column) * this.board.delta_y) + (this.board.delta_y /2);
 //System.out.println(this.getCoordX()+"	- "+this.board.sur_x+"	- ("+row+" * "+this.board.delta_x+") - "+" ("+this.board.delta_x+" / "+2+")	");
 //System.out.println(this.getCoordY()+"	+ "+this.board.sur_y+"	+ ("+column+" * "+this.board.delta_y+") + "+" ("+this.board.delta_y+" / "+2+")	"); 
 double[][] m = {	{ x },
 			{ y  },
 			{ 0.0 }};
 	double t = Math.toRadians(this.board.theta);
	double[][] r_z = { { Math.cos(t), -Math.sin(t), 0.0 },
			 { Math.sin(t), Math.cos(t), 0.0 },
 			 { 0.0, 0.0, 1.0} };
 	Matrix xyz = new Matrix(m);
 	Matrix rot = new Matrix(r_z);
	Matrix rotated_xyz = rot.times(xyz);
 	//rotated_xyz.show();
 	//System.out.println("\nTESTING X/Y	:	"+boardLocation.row+"	"+boardLocation.column+"\n");
 	//System.out.println("\nCartesian String	:	"+this.board.toCartesian(this.pos));
 	//System.out.println("\nCartesian Coords	:	"+this.board.toCartesian(boardLocation.row,boardLocation.column));
     	Point xyz_point = new Point();
	xyz_point.x = (-1) * rotated_xyz.data[0][0] + this.getCoordX();
	xyz_point.y = rotated_xyz.data[1][0]+this.getCoordY();
	xyz_point.z = rotated_xyz.data[2][0]+this.getCoordZ();
 //xyz_point.x = 23;
 //	xyz_point.y = 23;
 //	xyz_point.z = 23;
 	return(xyz_point);
   }
 
   class BoardLocation{
     public int row;
     public int column;
       
 	public BoardLocation() {
 		// write this function. Compute the row and column correspoding to String pos.
 
 		//row=23; // ????
 		//column=23; // ????
 	}
 	public BoardLocation(int column, int row) {
     		this.row = row;
 		this.column = column;
 	}
 	public BoardLocation(String loc) {
 		this.column = this.getCol(loc.charAt(0));
 		this.row = this.getRow(loc.charAt(1));
 	}
 
 	public int getCol(char col) {
 		String columns = "abcdefgh";
 		return columns.indexOf(col);
   	}
 	public int getRow(char row) {
 		return Integer.parseInt(String.valueOf(row))-1;
 	}
 }
 }
