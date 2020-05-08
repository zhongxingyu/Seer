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
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.IThemeManagerService;
 import android.os.*;
 import android.app.Fragment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import com.android.thememanager.*;
 import com.android.thememanager.activity.ThemeDetailActivity;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.LinkedList;
 import java.util.List;
 
 public class ThemeChooserFragment extends Fragment {
     private static final String TAG = "ThemeManager";
     private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;
 
     private GridView mGridView = null;
     private ImageAdapter mAdapter = null;
     private LoadThemesInfoTask mTask = null;
     private ImageView mChameleon = null;
     private List<Theme> mThemesList;
 
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
 
         ThemeUtils.createCacheDir();
 
         mChameleon = (ImageView) v.findViewById(R.id.loadingIndicator);
         mGridView = (GridView) v.findViewById(R.id.coverflow);
 
         mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent = new Intent(getActivity(), ThemeDetailActivity.class);
                 intent.putExtra("theme_id", mThemesList.get(i).getId());
                 startActivity(intent);
             }
         });
 
         mChameleon.setVisibility(View.VISIBLE);
         mGridView.setVisibility(View.GONE);
 
         mTask = new LoadThemesInfoTask();
         mTask.execute();
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
             ThemesDataSource dataSource = new ThemesDataSource(getActivity());
             dataSource.open();
             mThemesList = dataSource.getAllThemes();
             dataSource.close();
 
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
         mAdapter.destroy();
         mAdapter = null;
         mGridView = null;
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
                 if (s.toLowerCase().endsWith(".ctz") || s.toLowerCase().endsWith(".mtz"))
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
 
         private ImageView[] mImages;
        private int mPreviewWidth;
         private int mPreviewHeight;
 
         public ImageAdapter(Context c) {
             mContext = c;
             DisplayMetrics dm = c.getResources().getDisplayMetrics();
            mPreviewWidth = dm.widthPixels / 3;
             mPreviewHeight = dm.heightPixels / 3;
         }
 
         public int getCount() {
             return mThemesList.size();
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
                 FrameLayout fl = (FrameLayout)v.findViewById(R.id.preview_layout);
                 LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)fl.getLayoutParams();
                params.width = mPreviewWidth;
                 params.height = mPreviewHeight;
                 fl.setLayoutParams(params);
             }
             ImageView i = (ImageView)v.findViewById(R.id.preview_image);//mImages[position];//new ImageView(mContext);
             if (i.getDrawable() == null) {
                 i.setImageResource(R.drawable.preview);
             }
             mPreviewManager.fetchDrawableOnThread(mThemesList.get(position), i);
 
             TextView tv = (TextView) v.findViewById(R.id.theme_name);
             tv.setText(mThemesList.get(position).getTitle());
 
             if (mThemesList.get(position).getIsCosTheme())
                 v.findViewById(R.id.miui_indicator).setVisibility(View.GONE);
             else
                 v.findViewById(R.id.miui_indicator).setVisibility(View.VISIBLE);
 
             return v;
         }
 
         public void destroy() {
             mPreviewManager = null;
             mContext = null;
         }
     }
 
     private void removeNonExistingThemes(String[] availableThemes) {
         List<Theme> themes = ThemeUtils.getAllThemes(getActivity());
         for (Theme theme : themes) {
             boolean exists = false;
             for (String s : availableThemes) {
                 if (theme.getThemePath().contains(s)) {
                     exists = true;
                     break;
                 }
             }
             if (!exists) {
                 ThemeUtils.deleteTheme(theme, getActivity());
                 ThemeUtils.deleteThemeCacheDir(theme.getFileName());
             }
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
             String[] availableThemes = themeList(THEMES_PATH);
             for (String themeId : availableThemes) {
                 ThemeUtils.addThemeEntryToDb(ThemeUtils.stripExtension(themeId),
                         THEMES_PATH + "/" + themeId,
                         getActivity());
             }
             removeNonExistingThemes(availableThemes);
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
