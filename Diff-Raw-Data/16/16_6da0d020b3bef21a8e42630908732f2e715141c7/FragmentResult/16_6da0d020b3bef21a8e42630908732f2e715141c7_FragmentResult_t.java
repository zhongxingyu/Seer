 package se.mah.kd330a.project.find.view;
 
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.find.data.ResultPagerAdapter;
 import se.mah.kd330a.project.find.data.RoomDbHandler;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 //import android.widget.Toast;
 import android.widget.TextView;
 
 public class FragmentResult extends Fragment {
 
 	public static final String ARG_ROOMNR = "roomNr";
 	public static final String ARG_BUILDINGPOS = "building";
 	private static final String SAVE_CURRENT = "currPic";
 
 	private RoomDbHandler mDbHandler;
 	private int buildingPos;
 
 	private ResultPagerAdapter mResultPagerAdapter;
 	private ViewPager mViewPager;
 	private int mSelectedTab = -1;
 
 	private RadioGroup radioG;
 
 	private OnClickListener rb_OnClick = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			mSelectedTab = Integer.parseInt(v.getTag().toString());
 			mViewPager.setCurrentItem(mSelectedTab);
 
 			//Text for Step should be changed here 
 			TextView tvSteps = (TextView) getView().findViewById(R.id.text_find_indicator_steps);
 			String txt = (String) getString(R.string.text_find_indicator_steps) + " ";
 			int step = mSelectedTab + 1; 
 			txt += step + "/" + radioG.getChildCount();
 			tvSteps.setText(txt);
 			//Toast.makeText(getActivity(), radioPicButton.getTag().toString(), Toast.LENGTH_SHORT).show();			
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.i("project", "onCreate " + mSelectedTab);
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		Log.i("project", "onCreateView " + mSelectedTab);
 		ViewGroup rootView = (ViewGroup) inflater
 				.inflate(R.layout.fragment_screen_find_result, container, false);
 		Bundle args = getArguments();
 		String roomNr = args.getString(ARG_ROOMNR);
 		buildingPos = args.getInt(ARG_BUILDINGPOS);
 		if (roomNr != null) {
 
 			mDbHandler = new RoomDbHandler(getActivity());
 			if (mDbHandler.isRoomExists(roomNr))
 				Log.i("project", mDbHandler.getRoomNr());
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.i("project", "onStart " + mSelectedTab);
 		mResultPagerAdapter = new ResultPagerAdapter(getChildFragmentManager(), 
 				mDbHandler.getRoomDetails());
 		mViewPager = (ViewPager) getView().findViewById(R.id.vp_Find_Pager);
 		mViewPager.setAdapter(mResultPagerAdapter);
 
 		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				mSelectedTab = position;
 				RadioButton radioB = (RadioButton) radioG.findViewWithTag(mSelectedTab);
 				radioB.setChecked(true);
 
 				//Text for Step should be changed here 
 				TextView tvSteps = (TextView) getView().findViewById(R.id.text_find_indicator_steps);
 				String txt = (String) getString(R.string.text_find_indicator_steps) + " ";
 				int step = Integer.parseInt(radioB.getTag().toString()) + 1; 
 				txt += step + "/" + radioG.getChildCount();
 				tvSteps.setText(txt);
 
 				//Toast.makeText(getActivity(), "rb_Find_Radio" + position, Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		radioG = (RadioGroup) getView().findViewById(R.id.rg_Find_Pager_Indicator);
 		RadioButton radioB;
 
 		if (radioG.getChildCount() == 0) {
 
 			for (int i = 0; i < mResultPagerAdapter.getCount(); i++) {
 
 				radioB = new RadioButton(getActivity());
 				radioB.setTag(i);
 				radioB.setButtonDrawable(R.drawable.find_page_indicator);
 				radioB.setFocusable(false);
 				radioB.setChecked(false); 
 
 				radioB.setPadding(convertDpToPixel(10), 0, convertDpToPixel(10), 0);
 
 				radioB.setOnClickListener(rb_OnClick);
 				radioG.addView(radioB);
 
 			}
 
 			((RadioButton)radioG.findViewWithTag(0)).setChecked(true);
 			String txt = (String) getResources().getText(R.string.text_find_indicator_steps) 
 					+ " 1/" + radioG.getChildCount();
 			((TextView) getView().findViewById(R.id.text_find_indicator_steps)).setText(txt);
 		}
 		if (mSelectedTab > -1)  {
 			radioB = (RadioButton) getView().findViewWithTag(mSelectedTab); 
 			radioB.setChecked(true);
 			mViewPager.setCurrentItem(Integer.parseInt(radioB.getTag().toString()));
 			String txt = (String) getResources().getText(R.string.text_find_indicator_steps) 
 					+ " " + (mSelectedTab + 1) + "/" + radioG.getChildCount();
 			((TextView) getView().findViewById(R.id.text_find_indicator_steps)).setText(txt);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.find, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.find_menu_floormap:
 			Fragment fragment = new FragmentFloorMap();
 
 			Bundle args = new Bundle();
 			args.putString(FragmentFloorMap.ARG_FLOORMAP, mDbHandler.getMapName());
 			fragment.setArguments(args);
 
 			FragmentManager	 fragmentManager = getActivity().getSupportFragmentManager();
 			FragmentTransaction fragmentTrans = fragmentManager.beginTransaction();	
 			fragmentTrans.replace(R.id.content_frame, fragment);
 			fragmentTrans.addToBackStack(null);
 			fragmentTrans.commit();
 			return true;
 
 		case R.id.find_menu_google:
 			String[] buildingNames = getResources().getStringArray(R.array.find_building_array);
 			String location = buildingNames[buildingPos];
 
 			if(location.equals("Klerken (Kl)"))
				location = "Jan Waldenstroms gata 25";
			else if(location.equals("University Hospital (As)"))
				location = "Carl Gustafs vag 34";
 
 			//getting the google map
 			Intent i = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse("geo:0,0?q="+location+"+Malmo+Sweden"));
 
 			startActivity(i);
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	public int convertDpToPixel(float dp){
 		//Resources resources = getResources();
 		DisplayMetrics metrics = getResources().getDisplayMetrics();
 		float px = dp * (metrics.densityDpi / 160f);
 		return (int) px;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		Log.i("project", "onSaveInstanceState " + mSelectedTab);
 		super.onSaveInstanceState(outState);
 		outState.putInt(SAVE_CURRENT, mSelectedTab);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		Log.i("project", "onActivityCreated " + mSelectedTab);
 		super.onActivityCreated(savedInstanceState);
 		
 		if (savedInstanceState != null) {
 			mSelectedTab = savedInstanceState.getInt(SAVE_CURRENT);
 		}
 	}
 }
