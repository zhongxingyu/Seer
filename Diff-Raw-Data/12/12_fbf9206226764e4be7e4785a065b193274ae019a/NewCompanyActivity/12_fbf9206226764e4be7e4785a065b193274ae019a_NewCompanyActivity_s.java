 /*
  * Starts the game. Initializes managers based on user choices.
  */
 package spaceappschallenge.moonville.activities;
 
 import spaceappschallenge.moonville.R;
 import spaceappschallenge.moonville.managers.MoonBaseManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 
 public class NewCompanyActivity extends Activity
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_new_game);
 
 		initModel();
 	}
 	
 	
 	
 	protected void initModel()
 	{
		//create the new moonbase
		MoonBaseManager.createNewMoonBase( 0, 0, 40000000 ); //$40.000.000.000, but it's an int... so per thousand :)
 	}
 	
 
 	//I changed the init method to this one, which is triggered by the onClick property in the xml of the button
 	//since we are using separate activities, this is a very easy way to implement navigation IMO.
 	public void showBaseOverviewScreen(View view)
 	{	
 		view.getContext().startActivity( new Intent( this, BaseOverviewActivity.class ) );
 		this.finish();
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_new_company, menu);
 		return true;
 	}
 
 }
