 /**
  * CredentialDetailActivity.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, Februari 2013.
  */
 
 package org.irmacard.androidmanagement;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 
 /**
  * An activity representing a single Credential detail screen. This activity is
  * only used on handset devices. On tablet-size devices, item details are
  * presented side-by-side with a list of items in a
  * {@link CredentialListActivity}.
  * <p>
  * This activity is mostly just a 'shell' activity containing nothing more than
  * a {@link CredentialDetailFragment}.
  */
 public class SettingsActivity extends FragmentActivity implements SettingsFragmentActivityI {
 	
 	public static final int RESULT_CHANGE_CARD_PIN = RESULT_FIRST_USER;
 	public static final int RESULT_CHANGE_CRED_PIN = RESULT_FIRST_USER + 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
 
 		// Show the Up button in the action bar.
 		if(getActionBar() != null) {
 			// TODO: workaround for now, figure out what is really going on here.
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 
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
 			SettingsFragment fragment = new SettingsFragment();
 			Bundle arguments = new Bundle();
 			arguments.putSerializable(
 					SettingsFragment.ARG_CARD_VERSION,
 					getIntent().getSerializableExtra(
 							SettingsFragment.ARG_CARD_VERSION));
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.add(R.id.settings_container, fragment).commit();
 		}
 	}
 
 	@Override
 	public void onChangeCardPIN() {
 		setResult(RESULT_CHANGE_CARD_PIN);
 		finish();
 	}
 
 	@Override
 	public void onChangeCredPIN() {
 		setResult(RESULT_CHANGE_CRED_PIN);
 		finish();
 	}
 
 	
 	
 }
