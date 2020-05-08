 package com.cmput301w13t09.cmput301project.activities;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.cmput301w13t09.cmput301project.R;
 
 public class CreateNewRecipeView extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_new_recipe_view);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_create_new_recipe_view, menu);
 		return true;
 	}
	public boolean onOptionsItemSelected(MenuItem item){
		//TODO Grab all the data and make a recipe
		return true;
	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			switch (position){
 			case 0:
 				Fragment descriptionFragment = new DescriptionSectionFragment();
 				Bundle desriptionArgs = new Bundle();
 				desriptionArgs.putInt(DescriptionSectionFragment.ARG_SECTION_NUMBER, position + 1);
 				descriptionFragment.setArguments(desriptionArgs);
 				return descriptionFragment; 
 			case 1:
 				Fragment ingredientFragment = new IngredientSectionFragment();
 				Bundle ingredientArgs = new Bundle();
 				ingredientArgs.putInt(DescriptionSectionFragment.ARG_SECTION_NUMBER, position + 1);
 				ingredientFragment .setArguments(ingredientArgs);
 				return ingredientFragment ;
 			case 2:
 				Fragment instructionFragment = new IngredientSectionFragment();
 				Bundle instructoinArgs = new Bundle();
 				instructoinArgs.putInt(DescriptionSectionFragment.ARG_SECTION_NUMBER, position + 1);
 				instructionFragment .setArguments(instructoinArgs);
 				return instructionFragment ;
 			}
 			return new Fragment();
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.createNewRecipe_title_section1).toUpperCase();
 			case 1:
 				return getString(R.string.createNewRecipe_title_section2).toUpperCase();
 			case 2:
 				return getString(R.string.createNewRecipe_title_section3).toUpperCase();
 			}
 			return null;
 		}
 	}
 	/**
 	 * A  fragment representing the display section of the create new recipe.
 	 */
 	public static class DescriptionSectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DescriptionSectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			// Create a new TextView and set its text to the fragment's section
 			// number argument value.
 			View tabView = inflater.inflate(R.layout.activity_add_new_recipe_description_tab, container, false);
 			return tabView;
 		}
 	}
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class IngredientSectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 		public Button addIngredientButton;
 		public ListView ingredientList;
 		
 		public IngredientSectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			// Create a new TextView and set its text to the fragment's section
 			// number argument value.
 			View tabView = inflater.inflate(R.layout.activity_my_pantry_view, container, false);
 			addIngredientButton = (Button) tabView.findViewById(R.id.myPantryAddIngredientButton);
 			addIngredientButton.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					try {
 						Intent addIngredient = new Intent("activities.AddIngredient");
 						startActivity(addIngredient);
 					}catch (Throwable e){
 						e.printStackTrace();
 					}
 				}
 			});
 			
 			return tabView;
 		}
 	}
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class InstructionSectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public InstructionSectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			// Create a new TextView and set its text to the fragment's section
 			// number argument value.
 			View tabView = inflater.inflate(R.layout.activity_add_new_recipe_instruction_tab, container, false);
 			return tabView;
 		}
 	}
 
 }
