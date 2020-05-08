 /*
  * Copyright (C) 2012 The ChameleonOS Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.thememanager.fragment;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.IThemeManagerService;
 import android.os.*;
 import android.app.Fragment;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import com.android.thememanager.Globals;
 import com.android.thememanager.PreviewManager;
 import com.android.thememanager.R;
 import com.android.thememanager.ThemeUtils;
 import com.android.thememanager.activity.ThemeDetailActivity;
 import com.android.thememanager.activity.ThemeManagerTabActivity;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.LinkedList;
 import java.util.List;
 
 public class ThemeChooserFragment extends Fragment {
     private static final String TAG = "ThemeManager";
     private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;
 
     private GridView mGridView = null;
     private String[] mThemeList = null;
     private ImageAdapter mAdapter = null;
     private LoadThemesInfoTask mTask = null;
     private ImageView mChameleon = null;
 
     private boolean mReady = true;
     private List<Runnable> mPendingCallbacks = new LinkedList<Runnable>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         this.setRetainInstance(true);
         this.setHasOptionsMenu(true);
 	}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.activity_theme_chooser, container, false);
 
         mThemeList = themeList(THEMES_PATH);
 
         ThemeUtils.createCacheDir();
 
         mChameleon = (ImageView) v.findViewById(R.id.loadingIndicator);
         mGridView = (GridView) v.findViewById(R.id.coverflow);
 
         mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent = new Intent(getActivity(), ThemeDetailActivity.class);
                 intent.putExtra("theme_name", mThemeList[i]);
                 startActivity(intent);
             }
         });
 
        mChameleon.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.GONE);

         mTask = new LoadThemesInfoTask();
         mTask.execute();
 /*
         getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 //getActivity().showDialog(ThemeManagerTabActivity.DIALOG_LOAD_THEMES_PROGRESS);
                 for (String themeId : mThemeList) {
                     ThemeUtils.addThemeEntryToDb(ThemeUtils.stripExtension(themeId),
                             THEMES_PATH + "/" + themeId,
                             getActivity());
                 }
                 markAsDone();
             }
         });
 */
         return v;
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         mReady = false;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mReady = true;
 
         int pendingCallbakcs = mPendingCallbacks.size();
 
         while(pendingCallbakcs-- > 0)
             getActivity().runOnUiThread(mPendingCallbacks.remove(0));
     }
 
     private Handler mViewUpdateHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             mAdapter = new ImageAdapter(getActivity());
             mGridView.setAdapter(mAdapter);
             mChameleon.setVisibility(View.GONE);
             mGridView.setVisibility(View.VISIBLE);
             //getActivity().dismissDialog(ThemeManagerTabActivity.DIALOG_LOAD_THEMES_PROGRESS);
         }
     };
 
     public void runWhenReady(Runnable runnable) {
         if (mReady)
             getActivity().runOnUiThread(runnable);
         else
             mPendingCallbacks.add(runnable);
     }
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.activity_theme_chooser, menu);
 	}
 
     @Override
     public boolean onOptionsItemSelected(MenuItem menuItem) {
         switch (menuItem.getItemId()) {
             case R.id.menu_reset:
                 // have the theme service remove the existing theme
                 IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                 try {
                     ts.removeThemeAndApply();
                 } catch (Exception e) {
                     Log.e(TAG, "Failed to call ThemeService.removeTheme", e);
                 }
                 return true;
             default:
                 return false;
         }
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mAdapter.destroyImages();
         mAdapter = null;
         System.gc();
     }
 
     @Override
     public void onResume() {
         super.onResume();
     }
 
     private String[] themeList(String path) {
         Log.d(TAG, "Returning theme list for " + path);
         FilenameFilter themeFilter = new FilenameFilter() {
             @Override
             public boolean accept(File file, String s) {
                 if (s.toLowerCase().endsWith(".mtz"))
                     return true;
                 else
                     return false;
             }
         };
 
         File dir = new File(path);
         String[] dirList = null;
         if (dir.exists() && dir.isDirectory())
             dirList = dir.list(themeFilter);
         else
             Log.e(TAG, path + " does not exist or is not a directory!");
         return dirList;
     }
 
     void markAsDone() {
         mViewUpdateHandler.sendEmptyMessage(0);
     }
 
     public class ImageAdapter extends BaseAdapter {
         private Context mContext;
 
         private PreviewManager mPreviewManager = new PreviewManager();
 
         private String[] mImageIds = themeList(THEMES_PATH);
 
         private ImageView[] mImages;
 
         private ThemeUtils.ThemeDetails[] mDetails;
 
         public ImageAdapter(Context c) {
             mContext = c;
             if (mImages == null) {
                 mImages = new ImageView[mImageIds.length];
                 mDetails = new ThemeUtils.ThemeDetails[mImageIds.length];
                 for (int i = 0; i < mImages.length; i++) {
                     mDetails[i] = ThemeUtils.getThemeDetails(
                             THEMES_PATH + "/" + mImageIds[i]);
                 }
             }
         }
 
         public int getCount() {
             return mImageIds.length;
         }
 
         public Object getItem(int position) {
             return position;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = inflater.inflate(R.layout.theme_preview, null);
             }
             ImageView i = (ImageView)v.findViewById(R.id.preview_image);//mImages[position];//new ImageView(mContext);
             if (mImages[position] == null) {
                 mPreviewManager.fetchDrawableOnThread(ThemeUtils.stripExtension(mImageIds[position]), i);
                 mImages[position] = i;
             } else
                 i.setImageDrawable(mImages[position].getDrawable());
             i.setAdjustViewBounds(true);
 
             TextView tv = (TextView) v.findViewById(R.id.theme_name);
 
             tv.setText(mDetails[position].title);
 
             return v;
         }
 
         public void destroyImages() {
             for (int i = 0; i < mImages.length; i++) {
                 if (mImages[i].getDrawable() != null)
                     mImages[i].getDrawable().setCallback(null);
                 mImages[i].setImageDrawable(null);
             }
 
             mPreviewManager = null;
         }
     }
 
     private class LoadThemesInfoTask extends AsyncTask<String, Integer, Boolean> {
         int progress = 0;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             //getActivity().showDialog(ThemeManagerTabActivity.DIALOG_LOAD_THEMES_PROGRESS);
         }
 
         @Override
         protected Boolean doInBackground(String... strings) {
             for (String themeId : mThemeList) {
                 ThemeUtils.addThemeEntryToDb(ThemeUtils.stripExtension(themeId),
                         THEMES_PATH + "/" + themeId,
                         getActivity());
             }
             return Boolean.TRUE;
         }
 
         @Override
         protected void onPostExecute(Boolean aBoolean) {
             runWhenReady(new Runnable() {
                 @Override
                 public void run() {
                     markAsDone();
                 }
             });
         }
     }
 }
