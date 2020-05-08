 package uk.ac.cam.groupproj.racethewild;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class CheckInScene extends Activity {
 	
 	
 	Engine e = Engine.get();
 	// Displayed when the user cashes in their movement.
 	// Displays the number of movement points awarded, the released animal's graphic, some exciting
 	// text, and a hint for finding it.
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_check_in_scene);
 		// Show the Up button in the action bar.
 		List<Animal> animals = e.getAllAnimals();
 		int distance = e.fetchSatNavData().getDistance();
 		int closest = Integer.MAX_VALUE;
 		Animal chosen = null;
 		for(Animal a:animals){
 			if((a.getDistancePerDay() < closest) && a.getColour()==Colour.White){
 				closest = a.getDistancePerDay();
 				chosen = a;
 			}
 		}
 		e.checkIn(chosen);
 		Bitmap background;
 		
		TextView textView = (TextView) findViewById(R.id.animalName);
		textView.setText(chosen.getName());
		
		TextView textView1 = (TextView) findViewById(R.id.animalFacts);
		textView1.setText(chosen.getHint());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_check_in_scene, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 
 	//public void render(); // Draw the scene to the screen.
 	//public void update(); // Updates the scene on the screen.
 	//public void close(); // Tell the scene to cleanup.
 }
