 package org.av.maruvortex;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 import java.math.*;
 import java.util.*;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class MaruVortex extends Activity {
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(new Panel(this));
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 	}
 
 
 }
 class Panel extends SurfaceView implements SurfaceHolder.Callback{
 	int score = 0;
 	Paint black = new Paint();
 	Paint aA = new Paint(Paint.ANTI_ALIAS_FLAG);
 	long start;
 	private static final String LOG_TAG = "MaruVortex";
 	long t = -1000, f = -1000, g = -100;
 	Paint _paint = new Paint();
 
 	Random r = new Random();
 	HashSet<BoxParticle> squares = new HashSet<BoxParticle>();
 	HashSet<Bullet> bullets = new HashSet<Bullet>();
 	//BoxParticle b = new BoxParticle(r, 400, 400);
 	private DrawThread _thread;
 	private int _x = 20;
 	private int _y = 20;
 	private int bulletid = 0;
 	Matrix matrix = new Matrix();
 	Bitmap test = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
 	Bitmap square = BitmapFactory.decodeResource(getResources(), R.drawable.square);
 	Bitmap bulletBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
 	HashSet<Bullet> rms = new HashSet<Bullet>();
 	HashSet<BoxParticle> rms2 = new HashSet<BoxParticle>();
 	private int sq(int x){
 		return x*x;
 	}
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 		start = SystemClock.elapsedRealtime();
 		_paint.setColor(Color.WHITE);
 		t = SystemClock.elapsedRealtime();
 		_thread.setRunning(true);
 		_thread.start();
 		black.setColor(Color.BLACK);
 
 	}
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		_thread.setRunning(false);
 		while(true) {
 			try {
 				_thread.join();
 				break;
 			}
 			catch (InterruptedException e) {
 
 			}
 		}
 
 	}
 	public Panel(Context context) {
 
 		super(context);
 		getHolder().addCallback(this);
 		_thread = new DrawThread(getHolder(), this);
 		setFocusable(true);
 		// TODO Auto-generated constructor stub
 	}
 	@Override
 	public void onDraw (Canvas canvas){
 		if(canvas==null) return;
 		long nt = SystemClock.elapsedRealtime();
 		if (t < 0) t=nt;
		canvas.drawColor(Color.BLACK);
 		//canvas.drawBitmap(square, b.getx(), b.gety(), null);
 		if(nt-g >= 100) {
 			bullets.add(new Bullet(bulletid++, _x, _y, 100, 100, canvas.getHeight(), canvas.getWidth()));
 			g = nt;
 		}
 
 		if(nt-f >= 1000) {
 			squares.add(new BoxParticle(r, canvas.getHeight(), canvas.getWidth()));
 			f = nt;
 		}
 
 		rms.clear();
 		rms2.clear();
 
 		for (Bullet i : bullets)
 			if (!i.onscreen()) rms.add(i);
 
 		for (BoxParticle i : squares)
 			if(!i.onscreen()) rms2.add(i);
 
 		bullets.removeAll(rms);
 		squares.removeAll(rms2);
 		rms.clear();
 		rms2.clear();
 		for (Bullet i : bullets) {
 			for (BoxParticle j : squares){
 				//Log.d(AVTAG, i.getx() + " " + i.gety() + " " + j.getx() + " " + j.gety());
 				//Log.d(AVTAG, Math.sqrt(sq(i.getx()-j.getx())+sq(i.gety()-j.gety())) + " " + (i.getRadius()+j.getRadius()));
				if(sq(i.getx()-j.getx())+sq(i.gety()-j.gety())<=sq(i.getRadius()+j.getRadius())) {
 
 					rms.add(i);
 					rms2.add(j);
 				    score++;
 				}
 
 			}
 
 		}
 		bullets.removeAll(rms);
 		squares.removeAll(rms2);
 		for (BoxParticle i : squares) {
 			i.update(((double)(nt-t))/1000);
 
 			matrix.setRotate(i.getAngle(),square.getWidth()/2,square.getHeight()/2);
			matrix.postTranslate(i.getx()-square.getWidth()/2, i.gety()-square.getHeight()/2);
 
 			//Log.d(AVTAG, "Position: " + i.getx() + ", " + i.gety());
 
 			canvas.drawBitmap(square, matrix, aA);
 
 			//canvas.drawBitmap(square, i.getx()-testWidth>>1, i.gety()-testHeight>>1, null);				
 			//Log.d(AVTAG, Double.toString(Math.atan2(canvas.getHeight()/2 - _y,  canvas.getWidth()/2 - _x)));
 		}
 		for(Bullet i : bullets){
 			i.update(((double)(nt-t))/1000);
 
 
 			matrix.setRotate(i.getAngle(),bulletBitmap.getWidth()/2,bulletBitmap.getHeight()/2);
			matrix.postTranslate(i.getx()-bulletBitmap.getWidth()/2, i.gety()-bulletBitmap.getHeight()/2);
 			//Log.d(AVTAG, ""+ i.getAngle());
 			//if (i.id==15)
 				//Log.d(AVTAG, "Position:" + i.getx() + " " + i.gety());
 			canvas.drawBitmap(bulletBitmap, matrix, aA);
 			//Log.d(AVTAG,""+ i.getAngle());
 		}
 
 		canvas.drawBitmap(test, _x-test.getWidth()/2, _y-test.getHeight()/2, null);
 		canvas.drawText("Score: " + score, 50, 25, black);
 		t = nt;
 	}
 	@Override
 	public boolean onTouchEvent (MotionEvent event){
 		_x = (int) event.getX();
 		_y = (int) event.getY();
 		return true;
 	}
 }
 class DrawThread extends Thread{
 	private SurfaceHolder _surfaceHolder;
 	private Panel _panel;
 	private boolean _run = false;
 	public DrawThread(SurfaceHolder surfaceHolder, Panel panel){
 		_surfaceHolder = surfaceHolder;
 		_panel = panel;
 
 	}
 	public void setRunning(boolean run){
 		_run = run;
 	}
 	@Override
 	public void run() {
 		Canvas c;
 		while(_run) {
 			c = null;
 			try {
 				c = _surfaceHolder.lockCanvas();
 				synchronized(_surfaceHolder) {
 					_panel.onDraw(c);
 				}
 			}
 			finally {
 				if(c != null)
 					_surfaceHolder.unlockCanvasAndPost(c);
 
 			}
 		}
 	}
 }
