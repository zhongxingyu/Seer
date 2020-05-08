 package dlmbg.pckg.wisata.kuliner;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MainActivity extends  Activity {
 	  static final LatLng DENPASAR = new LatLng(-8.658075,115.211563);
 	  private GoogleMap map;
 
 	  @Override
 	  protected void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_main);
 	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
 	        .getMap();
	    Marker tamanmini = map.addMarker(new MarkerOptions()
 	        .position(DENPASAR)
 	        .title("Denpasar")
 	        .snippet("Kota Denpasar - Kota Berwawasan Budaya"));
 
 	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(DENPASAR, 15));
 
 	    map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
 	  }
 
 	  @Override
 	  public boolean onCreateOptionsMenu(Menu menu) {
 	    getMenuInflater().inflate(R.menu.main, menu);
 	    return true;
 	  }
 
 	}
