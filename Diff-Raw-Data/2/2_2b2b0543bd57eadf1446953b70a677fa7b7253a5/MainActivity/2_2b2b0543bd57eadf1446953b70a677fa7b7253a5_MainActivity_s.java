 package com.example.myfirstapp;
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 public class MainActivity extends ListActivity {
 	private CharacterDataSource datasource;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         datasource = new CharacterDataSource(this);
 	    datasource.open();
 
 	    List<Character> values = datasource.getAllCharacters();
 
 	    // Use the SimpleCursorAdapter to show the
 	    // elements in a ListView
 	    ArrayAdapter<Character> adapter = new ArrayAdapter<Character>(this,
 	        android.R.layout.simple_list_item_1, values);
 	    setListAdapter(adapter);
     }
 
  // Will be called via the onClick attribute
  	// of the buttons in main.xml
  	public void onClick(View view) {
  	  @SuppressWarnings("unchecked")
  	  ArrayAdapter<Character> adapter = (ArrayAdapter<Character>) getListAdapter();
 	  if(adapter.getCount()<0){
  		  Character character = datasource.createCharacter("Danny McSizzle");
  		  adapter.add(character);
  		  adapter.notifyDataSetChanged();
  	  }else{
  		  while(adapter.getCount() > 1){
  			  Character character = (Character)getListAdapter().getItem(0);
  			  datasource.deleteCharacter(character);
  			  adapter.remove(character);
  		  }
  	  }
  	}
 
  	@Override
  	protected void onResume() {
  	  datasource.open();
  	  super.onResume();
  	}
 
  	@Override
  	protected void onPause() {
  	  datasource.close();
  	  super.onPause();
  	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
