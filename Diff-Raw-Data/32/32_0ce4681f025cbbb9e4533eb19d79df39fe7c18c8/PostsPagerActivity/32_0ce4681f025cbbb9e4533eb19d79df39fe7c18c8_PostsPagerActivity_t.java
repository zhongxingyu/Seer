 
 package com.shaubert.dirty;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.view.MenuItem;
 import android.view.ViewStub;
 
 import com.shaubert.dirty.DirtyPostFragmentsAdapter.OnLoadCompleteListener;
 import com.shaubert.util.Shlog;
 import com.shaubert.util.Versions;
 
 public class PostsPagerActivity extends DirtyBaseActivity {
 
 	public static final Shlog SHLOG = new Shlog(PostsPagerActivity.class.getSimpleName());
 	
 	
 	public static final String EXTRA_POST_ID = "post-id-extra";
 	
     private ViewPager postPager;
     private DirtyPostFragmentsAdapter postFragmentsAdapter;
     
     @TargetApi(11)
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (Versions.isApiLevelAvailable(11)) {
         	getActionBar().setDisplayHomeAsUpEnabled(true);
         }
         
         tryMoveToLastViewedPost(savedInstanceState);
     }
 
 	private void tryMoveToLastViewedPost(Bundle savedInstanceState) {
 		if (savedInstanceState == null) {
         	if (getIntent() != null) {
         		long postId = getIntent().getLongExtra(EXTRA_POST_ID, -1);
         		navigateToPost(postId);
         	}
         } else {
         	long lastPostId = savedInstanceState.getLong("last-post-id", -1);
         	navigateToPost(lastPostId);
         }
 	}
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     	outState.putLong("last-post-id", tryGetCurrentPostId());
     }
 
     @Override
     public void onContentChanged() {
     	super.onContentChanged();
     	
     	ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
     	stub.setLayoutResource(R.layout.l_posts_pager);
     	stub.inflate();
     	
         postPager = (ViewPager)findViewById(R.id.post_pager);
         postFragmentsAdapter = new DirtyPostFragmentsAdapter(this);
         postPager.setAdapter(postFragmentsAdapter);
         postFragmentsAdapter.setShowOnlyFavorites(dirtyPreferences.isShowingOnlyFavorites());
        postFragmentsAdapter.setEmptyView(dirtyTv);
         
         getSupportLoaderManager().initLoader(Loaders.DIRTY_POST_IDS_LOADER, null, postFragmentsAdapter);
     }
     
     private void navigateToPost(final long postId) {
         postFragmentsAdapter.setLoadCompleteListener(new OnLoadCompleteListener() {
             @Override
             public void onLoadComplete(DirtyPostFragmentsAdapter adapter) {
                 if (postId >= 0) {
                     int pos = adapter.findPosition(postId, -1);
                     if (pos >= 0 && postPager.getCurrentItem() != pos) {
                         postPager.setCurrentItem(pos, false);
                     }
                 }
                 adapter.setLoadCompleteListener(null);
             }
         });
 
     }
 
     @Override
     protected void onPause() {
     	saveCurrentPostId();
         super.onPause();
     }
     
     private void saveCurrentPostId() {
         long curPostId = tryGetCurrentPostId();
         if (curPostId >= 0) {
         	SHLOG.d("saving current post id (" + curPostId + ")");
             dirtyPreferences.setLastViewedPostId(curPostId);
         }
     }
     
     private long tryGetCurrentPostId() {
     	if (postFragmentsAdapter.getCount() > 0) {
             int currentItem = postPager.getCurrentItem();
             return postFragmentsAdapter.getStableId(currentItem);
     	} else {
     		return -1;
     	}
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 			case android.R.id.home:
 				onBackPressed();
 				return true;
         
             case R.id.go_to_first_menu_item:
         		if (postFragmentsAdapter.getCount() > 0) {
         			postPager.setCurrentItem(0, true);
         		}
                 return true;
                 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void toggleShowFavorites() {
     	super.toggleShowFavorites();
         boolean show = dirtyPreferences.isShowingOnlyFavorites();
         postFragmentsAdapter.setShowOnlyFavorites(show);
         getSupportLoaderManager().restartLoader(Loaders.DIRTY_POST_IDS_LOADER, null, postFragmentsAdapter);
     }
     
     public static void startMe(Context context, long postId) {
 		Intent intent = new Intent(context, PostsPagerActivity.class);
 		intent.putExtra(PostsPagerActivity.EXTRA_POST_ID, postId);
 		context.startActivity(intent);
     }
     
     @Override
     public void onBackPressed() {
     	Intent data = new Intent();
     	data.putExtra(EXTRA_POST_ID, postFragmentsAdapter.getStableId(postPager.getCurrentItem()));
     	setResult(RESULT_OK, data);
     	
     	super.onBackPressed();
     }
 }
