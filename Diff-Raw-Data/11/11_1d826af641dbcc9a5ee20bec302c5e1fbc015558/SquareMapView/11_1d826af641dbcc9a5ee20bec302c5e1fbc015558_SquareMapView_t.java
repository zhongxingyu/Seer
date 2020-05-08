 package carnero.me.view;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PaintFlagsDrawFilter;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.View;
 import carnero.me.Constants;
 import carnero.me.Map;
 import carnero.me.R;
 import carnero.me.VisitedPlaces;
 import carnero.me.model.GeoPoint;
 
 @SuppressWarnings("unused")
 public class SquareMapView extends View {
 
 	private Context mContext;
 	private boolean mRenderMap = true;
 	// dimensions
 	private int mWidth = 0;
 	private int mHeight = 0;
 	private int mSquareSize = 0;
 	private int mSquareMargin = 0;
 	private int mPaddingLeft = 0;
 	private int mPaddingTop = 0;
 	// location
 	private Integer mLatitude = Constants.DEF_LATITUDE;
 	private Integer mLongitude = Constants.DEF_LONGITUDE;
	private long mLatitudeMargin = 0l;
	private long mLongitudeMargin = 0l;
 	private long mSquareLat;
 	private long mSquareLon;
 	// paint
 	private PaintFlagsDrawFilter mSetFilter;
 	private PaintFlagsDrawFilter mRemFilter;
 	private Paint mSquareGreyPaint;
 	private Paint mSquareHighlightPaint;
 	private Paint mSquareBluePaint;
 	private int[] mLocationGlow = new int[]{10, 20, 30, 100};
 
 	public SquareMapView(Context context) {
 		super(context);
 
 		mContext = context;
 	}
 
 	public SquareMapView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		mContext = context;
 	}
 
 	public SquareMapView(Context context, AttributeSet attrs, int style) {
 		super(context, attrs, style);
 
 		mContext = context;
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 		mSetFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
 		mRemFilter = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0);
 
 		if (mSquareGreyPaint == null) {
 			final int color = mContext.getResources().getColor(R.color.map_square_grey);
 
 			mSquareGreyPaint = new Paint();
 			mSquareGreyPaint.setColor(color);
 		}
 		if (mSquareHighlightPaint == null) {
 			final int color = mContext.getResources().getColor(R.color.map_square_highlight);
 
 			mSquareHighlightPaint = new Paint();
 			mSquareHighlightPaint.setColor(color);
 		}
 		if (mSquareBluePaint == null) {
 			final int color = mContext.getResources().getColor(R.color.map_square_blue);
 
 			mSquareBluePaint = new Paint();
 			mSquareBluePaint.setColor(color);
 		}
 	}
 
 	@Override
 	public void onSizeChanged(int w, int h, int wOld, int hOld) {
 		mWidth = w;
 		mHeight = h;
 
 		countSquareSize();
 		invalidate();
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		if (!mRenderMap) {
 			return;
 		}
 
 		canvas.setDrawFilter(mSetFilter);
 
 		// map
 		int x;
 		int y;
 		int posX;
 		int posY;
 
 		for (x = 0; x < Map.MAP_WIDTH; x++) {
 			for (y = 0; y < Map.MAP_HEIGHT; y++) {
 				if (Map.MAP_DEFINITION[y][x]) {
 					posX = (x * (mSquareSize + mSquareMargin)) + mPaddingLeft;
 					posY = (y * (mSquareSize + mSquareMargin)) + mPaddingTop;
 
 					canvas.drawRect(posX, posY, posX + mSquareSize, posY + mSquareSize, mSquareGreyPaint); // left top right bottom
 				}
 			}
 		}
 
 		// visited places
 		for (int[] place : VisitedPlaces.VISITED_PLACES_E6) {
 			final int[] square = getSquareFromLocation(place[0], place[1]);
 			canvas.drawRect(square[0], square[1], square[0] + mSquareSize, square[1] + mSquareSize, mSquareHighlightPaint);
 		}
 
 		// current location
 		if (mLatitude != null && mLongitude != null) {
 			final int[] square = getSquareFromLocation(mLatitude, mLongitude);
 
 			int cnt = mLocationGlow.length;
 			RectF rect;
 			for (int g : mLocationGlow) {
 				rect = new RectF(square[0] - cnt, square[1] - cnt, square[0] + cnt + mSquareSize, square[1] + cnt + mSquareSize);
 
 				mSquareBluePaint.setAlpha(g);
 				canvas.drawRoundRect(rect, cnt, cnt, mSquareBluePaint);
 
 				cnt = cnt - 1;
 			}
 		}
 
 		canvas.setDrawFilter(mRemFilter);
 	}
 
 	public void setCurrentLocation(GeoPoint geoPoint) {
 		mLatitude = geoPoint.getLatitudeE6();
 		mLongitude = geoPoint.getLongitudeE6();
 
 		invalidate();
 	}
 
 	private void countSquareSize() {
 		final float maxSquareW = mWidth / Map.MAP_WIDTH; // maximum square width
 		final float maxSquareH = mHeight / Map.MAP_HEIGHT; // maximum square height
 
 		final float maxSquare;
 		if (maxSquareW <= maxSquareH) {
 			maxSquare = maxSquareW;
 		} else {
 			maxSquare = maxSquareH;
 		}
 
 		double border = maxSquare * 0.25;
 		if (border < 1) {
 			border = 1.0d;
 		}
 		mSquareMargin = (int) Math.floor(border);
 
 		double square = maxSquare - mSquareMargin;
 
 		mSquareSize = (int) Math.floor(square);
 
		mLatitudeMargin = (int) (180 * 1e6) - Math.abs(Map.MAP_LATITUDE_MIN_E6);
		mLongitudeMargin = (int) (90 * 1e6) - Map.MAP_LONGITUDE_MAX_E6;

 		mPaddingLeft = Math.round((mWidth - (Map.MAP_WIDTH * (mSquareSize + mSquareMargin))) / 2);
 		mPaddingTop = Math.round((mHeight - (Map.MAP_HEIGHT * (mSquareSize + mSquareMargin))) / 2);
 
 		mSquareLat = (Map.MAP_LATITUDE_MAX_E6 - Map.MAP_LATITUDE_MIN_E6) / Map.MAP_WIDTH;
 		mSquareLon = (Map.MAP_LONGITUDE_MAX_E6 - Map.MAP_LONGITUDE_MIN_E6) / Map.MAP_HEIGHT;
 
 		mRenderMap = mSquareSize >= 2;
 	}
 
 	private int[] getSquareFromLocation(int latitudeE6, int longitudeE6) {
 		final int[] square = new int[]{0, 0};
 
 		if (latitudeE6 > Map.MAP_LATITUDE_MAX_E6) {
 			latitudeE6 = Map.MAP_LATITUDE_MAX_E6;
 		} else if (latitudeE6 < Map.MAP_LATITUDE_MIN_E6) {
 			latitudeE6 = Map.MAP_LATITUDE_MIN_E6;
 		}
 		if (longitudeE6 > Map.MAP_LONGITUDE_MAX_E6) {
 			longitudeE6 = Map.MAP_LONGITUDE_MAX_E6;
 		} else if (longitudeE6 < Map.MAP_LONGITUDE_MIN_E6) {
 			longitudeE6 = Map.MAP_LONGITUDE_MIN_E6;
 		}
 
		final long leftPos = (latitudeE6 - mLatitudeMargin) - Map.MAP_LATITUDE_MIN_E6;
		final long topPos = Map.MAP_LONGITUDE_MAX_E6 - (longitudeE6 - mLongitudeMargin);
 		final int x = (int) (leftPos / mSquareLat);
 		final int y = (int) (topPos / mSquareLon);
 
 		square[0] = (x * (mSquareSize + mSquareMargin)) + mPaddingLeft;
 		square[1] = (y * (mSquareSize + mSquareMargin)) + mPaddingTop;
 
 		return square;
 	}
 }
