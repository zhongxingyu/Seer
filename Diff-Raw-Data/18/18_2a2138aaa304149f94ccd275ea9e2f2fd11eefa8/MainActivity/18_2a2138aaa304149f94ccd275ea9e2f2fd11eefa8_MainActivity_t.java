 package com.example.zonedhobbitsportfolio;
 
 import java.util.concurrent.ExecutionException;
 
 import android.os.Bundle;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
import android.graphics.Typeface;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	ListView test;
	TextView header_main;
	
 	Person[] arraypersons = new Person[3];
	
 	int i = 0;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         test = (ListView) findViewById(R.id.list_main);
         
        header_main = (TextView) findViewById(R.id.text_header_main);
 
         //Fetch the info from the server and add it to the person object created before.
 
         setUpInfo("http://puertosur.com.ar/Martin/andPorfolio/zhPortfolioAPI.php");
 		setUpInfo("http://fredrik-andersson.se/zh/zhPortfolioAPI.php");
         setUpInfo("http://alphahw.eu/zh/zhPortfolioAPI.php");
         
        Typeface font = Typeface.createFromAsset(this.getAssets(),"fonts/Edmondsans-Bold.otf");
        header_main.setTypeface(font);
        
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public void setUpInfo(String url) {
     	Fetcher garcon = new Fetcher(this);
     	garcon.execute(url);
     }
     
     public void moveToProfile(View v){
     	Intent i = new Intent(this, ProfileActivity.class);
     	startActivity(i); 
     }
     
     public void makeMainList(Person person) {
     	Log.i("PERSON", person.toString());
     	arraypersons[i] = person;
     	i++;
     	
     	if(i == 3){
     		
     		CustomAdapter test1 = new CustomAdapter(this, test.getId(), arraypersons);  
             test.setAdapter(test1);
     		
     	}
     }
     
 }
