 package net.trajano.gasprices;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 /**
  * This activity is shown to display a list of valid cities and allow the user
  * to select one.
  * 
  * @author Archimedes Trajano (developer@trajano.net)
  * 
  */
 public class CitySelectionActivity extends ListActivity {
 	/**
 	 * Preference data, stored in memory until destruction.
 	 */
 	private PreferenceAdaptor preferences;
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(new CityListAdapter(this));
 	}
 
 	/**
 	 * When the item is selected, it updates the selected city preference rather
 	 * than sending the data back. It is this activity that is setting what it
 	 * wants
 	 */
 	@Override
 	protected void onListItemClick(final ListView l, final View v,
 			final int position, final long id) {
 		final PreferenceAdaptorEditor editor = preferences.edit();
 		editor.setSelectedCityId(id);
 		editor.apply();
 		final Intent intent = new Intent();
 		setResult(RESULT_OK, intent);
 		finish();
 	}
 }
