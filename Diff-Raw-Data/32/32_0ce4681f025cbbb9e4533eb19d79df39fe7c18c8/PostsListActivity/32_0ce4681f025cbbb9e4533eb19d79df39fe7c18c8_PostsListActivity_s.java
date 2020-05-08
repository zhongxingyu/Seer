 package com.shaubert.dirty;
 
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewStub;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.shaubert.dirty.DirtyPostCompactAdapter.OnLoadCompleteListener;
 import com.shaubert.util.DatabaseCleaner;
 import com.shaubert.util.FasterScrollerView;
 import com.shaubert.util.Files;
 import com.shaubert.util.Views;
 
 public class PostsListActivity extends DirtyBaseActivity {
 
 	private static final int PAGER_REQUEST_CODE = 41;
 	public static final String EXTRA_FROM_NOTIFICATION = "from-notification";
 	
     private ListView postsCompactList;
     private DirtyPostCompactAdapter postCompactAdapter;
 
 	private boolean isPaused;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notificationManager.cancel(DirtyNewPostsStatusBarNotification.NEW_POSTS_NOTIFICATION_ID);
         dirtyPreferences.setNewPostsCount(0);
         
         if (savedInstanceState == null) {
             Files.startCleanUpCacheIfNeeded(this, Files.PREFFERED_MAX_CACHE_SIZE);
             DatabaseCleaner.startCleanUpIfNeeded(this);
         }
 
         if (savedInstanceState == null) {
             restoreLastViewedPost();
         }
     }
     
     @Override
     public void onContentChanged() {
     	super.onContentChanged();
     	
     	ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
     	stub.setLayoutResource(R.layout.l_posts_list);
     	stub.inflate();
     	
     	FasterScrollerView fasterScroller = (FasterScrollerView) findViewById(R.id.fastscroll);
 		fasterScroller.setFastScrollEnabled(true);
     	
         postsCompactList = (ListView) findViewById(R.id.posts_list);
         postsCompactList.setEmptyView(dirtyTv);
         postCompactAdapter = new DirtyPostCompactAdapter(this);
         postCompactAdapter.setShowOnlyFavorites(dirtyPreferences.isShowingOnlyFavorites());
         postCompactAdapter.setFasterScrollerView(fasterScroller);
         postsCompactList.addHeaderView(Views.createVerticalSpacer(this, 10), null, false);
         postsCompactList.addFooterView(Views.createVerticalSpacer(this, 30), null, false);
         postsCompactList.setAdapter(postCompactAdapter);
         postsCompactList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				openPager(id);
 			}
 		});
         
         getSupportLoaderManager().restartLoader(Loaders.DIRTY_POSTS_LOADER, null, postCompactAdapter);
     }
     
     private void restoreLastViewedPost() {
     	long id = dirtyPreferences.getLastViewedPostId();
         if (id >= 0) {
         	openPager(id);
         }
     }
 
     @Override
     public void startGertrudaRefresh(String gertrudaUrl) {
     	if (!isPaused) {
     		super.startGertrudaRefresh(gertrudaUrl);
     	}
     }
     
     @Override
     public void showLoadingPostsError() {
     	if (!isPaused) {
     		super.showLoadingPostsError();
     	}
     }
     
     @Override
     public void showPostLoadedMessage(int newCount) {
     	if (!isPaused) {
     		super.showPostLoadedMessage(newCount);
     	}
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	
     	boolean onlyFav = dirtyPreferences.isShowingOnlyFavorites();
     	if (onlyFav != postCompactAdapter.isShowOnlyFavorites()) {
     		postCompactAdapter.setShowOnlyFavorites(onlyFav);
     		getSupportLoaderManager().restartLoader(Loaders.DIRTY_POSTS_LOADER, null, postCompactAdapter);
     	}
     	postCompactAdapter.notifyDataSetChanged();
     }
     
     private void openPager(long postId) {
     	Intent intent = new Intent(this, PostsPagerActivity.class);
 		intent.putExtra(PostsPagerActivity.EXTRA_POST_ID, postId);
 		attachRequestsIds(intent);
 		this.startActivityForResult(intent, PAGER_REQUEST_CODE);
 		isPaused = true;
     }
      
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (requestCode == PAGER_REQUEST_CODE) {
     		isPaused = false;
     		if (data != null) {
     			final long postId = data.getLongExtra(PostsPagerActivity.EXTRA_POST_ID, -1);
     			if (postId >= 0) {
    				int pos = postCompactAdapter.getPostPosition(postId);
    				if (pos >= 0) {
    					postsCompactList.setSelection(pos + 1);
    				} else {
     					postCompactAdapter.setLoadCompleteListener(new OnLoadCompleteListener() {
 							@Override
 							public void onLoadComplete(DirtyPostCompactAdapter adapter) {
								int pos = postCompactAdapter.getPostPosition(postId);
								if (pos >= 0) {
									postsCompactList.setSelection(pos + 1);
								}
 			    				postCompactAdapter.setLoadCompleteListener(null);
 							}
 						});
     				}
     			}
     		}
     	}
     	super.onActivityResult(requestCode, resultCode, data);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.go_to_first_menu_item:
            		postsCompactList.setSelection(0);
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void toggleShowFavorites() {
     	super.toggleShowFavorites();
         boolean show = dirtyPreferences.isShowingOnlyFavorites();
         postCompactAdapter.setShowOnlyFavorites(show);
         getSupportLoaderManager().restartLoader(Loaders.DIRTY_POSTS_LOADER, null, postCompactAdapter);
     }
     
 }
