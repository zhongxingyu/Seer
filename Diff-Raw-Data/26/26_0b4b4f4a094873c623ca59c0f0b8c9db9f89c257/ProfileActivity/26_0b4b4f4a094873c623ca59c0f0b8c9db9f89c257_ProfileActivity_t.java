 package com.bjorsond.android.timeline;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.swarmconnect.Swarm;
 import com.swarmconnect.SwarmActivity;
 
 /**
  * The activity for the user's profile
  * 
  * @author Bjrnar
  *
  */
 
 public class ProfileActivity extends SwarmActivity{
 
 	private ImageView profilePicture, levelImage;
 	private ImageButton homeButton;
 	private TextView showPointsAboveProgressBar, userNameField, numberOfAchievements;
 	private ProgressBar levelProgressBar;
 	private Bitmap Image = null;
 	private Bitmap rotateImage = null;
 	private static final int GALLERY = 1;
 	
 	
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profileactivitylayout);
 		
 		//Setting Swarm active
 		Swarm.setActive(this);
 		
 		setupViews();
 		setupLevelAndPoints();
 	}
 	
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == GALLERY && resultCode != 0) {
 		    Uri mImageUri = data.getData();        
 		    try {
 		    	Image = Media.getBitmap(this.getContentResolver(), mImageUri);
 		        if (getOrientation(getApplicationContext(), mImageUri) != 0) {
 		        	Matrix matrix = new Matrix();
 		        	matrix.postRotate(getOrientation(getApplicationContext(), mImageUri));
 		        	if (rotateImage != null) rotateImage.recycle();
 		        	rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
 		        	profilePicture.setImageBitmap(rotateImage);
 		        } else
 		        	profilePicture.setImageBitmap(Image);        
 		      	} catch (FileNotFoundException e) {
 		      		e.printStackTrace();
 		      	} catch (IOException e) {
 		      		e.printStackTrace();
 		      	}
     	}
 	}
 	
 	/**
 	 * If an image is taken in horizontal angle, this method flips it and displays correctly
 	 * @param context 
 	 * @param photoUri
 	 * @return
 	 */
 	public static int getOrientation(Context context, Uri photoUri) {
 	    Cursor cursor = context.getContentResolver().query(photoUri,
 	        new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);
 	 
 	    if (cursor.getCount() != 1) {
 	    	return -1;
 	    }
 	    cursor.moveToFirst();
 	    return cursor.getInt(0);
 	}
 	
 	
 	
 	/**
 	 * The method that updates levels and points according to what the user has acquired
 	 */
 	public void setupLevelAndPoints(){
 		int[] levelAndPoints = new int[]{0, 0, 0};
 		levelAndPoints = DashboardActivity.getLevelAndPoints();
 		Log.i("levelAndPoints content", levelAndPoints.toString());
		profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_start));
 		//Setting up level
 		levelImage = (ImageView) findViewById(R.id.levelImageView);
 		if(levelAndPoints[0] == 1){
 			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_one));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_start));
 		}else if(levelAndPoints[0] == 2){
 			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_two));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_one));
 		}else if(levelAndPoints[0] == 3){
 			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_three));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_one));
 		}else if(levelAndPoints[0] == 4){
 			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_four));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_two));
 		}else if(levelAndPoints[0] == 5){
 			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_five));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_two));
 		}else if(levelAndPoints[0] == 6){
 			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_six));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_three));
		}else if(levelAndPoints[0] == 7){
			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_seven));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_three));
		}else if(levelAndPoints[0] == 8){
			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_eight));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_four));
		}else if(levelAndPoints[0] == 9){
			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_nine));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_four));
		}else if(levelAndPoints[0] == 10){
			levelImage.setImageDrawable(getResources().getDrawable(R.drawable.profile_level_ten));
			profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.profile_picture_five));
 		}
 		
 		//Setting up progressbar and points above bar
 		levelProgressBar = (ProgressBar) findViewById(R.id.levelProgressBar);
 		levelProgressBar.setVisibility(ProgressBar.VISIBLE);
 		levelProgressBar.setMax(levelAndPoints[2]);
 		levelProgressBar.setProgress(levelAndPoints[1]);
 
 		showPointsAboveProgressBar = (TextView) findViewById(R.id.pointsInProgressBar);
 		showPointsAboveProgressBar.setText(levelAndPoints[1] + "/" + levelAndPoints[2]);
 	}
 	
 	
 	
 	
 	private void setupViews() {
 		profilePicture = (ImageView) findViewById(R.id.profilePicture);
 		
 		userNameField = (TextView) findViewById(R.id.ProfileName);
 		userNameField.setText(Swarm.user.username);
 		
 		numberOfAchievements = (TextView) findViewById(R.id.ProfileAchievementCountNumber);
 		numberOfAchievements.setText("" + Swarm.user.points);
 		
 		homeButton = (ImageButton) findViewById(R.id.profileHeaderHomeButton);
 		homeButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				finish();
 			}
 		});
 	}
 }
