 package com.example.webviewscroll;
 
 import java.util.IllegalFormatCodePointException;
 
 import android.R.integer;
 import android.animation.AnimatorSet;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.Animatable;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.Interpolator;
 import android.view.animation.TranslateAnimation;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AbsoluteLayout;
 import android.widget.OverScroller;
 import android.widget.Scroller;
 
 public class WebViewZoom extends AbsoluteLayout {
 
 	WebView mWebView = null;
 	AbsoluteLayout.LayoutParams mWebViewLayout = null;
 	Handler mHandler = null;
 	
 	interface OnContentMovedListener {
 		abstract public void onContentMoved(int dx, int dy);
 	}
 	
 	private OnContentMovedListener mOnContentMovedListener = null;
 	
 	public void setOnContentMovedListener(OnContentMovedListener onContentMovedListener) {
 		mOnContentMovedListener = onContentMovedListener;
 	}
 //	
 	public interface TopBarInterface {
 		abstract public int getHeight();
 	}
 	
 	private TopBarInterface mTopBar = null;
 	
 	public void setTopBar(TopBarInterface topBar) {
 		mTopBar = topBar;
 	}
 
 	public interface WebViewScrollListener {
 		abstract public void onWebViewScroll(int l, int t, int oldl, int oldt);
 	}
 	
 	public void setWebViewScrollListener(WebViewScrollListener webViewScrollLinstener) {
 		mWebViewScrollListener = webViewScrollLinstener;
 	}
 	
 	WebViewScrollListener mWebViewScrollListener = null;
 	
 	public WebViewZoom(Context context) {
 		super(context);
 		init();
 	}
 
 	public WebViewZoom(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public WebViewZoom(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	private void init() {
 		
 		setBackgroundColor(0);
 		
 		if (mWebView == null) {
 			
 			mHandler = new Handler();
 			
 			mWebView = new WebView(getContext()) {
 				
 				float dragBeginY = 0;
 				int contentMovedY = 0;
 
 				int topBarHeightCache = 0;
 				
 				@Override
 				public boolean onTouchEvent(MotionEvent event) {
 					
 					if (mTopBar == null) {
 						return super.onTouchEvent(event);
 					}
 					
 					boolean result = false;
 					
 					int dy = (int)(event.getY() - dragBeginY - contentMovedY + 0.5);
 					if (event.getAction() == MotionEvent.ACTION_DOWN) {
 						dragBeginY = event.getY();
 						contentMovedY = 0;
 						result = super.onTouchEvent(event);
 					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 						int contentTop = getContentTop() + dy;
 						if (contentTop <= 0) {
 							dy = -getContentTop();
 						} else if (mTopBar.getHeight() <= contentTop) {
 							dy = mTopBar.getHeight() - getContentTop();
 						}
 						contentTop = getContentTop() + dy;
 						if (dy > 0 && getScrollY() <= 0 || dy < 0 && getScrollY() > 0) {
 							moveContent(0, dy);
 							dragBeginY -= dy;
 							contentMovedY += dy;
 							result = true;
 						} else {
 							dragBeginY = event.getY();
 							contentMovedY = 0;
 						}
 					} else if (event.getAction() == MotionEvent.ACTION_UP) {
 						;
 					}
 					if (!result) {
 						result = super.onTouchEvent(event);
 					}
 					return result;
 				}
 
 				@Override
 				protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 					
 					super.onScrollChanged(l, t, oldl, oldt);
 					
 					if (mWebViewScrollListener != null) {
 						mWebViewScrollListener.onWebViewScroll(l, t, oldl, oldt);
 					}
 					
 				}
 			};
 			mWebViewLayout = new AbsoluteLayout.LayoutParams(500, 600 + 60, 0, 60);
 			this.addView(mWebView, mWebViewLayout);
 			mWebView.loadUrl("http://m.sina.com.cn");
 			mWebView.setWebViewClient(new WebViewClient() {
 				@Override
 				public boolean shouldOverrideUrlLoading(WebView view, String url) {
 					view.loadUrl(url);
 					return true;
 				}
 			});
 //			mWebView.setBackgroundColor(0);
 		}
 	}
 
 	public int getContentTop() {
 		return mWebViewLayout.y;
 	}
 
 	public int getWebViewScrollY() {
 		if (mWebView != null) {
 			return mWebView.getScrollY();
 		} else {
 			return 0;
 		}
 	}
 
 	public void scrollWebViewBy(int dx, int dy) {
 		if (mWebView != null) {
 			mWebView.scrollBy(dx, dy);
 		}
 	}
 
 	public void moveContent(final int dx, final int dy, int animationDurationMS) {
 		moveContent(dx, dy, animationDurationMS, null);
 	}
 	
 	public void moveContent(final int dx, final int dy, int animationDurationMS, Interpolator inter) {
 //		animationDurationMS *= 10;
 		
 		if (animationDurationMS < 0) {
 			mWebViewLayout.x += dx;
 			mWebViewLayout.y += dy;
 			requestLayout();
 			if (mOnContentMovedListener != null) {
 				mOnContentMovedListener.onContentMoved(dx, dy);
 			}
 		} else {
 			moveContent(dx, dy, -1);
 			TranslateAnimation anim = new TranslateAnimation(-dx, 0, -dy, 0);
 			anim.setDuration(animationDurationMS);
 			anim.setInterpolator(inter == null ? new DecelerateInterpolator(0.6f) : inter);
 			anim.setAnimationListener(new Animation.AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation animation) {
 					Log.i("webview", "start");
 				}
 				@Override
 				public void onAnimationRepeat(Animation animation) {
 					Log.i("webview", "rep");
 				}
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					Log.i("webview", "end");
 				}
 			});
 			mWebView.startAnimation(anim);
 		}
 	}
 	
 	public void moveContent(int dx, int dy) {
 		moveContent(dx, dy, -1);
 		
 	}

	public void flingScroll(int vx, int vy) {
		if (mWebView != null) {
			mWebView.flingScroll(vx, vy);
		}
	}
 	
 }
