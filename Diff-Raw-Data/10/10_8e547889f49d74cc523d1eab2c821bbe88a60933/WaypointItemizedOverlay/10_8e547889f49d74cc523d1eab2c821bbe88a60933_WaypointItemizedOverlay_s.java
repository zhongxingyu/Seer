 package com.example.waypoint;
 
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.util.Xml;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class WaypointItemizedOverlay extends ItemizedOverlay<OverlayItem> {
 	private Context mContext;
 	
 	private OverlayItem dragItem = null;
 	private ImageView dragImage=null;
     private int xDragImageOffset=0;
     private int yDragImageOffset=0;
     private int xDragTouchOffset=0;
     private int yDragTouchOffset=0;
     private Drawable marker=null;
 	
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	
 	public WaypointItemizedOverlay(Drawable defaultMarker) {
 		super(boundCenterBottom(defaultMarker));
 		
 		this.marker = defaultMarker;
 		
 		mOverlays.add(new OverlayItem(getPoint(40.748963847316034,
 		                                          -73.96807193756104),
 		                                "UN", "United Nations"));
 		      
 		populate();    
 		
 	}
 	
 	private GeoPoint getPoint(double lat, double lon) {
         return(new GeoPoint((int)(lat*1000000.0),(int)(lon*1000000.0)));
     }
 	
 	public WaypointItemizedOverlay(Drawable defaultMarker, Context context) {
 		  super(boundCenterBottom(defaultMarker));
 		  mContext = context;
 		 
 		  //Make the imageview from scratch because the R file refuses to work.
 		  dragImage= new ImageView(context);  
 		  dragImage.setImageDrawable(defaultMarker);
 		  //dragImage.setLayoutParams(new LayoutParams(context,Xml.asAttributeSet(context.getResources().getXml(R.layout.drag))));
		  XmlPullParser parser = context.getResources().getXml(R.layout.drag);
		  AttributeSet attr = Xml.asAttributeSet(parser);
		  LayoutParams lp = new LayoutParams(context,attr);

 		  dragImage.setLayoutParams(lp);
 		  
 		  Log.i("ETHAN", "MADE THE IMAGE VIEW");
 		  
 		  xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
 		  yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();
 		  this.marker = defaultMarker;
 			
 	      
 		  mOverlays.add(new OverlayItem(getPoint(40.748963847316034,
 		                                          -73.96807193756104),
 		                                "UN", "United Nations"));
 		      
 		  populate();    
 	}
 
 	@Override
 	protected boolean onTap(int index) {
 	  OverlayItem item = mOverlays.get(index);
 	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
 	  dialog.setTitle(item.getTitle());
 	  dialog.setMessage(item.getSnippet());
 	  dialog.show();
 	  return true;
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 	  return mOverlays.get(i);
 	}
 	
 	@Override
 	public int size() {
 	  return mOverlays.size();
 	}
 	
  
     public boolean onTouchEvent(MotionEvent event,MapView map) {
     	//Get information about the action
         final int action=event.getAction();
         final int x=(int)event.getX();
         final int y=(int)event.getY();
         //To be returned:
         boolean result=false;
         
         
         if (action==MotionEvent.ACTION_DOWN) {
         	boolean createNewItem = false;
             for (OverlayItem item : mOverlays) {
               Point p=new Point(0,0);
               
               //This places a point into p that is actually useful
               map.getProjection().toPixels(item.getPoint(), p);
               
               //Are we touching this item?
               if (hitTest(item, marker, x-p.x, y-p.y)) {
                   result=true;
                   dragItem =item;
                   mOverlays.remove(dragItem);
                   populate();
 
                   xDragTouchOffset=0;
                   yDragTouchOffset=0;
                   
                   setDragImagePosition(p.x, p.y);
                   dragImage.setVisibility(View.VISIBLE);
 
                   xDragTouchOffset=x-p.x;
                   yDragTouchOffset=y-p.y;
                   createNewItem = true;
                   break;
                 }
             }
             if(createNewItem){
             	//Put in code for us making a new item where we clicked!
             	Log.i("Create New Item True", "We want to create an item");
             }
         }
         else if(action==MotionEvent.ACTION_MOVE && dragItem!=null){
         	setDragImagePosition(x,y);
         	result = true;
         }
         else if(action==MotionEvent.ACTION_UP && dragItem!=null){
         	dragImage.setVisibility(View.GONE);
             
             GeoPoint pt=map.getProjection().fromPixels(x-xDragTouchOffset,
                                                        y-yDragTouchOffset);
             OverlayItem toDrop=new OverlayItem(pt, dragItem.getTitle(),
                                                dragItem.getSnippet());
             
             mOverlays.add(toDrop);
             populate();
             
             dragItem=null;
             result=true;
         }
         return (result || super.onTouchEvent(event,map));
     }
     
     //Yoink! ( https://github.com/commonsguy/cw-advandroid/blob/master/Maps/NooYawkTouch/src/com/commonsware/android/maptouch/NooYawk.java )
     private void setDragImagePosition(int x, int y) {
         RelativeLayout.LayoutParams lp=
           (RelativeLayout.LayoutParams)dragImage.getLayoutParams();
               
         lp.setMargins(x-xDragImageOffset-xDragTouchOffset,y-yDragImageOffset-yDragTouchOffset, 0, 0);
         dragImage.setLayoutParams(lp);
       }
 	
 	public void addOverlay(OverlayItem overlay) {
 	    mOverlays.add(overlay);
 	    populate();
 	}
 
 }
