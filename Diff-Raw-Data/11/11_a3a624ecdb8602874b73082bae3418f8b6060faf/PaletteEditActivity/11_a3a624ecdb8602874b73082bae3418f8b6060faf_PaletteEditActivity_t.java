 package com.tzapps.tzpalette.ui;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.tzapps.common.ui.OnFragmentStatusChangedListener;
 import com.tzapps.common.utils.BitmapUtils;
 import com.tzapps.common.utils.MediaHelper;
 import com.tzapps.common.utils.StringUtils;
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.data.PaletteData;
 import com.tzapps.tzpalette.data.PaletteDataHelper;
 import com.tzapps.tzpalette.debug.MyDebug;
 
 public class PaletteEditActivity extends Activity implements OnFragmentStatusChangedListener
 {
     private static final String TAG = "PaletteEditActivity";
     
     private PaletteData mCurrentData;
     private PaletteDataHelper mDataHelper;
     
     private PaletteEditFragment mEditFragment;
     private ProgressDialog mProgresDialog;
     
     private void init()
     {
         mDataHelper = PaletteDataHelper.getInstance(this);
         
         long dataId = getIntent().getExtras().getLong(MainActivity.PALETTE_DATA_ID);
         Uri imageUrl = getIntent().getData();
         
         if (dataId != -1)
         {
             mCurrentData = mDataHelper.get(dataId);
         }
         else if (imageUrl != null)
         {
             handlePicture(imageUrl);
            
            // cleanup the data so we will not re-handle the picture when rotate the screen
            getIntent().setData(null);
         }
         else
         {
             if (MyDebug.LOG)
                 Log.e(TAG, "cannot initialize to get corrent palette data");
         }
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         // Display the fragment as the main content
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new PaletteEditFragment())
                 .commit();
         
         // Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
         {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
         
         init();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.palette_edit_view_actions, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
                 
             case R.id.action_cancel:
                 doCancel();
                 return true;
                 
             case R.id.action_save:
                 doSave();
                 return true;
                 
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void onClick(View view)
     {
         switch (view.getId())
         {
             case R.id.btn_analysis:
                 analysisPicture();
                 break;
         }
     }
 
     @Override
     public void onFragmentViewCreated(Fragment fragment)
     {
         if (fragment instanceof PaletteEditFragment)
             mEditFragment = (PaletteEditFragment)fragment;
         
         updateEditVeiw(true);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
 
         if (mCurrentData != null)
             outState.putParcelable("currentPaletteData", mCurrentData);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState)
     {
         super.onRestoreInstanceState(savedInstanceState);
 
         mCurrentData = savedInstanceState.getParcelable("currentPaletteData");
 
         updateEditVeiw(true);
     }
     
     /** Called when the user performs the Analysis action */
     private void analysisPicture()
     {
         Log.d(TAG, "analysis the picture");
 
         if (mCurrentData == null)
             return;
 
         new PaletteDataAnalysisTask().execute(mCurrentData);
     }
     
     /** Called when the user performs the Cancel action */
     private void doCancel()
     {
         if (MyDebug.LOG)
             Log.d(TAG, "cancel the edit");
         
         setResult(RESULT_CANCELED);
         finish();
     }
     
     /** Called when the user performs the Save action */
     private void doSave()
     {
         if (MyDebug.LOG)
             Log.d(TAG, "save the edit");
 
         if (mCurrentData == null)
             return;
         
         long    id;
         boolean addNew;
 
         if (mCurrentData.getId() == -1)
         {
             id = mDataHelper.add(mCurrentData);
             addNew = true;
         }
         else
         {
             mDataHelper.update(mCurrentData, /*updateThumb*/false);
             
             id = mCurrentData.getId();
             addNew = false;
         }
         
         Intent data = new Intent();
         data.putExtra(MainActivity.PALETTE_DATA_ID, id);
         data.putExtra(MainActivity.PALETTE_DATA_ADDNEW, addNew);
         
         setResult(RESULT_OK, data);
         finish();
     }
     
     /** refresh edit fragment with persisted palette data */
     private void updateEditVeiw(boolean updatePicture)
     {
         if (mCurrentData == null || mEditFragment == null)
             return;
         
         if (updatePicture)
         {
             Bitmap bitmap    = null;
             String imagePath = mCurrentData.getImageUrl();
             Uri    imageUri  = imagePath == null ? null : Uri.parse(imagePath);
             
             bitmap = BitmapUtils.getBitmapFromUri(this, imageUri);
             
             if (bitmap != null)
             {
                 int orientation;
                 
                 /*
                  * This is a quick fix on picture orientation for the picture taken
                  * from the camera, as it will be always rotated to landscape 
                  * incorrectly even if we take it in portrait mode...
                  */
                 orientation = MediaHelper.getPictureOrientation(this, imageUri);
                 bitmap = BitmapUtils.getRotatedBitmap(bitmap, orientation);
                 
                 mEditFragment.updateImageView(bitmap);
             }
         }
         
         mEditFragment.updateColors(mCurrentData.getColors());
         mEditFragment.udpateTitle(mCurrentData.getTitle());
     }
     
     private String getPictureTilteFromUri(Uri uri)
     {
         String filename = null;
         
         String scheme = uri.getScheme();
         if (scheme.equals("file"))
         {
             filename = uri.getLastPathSegment();
         }
         else if (scheme.equals("content"))
         {
             String[] proj = {MediaStore.Images.Media.TITLE};
             
             Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
             
             if (cursor != null && cursor.getCount() != 0)
             {
                 int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                 cursor.moveToFirst();
                 filename = cursor.getString(columnIndex);
             }
             
             cursor.close();
         }
         
        // FIXME: sometimes we cannot parse and get image file name correctly
         if (StringUtils.isEmpty(filename))
             filename = getResources().getString(R.string.palette_title_default);
         
         return filename;
     }
     
     private void handlePicture(Uri imageUrl)
     {
         assert(imageUrl != null);
         
         mCurrentData = new PaletteData();
        
         //TODO something is still wrong on file title fetching logic
         mCurrentData.setTitle(getPictureTilteFromUri(imageUrl));
         mCurrentData.setImageUrl(imageUrl.toString());
         
         // start to analysis the picture immediately after loading it
         new PaletteDataAnalysisTask().execute(mCurrentData);
     }
 
     private void startAnalysis()
     {
         if (mProgresDialog == null)
             mProgresDialog = new ProgressDialog(this);
 
         mProgresDialog.setMessage(getResources().getText(R.string.analysis_picture_in_process));
         mProgresDialog.setIndeterminate(true);
         mProgresDialog.setCancelable(false);
         mProgresDialog.show();
     }
 
     private void stopAnalysis()
     {
         mProgresDialog.hide();
     }
 
     /**
      * This is a helper class used to do the palette data analysis process asynchronously
      */
     private class PaletteDataAnalysisTask extends AsyncTask<PaletteData, Void, PaletteData>
     {
         protected void onPreExecute()
         {
             super.onPreExecute();
             startAnalysis();
         }
 
         /*
          * The system calls this to perform work in a worker thread and delivers it the parameters
          * given to AsyncTask.execute()
          */
         protected PaletteData doInBackground(PaletteData... dataArray)
         {
             PaletteData data = dataArray[0];
             
             mDataHelper.analysis(data, /* reset */true);
 
             return data;
         }
 
         /*
          * The system calls this to perform work in the UI thread and delivers the result from
          * doInBackground()
          */
         protected void onPostExecute(PaletteData result)
         {
             stopAnalysis();
             updateEditVeiw(false);
         }
     }
     
 }
