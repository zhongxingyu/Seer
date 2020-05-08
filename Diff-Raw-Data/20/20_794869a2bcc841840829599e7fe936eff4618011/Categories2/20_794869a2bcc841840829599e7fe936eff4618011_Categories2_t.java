 package com.example.mobileindia;
 
 import com.parse.ParseAnonymousUtils;
 import com.parse.ParseUser;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Categories2 extends Activity {
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_categories2);
 		setHeader();
 		
 		addCatButtons();	
 		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
        ListViewCategory.parsePostList = null;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_categories2, menu);
 		return true;
 	}
 	
 	 @Override
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	    	Intent back = new Intent(this,CitySelect.class);
 	        startActivity(back);
 	        return super.onOptionsItemSelected(item);
 	    }
 	 
 	  @Override
 	    public boolean onKeyDown(int keyCode, KeyEvent event) {
 	        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 	           // Log.d(this.getClass().getName(), "back button pressed");
 	        	Intent back = new Intent(this,CitySelect.class);
 	            startActivity(back);
 	        }
 	        return super.onKeyDown(keyCode, event);
 	    }
 	
 	public void addCatButtons(){
 		
 		int i = 0;
 		String[] catName = new String[10];
 		catName = getCats();
 		
 		while(i < 10){
 			final String NameOfCat = catName[i];
 			Button catButton = new Button(this);
 			catButton.setText(NameOfCat);
 			
 			String passVal = "";
 			
 			Bundle extras = getIntent().getExtras();
 			if (extras != null) {
 			    String extraText = extras.getString("CityName");
 			    
 			    Context context = getApplicationContext();
 			    
 			    passVal = NameOfCat + "|" + extraText;
 			    
 			}
 			
 			catButton.setOnClickListener(new Button.OnClickListener() {
 				
 			    public void onClick(View v) {
 			    	gotoPost(v, NameOfCat);
 			    }
 			});
 				
 			LinearLayout ll = (LinearLayout)findViewById(R.id.CatList);
 			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 			ll.addView(catButton, lp);
 			
 			i++;
 		}
 		
 		
 	}
 	
 	public void gotoPost(View v, String passVal){
 		 ListViewCategory.CATEGORY = passVal;
          Intent i = new Intent(this, ListViewCategory.class);
          if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
         	 ListViewCategory.hideAdd = false;
         	 Single_Post.hideAdd = false;
          }else{
         	 ListViewCategory.hideAdd = true;
         	 Single_Post.hideAdd = true;
          }
 		 startActivity(i);
 	}
 	
 	public void setHeader(){
 		String headerText = "";
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 		    String extraText = extras.getString("CityName");
 
 		    Context context = getApplicationContext();
 //		    CharSequence text = "Categories For " + extraText;
 		    
 		    headerText = "Categories For " + extraText;
 		    
 //		    int duration = Toast.LENGTH_LONG;
 //		    Toast toast = Toast.makeText(context, text, duration);
 //		    toast.show();
 		    
 		}
 		
 		if (headerText == "") {
 			headerText = "Categories";
 		}
 		
 		TextView t = new TextView(this);
 		t=(TextView)findViewById(R.id.mainActivityUserLabel);
 		
 		t.setText(headerText);
 		
 		
 	}
 	
 	public String[] getCats(){
 		String[] cats;
 		cats = new String[10];
 		
 		cats[0] = "Food";
 		cats[1] = "Home";
 		cats[2] = "Services";
 		cats[3] = "Taxi";
 		cats[4] = "For Sale";
 		cats[5] = "Trade";
 		cats[6] = "Want To Buy";
 		cats[7] = "Help Wanted";
 		cats[8] = "Other";
 		cats[9] = "";
 		
 		
 		return cats;
 		
 	}
 
 }
