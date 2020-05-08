 package com.innutrac.poly.innutrac;
 
 import java.io.*;
import java.text.DecimalFormat;
 import java.util.*;
 
 import android.os.*;
 import android.app.*;
 import android.view.Menu;
 import android.content.*;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.*;
 import android.widget.*;
 
 import com.innutrac.poly.innutrac.database.*;
 
 public class MainActivity extends Activity {
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private String[] mNavTitles;
 	TextView calCount;
	DecimalFormat df = new DecimalFormat("#.00");
 
 	SharedPreferences prefs;
 	ArrayList<Food> foodsEatenToday = new ArrayList<Food>();
 	DailyPlan dailyPlan;
 
 	// place holder for the recommended intakes according to user profile.
 	private String recCal, recCarb, recChol, recUnsat, recSat, recFib, recProt,
 			recSod, recSug;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		calCount = (TextView) findViewById(R.id.calCount);
 
 		// SharedPreferences prefs = getPreferences(MODE_PRIVATE);
 		// long savedTime = prefs.getLong("savedTime", (long) 0.0);
 		// Date today = new Date();
 		// Date saveDate = new Date(savedTime);
 		//
 		// if (getZeroTimeDate(today).compareTo(getZeroTimeDate(saveDate)) == 1)
 		// {
 		// setUpNewDay();
 		// } else {
 		// retrieveObjectState();
 		// }
 
 		// above codes is now put into a method.
 		checkForNewDay();
 
 		if (getIntent().hasExtra("addFood")) {
 			String prev = getIntent().getStringExtra("addFood");
 			if (prev.equalsIgnoreCase("true")) {
 				FoodDatabase fdb = new FoodDatabase(this);
 				fdb.open("FoodRecord");
 				Food eatenFood = fdb.getMostRecentFoodInsert();
 				dailyPlan.eatFood(eatenFood);
 				foodsEatenToday.add(eatenFood);
 				fdb.close();
 
 				saveObjectState();
 			}
 		} else if (getIntent().hasExtra("calBurned")) {
			double burnedCal = getIntent().getDoubleExtra("calBurned", 0.00);
			burnedCal = Double.parseDouble(df.format(burnedCal));
 
			Toast.makeText(this, "Calories Burned: " + burnedCal,
					Toast.LENGTH_SHORT).show();
 			dailyPlan.setCurrentCalories(dailyPlan.getCurrentCalories()
 					- burnedCal);

			saveObjectState();
 		}
 
		double cal = Double.parseDouble(df.format(dailyPlan
				.getCurrentCalories()));
		calCount.setText("Calories: " + cal);

 		mNavTitles = new String[6];
 		mNavTitles[0] = "Main";
 		mNavTitles[1] = "History";
 		mNavTitles[2] = "Custom Food Maker";
 		mNavTitles[3] = "Exercise";
 		mNavTitles[4] = "Edit Profile";
 		mNavTitles[5] = "Recommendations";
 		//
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// View view = View.inflate(this, R.layout.logo_block, null);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 
 		// mDrawerList.addHeaderView(view);
 
 		// set up the drawer's list view with items and click listener
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.drawer_list_item, mNavTitles));
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				getActionBar().setTitle(mTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				drawerView.setEnabled(true);
 				drawerView.setSelected(true);
 				getActionBar().setTitle(mDrawerTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		if (savedInstanceState == null) {
 			selectItem(0);
 		}
 	}
 
 	/* The click listener for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			selectItem(position);
 		}
 	}
 
 	// make menu button open the navigation drawer
 	@Override
 	public boolean onKeyDown(int keycode, KeyEvent e) {
 		switch (keycode) {
 		case KeyEvent.KEYCODE_MENU:
 			boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 			if (drawerOpen)
 				mDrawerLayout.closeDrawer(mDrawerList);
 			else
 				mDrawerLayout.openDrawer(mDrawerList);
 		case KeyEvent.KEYCODE_BACK:
 
 		}
 		return super.onKeyDown(keycode, e);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/* Called whenever we call invalidateOptionsMenu() */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If the nav drawer is open, hide action items related to the content
 		// view
 		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 		menu.findItem(R.id.action_new).setVisible(!drawerOpen);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.action_new:
 			Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
 
 			if (intent.resolveActivity(getPackageManager()) != null) {
 				startActivity(intent);
 				this.overridePendingTransition(R.anim.slide_in_from_bottom,
 						R.anim.fade_out_half);
 			} else {
 				Toast.makeText(this, R.string.action_void, Toast.LENGTH_LONG)
 						.show();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// fragment switching here
 	private void selectItem(int position) {
 		FragmentTransaction transaction = getFragmentManager()
 				.beginTransaction();
 		// transaction.disallowAddToBackStack();
 
 		switch (position) {
 		case 0:
 			// For testing: System.out.println(this.foodsEatenToday.toString());
 			checkForNewDay();
 			transaction.replace(R.id.content_frame, new PieViewFragment(),
 					"current_fragment").commit();
 			break;
 		case 1:
 			transaction.replace(R.id.content_frame, new HistoryFragment(),
 					"current_fragment").commit();
 			break;
 		case 3:
 			transaction.replace(R.id.content_frame, new ExerciseFragment(),
 					"current_fragment").commit();
 			break;
 		case 4:
 			startActivity(new Intent(MainActivity.this, UserInfoActivity.class)
 					.putExtra("title", "Main"));
 
 			break;
 		case 5:
 			transaction.replace(R.id.content_frame,
 					new RecommendationFragment(), "current_fragment").commit();
 			break;
 		default:
 			transaction.replace(R.id.content_frame, new Fragment(),
 					"current_fragment").commit();
 			break;
 		}
 
 		// update selected item and title, then close the drawer
 		mDrawerList.setItemChecked(position, true);
 		setTitle(mNavTitles[position]);
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	/**
 	 * When using the ActionBarDrawerToggle, you must call it during
 	 * onPostCreate() and onConfigurationChanged()...
 	 */
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggles
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	public void setUpNewDay() {
 		long newDay = System.currentTimeMillis();
 
 		if (dailyPlan != null) {
 			FoodDatabase tmpFoodDB = new FoodDatabase(this);
 			tmpFoodDB.open("UserTotalyDailyIntake");
 			tmpFoodDB.addUserIntakeForTheDay(dailyPlan);
 			tmpFoodDB.close();
 		}
 
 		ProfileDatabase tmpProfDB = new ProfileDatabase(this);
 		tmpProfDB.open("UserDatabase");
 		if (tmpProfDB.isEmpty()) {
 			getRecommendedValuesForUser('M', 25);
 			recProt = String.valueOf(Double.parseDouble(recProt)
 					* (160 * 0.453592));
 		} else {
 			User p = tmpProfDB.getProfile();
 			getRecommendedValuesForUser(p.getGender().toUpperCase(Locale.US)
 					.charAt(0), Integer.parseInt(p.getAge()));
 			if (p.getWeight().isEmpty()) {
 				recProt = String.valueOf(Double.parseDouble(recProt)
 						* (160 * 0.453592));
 			} else {
 				recProt = String.valueOf(Double.parseDouble(recProt)
 						* (Integer.parseInt(p.getWeight()) * 0.453592));
 
 			}
 		}
 
 		this.dailyPlan = new DailyPlan(Double.parseDouble(recCal),
 				Double.parseDouble(recCarb), Double.parseDouble(recChol),
 				Double.parseDouble(recUnsat), Double.parseDouble(recSat),
 				Double.parseDouble(recFib), Double.parseDouble(recProt),
 				Double.parseDouble(recSod), Double.parseDouble(recSug),
 				String.valueOf(newDay));
 		tmpProfDB.close();
 		saveObjectState();
 
 		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
 		editor.putLong("savedTime", newDay);
 		editor.commit();
 		// Need reset pie chart for new day
 	}
 
 	public void getRecommendedValuesForUser(char sex, int age) {
 		NutrientsDatabase ndb = new NutrientsDatabase(this);
 		ndb.open();
 
 		Cursor cur = ndb.getReadableDatabase().rawQuery(
 				"SELECT * FROM intake_requirements WHERE sex = \"" + sex
 						+ "\"AND age_uLimit >= " + age + " AND age_lLimit <= "
 						+ age, null);
 		if (cur.moveToFirst()) {
 			do {
 				recProt = cur.getString(3);
 				recCarb = cur.getString(4);
 				recSod = cur.getString(5);
 				recChol = cur.getString(6);
 				recUnsat = cur.getString(7);
 				recCal = cur.getString(8);
 				recSug = cur.getString(9);
 				recFib = cur.getString(10);
 				recSat = cur.getString(12);
 			} while (cur.moveToNext());
 		}
 		ndb.close();
 	}
 
 	@SuppressWarnings("resource")
 	public void saveObjectState() {
 		FileOutputStream f_out;
 		ObjectOutputStream obj_out;
 
 		String sdPath = Environment.getExternalStorageDirectory()
 				.getAbsolutePath();
 		File savedObj = new File(sdPath + "/Innutrac/vals/myobject.data");
 		File savedList = new File(sdPath + "/Innutrac/vals/mylist.data");
 
 		if (savedObj.exists() && savedList.exists()) {
 			// do nothing
 		} else {
 			File dir = new File(sdPath + "/Innutrac/vals");
 			dir.mkdirs();
 			savedObj = new File(dir, "myobject.data");
 			savedList = new File(dir, "mylist.data");
 			try {
 				FileOutputStream f1 = new FileOutputStream(savedObj);
 				FileOutputStream f2 = new FileOutputStream(savedList);
 				f1.close();
 				f2.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			f_out = new FileOutputStream(savedObj);
 			obj_out = new ObjectOutputStream(f_out);
 			obj_out.writeObject(dailyPlan);
 
 			f_out = new FileOutputStream(savedList);
 			obj_out = new ObjectOutputStream(f_out);
 			if (foodsEatenToday.isEmpty()) {
 				obj_out.writeInt(0);
 			} else {
 				obj_out.writeInt(foodsEatenToday.size());
 				for (int i = 0; i < foodsEatenToday.size(); i++) {
 					obj_out.writeObject(foodsEatenToday.get(i));
 				}
 			}
 
 			f_out.close();
 			obj_out.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("resource")
 	public void retrieveObjectState() {
 		String sdPath = Environment.getExternalStorageDirectory()
 				.getAbsolutePath();
 		File savedObj = new File(sdPath + "/Innutrac/vals/myobject.data");
 		File savedList = new File(sdPath + "/Innutrac/vals/mylist.data");
 
 		FileInputStream f_in;
 		ObjectInputStream obj_in;
 		try {
 			f_in = new FileInputStream(savedObj);
 			obj_in = new ObjectInputStream(f_in);
 			dailyPlan = (DailyPlan) obj_in.readObject();
 
 			f_in = new FileInputStream(savedList);
 			obj_in = new ObjectInputStream(f_in);
 			int size = obj_in.readInt();
 			foodsEatenToday = new ArrayList<Food>();
 			for (int i = 0; i < size; i++) {
 				foodsEatenToday.add((Food) obj_in.readObject());
 			}
 
 			f_in.close();
 			obj_in.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (StreamCorruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static Date getZeroTimeDate(Date fecha) {
 		Date res = fecha;
 		Calendar calendar = Calendar.getInstance();
 
 		calendar.setTime(fecha);
 		calendar.set(Calendar.HOUR_OF_DAY, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		res = calendar.getTime();
 		return res;
 	}
 
 	public DailyPlan getTodayDailyPlan() {
 		return dailyPlan;
 	}
 
 	// public void setTodayDailyPlan(DailyPlan dp) {
 	// dailyPlan = dp;
 	// }
 
 	public ArrayList<Food> getTodayFoodList() {
 		return foodsEatenToday;
 	}
 
 	public void checkForNewDay() {
 		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
 		long savedTime = prefs.getLong("savedTime", (long) 0.0);
 		Date today = new Date();
 		Date saveDate = new Date(savedTime);
 
 		if (getZeroTimeDate(today).compareTo(getZeroTimeDate(saveDate)) == 1) {
 			setUpNewDay();
 		} else {
 			retrieveObjectState();
 		}
 	}
 }
