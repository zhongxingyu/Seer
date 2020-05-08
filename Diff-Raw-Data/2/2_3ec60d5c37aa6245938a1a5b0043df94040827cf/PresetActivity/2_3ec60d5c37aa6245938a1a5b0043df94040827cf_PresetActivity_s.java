 package com.tonyjhuang.listpop;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class PresetActivity extends FragmentActivity implements
 		AddArrayContainer {
 	private Spinner listType;
 	private Button finish;
 	// Custom Spinner selection listener.
 	COISL coisl = new COISL();
 	FragmentManager fm = getSupportFragmentManager();
 	private static final String TAG = "PresetActivity";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.presets);
 		Log.d(TAG, "PresetActivity started!");
 
 		// Get Spinner handle and attach custom listener.
		listType = (Spinner) findViewById(R.id.spinner);
 		Log.d(TAG, "Spinner reference gotten. Attempting Operation: COISL.");
 		Log.d(TAG, "Before anything... getSupportFragmentManager = "
 				+ PresetActivity.this.getSupportFragmentManager());
 		listType.setOnItemSelectedListener(coisl);
 		Log.d(TAG, "Spinner initialized!");
 		// Get finish button handle and attach OnClickListener.
 		finish = (Button) findViewById(R.id.finish);
 		hookUpFinish();
 		Log.d(TAG, "Finish Button initialized! Setup completed!");
 	}
 
 	// Depending on which Fragment is active, call the appropriate finish
 	// method.
 	private void hookUpFinish() {
 		finish.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Fragment currentFragment;
 
 				switch (listType.getSelectedItemPosition()) {
 				case COISL.NUMBER_RANGE_SPINNER_INDEX:
 					currentFragment = (Fragment) fm
 							.findFragmentById(R.id.fragmentframe);
 
 					checkNumberFinale((NumberRangeFragment) currentFragment);
 					break;
 
 				case COISL.LETTER_RANGE_SPINNER_INDEX:
 					currentFragment = (LetterRangeFragment) fm
 							.findFragmentById(R.id.fragmentframe);
 
 					checkLetterFinale((LetterRangeFragment) currentFragment);
 					break;
 
 				case COISL.EIGHT_BALL_SPINNER_INDEX:
 					currentFragment = (EightBallFragment) fm
 							.findFragmentById(R.id.fragmentframe);
 
 					checkEightBallFinale((EightBallFragment) currentFragment);
 					break;
 				}
 			}
 		});
 	}
 
 	// Check if the range is valid (low < high). If not, Toast. If so,
 	// create intent with extras and finish activity.
 	private void checkNumberFinale(NumberRangeFragment nrf) {
 		int low = nrf.getLowerBound();
 		int high = nrf.getUpperBound();
 
 		if (low != 100 && high != 100) {
 
 			if (low > high)
 				alertToInvalidRange();
 			else {
 				ArrayList<String> a = indexToArray(low, high);
 				Intent i = new Intent();
 				i.putStringArrayListExtra(DbAdapter.LIST, a);
 				setResult(RESULT_OK, i);
 				finish();
 			}
 		}
 	}
 
 	// Create ArrayList of numbers from low to high converted to Strings.
 	private ArrayList<String> indexToArray(int low, int high) {
 		ArrayList<String> a = new ArrayList<String>();
 		for (int i = low; i < (high + 1); i++) {
 			a.add(String.valueOf(i));
 		}
 		return a;
 	}
 
 	// Check if range is valid (low < high). If not, toast. If so,
 	// check title. If it exists, create intent w/extras and finish activity.
 	// If not, create the default title and finish.
 	private void checkLetterFinale(LetterRangeFragment lrf) {
 		String low = lrf.getLowerBoundSpinner().getSelectedItem().toString();
 		String high = lrf.getUpperBoundSpinner().getSelectedItem().toString();
 
 		if (low.compareTo(high) > 0) {
 			alertToInvalidRange();
 		} else {
 			Intent i = new Intent();
 
 			char _low = low.charAt(0);
 			char _high = high.charAt(0);
 			i.putStringArrayListExtra(DbAdapter.LIST,
 					indexCharToArray(_low, _high));
 			setResult(RESULT_OK, i);
 			finish();
 		}
 	}
 
 	// Create ArrayList of letters from low to high through ascii codes.
 	private ArrayList<String> indexCharToArray(char low, char high) {
 		int _low = (int) low;
 		int _high = (int) high;
 
 		ArrayList<String> array = new ArrayList<String>();
 
 		for (int i = _low; i < (_high + 1); i++) {
 			char c = (char) i;
 			String s = Character.toString(c);
 			array.add(s);
 		}
 
 		return array;
 
 	}
 
 	private void checkEightBallFinale(EightBallFragment ebf) {
 		ArrayList<String> list = ebf.getList();
 
 		Intent i = new Intent();
 
 		if (list.size() == 0) {
 			String warning = getResources().getString(R.string.no_items);
 			Toast.makeText(PresetActivity.this, warning, Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			i.putStringArrayListExtra(DbAdapter.LIST, list);
 			setResult(RESULT_OK, i);
 			finish();
 
 		}
 	}
 
 	// Create toast alerting user to an invalid range.
 	private void alertToInvalidRange() {
 		String warning = getResources().getString(R.string.invalid_range);
 		Toast.makeText(PresetActivity.this, warning, Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This is called when the Home (Up) button is pressed
 			// in the Action Bar.
 			Intent i = new Intent(this, StartActivity.class);
 			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
 					| Intent.FLAG_ACTIVITY_NEW_TASK);
 			setResult(RESULT_CANCELED, i);
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	// My implementation of OnItemSelectedListener for the Fragment preset
 	// Spinner.
 	private class COISL implements OnItemSelectedListener {
 		private static final int NUMBER_RANGE_SPINNER_INDEX = 0;
 		private static final int LETTER_RANGE_SPINNER_INDEX = 1;
 		private static final int EIGHT_BALL_SPINNER_INDEX = 2;
 		private static final String TAG = "PresetActivity.COISL";
 
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view,
 				int position, long id) {
 
 			Log.d(TAG, "COISL initialized, getting fragment manager.");
 			FragmentManager fragmentManager = PresetActivity.this
 					.getSupportFragmentManager();
 			FragmentTransaction fragmentTransaction = ((android.support.v4.app.FragmentManager) fragmentManager)
 					.beginTransaction();
 
 			Fragment newFragment = null;
 			Log.d(TAG, "COISL initialized.");
 
 			switch (position) {
 			case NUMBER_RANGE_SPINNER_INDEX:
 				newFragment = new NumberRangeFragment();
 				break;
 			case LETTER_RANGE_SPINNER_INDEX:
 				newFragment = new LetterRangeFragment();
 				break;
 			case EIGHT_BALL_SPINNER_INDEX:
 				newFragment = new EightBallFragment();
 			}
 			fragmentTransaction.replace(R.id.fragmentframe, newFragment);
 			fragmentTransaction.commit();
 
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> parent) {
 
 		}
 
 	}
 
 	@Override
 	public void deleteFromAdapter(int position) {
 		EightBallFragment currentFragment = (EightBallFragment) fm
 				.findFragmentById(R.id.fragmentframe);
 		currentFragment.deleteFromActivity(position);
 
 	}
 }
