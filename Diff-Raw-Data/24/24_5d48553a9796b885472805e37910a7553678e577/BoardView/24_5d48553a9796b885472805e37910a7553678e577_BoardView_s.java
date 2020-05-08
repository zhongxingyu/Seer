 package edu.aueb.cs.uw;
 
 import java.util.Iterator;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.ImageButton;
 import android.widget.PopupWindow;
 import edu.aueb.cs.uw.core.Board;
 import edu.aueb.cs.uw.core.GameEngine;
 import edu.aueb.cs.uw.core.Tile;
 import edu.aueb.cs.uw.core.TileStack;
 import edu.aueb.cs.uw.core.Tray;
 
 public class BoardView extends SurfaceView implements SurfaceHolder.Callback {
 	
 	private static int BOARD_SIZE=10;
 	private static final float MIN_FONT_DIPS = 14.0f;
 	
 	private Dimensions dimensions;
 	
 	private ImageButton endTurn;
 	
 	protected int defaultFontSize;
 	
 	private DrawingThread thread;
 	
 	class DrawingThread extends Thread {
 		
 		private static final int TRAY_AREA=1;
 		private static final int BOARD_AREA=2;
 		private static final int SCORE_AREA=3;
 		private static final String STACK_MESSAGE="Tile Stack";
 		
 		private boolean mRun;
 		private SurfaceHolder mSurfaceHolder;
 		
 		private Dimensions dims;
 		private GameEngine ge;
 		private int defaultFontS;
 		private PopupWindow pw;
 		
 		private int event;
 		private int eventX,eventY;
 		
 		private Handler mHandler;
 		
 		private boolean switchMode;
 		private boolean stackOpen;
 		private boolean tileIsMoved;
 		private boolean boardTileIsMoved;
 		private TileStack openStack;
 		private Tile movingTile;
 		private Rect [] tilesTray;
 		
 		private Paint fillScorePaint;
 	    private Paint fillTrayPaint;
 	    private Paint fillBoardPaint;
 	    private Paint strokePaint;
 	    private Paint tileFillPaint;
 	    private Paint tileStrokePaint;
 	    private Paint tileTextPaint;
 	    private Paint centralSquarePaint;
 	    private Paint scorePaint;
 	    private Paint selectedTilePaint;
 	    private Paint underneathCellPaint;
 	    private Paint stackPanePaint;
 	    private Paint stackTextPaint;
 	    
 	    private int selectedTileNum;
 	    private int selectedBoardTileX,selectedBoardTileY;
 	    private int movingTileX,movingTileY;
 	    private int topLeftX,topLeftY;
 		
 		public DrawingThread(SurfaceHolder holder, Handler handler){
 			mSurfaceHolder=holder;
 			mRun=false;
 			switchMode=false;
 			tileIsMoved=false;
 			boardTileIsMoved=false;
 			stackOpen=false;
 			openStack=new TileStack();
 			event=eventX=eventY=-1;
 			selectedTileNum=-1;
 			selectedBoardTileX=-1;
 			selectedBoardTileY=-1;
 			movingTileX=-1;
 			movingTileY=-1;
 			topLeftX=-1;
 			topLeftY=-1;
 			mHandler=handler;
 			dims=new Dimensions();
 			movingTile=new Tile();
 			defaultFontS=(int)BoardView.MIN_FONT_DIPS;
 			ge=null;
 			tilesTray=new Rect[1];
 			paintInitialisation();
 		}
 		
 		public void setRunning(boolean b){
 			mRun=b;
 		}
 		
 		
 		@Override
 		public void run() {
 			while(mRun){
 				Canvas c = null;
 				try {
 					c=mSurfaceHolder.lockCanvas(null);
 					synchronized (mSurfaceHolder) {
 						updateGame();
 						doDraw(c);
 					}
 				} finally {
 					if (c!=null) {
 						mSurfaceHolder.unlockCanvasAndPost(c);
 					}
 				}
 			}
 		}
 		
 		public void setSwitchMode(boolean b) {
 			synchronized (mSurfaceHolder) {
 				switchMode=b;
 			}
 		}
 		
 		public void setGameEngine(GameEngine ge) {
 			synchronized (mSurfaceHolder) {
 				this.ge=ge;
 			}
 		}
 		
 		public void setDimensions(Dimensions dimensions){
 			synchronized (mSurfaceHolder) {
 				dims=dimensions;				
 			}
 		}
 
 		public void setEventInfo(int evt, int x, int y) {
 			synchronized (mSurfaceHolder) {
 				event=evt;
 				eventX=x;
 				eventY=y;
 			}
 		}
 		
 		public void setDefaultFontSize(int dfs){
 			synchronized (mSurfaceHolder) {
 				defaultFontS=dfs;
 			}
 		}
 		
 		public void setPopupWindow(PopupWindow popUp) {
 			synchronized (mSurfaceHolder) {
 				this.pw=popUp;
 			}
 		}
 		
 		
 		private void updateGame() {
 			if(ge==null) return;
 			switch(event){
 			case MotionEvent.ACTION_DOWN:
 				int area=getArea(eventX, eventY);
 				switch(area){
 				case TRAY_AREA:
 					handleTrayClick(eventX,eventY);
 					if(switchMode&&selectedTileNum!=-1){
 						switchMode=false;
 						pw.dismiss();
 						ge.makeSwitch(selectedTileNum);
 						selectedTileNum=-1;
 					}
 					break;
 				case BOARD_AREA:
 					handleBoardClick(eventX,eventY);
 					break;
 				case SCORE_AREA:
 				default:
 					break;
 				}
 				break;
 			case MotionEvent.ACTION_MOVE:
 				stackOpen=false;
 				openStack=null;
 				if(selectedTileNum!=-1){
 					int turn=ge.getPlayerTurn();
 					Tray t=ge.getPlayer(turn).getTray();
 					if(!tileIsMoved) 
 						movingTile=t.temporaryRemoveTile(selectedTileNum);
 					tileIsMoved=true;
 					movingTileX=getMovingTileXPos(eventX);
 					movingTileY=getMovingTileYPos(eventY);
 				} else if(selectedBoardTileX!=-1&&selectedBoardTileY!=-1){
 					Board b=ge.getBoard();
 					if(!tileIsMoved)
 						movingTile=b.removeTile(selectedBoardTileX, selectedBoardTileY);
 					tileIsMoved=true;
 					boardTileIsMoved=true;
 					movingTileX=getMovingTileXPos(eventX);
 					movingTileY=getMovingTileYPos(eventY);
 				}
 				
 				break;
 			case MotionEvent.ACTION_UP:
 				stackOpen=false;
 				openStack=null;
 				if(tileIsMoved){
 					Board b=ge.getBoard();
 					int turn=ge.getPlayerTurn();
 					Tray t=ge.getPlayer(turn).getTray();
 					if(getArea(eventX, eventY)==BOARD_AREA){
 						int i=findCellRow(eventY);
 						int j=findCellCol(eventX);
 						if(b.canAddTile(i,j,movingTile)) {
 							b.addTile(movingTile, i, j);
 							if(!boardTileIsMoved) {
 								t.addTempRemovedTile(movingTile, selectedTileNum);
 								t.useTile(selectedTileNum);
 							}
 						}
 						else {
 							if(boardTileIsMoved) {
 								b.addTile(movingTile, selectedBoardTileX, selectedBoardTileY);
 							}
 							else {
 								t.addTempRemovedTile(movingTile, selectedTileNum);
 							}
 						}
 					} else if(getArea(eventX, eventY)==TRAY_AREA) {
 						if(boardTileIsMoved) {
 							t.addTile(movingTile);
 						}
 						else if(selectedTileNum!=-1) {
 							t.addTempRemovedTile(movingTile, selectedTileNum); 
 						}
 					} else{
 						if(selectedTileNum!=-1) {
 							t.addTempRemovedTile(movingTile, selectedTileNum); 
 						}
 						else if(selectedBoardTileX!=-1&&selectedBoardTileY!=-1) {
 							b.addTile(movingTile, selectedBoardTileX, selectedBoardTileY);
 						}
 					}
 					if(ge.getBoard().isValidPlacement()){
 						Message msg = mHandler.obtainMessage();
 		                Bundle bundle = new Bundle();
 		                bundle.putBoolean("clickable", true);
 		                msg.setData(bundle);
 		                mHandler.sendMessage(msg);
 					} else {
 						Message msg = mHandler.obtainMessage();
 		                Bundle bundle = new Bundle();
 		                bundle.putBoolean("clickable", false);
 		                msg.setData(bundle);
 		                mHandler.sendMessage(msg);
 					}
 					undoMovingChanges();
 				} else {
 					undoMovingChanges();
 				}
 				break;
 			default:
 				break;	
 			}
		
 		}
 
 		private void doDraw(Canvas canvas) {
 			if(ge==null) return;
 			Rect bRect=new Rect(0,dims.getScoreHeight(),dims.getTotalWidth(),dims.getScoreHeight()+dims.getBoardheight());
 			canvas.drawRect(bRect, fillBoardPaint);
 			Rect scRect=new Rect(0,0, dims.getTotalWidth(),dims.getTotalHeight()-dims.getBoardheight()-dims.getTrayHeight());
 			canvas.drawRect(scRect, fillScorePaint);
 			Rect tRect=new Rect(0,dims.getScoreHeight()+dims.getBoardheight(), dims.getTotalWidth(),dims.getTotalHeight());
 			canvas.drawRect(tRect, fillTrayPaint);
 			canvas.drawRect(scRect, fillScorePaint);
 			drawTray(canvas);
 			drawBoard(canvas);
 			drawScore(canvas);
 			drawMovingTile(canvas);
 			if(stackOpen){
 				drawStack(canvas, topLeftX, topLeftY, openStack);
 			}
 		}
 		
 		private void drawStack(Canvas canvas, int x, int y,
 				TileStack ts) {
 			int size=ts.getSize();
 			int height=size*dims.getCellSize()+2*dims.getCellSize();
 			int width=dims.getCellSize()*2;
 			Tile t;
 			Rect paneRect=new Rect(x,y,x+width,y+height);
 			canvas.drawRect(paneRect, stackPanePaint);
 			stackPanePaint.setStyle(Paint.Style.STROKE);
 			stackPanePaint.setColor(Color.BLACK);
 			canvas.drawRect(paneRect, stackPanePaint);
 			stackPanePaint.setStyle(Paint.Style.FILL);
 			stackPanePaint.setARGB(255, 99, 120, 130);
 			canvas.drawText(STACK_MESSAGE, x+width/2, y+dims.getCellSize()/2, stackTextPaint);
 			canvas.drawLine(x, y+dims.getCellSize()/2+4, x+width, y+dims.getCellSize()/2+4, stackTextPaint);
 			Iterator<Tile>iter=ts.getStackIterator();
 			int i=1;
 			float txtSize=stackTextPaint.getTextSize();
 			stackTextPaint.setTextAlign(Align.LEFT);
 			stackTextPaint.setTextSize(2*defaultFontS);
 			while(iter.hasNext()){
 				t=iter.next();
 				canvas.drawText(Integer.toString(i), x+dims.getCellSize()/4 , y+i*dims.getCellSize()+5*dims.getCellSize()/6,
 						stackTextPaint);
 				drawTile(canvas, x+3*dims.getCellSize()/4, y+((size+1)-i)*dims.getCellSize(),
 						x+3*dims.getCellSize()/4+dims.getCellSize(), y+((size+1)-i)*dims.getCellSize()+dims.getCellSize(),
 						Character.toString(t.getLetter()));
 				i++;
 			}
 			stackTextPaint.setTextAlign(Align.CENTER);
 			stackTextPaint.setTextSize(txtSize);
 		}
 
 		private void drawMovingTile(Canvas canvas) {
 			if(tileIsMoved){
 				tileTextPaint.setTextSize(defaultFontS*2);
 				drawTile(canvas, movingTileX-dims.getCellSize()/2, movingTileY-dims.getCellSize()/2, 
 						movingTileX+dims.getCellSize()/2, movingTileY+dims.getCellSize()/2,
 						Character.toString(movingTile.getLetter()));
 				if(getArea(movingTileX, movingTileY)==BOARD_AREA){
 					int i=findCellRow(movingTileY);
 					int j=findCellCol(movingTileX);
 					Board b=ge.getBoard();
 					if(b.canAddTile(i,j,movingTile))
 						underneathCellPaint.setColor(Color.GREEN);
 					else
 						underneathCellPaint.setColor(Color.RED);
 					int left=dims.getPadding()+j*dims.getCellSize();
 					int right=left+dims.getCellSize();
 					int top=dims.getScoreHeight()+i*dims.getCellSize();
 					int bottom=top+dims.getCellSize();
 					Rect underRect=new Rect(left, top, right, bottom);
 					canvas.drawRect(underRect, underneathCellPaint);
 				}
 			}
 		}
 
 		private void drawScore(Canvas canvas) {
 			if(ge==null) return;
 			
 			int numOfPlayers=ge.getNumPlayers();
 			
 			int maxHeight=dims.getScoreHeight();
 			float scoreWidth=dims.getTotalWidth()/numOfPlayers;
 			
 			scorePaint.setTextAlign(Align.CENTER);
 			
 			for(int i=0;i<numOfPlayers;i++){
 				if(i==ge.getPlayerTurn())
 					scorePaint.setTextSize(1.3f*defaultFontS);
 				scorePaint.setColor(ge.getPlayer(i).getColor());
 				canvas.drawText(ge.getPlayer(i).getNickname()+":"+ge.getPlayer(i).getScore(),
 						i*scoreWidth+(scoreWidth/2) ,3*maxHeight/4, scorePaint);
 				scorePaint.setTextSize(defaultFontS);
 			}
 			
 		}
 
 		private void drawBoard(Canvas canvas) {
 			int bHeight=dims.getBoardheight();
 			
 			int bWidth=bHeight;
 			int sHeight=dims.getScoreHeight();
 			int padding=dims.getPadding();
 			
 			int left=dims.getPadding()+(BOARD_SIZE/2-1)*dims.getCellSize();
 			int top=dims.getScoreHeight()+(BOARD_SIZE/2-1)*dims.getCellSize();
 			int bottom= 2*dims.getCellSize()+top;
 			int right= 2*dims.getCellSize()+left;
 			
 			Rect centralRect=new Rect(left, top, right, bottom);
 			canvas.drawRect(centralRect, centralSquarePaint);
 			
 			for(int i=0;i<11;i++)
 			{
 				int x=i*dims.getCellSize();
 				canvas.drawLine(padding+x,sHeight,padding+x,sHeight+bHeight,strokePaint);
 			}
 			
 			for(int i=0;i<11;i++)
 			{
 				int y=i*dims.getCellSize();
 				canvas.drawLine(padding,sHeight+y,padding+bWidth,sHeight+y,strokePaint);
 			}
 			
 			if(ge==null) return;
 			
 			Board b=ge.getBoard();
 			TileStack [][] ts=b.getTilePlacement();
 			for(int i=0;i<ts.length;i++){
 				for(int j=0;j<ts[i].length;j++){
 					if(b.hasTile(i, j)){
 						if(selectedBoardTileX==i&&selectedBoardTileY==j)
 							tileFillPaint.setColor(Color.YELLOW);
 						else if(b.getTile(i, j).getAge()==b.getTurn())
 							tileFillPaint.setARGB(255, 160, 160, 200);
 						left=dims.getPadding()+j*dims.getCellSize();
 						right=left+dims.getCellSize();
 						top=dims.getScoreHeight()+i*dims.getCellSize();
 						bottom=top+dims.getCellSize();
 						tileTextPaint.setTextSize(2*defaultFontS);
 						drawTile(canvas,left , top, right, bottom, Character.toString(ts[i][j].getTop().getLetter()));
 						tileFillPaint.setColor(Color.WHITE);
 					}
 				}
 			}	
 		}
 
 		private void drawTray(Canvas canvas) {
 
 			int turn=ge.getPlayerTurn();
 			Tray t=ge.getPlayer(turn).getTray();
 			int tileSize=dims.getTotalWidth()/Tray.TRAY_SIZE;
 			if(tileSize>=dims.getTrayHeight())
 				tileSize=4*dims.getTrayHeight()/5;
 			int bot_border=(dims.getTrayHeight()-tileSize)/2;
 			int space=(dims.getTotalWidth()-(tileSize*Tray.TRAY_SIZE))/(Tray.TRAY_SIZE+1);
 			tilesTray=new Rect[t.getNumUnusedTiles()]; 
 			for(int i=0;i<t.getNumUnusedTiles();i++){
 				if(t.getTile(i)==null) continue;
 				if(selectedTileNum==i)
 					tileFillPaint.setColor(Color.YELLOW);
 				tileTextPaint.setTextSize(defaultFontS*3);
 				tilesTray[i]=drawTile(canvas, i*tileSize+(i+1)*space, dims.getTotalHeight()-tileSize-bot_border, 
 						i*tileSize+(i+1)*space+tileSize, dims.getTotalHeight()-bot_border, t.getTileLetter(i));
 				tileFillPaint.setColor(Color.WHITE);
 			}
 		}
 		
 		private Rect drawTile(Canvas canvas, int left, int top, int right, int bottom, String text){
 			Rect tileRect;
 			tileRect=new Rect(left, top, right, bottom);
 			canvas.drawRect(tileRect, tileFillPaint);
 			canvas.drawRect(tileRect, tileStrokePaint);
 			canvas.drawText(text,left+(right-left)/2,bottom-(bottom-top)/5, tileTextPaint);
 			return tileRect;
 		}
 		
 		private void undoMovingChanges(){
 			tileIsMoved=false;
 			boardTileIsMoved=false;
 			selectedTileNum=-1;
 			selectedBoardTileX=-1;
 			selectedBoardTileY=-1;
 		}
 		
 		private int getMovingTileXPos(int x){
 			if(x<dims.getCellSize()/2)
 				return dims.getCellSize()/2;
 			else if(x>dims.getTotalWidth()-dims.getCellSize()/2)
 				return dims.getTotalWidth()-dims.getCellSize()/2;
 			else 
 				return x;
 		}
 		
 		private int getMovingTileYPos(int y){
 			if(y<dims.getCellSize()/2)
 				return dims.getCellSize()/2;
 			else if(y>dims.getTotalHeight()-dims.getCellSize()/2)
 				return dims.getTotalHeight()-dims.getCellSize()/2;
 			else
 				return y;
 		}
 		
 		private void handleBoardClick(int x, int y) {
 			Board b=ge.getBoard();
 			int turn=b.getTurn();
 			int i=findCellRow(y);
 			int j=findCellCol(x);
 			Tile t=b.getTile(i, j);
 			if(t!=null){
 				stackOpen=true;
 				openStack=(ge.getBoard().getTilePlacement())[i][j];
 				calculateStackCoords(x, y);
 				if(t.getAge()==turn) {
 					selectedBoardTileX=i;
 					selectedBoardTileY=j;
 				}			
 			}
 		}
 		
 		private void handleTrayClick(int x, int y) {
 			for(int i=0;i<tilesTray.length;i++){
 				if(tilesTray[i].contains(x, y))
 						selectedTileNum=i;
 			}			
 		}
 		
 		private void calculateStackCoords(int x, int y){
 			int size=openStack.getSize();
 			int height=size*dims.getCellSize()+2*dims.getCellSize();
 			if(y+height<=dims.getBoardheight()+dims.getScoreHeight())
 				topLeftY=y;
 			else
 				topLeftY=dims.getBoardheight()+dims.getScoreHeight()-height;		
 			if(x>=dims.getTotalWidth()-x)
 				topLeftX=x-dims.getCellSize()*3;
 			else
 				topLeftX=x+dims.getCellSize();
 		}
 		
 		private int getArea(int x,int y){
 			if(y<=getDimensions().getScoreHeight())
 				return SCORE_AREA;
 			else if(y>getDimensions().getScoreHeight()&&
 					y<=getDimensions().getScoreHeight()+getDimensions().getBoardheight())
 				return BOARD_AREA;
 			else return TRAY_AREA;
 		}
 		
 		private int findCellRow(int y){
 			/*Needs y coordinate to find row*/
 			for(int i=0;i<BOARD_SIZE;i++){
 				if(y>=dims.getScoreHeight()+i*dims.getCellSize()&&
 						y<=dims.getScoreHeight()+i*dims.getCellSize()+dims.getCellSize())
 					return i;
 			}
 			return 0;
 		}
 		
 		private int findCellCol(int x){
 			/*Needs x coordinate to find column*/
 			for(int i=0;i<BOARD_SIZE;i++){
 				if(x>=dims.getPadding()+i*dims.getCellSize()&&
 						x<=dims.getPadding()+i*dims.getCellSize()+dims.getCellSize())
 					return i;
 			}
 			return 0;
 		}
 
 		private void paintInitialisation(){
 			fillScorePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 			fillScorePaint.setARGB(200, 75, 75, 75);
 			fillTrayPaint=new Paint();
 			fillTrayPaint.setARGB(200, 75, 75, 75);
 			fillBoardPaint=new Paint();
 			fillBoardPaint.setARGB(240, 150, 150, 150);
 			strokePaint=new Paint();
 			strokePaint.setStyle(Paint.Style.STROKE);
 			strokePaint.setColor(Color.WHITE);
 			tileStrokePaint=new Paint();
 			tileStrokePaint.setStyle(Paint.Style.STROKE);
 			tileStrokePaint.setColor(Color.BLACK);
 			tileFillPaint=new Paint();
 			tileFillPaint.setStyle(Paint.Style.FILL);
 			tileFillPaint.setColor(Color.WHITE);
 			tileTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 			tileTextPaint.setColor(Color.BLACK);
 			tileTextPaint.setStyle(Paint.Style.STROKE);
 			tileTextPaint.setTextAlign(Align.CENTER);
 			stackPanePaint=new Paint();
 			stackPanePaint.setStyle(Paint.Style.FILL);
 			stackPanePaint.setARGB(255, 99, 120, 130);
 			stackTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 			stackTextPaint.setColor(Color.BLACK);
 			stackTextPaint.setTextAlign(Align.CENTER);
 			stackTextPaint.setStyle(Paint.Style.STROKE);
 			selectedTilePaint=new Paint();
 			selectedTilePaint.setStyle(Paint.Style.FILL);
 			selectedTilePaint.setColor(Color.YELLOW);
 			underneathCellPaint=new Paint();
 			underneathCellPaint.setStyle(Style.STROKE);
 			underneathCellPaint.setColor(Color.RED);
 			centralSquarePaint=new Paint();
 			centralSquarePaint.setColor(Color.RED);
 			scorePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 	   
 			float curWidth = tileStrokePaint.getStrokeWidth();
 			curWidth *= 2;
 			if ( curWidth < 2 ) {
 				curWidth = 2;
 			}
 			tileStrokePaint.setStrokeWidth(curWidth);
 		}
 		
 	}
 	
 	
 	final Handler handler = new Handler() {
         public void handleMessage(Message msg) {
             boolean clickable=msg.getData().getBoolean("clickable");
             if(clickable){
             	endTurn.setClickable(true);
 				endTurn.setImageResource(R.drawable.end_turn_available);
             } else {
             	endTurn.setClickable(false);
 				endTurn.setImageResource(R.drawable.end_turn);
             }
         }
     };
     
 	public BoardView( Context context, AttributeSet attrs ) 
     {
         super(context, attrs);
         
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
         
 //        endTurn=(ImageButton)findViewById(R.id.endturn_button_horizontal);
         thread=new DrawingThread(holder,handler);
     }
 	
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 	
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		final float scale = getResources().getDisplayMetrics().density;
 		defaultFontSize = (int)(MIN_FONT_DIPS * scale + 0.5f);	
 		thread.setDefaultFontSize(defaultFontSize);
 		dimensions=calculateDimensions(getWidth(), getHeight());
 		thread.setDimensions(dimensions);
 		thread.setRunning(true);
 		thread.start();
 	}
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry=true;
 		thread.setRunning(false);
 		while(retry){
 			try {
 				thread.join();
 				retry=false;
 			} catch (InterruptedException e){
 				
 			}
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		int evt=event.getAction();
 		int x=(int)event.getX();
 		int y=(int)event.getY();
 		thread.setEventInfo(evt,x,y);
		return true;
 	}
 	
 	public void setEndTurn(ImageButton end){
 		endTurn=end;
 		endTurn.setClickable(false);
 	}
 	
 	public void setSwitchMode(boolean b) {
 		thread.setSwitchMode(b);
 	}
 
 	public Dimensions getDimensions() {
 		return dimensions;
 	}
 	
 	public void setGameEngine(GameEngine ge) {
 		thread.setGameEngine(ge);
 	}
 	
 	public void setPopupWindow(PopupWindow popUp) {
 		thread.setPopupWindow(popUp);
 	}
 	
 	private Dimensions calculateDimensions(int width,int height){
 		int cellSize,padding,top,bWidth,bHeight,scHeight,tHeight;
 		int maxCellSize;
 
 		Dimensions dims=new Dimensions();
 		dims.setTotalWidth(width);
 		dims.setTotalHeight(height);		
 		cellSize=width/BOARD_SIZE;
 		maxCellSize=3*defaultFontSize;
 		if(maxCellSize<cellSize)
 			cellSize=maxCellSize;
 		bWidth=BOARD_SIZE*cellSize;
 		bHeight=bWidth;
 		padding=(width-bWidth)/2;
 		scHeight=defaultFontSize*2;
 		tHeight=cellSize*3;
 
 		if(height>=bWidth+scHeight+tHeight) {
 			top=(height-bWidth-scHeight-tHeight)/2;
 		}
 		else {
 			top=0;
 			tHeight=4*defaultFontSize;
 			if(height-bHeight-tHeight>tHeight){
 				tHeight=height-bHeight-tHeight;
 				scHeight=height-bHeight-tHeight;
 			} else{
 				scHeight=height-bHeight-tHeight;				
 			}
 		}
 		dims.setCellSize(cellSize);
 		dims.setBoardheight(bHeight);
 		dims.setPadding(padding);
 		dims.setScoreHeight(scHeight);
 		dims.setTop(top);
 		dims.setTrayHeight(tHeight);
 		return dims;
 	}
 
 	
 }
