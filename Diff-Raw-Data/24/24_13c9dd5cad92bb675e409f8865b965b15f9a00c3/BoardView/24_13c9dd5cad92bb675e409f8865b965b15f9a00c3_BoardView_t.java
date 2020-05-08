 package edu.rochester.nbook.monopdroid;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 public class BoardView extends SurfaceView implements Runnable {
     public enum DrawState {
     	WAIT_JOIN, WAIT_CREATE, CONFIG, INIT, RUN, END
     }
     
     public interface BoardViewListener {
     	public void onConfigChange(String command, String value);
     }
     
     private interface RegionListener {
     	public void onRegionClick(Region region);
     	public void onRegionLongPress(Region region);
     }
     
     private abstract class Region {
     	private String tag;
     	private Rect bounds;
     	
     	public Region(String tag, Rect bounds) {
     		this.tag = tag;
     		this.bounds = bounds;
     	}
     	
     	public String getTag() {
     		return tag;
     	}
     	
     	public Rect getBounds() {
     		return bounds;
     	}
     	
     	public abstract void draw(Canvas canvas);
     }
     
     private class TextRegion extends Region {
     	private Paint textPaint;
     	
     	public TextRegion(String text, Rect bounds, Paint textPaint) {
 			super(text, bounds);
 			this.textPaint = textPaint;
 		}
     	
     	@Override
     	public void draw(Canvas canvas) {
     		canvas.drawText(getTag(), getBounds().left, getBounds().top + 30, textPaint);
     	}
     }
     
     private class DrawableRegion extends Region {
     	private Paint imgPaint;
     	private Bitmap bmp;
 
 		public DrawableRegion(String tag, Rect bounds, Bitmap bitmap, Paint imgPaint) {
 			super(tag, bounds);
 			this.bmp = bitmap;
 			this.imgPaint = imgPaint;
 		}
 		
 		public void setBitmap(Bitmap bmp) {
 			this.bmp = bmp;
 		}
 		
 		@Override
 		public void draw(Canvas canvas) {
 			canvas.drawBitmap(bmp, getBounds().left, getBounds().top, imgPaint);
 		}
     }
     
     private class GestureRegion extends Region {
     	private RegionListener listener;
     	private boolean isPressed;
     	private Paint bgPaint;
     	
 		public GestureRegion(String tag, Rect bounds, Paint paint, RegionListener listener) {
 			super(tag, bounds);
 			this.listener = listener;
 			this.bgPaint = paint;
 		}
 
 		@Override
 		public void draw(Canvas canvas) {
 			if (isPressed) {
 				canvas.drawRoundRect(new RectF(getBounds()), 4f, 4f, bgPaint);
 			}
 		}
 		
 		public void onDown() {
 			isPressed = true;
 		}
 		
 		public void onUp() {
 			isPressed = false;
 		}
 		
 		public void invokeClick() {
 			listener.onRegionClick(this);
 		}
     	
 		public void invokeLongPress() {
 			listener.onRegionLongPress(this);
 		}
     }
     
     // ui thread exclusives
 	private Thread surfaceThread = null;
     private SurfaceHolder surfaceHolder = null;
     private ScaleGestureDetector scaleDetector = null;
     private GestureDetector scrollDetector = null;
     private BoardViewListener thisListener = null;
     
     // draw thread cached images
 	private static Bitmap checkIconChecked = null;
 	private static Bitmap checkIconUnchecked = null;
 	private static Bitmap checkIconCheckedFocused = null;
 	private static Bitmap checkIconUncheckedFocused = null;
 	private static Bitmap checkIconCheckedDisabled = null;
 	private static Bitmap checkIconUncheckedDisabled = null;
 
     // draw thread exclusives (cached data and scale state)
     private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     private Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     
     // used to store draw state across threads
     private volatile boolean running = false;
     private volatile DrawState state = DrawState.WAIT_CREATE;
     private volatile List<Region> regions = new ArrayList<BoardView.Region>();
     private volatile List<Estate> estates = new ArrayList<Estate>();
     private volatile List<Configurable> config = new ArrayList<Configurable>();
     private volatile List<Player> player = new ArrayList<Player>();
     private volatile float offsetX = 0f;
     private volatile float offsetY = 0f;
     private volatile float scale = 1f;
     
     // used by gesture objects to enable/disable scaling/translation
     private float maxScale = 1f;
     private float minScale = 1f;
     private boolean enableTranslate = false;
     private boolean enableScale = false;
     
     public void setBoardViewListener(BoardViewListener listener) {
     	thisListener = listener;
     }
     
     public DrawState getState() {
     	return state;
     }
     
     public void setState(DrawState state) {
     	this.state = state;
 		Log.d("monopd", "surface: state = " + state.toString());
 		synchronized (regions) {
 			regions.clear();
 			offsetX = offsetY = 0f;
 			scale = 1f;
 			enableTranslate = false;
 			enableScale = false;
 			switch (state) {
 			case WAIT_CREATE:
 				regions.add(new TextRegion("Creating game...", new Rect(getWidth() / 3, getHeight() / 2, getWidth(), getHeight()), textPaint));
 				break;
 			case WAIT_JOIN:
 				regions.add(new TextRegion("Joining game...", new Rect(getWidth() / 3, getHeight() / 2, getWidth(), getHeight()), textPaint));
 				break;
 			case CONFIG:
 				enableTranslate = true;
 				enableScale = false;
 				break;
 			case INIT:
 				regions.add(new TextRegion("Starting game...", new Rect(getWidth() / 3, getHeight() / 2, getWidth(), getHeight()), textPaint));
 				break;
 			case RUN:
 				enableTranslate = true;
 				enableScale = true;
 				break;
 			case END:
 				enableTranslate = true;
 				enableScale = true;
 				break;
 			}
 		}
     }
 
 	public void setConfigurables(List<Configurable> configurables) {
 		this.config = configurables;
 
 		synchronized (regions) {
 			regions.clear();
 			int index = 0;
 			for (Configurable config : configurables) {
 				int textWidth = (int) textPaint.measureText(config.getTitle());
 				regions.add(new TextRegion(config.getTitle(), new Rect(45, 10 + (index * 55), 60 + textWidth, 55 + (index * 55)), textPaint));
 				Bitmap check = null;
 				if (config.isEditable()) {
 					if (config.getValue().equals("0")) {
 						check = checkIconUnchecked;
 					} else {
 						check = checkIconChecked;
 					}
 				} else {
 					if (config.getValue().equals("0")) {
 						check = checkIconUnchecked;
 					} else {
 						check = checkIconChecked;
 					}
 				}
 				regions.add(new DrawableRegion("check-" + config.getCommand(), new Rect(5, 15 + (index * 55), 40, 55 + (index * 55)), check, textPaint));
 				if (config.isEditable()) {
 					regions.add(new GestureRegion(config.getCommand(), new Rect(5, 10 + (index * 55), 20 + textWidth, 55 + (index * 55)), highlightPaint, new RegionListener() {
 						@Override
 						public void onRegionLongPress(Region region) {
 							// do nthing on check box long-press
 						}
 						
 						@Override
 						public void onRegionClick(Region region) {
 							if (thisListener != null) {
 								for (Configurable currentConfigurable : BoardView.this.config) {
 									if (currentConfigurable.getCommand().equals(region.getTag())) {
 										thisListener.onConfigChange(currentConfigurable.getCommand(), currentConfigurable.getValue().equals("0") ? "1" : "0");
 									}
 								}
 							}
 						}
 					}));
 				}
 				index++;
 			}
 		}
 	}
     
     public BoardView(Context context, AttributeSet attrs) {
     	super(context, attrs);
     	privateInit();
     }
 
 	public BoardView(Context context, AttributeSet attrs, int defStyle) {
     	super(context, attrs, defStyle);
     	privateInit();
     }
     
     private void privateInit() {
     	surfaceHolder = getHolder();
     	//spaces = new ArrayList<BoardView.Space>(40);
     	scrollDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
 			@Override
 			public boolean onSingleTapUp(MotionEvent e) {
 				int x = (int)e.getX() - (int)offsetX;
 				int y = (int)e.getY() - (int)offsetY;
 				synchronized (regions) {
 					for (Region region : regions) {
 						if (region instanceof GestureRegion) {
 							GestureRegion greg = (GestureRegion) region;
 							if (greg.getBounds().contains(x, y)) {
 								greg.invokeClick();
 								greg.onUp();
 								return true;
 							}
 						}
 					}
 				}
 				return false;
 			}
 			
 			@Override
 			public void onShowPress(MotionEvent e) {
 				int x = (int)e.getX() - (int)offsetX;
 				int y = (int)e.getY() - (int)offsetY;
 				synchronized (regions) {
 					for (Region region : regions) {
 						if (region instanceof GestureRegion) {
 							GestureRegion greg = (GestureRegion) region;
 							if (greg.getBounds().contains(x, y)) {
 								greg.invokeClick();
 								greg.onDown();
 								return;
 							}
 						}
 					}
 				}
 			}
 			
 			@Override
 			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 					float distanceY) {
 				if (enableTranslate) {
 					offsetX -= distanceX;
 					offsetY -= distanceY;
 					return true;
 				} else {
 					return false;
 				}
 			}
 			
 			@Override
 			public void onLongPress(MotionEvent e) {
 				int x = (int)e.getX() - (int)offsetX;
 				int y = (int)e.getY() - (int)offsetY;
 				synchronized (regions) {
 					for (Region region : regions) {
 						if (region instanceof GestureRegion) {
 							GestureRegion greg = (GestureRegion) region;
 							if (greg.getBounds().contains(x, y)) {
 								greg.invokeLongPress();
 								greg.onUp();
 								return;
 							}
 						}
 					}
 				}
 			}
 			
 			@Override
 			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 					float velocityY) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public boolean onDown(MotionEvent e) {
 				return true;
 			}
 		});
 	}
     
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	return scrollDetector.onTouchEvent(event);
     }
     
     public void onResume() {
     	running = true;
     	surfaceThread = new Thread(this);
     	surfaceThread.start();
     }
 
     public void onPause() {
     	boolean retry = true;
     	running = false;
     	while (retry) {
 	    	try {
 	    		surfaceThread.join();
 	    		retry = false;
 	    	} catch (InterruptedException e) {
 	    		e.printStackTrace();
 	    	}
 	    }
     }
     
     /**
      * Returns a square view with maximum size for this area.
      */
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     	int measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec);
     	int measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec);
     	measuredWidth = measuredHeight = Math.min(measuredWidth, measuredHeight);
     	setMeasuredDimension(measuredWidth, measuredHeight);
     }
     
     
     @Override
     public void run() {
 		Log.d("monopd", "surface: Surface thread init");
     	// init cached data
     	bgPaint.setStyle(Style.FILL);
     	bgPaint.setColor(Color.BLACK);
     	textPaint.setStyle(Style.STROKE);
     	textPaint.setColor(Color.WHITE);
 		textPaint.setTextSize(24);
 		highlightPaint.setStyle(Style.STROKE);
 		highlightPaint.setColor(Color.YELLOW);
 		highlightPaint.setStrokeWidth(2f);
     	checkIconChecked = BitmapFactory.decodeResource(getContext().getResources(),
                 android.R.drawable.checkbox_on_background);
     	checkIconUnchecked = BitmapFactory.decodeResource(getContext().getResources(),
                 android.R.drawable.checkbox_off_background);
     	checkIconChecked = BitmapFactory.decodeResource(getContext().getResources(),
                 android.R.drawable.checkbox_on_background);
     	checkIconUnchecked = BitmapFactory.decodeResource(getContext().getResources(),
                 android.R.drawable.checkbox_off_background);
     	checkIconChecked = BitmapFactory.decodeResource(getContext().getResources(),
                 android.R.drawable.checkbox_on_background);
     	checkIconUnchecked = BitmapFactory.decodeResource(getContext().getResources(),
                 android.R.drawable.checkbox_off_background);
 
 		Log.d("monopd", "surface: Completed surface set-up");
 		
 		//Paint oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		//oPaint.setColor(Color.YELLOW);
 		//oPaint.setStyle(Paint.Style.STROKE);
     	while (running) {
     		if (surfaceHolder.getSurface().isValid()) {
     			Canvas canvas = surfaceHolder.lockCanvas();
     			canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), bgPaint);
     			canvas.scale(scale, scale);
     			canvas.translate(offsetX, offsetY);
     			synchronized (regions) {
 	    			for (Region region : regions) {
 	        			//canvas.drawRect(region.getBounds(), oPaint);
 	    				region.draw(canvas);
 	    			}
     			}
     			surfaceHolder.unlockCanvasAndPost(canvas);
     		}
     	}
     }
 }
