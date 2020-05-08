 package com.warmice.android.videomessaging.ui;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.ActionBarSherlock;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 import com.warmice.android.videomessaging.R;
 import com.warmice.android.videomessaging.data.CurrentUser;
 import com.warmice.android.videomessaging.file.image.CurrentUserImage;
 import com.warmice.android.videomessaging.file.image.Image;
 import com.warmice.android.videomessaging.file.image.Image.ImageLoadedListener;
 import com.warmice.android.videomessaging.tools.networktasks.SignOutTask;
 import com.warmice.android.videomessaging.tools.networktasks.UpdateUserTask;
 import com.warmice.android.videomessaging.tools.networktasks.UpdateUserTask.UserUpdateListener;
 import com.warmice.android.videomessaging.ui.dialog.ProgressFragment;
 
 public class SlidingMenuActivity extends SlidingFragmentActivity implements
 		ImageLoadedListener, UserUpdateListener {
 
 	private final static int USER_ICON = 0;
 
 	private static final int LOAD_IMAGE = 0;
 	private static final int CROP_IMAGE = 1;
 
 	private ViewGroup mAccountMenu;
 	private ViewGroup mEditMenu;
 	private TextView mAccountName;
 	private TextView mAccountUsername;
 	private TextView mEditUsername;
	private TextView mEditFirstName;
	private TextView mEditLastName;
 	private ImageButton mEditPhoto;
 	private ImageView mPhoto;
 	private Bitmap mBitmap;
 	
 	private DialogFragment mProgressFragment;
 
 	private boolean isEditting;
 
 	private boolean mImageUpdated;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		displayAccountDetails();
 		loadImage();
 		buildMenuDropShadow();
 
 		mImageUpdated = false;
 		ActionBarSherlock sherlock = getSherlock();
 		sherlock.getActionBar().setDisplayHomeAsUpEnabled(true);
 		sherlock.getActionBar().setHomeButtonEnabled(true);
 	}
 
 	private void displayAccountDetails() {
 		if (mAccountMenu == null) {
 			loadAccountViews();
 			setupAccountMenuFields();
 		}
 		setBehindContentView(mAccountMenu);
 		displayUserIcon(mBitmap);
 		isEditting = false;
 	}
 
 	private void loadAccountViews() {
 		LayoutInflater inflater = LayoutInflater.from(this);
 		mAccountMenu = (ViewGroup) inflater
 				.inflate(R.layout.sliding_menu, null);
 		mAccountName = (TextView) mAccountMenu.findViewById(R.id.name);
 		mPhoto = (ImageView) mAccountMenu.findViewById(R.id.photo);
 		mAccountUsername = (TextView) mAccountMenu.findViewById(R.id.username);
 	}
 
 	private void setupAccountMenuFields() {
 		CurrentUser user = CurrentUser.load(this);
 		mAccountUsername.setText(user.username);
 		mAccountName.setText(user.getName());
 	}
 
 	private void displayEditView() {
 		if (mEditMenu == null) {
 			loadEditViews();
 			setupEditMenuFields();
 		}
 		setBehindContentView(mEditMenu);
 		displayUserIcon(mBitmap);
 		isEditting = true;
 	}
 
 	private void loadEditViews() {
 		LayoutInflater inflater = LayoutInflater.from(this);
 		mEditMenu = (ViewGroup) inflater.inflate(R.layout.sliding_menu_edit,
 				null);
		mEditFirstName = (TextView) mEditMenu.findViewById(R.id.first_name);
		mEditLastName = (TextView) mEditMenu.findViewById(R.id.last_name);
 		mEditPhoto = (ImageButton) mEditMenu.findViewById(R.id.photo);
 		mEditUsername = (TextView) mEditMenu.findViewById(R.id.username);
 	}
 
 	private void setupEditMenuFields() {
 		CurrentUser user = CurrentUser.load(this);
 		mEditUsername.setText(user.username);
 		mEditFirstName.setText(user.first_name);
 		mEditLastName.setText(user.last_name);
 	}
 
 	private void buildMenuDropShadow() {
 		SlidingMenu sm = getSlidingMenu();
 		sm.setShadowWidthRes(R.dimen.shadow_width);
 		sm.setShadowDrawable(R.drawable.shadow);
 		sm.setBehindOffsetRes(R.dimen.actionbar_home_width);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_contacts, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void onSideMenuItemSelected(View view) {
 		switch (view.getId()) {
 		case R.id.sign_out:
 			signOut();
 			break;
 		case R.id.edit:
 			displayEditView();
 			break;
 		case R.id.photo:
 			captureNewPhoto();
 			break;
 		case R.id.save:
 			updateUser();
 			break;
 		}
 	}
 
 	private void signOut() {
 		new SignOutTask(this).execute();
 		Intent intent = new Intent(this, SignInActivity.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 
 	private void captureNewPhoto() {
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("image/*");
 		startActivityForResult(intent, LOAD_IMAGE);
 	}
 
 	private void updateUser() {
 		displayProgress();
 		UpdateUserTask task = new UpdateUserTask(this, this);
 		if (mImageUpdated) {
 			task.setNewPicture();
 			mImageUpdated = false;
 		}
 		task.execute();
 	}
 
 	private void displayProgress() {
 		if (mProgressFragment == null) {
 			mProgressFragment = ProgressFragment.newInstance(null);
 		}
 	    mProgressFragment.show(getSupportFragmentManager(), "dialog");
 	}
 
 	@Override
 	public void onUpdateFinish() {
 		destroyProgress();
 		displayAccountDetails();
		setupEditMenuFields();
 	}
 
 	private void destroyProgress() {
 		mProgressFragment.dismiss();
 	}
 
 	protected boolean fieldIsEmpty(EditText editText) {
 		if (getTrimmedText(editText).length() > 0)
 			return false;
 		else
 			return true;
 	}
 
 	public String getTrimmedText(EditText editText) {
 		return editText.getText().toString().trim();
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (isBehindShowing() && isEditting) {
 			displayAccountDetails();
 			return true;
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_CANCELED) {
 			return;
 		}
 		switch (requestCode) {
 		case LOAD_IMAGE:
 			cropImage(data);
 			break;
 		case CROP_IMAGE:
 			loadImage();
 			break;
 		}
 
 	}
 
 	private void cropImage(Intent data) {
 		data.toString();
 		Uri contentUri = data.getData();
 		if (contentUri != null) {
 			Intent intent = new Intent(this, CropImageActivity.class);
 			intent.setData(contentUri);
 			startActivityForResult(intent, CROP_IMAGE);
 		}
 	}
 
 	private void loadImage() {
 		Image image = new CurrentUserImage(getApplicationContext());
 		image.setDimens(256, 256);
 		image.load(this, USER_ICON);
 	}
 
 	@Override
 	public void onImageLoaded(int imageId, Boolean succeeded, Bitmap bitmap) {
 		if (succeeded) {
 			switch (imageId) {
 			case USER_ICON:
 				mBitmap = bitmap;
 				mImageUpdated = true;
 				displayUserIcon(bitmap);
 				break;
 			}
 		}
 	}
 
 	private void displayUserIcon(Bitmap bitmap) {
 		if (bitmap != null) {
 			if (mEditPhoto != null) {
 				mEditPhoto.setImageBitmap(bitmap);
 			}
 
 			if (mPhoto != null) {
 				mPhoto.setImageBitmap(bitmap);
 			}
 		}
 	}
 
 }
