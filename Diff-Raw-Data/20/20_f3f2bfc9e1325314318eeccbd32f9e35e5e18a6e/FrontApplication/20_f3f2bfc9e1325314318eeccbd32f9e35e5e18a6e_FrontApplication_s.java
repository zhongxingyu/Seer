 package fr.umlv.escape.front;
 
 import org.jbox2d.dynamics.Body;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.widget.Toast;
 import fr.umlv.escape.R;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.world.Bodys;
 
 /** This class manage if a {@link Drawable} object should still be drawn at the screen or not.
  * {@link Drawable} are not directly drawn by the the DrawableMonitor but through the {@link DrawThread}.
  */
 public class FrontApplication extends SurfaceView{
 	private SurfaceHolder holder;
 	private Gesture gesture;
 
 	public FrontApplication(Context context) {
 		super(context);
 		this.holder = getHolder();
 		callback();
 	}
 
 	private void callback(){    	  
 		getHolder().addCallback(new Callback() {
 			private Thread drawThread;
 			BattleField battleField;
 
 			@Override
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				drawThread.interrupt();
 			}
 
 			@Override
 			public void surfaceCreated(SurfaceHolder arg0) {
 				battleField = new BattleField(getWidth(),getHeight(),BitmapFactory.decodeResource(getResources(), R.drawable.level1));
 
 				Body body = Bodys.createBasicRectangle(200, 200, 200, 200, 1);
 				Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.default_ship_player); 
 				battleField.shipList.add(new Ship("default_ship_player", 100, body, image));
 
 				gesture = new Gesture(getWidth(), getHeight());
 
 				drawThread = new DrawThread(holder, battleField);
 				drawThread.start();
 			}
 
 			@Override
 			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){    				
 				// TODO enelever
 			}
 
 		});
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent arg1) {
 		switch (arg1.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			Point p = new Point((int)arg1.getX(), (int)arg1.getY());
 			Log.wtf(VIEW_LOG_TAG, "Point:"+p.x+":"+p.y);
 			gesture.addPoint(p);
 			break;
 		case MotionEvent.ACTION_MOVE:
 			Point p2 = new Point((int)arg1.getX(), (int)arg1.getY());
 			Log.wtf(VIEW_LOG_TAG, "Point:"+p2.x+":"+p2.y);
 			gesture.addPoint(p2);
 			break;
 		case MotionEvent.ACTION_UP:
 			gesture.detectGesture();
 			switch (gesture.getLastGestureDetected()) {
 			case BACK_OFF:
 				Toast.makeText(getContext(), "BACK_OFF", Toast.LENGTH_SHORT).show();
 				break;
 			case NOT_GESTURE:
 				Toast.makeText(getContext(), "NOT_GESTURE", Toast.LENGTH_SHORT).show();
 				break;
 			case NOT_DETECTED:
 				Toast.makeText(getContext(), "NOT_DETECTED", Toast.LENGTH_SHORT).show();
 				break;
 			case LEFT_DIAG:
 				Toast.makeText(getContext(), "LEFT_DIAG", Toast.LENGTH_SHORT).show();
 				break;
 			case RIGHT_CIRCLE:
 				Toast.makeText(getContext(), "RIGHT_CIRCLE", Toast.LENGTH_SHORT).show();
 				break;
 			default:
 				Toast.makeText(getContext(), "default :(", Toast.LENGTH_SHORT).show();
 				break;
 			}
 
 		default:
 			break;
 		}
 		Log.wtf(VIEW_LOG_TAG, "touch");
 		return true;
 	}
 }
