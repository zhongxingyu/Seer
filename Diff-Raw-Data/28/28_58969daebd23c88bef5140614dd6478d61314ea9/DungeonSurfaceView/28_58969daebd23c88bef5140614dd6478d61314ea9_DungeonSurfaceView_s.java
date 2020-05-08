 package rpisdd.rpgme.gamelogic.dungeon.viewcontrol;
 
 import rpisdd.rpgme.gamelogic.dungeon.model.Dungeon;
 import rpisdd.rpgme.gamelogic.player.Player;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.HorizontalScrollView;
 import android.widget.ScrollView;
 
 public class DungeonSurfaceView extends SurfaceView implements
 		SurfaceHolder.Callback, ThreadedSurfaceView {
 
 	private BitmapDrawable background;
 	private ViewThread thread;
 	private AvatarView avatar;
 	private FloorView floor;
 
 	public ScrollView scrollV;
 	public HorizontalScrollView scrollH;
 
 	public DungeonSurfaceView(Context context) {
 		super(context);
 		init(context);
 	}
 
 	public DungeonSurfaceView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 		init(context);
 	}
 
 	public DungeonSurfaceView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 	}
 
 	private void init(Context context) {
 
 		getHolder().addCallback(this);
 
 		setFocusable(true);
 
 	}
 
 	public void setFloorView(Dungeon dungeon) {
 
 		avatar = new AvatarView(0, 0, true, (Activity) getContext());
 
 		floor = new FloorView(dungeon, getResources());
 
 	}
 
 	boolean snapOnce = false;
 
 	/**
 	 * This is the game update method. It iterates through all the objects and
 	 * calls their update method if they have one or calls specific engine's
 	 * update method.
 	 */
 	@Override
 	public void update() {
 
 		/*
 		 * ((Activity) getContext()).runOnUiThread(new Runnable() {
 		 * 
 		 * @Override public void run() { ((MainActivity)
 		 * getContext()).getActionBar(); } });
 		 */
 
 		if (!snapOnce) {
 			snapOnce = true;
 			jumpScrollPosition();
 		}
 
 		floor.update();
 		avatar.update(thread);
 
 		avatar.x = FloorView.indexToCoord(Player.getPlayer().getRoomX());
 		avatar.y = FloorView.indexToCoord(Player.getPlayer().getRoomY());
 
 	}
 
 	@Override
 	public void render(Canvas canvas) {
 
 		// canvas.drawColor(Color.BLACK);
 
 		floor.draw(canvas);
 		avatar.draw(canvas);
 	}
 
 	@Override
 	public void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		if (changed) {
 			if (floor == null) {
 				System.out.println("Waring: Floor's null!");
 			} else {
 				(this).layout(0, 0, (int) floor.width, (int) floor.height);
 			}
 		}
 	}
 
 	public void snapScrollPosition() {
 
 		((Activity) getContext()).runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 
				float canvasX = avatar.x;
				float canvasY = avatar.y;
 
 				float newX = canvasX - (getScreenWidth() / 2f);
 				float newY = canvasY - (getScreenHeight() / 2f);
 
 				scrollH.smoothScrollTo((int) newX, 0);
 				scrollV.smoothScrollTo(0, (int) newY);
 
 			}
 		});
 
 	}
 
 	public void jumpScrollPosition() {
 
 		((Activity) getContext()).runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 
				float canvasX = avatar.x;
				float canvasY = avatar.y;
 
 				float newX = canvasX - (getScreenWidth() / 2f);
 				float newY = canvasY - (getScreenHeight() / 2f);
 
 				scrollH.scrollTo((int) newX, 0);
 				scrollV.scrollTo(0, (int) newY);
 
 			}
 		});
 
 	}
 
 	public int getScreenWidth() {
 		WindowManager wm = (WindowManager) getContext().getSystemService(
 				Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		return size.x;
 	}
 
 	public int getScreenHeight() {
 		return scrollV.getBottom();
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 
 		thread = new ViewThread(getHolder(), this);
 		thread.setRunning(true);
 		thread.start();
 
 		((Activity) getContext()).runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 
 				LayoutParams lp = getLayoutParams();
 				lp.height = (int) floor.height;
 				setLayoutParams(lp);
 
 			}
 		});
 
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d("Debug", "Surface is being destroyed");
 		// tell the thread to shut down and wait for it to finish
 		// this is a clean shutdown
 		boolean retry = true;
 		while (retry) {
 			try {
 				thread.setRunning(false);
 				thread.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			retry = false;
 		}
 		Log.d("Debug", "Thread was shut down cleanly");
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			int touchX = (int) event.getX();
 			int touchY = (int) event.getY();
 
 			// System.out.println(Integer.toString(touchX) + ","
 			// + Integer.toString(touchY));
 
 			if (floor.setRoomTouched(touchX, touchY, avatar,
 					(Activity) getContext())) {
 				snapScrollPosition();
 			}
 
 		}
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 		}
 
 		return true;
 	}
 
 }
