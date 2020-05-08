 package spaceappschallenge.moonville.activities;
 
 import spaceappschallenge.moonville.R;
 import spaceappschallenge.moonville.managers.ApplicationService;
 import spaceappschallenge.moonville.managers.MoonBaseManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 public class MainMenuActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_mainmenu);
 		// The following line allows us to share the application context
 		// throughout the application
 		// Other classes can access the context through ApplicationService
 		Log.i("Main", "trying to get instance");
 		ApplicationService app = ApplicationService.getInstance();
 		Log.i("Main", "trying to get context");
 		app.setApplicationContext(this.getApplicationContext());
 	}
 
 	// methods called by onClick property of button in xml
 	public void showBaseOverviewScreen(View view) {
		MoonBaseManager.loadSavedMoonbase(this);
 		view.getContext().startActivity(
 				new Intent(this, BaseOverviewActivity.class));
 	}
 
 	public void showNewGameScreen(View view) {
 		view.getContext()
 				.startActivity(new Intent(this, NewGameActivity.class));
 	}
 
 	public void showCreditsScreen(View view) {
 		view.getContext()
 				.startActivity(new Intent(this, CreditsActivity.class));
 	}
 
 	public void exitGame(View view) {
 		finish();
 		System.exit(0);
 	}
 
 }
