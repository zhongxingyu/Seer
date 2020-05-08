 package com.mytutor.search;
 import android.app.Activity;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 import com.mytutor.search.TutorReview;
 import com.mytutor.search.EmailTutor;
 import com.mytutor.session.ServerSession;
 
 import com.mytutor.R;
 
 import com.mytutor.authentication.AuthenticationHelper;
 import com.mytutor.profile.Profile;
 
 
 public class ViewProfile extends Activity
 {
 
    String email_ = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
 	   super.onCreate(savedInstanceState);
 	   setContentView(R.layout.result_profile);
 	   
        ImageView image=(ImageView)findViewById(R.id.image);
        TextView firstName = (TextView)findViewById(R.id.firstName);
        TextView lastName = (TextView)findViewById(R.id.lastName);
        TextView rate = (TextView)findViewById(R.id.rate);
        RatingBar ratingBar = (RatingBar)findViewById(R.id.rating);
        TextView ratingView = (TextView) findViewById(R.id.ratingInfo);
        TextView schedule = (TextView)findViewById(R.id.schedule);
        TextView profile = (TextView)findViewById(R.id.comment);
        Button emailMe = (Button)findViewById(R.id.emailMeButton);
 	   
        ImageLoader imageLoader = new ImageLoader(); 
        String url = "http://omega.uta.edu/~jwe0053/picture.php?email="+getIntent().getExtras().getString("email");
        imageLoader.fetchDrawableOnThread(url, image);
        firstName.setText(getIntent().getExtras().getString("username"));
        lastName.setText(getIntent().getExtras().getString("lastname"));
        rate.setText(getIntent().getExtras().getString("rate"));
        ratingBar.setRating(Float.parseFloat(getIntent().getExtras().getString("rating")));
        ratingView.setText(String.format("(%.1f)", Float.parseFloat(getIntent().getExtras().getString("rating"))));
        schedule.setText(getIntent().getExtras().getString("schedule"));
        profile.setText(getIntent().getExtras().getString("profile"));
        
        AuthenticationHelper ah = new AuthenticationHelper(this);

		Log.i("SearchResults", "email=" + email_);
        try {
 			email_ = ah.getToken();
 		} catch (Exception e1) {
			//e1.printStackTrace();
			emailMe.setEnabled(false);
 		}
		Log.i("SearchResults", "email=" + email_);
         if(email_ == "")
        	emailMe.setEnabled(false);
        
    }
    
    /** Called when the user touches the rating button */
    public void rateMeClicked(View view) {
        Editor editor = null;
 	// Do something in response to button click
 	   TutorReview.showRateDialog(this, editor);
    }
    
    /** Called when the user touches the email button */
    public void emailMeClicked(View view) {
        Editor editor = null;
        Profile profile = ServerSession.getProfile(email_);
 	// Do something in response to button click
 	   EmailTutor.showEmailDialog(this, editor, getIntent().getExtras().getString("username")+" " + 
 	                              getIntent().getExtras().getString("lastname"), getIntent().getExtras().getString("email"),
 	                              profile.getFirstName()+" "+profile.getLastName(), profile.getEmail());
    }
 }
