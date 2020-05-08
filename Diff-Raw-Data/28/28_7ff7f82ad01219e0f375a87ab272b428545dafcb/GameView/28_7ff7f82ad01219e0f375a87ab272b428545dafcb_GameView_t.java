 package com.game.pong;
 
 import java.lang.Thread.State;
 import com.example.pong.R;
 import com.game.math.Vector2;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Paint.Style;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 
 
 	class GameThread extends Thread {
 
 		//states 
 		public static final int STATE_LOSE = 1;
 
 		public static final int STATE_PAUSE = 2;
 
 		public static final int STATE_READY = 3;
 
 		public static final int STATE_RUNNING = 4;
 
 
 		//padding for how far from the bottom or top
 		static final int PADDING = 100;
 
 		static final int WINPOINT = 3;
 
 		private Paint m_paint;
 
 		private Player m_player1;
 
 		private Player m_player2;
 
 		private Ball m_ball;
 
 		//user interface class
 		private UserInterface m_UI;
 
 		//canvas dimensions
 		private int m_canvasWidth;
 
 		private int m_canvasHeight;
 
 		private long m_lastTime;
 
 		private int m_mode;
 
 		private boolean m_run = false;
 
 		// Handle to the surface manager
 		private SurfaceHolder m_surfaceHolder;
 
 		public GameThread(SurfaceHolder surfaceHolder, Context context,
 				Handler handler) {
 
 			m_surfaceHolder = surfaceHolder;
 			m_handler = handler;
 			m_context = context;
 
 			DisplayMetrics metrics = m_context.getResources().getDisplayMetrics();
 			m_canvasWidth = metrics.widthPixels;
 			m_canvasHeight = metrics.heightPixels;
 
 			m_paint = new Paint();
 
 			m_ball = new Ball(new Vector2(m_canvasWidth /2, m_canvasHeight/2), 5.0f, Color.WHITE);
 			m_ball.setVelocity(new Vector2(0, 8));
 			m_ball.setSpeed(10);
 
 			m_player1 = new Player(m_context, new Vector2(90, 10), new Vector2(m_canvasWidth/2, m_canvasHeight - PADDING), R.drawable.paddle, m_canvasWidth, true);
 			m_player1.isPlayer(true);
 
 			m_player2 = new Player(m_context, new Vector2(90, 10), new Vector2(m_canvasWidth/2, PADDING), R.drawable.paddle2, m_canvasWidth, false);
 
 			m_UI = new UserInterface(m_context, m_canvasWidth, m_canvasHeight);
 			
 		}
 
 		//initalise the game
 		public void doStart() {
 			synchronized (m_surfaceHolder) {
 
 				resetGame();
 				m_lastTime = System.currentTimeMillis() + 100;
 				setState(STATE_RUNNING);
 			}
 		}
 		
 		public void pause() {
 			synchronized (m_surfaceHolder) {
 				if (m_mode == STATE_RUNNING) 
 					setState(STATE_PAUSE);
 			}
 		}
 
 		@Override
 		public void run() {
 
 
 			while (m_run) {
 				Canvas c = null;
 				try {
 					c = m_surfaceHolder.lockCanvas(null);
 					synchronized (m_surfaceHolder) {
 						if (m_mode == STATE_RUNNING){
 							updateGame();
 						}
 						doDraw(c);
 					}
 				}catch(Exception e){}
 				finally {
 
 					if (c != null) {
 						m_surfaceHolder.unlockCanvasAndPost(c);
 					}
 				}
 			}
 
 		}
 
 
 		public void setRunning(boolean b) {
 			m_run = b;
 		}
 
 
 		public void setState(int mode) {
 			synchronized (m_surfaceHolder) {
 				setState(mode, null);
 			}
 		}
 
 
 		public void setState(int mode, CharSequence message) {
 			synchronized (m_surfaceHolder) {
 				m_mode = mode;
 			}
 		}
 
 		public void setPlayers(boolean onePlayer){
 			m_player2.isPlayer(onePlayer);
 		}
 
 
 		public void setSurfaceSize(int width, int height) {
 
 			synchronized (m_surfaceHolder) {
 				m_canvasWidth = width;
 				m_canvasHeight = height;
 			}
 		}
 
 
 		public void unpause() {
 
 			synchronized (m_surfaceHolder) {
 				m_lastTime = System.currentTimeMillis() + 100;
 			}
 			setState(STATE_RUNNING);
 		}
 
 
 		private void doDraw(Canvas canvas) {
 
 			canvas.drawARGB(255, 0, 0, 0);
 			m_UI.draw(canvas, m_paint);
 			if(!foundWinner()){
 				m_paint.setStyle(Style.FILL);
 
 				m_player1.draw(canvas, m_paint);
 				m_player2.draw(canvas, m_paint);
 				m_ball.draw(canvas, m_paint);
 
 			}
 			else {
 				m_UI.showSplashScreen(true);
				m_thread.pause();
 			}
 			
 			if(m_mode == STATE_READY){
 				canvas.drawText("Start game through Menu", m_canvasWidth /3, m_canvasHeight/3, m_paint);
 			}
 
 		}
 
 		private void updateGame() {
 
 			long now = System.currentTimeMillis();
 
 			if (m_lastTime > now) 
 				return;
 			double elapsed = (now - m_lastTime) / 1000.0;
 			m_lastTime = now;
 
 			m_ball.update(elapsed);
 
 			if (m_ball.getPosition().x > m_canvasWidth - m_ball.getRadius() * 3 || 
 					m_ball.getPosition().x < 0 + m_ball.getRadius() * 3) {
 				m_ball.inverseX();
 			}
 
 			if (m_ball.getPosition().y < 0) {
 				m_player1.addPoints();
 				m_UI.updateScore(true);//true for player 1
 				m_ball.resetBall(new Vector2(m_canvasWidth, m_canvasHeight));
 				m_player2.randomizeSpeed();
 			}
 			else if(m_ball.getPosition().y > m_canvasHeight){
 				m_player2.addPoints();
 				m_UI.updateScore(false);
 				m_ball.resetBall(new Vector2(m_canvasWidth, m_canvasHeight));
 			}
 
 			if (collided(m_player1.getRectangle())) {
 				if (m_ball.getPosition().x < m_player1.leftSide()) {
 					m_ball.setVelocity(-m_ball.getVelocity().y, -m_ball.getVelocity().y );
 				}
 				if (m_ball.getPosition().x > m_player1.rightSide()){
 
 					m_ball.setVelocity(m_ball.getVelocity().y, -m_ball.getVelocity().y );
 				}
 				if (m_ball.getPosition().x <= m_player1.rightSide() &&
 						m_ball.getPosition().x >= m_player1.leftSide()) {
 
 					m_ball.inverseY();
 				}
 			}
 			if (collided(m_player2.getRectangle())) {
 
 				if (m_ball.getPosition().x < m_player2.leftSide()) {
 					m_ball.setVelocity(m_ball.getVelocity().y, -m_ball.getVelocity().y );
 				}
 				if (m_ball.getPosition().x > m_player2.rightSide()){
 					m_ball.setVelocity(-m_ball.getVelocity().y, -m_ball.getVelocity().y );
 				}
 				if (m_ball.getPosition().x <= m_player2.rightSide() &&
 						m_ball.getPosition().x >= m_player2.leftSide()) {
 
 					m_ball.inverseY();
 				}
 				m_player2.randomizeSpeed();
 			}
 
 			if (!m_player2.isPlayer()) {
 				m_player2 = doAI(m_player2);
 			}
 
 		}
 
 		private Player doAI(Player ai){
 			
 			float middle = ai.getRectangle().centerX();
 			float minRange = (m_canvasWidth / 2);
 			float maxRange = (m_canvasWidth / 2) - (ai.getDimensions().x);
 			float width = (ai.getDimensions().x);
 
 
 			if(m_ball.getPosition().y < m_canvasHeight/2)
 			{
 				if (m_ball.getVelocity().y > 0) {
 
 					if (middle < minRange - width) {
 						ai.move(ai.getSpeed());
 					}
 					else if (middle > maxRange + width){
 						ai.move(-ai.getSpeed());
 					}
 				}
 				else if(m_ball.getVelocity().y < 0)
 				{
 					if (middle != m_ball.getPosition().x) {
 
 						if (m_ball.getPosition().x < middle - 10) {
 							ai.move(-ai.getSpeed());
 
 						}
 						else if(m_ball.getPosition().x > middle + 10)
 						{
 							ai.move(ai.getSpeed());
 						}
 
 					}
 
 				}
 			}
 			return ai;
 		}
 
 		private boolean collided(Rect rectangle){
 			
 			float bx = m_ball.getPosition().x; 
 			float by = m_ball.getPosition().y;
 			float radius = m_ball.getRadius();
 			float rl = rectangle.left;
 			float rr = rectangle.right;
 			float rt = rectangle.top;
 			float rb = rectangle.bottom;
 
 			if (by + radius>= rt &&
 					by <= rb &&
 					bx <= rr && 
 					bx >= rl ) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public boolean foundWinner(){
 			
 			return (m_player1.getPoints() > WINPOINT-1 || m_player2.getPoints() > WINPOINT-1);
 		}
 
 		public void resetGame(){
 			
 			m_ball.resetBall(new Vector2(m_canvasWidth, m_canvasHeight));
 			m_player1.resetPlayer();
 			m_player2.resetPlayer();
 			m_player2.isPlayer(m_player2.isPlayer());
 			m_UI.resetScore();
 			m_UI.showSplashScreen(false);
 			setState(STATE_RUNNING);
 		}
 
 		public void handleInput(MotionEvent event){
 	
 			InputHandler ih = InputHandler.getInstance();
 			
 			for(int i = 0; i < ih.getTouchCount(event); i++) {
 
 				int x = (int) ih.getX(event, i);
 				int y = (int) ih.getY(event, i);
 
 				switch (event.getAction()){
 				case MotionEvent.ACTION_DOWN:
 
 					if(m_player1.getHandle().contains(x, y)){
 						m_player1.setSelected(true);
 					}
 
 					if(m_player2.getHandle().contains(x, y)){
 						m_player2.setSelected(true);
 						m_player2.isPlayer(true);
 					}
 
 
 					break;
 				case MotionEvent.ACTION_MOVE:
 					if (m_player1.isSelected()) {
 						m_player1.setPosition(x - (m_player1.getDimensions().x/2), m_player1.getPosition().y);
 					}
 					if (m_player2.isSelected()) {
 						m_player2.setPosition(x - (m_player2.getDimensions().x/2), m_player2.getPosition().y);
 					}
 					break;
 				case MotionEvent.ACTION_UP:
 					m_player1.setSelected(false);
 					m_player2.setSelected(false);
 					if (foundWinner()) {
						resetGame();
						doStart();
 					}
 					break;
 
 				}
 			}
 		}
 	}
 
 	private Context m_context;
 
 	private GameThread m_thread;
 	
 	private Handler m_handler;
 
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 
 		SurfaceHolder holder = getHolder();
 		holder.addCallback(this);
 		
 		m_handler = new Handler() {
 			@Override
 			public void handleMessage(Message m) {
 
 				Bundle b = m.getData();
 				MotionEvent e = b.getParcelable("event");
 				m_thread.handleInput(e);
 			}
 		};
 
 		m_thread = new GameThread(holder, context, m_handler);
 		setFocusable(true); 
 	}
 
 
 	public GameThread getThread() {
 		return m_thread;
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasWindowFocus) {
 		if (!hasWindowFocus)
 			m_thread.pause();
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		m_thread.setSurfaceSize(width, height);
 	}
 
 
 	public void surfaceCreated(SurfaceHolder holder) {
 
 		if(m_thread.getState() == State.TERMINATED)
 		{
			m_thread = new GameThread(getHolder(), m_context, m_handler);
 			m_thread.setRunning(true);
 			m_thread.start();
			m_thread.doStart();
 		}
 		else
 		{
 			m_thread.setRunning(true);
 			m_thread.start();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 
 		boolean retry = true;
 		m_thread.setRunning(false);
 		while (retry) {
 			try {
 
 				m_thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 			}
 		}
 
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		Message m = m_handler.obtainMessage();
 		
 		Bundle bundle = new Bundle();
 		bundle.putParcelable("event", event);
 		
 		m.setData(bundle);
 		m_handler.sendMessage(m);
 		
 		return true;
 
 	}
 }
