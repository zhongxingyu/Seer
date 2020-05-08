 package labs.brian.customviews;
 
 import java.util.Stack;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.util.SparseIntArray;
 import android.view.View;
 
 public class TetrisGrid extends View {
 
 	private static final int squaresWide = 10;
 	private static int squaresHeight = 22;//2 are not shown
 	private int [][] gameGrid;
 	private static long gameScore;
 	private static final int lineScore = 100;
 	private onEndGameListener listener;
 	
 	private static TetrisPiece piece;
 	private static final Point [] linePiece = {new Point(3,0),new Point(4,0),new Point(5,0),new Point(6,0)};
 	private static final Point [] lPiece = {new Point(3,0),new Point(4,0),new Point(5,0),new Point(3,1)};
 	private static final Point [] tPiece = {new Point(3,0),new Point(4,0),new Point(5,0),new Point(4,1)};
 	private static final Point [] zPiece = {new Point(3,0),new Point(4,0),new Point(4,1),new Point(5,1)};
 	private static final Point [] squarePiece = {new Point(3,0),new Point(4,0),new Point(3,1),new Point(4,1)};
 	private static enum PieceMovement {
 		TRANSLATE_LEFT,TRANSLATE_RIGHT,TRANSLATE_DOWN,ROTATE_COUNTERCLOCKWISE,ROTATE_CLOCKWISE
 	};
 	private static PieceMovement currentMovement = PieceMovement.TRANSLATE_DOWN;
 	private static int amtPieceMoves = 0;
 	
 	
 	private Paint linePaint,lPaint,tPaint,zPaint,squarePaint,borderPaint,emptyPaint,emptyBorderPaint;
 	private Paint alphaLinePaint,alphaLPaint,alphaTPaint,alphaZPaint,alphaSquarePaint;
 	private Rect squareLocation;
 	private int minSquareSideLength = 20;
 	
 	public TetrisGrid(Context context){
 		super(context);
 		init(context);
 	}
 	
 	public long getGameScore(){
 		return gameScore;
 	}
 	
 	public TetrisGrid(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 	
 	public TetrisGrid(Context context, AttributeSet attrs,int style){
 		super(context,attrs,style);
 		init(context);
 	}
 	
 	private void init(Context con){
 		setWillNotDraw(false);
 		gameGrid = new int[squaresWide][squaresHeight];
 		
 		linePaint = new Paint();
 		linePaint.setStyle(Style.FILL);
 		linePaint.setARGB(255, 20, 231, 213);
 		alphaLinePaint = new Paint();
 		alphaLinePaint.setStyle(Style.FILL);
 		alphaLinePaint.setARGB(125, 20, 231, 213);
 		
 		lPaint = new Paint();
 		lPaint.setStyle(Style.FILL);
 		lPaint.setARGB(255, 147, 28, 222);
 		alphaLPaint = new Paint();
 		alphaLPaint.setStyle(Style.FILL);
 		alphaLPaint.setARGB(125, 147, 28, 222);
 		
 		tPaint = new Paint();
 		tPaint.setStyle(Style.FILL);
 		tPaint.setARGB(255, 142, 132, 132);
 		alphaTPaint = new Paint();
 		alphaTPaint.setStyle(Style.FILL);
 		alphaTPaint.setARGB(125, 142, 132, 132);
 		
 		zPaint = new Paint();
 		zPaint.setStyle(Style.FILL);
 		zPaint.setARGB(255, 25, 236, 75);
 		alphaZPaint = new Paint();
 		alphaZPaint.setStyle(Style.FILL);
 		alphaZPaint.setARGB(125, 25, 236, 75);
 		
 		squarePaint = new Paint();
 		squarePaint.setStyle(Style.FILL);
 		squarePaint.setARGB(255, 242, 244, 43);
 		alphaSquarePaint = new Paint();
 		alphaSquarePaint.setStyle(Style.FILL);
 		alphaSquarePaint.setARGB(125, 242, 244, 43);
 		
 		borderPaint = new Paint();
 		borderPaint.setStyle(Style.STROKE);
 		borderPaint.setColor(Color.BLACK);
 		borderPaint.setStrokeWidth(0);
 		
 		emptyBorderPaint = new Paint();
 		emptyBorderPaint.setStyle(Style.STROKE);
 		emptyBorderPaint.setColor(Color.WHITE);
 		emptyBorderPaint.setStrokeWidth(0);
 		
 		emptyPaint = new Paint();
 		emptyPaint.setStyle(Style.FILL);
 		emptyPaint.setColor(Color.BLACK);
 		
 		squareLocation = new Rect();
 		createRandomPiece();
 	}
 	
 	@Override
 	public void onWindowFocusChanged (boolean hasFocus) {
 		minSquareSideLength = getMeasuredWidth()/squaresWide; 
 	}
 	
 	public void onDraw(Canvas canvas){
 		for(int y = 0;y<squaresHeight;y++){//draws board
 			for(int x = 0;x<squaresWide;x++){
 				squareLocation.left = x*minSquareSideLength;
 				squareLocation.top = y*minSquareSideLength;
 				squareLocation.right = (x+1)*minSquareSideLength;
 				squareLocation.bottom = (y+1)*minSquareSideLength;
 				switch(gameGrid[x][y]){
 					case 1:
 						canvas.drawRect(squareLocation, linePaint);
 						canvas.drawRect(squareLocation, borderPaint);
 						break;
 					case 2:
 						canvas.drawRect(squareLocation, lPaint);
 						canvas.drawRect(squareLocation, borderPaint);
 						break;
 					case 3:
 						canvas.drawRect(squareLocation, tPaint);
 						canvas.drawRect(squareLocation, borderPaint);
 						break;
 					case 4:
 						canvas.drawRect(squareLocation, zPaint);
 						canvas.drawRect(squareLocation, borderPaint);
 						break;
 					case 5:
 						canvas.drawRect(squareLocation, squarePaint);
 						canvas.drawRect(squareLocation, borderPaint);
 						break;
 					default:
 						canvas.drawRect(squareLocation, emptyPaint);
 						canvas.drawRect(squareLocation, emptyBorderPaint);
 						break;
 				}
 			}
 		}
 		
 		switch(piece.getColor()){//draws piece
 			case 1:
 				for(Point p : piece.getLocations()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, linePaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				for(Point p : getBottomLocation()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, alphaLinePaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				break;
 			case 2:
 				for(Point p : piece.getLocations()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, lPaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				for(Point p : getBottomLocation()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, alphaLPaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				break;
 			case 3:
 				for(Point p : piece.getLocations()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, tPaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				for(Point p : getBottomLocation()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, alphaTPaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				break;
 			case 4:
 				for(Point p : piece.getLocations()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, zPaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				for(Point p : getBottomLocation()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, alphaZPaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				break;
 			case 5:
 				for(Point p : piece.getLocations()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, squarePaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				for(Point p : getBottomLocation()){
 					applyBoardTranslation(p);
 					canvas.drawRect(squareLocation, alphaSquarePaint);
 					canvas.drawRect(squareLocation, borderPaint);
 				}
 				break;
 		}
 		super.onDraw(canvas);
 	}
 	
 	private void applyBoardTranslation(Point p){
 		squareLocation.left = p.x*minSquareSideLength;
 		squareLocation.top = p.y*minSquareSideLength;
 		squareLocation.right = (p.x+1)*minSquareSideLength;
 		squareLocation.bottom = (p.y+1)*minSquareSideLength;
 	}
 	
 	public void translatePiece(int x){
 		if(x == -1){
 			currentMovement = PieceMovement.TRANSLATE_LEFT;
 			performMove();
 		}else if(x == 1){
 			currentMovement = PieceMovement.TRANSLATE_RIGHT;
 			performMove();
 		}else{//go down one in the y direction
 			currentMovement = PieceMovement.TRANSLATE_DOWN;
 			performMove();
 		}
 	}
 	
 	public static final int ROTATE_CLOCKWISE = 1;
 	public static final int ROTATE_COUNTERCLOCKWISE = 2;
 	public void rotatePiece(int dir){
 		if(dir == ROTATE_CLOCKWISE){
 			currentMovement = PieceMovement.ROTATE_CLOCKWISE;
 			performMove();
 		}else if (dir == ROTATE_COUNTERCLOCKWISE){
 			currentMovement = PieceMovement.ROTATE_COUNTERCLOCKWISE;
 			performMove();
 		}
 	}
 	
 	public void sendCurrentPieceToBottom(){
 		Point [] tmp = getBottomLocation();
 		applyPieceToBoard(tmp);
 		performMove();
 	}
 	
 	private Point [] getBottomLocation(){
 		Point [] tmp = piece.getSquaresClone();
 		//find bottom most squares in piece
 		int highestY = 0;
 		SparseIntArray bottomMostPieces = new SparseIntArray(4);
 		for(Point p : tmp){
 			if(p.y > highestY)
 				highestY = p.y;
 			if(bottomMostPieces.indexOfKey(p.x) < 0)
 				bottomMostPieces.put(p.x, p.y);
 			else if (bottomMostPieces.get(p.x) < p.y)
 				bottomMostPieces.put(p.x, p.y);
 		}
 		
 		int distance = 40;
 		//find bottom most location for piece whether it be another piece or the bottom of the board
 		//start from bottom up
 		for(int i = 0;i < bottomMostPieces.size();i++){
 			int x = bottomMostPieces.keyAt(i);
 			int y = bottomMostPieces.get(x);
 			for(int gridYLocation = squaresHeight - 1;gridYLocation > y;gridYLocation --){
 				if(gameGrid[x][gridYLocation] != 0){
 					int newDistance = gridYLocation - y - 1;
 					if(distance > newDistance)
 						distance = newDistance;
 				}
 			}
 		}
 		if(distance == 40)
 			distance = squaresHeight - 1 - highestY;
 			
 		for(Point p : tmp){
 				p.y += distance;
 		}
 		return tmp;
 	}
 	
 	/**
 	 * returns true if a collision will happen on the next piece move, if true the piece should become null, one should check if any new points were earned
 	 * @return
 	 */
 	public void performMove(){
 		Point [] tmp;
 		switch(currentMovement){
 			case ROTATE_CLOCKWISE:
 				tmp = piece.preRotateClockWise();
 				if(!checkForCollision(tmp)){
 					piece.postRotateClockWise();
 					amtPieceMoves++;
 				}
 				break;
 			case ROTATE_COUNTERCLOCKWISE:
 				tmp = piece.preRotateCounterClockwise();
 				if(!checkForCollision(tmp)){
 					piece.postRotateCounterClockwise();
 					amtPieceMoves++;
 				}
 				break;
 			case TRANSLATE_DOWN:
 				tmp = piece.preTranslateDown();
 				if(checkNotAbleToGoDown(tmp)){
 					applyPieceToBoard(tmp);
 				}else{
 					piece.postTranslateDown();
 					amtPieceMoves++;
 				}
 				break;
 			case TRANSLATE_LEFT:
 				tmp = piece.preTranslateLeft();
 				if(!checkForCollision(tmp)){
 					piece.postTranslateLeft();
 					amtPieceMoves++;
 				}
 				break;
 			case TRANSLATE_RIGHT:
 				tmp = piece.preTranslateRight();
 				if(!checkForCollision(tmp)){
 					piece.postTranslateRight();
 					amtPieceMoves++;
 				}
 				break;
 		}
 		invalidate();
 		currentMovement = PieceMovement.TRANSLATE_DOWN;
 	}
 	
 	private boolean checkForCollision(Point [] points){
 		switch(currentMovement){
 			case ROTATE_CLOCKWISE:
 			case ROTATE_COUNTERCLOCKWISE:
 				for(Point p : points){
 					if(p.x < 0 || p.x >= squaresWide || gameGrid[p.x][p.y] != 0)
 						return true;
 				}
 				break;
 			case TRANSLATE_LEFT:
 				for(Point p : points)
 					if(p.x < 0 || gameGrid[p.x][p.y] != 0)
 						return true;
 				break;
 			case TRANSLATE_RIGHT:
 				for(Point p : points)
 					if(p.x == squaresWide || gameGrid[p.x][p.y] != 0)
 						return true;
 				break;
 		}
 		return false;
 	}
 	
 	private boolean checkNotAbleToGoDown(Point[] points){
 		for(Point p : points){
 			if((p.y + 1 >= squaresHeight) || gameGrid[p.x][p.y + 1] != 0 )
 				return true;
 		}
 		return false;
 	}
 	
 	private void applyPieceToBoard(Point [] points){
 		if(amtPieceMoves == 0){
 			onEndGame();
 			return;
 		}
 		
 		for(Point p : points){
 			gameGrid[p.x][p.y] = piece.getColor();
 		}
 		checkForPoints();
 		createRandomPiece();
 	}
 	
	
 	private void checkForPoints(){
 		int tmpScore = 0;
 		Stack<Integer> linesToDelete = new Stack<Integer>();
 		
 		for(int y = 0;y<squaresHeight;y++){
 			boolean line = true;
 			for(int x = 0;x<squaresWide;x++){
 				if(gameGrid[x][y] == 0){
 					line = false;
 					break;
 				}	
 			}
 			if(line)
 				linesToDelete.add(y);
 		}
 		tmpScore = lineScore * linesToDelete.size()*linesToDelete.size();
 		
 		if(tmpScore == 0)
 			return;
 		
 		int linesDeleted = 0;
 		int [][] tmpGrid = new int[squaresWide][squaresHeight];
		for(int y = squaresHeight - 1;y > linesToDelete.size();y--){
 			if(!linesToDelete.empty() && y == linesToDelete.peek()){
 				linesToDelete.pop();
 				linesDeleted ++;
 				continue;
 			}
 			for(int x = 0;x<squaresWide;x++){
 				tmpGrid[x][y] = gameGrid[x][y-linesDeleted];
 			}
 		}
 		
 		gameScore += tmpScore;
 		gameGrid = tmpGrid;
 		checkForPoints();
 	}
 	
 	private void onEndGame(){
 		listener.onEndGame();
 	}
 	
 	private void createRandomPiece(){
 		switch((int)(Math.random()*5)){
 		case 0:
 			piece = new TetrisPiece(linePiece);
 			piece.setColor(1);
 			break;
 		case 1:
 			piece = new TetrisPiece(lPiece);
 			piece.setColor(2);
 			break;
 		case 2:
 			piece = new TetrisPiece(tPiece);
 			piece.setColor(3);
 			break;
 		case 3:
 			piece = new TetrisPiece(zPiece);
 			piece.setColor(4);
 			break;
 		case 4:
 			piece = new TetrisPiece(squarePiece);
 			piece.setColor(5);
 			break;
 		}
 		amtPieceMoves = 0;
 	}
 	
 	private static class TetrisPiece{
 		private Point [] squareLocations;
 		private int pieceColor;
 		
 		public TetrisPiece(Point... points){
 			squareLocations = clone(points);
 		}
 		
 		private Point[] clone(Point[] toBeCloned){
 			Point[] tmp = new Point[toBeCloned.length];
 			for(int i=0;i<tmp.length;i++){
 				tmp[i] = new Point(toBeCloned[i].x,toBeCloned[i].y);
 			}
 			return tmp;
 		}
 		
 		public Point[] getSquaresClone(){
 			return clone(squareLocations);
 		}
 		
 		public void setColor(int color){
 			pieceColor = color;
 		}
 		
 		public int getColor(){
 			return pieceColor;
 		}
 		
 		public Point [] getLocations(){
 			return squareLocations;
 		}
 		
 		public void postRotateClockWise(){
 			rotateClockWise(squareLocations);
 		}
 		
 		public Point[] preRotateClockWise(){
 			return rotateClockWise(clone(squareLocations));
 		}
 		
 		private Point[] rotateClockWise(Point[] points){
 			Matrix TT = new Matrix();
 			TT.postRotate(90, points[0].x, points[0].y);
 			float[] pts = new float [points.length*2];
 			for(int i = 0;i<pts.length;i+=2){
 				pts[i] = points[i/2].x;
 				pts[i+1] = points[i/2].y;
 			}
 			TT.mapPoints(pts);
 			for(int i = 0;i<pts.length;i+=2){
 				points[i/2].x = (int) pts[i];
 				points[i/2].y = (int) pts[i+1];
 			}
 			return points;
 		}
 		
 		public void postRotateCounterClockwise(){
 			rotateCounterClockWise(squareLocations);
 		}
 		
 		public Point[] preRotateCounterClockwise(){
 			return rotateCounterClockWise(clone(squareLocations));
 		}
 		
 		private Point[] rotateCounterClockWise(Point[] points){
 			Matrix TT = new Matrix();
 			TT.postRotate(-90, points[0].x, points[0].y);
 			float[] pts = new float [points.length*2];
 			for(int i = 0;i<pts.length;i+=2){
 				pts[i] = points[i/2].x;
 				pts[i+1] = points[i/2].y;
 			}
 			TT.mapPoints(pts);
 			for(int i = 0;i<pts.length;i+=2){
 				points[i/2].x = (int) pts[i];
 				points[i/2].y = (int) pts[i+1];
 			}
 			return points;
 		}
 		
 		public void postTranslateLeft(){
 			translateLeft(squareLocations);
 		}
 		
 		public Point[] preTranslateLeft(){
 			return translateLeft(clone(squareLocations));
 		}
 		
 		private Point[] translateLeft(Point[] points){
 			for(Point p : points){
 				p.x -= 1;
 			}
 			return points;
 		}
 		
 		public void postTranslateDown(){
 			translateDown(squareLocations);
 		}
 		
 		public Point[] preTranslateDown(){
 			
 			return translateDown(clone(squareLocations));
 		}
 		
 		private Point[] translateDown(Point[] points){
 			for(Point p : points){
 				p.y += 1;
 			}
 			return points;
 		}
 		
 		public void postTranslateRight(){
 			translateRight(squareLocations);
 		}
 		
 		public Point[] preTranslateRight(){
 			return translateRight(clone(squareLocations));
 		}
 		
 		private Point[] translateRight(Point[] points){
 			for(Point p : points){
 				p.x += 1;
 			}
 			return points;
 		}
 	}
 
 	public interface onEndGameListener{
 		public void onEndGame();
 	}
 	
 	public void setOnEndGameListener(onEndGameListener listener){
 		this.listener = listener;
 	}
 }
