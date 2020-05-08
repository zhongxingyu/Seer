 package com.example.canvastest.activities;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AbsoluteLayout;
 import android.widget.Toast;
 import android.widget.AbsoluteLayout.LayoutParams;
 
 import com.example.canvastest.R;
 import com.example.canvastest.utils.SurfaceDataAdapter;
 import com.example.canvastest.utils.SurfaceWidget;
 
 /*
  * http://androidrox.wordpress.com/2011/05/13/android-sample-app-drag-and-drop-image-using-touch/
  * 
  */
 
 @SuppressWarnings("deprecation")
 public class SharedScreenView extends Activity {
 
 	AbsoluteLayout aLayout;
 	StickyWidget current = null;
 	StickyWidget selected = null;
 
 	int _scaleFactor = 6;
 	int _highiestZorder = 0;
 	int _zCounter = 0;
 
 	int MAX_TOUCH_AREA = 6;
 	long TOUCH_HOLD_TIME = 1500;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_shared_screen_view);
 
 		UpdateScreen();
 
 		aLayout = (AbsoluteLayout) findViewById(R.id.absLayout);
 
 		aLayout.setOnTouchListener(new OnTouchListener() {
 
 			float deltaX, deltaY, downX, downY;
 			boolean isMoving = false;
 			boolean isHoldEventNotOccured = true;
 			long downTime;
			int i = 0;
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (current != null) {
 
 					if (event.getAction() == MotionEvent.ACTION_MOVE) {
 
 						if (isMoving) {
 							LayoutParams lp = new LayoutParams(current.getWidth(), current.getHeight(), (int) event.getX() - (int) deltaX, (int) event.getY() - (int) deltaY);
 							current.bringToFront();
 							current.setLayoutParams(lp);
 						}
 
 						if (Math.abs(downX - event.getX()) > MAX_TOUCH_AREA || downY - event.getY() > MAX_TOUCH_AREA)
 							isMoving = true;
 
 						if (!isMoving && SystemClock.elapsedRealtime() - downTime > TOUCH_HOLD_TIME) {
							if(i<1){
								OnWidgetPressAction();
								i++; //Dziala???
							}
 							isHoldEventNotOccured = false;
 						}
 
 					}
 
 					if (event.getAction() == MotionEvent.ACTION_DOWN) {
 						deltaX = event.getX() - current.getX();
 						deltaY = event.getY() - current.getY();
 
 						downX = event.getX();
 						downY = event.getY();
 
 						downTime = event.getDownTime();
 					}
 
 					if (event.getAction() == MotionEvent.ACTION_UP) {
 						_zCounter++;
 						if (current != null)
 							current.UpadatePosition();
 						current = null;
 						isMoving = false;
 					}
 				} else {
 					if (selected != null)
 						selected.draw(selected.canvas);
 					selected = null;
 
 				}
 				return isHoldEventNotOccured;
 
 			}
 
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.shared_screen_view, menu);
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void UpdateScreen() {
 
 		new AsyncTask<Void, Void, List<SurfaceWidget>>() {
 
 			@Override
 			protected List<SurfaceWidget> doInBackground(Void... params) {
 				return SurfaceDataAdapter.GetSurfaceWidgets();
 			}
 
 			@Override
 			protected void onPostExecute(List<SurfaceWidget> widgets) {
 
 				_highiestZorder = widgets.get(widgets.size() - 1).getZorder().intValue();
 				aLayout.removeAllViews();
 
 				for (SurfaceWidget w : widgets) {
 
 					final StickyWidget widgetItem = new StickyWidget(aLayout.getContext(), w);
 					aLayout.addView(widgetItem);
 
 					widgetItem.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							current = widgetItem;
 
 							if (selected != null) {
 								selected.draw(selected.canvas);
 								selected = current;
 							} else {
 								selected = current;
 							}
 							return false;
 						}
 					});
 				}
 
 			};
 
 		}.execute();
 
 	}
	
	public void OnWidgetPressAction(){
		
	}
 
 	private class StickyWidget extends View {
 
 		Paint widgetPaint;
 		String id;
		String content;
		String type;
 		int width;
 		int height;
 		int posX;
 		int posY;
 		Canvas canvas;
 
 		public StickyWidget(Context context, SurfaceWidget sw) {
 			super(context);
 
 			this.id = sw.get_id();
			this.type = sw.getType();
			this.content = sw.getContent();
 			this.width = (int) Math.round(sw.getSize().getWidth().doubleValue()) * _scaleFactor;
 			this.height = (int) Math.round(sw.getSize().getHeight().doubleValue()) * _scaleFactor;
 			this.posX = (int) Math.round(sw.getPos().getLeft().doubleValue()) * _scaleFactor;
 			this.posY = (int) Math.round(sw.getPos().getTop().doubleValue()) * _scaleFactor;
 			this.widgetPaint = new Paint();
 			this.widgetPaint.setColor(Color.parseColor(sw.getBgColour()));
 
 			LayoutParams lp = new LayoutParams(width, height, posX, posY);
 			this.setLayoutParams(lp);
			
 
 		}
 
 		@Override
 		protected void onDraw(Canvas c) {
 			canvas = c;
 			if (this == selected) {
 				DrawSelected();
 			} else {
 				DrawUnselected();
 			}
 		}
 
 		public void UpadatePosition() {
 			final SurfaceWidget sw = new SurfaceWidget();
 			sw.set_id(id);
 			sw.setZorder(_highiestZorder + _zCounter);
 			sw.setPos(sw.new Pos(this.getLeft() / _scaleFactor, this.getTop() / _scaleFactor));
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					SurfaceDataAdapter.EditWidget(sw);
 				}
 			}).start();
 		}
 
 		public void DrawSelected() {
 			canvas.drawRect(0, 0, width, height, widgetPaint);
 			Paint stroke = new Paint();
 			stroke.setStyle(Paint.Style.STROKE);
 			stroke.setColor(Color.GRAY);
 			stroke.setStrokeWidth(4);
 			canvas.drawRect(0, 0, width - 1, height - 1, stroke);
 		}
 
 		public void DrawUnselected() {
 			canvas.drawRect(0, 0, width, height, widgetPaint);
 		}
 
 	}
 
 }
