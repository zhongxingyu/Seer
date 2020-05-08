 
 package com.egloos.realmove.android.fp.activity;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.egloos.realmove.android.fp.R;
 import com.egloos.realmove.android.fp.common.BaseFragment;
 import com.egloos.realmove.android.fp.common.FpLog;
 import com.egloos.realmove.android.fp.db.DBAdapter;
 import com.egloos.realmove.android.fp.db.LoadPageTask;
 import com.egloos.realmove.android.fp.db.LoadPageTask.Callback;
 import com.egloos.realmove.android.fp.model.Link;
 import com.egloos.realmove.android.fp.model.Page;
 import com.egloos.realmove.android.fp.view.LinkImageEditView;
 import com.egloos.realmove.android.fp.view.LinkImageEditView.OnLinkChangeListener;
 import com.example.android.bitmapfun.util.ImageCache;
 import com.example.android.bitmapfun.util.ImageFetcher;
 import com.example.android.bitmapfun.util.ImageWorker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.util.ArrayList;
 
 public class PageEditFragment extends BaseFragment implements OnLinkChangeListener,
         ImageWorker.Callback, PageListFragment.SelectCallback {
 
     private static final String TAG = PageEditFragment.class.getSimpleName();
 
     public static final String EXTRA_PAGE_ID = "pageId";
 
     private Page mPage;
 
     private int mWidth;
     private int mHeight;
     private ImageWorker mImageFetcher;
     private LinkImageEditView mPageView;
 
     private ActionBar mActionBar;
 
     public static PageEditFragment newInstance(int pageId) {
         PageEditFragment fragment = new PageEditFragment();
 
         Bundle args = new Bundle();
         args.putInt(EXTRA_PAGE_ID, pageId);
         fragment.setArguments(args);
 
         return fragment;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         final View view = inflater.inflate(R.layout.page_edit_fragment, container, false);
         mPageView = (LinkImageEditView) view.findViewById(R.id.page);
         mPageView.setOnLinkChangeListener(this);
 
         prepareCache();
 
         mActionBar = getSherlockActivity().getSupportActionBar();
 
         // Hide title text and set home as up
         mActionBar.setDisplayShowTitleEnabled(false);
         mActionBar.setDisplayHomeAsUpEnabled(true);
 
         /*
          * // Hide and show the ActionBar as the visibility changes
          * mPageView.setOnSystemUiVisibilityChangeListener( new
          * View.OnSystemUiVisibilityChangeListener() {
          * @Override public void onSystemUiVisibilityChange(int vis) { if ((vis &
          * View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) { mActionBar.hide(); } else { mActionBar.show(); }
          * } }); if (Utils.hasJellyBean()) { // Start low profile mode and hide ActionBar
          * mPageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); }
          */
 
         mActionBar.hide();
 
         load(getArguments().getInt(EXTRA_PAGE_ID));
 
         return view;
     }
 
     private void load(int pageId) {
         new LoadPageTask(mContext, new LoadPageTask.Callback() {
             @Override
             public void onLoad(Page page) {
                 if (page == null) {
                     Toast.makeText(mContext, R.string.fail_to_load_page, Toast.LENGTH_SHORT).show();
                     finishActivity();
                 }
                 mPage = page;
                 mImageFetcher.loadImage(Uri.fromFile(new File(mPage.getImagePath())), mPageView);
             }
         }).execute(pageId);
     }
 
     private void prepareCache() {
         DisplayMetrics displayMetrics = new DisplayMetrics();
         getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
         mWidth = displayMetrics.widthPixels;
         mHeight = displayMetrics.heightPixels;
 
         FpLog.d(TAG, "prepareCache()", mWidth, mHeight);
 
         ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mContext,
                 "page_view");
         cacheParams.setMemCacheSizePercent(0.25f);
 
         mImageFetcher = new ImageFetcher(mContext, mWidth, mHeight);
         mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
         mImageFetcher.setImageFadeIn(false);
         mImageFetcher.setCallback(this);
     }
 
     @Override
     public void onDestroyView() {
         if (mImageFetcher != null) {
             mImageFetcher.closeCache();
             mImageFetcher = null;
         }
 
         super.onDestroyView();
     }
 
     @Override
     public void onLoadImage(boolean success, final ImageView pageView, final Bitmap bitmap) {
         if (success) {
             ArrayList<Link> links = mPage.getLinks();
             if (links == null) {
                 links = new ArrayList<Link>();
                 mPage.setLinks(links);
             }
             mPageView.setLinks(links);
             mPageView.setLinkShow(true);
         }
     }
 
     enum Command {
         INSERT, DELETE, MODIFY
     }
 
     private class LinkTask extends AsyncTask<Void, Void, Boolean> {
 
         private Link mLink;
         private Command mCommand;
 
         public LinkTask(Link link, Command command) {
             mLink = link;
             mCommand = command;
         }
 
         @Override
         protected Boolean doInBackground(Void... params) {
             synchronized (mContext) {
                 DBAdapter db = null;
                 try {
                     db = new DBAdapter(mContext).open();
                     switch (mCommand) {
                         case DELETE:
                             db.deleteLink(mLink.getId());
                             break;
                         case INSERT:
                             db.insertLink(mLink);
                             break;
                         case MODIFY:
                             db.updateLink(mLink);
                             break;
                         default:
                             break;
                     }
                     return true;
                 } catch (Exception ex) {
                     FpLog.e(TAG, ex);
                     return false;
                 } finally {
                     if (db != null)
                         db.close();
                 }
             }
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             if (result) {
                 mPageView.invalidate();
             }
             super.onPostExecute(result);
         }
 
     }
 
     @Override
     public void linkAdded(Link link) {
         FpLog.d(TAG, "linkAdded()");
         link.setPageId(mPage.getId());
         new LinkTask(link, Command.INSERT).execute();
     }
 
     @Override
     public void linkRemoved(Link link) {
         FpLog.d(TAG, "linkRemoved()");
         new LinkTask(link, Command.DELETE).execute();
     }
 
     @Override
     public void linkModified(Link link) {
         FpLog.d(TAG, "linkModified()");
         new LinkTask(link, Command.MODIFY).execute();
     }
 
     private Link mSelectedLink;
 
     @Override
     public void requestSetTarget(Link link) {
         if (link == null)
             return;
 
         mSelectedLink = link;
 
         PageListFragment fragment = PageListFragment.newInstance(mPage.getProjectId(),
                link.getTargetPageId(), PageListFragment.Mode.SELECT);
         fragment.show(getActivity().getSupportFragmentManager(), PageListFragment.TAG_DIALOG);
         fragment.setSelectCallback(new PageListFragment.SelectCallback() {
             public void pageSelected(Page page) {
                 if (page != null) {
                     mSelectedLink.setTargetPageId(page.getId());
                     new LinkTask(mSelectedLink, Command.MODIFY).execute();
                 }
             }
         });
     }
 
     @Override
     public void pageSelected(Page page) {
         FpLog.d(TAG, "pageSelected");
         if (page != null && mSelectedLink != null) {
             mSelectedLink.setTargetPageId(page.getId());
         }
 
         mSelectedLink = null;
     }
 
 }
