 package com.tzapps.tzpalette.ui;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v13.app.FragmentStatePagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Toast;
 
 import com.tzapps.common.ui.OnFragmentStatusChangedListener;
 import com.tzapps.common.utils.BitmapUtils;
 import com.tzapps.common.utils.ClipboardUtils;
 import com.tzapps.common.utils.ColorUtils;
 import com.tzapps.tzpalette.Constants;
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.data.PaletteData;
 import com.tzapps.tzpalette.data.PaletteDataComparator.Sorter;
 import com.tzapps.tzpalette.data.PaletteDataHelper;
 import com.tzapps.tzpalette.debug.MyDebug;
 import com.tzapps.tzpalette.ui.dialog.ColorInfoDialogFragment;
 import com.tzapps.tzpalette.ui.view.ColorInfoListView;
 
 public class PaletteCardActivity extends Activity implements OnFragmentStatusChangedListener, OnItemClickListener, OnItemLongClickListener
 {
     private static final String TAG = "PaletteCardActivity";
 
     private static final int PALETTE_CARD_EDIT_RESULT  = 0;
     private static final int PALETTE_CARD_SHARE_RESULT = 1;
     
     private ViewPager mViewPager;
     private ColorInfoListView mColorInfoList;
     private PaletteCardAdapter mCardAdapter;
     private PaletteDataHelper mDataHelper;
     
     private Sorter mSorter;
     private File mTempShareFile;
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_palette_card);
         
         String sorterName = getIntent().getExtras().getString(Constants.PALETTE_DATA_SORTER_NAME);
         mSorter = Sorter.fromString(sorterName);
         
         if (mSorter == null)
             mSorter = Constants.PALETTE_DATA_SORTER_DEFAULT;
         
         mViewPager = (ViewPager) findViewById(R.id.palette_card_pager);
         mColorInfoList = (ColorInfoListView) findViewById(R.id.palette_card_color_list);
         mColorInfoList.setOnItemClickListener(this);
         mColorInfoList.setOnItemLongClickListener(this);
         
         mDataHelper = PaletteDataHelper.getInstance(this);
         mCardAdapter = new PaletteCardAdapter(this, mViewPager, mColorInfoList);
         
         long dataId = getIntent().getExtras().getLong(Constants.PALETTE_DATA_ID);
         mCardAdapter.setCurrentCard(dataId);
         
         mViewPager.setOffscreenPageLimit(Constants.PALETTE_CARD_PAGE_OFFSCREEN_LIMIT);
         mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.palette_card_page_margin));
         
         // Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
         {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
     
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         clearTemp();
     }
     
     private void clearTemp()
     {
         if (mTempShareFile != null)
             mTempShareFile.delete();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.palette_card_view_actions, menu);
 
         //Locate MenuItem with ShareActionProvider
         MenuItem item = menu.findItem(R.id.action_share);
 
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
                 
             case R.id.action_share:
                 sharePaletteCard();
                 return true;
                 
             case R.id.action_edit:
                 PaletteData curData = mCardAdapter.getCurrentData();
                 openEditView(curData.getId());
                 return true;
                 
             case R.id.action_delete:
                 deletePaletteCard();
                 return true;
                 
             case R.id.action_emailPalette:
                 if (MyDebug.LOG)
                     Log.d(TAG, "send palette card via email");
                 
                 //TODO: implement email palette function
                 return true;
                 
             case R.id.action_export:
                 if (MyDebug.LOG)
                     Log.d(TAG, "export palette card");
                 
                 exportPaletteCard();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id)
     {
         int color = mColorInfoList.getColor(position);
         
         if (MyDebug.LOG)
             Log.d(TAG, "color info list item clicked: " + ColorUtils.colorToHtml(color));
         
         // Show color info detail dialog
         ColorInfoDialogFragment dialogFrag =
                 ColorInfoDialogFragment.newInstance(getString(R.string.title_color_info), color);
         dialogFrag.show(getFragmentManager(), "dialog");
     }
     
     @Override
     public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
     {
         int color = mColorInfoList.getColor(position);
         
         if (MyDebug.LOG)
             Log.d(TAG, "color info list item long clicked: " + ColorUtils.colorToHtml(color));
         
         // Copy color's html(hex) value into clipboard
         String toastText = getResources().getString(R.string.copy_color_into_clipboard);
         toastText = String.format(toastText, ColorUtils.colorToHtml(color));
         
         ClipboardUtils.setPlainText(this, "Copied color", ColorUtils.colorToHtml(color));
         Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
         
         return true;
     }
     
     public void onClick(View view)
     {
         switch (view.getId())
         {
             case R.id.palette_card_thumb:
                 long dataId = (Long)view.getTag();
                 openEditView(dataId);
                 break;
         }
     }
 
     @Override
     public void onFragmentViewCreated(Fragment fragment){}
     
     public void setSorter(Sorter sorter)
     {
         mSorter = sorter;
     }
     
     public Sorter getSorter()
     {
         return mSorter;
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent)
     {
         // Check which request we are responding to
         switch (requestCode)
         {
             case PALETTE_CARD_EDIT_RESULT:
                 if (resultCode == RESULT_OK)
                 {
                     long dataId = intent.getLongExtra(Constants.PALETTE_DATA_ID, Long.valueOf(-1));
                     
                     if (dataId != -1)
                     {
                         PaletteData data = mDataHelper.get(dataId);
                         mCardAdapter.updateCard(data);
                     }
                 }
                 break;
                 
             case PALETTE_CARD_SHARE_RESULT:
                 if (mTempShareFile != null)
                     mTempShareFile.delete();
                 break;
         }
     }
     
     private File getTempFile(String filename)
     {
         String path = Environment.getExternalStorageDirectory().toString();
         File file = new File(path + File.separator + Constants.FOLDER_HOME + File.separator + 
                              Constants.SUBFOLDER_TEMP + File.separator + filename);
         file.getParentFile().mkdirs();
         
         if (file.exists())
             file.delete();
         
         return file;
     }
     
     private void sharePaletteCard()
     {
         View view = mCardAdapter.getCurrentView();
         View paletteCard = view.findViewById(R.id.palette_card_frame);
         Bitmap bitmap = BitmapUtils.getBitmapFromView(paletteCard);
 
         assert (bitmap != null);
 
         mTempShareFile = getTempFile(Constants.TZPALETTE_TEMP_SHARE_FILE_NAME);
         BitmapUtils.saveBitmapToFile(bitmap, mTempShareFile);
 
         Intent shareIntent = new Intent(Intent.ACTION_SEND);
 
         shareIntent.setType("image/jpeg");
         shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mTempShareFile));
         shareIntent.putExtra(Intent.EXTRA_TEXT, mCardAdapter.getCurrentData().getTitle());
         
         String actionTitle = getString(R.string.title_share_action);
         
         startActivityForResult(Intent.createChooser(shareIntent, actionTitle),
                                PALETTE_CARD_SHARE_RESULT);
     }
     
     private void exportPaletteCard()
     {
         View view = mCardAdapter.getCurrentView();
         View paletteCard = view.findViewById(R.id.palette_card_frame);
         Bitmap bitmap = BitmapUtils.getBitmapFromView(paletteCard);
 
         assert (bitmap != null);
         
         String title = mCardAdapter.getCurrentData().getTitle();
 
         if (title == null)
             title = getResources().getString(R.string.palette_title_default);
         
         String folderName = Constants.FOLDER_HOME + File.separator + Constants.SUBFOLDER_EXPORT;
         String fileName = Constants.TZPALETTE_FILE_PREFIX + title.replace(" ", "_");
 
         File file = BitmapUtils.saveBitmapToSDCard(bitmap, folderName + File.separator + fileName);
 
         /* invoke the system's media scanner to add the photo to
          * the Media Provider's database
          */
         Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
         mediaScanIntent.setData(Uri.fromFile(file));
         sendBroadcast(mediaScanIntent);
         
         Toast.makeText(this, "Palette Card <" + title + "> exported", Toast.LENGTH_SHORT).show();
     }
     
     private void openEditView(long dataId)
     {
         if (MyDebug.LOG)
             Log.d(TAG, "edit palette card: " + dataId);
         
         Intent intent = new Intent(this, PaletteEditActivity.class);
         
         intent.putExtra(Constants.PALETTE_DATA_ID, dataId);
         
         startActivityForResult(intent, PALETTE_CARD_EDIT_RESULT);
     }
     
     private void deletePaletteCard()
     {
         PaletteData data = mCardAdapter.getCurrentData();
         
         if (data == null)
             return;
         
         if (MyDebug.LOG)
             Log.d(TAG, "delete paletted card: " + data);
         
         mCardAdapter.deleteCard(data);
         mDataHelper.delete(data);
     }
     
     private class PaletteCardAdapter extends FragmentStatePagerAdapter 
     {
         private Context mContext;
         private ViewPager mViewPager;
         private ColorInfoListView mColorInfoList;
         
         private List<PaletteData> dataList;
         private PaletteDataHelper mDataHelper;
         private View mCurrentView;
 
         public PaletteCardAdapter(Activity activity, ViewPager pager, ColorInfoListView colorInfoList)
         {
             super(activity.getFragmentManager());
             mContext = activity;
             mViewPager = pager;
             mColorInfoList = colorInfoList;
             mDataHelper = PaletteDataHelper.getInstance(mContext);
             
             dataList = mDataHelper.getAllData();
             Collections.sort(dataList, mSorter.getComparator());
             
             mViewPager.setAdapter(this);
         }
 
         public void setCurrentCard(long dataId)
         {
             int index = 0;
             PaletteData curData = null;
             
             for (int i = 0; i < dataList.size(); i++)
             {
                 PaletteData data = dataList.get(i);
                 
                 if (data.getId() == dataId)
                 {
                     curData = data;
                     index = i;
                     break;
                 }
             }
             
             mViewPager.setCurrentItem(index);
             
             if (curData != null)
                 mColorInfoList.setColors(curData.getColors());
         }
         
         public PaletteData getCurrentData()
         {
             int index = mViewPager.getCurrentItem();
             
             return dataList.get(index);
         }
         
         public View getCurrentView()
         {
             return mCurrentView;
         }
         
         public void updateCard(PaletteData data)
         {
             for (PaletteData d : dataList)
             {
                 if (d.getId() == data.getId() && !d.equals(data))
                 {
                     d.copy(data);
                     Collections.sort(dataList, mSorter.getComparator());
                     notifyDataSetChanged();
                     break;
                 }
             }
         }
         
         public void deleteCard(PaletteData data)
         {
             int index = -1;
             
             for (int i = 0; i < dataList.size(); i++)
             {
                 if (dataList.get(i).getId() == data.getId())
                 {
                     index = i;
                     break;
                 }
             }
             
             if (index != -1)
             {
                 dataList.remove(index);
                 notifyDataSetChanged();
             }
         }
         
         @Override 
         public void setPrimaryItem(ViewGroup container, int position, Object object)
         {
             super.setPrimaryItem(container, position, object);
             
             mCurrentView = ((Fragment)object).getView();
             
             PaletteData curData = dataList.get(position);
             if (curData != null)
                 mColorInfoList.setColors(curData.getColors());
         }
         
         @Override
         public int getItemPosition(Object object)
         {
             // force to destroy and recreate palette card in given 
             // fragment, it will fix the issue that the palette card
             // view cannot updated when open it in edit view and then
             // save the change to back. However, it is inefficient 
             // and might have the performance issue. And if so
             // it will need to have a further tweaking. 
             //
             // see more detailed discussion on 
             // http://stackoverflow.com/questions/10849552/android-viewpager-cant-update-dynamically
             return POSITION_NONE;
         }
         
         @Override
         public Fragment getItem(int position)
         {
             PaletteData data = dataList.get(position);
             
             PaletteCardFragment fragment = (PaletteCardFragment)Fragment.instantiate(mContext, 
                                         PaletteCardFragment.class.getName(), null);
             fragment.setData(data);
             
             return fragment;
         }
 
         @Override
         public Object instantiateItem(ViewGroup container, int position)
         {
             PaletteCardFragment fragment = (PaletteCardFragment)super.instantiateItem(container, position);
             
             PaletteData data = dataList.get(position);
             fragment.setData(data);
             
             return fragment;
         }
         
         @Override
         public int getCount()
         {
             return dataList.size();
         }
 
     }
 
 }
