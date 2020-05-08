 package cs309.a1.player.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import cs309.a1.shared.activities.DeviceListActivity;
 
 public class ConnectActivity extends Activity{
 
 	private static int DEVICE_LIST_RESULT = Math.abs(DeviceListActivity.class.getName().hashCode());
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Show the device list
 		Intent showDeviceList = new Intent(this, DeviceListActivity.class);
 		startActivityForResult(showDeviceList, DEVICE_LIST_RESULT);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == DEVICE_LIST_RESULT && resultCode != RESULT_CANCELED) {
 			// TODO: start connecting to device, then show the cards screen
 			Intent showCards = new Intent(this, ShowCardsActivity.class);
 			startActivity(showCards);
 
			setResult(RESULT_OK);
			finish();
 		} else {
 			// The user cancelled out of the device list, so return them to the main menu
 			setResult(RESULT_CANCELED);
 			finish();
 		}
 	}
 }
