 package com.example.webviewscroll;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.RelativeLayout;
 
 @SuppressLint("NewApi")
 public class MainActivity extends Activity {
 
 	MainPageLayout mContentView = null;
 	AddressBar mAddressBar = null;
 	WebViewZoom mWebViewZoom = null;
 	
 	Handler mHandler = null;
 	
 	private void setupContentView() {
 		if (mContentView != null) {
 			return;
 		}
 		
 		mContentView = new MainPageLayout(this) {
 			@Override
 			public boolean dispatchTouchEvent(MotionEvent ev) {
 				if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
 					touching = false;
 				} else {
 					animating = false;
 					touching = true;
 				}
 				return super.dispatchTouchEvent(ev);
 			}
 		};
 		
 		mAddressBar = new AddressBar(this);
 		mWebViewZoom = new WebViewZoom(this);
 		mWebViewZoom.setOnTouchListener(new View.OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				Log.i("mWebViewZoom", "" + v.toString() + "======" + event.toString());
 				return false;
 			}
 		});
 
 		mContentView.addView(mWebViewZoom);
 		mContentView.addView(mAddressBar, new RelativeLayout.LayoutParams(500, 60));
 
 		mWebViewZoom.setTopBar(new WebViewZoom.TopBarInterface() {
 
 			@Override
 			public int getHeight() {
 				return mAddressBar.getContentHeight();
 			}
 		});
 		
 		mWebViewZoom.setOnContentMovedListener(new WebViewZoom.OnContentMovedListener() {
 			
 			@Override
 			public void onContentMoved(int dx, int dy) {
 				Log.i("onContentMoved", "OnScrollListener===== dy = " + dy);
 				
 				int newBottom = mAddressBar.getContentBottom() + dy;
 				if (0 <= newBottom && newBottom <= mAddressBar.getContentHeight()) {
 					mAddressBar.moveContent(0, dy);
 					
 					Log.i("onContentMoved", "onContentMoved");
 				}
 			}
 		});
 		
 		mWebViewZoom.setWebViewScrollListener(new WebViewZoom.WebViewScrollListener() {
 			
 			@Override
 			public void onWebViewScroll(int l, int t, int oldl, int oldt) {
 
 				if (touching) {
 					return;
 				}
 				
 				lastY2 = lastY;
 				Y2TimeStampMS = YTimeStampMS;
 				lastY = t;
 				YTimeStampMS = System.currentTimeMillis();
 				
 				
 				boolean shouldAnimate = true;
 				
 				if (animating) {
 					shouldAnimate = false;
 				}
 				
 				if (  shouldAnimate
 				   && lastY != SCROLL_INVALID_VALUE
 				   && lastY2 != SCROLL_INVALID_VALUE
 				   && YTimeStampMS != TIME_INVALID_VALUE
 				   && Y2TimeStampMS != TIME_INVALID_VALUE) {
 					pxPerSec = (1.0f * lastY - lastY2) * 1000 / (Y2TimeStampMS - YTimeStampMS);
 				} else {
 					shouldAnimate = false;
 				}
 				
 				if (shouldAnimate && pxPerSec > 0) {
 					;
 				} else {
 					shouldAnimate = false;
 				}
 				
 				if (shouldAnimate && pxPerSec > 0) {
 					shouldAnimate = true;
 				} else {
 					shouldAnimate = false;
 				}
 
 				int guessNextScrollY = t - (lastY2 - lastY);
 
 				if (shouldAnimate && guessNextScrollY <= 0) {
 					shouldAnimate = true;
 				} else {
 					shouldAnimate = false;
 				}
 
 				if (shouldAnimate) {

					mWebViewZoom.flingScroll(0, 0);
 					
 					if (mAddressBar != null) {
 						addressBarHeightCache = mAddressBar.getContentHeight();
 					}
 					
 					final float spd = pxPerSec;
 
 					animating = true;
 					mHandler.postDelayed(new Runnable() {
 						private void runImpl() {
 							
 //							if (spd > 400) {
 //								mWebViewZoom.moveContent(0, mAddressBar.getContentHeight(), -1);
 //								mAddressBar.showWithAnimation();
 //							}
 //							
 ////							animating = true;
 //							
 							float distance = spd * spd / pxPerSecDecrease / 2;
 							if (distance > addressBarHeightCache) {
 								distance = addressBarHeightCache;
 							}
 							float sec = 2 * distance / spd;
 
 							Log.i("toanimate", String.format("distance = %f, sec = %f", distance, sec));
 
 							mAddressBar.showWithAnimation((int)(sec * 1000 + 0.5));
 							if (sec < 50 && spd > 1000) {
 								mWebViewZoom.moveContent(0, (int)(distance + 0.5), -1);
 							} else {
 								mWebViewZoom.moveContent(0, (int)(distance + 0.5), (int)(sec * 1000 + 0.5));
 							}
 //							mWebViewZoom.moveContent(0, (int)(distance + 0.5), -1);
 //							mWebViewZoom.moveContent(0, 60, 3000);
 //							mAddressBar.moveContent(0, (int)(distance + 0.5), (int)(sec * 1000 + 0.5));
							mWebViewZoom.scrollWebViewBy(0, -mWebViewZoom.getWebViewScrollY());
							
 						}
 						@Override
 						public void run() {
 							runImpl();
 						}
 					}, 0);
 				}
 			}
 		});
 		
 		setContentView(mContentView);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		mHandler = new Handler();
 		
 		setupContentView();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		
 
 		return true;
 	}
 
 	final static int SCROLL_INVALID_VALUE = ~0;
 	final static long TIME_INVALID_VALUE = ~0L;
 	int lastY = SCROLL_INVALID_VALUE;
 	long YTimeStampMS = TIME_INVALID_VALUE;
 	int lastY2 = SCROLL_INVALID_VALUE;
 	long Y2TimeStampMS = TIME_INVALID_VALUE;
 	float pxPerSec = 0; 
 	float pxPerSecDecrease = 10;
 	float deceleratorParam = 1.0f;
 	int addressBarHeightCache = 0;
 	boolean animating = false;
 	
 	boolean touching = true;
 }
