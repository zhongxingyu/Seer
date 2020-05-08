 package jones.code.scenario;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import jones.code.AppExtender;
 import jones.code.QLPickupItemType;
 import jones.code.R;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class QLScenarioShowItemsActivity extends Activity {
 	
 	@SuppressLint({ "ParserError", "ParserError" })
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.qlmemorygenerator);
 		
 		AppExtender share = (AppExtender) getApplication();			
		
		if (share.scenarioInfo == null) {
			setResult(RESULT_CANCELED);
			finish();
		}
	
 		updatePickupItemDisplay(share.currentPickupItem);
 
 		new Timer().schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						ImageView iv = (ImageView) findViewById(R.id.pickup_icon);
 						iv.setVisibility(2);
 						TextView t1 = (TextView) findViewById(R.id.pickupTime);
 						t1.setText("Time is up");
 					}
 				});
 			}
 		}, share.scenarioInfo.getTimeoutItemVisible());
 
 		new Timer().schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						setResult(RESULT_OK);
 						finish();
 					}
 				});
 			}
 		}, share.scenarioInfo.getTimeoutBetweenSpawns());
 	}
 	
 	private void updatePickupItemDisplay(PickupItem pickupItem) {
 		updateIcon(pickupItem.getItemType());
 		updateTextView(pickupItem.getPickupTime());
 	}
 	
 	private void updateIcon(QLPickupItemType itemType) {
 		ImageView iv = (ImageView) findViewById(R.id.pickup_icon);
 		
 		switch (itemType) {
 		case MH: 
 			iv.setImageResource(R.drawable.ic_menu_mh);
 			break;
 		case RA: 
 			iv.setImageResource(R.drawable.ic_menu_ra);
 			break;
 		}
 		iv.refreshDrawableState();
 	}
 	
 	private void updateTextView(int pickupTime) {
 		TextView t = (TextView) findViewById(R.id.pickupTime);
 		
 		String itemTime = Integer.toString(pickupTime);
 		
 		if (itemTime.length() == 1) {
 			itemTime = "0" + itemTime;
 		}
 		
 		String textToDisplay = "XX:" + itemTime;
 	    t.setText(textToDisplay);
 	}
 }
