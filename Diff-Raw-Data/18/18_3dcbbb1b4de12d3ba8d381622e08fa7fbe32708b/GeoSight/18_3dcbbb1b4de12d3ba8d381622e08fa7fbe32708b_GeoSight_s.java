 package edu.illinois.geosight;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import edu.illinois.geosight.servercom.Sight;
 
 public class GeoSight extends Activity implements OnClickListener {
 	
 	private Button cameraPreview;
 	private Button mapButton;
 	private Button navButton;
 	private Button sightsButton;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         cameraPreview = (Button) findViewById(R.id.CameraPreview);
         cameraPreview.setOnClickListener(this);
         
         mapButton = (Button) findViewById(R.id.MapButton);
         mapButton.setOnClickListener(this);
         
         navButton = (Button) findViewById(R.id.NavButton);
         navButton.setOnClickListener(this);
         
         sightsButton = (Button) findViewById(R.id.SightsButton);
         sightsButton.setOnClickListener(this);
     }
     
 	@Override
 	public void onClick(View v) {
 		Intent intent = null;
 		
 		if( v.getId() == R.id.CameraPreview){
 			intent = new Intent(this, CameraActivity.class);
 		} else if( v.getId() == R.id.MapButton ){
 			intent = new Intent(GeoSight.this, GoogleMapActivity.class);
 		} else if( v.getId() == R.id.SightsButton ){
 			intent = new Intent(GeoSight.this, SightListActivity.class);
 		} else if( v.getId() == R.id.NavButton ){
 			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=40.113849,-88.224282") );
 		}
 		
 		startActivity(intent);
 	}
 }
