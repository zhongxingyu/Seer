 package com.goodhearted.smokebegone;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 public class Info_Activity extends Activity {
 
 	MenuHandler handler;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_info);
 		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
 		tabHost.setup();
 	
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		TabSpec spec1=tabHost.newTabSpec("Tab 1");
 		spec1.setContent(R.id.tab1);
 		spec1.setIndicator("KWF Informatie");
 
 		TabSpec spec2=tabHost.newTabSpec("Tab 2");
 		spec2.setIndicator("Applicatie Informatie");
 		spec2.setContent(R.id.tab2);
 
 
 		tabHost.addTab(spec1);
 		tabHost.addTab(spec2);
 		readyMenu();
 		
 		Button donate = (Button)findViewById(R.id.bDoneer);
 		donate.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v)
 			{
 				Uri uri = Uri.parse( "http://helpen.kwfkankerbestrijding.nl/helpen-als-particulier/Pages/ik-geef-geld.aspx" );
 				startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
 			}
 		});}
 			
 	
 	public boolean onOptionsItemSelected(MenuItem item){
         SlideHolder x = (SlideHolder) findViewById(R.id.bla);
         x.toggle();
         return true;
 
     }
 	
 	private void readyMenu() {
 		handler = new MenuHandler(this);
 		for(int i = 0; i < MenuHandler.allitems.length; i ++) {
 			findViewById(MenuHandler.allitems[i]).setOnClickListener(handler);
 		}
 	}
 
 }
