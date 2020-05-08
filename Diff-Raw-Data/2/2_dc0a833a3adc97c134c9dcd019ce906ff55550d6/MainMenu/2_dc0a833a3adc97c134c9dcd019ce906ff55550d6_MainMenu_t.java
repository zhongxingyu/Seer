 package uk.ac.cam.groupproj.racethewild;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 
 public class MainMenu extends Activity {
 
 	// public final static String ENGINE_MESSAGE =
 	// "uk.ac.cam.groupproj.racethewild.ENGINE";
 
 	Engine engine;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_menu);
		if (Engine.get() == null) Engine.initEngine(getResources()); // Initialise the engine.
 		engine = Engine.get();
 	}
 
 	public void moveToCollection(View view) {
 		Intent intent = new Intent(this, AnimalCollection.class);
 
 		// intent.putExtra(ENGINE_MESSAGE, engine); //for when we start sending
 		// around Engine.
 		startActivity(intent);
 	}
 
 	public void moveToNodeMap(View view) {
 		Intent intent = new Intent(this, NodeScene.class); 
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 		return true;
 	}
 
 }
