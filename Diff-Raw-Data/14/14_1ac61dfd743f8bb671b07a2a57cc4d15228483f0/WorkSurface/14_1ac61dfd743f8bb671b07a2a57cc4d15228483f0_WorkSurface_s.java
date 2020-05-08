 package com.fresko;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class WorkSurface extends SurfaceView implements SurfaceHolder.Callback {
 
 	public interface TouchCallback {
 		public void handleSelectedUpdate();
 
 		public void handleDestinationUpdate();
 	}
 	private TouchCallback touchCallback;
 
 	Bitmap[][] chunks;
 	Point selected;
 	Point destination;
 	
 	boolean suspended;
 	
 	/** Handle to the application context, used to e.g. fetch Drawables. */
 	private Context mContext;
 
 	static Paint paint_antialisedSolid = new Paint(Paint.ANTI_ALIAS_FLAG);
 	static {
 		paint_antialisedSolid.setStyle(Paint.Style.STROKE);
 		paint_antialisedSolid.setColor(Color.CYAN);
 	}
 	static Paint paint_antialisedDarker = new Paint(Paint.ANTI_ALIAS_FLAG);
 	static {
 		paint_antialisedDarker.setStyle(Paint.Style.FILL);
 		paint_antialisedDarker.setColor(Color.BLACK);
 		paint_antialisedDarker.setAlpha(1);
 	}
 	static Paint paint_Black = new Paint(Paint.ANTI_ALIAS_FLAG);
 	static {
 		paint_Black.setStyle(Paint.Style.FILL);
 		paint_Black.setColor(Color.BLACK);
 	}
 
 	Bitmap buffer = null;
 
 	public WorkSurface(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 
 		// register our interest in hearing about changes to our surface
 		SurfaceHolder holder = getHolder();
 		holder.addCallback(this);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 		// recalculate scaling parameters
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 		// create black buffer by size and paint images on it if exist
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		buffer = null;
 	}
 
 	public void updateAfterEvent() {
 		SurfaceHolder holder = getHolder();
 		Canvas c = holder.lockCanvas(null);
 		updateBuffer();
 		doDraw(c);
 		holder.unlockCanvasAndPost(c);
 	}
 
 	/**
 	 * Draw to buffer
 	 */
 	public void updateBuffer() {
 		Canvas canvas = new Canvas(buffer);
 		float posX = 0;
 		float posY = 0;
 		float chunkWidth = buffer.getWidth()/chunks[0].length;
 		float chunkHeight = buffer.getHeight()/chunks.length;
 		int x = 0;
 		int y = 0;
 		for(Bitmap[] line :chunks) {
 			for(Bitmap chunk: line){
 				canvas.drawBitmap(chunks[y][x++], posX, posY, paint_antialisedSolid);
				posX =+ chunkWidth;
 			}
			y ++; x = 0;
			posY =+ chunkHeight;
 		}
 		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
 				paint_antialisedDarker);
 	}
 
 	/**
 	 * Draw scaled on surface
 	 * 
 	 * @param nativeCanvas
 	 */
 	public void doDraw(Canvas nativeCanvas) {
 		//scale
 		nativeCanvas.drawBitmap(buffer, 0, 0, null);
 	}
 
 	public Bitmap[][] getChunks() {
 		return chunks;
 	}
 
 	public void setChunks(Bitmap[][] chunks) {
 		this.chunks = chunks;
 		buffer = Bitmap.createBitmap(chunks[0][0].getWidth() * chunks[0].length,
 				chunks[0][0].getHeight() * chunks.length, Bitmap.Config.ARGB_8888);
 		updateAfterEvent();
 	}
 
 	public Point getSelected() {
 		return selected;
 	}
 
 	public void setSelected(Point selected) {
 		this.selected = selected;
 	}
 
 	public Point getDestination() {
 		return destination;
 	}
 
 	public void setDestination(Point destination) {
 		this.destination = destination;
 	}
 
 	public Bitmap getImage() {
 		return buffer;
 	}
 	
 	public void swapSelections() {
 		// Change image locations
 		selected = null;
 		destination = null;
 	}
 
 	public TouchCallback getTouchCallback() {
 		return touchCallback;
 	}
 
 	public void setTouchCallback(TouchCallback touchCallback) {
 		this.touchCallback = touchCallback;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (chunks != null) {
 
 			if(suspended) {
 				Log.w("Fresco", "Work surface is in suspended state. Skiping touch event.");
 				return true;
 			}
 			
 			float touched_x = event.getX();
 			float touched_y = event.getY();
 			int action = event.getAction();
 			boolean touched = false;
 			switch (action) {
 				case MotionEvent.ACTION_DOWN:
 					touched = true;
 					break;
 				case MotionEvent.ACTION_MOVE:
 					touched = true;
 					break;
 				case MotionEvent.ACTION_UP:
 					touched = false;
 					break;
 				case MotionEvent.ACTION_CANCEL:
 					touched = false;
 					break;
 				case MotionEvent.ACTION_OUTSIDE:
 					touched = false;
 					break;
 				default:
 			}
 			if(touched) {
 				if(selected == null) {
					selected = new Point((int)touched_x % (int) (getWidth()/chunks[0].length), (int)touched_y % (int) (getHeight()/chunks.length));
 					touchCallback.handleSelectedUpdate();
 				} else if(destination == null) {
					destination = new Point((int)touched_x % (int) (getWidth()/chunks[0].length), (int)touched_y % (int) (getHeight()/chunks.length));
 					updateAfterEvent();
 					new Thread() {
 						public void run() {
 							Bitmap selectedChunk = chunks[selected.y][selected.x];
 							chunks[selected.y][selected.x] = chunks[destination.y][selected.x];
 							chunks[destination.y][selected.x] = selectedChunk;
 							updateAfterEvent();
 						};
 					}.start();
 					touchCallback.handleDestinationUpdate();
 				}
 			}
 		}
 		
 		return super.onTouchEvent(event);
 	}
 }
