 package games.distetris.presentation;
 
 import games.distetris.domain.CtrlDomain;
import games.distetris.domain.CtrlGame;
 import games.distetris.domain.Data;
 import games.distetris.domain.Listener;
 import games.distetris.domain.Piece;
 import games.distetris.domain.PieceConstants;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.Log;
 import android.view.View;
 
 public class GameView extends View implements Listener {
 	private final int SQSIZE = 20;
 	private final int PADDING = 5;
 	
 	private Paint fillpaint;
 	private Paint strokepaint;
 	private CtrlDomain dc;
 	private int boardw;
 	private int boardh;
 	public boolean gameover = false;
 	public ArrayList<Integer> deletelines = null;
 	
 	
 	public GameView(Context context) {
 		super(context);
 		fillpaint = new Paint();
 		strokepaint = new Paint();
 		strokepaint.setAntiAlias(true);
 		strokepaint.setStrokeWidth(2);
 		strokepaint.setStrokeCap(Paint.Cap.BUTT);
 		strokepaint.setStrokeJoin(Paint.Join.ROUND);
 		strokepaint.setStyle(Paint.Style.STROKE);
 		fillpaint.setStyle(Paint.Style.FILL);
 		
 		dc = CtrlDomain.getInstance();
 		
 		int[][] board = dc.getBoard();
 		boardw = board[0].length;
 		boardh = board.length;
 		
 	}
 
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		super.onDraw(canvas);
 		
 		int[][] board = dc.getBoard();
 		
 		drawSquarePlayerZoneBackground(canvas,board);
 		if(this.deletelines != null) {
 			deleteLinesAnim();
 		}
 		drawBoard(canvas,board);
 		drawGameBackground(canvas);
 		drawInfoZone(canvas,board,dc.getNextPiece());
 		drawPiece(canvas);
 		
 		if(gameover) drawGameOverLayer(canvas);
 	}
 
 	private void deleteLinesAnim() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Draw the GameOver Screen
 	 * @param canvas
 	 */
 	private void drawGameOverLayer(Canvas canvas) {
 		int top = 0;
 		int left = 0;
 		int bottom = getHeight();
 		int right = getWidth();
 		
 		fillpaint.setColor(getResources().getColor(R.color.GAMEOVERSCREEN));
 		canvas.drawRect(left, top, right, bottom, fillpaint);
 		
 		strokepaint.setColor(Color.WHITE);
 		strokepaint.setTextSize(12);
 		canvas.drawText("GAME OVER", left + right/3, top + bottom/2, strokepaint);
 	}
 
 	
 	/**
 	 * Draws a piece in its location
 	 * @param canvas
 	 */
 	private void drawPiece(Canvas canvas) {
 		Piece p 			= dc.getCurrentPiece();
 		int psize 			= PieceConstants.PIECESIZE;
 		
 		strokepaint.setColor(getResources().getColor(R.color.PIECESTROKE));
 		strokepaint.setStrokeWidth(1);
 		fillpaint.setColor(p.color);
 		
 		for(int r = p.x,pr = 0;r<p.x+psize;r++,pr++){
 			for(int c = p.y,pc = 0;c<p.y+psize;c++,pc++){
 				if(p.getBlockType(pr, pc)!=PieceConstants.FREEBLOCK) drawSquare(canvas,r,c);
 			}
 		}
 
 		
 	}
 
 	/**
 	 * Draws a piece or board square in screen at the correct position
 	 * @param canvas
 	 */
 	private void drawSquare(Canvas canvas,int r,int c){
 		int linezero = getHeight() - boardh*SQSIZE - SQSIZE;
 		int left = c*SQSIZE;
 		int top = linezero + r*SQSIZE;
 		int right = c*SQSIZE+SQSIZE;
 		int bottom = linezero + r*SQSIZE + SQSIZE;
 		
 		canvas.drawRect(left, top, right, bottom, fillpaint);
 		canvas.drawRect(left, top, right, bottom, strokepaint);
 	}
 	
 	/**
 	 * Draws All the board
 	 * @param canvas
 	 * @param board
 	 */
 	private void drawBoard(Canvas canvas, int[][] board) {
 		
 		strokepaint.setColor(getResources().getColor(R.color.PIECESTROKE));
 		strokepaint.setStrokeWidth(1);
 		
 		// int linezero = getHeight() - boardh*SQSIZE - SQSIZE;
 		
 		//the board is draw from left bottom to right top
 		for(int r=0;r<boardh;r++){
 			for(int c=0;c<boardw;c++){
 				//if it's null there's nothing to draw
 				if(board[r][c] != 0){
 					fillpaint.setColor(board[r][c]);
 					drawSquare(canvas,r,c);
 				}
 			}
 		}		
 	}
 
 	/**
 	 * Draws the Info zone of the game, containing
 	 *  - Next piece
 	 *  - Player's colors and score
 	 * @param canvas
 	 * @param board
 	 */
 	private void drawInfoZone(Canvas canvas, int[][] board,Piece p) {
 
 		int zone_start_left = boardw*SQSIZE + PADDING;
 		int zone_start_top = getHeight() - boardh*SQSIZE - SQSIZE;
 		int zone_start_right = getWidth() - PADDING;
 		// int zone_start_bottom = getHeight();
 		
 		strokepaint.setColor(getResources().getColor(R.color.INFOSQSTROKE));
 		strokepaint.setStrokeWidth(3);
 		
 		int piecesq_left = zone_start_left;
 		int piecesq_top = zone_start_top;
 		int piecesq_right = zone_start_right;
 		int piecesq_bottom = zone_start_top + 2*PADDING + 4*SQSIZE;
 		
 		RectF piecesq = new RectF(piecesq_left,piecesq_top,piecesq_right,piecesq_bottom);
 		canvas.drawRoundRect(piecesq, 10, 10, strokepaint);
 		drawNextPiece(piecesq,canvas,p);
 		
 		drawScores(canvas, piecesq_bottom + 2*PADDING , zone_start_left, piecesq_right);
 	}
 
 	/**
 	 * Draw the scores on screen
 	 * @param canvas
 	 * @param top top position where toTimerTask example start drawing
 	 * @param left left position where to start drawing
 	 */
 	private void drawScores(Canvas canvas, int top, int left, int right) {
 		HashMap<String,Data> playerData = dc.getPlayers();
 		Vector<Vector<String>> names = new Vector<Vector<String>>();
 		
		for (int i=0; i<playerData.size(); i++) {
 			names.add(new Vector<String>());
 		}
 		
 		for (Entry<String, Data> player : playerData.entrySet()) {
 			// FIXME: IndexOutOfBoundsException
 			names.get(player.getValue().getTeam()).addElement(player.getKey());
 		}
 		
 		for (int i=0; i<names.size(); i++) {
 			if (names.get(i).isEmpty()) { continue; }
 			Vector<String> vs = names.get(i);
 			
 			strokepaint.setColor(getResources().getColor(R.color.INFOSQSTROKE));
 			strokepaint.setStrokeWidth(3);
 			
 			RectF piecesq = new RectF(left, top, right, top + 3*PADDING*(vs.size()+1)+PADDING);
 			canvas.drawRoundRect(piecesq, 10, 10, strokepaint);
 
 			int score = 0;
 			strokepaint.setStrokeWidth(1);
 			
 			for (int j=0; j<vs.size(); j++) {
 				top = top + 3*PADDING;
 				strokepaint.setColor(playerData.get(vs.get(j).toString()).getColor());
 				canvas.drawText(vs.get(j).toString(), left+PADDING, top, strokepaint);
 				canvas.drawText(String.valueOf(playerData.get(vs.get(j).toString()).getScore()), left+80, top, strokepaint);
 				score += playerData.get(vs.get(j).toString()).getScore();
 			}
 			
 			top = top + 3*PADDING;
 			strokepaint.setColor(Color.DKGRAY);
 			strokepaint.setStrokeWidth(1);
 			canvas.drawText(String.valueOf(score), left+70, top, strokepaint);
 			
 			top = top + 2*PADDING;
 		}
 	}
 
 	/**
 	 * Draws the next piece inside a Rectangle
 	 * @param piecesq
 	 * @param canvas
 	 * @param p
 	 */
 	private void drawNextPiece(RectF piecesq, Canvas canvas, Piece p) {
 		int left  = (int) piecesq.left;
 		int top = (int) piecesq.top;
 		int psize = PieceConstants.PIECESIZE;
 		
 		strokepaint.setColor(getResources().getColor(R.color.PIECESTROKE));
 		strokepaint.setStrokeWidth(1);
 		fillpaint.setColor(p.color);
 		
 		for(int r = p.x,pr = 0;r<p.x+psize;r++,pr++){
 			for(int c = p.y,pc = 0;c<p.y+psize;c++,pc++){
 				if(p.getBlockType(pr, pc)!=PieceConstants.FREEBLOCK){
 					int pleft 	= left + pc*SQSIZE;
 					int ptop 	= top + pr*SQSIZE;
 					int pright 	= left + pc*SQSIZE + SQSIZE;
 					int pbott 	= top + pr*SQSIZE + SQSIZE;
 					canvas.drawRect(pleft, ptop, pright, pbott, fillpaint);
 					canvas.drawRect(pleft, ptop, pright, pbott, strokepaint);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws the application playing background
 	 * @param canvas
 	 */
 	private void drawGameBackground(Canvas canvas) {
 		this.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
 	}
 
 	/**
 	 * Draws on screen the playable area
 	 * 
 	 * @param canvas
 	 * @param board
 	 */
 	private void drawSquarePlayerZoneBackground(Canvas canvas, int[][] board) {
 		
 		int left = 0; 
 		int top = getHeight() - boardh*SQSIZE - SQSIZE;
 		int right = boardw*SQSIZE;
 		int bottom = getHeight();
 		
 		fillpaint.setColor(getResources().getColor(R.color.BG));
 		strokepaint.setColor(getResources().getColor(R.color.PLAYZONESTROKE));
 		strokepaint.setStrokeWidth(1);
 		
 		canvas.drawRect(left, top, right, bottom, fillpaint);
 		canvas.drawRect(left, top, right, bottom, strokepaint);
 	}
 
 
 	/**
 	 * Set the current Piece new position from
 	 * touched screen position
 	 * @param x
 	 * @param y
 	 */
 	public void setPieceBoardPosFromScreenPos(float x, float y){
 		int bc = calcBoardColFromScreenX(x);
 		if(bc==-1) return;
 		dc.getCurrentPiece().y = bc;
 	}
 
 	/**
 	 * Calculate Board column from screen x
 	 * @param x
 	 * @return
 	 * @NOTE Needs optimize (like calcBoardRowFromScreenY)
 	 */
 	public int calcBoardColFromScreenX(float x) {
 		int limitright = boardw*SQSIZE + SQSIZE;
 		
 		if(x>limitright){
 			return -1;
 		}
 		for(int c=0;c<boardw;c++){
 			int left = c*SQSIZE;
 			int right = c*SQSIZE+SQSIZE;
 			if(x<right && x>left){
 				return c;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Calcs the row position from an y in screen pixels
 	 * 
 	 * @param y Screen pixels vertical
 	 * @return board row number
 	 */
 	public int calcBoardRowFromScreenY(float y){
 		
 		int limitbottom = getHeight();
 		int limittop	= limitbottom - boardh*SQSIZE;
 		int boardhinpx = boardh*SQSIZE;
 		
 		if(y>limitbottom || y<limittop) return -1;
 		
 		return (int) (((y-limittop)/boardhinpx)*boardh);
 	}
 	
 	/**
 	 * Checks if an x,y pair of values is inside the touch zone of the
 	 * current playing piece
 	 * 
 	 * @param x x screen value
 	 * @param y y screen value
 	 * @return True/false 
 	 */
 	public boolean touchedInPlayPiece(float x, float y) {
 		/*Piece p				= dc.getCurrentPiece();
 		int psize 			= PieceConstants.PIECESIZE;
 		
 		int linezero = getHeight() - boardh*SQSIZE - SQSIZE;
 		int cc = 5;
 		int left = p.x * SQSIZE;
 		int top = linezero + p.y *SQSIZE;
 		int right = p.x * SQSIZE+ psize*SQSIZE;
 		int bottom = linezero + p.y * SQSIZE + psize*SQSIZE;
 		
 		if(x>left && x<right && y>top && y<bottom) return true;
 		
 		return false;*/
 		
 		int tc = calcBoardColFromScreenX(x);
 		int tr = calcBoardRowFromScreenY(y);
 		Piece p				= dc.getCurrentPiece();
 		int psize 			= PieceConstants.PIECESIZE;
 		int left 	= p.y;
 		int right 	= p.y + psize;
 		int top		= p.x;
 		int bott	= p.x + psize;
 		
 		Log.d("touchedInPlayPiece","tr:"+tr+" tc:"+tc);
 		Log.d("PiecePosition","x:"+left+" y:"+top);
 		
 		if(tc >= left && tc <= right
 			&& tr<=bott && tr>=top	) return true;
 		
 		return false;
 	}
 	
 }
 
