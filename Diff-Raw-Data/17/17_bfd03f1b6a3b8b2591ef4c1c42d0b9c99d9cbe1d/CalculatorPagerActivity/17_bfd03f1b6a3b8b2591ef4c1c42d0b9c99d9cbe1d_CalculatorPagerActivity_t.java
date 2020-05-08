 package com.ACM.binarycalculator;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.support.v4.view.ViewPager.PageTransformer;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 /**
  * 
  * @author James Van Gaasbeck, ACM at UCF <jjvg@knights.ucf.edu>
  * 
  * 
  */
 public class CalculatorPagerActivity extends SherlockFragmentActivity implements
 		FragmentDataPasser {
 	private static final String TAG = "CalculatorPagerActivity";
 
 	private ViewPager mViewPager;
 	private static final int NUMBER_OF_VIEWS = 4;
 	// constants used for the screen animations
 	private static float MIN_SCALE = 0.85f;
 
 	// array of the names of the view, putting them in an array makes it easier
 	// when setting, if adding/switching views around then we need to update
 	// this array.
 	private final String[] viewNames = { "Binary", "Hex", "Decimal", "Octal" };
 
 	// there is code in onCreate() that cannot be used on a device running
 	// something before API 11 (HONEYCOMB)
 	@TargetApi(11)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// to get rid of the title bar
 		// requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// we need to get a reference to our support action bar
 		final com.actionbarsherlock.app.ActionBar supportActionBar = getSupportActionBar();
 		super.onCreate(savedInstanceState);
 
 		// set the content view to our blank ViewPager layout
 		setContentView(R.layout.activity_main);
 
 		// set the ID of the viewPager because it needs a reference ID
 		mViewPager = (ViewPager) findViewById(R.id.viewPager);
 
 		// code to have a depth affect when swiping pages, kind of gives a
 		// stacking effect, like sifting thru a stack of paper
 		//
 		// we will only run this page animation code on devices running
 		// something above API 11 (HONEYCOMB)
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			mViewPager.setPageTransformer(true, new PageTransformer() {
 
 				@Override
 				public void transformPage(View view, float position) {
 					int pageWidth = view.getWidth();
 
 					if (position < -1) {
 						// if page is way off screen to the left set it's alpha
 						// to zero
 						view.setAlpha(0);
 
 					} else if (position <= 0) {
 						// uses the default page transition when sliding the
 						// page to the
 						// left
 						view.setAlpha(1);
 						view.setTranslationX(0);
 						view.setScaleX(1);
 						view.setScaleY(1);
 
 					} else if (position <= 1) {
 						// fade the view out
 						view.setAlpha(1 - position);
 						// when sliding the page to the right we need to
 						// counteract the
 						// default page transition to make it so there is a
 						// depth effect
 						view.setTranslationX(pageWidth * -position);
 
 						// scale the page down
 						float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
 								* (1 - Math.abs(position));
 						view.setScaleX(scaleFactor);
 						view.setScaleY(scaleFactor);
 
 					} else {
 						// view is way off screen
 						view.setAlpha(0);
 					}
 				}
 			});
		} else {
			// sets the margin to be a little wider so there is a
			// distinction between each individual view when page turning
			mViewPager.setPageMargin(30);
 		}
 
 		// transition animation makes it useless.
 		mViewPager.setBackgroundColor(getApplication().getResources().getColor(
 				R.color.Black));
 		// actionBar.setBackgroundDrawable(new
 		// ColorDrawable(Color.parseColor("#EAEAAE")));
 
 		// we need to set the number of pages that stay in memory so that all
 		// the pages/fragments are updated rather than just the adjacent pages.
 		mViewPager.setOffscreenPageLimit(NUMBER_OF_VIEWS - 1);
 
 		// get the supported fragment manager
 		FragmentManager fm = getSupportFragmentManager();
 		// set up the viewPager adapter, this is the dataSource/list of the
 		// views that will be inflated when turning the page.
 		mViewPager.setAdapter(new FragmentPagerAdapter(fm) {
 
 			// these are the only two methods that are necessary to have a
 			// working ViewPager.
 			@Override
 			public int getCount() {
 				// return the number of views we have put in our list. If
 				// adding/removing views need to update the integer-constant at
 				// the top of this activity
 				return NUMBER_OF_VIEWS;
 			}
 
 			@Override
			public SherlockFragment getItem(int position) {
 				switch (position) {
 				case 0:
 					Log.d(TAG, "---In getPosition(), position 0---");
 					// if the position is 0 return the binary view
 
 					// the newInstance() method is a work around to let up pass
 					// arguments into each fragment. It is pretty much a
 					// homemade constructor that calls the fragments constructor
 					// and allows up to pass in data to the fragments.
 					CalculatorBinaryFragment binFrag = (CalculatorBinaryFragment) CalculatorBinaryFragment
 							.newInstance();
 					return binFrag;
 				case 1:
 					Log.d(TAG, "---In getPosition(), posistion 1---");
 
 					return CalculatorHexFragment.newInstance();
 
 				case 2:
 					Log.d(TAG, "---In getPosition(), position 2---");
 					return CalculatorDecimalFragment.newInstance();
 
 				case 3:
 					Log.d(TAG, "---In getPosition(), position 3---");
 
 					return CalculatorOctalFragment.newInstance();
 
 				default:
 					Log.d(TAG, "---In getPosition(), DEFAULT---");
 
 					return CalculatorBinaryFragment.newInstance();
 				}
 
 			}
 		});
 
 		//
 		// this is a callback for the ViewPager
 		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
 			/*
 			 * If changing the views ordering remember to update the Toasts.
 			 */
 			@Override
 			public void onPageSelected(int position) {
 
 				// set the action bars tab to the correct page when the user
 				// swipes the tab
 				getSupportActionBar().setSelectedNavigationItem(position);
 
 				// All this code does is present a Toast when switching pages
 				switch (position) {
 				case 0:
 
 					// makes a Toast and shows it, but for only three-quarters
 					// of
 					// a second because the standard Toast.LENGTH_SHORT is too
 					// long (2seconds)
 					final Toast toastBin = Toast.makeText(
 							getApplicationContext(), "Binary",
 							Toast.LENGTH_SHORT);
 					toastBin.show();
 
 					Handler handBin = new Handler();
 					handBin.postDelayed(new Runnable() {
 
 						@Override
 						public void run() {
 							toastBin.cancel();
 
 						}
 					}, 750); // show for only 750milliseconds (3/4ths a second)
 					break;
 
 				case 1:
 
 					final Toast toastDec = Toast.makeText(
 							getApplicationContext(), "Hexadecimal",
 							Toast.LENGTH_SHORT);
 					toastDec.show();
 
 					Handler handDec = new Handler();
 					handDec.postDelayed(new Runnable() {
 
 						@Override
 						public void run() {
 							toastDec.cancel();
 
 						}
 					}, 750);
 					break;
 
 				case 2:
 
 					final Toast toastOct = Toast.makeText(
 							getApplicationContext(), "Decimal",
 							Toast.LENGTH_SHORT);
 					toastOct.show();
 
 					Handler handOct = new Handler();
 					handOct.postDelayed(new Runnable() {
 
 						@Override
 						public void run() {
 							toastOct.cancel();
 
 						}
 					}, 750);
 					break;
 
 				case 3:
 
 					final Toast toastHex = Toast.makeText(
 							getApplicationContext(), "Octal",
 							Toast.LENGTH_SHORT);
 					toastHex.show();
 
 					Handler handHex = new Handler();
 					handHex.postDelayed(new Runnable() {
 
 						@Override
 						public void run() {
 							toastHex.cancel();
 
 						}
 					}, 750);
 					break;
 
 				default:
 					break;
 				}
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// nothing
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				// nothing
 			}
 		});
 
 		/*
 		 * Tabbed action bar sherlock code
 		 */
 		// set that the tabs will be in the action bar
 		supportActionBar
 				.setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_TABS);
 
 		// we need a Tab listener
 		com.actionbarsherlock.app.ActionBar.TabListener tabListener = new com.actionbarsherlock.app.ActionBar.TabListener() {
 
 			@Override
 			public void onTabSelected(
 					com.actionbarsherlock.app.ActionBar.Tab tab,
 					android.support.v4.app.FragmentTransaction ft) {
 				// set the viewPager to the correct view
 				mViewPager.setCurrentItem(tab.getPosition());
 			}
 
 			@Override
 			public void onTabUnselected(
 					com.actionbarsherlock.app.ActionBar.Tab tab,
 					android.support.v4.app.FragmentTransaction ft) {
 				// Could probably do nothing here cause the logic is handled
 				// when a tab is selected.
 			}
 
 			@Override
 			public void onTabReselected(
 					com.actionbarsherlock.app.ActionBar.Tab tab,
 					android.support.v4.app.FragmentTransaction ft) {
 				// Do nothing
 			}
 		};
 
 		// add/set the text of the tabs
 		for (int i = 0; i < NUMBER_OF_VIEWS; i++) {
 			supportActionBar.addTab(supportActionBar.newTab()
 					.setText(viewNames[i]).setTabListener(tabListener));
 		}
 
 	} // closes onCreate()
 
 	// the callback that will receive info from the fragments and then update
 	// all the fragments
 	@Override
 	public void onDataPassed(String dataToBePassed,
 			int fragmentNumberInPagerAdapter, int numbersBase) {
 
 		CalculatorBinaryFragment binaryFrag = null;
 		CalculatorDecimalFragment decFrag = null;
 		CalculatorOctalFragment octalFrag = null;
 		CalculatorHexFragment hexFrag = null;
 		//
 		// finding a fragment by tag is kind of tricky at first, because it's
 		// not intuitively simple to set the tag, and the tag it is assigned by
 		// default can seem pretty cryptic. But if you single step the
 		// application, or google, you will
 		// see that the tag that's assigned to the fragment follows these lines:
 		// "android:switcher:theIdNumberOftheViewContainer:theNumberIntheAdapter".
 		// the ID number of the container is the mViewPagerID number in our
 		// case.
 		//
 		// Find each of the fragments by Tag and then call their public method
 		// we made to update the workingTextView.
 		switch (fragmentNumberInPagerAdapter) {
 		/*
 		 * If changing the ordering of the views, this whole thing will need to
 		 * be reconfigured.
 		 */
 		case 0:
 
 			decFrag = (CalculatorDecimalFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":2");
 			if (decFrag != null) {
 				decFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			octalFrag = (CalculatorOctalFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":3");
 			if (octalFrag != null) {
 				octalFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			hexFrag = (CalculatorHexFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":1");
 			if (hexFrag != null) {
 				hexFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 			break;
 
 		case 1:
 			binaryFrag = (CalculatorBinaryFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":0");
 			if (binaryFrag != null) {
 				binaryFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			octalFrag = (CalculatorOctalFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":3");
 			if (octalFrag != null) {
 				octalFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			decFrag = (CalculatorDecimalFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":2");
 			if (decFrag != null) {
 				decFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 			break;
 
 		case 2:
 			binaryFrag = (CalculatorBinaryFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":0");
 			if (binaryFrag != null) {
 				binaryFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			hexFrag = (CalculatorHexFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":1");
 			if (hexFrag != null) {
 				hexFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			octalFrag = (CalculatorOctalFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":3");
 			if (octalFrag != null) {
 				octalFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 			break;
 
 		case 3:
 			binaryFrag = (CalculatorBinaryFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":0");
 			if (binaryFrag != null) {
 				binaryFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			decFrag = (CalculatorDecimalFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":2");
 			if (decFrag != null) {
 				decFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			hexFrag = (CalculatorHexFragment) getSupportFragmentManager()
 					.findFragmentByTag(
 							"android:switcher:" + R.id.viewPager + ":1");
 			if (hexFrag != null) {
 				hexFrag.updateWorkingTextView(dataToBePassed, numbersBase);
 			}
 
 			break;
 
 		default:
 			// shouldn't happen
 			break;
 		}
 
 	}
 }
