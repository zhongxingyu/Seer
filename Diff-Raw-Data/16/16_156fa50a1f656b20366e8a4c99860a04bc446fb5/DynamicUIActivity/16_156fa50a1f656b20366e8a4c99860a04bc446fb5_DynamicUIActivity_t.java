 package com.siteduzero.android.dynamicui;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 
 import com.siteduzero.android.R;
 import com.siteduzero.android.dynamicui.CountryListFragment.OnCountrySelectedListener;
 
 public class DynamicUIActivity extends FragmentActivity implements
 		OnCountrySelectedListener {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dynamic_ui);
 
 		if (findViewById(R.id.frameLayoutDynamicUi) != null) {
 			final CountryListFragment listFragment = new CountryListFragment();
 			getSupportFragmentManager().beginTransaction()
					.add(R.id.frameLayoutDynamicUi, listFragment).commit();
 		}
 	}
 
 	@Override
 	public void onCountrySelected(int position) {
 		if (findViewById(R.id.frameLayoutDynamicUi) == null) {
 			// If we are in landscape mode, we show article in the second
 			// fragment.
 			final CountryDetailsFragment detailsFragment = (CountryDetailsFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.fragmentDetails);
 			detailsFragment.updateCountry(position);
 		} else {
 			// Else, we show the other fragment in portrait mode.
 			final CountryDetailsFragment detailsFragment = CountryDetailsFragment
 					.newInstance(position);
 			final FragmentTransaction ft = getSupportFragmentManager()
 					.beginTransaction();
 			ft.replace(R.id.frameLayoutDynamicUi, detailsFragment);
 			ft.addToBackStack(null);
 			ft.commit();
 		}
 	}
 
 }
