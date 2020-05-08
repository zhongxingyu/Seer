 /*
  * Copyright (C) 2013 The ChameleonOS Project
  *
  * Licensed under the GNU GPLv2 license
  *
  * The text of the license can be found in the LICENSE file
  * or at https://www.gnu.org/licenses/gpl-2.0.txt
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
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ServiceManager;
 import android.app.Fragment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.android.thememanager.Globals;
 import com.android.thememanager.PreviewHolder;
 import com.android.thememanager.PreviewManager;
 import com.android.thememanager.R;
 import com.android.thememanager.SimpleDialogs;
 import com.android.thememanager.Theme;
 import com.android.thememanager.ThemesDataSource;
 import com.android.thememanager.ThemeUtils;
 import com.android.thememanager.activity.ThemeDetailActivity;
 import com.android.thememanager.widget.FlipImageView;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.LinkedList;
 import java.util.List;
 
 public class ThemeChooserFragment extends Fragment {
     private static final String TAG = "ThemeManager";
     private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;
 
     private GridView mGridView = null;
     private PreviewAdapter mAdapter = null;
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
         registerForContextMenu(mGridView);
 
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
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.context_menu_theme, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
             case R.id.menu_delete_theme:
                 int id = info.targetView.getId();
                 Theme theme = mThemesList.get(id);
                 if (!theme.getIsDefaultTheme()) {
                     ThemeUtils.deleteTheme(theme, getActivity());
                     ThemeUtils.deleteThemeCacheDir(theme.getFileName());
                     mViewUpdateHandler.sendEmptyMessage(0);
                 }
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     private Handler mViewUpdateHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             ThemesDataSource dataSource = new ThemesDataSource(getActivity());
             dataSource.open();
             mThemesList = dataSource.getCompleteThemes();
             dataSource.close();
 
             mAdapter = new PreviewAdapter(getActivity());
             mGridView.setAdapter(mAdapter);
             mChameleon.setVisibility(View.GONE);
             mGridView.setVisibility(View.VISIBLE);
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
                 final IThemeManagerService ts = 
                         IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                 if (ThemeUtils.installedThemeHasFonts()) {
                     SimpleDialogs.displayYesNoDialog(
                             getString(R.string.dlg_reset_theme_with_font_and_reboot),
                             getString(R.string.dlg_reset_theme_with_font_without_reboot),
                             getString(R.string.dlg_reset_theme_with_font_title),
                             getString(R.string.dlg_reset_theme_with_font_body),
                             getActivity(),
                             new SimpleDialogs.OnYesNoResponse() {
                                 @Override
                                 public void onYesNoResponse(boolean isYes) {
                                    try {
                                       ts.removeThemeAndApply(isYes);
                                    } catch(Exception e) {}
                                 }
                             });
                 } else
                     try {
                         ts.removeThemeAndApply(false);
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
 
     public class PreviewAdapter extends BaseAdapter {
         private Context mContext;
 
         private PreviewManager mPreviewManager = new PreviewManager();
 
         private View[] mPreviews;
         private int mPreviewWidth;
         private int mPreviewHeight;
 
         public PreviewAdapter(Context c) {
             mContext = c;
             DisplayMetrics dm = c.getResources().getDisplayMetrics();
             mPreviewWidth = dm.widthPixels / 3;
             mPreviewHeight = dm.heightPixels / 3;
 
             preloadPreviews();
         }
 
         private void preloadPreviews() {
             mPreviews = new View[mThemesList.size()];
             for (int i = 0; i < mPreviews.length; i++) {
                 LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 mPreviews[i] = inflater.inflate(R.layout.theme_preview, null);
                mPreviews[i].setId(i);
                 FrameLayout fl = (FrameLayout)mPreviews[i].findViewById(R.id.preview_layout);
                 LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)fl.getLayoutParams();
                 params.width = mPreviewWidth;
                 params.height = mPreviewHeight;
                 fl.setLayoutParams(params);
                 PreviewHolder holder = new PreviewHolder();
                 holder.preview = (FlipImageView) mPreviews[i].findViewById(R.id.preview_image);
                 holder.name = (TextView) mPreviews[i].findViewById(R.id.theme_name);
                 holder.osTag = (ImageView) mPreviews[i].findViewById(R.id.os_indicator);
                 holder.progress = mPreviews[i].findViewById(R.id.loading_indicator);
                 holder.index = i;
                 mPreviews[i].setTag(holder);
                 mPreviewManager.fetchDrawableOnThread(mThemesList.get(i), holder);
 
                 holder.name.setText(mThemesList.get(i).getTitle());
                 holder.preview.setImageResource(R.drawable.empty_preview);
 
                 if (mThemesList.get(i).getIsCosTheme())
                     holder.osTag.setImageResource(R.drawable.chaos);
                 else
                     holder.osTag.setImageResource(R.drawable.miui);
             }
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
             return mPreviews[position];
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
             ThemeUtils.addThemeEntryToDb("default", Globals.DEFAULT_SYSTEM_THEME, getActivity(), true);
             String[] availableThemes = themeList(THEMES_PATH);
             for (String themeId : availableThemes) {
                 ThemeUtils.addThemeEntryToDb(ThemeUtils.stripExtension(themeId),
                         THEMES_PATH + "/" + themeId,
                         getActivity(), false);
             }
             //removeNonExistingThemes(availableThemes);
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
