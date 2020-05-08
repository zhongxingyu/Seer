 package com.chute.android.photopickerplus.app;
 
 import java.io.File;
import java.io.FileNotFoundException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.chute.android.photopickerplus.R;
 import com.chute.android.photopickerplus.dao.MediaDAO;
 import com.chute.android.photopickerplus.util.AppUtil;
 import com.chute.android.photopickerplus.util.Constants;
 import com.chute.android.photopickerplus.util.NotificationUtil;
 import com.chute.android.photopickerplus.util.PreferenceUtil;
 import com.chute.android.photopickerplus.util.intent.AlbumsActivityIntentWrapper;
 import com.chute.android.photopickerplus.util.intent.IntentUtil;
 import com.chute.android.photopickerplus.util.intent.PhotoStreamActivityIntentWrapper;
 import com.chute.sdk.api.GCHttpCallback;
 import com.chute.sdk.api.account.GCAccounts;
 import com.chute.sdk.api.authentication.GCAuthenticationFactory.AccountType;
 import com.chute.sdk.collections.GCAccountsCollection;
 import com.chute.sdk.model.GCAccountMediaModel;
 import com.chute.sdk.model.GCAccountModel;
 import com.chute.sdk.model.GCAccountStore;
 import com.chute.sdk.model.GCHttpRequestParameters;
 import com.darko.imagedownloader.ImageLoader;
 
 public class ChooseServiceActivity extends Activity {
 
     public static final String TAG = ChooseServiceActivity.class.getSimpleName();
 
     private TextView txtFacebook;
     private TextView txtPicasa;
     private TextView txtFlickr;
     private TextView txtInstagram;
     private LinearLayout take_photos;
     private LinearLayout facebook;
     private LinearLayout picasa;
     private LinearLayout instagram;
     private LinearLayout flickr;
     private LinearLayout allPhotos;
     private LinearLayout cameraPhotos;
     private ImageView img_all_photos;
     private ImageView img_camera_photos;
     private AccountType accountType;
     private ImageLoader loader;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.service_layout);
 
 	loader = ImageLoader.get(ChooseServiceActivity.this);
 
 	txtFacebook = (TextView) findViewById(R.id.txt_facebook);
 	txtFacebook.setTag(AccountType.FACEBOOK);
 	txtPicasa = (TextView) findViewById(R.id.txt_picasa);
 	txtPicasa.setTag(AccountType.PICASA);
 	txtFlickr = (TextView) findViewById(R.id.txt_flickr);
 	txtFlickr.setTag(AccountType.FLICKR);
 	txtInstagram = (TextView) findViewById(R.id.txt_instagram);
 	txtInstagram.setTag(AccountType.INSTAGRAM);
 
 	facebook = (LinearLayout) findViewById(R.id.linear_fb);
 	facebook.setTag(AccountType.FACEBOOK);
 	flickr = (LinearLayout) findViewById(R.id.linear_flickr);
 	flickr.setTag(AccountType.FLICKR);
 	picasa = (LinearLayout) findViewById(R.id.linear_picasa);
 	picasa.setTag(AccountType.PICASA);
 	instagram = (LinearLayout) findViewById(R.id.linear_instagram);
 	instagram.setTag(AccountType.INSTAGRAM);
 
 	allPhotos = (LinearLayout) findViewById(R.id.all_photos_linear);
 	allPhotos.setOnClickListener(new OnPhotoStreamListener());
 
 	cameraPhotos = (LinearLayout) findViewById(R.id.camera_shots_linear);
 	cameraPhotos.setOnClickListener(new OnCameraRollListener());
 
 	img_all_photos = (ImageView) findViewById(R.id.all_photos_icon);
 	img_camera_photos = (ImageView) findViewById(R.id.camera_shots_icon);
 
 	Cursor cursorAllPhotos = MediaDAO.getAllMediaPhotos(ChooseServiceActivity.this);
 	if (cursorAllPhotos != null && cursorAllPhotos.moveToFirst()) {
 	    String path = cursorAllPhotos.getString(cursorAllPhotos
 		    .getColumnIndex(MediaStore.Images.Media.DATA));
 	    loader.displayImage(Uri.fromFile(new File(path)).toString(), img_all_photos);
 	}
 
 	Cursor cursorCameraPhotos = MediaDAO.getCameraPhotos(ChooseServiceActivity.this);
 	if (cursorCameraPhotos != null && cursorCameraPhotos.moveToFirst()) {
 	    String path = cursorCameraPhotos.getString(cursorCameraPhotos
 		    .getColumnIndex(MediaStore.Images.Media.DATA));
 	    loader.displayImage(Uri.fromFile(new File(path)).toString(), img_camera_photos);
 	}
 
 	take_photos = (LinearLayout) findViewById(R.id.album3_linear);
 	take_photos.setOnClickListener(new OnCameraClickListener());
 
 	facebook.setOnClickListener(new OnLoginClickListener());
 	picasa.setOnClickListener(new OnLoginClickListener());
 	flickr.setOnClickListener(new OnLoginClickListener());
 	instagram.setOnClickListener(new OnLoginClickListener());
     }
 
     private final class OnLoginClickListener implements OnClickListener {
 
 	@Override
 	public void onClick(View v) {
 	    accountType = (AccountType) v.getTag();
 	    if (PreferenceUtil.get().hasAccountId(accountType)) {
 		accountClicked(PreferenceUtil.get().getAccountId(accountType));
 	    } else {
 		GCAccountStore.getInstance(getApplicationContext()).startAuthenticationActivity(
 			ChooseServiceActivity.this, accountType, Constants.PERMISSIONS_SCOPE,
 			Constants.CALLBACK_URL, Constants.CLIENT_ID, Constants.CLIENT_SECRET);
 	    }
 	}
     }
 
     private final class AccountsCallback implements GCHttpCallback<GCAccountsCollection> {
 
 	@Override
 	public void onSuccess(GCAccountsCollection responseData) {
 	    if (accountType == null) {
 		return;
 	    }
 	    for (GCAccountModel accountModel : responseData) {
 		if (accountModel.getType().equalsIgnoreCase(accountType.getName())) {
 		    PreferenceUtil.get().setNameForAccount(accountType,
 			    accountModel.getUser().getName());
 		    PreferenceUtil.get().setIdForAccount(accountType, accountModel.getId());
 		    accountClicked(accountModel.getId());
 		}
 	    }
 	}
 
 	@Override
 	public void onHttpException(GCHttpRequestParameters params, Throwable exception) {
 	}
 
 	@Override
 	public void onHttpError(int responseCode, String statusMessage) {
 	}
 
 	@Override
 	public void onParserException(int responseCode, Throwable exception) {
 	}
 
     }
 
     public void accountClicked(String accountId) {
 	AlbumsActivityIntentWrapper wrapper = new AlbumsActivityIntentWrapper(
 		ChooseServiceActivity.this);
 	wrapper.setAccountId(accountId);
 	wrapper.startActivity(ChooseServiceActivity.this);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	super.onActivityResult(requestCode, resultCode, data);
 	if (resultCode == Activity.RESULT_OK) {
 	    if (requestCode == GCAccountStore.AUTHENTICATION_REQUEST_CODE) {
 		GCAccounts.all(getApplicationContext(), new AccountsCallback()).executeAsync();
 	    }
 	    if (requestCode == PhotoStreamActivityIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY) {
 		finish();
 	    } else if (requestCode == Constants.CAMERA_PIC_REQUEST) {
 		// Bitmap image = (Bitmap) data.getExtras().get("data");
		String path = "";
		File tempFile = AppUtil.getTempFile(getApplicationContext());
		if (AppUtil.hasImageCaptureBug() == false && tempFile.length() > 0) {
		    Uri u;
		    try {
			u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(
				getContentResolver(), tempFile.getAbsolutePath(), null, null));
			tempFile.delete();
			path = u.toString();
		    } catch (FileNotFoundException e) {
			Log.d(TAG, "", e);
		    }
 		} else {
 		    Log.e(TAG, "Bug " + data.getData().getPath());
		    path = Uri.fromFile(
			    new File(AppUtil.getPath(getApplicationContext(), data.getData())))
			    .toString();
 		}
 		Log.d(TAG, path);
 		final GCAccountMediaModel model = new GCAccountMediaModel();
 		model.setLargeUrl(path);
 		model.setThumbUrl(path);
 		model.setUrl(path);
 		IntentUtil.deliverDataToInitialActivity(this, model);
 	    }
 	}
     }
 
     private class OnCameraClickListener implements OnClickListener {
 
 	@Override
 	public void onClick(View v) {
 	    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
 		NotificationUtil.makeToast(getApplicationContext(), R.string.toast_feature_camera);
 		return;
 	    }
 	    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 	    if (AppUtil.hasImageCaptureBug() == false) {
 		intent.putExtra(MediaStore.EXTRA_OUTPUT,
 			Uri.fromFile(AppUtil.getTempFile(ChooseServiceActivity.this)));
 	    } else {
 		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
 			android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 	    }
 	    startActivityForResult(intent, Constants.CAMERA_PIC_REQUEST);
 	}
     }
 
     private final class OnPhotoStreamListener implements OnClickListener {
 
 	@Override
 	public void onClick(View v) {
 	    final PhotoStreamActivityIntentWrapper streamWrapper = new PhotoStreamActivityIntentWrapper(
 		    ChooseServiceActivity.this);
 	    streamWrapper.setFilterType(PhotoStreamActivityIntentWrapper.TYPE_ALL_PHOTOS);
 	    streamWrapper.startActivityForResult(ChooseServiceActivity.this,
 		    PhotoStreamActivityIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
 	}
     }
 
     private final class OnCameraRollListener implements OnClickListener {
 
 	@Override
 	public void onClick(View v) {
 	    final PhotoStreamActivityIntentWrapper streamWrapper = new PhotoStreamActivityIntentWrapper(
 		    ChooseServiceActivity.this);
 	    streamWrapper.setFilterType(PhotoStreamActivityIntentWrapper.TYPE_CAMERA_ROLL);
 	    streamWrapper.startActivityForResult(ChooseServiceActivity.this,
 		    PhotoStreamActivityIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
 	}
 
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
 	super.onNewIntent(intent);
 	setResult(Activity.RESULT_OK, new Intent().putExtras(intent.getExtras()));
 	finish();
     }
 
     @Override
     protected void onResume() {
 	super.onResume();
 	if (PreferenceUtil.get().hasAccountId(AccountType.PICASA)) {
 	    if (PreferenceUtil.get().hasAccountName(AccountType.PICASA)) {
 		txtPicasa.setText(PreferenceUtil.get().getAccountName(AccountType.PICASA));
 
 	    }
 	}
 	if (PreferenceUtil.get().hasAccountId(AccountType.FACEBOOK)) {
 	    if (PreferenceUtil.get().hasAccountName(AccountType.FACEBOOK)) {
 		txtFacebook.setText(PreferenceUtil.get().getAccountName(AccountType.FACEBOOK));
 	    }
 	}
 	if (PreferenceUtil.get().hasAccountId(AccountType.FLICKR)) {
 	    if (PreferenceUtil.get().hasAccountName(AccountType.FLICKR)) {
 		txtFlickr.setText(PreferenceUtil.get().getAccountName(AccountType.FLICKR));
 	    }
 	}
 	if (PreferenceUtil.get().hasAccountId(AccountType.INSTAGRAM)) {
 	    if (PreferenceUtil.get().hasAccountName(AccountType.INSTAGRAM)) {
 		txtInstagram.setText(PreferenceUtil.get().getAccountName(AccountType.INSTAGRAM));
 	    }
 	}
     }
 }
