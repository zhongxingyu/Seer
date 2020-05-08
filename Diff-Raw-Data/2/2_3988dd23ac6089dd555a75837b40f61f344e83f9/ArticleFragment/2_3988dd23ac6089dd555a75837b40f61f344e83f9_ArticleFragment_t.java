 package org.geekhub.shuUA.rssreader.fragment;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import org.geekhub.shuUA.rssreader.R;
 import org.geekhub.shuUA.rssreader.db.ArticleTable;
 import org.geekhub.shuUA.rssreader.db.DatabaseHelper;
 import org.geekhub.shuUA.rssreader.object.Article;
 
 
 public class ArticleFragment extends SherlockFragment {
     private static int id;
     private static boolean isLiked;
     Menu mMenu;
     DatabaseHelper dbHelper;
     SQLiteDatabase database;
     private static String content,title;
     WebView tw;
     public Article article;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.article_frag,container,false);
     }
 
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         setHasOptionsMenu(true);
 
         if (Build.VERSION.SDK_INT >=11) {
             getSherlockActivity().invalidateOptionsMenu();
             getSherlockActivity().openOptionsMenu();
         }
 
         id = getActivity().getIntent().getIntExtra("ID",0);
         dbHelper = new DatabaseHelper(getActivity());
         database = dbHelper.getWritableDatabase();
         Cursor cursor = database.query(ArticleTable.TABLE_ARTICLES, null, ArticleTable.COLUMN_ID + "=" + id, null,null,null,null);
         cursor.moveToFirst();
         title = cursor.getString(cursor.getColumnIndex(ArticleTable.COLUMN_TITLE));
         content = cursor.getString(cursor.getColumnIndex(ArticleTable.COLUMN_CONTENT));
         int like = cursor.getInt(cursor.getColumnIndex(ArticleTable.COLUMN_LIKE));
 
         if (like == 0) {
             isLiked = false;
         }   else {
             isLiked = true;
         }
 
         Like(mMenu.findItem(R.id.like));
 
         getSherlockActivity().getSupportActionBar().setTitle(title);
         tw = (WebView) getView().findViewById(R.id.art_content);
         tw.loadData(content,"text/html",null);
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.like:
                 isLiked = !isLiked;
                 Like(item);
                 break;
             case R.id.share:
                 Intent sendIntent = new Intent();
                 sendIntent.setAction(Intent.ACTION_SEND);
                 sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                 sendIntent.putExtra(Intent.EXTRA_TITLE,title);
                 sendIntent.setType("text/plain");
                 startActivity(sendIntent);
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
 
        inflater.inflate(R.menu.article_menu, menu);
 
        if (Build.VERSION.SDK_INT >=17 && mMenu != null) {
             Like(mMenu.findItem(R.id.like));
         }
         mMenu = menu;
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         mMenu = menu;
     }
 
     private void Like(MenuItem item) {
         int intLike;
 
         if (isLiked) {
             intLike = 1;
             item.setIcon(R.drawable.ic_menu_liked);
         }   else {
             intLike = 0;
             item.setIcon(R.drawable.ic_menu_not_liked);
         }
         String sql = "UPDATE " + ArticleTable.TABLE_ARTICLES
                 + " SET " + ArticleTable.COLUMN_LIKE + "=" + intLike
                 + " WHERE " + ArticleTable.COLUMN_ID  + "=" + id;
         database.execSQL(sql);
     }
 
 
 
 }
