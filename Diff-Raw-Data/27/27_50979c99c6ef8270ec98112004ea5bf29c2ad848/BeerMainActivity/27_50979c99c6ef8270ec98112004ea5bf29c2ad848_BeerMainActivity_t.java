 package eu.silvere;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class BeerMainActivity extends Activity {
 
 	private BeerBottleLoader mBeerBottleLoader;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.beer_main_activity_view);
 
 		mBeerBottleLoader = new BeerBottleLoader(getResources());
 		mBeerBottleLoader.start();
		Log.d("Beer", "Beer" + "Loading bottles");
 
 		Button mLaunchButton = (Button) findViewById(R.id.launch);
 
 		mLaunchButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Context ctx = getApplicationContext();
 				Intent intent = new Intent(ctx, BeerListActivity.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 				Bundle bundle = new Bundle();
 
 				bundle.putParcelableArrayList("bottle_list", mBeerBottleLoader.getBottles());
 
 				intent.putExtras(bundle);
 
 				ctx.startActivity(intent);
 			}
 		});
 	}
 
 }
