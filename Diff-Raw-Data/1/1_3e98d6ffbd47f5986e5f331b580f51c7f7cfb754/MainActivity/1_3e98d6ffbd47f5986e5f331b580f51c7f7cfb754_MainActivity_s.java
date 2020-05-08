 package com.tzapps.tzpalette.ui;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
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
 
 import com.tzapps.common.ui.OnFragmentStatusChangedListener;
 import com.tzapps.common.utils.ActivityUtils;
 import com.tzapps.common.utils.BitmapUtils;
 import com.tzapps.tzpalette.Constants;
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.data.PaletteData;
 import com.tzapps.tzpalette.data.PaletteDataComparator.Sorter;
 import com.tzapps.tzpalette.data.PaletteDataHelper;
 import com.tzapps.tzpalette.debug.DebugInfo;
 import com.tzapps.tzpalette.debug.MyDebug;
 import com.tzapps.tzpalette.ui.PaletteListFragment.OnClickPaletteItemListener;
 import com.tzapps.tzpalette.ui.dialog.PaletteDataOption;
 import com.tzapps.tzpalette.ui.dialog.PaletteDataOptionsDialogFragment;
 import com.tzapps.tzpalette.ui.dialog.PaletteDataSortByDialogFragment;
 import com.tzapps.tzpalette.ui.dialog.PaletteDataOptionsDialogFragment.OnClickPaletteItemOptionListener;
 import com.tzapps.tzpalette.ui.dialog.PaletteDataSortByDialogFragment.OnClickPaletteDataSorterListener;
 
 public class MainActivity extends Activity implements OnFragmentStatusChangedListener,
         OnClickPaletteItemOptionListener, OnClickPaletteItemListener, OnClickPaletteDataSorterListener
 {
     private final static String TAG = "MainActivity";
 
     /** Called when the user clicks the TakePicture button */
     private static final int TAKE_PHOTE_RESULT = 1;
     /** Called when the user clicks the LoadPicture button */
     private static final int LOAD_PICTURE_RESULT = 2;
     /** Called when the user opens the Palette Edit view */
     private static final int PALETTE_EDIT_RESULT = 3;
     
     private static final int PAGE_CAPTURE_VIEW_POSITION = 0;
     private static final int PAGE_PALETTE_LIST_POSITION = 1;
     private static final int PAGE_ABOUT_VIEW_POSITION   = 2;
 
     private ViewPager mViewPager;
     private TabsAdapter mTabsAdapter;
 
     private String mCurrentPhotoPath;
     
     private PaletteDataHelper mDataHelper;
     private Sorter mDataSorter = Constants.PALETTE_DATA_SORTER_DEFAULT;
 
     private ShareActionProvider mShareActionProvider;
 
     private PaletteListFragment mPaletteListFragment;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         ActivityUtils.forceToShowOverflowOptionsOnActoinBar(this);
 
         mDataHelper = PaletteDataHelper.getInstance(this);
 
         mViewPager = new ViewPager(this);
         mViewPager.setId(R.id.main_pager);
         setContentView(mViewPager);
 
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         mTabsAdapter = new TabsAdapter(this, mViewPager);
         mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.title_capture_view)), CaptureFragment.class, null);
         mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.title_palette_list_view)), PaletteListFragment.class,null);
         mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.title_about_view)), AboutFragment.class, null);
         
         // Open palette list view directly if there has been already record in database
         if (mDataHelper.getDataCount() > 0)
             mTabsAdapter.setSelectedPage(PAGE_PALETTE_LIST_POSITION);
 
         // Get intent, action and MIME type
         Intent intent = getIntent();
         String action = intent.getAction();
         String type = intent.getType();
 
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
             openEditView(imageUri);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
 
         outState.putInt("tab", mTabsAdapter.getSelectedNavigationIndex());
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState)
     {
         super.onRestoreInstanceState(savedInstanceState);
 
         mTabsAdapter.setSelectedTab(savedInstanceState.getInt("tab", 0));
     }
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         //menu.clear();
         return super.onPrepareOptionsMenu(menu);
     }
     
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         
         int position = mViewPager.getCurrentItem();
         
         switch (position)
         {
             case PAGE_CAPTURE_VIEW_POSITION:
                 inflater.inflate(R.menu.capture_view_actions, menu);
                 break;
                 
             case PAGE_PALETTE_LIST_POSITION:
                 inflater.inflate(R.menu.palette_list_view_actions, menu);
                 break;
                 
             case PAGE_ABOUT_VIEW_POSITION:
                 inflater.inflate(R.menu.about_view_actions, menu);
                 break;
         }
 
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
                 sharePalette();
                 return true;
 
             case R.id.action_export:
                 exportPalette();
                 return true;
 
             case R.id.action_takePhoto:
                 takePhoto();
                 return true;
 
             case R.id.action_loadPicture:
                 loadPicture();
                 return true;
                 
             case R.id.action_sortBy:
                 PaletteDataSortByDialogFragment dialogFrag =
                         PaletteDataSortByDialogFragment.newInstance(getString(R.string.action_sortBy));
                 dialogFrag.show(getFragmentManager(), "dialog");
                 return true;
 
             case R.id.action_settings:
                 openSettings();
                 return true;
 
             case R.id.action_about:
                 mTabsAdapter.setSelectedPage(PAGE_ABOUT_VIEW_POSITION);
                 return true;
                 
             case R.id.action_feedback:
                sendFeedback();
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void sendFeedback()
     {
         MyDebug.sendFeedback(this);
     }
     
     private void openSettings()
     {
         Intent intent = new Intent(this, SettingsActivity.class);
         startActivity(intent);
     }
 
     @Override
     public void onPaletteItemOptionClicked(int position, long dataId, PaletteDataOption option)
     {
         assert (mPaletteListFragment != null);
 
         PaletteData data = mPaletteListFragment.getItem(position);
 
         if (data == null)
             return;
 
         switch (option)
         {
             case Rename:
                 Log.d(TAG, "Rename palatte item(position=" + position + " , id=" + dataId + ")");
                 showRenameDialog(position, dataId);
                 break;
 
             case Delete:
                 Log.d(TAG, "Delete palatte item(position=" + position + " , id=" + dataId + ")");
                 mPaletteListFragment.remove(data);
                 mDataHelper.delete(data);
                 break;
                 
             case View:
                 Log.d(TAG, "View palette item (position=" + position + " , id=" + dataId + ")");
                 openPaletteCardView(dataId);
                 break;
                 
             case Edit:
                 Log.d(TAG, "Edit palette item (position=" + position + " , id=" + dataId + ")");
                 openEditView(dataId);
                 break;
                 
             case Favourite:
                 Log.d(TAG, "Favourite palette (position=" + position + " , id=" + dataId + ")");
                 data.setFavourite(true);
                 mDataHelper.update(data, false);
                 mPaletteListFragment.update(data);
                 break;
                 
             case UnFavourite:
                 Log.d(TAG, "Unfavourite palette (position=" + position + " , id=" + dataId + ")");
                 data.setFavourite(false);
                 mDataHelper.update(data, false);
                 mPaletteListFragment.update(data);
                 break;
         }
     }
     
     @Override
     public void onPaletteDataSorterClicked(Sorter sorter)
     {
         Log.d(TAG, "sorter " + sorter + " selected");
         
         mDataSorter = sorter;
         mPaletteListFragment.setSorter(sorter);
     }
 
 
     public void onClick(View view)
     {
         switch (view.getId())
         {
             case R.id.palette_item_options:
                 long dataId = (Long) view.getTag(R.id.TAG_PALETTE_DATA_ID);
                 int itemPosition = (Integer) view.getTag(R.id.TAG_PALETTE_ITEM_POSITION);
 
                 PaletteData data = mPaletteListFragment.getItem(itemPosition);
 
                 Log.d(TAG, "Show options on palette data + " + data);
 
                 PaletteDataOptionsDialogFragment optionDialogFrag =
                         PaletteDataOptionsDialogFragment.newInstance(itemPosition, dataId, data);
                 optionDialogFrag.show(getFragmentManager(), "dialog");
                 break;
                 
             case R.id.btn_loadPicture:
                 loadPicture();
                 break;
                  
             case R.id.btn_takePhoto:
                 takePhoto();
                 
             case R.id.btn_feedback:
                 sendFeedback();
                 break;
         }
     }
     
     @Override
     public void onPaletteItemClick(int position, long dataId, PaletteData data)
     {
         // TODO view the palette data
         Log.i(TAG, "palette data " + data.getId() + " clicked");
         
         openPaletteCardView(dataId);
     }
     
     private void openPaletteCardView(long dataId)
     {
         Intent intent = new Intent(this, PaletteCardActivity.class);
         
         intent.putExtra(Constants.PALETTE_DATA_ID, dataId);
         intent.putExtra(Constants.PALETTE_DATA_SORTER_NAME, mDataSorter.getName());
         startActivity(intent);
     }
 
     @Override
     public void onPaletteItemLongClick(int position, long dataId, PaletteData data)
     {
         Log.i(TAG, "palette data " + data.getId() + " long clicked");
         
         PaletteDataOptionsDialogFragment optionDialogFrag =
                 PaletteDataOptionsDialogFragment.newInstance(position, dataId, data);
         optionDialogFrag.show(getFragmentManager(), "dialog");
     }
 
 
     private void updatePaletteDataTitle(int position, long dataId, String title)
     {
         assert (mPaletteListFragment != null);
 
         PaletteData data = mPaletteListFragment.getItem(position);
 
         if (data == null)
             return;
 
         data.setTitle(title);
         mDataHelper.update(data, /* updateThumb */false);
 
         mPaletteListFragment.refresh();
     }
 
     private void showRenameDialog(final int position, final long dataId)
     {
         assert (mPaletteListFragment != null);
 
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
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                         String text = input.getText().toString();
                         updatePaletteDataTitle(position, dataId, text);
                     }
                 })
                 .setNegativeButton(android.R.string.cancel, null);
 
         final AlertDialog dialog = alert.create();
 
         input.setOnFocusChangeListener(new OnFocusChangeListener()
             {
                 @Override
                 public void onFocusChange(View v, boolean hasFocus)
                 {
                     if (hasFocus)
                     {
                         dialog.getWindow().setSoftInputMode(
                                 WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                     }
                 }
             }
         );
 
         dialog.show();
     }
 
     private void sharePalette()
     {
         View paletteCard = null; //(View) findViewById(R.id.capture_view_frame);
         Bitmap bitmap = BitmapUtils.getBitmapFromView(paletteCard);
 
         assert (bitmap != null);
 
         // TODO make the share function work rather than just a trial version
 
         String name = Constants.FOLDER_HOME + File.separator + "share";
 
         File file = BitmapUtils.saveBitmapToSDCard(bitmap, name);
 
         Intent sendIntent = new Intent(Intent.ACTION_SEND);
 
         sendIntent.setType("image/jpeg");
         sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
         sendIntent.putExtra(Intent.EXTRA_TEXT, "My Palette");
         startActivity(Intent.createChooser(sendIntent, "share"));
     }
 
     private void exportPalette()
     {
         View paletteCard = null; //(View) findViewById(R.id.capture_view_bottom_bar);
         Bitmap bitmap = BitmapUtils.getBitmapFromView(paletteCard);
 
         assert (bitmap != null);
 
         String title = null; //mCurrentPalette.getTitle();
 
         if (title == null)
             title = getResources().getString(R.string.palette_title_default);
 
         BitmapUtils.saveBitmapToSDCard(bitmap, Constants.FOLDER_HOME + File.separator + title);
 
         Toast.makeText(this, "Palette <" + title + "> has been exported", Toast.LENGTH_SHORT)
                 .show();
     }
 
     @Override
     public void onFragmentViewCreated(Fragment fragment)
     {
         if (fragment instanceof PaletteListFragment)
         {
             mPaletteListFragment = (PaletteListFragment) fragment;
             mPaletteListFragment.addAll(mDataHelper.getAllData());
         }
     }
 
     private File getAlbumDir()
     {
         File storageDir = new File(
                 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                 Constants.FOLDER_HOME);
 
         if (!storageDir.isDirectory())
             storageDir.mkdirs();
 
         return storageDir;
     }
 
     private File createImageFile() throws IOException
     {
         // Create an image file name
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         String imageFileName = Constants.TZPALETTE_FILE_PREFIX + "_" + timeStamp;
         File image = File.createTempFile(imageFileName, ".jpg", getAlbumDir());
 
         mCurrentPhotoPath = image.getAbsolutePath();
 
         return image;
     }
 
     /** Called when the user performs the Take Photo action */
     private void takePhoto()
     {
         Log.d(TAG, "take a photo");
 
         if (ActivityUtils.isIntentAvailable(getBaseContext(), MediaStore.ACTION_IMAGE_CAPTURE))
         {
             try
             {
                 File file = createImageFile();
                 Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
                 takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                 startActivityForResult(takePictureIntent, TAKE_PHOTE_RESULT);
             }
             catch (IOException e)
             {
                 Log.e(TAG, "take a photo failed");
             }
         }
         else
         {
             Log.e(TAG, "no camera found");
         }
     }
 
     /** Called when the user performs the Load Picture action */
     private void loadPicture()
     {
         Log.d(TAG, "load a picture");
         Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
         photoPickerIntent.setType("image/*");
         startActivityForResult(photoPickerIntent, LOAD_PICTURE_RESULT);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent)
     {
         // Check which request we are responding to
         switch (requestCode)
         {
             case TAKE_PHOTE_RESULT:
                 if (resultCode == RESULT_OK && mCurrentPhotoPath != null)
                 {
                     
                     Uri selectedImage = Uri.fromFile(new File(mCurrentPhotoPath));
                     
                     if (selectedImage != null)
                     {
                         /* invoke the system's media scanner to add the photo to
                          * the Media Provider's database
                          */
                         Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                         mediaScanIntent.setData(selectedImage);
                         sendBroadcast(mediaScanIntent);
                         
                         openEditView(selectedImage);
                     }
                 }
                 break;
 
             case LOAD_PICTURE_RESULT:
                 if (resultCode == RESULT_OK)
                 {
                     Uri selectedImage = intent.getData();
 
                     if (selectedImage != null)
                         openEditView(selectedImage);
                 }
                 break;
                 
             case PALETTE_EDIT_RESULT:
                 if (resultCode == RESULT_OK)
                 {
                     long dataId = intent.getLongExtra(Constants.PALETTE_DATA_ID, Long.valueOf(-1));
                     boolean addNew = intent.getBooleanExtra(Constants.PALETTE_DATA_ADDNEW, false);
                     
                     PaletteData data = mDataHelper.get(dataId);
                     
                     if (data != null)
                     {
                         if (addNew)
                             mPaletteListFragment.add(data);
                         else
                             mPaletteListFragment.update(data);
                     }
                     
                     // navigate to the palette list view after saving/updating a palette data
                     mTabsAdapter.setSelectedPage(PAGE_PALETTE_LIST_POSITION);
                 }
                 break;
         }
     }
     
     private void openEditView(long dataId)
     {
         Intent intent = new Intent(this, PaletteEditActivity.class);
         
         intent.putExtra(Constants.PALETTE_DATA_ID, dataId);
         
         startActivityForResult(intent, PALETTE_EDIT_RESULT);
     }
     
     private void openEditView(Uri selectedImage)
     {
         Intent intent = new Intent(this, PaletteEditActivity.class);
         
         intent.putExtra(Constants.PALETTE_DATA_ID, Long.valueOf(-1));
         intent.setData(selectedImage);
         
         startActivityForResult(intent, PALETTE_EDIT_RESULT);
     }
     
     private void updateOptionMenu()
     {
         invalidateOptionsMenu();
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
     public class TabsAdapter extends FragmentPagerAdapter
             implements ActionBar.TabListener, ViewPager.OnPageChangeListener
     {
         private final Context mContext;
         private final ActionBar mActionBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
         final class TabInfo
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
 
         public int getSelectedNavigationIndex()
         {
             return mActionBar.getSelectedNavigationIndex();
         }
 
         public void setSelectedTab(int position)
         {
             mActionBar.setSelectedNavigationItem(position);
         }
         
         public void setSelectedPage(int position)
         {
             mViewPager.setCurrentItem(position);
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
             
             //Invalidate the options menu to re-create them
             updateOptionMenu();
             
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
