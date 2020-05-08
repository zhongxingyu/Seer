 package com.strollimo.android.view;
 
 import android.app.ActionBar;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import com.google.zxing.config.ZXingLibConfig;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.strollimo.android.R;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.controller.AccomplishableController;
 import com.strollimo.android.controller.PhotoUploadController;
 import com.strollimo.android.controller.UserService;
 import com.strollimo.android.model.BaseAccomplishable;
 import com.strollimo.android.model.Mystery;
 import com.strollimo.android.model.Secret;
 import com.strollimo.android.network.AmazonUrl;
 import com.strollimo.android.network.StrollimoApi;
 import com.strollimo.android.network.response.PickupSecretResponse;
 import com.strollimo.android.util.BitmapUtils;
 import com.viewpagerindicator.CirclePageIndicator;
 import retrofit.Callback;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DetailsActivity extends FragmentActivity {
     public static final String TAG = DetailsActivity.class.getSimpleName();
 
     public static final String PLACE_ID_EXTRA = "place_id";
     private static final int TEMPORARY_TAKE_PHOTO = 15;
 
     private ZXingLibConfig zxingLibConfig;
     private AccomplishableController mAccomplishableController;
     private UserService mUserService;
     private StrollimoPreferences mPrefs;
     private Mystery mCurrentMystery;
     private Secret mSelectedSecret;
     private ViewPager mViewPager;
     private SecretSlideAdapter mPagerAdapter;
     private File mImage;
 
     public static Intent createDetailsIntent(Context context, String placeId) {
         Intent intent = new Intent(context, DetailsActivity.class);
         intent.putExtra(PLACE_ID_EXTRA, placeId);
         return intent;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         setContentView(R.layout.details_screen);
 
         mAccomplishableController = ((StrollimoApplication) getApplication()).getService(AccomplishableController.class);
         mUserService = ((StrollimoApplication) getApplication()).getService(UserService.class);
         mPrefs = ((StrollimoApplication) getApplication()).getService(StrollimoPreferences.class);
         zxingLibConfig = new ZXingLibConfig();
         zxingLibConfig.useFrontLight = true;
 
         mViewPager = (ViewPager) findViewById(R.id.secret_pager);
         mViewPager.setPageTransformer(true, new DepthPageTransformer());
         mCurrentMystery = mAccomplishableController.getMysteryById(getIntent().getStringExtra(PLACE_ID_EXTRA));
         mPagerAdapter = new SecretSlideAdapter(getSupportFragmentManager(), getApplicationContext(), mCurrentMystery);
         mViewPager.setAdapter(mPagerAdapter);
         mPagerAdapter.setOnSecretClickListener(new OnSecretClickListener() {
             @Override
             public void onSecretClicked(Secret secret) {
                 mSelectedSecret = secret;
                 launchPickupActivity(mSelectedSecret.getId());
 
             }
         });
         String title = mCurrentMystery == null ? "Error" : mCurrentMystery.getName().toUpperCase();
         actionBar.setTitle(title);
 
 
         CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.page_indicator);
         indicator.setViewPager(mViewPager);
         indicator.setSnap(true);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mPagerAdapter.notifyDataSetChanged();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (resultCode != RESULT_OK) {
             return;
         }
 
         switch (requestCode) {
             case IntentIntegrator.REQUEST_CODE:
                 IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
                         resultCode, data);
                 if (scanResult == null) {
                     return;
                 }
                 String result = scanResult.getContents();
                 handleResult(mCurrentMystery.isScannedCodeValid(result));
                 break;
             case PhotoCaptureActivity.REQUEST_CODE:
                 handleResult(PhotoCaptureActivity.getResult(requestCode, resultCode, data));
                 break;
             case TEMPORARY_TAKE_PHOTO:
                 final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Uploading photo for checking...");
                 progressDialog.show();
 
                 Bitmap bitmap = BitmapUtils.getBitmapFromFile(mImage, 800, 600);
                 final AmazonUrl pickupPhotoUrl = AmazonUrl.createPickupPhotoUrl(mSelectedSecret.getId(), mPrefs.getDeviceUUID());
                 StrollimoApplication.getService(PhotoUploadController.class).asyncUploadPhotoToAmazon(pickupPhotoUrl, bitmap, new PhotoUploadController.Callback() {
                     @Override
                     public void onSuccess() {
                         mUserService.captureSecret(mSelectedSecret);
                         mPagerAdapter.notifyDataSetChanged();
                         StrollimoApplication.getService(StrollimoApi.class).pickupSecret(mSelectedSecret, pickupPhotoUrl.getUrl(), new Callback<PickupSecretResponse>() {
                             @Override
                             public void success(PickupSecretResponse pickupSecretResponse, Response response) {
                                 mSelectedSecret.setStatus(BaseAccomplishable.Status.PENDING);
                                 mAccomplishableController.saveAllData();
                                 mPagerAdapter.notifyDataSetChanged();
                                 progressDialog.dismiss();
                             }
 
                             @Override
                             public void failure(RetrofitError retrofitError) {
                                 mPagerAdapter.notifyDataSetChanged();
                                 progressDialog.dismiss();
                             }
                         });
                     }
 
                     @Override
                     public void onError(Exception ex) {
                         progressDialog.dismiss();
                     }
                 });
 
 
             default:
         }
     }
 
     private void handleResult(boolean captureSuccessful) {
         if (captureSuccessful) {
             // TODO: recreate these dialogs
             mUserService.captureSecret(mSelectedSecret);
 //            TreasureFoundDialog dialog = new TreasureFoundDialog(placesFound, placesCount, coinValue, levelUp, levelText);
 //            dialog.show(getFragmentManager(), "dialog");
         } else {
             // TODO: recreate these dialogs
 //            new TreasureNotFoundDialog().show(getFragmentManager(), "dialog");
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         if (mPrefs.isDebugModeOn()) {
             getMenuInflater().inflate(R.menu.main_options, menu);
         }
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (mPrefs.isDebugModeOn()) {
             menu.findItem(R.id.use_barcode).setChecked(mPrefs.isUseBarcode());
         }
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 onBackPressed();
                 return true;
             case R.id.use_barcode:
                 boolean checked = item.isChecked();
                 mPrefs.setUseBarcode(!checked);
                 item.setChecked(!checked);
                 return true;
             case R.id.add_secret:
                 launchAddSecret();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void launchAddSecret() {
         Intent intent = new Intent(this, AddSecretActivity.class);
         intent.putExtra(PLACE_ID_EXTRA, mCurrentMystery.getId());
         startActivity(intent);
     }
 
     public void launchPickupActivity(String secretId) {
         try {
             File folder = new File(Environment.getExternalStorageDirectory() + "/strollimoTmpPickImg");
             if (!folder.exists()) {
                 folder.mkdir();
             }
             mImage = new File(folder + "/" + secretId + ".jpg");
             Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImage));
             startActivityForResult(takePictureIntent, TEMPORARY_TAKE_PHOTO);
         } catch (Exception e) {
             Log.e(TAG, "LunchPickupActivity error " + e.toString());
         }
         // Temporarily disabling capture modes
 //        if (mPrefs.isUseBarcode()) {
 //            IntentIntegrator integrator = new IntentIntegrator(this);
 //            integrator.initiateScan();
 //        } else {
 //            PhotoCaptureActivity.initiatePhotoCapture(this, secretId);
 //        }
     }
 
     private String createFilename() {
         String timeStamp =
                 new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         String imageFileName = "pic_big_1" + timeStamp + "_";
         return imageFileName;
     }
 
     public interface OnSecretClickListener {
         public void onSecretClicked(Secret secret);
     }
 
 }
