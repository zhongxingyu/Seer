 package com.example.esbee;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * The SoundLibraryActivity holds the constructors for ALL the sounds, button labels, and package 
  * titles. This is where you should declare your set of packages of sounds. Example: If you 
  * wanted to initialize a new drum library, you initialize the package title to "Cool Drum Sounds"
  * the button labels to reflect what each instrument is (ie snare, bass, cowbell), and the
  * actual sounds the title of the mp3 filename which should be located in its own folder under
  * "raw". The files should be in mp3 format. 
  * @author Biru
  *
  */
 
 public class SoundLibraryActivity extends Activity implements View.OnClickListener{
 		
 	PresentPackage drumPkg = new PresentPackage();
 	PresentPackage moviePkg = new PresentPackage();
 	PresentPackage digitalPkg = new PresentPackage();
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sound_library);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		drumPkg.setBtnLbl("hihat", "snare", "Bass 1", "Bass 2");
 		drumPkg.setBtnSound(R.raw.hihat, R.raw.snare, R.raw.bass, R.raw.bass2);
 		drumPkg.setPkgTtl("Drum Set");
 		
 		moviePkg.setBtnLbl("applause", "boo", "rimshot", "wilhelm");
 		moviePkg.setBtnSound(R.raw.applause, R.raw.boo, R.raw.rimshot, R.raw.wilhelm);
 		moviePkg.setPkgTtl("Movies");
 		
 		digitalPkg.setBtnLbl("95", "dialup", "ps1", "mac");
 		digitalPkg.setBtnSound(R.raw.windows, R.raw.dialup, R.raw.ps1, R.raw.mac);
 		digitalPkg.setPkgTtl("Digital");
 		
 		//link present buttons to virtual buttons
 		final Button presentBtn1 = (Button)findViewById(R.id.present1);
 		presentBtn1.setText(drumPkg.getPkgTtl());
 		
 		final Button presentBtn2 = (Button)findViewById(R.id.present2);
 		presentBtn2.setText(moviePkg.getPkgTtl());
 		
 		final Button presentBtn3 = (Button)findViewById(R.id.present3);
 		presentBtn3.setText(digitalPkg.getPkgTtl());
 
 		
 		//onClickListener
 		presentBtn1.setOnClickListener(this);
 		presentBtn2.setOnClickListener(this);
 		presentBtn3.setOnClickListener(this);
 		
 	}
 	
 	public void onClick(View v)
 	{
 		Intent i = new Intent(this, MainActivity.class);
 		
 		switch(v.getId())
 		{
 
 		//If present1 was clicked, MainActivity should reload with new sounds, title, and labels.
			case R.id.present1:
 			i.putExtra("newOnesound", drumPkg.soundOne);
 			i.putExtra("newTwosound", drumPkg.soundTwo);
 			i.putExtra("newThreesound", drumPkg.soundThree);
 			i.putExtra("newFoursound", drumPkg.soundFour);
 			i.putExtra("btnOneLbl", drumPkg.buttonOneLabel);
 			i.putExtra("btnTwoLbl", drumPkg.buttonTwoLabel);
 			i.putExtra("btnThreeLbl", drumPkg.buttonThreeLabel);
 			i.putExtra("btnFourLbl", drumPkg.buttonFourLabel);
 			startActivity(i);
			break;
 
			case R.id.present2:
 			i.putExtra("newOnesound", moviePkg.soundOne);
 			i.putExtra("newTwosound", moviePkg.soundTwo);
 			i.putExtra("newThreesound", moviePkg.soundThree);
 			i.putExtra("newFoursound", moviePkg.soundFour);
 			i.putExtra("btnOneLbl", moviePkg.buttonOneLabel);
 			i.putExtra("btnTwoLbl", moviePkg.buttonTwoLabel);
 			i.putExtra("btnThreeLbl", moviePkg.buttonThreeLabel);
 			i.putExtra("btnFourLbl", moviePkg.buttonFourLabel);
 			startActivity(i);
			break;
 
			case R.id.present3:
 			i.putExtra("newOnesound", digitalPkg.soundOne);
 			i.putExtra("newTwosound", digitalPkg.soundTwo);
 			i.putExtra("newThreesound", digitalPkg.soundThree);
 			i.putExtra("newFoursound", digitalPkg.soundFour);
 			i.putExtra("btnOneLbl", digitalPkg.buttonOneLabel);
 			i.putExtra("btnTwoLbl", digitalPkg.buttonTwoLabel);
 			i.putExtra("btnThreeLbl", digitalPkg.buttonThreeLabel);
 			i.putExtra("btnFourLbl", digitalPkg.buttonFourLabel);
 			startActivity(i);
			break;
 
 			
 		}
 		
 		/**
 		 * When the user clicks the package, it should reload MainActivity buttons 
 		 * with the new sounds, info, and everything. Then it should take the user
 		 * to the MainActivity screen.
 		 */
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.sound_library, menu);
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
 
 }
