 
 package com.egloos.realmove.android.fp.activity;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.egloos.realmove.android.fp.R;
 import com.egloos.realmove.android.fp.common.BaseFragment;
 import com.egloos.realmove.android.fp.common.FpLog;
 import com.egloos.realmove.android.fp.db.DBAdapter;
 import com.egloos.realmove.android.fp.db.LoadProjectListTask;
 import com.egloos.realmove.android.fp.db.LoadProjectListTask.Callback;
 import com.egloos.realmove.android.fp.model.Project;
 import com.example.android.bitmapfun.util.ImageCache;
 import com.example.android.bitmapfun.util.ImageFetcher;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 public class ProjectListFragment extends BaseFragment implements OnItemClickListener, Callback {
 
     private static final String TAG = ProjectListFragment.class.getSimpleName();
 
     private ProjectListAdapter mAdapter;
     private ImageFetcher mImageFetcher;
 
     private ArrayList<Project> mProjects;
 
     public static ProjectListFragment newInstance() {
         return new ProjectListFragment();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
 
         ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
                 "page_list");
         cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
 
         // The ImageFetcher takes care of loading images into our ImageView children asynchronously
         int size = getActivity().getResources().getDimensionPixelSize(R.dimen.page_thumbnail_size);
         mImageFetcher = new ImageFetcher(getActivity(), size, size);
         mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
         mImageFetcher.setImageFadeIn(false);
 
         mAdapter = new ProjectListAdapter(getActivity(), mImageFetcher);
     }
 
     /**
      * 실제 view 를 생성한다.
      * 
      * @param inflater
      * @param container
      * @return
      */
     private View createView(LayoutInflater inflater, ViewGroup container) {
         FpLog.d(TAG, "createView()");
         final View view = inflater.inflate(R.layout.project_list_fragment, container, false);
         final ListView listView = (ListView) view.findViewById(android.R.id.list);
         listView.setAdapter(mAdapter);
         listView.setOnItemClickListener(this);
 
         return view;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         FpLog.d(TAG, "onCreateView()");
 
         load();
 
         View view = createView(inflater, container);
         return view;
     }
 
     private void load() {
         new LoadProjectListTask(getActivity(), this).execute();
     }
 
     @Override
     public void onLoad(ArrayList<Project> projects) {
         this.mProjects = projects;
         if (projects == null) {
             // do nothing
             return;
         }
 
         mAdapter.setProjects(projects);
         mAdapter.notifyDataSetChanged();
     }
 
     @Override
     public void onDestroy() {
         if (mImageFetcher != null) {
             mImageFetcher.closeCache();
             mImageFetcher = null;
         }
 
         mAdapter = null;
         super.onDestroy();
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Intent intent = new Intent(mContext, PageListActivity.class);
         intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, mProjects.get(position).getId());
         startActivity(intent);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.menu_project_list_option, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.add: {
                 LayoutInflater factory = LayoutInflater.from(mContext);
                 final View textEntryView = factory.inflate(R.layout.input_project_dialog, null);
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                 builder.setView(textEntryView)
                        .setTitle(R.string.input_project_info)
                         .setNegativeButton(android.R.string.cancel, null)
                         .setPositiveButton(android.R.string.ok,
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int which) {
                                         if (which == DialogInterface.BUTTON_POSITIVE) {
                                             CharSequence txt = ((TextView) textEntryView
                                                     .findViewById(R.id.subject)).getText();
                                             if (txt != null) {
                                                 String subject = txt.toString().trim();
                                                 if (subject.length() > 0) {
                                                     createProject(subject);
                                                     return;
                                                 }
                                             }
 
                                            Toast.makeText(mContext, R.string.error_subject_empty,
                                                     Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 });
                 builder.create().show();
                 break;
             }
 
         }
         return super.onOptionsItemSelected(item);
     }
 
     protected void createProject(String subject) {
         final Project project = new Project();
         project.setSubject(subject);
         project.setCreated(new Date());
 
         new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... params) {
                 DBAdapter db = null;
                 try {
                     db = new DBAdapter(mContext).open();
                     db.insertProject(project);
                 } catch (Exception e) {
                     FpLog.e(TAG, e);
                 } finally {
                     if (db != null)
                         db.close();
                 }
 
                 return null;
             }
 
             @Override
             protected void onPostExecute(Void result) {
                 load();
                 super.onPostExecute(result);
             }
 
         }.execute();
     }
 }
