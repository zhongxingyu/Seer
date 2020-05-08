 package project.DirectionOfPhoto;
 
 import java.io.IOException;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.media.ExifInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.LinearLayout;
 import android.widget.Switch;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ZoomControls;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class MapActivity extends com.google.android.maps.MapActivity implements
 		OnTouchListener {
 	private String path;
 	private float[] mapAddr = new float[2];
 	private GeoPoint nowPoint, oPoint;
 	private MapView mapView;
 	private int isEdit = 0;
 	private OverlayItem overlayItem;
 	private MapOverlay mapOverlay;
 	private ExifInterface exifInterface;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		// oBundle
 		Bundle bundle = this.getIntent().getExtras();
 		if (bundle != null)
 			path = bundle.getString("Image");
 
 		// ZOOM
 		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.zoomView);
 		mapView = (MapView) findViewById(R.id.mapView);
 		ZoomControls myZoom = (ZoomControls) mapView.getZoomControls();
 		linearLayout.addView(myZoom);
 
 		Drawable mdrawable = this.getResources().getDrawable(
 				R.drawable.ic_launcher);
 		mapOverlay = new MapOverlay(mdrawable);
 
 		// oJPGB~T
 		try {
 			exifInterface = new ExifInterface(path);
 			if (exifInterface.getLatLong(mapAddr)) {
 
 				oPoint = new GeoPoint((int) (mapAddr[0] * 1E6),
 						(int) (mapAddr[1] * 1E6));
 				nowPoint = oPoint;
 				// إ߷sOverlay
 				setOverlay(nowPoint);
 			} else
 				nowPoint = new GeoPoint(24968134, 121195464);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		/*
 		 * GeoPoint addPoint = new GeoPoint(24968134, 121195464); overlayItem =
 		 * new OverlayItem(addPoint, "", "");
 		 * mapOverlay.addOverlayItem(overlayItem);
 		 * mapView.getOverlays().add(mapOverlay);
 		 */
 
 		// ]waddPoint
 		mapView.getController().setCenter(nowPoint);
 
 		// oy
 		mapView.setOnTouchListener(this);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mapmenu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (item.getItemId()) {
 		case R.id.save:
 			if (isEdit == 0)
 				Toast.makeText(this, "Sק", Toast.LENGTH_SHORT).show();
 			else {
 				oPoint = nowPoint;
 				exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
 						toDMS(oPoint.getLatitudeE6() / 1E6));
 				exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
 						toDMS(oPoint.getLongitudeE6() / 1E6));
 				exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
 						"N");
 				exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
 						"E");
 				try {
 					exifInterface.saveAttributes();
 					Toast.makeText(this, "xs\", Toast.LENGTH_SHORT).show();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			finish();
 			break;
 		case R.id.load:
 			if (oPoint == null)
 				Toast.makeText(this, "ӨS[T", Toast.LENGTH_SHORT).show();
 			else {
 				setOverlay(oPoint);
 				isEdit = 0;
 			}
 			break;
 		case R.id.clear:
 			exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "");
 			exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "");
 			exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "");
 			exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "");
 			try {
 				exifInterface.saveAttributes();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			setOverlay();
 			Toast.makeText(this, "M\", Toast.LENGTH_SHORT).show();
 			finish();
 			break;
 		case R.id.quit:
 			builder.setTitle("T{")
 					.setMessage("T{{")
 					.setNegativeButton("", null)
 					.setPositiveButton("Tw",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int which) {
 									finish();
 								}
 							}).show();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		Switch edit = (Switch) findViewById(R.id.edit);
 		if (edit.isChecked() && event.getAction() == MotionEvent.ACTION_UP
 				&& event.getEventTime() - event.getDownTime() > 200) {
 			// event.getEventTime()-event.getDownTime() OUɶ
 			// oUy
 			nowPoint = mapView.getProjection().fromPixels((int) event.getX(),
 					(int) event.getY());
 
 			setOverlay(nowPoint);
 			isEdit = 1;
 
 		}
 		return super.onTouchEvent(event);
 
 		// UIy = nowPoint.getLatitudeE6()/1E6
 		// UIy = nowPoint.getLongitudeE6()/1E6
 	}
 
 	public String toDMS(double d) {
 		String s = new String();
 		s = "";
 		for (int i = 0; i < 3; i++) {
 
 			s += Integer.toString((int) d);
 			s += "/1";
 			d -= (int) d;
 			d *= 60;
 			if (i != 2)
 				s += ',';
 		}
 		return s;
 	}
 
 	public void setOverlay(GeoPoint p) {
 		TextView l1 = (TextView) findViewById(R.id.l1);
 		TextView l2 = (TextView) findViewById(R.id.l2);
 		l1.setText(toDMS(p.getLatitudeE6() / 1E6));
 		l2.setText(toDMS(p.getLongitudeE6() / 1E6));
 		// M
 		if (mapView.getOverlays().size() > 0) {
 			MapOverlay sitesOverlay = (MapOverlay) mapView.getOverlays().get(0);
 			sitesOverlay.clear();
 		}
 		// إ߷sOverlay
 		overlayItem = new OverlayItem(p, "", "");
 		mapOverlay.addOverlayItem(overlayItem);
 		mapView.getOverlays().add(mapOverlay);
 		// VsOverlay
 		mapView.getController().animateTo(p);
 	}
 
 	public void setOverlay() {
 		TextView l1 = (TextView) findViewById(R.id.l1);
 		TextView l2 = (TextView) findViewById(R.id.l2);
 		l1.setText("NULL");
 		l2.setText("NULL");
 		// M
 		if (mapView.getOverlays().size() > 0) {
 			MapOverlay sitesOverlay = (MapOverlay) mapView.getOverlays().get(0);
 			sitesOverlay.clear();
 		}
 
 	}
 }
