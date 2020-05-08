 package org.epstudios.epcoding;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 /**
  * An activity representing a list of Procedures. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link ProcedureDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link ProcedureListFragment} and the item details (if present) is a
  * {@link ProcedureDetailFragment}.
  * <p>
  * This activity also implements the required
  * {@link ProcedureListFragment.Callbacks} interface to listen for item
  * selections.
  */
 public class ProcedureListActivity extends FragmentActivity implements
 		ProcedureListFragment.Callbacks {
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean mTwoPane;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_procedure_list);
 
 		if (findViewById(R.id.procedure_detail_container) != null) {
 			// The detail container view will be present only in the
 			// large-screen layouts (res/values-large and
 			// res/values-sw600dp). If this view is present, then the
 			// activity should be in two-pane mode.
 			mTwoPane = true;
 
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			((ProcedureListFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.procedure_list))
 					.setActivateOnItemClick(true);
 		}
 
 		// TODO: If exposing deep links into your app, handle intents here.
 	}
 
 	/**
 	 * Callback method from {@link ProcedureListFragment.Callbacks} indicating
 	 * that the item with the given ID was selected.
 	 */
 	@Override
 	public void onItemSelected(String id) {
 		if (mTwoPane) {
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putString(ProcedureDetailFragment.ARG_ITEM_ID, id);
 			ProcedureDetailFragment fragment = new ProcedureDetailFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.procedure_detail_container, fragment)
 					.commit();
 
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Intent detailIntent = new Intent(this,
 					ProcedureDetailActivity.class);
 			detailIntent.putExtra(ProcedureDetailFragment.ARG_ITEM_ID, id);
 			startActivity(detailIntent);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		ProcedureDetailFragment fragment = (ProcedureDetailFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.procedure_detail_container);
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpTo(this, new Intent(this,
 					ProcedureListActivity.class));
 			return true;
 		case R.id.about:
 			startActivity(new Intent(this, About.class));
 			return true;
 		case R.id.editCodeList:
 			if (fragment != null && fragment instanceof ProcedureDetailFragment)
				fragment.saveCoding();
 			return true;
 		case R.id.saveCodeSelection:
 			if (fragment != null && fragment instanceof ProcedureDetailFragment)
				; // fragment.saveCodeSelection();
 			return true;
 		case R.id.settings:
 			if (fragment != null && fragment instanceof ProcedureDetailFragment)
 				fragment.saveSettings();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void saveSettings() {
 		Toast toast = Toast.makeText(this, "Saving settings",
 				Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 }
