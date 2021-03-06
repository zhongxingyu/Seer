 /**
  * Copyright (C) 2011 Joseph Lehner <joseph.c.lehner@gmail.com>
  *
  * This file is part of RxDroid.
  *
  * RxDroid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * RxDroid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with RxDroid.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  */
 
 package at.caspase.rxdroid;
 
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.SpannableString;
 import android.text.format.DateFormat;
 import android.text.style.RelativeSizeSpan;
 import android.text.style.UnderlineSpan;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.ViewStub;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import at.caspase.rxdroid.Fraction.MutableFraction;
 import at.caspase.rxdroid.InfiniteViewPagerAdapter.ViewFactory;
 import at.caspase.rxdroid.NotificationReceiver.OnDoseTimeChangeListener;
 import at.caspase.rxdroid.db.Database;
 import at.caspase.rxdroid.db.Drug;
 import at.caspase.rxdroid.db.Entries;
 import at.caspase.rxdroid.db.Entry;
 import at.caspase.rxdroid.db.Intake;
 import at.caspase.rxdroid.ui.DrugOverviewAdapter;
 import at.caspase.rxdroid.util.CollectionUtils;
 import at.caspase.rxdroid.util.DateTime;
 import at.caspase.rxdroid.util.Util;
 
 public class DrugListActivity extends Activity implements OnLongClickListener,
 		OnDateSetListener, OnSharedPreferenceChangeListener, ViewFactory
 {
 	private static final String TAG = DrugListActivity.class.getName();
 	private static final boolean LOGV = false;
 
 	private static final int MENU_SELECT_DATE = 1;
 	private static final int MENU_ADD = 0;
 	private static final int MENU_PREFERENCES = 2;
 	private static final int MENU_TOGGLE_FILTERING = 3;
 
 	private static final int CMENU_TOGGLE_INTAKE = 0;
	// public static final int CMENU_CHANGE_DOSE = 1;
 	private static final int CMENU_EDIT_DRUG = 2;
	// public static final int CMENU_SHOW_SUPPLY_STATUS = 3;
 	private static final int CMENU_IGNORE_DOSE = 4;
 
 	public static final String EXTRA_DATE = "date";
 	public static final String EXTRA_STARTED_FROM_NOTIFICATION = "started_from_notification";
 
 	public static final int TAG_DRUG_ID = R.id.tag_drug_id;
 
 	private SharedPreferences mSharedPreferences;
 
 	private ViewPager mPager;
 	private TextView mTextDate;
 
 	private Date mDate;
 
 	private boolean mShowingAll = false;
 
 	private int mSwipeDirection = 0;
 	private int mLastPage = -1;
 
 	private boolean mIsShowing = false;
 
 	@TargetApi(11)
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		setTheme(Theme.get());
 		setContentView(R.layout.drug_list);
 
 		mPager = (ViewPager) findViewById(R.id.drug_list_pager);
 		mTextDate = (TextView) findViewById(R.id.text_date);
 
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
 
 		mTextDate.setOnLongClickListener(mDateClickListener);
 		mTextDate.setOnClickListener(mDateClickListener);
 
 		// FIXME hack
 		if(!Version.SDK_IS_PRE_HONEYCOMB)
 			mTextDate.setVisibility(View.GONE);
 
 		mPager.setOnPageChangeListener(mPageListener);
 		mPager.setOffscreenPageLimit(1);
 
 		startNotificationService();
 
 		Database.registerEventListener(mDatabaseListener);
 
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 
 		LockscreenActivity.startMaybe(this);
 		Settings.maybeLockInPortraitMode(this);
 
 		Database.init();
 		mIsShowing = true;
 		RxDroid.setIsVisible(this, true);
 
 		final Intent intent = getIntent();
 		if(intent != null)
 			mDate = (Date) intent.getSerializableExtra(EXTRA_DATE);
 
 		if(mDate == null)
 			mDate = Settings.getActiveDate();
 
 		setDate(mDate, PAGER_INIT);
 		NotificationReceiver.registerOnDoseTimeChangeListener(mDoseTimeListener);
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		mIsShowing = false;
 		RxDroid.setIsVisible(this, false);
 		NotificationReceiver.unregisterOnDoseTimeChangeListener(mDoseTimeListener);
 	}
 
 	@Override
 	protected void onStop()
 	{
 		// TODO Auto-generated method stub
 		super.onStop();
 		mPager.removeAllViews();
 		//mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
 		Database.unregisterEventListener(mDatabaseListener);
 	}
 
 	@TargetApi(11)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		menu.add(0, MENU_ADD, 0, R.string._title_add).setIcon(android.R.drawable.ic_menu_add);
 
 		if(Version.SDK_IS_HONEYCOMB_OR_NEWER)
 		{
 			// Title is set in onPrepareOptionsMenu
 
 			menu.add(0, MENU_SELECT_DATE, 0, null)
 				.setIcon(R.drawable.ic_menu_calendar_light)
 				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
 			;
 		}
 
 		menu.add(0, MENU_PREFERENCES, 0, R.string._title_preferences).setIcon(android.R.drawable.ic_menu_preferences);
 		menu.add(0, MENU_TOGGLE_FILTERING, 0, R.string._title_show_all).setIcon(android.R.drawable.ic_menu_view);
 
 		if(Version.SDK_IS_HONEYCOMB_OR_NEWER)
 		{
 			//menu.getItem(MENU_ADD).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			//menu.getItem(MENU_PREFERENCES).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 			//menu.getItem(MENU_TOGGLE_FILTERING).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		}
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		if(Version.SDK_IS_HONEYCOMB_OR_NEWER)
 		{
 			final int titleResId = DateTime.isToday(mDate) ? R.string._title_go_to_date : R.string._title_today;
 			menu.findItem(MENU_SELECT_DATE).setTitle(titleResId);
 		}
 
 		menu.findItem(MENU_TOGGLE_FILTERING).setTitle(mShowingAll ? R.string._title_filter : R.string._title_show_all);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 			case MENU_SELECT_DATE:
 			{
 				if(mDate.equals(Settings.getActiveDate()))
 					mDateClickListener.onLongClick(null);
 				else
 					mDateClickListener.onClick(null);
 
 				return true;
 			}
 
 			case MENU_ADD:
 			{
 				Intent intent = new Intent(Intent.ACTION_INSERT);
 				intent.setClass(this, DrugEditActivity.class);
 				startActivity(intent);
 				return true;
 			}
 			case MENU_PREFERENCES:
 			{
 				Intent intent = new Intent();
 				intent.setClass(this, PreferencesActivity.class);
 				startActivity(intent);
 				return true;
 			}
 			case MENU_TOGGLE_FILTERING:
 			{
 				mShowingAll = !mShowingAll;
 				setDate(mDate, PAGER_INIT);
 				return true;
 			}
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
 	{
 		final DoseView doseView = (DoseView) v;
 		final Drug drug = doseView.getDrug();
 		final int doseTime = doseView.getDoseTime();
 
 		// menu.setHeaderIcon(android.R.drawable.ic_menu_agenda);
 		menu.setHeaderTitle(drug.getName());
 
 		// ////////////////////////////////////////////////
 
 		final Intent editIntent = new Intent(this, DrugEditActivity.class);
 		editIntent.setAction(Intent.ACTION_EDIT);
 		editIntent.putExtra(DrugEditActivity.EXTRA_DRUG, drug);
 		menu.add(0, CMENU_EDIT_DRUG, 0, R.string._title_edit_drug).setIntent(editIntent);
 
 		// ////////////////////////////////////////////////
 
 		final boolean wasDoseTaken = doseView.wasDoseTaken();
 		final int toggleIntakeMessageId;
 
 		if(wasDoseTaken)
 			toggleIntakeMessageId = R.string._title_mark_not_taken;
 		else
 			toggleIntakeMessageId = R.string._title_mark_taken;
 
 		menu.add(0, CMENU_TOGGLE_INTAKE, 0, toggleIntakeMessageId).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 				@Override
 				public boolean onMenuItemClick(MenuItem item)
 				{
 					if(!wasDoseTaken)
 						doseView.performClick();
 					else
 					{
 						MutableFraction dose = new MutableFraction();
 						for(Intake intake : Entries.findIntakes(drug, mDate, doseTime))
 						{
 							dose.add(intake.getDose());
 							Database.delete(intake);
 						}
 
 						drug.setCurrentSupply(drug.getCurrentSupply().plus(dose));
 						Database.update(drug);
 					}
 
 					return true;
 				}
 		});
 
 		if(!wasDoseTaken)
 		{
 			menu.add(0, CMENU_IGNORE_DOSE, 0, R.string._title_ignore_dose)
 					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 						@Override
 						public boolean onMenuItemClick(MenuItem item)
 						{
 							Database.create(new Intake(drug, mDate, doseTime));
 							return true;
 						}
 					});
 		}
 	}
 
 	public void onDrugNameClick(View view)
 	{
 		Intent intent = new Intent(Intent.ACTION_EDIT);
 		intent.setClass(this, DrugEditActivity.class);
 
 		Drug drug = Drug.get((Integer) view.getTag(TAG_DRUG_ID));
 		intent.putExtra(DrugEditActivity.EXTRA_DRUG, drug);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 
 		//startActivityForResult(intent, 0);
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onLongClick(View view)
 	{
 		if(view.getId() == R.id.text_date)
 		{
 
 		}
 		return false;
 	}
 
 	@Override
 	public void onDateSet(DatePicker view, int year, int month, int day)
 	{
 		setDate(DateTime.date(year, month, day), PAGER_INIT);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
 	{
 		if(Settings.Keys.THEME_IS_DARK.equals(key))
 			finish(); // TODO fix this naughty hack
 		else if(mIsShowing)
 			setDate(mDate, PAGER_INIT);
 	}
 
 	@Override
 	public View makeView(int offset)
 	{
 		if(offset <= -(InfiniteViewPagerAdapter.MAX/2))
 		{
 			if(LOGV) Log.d(TAG, "makeView: returning stub for offset=" + offset);
 			return new ViewStub(this);
 		}
 
 		if(LOGV) Log.d(TAG, "makeView: offset=" + offset);
 
 		final View v = getLayoutInflater().inflate(R.layout.drug_list_fragment, null);
 		final ListView listView = (ListView) v.findViewById(android.R.id.list);
 		final TextView emptyView = (TextView) v.findViewById(android.R.id.empty);
 
 		final Calendar cal = DateTime.calendarFromDate(mDate);
 
 		if(mSwipeDirection == 0)
 			cal.add(Calendar.DAY_OF_MONTH, offset);
 		else
 			cal.add(Calendar.DAY_OF_MONTH, mSwipeDirection < 0 ? -1 : 1);
 
 		if(LOGV) Log.v(TAG, "  cal=" + DateTime.toString(cal));
 
 		final List<Drug> drugs = Database.getAll(Drug.class);
 		Collections.sort(drugs);
 		updateListAdapter(listView, cal.getTime(), drugs);
 
 		final int emptyResId = drugs.isEmpty() ? R.string.virtual_msg_no_drugs : R.string._msg_no_doses_on_this_day;
 		emptyView.setText(getString(emptyResId, getString(R.string._title_add)));
 
 		listView.setEmptyView(emptyView);
 
 		return v;
 	}
 
 	public void onDoseViewClick(View view)
 	{
 		final DoseView doseView = (DoseView) view;
 		final Date date = doseView.getDate();
 
 		if(!date.equals(mDate))
 			throw new IllegalStateException("Activity date " + mDate + " differs from DoseView date " + date);
 
 		final Bundle args = new Bundle();
 		args.putInt(IntakeDialog.ARG_DRUG_ID, doseView.getDrug().getId());
 		args.putInt(IntakeDialog.ARG_DOSE_TIME, doseView.getDoseTime());
 		args.putSerializable(IntakeDialog.ARG_DATE, date);
 
 		showDialog(R.id.dose_dialog, args);
 	}
 
 	public void onMissedIndicatorClicked(View view)
 	{
 		final Drug drug = (Drug) view.getTag();
 		final Calendar cal = DateTime.calendarFromDate(mDate);
 
 		do
 		{
 			cal.add(Calendar.DAY_OF_MONTH, -1);
 		} while(!drug.hasDoseOnDate(cal.getTime()));
 
 		Toast.makeText(getApplicationContext(), R.string._toast_drug_notification_icon, Toast.LENGTH_SHORT).show();
 
 		setDate(cal.getTime(), PAGER_INIT | PAGER_SCROLL);
 	}
 
 	public void onLowSupplyIndicatorClicked(View view) {
 		Toast.makeText(this, R.string._toast_low_supplies, Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id, Bundle args)
 	{
 		if(id == R.id.dose_dialog)
 			return new IntakeDialog(this);
 
 		return super.onCreateDialog(id, args);
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog, Bundle args)
 	{
 		if(id == R.id.dose_dialog)
 		{
 			if(!Database.exists(Drug.class, args.getInt(IntakeDialog.ARG_DRUG_ID, -1)))
 			{
 				// If the drug currently associated with the dialog is deleted,
 				// setArgs() throws when attempting to restore from the non-existent
 				// drug id.
 				return;
 			}
 
 			((IntakeDialog) dialog).setArgs(args);
 		}
 		else
 			super.onPrepareDialog(id, dialog, args);
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 	}
 
 	private static final int PAGER_SCROLL = 1;
 	private static final int PAGER_INIT = 1 << 1;
 
 	@TargetApi(11)
 	private void setDate(Date date, int flags)
 	{
 		if(LOGV) Log.v(TAG, "setDate: date=" + date + ", flags=" + flags);
 
 		if(!mIsShowing)
 		{
 			if(LOGV) Log.v(TAG, "setDate: activity is not showing; ignoring");
 			return;
 		}
 
 		mDate = date;
 		getIntent().putExtra(EXTRA_DATE, mDate);
 
 		if((flags & PAGER_INIT) != 0)
 		{
 			final boolean smoothScroll = (flags & PAGER_SCROLL) != 0;
 
 			mSwipeDirection = 0;
 			mLastPage = -1;
 
 			mPager.removeAllViews();
 
 			if(Database.countAll(Drug.class) != 0)
 			{
 				mPager.setAdapter(new InfiniteViewPagerAdapter(this));
 				//mPager.setCurrentItem(1);
 				mPager.setCurrentItem(InfiniteViewPagerAdapter.CENTER, smoothScroll);
 			}
 			else
 			{
 				mPager.setAdapter(new PagerAdapter() {
 
 					@Override
 					public boolean isViewFromObject(View v, Object o) {
 						return v == (View) o;
 					}
 
 					@Override
 					public int getCount()
 					{
 						// TODO Auto-generated method stub
 						return 1;
 					}
 
 					@Override
 					public Object instantiateItem(ViewGroup container, int position)
 					{
 						final View v = makeView(0);
 						Util.detachFromParent(v);
 						container.addView(v);
 						return v;
 					}
 
 					@Override
 					public void destroyItem(ViewGroup container, int position, Object item) {
 						container.removeView((View) item);
 					}
 				});
 				mPager.setCurrentItem(0);
 			}
 		}
 
 		if(Version.SDK_IS_HONEYCOMB_OR_NEWER)
 			invalidateOptionsMenu();
 
 		updateDateString();
 	}
 
 	private void updateListAdapter(ListView listView, Date date, List<Drug> drugs)
 	{
 		if(listView == null)
 		{
 			Log.w(TAG, "updateListAdapter: listView==null");
 			return;
 		}
 
 		if(drugs == null)
 		{
 			drugs = Database.getAll(Drug.class);
 			Collections.sort(drugs);
 		}
 
 		final DrugOverviewAdapter adapter = new DrugOverviewAdapter(this, drugs, date);
 		adapter.setFilter(mShowingAll ? null : new DrugFilter(date));
 
 		listView.setAdapter(adapter);
 	}
 
 	private void startNotificationService()
 	{
 		NotificationReceiver.sendBroadcastToSelf(false);
 		Database.registerEventListener(DATABASE_WATCHER);
 	}
 
 	@TargetApi(11)
 	private void updateDateString()
 	{
 		if(mDate == null)
 			return;
 
 		final SpannableString dateString = new SpannableString(DateFormat.getDateFormat(this).format(mDate.getTime()));
 
 		if(DateTime.isToday(mDate))
 			Util.applyStyle(dateString, new UnderlineSpan());
 
 		if(Version.SDK_IS_HONEYCOMB_OR_NEWER)
 		{
 			Util.applyStyle(dateString, new RelativeSizeSpan(0.75f));
 			getActionBar().setSubtitle(dateString);
 		}
 		else
 			mTextDate.setText(dateString);
 	}
 
 	static class DrugFilter implements CollectionUtils.Filter<Drug>
 	{
 		final boolean mShowSupplyMonitors = Settings.getBoolean(Settings.Keys.SHOW_SUPPLY_MONITORS, false);
 
 		private Date mFilterDate;
 
 		public DrugFilter(Date date) {
 			mFilterDate = date;
 		}
 
 		@Override
 		public boolean matches(Drug drug)
 		{
 			if(mFilterDate == null)
 				return true;
 
 			if(drug.isAutoAddIntakesEnabled())
 			{
 				if(Settings.hasLowSupplies(drug))
 					return true;
 
 				return mShowSupplyMonitors;
 			}
 
 			if(Entries.countIntakes(drug, mFilterDate, null) != 0)
 				return true;
 
 			if(!drug.isActive())
 				return false;
 
 			if(DateTime.isToday(mFilterDate) && Entries.hasMissingIntakesBeforeDate(drug, mFilterDate))
 				return true;
 
 			if(!drug.hasDoseOnDate(mFilterDate))
 				return false;
 
 			return true;
 		}
 	}
 
 	interface OnClickAndLongClickListener extends OnClickListener, OnLongClickListener {};
 
 	private final OnClickAndLongClickListener mDateClickListener = new OnClickAndLongClickListener() {
 
 		@Override
 		public boolean onLongClick(View v)
 		{
 			Calendar cal = DateTime.calendarFromDate(mDate);
 
 			final int year = cal.get(Calendar.YEAR);
 			final int month = cal.get(Calendar.MONTH);
 			final int day = cal.get(Calendar.DAY_OF_MONTH);
 
 			DatePickerDialog dialog = new DatePickerDialog(DrugListActivity.this, DrugListActivity.this, year, month, day);
 			dialog.show();
 			return true;
 		}
 
 		@Override
 		public void onClick(View v)
 		{
 			final Date activeDate = Settings.getActiveDate();
 			setDate(activeDate, PAGER_INIT | PAGER_SCROLL);
 		}
 	};
 
 	private final OnPageChangeListener mPageListener = new OnPageChangeListener() {
 
 		int mPage = InfiniteViewPagerAdapter.CENTER;
 
 		@Override
 		public void onPageSelected(int page)
 		{
 			if(LOGV) Log.v(TAG, "onPageSelected: page=" + page);
 
 			mPage = page;
 
 			//final int swipeDirection;
 
 			if(mLastPage == -1)
 			{
 				mLastPage = InfiniteViewPagerAdapter.CENTER;
 				mSwipeDirection = 0;
 			}
 			else
 				mSwipeDirection = mPage - mLastPage;
 
 			if(LOGV) Log.v(TAG, "  swipeDirection=" + mSwipeDirection);
 
 			if(mSwipeDirection != 0)
 			{
 				if(LOGV) Log.v(TAG, "  mDate: " + mDate);
 				setDate(DateTime.add(mDate, Calendar.DAY_OF_MONTH, mSwipeDirection), 0);
 			}
 
 			mLastPage = page;
 		}
 
 		@Override
 		public void onPageScrolled(int arg0, float arg1, int arg2) {}
 
 		@Override
 		public void onPageScrollStateChanged(int state)
 		{
 //			if(LOGV)
 //			{
 //				final String[] states = { "IDLE", "DRAGGING", "SETTLING" };
 //				Log.v(TAG, "onPageScrollStateChanged: page=" + mPage);
 //				Log.v(TAG, "  state=" + states[state]);
 //			}
 //
 //			if(state == ViewPager.SCROLL_STATE_IDLE)
 //			{
 //				mSwipeDirection = mLastPage != -1 ? mPage - mLastPage : 0;
 //
 //				if(LOGV) Log.v(TAG, "onPageScrollStateChanged: mPage=" + mPage +
 //						", mLastPage=" + mLastPage + ", mSwipeDirection=" + mSwipeDirection);
 //
 //				if(mSwipeDirection != 0)
 //				{
 //					//final int shiftBy = mSwipeDirection < 0 ? -1 : 1;
 //					//setDate(DateTime.add(mDate, Calendar.DAY_OF_MONTH, shiftBy), 0);
 //				}
 //
 //				//mLastPage = mPage;
 //			}
 		}
 	};
 
 	private final Database.OnChangeListener mDatabaseListener = new Database.OnChangeListener() {
 
 		@Override
 		public void onEntryUpdated(Entry entry, int flags) {}
 
 		@Override
 		public void onEntryDeleted(Entry entry, int flags)
 		{
 			if(entry instanceof Drug)
 				setDate(mDate, PAGER_INIT);
 		}
 
 		@Override
 		public void onEntryCreated(Entry entry, int flags)
 		{
 			if(entry instanceof Drug)
 				setDate(mDate, PAGER_INIT);
 		}
 	};
 
 	private final OnDoseTimeChangeListener mDoseTimeListener = new OnDoseTimeChangeListener() {
 
 		@Override
 		public void onDoseTimeBegin(Date date, int doseTime)
 		{
 			if(!date.equals(mDate))
 				setDate(date, PAGER_INIT);
 		}
 
 		public void onDoseTimeEnd(Date date, int doseTime)
 		{
 			setDate(mDate, PAGER_INIT);
 		}
 	};
 
 	private final Database.OnChangeListener DATABASE_WATCHER = new Database.OnChangeListener() {
 
 		@Override
 		public void onEntryUpdated(Entry entry, int flags) {
 			NotificationReceiver.sendBroadcastToSelf(entry instanceof Intake);
 		}
 
 		@Override
 		public void onEntryDeleted(Entry entry, int flags)
 		{
 			NotificationReceiver.sendBroadcastToSelf(false);
 			try
 			{
 				DrugListActivity.this.removeDialog(R.id.dose_dialog);
 			}
 			catch(Exception e)
 			{
 
 			}
 		}
 
 		@Override
 		public void onEntryCreated(Entry entry, int flags) {
 			NotificationReceiver.sendBroadcastToSelf(false);
 		}
 	};
 }
