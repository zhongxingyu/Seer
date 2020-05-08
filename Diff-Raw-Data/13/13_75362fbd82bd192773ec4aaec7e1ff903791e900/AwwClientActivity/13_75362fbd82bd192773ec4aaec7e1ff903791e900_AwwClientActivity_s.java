 package com.elevine.aww;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 
 public class AwwClientActivity extends SherlockFragmentActivity {
 
 	private PostsFragment postsFragment = null;
 	private FragmentManager fragmentManager = null;
 	private Fragment favoritesFragment = null;
 	private ActionBar actionBar = null;
 
 	private Menu menu = null;
 	private MenuItem favoritesMenuItem = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// This has to be called before setContentView and you must use the
 		// class in com.actionbarsherlock.view and NOT android.view
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		setContentView(R.layout.main);
 		this.fragmentManager = getSupportFragmentManager();
 		this.postsFragment = (PostsFragment) fragmentManager
 				.findFragmentById(R.id.posts_fragment);
 		this.favoritesFragment = new FavoritesFragment();
 
 		actionBar = getSupportActionBar();
 
 	}
 
 	public void onThumbnailClick(View v) {
 		if (postsFragment.isVisible()) {
 			postsFragment.onThumbnailClick(v);
 		}
 	}
 
 	public void onTitleClick(View v) {
 		if (postsFragment.isVisible()) {
 			postsFragment.onTitleClick(v);
 		}
 	}
 
 	public void onFavoriteClick(View v) {
 		postsFragment.onFavoriteClick(v);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.menu = menu;
 
 		MenuItem favoritesMenuItem = menu.add("Favorites").setIcon(
 				R.drawable.ic_action_favorites);
 
 		favoritesMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		this.favoritesMenuItem = favoritesMenuItem;
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getTitle().equals("Favorites")) {
 			this.fragmentManager
 					.beginTransaction()
 					//.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
 					.setCustomAnimations(android.R.anim.slide_in_left,
 							android.R.anim.slide_out_right,
 							android.R.anim.fade_in,
 							android.R.anim.fade_out)
 					.hide(postsFragment)
 					.replace(R.id.fragment_container, favoritesFragment)
 					.addToBackStack(null).commit();
 			getSupportFragmentManager().executePendingTransactions();
 			actionBar.setDisplayHomeAsUpEnabled(true);
 			actionBar.setTitle("Aww! - Favorites");
 			this.menu.removeItem(favoritesMenuItem.getItemId());
 			return true;
 		} else if (item.getItemId() == android.R.id.home) {
			this.fragmentManager.popBackStack();

			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setTitle("Aww!");
			this.onCreateOptionsMenu(menu);
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
