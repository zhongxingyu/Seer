 package cz.android.monet.restexample.fragments;
 
 import java.util.List;

 import cz.android.monet.restexample.R;
 import cz.android.monet.restexample.SendUserIdAsyncTask;
 import cz.android.monet.restexample.interfaces.OnServerResultReturned;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class InputFragment extends Fragment {
 
 	private static final String TAG = "InputFragment";
 
 	private EditText host;
 	private EditText sendData;
 	OnServerResultReturned mResultCallback;
 
 	// The resul code
 	static final int RESULT_OK = -1;
 
 	// The request code
 	static final int PICK_CONTACT_REQUEST = 1;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		host = ((EditText) getView().findViewById(R.id.editHostAddress));
 		sendData = (EditText) getView().findViewById(R.id.editSendData);
 		
 		 // Get the intent that started this activity
 	    Intent intent = getActivity().getIntent();
 	    Uri data = intent.getData();
 	    
 	    if (data != null && intent.getType().equals("text/plain")) {
 	    	host.setText(data.getHost().toString());
 	    }
 	    else
 	    {
 	    	host.setText("193.33.22.109");
 	    }
 
 		sendData.setText("1");
 
 		Button butSend = (Button) getView().findViewById(R.id.btnSend);
 		butSend.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				new SendUserIdAsyncTask().execute(host.getText().toString(),
 						sendData.getText().toString(), mResultCallback);
 			}
 
 		});
 
 		Button butTest = (Button) getView().findViewById(R.id.btnReadBarCode);
 		butTest.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri
 						.parse("content://contacts"));
 				pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only
 																// contacts w/
 																// phone numbers
 
 				PackageManager packageManager = getActivity()
 						.getApplicationContext().getPackageManager();
 				List<ResolveInfo> activities = packageManager
 						.queryIntentActivities(pickContactIntent, 0);
 				boolean isIntentSafe = activities.size() > 0;
 
 				if (isIntentSafe) {
 					startActivityForResult(pickContactIntent,
 							PICK_CONTACT_REQUEST);
 				} else {
 					Log.e(TAG, "Activity pickContactIntent isn't safe.");
 				}
 			}
 		});
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 		return inflater.inflate(R.layout.input, container, false);
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		// This makes sure that the container activity has implemented
 		// the callback interface. If not, it throws an exception
 		try {
 			mResultCallback = (OnServerResultReturned) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnServerResultReturned");
 		}
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check which request we're responding to
 		switch (requestCode) {
 		case PICK_CONTACT_REQUEST:
 			// Make sure the request was successful
 			if (resultCode == RESULT_OK) {
 				// Get the URI that points to the selected contact
 				Uri contactUri = data.getData();
 				// We only need the NUMBER column, because there will be only
 				// one row in the result
 				String[] projection = { Phone.NUMBER };
 
 				// Perform the query on the contact to get the NUMBER column
 				// We don't need a selection or sort order (there's only one
 				// result for the given URI)
 				// CAUTION: The query() method should be called from a separate
 				// thread to avoid blocking
 				// your app's UI thread. (For simplicity of the sample, this
 				// code doesn't do that.)
 				// Consider using CursorLoader to perform the query.
 				Cursor cursor = getActivity().getContentResolver().query(
 						contactUri, projection, null, null, null);
 				cursor.moveToFirst();
 
 				// Retrieve the phone number from the NUMBER column
 				int column = cursor.getColumnIndex(Phone.NUMBER);
 				String number = cursor.getString(column);
 
 				// Do something with the phone number...
 				mResultCallback.onResultReturned(number);
 				sendData.setText(number);
 
 			}
 			break;
 		}
 	}
 }
