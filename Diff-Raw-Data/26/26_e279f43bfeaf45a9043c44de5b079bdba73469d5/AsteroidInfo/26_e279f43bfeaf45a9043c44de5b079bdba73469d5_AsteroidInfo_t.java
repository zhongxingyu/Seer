 package ie.ucd.asteroid;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 //detailed information on the Asteroid
 public class AsteroidInfo extends Activity {
 	
 	DBAdapter db = new DBAdapter(this);
 	private static final String ASTEROIDINFO_TAG = "Asteroid_Info"; //for debugging
 	int id;
 	TextView title;
 	Double approxDiameter;
 	Calc calc = new Calc();
 	Context con = AsteroidInfo.this;
 	ImageView asteroid;
 	TextView diam;
 	String minMax;
 	TextView numOfBuses;
 	Double buses;
 	TextView approachDateTime;
 	TextView distance; 
 	TextView tvVelocity;
 	
 	//array for looping textviews
 	int [] tvNames = {
 			R.id.size,
 			R.id.ca,
 			R.id.vel_title,
 			R.id.cad,
 			R.id.num_of_buses,
 			R.id.close_approach_date_time,
 			R.id.distance,
 			R.id.velocity
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_asteroid_info);
         
         //get info sent from weeklyasteroids.java
         id = getIntent().getExtras().getInt("pos");
         
         //get relevant data from database for the chosen asteroid
         db.openDB();
         String name = db.getName(id);
 		String date = db.getApproachDateDisplay(id);
 		String time = db.getTime(id);
 		Double maxDiameter = db.getMaxDiameter(id);
 		Double minDiameter = db.getMinDiameter(id); 
 		Double dist = db.getApproachDistance(id);
 		Double velocity = db.getRelativeVelocity(id);
         db.closeDB();
         
         //find the approximate diameter
         approxDiameter = (minDiameter + maxDiameter) / 2;
         
         //set title text
         title = (TextView) findViewById(R.id.ast_title);
         title.setText(name);
         
         //set which image to appear
         asteroid = (ImageView) findViewById(R.id.ast_image);
         asteroid.setImageResource(calc.asteroidSizeDecider(approxDiameter, true));
         
         //showing diameter min - max
        if ( minDiameter.doubleValue() == maxDiameter.doubleValue() ) {
 			// If max and min diameter are the same, only print out the one diameter
 			minMax = getText(R.string.diameter) + " " + String.valueOf(minDiameter) + "m";
 		} else {
 			minMax = getText(R.string.diameter) + " " + String.valueOf(minDiameter) + "m - " + String.valueOf(maxDiameter) + "m";
 		}
         
         diam = (TextView) findViewById(R.id.min_max);
         diam.setText(minMax);
         Log.w(ASTEROIDINFO_TAG, "minMax is: " + minMax);
         
         //show number of buses
         buses = calc.CompareToBus(approxDiameter);
         int rnd_buses = (int) Math.round(buses);
 		LinearLayout info_layout = (LinearLayout) findViewById(R.id.asteroid_info_buses);
 		Context c = AsteroidInfo.this;
 		calc.MakeBuses(rnd_buses, info_layout, c);
 		
         //cut to whole num for display (not rounded - just formatted)
 		numOfBuses = (TextView) findViewById(R.id.num_of_buses);
         numOfBuses.setText("x" + String.format("%.0f", buses)); 
         
         //show closest approach date/time
         approachDateTime = (TextView) findViewById(R.id.close_approach_date_time);
         approachDateTime.setText(time + ", " + date);
         
         //show closest approach distance
         distance = (TextView) findViewById(R.id.distance);
         distance.setText(String.valueOf(dist) + " AU");
         
         //show velocity
         tvVelocity = (TextView) findViewById(R.id.velocity);
         tvVelocity.setText(String.valueOf(velocity) + " m/s");
         
         //setting fonts for display
         TextView titleTv= (TextView) findViewById(R.id.ast_title);
 		Typeface titleFace=Typeface.createFromAsset(getAssets(),"fonts/Roboto-CondensedItalic.ttf");
 		titleTv.setTypeface(titleFace);
 		TextView diamTv= (TextView) findViewById(R.id.min_max);
 		Typeface diamFace=Typeface.createFromAsset(getAssets(),"fonts/Roboto-BoldCondensed.ttf");
 		diamTv.setTypeface(diamFace);
 		
 		//set typeface for textviews which will use the same one. 
 		for (int i = 0; i < tvNames.length; i++) {
 
 			TextView text;
 			Typeface face;
 
 			text = (TextView) findViewById(tvNames[i]);
 			face = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Condensed.ttf");
 			text.setTypeface(face);
 
 		}
 		
		Button key_leg = (Button)findViewById(R.id.info_key_btn);
 		
 		key_leg.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				calc.key(con);
 			}
 	
 		});
 		
 		
     }
     //back button
     public void close(View v){
 		finish();
 	}
 }
