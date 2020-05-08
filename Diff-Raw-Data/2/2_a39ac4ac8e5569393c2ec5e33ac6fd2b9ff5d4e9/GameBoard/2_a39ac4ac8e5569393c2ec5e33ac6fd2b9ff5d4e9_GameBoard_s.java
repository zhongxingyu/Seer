 package com.example.holoreversi.model;
 
 import java.util.ArrayList;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 //import com.example.holoreversiEngine.move;
 //import com.example.holoreversiEngine.Board.TKind;
 
 public class GameBoard implements Board,Parcelable {
 
 	private int scoreWhite;
 	private int scoreBlack;
 	private int boardSize;
 	private Cell tiles[][] = null;
 	//private ArrayList<ArrayList<Cell>> stepChanges;
 	private ArrayList<Cell> stepChanges;
 	private ArrayList<Callback> listenrs;
 	private int step;
 
 	public GameBoard(int size)
 	{
 		stepChanges = new ArrayList<Cell>();
 		listenrs = new ArrayList<Board.Callback>();
 		boardSize = size;
 		resetBoard();
 	}
 	
     public GameBoard(Parcel in) {
 		stepChanges = new ArrayList<Cell>();
 		listenrs = new ArrayList<Board.Callback>();
     	boardSize = in.readInt();
     	tiles = new Cell[boardSize][boardSize];
 		for (int i=0;i<boardSize;i++) {
 			for (int j=0;j<boardSize;j++) {
 				tiles[i][j] = new Cell(i, j);
 				tiles[i][j].contents = in.readInt();
 				// it might be more correct to write and read entire arrays but i;m not sure how to test it
 				
 			}
 		}
 		step = in.readInt();
 		int size = in.readInt();
 		stepChanges = new ArrayList<Cell>();
 		for(int i=0;i<size;i++)
 		{
 			stepChanges.add(new Cell(in));
 		}
 		calculateScore();
 		
 	}
 
 	public static final Parcelable.Creator<GameBoard> CREATOR
     	= new Parcelable.Creator<GameBoard>() {
     		public GameBoard createFromParcel(Parcel in) {
     			return new GameBoard(in);
     			}
     		public GameBoard[] newArray(int size) {
     			return null; // i think it is correct since there can only be one board
     		}
     };
     public void resetBoard()
     {
 		stepChanges.clear();
     	tiles = new Cell[boardSize][boardSize];
 		for (int i=0;i<boardSize;i++) {
 			for (int j=0;j<boardSize;j++) {
 				tiles[i][j] = new Cell(i, j);
 			}
 		}
 		tiles[boardSize/2-1][boardSize/2].contents=BLACK;
 		tiles[boardSize/2][boardSize/2-1].contents=BLACK;
 		tiles[boardSize/2-1][boardSize/2-1].contents=WHITE;
 		tiles[boardSize/2][boardSize/2].contents=WHITE;
 		calculateScore();
 		step = 0;
     }
 	private void calculateScore() {
 		scoreBlack = 0;
 		scoreWhite = 0;
 		for (Cell[] c : tiles) {
 			for (Cell single : c) {
 				switch (single.contents) {
 				case BLACK:
 					scoreBlack++;
 					break;
 				case WHITE:
 					scoreWhite++;
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	}
 	private ArrayList<Cell> getEmpty()
 	{
 		ArrayList<Cell> arr = new ArrayList<Cell>();
 		for (Cell[] c : tiles) {
 			for (Cell single : c) {
 				if(single.contents == EMPTY)
 					arr.add(single);
 			}
 		}
 		return arr;
 	}
 	@Override
 	public ArrayList<Cell> getAllowedMoves() {
 		int kind = currentPlayer();
 		if (kind == BLACK) {
 			return getAllowedCells(BLACK);
 		}
 		return getAllowedCells(WHITE);
 	}
 
 	private ArrayList<Cell> getAllowedCells(int kind)
 	{
 		ArrayList<Cell> arr = new ArrayList<Cell>();
 		ArrayList<Cell> emptyCells = getEmpty();
 		for (Cell cell : emptyCells) {
 			if(isValid(cell, kind))
 				arr.add(cell);
 		}
 		return arr;
 	}
 	
 	@Override
 	public void addCallbackListener(Callback callback) {
 		listenrs.add(callback);
 	}
 
 	@Override
 	public int getSize() {
 		return boardSize;
 	}
 
 	@Override
 	public int getScoreWhite() {
 		return scoreWhite;
 	}
 
 	@Override
 	public int getScoreBlack() {
 		return scoreBlack;
 	}
 	private int checkCell(int x,int y, int incx, int incy, int kind , boolean set)  {
 		// totally based with limited understanding on reversi.java.net
 		int opponent;
 		if (kind == BLACK) opponent=WHITE; else opponent=BLACK;
 		int n_inc=0;
 		x+=incx; y+=incy;
 		while ((x<boardSize) && (x>=0) && (y<boardSize) && (y>=0) && (tiles[x][y].contents==opponent)) {
 			x+=incx; y+=incy;
 			n_inc++;
 		}
 		if ((n_inc != 0) && (x<boardSize) && (x>=0) && (y<boardSize) && (y>=0) && (tiles[x][y].contents==kind)) {
 			 if (set)
 			 for (int j = 1 ; j <= n_inc ; j++) {
 				x-=incx; y-=incy;
 				updateTile(x, y,kind);
 			 }
 			return n_inc;
 		}
 		else return 0;
 	}
 	private boolean isValid(Cell cell, int kind) {
 		// check increasing x 
 		if (checkCell(cell.x,cell.y,1,0,kind,false) != 0) return true;
 		// check decreasing x 
 		if (checkCell(cell.x,cell.y,-1,0,kind,false) != 0) return true;
 		// check increasing y 
 		if (checkCell(cell.x,cell.y,0,1,kind,false) != 0) return true;
 		// check decreasing y 
 		if (checkCell(cell.x,cell.y,0,-1,kind,false) != 0) return true;
 		// check diagonals 
 		if (checkCell(cell.x,cell.y,1,1,kind,false) != 0) return true;
 		if (checkCell(cell.x,cell.y,-1,1,kind,false) != 0) return true;
 		if (checkCell(cell.x,cell.y,1,-1,kind,false) != 0) return true;
 		if (checkCell(cell.x,cell.y,-1,-1,kind,false) != 0) return true;
 		return false;
 	}
 	private int move(int x,int y, int kind)
 	{
 		// check increasing x
 		int j=checkCell(x,y, 1,0,kind,true);
 		// check decreasing x
 		j+=checkCell(x,y, -1,0,kind,true);
 		// check increasing y
 		j+=checkCell(x,y, 0,1,kind,true);
 		// check decreasing y
 		j+=checkCell(x,y, 0,-1,kind,true);
 		// check diagonals
 		j+=checkCell(x,y, 1,1,kind,true);
 		j+=checkCell(x,y, -1,1,kind,true);
 		j+=checkCell(x,y, 1,-1,kind,true);
 		j+=checkCell(x,y, -1,-1,kind,true);
 		if (j != 0)  {
 			updateTile(x, y, kind);
 		}
 		return j;
 	}
 	
 	public boolean undoMove()
 	{	
 		if(step == 0)
 			return false;
 		if(stepChanges.size() == 0)
 			return false;
 		step--;
 		for (Cell cell : stepChanges) {
 			tiles[cell.x][cell.y].contents = cell.contents; 
 		}
 		calculateScore();
 		stepChanges.clear();
 		notifyCellUpdate();
 		return true;
 	}
 	
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		// not so sure what should be in here
 		return 0;
 	}
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(boardSize);
 		for (Cell[] row : tiles) {
 			for(Cell cell : row) {
 				dest.writeInt(cell.contents);
 			}
 		}
 		dest.writeInt(step);
 		dest.writeInt(stepChanges.size());
 		for (Cell cell : stepChanges) {
 			cell.writeToParcel(dest, flags);
 		}
 	}
 	
 	@Override
 	public int currentPlayer()
 	{
 		if(step % 2 == 0)
 			return BLACK;
 		return WHITE;
 	}
 
 	@Override
 	public Cell[][] getAll() {
 		return tiles;
 	}
 
 	@Override
 	public void move(Cell cell) {
 		int kind = currentPlayer();
 		stepChanges.clear();
 		int changed = move(cell.x,cell.y,kind);
 		if (changed == 0) {
 			return;
 		}
 		calculateScore();
 		step++;
 		if(getAllowedMoves().size() == 0)
 			step++;
 		
 		notifyCellUpdate();
 	}
 	
 
 	private void updateTile(int x, int y,int kind)
 	{
 		Cell temp = new Cell(x, y);
 		temp.contents = tiles[x][y].contents;
 		stepChanges.add(temp);
 		tiles[x][y].contents = kind;
 	}
 	
 	private void notifyCellUpdate()
 	{
 		for (Callback callback : listenrs) {
 			callback.onBoardUpdate(this);
 		}
 	}
 	@Override
 	public boolean isGameEnded()
 	{
 		if(scoreBlack+scoreWhite == boardSize*boardSize)
 			return true;
 		if(getAllowedMoves().size() == 0)
 			return true;
 		return false;
 	}
 	@Override
 	public int winner()
 	{
 		if(!isGameEnded())
 		{
 			return -1;
 		}
 		if(scoreBlack > scoreWhite){
 			return BLACK;
 		}
 		else if (scoreWhite > scoreBlack) {
 			return WHITE;
 		}
 		return EMPTY;
 	}
 }
