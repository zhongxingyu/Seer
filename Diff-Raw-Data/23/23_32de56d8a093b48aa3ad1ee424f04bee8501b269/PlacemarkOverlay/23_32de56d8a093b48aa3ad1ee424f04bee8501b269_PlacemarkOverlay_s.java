 package se.lanzen.LunchToday;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.Projection;
 
 public class PlacemarkOverlay extends ItemizedOverlay<OverlayItem> {
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	private int mColor = Color.RED;
 	private Context mContext;
 	private List<GeoPoint> mPolygon = null;
 	private String mResturantName;
	private GeoPoint mCenter; 
 	
 	@Override
 	public void draw(Canvas canvas, MapView mv, boolean shadow)
 	{
 	    Projection projection = mv.getProjection();
 	    Path path = new Path();
 	    Point from = new Point();
 	    // draw is called twice, with and without shadow.
 	    if(!shadow) {
 	    	
 		    Paint mPaint = new Paint();
 		    mPaint.setStyle(Style.FILL);
 		    mPaint.setColor(mColor);
 		    mPaint.setAntiAlias(true);
 	    } else {
 	    	Paint strokePaint = new Paint();
 	        strokePaint.setARGB(255, 0, 0, 0);
 	        strokePaint.setTextAlign(Paint.Align.CENTER);
 	        strokePaint.setTextSize(16);
 	        strokePaint.setTypeface(Typeface.DEFAULT_BOLD);
 	        strokePaint.setStyle(Paint.Style.STROKE);
 	        strokePaint.setStrokeWidth(2);
 
 	        Paint textPaint = new Paint();
 	        textPaint.setARGB(255, 200, 200, 200);
 	        textPaint.setTextAlign(Paint.Align.CENTER);
 	        textPaint.setTextSize(16);
 	        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
 	        projection.toPixels(mCenter, from);
	        textPaint.getTextPath(mResturantName, 0, mResturantName.length(), from.x, from.y - 40, path);
 	        canvas.drawPath(path, strokePaint);
 	        canvas.drawPath(path, textPaint);
 	    }
 	    super.draw(canvas, mv, shadow);
 	}
 
 	public void setPolygonColor(int color) {
 		mColor = color;
 	}
 //	public void addPlacemark(Placemark pm) {
 //		mPlacemarks.add(pm);
 //	}
 	
 	public void addOverlay(OverlayItem overlay) {
 	    mOverlays.add(overlay);
 	    populate();
 	}
 	
 //	public PlacemarkOverlay(Drawable defaultMarker) {
 //		super(boundCenterBottom(defaultMarker));
 //	}
 
 	public PlacemarkOverlay(Drawable drawable, Context context) {
 		super(boundCenterBottom(drawable));
 		mContext = context;
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		return mOverlays.get(i);
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
 	public int size() {
 		return mOverlays.size();
 	}
 
 	public void setPolygon(List<GeoPoint> polygon) {
 		mPolygon = polygon;
 		
 	}
 
 	public void setText(String resturantName) {
 		mResturantName = resturantName;
 	}
 
 	public void setCenter(GeoPoint gp) {
 		mCenter = gp;
 	}
 
 }
