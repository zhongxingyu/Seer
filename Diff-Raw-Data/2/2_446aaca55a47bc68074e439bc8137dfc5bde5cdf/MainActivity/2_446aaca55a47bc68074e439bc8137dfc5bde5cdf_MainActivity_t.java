 package edu.byu.isys413.data.android;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity {
 
 	ViewFlipper vf = null;
 	HttpClient client = null;
 	List<Picture> picList = new ArrayList<Picture>();
 	
 	// These correspond to the order the layouts appear in the ViewFlipper in activity_main.xml.
 	static int LOGIN_VIEW = 0;
 	static int PIC_LIST_VIEW = 1;
 	static int SHOW_PIC_VIEW = 2;
 	static int UPLOAD_PIC_VIEW = 3;
 	
 	static final int UPLOAD_PHOTO_CODE = 1;
 	
 	Picture shownPic = null;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         vf = (ViewFlipper)findViewById(R.id.viewFlipper);
         client = new DefaultHttpClient();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     /**
      * This method does the following:
      * 	- Posts the credentials to the server
      * 	- If valid user, list of pic captions is displayed
      * 	- click and long click listeners are attached to the items in the list view.
      * 		- long click moves user to next view where a preview of the image is displayed
      * 		- regular click toggles whether the picture is selected or not for eventual purchase
      * @param view
      */
     public void loginSubmit(final View view) {
     	// Force the keyboard to close.
     	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     	imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.editTextPassword)).getWindowToken(), 0);
     	// Post the login credentials.
     	try {
 	    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 	    	EditText email = (EditText) findViewById(R.id.editTextEmail);
 	    	EditText password = (EditText) findViewById(R.id.editTextPassword);
 	    	nameValuePairs.add(new BasicNameValuePair("format", "json"));
 	    	nameValuePairs.add(new BasicNameValuePair("email", email.getText().toString()));
 	        nameValuePairs.add(new BasicNameValuePair("password", password.getText().toString()));
 	        JSONObject json = makeRequest("Login", nameValuePairs);
 			if(json.getString("status").equals("success")) {
				// Set the text of the login feedback to blank (for when they log out, if they missed the password once.
				((TextView) findViewById(R.id.textViewLoginStatus)).setText("");
 				// Get the pics out of the JSON array
 				populatePicListFromJson(json);
 				// Assign the picList to the ListView
 				ListView pics = (ListView) findViewById(R.id.listViewPics);
 				pics.setAdapter(new ArrayAdapter(this, R.layout.pic_in_list, picList));
 				pics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 						Picture pic = (Picture) parent.getItemAtPosition(position);
 						pic.toggleSelected();
 					}
 				});
 				pics.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 					@Override
 					public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
 						try {
 							Picture pic = (Picture) parent.getItemAtPosition(position);
 							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 							nameValuePairs.add(new BasicNameValuePair("picId", pic.getGuid()));
 					        JSONObject json = makeRequest("PicData", nameValuePairs);
 							// Get the pic data, decode it, and show it in the view.
 							String picData = json.getString("picData");
 							byte[] picDataBytes = Base64.decode(picData, Base64.DEFAULT);
 				        	ImageView showPicture = (ImageView) findViewById(R.id.imageViewShowPicture);
 				        	showPicture.setImageBitmap(BitmapFactory.decodeByteArray(picDataBytes, 0, picDataBytes.length));
 				        	// Show the caption in the view, too.
 				        	TextView showPictureCaption = (TextView) findViewById(R.id.textViewShowPictureCaption);
 				        	showPictureCaption.setText(pic.getCaption());
 				        	// Finally, show the ShowPicture view.
 				        	showShowPictureView();
 				        	// Set the shownPic to the current picture, in case the user buys prints from show picture view.
 				        	shownPic = pic;
 						} catch(Exception e) {
 							e.printStackTrace();
 						}
 						return true;// See http://developer.android.com/reference/android/widget/AdapterView.OnItemLongClickListener.html
 					}
 				});
 				showListView();
 			} else {
 				TextView loginStatus = (TextView) findViewById(R.id.textViewLoginStatus);
 				loginStatus.setText("Bummer. The email/password you provided didn't work.");
 			}
     	} catch(Exception e) {
     		e.printStackTrace();
     	}
     }
     
     private void populatePicListFromJson(JSONObject json) throws JSONException {
     	// Empty the picList
     	picList.clear();
     	// Get the pics out of the JSON array
 		JSONArray picsJa = json.getJSONArray("pics");
 		for(int i = 0; i < picsJa.length(); i++) {
 			JSONObject picJo = picsJa.getJSONObject(i);
 			Picture pic = new Picture(picJo.getString("id"), picJo.getString("caption"));
 			picList.add(pic);
 		}
     }
     
     /**
      * @param action
      * @return The full URL for that action
      */
     private String getUrlFromAction(String action) {
     	StringBuilder bldr = new StringBuilder();
     	bldr.append("http://10.0.2.2:8080/MyStuffSprint/edu.byu.isys413.data.actions.");
     	bldr.append(action);
     	bldr.append(".action");
     	return bldr.toString();
     }
     
     /**
      * Sends the user to the camera to take the picture.
      * @param view
      */
     public void beginUpload(View view) {
     	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
     	startActivityForResult(intent, UPLOAD_PHOTO_CODE);
     }
     
     /**
      * Helper method for referring to the temporary file created by taking the photo.
      * @return File object.
      */
     private File getTempFile() {
     	File path = new File(Environment.getExternalStorageDirectory(), "ISysMyStuffTmp");
     	if(!path.exists()) {
     		path.mkdir();
     	}
     	return new File(path, "img.tmp");
     }
     
     /**
      * Fires when user is done with the photo taking activity. Processes image data,
      * populates image view with the bitmap, and encodes the image data as a string
      * for later transmission to the server.
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if(resultCode == RESULT_OK) {
     		switch(requestCode) {
     			case UPLOAD_PHOTO_CODE:
     				
     				try {
     					
     					File file = getTempFile();
     					
     					// Get image data and put it in imageViewer
     					Bitmap pic = Media.getBitmap(getContentResolver(), Uri.fromFile(file));
     					ImageView upload = (ImageView) findViewById(R.id.imageViewUpload);
     					upload.setImageBitmap(pic);
     					
     					// Show upload view.
     					showUploadPictureView();
     					
     				} catch (FileNotFoundException e) {
 						e.printStackTrace();
     				} catch (IOException e) {
     					e.printStackTrace();
     				}
     				break;
     		}
     	}
     }
     
     /**
      * After the user has taken a photo, confirming that they want to
      * upload it calls this method.
      * @param view
      */
     public void finishUpload(View view) {
     	// Create a string from the Base64-encoded picture.
     	ImageView upload = (ImageView) findViewById(R.id.imageViewUpload);
     	Bitmap bitmap = ((BitmapDrawable)upload.getDrawable()).getBitmap();
     	ByteArrayOutputStream stream = new ByteArrayOutputStream();
     	bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
     	byte[] byteArray = stream.toByteArray();
     	String picData = Base64.encodeToString(byteArray, Base64.DEFAULT);
     	
     	// Get value of the caption.
     	String caption = ((EditText) findViewById(R.id.editTextUploadCaption)).getText().toString();
     	
     	// Send to server and get updated pic list
     	try {
 	    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 	    	nameValuePairs.add(new BasicNameValuePair("picData", picData));
 	    	nameValuePairs.add(new BasicNameValuePair("caption", caption));
 	    	JSONObject json = makeRequest("UploadPicture", nameValuePairs);
 			populatePicListFromJson(json);
 			// We need to notify the adapter that the dataset changed.
 			ListView pics = (ListView) findViewById(R.id.listViewPics);
 			pics.invalidateViews();
     	} catch(Exception e) {
     		// TODO: handle this.
     	}
     	
     	// Switch to pic list view.
     	showListView();
     }
     
     /**
      * Sends a message to the server to delete the session and
      * returns the user to the login view.
      * @param view
      */
     public void logout(View view) {
     	try {
     	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     	nameValuePairs.add(new BasicNameValuePair("format","json"));
     	JSONObject json = makeRequest("Logout", nameValuePairs);
     	if(json.getString("status").equals("success")) {
     		// clear the password input and show the login view.
     		((EditText) findViewById(R.id.editTextPassword)).setText("");
     		showLoginView();
     	}
     	} catch(Exception e) {
     		// TODO: handle this.
     	}
     }
     
     /**
      * For making HTTP requests from within the application.
      * @param action
      * @param data
      * @return JSONObject json
      * @throws Exception
      */
     public JSONObject makeRequest(String action, List<NameValuePair> data) throws Exception {
     	HttpPost request = new HttpPost(getUrlFromAction(action));
     	request.setEntity(new UrlEncodedFormEntity(data));
         HttpResponse response = client.execute(request);
         StatusLine statusLine = response.getStatusLine();
         int statusCode = statusLine.getStatusCode();
         if (statusCode == 200) {
 			HttpEntity entity = response.getEntity();
 			InputStream content = entity.getContent();
 			StringBuilder builder = new StringBuilder();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 			return new JSONObject(builder.toString());
         } else {
         	throw new Exception("Got a response other than 200");
         }
     }
     
     public void buyShownPicture(View view) {
     	try {
     		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     		nameValuePairs.add(new BasicNameValuePair("guid", shownPic.getGuid()));
 			JSONObject json = makeRequest("BuyPrints", nameValuePairs);
 			// Get json message and display it in a toast.
 			Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
 			showListView();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public void buySelectedPictures(View view) {
     	try {
     		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     		for(Picture pic : picList) {
     			if(pic.isSelected()) {
     				nameValuePairs.add(new BasicNameValuePair("guid", pic.getGuid()));
     			}
     		}
     		if(nameValuePairs.size() > 0) {
     			JSONObject json = makeRequest("BuyPrints", nameValuePairs);
     			// Get json message and display it in a toast.
     			Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
     			showListView();
     		} else {
     			throw new Exception("Cannot buy 0 prints.");
     		}
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     }
     
     /**
      * The following are convenience methods for using the ViewFlipper.
      * Overloaded with View parameter so that they may be called
      * directly from an event handler if desired.
      */
     public void showLoginView() { vf.setDisplayedChild(LOGIN_VIEW); }
     public void showLoginView(View view) { vf.setDisplayedChild(LOGIN_VIEW); }
     public void showListView() { vf.setDisplayedChild(PIC_LIST_VIEW); }
     public void showListView(View view) { vf.setDisplayedChild(PIC_LIST_VIEW); }
     public void showShowPictureView() { vf.setDisplayedChild(SHOW_PIC_VIEW); }
     public void showShowPictureView(View view) { vf.setDisplayedChild(SHOW_PIC_VIEW); }
     public void showUploadPictureView() { vf.setDisplayedChild(UPLOAD_PIC_VIEW); }
     public void showUploadPictureView(View view) { vf.setDisplayedChild(UPLOAD_PIC_VIEW); }
     
 }
