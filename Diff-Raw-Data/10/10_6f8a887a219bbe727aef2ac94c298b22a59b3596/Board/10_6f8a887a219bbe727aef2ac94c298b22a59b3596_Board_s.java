 import java.awt.Point;
 
 public class Board{
 
 	public boolean doesFit(Piece p, Point location){
		if(getPiece(location) == null){		
 			if((getTop(location)== null||p.getTop()+getTop(location).getBottom()==0)&&
 					(getRight(location)== null||p.getRight()+getRight(location).getLeft()==0) &&
 					(getBottom(location)== null||p.getBottom()+getBottom(location).getTop()==0) &&
 					(getLeft(location)== null||p.getLeft()+getLeft(location).getRight()==0)){
 				return true;
 
 			}
 			else return false;
 		}
 		else return false;
 	}
 	private Piece getTop(Point p){
 		if(isValid(new Point((int) p.x,(int) p.y-1))){
 			return board[(int) p.x][(int) p.y-1];
 		}
 		else return null;
 	}
 	private Piece getRight(Point p){
 		if(isValid(new Point((int) p.x+1,(int) p.y))){
 			return board[(int) p.x+1][(int) p.y];
 		}
 		else return null;
 	}
 	private Piece getBottom(Point p){
 		if(isValid(new Point((int) p.x,(int) p.y+1))){
 			return board[(int) p.x][(int) p.y+1];
 		}
 		else return null;
 	}
 	private Piece getLeft(Point p){
 		if(isValid(new Point((int) p.x-1,(int) p.y))){
 			return board[(int) p.x-1][(int) p.y];
 		}
 		else return null;
 	}
 
 	public Piece getPiece(Point p){
 		if(isValid(p)){
 			return board[(int) p.x][(int) p.y];
 		}
 		else return null;
 	}
 
 	public boolean isValid(Point p){
 		if(p.getX()<0||p.getY()<0||p.getX()>=xDim||p.getY()>=yDim){
 			return false;
 		}
 		else return true;
 	}
 
 	public boolean isOccupied(Point p){
 		if(getPiece(p)==null){
 			return false;
 		}
 		else return true;
 	}
 	
 	public int getRows(){
 		return xDim;
 	}
 	public int getCols(){
 		return yDim;
 	}
 	public boolean setPiece(Piece p, Point location){
 		if(getPiece(location)==null && doesFit(p,location)){
 			board[(int) location.getX()][(int) location.getY()] = p;
 			return true;
 		}
 		else return false;
 	}
 	public void clear(){
 		for(int i = 0;i<=xDim;i++){
 			for(int j = 0;j<=yDim;j++){
 				setPiece(null,new Point(i,j));
 			}
 		}
 	}
 	
 	public String toString(){
 		String s = "";
 		for(int i=0;i<xDim;i++){
 			for(int j=0;j<yDim;j++){
 				if(getPiece(new Point(i,j))==null){
 					s+="[      ]";
 				}
 				else{
 					s+=getPiece(new Point(i,j)).toString();
 				}
 			}
 			s+="\n";
 		}
 		return s;
 	}
 
 	public Board(){
 		this(3,3);
 	}
 
 	public Board(int xDim, int yDim){
 		this.xDim=xDim;
 		this.yDim=yDim;
 		board = new Piece[xDim][yDim];
 	}
 
 	public static final Piece[] PIECES = {  new Piece(Piece.CLUB_OUT, Piece.DIAMOND_IN, Piece.CLUB_IN, Piece.HEART_OUT),
 		new Piece(Piece.SPADE_OUT, Piece.SPADE_IN, Piece.HEART_IN, Piece.DIAMOND_OUT),
 		new Piece(Piece.HEART_OUT, Piece.SPADE_IN, Piece.CLUB_IN, Piece.SPADE_OUT),
 		new Piece(Piece.HEART_OUT, Piece.CLUB_IN, Piece.CLUB_IN, Piece.DIAMOND_OUT),
 		new Piece(Piece.SPADE_OUT, Piece.HEART_IN, Piece.CLUB_IN, Piece.SPADE_OUT),
 		new Piece(Piece.HEART_OUT, Piece.DIAMOND_IN, Piece.HEART_IN, Piece.DIAMOND_OUT),
 		new Piece(Piece.SPADE_OUT, Piece.HEART_IN, Piece.DIAMOND_IN, Piece.DIAMOND_OUT),
 		new Piece(Piece.CLUB_OUT, Piece.SPADE_IN, Piece.HEART_IN, Piece.HEART_OUT),
 		new Piece(Piece.CLUB_OUT, Piece.DIAMOND_IN, Piece.DIAMOND_OUT, Piece.CLUB_IN)};
 	private Piece[][] board = new Piece[0][0];
 	private int xDim;
 	private int yDim;
 }
