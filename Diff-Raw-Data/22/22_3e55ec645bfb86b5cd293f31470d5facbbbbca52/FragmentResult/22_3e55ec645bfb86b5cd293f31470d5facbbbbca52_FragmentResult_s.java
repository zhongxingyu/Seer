 package se.mah.kd330a.project.find;
 
 import java.io.InputStream;
 
 import se.mah.kd330a.project.R;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.FragmentTransaction;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FragmentResult extends Fragment implements ActionBar.TabListener {
 
 	public static final String FIND_EXTRA_ROOMNR = "roomNr";
 	private RoomDbHandler mDbHandler;
 
 	ResultPagerAdapter mResultPagerAdapter;
 	ViewPager mViewPager;
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		ViewGroup rootView = (ViewGroup) inflater
 				.inflate(R.layout.fragment_screen_find_result, container, false);
 		Bundle args = getArguments();
 		String roomNr = args.getString(FIND_EXTRA_ROOMNR);
 
 		if (roomNr != null) {
 
 			mDbHandler = new RoomDbHandler(getActivity());
 			if (mDbHandler.isRoomExists(roomNr))
 				Log.i("MAH app Find", mDbHandler.getRoomNr());
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		mResultPagerAdapter = new ResultPagerAdapter(getActivity().getSupportFragmentManager(), 
 				mDbHandler.getRoomDetails());
 		mViewPager = (ViewPager) getView().findViewById(R.id.vp_Find_Pager);
 		mViewPager.setAdapter(mResultPagerAdapter);
		
 		final ActionBar actionBar = getActivity().getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
 		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				// When swiping between different app sections, select the corresponding tab. We can 
 				// also use ActionBar.Tab#select() to do this if we have a reference to the Tab.
 				actionBar.setSelectedNavigationItem(position);
 			}
 		});
		
 		for (int i = 0; i < mDbHandler.getRoomDetails().getPath().size(); i++ ) {
 			actionBar.addTab(actionBar.newTab().setCustomView(R.layout.view_pager_custom_tab)
 					.setTabListener(this));
 			((TextView) actionBar.getTabAt(i).getCustomView().findViewById(R.id.tv_Find_Tab))
					.setText(Integer.toString(i + 1));
 		}
 	}
 
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public static class ResultPagerAdapter extends FragmentStatePagerAdapter {
 
 		private PathToRoom mRoomDetails;
 
 		public ResultPagerAdapter(FragmentManager fm, PathToRoom room) {
 			super(fm);
 			mRoomDetails = room;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Fragment fragment = new NavStepFragment();
 			Bundle args = new Bundle();
 			args.putString(NavStepFragment.ARG_PICNAME, mRoomDetails.getPath().get(position));
 			args.putString(NavStepFragment.ARG_TEXTUP, mRoomDetails.getRoomMane());
 			args.putString(NavStepFragment.ARG_TEXTDOWN, mRoomDetails.getTexts().get(position));
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			if (mRoomDetails != null)
 				return mRoomDetails.getPath().size();
 			else
 				return 0;
 		}
 
 	}
 
 	public static class NavStepFragment extends Fragment {
 
 		public static final String ARG_PICNAME = "pic";
 		public static final String ARG_TEXTUP = "uptext";
 		public static final String ARG_TEXTDOWN = "downtext";
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_screen_find_step, container, false);
 			Bundle args = getArguments();
 
 			//((TextView) rootView.findViewById(R.id.tv_Find_TextUp)).setText(args.getString(ARG_PICNAME));
 
 			int strDown;
 
 			((TextView) rootView.findViewById(R.id.tv_Find_TextUp)).setText(args.getString(ARG_TEXTUP));
 
 			try {
 				strDown = getResources().getIdentifier(args.getString(ARG_TEXTUP), "string", getActivity().getPackageName());
 				((TextView) rootView.findViewById(R.id.tv_Find_TextDown)).setText(getString(strDown));
 			}
 			catch (Exception e) {
 				strDown = -1;
 				e.printStackTrace();
 			}
 
 			((ImageView) rootView.findViewById(R.id.img_Find_PathPic)).setImageDrawable(loadImageFromAssets(args.getString(ARG_PICNAME)));
 
 			return rootView;
 		}
 
 		private Drawable loadImageFromAssets(String pic) {
 			Drawable buffer = null;
 			try {
 				InputStream inPic = getActivity().getBaseContext().getAssets().open("find_nav_pics/" + pic + ".jpg");
 				buffer = Drawable.createFromStream(inPic, null);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return buffer;
 		}
 	}
 }
