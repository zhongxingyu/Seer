 package com.tenzenway.arduino.toy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class MainView extends View {
 	private static final Point _red = new Point(50, 100);
 	private static final Point _blue = new Point(250, 100);
 	private static final Point _yellow = new Point(150, 300);
 
 	private final List<JoystickListener> _joystickListeners = new ArrayList();
 
 	private Point _joystick = new Point(150, 200);
 	private final int _r = 30;
 	private boolean _move = false;
 	private long _lastMove = 0;
 
 	public MainView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public synchronized void addJoystickListener(JoystickListener listener) {
 		_joystickListeners.add(listener);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent me) {
 		// are we trying to move the "joystick"
 		if (Math.abs(me.getX() - _joystick.x) <= _r
 				&& Math.abs(me.getY() - _joystick.y) <= _r) {
 			if (me.getAction() == MotionEvent.ACTION_DOWN) {
 				_move = true;
 			} else if (me.getAction() == MotionEvent.ACTION_UP) {
 				_move = false;
 			} else if (me.getAction() == MotionEvent.ACTION_MOVE && _move
 					&& (System.currentTimeMillis() - _lastMove > 50)) {
 				_lastMove = System.currentTimeMillis();
 				// call all the listeners
 				new Thread() {
 					@Override
 					public void run() {
 						ColValues updatedColours = new ColValues();
 						for (JoystickListener listener : _joystickListeners) {
 							listener.onMove(updatedColours);
 						}
 					}
 				}.start();
 			}
 		}
 
 		if (_move) {
 			_joystick.set(Math.round(me.getX()), Math.round(me.getY()));
 
 			// force to redraw
 			this.invalidate();
 		}
 
 		return true;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		// draw the fixed circles
 		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		paint.setColor(Color.RED);
 		canvas.drawCircle(_red.x, _red.y, 20, paint);
 		paint.setColor(Color.BLUE);
 		canvas.drawCircle(_blue.x, _blue.y, 20, paint);
		paint.setColor(Color.YELLOW);
 		canvas.drawCircle(_yellow.x, _yellow.y, 20, paint);
 
 		// draw the moving circle (the "joystick")
 		paint.setColor(Color.GRAY);
 		canvas.drawCircle(_joystick.x, _joystick.y, _r, paint);
 	}
 
 	public static interface JoystickListener {
 		void onMove(ColValues newColours);
 	}
 
 	public class ColValues {
 		public final int red;
 		public final int blue;
 		public final int orange;
 
 		public ColValues(int red, int blue, int orange) {
 			this.red = red;
 			this.blue = blue;
 			this.orange = orange;
 		}
 
 		/**
 		 * Calculates the distances from the position of the joystick and base
 		 * colour circles
 		 */
 		public ColValues() {
 			final double maxDist = getDist(_red, _blue);
 
 			this.red = 100 - (int) Math.round(getDist(_joystick, _red) * 100.0
 					/ maxDist);
 			this.blue = 100 - (int) Math.round(getDist(_joystick, _blue)
 					* 100.0 / maxDist);
 			this.orange = 100 - (int) Math.round(getDist(_joystick, _yellow)
 					* 100.0 / maxDist);
 		}
 
 		private int getDist(Point a, Point b) {
 			return (int) Math.round(Math.sqrt(Math.pow(a.x - b.x, 2)
 					+ Math.pow(a.y - b.y, 2)));
 		}
 
 		@Override
 		public String toString() {
 			return "ColValues [red=" + red + ", blue=" + blue + ", orange="
 					+ orange + "]";
 		}
 	}
 }
