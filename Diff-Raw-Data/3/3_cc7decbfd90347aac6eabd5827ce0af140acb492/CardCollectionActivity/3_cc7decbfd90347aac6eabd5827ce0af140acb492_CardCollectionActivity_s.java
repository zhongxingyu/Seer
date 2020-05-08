 package com.tearoffcalendar.activities;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.example.android.lifecycle.R;
 import com.tearoffcalendar.app.TearOffApp;
 import com.tearoffcalendar.themes.BasicTheme;
 import com.tearoffcalendar.themes.BasicThemeManager;
 import com.tearoffcalendar.themes.ThemeException;
 
 public class CardCollectionActivity extends FragmentActivity implements
 		FaceUpCardFragment.OnHeadlineSelectedListener,
 		FaceDownCardFragment.OnFaceDownCardClickListener {
 
 	static final String TAG = "CardCollectionActivity";
 
 	private String preferenceTornCardsCollectionKey;
 	private String preferenceFileKey;
 
 	private FaceUpCardFragment firstFragment;
 
 	private static final BasicThemeManager themeManager = TearOffApp
 			.getInstance().getThemeManager();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		preferenceTornCardsCollectionKey = getString(R.string.preference_file_torn_cards_key);
 		preferenceFileKey = getString(R.string.preference_file_key);
 
 		SharedPreferences sharedPref = this.getSharedPreferences(
 				preferenceFileKey, Context.MODE_PRIVATE);
 		Set<String> tornCards = sharedPref.getStringSet(
 				preferenceTornCardsCollectionKey, new HashSet<String>());
 		Log.v(TAG, tornCards.toString());
 		List<String> list = new ArrayList<String>(tornCards);
 
 		setContentView(R.layout.card_fragment_container);
 
 		// Check whether the activity is using the layout version with
 		// the fragment_container FrameLayout. If so, we must add the first
 		// fragment
 		if (findViewById(R.id.card_fragment_container) != null) {
 
 			// However, if we're being restored from a previous state,
 			// then we don't need to do anything and should return or else
 			// we could end up with overlapping fragments.
 			if (savedInstanceState != null) {
 				return;
 			}
 
 			// Create an instance of ExampleFragment
 			firstFragment = new FaceUpCardFragment();
 			firstFragment.setCardNames(list);
 
 			// In case this activity was started with special instructions from
 			// an Intent,
 			// pass the Intent's extras to the fragment as arguments
 			firstFragment.setArguments(getIntent().getExtras());
 
 			// Add the fragment to the 'fragment_container' FrameLayout
 			getSupportFragmentManager().beginTransaction()
 					.add(R.id.card_fragment_container, firstFragment).commit();
 		}
 
 		// initListView(list);
 	}
 
 	public void onCardSelected(Date date) {
 		// The user selected the headline of an article from the
 		// HeadlinesFragment
 
 		// Capture the card fragment from the activity layout
 		FaceDownCardFragment cardFrag = (FaceDownCardFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.face_down_card_fragment);
 
 		if (cardFrag != null) {
 			// If card frag is available, we're in two-pane layout...
 
 			// Call a method in the CardFragment to update its content
 			String str = getWebViewTextByDate(date);
 			Log.v(TAG, "Resulted: " + str);
 			cardFrag.updateCardView(str);
 		} else {
 			// If the frag is not available, we're in the one-pane layout and
 			// must swap frags...
 
 			// Create fragment and give it an argument for the selected article
 			FaceDownCardFragment newFragment = new FaceDownCardFragment();
 			Bundle args = new Bundle();
 			String str = getWebViewTextByDate(date);
 			Log.v(TAG, "One pane, resulted: " + str);
 			args.putString(FaceDownCardFragment.CARD_WEB_VIEW_KEY, str);
 			newFragment.setArguments(args);
 			FragmentTransaction transaction = getSupportFragmentManager()
 					.beginTransaction();
 
 			// Replace whatever is in the fragment_container view with this
 			// fragment,
 			// and add the transaction to the back stack so the user can
 			// navigate back
 			transaction.replace(R.id.card_fragment_container, newFragment);
 			transaction.addToBackStack(null);
 
 			// Commit the transaction
 			transaction.commit();
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_card_collection, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.v(TAG, "onOptionsItemSelected");
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Log.v(TAG, "Resetting torn cards collection...");
 			SharedPreferences sharedPref = this.getSharedPreferences(
 					preferenceFileKey, Context.MODE_PRIVATE);
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.remove(preferenceTornCardsCollectionKey);
 			editor.commit();
 			// Pass empty list to reset view
 
 			firstFragment.resetCardNames();
 			Toast.makeText(getApplicationContext(),
 					"Torn cards history is reset", Toast.LENGTH_SHORT).show();
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private String getWebViewTextByDate(Date date) {
 		SharedPreferences sharedPref = this.getSharedPreferences(
 				getString(R.string.preference_file_key), Context.MODE_PRIVATE);
 		String themeNameKey = getString(R.string.current_theme_key);
 		String currentThemeName = sharedPref.getString(themeNameKey, "");
 		if (currentThemeName.isEmpty()) {
 			// TODO: Add errh
 		} else {
 			try {
 				BasicTheme theme = themeManager
 						.getThemeByName(currentThemeName);
 				return theme.getTextCard(date);
 			} catch (ThemeException te) {
 				Log.e(TAG, te.getMessage());
 				return "";
 			}
 		}
 		return "";
 	}
 
 	public void onFaceDownCardClick() {
 		Log.v(TAG, "Clicked!");
 		FragmentTransaction transaction = getSupportFragmentManager()
 				.beginTransaction();
 		transaction.replace(R.id.card_fragment_container, firstFragment);
 		transaction.addToBackStack(null);
 		transaction.commit();
 	}
 
 }
