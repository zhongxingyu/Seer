 package com.eceapp.sasel;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
 import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
 import com.google.api.client.http.FileContent;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.drive.Drive;
 import com.google.api.services.drive.Drive.Files;
 import com.google.api.services.drive.Drive.Files.Get;
 import com.google.api.services.drive.DriveScopes;
 import com.google.api.services.drive.model.File;
 import com.google.api.services.drive.model.FileList;
 
 public class Preparation extends Activity {
 	public static final String SERVERS_KEY = "servers";
 
 	public static final int REQUEST_ACCOUNT_PICKER = 0xd;
 	public static final int REQUEST_AUTHORIZATION  = 0xf;
 	public static final String DEFAULT_ENCRYPTED_FILENAME = "serverManager.ece";
 	
 	public static final int EOF = -1;
 	public static final int NO_PREVIOUS_FILE_DETECTED = 0;
 	public static final int DUPLICATE_FILES_DETECTED = 1;
 	public static final int SINGLE_VALID_FILE_DETECTED = 2;
 	public static final int MAIN_SERVERS_POPULATED = 3;
 	public static final int CHANGING_ENCRYPTION_KEYS = 4;
 	public static final int CHANGING_ENCRYPTION_KEYS_FOR_UPLOAD = 5;
 	
 	public static final String RETURNED_SERVERS_KEY = "returnedServers";
 	public static final String GOOGLE_ACCOUNT_NAME = "googleAccountName";
 	public static final String IV_KEY = "ivKey";
 	public static final String ENCRYPTION_KEY = "encryptionKey";
 	public static final String SERVER_MAP	= "serverMap";
 
 	private static final String DEV_IV_KEY = "1234567890123456";
 	private static final String DEV_ENCRYPTION_KEY = "1234567890123456";
 	private Handler encryptionCredentialHandler;
 	private Handler dataSpinnerHandler;
 	private GoogleAccountCredential credential = null;
 	private Uri fileUri;
 	private Drive service = null;
 	private CipherAlgo cypherAl = null;
 	private String ivKey;
 	private String encryptionKey;
 	private HashMap<String,Server> serverMap = new HashMap<String,Server>();
 	
 	
 	/*Defining messages here*/
 	public static final String INVALID_ENCRYPTION_CREDENTIALS = "Invalid encryption credentials";
 	
 	public static final String UPLOAD_COMPLETE = "uploadComplete";
 	public static final String UPLOAD_STARTED = "uploadStarted";
 	public static final String UPLOAD_FAILED = "uploadFailed";
 	
 	public static final String DOWNLOAD_STARTED = "downloadStarted";
 	public static final String DOWNLOAD_COMPLETE = "downloadComplete";
 	public static final String DOWNLOAD_FAILED = "downloadFailed";
 	
 	public static final String TURN_OFF_SPINNER = "turnOffSpinner";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preparation);
 
 		/*
 		//Handling restart changes eg screen rotation, deprecated as of yesterday Wednesday 28th August 2013 -- 5:40PM
 		final MapCredentialSaver savedMapCreds = (MapCredentialSaver)getLastNonConfigurationInstance();
 		if (savedMapCreds != null){
 			credential = savedMapCreds.theCredential;
 			serverMap = savedMapCreds.theServerMap;
 			ivKey = savedMapCreds.theIvKey;
 			encryptionKey = savedMapCreds.theEncryptionKey;
 			
 			Button googleAccountButton = (Button) findViewById(R.id.googleAccountButton);
 			googleAccountButton.setError(null);
 			googleAccountButton.setText("Selected account: "+credential.getSelectedAccountName());
 			
 			
 			EditText iv_ET=(EditText)findViewById(R.id.ivKey);
 			iv_ET.setText(ivKey);
 			EditText encKey_ET =(EditText)findViewById(R.id.encryptionKey);
 			encKey_ET.setText(encryptionKey);
 			toggleSpinner(false);
 			return;
 		}
 		*/
 		
 		EditText iv_ET=(EditText)findViewById(R.id.ivKey);
 		iv_ET.setText(DEV_IV_KEY);
 		EditText encKey_ET =(EditText)findViewById(R.id.encryptionKey);
 		encKey_ET.setText(DEV_ENCRYPTION_KEY);
 
 		encryptionCredentialHandler = new Handler(){
 			@Override
 			public void handleMessage(Message msg){
 				String inMsg=(String)msg.obj;
 				if (inMsg.equals(getString(R.string.invalid_encryption_credentials))){
 					EditText iv_et=(EditText)findViewById(R.id.ivKey);
 					int ivLen = iv_et.length();
 
 					EditText encKey_et=(EditText)findViewById(R.id.encryptionKey);
 					int encKeyLen = encKey_et.length();
 					boolean keyLenMatch = true;
 					if(ivLen != CipherAlgo.REQ_IV_LEN){
 						int overFlow = ivLen-CipherAlgo.REQ_IV_LEN;
 					
 						String prefixQuantity = overFlow > 0 ? "less" : "more";
 						iv_et.setError(Math.abs(overFlow)+" "+prefixQuantity+" characters");
 						keyLenMatch = false;
 					}
 
 					if (encKeyLen != CipherAlgo.REQ_ENCRYPTION_LEN){
 						int overFlow = encKeyLen-CipherAlgo.REQ_IV_LEN;
 						
 						String prefixQuantity = overFlow > 0 ? "less" : "more";
 						encKey_et.setError(Math.abs(overFlow)+" "+prefixQuantity+" characters");
 						keyLenMatch = false;
 					}
 					
 					if (keyLenMatch){
 						iv_et.setError(getString(R.string.invalid)+" ivKey");
 						encKey_et.setError(getString(R.string.invalid)+" encryptionKey");
 					}
 					toggleSpinner(false);
 				}
 			}
 		};
 		
 		dataSpinnerHandler = new Handler(){
 			@Override
 			public void handleMessage(Message msg){
 				String inMsg=(String)msg.obj;
 				if (inMsg.equals(UPLOAD_STARTED) || inMsg.equals(DOWNLOAD_STARTED)){
 					toggleSpinner(true);
 				}else if (inMsg.equals(UPLOAD_COMPLETE) || inMsg.equals(DOWNLOAD_COMPLETE)){
 					toggleSpinner(false);
 				}else if (inMsg.equals(TURN_OFF_SPINNER)){
 					toggleSpinner(false);
 				}else if (inMsg.equals(DOWNLOAD_FAILED) || inMsg.equals(UPLOAD_FAILED)){
 					toggleSpinner(false);
 				}
 			}
 		};
 	}
 	
 	public  void triggerAccountPick(){
 		credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
 		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
 	}
 	
 	public void selectNewAccount(View v){
 		triggerAccountPick();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.preparation, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		switch(requestCode){
 		case REQUEST_ACCOUNT_PICKER:
 			System.out.println("ACCOUNT_PICKER_REQUESTED");
 			if ((resultCode == RESULT_OK) && (data != null) && (data.getExtras() != null)){
 				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
 
 				if (accountName != null){
 					credential.setSelectedAccountName(accountName);
 					service = getDriveService(credential);
 					toggleSpinner(false);
 					Button googleAccountButton = (Button) findViewById(R.id.googleAccountButton);
 					googleAccountButton.setError(null);
 					googleAccountButton.setText("Selected account: "+accountName);
 					
 				}else{
 					//User denied access, show them the account chooser again
 					startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
 				}
 			}
 			break;
 			
 		case REQUEST_AUTHORIZATION:
 			startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
 			break;
 			
 		case MAIN_SERVERS_POPULATED:
 			System.out.println("Received data from main_servers_populated ");
 			
 			System.out.println("Result code "+(resultCode == RESULT_OK));
 			if ((resultCode == RESULT_OK) && (data != null)){
 				Bundle extras = data.getExtras();
 				if (extras != null){
 					System.out.println("extras "+extras);
 					Object returnedData = extras.get(RETURNED_SERVERS_KEY);
 					if (returnedData != null){
 						serverMap = (HashMap<String, Server>)returnedData;
 						
 						AlertDialog.Builder builder = new AlertDialog.Builder(this);
 						final Context dataContext = this;
 						builder.setTitle("Change IV & Password? ");  
 						
 						builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								uploadToDrive();
 							}
 						});
 						
 						builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								Intent changeKeysIntent = new Intent(dataContext, IvSetEncryptionKeyActivity.class);
 								startActivityForResult(changeKeysIntent,CHANGING_ENCRYPTION_KEYS_FOR_UPLOAD);
 							}
 						});
 						
 						AlertDialog myKeysDialog = builder.create();
 						myKeysDialog.show();
 					}
 				}
 			}
 			toggleSpinner(false);
 			break;
 
 		case CHANGING_ENCRYPTION_KEYS_FOR_UPLOAD:
 			if ((resultCode == RESULT_OK) && (data != null)){
 				Bundle extras = data.getExtras();
 				encryptionKey = extras.getString(ENCRYPTION_KEY);
 				ivKey = extras.getString(IV_KEY);
 				EditText iv_ET=(EditText)findViewById(R.id.ivKey);
 				iv_ET.setText(ivKey);
 				EditText encKey_ET =(EditText)findViewById(R.id.encryptionKey);
 				encKey_ET.setText(encryptionKey);
 				showToast("IV and Encryption Keys changed");
 				uploadToDrive();
 			}
 			break;
 
 	
 		default:
 			break;
 		}
 	}
 	
 	public boolean allKeyLensValid(String encryptionKey, String ivKey){
 		return (encryptionKey != null) && (ivKey != null) 
 				&& (encryptionKey.length() == CipherAlgo.REQ_ENCRYPTION_LEN) 
 				&& (ivKey.length() == CipherAlgo.REQ_IV_LEN);
 	}
 	
 	public void openSASELBook(View v){
 		EditText et=(EditText)findViewById(R.id.ivKey);
 		ivKey=et.getText().toString();
 
 		et=(EditText)findViewById(R.id.encryptionKey);
 		encryptionKey=et.getText().toString();
 		
 		if (! allKeyLensValid(encryptionKey, ivKey)){
 			Message invalidKeyMsg = new Message();
 			invalidKeyMsg.obj = getString(R.string.invalid_encryption_credentials);
 			encryptionCredentialHandler.sendMessage(invalidKeyMsg);
 		}else{
 			if (credential != null){
 				getDecryptedFile();
 			}else{
 				showToast("Select account first");
 				Button selectAccountButton = (Button)findViewById(R.id.googleAccountButton);
 
 				selectAccountButton.setError(getString(R.string.select_account));
 				toggleSpinner(false);
 			}
 		}
		
 		/*
 		Intent ssPIntent = new Intent(this, SingleServerPage.class);
 		ssPIntent.putExtra(MainMenu.SINGLE_SERVER_DATA, new Server());
 		startActivityForResult(ssPIntent, EOF);
 		*/
 	}
 	
 	//Helper function for validating input
 	public boolean validatePattern(String target, String validatorRegex){
 		if (target == null || validatorRegex == null) return false;
 		Pattern regComp = Pattern.compile(validatorRegex);
 		return regComp.matcher(target).find();
 	}
 
 	private void manageServers(boolean dataExists){
 		final Context dataContext = this;
 		if (dataExists){
 			Intent intent = new Intent(dataContext, MainMenu.class);
 			intent.putExtra(SERVER_MAP, this.serverMap);
			intent.putExtra(GOOGLE_ACCOUNT_NAME, credential.getSelectedAccountName());
			
 			startActivityForResult(intent, MAIN_SERVERS_POPULATED);
 		}else{
 			Looper.prepare();
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			
 			builder.setTitle("No data exists on account: "+credential.getSelectedAccountName());  
 			builder.setNegativeButton(R.string.create_new_server, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Intent intent = new Intent(dataContext, MainMenu.class);
 					intent.putExtra(SERVER_MAP, serverMap);
 					intent.putExtra(GOOGLE_ACCOUNT_NAME, credential.getSelectedAccountName());
 					
 					startActivityForResult(intent, MAIN_SERVERS_POPULATED);
 				}
 			});
 			
 			builder.setPositiveButton(R.string.select_new_account, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					triggerAccountPick();
 				}
 			});
 			
 			builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Message msg = new Message();
 					msg.obj = TURN_OFF_SPINNER;
 					dataSpinnerHandler.sendMessage(msg);
 					return; 
 				}
 			});
 			
 			AlertDialog newAccountDialog = builder.create();
 			newAccountDialog.show();
 
 			Looper.loop();
 		}
 	}
 
 	public void toggleSpinner(boolean showSpinner){
 		int startButtonVisibility = View.VISIBLE;
 		int progressSpinnerVisibility = View.INVISIBLE;
 		if (showSpinner){
 			startButtonVisibility = View.INVISIBLE;
 			progressSpinnerVisibility = View.VISIBLE;
 		}
 		
 		View startButton = findViewById(R.id.sign_in_button);
 		startButton.setVisibility(startButtonVisibility);
 		
 		View progressBar  = findViewById(R.id.preparationProgressBar);
 		progressBar.setVisibility(progressSpinnerVisibility);
 	}
 	
 	private Drive getDriveService(GoogleAccountCredential credential){
 		return new Drive.Builder( 
 		   AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential
 		   ).build();
 	}
 
 	public void showToast(final String toast){
 		runOnUiThread( new Runnable(){
 			@Override
 			public void run(){
 				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	//Single-thread based function that Downloads off your drive the first file 
 	//whose name matches our encrypted file. Once downloaded, the file is then decrypted
 	private void getDecryptedFile(){
 		Thread downloader_th = new Thread( new Runnable(){
 			@Override
 			public void run(){
 				Message dlStatusMsg = new Message();
 		
 				dlStatusMsg.obj = DOWNLOAD_STARTED;
 			
 				dataSpinnerHandler.sendMessage(dlStatusMsg);
 				try{
 					java.util.List <File>result = new ArrayList<File>();
 					Files.List request = service.files().list().setQ("title = '"+DEFAULT_ENCRYPTED_FILENAME+"'");
 					FileList files = request.execute();	
 					result.addAll(files.getItems());
 
 					System.out.println("Number of files  "+ result.size());
 					if (result.size() > 0){
 						Get fileMetaData = (service.files()).get((String)result.get(0).get("id"));
 						File file = fileMetaData.execute();
 						System.err.println("Title: "+file.getTitle());
 						System.err.println("Description: "+file.getDescription());
 						System.err.println("Mime type :"+file.getMimeType());
 						System.err.println("file's get request "+fileMetaData.entrySet());
 						System.err.println("FILE ID "+file.getId());
 	
 						//The file's content as downloaded
 						InputStream fileStream = downloadFile(service, file);
 	
 						if (fileStream != null){
 							String buffer = "";
 							int bufInt=0;
 							
 							do{
 								bufInt = fileStream.read();
 								buffer += (char)bufInt;
 							}while((bufInt != EOF));
 	
 							byte[] decodedData = Base64.decode(buffer, Base64.DEFAULT);
 	
 							//Time to decrypt downloaded data
 							//first initialize the CipherAlgo object
 							if (!initCipher()){ //Failed to initialize the cipher and its keys;
 								showToast("Failed to initialize the cipher");
 								return;
 							}
 	
 							try{
 								String decryptedJSONData = new String(cypherAl.decrypt(decodedData));
 								showToast("Succesfully downloaded and decrypted file from the drive");
 								//ServerParser srvParser = new ServerParser(decryptedJSONData);
 								JSONParser parser = new JSONParser();
 								JSONObject jsonHashMap = (JSONObject)parser.parse(decryptedJSONData);
 	
 								if (jsonHashMap == null){
 									showToast("Invalid IV and encryption key combination");
 									
 									return;
 								}
 								JSONArray serverArray = (JSONArray)jsonHashMap.get(SERVERS_KEY);
 								if (serverArray == null){
 									showToast("Expected mapping of "+SERVERS_KEY+" to \nan array of server data");
 									return;
 								}else{
 									for (Object serverData : serverArray){
 										Server s = Server.serverFromJSON((String)serverData);
 										if ( s != null){
 											serverMap.put(s.getName(), s);
 											//System.out.println("srv returned "+s.toJSONString());
 										}
 									}
 									manageServers(true);							
 								}
 							}catch(Exception e){						
 								e.printStackTrace();
 								Message msg = new Message();
 								msg.obj = getString(R.string.invalid_encryption_credentials);
 							
 								encryptionCredentialHandler.sendMessage(msg);
 								showToast("Invalid IV key or Encryption key");
 							}
 						}else{ //No data was found on the drive
 							showToast("Creating data for the first time");
 							//We'll be sending them to the server management page 
 							//regardless of whether there was data on their drive
 							manageServers(false);	
 						}
 					}else{
 						showToast("No data found on your drive");
 						manageServers(false); //false as an argument to show that no data was detected
 					}
 				}catch(UserRecoverableAuthIOException e){
 					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
 				}catch(IOException e){
 					showToast(e.toString());
 					System.err.println(e.toString());
 					Message dlFailed = new Message();
 					dlFailed.obj = DOWNLOAD_FAILED;
 					dataSpinnerHandler.sendMessage(dlFailed);
 				}
 			}
 		});
 
 		downloader_th.start();
 	}
 
 	//Function to enable downloading of file
 	private InputStream downloadFile(Drive service, File file){
 		if ((file.getDownloadUrl() != null) && (file.getDownloadUrl().length() > 0)){
 			try{
 				HttpResponse resp = service.getRequestFactory().
 					buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
 				return resp.getContent();
 			}catch(IOException e){
 				showToast(e.toString());
 				System.err.println(e.toString());
 				return null;
 			}
 		}else{
 			//No content stored on Drive
 			return null;
 		}
 	}
 
 	public boolean initCipher(){
 		//Function to initialize and change the encryption and initialization vector keys
 		if (cypherAl == null){
 			try{
 				cypherAl = new CipherAlgo(encryptionKey,ivKey);
 				return true;
 			}catch(Exception e){
 				e.printStackTrace();
 				return false;
 			}
 		}
 		cypherAl.setEncryptionKey(encryptionKey);
 		cypherAl.setIVKey(ivKey);
 		return true;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void uploadToDrive(){
 		toggleSpinner(true);
 		Resources res = getResources();
 		if (credential == null){
 			showToast(res.getString(R.string.null_credential_warning));
 			return;
 		}else if (service == null){
 			showToast(res.getString(R.string.null_drive_warning));
 			return;
 		}
 		
 		showToast("Started Upload activity");
 		String mediaStorageDir = Environment.getExternalStoragePublicDirectory(
 				Environment.DIRECTORY_DOWNLOADS).getPath();
 
 		fileUri = Uri.fromFile( 
 				new java.io.File(mediaStorageDir + java.io.File.separator + 
 				DEFAULT_ENCRYPTED_FILENAME)
 		);
 
 		//first initialize the CipherAlgo object
 		if (!initCipher()){ //Failed to initialize the cipher and its keys;
 			showToast("Failed to initialize the cipher");
 			return;
 		}
 
 		try{
 			FileWriter outputStream = new FileWriter(fileUri.getPath());
 			JSONObject serverJSON = new JSONObject();
 			JSONArray serverArray = new JSONArray();
 
 			for ( Server s: serverMap.values()){
 				serverArray.add(s.toJSONString());
 			}
 			
 			serverJSON.put(SERVERS_KEY, serverArray);
 			System.err.println("Before encryption "+serverJSON.toJSONString());
 			
 			//Encrypting the data 
 			byte[] encryptedData = cypherAl.encrypt(serverJSON.toJSONString());
 			
 			//Creating a local copy of the encrypted content that is clobberable
 			outputStream.write(Base64.encodeToString(encryptedData, Base64.DEFAULT));
 
 			outputStream.close();
 
 			System.out.println("Data to be uploaded "+serverJSON.toString());
 			//Time to save to the drive
 			saveFileToDrive();
 
 		}catch(IOException e){
 			showToast("IOEXception " + e.toString());
 		}catch(Exception e){
 			showToast(e.toString());
 			System.err.println(e.toString());
 		}
 	}
 
 	private void saveFileToDrive() {
 		Thread upload_th = new Thread( new Runnable() {
 			@Override
 			public void run(){
 				try{
 					//File's binary content
 					java.io.File fileContent = new java.io.File(fileUri.getPath());
 					//File's metadata
 					File body = new File();
 					body.setTitle(fileContent.getName());
 
 					body.setMimeType("text/data");
 					Message uploadStatusMsg = new Message();
 					uploadStatusMsg.obj = UPLOAD_STARTED;
 					dataSpinnerHandler.sendMessage(uploadStatusMsg);
 					FileContent mediaContent = new FileContent("text/txt", fileContent);
 
 					java.util.List <File> result = new ArrayList<File>();
 					Files.List request = service.files().list().setQ("title = '"+DEFAULT_ENCRYPTED_FILENAME+"'");
 					FileList files = request.execute();	
 					result.addAll(files.getItems());
 					System.out.println("size is "+ result.size());
 
 					if (result.size()==0){	//No file found with our target name
 						File file = service.files().insert(body, mediaContent).execute();
 						if (file != null){
 							Message signalUlMsg = new Message();
 							signalUlMsg.obj = UPLOAD_COMPLETE;
 							dataSpinnerHandler.sendMessage(signalUlMsg);
 							showToast("File uploaded: " + file.getTitle());
 							showToast("File ID: "+file.getId());
 						}
 					}else {	//eceData.ece exists
 						System.out.println(result.get(0).get("id"));
 						System.out.print("i am here!");
 						String existingFileId = (String) result.get(0).get("id");
 						System.out.println(existingFileId);
 						File updatedFile = service.files().update(existingFileId, body, mediaContent).execute();
 						
 						if (updatedFile != null){
 							showToast("File uploaded: " + updatedFile.getTitle());
 							showToast("File ID: "+updatedFile.getId());
 					
 							//Let's clear the serverMap of entered data
 							serverMap.clear();
 							showToast("serverMap cleared for use with another account");
 							
 							Message signalUlMsg = new Message();
 							signalUlMsg.obj = UPLOAD_COMPLETE;
 							uploadStatusMsg.obj = UPLOAD_COMPLETE;
 							dataSpinnerHandler.sendMessage(signalUlMsg);	
 						}else{
 							showToast("Upload unsuccessful!\nData has not been cleared");
 						}
 					}
 					
 				} catch (UserRecoverableAuthIOException e){
 					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
 					
 				} catch (IOException e){
 					showToast( e.toString());
 					e.printStackTrace();
 				} catch (Exception e){
 					Message uploadStatusMsg = new Message();
 					uploadStatusMsg.obj = UPLOAD_FAILED;
 					dataSpinnerHandler.sendMessage(uploadStatusMsg);
 					showToast(e.toString());
 				}
 			}
 		});
 		upload_th.start();
 	}
 	
 	public void changeEncryptionCredentials(View v){
 		Intent keyChangeIntent = new Intent(v.getContext(), IvSetEncryptionKeyActivity.class);
 		startActivityForResult(keyChangeIntent, CHANGING_ENCRYPTION_KEYS);
 	}
 	
 	//Our state-saving object for restoring our data on recreation of the app
 	public class MapCredentialSaver{
 		public HashMap<String, Server>theServerMap = null;
 		public GoogleAccountCredential theCredential = null;
 		public String theIvKey = null;
 		public String theEncryptionKey = null;
 		public MapCredentialSaver(HashMap<String, Server>srvMap, GoogleAccountCredential credential){
 			theServerMap = (HashMap<String, Server>)srvMap.clone();
 			theCredential = credential;
 			theIvKey = ivKey;
 			theEncryptionKey = encryptionKey;
 		}
 	}
 	
 	@Override
 	public Object onRetainNonConfigurationInstance(){
 		final MapCredentialSaver saver = new MapCredentialSaver(serverMap, credential);
 		return saver;
 	}
 }
