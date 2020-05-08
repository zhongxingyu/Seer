 package com.dbstar.settings;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.dbstar.settings.R;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 
 public class GDSettingsActivity extends PreferenceActivity {
 
 	private static final String LOG_TAG = "Settings";
 	private static final String META_DATA_KEY_HEADER_ID = "com.dbstar.settings.TOP_LEVEL_HEADER_ID";
 	private static final String META_DATA_KEY_FRAGMENT_CLASS = "com.dbstar.settings.FRAGMENT_CLASS";
 	private static final String META_DATA_KEY_PARENT_TITLE = "com.dbstar.settings.PARENT_FRAGMENT_TITLE";
 	private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS = "com.dbstar.settings.PARENT_FRAGMENT_CLASS";
 
 	private static final String SAVE_KEY_CURRENT_HEADER = "com.android.settings.CURRENT_HEADER";
 	private static final String SAVE_KEY_PARENT_HEADER = "com.android.settings.PARENT_HEADER";
 
 	protected String mFragmentClass;
 	protected int mTopLevelHeaderId;
 	protected Header mFirstHeader;
 	protected Header mCurrentHeader;
 	protected Header mParentHeader;
 	
 	private boolean mInLocalHeaderSwitch;
 
 	protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();
 	private List<Header> mHeaders;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		getMetaData();
 //		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
 		mInLocalHeaderSwitch = true;
 		super.onCreate(savedInstanceState);
 		mInLocalHeaderSwitch = false;
 		
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_view);
 //		getListView().setBackgroundResource(R.drawable.view_background);
 
 		if (!onIsHidingHeaders() && onIsMultiPane()) {
 			highlightHeader();
 		}
 
 		// Retrieve any saved state
 		if (savedInstanceState != null) {
 			mCurrentHeader = savedInstanceState
 					.getParcelable(SAVE_KEY_CURRENT_HEADER);
 			mParentHeader = savedInstanceState
 					.getParcelable(SAVE_KEY_PARENT_HEADER);
 		}
 
 		// If the current header was saved, switch to it
 		if (savedInstanceState != null && mCurrentHeader != null) {
 			// switchToHeaderLocal(mCurrentHeader);
 			showBreadCrumbs(mCurrentHeader.title, null);
 		}
 
 		if (mParentHeader != null) {
 			setParentTitle(mParentHeader.title, null, new OnClickListener() {
 				public void onClick(View v) {
 					switchToParent(mParentHeader.fragment);
 				}
 			});
 		}
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		// Save the current fragment, if it is the same as originally launched
 		if (mCurrentHeader != null) {
 			outState.putParcelable(SAVE_KEY_CURRENT_HEADER, mCurrentHeader);
 		}
 		if (mParentHeader != null) {
 			outState.putParcelable(SAVE_KEY_PARENT_HEADER, mParentHeader);
 		}
 	}
 	
 	/**
      * Override initial header when an activity-alias is causing Settings to be launched
      * for a specific fragment encoded in the android:name parameter.
      */
     @Override
     public Header onGetInitialHeader() {
         String fragmentClass = getStartingFragmentClass(super.getIntent());
         if (fragmentClass != null) {
             Header header = new Header();
             header.fragment = fragmentClass;
             header.title = getTitle();
             header.fragmentArguments = getIntent().getExtras();
             mCurrentHeader = header;
             return header;
         }
 
         return mFirstHeader;
     }
 
 	// Called when the user has clicked on a Preference that has a fragment
 	// class name associated with it.
 	@Override
 	public boolean onPreferenceStartFragment(PreferenceFragment caller,
 			Preference pref) {
 		int titleRes = pref.getTitleRes();
 		startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes,
 				null, null, 0);
 		return true;
 	}
 
 	private void highlightHeader() {
 		if (mTopLevelHeaderId != 0) {
 			Integer index = mHeaderIndexMap.get(mTopLevelHeaderId);
 			if (index != null) {
 				getListView().setItemChecked(index, true);
 				getListView().smoothScrollToPosition(index);
 			}
 		}
 	}
 
 	private void switchToHeaderLocal(Header header) {
 		mInLocalHeaderSwitch = true;
 		switchToHeader(header);
 		mInLocalHeaderSwitch = false;
 	}
 
 	@Override
 	public void switchToHeader(Header header) {
 		if (!mInLocalHeaderSwitch) {
 			mCurrentHeader = null;
 			mParentHeader = null;
 		}
 		super.switchToHeader(header);
 	}
 
 	/**
 	 * Switch to parent fragment and store the grand parent's info
 	 * 
 	 * @param className
 	 *            name of the activity wrapper for the parent fragment.
 	 */
 	private void switchToParent(String className) {
 		final ComponentName cn = new ComponentName(this, className);
 		try {
 			final PackageManager pm = getPackageManager();
 			final ActivityInfo parentInfo = pm.getActivityInfo(cn,
 					PackageManager.GET_META_DATA);
 
 			if (parentInfo != null && parentInfo.metaData != null) {
 				String fragmentClass = parentInfo.metaData
 						.getString(META_DATA_KEY_FRAGMENT_CLASS);
 				CharSequence fragmentTitle = parentInfo.loadLabel(pm);
 				Header parentHeader = new Header();
 				parentHeader.fragment = fragmentClass;
 				parentHeader.title = fragmentTitle;
 				mCurrentHeader = parentHeader;
 
 				switchToHeaderLocal(parentHeader);
 				highlightHeader();
 
 				mParentHeader = new Header();
 				mParentHeader.fragment = parentInfo.metaData
 						.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
 				mParentHeader.title = parentInfo.metaData
 						.getString(META_DATA_KEY_PARENT_TITLE);
 			}
 		} catch (NameNotFoundException nnfe) {
 			Log.w(LOG_TAG, "Could not find parent activity : " + className);
 		}
 	}
 
 	/**
 	 * Populate the activity with the top-level headers.
 	 */
 	@Override
 	public void onBuildHeaders(List<Header> headers) {
 		buildHeaders(headers);
 		updateHeadersList(headers);
 	}
 
 	protected void buildHeaders(List<Header> headers) {
 
 	}
 
 	protected void updateHeadersList(List<Header> headers) {
 
 	}
 
 	private void getMetaData() {
 		try {
 			ActivityInfo activityInfo = getPackageManager().getActivityInfo(
 					getComponentName(), PackageManager.GET_META_DATA);
 			if (activityInfo == null || activityInfo.metaData == null)
 				return;
 			mTopLevelHeaderId = activityInfo.metaData.getInt(META_DATA_KEY_HEADER_ID);
 			mFragmentClass = activityInfo.metaData
 					.getString(META_DATA_KEY_FRAGMENT_CLASS);
 
 			// Check if it has a parent specified and create a Header object
 			final int parentHeaderTitleRes = activityInfo.metaData
 					.getInt(META_DATA_KEY_PARENT_TITLE);
 			String parentFragmentClass = activityInfo.metaData
 					.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
 			if (parentFragmentClass != null) {
 				mParentHeader = new Header();
 				mParentHeader.fragment = parentFragmentClass;
 				if (parentHeaderTitleRes != 0) {
 					mParentHeader.title = getResources().getString(
 							parentHeaderTitleRes);
 				}
 			}
 		} catch (NameNotFoundException nnfe) {
 			// No recovery
 		}
 	}
 
 	@Override
 	public Intent getIntent() {
 		Intent superIntent = super.getIntent();
 		String startingFragment = getStartingFragmentClass(superIntent);
 		// This is called from super.onCreate, isMultiPane() is not yet reliable
 		// Do not use onIsHidingHeaders either, which relies itself on this
 		// method
 		if (startingFragment != null && !onIsMultiPane()) {
 			Intent modIntent = new Intent(superIntent);
 			modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
 			Bundle args = superIntent.getExtras();
 			if (args != null) {
 				args = new Bundle(args);
 			} else {
 				args = new Bundle();
 			}
 			args.putParcelable("intent", superIntent);
 			modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS,
 					superIntent.getExtras());
 			return modIntent;
 		}
 		return superIntent;
 	}
 
 	/**
 	 * Checks if the component name in the intent is different from the Settings
 	 * class and returns the class name to load as a fragment.
 	 */
 	protected String getStartingFragmentClass(Intent intent) {
 		if (mFragmentClass != null)
 			return mFragmentClass;
 
 		String intentClass = intent.getComponent().getClassName();
 		if (intentClass.equals(getClass().getName()))
 			return null;
 
 		return intentClass;
 	}
 	
 	@Override
     public void setListAdapter(ListAdapter adapter) {
         if (mHeaders == null) {
             mHeaders = new ArrayList<Header>();
             // When the saved state provides the list of headers, onBuildHeaders is not called
             // Copy the list of Headers from the adapter, preserving their order
             for (int i = 0; i < adapter.getCount(); i++) {
                 mHeaders.add((Header) adapter.getItem(i));
             }
         }
 
         // Ignore the adapter provided by PreferenceActivity and substitute ours instead
         super.setListAdapter(new HeaderAdapter(this, mHeaders));
     }
 
 	protected static class HeaderAdapter extends ArrayAdapter<Header> {
 		static final int HEADER_TYPE_CATEGORY = 0;
 		static final int HEADER_TYPE_NORMAL = 1;
 		private static final int HEADER_TYPE_COUNT = HEADER_TYPE_NORMAL + 1;
 
 		private static class HeaderViewHolder {
 			ImageView icon;
 			TextView title;
 			TextView summary;
 		}
 
 		private LayoutInflater mInflater;
 
 		static int getHeaderType(Header header) {
 			if (header.fragment == null && header.intent == null) {
 				return HEADER_TYPE_CATEGORY;
 			} else {
 				return HEADER_TYPE_NORMAL;
 			}
 		}
 
 		@Override
 		public int getItemViewType(int position) {
 			Header header = getItem(position);
 			return getHeaderType(header);
 		}
 
 		@Override
 		public boolean areAllItemsEnabled() {
 			return false; // because of categories
 		}
 
 		@Override
 		public boolean isEnabled(int position) {
 			return getItemViewType(position) != HEADER_TYPE_CATEGORY;
 		}
 
 		@Override
 		public int getViewTypeCount() {
 			return HEADER_TYPE_COUNT;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		public HeaderAdapter(Context context, List<Header> objects) {
 			super(context, 0, objects);
 			mInflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			HeaderViewHolder holder;
 			Header header = getItem(position);
 			int headerType = getHeaderType(header);
 			View view = null;
 
 			if (convertView == null) {
 				holder = new HeaderViewHolder();
 				switch (headerType) {
 				case HEADER_TYPE_CATEGORY:
 					view = new TextView(getContext());
 					//, null,	android.R.attr.listSeparatorTextViewStyle);
 
 					// if(Utils.platformHasTvUiMode()){
 					// TextView v = (TextView)view;
 					// v.setTextSize(20);
 					// }
 					holder.title = (TextView) view;
 					break;
 
 				case HEADER_TYPE_NORMAL:
 					view = mInflater.inflate(R.layout.preference_header_item,
 							parent, false);
 					holder.icon = (ImageView) view.findViewById(R.id.icon);
 					holder.title = (TextView) view.findViewById(R.id.title);
 					holder.summary = (TextView) view.findViewById(R.id.summary);
 
 					// if(Utils.platformHasTvUiMode()){
 					// holder.title.setTextSize(36);
 					// }
 					break;
 				}
 				view.setTag(holder);
 			} else {
 				view = convertView;
 				holder = (HeaderViewHolder) view.getTag();
 			}
 
 			// All view fields must be updated every time, because the view may
 			// be recycled
 			switch (headerType) {
 			case HEADER_TYPE_CATEGORY:
 				holder.title.setText(header.getTitle(getContext()
 						.getResources()));
 				break;
 
 			//$FALL-THROUGH$
 			case HEADER_TYPE_NORMAL:
 				holder.icon.setImageResource(header.iconRes);
 				holder.title.setText(header.getTitle(getContext()
 						.getResources()));
 				CharSequence summary = header.getSummary(getContext()
 						.getResources());
 				if (!TextUtils.isEmpty(summary)) {
 					holder.summary.setVisibility(View.VISIBLE);
 					holder.summary.setText(summary);
 				} else {
 					holder.summary.setVisibility(View.GONE);
 				}
 				break;
 			}
 
 			return view;
 		}
 	}
 
 }
