 package com.t1dm.t1dmanagementapp;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import android.R.color;
 import android.os.Bundle;
 import android.app.ActionBar.Tab;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.Menu;
import android.view.MenuInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SlidingDrawer;
 import android.widget.TabHost;
 import android.widget.Toast;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 
 @SuppressWarnings("deprecation")
 public class HomeScreen extends TabActivity implements OnClickListener {
  
 	
 	private SlidingDrawer slidingDrawer;
 	private Button slideButton;
 	private ListView listview ;
 	private SettingsArrayAdapter adapter;
 	private CommonMethods commonMethods = new CommonMethods();
 	private T1DMApplication appContext;
 	
 	//private Fragment emergencyModule = new ManageEmergencyModule();
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		 super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_home_screen);
 
          appContext = ((T1DMApplication) getApplicationContext());
  		appContext.setContext(getApplicationContext());
  		
          TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
          
         TabHost.TabSpec tab1 = tabHost.newTabSpec("Monitoring");
         //TabHost.TabSpec tab2 = tabHost.newTabSpec("Profile");
         TabHost.TabSpec tab3 = tabHost.newTabSpec("Schedules");
         TabHost.TabSpec tab4 = tabHost.newTabSpec("Body Profile");
         TabHost.TabSpec tab2 = tabHost.newTabSpec("Trend");
         TabHost.TabSpec tab5 = tabHost.newTabSpec("Foods");
 
        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
         tab1.setIndicator("Monitoring");
         tab1.setContent(new Intent(this,BeginMonitoring.class));
        
         tab2.setIndicator("Trend");
         Intent intent = new Intent(appContext.getContext(), ReadingsChart.class);
         Calendar calendar = Calendar.getInstance();
         String month = (calendar.get(Calendar.MONTH)+1<10)?0+Integer.toString(calendar.get(Calendar.MONTH)+1):
         	Integer.toString(calendar.get(Calendar.MONTH)+1);
 		String day = (calendar.get(Calendar.DAY_OF_MONTH)<10)?0+Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)):
 			Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
 		
 		String today = calendar.get(Calendar.YEAR)+"-"+month+"-"+day;
 		
 		intent.putExtra("FromDate", today);
 		intent.putExtra("ToDate", today);
 		
 		
         tab2.setContent(intent);
 
         tab3.setIndicator("Schedules");
         tab3.setContent(new Intent(this,Schedules.class));
        
         tab4.setIndicator("Body Profile");
         tab4.setContent(new Intent(this,BodyRequirements.class));
         
         tab5.setIndicator("Foods");
         tab5.setContent(new Intent(this,AvailableFoods.class));
        
         /** Add the tabs  to the TabHost to display. */
         tabHost.addTab(tab1);
         tabHost.addTab(tab2);
         tabHost.addTab(tab3);
         tabHost.addTab(tab4);
         tabHost.addTab(tab5);
           
         slideButton = (Button) findViewById(R.id.handle);
 		  slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
 		 
 		  
 		  slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
 		   @Override
 		   public void onDrawerOpened() {
 		    slideButton.setText(">");
 		    listview = (ListView) findViewById(R.id.slidingList);
 		    listview.setBackgroundColor(Color.LTGRAY);
 		    
 		    adapter = new SettingsArrayAdapter(HomeScreen.this, android.R.layout.simple_list_item_1 , commonMethods.SETTINGS);
 		    listview.setAdapter(adapter);
 
 		    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			      @Override
 			      public void onItemClick(AdapterView<?> parent, View view,
 			          int position, long id) {
 			        final String item = (String) parent.getItemAtPosition(position);
 	/*"Use Audio Message", 
 	 * "Emergency Mode", 
 	 * "Insulin Dosage", 
 	 * "My Profile", 
 	 * "Meal Plan",
 	 * "Report", 
 	 * "Feedback"
 	 * */
 			        switch(position)
 			        {
 			        case 0:
 			        	startActivity(new Intent(appContext.getContext(),	RecordMessage.class));
 			        	break;
 			        case 1:
 			        	startActivity(new Intent(appContext.getContext(),	ManageEmergencyModule.class));
 			        	break;
 			        case 2:
 			        	startActivity(new Intent(appContext.getContext(),	ManageInsulinDosage.class));
 			        	break;
 			        case 3:
 			        	startActivity(new Intent(appContext.getContext(),	UserDetailsActivity.class));
 			        	break;
 			        case 4:
 			        	startActivity(new Intent(appContext.getContext(),	ShowMealPlan.class));
 			        	break;			        
 			        case 5:
 			        	startActivity(new Intent(appContext.getContext(),	GenerateReport.class));
 			        	break;			
 			        case 6:
 			        	startActivity(new Intent(appContext.getContext(),	SendFeedback.class));
 			        	break;
 			        
 			        }
 			      }
 			});
 		   }
 		  });
 		  slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
 		   @Override
 		   public void onDrawerClosed() {
 		    slideButton.setText("<");
 		   }
 		  });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
		   int tab = getTabHost().getCurrentTab();
		   if (tab==1)
		       inflater.inflate(R.menu.begin_monitoring, menu); 
		   else if (tab==2)
		       inflater.inflate(R.menu.readings_chart, menu);
		   else if (tab==5)
		       inflater.inflate(R.menu.available_foods, menu);
		   
		   
		  return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private class SettingsArrayAdapter extends ArrayAdapter<String> {
 
 	    public SettingsArrayAdapter(Context context, int textViewResourceId, List<String> entries) {
 	      super(context, textViewResourceId, entries);	     
 	    }
 	    	    
 	  }
 }
