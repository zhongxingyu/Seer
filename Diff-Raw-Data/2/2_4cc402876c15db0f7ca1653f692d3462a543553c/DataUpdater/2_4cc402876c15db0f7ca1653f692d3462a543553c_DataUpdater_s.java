 package com.ouchadam.fang.persistance;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import novoda.android.typewriter.cursor.CursorList;
 import novoda.android.typewriter.cursor.CursorMarshaller;
 
 public class DataUpdater<T> implements LoaderManager.LoaderCallbacks<Cursor> {
 
     private final Query values;
     private final Context context;
     private final DataUpdatedListener<T> listener;
     private final CursorMarshaller<T> marshaller;
     private final LoaderManager loaderManager;
 
     private CursorList<T> cursorList;
 
     public interface DataUpdatedListener<T> {
         void onDataUpdated(List<T> data);
     }
 
     public DataUpdater(Context context, Query values, CursorMarshaller<T> marshaller,
                        DataUpdatedListener<T> listener, LoaderManager loaderManager) {
         this.values = values;
         this.context = context;
         this.listener = listener;
         this.marshaller = marshaller;
         this.loaderManager = loaderManager;
     }
 
     public void startWatchingData() {
         loaderManager.restartLoader(loaderId(), null, this);
     }
 
     public void stopWatchingData() {
         loaderManager.destroyLoader(loaderId());
     }
 
     private int loaderId() {
        return hashCode();
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         return new CursorLoader(context, values.uri, values.projection, values.selection, values.selectionArgs, values.sortOrder);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
         CursorList<T> oldList = cursorList;
         cursorList = new CursorList<T>(cursor, marshaller);
         listener.onDataUpdated(cursorList);
         if (oldList != null) {
             oldList.close();
         }
     }
 
     @Override
     public void onLoaderReset(Loader loader) {
         safeCloseCursor();
     }
 
     private void safeCloseCursor() {
         if (cursorList != null) {
             listener.onDataUpdated(new ArrayList<T>());
             cursorList.close();
         }
     }
 
 }
