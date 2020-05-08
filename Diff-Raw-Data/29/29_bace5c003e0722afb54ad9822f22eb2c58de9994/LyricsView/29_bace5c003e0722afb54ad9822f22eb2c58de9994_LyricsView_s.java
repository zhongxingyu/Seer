 package info.dourok.android.lyrics;
 
 import info.dourok.android.lyrics.Lyric.LyricItem;
 import info.dourok.android.lyrics.Lyric.LyricsItemNode;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.text.TextPaint;
 import android.util.AttributeSet;
 import android.util.TypedValue;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class LyricsView extends View implements LyricsPlayer {
 	private static final String TAG = "LyricsView";
 
 	public static interface PositionChangeListener {
 
 		void onPositionChanged(LyricsView lyricsView, float differ,
 				boolean fromUser);
 
 		void onStartTouch(LyricsView lyricsView);
 
 		void onStopTouch(LyricsView lyricsView);
 	}
 
 	private int mContentWidth;
 
 	public LyricsView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		// 設置默認值
 		HIGHTLIGHT_POSITON_IN_PERCENT = 0.3f;// 高亮行在屏幕中的位置
 		mSpacingMult = 1.3f;
 		mSpacingAdd = 0;
 		int defColor = 0xffffffff;
 		int hlColor = 0xFF86c000;
 
 		float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
 				18f, getContext().getResources().getDisplayMetrics());
 
 		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
 		mTextPaint.setTextSize(size);
 		mTextPaint.setColor(defColor);
 
 		mHighlightPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
 		mHighlightPaint.setTextSize(size);
 		mHighlightPaint.setColor(hlColor);
 		mHighlightPaint.setShadowLayer(5, 0, 0, hlColor);
 
 		fontHeight = mTextPaint.getFontMetricsInt(null);
 		fontHeight = mTextPaint.getFontMetricsInt(null);
 		mLineHeight = (int) (fontHeight * mSpacingMult + mSpacingAdd);
 		setPadding(20, 50, 20, 10);
 
 		positionChangeListener = new OffsetChanger(); // FIXME
 
 	}
 
 	public LyricsView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public LyricsView(Context context) {
 		this(context, null, 0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.view.View#onMeasure(int, int)
 	 */
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
 		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
 
 		int width;
 		int height;
 		if (widthMode == MeasureSpec.EXACTLY) {
 			// Parent has told us how big to be. So be it.
 			width = widthSize;
 		} else {
 			width = 320;
 			// TODO 让LyricsView 自己决定要多宽 以不换行为标准
 		}
 
 		int contentWidth = width - getPaddingLeft() - getPaddingRight();
 
 		if (contentWidth > 0 && contentWidth != mContentWidth) {
 			// 宽度变化 需重新布局歌词
 			mContentWidth = contentWidth;
 			if (mLyrics != null) {
 				layoutLyrics(contentWidth);
 			}
 		}
 		if (heightMode == MeasureSpec.EXACTLY) {
 			// Parent has told us how big to be. So be it.
 			height = heightSize;
 		} else {
 			height = 480;
 			// TODO 让LyricsView 自己决定要多高
 		}
 
 		mHightlightItemPositon = height * HIGHTLIGHT_POSITON_IN_PERCENT;
 
 		setMeasuredDimension(width, height);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		canvas.save();
 		canvas.translate(getPaddingLeft(), 0);
 
 		float startHeight = mHightlightItemPositon - getCurOffset();
 		float height = startHeight;
 		int i = mCurLyricsItemIndex - 1;
 
 		while (height > 0 && i >= 0) {
 			height = drawLyricsItemNeg(mLyricsItems.get(i--), mTextPaint,
 					canvas, height);
 		}
 		i = mCurLyricsItemIndex;
 		height = drawLyricsItem(mLyricsItems.get(i++),
 				mLyrics.isHasTimestamp() ? mHighlightPaint : mTextPaint,
 				canvas, startHeight);
 
 		while (height < canvas.getClipBounds().height() + getLineHeight()
 				&& i < mLyricsItems.size()) {
 			height = drawLyricsItem(mLyricsItems.get(i++), mTextPaint, canvas,
 					height);
 
 		}
 
 		// //debug
 		// Log.d(TAG, canvas.getClipBounds().height() + " "+getHeight());
 		// canvas.drawLine(0, mHightlightItemPositon, getContentWidth(),
 		// mHightlightItemPositon, mCurPaint);
 		// canvas.drawText("i:" + mCurLyricsItemIndex + " t:" + mCurTime + " o:"
 		// + getCurOffset() + " h:" + getCurItemHeight(), 0,
 		// mHightlightItemPositon, mNormalPaint);
 
 		canvas.restore();
 	}
 
 	private Paint mTextPaint;
 	private Paint mHighlightPaint;
 
 	private float HIGHTLIGHT_POSITON_IN_PERCENT = 0.3f;
 	private float mHightlightItemPositon;
 
 	private PositionChangeListener positionChangeListener;
 	private SongWrapper mSong;
 	private Lyric mLyrics;
 	private ArrayList<LyricItem> mLyricsItems;
 	private int mCurLyricsItemIndex;
 	private int mCurTime;
 	// private float mLyricsWidth;
 	private float fontHeight;
 	private int mLineHeight;
 	private int mRealItemLength; // 总的行数
 	private int mCurItemLength; // 当前项目所在的行数
 
 	private float mSpacingMult = 1.3f;
 	private float mSpacingAdd = 0;
 
 	public void setSong(SongWrapper song) {

 		this.mSong = song;
		this.mLyrics = mSong.getLyrics();
		this.mLyricsItems = mLyrics.getLyricsItems();
 		if (song.getLyrics() != null) {
 			updateLyrics();
 		}
 
 	}
 
 	public void updateLyrics() {
 		// Log.d(TAG, "updateLyrics");
 
 		layoutLyrics(getContentWidth());
 		mCurTime = 0;
 		mCurLyricsItemIndex = 0;
 		mCurItemLength = 0;
 
 	}
 
 	public Lyric getLyrics() {
 		return mLyrics;
 	}
 
 	private float getContentWidth() {
 		return mContentWidth;
 	}
 
 	private void layoutLyrics(float width) {
 
 		if (width <= 0) {
 			// if call this method before onMeasure just do nothing
 			return;
 		}
 
 		mRealItemLength = 0;
 		for (LyricItem item : mLyricsItems) {
 			mRealItemLength += splitLyricsItem(item, mTextPaint, width);
 		}
 
 	}
 
 	private static int splitLyricsItem(LyricItem item, Paint paint, float width) {
 		LyricsItemNode node = null;
 		LyricsItemNode head = null;
 		int i = 0;
 		int c = 0; // counter
 		String text = item.mText;
 
 		if (text.equals("")) { // 如果是空串则空出一行
 			item.setHead(new LyricsItemNode(0, 0f), 1);
 			return 1;
 		}
 		// if (item.mNodeLenght == 1 && item.getHeadNode().width != 0
 		// && item.getHeadNode().width < width) {
 		// }
 
 		while (i < text.length()) {
 			float f[] = new float[1];
 			int e = paint.breakText(text, i, text.length(), true, width, f);
 			i += e;
 			c++;
 			if (node == null) {
 				node = new LyricsItemNode(i, f[0]);
 
 				head = node;
 			} else {
 				node.next = new LyricsItemNode(i, f[0]);
 				node = node.next;
 			}
 
 		}
 
 		item.setHead(head, c);
 		return c;
 	}
 
 	private float drawLyricsItem(LyricItem item, Paint paint, Canvas canvas,
 			float startHeight) {
 		LyricsItemNode node = item.getHeadNode();
 		float height = getLineHeight();
 		int i = 0;
 		String text = item.mText;
 		while (node != null) {
 			float x = (getContentWidth() - node.width) / 2;
 			// System.out.println(text.length()+" :"+ i+" "+ node.pos);
 			// System.out.println(text.substring(i, node.pos));
 			canvas.drawText(text, i, node.pos, x, startHeight, paint);
 			i = node.pos;
 			startHeight += height;
 			node = node.next;
 		}
 		return startHeight;
 	}
 
 	private float drawLyricsItemNeg(LyricItem item, Paint paint, Canvas canvas,
 			float startHeight) {
 		float height = getLineHeight();
 
 		startHeight = startHeight - item.mNodeLenght * height;
 		float returnHeight = startHeight;
 		LyricsItemNode node = item.getHeadNode();
 		int i = 0;
 		String text = item.mText;
 		while (node != null) {
 			float x = (getContentWidth() - node.width) / 2;
 			// System.out.println(text.length()+" :"+ i+" "+ node.pos);
 			// System.out.println(text.substring(i, node.pos));
 
 			canvas.drawText(text, i, node.pos, x, startHeight, paint);
 			i = node.pos;
 			startHeight += height;
 			node = node.next;
 		}
 		return returnHeight;
 	}
 
 	public void refresh(int time) {
 		try {
 			updateCurLyricsItemIndex(time);
 			invalidate();
 		} catch (LyricsException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void refresh() {
 		refresh(mSong.getCurentTime());
 	}
 
 	/**
 	 * mCurLyricsItemIndex modify only in this method make sure
 	 * mCurLyricsItemIndex fit the size of items thread-safety
 	 * 
 	 * @param time
 	 * @throws LyricsException
 	 */
 	private void updateCurLyricsItemIndex(int time) throws LyricsException {
 		this.mCurTime = time;
 
 		if (mLyrics.isHasTimestamp()) {
 			if (time > mLyricsItems.get(mCurLyricsItemIndex).getTime()) {
 				for (int i = mCurLyricsItemIndex + 1; i < mLyricsItems.size(); i++) {
 
 					if (time < mLyricsItems.get(i).getTime()) {
 						mCurLyricsItemIndex = i - 1;
 						return;
 					}
 					mCurItemLength += mLyricsItems.get(i).mNodeLenght;
 				}
 				// time > Lyrics.maxtime 显示最后一项
 				mCurLyricsItemIndex = mLyricsItems.size() - 1;
 			} else {
 				// 传入时间比当前时间还要小,一般只可能发生在动态改变歌词offset的时候
 				for (int i = mCurLyricsItemIndex - 1; i >= 0; i--) {
 					if (time > mLyricsItems.get(i).getTime()) {
 						mCurLyricsItemIndex = i;
 						return;
 					}
 					mCurItemLength -= mLyricsItems.get(i).mNodeLenght;
 				}
 
 				mCurLyricsItemIndex = 0;
 			}
 		} else {
 			// 没有时间戳时,歌词平滑滚动
 			mCurLyricsItemIndex = time * mLyrics.getLyricsItems().size()
 					/ mSong.getTotalTime();
 			if (mCurLyricsItemIndex >= mLyricsItems.size()) {
 				mCurLyricsItemIndex = mLyricsItems.size() - 1;
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return 两句歌词间的时间差 > 0
 	 */
 	private int getCurTimeInterval() {
 
 		if (mLyrics.isHasTimestamp()) {
 			int time1 = mLyricsItems.get(mCurLyricsItemIndex).getTime();
 			int time2;
 			if (mCurLyricsItemIndex + 1 == mLyricsItems.size()) {
 				time2 = mSong.getTotalTime() > time1 ? mSong.getTotalTime()
 						: time1 + 1/* 不会发生 */;
 			} else {
 				time2 = mLyricsItems.get(mCurLyricsItemIndex + 1).getTime();
 			}
 			return time2 - time1;
 		}
 		return mSong.getTotalTime() / mLyrics.getLyricsItems().size();
 	}
 
 	private int getCurItemHeight() {
 		return mLineHeight * mLyricsItems.get(mCurLyricsItemIndex).mNodeLenght;
 	}
 
 	private int getCurOffset() {
 		int curItemHeight = getCurItemHeight();
 		try {
 			exceptionCheck();
 		} catch (LyricsException e) {
 			return curItemHeight;
 		}
 
 		if (mLyrics.isHasTimestamp()) {
 			int timediff = mCurTime
 					- mLyricsItems.get(mCurLyricsItemIndex).getTime();
 			int diff = getCurTimeInterval();
 			if (timediff == 0 || diff == 0) {
 				return curItemHeight;
 			}
 			return curItemHeight * (timediff) / (diff);
 		} else {
 			int ut = mSong.getTotalTime() / mLyrics.getLyricsItems().size();
 			return curItemHeight * (mCurTime % ut) / ut;
 		}
 	}
 
 	private int getLineHeight() {
 		return mLineHeight;
 	}
 
 	private void exceptionCheck() throws LyricsException {
 		if (mSong.getTotalTime() < mLyricsItems.size()) {
 			throw new LyricsException("Lyrics Item's size :"
 					+ mLyricsItems.size() + " is bigger than Song's duration :"
 					+ mSong.getTotalTime() + " in ms");
 		}
 		if (mSong.getTotalTime() < mCurTime) {
 			throw new LyricsException("curent time " + mCurTime
 					+ "can't bigger than song 's duration :"
 					+ mSong.getTotalTime());
 		}
 	}
 
 	private float spx, spy;
 	private float cpx, cpy;
 	private float offsety = 0;
 
 	private void motionMove(float x, float y) {
 		float dy = y - cpy;
 		if (positionChangeListener != null) {
 			positionChangeListener.onPositionChanged(this, dy, true);
 		}
 		offsety += (y - cpy);
 		cpx = x;
 		cpy = y;
 
 		System.out.println("offsety : " + offsety);
 	}
 
 	private void motionStart(float x, float y) {
 		spx = x;
 		spy = y;
 		cpx = spx;
 		cpy = spy;
 		if (positionChangeListener != null) {
 			positionChangeListener.onStartTouch(this);
 		}
 	}
 
 	private void motionStop(float x, float y) {
 		if (positionChangeListener != null) {
 			positionChangeListener.onStopTouch(this);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			motionStart(event.getX(), event.getY());
 			break;
 		case MotionEvent.ACTION_MOVE:
 			motionMove(event.getX(), event.getY());
 			break;
 
 		case MotionEvent.ACTION_UP:
 			motionStop(event.getX(), event.getY());
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 	private final static int LEAST_TIME_INTERVAL = 20; // ms
 	private final static int LEAST_MOTION_INTERVAL = 1; // px
 
 	private class OffsetChanger implements PositionChangeListener {
 
 		public void onPositionChanged(LyricsView lyricsView, float differ,
 				boolean fromUser) {
 			if (mLyrics != null)
 				mLyrics.accOffset((int) (15 * differ));
 		}
 
 		public void onStartTouch(LyricsView lyricsView) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onStopTouch(LyricsView lyricsView) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 	public LyricsRefreshHandler buildLyricsRefreshHandler() {
 
 		return new LyricsRefreshHandler() {
 
 			/**
 			 * 计算下一次刷新的时间，但歌词滚动距离超过 @LEAST_MOTION_INTERVAL 时,才需要刷新， 这个时间最小是 @LEAST_TIME_INTERVAL
 			 * 
 			 * @return
 			 * @throws LyricsException
 			 */
 			public long getRefreshTime() throws LyricsException {
 				exceptionCheck();
 				long t = getCurTimeInterval();
 				long L = getCurItemHeight();
 				// Log.d("LyricsRefreshHandler","t:"+t+" L:"+L);
 				t = t * LEAST_MOTION_INTERVAL / L;
 
 				if (t > LEAST_TIME_INTERVAL)
 					return t;
 				else
 					return LEAST_TIME_INTERVAL;
 			}
 
 			public long getRefreshTime(long dif) throws LyricsException {
 				long t = (getRefreshTime() - dif);
 				if (t < 0) {
 					return 0;
 				}
 				return t;
 			}
 		};
 
 	}
 
 }
