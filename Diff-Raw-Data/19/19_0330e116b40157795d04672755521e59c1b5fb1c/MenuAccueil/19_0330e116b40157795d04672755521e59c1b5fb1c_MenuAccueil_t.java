 package com.example.nuitinfogroupe2asobo;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
import android.widget.Toast;
 
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
		intent.putExtra("param", "plat+principal");
 		startActivity(intent);
 	}
 	
 	public void choixDesserts(View view)
 	{
 		Intent intent = new Intent(this, ChoixActivity.class);
		intent.putExtra("param", "dessert");
 		startActivity(intent);
 	}
 	
	public void choixMenu(View view)
 	{
 		Intent intent = new Intent(this, ChoixActivity.class);
		intent.putExtra("param", "Menu");
 		startActivity(intent);
 	}
	
	public void choixLog(View view)
	{
		Toast.makeText(getApplicationContext(), "À faire !", Toast.LENGTH_SHORT).show();
	}
 
 }
