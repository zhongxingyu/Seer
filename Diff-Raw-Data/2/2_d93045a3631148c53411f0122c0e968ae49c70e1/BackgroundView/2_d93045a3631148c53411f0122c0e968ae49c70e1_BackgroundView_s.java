 package com.mpl.altimeter;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 
 import com.trevorpage.tpsvg.SVGView;
 
 public class BackgroundView extends SVGView {
 	private static final String	TAG = "BackgroundView";
 	private double				_altitude = 0;
 	private Paint				_painter = new Paint();
 	private static int			_color = Color.YELLOW;
 	private static int			_maxHeight = 9000;
 	private static int			_minHeight = -4947;
 	private static int			_offset = _maxHeight - _minHeight;
 
 	public BackgroundView(Context context) {
 		super(context);
 		init();
 	}
 
 	public BackgroundView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public BackgroundView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	private void init() {
 		_painter.setColor(_color);
 		setFill(true);
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		// Get the real displayed image size
 		int realHeight = getHeight();
 		int realWidth = getWidth();
		boolean higher = getHeight() / getWidth() > 1280 / 800;
 		if (higher)
 			realWidth = 800 * getHeight() / 1280;
 		else
 			realHeight = 1280 * getWidth() / 800;
 		Log.d(TAG, "real w: " + realWidth);
 		Log.d(TAG, "real h: " + realHeight);
 		int height = getHeightForImageHeight(realHeight);
 		canvas.drawLine(0, height, realWidth, height, _painter);
 	}
 	
 	public void setAltitude(double altitude) {
 		if (altitude > _maxHeight)
 			altitude = _maxHeight;
 		else if (altitude < _minHeight)
 			altitude = _minHeight;
 		_altitude = altitude;
 		invalidate();
 	}
 	
 	private int getHeightForImageHeight(int imageHeight) {
 		Log.d(TAG, "image height: " + imageHeight);
 		int res = (int)(imageHeight * (1 - ((_altitude - _minHeight) / _offset)));
 		Log.d(TAG, String.valueOf(res) + " (" + _altitude + "m)");
 		return res;
 	}
 }
