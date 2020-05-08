 package rpisdd.rpgme.activities;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.dungeon.model.Monster;
 import rpisdd.rpgme.gamelogic.dungeon.viewcontrol.RoomView;
 import rpisdd.rpgme.gamelogic.dungeon.viewcontrol.ViewObject;
 import rpisdd.rpgme.gamelogic.items.Item;
 import rpisdd.rpgme.gamelogic.player.Player;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 //This is the main activity of the game, that will host the various menus and such.
 public class MainActivity extends FragmentActivity {
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private int mDrawerIcon;
 	private int mIcon;
 
 	// Quests = 0
 	// Quest history = 1
 	// Stats = 2
 	// Inventory = 3
 	// Shop = 4
 	// Dungeon = 5
 	private final String[] mMenuTitles = { "Quests", "Quest History", "Stats",
 			"Inventory", "Shop", "Dungeon" };
 	private final int[] mMenuIcons = { R.drawable.ic_quests,
 			R.drawable.ic_quest_history, R.drawable.ic_stats,
 			R.drawable.ic_inventory, R.drawable.ic_shop, R.drawable.ic_dungeon };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.master_layout);
 
 		Item.load(this);
 		Monster.load(this);
 		Player.loadPlayer(this);
 		Player.getPlayer().setActivity(this);
 
 		ViewObject.setScaleFactor(this);
 		RoomView.setWidth();
 
 		mTitle = mDrawerTitle = getTitle();
 		mIcon = mDrawerIcon = R.drawable.ic_launcher;
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 		// set up the drawer's list view with items and click listener
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.drawer_list_item, mMenuTitles));
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, // host Activity
 				mDrawerLayout, // DrawerLayout object
 				R.drawable.ic_drawer, // nav drawer image to replace 'Up' caret
 				R.string.drawer_open, // "open drawer" description for
 										// accessibility
 				R.string.drawer_close // "close drawer" description for
 										// accessibility
 		) {
 			@Override
 			public void onDrawerClosed(View view) {
 				getActionBar().setTitle(mTitle);
 				getActionBar().setIcon(mIcon);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			@Override
 			public void onDrawerOpened(View drawerView) {
 				// this would set the activity bar to have the default info
 				// when the drawer is open (RPGme and app icon)
 				// getActionBar().setTitle(mDrawerTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			@Override
 			public void onDrawerSlide(View drawerView, float slideOffset) {
 				super.onDrawerSlide(drawerView, slideOffset);
 				mDrawerLayout.bringChildToFront(drawerView);
 				mDrawerLayout.requestLayout();
 			}
 
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		// Select the first menu, Quests
 		if (savedInstanceState == null) {
 			selectItem(0);
 		}
 
 		supportInvalidateOptionsMenu();
 	}
 
 	public Fragment getCurrentFragment() {
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		Fragment currentFragment = fragmentManager
 				.findFragmentByTag("fragmentTag");
 		return currentFragment;
 	}
 
 	// Save all data here
 	@Override
 	public void onPause() {
 		super.onPause();
 		System.out.println("Onpause");
 
 		Player.getPlayer().savePlayer(this);
 
 		// If the player is in a battle, save their last position instead
 		// so that they don't end up in the monster room when the app reloads
 
 		if (getCurrentFragment() instanceof BattleMenu) {
 			System.out.println("We're in battle");
 			Player.getPlayer().saveRoomAsPrev(this);
 		}
 
 	}
 
 	// Back button pressed: emulate home button
 	@Override
 	public void onBackPressed() {
 		if (getCurrentFragment() instanceof CreateQuestMenu) {
 			changeFragment(new QuestMenu());
 		} else {
 			moveTaskToBack(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	private TextView actionBarEnergyText = null;
 
 	// Called whenever we call invalidateOptionsMenu()
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If nav drawer is open, hide action items related to the content view
 		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 
 		MenuItem menuItem = menu.findItem(R.id.actionBarEnergy);
 
 		if (menuItem == null) {
 			System.out.println("menuitem null");
 		}
 
 		View v = menuItem.getActionView();
 		if (v == null) {
 			System.out.println("view null");
 		}
 		actionBarEnergyText = (TextView) v
 				.findViewById(R.id.actionBarEnergyText);
 
 		if (actionBarEnergyText == null) {
 			System.out.println("text view null");
 		}
 
 		String energyText = Integer.toString(Player.getPlayer().getEnergy())
 				+ "/" + Integer.toString(Player.getPlayer().getMaxEnergy());
 
 		actionBarEnergyText.setText(energyText);
 
 		return super.onPrepareOptionsMenu(menu);
 
 	}
 
 	public void updateEnergyBar() {
 
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				if (actionBarEnergyText != null) {
 					String energyText = Integer.toString(Player.getPlayer()
 							.getEnergy())
 							+ "/"
 							+ Integer.toString(Player.getPlayer()
 									.getMaxEnergy());
 					actionBarEnergyText.setText(energyText);
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// The action bar home/up action should open or close the drawer.
 		// ActionBarDrawerToggle will take care of this.
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		return false;
 	}
 
 	// The click listner for ListView in the navigation drawer
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			selectItem(position);
 		}
 	}
 
 	// Change the current fragment, or menu.
 	public void changeFragment(Fragment fragment) {
 
 		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 		try {
 			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
 					.getWindowToken(), 0);
 		} catch (Exception e) {
 
 		}
 		// Need to change title to "Stats Menu" if we get kicked out of dungeon
 		if (fragment instanceof StatsMenu) {
 			mDrawerList.setItemChecked(2, true);
 			setTitle(mMenuTitles[2]);
 			getActionBar().setIcon(mMenuIcons[2]);
 		} else if (fragment instanceof DungeonMenu) {
 			int floor = Player.getPlayer().getDungeon().getLevel();
 			setTitle(mMenuTitles[5] + " B" + Integer.toString(floor));
 		}
 
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		fragmentManager.beginTransaction()
 				.replace(R.id.content_frame, fragment, "fragmentTag").commit();
 	}
 
 	// Called when an item was selected in the navigation drawer.
 	private void selectItem(int position) {
 
 		Fragment fragment;
 
 		if (mMenuTitles[position].equals("Quests")) {
 			fragment = new QuestMenu();
 		} else if (mMenuTitles[position].equals("Inventory")) {
 			fragment = new InventoryMenu();
 		} else if (mMenuTitles[position].equals("Stats")) {
 			fragment = new StatsMenu();
 		} else if (mMenuTitles[position].equals("Shop")) {
 			fragment = new ShopMenu();
 		} else if (mMenuTitles[position].equals("Dungeon")) {
 			if (Player.getPlayer().isConscious()) {
 				fragment = new DungeonMenu();
 			} else {
 				fragment = new UnconsciousWarning();
 			}
 		} else if (mMenuTitles[position].equals("Quest History")) {
 			fragment = new QuestHistoryMenu();
 		} else {
 			// update selected item and title, then close the drawer
 			mDrawerList.setItemChecked(position, true);
 			setTitle(mMenuTitles[position]);
 			setIcon(mMenuIcons[position]);
 			mDrawerLayout.closeDrawer(mDrawerList);
 			return;
 		}
 
 		// update selected item and title, then close the drawer
 		mDrawerList.setItemChecked(position, true);
 		setTitle(mMenuTitles[position]);
 		setIcon(mMenuIcons[position]);
 		mDrawerLayout.closeDrawer(mDrawerList);
 
		changeFragment(fragment);
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	public void setIcon(int iconLocation) {
 		mIcon = iconLocation;
 		getActionBar().setIcon(iconLocation);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggls
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	public void enableActionBar(boolean show) {
 
 		if (show) {
 			System.out.println("We are showing the action bar");
 		} else {
 			System.out.println("We are hiding the action bar");
 		}
 
 		if (!show) {
 			getActionBar().hide();
 		} else {
 			getActionBar().show();
 		}
 	}
 
 	public void enableNavigationDrawer(boolean show) {
 		if (!show) {
 			mDrawerLayout
 					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
 		} else {
 			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
 		}
 	}
 
 	public void enableHomeButton(boolean isEnabled) {
 		getActionBar().setHomeButtonEnabled(isEnabled);
 	}
 
 	public void hideSoftKeyboard() {
 		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 		inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
 				.getWindowToken(), 0);
 	}
 
 	public void setupUI(View view) {
 
 		// Set up touch listener for non-text box views to hide keyboard.
 		if (!(view instanceof EditText)) {
 
 			view.setOnTouchListener(new OnTouchListener() {
 
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					hideSoftKeyboard();
 					return false;
 				}
 
 			});
 		}
 
 		// If a layout container, iterate over children and seed recursion.
 		if (view instanceof ViewGroup) {
 
 			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
 
 				View innerView = ((ViewGroup) view).getChildAt(i);
 
 				setupUI(innerView);
 			}
 		}
 	}
 
 }
