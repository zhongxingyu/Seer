 package com.utopia.lijiang.baidu;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 
 import com.baidu.mapapi.GeoPoint;
 import com.baidu.mapapi.ItemizedOverlay;
 import com.baidu.mapapi.MKPoiInfo;
 import com.baidu.mapapi.MapView;
 import com.baidu.mapapi.OverlayItem;
 import com.baidu.mapapi.Projection;
 
 public class BaiduItemizedOverlay extends ItemizedOverlay<OverlayItem>{
 
 	public static final int MARKER_WIDTH = 50;
 	public static final int MARKER_HEIGHT = 55;
 	
 	public Boolean isShowNumber;
 	
 	protected BaiduMapActivity activity = null;
 	protected List<OverlayItem> items = null;
 	protected Drawable marker;
 	
 	
 	public BaiduItemizedOverlay(BaiduMapActivity activity,Drawable marker){
 		this(activity,marker,null);
 	}
 		
 	public BaiduItemizedOverlay(BaiduMapActivity activity,Drawable marker,List<OverlayItem> items) {
 		super(boundCenterBottom(marker));
 		
 		this.activity = activity;
 		this.marker = marker;
 		this.items = items;
 		this.isShowNumber = false;
 		
 		if(this.items == null){
 			this.items = new ArrayList<OverlayItem>();	
 		}
 
 		configMarker();
 		populate();  
 	}
 
 	private void configMarker() {
 		this.marker.setBounds(0, 0, MARKER_WIDTH, MARKER_HEIGHT);
 		this.marker.setDither(true);
 		this.marker.setFilterBitmap(true);
 	}
 
 	public void setItems(List<OverlayItem> items){
 		this.items = null;
 		this.items = items;
 		populate();
 	}
 	
 	public void setData(List<MKPoiInfo> infos){
 		items.clear();
 		MKPoiInfo info = null;
 		Iterator<MKPoiInfo> it = infos.iterator(); 
 		while(it.hasNext()){
 			info = it.next();
 			items.add(new OverlayItem(info.pt,info.name,info.address));
 		}
 		
 		populate();  
 	}
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {	
 		super.draw(canvas, mapView, shadow);
 		boundCenterBottom(marker);
 		
 		Projection projection = mapView.getProjection(); 
 		for (int index = size() - 1; index >= 0; index--) { 
 			OverlayItem overLayItem = getItem(index);
			@SuppressWarnings("unused")
 			String title = overLayItem.getTitle();
 			Point point = projection.toPixels(overLayItem.getPoint(), null); 
 
 //			Hide Text
 			/*Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);
 			paintText.setTypeface(Typeface.DEFAULT_BOLD);
 			paintText.setFakeBoldText(true); 
 			paintText.setColor(Color.BLACK);
 			paintText.setTextSize(16f);
 			canvas.drawText(title, point.x-15, point.y+18, paintText); */
 			
 			//Draw number
 			if(isShowNumber){	
 //				Point markStartPoint = getMarkerStartPoint(point);
 		        Paint numberPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);  
 		        numberPaint.setColor(Color.WHITE);  
 		        numberPaint.setTypeface(Typeface.DEFAULT_BOLD); 
 		        
 		        int number = index +1;
 		        if(number/10 == 0){
 		        	numberPaint.setTextSize(19f);  
 		        	canvas.drawText(String.valueOf(number), point.x-5, point.y-26, numberPaint);
 		        }
 		        else{
 		        	numberPaint.setTextSize(17f);  
 		        	canvas.drawText(String.valueOf(number), point.x-10, point.y-26, numberPaint);
 		        }
 			}
 		}
 	
 	}
 
 	@SuppressWarnings("unused")
 	private Point getMarkerStartPoint(Point point){
 		Point pt = new Point();
 		//In draw() we used boundCenterBottom(marker); so use this to find start point
 		int x = point.x - BaiduItemizedOverlay.MARKER_WIDTH/2; //this.marker.getIntrinsicWidth()/2;
 		int y = point.y - BaiduItemizedOverlay.MARKER_HEIGHT;//this.marker.getIntrinsicHeight();
 		pt.set(x, y);
 		
 		return pt;	
 	}
 	
 	@Override
 	protected OverlayItem createItem(int i) {
 		return items.get(i);
 	}
 
 	@Override
 	public int size() {
 		return items.size();
 	}
 	
 	@Override	
 	protected boolean onTap(int i) {
 		setFocus(items.get(i));	
 		return activity.onTapped(i, items.get(i));
 	}
 
 	@Override
 	public boolean onTap(GeoPoint pt, MapView v) {
 		activity.onTapping(pt, v);
 		return super.onTap(pt, v);
 	}
 
 }
