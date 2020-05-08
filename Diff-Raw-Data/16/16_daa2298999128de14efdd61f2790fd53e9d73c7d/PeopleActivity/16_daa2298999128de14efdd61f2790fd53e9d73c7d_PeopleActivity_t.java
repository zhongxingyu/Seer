 package com.thebitisland.campamentosdiaper;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.thebitisland.campamentosdiaper.auxClasses.*;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 
 public class PeopleActivity extends Activity{
 
 	private ExpandableListAdapter mAdapter;
	static Context context;
 	DBManager database;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_people);
 		
 		context = getApplicationContext();
 		database = new DBManager(this);
 		generateExpandableListView();
 
 	}
 
 	/* Why do we even need a menu here?
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}*/
 	
 	public void generateExpandableListView(){
 		List<Person> cats = new ArrayList<Person>();
 		List<List<PersonOptions>> items = new ArrayList<List<PersonOptions>>();
 		database.open();
 		String[][] users = database.getAllUsers();
 		for(int i=0;i<users.length;i++) {
			Bitmap photo = getBitMap(users[i][4]);
			
 		cats.add(new Person(users[i][2]+" "+users[i][3],users[i][6],"Monitor", photo, 1));
 		List<PersonOptions> persona = new ArrayList<PersonOptions>();
 		persona.add(new PersonOptions(Integer.parseInt(users[i][0])));
 		items.add(persona);
 		}
 		
 
 		//Bitmap bmp = getBitMap(String name);
 
 		//Bitmap bPNGcompress = codec(bmp, Bitmap.CompressFormat.PNG, 0);
 
 		//int h = bmp.getHeight();
 		
 		//Log.d("altura",""+h);
 		/*
 		List<PersonOptions> persona2 = new ArrayList<PersonOptions>();
 		persona2.add(new PersonOptions(1));
 		items.add(persona2);
 		
 		List<PersonOptions> persona3 = new ArrayList<PersonOptions>();
 		persona3.add(new PersonOptions(1));
 		items.add(persona3);
 		*/
 		ExpandableListView exList = (ExpandableListView) findViewById(R.id.people_list);  
 		mAdapter = new PeopleExpandableListAdapter(context, cats, items);
 		exList.setIndicatorBounds(0, 20);
 		exList.setAdapter(mAdapter);
 	}
 	
 	/*Generate PNG bitmap from bitmap */
 	private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
 			int quality) {
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		src.compress(format, quality, os);
  
 		byte[] array = os.toByteArray();
 		return BitmapFactory.decodeByteArray(array, 0, array.length);
 	}
 	
 	/*Generate bitmap from string given from DB */
 	private static Bitmap getBitMap(String name){
 		
 		Bitmap bitmap;
		if(name == null || name.equals(""))
			return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.photo);
 		
 		byte[] decodedString;       
 
 		decodedString = Base64.decode(name, Base64.DEFAULT);
 		bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
 		return bitmap;
 	}
 	
 	
  
 }
 
