 package com.niceapps.app;
 
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.ProfilePictureView;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Item_details extends Activity {
 	private static String DISK_URL = "http://niceapps.herokuapp.com/disks/";
 	private TextView title, condition, interests;
 	private Disk disk;
 	private ProfilePictureView profilePictureView;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.item_details);
 
 		disk = (Disk) getIntent().getSerializableExtra("disk");
 		DISK_URL += disk.getId() + ".json";
 
 		title = (TextView) findViewById(R.id.textView1);
 		condition = (TextView) findViewById(R.id.condition);
 		interests = (TextView) findViewById(R.id.interest);
 
 		title.setText(disk.getTitle());
 		condition.setText(disk.getConditions());
 		interests.setText(disk.getInterest());
 
 		// Find the user's profile picture custom view
		profilePictureView = (ProfilePictureView) findViewById(R.id.profile_pic);
 		profilePictureView.setCropped(false);
 		Session session = Session.getActiveSession();
 		if (session != null && session.isOpened()) {
 			// Get the user's data
 			makeMeRequest(session);
 		}
 
 		// Accion a realizar en caso de que se oprima el boton Im interested
 		((Button) findViewById(R.id.button2))
 				.setOnClickListener(new OnClickListener() {
 					public void onClick(View view) {
 						send_offer(); // Manda llamar al m�todo send_offer
 					}
 				});
 	}
 
 	private void send_offer() {
 		Intent intent = new Intent(getBaseContext(), Offer_for_item.class);
 		intent.putExtra("disk", disk);
 		startActivity(intent);
 	}
 	
 	private void makeMeRequest(final Session session) {
 		// Make an API call to get user data and define a
 		// new callback to handle the response.
 		Request request = Request.newMeRequest(session,
 				new Request.GraphUserCallback() {
 					@Override
 					public void onCompleted(GraphUser user, Response response) {
 						// If the response is successful
 						if (session == Session.getActiveSession()) {
 							if (user != null) {
 								// Set the id for the ProfilePictureView
 								// view that in turn displays the profile
 								// picture.
 								profilePictureView.setProfileId(user.getId());
 							}
 						}
 						if (response.getError() != null) {
 							// Handle errors, will do so later.
 						}
 					}
 				});
 		request.executeAsync();
 	}
 
 }
