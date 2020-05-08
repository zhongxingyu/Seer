 package org.kvj.quebec4.ui.widget;
 
 import org.kvj.quebec4.R;
 import org.kvj.quebec4.service.DrawingController;
 import org.kvj.quebec4.service.Q4App;
 import org.kvj.quebec4.service.Q4Controller;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class PenCatcher extends View implements OnTouchListener {
 
 	private static final String TAG = "Pen";
 	Canvas c = null;
 	Path path = new Path();
 	Paint pathPaint = new Paint();
 	float strokeWidth = 2;
 	int x = -1, y = -1;
 	boolean erasing = false;
 	private static final float TOUCH_TOLERANCE = 4;
 	static int[] widths = { 2, 4, 7 };
 
 	enum DrawState {
 		Wait, Draw
 	};
 
 	DrawState drawState = DrawState.Wait;
 	private Q4Controller controller = null;
 
 	public PenCatcher(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		setOnTouchListener(this);
 	}
 
 	public boolean onTouch(View view, MotionEvent event) {
 		final int pointerCount = event.getPointerCount();
 		if (MotionEvent.ACTION_DOWN == event.getAction() && pointerCount == 1) {
 			drawState = DrawState.Draw;
 			x = (int) event.getX();
 			y = (int) event.getY();
 			path.reset();
 			path.moveTo(x, y);
 			pathPaint = new Paint();
 			pathPaint.setStyle(Style.STROKE);
			pathPaint.setStrokeCap(Cap.ROUND);
			pathPaint.setStrokeJoin(Join.ROUND);
 			int size = Q4App.getInstance().getIntPreference(R.string.penSize,
 					R.string.penSizeDefault);
 			strokeWidth = widths[0];
 			if (size >= 0 && size < widths.length) {
 				strokeWidth = widths[size];
 			}
 			if (erasing) {
 				pathPaint.setARGB(255, 255, 255, 255);
 				strokeWidth *= 3;
 				pathPaint.setXfermode(new PorterDuffXfermode(
 						PorterDuff.Mode.CLEAR));
 			} else {
 				pathPaint.setARGB(255, 0xdd, 0xdd, 0xdd);
 			}
 			pathPaint.setStrokeWidth(DrawingController.dp2px(getContext(),
 					strokeWidth));
 			return true;
 		}
 		if (MotionEvent.ACTION_MOVE == event.getAction()
 				&& drawState == DrawState.Draw) {
 			int toX = (int) event.getX();
 			int toY = (int) event.getY();
 			float dx = Math.abs(x - toX);
 			float dy = Math.abs(y - toY);
			// if (dx < TOUCH_TOLERANCE && dy < TOUCH_TOLERANCE) {
			// return true;
			// }
 			path.quadTo(toX, toY, (x + toX) / 2, (y + toY) / 2);
 			Rect rect = new Rect();
 			int gap = (int) DrawingController.dp2px(getContext(), strokeWidth);
 			rect.left = Math.min(x, toX) - gap;
 			rect.top = Math.min(y, toY) - gap;
 			rect.right = Math.max(x, toX) + gap;
 			rect.bottom = Math.max(y, toY) + gap;
 			invalidate(rect);
 			x = toX;
 			y = toY;
 			return true;
 		}
 		if (MotionEvent.ACTION_UP == event.getAction()) {
 			drawState = DrawState.Wait;
 			Matrix m = controller.getDrawing().getMatrix(this);
 			Matrix inv = new Matrix();
 			if (m.invert(inv)) {
 				c.setMatrix(inv);
 			}
 			c.drawPath(path, pathPaint);
 			path.reset();
 			return true;
 		}
 		// Log.i(TAG, "Touch: " + event.getAction() + ", " + pointerCount);
 		// Log.i(TAG, "At time: " + event.getEventTime());
 		// for (int p = 0; p < pointerCount; p++) {
 		// Log.i(TAG, String.format("  pointer %d: (%1.2f,%1.2f)",
 		// event.getPointerId(p), event.getX(p), event.getY(p)));
 		// }
 		return true;
 	}
 
 	@Override
 	protected void onAttachedToWindow() {
 		Log.i(TAG, "Attached: " + getWidth() + ", " + getHeight());
 		super.onAttachedToWindow();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		if (null != controller) {
 			canvas.drawBitmap(controller.getDrawing().getPage(), controller
 					.getDrawing().getMatrix(this), new Paint());
 			canvas.drawPath(path, pathPaint);
 		}
 	}
 
 	public void setController(Q4Controller controller) {
 		this.controller = controller;
 		pageChanged();
 	}
 
 	public boolean isErasing() {
 		return erasing;
 	}
 
 	public void setErasing(boolean erasing) {
 		this.erasing = erasing;
 	}
 
 	public void pageChanged() {
 		c = new Canvas();
 		c.setBitmap(controller.getDrawing().getPage());
 		invalidate();
 	}
 
 }
