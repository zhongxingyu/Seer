 package com.ruoyiwang.chi;
 
 import java.util.ArrayList;
 import com.ruoyiwang.chi.R;
import com.ruoyiwang.chi.model.ChiRegion;
import com.ruoyiwang.chi.model.ChiRestaurant;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private ChiRegion crUwPlaza;
 	//generates the list of restaurants in university plaza beside uWaterloo
 	private ArrayList<ChiRestaurant> getListOfPlacesToEat() {
 		ArrayList<ChiRestaurant> alListOfPlacesToEat = new ArrayList<ChiRestaurant>(1);
 		alListOfPlacesToEat.add(new ChiRestaurant("Sweet Dreams Tea Shop",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Curry in a Hurry",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Harveys",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Shandiz Persian Cuisine",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("East Side Mario's",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("McGinnis FrontRow Restaurant",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("The Grill",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Vegitarian Fastfood Restaurant",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Kismet Retaurant",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Home Garden ̨С",""));
 		alListOfPlacesToEat.add(new ChiRestaurant(" China Legend",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Mickey's Eatery",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Subway",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Da Won",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Campus Pizza",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Mr. Panino's Beijing House С",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Chen's Restaurant Ӳ",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Seoul Soul",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Phat Cat",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("William's Coffee Pub",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Pita Factory",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Panda King",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Grab-a-Greek",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Waterloo Star ͬ԰",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Bubble Tease",""));
 		alListOfPlacesToEat.add(new ChiRestaurant("Sogoʳ",""));
 		return alListOfPlacesToEat;
 	}
 	private void loadUWRegion(){
 		ChiRegion crUwPlaza = new ChiRegion("University Plaza", "plaza", getListOfPlacesToEat());
 		this.crUwPlaza = crUwPlaza;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		this.loadUWRegion();
 		String message = "Click the button below to get a random restaurant!";
 		// Create the text view
 		TextView textView = (TextView) findViewById(R.id.tvMainOutput);
 		textView.setText(message);
 	}
 
 	public void getNewRestaurant(View view) {
 		ChiRestaurant crRandomRestaurant = this.crUwPlaza.getRandomRestaurant();
 
 		TextView textView = (TextView) findViewById(R.id.tvMainOutput);
 		textView.setText(crRandomRestaurant.name());
 	}
 }
