 package com.fbrs.zombieoutbreak.pages.map;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import android.util.Log;
 
 import com.fbrs.zombieoutbreak.R;
 import com.fbrs.zombieoutbreak.pages.InfestMap;
 import com.fbrs.zombieserver.map.serial.OverLayItemDescriptor;
 import com.fbrs.zombieserver.map.serial.QuadTree;
 import com.fbrs.zombieserver.map.serial.RectF;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class ZombieOverlay extends Overlay{
 
 	private Bitmap bmp = getBMP();
 	private QuadTree overlayList;
 	private Paint paint = new Paint();
 
 
 	public ZombieOverlay()
 	{
 		try {
 			overlayList = ParseMap.getFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e("ZombieOutbreak", null, e);
 		}
 		paint.setStrokeWidth(1);
 		paint.setARGB(255, 255, 0, 0);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeCap(Paint.Cap.ROUND);
 	}
 
 	public int prv_dept = 0;
 	@Override
 	public boolean draw(Canvas canvas, MapView mapv, boolean shadow, long when)
 	{
 		super.draw(canvas,mapv, shadow);
 		paint.setStrokeWidth(10);
 		Projection MapProjections  = mapv.getProjection();
 		GeoPoint center = mapv.getMapCenter();
 		int latSpan = mapv.getLatitudeSpan()/2;
 		int lngSpan = mapv.getLongitudeSpan()/2;
 		GeoPoint a = new GeoPoint(center.getLatitudeE6() + latSpan, center.getLongitudeE6() + lngSpan);
 		GeoPoint b = new GeoPoint(center.getLatitudeE6() - latSpan, center.getLongitudeE6() - lngSpan);
 		
 		//GeoPoint a = MapProjections.fromPixels(mapv.getLeft(), mapv.getTop());
 		//GeoPoint b = MapProjections.fromPixels(mapv.getRight(), mapv.getBottom());
 		//Log.v("ZombieOutbreak", "Rect = " + b.getLatitudeE6()+","+ a.getLongitudeE6()+","+ a.getLatitudeE6()+","+ b.getLongitudeE6());
 		ArrayList<OverLayItemDescriptor> descList;
		descList = overlayList.query2D(new RectF(b.getLatitudeE6(), a.getLongitudeE6(), a.getLatitudeE6(), b.getLongitudeE6()), mapv.getZoomLevel()+2);
 
 		if(descList != null) {
 			//Log.v("ZombieOutbreak", "Drawing " + descList.size() + " nodes");
 			//String s = "";
 			for(OverLayItemDescriptor desc : descList) {
 				if(desc.zombie) {
 					paint.setARGB(255, 255, 0, 0);
 					//Log.v("ZombieOutbreak", "Found Zombie");
 				}
 				else
 					paint.setARGB(255, 0, 255, 0);
 				Point drawPoint = new Point();
 //				drawPoint = mapv.getProjection().toPixels(new GeoPoint(-40000000, -40000000), drawPoint);
 				drawPoint = mapv.getProjection().toPixels(new GeoPoint(desc.lat, desc.lng), drawPoint);
				Log.v("ZombieOutbreak", desc.lat + " => " + drawPoint.x + " _ " + desc.lng + "=>" +drawPoint.y);
 				canvas.drawPoint(drawPoint.x, drawPoint.y, paint);
 //
 			}
 		}
 		else
 			Log.e("ZombieOutbreak", "Tree returned nothing");
 		return true;
 	}
 	//Depreciated
 	public Bitmap getBMP()
 	{
 		try {
 			URL url = new URL("https://s3.amazonaws.com/ZombieMap/map.png");
 			BufferedInputStream bis = new BufferedInputStream(url.openConnection().getInputStream());
 			return bmp = BitmapFactory.decodeStream(bis);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
