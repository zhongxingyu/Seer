 package it.giacomos.android.osmer.pro.widgets;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.google.android.gms.maps.model.LatLng;
 
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.pro.forecastRepr.Area;
 import it.giacomos.android.osmer.pro.forecastRepr.ForecastDataFactory;
 import it.giacomos.android.osmer.pro.forecastRepr.ForecastDataInterface;
 import it.giacomos.android.osmer.pro.forecastRepr.ForecastDataStringMap;
 import it.giacomos.android.osmer.pro.forecastRepr.ForecastDataType;
 import it.giacomos.android.osmer.pro.forecastRepr.Locality;
 import it.giacomos.android.osmer.pro.forecastRepr.Strip;
 import it.giacomos.android.osmer.pro.forecastRepr.Zone;
 import it.giacomos.android.osmer.pro.forecastRepr.ZoneMapper;
 import it.giacomos.android.osmer.pro.network.state.ViewType;
 import it.giacomos.android.osmer.pro.preferences.Settings;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.widget.Toast;
 
 public class MapWithForecastImage extends MapWithLocationImage implements OnLongClickListener
 {
 	public MapWithForecastImage(Context context, AttributeSet attrs) 
 	{
 		super(context, attrs);
 		Settings s = new Settings(context);
 		mCachedTextFontSize = s.getMapWithForecastImageTextFontSize();
 		mHintForecastIconEnabled = s.isForecastIconsHintEnabled();
 		mZoneLongPressHintEnabled = s.isZoneLongPressHintEnabled();
 		/* will contain the rectangles in the image that are associated to a 
 		 * forecast data interface, area, locality or strip.
 		 */
 		mCurrentTouchedPoint = new PointF(-1.0f, -1.0f);
 		mForecastDataStringMap = new ForecastDataStringMap(getResources());
 		int densityDpi = this.getResources().getDisplayMetrics().densityDpi;
 		/* adjust font according to density... */
 		if(densityDpi == DisplayMetrics.DENSITY_MEDIUM ||
 				densityDpi == DisplayMetrics.DENSITY_LOW)
 			mFontSize = 10;
 		else if(densityDpi == DisplayMetrics.DENSITY_HIGH)
 			mFontSize = 20;
 		else if(densityDpi == DisplayMetrics.DENSITY_XHIGH)
 			mFontSize = 24;
 		else if(densityDpi == DisplayMetrics.DENSITY_XXHIGH)
 			mFontSize = 28;
 		else if(densityDpi == DisplayMetrics.DENSITY_XXXHIGH)
 			mFontSize = 32;
 		else /* shouldn't happen */
 			mFontSize = 36;
 
 		setOnLongClickListener(this);
 
 		/* this hash map besides storing the list of ForecastDataInterface, associates to 
 		 * each ForecastDataInterface its geometry, that is updated inside the draw method.
 		 */
 		mForecastData = new HashMap<ForecastDataInterface, RectF>();
 
 		mAreaTouchListener = null;
 	}
 
 	public void setViewType(ViewType vt)
 	{
 		mViewType = vt;
 	}
 
 	public ViewType getViewType()
 	{
 		return mViewType;
 	}
 
 	public void setSymTable(String symtab)
 	{
 		ForecastDataFactory forecastDataFactory = new ForecastDataFactory(getResources());
 		ArrayList<ForecastDataInterface > forecastDataIfList =  forecastDataFactory.getForecastData(symtab);
 		for(ForecastDataInterface fdi : forecastDataIfList)
 			mForecastData.put(fdi, null);
 		this.invalidate();
 	}
 
 	public void setAreaTouchListener(AreaTouchListener atl)
 	{
 		mAreaTouchListener = atl;
 	}
 
 	/** sets a null callback on the drawables.
 	 * recycles bitmaps.
 	 * Checks for null in case setBitmap was called with a null parameter (and
 	 * so the bitmap drawable is null).
 	 */
 	public void unbindDrawables()
 	{		
 		/* must check for null (download error or data unavailable until 
 		 * afternoon for two days symtable).
 		 */
 		if(mForecastData != null)
 		{
 			for(ForecastDataInterface fdi : mForecastData.keySet())
 			{
 				if(fdi.getType() == ForecastDataType.AREA)
 				{
 					Area a = (Area) fdi;
 					Bitmap bmp = a.getSymbol();
 					if(bmp != null)
 					{
 						//						Log.e("MapWithForecastImage.umbindDrawables", "recycling bitmap " + bmp + ": " + a.getId() + ", " + mViewType);
 						bmp.recycle();
 					}
 					/* recycle wind symbols */
 					bmp = a.getWindSymbol();
 					if(bmp != null)
 					{
 						bmp.recycle();
 						//						Log.e("MapWithForecastImage.umbindDrawables", "recycling WIND bitmap " + bmp + ": " + a.getId() + ", " + mViewType);
 					}
 				}
 				else if(fdi.getType() == ForecastDataType.LOCALITY)
 				{
 					Locality l = (Locality) (fdi);
 					Bitmap bmp = l.lightningBitmap();
 					if(bmp != null)
 					{
 						//						Log.e("MapWithForecastImage.umbindDrawables", "recycling LIGHTNING bitmap " + bmp + ": " + l.getName() + ", " + mViewType);
 						bmp.recycle();
 					}
 					bmp = l.snowBitmap();
 					if(bmp != null)
 					{
 						//						Log.e("MapWithForecastImage.umbindDrawables", "recycling SNOW bitmap " + bmp + ": " + l.getName() + ", " + mViewType);
 						bmp.recycle();
 					}
 				}
 			}
 		}
 	}
 
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		drawForecast(canvas);
 	}
 
 	public void drawTextOnMap(Canvas canvas, String text)
 	{
 		int w = this.getWidth();
 		int h = getHeight();
 		int yTxt = h / 2;
 		int xTxt = w / 2;
 		int nLines = 1;
 		int i;
 		int txtH;
 		int txtW;
 		int totTxtH;
 		boolean neededFontShrink = false;
 		Rect txtRect = new Rect();
 		String [] lines = text.split("\n");
 		nLines = lines.length;
 		if(nLines > 0)
 		{
 			mPaint.setTextSize(mCachedTextFontSize);
 			String longestLine = "";
 			/* calculate the len of the longest line */
 			for(i = 0; i < nLines; i++)
 				if(lines[i].length() > longestLine.length())
 					longestLine = lines[i];
 
 			/* perform a first length measure of the longest line */
 			mPaint.getTextBounds(longestLine, 0, longestLine.length(), txtRect);
 
 			while(txtRect.width() > (w - 100))
 			{
 				mCachedTextFontSize -= 3.0f;
 				mPaint.setTextSize(mCachedTextFontSize);
 				mPaint.getTextBounds(longestLine, 0, longestLine.length(), txtRect);
 				neededFontShrink = true;
 			}	
 			if(neededFontShrink) /* save in preferences */
 				new Settings(getContext()).setMapWithForecastImageTextFontSize(mCachedTextFontSize);
 
 			/* font is now correctly scaled to fit into the image width */
 			mPaint.setColor(Color.WHITE);
 
 			txtH = txtRect.height();
 			totTxtH = (txtH + 4) * nLines;
 			for(i = 0; i < nLines; i++)
 			{
 				mPaint.getTextBounds(lines[i], 0, lines[i].length(), txtRect);
 				txtW = txtRect.width();
 				xTxt = w / 2 - txtW / 2;
 				yTxt = h / 2 - totTxtH /2 + i * (txtH + 2);
 				canvas.drawText(lines[i], xTxt, yTxt, mPaint);
 			}
 		}
 	}
 
 	public void drawForecast(Canvas canvas)
 	{
 		/* mForecastData may be null if an error occurred or if o
 		 * the two days symbol table is empty.
 		 */
 		if(mForecastData != null && mForecastData.size() == 0 && mViewType == ViewType.TWODAYS_SYMTABLE)
 		{
 			drawTextOnMap(canvas, getResources().getString(R.string.forecast_map_avail_afternoon)); /* draw "Forecast available in the afternoon */
 		}
 		else if(mForecastData != null)
 		{
 			/* do not draw a rectangle around more than one symbol (single selection) */
 			boolean selectionIsActive = false;
 			RectF rect;
 			Paint paint = new Paint();
 			int iconW, iconH;
 			float x, y;
 			PointF p = null;
 			LocationToImgPixelMapper locationMapper = new LocationToImgPixelMapper();
 			ZoneMapper zoneMapper = new ZoneMapper(this);
 			ForecastDataInterface selectedForecastData = null;
 			for(ForecastDataInterface fdi : mForecastData.keySet())
 			{
 				if(fdi.getType() == ForecastDataType.AREA)
 				{
 					Area a = (Area) fdi;
 					if(!a.isEmpty())
 					{
 						LatLng llng = a.getLatLng();
 						p = locationMapper.mapToPoint(this, llng.latitude, llng.longitude);
 						Bitmap symbol = a.getSymbol();
 						if(symbol != null)
 						{
 							iconW = symbol.getWidth();
 							iconH = symbol.getHeight();
 							x = p.x - iconW/2;
 							y = p.y - iconH/1.45f;
 							canvas.drawBitmap(symbol, x, y, paint);
 							rect = new RectF(x, y, x + iconW, y + iconH);
 							/* update geometry information for fdi */
 							mForecastData.put(fdi, rect);
 							if(!selectionIsActive && rect.contains(mCurrentTouchedPoint.x, mCurrentTouchedPoint.y))
 							{
 								/* draw a rounded rect around */
 								/* colour for text and circles */
 								mPaint.setARGB(255, 10, 255, 10);
 								mPaint.setStyle(Paint.Style.STROKE);
 								canvas.drawRoundRect(rect, 6, 6, mPaint);
 								selectionIsActive = true;
 								drawBottomLeftText(canvas, a.getData(mForecastDataStringMap), false);
 								if(a.hasDetailedWindData())
 									drawTopRightTextIfOrientationPortrait(canvas, a.getDetailedWindData(mForecastDataStringMap));
 								selectedForecastData = a;
 							}
 						}
 						/* get wind symbol if avail */
 						if(a.hasWindSymbol())
 						{
 							llng = a.getWindLocationLanLng();
 							if(llng != null)
 							{
 								p = locationMapper.mapToPoint(this, llng.latitude, llng.longitude);
 								symbol = a.getWindSymbol();
 								iconW = symbol.getWidth();
 								iconH = symbol.getHeight();
 								x = p.x - iconW/2;
 								y = p.y - iconH/1.45f;
 								canvas.drawBitmap(symbol, x, y, paint);
 							}
 						}
 
 					}
 				}
 				else if(fdi.getType() == ForecastDataType.STRIP)
 				{
 					Strip s = (Strip) fdi;
 					if(!s.isEmpty())
 					{
 						LatLng ll = s.getLatLng();
 						PointF pt = locationMapper.mapToPoint(this, ll.latitude, ll.longitude);
 						if(!s.t1000.isEmpty() || !s.t2000.isEmpty())
 							drawT1000T2000(canvas, s, pt);
 						if(!s.tMax.isEmpty() || !s.tMin.isEmpty())
 							drawTMinTMax(canvas, s, pt);
 					}
 
 				}
 				else if(fdi.getType() == ForecastDataType.LOCALITY)
 				{
 					Locality l = (Locality) fdi;
 					if(l.hasSomeBitmap() && !l.isEmpty())
 					{
 						LatLng llng = l.getLatLng();
 						p = locationMapper.mapToPoint(this, llng.latitude, llng.longitude);
 						Bitmap b = l.lightningBitmap();
 						if(b != null)
 						{
 							iconW = b.getWidth();
 							iconH = b.getHeight();
 							x = p.x - iconW/2;
 							y = p.y - iconH/1.45f;
 							canvas.drawBitmap(b, x, y, paint);
 							rect = new RectF(x, y, x + iconW, y + iconH);
 							/* update geometry information for fdi */
 							mForecastData.put(l, rect);
 							if(!selectionIsActive && rect.contains(mCurrentTouchedPoint.x, mCurrentTouchedPoint.y))
 							{
 								/* draw a rounded rect around */
 								/* colour for text and circles */
 								mPaint.setARGB(255, 10, 255, 10);
 								mPaint.setStyle(Paint.Style.STROKE);
 								canvas.drawRoundRect(rect, 6, 6, mPaint);
 								selectionIsActive = true;
 								drawBottomLeftText(canvas, l.getData(mForecastDataStringMap), false);
 								selectedForecastData = l;
 							}
 						}
 						b = l.snowBitmap();
 						if(b != null)
 						{
 							iconW = b.getWidth();
 							iconH = b.getHeight();
 							x = p.x - iconW/2;
 							y = p.y - iconH/1.45f;
 							canvas.drawBitmap(b, x, y, paint);
 							rect = new RectF(x, y, x + iconW, y + iconH);
 							mForecastData.put(l, rect);
 							if(!selectionIsActive && rect.contains(mCurrentTouchedPoint.x, mCurrentTouchedPoint.y))
 							{
 								/* draw a rounded rect around */
 								/* colour for text and circles */
 								mPaint.setARGB(255, 10, 255, 10);
 								mPaint.setStyle(Paint.Style.STROKE);
 								canvas.drawRoundRect(rect, 6, 6, mPaint);
 								selectionIsActive = true;
 								drawBottomLeftText(canvas, l.getData(mForecastDataStringMap), false);
 								selectedForecastData = l;
 							}
 						}
 					}
 				} /* end if fdi is LOCALITY */
 				else if(fdi.getType() == ForecastDataType.ZONE)
 				{
 					Zone z = (Zone) fdi;
 					if(mOnLongPress)
 					{
 						mPaint.setStyle(Paint.Style.STROKE);
 						mPaint.setARGB(160, 100, 115, 75);
 						Path pa = zoneMapper.getAreaPath(z.getId());
 						if(pa != null)
 							canvas.drawPath(pa, mPaint);
 						if(z.isSelected())
 						{
 							mPaint.setStyle(Paint.Style.FILL);
 							mPaint.setARGB(120, 240, 255, 215);
 							canvas.drawPath(pa, mPaint);
							drawBottomLeftText(canvas, z.getData(mForecastDataStringMap), true);
 							selectedForecastData = z;
 						}
 					}
 
 				}
 			} /* end for(ForecastDataInterface fdi : mForecastData) */
 			if(selectionIsActive && mHintForecastIconEnabled)
 				this.disableForecastIconsHint();
 
 		} /* else if(mForecastData != null) */
 
 		/* draw the location */
 		if(getDrawLocationEnabled())
 			drawLocation(canvas);
 	}
 
 	private void drawTMinTMax(Canvas canvas, Strip s, PointF pt) 
 	{
 		int tMaxXShift = 0;
 		float fontSize = mFontSize * 0.9f;
 		Rect txtRect = new Rect();
 		String text;
 		int margin = 4;
 		float y = pt.y;
 		float x;
 		mPaint.setTextSize(fontSize);
 		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		if(!s.tMin.isEmpty())
 		{
 			mPaint.setARGB(120, 0, 0, 250);
 			text = s.tMin + getResources().getString(R.string.celsius);
 			mPaint.getTextBounds(text, 0, text.length(), txtRect);
 			canvas.drawRoundRect(new RectF(pt.x - margin, y - txtRect.height() - margin, pt.x + margin + txtRect.right, y+ txtRect.bottom + margin), 6, 6, mPaint);
 			mPaint.setColor(Color.WHITE);
 			canvas.drawText(text, pt.x, y, mPaint);
 			y += (txtRect.height() + 2 * margin) * 1.25f;
 			tMaxXShift = txtRect.width() / 3;
 		}
 		if(!s.tMax.isEmpty())
 		{			
 			x = pt.x + tMaxXShift;
 			mPaint.setARGB(120, 240, 20, 20);
 			text = s.tMax + getResources().getString(R.string.celsius);
 			mPaint.getTextBounds(text, 0, text.length(), txtRect);
 			canvas.drawRoundRect(new RectF(x - margin, y - txtRect.height() - margin, x + txtRect.right + margin, y+ txtRect.bottom + margin), 6, 6, mPaint);
 			mPaint.setColor(Color.WHITE);
 			canvas.drawText(text, x, y, mPaint);
 		}
 
 	}
 
 	private void drawT1000T2000(Canvas canvas, Strip s, PointF pt) 
 	{
 		int t1000XShift = 0;
 		float fontSize = mFontSize;
 		Rect txtRect = new Rect();
 		String text;
 		float y = pt.y;
 		float x;
 		mPaint.setTextSize(fontSize);
 		mPaint.setColor(Color.WHITE);
 		mPaint.setStyle(Paint.Style.STROKE);
 		if(!s.t1000.isEmpty())
 		{			
 			text = getResources().getString(R.string.t2000) + ": " + s.t2000 + getResources().getString(R.string.celsius);
 			mPaint.getTextBounds(text, 0, text.length(), txtRect);
 			canvas.drawText(text, pt.x, y, mPaint);
 			//			mPaint.setColor(Color.BLUE);
 			//			canvas.drawRoundRect(new RectF(pt.x - 4, y - txtRect.height() - 4, pt.x + 4 + txtRect.right, y+ txtRect.bottom + 4), 6, 6, mPaint);
 			y += txtRect.height() + 4;
 			t1000XShift = txtRect.width() / 4;
 		}
 		if(!s.t2000.isEmpty())
 		{			
 			text = getResources().getString(R.string.t1000) + ": " + s.t1000 + getResources().getString(R.string.celsius);
 			mPaint.setColor(Color.WHITE);
 			//			mPaint.getTextBounds(text, 0, text.length(), txtRect);
 			x = pt.x + t1000XShift;
 			canvas.drawText(text, x, y, mPaint);
 			//			mPaint.setColor(Color.LTGRAY);
 			//			canvas.drawRoundRect(new RectF(x - 4, y - txtRect.height() - 4, pt.x + txtRect.right + 4, y+ txtRect.bottom + 4), 6, 6, mPaint);
 		}
 	}
 
 	/** draws the text passed in data on the top right of the view, aligning the 
 	 * text on the right.
 	 * The text is drawn if the orientation is portrait, otherwise there is no space.
 	 * 
 	 * @param canvas the canvas to draw onto
 	 * @param data the text to draw
 	 */
 	private void drawTopRightTextIfOrientationPortrait(Canvas canvas, String data) 
 	{
 		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
 		{
 			/* make text a bit smaller */
 			float fontSize = mFontSize * 0.78f;
 			String [] lines = data.split("\n");
 			int nLines = lines.length;
 			int margin = 3;
 			int textH;
 			int x = 3, y, i;
 			int w = getWidth();
 			if(nLines > 0)
 			{
 				Rect txtR = new Rect();
 				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 				mPaint.setARGB(255, 16, 85, 45);
 				mPaint.setTextSize(fontSize);
 				mPaint.getTextBounds(lines[0], 0, lines[0].length(), txtR);
 				margin = txtR.height() / 5;
 				textH = txtR.height() + margin;
 				y = textH;
 				for(i = 0; i < nLines; i++)
 				{
 					if(i > 0) /* txtR already calculated above for lines[0] */
 						mPaint.getTextBounds(lines[i], 0, lines[i].length(), txtR);
 					x = w - txtR.width() - margin;
 					canvas.drawText(lines[i], x, y, mPaint);
 					y += textH;
 				}
 			}
 		}
 	}
 
 	private void drawBottomLeftText(Canvas canvas, String data, boolean drawBackground) 
 	{
 		String [] lines = data.split("\n");
 		int nLines = lines.length;
 		int margin = 3;
 		int textH;
 		int x = 4, y, i, bgRectX, bgRectY, bgRectH, bgRectW;
 		float fontSize = mFontSize * 0.85f;
 		if(nLines > 0)
 		{
 			Rect txtR = new Rect();
 			mPaint.setTextSize(fontSize);
 			mPaint.getTextBounds(lines[0], 0, lines[0].length(), txtR);
 			margin = txtR.height() / 5;
 			textH = txtR.height() + margin;
 			y = getHeight() - (nLines -1) * textH - 2 * margin;
 
 			if(drawBackground)
 			{
 				bgRectH = textH;
 				bgRectY = y - textH;
 				bgRectW = txtR.width();
 				for(i = 1; i < nLines; i++)
 				{
 					mPaint.getTextBounds(lines[i], 0, lines[i].length(), txtR);
 					if(txtR.width() > bgRectW)
 						bgRectW = txtR.width();
 					bgRectH += textH;
 				}
 				bgRectX = x;
 				bgRectH += textH;
 				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 				mPaint.setARGB(230, 255, 255, 255);
 				canvas.drawRoundRect(new RectF(bgRectX, bgRectY, bgRectX + bgRectW  + margin, bgRectY + bgRectH), 6, 6, mPaint);
 			}
 
 			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 			mPaint.setARGB(255, 16, 85, 45);
 			for(i = 0; i < nLines; i++)
 			{
 				canvas.drawText(lines[i], x, y, mPaint);
 				y += textH;
 			}
 		}
 	}
 
 	public boolean onTouchEvent (MotionEvent event)
 	{
 		/* save the touched point */
 		if(event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			mDownPoint = new PointF(event.getX(), event.getY());
 			mOnLongPress = mLongPressed = false;
 		}
 		else if(event.getAction() == MotionEvent.ACTION_UP)
 		{
 			double dist = Math.sqrt(Math.pow(event.getX() - mDownPoint.x, 2) + Math.pow(event.getY() - mDownPoint.y, 2));
 			if(dist < Math.min(getWidth(), getHeight()) / 10 && !mOnLongPress)
 			{
 				Log.e("onTouchEvent", " ACTION_DOWN");
 				mCurrentTouchedPoint.x = event.getX(); 
 				mCurrentTouchedPoint.y = event.getY();
 
 				if(mAreaTouchListener != null)
 				{
 					int id = 0;
 					for(ForecastDataInterface fdi : mForecastData.keySet())
 					{
 						if(fdi.getType() == ForecastDataType.AREA)
 						{
 							RectF rect = mForecastData.get(fdi);
 							if(rect != null && rect.contains(mCurrentTouchedPoint.x, mCurrentTouchedPoint.y))
 							{
 								ForecastDataIdMapper forecastDataIdMapper = new ForecastDataIdMapper();
 								id = forecastDataIdMapper.get(fdi);
 								break;
 							}
 							else if(rect == null)
 								Log.e("MapWithForecast... onTouchEvent", " rect null for fdi " + fdi.getId());
 						}
 					}
 					Log.e("MapWithForecastImage.onDraw", "areaTouched with id " + id);
 					mAreaTouchListener.onAreaTouched(id);
 				}
 
 				this.invalidate();
 			}
 		}
 		return super.onTouchEvent(event);
 	}
 
 	@Override
 	public boolean onLongClick(View view) 
 	{
 		mOnLongPress = true;
 		PointF touchedPoint = new PointF(mDownPoint.x, mDownPoint.y);
 		Zone selectedZone = selectZone(touchedPoint);
 		/* if the long press hint is enabled, then disable it */
 		if(mZoneLongPressHintEnabled)
 			mDisableLongPressHint();
 
 		if(mAreaTouchListener != null)
 		{
 			int id = 0;
 			if(selectedZone != null)
 			{
 				ForecastDataIdMapper forecastDataIdMapper = new ForecastDataIdMapper();
 				id = forecastDataIdMapper.get(selectedZone);
 			}
 			Log.e("MapWithForecastImage.onLongClick", "areaTouched with id " + id);
 			mAreaTouchListener.onAreaTouched(id);
 		}
 
 		/* reset current touched point in order not to draw the selection on the icons,
 		 * in case the used long clicked on an icon.
 		 */
 		mCurrentTouchedPoint.x = mCurrentTouchedPoint.y = -1;
 		this.invalidate();
 		return true;
 	}
 
 	protected void mDisableLongPressHint()
 	{
 		mZoneLongPressHintEnabled = false;
 		Settings s = new Settings(getContext());
 		s.setZoneLongPressHintEnabled(false);
 		s = null;
 	}
 
 	protected void disableForecastIconsHint()
 	{
 		mHintForecastIconEnabled = false;
 		Settings s = new Settings(getContext());
 		s.setForecastIconsHintEnabled(false);
 		s = null;
 	}
 
 	/** selects the zone under the point p, deselects all the others
 	 * 
 	 * @param p the point to search
 	 * @return the selected zone
 	 */
 	private Zone selectZone(PointF p)
 	{
 		Zone selectedZone = null;
 		ZoneMapper zm = new ZoneMapper(this);
 		for(ForecastDataInterface f : mForecastData.keySet())
 		{
 			if(f.getType() == ForecastDataType.ZONE)
 			{
 				Zone z = (Zone ) f;
 				if(zm.getAreaRegion(z.getId()).contains(Math.round(p.x), Math.round(p.y)))
 				{
 					selectedZone = z;
 					z.setSelected(true);
 				}
 				else 
 					z.setSelected(false);
 			}
 		}
 		return selectedZone;
 	}
 
 	@SuppressWarnings("unused")
 	private ForecastDataInterface getForecastDataInterface(String id)
 	{
 		for(ForecastDataInterface f : mForecastData.keySet())
 			if(f.getId().compareTo(id) == 0)
 				return f;
 		return null;
 	}
 
 	private ViewType mViewType;
 	private HashMap<ForecastDataInterface, RectF> mForecastData;
 	private float mCachedTextFontSize;
 	private PointF mCurrentTouchedPoint;
 	private int mFontSize;
 	private ForecastDataStringMap mForecastDataStringMap;
 	private boolean mHintForecastIconEnabled, mZoneLongPressHintEnabled;
 	private boolean mOnLongPress, mLongPressed;
 	private AreaTouchListener mAreaTouchListener;
 	private PointF mDownPoint;
 }
