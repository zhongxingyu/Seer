 package com.jewellbiz.android.jewellbiz.ui;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.jewellbiz.android.jewellbiz.R;
 import com.jewellbiz.android.jewellbiz.data.JbzProvider;
 
 import android.content.ContentUris;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class MainActivity extends BaseActivity implements JbzListFragment.OnItemSelectedListener {
		
 	private boolean mDualFragments = false;
 	private String[] mToggleLabels = {"Show Titles", "Hide Titles"};
 	private boolean mTitlesHidden = false;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.main);
 		
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		
 		// find our Fragments
 		JbzListFragment listFrag = (JbzListFragment) getSupportFragmentManager().findFragmentById(R.id.list_frag);
 		ContentFragment contentFrag = (ContentFragment) getSupportFragmentManager().findFragmentById(R.id.content_frag);
 		if (contentFrag != null) mDualFragments = true;
 		
 		if (mTitlesHidden) {
 			getSupportFragmentManager().beginTransaction().hide(listFrag).commit();
 		}
 		
 		
 	}
 	
 	/*private void viewArticle(Intent launchIntent, JbzListFragment list) {
 		final long articleId = launchIntent.getLongExtra(EXTRA_ARTICLE_ID, -1);
 		final String articleUrl = launchIntent.getStringExtra(EXTRA_ARTICLE_URL);
 		
 		showContent(ContentUris.withAppendedId(JbzProvider.CONTENT_URI, articleId));
 		
 		
 		if (!mDualFragments) {
 			Intent showContent = new Intent(this, ContentActivity.class);
 			showContent.setData(Uri.parse(articleUrl));
 			startActivity(showContent);
 		} else {
 			ContentFragment mWebView = (ContentFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.content_frag);
 			mWebView.updateUrl(articleUrl);
 		}
 	}*/
 	
 	// Options Menu
 	// -----------------
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If not showing both fragments, remove the "toggle titles" menu item
 		if (!mDualFragments) {
 			menu.removeItem(R.id.menu_toggleList);
 		} else {
 			menu.findItem(R.id.menu_toggleList).setTitle(mToggleLabels[mTitlesHidden ? 0 : 1]);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		
 		case R.id.refresh:
 			//startService(item.getIntent());
 			Toast.makeText(this, "You hit refresh", Toast.LENGTH_SHORT).show();
 			return true;
 			
 		case R.id.menu_toggleList:
 			//toggleVisibleTitles();
 			Toast.makeText(this, "You hit toggle list", Toast.LENGTH_SHORT).show();
 			return true;
 			
 		case R.id.menu_toggleTheme:
 			/*if (mThemeId == R.style.AppThemeDark) {
 				mThemeId = R.style.AppThemeLight;
 			} else {
 				mThemeId = R.style.AppTheme_Dark;
 			}
 			this.recreate();*/
 			Toast.makeText(this, "You hit toggle theme", Toast.LENGTH_SHORT).show();
 			return true;
 			
 		case R.id.menu_prefs:
 			//startActivity(item.getIntent());
 			Toast.makeText(this, "You hit prefs", Toast.LENGTH_SHORT).show();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);		
 		}
 		
 	}
 	
 	@Override
 	public void onSaveInstanceState (Bundle outState) {
 		super.onSaveInstanceState(outState);
 		//outState.putInt("theme", mThemeId);
 		outState.putBoolean("titlesHidden", mTitlesHidden);
 	}
 	
 	@Override
	public void onItemSelected(String articleUrl) {
 		ContentFragment contentFrag = (ContentFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.content_frag);
 		
 		if (contentFrag == null || !contentFrag.isInLayout()) {
			Intent showContent = new Intent(this,
 	                ContentActivity.class);
 	        showContent.setData(Uri.parse(articleUrl));
 	        startActivity(showContent);
 		} else {
 			contentFrag.updateUrl(articleUrl);
 		}
 		
 		
 	}
 	
 	
 
 }
