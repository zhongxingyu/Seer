 package com.wingman;
 
 import java.net.MalformedURLException;
 import com.github.mhendred.face4j.model.Face;
 import com.github.mhendred.face4j.model.Photo;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.wingman.models.PersonModel;
 import com.wingman.parsers.AppharborWrapper;
 import com.wingman.parsers.FaceComWrapper;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class PersonActivity extends Activity{
 	
 	private static final String TAG = "PersonActivity";
 	private static final String IMAGE_PATH = "/sdcard/wingman/LatestWingman.jpg";
 	//private static final String APPHARBOR_URL = "http://wingman.apphb.com/";
 	private static final int ADD_NEW_USER_REQUEST = 0;
 	//private static final String MY_AD_UNIT_ID = "a14e50830f3a4d4";
 	private PersonModel m_pm;
 	
 	
     private class ResizePicture extends AsyncTask<PersonModel, Integer, Bitmap> {
         protected Bitmap doInBackground(PersonModel... pms) {
             int count = pms.length;
             PersonModel pm;
             if(count > 0)
             {
             	pm = pms[0];
             	// Load picture taken from cam
         		Bitmap orgImage = BitmapFactory.decodeFile(IMAGE_PATH);
         		
         		Photo myPhoto = pm.getPhoto();
         		
         		//myPhoto.scaleFaceRects(image.getMeasuredWidth(), image.getMeasuredHeight());
         		//myPhoto.scaleFaceRects(orgImage.getWidth(), orgImage.getHeight());
         		//myPhoto.scaleFaceRects(myPhoto.getWidth(), myPhoto.getHeight());
         		Face face = myPhoto.getFaces().get(pm.getSelectedFace());
         		int x = (int) face.getRectangle().left;
         		int y = (int) face.getRectangle().top;
         		int width = (int) (face.getRectangle().right - face.getRectangle().left);
         		int height = (int) (face.getRectangle().bottom - face.getRectangle().top);
         		// Crop it
         		Bitmap nImage = Bitmap.createBitmap(orgImage, x, y, width, height);
         		return nImage;
             } else {
             	throw new RuntimeException("ERR: no personmodel given for picture resize");
             }
         }
 
 
         protected void onPostExecute(Bitmap result) {
             //showDialog("Downloaded " + result + " bytes");
         	ImageView image = (ImageView) findViewById(R.id.imageView1);
         	image.setImageBitmap(result);
         	
         	String UID = m_pm.getPhoto().getFaces().get(m_pm.getSelectedFace()).getGuess().first;
     		if(UID == "Unknown"){
     			doNewUser(m_pm);
     		} else {
     			//int idPerson = Integer.valueOf(UID.trim()).intValue();
     			
     			//FaceComWrapper.faceClient.recognize(imageFile, uids)
     			//m_pm.setID_PERSON(idPerson);
     			m_pm = FaceComWrapper.recognizePersonModel(m_pm);
     			//String reeelzUrl = m_pm.getPhoto().getFaces().get(m_pm.getSelectedFace()).getGuess().first;
     			doKnownUser(m_pm);
     		}
     		
         }
     }
 	
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.person);
 		
 		
 		
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		m_pm = (PersonModel) extras.get("PersonModel");
 		new ResizePicture().execute(m_pm);
 		
 		TextView tv = (TextView)findViewById(R.id.textView1);
 		tv.setText("Loading, please wait.");
 		Button b1 = (Button)findViewById(R.id.button1);
 		Button b2 = (Button)findViewById(R.id.button2);
 		Button b3 = (Button)findViewById(R.id.button3);
 		b1.setText("Loading");
 		b2.setText("Loading");
 		b3.setText("Loading");
 		b1.setEnabled(false);
 		b2.setEnabled(false);
 		b3.setEnabled(false);
 
 		
 	}
 	
 	private void doKnownUser(PersonModel pm) {
 		// TODO Auto-generated method stub
 		//TextView uid_textview = (TextView) findViewById(R.id.textView1);
 		//String myUrl = APPHARBOR_URL + "Show?uid=" + pm.getPhoto().getFaces().get(pm.getSelectedFace()).getGuess().first;
 		//PersonModel myPm = pm;
 		//myPm.getID_PERSON();
 		//FaceComWrapper fcw = new FaceComWrapper();
 		//AppharborWrapper aw = new AppharborWrapper(myPm);
 		//List<PersonModel> descriptions ;
 		StringBuilder sb = new StringBuilder("Person recognized! \n");
 		//try {
 			//myPm = fcw.addPerson(myPm);
 			//myPm = aw.addPersonTag(myPm);
 			//aw.attachTag(myPm.getID_DESCRIPTION(), myPm.getIdTag());
 			//descriptions = aw.FindAllDescriptions(pm);
 			//int everythingwentbetterthanexpected = 5+10;
 		//} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 		//	e.printStackTrace();
 		//}
 		
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setEnabled(false);
		pb.setVisibility(View.GONE);
 		
 		TextView text1 = (TextView) findViewById(R.id.textView1);
 		text1.setText(sb.toString());
 		
 		Button button1 = (Button) findViewById(R.id.button1);
 		Button button2 = (Button) findViewById(R.id.button2);
 		Button button3 = (Button) findViewById(R.id.button3);
 		
 		button1.setEnabled(true);
 		button1.setText("I know this person!");
 		button1.setOnClickListener(new OnClickListener() {
 			
 			// "I know this person"
 			@Override
 			public void onClick(View arg0) {
 				// Run Add new UserTag activity
 				Intent myIntent = new Intent(getBaseContext(), AddUserTagActivity.class);
 				myIntent.putExtra("PersonModel", m_pm);
 				startActivity(myIntent);
 			}
 		});
 		
 		button2.setEnabled(true);
 		button2.setText("See descriptions");
 		button2.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// 
 				Intent i = new Intent(getBaseContext(), DisplayDescriptionActivity.class);
 				i.putExtra("PersonModel", m_pm);
 				startActivity(i);
 			}
 		});
 		
 		button3.setEnabled(true);
 		button3.setText("Back");
 		button3.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				//Intent i = new Intent(getBaseContext(), FaceRecActivity.class);
 				//startActivity(i);
 				finish();				
 			}
 		});
 	}
 
 	private void doNewUser(PersonModel pm) {
 		// TODO Auto-generated method stub
 		TextView text1 = (TextView) findViewById(R.id.textView1);
 		
 		text1.setText("Person not found in database! Would you like to add this person?");
 
 		final Button button1 = (Button) findViewById(R.id.button1);
 		button1.setEnabled(true);
 		final Button button2 = (Button) findViewById(R.id.button2);
 		final Button button3 = (Button) findViewById(R.id.button3);
 		button3.setEnabled(true);
 		button1.setText("Yes");
 		button1.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent i = getIntent();
 				Bundle extras = i.getExtras();
 				PersonModel pm = (PersonModel) extras.get("PersonModel");
 				
 				Intent myIntent = new Intent(getBaseContext(), AddUserActivity.class);
 				myIntent.putExtra("PersonModel", pm);
 				startActivityForResult(
 		                 myIntent,
 		                 ADD_NEW_USER_REQUEST);
 				}			
 		});
 		
 		
 		button2.setText("");
 		button2.setEnabled(false);
 		
 		button3.setText("No");
 		button3.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				finish();
 			}
 		});
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == ADD_NEW_USER_REQUEST) {
             if (resultCode == RESULT_OK) {
                 // User input received
             	Bundle extras = data.getExtras();
             	PersonModel m_pm = (PersonModel) extras.get("UserInput");
             	
             	// Store input in central db
             	AppharborWrapper ah = new AppharborWrapper(m_pm);
             	FaceComWrapper fcw = new FaceComWrapper();
             	try {
 					m_pm = ah.addPerson(m_pm);
 					
 					// Add person to face.com, with the PersonID generated by AH
 	            	m_pm = fcw.addPerson(m_pm);
 	            	// Put the tagID from Face.com in AH
 	            	ah.attachTag(m_pm.getID_DESCRIPTION(),m_pm.getIdTag());
 
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				}
 
             	TextView tv = (TextView) findViewById(R.id.textView1);
             	if(ah.errorCode != "")
             	{
             		tv.setText("ERROR: " + ah.errorCode);
             	}
             	else
             	{
             		tv.setText("Person added succesfully, and will now be available for recognition");
             		Button button1 = (Button)findViewById(R.id.button1);
             		button1.setText("");
             		button1.setEnabled(false);
             		Button button2 = (Button)findViewById(R.id.button2);
             		button2.setText("");
             		button2.setEnabled(false);
             		Button button3 = (Button)findViewById(R.id.button3);
             		button3.setText("OK");
             		button3.setOnClickListener(new View.OnClickListener() {
             			@Override
             			public void onClick(View arg0) {
             				// Run face recognition activity
             				// TODO: Should return to WingmanActivity.class instead
             				Intent myIntent = new Intent(getBaseContext(), WingmanActivity.class);
             				startActivity(myIntent);
             				finish();
             			}
             		});
             	}
        
             } 
         } 
     }
 	
 	
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	protected void onResume() {
 		Log.e(TAG, "onResume");
 		super.onResume();
 	}
 
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	protected void onStop() {
 		Log.e(TAG, "onStop");
 		super.onStop();
 	}
 }
