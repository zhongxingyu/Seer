 /**
  *  Copyright 2011-2013 Tony Murray <murraytony@gmail.com>
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.splashmobileproductions.scorekeep;
 
 
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class HomeFragment extends Fragment {
     private static final String DEBUG_TAG = "ScoreKeep:HomeFragment";
 	private GameHistory historyFragment;
 	private SettingsFragment settingsFragment;
 	private PlayerList playerListFragment;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.dashboard, container, false);
 
 		Context context = getActivity().getApplicationContext();
 
 		// Attach event handlers
 		final Intent newGameIntent = new Intent(context, NewGameFragment.class);
 		view.findViewById(R.id.home_btn_new).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) { startActivity(newGameIntent); }
 		});
 
 		view.findViewById(R.id.home_btn_history).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				if (historyFragment == null) historyFragment = new GameHistory();
 				changeFragments(historyFragment);
 			}
 		});
 
 		view.findViewById(R.id.home_btn_players).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				if (playerListFragment == null) playerListFragment = new PlayerList();
 				changeFragments(playerListFragment);
 			}	});
 
 		view.findViewById(R.id.home_btn_settings).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				if (settingsFragment == null) settingsFragment = new SettingsFragment();
 				changeFragments(settingsFragment);
 			}
 		});
 
 		return view;
 	}
 
 	private void changeFragments(Fragment target) {
 		FragmentManager fragmentManager = getActivity().getFragmentManager();
 		FragmentTransaction ft = fragmentManager.beginTransaction();
 		ft.replace(R.id.home_fragment_frame, target);
 		ft.addToBackStack(null);
 		ft.commit();
 
 		// clear the logo image
 		View logo = getActivity().findViewById(R.id.app_logo_layout);
 		if(logo !=null) logo.setVisibility(View.GONE);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         //Log.d(DEBUG_TAG, "Creating menu");
         menu.clear();
         inflater.inflate(R.menu.home_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {		
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.home_menu_new:
 			startActivity(new Intent(getActivity().getApplicationContext(), NewGameFragment.class));
 			return true;
 		case R.id.home_menu_about:
 			//show about dialog
 			try {
				AboutDialogBuilder.create(getActivity().getApplicationContext()).show();
 			} catch (NameNotFoundException nnfe) {}
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	public static class AboutDialogBuilder {
 		public static AlertDialog create( Context context ) throws NameNotFoundException {
 			// Try to load the a package matching the name of our own package
 			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
 			String versionInfo = pInfo.versionName;
 
 			String aboutTitle = String.format("About %s", context.getString(R.string.app_name));
 			String versionString = String.format("Version: %s", versionInfo);
 			String aboutText = context.getString(R.string.about_dialog_text);
 
 			// Set up the TextView
 			final TextView message = new TextView(context);
 			// We'll use a spannable string to be able to make links clickable
 			final SpannableString s = new SpannableString(aboutText);
 
 			// Set some padding
 			message.setPadding(5, 5, 5, 5);
 			// Set up the final string
 			message.setText(versionString + "\n\n" + s);
 			// Now linkify the text
 			Linkify.addLinks(message, Linkify.ALL);
 
 			return new AlertDialog.Builder(context).setTitle(aboutTitle).setCancelable(true).setIcon(R.drawable.icon).setPositiveButton(
 					context.getString(android.R.string.ok), null).setView(message).create();
 		}
 	}
 
 }
