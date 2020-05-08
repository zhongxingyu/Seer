 package com.pragyancsg.pragyanapp13;
 
 import java.sql.Time;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.TextSwitcher;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher.ViewFactory;
 
 public class PragyanMainActivity extends FragmentActivity implements
 		ViewFactory {
 
 	private TextSwitcher menuSwitcher;
 	private ImageSwitcher bgSwitcher;
 	private PragyanDataParser dataProvider;
 	CustomPagerAdapter myPagerAdapter;
 
 	private int currentPage = 0;
 	String rootName = "root";
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	private ViewPager myViewPager;
 	private TextSwitcher menuNextSwitcher;
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_pragyan_main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 			switch(item.getItemId()){
 			case R.id.menu_update:
 				if(HelperUtils.getDataProvider()!=null){
 					if(HelperUtils.isNetworkOnline(this))
 						new UpdateXmlAsyncTask(this).execute();
 					else
 						Toast.makeText(this, "No Network Connection", Toast.LENGTH_SHORT).show();
 				}
 				default:
 					return super.onOptionsItemSelected(item);
 			}
 	}
 	
 	class UpdateXmlAsyncTask extends AsyncTask<Void, String, Void>{
 
 		public Context context;
 		private ProgressDialog progressDialog;
 		private String failed = "";
 		
 		public UpdateXmlAsyncTask(PragyanMainActivity pragyanMainActivity) {
 			context = pragyanMainActivity;
 			progressDialog = new ProgressDialog(context);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			progressDialog.setCancelable(false);
 			progressDialog.setIndeterminate(true);
 			
 		}
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			progressDialog.show();
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			progressDialog.dismiss();
 			if(failed.length() == 0){
 				Toast.makeText(context, "Update Complete! Refreshing...", Toast.LENGTH_SHORT).show();
 				finish();
 				startActivity(getIntent());
 			}
 			else
 				Toast.makeText(context, failed, Toast.LENGTH_SHORT).show();
 			failed="";
 			super.onPostExecute(result);
 		}
 		
 		@Override
 		protected void onProgressUpdate(String... values) {
 			progressDialog.setMessage(values[0]);
 			super.onProgressUpdate(values);
 		}
 		
 		public void displayMessage(String mesg){
 			publishProgress(mesg);
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			// TODO Auto-generated method stub
			HelperUtils.getDataProvider().updateFromRemoteXml("http://192.168.1.150/pragyanv5.xml",this);  //DEBUG:  "http://192.168.1.150/pragyanv4.xml", this);
			//PROD:      "http://delta.nitt.edu/~robo/pragyanv4.xml",this);
 			return null;
 		}
 		public void notifyFailure(String string) {
 			failed = string;
 		}
 		
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.pragyan_main);
 
 		HelperUtils.cacheBgs(this);
 		dataProvider = new PragyanDataParser(getApplicationContext());
 		HelperUtils.setDataProvider(dataProvider);
 
 		menuSwitcher = (TextSwitcher) findViewById(R.id.menuTitle);
 		menuNextSwitcher = (TextSwitcher) findViewById(R.id.menuTitleNext);
 		bgSwitcher = (ImageSwitcher) findViewById(R.id.bg_image_switcher);
 		menuSwitcher.setFactory(this);
 		menuNextSwitcher.setFactory(new ViewFactory() {
 			
 			@Override
 			public View makeView() {
 				TextView tv = new TextView(getApplicationContext());
 				final float scale = getResources().getDisplayMetrics().density;
 				tv.setPadding((int) (15 * scale + 0.5f), (int) (10 * scale + 0.5f), 0,
 						(int) (10 * scale + 0.5f));
 				// tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				// LayoutParams.WRAP_CONTENT));
 				tv.setTextColor(getResources().getColor(R.color.Ivory));
 				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
 				tv.setMaxLines(1);
 				return tv;
 			}
 		});
 		setMenuTitle(dataProvider.getItemUnderWithIndex(rootName, 0)
 				.getEventName(), "next");
 		setMenuNextTitle(dataProvider.getItemUnderWithIndex(rootName, 1)
 				.getEventName(), "next");
 		
 		bgSwitcher.setFactory(new ViewFactory() {
 
 			@Override
 			public View makeView() {
 				Log.d("SWITCHER","Making view");
 				ImageView v1 = new ImageView(getApplicationContext());
 				v1.setScaleType(ImageView.ScaleType.FIT_XY);
 				v1.setLayoutParams(new ImageSwitcher.LayoutParams(
 						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 				return v1;
 			}
 		});
 		bgSwitcher.setInAnimation(AnimationUtils.loadAnimation(
 				getApplicationContext(), android.R.anim.fade_in));
 		bgSwitcher.setOutAnimation(AnimationUtils.loadAnimation(
 				getApplicationContext(), android.R.anim.fade_out));
 
 		myPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
 		myViewPager = (ViewPager) findViewById(R.id.pager);
 		myViewPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageSelected(int arg0) {
 				Log.d("PAGER", "select" + String.valueOf(arg0));
 				HelperUtils.setNewBg(bgSwitcher, getActivity());
 				if (currentPage < arg0) {
 					setMenuTitle(
 							dataProvider.getItemUnderWithIndex(rootName, arg0)
 									.getEventName(), "next");
 					try{
 					setMenuNextTitle(
 							dataProvider.getItemUnderWithIndex(rootName, arg0+1)
 									.getEventName(), "next");
 					}catch (IndexOutOfBoundsException e) {
 						setMenuNextTitle(
 								" ", "next");
 					}
 					currentPage = arg0;
 					
 					return;
 				}
 				if (currentPage > arg0) {
 					setMenuTitle(
 							dataProvider.getItemUnderWithIndex(rootName, arg0)
 									.getEventName(), "prev");
 					
 					try{
 					setMenuNextTitle(
 							dataProvider.getItemUnderWithIndex(rootName, arg0+1)
 									.getEventName(), "prev");
 					}
 					catch (IndexOutOfBoundsException e) {
 						setMenuNextTitle(
 								" ", "prev");
 					}
 					currentPage = arg0;
 					return;
 				}
 				
 				
 
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		myViewPager.setAdapter(myPagerAdapter);
 		HelperUtils.setNewBg(bgSwitcher, getActivity());
 
 	}
 
 	
 	public Activity getActivity(){
 		return this;
 	}
 	public void setMenuNextTitle(String title, String direction) {
 		if (direction.equalsIgnoreCase("next")) {
 			Log.d("SWITCH", "next");
 			Animation in = AnimationUtils.loadAnimation(
 					getApplicationContext(), android.R.anim.fade_in);
 			menuNextSwitcher.setInAnimation(in);
 			//menuNextSwitcher.setInAnimation(in);
 			Animation out = AnimationUtils.loadAnimation(
 					getApplicationContext(), android.R.anim.fade_out);
 			menuNextSwitcher.setOutAnimation(out);
 			//menuNextSwitcher.setInAnimation(out);
 		} else if (direction.equalsIgnoreCase("prev")) {
 			Log.d("SWITCH", "prev");
 			Animation in = AnimationUtils.loadAnimation(
 					getApplicationContext(),android.R.anim.fade_in);
 			menuNextSwitcher.setInAnimation(in);
 		//	menuNextSwitcher.setInAnimation(in);
 			Animation out = AnimationUtils.loadAnimation(
 					getApplicationContext(), android.R.anim.fade_out);
 			menuNextSwitcher.setOutAnimation(out);
 		//	menuNextSwitcher.setOutAnimation(out);
 		}
 		menuNextSwitcher.setText(title);
 		//menuNextSwitcher.setText(title);
 	}
 
 	public void setMenuTitle(String title, String direction) {
 		if (direction.equalsIgnoreCase("next")) {
 			Log.d("SWITCH", "next");
 			Animation in = AnimationUtils.loadAnimation(
 					getApplicationContext(), R.anim.slide_in_right);
 			menuSwitcher.setInAnimation(in);
 			//menuNextSwitcher.setInAnimation(in);
 			Animation out = AnimationUtils.loadAnimation(
 					getApplicationContext(), R.anim.slide_out_left);
 			menuSwitcher.setOutAnimation(out);
 			//menuNextSwitcher.setInAnimation(out);
 		} else if (direction.equalsIgnoreCase("prev")) {
 			Log.d("SWITCH", "prev");
 			Animation in = AnimationUtils.loadAnimation(
 					getApplicationContext(), android.R.anim.slide_in_left);
 			menuSwitcher.setInAnimation(in);
 		//	menuNextSwitcher.setInAnimation(in);
 			Animation out = AnimationUtils.loadAnimation(
 					getApplicationContext(), android.R.anim.slide_out_right);
 			menuSwitcher.setOutAnimation(out);
 		//	menuNextSwitcher.setOutAnimation(out);
 		}
 		menuSwitcher.setText(title);
 		//menuNextSwitcher.setText(title);
 	}
 	
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class CustomPagerAdapter extends FragmentPagerAdapter {
 
 		public CustomPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// Get the FRAGMENT.
 			Log.d("POSITION", String.valueOf(position));
 			
 			
 			Log.d("FRAGMENT",dataProvider
 					.getItemUnderWithIndex(rootName, position).getEventName());
 			if(dataProvider.getItemUnderWithIndex(rootName, position).getEventName().equalsIgnoreCase("what's now")) 
 			{
 				Log.d("FRAGMENT","WHATS NOW");
 				Fragment fragment = new StaggeredNowFragment(dataProvider);
 				return fragment;
 			}
 			else if(dataProvider.getItemUnderWithIndex(rootName, position).getEventName().equalsIgnoreCase("what's next")) 
 			{
 				Log.d("FRAGMENT","WHATS NEXT");
 				Fragment fragment = new StaggeredNextFragment(dataProvider);
 				return fragment;
 			}
 			else if(dataProvider.getItemUnderWithIndex(rootName, position).getEventName().equalsIgnoreCase("resources")) 
 			{
 				Log.d("FRAGMENT","RESOURCES");
 				Fragment fragment = new StaggeredDownloadFragment(dataProvider);
 				return fragment;
 			}
 			else{
 				Fragment fragment = new StaggeredFragment(dataProvider
 						.getItemUnderWithIndex(rootName, position).getEventName(),
 						dataProvider);
 				
 				// Bundle args = new Bundle();
 				// args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position +
 				// 1);
 				// fragment.setArguments(args);
 				return fragment;
 			}
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			// METHOD THAT WILL GET FRAGMENT COUNT
 			return dataProvider.getNumberOfItemsUnder(rootName);
 		}
 
 	}
 
 	@Override
 	public View makeView() {
 		TextView tv = new TextView(this);
 		final float scale = getResources().getDisplayMetrics().density;
 		tv.setPadding((int) (15 * scale + 0.5f), (int) (10 * scale + 0.5f), 0,
 				(int) (10 * scale + 0.5f));
 		// tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 		// LayoutParams.WRAP_CONTENT));
 		tv.setTextColor(getResources().getColor(R.color.Ivory));
 		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
 		return tv;
 	}
 
 }
