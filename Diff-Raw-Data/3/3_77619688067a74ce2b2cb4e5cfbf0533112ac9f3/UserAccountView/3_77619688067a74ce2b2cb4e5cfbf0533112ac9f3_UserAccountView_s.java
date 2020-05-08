 package bu.edu.cs673.edukid.settings.useraccount;
 
 import java.io.IOException;
 import java.util.List;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.media.*;
 import bu.edu.cs673.edukid.R;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.ImageUtils;
 import bu.edu.cs673.edukid.db.model.UserAccount;
 
 /**
  * The view which contains the user account information. The user account can be
  * seen and edited here.
  * 
  * @author Peter Trevino
  * 
  * @see UserAccount
  * 
  */
 public class UserAccountView extends Activity implements OnClickListener {
 
 	private static final int TAKE_PICTURE = 1888;
 
 	private boolean mStartRecording = true;
 
 	private static final long DATABASE_ERROR = -1;
 
 	private String userName;
 
 	private ImageView userImage;
 
 	private ImageView micImage;
 
 	private Database database = Database.getInstance(this);
 
 	public MediaRecorder recorder = new MediaRecorder();
 	private String mFileName = "";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_account);
 		userImage = (ImageView) findViewById(R.id.createUserImage);
 		micImage = (ImageView) findViewById(R.id.accountCreationRecorderButton);
 
 		// Populate user account info from database (if any)
 		List<UserAccount> userAccounts = database.getUserAccounts();
 
 		if (userAccounts.size() == 1) {
 			UserAccount userAccount = userAccounts.get(0);
 
 			// Set user name
 			EditText userNameTextField = ((EditText) findViewById(R.id.createEditChildName));
 			userNameTextField.setText(userAccount.getUserName());
 
 			// Set user image
 			userImage.setImageDrawable(ImageUtils
 					.byteArrayToDrawable(userAccount.getUserImage()));
 		}
 
 		// Add listeners
 		Button createSaveButton = (Button) findViewById(R.id.createSaveButton);
 		createSaveButton.setOnClickListener(this);
 		ImageButton createUploadPhotoButton = (ImageButton) findViewById(R.id.createUploadPhotoButton);
 		createUploadPhotoButton.setOnClickListener(this);
 		micImage.setOnClickListener(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.createSaveButton:
 			saveUserAccount();
 			break;
 		case R.id.createUploadPhotoButton:
 			// TODO: we should have other options other than the camera like
 			// picking from the camera roll
 			startCamera();
 			break;
 		case R.id.accountCreationRecorderButton:
 			// TODO:have state of button switch between start and stop recording
 			onRecord(mStartRecording);
 			mStartRecording = !mStartRecording;
 			break;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
 			Bitmap photo = (Bitmap) data.getExtras().get("data");
 
 			if (photo != null) {
 				userImage.setImageBitmap(photo);
 			}
 		}
 	}
 
 	/**
 	 * Saves the user account in the database.
 	 */
 	private void saveUserAccount() {
 		userName = ((EditText) findViewById(R.id.createEditChildName))
 				.getText().toString();
 		List<UserAccount> userAccounts = database.getUserAccounts();
 		long result = DATABASE_ERROR;
 
 		if (userAccounts.size() == 0) {
 			// TODO: Peter: replace "" with real user sound.
 			result = database.addUserAccount(userName, "",
 					userImage.getDrawable());
 		} else if (userAccounts.size() == 1) {
 			UserAccount userAccount = userAccounts.get(0);
 			userAccount.setUserName(userName);
 			userAccount.setUserImage(ImageUtils.drawableToByteArray(userImage
 					.getDrawable()));
 			result = database.editUserAccount(userAccount);
 		} else {
 			// TODO: implement more than 1 user. Should not get here now.
 		}
 
 		if (result != DATABASE_ERROR) {
 			Toast.makeText(this, "User account saved successfully!",
 					Toast.LENGTH_LONG).show();
 		} else {
 			// TODO: inform user of error
 		}
 	}
 
 	/**
 	 * Starts the front facing camera to take a picture.
 	 */
 	private void startCamera() {
 		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 		startActivityForResult(intent, TAKE_PICTURE);
 	}
 
 	public void onRecord(boolean start) {
 		if (start) {
 			startRecording();
 
 		} else {
 			stopRecording();
 		}
 	}
 
 	private void startRecording() {
 		micImage.setBackgroundResource(R.drawable.abacus);
 		recorder = new MediaRecorder();
 		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
 		mFileName += "/audiorecordtest.3gp";
 		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 		recorder.setOutputFile(mFileName);
 		try {
 			recorder.prepare();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		recorder.start();
 	}
 
 	private void stopRecording() {
 		micImage.setBackgroundResource(R.drawable.mikebutton);
 		recorder.stop();
 		recorder.release();
 		recorder = null;
 	}
 
 }
