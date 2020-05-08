 package wb.receiptslibrary;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.view.MotionEvent;
 import wb.android.camera.CameraPreview;
import wb.receiptspro.R;
 
 public class CustomPreview extends CameraPreview {
 	
 	private boolean _isPictureTaken;
 	private Paint _bPaint;
 	private Bitmap _greenCheck, _redX;
 	private int _width, _height;
 	private MyCameraActivity _cameraActivity;
 	private int _checkLeft, _checkTop, _xLeft, _xTop;
 	
 	public CustomPreview(MyCameraActivity cameraActivity, int widthPixels, int heightPixels) {
 		super(cameraActivity);
 		_cameraActivity = cameraActivity;
 		_isPictureTaken = false;
 		//_yellow = new Paint(Paint.ANTI_ALIAS_FLAG); _yellow.setColor(Color.YELLOW);
 		_bPaint = new Paint(Paint.ANTI_ALIAS_FLAG); 
 		_width = widthPixels; _height = heightPixels;
 		_greenCheck = BitmapFactory.decodeResource(getResources(), R.drawable.greencheck);
 		_redX = BitmapFactory.decodeResource(getResources(), R.drawable.redx);
 		_checkLeft = _greenCheck.getWidth()/2;
 		_checkTop = _height-2*_greenCheck.getHeight();
 		_xLeft = _width - 3*_redX.getWidth()/2;
 		_xTop = _height-2*_redX.getHeight();
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		if (_isPictureTaken) {
 			canvas.drawBitmap(_greenCheck, _checkLeft, _checkTop, _bPaint);
 			canvas.drawBitmap(_redX, _xLeft, _xTop, _bPaint);
 			/*
 			canvas.drawRect(_bl, _yellow);
 			canvas.drawRect(_bm, _yellow);
 			canvas.drawRect(_br, _yellow);
 			canvas.drawRect(_tl, _yellow);
 			canvas.drawRect(_tr, _yellow);
 			canvas.drawRect(_tm, _yellow);
 			canvas.drawRect(_ml, _yellow);
 			canvas.drawRect(_mr, _yellow);
 			canvas.drawLine(_bl.centerX(), _bl.centerY(), _ml.centerX(), _ml.centerY(), _yellow);
 			canvas.drawLine(_ml.centerX(), _ml.centerY(), _tl.centerX(), _tl.centerY(), _yellow);
 			canvas.drawLine(_tl.centerX(), _tl.centerY(), _tm.centerX(), _tm.centerY(), _yellow);
 			canvas.drawLine(_tm.centerX(), _tm.centerY(), _tr.centerX(), _tr.centerY(), _yellow);
 			canvas.drawLine(_tr.centerX(), _tr.centerY(), _mr.centerX(), _mr.centerY(), _yellow);
 			canvas.drawLine(_mr.centerX(), _mr.centerY(), _br.centerX(), _br.centerY(), _yellow);
 			canvas.drawLine(_br.centerX(), _br.centerY(), _bm.centerX(), _bm.centerY(), _yellow);
 			canvas.drawLine(_bm.centerX(), _bm.centerY(), _bl.centerX(), _bl.centerY(), _yellow);
 			*/
 		}
 	}
 	
 	public final boolean onTouchEvent(MotionEvent event) {
 		if (_isPictureTaken) {
 			final int checkRadiusSquared = _greenCheck.getHeight()*8;
 			final int xRadiusSquared = _redX.getHeight()*8;
 			final int checkDx = _checkLeft + _greenCheck.getWidth()/2 - ((int)event.getX());
 			final int checkDy = _checkTop + _greenCheck.getHeight()/2 - ((int)event.getY());
 			final int xDx = _xLeft + _redX.getWidth()/2 - ((int)event.getX());
 			final int xDy = _xTop + _redX.getHeight()/2 - ((int)event.getY());
 			if (checkRadiusSquared > checkDx*checkDx - checkDy*checkDy) {
 				_isPictureTaken = false;
 				invalidate();
 				_cameraActivity.checkCallback();
 				return true;
 			}
 			else if (xRadiusSquared > xDx*xDx - xDy*xDy) {
 				_isPictureTaken = false;
 				invalidate();
 				_cameraActivity.xCallback();
 				return true;
 			}
 		}
 		return false;
 		/*
 		if (_isPictureTaken) {
 			float x = event.getX(), y = event.getY();
 			if (this.containsTouch(_tl, x, y)) {
 				_tl.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_tm, x, y)) {
 				_tm.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_tr, x, y)) {
 				_tr.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_ml, x, y)) {
 				_ml.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_mr, x, y)) {
 				_mr.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_bl, x, y)) {
 				_bl.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_bm, x, y)) {
 				_bm.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			else if (this.containsTouch(_br, x, y)) {
 				_br.set(x, y, x+RECT_DIM, y+RECT_DIM);
 				this.invalidate();
 				return true;
 			}
 			return false;
 		}*/
 	}
 	
 	/*
 	private final boolean containsTouch(RectF rectF, float x, float y) {
 		float dx = rectF.centerX()-x; float dy = rectF.centerY()-y;
 		return RADIUS*RADIUS > dx*dx + dy*dy;
 	}
 	*/
 	public void setPictureTakenCallback(boolean isPictureTaken) {
 		_isPictureTaken = isPictureTaken;
 		if (isPictureTaken) {
 			/*
 			int l = EDGE, t = EDGE, r = _width - EDGE, b = _height - EDGE;
 			int xmid = _width/2, ymid = _height/2;
 			_tl = new RectF(l, t, l+RECT_DIM, t+RECT_DIM);
 			_tm = new RectF(xmid, t, xmid+RECT_DIM, t+RECT_DIM);
 			_tr = new RectF(r, t, r+RECT_DIM, t+RECT_DIM);
 			_ml = new RectF(l, ymid, l+RECT_DIM, ymid + RECT_DIM);
 			_mr = new RectF(r, ymid, r+RECT_DIM, ymid + RECT_DIM);
 			_bl = new RectF(l, b, l+RECT_DIM, b+RECT_DIM);
 			_bm = new RectF(xmid, b, xmid+RECT_DIM, b+RECT_DIM);
 			_br = new RectF(r, b, r+RECT_DIM, b+RECT_DIM);
 			*/
 		}
 		this.invalidate();
 	}
 
 }
