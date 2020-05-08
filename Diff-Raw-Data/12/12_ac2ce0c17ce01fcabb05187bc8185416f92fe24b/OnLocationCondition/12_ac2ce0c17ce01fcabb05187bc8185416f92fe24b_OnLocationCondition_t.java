 /**
  * 
  */
 package es.udc.choveduro.ifttd.conditions;
 
 import java.io.IOException;
import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 import es.udc.choveduro.ifttd.EasyActivity;
 import es.udc.choveduro.ifttd.R;
 import es.udc.choveduro.ifttd.consequences.ShowNotification;
 import es.udc.choveduro.ifttd.types.CallbackIF;
 import es.udc.choveduro.ifttd.types.Condition;
 
 /**
  * @author ch01
  * 
  */
 public class OnLocationCondition extends Condition {
 
 	/**
 	 */
 	@Override
 	public String getName() {
 		return "Localización";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see es.udc.choveduro.ifttd.types.Condition#getImageResource()
 	 */
 	@Override
 	public int getImageResource() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see es.udc.choveduro.ifttd.types.Condition#getShortDesc()
 	 */
 	@Override
 	public String getShortDesc() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * es.udc.choveduro.ifttd.types.Condition#configure(es.udc.choveduro.ifttd
 	 * .EasyActivity, es.udc.choveduro.ifttd.types.CallbackIF)
 	 */
 	@Override
 	public void configure(EasyActivity ctx, final CallbackIF callback) {
 		ctx.launchActivity(PrefsActivity.class, new CallbackIF(){
 
 			@Override
 			public void resultOK(String resultString, Bundle resultMap) {
				HashMap<String, Serializable> configuration = OnLocationCondition.this.getConfig();
 				configuration.put("latitude", OnLocationCondition.PrefsActivity.sp.getString("latitud", "0"));
 				configuration.put("longitude", OnLocationCondition.PrefsActivity.sp.getString("longitud", "0"));
 				configuration.put("distance", OnLocationCondition.PrefsActivity.sp.getString("distance", "0"));
 				callback.resultCancel(resultString, resultMap);
 			}
 
 			@Override
 			public void resultCancel(String resultString, Bundle resultMap) {
 				callback.resultCancel(resultString, resultMap);
 				
 			}
 			
 		});
 
 	}
 	
 
 
 	public final static class PrefsActivity extends PreferenceActivity {
 
 		static SharedPreferences sp;
 		
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.preferences_location);
 			setContentView(R.layout.preferences_general);
 			if(sp == null){
 				PrefsActivity.sp = getPreferences(MODE_WORLD_READABLE);
 			}
 		}
 	}
 
 	public final static class SetLocationMapActivity extends MapActivity {
 
 		GeoPoint p;
 
 		class PuntoMapOverlay extends com.google.android.maps.Overlay {
 			@Override
 			public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
 					long when) {
 				super.draw(canvas, mapView, shadow);
 
 				// ---translate the GeoPoint to screen pixels---
 				Point screenPts = new Point();
 				mapView.getProjection().toPixels(p, screenPts);
 
 				// ---add the marker---
 				Bitmap bmp = BitmapFactory.decodeResource(getResources(),
 						R.drawable.symbol_inf);
 				
 				canvas.drawBitmap(bmp, (screenPts.x - bmp.getWidth())/2, (screenPts.y - bmp.getHeight())/2, null);
 				return true;
 			}
 		}
 		
 		class TouchMapOverlay extends com.google.android.maps.Overlay {
 			@Override
 			public boolean onTouchEvent(MotionEvent event, MapView mapView) {
 				// ---when user lifts his finger---
 				if (event.getAction() == 1){
 					p = mapView.getProjection().fromPixels((int) event.getX(),
 							(int) event.getY());
 					MapView map = (MapView) findViewById(R.id.mapviewselect);
 					if(map.getOverlays().size() == 1){
 						map.getOverlays().add(new PuntoMapOverlay());
 					}
 				}
 				return false;
 			}
 
 		}
 
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			setContentView(R.layout.select_point_map);
 
 			MapView map = (MapView) findViewById(R.id.mapviewselect);
 			map.setBuiltInZoomControls(true);
 			map.getOverlays().add(new TouchMapOverlay());
 			
 			
 			Button b_accept = (Button) findViewById(R.id.configure_accept_button);
 			b_accept.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					if (p != null) {
 						OnLocationCondition.PrefsActivity.sp.edit().putInt("longitud", p.getLongitudeE6());
 						OnLocationCondition.PrefsActivity.sp.edit().putInt("latitud", p.getLatitudeE6());
 
 						SetLocationMapActivity.this.finish();
 
 					} else {
 		                Toast.makeText(SetLocationMapActivity.this, "Select some location!!", Toast.LENGTH_SHORT).show();
 					}
 
 				}
 
 			});
 
 			Button b_cancel = (Button) findViewById(R.id.configure_accept_button);
 			b_cancel.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					SetLocationMapActivity.this.finish();
 				}
 
 			});
 		}
 
 		@Override
 		protected boolean isRouteDisplayed() {
 			return false;
 		}
 
 	}
 
 	public final static class SetPlaceMapActivity extends MapActivity {
 
 		GeoPoint p;
 
 		class PuntoMapOverlay extends com.google.android.maps.Overlay {
 			@Override
 			public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
 					long when) {
 				super.draw(canvas, mapView, shadow);
 
 				// ---translate the GeoPoint to screen pixels---
 				Point screenPts = new Point();
 				mapView.getProjection().toPixels(p, screenPts);
 
 				// ---add the marker---
 				Bitmap bmp = BitmapFactory.decodeResource(getResources(),
 						R.drawable.symbol_inf);
 				
 				canvas.drawBitmap(bmp, (screenPts.x - bmp.getWidth())/2, (screenPts.y - bmp.getHeight())/2, null);
 				return true;
 			}
 		}
 		
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			setContentView(R.layout.select_point_map);
 
 			final MapView map = (MapView) findViewById(R.id.mapviewselect);
 			map.setBuiltInZoomControls(true);
 			
 			Button b_search = (Button) findViewById(R.id.searchLocationButton);
 			b_search.setOnClickListener(new OnClickListener(){
 
 				@Override
 				public void onClick(View v) {
 					
 					EditText tf =  (EditText)findViewById(R.id.location_field);
 					String sitio =  tf.getText().toString();
 					
 					if(sitio.equals("")){
 		                Toast.makeText(SetPlaceMapActivity.this, "Write something!!", Toast.LENGTH_SHORT).show();
 						return;
 					}
 					
 					Geocoder geoCoder = new Geocoder(SetPlaceMapActivity.this, Locale.getDefault());    
 			        try {
 			            List<Address> addresses = geoCoder.getFromLocationName(
 			                sitio, 5);
 			            String add = "";
 			            //TODO Allow to select among posibilities
 			            if (addresses.size() > 0) {
 			                p = new GeoPoint(
 			                        (int) (addresses.get(0).getLatitude() * 1E6), 
 			                        (int) (addresses.get(0).getLongitude() * 1E6));
 			                Toast.makeText(SetPlaceMapActivity.this, "Localizacion: " + addresses.get(0).getFeatureName(), Toast.LENGTH_SHORT).show();
 			                map.getOverlays().clear();
 			                map.getOverlays().add(new PuntoMapOverlay());
 			            }    
 			        } catch (IOException e) {
 			            e.printStackTrace();
 			        }					
 				}
 					
 			});
 			
 			Button b_accept = (Button) findViewById(R.id.configure_accept_button);
 			b_accept.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					if (p != null) {
 						OnLocationCondition.PrefsActivity.sp.edit().putInt("longitud", p.getLongitudeE6());
 						OnLocationCondition.PrefsActivity.sp.edit().putInt("latitud", p.getLatitudeE6());
 
 		                Toast.makeText(SetPlaceMapActivity.this, "Actualizar datos!!", Toast.LENGTH_SHORT).show();
 						SetPlaceMapActivity.this.finish();
 
 					} else {
 		                Toast.makeText(SetPlaceMapActivity.this, "Click Somewhere!!", Toast.LENGTH_SHORT).show();
 					}
 				}
 			});
 
 			Button b_cancel = (Button) findViewById(R.id.configure_accept_button);
 			b_cancel.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					SetPlaceMapActivity.this.finish();
 				}
 
 			});
 		}
 
 		@Override
 		protected boolean isRouteDisplayed() {
 			return false;
 		}
 
 	}
 
 }
