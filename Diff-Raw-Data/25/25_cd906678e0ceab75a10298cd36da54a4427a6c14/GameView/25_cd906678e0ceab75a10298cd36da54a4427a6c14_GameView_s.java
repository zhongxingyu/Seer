 package org.ivan.simple.game;
 
 import org.ivan.simple.ImageProvider;
 import org.ivan.simple.R;
 import org.ivan.simple.UserControlType;
 import org.ivan.simple.game.hero.Hero;
 import org.ivan.simple.game.level.LevelCell;
 import org.ivan.simple.game.level.LevelView;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView {
 	
 	private static int GRID_STEP;
 	
 	private static int JUMP_SPEED;
 	private static int ANIMATION_JUMP_SPEED;
 	
 	private static int LEFT_BOUND;
 	private static int RIGHT_BOUND;
 	private static int TOP_BOUND;
 	private static int BOTTOM_BOUND;
 	
 	private GameManager gameLoopThread;
 	
 	private Hero hero;
 	private LevelView level;
 	
 	private GameControl control;
 	
 	private int backgroundId;
 	private Bitmap background;
 	private Bitmap pause;
 	private Bitmap restart;
 	private Bitmap back;
 	
 	private LevelCell prevCell;
 //	private Motion prevMotion;
 	
 	private int levId = 0;
 	
 	protected boolean finished = false;
 	private boolean paused = true;
 	
 	public GameView(Context context) {
 		super(context);
 		init();
 	}
 	
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public GameView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	private final void init() {
 		getHolder().addCallback(new SurfaceHolder.Callback() {
 			
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				// turn motion to initial stage (stage == 0)
 				//level.model.getMotion().startMotion();
                 stopManager();
                background = null;
        		pause = null;
        		restart = null;
        		back = null;
 			}
 			
 			public void surfaceCreated(SurfaceHolder holder) {
 				initSurface();
 				if(gameLoopThread == null) {
 					startManager();
 				}
 				gameLoopThread.doDraw(false);
 			}
 			
 			public void surfaceChanged(SurfaceHolder holder, int format, int width,
 					int height) {
 				gameLoopThread.doDraw(false);
 			}
 		});
 
 	}
 	
 	protected void startManager() {
 		gameLoopThread = new GameManager(this);
 		gameLoopThread.setRunning(true);
 		gameLoopThread.start();
 		paused = false;
 	}
 	
 	protected void stopManager() {
 		if(gameLoopThread == null) return;
 		System.out.println("Stop game loop");
 		paused = true;
 		boolean retry = true;
         gameLoopThread.setRunning(false);
         while (retry) {
            try {
                  gameLoopThread.join();
                  retry = false;
            } catch (InterruptedException e) {
         	   
            }
         }
 	}
 	
 	private void initSurface() {
 		background = ImageProvider.getBitmap(backgroundId);
 		pause = ImageProvider.getBitmap(R.drawable.pause);
 		restart = ImageProvider.getBitmap(R.drawable.restart);
 		back = ImageProvider.getBitmap(R.drawable.back);
 		
 		hero = new Hero();
 		
 		GRID_STEP = hero.getSprite().getWidth() % 4 == 0 ? hero.getSprite().getWidth() : (hero.getSprite().getWidth() / 4  + 1) * 4;
 		TOP_BOUND = GRID_STEP;
 		BOTTOM_BOUND = getHeight() - GRID_STEP;
 		BOTTOM_BOUND -= BOTTOM_BOUND % GRID_STEP;
 		// TODO check this bound carefully!
 		LEFT_BOUND = GRID_STEP + 16;
 		RIGHT_BOUND = getWidth() - GRID_STEP;
 		RIGHT_BOUND -= RIGHT_BOUND % GRID_STEP;
 		JUMP_SPEED = GRID_STEP;
 		ANIMATION_JUMP_SPEED = JUMP_SPEED / 8;
 		
 		level = new LevelView(levId, GRID_STEP, LEFT_BOUND, TOP_BOUND);
 		control = new GameControl(this, level.model, hero);
 		prevCell = level.model.getHeroCell();
 //		prevMotion = level.model.getMotion();
 		
 		hero.heroX = LEFT_BOUND + level.model.heroX * GRID_STEP;
 		hero.heroY = TOP_BOUND + level.model.heroY * GRID_STEP;
 	}
 	
 	/**
 	 * Draw hero, level and etc.
 	 */
 	@Override
 	protected void onDraw(Canvas canvas) {
 		onDraw(canvas, false);
 	}
 	
 	protected void onDraw(Canvas canvas, boolean update) {
 		canvas.drawColor(Color.WHITE);
 		canvas.drawBitmap(background, 0, 0, null);
 		canvas.drawBitmap(pause, 10, 50, null);
 		canvas.drawBitmap(restart, 10, 90, null);
 		canvas.drawBitmap(back, 10, 130, null);
 		level.onDraw(canvas, update);
 		hero.onDraw(canvas, update);
 //		level.drawGrid(canvas);
 		drawFPS(canvas);
 		if(level.model.isLost()) {
 			drawLose(canvas);
 		} else if(level.model.isComplete()) {
 			drawWin(canvas);
 		}
 	}
 	
 	private void drawFPS(Canvas canvas) {
 		Paint paint = new Paint(); 
 		paint.setStyle(Paint.Style.FILL); 
 		paint.setTextSize(25); 
 		paint.setColor(Color.BLUE);
 		canvas.drawText("FPS: " + GameManager.getFPS(), 5, 25, paint);
 	}
 	
 	private void drawWin(Canvas canvas) {
 		String complete = "COMPLETE";
 		Paint paint = new Paint(); 		
 		paint.setTextSize(80);
 		Rect textRect = new Rect();
 		paint.getTextBounds(complete, 0, complete.length(), textRect);
 		canvas.rotate(-35, getWidth() / 2, getHeight() / 2);
 		paint.setStyle(Paint.Style.FILL);
 		paint.setColor(Color.RED);
 		canvas.drawText(complete, getWidth() / 2 - textRect.exactCenterX(), getHeight() / 2 - textRect.exactCenterY(), paint);
 		canvas.restore();
 	}
 	
 	private void drawLose(Canvas canvas) {
 		String complete = "GAME OVER";
 		Paint paint = new Paint(); 		
 		paint.setTextSize(80);
 		Rect textRect = new Rect();
 		paint.getTextBounds(complete, 0, complete.length(), textRect);
 		canvas.rotate(-35, getWidth() / 2, getHeight() / 2);
 		paint.setStyle(Paint.Style.FILL);
 		paint.setColor(Color.BLACK);
 		canvas.drawText(complete, getWidth() / 2 - textRect.exactCenterX(), getHeight() / 2 - textRect.exactCenterY(), paint);
 		canvas.restore();
 	}
 	
 	/**
 	 * Checks if game is ready to switch hero animation and/or motion
 	 * @return
 	 */
 	protected boolean readyForUpdate() {
 		// if the level is complete or lost the game should be not updatable on this level 
 		if(level.model.isLost()) return false;
 		if(level.model.isComplete()) return false;
 		
 		boolean inControlState = hero.isInControlState();
 		/*
 		 * Hero is in control state usually when motion animation has ended
 		 * If hero animation is in starting state game model should not be updated
 		 * (after starting animation main animation will be played)
 		 * If hero animation is in finishing state game model should not be updated
 		 * (after finishing animation next motion animation will begin)   
 		 */
 		boolean stateReady = inControlState;
 		// change behavior only if hero is in ready for update state AND is on grid point
 		return stateReady;// && (hero.heroX % GRID_STEP == 0) && (hero.heroY % GRID_STEP == 0);
 	}
 	
 	/**
 	 * Switch hero animation and motion
 	 */
 	protected void updateGame() {
 		// try to end pre/post motion if it exists
 		boolean continued = continueModel();
 		// get new motion type only if it was not obtained yet
 		// (obtained yet means that pre- or post- motion was just ended)
 		if(!continued) {
 			updateModel();
 		}
 	}
 	
 	/**
 	 * Use user control to obtain next motion type, move hero in model (to next level cell),
 	 * switch hero motion animation and cell platforms reaction to this animation  
 	 */
 	private void updateModel() {
 		// Used to remember pressed control (action down performed and no other actions after)
 		if(level.model.getControlType() == UserControlType.IDLE) {
 			level.model.setControlType(control.pressedControl);
 		}
 //		prevMotion = level.model.getMotion();
 		// Store cell before update in purpose to play cell animation (like floor movement while jump) 
 		prevCell = level.model.getHeroCell();
 		// calculate new motion depending on current motion, hero cell and user control
 		level.model.updateGame();
 		// switch hero animation
 		hero.finishPrevMotion(level.model.getMotion(), level.model.getPrevMotion(), prevCell);
 		// play cell reaction to new motion
 		if(!hero.isFinishing()) {
 			hero.switchToCurrentMotion();
 			prevCell.updateCell(level.model.getMotion(), level.model.getPrevMotion());
 		}
 	}
 	
 	/**
 	 * Switch to next animation after pre/post- animation finished
 	 * @return true if pre or post animation ended, otherwise - false 
 	 */
 	private boolean continueModel() {
 		if(hero.isFinishing()) {
 			// when motion at last switches we need to play cell animation
 			if(hero.isFinishingMotionEnded(/*level.model.getPrevMotion()*/)) {
 				hero.switchToCurrentMotion();
 				prevCell.updateCell(level.model.getMotion(), level.model.getPrevMotion());
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Move hero sprite on the screen
 	 */
 	protected void updateHeroScreenPosition() {
 		if(level.model.isLost()) {
 			finished = !moveLose();
 		} else if(level.model.isComplete()) {
 			finished = !hero.playWinAnimation();
 		} else {
 			if(hero.getRealMotion().getType() == MotionType.TP_LEFT || 
 					hero.getRealMotion().getType() == MotionType.TP_RIGHT ||
 					hero.getRealMotion().getType() == MotionType.TP) {
 				hero.heroX = LEFT_BOUND + level.model.heroX * GRID_STEP;
 				hero.heroY = TOP_BOUND + level.model.heroY * GRID_STEP;
 			}
 			int xSpeed = hero.getRealMotion().getXSpeed() * ANIMATION_JUMP_SPEED;
 			int ySpeed = hero.getRealMotion().getYSpeed() * ANIMATION_JUMP_SPEED;
 			
 			hero.heroX += xSpeed;
 			hero.heroY += ySpeed;
 		}
 	}
 	
 	/**
 	 * Random rotating movement if hero was spiked
 	 * @return
 	 */
 	private boolean moveLose() {
 		if((-GRID_STEP < hero.heroX && hero.heroX < getWidth() + GRID_STEP) && (-GRID_STEP < hero.heroY && hero.heroY < getHeight() + GRID_STEP)) {
 			if(hero.getRealMotion().getType() == MotionType.FALL) {
 				hero.heroY += ANIMATION_JUMP_SPEED;
 				return true;
 			}
 			hero.playLoseAnimation();
 			double rand = Math.random();
 			if(rand < 0.33) {
 				hero.heroX += JUMP_SPEED;
 			} else if(rand < 0.66) {
 				hero.heroX -= JUMP_SPEED;
 			}
 			rand = Math.random();
 			if(rand < 0.33) {
 				hero.heroY += JUMP_SPEED;
 			} else if(rand < 0.66) {
 				hero.heroY -= JUMP_SPEED;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isComplete() {
 		return level.model.isComplete();
 	}
 	
 	protected void setLevId(int levId) {
 		this.levId = levId;
 		this.backgroundId = getBackgroundId(levId);
 	}
 	
 	private int getBackgroundId(int levId) {
 		switch(levId) {
 		case 1: return R.drawable.background_l_1;
 		case 2: return R.drawable.background_l_2;
 		case 3: return R.drawable.background_l_3;
 		case 4: return R.drawable.background_l_4;
 		default:return R.drawable.background_l_1;
 		}
 	}
 	
 	protected int getLevId() {
 		return levId;
 	}
 	
 	protected boolean isRunning() {
 		return gameLoopThread.isRunning();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(control.processServiceButton(event)) {
 			return true;
 		}
 		if(!paused) {
 			if(control.oneHandControl(event)) {
 				return true;
 			}
 		}
 		return super.onTouchEvent(event);
 	}
 
 }
