 package me.kstep.appshell;
 
 import android.app.Activity;
 import android.app.LoaderManager;
 import android.content.Context;
 import android.content.Loader;
 import android.content.Loader;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import java.util.List;
 
 public class AppListView extends ListView implements LoaderManager.LoaderCallbacks<List<AppEntry>>, AdapterView.OnItemClickListener {
 
     public AppListView(Context context, AttributeSet attrs) {
         super(context, attrs);
         ((Activity) context).getLoaderManager().initLoader(0, null, this);
         setOnItemClickListener(this);
     }
 
     public void onLoaderReset(Loader<List<AppEntry>> loader) {
         ((AppListAdapter) getAdapter()).setData(null);
     }
 
     public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
         return new AppListLoader(getContext());
     }
 
     public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
         ((AppListAdapter) getAdapter()).setData(data);
     }
 
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         AppEntry entry = (AppEntry) getAdapter().getItem(position);
        entry.runApp();
     }
 }
