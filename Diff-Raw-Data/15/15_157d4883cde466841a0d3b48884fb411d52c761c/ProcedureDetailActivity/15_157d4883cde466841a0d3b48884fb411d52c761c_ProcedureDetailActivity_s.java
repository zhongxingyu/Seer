 package org.epstudios.epcoding;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /**
  * An activity representing a single Procedure detail screen. This activity is
  * only used on handset devices. On tablet-size devices, item details are
  * presented side-by-side with a list of items in a
  * {@link ProcedureListActivity}.
  * <p>
  * This activity is mostly just a 'shell' activity containing nothing more than
  * a {@link ProcedureDetailFragment}.
  */
 public class ProcedureDetailActivity extends FragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_procedure_detail);
 
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		// savedInstanceState is non-null when there is fragment state
 		// saved from previous configurations of this activity
 		// (e.g. when rotating the screen from portrait to landscape).
 		// In this case, the fragment will automatically be re-added
 		// to its container so we don't need to manually add it.
 		// For more information, see the Fragments API guide at:
 		//
 		// http://developer.android.com/guide/components/fragments.html
 		//
 		if (savedInstanceState == null) {
 			// Create the detail fragment and add it to the activity
 			// using a fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putString(
 					ProcedureDetailFragment.ARG_ITEM_ID,
 					getIntent().getStringExtra(
 							ProcedureDetailFragment.ARG_ITEM_ID));
 			ProcedureDetailFragment fragment = new ProcedureDetailFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.add(R.id.procedure_detail_container, fragment).commit();
 		}
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
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 }
