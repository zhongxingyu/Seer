 /**
  * 
  */
 package puzzlingbytes.se.app.abstractparcelable;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 
 /**
  * @author Mikael Olsson
  * 
  */
 public class ReceiverActivity extends Activity {
 
 	private static final String TAG = ReceiverActivity.class.getSimpleName();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Intent intent = getIntent();
 
 		if (intent != null && intent.getExtras() != null) {
 			Parcelable[] parcelables = intent
 					.getParcelableArrayExtra(MainActivity.EXTRA_ITEMS);
 
 			Log.d(TAG, "Num parcelables: " + parcelables.length);
 
 			for (Parcelable parcelable : parcelables) {
 				if (parcelable != null) {
					Log.d(TAG, "Parcelable Class: "
 							+ parcelable.getClass().getSimpleName());
 					if (parcelable instanceof ItemTypeOne) {
 						ItemTypeOne itemTypeOne = (ItemTypeOne) parcelable;
 						Log.d(TAG,
 								"Class = " + ItemTypeOne.class.getSimpleName());
 						Log.d(TAG, itemTypeOne.toString());
 					} else if (parcelable instanceof ItemTypeTwo) {
 						ItemTypeTwo itemTypeTwo = (ItemTypeTwo) parcelable;
 						Log.d(TAG,
 								"Class = " + ItemTypeTwo.class.getSimpleName());
 						Log.d(TAG, itemTypeTwo.toString());
 					} else {
 						Log.d(TAG, "Class Unknown");
 					}
 				} else {
 					Log.d(TAG, "Parcelable == NULL");
 				}
 
 			}
 		}
 
 	}
 }
