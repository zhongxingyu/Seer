 package com.example.boox;
 
 import listasLibros.GestorListas;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AddNewListActivity extends Activity {
 	
 	final int mode = Activity.MODE_PRIVATE;
 	public static final String myPrefs = "prefs";
 	String uname = "";
 	Context context = this;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_list);
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
 
 		SharedPreferences mySharedPreferences = getSharedPreferences(myPrefs, mode);
 		uname = mySharedPreferences.getString("username", "");
 	
 	
 	}
 
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case android.R.id.home:
             // This is called when the Home (Up) button is pressed
             // in the Action Bar.
             Intent parentActivityIntent = new Intent(this, TabsActivity.class);
             parentActivityIntent.addFlags(
                     Intent.FLAG_ACTIVITY_CLEAR_TOP |
                     Intent.FLAG_ACTIVITY_NEW_TASK);
             finish(); 
             return true;
     	}
     	return false;
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_search_friend, menu);
 		return true;
 	}
 	
     public void onPressAdd(View view) {
 
 		EditText et = (EditText) findViewById(R.id.add_list);
 		GestorListas gl = new GestorListas(uname, AddNewListActivity.this);
 		String nlista = et.getText().toString();
 		if(nlista.equals("All") || nlista.equals("Crossing List") || nlista.equals("Favoritos")){
 			Toast t = Toast.makeText(this.getApplicationContext(), "Nombre invalido" ,Toast.LENGTH_SHORT);
 			t.show();
 			return;
 		}
 		boolean added = false;
 		added = gl.AddListaVacia(nlista);
 		if(added = false){
 			Toast t = Toast.makeText(this.getApplicationContext(), "Esa lista ya existe" ,Toast.LENGTH_SHORT);
 			t.show();
 			return;
 		}
 		Toast t = Toast.makeText(this.getApplicationContext(), "Lista creada" ,Toast.LENGTH_SHORT);
 		t.show();
    	Intent intent = new Intent();
        intent.setClass(context, TabsActivity.class);
        intent.putExtra("addlist", nlista);
        startActivity(intent);
     }
  
 }
