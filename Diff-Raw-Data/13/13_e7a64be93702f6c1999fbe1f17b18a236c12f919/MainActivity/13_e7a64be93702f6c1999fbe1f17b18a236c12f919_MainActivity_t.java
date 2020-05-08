 package com.tzapps.tzpalette.ui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ShareActionProvider;
 import android.widget.Toast;
 
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.data.PaletteData;
 import com.tzapps.tzpalette.data.PaletteDataHelper;
 import com.tzapps.tzpalette.ui.PaletteItemOptionsDialogFragment.OnClickPaletteItemOptionListener;
 import com.tzapps.tzpalette.ui.PaletteItemOptionsDialogFragment.PaletteItemOption;
 import com.tzapps.ui.OnFragmentStatusChangedListener;
 import com.tzapps.utils.ActivityUtils;
 import com.tzapps.utils.BitmapUtils;
 
 public class MainActivity extends Activity implements OnFragmentStatusChangedListener, OnClickPaletteItemOptionListener
 {
     private final static String TAG = "MainActivity";
     
     private final static String FOLDER_HOME = "tzpalette";
 
     /** Called when the user clicks the TakePicture button */
     static final int TAKE_PHOTE_RESULT = 1;
     /** Called when the user clicks the LoadPicture button */
     static final int LOAD_PICTURE_RESULT = 2;
 
     ProgressDialog mDialog;
     ViewPager mViewPager;
     TabsAdapter mTabsAdapter;
     
     PaletteData       mCurrentPalette;
     PaletteDataHelper mDataHelper;
     
     ShareActionProvider mShareActionProvider;
 
     CaptureFragment mCaptureFrag;
     PaletteListFragment mPaletteListFragment;
     CaptureFragment fragment3;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         ActivityUtils.forceToShowOverflowOptionsOnActoinBar(this);
         
         mDataHelper = PaletteDataHelper.getInstance(this);
 
         mViewPager = new ViewPager(this);
         mViewPager.setId(R.id.pager);
         setContentView(mViewPager);
 
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         mTabsAdapter = new TabsAdapter(this, mViewPager);
         mTabsAdapter.addTab(actionBar.newTab().setText("Capture"), CaptureFragment.class, null);
         mTabsAdapter.addTab(actionBar.newTab().setText("My Palettes"), PaletteListFragment.class, null);
         //mTabsAdapter.addTab(actionBar.newTab().setText("About"), CaptureFragment.class, null);
         
         // Get intent, action and MIME type
         Intent intent = getIntent();
         String action = intent.getAction();
         String type   = intent.getType();
         
         if (Intent.ACTION_SEND.equals(action) && type != null)
         {
             if (type.startsWith("image/"))
                 handleSendImage(intent);
         }
     }
     
     private void handleSendImage(Intent intent)
     {
         Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
         
         if (imageUri != null)
         {
             InputStream imageStream;
             Bitmap      bitmap = null;
             try
             {
                 imageStream = getContentResolver().openInputStream(imageUri);
                 bitmap = BitmapFactory.decodeStream(imageStream);
             }
             catch (FileNotFoundException e)
             {
                 e.printStackTrace();
                 Log.e(TAG, "load picture failed");
             }
             handlePicture(bitmap);
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
 
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
         
         // TODO: convert the PaletteData to parcelable
         
         if (mCurrentPalette != null)
         {
             outState.putParcelable("bitmap", mCurrentPalette.getThumb());
             outState.putIntArray("colors", mCurrentPalette.getColors());
         }
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState)
     {
         super.onRestoreInstanceState(savedInstanceState);
 
         mTabsAdapter.setSelectedTab(savedInstanceState.getInt("tab", 0));
 
         Bitmap bitmap = (Bitmap) savedInstanceState.getParcelable("bitmap");
         int[] colors = savedInstanceState.getIntArray("colors");
 
         if (mCurrentPalette == null)
             mCurrentPalette = new PaletteData();
 
         mCurrentPalette.setThumb(bitmap);
         mCurrentPalette.addColors(colors, /* reset */true);
 
         refresh();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.capture_view_actions, menu);
        
         /* TODO adjust menu items dynamically based on:
          * 1. the current tab
          * 2. whether a palette data exists
          */
         
         // Locate MenuItem with ShareActionProvider
         MenuItem item = menu.findItem(R.id.action_share);
         
         // Fetch and store ShareActionProvider
         mShareActionProvider = (ShareActionProvider) item.getActionProvider();
         
         // TODO adjust the share item contents based on the palette data
         return super.onCreateOptionsMenu(menu);
     }
     
     // Call to update the share intent
     private void setShareIntent(Intent shareIntent)
     {
         if (mShareActionProvider != null)
         {
             mShareActionProvider.setShareIntent(shareIntent);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         // Handle presses on the action bar items
         switch (item.getItemId())
         {
             case R.id.action_share:
                 sharePalette(item.getActionView());
                 return true;
                 
             case R.id.action_export:
                 exportPalette(item.getActionView());
                 return true;
             
             case R.id.action_takePhoto:
                 takePhoto(item.getActionView());
                 return true;
 
             case R.id.action_loadPicture:
                 loadPicture(item.getActionView());
                 return true;
 
             case R.id.action_settings:
                 // openSettings();
                 //sharePalette(item.getActionView());
                 return true;
 
             case R.id.action_about:
                 mTabsAdapter.setSelectedTab(2);
                 return true;
 
             case R.id.action_save:
                 savePalette(item.getActionView());
                 return true;
 
             case R.id.action_clear:
                 clearPalette(item.getActionView());
                 return true;
 
             case R.id.action_analysis:
                 analysisPicture(item.getActionView());
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void onClick(View view)
     {
         switch (view.getId())
         {
             case R.id.palette_item_options:
                 long dataId = (Long)view.getTag(R.id.TAG_PALETTE_DATA_ID);
                 int  itemPosition = (Integer)view.getTag(R.id.TAG_PALETTE_ITEM_POSITION);
                 
                 PaletteData data = mPaletteListFragment.getItem(itemPosition);
                 
                 Log.d(TAG, "Show options on palette data + " + data);
                 
                 PaletteItemOptionsDialogFragment optionDialogFrag =
                         PaletteItemOptionsDialogFragment.newInstance(data.getTitle(), itemPosition, dataId);
                 
                 //dialogFrag.getDialog().setCanceledOnTouchOutside(true);
                 optionDialogFrag.show(getFragmentManager(), "dialog");
                 break;
         }
     }
     
     private void updatePaletteDataTitle(int position, long dataId, String title)
     {
         assert(mPaletteListFragment != null);
         
         PaletteData data = mPaletteListFragment.getItem(position);
         
         if (data == null)
             return;
         
         data.setTitle(title);
         mDataHelper.update(data, /*updateThumb*/false);
         
         mPaletteListFragment.refresh();
     }
     
     private void showRenameDialog(final int position, final long dataId)
     {
         assert(mPaletteListFragment != null);
         
         PaletteData data = mPaletteListFragment.getItem(position);
         
         if (data == null)
             return;
         
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);        
         final EditText input = new EditText(this);
         
         input.setText(data.getTitle());
         input.setSingleLine(true);
         input.setSelectAllOnFocus(true);
 
         
         alert.setTitle(R.string.action_rename)
              .setView(input)
              .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which)
                   {
                       String text = input.getText().toString();
                       updatePaletteDataTitle(position, dataId, text);
                   }
               })
               .setNegativeButton("Cancel", null);
         
         final AlertDialog dialog = alert.create();
               
         input.setOnFocusChangeListener(new OnFocusChangeListener() 
             {
                 @Override
                 public void onFocusChange(View v, boolean hasFocus) 
                 {
                    if(hasFocus)
                    {
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                 }
             }
         );
         
         dialog.show();
     }
     
     @Override
     public void onClick(int position, long dataId, PaletteItemOption option)
     {
         assert(mPaletteListFragment != null);
         
         PaletteData data = mPaletteListFragment.getItem(position);
         
         if (data == null)
             return;
         
         switch(option)
         {
             case Rename:
                 Log.d(TAG, "Rename palatte item/data at position " + position + ", dataId is " + dataId);
                 showRenameDialog(position, dataId);
                 break;
                 
             case Delete:
                 Log.d(TAG, "Delete palatte item/data at position " + position + ", dataId is " + dataId);
                 mPaletteListFragment.remove(data);
                 mDataHelper.delete(data);
                 break;
         }
         
     }
     
     private void sharePalette(View view)
     {
         if (mCurrentPalette == null)
             return;
         
         View paletteCard = (View) findViewById(R.id.frame_palette_card);
         Bitmap bitmap = BitmapUtils.getBitmapFromView(paletteCard);
         
         assert(bitmap != null);
         
         //TODO make the share function work rather than just a trial version
         
         String name = FOLDER_HOME + File.separator + "share";
         
         File file = BitmapUtils.saveBitmapToSDCard(bitmap, name);
         
         Intent sendIntent = new Intent(Intent.ACTION_SEND);
         
         sendIntent.setType("image/jpeg");
         sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
         sendIntent.putExtra(Intent.EXTRA_TEXT, "My Palette");
         startActivity(Intent.createChooser(sendIntent, "share"));
     }
     
     private void exportPalette(View view)
     {
         if (mCurrentPalette == null)
             return;
         
         View paletteCard = (View) findViewById(R.id.frame_palette_card);
         Bitmap bitmap = BitmapUtils.getBitmapFromView(paletteCard);
         
         assert(bitmap != null);
         
         String title = mCurrentPalette.getTitle();
         
         if (title == null)
             title = getResources().getString(R.string.palette_title_default);
         
         BitmapUtils.saveBitmapToSDCard(bitmap, FOLDER_HOME + File.separator + title);
         
         Toast.makeText(this, "Palette <" + title + "> has been exported", Toast.LENGTH_SHORT).show();
     }
 
     /** refresh fragments in main activity with persisted mPaletteData */
     private void refresh()
     {
         if (mCaptureFrag != null && mCurrentPalette != null)
         {
             mCaptureFrag.updateImageView(mCurrentPalette.getThumb());
             mCaptureFrag.updateColors(mCurrentPalette.getColors());
         }
     }
 
     @Override
     public void onFragmentViewCreated(Fragment fragment)
     {
         if (fragment instanceof CaptureFragment)
             mCaptureFrag = (CaptureFragment) fragment;
         
         if (fragment instanceof PaletteListFragment)
         {
             mPaletteListFragment = (PaletteListFragment) fragment;
             mPaletteListFragment.addAll(mDataHelper.getAllData());
         }
 
         refresh();
     }
 
     /** Called when the user performs the Take Photo action */
     public void takePhoto(View view)
     {
         Log.d(TAG, "take a photo");
 
         if (ActivityUtils.isIntentAvailable(getBaseContext(), MediaStore.ACTION_IMAGE_CAPTURE))
         {
             Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             startActivityForResult(takePictureIntent, TAKE_PHOTE_RESULT);
         }
         else
         {
             Log.e(TAG, "no camera found");
         }
     }
 
     /** Called when the user performs the Load Picture action */
     public void loadPicture(View view)
     {
         Log.d(TAG, "load a picture");
         Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
         photoPickerIntent.setType("image/*");
         startActivityForResult(photoPickerIntent, LOAD_PICTURE_RESULT);
     }
 
     /** Called when the user performs the Analysis action */
     public void analysisPicture(View view)
     {
         Log.d(TAG, "analysis the picture");
 
         if (mCurrentPalette == null)
             return;
 
         new PaletteDataAnalysisTask().execute(mCurrentPalette);
     }
 
     /** Called when the user performs the Clear action */
     public void clearPalette(View view)
     {
         Log.d(TAG, "clear the picture");
 
         if (mCurrentPalette == null)
             return;
 
         mCurrentPalette.clear();
         refresh();
     }
 
     /** Called when the user performs the Save action */
     public void savePalette(View view)
     {
         Log.d(TAG, "save the palette");
 
         if (mCurrentPalette == null)
             return;
         
        if (mCurrentPalette.getId() == -1)
        {
            mDataHelper.add(mCurrentPalette);
             mPaletteListFragment.add(mCurrentPalette);
        }
        else
        {
            mDataHelper.update(mCurrentPalette, /*updateThumb*/false);
            mPaletteListFragment.refresh();
        }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         Bitmap bitmap = null;
 
         // Check which request we are responding to
         switch (requestCode)
         {
             case TAKE_PHOTE_RESULT:
                 if (resultCode == RESULT_OK)
                 {
                     Bundle extras = data.getExtras();
                     bitmap = (Bitmap) extras.get("data");
 
                     if (bitmap != null)
                         handlePicture(bitmap);
                 }
                 break;
 
             case LOAD_PICTURE_RESULT:
                 if (resultCode == RESULT_OK)
                 {
                     Uri selectedImage = data.getData();
 
                     if (selectedImage != null)
                     {
                         InputStream imageStream;
                         try
                         {
                             imageStream = getContentResolver().openInputStream(selectedImage);
                             bitmap = BitmapFactory.decodeStream(imageStream);
                         }
                         catch (FileNotFoundException e)
                         {
                             e.printStackTrace();
                             Log.e(TAG, "load picture failed");
                         }
                         handlePicture(bitmap);
                     }
                 }
                 break;
 
             default:
                 break;
         }
 
     }
 
     private void handlePicture(Bitmap bitmap)
     {
         assert (bitmap != null);
 
         if (mCurrentPalette == null)
         {
             mCurrentPalette = new PaletteData(bitmap);
         }
         else
         {
             mCurrentPalette.clearColors();
             mCurrentPalette.setThumb(bitmap);
         }
 
         refresh();
 
         // start to analysis the picture immediately after loading it
         new PaletteDataAnalysisTask().execute(mCurrentPalette);
     }
 
     private void startAnalysis()
     {
         if (mDialog == null)
             mDialog = new ProgressDialog(this);
 
         mDialog.setMessage(getResources().getText(R.string.analysis_picture_in_process));
         mDialog.setIndeterminate(false);
         mDialog.setCancelable(true);
         mDialog.show();
     }
 
     private void stopAnalysis()
     {
         mDialog.hide();
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
             refresh();
             stopAnalysis();
         }
     }
 
     /**
      * This is a helper class that implements the management of tabs and all details of connecting a
      * ViewPager with associated TabHost. It relies on a trick. Normally a tab host has a simple API
      * for supplying a View or Intent that each tab will show. This is not sufficient for switching
      * between pages. So instead we make the content part of the tab host 0dp high (it is not shown)
      * and the TabsAdapter supplies its own dummy view to show as the tab content. It listens to
      * changes in tabs, and takes care of switch to the correct paged in the ViewPager whenever the
      * selected tab changes.
      */
     public static class TabsAdapter extends FragmentPagerAdapter
             implements ActionBar.TabListener, ViewPager.OnPageChangeListener
     {
         private final Context mContext;
         private final ActionBar mActionBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
         static final class TabInfo
         {
             private final Class<?> clss;
             private final Bundle args;
 
             TabInfo(Class<?> _class, Bundle _args)
             {
                 clss = _class;
                 args = _args;
             }
         }
 
         public TabsAdapter(Activity activity, ViewPager pager)
         {
             super(activity.getFragmentManager());
             mContext = activity;
             mActionBar = activity.getActionBar();
             mViewPager = pager;
             mViewPager.setAdapter(this);
             mViewPager.setOnPageChangeListener(this);
         }
 
         public void setSelectedTab(int position)
         {
             mActionBar.setSelectedNavigationItem(position);
         }
 
         public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args)
         {
             TabInfo info = new TabInfo(clss, args);
             tab.setTag(info);
             tab.setTabListener(this);
 
             mTabs.add(info);
             mActionBar.addTab(tab);
 
             notifyDataSetChanged();
         }
 
         @Override
         public int getCount()
         {
             return mTabs.size();
         }
 
         @Override
         public Fragment getItem(int position)
         {
             TabInfo info = mTabs.get(position);
 
             return Fragment.instantiate(mContext, info.clss.getName(), info.args);
         }
 
         @Override
         public void onPageSelected(int position)
         {
             mActionBar.setSelectedNavigationItem(position);
         }
 
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
         {}
 
         @Override
         public void onPageScrollStateChanged(int state)
         {}
 
         @Override
         public void onTabSelected(Tab tab, FragmentTransaction ft)
         {
             mViewPager.setCurrentItem(tab.getPosition());
         }
 
         @Override
         public void onTabUnselected(Tab tab, FragmentTransaction ft)
         {}
 
         @Override
         public void onTabReselected(Tab tab, FragmentTransaction ft)
         {}
     }
 }
