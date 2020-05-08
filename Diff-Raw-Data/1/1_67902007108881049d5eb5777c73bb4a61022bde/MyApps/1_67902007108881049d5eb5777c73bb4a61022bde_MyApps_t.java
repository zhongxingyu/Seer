 package rex.login;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
 
 
 import rex.login.AppInfoHelper.AppDetails;
 import rex.login.AppInfoHelper.AppDetails.Times;
 import rex.login.SalesBarChart;
 import rex.login.IDemoChart;
 import rex.login.AbstractDemoChart;
 
 import android.R.drawable;
 import android.R.string;
 
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextSwitcher;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MyApps extends Activity  {
 	private TabHost mTabHost;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ViewGroup v = (ViewGroup) getLayoutInflater().inflate(R.layout.myappscontainer, null);
 		setContentView(v);
 		
 		ImageView rex = (ImageView) findViewById(R.drawable.t_rex);
 		ViewGroup myapps = (ViewGroup) getLayoutInflater().inflate(R.layout.myapps, null);
 		ViewGroup banner = (ViewGroup) getLayoutInflater().inflate(R.layout.banner,null);
 		ViewGroup putbannerhere = (ViewGroup) findViewById(R.id.putbannerhere);
 		putbannerhere.addView(banner);
 		
 		
 		ArrayList<String> values = new ArrayList<String>();
 		List<String> cats = AppInfoHelper.instance().getCategories();
 		for(String cat:  cats)
 		{
 			values.add("Category: " + cat);
 			ViewGroup category = (ViewGroup) getLayoutInflater().inflate(R.layout.category,null);
 			TextView cathere = (TextView) category.findViewById(R.id.cathere);
 			ViewGroup vg = (ViewGroup) v.findViewById(R.id.putappshere);
 			vg.addView(category);
 			cathere.setText(cat);
 			
 			List<AppInfoHelper.AppSummary>appsByUsage = AppInfoHelper.instance().getAppsSortedByUsage(cat);
 			for(AppInfoHelper.AppSummary sum: appsByUsage)
 			{
 				try
 				{
 					MyAppList myapplist = new MyAppList(sum.appName, sum.timeLastPlayed, sum.icon, this);
 					ViewGroup displayapps = (ViewGroup) category.findViewById(R.id.displayapps);
 					if(displayapps == null)
 					    continue;
 					
 					displayapps.addView(myapplist.getMyappslist());
 					AppDetails details = AppInfoHelper.instance().getDetails(sum.packageName);
 					Times firstTime = details.times.getFirst();
 					long st = firstTime.start;
					/*
                         for(Times times: details.times)
                         {
                             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                             String startTime = formatter.format(times.start);
                             String stopTime = formatter.format(times.stop);
 
                             MyAppList myapplist = new MyAppList(startTime + " to " + stopTime, 0, sum.icon, this);
                             vg.addView(myapplist.getMyappslist());
                         }**/
 				}
 				catch(Exception e)
 				{
 					Toast.makeText(getApplicationContext(), "4:" + e.toString(),
 							2000).show();
 
 				}
 			} 
 		}
 		Toast.makeText(getApplicationContext(), Integer.toString(v.getChildCount()),
 				2000).show();
 
 		Button button2 = (Button) findViewById(R.id.button3);
 
 		button2.setOnClickListener(new View.OnClickListener(){
 			public void onClick(View view){
 				Intent intent = new Intent();
 				setResult(RESULT_OK, intent);
 				finish();
 			}
 		});
         setupTabHost();
         //mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
 
         setupTab(new TextView(this), "Games");
         setupTab(new TextView(this), "Social");
         setupTab(new TextView(this), "Media");
 	}
     private void setupTabHost() {
         mTabHost = (TabHost) findViewById(android.R.id.tabhost);
         mTabHost.setup();
     }
     private void setupTab(final View view, final String tag) {
         View tabview = createTabView(mTabHost.getContext(), tag);
 
         TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
             public View createTabContent(String tag) {return view;}
         });
         mTabHost.addTab(setContent);
 
     }
 
     private static View createTabView(final Context context, final String text) {
         View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
         TextView tv = (TextView) view.findViewById(R.id.tabsText);
         tv.setText(text);
         tv.setLinkTextColor(0x000000);
         return view;
     }
 }
 
 
 
