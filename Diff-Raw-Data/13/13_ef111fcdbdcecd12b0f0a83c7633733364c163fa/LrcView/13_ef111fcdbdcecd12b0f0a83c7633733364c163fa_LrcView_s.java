 package com.roger.lyricsmusicplayer.lyrics;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.TranslateAnimation;
import android.widget.TextView;
 
 import com.roger.lyricsmusicplayer.lyrics.iterfa.ILrcDisplay;
 import com.roger.lyricsmusicplayer.lyrics.iterfa.ISeekalble;
 import com.roger.lyricsmusicplayer.lyrics.iterfa.Range;
 
 public class LrcView extends View implements ISeekalble, ILrcDisplay {
 
 	private static final String TAG = "LrcView";
 	private ArrayList<LrcRow> mLrcAll;
 	private int mCurrentRow = 0;
 	private Paint mPaint;
 	private int mFontSize = 26;
 	private int mPaddingY = 10;
 	private int mHighLightColor = Color.YELLOW;
 	private int mNormalColor = Color.BLACK;
 	private int maxCharPerRow;
 	private final String NO_LRC_TIPS = "No Lrc right row";
 	private final String DOWNLOAD_FAILED_TIPS = "Lrc download failed";
 	private String mDisplay_Tips;
 	private int mHeight;
 	private int mWidth;
 	private int rowX;
 	public LrcView(Context context, AttributeSet attr) {
 		super(context, attr);
 		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		mPaint.setTextSize(mFontSize);
 		mDisplay_Tips = NO_LRC_TIPS;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		maxCharPerRow = getParentWidth() / getFontSize();
 		Log.v(TAG, "MAX CHARS:" + maxCharPerRow);
 
 		mHeight = getHeight();
 		mWidth = getWidth();
 
 		rowX = mWidth / 2;
 		int rowY;
 		int row;
 		if (mLrcAll == null || mLrcAll.size() == 0) {
 			Log.v(TAG, "noLrc");
 			displayTips(canvas);
 			return;
 
 		}
 
 		// draw highlight row
 		int highlightRowY = mHeight / 2 - mFontSize;
 		LrcRow currentRow;
 		currentRow = mLrcAll.get(mCurrentRow);
 		mPaint.setTextAlign(Align.CENTER);
 		mPaint.setColor(mHighLightColor);
 		displayLrcRow(currentRow, canvas, mPaint, highlightRowY, Direction.DOWN);
 
 		// draw before
 		row = mCurrentRow - 1;
		rowY = currentRow.getRange().upBound;// - mPaddingY - mFontSize;
 		mPaint.setColor(mNormalColor);
 		while (row >= 0 && rowY > -mFontSize) {
 			LrcRow rowTodraw = mLrcAll.get(row);
 			displayLrcRow(rowTodraw, canvas, mPaint, rowY, Direction.UP);
 			row--;
			rowY = rowTodraw.getRange().upBound - (mPaddingY + mFontSize);
 		}
 
 		// draw after
 		row = mCurrentRow + 1;
		rowY = currentRow.getRange().lowBound;// + mPaddingY + mFontSize;
 		while (row < mLrcAll.size() && rowY < mHeight) {
 			LrcRow rowTodraw = mLrcAll.get(row);
 			displayLrcRow(rowTodraw, canvas, mPaint, rowY, Direction.DOWN);
 			row++;
			rowY = rowTodraw.getRange().lowBound + (mPaddingY + mFontSize);
 		}
 		super.onDraw(canvas);
 	}
 
 	private void displayTips(Canvas canvas) {
 		int y = mHeight / 2 - mFontSize;
 		mPaint.setTextAlign(Align.CENTER);
 		mPaint.setColor(mHighLightColor);
 		canvas.drawText(mDisplay_Tips, rowX, y, mPaint);
 	}
 
 	private void seekToRow(int pos) {
 		if (pos == mCurrentRow)
 			return;
 		mCurrentRow = pos;
 		Log.v(TAG, "Seek To Row:" + pos);
 //		 invalidate();
 		startAnimation();
 	}
 
 	@Override
 	public void seekToTime(long milliSec) {
 		if (mLrcAll == null || mLrcAll.size() == 0)
 			return;
 		int low = 0;
 		int high = mLrcAll.size() - 1;
 		int mid;
 		while (low != high) {
 			mid = (low + high) / 2;
 			if (mLrcAll.get(mid).time < milliSec) {
 				low = mid + 1;
 			} else {
 				high = mid;
 			}
 		}
 		if (mLrcAll.get(low).time != milliSec && low != 0) {
 			low = low - 1;
 		}
 		seekToRow(low);
 	}
 
 	private void startAnimation() {
 
 		LrcRow row = mLrcAll.get(mCurrentRow);
 		long currentTime = row.time;
 		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, 0
 				- row.getRange().lines * (mPaddingY + mFontSize));
 		int nextRow = mCurrentRow == mLrcAll.size() - 1
 				? mCurrentRow
 				: mCurrentRow + 1;
 
 		long nextTime = mLrcAll.get(nextRow).time;
 		ta.setDuration(nextTime - currentTime);
 		ta.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				invalidate();
 			}
 		});
 		this.startAnimation(ta);
 	}
 
 	public ArrayList<LrcRow> getLrcAll() {
 		return mLrcAll;
 	}
 
 	public void setLrcAll(ArrayList<LrcRow> mLrcAll) {
 		this.mLrcAll = mLrcAll;
 	}
 
 	@Override
 	public int getFontSize() {
 		return mFontSize;
 	}
 
 	@Override
 	public int getParentWidth() {
 		return getWidth();
 	}
 
 	@Override
 	public void displayLrcRow(LrcRow row, Canvas canvas, Paint paint,
 			int yCurrent, Direction direction) {
 		if (row == null || row.content == null)
 			return;
 		Log.v(TAG, "Begine draw row");
 		int charNum = row.content.length();
 		double linesCount = Math.ceil(Double.valueOf(charNum) / maxCharPerRow);
 		Log.v(TAG, "lines:" + linesCount);
 		Log.v(TAG, "charNum:" + charNum);
 		int lines = (int) linesCount;
 		int y = yCurrent;
 		Range range = new Range();
 		range.lines = lines;
 		if (direction == Direction.UP) {
 			y -= lines * (getFontSize() + mPaddingY);
 			range.upBound = y;
 			range.lowBound = yCurrent;
 		} else {
 			y += lines * (getFontSize() + mPaddingY);
 			range.upBound = yCurrent;
 			range.lowBound = y;
 		}
 		if(direction==Direction.DOWN)
 		{
 			y=yCurrent;
 		}
 		int indexFrom = 0;
 		int indexTo;
 
 		while (lines > 0 && charNum > 0) {
 			if (lines > 1) {
 				indexTo = indexFrom + maxCharPerRow;
 			} else {
 				indexTo = charNum;
 			}
 			Log.v(TAG, "from:" + indexFrom);
 			Log.v(TAG, "to:" + indexTo);
 			if (indexFrom > charNum - 1 || indexTo > charNum|| indexTo < 0
 					|| indexFrom < 0) {
 				Log.e(TAG, "index out of bound break");
 				break;
 			}
 			Log.v(TAG, "draw Text:" + row.content.substring(indexFrom, indexTo));
 			canvas.drawText(row.content.substring(indexFrom, indexTo), rowX, y,
 					mPaint);
 			y += getFontSize() + mPaddingY;
 			indexFrom = indexTo;
 			lines--;
 		}
 		row.setRange(range);
 
 		return;
 	}
 }
