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
 		_painter.setColor(_color);
 	}
 
 	public BackgroundView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		_painter.setColor(_color);
 	}
 
 	public BackgroundView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		_painter.setColor(_color);
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
		int height = getHeightForImageHeight(canvas.getHeight());
		canvas.drawLine(0, height, canvas.getWidth(), height, _painter);
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
 		int res = (int)(imageHeight * (1 - ((_altitude - _minHeight) / _offset)));
		Log.d(TAG, String.valueOf(res));
 		return res;
 	}
 }
