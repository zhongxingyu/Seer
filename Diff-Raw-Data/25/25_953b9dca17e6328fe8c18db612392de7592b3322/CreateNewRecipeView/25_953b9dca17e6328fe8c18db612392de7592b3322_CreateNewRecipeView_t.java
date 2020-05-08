 package com.cmput301w13t09.cmput301project.activities;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.cmput301w13t09.cmput301project.PhotoModel;
 import com.cmput301w13t09.cmput301project.R;
 import com.cmput301w13t09.cmput301project.RecipeController;
 import com.cmput301w13t09.cmput301project.RecipeViewAssistant;
 
 /**
  * @author Kyle, Marcus, and Landre Class: CreateNewRecipeView CreateNewRecipe
  *         is that extends FragmentActivity and acts a way to gather input data
  *         for Recipes. CreateNewRecipe provides a top menu used for inputing
  *         different types of data. CreateNewRecipe will then use
  *         RecipeController in order to add the recipe to a recipelist and save
  *         that to recipe.data.
  */
 public class CreateNewRecipeView extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	private static final int CAPTURE_IMAGE = 10;
 	private static final int PICK_IMAGE = 1;
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
 	private RecipeViewAssistant rBuilder;
 	private RecipeController rController;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.recipe_view);
 
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
 		rBuilder = new RecipeViewAssistant(this);
 		rBuilder.saveNewToFile();
 		rController = new RecipeController(this);
 		
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		rBuilder.loadFromFile();
 		if (resultCode == RESULT_OK) {
 
 			if (requestCode == CAPTURE_IMAGE) {
 				Bitmap photo;
 
 				photo = (Bitmap) data.getExtras().get("data");
 				rBuilder.addPhoto(new PhotoModel( Bitmap.createScaledBitmap(photo, 400, 400, true)));
 				rBuilder.updateRecipe();
 				rBuilder.saveToFile();
 				super.onResumeFragments();
 			}
 
 			if (requestCode == PICK_IMAGE) {
 
 				Bitmap photo;
 
 				photo = (Bitmap) data.getExtras().get("data");
 				rBuilder.addPhoto(new PhotoModel(Bitmap.createScaledBitmap(photo, 400, 400, true)));
 				rBuilder.updateRecipe();
 				rBuilder.saveToFile();
 				super.onResumeFragments();
 			}
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_new_recipe_view_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.addNewRecipeDoneButton:
 			rBuilder.loadFromFile();
 			rController.addRecipe(rBuilder.getRecipe());
 			rController.saveToFile();
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
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
 			switch (position) {
 			case 0:
 				Fragment descriptionFragment = new ModifiableRecipeViewDescriptionSectionFragment();
 				return descriptionFragment;
 			case 1:
 				Fragment ingredientFragment = new ModifiableRecipeViewIngredientSectionFragment();
 				return ingredientFragment;
 			case 2:
 				Fragment instructionFragment = new ModifiableRecipeViewInstructionSectionFragment();
 				return instructionFragment;
 
 			case 3:
 				Fragment photoFragment = new ModifiableRecipeViewPictureSectionFragment();
 				return photoFragment;
 			}
 			return new Fragment();
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 4;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.createNewRecipe_title_section1);
 			case 1:
 				return getString(R.string.createNewRecipe_title_section2);
 			case 2:
 				return getString(R.string.createNewRecipe_title_section3);
 			case 3:
 				return ("Pictures");
 			}
 			return null;
 		}
 	}
 }
