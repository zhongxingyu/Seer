 package it.giacomos.android.osmer.widgets;
 
 import java.util.HashMap;
 
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.locationUtils.LocationNamesMap;
 import it.giacomos.android.osmer.network.state.ViewType;
 import it.giacomos.android.osmer.observations.ObservationData;
 import it.giacomos.android.osmer.observations.ObservationType;
 import it.giacomos.android.osmer.observations.ObservationsCache;
 import it.giacomos.android.osmer.observations.SituationImageObservationData;
 import it.giacomos.android.osmer.observations.SkyDrawableIdPicker;
 import it.giacomos.android.osmer.preferences.*;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.widget.Toast;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Parcelable;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class SituationImage extends MapWithLocationImage
 implements LatestObservationCacheChangeListener
 {
 	public SituationImage(Context context, AttributeSet attrs) 
 	{
 		super(context, attrs);
 		/* call by ourselves drawLocation, after the observation icons
 		 * are displayed.
 		 */
 		setDrawLocationEnabled(false);
 		mMap = new HashMap<Location, SituationImageObservationData>();
 		mObsRects = new HashMap<RectF, SituationImageObservationData>(); 
 		mCurrentTouchedPoint = new PointF(-1.0f, -1.0f);
 		mTxtRect = new Rect(); /* used in draw */
 		mSensibleArea = new RectF(); /* used in draw */
 		mLocationToImgPixelMapper = new LocationToImgPixelMapper(); /* used in draw */
 
 		int densityDpi = this.getResources().getDisplayMetrics().densityDpi;
 		/* adjust font according to density... */
 		if(densityDpi == DisplayMetrics.DENSITY_MEDIUM ||
 				densityDpi == DisplayMetrics.DENSITY_LOW)
 			mFontSize = 10;
 		else if(densityDpi == DisplayMetrics.DENSITY_HIGH)
 			mFontSize = 20;
 		else if(densityDpi == DisplayMetrics.DENSITY_XHIGH)
 			mFontSize = 25;
 		else if(densityDpi == DisplayMetrics.DENSITY_XXHIGH)
 			mFontSize = 30;
 		else if(densityDpi == DisplayMetrics.DENSITY_XXXHIGH)
 			mFontSize = 32;
 		else /* shouldn't happen */
 			mFontSize = 36;
 		/* in this class we use mPaint which is allocated in superclass */
 		mViewType = ViewType.HOME;
 		mHintForecastIconEnabled = new Settings(context).isForecastIconsHintEnabled();
 	}
 
 	public void setViewType(ViewType vt)
 	{
 		mViewType = vt;
 	}
 
 	public ViewType getViewType()
 	{
 		return mViewType;
 	}
 
 	public void onCacheUpdate(ObservationsCache oCache) 
 	{
 		dClearObservationsDataMap();
 
 		Resources res = this.getResources();
 		final String[] locations = { "Trieste", "Udine", "Gradisca d'Is.", "Pordenone",
 				"Tolmezzo", "Tarvisio", "Grado",
 				"Lignano", "Chievolis"
 		};
 
 		LocationNamesMap locMap = new LocationNamesMap();
 		for(int i = 0; i < locations.length; i++)
 		{
 			int drawableId = -1;
 			LatLng latlng = locMap.get(locations[i]);
 			if(latlng != null)
 			{
 				/* create a Loation not associated to any provider */
 				Location loc = new Location("");
 				loc.setLatitude(latlng.latitude);
 				loc.setLongitude(latlng.longitude);
 				ObservationData od = 
 						oCache.getObservationData(locations[i], ViewType.LATEST_TABLE);
 
 				if(od != null)
 				{
 					Bitmap iconBitmap = null;
 					String sky = od.get(ObservationType.SKY);
 					if(sky != null && !sky.contains("---"))
 					{
 						SkyDrawableIdPicker skyDrawableIdPicker = new SkyDrawableIdPicker();
 						drawableId = skyDrawableIdPicker.get(sky);
 						skyDrawableIdPicker = null;
 					}
 					if(drawableId > -1)
 						iconBitmap = BitmapFactory.decodeResource(res, drawableId);
 
 					String temp = od.get(ObservationType.TEMP);
 					String watTemp = od.get(ObservationType.SEA);
 					String snow = od.get(ObservationType.SNOW);	
 					String rain = od.get(ObservationType.RAIN);
 
 					mMap.put(loc, new SituationImageObservationData(od.location,
 							od.time, iconBitmap, temp, watTemp, snow, rain));
 				}
 			}
 		}
 		if(mMap.size() > 0)
 		{			
 			this.invalidate();
 		}
 	}
 
 	private void dClearObservationsDataMap()
 	{
 		for(SituationImageObservationData siod : mMap.values())
 		{
 			Bitmap icon = siod.getIcon();
 			if(icon != null)
 			{
 				icon.recycle();
 				icon = null;
 			}
 		}
 		mMap.clear();
 	}
 
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		Resources res = getResources();
 		mObsRects.clear();
 		String temp = ""; /* temperature */
 		/* mPaint is allocated in the superclass */
 		mPaint.setTextSize(21);
 
 		float startOfXText = 0;
 		float yCopyrightText = this.getHeight() - 5;
 
 		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
 		{
 			/* data source drawing on home screen has been disabled.
 			 * In low resolution devices it does not fit into the image.
 			 */
 			mPaint.setARGB(255, 0, 0, 0);
 			mPaint.setTextSize(mFontSize);			
 			/* Copyright below */
 			/* (C) 2013 Giacomo Strangolino */
 			startOfXText = 4;
 			String author = res.getString(R.string.author) + " " + res.getString(R.string.author_web);
 			mPaint.getTextBounds(author, 0, author.length(), mTxtRect);
 			canvas.drawText(author, startOfXText, yCopyrightText, mPaint);
 			yCopyrightText -= mTxtRect.height();
 		}
 
 
 
 		/* draw observation icons and text ! */
 		for(Location l : mMap.keySet())
 		{
 			PointF p = mLocationToImgPixelMapper.mapToPoint(this, l);
 			PointF iconPt = p;
 			float y = p.y;
 			float yMin = y;
 			float yMax = y;
 			float xMin = p.x;
 			float xMax = p.x;
 			float nextY = p.y, nextX = p.x;
 			Bitmap icon = null;
 			SituationImageObservationData d = mMap.get(l);
 			if(d.hasIcon())
 			{	
 				icon = d.getIcon();
 				iconPt.x -= icon.getWidth() / 2;
 				iconPt.y -= icon.getHeight();
 				yMin = iconPt.y;
 				xMax = iconPt.x + icon.getWidth();
 				yMax = yMin + icon.getHeight();
 				xMin = iconPt.x;
 				nextY = yMax;
 				/* nextX remains the same, p.x */
 				canvas.drawBitmap(icon, iconPt.x, iconPt.y, null);
 			}
 			else /* just draw a circle around the location */
 			{
 				mPaint.setARGB(255, 20, 30, 250);
 				canvas.drawCircle(p.x, p.y, 3, mPaint);
 				yMin = p.y - 3;
 				yMax = p.y + 3;
 				xMin = p.x - 3;
 				xMax = p.x + 3;
 				nextY = yMax + 1; 
 				nextX = xMax + 1; 
 			}
 			if(d.hasTemp())
 			{
 				mPaint.setARGB(255, 0, 0, 0);
 				temp = d.getTemp();
 				mPaint.getTextBounds(temp, 0, temp.length(), mTxtRect);
 				canvas.drawText(temp, nextX, nextY, mPaint);
 				if(nextX + mTxtRect.width() > xMax)
 					xMax = nextX + mTxtRect.width();
 				if(yMin > nextY - mTxtRect.height())
 					yMin = nextY -  mTxtRect.height();
 				yMax = nextY;
 				/* - xMin is not changed
 				 */
 				nextY += mTxtRect.height() + 4;
 				nextX += mTxtRect.width() / 5;
 			}
 			if(d.hasWaterTemp())
 			{
 				mPaint.setARGB(255, 0, 0, 255);
 				temp = d.getWaterTemp();
 				mPaint.getTextBounds(temp, 0, temp.length(), mTxtRect);
 				canvas.drawText(temp, nextX, nextY, mPaint);
 				yMax = nextY;
 				nextY += mTxtRect.height() + 4;
 				if(nextX + mTxtRect.width() > xMax)
 					xMax = nextX + mTxtRect.width();
 				/* xMin not changed */
 
 
 			}
 			if(d.hasSnow())
 			{
 				/* paint above icon */
 				mPaint.setARGB(255, 255, 255, 255);
 				temp = "*" + d.getSnow();
 				if(icon != null) /* 4 pixels above the icon */
 					y -= icon.getHeight() / 2 - 4;
 				else
 					y -= mTxtRect.height() + 2;
 				canvas.drawText(temp, p.x, y, mPaint);
 				mPaint.getTextBounds(temp, 0, temp.length(), mTxtRect);
 				if(p.x + mTxtRect.width() > xMax)
 					xMax = p.x + mTxtRect.width();
 				if(yMin >  y - mTxtRect.height())
 					yMin = y - mTxtRect.height();
 				yMax = nextY;
 				nextY += mTxtRect.height() + 4;
 			}
 			/* give a two pixel margin to sensible area rect */
 			mSensibleArea.left = xMin - 2;
 			mSensibleArea.top = yMin - 2;
 			mSensibleArea.right = xMax + 2;
 			mSensibleArea.bottom = yMax + 2;
 
 			//canvas.drawRoundRect(sensibleArea, 6, 6, mPaint);
 			mObsRects.put(mSensibleArea, d);
 			/* calculate text height using a capital X */
 			mPaint.getTextBounds("X", 0, 1, mTxtRect);
 
 			/* start of painting: copyright top */
 			y = yCopyrightText - 8;
 
 			/* colour for text and circles */
 			mPaint.setARGB(255, 10, 255, 10);
 
 			if(mSensibleArea.contains(mCurrentTouchedPoint.x, mCurrentTouchedPoint.y))
 			{
 				mPaint.setStyle(Paint.Style.STROKE);
 				canvas.drawRoundRect(mSensibleArea, 6, 6, mPaint);
 				mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 				mPaint.setARGB(255, 16, 85, 45);
 				String text = "";
 
 				if(d.hasRain())
 				{
 					String rain = d.getRain();
 					rain = rain.replaceAll("[^\\d+\\.)]", "");

					if(Float.parseFloat(rain) > 0.0f)
 					{
						try
 						{
 							text =  mRainStr + ": " + d.getRain();
 							canvas.drawText(text, 4, y, mPaint);
 							y -= (mTxtRect.height() + 5);
 						}
						catch(NumberFormatException nfe)
						{
 
						}
 					}
 				}
 				if(d.hasSnow())
 				{
 					text = mSnowStr + ": " + d.getSnow();
 					canvas.drawText(text, 4, y, mPaint);
 					y -= (mTxtRect.height() + 5); 
 				}
 				if(d.hasWaterTemp())
 				{
 					text = mSeaStr + ": " + d.getWaterTemp();
 					canvas.drawText(text, 4, y, mPaint);
 					y -= (mTxtRect.height() + 5); 
 				}
 				if(d.hasTemp())
 				{
 					text = d.getTemp();
 					canvas.drawText(text, 4, y, mPaint);
 					y -= (mTxtRect.height() + 5); 
 				}
 				canvas.drawText(d.getLocation() + " [" + d.getTime() + "]", 4, y, mPaint);
 
 				/* disable forecast icon hint */
 				if(mHintForecastIconEnabled)
 					this.disableForecastIconsHint();
 			}
 		}
 		/* finally, call drawLocation */
 		drawLocation(canvas);
 	}
 
 	public boolean onTouchEvent (MotionEvent event)
 	{
 		if(event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			mCurrentTouchedPoint.x = event.getX();
 			mCurrentTouchedPoint.y = event.getY();
 			this.invalidate();
 		}
 		return super.onTouchEvent(event);
 	}
 
 	public void cleanup()
 	{
 		/* for each SituationImageObservationData, gets the icon and recycles it
 		 * and then the map is cleared
 		 */
 		dClearObservationsDataMap();
 
 		for(SituationImageObservationData siod : mObsRects.values())
 		{
 			Bitmap bmp = siod.getIcon();
 			if(bmp != null)
 			{
 				bmp.recycle();
 				bmp = null;
 			}
 		}
 		mObsRects.clear();
 	}
 
 	protected void disableForecastIconsHint()
 	{
 		mHintForecastIconEnabled = false;
 		Settings s = new Settings(getContext());
 		s.setForecastIconsHintEnabled(false);
 		s = null;
 	}
 
 	private Rect mTxtRect;
 	private RectF mSensibleArea;
 	private ViewType mViewType;
 	LocationToImgPixelMapper mLocationToImgPixelMapper;
 
 	private HashMap<Location, SituationImageObservationData> mMap;
 	private HashMap<RectF, SituationImageObservationData> mObsRects;
 
 	private PointF mCurrentTouchedPoint;
 	private final String mSnowStr = getResources().getString(R.string.snow);
 	private final String mSeaStr = getResources().getString(R.string.sea);
 	private final String mRainStr = getResources().getString(R.string.rain);	
 	private int mFontSize;
 	private boolean mHintForecastIconEnabled;
 }
