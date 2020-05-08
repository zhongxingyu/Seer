 package it.giacomos.android.osmer.widgets.map;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import it.giacomos.android.osmer.locationUtils.GeoCoordinates;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.Circle;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.GroundOverlay;
 import com.google.android.gms.maps.model.GroundOverlayOptions;
 import com.google.android.maps.Overlay;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.Paint;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.LayerDrawable;
 import android.util.Log;
 
 public class RadarOverlay extends Overlay implements OOverlayInterface
 {
 	private GoogleMap mMap;
 	private GroundOverlay mGroundOverlay;
 	private Circle mGroundOverlayCircle;
 	private CircleOptions mCircleOptions;
 	private GroundOverlayOptions mGroundOverlayOptions;
 	
 
 	public static final long ACCEPTABLE_RADAR_DIFF_TIMESTAMP_MILLIS = 1000 * 60 * 60;
 	
 	RadarOverlay(GoogleMap googleMap) 
 	{
 		mMap = googleMap;
 		/* ground overlay: the radar image */
 		mGroundOverlay = null;
 		mGroundOverlayCircle = null;
 		mGroundOverlayOptions = new GroundOverlayOptions();
 		mGroundOverlayOptions.positionFromBounds(GeoCoordinates.radarImageBounds);
 		mGroundOverlayOptions.transparency(0.65f);
 		/* circle: delimits the radar area */
 		int color = Color.argb(120, 150, 160, 245);
 		mCircleOptions = new CircleOptions();
 		mCircleOptions.radius(GeoCoordinates.radarImageRadius());
 		mCircleOptions.center(GeoCoordinates.radarImageCenter);
 		mCircleOptions.strokeColor(color);
 		mCircleOptions.strokeWidth(1);
 	}
 
 	
 	@Override /* no info windows on radar layer */
 	public void hideInfoWindow()
 	{
 		
 	}
 	
 	@Override /* no info windows on radar layer */
 	public boolean isInfoWindowVisible()
 	{
 		return false;
 	}
 	
 	@Override
 	public int type() 
 	{
 		return OverlayType.RADAR;
 	}
 	
 	public Bitmap getBitmap()
 	{
 		return mBitmap;
 	}
 	
 	/** Remove the ground overlay from the map.
 	 *  Bitmap remains valid.
 	 */
 	@Override
 	public void clear()
 	{
 		if(mGroundOverlay != null)
 		{
 			mGroundOverlay.remove();
 			mGroundOverlay = null;
 		}
 		if(mGroundOverlayCircle != null)
 		{
 			mGroundOverlayCircle.remove();
 			mGroundOverlayCircle = null;
 		}
 	}
 	
 	public void update()
 	{
 		mRefreshBitmap();
 	}
 	
 
 	public void updateBlackAndWhite() 
 	{
		if(mBitmap == null)
			return;
		
 		Bitmap blackAndWhiteBmp;
 		Log.e("updateBlackAndWhite", "creo black and white");
 	//	update();
 		/* bitmap to black and white */
 		int width, height;
 	    height = mBitmap.getHeight();
 	    width = mBitmap.getWidth();    
 	    blackAndWhiteBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 	    Canvas c = new Canvas(blackAndWhiteBmp);
 	    Paint paint = new Paint();
 	    ColorMatrix cm = new ColorMatrix();
 	    cm.setSaturation(0);
 	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
 	    paint.setColorFilter(f);
 	    c.drawBitmap(mBitmap, 0, 0, paint);
 	    mBitmap.recycle();
 	    mBitmap = blackAndWhiteBmp;
 	    update();
 	}
 	
 	private void mRefreshBitmap()
 	{
 		if(mBitmap == null)
 			return;
 		
 		if(mGroundOverlay != null)
 		{
 			mGroundOverlay.remove();
 			mGroundOverlay = null;
 		}
 		if(mGroundOverlayCircle == null)
 			mGroundOverlayCircle = mMap.addCircle(mCircleOptions);
 		
 		/* specify the image before the ovelay is added */
 		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(mBitmap);
 		mGroundOverlayOptions.image(bitmapDescriptor);
 		mGroundOverlay = mMap.addGroundOverlay(mGroundOverlayOptions);
 	}
 	
 	public boolean bitmapValid()
 	{
 		return mBitmap != null;
 	}
 	
 	public void updateBitmap(Bitmap bmp)
 	{
 		if(mBitmap != null)
 			mBitmap.recycle();
 		mBitmap = null;
 		mBitmap = bmp;
 	}
 	
 	public void restoreFromInternalStorage(Context ctx)
 	{
 		File filesDir = ctx.getApplicationContext().getFilesDir();
 		Bitmap bmp = BitmapFactory.decodeFile(filesDir.getAbsolutePath() + "/lastRadarImage.bmp");
 		if(bmp != null)
 			updateBitmap(bmp);
 	}
 	
 	public boolean saveOnInternalStorage(Context ctx) 
 	{	
 		if(mBitmap != null)
 		{
 			FileOutputStream fos;
 			try 
 			{
 				fos = ctx.openFileOutput("lastRadarImage.bmp", Context.MODE_PRIVATE);
 				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);	
 				fos.close();
 				return true; /* success */
 			} 
 			catch (FileNotFoundException e) {
 					/* nada que hacer */
 			}
 			catch (IOException e) {
 
 			}
 		}
 		return false; /* bitmap null or impossible to save on file */
 	}	
 	
 	public void finalize()
 	{
 		if(mBitmap != null)
 			mBitmap.recycle();
 		mBitmap = null;
 	}
 	
 	private Bitmap mBitmap;
 
 }
