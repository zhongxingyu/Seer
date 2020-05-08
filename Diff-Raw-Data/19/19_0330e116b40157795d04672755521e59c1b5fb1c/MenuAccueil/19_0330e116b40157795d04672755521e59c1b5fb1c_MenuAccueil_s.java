 package com.example.nuitinfogroupe2asobo;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 
 public class MenuAccueil extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_accueil);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	public void choixEntrees(View view)
 	{
 		Intent intent = new Intent(this, ChoixActivity.class);
 		intent.putExtra("param", "entrée");
 		startActivity(intent);
 	}
 	
 	public void choixPlats(View view)
 	{
 		Intent intent = new Intent(this, ChoixActivity.class);
		intent.putExtra("param", "plats");
 		startActivity(intent);
 	}
 	
 	public void choixDesserts(View view)
 	{
 		Intent intent = new Intent(this, ChoixActivity.class);
		intent.putExtra("param", "desserts");
 		startActivity(intent);
 	}
 	
	public void chsoixSpecialitees(View view)
 	{
 		Intent intent = new Intent(this, ChoixActivity.class);
		intent.putExtra("param", "specialitees");
 		startActivity(intent);
 	}
 
 }
