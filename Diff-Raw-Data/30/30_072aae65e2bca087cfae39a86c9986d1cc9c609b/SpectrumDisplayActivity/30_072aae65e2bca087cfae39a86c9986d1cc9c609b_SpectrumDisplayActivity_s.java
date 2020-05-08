 package edu.tamu.csce470.mir;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class SpectrumDisplayActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_spectrum_display);
 		
 		SpectrumView spectrumView = (SpectrumView) findViewById(R.id.spectrumView);
 		
 		spectrumView.setSpectrum((Spectrum) getIntent().getExtras().getParcelable("spectrum"));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.spectrum_display, menu);
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
