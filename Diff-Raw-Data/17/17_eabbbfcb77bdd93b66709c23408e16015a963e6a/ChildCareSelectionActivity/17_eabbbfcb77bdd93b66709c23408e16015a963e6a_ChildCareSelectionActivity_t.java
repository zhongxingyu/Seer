 /**
  * 
  */
 package com.dinosaurwithakatana.childcare;
 
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.TextView;
 
 /**
  * @author vishnu
  *
  */
 public class ChildCareSelectionActivity extends Activity {
 
 	private String businessName, businessId, contactPhoneNumber, contactEmail, contactWebsite, contactAreaCode;
 	private String TAG = ChildCareSelectionActivity.class.getSimpleName();
 	private Bundle b;
 	protected JSONArray ratingArray;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_child_care_selection);		
 
 		Intent i = getIntent();
 		businessName = i.getStringExtra("Name");
 		businessId = i.getStringExtra("id");
 		b=i.getExtras();
 		
//			String ratingResponse = new GetRating().execute(b.getString("u_id")).get();
//			JSONObject ratingResponseObject = new JSONObject(ratingResponse);
 			
 //			ratingArray = ratingResponseObject.getJSONArray("rating");
 //			Log.v(TAG,ratingArray.toString());
 			// TODO Auto-generated catch block
 			// TODO Auto-generated catch block
 		Log.v(TAG,"Business Name: "+businessName);
 
 		TextView txtBusinessName = (TextView)findViewById(R.id.txt_business_name);
 		TextView txtTypeOfCare = (TextView)findViewById(R.id.txt_type_of_care);
 		TextView txtStreetAddress = (TextView)findViewById(R.id.txt_street_address);
 		TextView txtState = (TextView) findViewById(R.id.txt_state);
 		TextView txtZip = (TextView) findViewById(R.id.txt_zip);
 		TextView txtCity = (TextView) findViewById(R.id.txt_city);
 		Button callContact = (Button) findViewById(R.id.btnCallContact);
 		Button openWebsite = (Button) findViewById(R.id.btnWebsite);
 		Button emailContact = (Button) findViewById(R.id.btnEmailContact);
 		RatingBar myRating = (RatingBar) findViewById(R.id.rtngCareRating);
 
 		try{
 			String responseJSON = new GetSelectedCare().execute(businessId).get();
 			JSONArray o = new JSONArray(responseJSON);
 			Log.v(TAG,o.toString(1));
 			int responseLength = o.length();
 			Log.v(TAG,""+responseLength);
 
 			JSONObject responseObject = o.getJSONObject(0);
 			Log.v(TAG,"child care selection: " + responseObject.toString(1));
 			
 			txtBusinessName.setText(businessName);
 			txtTypeOfCare.setText(responseObject.getString("Type Of Care"));
 			txtStreetAddress.setText(responseObject.getString("Street Address"));
 			txtState.setText(responseObject.getString("State/Prov."));
 			txtZip.setText(responseObject.getString("Zip"));
 			txtCity.setText(responseObject.getString("City"));
 			contactEmail = responseObject.getString("Email");
 			contactPhoneNumber = responseObject.getString("Phone");
 			contactWebsite = responseObject.getString("Website");
 			contactAreaCode = responseObject.getString("Area Code");
 			
 			callContact.setText(contactAreaCode+"-"+contactPhoneNumber);
 			callContact.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					String fullContactNumber = contactAreaCode + "-" + contactPhoneNumber;
 					String uri = "tel:" + fullContactNumber.trim() ;
 					Intent intent = new Intent(Intent.ACTION_CALL);
 					intent.setData(Uri.parse(uri));
 					startActivity(intent);	
 				}
 			});
 			
 			emailContact.setText(contactEmail);
 			emailContact.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 			        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{contactEmail});
 			        emailIntent.setType("plain/text");
 			        startActivity(Intent.createChooser(emailIntent, "Send email..."));	
 				}
 			});
 			
 			openWebsite.setText(contactWebsite);
 			openWebsite.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contactWebsite));
 					startActivity(browserIntent);	
 				}
 			});
 			
 
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 		
 		//if rating value is changed,
 		//display the current rating value in the result (textview) automatically
 		myRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
 			public void onRatingChanged(RatingBar ratingBar, float rating,
 				boolean fromUser) {
 				
 				JSONObject userRating = new JSONObject();
 				JSONObject postObject = new JSONObject();
 				ratingArray = new JSONArray();
 				JSONObject ratingObject = new JSONObject();
 				try {
 				JSONObject businessID = new JSONObject(businessId);
 					userRating.put("care_id",businessID);
 					userRating.put("rating", rating);
 					ratingArray.put(userRating);
 					ratingObject.put("rating", ratingArray);
 					postObject.put("$set",ratingObject);
 					new PostUserRating().execute(postObject.toString(),b.getString("u_id"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 	 
 			}
 		});
 	} 
 	/**
 	 * 
 	 */
 	public ChildCareSelectionActivity() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	
 
 }
