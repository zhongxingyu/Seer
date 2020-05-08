 package com.gakshay.android.edakia;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.StrictMode;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.gakshay.android.util.ActivitiesHelper;
 import com.gakshay.android.util.CustomDialog;
 import com.gakshay.android.util.NetworkOperations;
 import com.gakshay.android.validation.Validator;
 
 
 public class ReceiveActivity extends BaseActivity {
 
 	private ProgressDialog progressDialog;	
 	private String documentName;
 	private String documentPath;
 	private EditText mobile;
 	private EditText secretCode;
 	private EditText receiverEmailAddress;
 	private String docTransCost;
 	private String userBalance;
 	private static final int PRINT_ACTIVITY = 1;
 	private static final int FILE_DOWNLOAD_ACTIVITY = 2;
 	private String localEdakiaDocStorage; 
 	private String receiveURL ;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_receive);
 		StrictMode.ThreadPolicy policy = new StrictMode.
 				ThreadPolicy.Builder().permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 		((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
 		((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);
 		((ImageView)findViewById(R.id.errImgSecCode)).setVisibility(ImageView.INVISIBLE);
 		localEdakiaDocStorage = Environment.getExternalStorageDirectory().getAbsolutePath() + getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("localEdakiaDocStorage","/mnt/sdcard/");
 		receiveURL =  this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("receiveURL","http://defaultURL");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			Intent intent = new Intent(ReceiveActivity.this, Edakia.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void getDocument(View view) {
 
 		mobile = (EditText) findViewById(R.id.receiveMobile);
 		secretCode = (EditText) findViewById(R.id.secretCode);
 		receiverEmailAddress = (EditText) findViewById(R.id.receiverEmail);
 
 		if(validateInputData()){
 			try {// Process the request further.
 				readAndDownloadDocument(prepareEdakiaURL(mobile,secretCode,receiverEmailAddress));			
 			}
 			catch (Exception e) {
 				//handle the exception !
 				e.printStackTrace();
 			}
 		}
 
 
 	}
 
 	private String prepareEdakiaURL(EditText mobile, EditText secretCode, EditText emailAddress){
 		String edakiaURL = null;
 		//sample URL 
 		//http://staging.edakia.in/api/transactions/receive.xml?transaction[receiver_mobile]=<sender>&transaction[receiver_email]=<email>&transaction[document_secret]=<secret_code>&serial_number=<serial_number>
 		edakiaURL = receiveURL + "?transaction[receiver_mobile]=" + mobile.getText() + "&transaction[document_secret]="+secretCode.getText()
 				+ "&transaction[receiver_email]="+emailAddress.getText().toString() 
 				+ "&serial_number=" +getSerialNumber();	
 		return edakiaURL;
 	}
 
 	private void readAndDownloadDocument(final String reqURL) {
 		progressDialog = ProgressDialog.show(this, getString(R.string.receiveDocPrgDlgTitle), getString(R.string.receiveDocPrgDlg),true,false );
 
 		new Thread() {
 			public void run() {
 				Message msg = Message.obtain();
 				msg.what = 1;
 				try {
 					Bundle b = new Bundle();
 					b.putString("response", NetworkOperations.readXMLResponseFromEdakia(reqURL));
 					msg.setData(b);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				messageHandlerForXPath.sendMessage(msg);					
 			}
 		}.start();
 	}
 
 	private boolean checkFileExists(String filePath){
 		File isFile = new File(filePath);
 		return isFile.exists();
 	}
 	private boolean prepareThisUserDocumentFolder(){
 		boolean hasPrepared=false;
 
 		try {
 			File edakiaDocsHome = new File(localEdakiaDocStorage);
 			if(edakiaDocsHome.exists() && edakiaDocsHome.isDirectory()){
 				ActivitiesHelper.deleteContentOfFile(new File(localEdakiaDocStorage));
 			}else{
 				edakiaDocsHome.mkdirs();
 			}
 			hasPrepared = true;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return hasPrepared;
 	}
 
 	private Handler messageHandlerForXPath = new Handler() {
 
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			String responseXPath = msg.getData().getString("response");
 			TextView text = (TextView) findViewById(R.id.Error);
 			text.setText(null);
 			text.setVisibility(TextView.INVISIBLE);
 			//Fetch value of document x path from returned XML string response.
 			if(responseXPath != null && !"".equalsIgnoreCase(responseXPath) && !responseXPath.contains("error")){
 				try{
 					documentPath = (ActivitiesHelper.fetchValuesFromReponse(responseXPath)).get("document_url");
 					docTransCost = (ActivitiesHelper.fetchValuesFromReponse(responseXPath)).get("cost");
 					userBalance = (ActivitiesHelper.fetchValuesFromReponse(responseXPath)).get("balance");
 					documentName = (documentPath.split("/"))[documentPath.split("/").length-1];
 					if(!prepareThisUserDocumentFolder()){
 						throw new Exception("Error while preparing directories for user.");
 					}
 					//set success images :
 
 					((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_success);
 					((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_success);
 					((ImageView)findViewById(R.id.errImgSecCode)).setImageResource(R.drawable.ic_success);
 
 
 					Intent fileDownloadIntent = new Intent(ReceiveActivity.this, FileDownloadAsyncActivity.class);
 					fileDownloadIntent.putExtra("downloadURL", documentPath);
 					fileDownloadIntent.putExtra("filePath", localEdakiaDocStorage + documentName);
 
 					startActivityForResult(fileDownloadIntent,FILE_DOWNLOAD_ACTIVITY);
 
 					/*if(documentPath.contains(".png") || documentPath.contains(".jpeg") || documentPath.contains(".jpg") || documentPath.contains(".gif")){
 						//downloadImage(documentPath, true);
 						isFileCreated = NetworkOperations.readAndCreateImageDocumentFromEdakia(documentPath, localEdakiaDocStorage + documentName);
 					}else {
 						isFileCreated = NetworkOperations.readAndCreateAnyDocumentFromEdakia(documentPath, localEdakiaDocStorage + documentName);
 					}*/
 
 
 				}catch(Exception anExcep){
 					progressDialog.dismiss();
 					anExcep.printStackTrace();
 					//Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find your document due to some internal error. Please bear with us for some time to serve you again.", Toast.LENGTH_LONG).show();
 					text.setText(getString(R.string.receive_error));
 					text.setVisibility(TextView.VISIBLE);
 					((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
 					((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);
 					((ImageView)findViewById(R.id.errImgSecCode)).setVisibility(ImageView.INVISIBLE);
 				}
 				progressDialog.dismiss();
 			}else if(responseXPath != null && responseXPath.contains("error") && responseXPath.contains("not found")){
 				progressDialog.cancel();
 				progressDialog.dismiss();
 				//could not find any document with this.
 				//Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find document matching this secret code & mobile number. \n Please make sure you entered correct inputs.", Toast.LENGTH_LONG).show();
 				text.setText(getString(R.string.receive_failed));
 				text.setVisibility(TextView.VISIBLE);
 				((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
 				((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);
 				((ImageView)findViewById(R.id.errImgSecCode)).setVisibility(ImageView.INVISIBLE);
 			}
 			else {
 				progressDialog.dismiss();
 				progressDialog.cancel();
 				//some other error.
 				//Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find your document due to some internal error. Please bear with us for some time to serve you again.", Toast.LENGTH_LONG).show();
				//	text.setText(getString(R.string.receive_error));
				text.setText((ActivitiesHelper.fetchValuesFromReponse(responseXPath)).get("error"));
 				text.setVisibility(TextView.VISIBLE);
 				((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
 				((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);
 				((ImageView)findViewById(R.id.errImgSecCode)).setVisibility(ImageView.INVISIBLE);
 			}
 		}
 	};
 
 	private boolean validateInputData(){
 		boolean isValid = false;
 		//initialize error text value to null.
 
 		TextView text = (TextView) findViewById(R.id.Error);
 		text.setText(null);
 		text.setVisibility(TextView.INVISIBLE);
 
 		((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.VISIBLE);
 		((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.VISIBLE);
 		((ImageView)findViewById(R.id.errImgSecCode)).setVisibility(ImageView.VISIBLE);
 
 
 		//Either provide mobile no. or email address not both.
 		if(receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString())
 				&& mobile.getText().toString() != null && !"".equalsIgnoreCase(mobile.getText().toString())){
 
 			//Toast.makeText(this, "Please provide single input,either email address or mobile no.", Toast.LENGTH_LONG).show();
 			text.setText(getString(R.string.both_mobile_email));
 			text.setVisibility(TextView.VISIBLE);
 			return false;
 		}
 
 		if((receiverEmailAddress.getText().toString() == null || "".equalsIgnoreCase(receiverEmailAddress.getText().toString()))
 				&& (mobile.getText().toString() == null || "".equalsIgnoreCase(mobile.getText().toString()))){
 
 			if(secretCode.getText() == null || "".equalsIgnoreCase(secretCode.getText().toString()) || secretCode.getText().toString().length() == 0){
 				//Toast.makeText(this, "Please provide inputs.", Toast.LENGTH_LONG).show();
 				text.setText(getString(R.string.all_mobile_email_secret_code));
 				text.setVisibility(TextView.VISIBLE);
 				((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
 				((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_error);
 				((ImageView)findViewById(R.id.errImgSecCode)).setImageResource(R.drawable.ic_error);
 
 			}else{
 				//Toast.makeText(this, "Please provide inputs,either email address or mobile no. with secret code.", Toast.LENGTH_LONG).show();
 				text.setText(getString(R.string.both_mobile_email));
 				text.setVisibility(TextView.VISIBLE);
 				((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
 				((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_error);
 
 			}
 			return false;
 		}
 		int valStatusCode = -5;
 		if((receiverEmailAddress.getText() == null || "".equalsIgnoreCase(receiverEmailAddress.getText().toString()) || receiverEmailAddress.getText().toString().length() == 0)
 				&& (mobile.getText().toString() != null && !"".equalsIgnoreCase(mobile.getText().toString()) && mobile.getText().toString().length() != 0)){
 
 			((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);
 
 			//Validate mobile no.
 			valStatusCode = Validator.validateMobileNumber(mobile.getText().toString()).ordinal();
 			switch(valStatusCode){
 			case 1:
 				//Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
 				text.setText(getString(R.string.empty_mobile));
 				text.setVisibility(TextView.VISIBLE);
 				((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
 				mobile.findFocus();
 				return false;
 			case 2:
 				//Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
 				text.setText(getString(R.string.invalid_mobile));
 				text.setVisibility(TextView.VISIBLE);
 				mobile.findFocus();
 				((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
 				return false;		
 			}
 
 		}
 
 
 		if((mobile.getText() == null || "".equalsIgnoreCase(mobile.getText().toString()) || mobile.getText().toString().length() == 0)
 				&& (receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString()) && receiverEmailAddress.getText().toString().length() != 0)){
 
 			//invisible mobile error image.
 			((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
 
 			//Validate email address.
 			valStatusCode = Validator.validateEmailAddress(receiverEmailAddress.getText().toString()).ordinal();
 			switch (valStatusCode) {
 			case 7:
 				//Toast.makeText(this, "Incorrect Email Address",Toast.LENGTH_LONG).show();
 				text.setText(getString(R.string.invalid_email));
 				text.setVisibility(TextView.VISIBLE);
 				receiverEmailAddress.findFocus();
 				((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_error);
 
 				return false;
 			}
 		}
 
 		//set as success. if had been erroneous should have been caught before.
 		((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_success);
 		((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_success);
 
 		//Validate secret no.
 		valStatusCode = Validator.validateSecretNumber(secretCode.getText().toString()).ordinal();
 		switch(valStatusCode){
 		case 3:
 			//Toast.makeText(this, "Enter Secret Number", Toast.LENGTH_LONG).show();
 			text.setText(getString(R.string.empty_secret_code));
 			text.setVisibility(TextView.VISIBLE);
 			secretCode.findFocus();
 			((ImageView)findViewById(R.id.errImgSecCode)).setImageResource(R.drawable.ic_error);
 			return false;
 		case 4:
 			//Toast.makeText(this, "Incorrect Secret Code", Toast.LENGTH_LONG).show();
 			text.setText(getString(R.string.invalid_secret_code));
 			text.setVisibility(TextView.VISIBLE);
 			secretCode.findFocus();
 			((ImageView)findViewById(R.id.errImgSecCode)).setImageResource(R.drawable.ic_error);
 
 			return false;					
 		}
 
 
 		if(valStatusCode == 0){
 			isValid = true;
 			((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_success);
 			((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_success);
 			((ImageView)findViewById(R.id.errImgSecCode)).setImageResource(R.drawable.ic_success);
 		}	
 		return isValid;	
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		if(requestCode == PRINT_ACTIVITY){
 			Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
 			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			homeIntent.putExtra("showResultDialogBox", "true");
 			if(resultCode == RESULT_OK){
 				homeIntent.putExtra("transactionType", "received");
 				homeIntent.putExtra("paidAmount", docTransCost);
 				homeIntent.putExtra("userBalance", userBalance);
 				homeIntent.putExtra("transactionCost", "");
 				homeIntent.putExtra("isError", "false");
 			}else{
 				homeIntent.putExtra("isError", "true");
 			}
 			startActivity(homeIntent);
 			finish();
 
 		}else if(requestCode == FILE_DOWNLOAD_ACTIVITY){
 			if(resultCode == RESULT_OK){
 				if(checkFileExists(localEdakiaDocStorage+documentName)){
 					String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(documentPath)));
 
 					try {
 						Intent i = new Intent(Intent.ACTION_VIEW);
 						i.setPackage(getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("printerShareActivity","com.dynamixsoftware.printershare"));
 						i.setDataAndType(Uri.fromFile(new File(localEdakiaDocStorage + documentName)), mimeType);
 						startActivityForResult(i,PRINT_ACTIVITY);
 						Thread.sleep(3000);
 						//Show cost dialog for transaction charges.
 						(CustomDialog.resultCostDialog(ReceiveActivity.this,R.style.Theme_customDialogTitleTheme, R.layout.custom_title, R.layout.result_dialog_cost, R.id.TrnsButton,
 								R.id.TrnsResult,getString(R.string.costDialogPrinterShareMsg) + docTransCost)).show();
 
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						Toast.makeText(ReceiveActivity.this, "Exception in calling eDakia-Print" , Toast.LENGTH_LONG).show();
 						startActivity((new Intent(ReceiveActivity.this, Edakia.class)));
 					}
 				}else{
 					TextView text = (TextView) findViewById(R.id.Error);
 					text.setText(getString(R.string.receive_error));
 					text.setVisibility(TextView.VISIBLE);
 					((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
 					((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);
 					((ImageView)findViewById(R.id.errImgSecCode)).setVisibility(ImageView.INVISIBLE);
 				}
 			}else{
 				Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
 				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				homeIntent.putExtra("showResultDialogBox", "true");
 				homeIntent.putExtra("transactionType", "received");
 				homeIntent.putExtra("isError", "true");
 
 				startActivity(homeIntent);
 				finish();
 			}
 		}
 
 	}
 
 }
