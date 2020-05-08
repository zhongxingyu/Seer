 package com.vuzzz.android.address;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.location.Address;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 import com.vuzzz.android.DimenHelper;
 
 public class SearchOverlay extends Overlay {
 
 	private static final String LOGO_LETTER = "V";
 	private final float CUBE_SIZE;
 	private final float HALF_CUBE_SIZE;
 	private final float THICKNESS;
 	private final float FONT_SIZE;
 	private final float TOP_PADDING;
 
 	private final Paint paint = new Paint();
 	private final Paint textPaint = new Paint();
 	private final int color = 0xFF33B5E5;
 
 	private static final Point point = new Point();
 
 	private List<Address> addresses;
 	private List<GeoPoint> geopoints;
 
 	private List<RectF> hitRects;
 	private float logoLetterHalfWidth;
 	private float halfTextSize;
 
 	public SearchOverlay(Context context) {
 
 		CUBE_SIZE = DimenHelper.pixelSize(context, 25f);
 		HALF_CUBE_SIZE = CUBE_SIZE / 2;
 		THICKNESS = DimenHelper.pixelSize(context, 5f);
 		FONT_SIZE = DimenHelper.pixelSize(context, 15f);
 		TOP_PADDING = DimenHelper.pixelSize(context, 2f);
 
 		paint.setAntiAlias(true);
 		paint.setStyle(Style.FILL);
 		textPaint.setAntiAlias(true);
 		textPaint.setColor(0xFFFFFFFF);
 		textPaint.setTextSize(FONT_SIZE);
 		textPaint.setFakeBoldText(true);
 		logoLetterHalfWidth = textPaint.measureText(LOGO_LETTER) / 2;
 		halfTextSize = FONT_SIZE / 2;
 
 	}
 
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 
 		if (addresses == null) {
 			return;
 		}
 
 		if (shadow) {
 			return;
 		}
 
 		Projection projection = mapView.getProjection();
 
 		int i = 0;
 		for (GeoPoint geopoint : geopoints) {
 
 			RectF rect = hitRects.get(i);
 
 			projection.toPixels(geopoint, point);
 
 			Path path = new Path();
 
 			float top = point.y - THICKNESS - TOP_PADDING;
 			float left = point.x - HALF_CUBE_SIZE;
 
 			rect.left = left;
 			rect.top = top;
 			rect.right = left + CUBE_SIZE + THICKNESS;
 			rect.bottom = top + CUBE_SIZE + THICKNESS;
 
 			// Whole thing
 			path.reset();
 			path.moveTo(left, top);
 			path.lineTo(left + CUBE_SIZE, top);
 			path.lineTo(left + CUBE_SIZE + THICKNESS, top + THICKNESS);
 			path.lineTo(left + CUBE_SIZE + THICKNESS, top + CUBE_SIZE + THICKNESS);
 			path.lineTo(left + THICKNESS, top + CUBE_SIZE + THICKNESS);
 			path.lineTo(left, top + CUBE_SIZE);
 			path.close();
 			paint.setColor(color);
 			canvas.drawPath(path, paint);
 
 			//
 			path.reset();
 			path.moveTo(left, top + CUBE_SIZE);
 			path.lineTo(left + CUBE_SIZE, top + CUBE_SIZE);
 			path.lineTo(left + CUBE_SIZE + THICKNESS, top + CUBE_SIZE + THICKNESS);
 			path.lineTo(left + THICKNESS, top + CUBE_SIZE + THICKNESS);
 			path.close();
 			paint.setColor(0x33000000);
 			canvas.drawPath(path, paint);
 
 			path.reset();
 			path.moveTo(left + CUBE_SIZE, top);
 			path.lineTo(left + CUBE_SIZE + THICKNESS, top + THICKNESS);
 			path.lineTo(left + CUBE_SIZE + THICKNESS, top + CUBE_SIZE + THICKNESS);
 			path.lineTo(left + CUBE_SIZE, top + CUBE_SIZE);
 			path.close();
 			paint.setColor(0x11000000);
 			canvas.drawPath(path, paint);
 
 			float middleX = left + HALF_CUBE_SIZE - logoLetterHalfWidth;
 			float middleY = top + CUBE_SIZE - halfTextSize;
 			canvas.drawText(LOGO_LETTER, middleX, middleY, textPaint);
 
 			i++;
 		}
 
 	}
 
 	public void setAddresses(List<Address> addresses, List<GeoPoint> geopoints) {
 		this.addresses = addresses;
 		this.geopoints = geopoints;
 		hitRects = new ArrayList<RectF>(addresses.size());
		for (GeoPoint geopoint : geopoints) {
 			hitRects.add(new RectF());
 		}
 	}
 
 	public Address onSingleTapUp(float x, float y) {
 
 		if (addresses == null) {
 			return null;
 		}
 
 		int i = 0;
 		for (RectF hitRect : hitRects) {
 
 			if (hitRect.contains(x, y)) {
 				return addresses.get(i);
 			}
 
 			i++;
 		}
 
 		return null;
 	}
 
 }
