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
 
 public class TabGroupView extends TabActivity {
 
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
 
 		/** TabSpec setContent() is used to set content for a particular tab. */
 		setupTab(new ImageView(this),R.drawable.ic_tab_winelist,TabGroupWine.class,"tab1");
 		setupTab(new ImageView(this),R.drawable.ic_tab_cellar,TabGroupCellar.class,"tab2");
 		setupTab(new ImageView(this),R.drawable.ic_tab_food,TabGroupFood.class,"tab3");
 		setupTab(new ImageView(this),R.drawable.ic_tab_events,TabGroupEvents.class,"tab4");
 		setupTab(new ImageView(this),R.drawable.ic_tab_location,TabGroupLocation.class,"tab5");
 		//mTabHost.setCurrentTab(1);
 
 	}
 	
 	private void setupTab(final View view, final int drawable, Class<?> tab_class, String tag) {
 		View tabview = createTabView(mTabHost.getContext(), drawable);
 		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(
 			new Intent(this,tab_class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 		getTabHost().addTab(setContent); 
 	}
 
 	private static View createTabView(final Context context, final int drawable) {
 		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
 		ImageView iv = (ImageView) view.findViewById(R.id.tabsImage);
 		iv.setImageResource(drawable);
 		return view;
 	}
 	
 	public TabHost getTabHost()
 	{
 		return mTabHost;
 	}
 }
