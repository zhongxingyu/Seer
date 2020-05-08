 package com.teatime.game.activity;
 
 import android.os.Bundle;
import android.view.View;
 import android.widget.LinearLayout;
import android.widget.TextView;
 
 import com.teatime.game.R;
 import com.teatime.game.model.World;
 import com.teatime.game.view.MapView;
 
 public class MapActivity extends BaseActivity {
 	
 	private World world;
 	private MapView mapView;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_map);
            
         // Retrieve linearLayout from layout_map
         LinearLayout rel = (LinearLayout) findViewById(R.id.map);
         
         world = World.createWorld(4);
         
         // Create map
         mapView = new MapView(50, 50, world, this, getResources());
         
         
         // Add mapview to linearlayout 
         rel.addView(mapView);
         
         // Draw the map
         mapView.draw();    
 	}
 }
