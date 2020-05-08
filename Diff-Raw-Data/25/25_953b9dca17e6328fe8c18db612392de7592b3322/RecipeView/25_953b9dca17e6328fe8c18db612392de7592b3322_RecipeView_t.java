 package com.cmput301w13t09.cmput301project.activities;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.cmput301w13t09.cmput301project.EmailBuilder;
 import com.cmput301w13t09.cmput301project.R;
 import com.cmput301w13t09.cmput301project.RecipeController;
 import com.cmput301w13t09.cmput301project.RecipeViewAssistant;
 import com.cmput301w13t09.cmput301project.UploadController;
 
 /**
  * @author Kyle, Marcus, and Landre Class:
  */
 public class RecipeView extends FragmentActivity implements
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
 	private int recipePosition;
 	private RecipeController rController;
 	private RecipeViewAssistant rAssitant;
 	private boolean fileWipe = false;
 	private String fileWipeName;
 	private UploadController webController;
 
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
 		rAssitant = new RecipeViewAssistant(this);
 		rController = new RecipeController(this);
 		Bundle bundle = getIntent().getExtras();
 		recipePosition = bundle.getInt("RECIPE_POSITION");
 		rAssitant.saveNewToFile();
 		rAssitant.setRecipe(rController.getRecipe(recipePosition));
 		rAssitant.saveToFile();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_recipe_view_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.ViewRecipeViewEdit:
 			try {
 				Intent editRecipe = new Intent("activities.EditRecipe");
 				editRecipe.putExtra("RECIPE_POSITION", recipePosition);
 				startActivity(editRecipe);
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 			return super.onOptionsItemSelected(item);
 		case R.id.ViewRecipeViewDelete:
 			rController.remove(recipePosition);
 			rController.saveToFile();
 			finish();
 			return super.onOptionsItemSelected(item);
 		case R.id.ViewRecipeViewEmail:
 
 			String shareURI = rAssitant.saveToShareFile();
 
 			Intent email = new Intent(Intent.ACTION_SEND);
 			email.setType("message/rfc822");
 			email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name)
 					+ " Recipe: " + rAssitant.getName());
 			email.putExtra(Intent.EXTRA_STREAM,
 					Uri.fromFile(new File(shareURI)));
 			email.putExtra(Intent.EXTRA_TEXT,
 					new EmailBuilder(rAssitant.getRecipe()).getMessage());
 			try {
 				Toast.makeText(this, "Openning email application...",
 						Toast.LENGTH_SHORT).show();
 				startActivity(Intent.createChooser(email, "Send mail..."));
 				fileWipeName = (shareURI);
 				fileWipe = true;
 			} catch (android.content.ActivityNotFoundException ex) {
 				new File(shareURI).delete();
 				Toast.makeText(this, "There are no email clients installed.",
 						Toast.LENGTH_SHORT).show();
 			} catch (NullPointerException nPE) {
 				nPE.printStackTrace();
 			}
 			return super.onOptionsItemSelected(item);
 		case R.id.ViewRecipeViewUpload:
 			if (android.os.Build.VERSION.SDK_INT > 9) {
 				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 						.permitAll().build();
 				StrictMode.setThreadPolicy(policy);
 			}
 			if (this.isNetworkAvailable()) {
 				try {
 					webController = new UploadController();
 					webController.insertRecipe(rAssitant.getRecipe());
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
 
 	protected void onResume() {
 		rController.loadFromFile();
 		rAssitant.setRecipe(rController.getRecipe(recipePosition));
 		rAssitant.saveToFile();
 		if (fileWipe) {
 			fileWipe = false;
 			new File(fileWipeName).delete();
 		}
 		super.onResume();
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
 				Fragment descriptionFragment = new RecipeViewDescriptionSectionFragment();
 				return descriptionFragment;
 			case 1:
 				Fragment ingredientFragment = new RecipeViewIngredientSectionFragment();
 				return ingredientFragment;
 			case 2:
 				Fragment instructionFragment = new RecipeViewInstructionSectionFragment();
 				return instructionFragment;
 			case 3:
 				Fragment photoFragment = new RecipeViewPictureSectionFragment();
 				return photoFragment;
 			}
 			return new Fragment();
 		}
 
 		@Override
 		public int getCount() {
 			// Show 4 total pages.
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
 				return "Pictures";
 			}
 			return null;
 		}
 
 	}
 }
