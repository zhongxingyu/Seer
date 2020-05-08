 
 package com.example.testels;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.NinePatch;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 
 /**
  * 项目名称：TestELS
  * 类名称：TetrisSufaceView
  * 类描述：
  * 创建人：xubt
  * 创建时间：2013-1-11 上午10:47:14
  * -------------------------------修订历史--------------------------
  * 修改人：xubt
  * 修改时间：2013-1-11 上午10:47:14
  * 修改备注：
  * @version：
  */
 public class TetrisSufaceView extends  SurfaceView implements Callback,Runnable,OnGestureListener,android.view.View.OnTouchListener{
 	private Context context;
 	private TetrisMoveRunnable tetrisMoveRunnable;
 	private Thread tetrisMoveThread;
 	private int sleep=50;
 	private long duration ;
 	private boolean isPause; //游戏是否处于暂停状态
 	private boolean isOver;  //游戏是否结束
 	private boolean isNew;   //图形是否是刚开始从boundTop位置开始
 	
 	private int valueKeyDown =0;//按下的键值
 	private static final int VALUE_LEFT_KEY=1,VALUE_RIGHT_KEY=2,VALUE_UP_KEY=3,VALUE_DOWN_KEY=4;
 	public static final int DIALOG_GAMEOVER =1,DIALOG_BACK =2;
 	private boolean long_press = false;
 	
 	private boolean flag = true; //线程run方法循环标志位
 	
 	private Thread surfaceViewDrawThread;
 	private SurfaceHolder surfaceHolder;
 	private Canvas canvas;
 	private Paint paint,paint1,paintBg;
 	
 	public static int rows=21,cols = 10;  //把屏幕分成21行，10列
 	
 	private int screenWidth,screenHeight;  //屏幕分辨率
 	private int boundLeft,boundRight,boundTop,boundBottom;  //游戏框四边的坐标
 	private int cellSize;   //一个小方块的单元大小
 	private Bitmap red_brick_bmp,green_brick_bmp,yellow_brick_bmp;
 	private NinePatch red_brick_np,green_brick_np,yellow_brick_np;
 	private int cellTop,cellLeft; //下落积木图形的坐标
 	private int score;
 	private int type,state,nextType,nextState;
 	
 	private SoundPool soundPool;
 	private Map<String, Integer> soundPoolMap;
 	private float volume;
 	
 	private GestureDetector mGestureDetector;
 	
 	
 	public TetrisSufaceView(Context context) {
 		super(context);
 		this.context = context;
 		setKeepScreenOn(true);
 		surfaceHolder = this.getHolder();
 		surfaceHolder.addCallback(this);
 		score = 0;
 		initSound();
 		type = new Random().nextInt(7);
 		int length = Shape.shape[type].length/4;
 		state =new Random().nextInt(length)*4;
 		Log.i("TAG", "init    type:"+type+"    state:"+state);
 		nextType = new Random().nextInt(7);
 		nextState = new Random().nextInt(Shape.shape[nextType].length/4)*4;
 		Log.i("TAG", "init    nextType:"+nextType+"    nextState:"+nextState);
 		green_brick_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.green_brick);
 		red_brick_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.red_brick);
 		yellow_brick_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_brick);
 		green_brick_np = new NinePatch(green_brick_bmp, green_brick_bmp.getNinePatchChunk(), null);
 		red_brick_np = new NinePatch(red_brick_bmp, red_brick_bmp.getNinePatchChunk(), null);
 		yellow_brick_np = new NinePatch(yellow_brick_bmp, yellow_brick_bmp.getNinePatchChunk(), null);
 		
 		paint = new Paint();
 		paint.setAntiAlias(true);
 		paint.setColor(Color.WHITE);
 		paint1 = new Paint();
 		paint.setColor(Color.GRAY);
 		paint1.setAntiAlias(true);
 		paintBg = new Paint();
 		paintBg.setColor(Color.BLACK);
 		setFocusable(true);
 		setFocusableInTouchMode(true);
 		this.setLongClickable(true);
 		
 		this.setLongClickable(true);
 		mGestureDetector = new GestureDetector(context, this);
 		this.setOnTouchListener(this);
 		mGestureDetector.setIsLongpressEnabled(true);
 		
 	}
 	
 	private void initSound(){
 		soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
 		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 		((Activity)context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		int nowVolume =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		volume = nowVolume/maxVolume*10;
 //		Log.i("TAG", "nowVolume:"+nowVolume+"    maxVolume:"+maxVolume+"    volume:"+volume);
 		volume=0;
 		soundPoolMap = new HashMap<String, Integer>();
 		soundPoolMap.put("move", soundPool.load(context, R.raw.move, 0));
 		soundPoolMap.put("getScore", soundPool.load(context, R.raw.getscore, 0));
 		soundPoolMap.put("down", soundPool.load(context, R.raw.down, 0));
 	}
 	
 	private void doDraw(){
 		canvas = surfaceHolder.lockCanvas();
 		canvas.drawRect(0, 0, screenWidth, screenHeight, paintBg);
 		Rect leftRect = new Rect(0, boundTop, boundLeft, boundBottom);
 		Rect rightRect = new Rect(boundRight, boundTop, boundRight+2, boundBottom);
 		Rect topRect = new Rect(0, boundTop-1, boundRight+2, boundTop);
 		Rect bottomRect = new Rect(0, boundBottom, boundRight+2, boundBottom+2);
 		canvas.drawRect(leftRect, paint);
 		canvas.drawRect(rightRect, paint);
 		canvas.drawRect(topRect, paint);
 		canvas.drawRect(bottomRect, paint);
 		drawMap();
 //		Log.e("TAG", "TetrisSurfaceView.doDraw  type:"+type+"   state:"+state);
 		getOneBrickShape(type, state);
 		
 		canvas.drawText("得分："+score, boundRight+10, boundTop+cellSize*2, paint);
 		canvas.drawText("下一个", boundRight+10, boundTop+cellSize*4, paint);
 		drawNextShape(nextType, nextState);
 		surfaceHolder.unlockCanvasAndPost(canvas);
 	}
 	/**
 	 * 在游戏右侧绘制下个图形
 	 * @方法名：drawNextShape
 	 * @功能描述：
 	 * @创建人：xubt
 	 * @创建时间：2013-1-15 下午3:49:24
 	 * @参数：@param type
 	 * @参数：@param state
 	 * @返回：void
 	 * @throws
 	 */
 	private void drawNextShape(int type,int state){
 		for (int i = state; i <state + 4; i++) {
 			for (int j = 0; j < 4; j++) {
 //				Log.i("TAG", "drawNextShape---type:"+type+"    state:"+state+"   i:"+i+"    j:"+j);
 				if(Shape.shape[type][i][j]==1){
					int cellSmallSize = (int) (cellSize*0.6);
 					int cell_top = boundTop+cellSize*5+cellSmallSize*(i-state);
 					int cell_left = boundRight+10+j*cellSmallSize;
 					Rect cellRect = new Rect(cell_left, cell_top, cell_left+cellSmallSize, cell_top+cellSmallSize);
 					yellow_brick_np.draw(canvas, cellRect);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 在游戏中绘制已经停止的方块，即在二维数组map中值为1的地方画方块
 	 * @方法名：drawMap
 	 * @功能描述：
 	 * @创建人：xubt
 	 * @创建时间：2013-1-15 下午3:04:34
 	 * @参数：
 	 * @返回：void
 	 * @throws
 	 */
 	private void drawMap(){
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				if(Shape.map[i][j]==1){
 //					Log.d("TAG", "TetrisSufaceView.drawMap   i:"+i+"    j:"+j);
 					int cell_top =boundTop+i*cellSize;
 					int cell_left = boundLeft+j*cellSize;
 					Rect cellRect = new Rect(cell_left, cell_top, cell_left+cellSize, cell_top+cellSize);
 					red_brick_np.draw(canvas, cellRect);
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * 在游戏中画出一个下落的积木
 	 * @方法名：getOneBrickShape
 	 * @功能描述：
 	 * @创建人：xubt
 	 * @创建时间：2013-1-15 下午3:07:49
 	 * @参数：@param type
 	 * @参数：@param state
 	 * @返回：void
 	 * @throws
 	 */
 	private void getOneBrickShape(int type,int state){
 //		Log.i("TAG", "getOneBrickShape  cellTop:"+cellTop+"     cellLeft:"+cellLeft);
 		for (int i = state; i < state+4; i++) {
 			for (int j = 0; j < 4; j++) {
 //				Log.i("TAG", "getOneBrickShape--type:"+type+"   state:"+state+"    i:"+i+"   j:"+j);
 				if(Shape.shape[type][i][j]==1){
 					int cell_top =cellTop+(i-state)*cellSize; //得到小方块的纵坐标
 					int cell_left = cellLeft+j*cellSize; //得到小方块的横坐标
 					
 //					int cell_left = cellLeft+(j-2)*cellSize; //得到小方块的横坐标,为了能让图形居中显示，故多减去2个cellSize
 					Rect cellRect = new Rect(cell_left, cell_top, cell_left+cellSize, cell_top+cellSize);
 					green_brick_np.draw(canvas, cellRect);
 					int mapX  = (cellLeft-boundLeft)/cellSize;
 					int mapY  = (cellTop-boundTop)/cellSize+1;
 					
 					if(isNew){//积木刚从顶部开始
 						if(Shape.map[mapY][mapX] ==1){
 							Log.e("TAG", "getOneBrickShape cell_top:"+cell_top+"   cell_left:"+cell_left);
							Log.i("TAG", "1111111111111tetrisMoveRunnable is null ?"+(tetrisMoveRunnable==null));
 							isOver = true;
 							break;
 						}
 					}
 					
 				}
 			}
 		}
 	}
 	/** 
 	 * @方法名：run
 	 * @功能描述：
 	 * @参数：
 	 * @throws
 	 */
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 //		Date startDate = new Date();
 		while (flag) {
 			long startTime = System.currentTimeMillis();
 			switch (valueKeyDown) {   
 			case VALUE_LEFT_KEY:   //左移
 				soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 				boolean isBound = Shape.isBound(type, state, cellLeft, cellTop, cellSize, 
 						Shape.LEFT, boundLeft, boundRight, boundTop, boundBottom, Shape.MOVE);
 				if(isBound){
 					cellLeft = Shape.moveLeft(cellLeft, cellSize);
 				}
 				valueKeyDown= 0;
 				break;
 			case VALUE_RIGHT_KEY:  //右移
 				soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 				 isBound = Shape.isBound(type, state, cellLeft, cellTop, cellSize, 
 						Shape.RIGHT, boundLeft, boundRight, boundTop, boundBottom, Shape.MOVE);
 				if(isBound){
 					cellLeft = Shape.moveRight(cellLeft, cellSize);
 				}
 				valueKeyDown= 0;
 				break;
 			case VALUE_DOWN_KEY: //加速
 				soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 				 isBound = Shape.isBound(type, state, cellLeft, cellTop, cellSize, 
 						Shape.DOWN, boundLeft, boundRight, boundTop, boundBottom, Shape.MOVE);
 				if(isBound){
 					cellTop = Shape.moveDown(cellTop, cellSize);
 				}
 				if(!long_press){
 					valueKeyDown= 0;
 				}
 				break;
 			case VALUE_UP_KEY:   //变形
 				soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 				isBound = Shape.isBound(type, state, cellLeft, cellTop, cellSize,
 						0,boundLeft, boundRight, boundTop, boundBottom, Shape.STATECHANGE);
 				if(isBound){
 					state = Shape.stateChange(type, state);
 				}
 				valueKeyDown= 0;
 				break;
 			default:
 				break;
 			}
 			if(cellTop==boundTop){
 //				Log.i("TAG", "TetrisSufaceView.run   cellTop==boundTop    --tetrisMoveRunnable is null ?"+(tetrisMoveRunnable==null));
 				isNew = true;
 			}else{
 				isNew = false;
 			}
 			
 			if(isOver){
 				
 				((MainActivity)context).mHandler.sendEmptyMessage(DIALOG_GAMEOVER);
 				gameOver();
 				
 			}
 			doDraw();
 			long endTime = System.currentTimeMillis();
 			duration = endTime -startTime;
 			Log.w("TAG", "duraton:"+duration);
 			try {
				Thread.sleep(sleep-duration);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	/** 
 	 * @方法名：surfaceCreated
 	 * @功能描述：
 	 * @参数：@param holder
 	 * @throws
 	 */
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		screenWidth = this.getWidth();
 		screenHeight = this.getHeight();
 		cellSize = screenHeight/rows;
 		Log.i("TAG", "cellSize:"+cellSize);
 		boundLeft = 2;
 		boundRight =boundLeft + cellSize*cols;
 		boundBottom = screenHeight-2;
 		boundTop = boundBottom-cellSize*rows;
		cellLeft =(boundRight-boundLeft)/2+boundLeft-2*cellSize;
 		cellTop = boundTop;
 		gameStart();
 	}
 	
 	private void gameStart(){
 		Shape.map =new int[rows][cols];
 		surfaceViewDrawThread = new Thread(this);
 		
 		surfaceViewDrawThread.start();
 		tetrisMoveRunnable = new TetrisMoveRunnable(this);
 		tetrisMoveThread = new Thread(tetrisMoveRunnable);
 		tetrisMoveThread.setName("move Thread");
 		tetrisMoveThread.start();
 	}
 	
 	/**
 	 * 
 	 * @方法名：gameOver
 	 * @功能描述：游戏结束，终止各线程
 	 * @创建人：xubt
 	 * @创建时间：2013-1-16 上午9:40:54
 	 * @参数：
 	 * @返回：void
 	 * @throws
 	 */
 	private void gameOver(){
 		flag = false;
 //		Log.i("TAG", "gameOver--tetrisMoveRunnable is null ?"+(tetrisMoveRunnable==null));
 		tetrisMoveRunnable.setFlag(false);
 		tetrisMoveRunnable.setFlagRun(false);
 		try {
 			surfaceViewDrawThread.join();
 			tetrisMoveThread.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	/** 
 	 * @方法名：surfaceChanged
 	 * @功能描述：
 	 * @参数：@param holder
 	 * @参数：@param format
 	 * @参数：@param width
 	 * @参数：@param height
 	 * @throws
 	 */
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/** 
 	 * @方法名：surfaceDestroyed
 	 * @功能描述：
 	 * @参数：@param holder
 	 * @throws
 	 */
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		if(!isOver){
 			gameOver();
 		}
 	}
 
 	public int getBoundLeft() {
 		return boundLeft;
 	}
 
 	public void setBoundLeft(int boundLeft) {
 		this.boundLeft = boundLeft;
 	}
 
 	public int getBoundRight() {
 		return boundRight;
 	}
 
 	public void setBoundRight(int boundRight) {
 		this.boundRight = boundRight;
 	}
 
 	public int getBoundTop() {
 		return boundTop;
 	}
 
 	public void setBoundTop(int boundTop) {
 		this.boundTop = boundTop;
 	}
 
 	public int getBoundBotttom() {
 		return boundBottom;
 	}
 
 	public void setBoundBotttom(int boundBottom) {
 		this.boundBottom = boundBottom;
 	}
 
 	public int getCellSize() {
 		return cellSize;
 	}
 
 	public void setCellSize(int cellSize) {
 		this.cellSize = cellSize;
 	}
 
 	public int getCellTop() {
 		return cellTop;
 	}
 
 	public void setCellTop(int cellTop) {
 		this.cellTop = cellTop;
 	}
 
 	public int getCellLeft() {
 		return cellLeft;
 	}
 
 	public void setCellLeft(int cellLeft) {
 		this.cellLeft = cellLeft;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	public SoundPool getSoundPool() {
 		return soundPool;
 	}
 
 	public void setSoundPool(SoundPool soundPool) {
 		this.soundPool = soundPool;
 	}
 
 	public Map<String, Integer> getSoundPoolMap() {
 		return soundPoolMap;
 	}
 
 	public void setSoundPoolMap(Map<String, Integer> soundPoolMap) {
 		this.soundPoolMap = soundPoolMap;
 	}
 
 	public float getVolume() {
 		return volume;
 	}
 
 	public void setVolume(float volume) {
 		this.volume = volume;
 	}
 
 	public int getNextType() {
 		return nextType;
 	}
 
 	public void setNextType(int nextType) {
 		this.nextType = nextType;
 	}
 
 	public int getNextState() {
 		return nextState;
 	}
 
 	public void setNextState(int nextState) {
 		this.nextState = nextState;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void setScore(int score) {
 		this.score = score;
 	}
 
 	/** 
 	 * @方法名：onDown
 	 * @功能描述：
 	 * @参数：@param e
 	 * @参数：@return
 	 * @throws
 	 */
 	@Override
 	public boolean onDown(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/** 
 	 * @方法名：onShowPress
 	 * @功能描述：
 	 * @参数：@param e
 	 * @throws
 	 */
 	@Override
 	public void onShowPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/** 
 	 * @方法名：onSingleTapUp
 	 * @功能描述：
 	 * @参数：@param e
 	 * @参数：@return
 	 * @throws
 	 */
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		// TODO Auto-generated method stub
 		if(!isPause){
 			valueKeyDown =VALUE_UP_KEY;
 		}
 		return false;
 	}
 
 	/** 
 	 * @方法名：onScroll
 	 * @功能描述：
 	 * @参数：@param e1
 	 * @参数：@param e2
 	 * @参数：@param distanceX
 	 * @参数：@param distanceY
 	 * @参数：@return
 	 * @throws
 	 */
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		// TODO Auto-generated method stub
 		if(!isPause){
 			float e1x = e1.getX();
 			float e2x = e2.getX();
 			float e1y = e1.getY();
 			float e2y = e2.getY();
 			float x = e2x- e1x;
 			float y = e2y- e1y;
 			if(Math.abs(x)>Math.abs(y)){
 				if(x<0){
 					valueKeyDown = VALUE_LEFT_KEY;
 				}else if(x>0){
 					valueKeyDown = VALUE_RIGHT_KEY;
 				}
 			}
 		}
 		return false;
 	}
 
 	/** 
 	 * @方法名：onLongPress
 	 * @功能描述：
 	 * @参数：@param e
 	 * @throws
 	 */
 	@Override
 	public void onLongPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 		if(!isPause){
 			long_press = true;
 			valueKeyDown = VALUE_DOWN_KEY;
 		}
 	}
 
 	/** 
 	 * @方法名：onFling
 	 * @功能描述：
 	 * @参数：@param e1
 	 * @参数：@param e2
 	 * @参数：@param velocityX
 	 * @参数：@param velocityY
 	 * @参数：@return
 	 * @throws
 	 */
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		// TODO Auto-generated method stub
 		float e1x =e1.getX();
 		float e2x = e2.getX();
 		float x = e2x-e1x;
 		float e1y = e1.getY();
 		float e2y = e2.getY();
 		float y = e2y-e1y;
 		float absX = Math.abs(x);
 		float absy = Math.abs(y);
 		if(absX<absy){
 			if(y>0 && !isPause){  //图形瞬间下落
 				soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 				boolean isBottom = false;
 				while (!isBottom) {
 					boolean isBound = Shape.isBound(type, state, cellLeft, cellTop, cellSize, Shape.DOWN, boundLeft, boundRight, boundTop, boundBottom, Shape.MOVE);
 					if(isBound){
 						isNew = false;
 						cellTop = Shape.moveDown(cellTop, cellSize);
 					}else{
 						isBottom = true;
 					}
 				}
 			}else if(y<0){ //游戏暂停或继续
 				soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 //				Log.i("TAG", "tetrisMoveRunnable is null ?"+(tetrisMoveRunnable==null));
 				if(tetrisMoveRunnable.isFlag()){
 					isPause = true;
 					tetrisMoveRunnable.setFlag(false);//停止下落线程中的下落动作。但不停止该线程。
 				}else if(!tetrisMoveRunnable.isFlag()){
 					isPause=false;
 					tetrisMoveRunnable.setFlag(true);
 				}
 			}
 		}
 		return false;
 	}
 
 	/** 
 	 * @方法名：onTouch
 	 * @功能描述：
 	 * @参数：@param v
 	 * @参数：@param event
 	 * @参数：@return
 	 * @throws
 	 */
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		return mGestureDetector.onTouchEvent(event);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if(!isPause){
 			if(keyCode==event.KEYCODE_DPAD_LEFT){
 				valueKeyDown = VALUE_LEFT_KEY ;
 			}else if(keyCode==event.KEYCODE_DPAD_RIGHT){
 				valueKeyDown = VALUE_RIGHT_KEY ;
 			}else if(keyCode==event.KEYCODE_DPAD_DOWN){
 				valueKeyDown = VALUE_DOWN_KEY ;
 			}else if(keyCode==event.KEYCODE_DPAD_UP){
 				valueKeyDown = VALUE_UP_KEY ;
 			}
 		}
 		if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
 			soundPool.play(soundPoolMap.get("move"), volume, volume, 0, 0, 1f);
 			if(tetrisMoveRunnable.isFlag()){
 				isPause = true;
 				tetrisMoveRunnable.setFlag(false);//停止下落线程中的下落动作。但不停止改线程。
 			}else if(!tetrisMoveRunnable.isFlag()){
 				isPause=false;
 				tetrisMoveRunnable.setFlag(true);
 			}
 		}
 		if(keyCode==KeyEvent.KEYCODE_BACK){
 			((MainActivity)context).mHandler.sendEmptyMessage(DIALOG_BACK);
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	
 
 }
