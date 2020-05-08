 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.cachelist.actions.context;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVector;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVectors;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheListAdapter;
 import com.google.code.geobeagle.activity.cachelist.presenter.TitleUpdater;
 import com.google.code.geobeagle.database.CacheWriter;
import com.google.code.geobeagle.database.DbFrontend;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class ContextActionDelete implements ContextAction {
     public static final int CACHE_LIST_DIALOG_CONFIRM_DELETE = 0;
 
     public static class ContextActionDeleteDialogHelper {
         private final ContextActionDelete mContextActionDelete;
         private final OnClickListener mOnClickOk;
 
         public ContextActionDeleteDialogHelper(ContextActionDelete contextActionDelete,
                 OnClickListener onClickOk) {
             mContextActionDelete = contextActionDelete;
             mOnClickOk = onClickOk;
         }
 
         public Dialog onCreateDialog(Builder builder) {
             return builder.setPositiveButton(R.string.delete_cache, mOnClickOk).create();
         }
 
         public void onPrepareDialog(Dialog dialog) {
             dialog.setTitle(mContextActionDelete.getConfirmDeleteTitle());
             final TextView textView = (TextView)dialog.findViewById(R.id.delete_cache);
             textView.setText(mContextActionDelete.getConfirmDeleteBodyText());
         }
     }
 
     public static class OnClickOk implements OnClickListener {
         private final ContextActionDelete mContextActionDelete;
 
         public OnClickOk(ContextActionDelete contextActionDelete) {
             mContextActionDelete = contextActionDelete;
         }
 
         public void onClick(DialogInterface dialog, int whichButton) {
             mContextActionDelete.delete();
             dialog.dismiss();
         }
     }
 
     private final Activity mActivity;
     private Provider<CacheWriter> mCacheWriterProvider;
     private final BaseAdapter mGeocacheListAdapter;
     private final GeocacheVectors mGeocacheVectors;
     private int mPosition;
     private final TitleUpdater mTitleUpdater;
    private final DbFrontend mDbFrontend;
 
     @Inject
     public ContextActionDelete(GeocacheListAdapter geocacheListAdapter,
             GeocacheVectors geocacheVectors, TitleUpdater titleUpdater,
            Provider<CacheWriter> cacheWriterProvider, Activity activity, DbFrontend dbFrontend) {
         mGeocacheListAdapter = geocacheListAdapter;
         mGeocacheVectors = geocacheVectors;
         mTitleUpdater = titleUpdater;
         mCacheWriterProvider = cacheWriterProvider;
         mActivity = activity;
        mDbFrontend = dbFrontend;
         mPosition = 0;
     }
 
     public void act(int position) {
         mPosition = position;
         mActivity.showDialog(CACHE_LIST_DIALOG_CONFIRM_DELETE);
     }
 
     void delete() {
         mCacheWriterProvider.get().deleteCache(mGeocacheVectors.get(mPosition).getId());
         mGeocacheVectors.remove(mPosition);
         mGeocacheListAdapter.notifyDataSetChanged();
         // TODO: How to get correct values?
        mTitleUpdater.update(mDbFrontend.countAll(), mGeocacheVectors.size());
     }
 
     public CharSequence getConfirmDeleteBodyText() {
         final GeocacheVector geocacheVector = mGeocacheVectors.get(mPosition);
         return String.format(mActivity.getString(R.string.confirm_delete_body_text), geocacheVector
                 .getId(), geocacheVector.getName());
     }
 
     public CharSequence getConfirmDeleteTitle() {
         return String.format(mActivity.getString(R.string.confirm_delete_title), mGeocacheVectors
                 .get(mPosition).getId());
     }
 }
