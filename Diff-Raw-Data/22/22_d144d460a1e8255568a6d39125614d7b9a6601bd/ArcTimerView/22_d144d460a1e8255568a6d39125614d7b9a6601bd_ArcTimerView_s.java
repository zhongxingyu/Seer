 package com.taavo.arctimer;
 
 import java.util.concurrent.Semaphore;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class ArcTimerView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
 
 	private int startTotalAngle = -90; 
 	private int sweepTotalAngle = 360;
 
 	private int startLapAngle = -90; 
 	private int sweepLapAngle = 360;
 
 	private RectF totalRect = new RectF();	
 	private RectF lapRect   = new RectF();
 	private Rect textRect  = new Rect();	
 	
 	private Paint workPaint = new Paint();
 	private Paint workBorderPaint = new Paint();
 	private Paint restPaint = new Paint();
 	private Paint restBorderPaint = new Paint();
 
 	
 	private Paint textPaint = new Paint();
 	private Paint strokePaint = new Paint();
 	
 	private float totalTextX = 0;
 	private float totalTextY = 0;
 	private float lapTextX = 0;
 	private float lapTextY = 0;
 	
 	private String totalText;
 	private String lapText;
 	
 	protected final Semaphore sem = new Semaphore(0);
 	
 	private IntervalSession is;
 	
 	private int totalAngle;
 	private int lapAngle;
 	private long totalTime;
 	private long lapTime;
 	
 	Thread t = null;
 	SurfaceHolder sh;
 	protected boolean run_thread = false;
 	
 	public ArcTimerView(Context context, IntervalSession is) {
 		super(context);
			
 		sh = getHolder();
 		
 		sh.addCallback(this);
 		
 		this.is = is;	
 		
 		calcTextTime();
 		
 		workPaint.setColor(0xff00ff00); 
 		workPaint.setAntiAlias(true);
 
 		restPaint.setColor(0xffff0000);
 		restPaint.setAntiAlias(true);
 		
 		workBorderPaint.setColor(0xffaaffaa); 
 		workBorderPaint.setAntiAlias(true);
 		workBorderPaint.setStyle(Paint.Style.STROKE);
 		workBorderPaint.setStrokeJoin(Paint.Join.ROUND);
 		workBorderPaint.setStrokeWidth(15);
 
 		restBorderPaint.setColor(0xffff8383);
 		restBorderPaint.setAntiAlias(true);
 		restBorderPaint.setStyle(Paint.Style.STROKE);
 		restBorderPaint.setStrokeJoin(Paint.Join.ROUND);
 		restBorderPaint.setStrokeWidth(15);
 		
 		textPaint.setColor(0xffffffff);
 		textPaint.setAntiAlias(true);
 		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/codename_coder_free_4f.ttf"));
 		textPaint.setTextSize(90); // TODO: Set text size dynamically
 		textPaint.getTextBounds(totalText, 0, totalText.length(), textRect);
 		
 
 		strokePaint.setColor(0xff000000);
 		strokePaint.setAntiAlias(true);
 		strokePaint.setTextSize(90);
 		strokePaint.setStyle(Paint.Style.STROKE);
 	    strokePaint.setStrokeWidth(2);
 	    strokePaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/codename_coder_free_4f.ttf"));		
 	}
 	
 
 	
 	public void updateClocks() {
 		totalAngle = (int) is.elapsedTime*360/(int)((is.restCycleTime+is.workCycleTime)*is.totalWorkCycles - is.restCycleTime);
 		lapAngle   = (int) is.elapsedCycleTime*360/(int)(is.currentCycleTime);
 		
 		startTotalAngle = (totalAngle%360 - 90);
 		sweepTotalAngle = (270 - startTotalAngle);
 		
 		startLapAngle = (lapAngle%360 - 90);
 		sweepLapAngle = (270 - startLapAngle);
 		
 		calcTextTime();
 		
 		sem.release();
 	}
 	
 	private void calcTextTime() {
		
 		totalTime = (is.restCycleTime+is.workCycleTime)*is.totalWorkCycles - is.restCycleTime - is.elapsedTime;
 		lapTime   = (is.currentCycleTime - is.elapsedCycleTime);
 		
 		int totalDec = (int) (totalTime%1000)/10;
 		int totalSec = (int) totalTime/1000%60;
 		int totalMin = (int) totalTime/1000/60;
 
 		int lapDec = (int) (lapTime%1000)/10;
 		int lapSec = (int) lapTime/1000%60;
 		int lapMin = (int) lapTime/1000/60;
 		
 		totalText = String.format("%02d:%02d.%02d", totalMin, totalSec, totalDec);
 		lapText = String.format("%02d:%02d.%02d", lapMin, lapSec, lapDec);
 	}
 	
 
	protected void doDraw(Canvas canvas) {
 		canvas.drawRGB(0, 0, 0);
 
 		Paint p = (is.currentCycleType == IntervalSession.TYPE_REST)?restPaint:workPaint;
 		Paint b = (is.currentCycleType == IntervalSession.TYPE_REST)?restBorderPaint:workBorderPaint;
 		
 		canvas.drawArc(totalRect, startTotalAngle, sweepTotalAngle, true, p);
 		canvas.drawArc(totalRect, startTotalAngle, sweepTotalAngle, true, b);
 		
 		canvas.drawArc(lapRect, startLapAngle, sweepLapAngle, true, p);
 		canvas.drawArc(lapRect, startLapAngle, sweepLapAngle, true, b);
 		
 		canvas.drawText(totalText, totalTextX, totalTextY+20, strokePaint);
 		canvas.drawText(totalText, totalTextX, totalTextY+20, textPaint);
 
 		canvas.drawText(lapText, lapTextX, lapTextY+20, strokePaint);
 		canvas.drawText(lapText, lapTextX, lapTextY+20, textPaint);
 	}
 
 
 
 	/**
	 * Calculate dimensions of gfx objects. Called when surface is created. 
 	 * Surface is also created when switching between landscape/portrait 
 	 */
 	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		// Called at least once after surfaceCreated
		
		// Recalculate dimensions
 		
 		float totalX = 0, totalY = 0;
 		float lapX = 0, lapY = 0;
 	
 		float diameter = 0;
 		float scale = (float) 0.90;
 		
 		if ( height > width ) { // portrait - clocks go over & under
 			
 			// smallest dimension determine max radius of circle (otherwise circle overflows or is squashed)
 			if( (height/2) > width) { // width determines max radius
 				diameter = width * scale;
 			} else { // (height/2) determines max radius
 				diameter = (height/2) * scale;	
 			}
 
 			totalY = height/4 - diameter/2;
 			totalX = width/2 - diameter/2; 
 			
 			lapY = height - height/4 - diameter/2;
 			lapX = width/2 - diameter/2;
 			
 			totalTextX = width/2 - textRect.width()/2;
 			totalTextY = height/4;
 			
 			lapTextX = width/2 - textRect.width()/2;
 			lapTextY = height - height/4;
 			
 		} else { // landscape - clocks go side-by-side			
 			if( (width/2) > height) { // height determines max radius				
 				diameter = height * scale;
 			} else { // (width/2) determines max radius
 				diameter = (width/2) * scale;
 			}
 
 			totalY = height/2 - diameter/2;
 			totalX = width/4 - diameter/2;
 			
 			lapY = height/2 - diameter/2;
 			lapX = width - width/4 - diameter/2;
 			
 			totalTextX = width/4 - textRect.centerX();
 			totalTextY = height/2;
 			
 			lapTextX = width - width/4 - textRect.centerX();
 			lapTextY = height/2;
 
 		}					
 		totalRect.set(totalX, totalY, totalX + diameter, totalY + diameter);
 		lapRect.set(lapX, lapY, lapX + diameter, lapY + diameter);		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		run_thread = true;
 		t = new Thread(this);
 		t.start();
 		sem.release();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		run_thread = false;
 		sem.release();		
 		while(true) {
 			try{
 				t.join();
 				break;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		t = null;
 	}
 	
 	/**
 	 * Gfx thread. Blocks until semaphore is released by updateClocks(), 
 	 * then locks canvas and draws.
 	 */
 	@Override
 	public void run() {
 		Canvas c = null;
 		while(run_thread) {
 			c = null;
 			try { // Block until semaphore is released
 				sem.acquire();
 				c = sh.lockCanvas();			
 				if (c != null) {
 					doDraw(c);
 				}			
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} finally {
 				if (c != null) {
 					sh.unlockCanvasAndPost(c);
 				}
 			}
 		}
 	}
 }
