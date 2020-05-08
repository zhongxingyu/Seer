 package com.joy.launcher2.joyfolder;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.widget.TextView;
 
 import com.joy.launcher2.R;
 import com.joy.launcher2.ShortcutInfo;
 import com.joy.launcher2.download.DownLoadDBHelper;
 import com.joy.launcher2.download.DownloadInfo;
 
 public class JoyIconView extends TextView {
 
 	DownLoadProgressBar mProgressBar;
 	DownloadInfo dInfo;
 	boolean isDrawProgressBar = false;
 	public JoyIconView(Context context) {
 		super(context);
 	}
 
 	public JoyIconView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public JoyIconView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public void initDownLoadProgressBar() {
 		if (mProgressBar == null) {
 			mProgressBar = new DownLoadProgressBar();
 		}
 	}
 
 	public void releaseDownLoadProgressBar() {
 		if (mProgressBar != null) {
 			mProgressBar = null;
 		}
 	}
 
 	public void setDownloadInfo(DownloadInfo dInfo) {
 		this.dInfo = dInfo;
 	}
 
 	public DownloadInfo getDownloadInfo() {
 		return this.dInfo;
 	}
 	public void showProgressBar(boolean draw){
 		isDrawProgressBar = draw;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		drawProgressBar(canvas);
 	}
 
 	public void drawProgressBar(Canvas canvas) {
 		if (!isDrawProgressBar) {
 			return;
 		}
 		initDownLoadProgressBar();
 		final int scrollX = mScrollX;
 		final int scrollY = mScrollY;
 		DownloadInfo downloadInfo = getDownloadInfo();
 		if ((scrollX | scrollY) == 0) {
 			drawProgressBar(canvas, downloadInfo);
 		} else {
 			canvas.translate(scrollX, scrollY);
 			drawProgressBar(canvas, downloadInfo);
 			canvas.translate(-scrollX, -scrollY);
 		}
 	}
 
 	private void drawProgressBar(Canvas canvas, DownloadInfo downloadInfo) {
 		mProgressBar.drawProgressBar(canvas, downloadInfo);
 	}
 
 	private void drawProgressBar(Canvas canvas, DownloadInfo downloadInfo,
 			int left, int top) {
 		mProgressBar.drawProgressBar(canvas, downloadInfo, left, top);
 	}
 
 	public Drawable getCompoundDrawable(Canvas canvas, int left, int top) {
 
 		if (mProgressBar != null) {
 			DownloadInfo downloadInfo = getDownloadInfo();
 			drawProgressBar(canvas, downloadInfo, left, top);
 		}
 		return null;
 	}
 
 	class DownLoadProgressBar {
 
 		private Drawable progressbar;
 		private Drawable progressbar_groove;
 		private Drawable progressbar_cursor;
 		private Drawable progressbar_bg;
 		private Rect progressbarBounds;
 
		final int offest = 2;
 		public DownLoadProgressBar() {
 			progressbar = getResources().getDrawable(
 					R.drawable.download_progressbar);
 			progressbar_bg = getResources().getDrawable(
 					R.drawable.download_progressbar_bg);
 			progressbar_groove = getResources().getDrawable(
 					R.drawable.download_progressbar_groove);
 			progressbar_cursor = getResources().getDrawable(
 					R.drawable.download_progressbar_cursor);
 		}
 
 		public void drawProgressBar(Canvas canvas, DownloadInfo downloadInfo) {
 			
 			Drawable[] drawables = getCompoundDrawables();
 			Drawable drawableTop = drawables[1];
			Rect bounds = drawableTop.getBounds();
			int progressbarLeft = bounds.left;
			int progressbarTop = bounds.top;
			int progressbarRight = bounds.right;
			int progressbarBottom = bounds.bottom;
			progressbarBounds = new Rect(progressbarLeft-offest, progressbarTop-offest, progressbarRight+offest, progressbarBottom+offest);
			int left = (getWidth() - bounds.width()) / 2;
 			int top = getPaddingTop();
 
 			drawProgressBar(canvas, downloadInfo, left, top);
 		}
 
 		private void drawProgressBar(Canvas canvas, DownloadInfo downloadInfo,
 				int left, int top) {
 			if (downloadInfo == null) {
 				canvas.save();
 				canvas.translate(left, top);
 				progressbar.setBounds(progressbarBounds);
 				progressbar.draw(canvas);
 				canvas.restore();
 				return;
 			} else if (downloadInfo != null) {
 				canvas.save();
 				canvas.translate(left, top);
 				progressbar_bg.setBounds(progressbarBounds);
 				progressbar_bg.draw(canvas);
 
				int maxw = progressbarBounds.width() - 2*offest;
 				int maxh = progressbarBounds.height() / 8;
 
				int progressbar_left = progressbarBounds.left + offest;
 				int progressbar_top = (progressbarBounds.bottom - progressbar_cursor
 						.getIntrinsicHeight()) / 2;
 				int progressbar_right = maxw;
 				int progressbar_bottom = progressbar_top + maxh;
 				progressbar_groove.setBounds(progressbar_left, progressbar_top,
 						progressbar_right, progressbar_bottom);
 				progressbar_groove.draw(canvas);
 
 				int w = maxw * downloadInfo.getCompletesize()
 						/ downloadInfo.getFilesize();
 				if (w < progressbar_cursor.getIntrinsicWidth()) {
 					// w = progressbar_cursor.getIntrinsicWidth();
 					canvas.restore();
 					return;
 				} else if (w >= maxw) {
 					w = maxw;
 				}
 				progressbar_cursor.setBounds(progressbar_left, progressbar_top,
 						w, progressbar_bottom);
 				progressbar_cursor.draw(canvas);
 				canvas.translate(left, top);
 				canvas.restore();
 			}
 		}
 	}
 }
