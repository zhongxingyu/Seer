 
 package com.egloos.realmove.android.fp.view;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.egloos.realmove.android.fp.activity.PageListFragment;
 import com.egloos.realmove.android.fp.model.Link;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 
 public class LinkSelectDialog extends Dialog {
 
     public static final String EXTRA_SELECTED_PAGE_ID = "selectedPageId";
 
     private Link mLink;
 
     private LinkSelectDialog(Context context, Link link) {
         super(context);
         this.mLink = link;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     public static void show(Context context, FragmentManager fm, int projectId, Link link) {
         if (link == null)
             return;
 
         PageListFragment fragment = PageListFragment.newInstance(projectId, link.getTargetPageId(),
                PageListFragment.Mode.ONLY_SELECT);
         fragment.show(fm, PageListFragment.TAG);
     }
     
 }
