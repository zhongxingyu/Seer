 package com.aragaer.reminder;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Region.Op;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 class DrawView extends View implements OnTouchListener {
 	static final int BITMAP_SIZE = 480;
 
 	static Paint p_grid = new Paint(0x7), p = new Paint(0x7), visible;
 	int size, radius;
 	Bitmap bmp, cache;
 	Canvas c = new Canvas(), cc = new Canvas();
 	Matrix m = new Matrix();
 
 	static {
 		p_grid.setColor(Color.LTGRAY);
 		p_grid.setStyle(Paint.Style.STROKE);
 		p.setColor(Color.WHITE);
 	}
 
 	public DrawView(Context context) {
 		this(context, null);
 	}
 
 	public DrawView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		setFocusableInTouchMode(true);
 		setOnTouchListener(this);
 		setDrawingCacheEnabled(false);
 		setWillNotCacheDrawing(true);
 		bringToFront();
 		reset();
 	}
 
 	public void reset() {
 		bmp = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.RGB_565);
 		c.setBitmap(bmp);
 	}
 
 	public Bitmap getBitmap() {
 		return bmp;
 	}
 
 	public void setBitmap(Bitmap b) {
 		bmp = b;
 		c.setBitmap(bmp);
 	}
 
 	public void setPaint(Paint paint) {
 		visible = paint;
		final int w = getWidth();
		final int h = getHeight();
		if (w > 0 && h > 0)
			cc.clipRect(0, 0, w, h, Op.REPLACE);
		draw_into_cache();
		postInvalidate();
 	}
 
 	protected void onMeasure(int w, int h) {
 		int m_size = Math.min(MeasureSpec.getSize(w), MeasureSpec.getSize(h)) - 1;
 		m_size -= m_size % 4 - 1;
 
 		super.onMeasure(m_size, m_size);
 		setMeasuredDimension(m_size, m_size);
 	}
 
 	public void onDraw(Canvas canvas) {
 //		long start = System.currentTimeMillis();
 		canvas.drawBitmap(cache, 0, 0, null);
 //		Log.d("DRAW", "real draw took "+(System.currentTimeMillis() - start));
 	}
 
 	/* most of the time we're drawing into clipped bitmap */
 	void draw_into_cache() {
 //		long start = System.currentTimeMillis();
 		final int cell = size / 4;
 		cc.drawColor(0, Mode.CLEAR);
 		for (int i = 0; i <= 4; i++) {
 			cc.drawLine(0, cell * i, size, cell * i, p_grid);
 			cc.drawLine(cell * i, 0, cell * i, size, p_grid);
 		}
 		cc.drawBitmap(bmp, m, visible);
 //		Log.d("DRAW", "cache draw took "+(System.currentTimeMillis() - start));
 	}
 
 	float x, y;
 	private void draw_path(float nx, float ny) {
 		float ox = x;
 		float oy = y;
 		x = nx;
 		y = ny;
 		c.drawLine(ox, oy, x, y, p);
 		c.drawCircle(x, y, radius, p);
 		int fx,	fy,	tx, ty;
 		if (x > ox) {
 			fx = (int) ox;
 			tx = (int) x;
 		} else {
 			fx = (int) x;
 			tx = (int) ox;
 		}
 		if (y > oy) {
 			fy = (int) oy;
 			ty = (int) y;
 		} else {
 			fy = (int) y;
 			ty = (int) oy;
 		}
 		draw_through_cache(fx, fy, tx, ty);
 	}
 
 	void draw_through_cache(int l, int t, int r, int b) {
 		l -= radius;
 		t -= radius;
 		r += radius + 1;
 		b += radius + 1;
 		cc.clipRect(l, t, r, b, Op.REPLACE);
 		draw_into_cache();
 		postInvalidate(l, t, r, b);
 	}
 
 	public boolean onTouch(View view, MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			x = event.getX();
 			y = event.getY();
 			c.drawCircle(x, y, radius, p);
 			draw_through_cache((int) x, (int) y, (int) x, (int) y);
 			break;
 		case MotionEvent.ACTION_MOVE:
 		case MotionEvent.ACTION_UP:
 			int history_size = event.getHistorySize();
 			for (int i = 0; i < history_size; i++)
 				draw_path(event.getHistoricalX(i), event.getHistoricalY(i));
 			draw_path(event.getX(), event.getY());
 			break;
 		default:
 			return false;
 		}
 		return true;
 	}
 
 	protected void onSizeChanged(int wn, int hn, int wo, int ho) {
 		super.onSizeChanged(wn, hn, wo, ho);
 		cache = Bitmap.createBitmap(wn, hn, Bitmap.Config.ARGB_8888);
 		cc.setBitmap(cache);
 		size = wn;
 		final float scale = 1f * size / BITMAP_SIZE;
 		m.setScale(scale, scale);
 		c.setMatrix(null);
 		c.scale(1 / scale, 1 / scale);
 		radius = size / 40;
 		p.setStrokeWidth(size / 20);
 		draw_into_cache();
 	}
 }
