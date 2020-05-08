 package edu.mines.alterego;
 
 import java.util.Locale;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameActivity extends FragmentActivity {
 
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
 
     int mGameId = -1;
     int mCharId = -1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_activity);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
         Intent i = getIntent();
         Bundle extras = i.getExtras();
         if (extras != null) {
             mGameId = extras.getInt((String) getResources().getText(R.string.gameid), -1);
         } else {
             mGameId = -1;
         }
 
         if (mGameId == -1) {
             // Yes, this is annoying, but it'll make an error VERY obvious. In testing, I have never seen this toast/error message. But ya never know
             Toast.makeText(this, "GameID not valid", 400).show();
             Log.e("AlterEgo::CharacterFragment", "Game ID is not valid!!!!!");
         }
 
         CharacterDBHelper dbhelper = new CharacterDBHelper(this);
         mCharId = dbhelper.getCharacterIdForGame(mGameId);
 
 
         Log.i("AlterEgo::CharFrag", "Found the character. The id was " + mCharId);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
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
             Fragment fragment;
             Bundle args = new Bundle();
             args.putInt((String) getResources().getText(R.string.charid), mCharId);
             args.putInt((String) getResources().getText(R.string.gameid), mGameId);
             switch(position) {
                 case 0:
                     fragment = new CharacterFragment();
                     fragment.setArguments(args);
                     break;
                 case 1:
                     fragment = new InventoryFragment();
                     fragment.setArguments(args);
                     break;
                 default:
                     fragment = new DummySectionFragment();
                     args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                     fragment.setArguments(args);
             }
 
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.title_character).toUpperCase(l);
 			case 1:
 				return getString(R.string.title_inventory).toUpperCase(l);
 			case 2:
 				return getString(R.string.title_section3).toUpperCase(l);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
 					container, false);
 			TextView dummyTextView = (TextView) rootView
 					.findViewById(R.id.section_label);
 			dummyTextView.setText(Integer.toString(getArguments().getInt(
 					ARG_SECTION_NUMBER)));
 			return rootView;
 		}
 	}
 
 }
