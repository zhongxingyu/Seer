 package com.example.zonedhobbitsportfolio;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	ListView test;
 	TextView header_main;
 	
 	static Person[] arraypersons = new Person[3];
 	
 	int i = 0;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         moveToSplash();
         
         test = (ListView) findViewById(R.id.list_main);
         
         header_main = (TextView) findViewById(R.id.text_header_main);
 
         //Fetch the info from the server and add it to the person object created before.
 
        setUpInfo("http://172.16.3.123:8888/fold/zhPortfolioAPI.php");
 		setUpInfo("http://fredrik-andersson.se/zh/zhPortfolioAPI.php");
         setUpInfo("http://alphahw.eu/zh/zhPortfolioAPI.php");
         
         Typeface font = Typeface.createFromAsset(this.getAssets(),"fonts/Edmondsans-Bold.otf");
         header_main.setTypeface(font);
         
         test.setOnItemClickListener(new OnItemClickListener() {
             
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         	
         		moveToProfile(position, view);
         		
         	}
         });
         
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
     
     public void moveToProfile(int position, View v){
     	
     	Intent i = new Intent(this, ProfileActivity.class);
 
     	//i.putExtra("Person", (Person) test.getItemAtPosition(position));
     	
     	i.putExtra("pos", position);
     	
     	startActivity(i); 
     }
     
     public void moveToSplash(){
     	Intent splashScreen = new Intent(this, SplashScreenActivity.class);
     	startActivity(splashScreen);
     }
     
     public void makeMainList(Person person) {
     	Log.i("PERSON", person.toString());
     	arraypersons[i] = person;
     	i++;
     	
     	if(i == 3){
     		
     		CustomAdapter test1 = new CustomAdapter(this, test.getId(), arraypersons);  
             test.setAdapter(test1);
             SplashScreenActivity.finishSplash();
     		
     	}
     }
     
     public static Person returnPerson(int pos) {
     	return arraypersons[pos];
     }
     
 }
