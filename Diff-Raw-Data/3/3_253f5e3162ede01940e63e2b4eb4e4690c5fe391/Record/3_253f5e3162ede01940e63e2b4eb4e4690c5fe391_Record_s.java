 package com.example.gifting;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class Record extends Activity implements OnClickListener{
 
 	private static final int ACTION_TAKE_VIDEO = 3;
 
 
 
 	Button b1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_record);
 
 		PackageManager pm = this.getPackageManager();
 		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
 			// create an alert dialog
 			alertbox("Error", "You Do Not Have A Camera");
 			
 		}else{
 			b1=(Button)findViewById(R.id.button1);
 			b1.setOnClickListener(this);
			fdispatchTakeVideoIntent();

 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_record, menu);
 		return true;
 	}
 
 	private void dispatchTakeVideoIntent() {
 		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 		startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
 	}
 
 
 	protected void alertbox(String title, String mymessage) {
 		new AlertDialog.Builder(this)
 				.setMessage(mymessage)
 				.setTitle(title)
 				.setCancelable(true)
 				.setNeutralButton(android.R.string.cancel,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								finish();
 							}
 						}).show();
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case R.id.button1:
 			//basically the same as before but sends an empty string for the address and sends the string containing all previous searches
 			dispatchTakeVideoIntent();
 			break;
 		}
 	}
 
 }
