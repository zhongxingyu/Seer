 package com.gakshay.android.edakia;
 
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.Toast;
 
 
 public class Edakia extends Activity {
 
 	private static final int ACTIVITY_CHOOSE_FILE = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		String showCostDialog = "false";
		if(getIntent() != null && getIntent().getExtras() != null)
 			showCostDialog = (String)getIntent().getExtras().get("showCostDialogBox");
		
		if("true".equalsIgnoreCase(showCostDialog))
			prepareCostDialogBox();
 	}
 
 	public void optionClickHandler(View view) {
 		switch(view.getId()){
 		case R.id.optionReceive:
 			Intent receiveIntent = new Intent(Edakia.this, ReceiveActivity.class);
 			Edakia.this.startActivity(receiveIntent);
 			break;
 		case R.id.optionSend:
 			Intent authenticateIntent = new Intent(Edakia.this, AuthenticateActivity.class);
 			Edakia.this.startActivity(authenticateIntent);
 			break;
 		case R.id.optionPrint:
 			Intent chooseFile,intent;
 			chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
 			chooseFile.setType("file/*");
 			intent = Intent.createChooser(chooseFile, "Choose a file");
 			startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
 			break;	
 		default:
 			break;
 		}
 	}
	
 	/** Called when an activity called by using startActivityForResult finishes. */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		if(requestCode == ACTIVITY_CHOOSE_FILE && resultCode == RESULT_OK){
 			Uri uri = data.getData();
 			Toast.makeText(this, "File selected to send :  " + uri.getPath(), Toast.LENGTH_LONG).show();
 			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(uri.getPath())));
 			try {
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setPackage("com.dynamixsoftware.printershare");
 				i.setDataAndType(Uri.fromFile(new File(uri.getPath())), mimeType);
 				startActivity(i);
 
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				Toast.makeText(this, "Invoke to printer share App didn't work !!  \n Going to Home Page" , Toast.LENGTH_LONG).show();
 			}
 
 
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		super.onBackPressed();
 		finish();
 	}
 
 	private void prepareCostDialogBox(){
 		AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
 		//set the message on d	ialog.
 		Intent intent = getIntent();
 		Bundle bundleData = intent.getExtras();
 		String tranctTitle;
 		if("received".equalsIgnoreCase((String) bundleData.get("transactionType")))
 			tranctTitle = "Received Successfully";
 		else 
 			tranctTitle = "Sent Successfully";
 
 		String tranctMsg = (String) bundleData.get("transactionMsg");
 		String dialogMessage = tranctTitle + "\n\n" + tranctMsg;
 		altDialog.setMessage(dialogMessage); // here add your message
 		altDialog.setCancelable(false);
 		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				//do nothing.
 			}
 		});
 		altDialog.show();
 	}
 }
