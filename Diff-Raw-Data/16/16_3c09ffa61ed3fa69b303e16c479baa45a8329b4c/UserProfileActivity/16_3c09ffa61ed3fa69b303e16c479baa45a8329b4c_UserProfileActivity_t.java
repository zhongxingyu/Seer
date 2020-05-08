 package com.vorsk.crossfitr;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 
 import com.vorsk.crossfitr.models.AchievementModel;
 import com.vorsk.crossfitr.models.ProfileModel;
 import com.vorsk.crossfitr.models.WorkoutSessionModel;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ImageView;
 
 
 
 public class UserProfileActivity extends Activity implements OnClickListener 
 {
 	private static final int CAMERA_REQUEST = 666;
 	
 	ProfileModel model = new ProfileModel(this);
 	WorkoutSessionModel sessionModel = new WorkoutSessionModel(this);
 	AchievementModel achievementModel = new AchievementModel(this);
 
 	
 	private TextView userNameText;
 	private TextView userBMIText;
 	private TextView userWeightText;
 	private TextView userGoalWeightText;
 	private TextView userHeightText;
 	private TextView userTotalWorkoutsText;
 	private TextView userLastWorkoutText;
 	private TextView userTotalAchievementsText;
 	
 	private ImageView photoButton;
 	
 	private File file;
 	private Typeface font;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.userprofile);
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		// Displaying user data
 		model.open();
 		font = Typeface.createFromAsset(this.getAssets(),
 				"fonts/Roboto-Thin.ttf");
 		
 		// Setting up photobutton
 		file = new File(Environment.getExternalStorageDirectory(), "profile.png");
 		photoButton = (ImageView) this.findViewById(R.id.user_pic_button);
 		Bitmap bMap = BitmapFactory.decodeFile(file.toString());
 		if(bMap != null){
 			photoButton.setImageBitmap(bMap);
 		}
 		
 		photoButton.setOnClickListener(new View.OnClickListener() {            
             public void onClick(View v) {
                 Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
                 startActivityForResult(cameraIntent, CAMERA_REQUEST); 
             }
         });
 
 		// If nothing entered, redirect the user to the edit profile page
 		// Name
 		userNameText = (TextView) findViewById(R.id.user_name);
 		/*if(model.getByAttribute("name") == null){
 			Intent u = new Intent(this, EditUserProfileActivity.class);
 			startActivity(u);
 		}
 		else */if(model.getByAttribute("name") != null){
 			userNameText.setText(model.getByAttribute("name").value);
 		}
 		userNameText.setTypeface(font);
 		
 		// BMI
 		userBMIText = (TextView) findViewById(R.id.user_bmi);
 		if((model.getByAttribute("weight") != null) && (model.getByAttribute("height") != null)){
 			userBMIText.setText(this.getString(R.string.user_bmi) + " " + model.calculateBMI().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
 		}
 		userBMIText.setTypeface(font);
 	
 		
 		
 		userWeightText = (TextView) findViewById(R.id.user_weight);
 		// Current Weight
 		if(model.getByAttribute("weight") != null){
 			userWeightText.setText(this.getString(R.string.user_weight) + " " + model.getByAttribute("weight").value);
 		}
 		userWeightText.setTypeface(font);
 		
 		// Goal Weight
 		userGoalWeightText = (TextView) findViewById(R.id.user_goal_weight);
 		if(model.getByAttribute("goal_weight") != null){
 			userGoalWeightText.setText(this.getString(R.string.user_goal_weight) + " " + model.getByAttribute("goal_weight").value);
 		}
 		userGoalWeightText.setTypeface(font);
 		
 		// Current Height
 		userHeightText = (TextView) findViewById(R.id.user_height);
 		if(model.getByAttribute("height") != null){
 			userHeightText.setText(this.getString(R.string.user_height) + " " + model.getByAttribute("height").value);
 		}
 		userHeightText.setTypeface(font);
 		
 		
 		// Getting data from workout model
 		sessionModel.open();
 		
 		// Total Workouts
 		userTotalWorkoutsText = (TextView) findViewById(R.id.user_total_workouts);
 		userTotalWorkoutsText.setTypeface(font);
 		if(model.getByAttribute("name") != null){
 			userTotalWorkoutsText.setText(this.getString(R.string.user_total_workouts) + " " + sessionModel.getTotal());
 		}
 		
 		// Last Workout
 		userLastWorkoutText = (TextView) findViewById(R.id.user_last_workout);
 		if(sessionModel.getMostRecent(null) != null){
 			Date date = new Date((sessionModel.getMostRecent(null).date_created));
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy h:mm a");
 			userLastWorkoutText.setText(this.getString(R.string.user_last_workout) + " " + formatter.format(date));
 		}
		else if(model.getByAttribute("name") != null){
			userLastWorkoutText.setText(this.getString(R.string.user_last_workout) + " N/A");
		}
 		userLastWorkoutText.setTypeface(font);
 		
 		// Total Achievements
 		userTotalAchievementsText = (TextView) findViewById(R.id.user_total_achievements);
 		userTotalAchievementsText.setTypeface(font);
 		if(model.getByAttribute("name") != null){
 			userTotalAchievementsText.setText(this.getString(R.string.user_total_achievements) + " " + achievementModel.getTotal());
 		}
 		
 		// Edit Profile button
 		View user_profile_button = findViewById(R.id.edit_profile_button);
 		user_profile_button.setOnClickListener(this);
 		Button fontButton = (Button) findViewById(R.id.edit_profile_button);
 		fontButton.setTypeface(font);
 		
 		
 		//Achievements button
 		View achievements_button = findViewById(R.id.achievements_button);
 		achievements_button.setOnClickListener(this);
 		fontButton = (Button) findViewById(R.id.achievements_button);
 		fontButton.setTypeface(font);
 		
 		model.close();
 	}
 
 	public void onClick(View v) 
 	{
 		Intent u;
 		switch (v.getId()) 
 		{
 		case R.id.edit_profile_button:
 			u = new Intent(this, EditUserProfileActivity.class);
 			startActivity(u);
 			break;
 		case R.id.achievements_button:
 			u = new Intent(this, AchievementActivity.class);
 			startActivity(u);
 			break;
 		}
 	}
 	
 	// Method for taking in photo from camera and setting as profile pic
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         if (requestCode == CAMERA_REQUEST && data != null) {  
             Bitmap photo = (Bitmap) data.getExtras().get("data");
             
             // CROPPING
             float scaleWidth = 1;
             float scaleHeight = 1;
             Matrix matrix = new Matrix();
             matrix.postScale(scaleWidth, scaleHeight);
             Bitmap resizedPhoto = Bitmap.createBitmap(photo, 0, photo.getWidth()/10 , photo.getWidth(), photo.getWidth(), matrix, true);
             photoButton.setImageBitmap(resizedPhoto);
             
             // Save file
             try {
             	file.createNewFile();
 				FileOutputStream out = new FileOutputStream(file);
 	            resizedPhoto.compress(Bitmap.CompressFormat.PNG, 90, out);
 	            out.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
         }
 	}
 	
 	//Back to frontpage method to make the skip from edit profile work more fluidly and stop 
 	//a back pressing cycle between the two pages.
 	@Override
 	public void onBackPressed() {
 			Intent u = new Intent(this, CrossFitrActivity.class);
 			startActivity(u);
 		}
 }
