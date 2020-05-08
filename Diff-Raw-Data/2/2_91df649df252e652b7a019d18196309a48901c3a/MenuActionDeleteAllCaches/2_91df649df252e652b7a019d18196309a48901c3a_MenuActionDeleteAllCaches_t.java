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
 
 package com.google.code.geobeagle.activity.cachelist.actions.menu;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.actions.MenuActionBase;
 import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh;
 import com.google.code.geobeagle.database.DbFrontend;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 
 public class MenuActionDeleteAllCaches extends MenuActionBase {
    static final class OnClickCancelListener implements DialogInterface.OnClickListener {
         public void onClick(DialogInterface dialog, int id) {
             dialog.cancel();
         }
     }
 
     private static class OnClickOkayListener implements DialogInterface.OnClickListener {
         private final CacheListRefresh cacheListRefresh;
         private final DbFrontend dbFrontend;
 
         OnClickOkayListener(DbFrontend dbFrontend, CacheListRefresh cacheListRefresh) {
             this.dbFrontend = dbFrontend;
             this.cacheListRefresh = cacheListRefresh;
         }
 
         public void onClick(DialogInterface dialog, int id) {
             dialog.dismiss();
             dbFrontend.deleteAll();
             cacheListRefresh.forceRefresh();
         }
     }
 
     private final Activity mActivity;
     private final Builder mBuilder;
     private final CacheListRefresh mCacheListRefresh;
     private final DbFrontend mDbFrontend;
 
     public MenuActionDeleteAllCaches(CacheListRefresh cacheListRefresh, Activity activity,
             DbFrontend dbFrontend, AlertDialog.Builder builder) {
         super(R.string.menu_delete_all_caches);
         mDbFrontend = dbFrontend;
         mBuilder = builder;
         mActivity = activity;
         mCacheListRefresh = cacheListRefresh;
     }
 
     @Override
     public void act() {
         buildAlertDialog(mDbFrontend, mCacheListRefresh).show();
     }
 
     private AlertDialog buildAlertDialog(DbFrontend dbFrontend, CacheListRefresh cacheListRefresh) {
         mBuilder.setTitle(R.string.delete_all_title);
         final OnClickOkayListener onClickOkayListener = new OnClickOkayListener(dbFrontend,
                 cacheListRefresh);
         final DialogInterface.OnClickListener onClickCancelListener = new OnClickCancelListener();
         mBuilder.setMessage(R.string.confirm_delete_all).setPositiveButton(
                 R.string.delete_all_title, onClickOkayListener).setNegativeButton(R.string.cancel,
                 onClickCancelListener);
         AlertDialog alertDialog = mBuilder.create();
         alertDialog.setOwnerActivity(mActivity);
         return alertDialog;
     }
 
 }
