 package watsons.wine;
 
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 public class TabBarExample extends TabActivity {
 
 	private TabHost mTabHost;
 	
 	private void setupTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);		
 		mTabHost.setup();
 	}
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tab);
 
 		/** TabHost will have Tabs */
 		setupTabHost();
 		/*TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
 		tabHost.getTabWidget().setStripEnabled(false);
 		tabHost.getTabWidget().setDividerDrawable(null);*/
 		/**
 		 * TabSpec used to create a new tab. By using TabSpec only we can able
 		 * to setContent to the tab. By using TabSpec setIndicator() we can set
 		 * name to tab.
 		 */
 
 		/** tid1 is firstTabSpec Id. Its used to access outside. */
 /*		TabSpec firstTabSpec = mTabHost.newTabSpec("tid1");
 		TabSpec secondTabSpec = mTabHost.newTabSpec("tid2");
 		TabSpec thirdTabSpec = mTabHost.newTabSpec("tid3");
 		TabSpec fourthTabSpec = mTabHost.newTabSpec("tid4");
 		TabSpec fifthTabSpec = mTabHost.newTabSpec("tid5");*/
 
 		/** TabSpec setIndicator() is used to set name for the tab. */
 		/** TabSpec setContent() is used to set content for a particular tab. */
 		setupTab(new ImageView(this),R.drawable.ic_tab_winelist,WineListTab.class);
 		setupTab(new ImageView(this),R.drawable.ic_tab_cellar,SecondTab.class);
 		setupTab(new ImageView(this),R.drawable.ic_tab_food,ThirdTab.class);
 		setupTab(new ImageView(this),R.drawable.ic_tab_events,FourthTab.class);
 		setupTab(new ImageView(this),R.drawable.ic_tab_location,FifthTab.class);
 		/*//firstTabSpec.setIndicator("", this.getResources().getDrawable(R.drawable.ic_tab_winelist)).setContent(
 		//		new Intent(this, FirstTab.class));
 		secondTabSpec.setIndicator("", this.getResources().getDrawable(R.drawable.ic_tab_cellar)).setContent(
 				new Intent(this, SecondTab.class));
 		thirdTabSpec.setIndicator("", this.getResources().getDrawable(R.drawable.ic_tab_food)).setContent(
 				new Intent(this, ThirdTab.class));
 		fourthTabSpec.setIndicator("", this.getResources().getDrawable(R.drawable.ic_tab_events)).setContent(
 				new Intent(this, FourthTab.class));
 		fifthTabSpec.setIndicator("", this.getResources().getDrawable(R.drawable.ic_tab_location)).setContent(
 				new Intent(this, FifthTab.class));
 */
 		/** Add tabSpec to the TabHost to display. */
 		//mTabHost.addTab(firstTabSpec);
 /*		mTabHost.addTab(secondTabSpec);
 		mTabHost.addTab(thirdTabSpec);
 		mTabHost.addTab(fourthTabSpec);
 		mTabHost.addTab(fifthTabSpec);*/
 
 	}
 	private void setupTab(final View view, final int drawable, Class<?> tab_class) {
 		View tabview = createTabView(mTabHost.getContext(), drawable);
 
 		TabSpec setContent = mTabHost.newTabSpec("test").setIndicator(tabview).setContent(
 			new Intent(this,tab_class)
 				//new TabContentFactory() {
 			//public View createTabContent(String tag) {return view;}}
 		);
 		mTabHost.addTab(setContent);
 	}
 
 	private static View createTabView(final Context context, final int drawable) {
 		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
 		ImageView iv = (ImageView) view.findViewById(R.id.tabsImage);
 		iv.setImageResource(drawable);
 		return view;
 	}
 }
