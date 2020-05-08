 package ru.terra.jbrss.activity;
 
 import android.app.ActionBar;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import ru.terra.jbrss.R;
 import ru.terra.jbrss.activity.components.FeedPostsCursorAdapter;
 import ru.terra.jbrss.constants.Constants;
 import ru.terra.jbrss.core.db.ProjectDbOpenHelper;
 import ru.terra.jbrss.entity.FeedPostEntity;
 import ru.terra.jbrss.service.MarkReadService;
 
 public class FeedPostsListActivity extends RoboActivity {
 
     @InjectView(R.id.lv_posts)
     private ListView lvPosts;
     private Integer feed;
     private boolean isFromDate;
     private CursorAdapter mAdapter;
     private String selections = null;
     private String[] selectionArgs = null;
     private String textFilter = "";
     private static final String SELECTION_ADD_UNREAD = " AND " + "p." + FeedPostEntity.POST_ISREAD + " = ?";
     private static final String SELECTION_SEARCH = " AND " + "pf.post_text match ? ";
     private ProjectDbOpenHelper dbOpenHelper;
     private SQLiteDatabase database;
     private Cursor postsCursor;
     private boolean filterEnabled = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.a_feed_list);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         dbOpenHelper = new ProjectDbOpenHelper(this);
         database = dbOpenHelper.getReadableDatabase();
         final Boolean isFromNotification = getIntent().getBooleanExtra("from_notify", false);
         if (!isFromNotification) {
             feed = getIntent().getIntExtra("id", -1);
             if (feed.equals(Constants.ALL_FEED_POSTS_ITEM_ID)) {
                 selections = null;
                 selectionArgs = null;
             } else {
                 selections = "p." + FeedPostEntity.POST_FEED_ID + " = ?";
                 selectionArgs = new String[]{feed.toString()};
             }
         } else {
             String fromDate = getIntent().getStringExtra("from_date");
             isFromDate = true;
             selections = "p." + FeedPostEntity.POST_DATE + " >= ?";
             selectionArgs = new String[]{fromDate != null ? fromDate : "0"};
         }
 
         new AsyncTask<Void, Void, Cursor>() {
             @Override
             protected Cursor doInBackground(Void... params) {
                String where = null;
                if (selections != null)
                    where = " where " + selections;
                else where = "";
                 return database.rawQuery("select p.* from post p "
 //                                + " inner join post_fts pf on p.ext_id=pf.ext_id "
                        + where
                        + " ORDER BY "
                        + "p." + FeedPostEntity.POST_DATE + " DESC ",
                         selectionArgs);
             }
 
             @Override
             protected void onPostExecute(Cursor cursor) {
                 postsCursor = cursor;
                 mAdapter = new FeedPostsCursorAdapter(FeedPostsListActivity.this, postsCursor);
                 lvPosts.setAdapter(mAdapter);
                 lvPosts.setOnItemClickListener(new OnItemClickListener() {
                     @Override
                     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                         startActivity(new Intent(FeedPostsListActivity.this, FeedPostsViewAcitivity.class).putExtra("pos", position).putExtra(FeedPostsViewAcitivity.SELECTION, selections)
                                 .putExtra(FeedPostsViewAcitivity.SELECTION_ARGS, selectionArgs));
                     }
                 });
             }
         }.execute();
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         if (!isFromDate) {
             getMenuInflater().inflate(R.menu.m_posts, menu);
             menu.findItem(R.id.mi_posts_show_read).setChecked(true);
             // Get the SearchView and set the searchable configuration
             SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
             SearchView searchView = (SearchView) menu.findItem(R.id.mi_posts_search).getActionView();
             searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
             searchView.setIconifiedByDefault(false);
             searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                 @Override
                 public boolean onQueryTextSubmit(String query) {
                     return false;
                 }
 
                 @Override
                 public boolean onQueryTextChange(String newText) {
                     textFilter = newText;
                     if (!TextUtils.isEmpty(textFilter)) {
                         filterEnabled = true;
                         if (selections == null)
                             selections = "pf.post_text match ? ";
                         else {
                             if (selections.contains("match"))
                                 selectionArgs[selectionArgs.length - 1] = textFilter;
                             else {
                                 selections += SELECTION_SEARCH;
                                 if (selectionArgs == null)
                                     selectionArgs = new String[]{textFilter};
                                 else {
                                     String[] newSelections = new String[selectionArgs.length + 1];
                                     for (int i = 0; i < selectionArgs.length; i++)
                                         newSelections[i] = selectionArgs[i];
                                     newSelections[newSelections.length - 1] = textFilter;
                                     selectionArgs = newSelections;
                                 }
                             }
                         }
 
                     } else {
                         filterEnabled = false;
                         if (selections.contains("match")) {
                             if (selections.equals("pf.post_text match ? "))
                                 selections = null;
                             else {
                                 selections = selections.substring(0, selections.length() - SELECTION_SEARCH.length());
                             }
 
                             if (selectionArgs.length == 1)
                                 selectionArgs = null;
                             else {
                                 String[] newSelections = new String[selectionArgs.length - 1];
                                 for (int i = 0; i < selectionArgs.length - 1; i++)
                                     newSelections[i] = selectionArgs[i];
                                 selectionArgs = newSelections;
                             }
                         }
                     }
 
                     restartDb();
                     return true;
                 }
             }
 
             );
         }
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.mi_posts_show_read: {
                 if (item.isChecked()) {
                     item.setChecked(false);
                     item.setTitle(R.string.all_posts);
 
                     if (selections == null)
                         selections = "p." + FeedPostEntity.POST_ISREAD + " = ?";
                     else
                         selections += SELECTION_ADD_UNREAD;
                     if (selectionArgs == null)
                         selectionArgs = new String[]{"false"};
                     else {
                         String[] newSelections = new String[selectionArgs.length + 1];
                         for (int i = 0; i < selectionArgs.length; i++)
                             newSelections[i] = selectionArgs[i];
                         newSelections[newSelections.length - 1] = "false";
                         selectionArgs = newSelections;
                     }
 
                 } else {
                     item.setChecked(true);
                     item.setTitle(R.string.unread);
 
                     if (selections.equals("p." + FeedPostEntity.POST_ISREAD + " = ?"))
                         selections = null;
                     else {
                         selections = selections.substring(0, selections.length() - SELECTION_ADD_UNREAD.length());
                     }
 
                     if (selectionArgs.length == 1)
                         selectionArgs = null;
                     else {
                         String[] newSelections = new String[selectionArgs.length - 1];
                         for (int i = 0; i < selectionArgs.length - 1; i++)
                             newSelections[i] = selectionArgs[i];
                         selectionArgs = newSelections;
                     }
 
                 }
                 restartDb();
                 return true;
             }
             case R.id.mi_posts_all_read: {
                 startService(new Intent(this, MarkReadService.class).putExtra("id", feed).putExtra("operation", Constants.OP_MARK_READ_FEED));
             }
             return true;
         }
         return true;
     }
 
     private void restartDb() {
         if (mAdapter != null) {
             if (postsCursor != null && !postsCursor.isClosed()) {
                 String sql = "select p.* from post p ";
                 if (filterEnabled)
                     sql += "inner join post_fts pf on p.ext_id=pf.ext_id ";
                 sql += " where "
                         + selections
                         + " ORDER BY "
                         + "p." + FeedPostEntity.POST_DATE + " DESC ";
                 postsCursor = database.rawQuery(sql, selectionArgs);
             }
             mAdapter.swapCursor(postsCursor);
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (postsCursor != null && !postsCursor.isClosed())
             postsCursor.close();
         if (database != null && database.isOpen())
             database.close();
     }
 }
