 package ru.terra.jbrss.activity;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.Html;
import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import ru.terra.jbrss.R;
 import ru.terra.jbrss.constants.Constants;
 import ru.terra.jbrss.core.db.ProjectDbOpenHelper;
 import ru.terra.jbrss.entity.FeedPostEntity;
 import ru.terra.jbrss.service.MarkReadService;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class FeedPostsViewAcitivity extends RoboActivity {
 
     @InjectView(R.id.vp_post)
     private ViewPager vpPost;
     private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
     private Cursor feedPosts;
     private int currPos = 0;
     public static final String SELECTION = "selection";
     public static final String SELECTION_ARGS = "selectionargs";
     private ProjectDbOpenHelper dbOpenHelper;
     private SQLiteDatabase database;
 
 
     private class MainAcvitiyAdapter extends PagerAdapter {
         @Override
         public int getCount() {
             return feedPosts.getCount();
         }
 
         @Override
         public Object instantiateItem(View collection, int position) {
             feedPosts.moveToPosition(position);
             View v = generateViewFromCursor();
             ((ViewPager) collection).addView(v, 0);
             return v;
         }
 
         @Override
         public void destroyItem(ViewGroup container, int position, Object object) {
             ((ViewPager) container).removeView((View) object);
         }
 
         @Override
         public boolean isViewFromObject(View arg0, Object arg1) {
             return arg0 == (View) arg1;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return "";
         }
 
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.a_post);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         dbOpenHelper = new ProjectDbOpenHelper(this);
         database = dbOpenHelper.getReadableDatabase();
         String selection = getIntent().getStringExtra(SELECTION);
        if (TextUtils.isEmpty(selection))
            selection = "";
         String sql = "select p.* from post p ";
         if (selection.contains("match"))
             sql += "inner join post_fts pf on p.ext_id=pf.ext_id ";
        String where = "";
        if (!selection.isEmpty())
            where = " where " + selection;
         feedPosts = database.rawQuery(sql
                + where
                 + " ORDER BY "
                 + "p." + FeedPostEntity.POST_DATE + " DESC ",
                 getIntent().getStringArrayExtra(SELECTION_ARGS));
         if (feedPosts != null) {
             Integer position = getIntent().getIntExtra("pos", 0);
             currPos = position;
             MainAcvitiyAdapter adapter = new MainAcvitiyAdapter();
             vpPost.setAdapter(adapter);
             vpPost.setCurrentItem(position, true);
             markRead(getId(position));
             vpPost.setOnPageChangeListener(new OnPageChangeListener() {
 
                 @Override
                 public void onPageSelected(int arg0) {
                     currPos = arg0;
                     markRead(getId(arg0));
                 }
 
                 @Override
                 public void onPageScrolled(int arg0, float arg1, int arg2) {
                 }
 
                 @Override
                 public void onPageScrollStateChanged(int arg0) {
                 }
             });
         } else {
             finish();
         }
     }
 
     private Integer getId(Integer pos) {
         Integer ret = 0;
         Integer prevPos = feedPosts.getPosition();
         feedPosts.moveToPosition(pos);
         ret = feedPosts.getInt(feedPosts.getColumnIndex(FeedPostEntity.POST_EXTERNAL_ID));
         feedPosts.moveToPosition(prevPos);
         return ret;
     }
 
     private void markRead(Integer postId) {
         startService(new Intent(FeedPostsViewAcitivity.this, MarkReadService.class).putExtra("id", postId).putExtra("operation",
                 Constants.OP_MARK_READ_POST));
     }
 
     private View generateViewFromCursor() {
         View ret = getLayoutInflater().inflate(R.layout.f_post, null);
         final String postLink = feedPosts.getString(feedPosts.getColumnIndex(FeedPostEntity.POST_LINK));
         TextView title = (TextView) ret.findViewById(R.id.tv_post_title);
         title.setTag(postLink);
         title.setText(feedPosts.getString(feedPosts.getColumnIndex(FeedPostEntity.POST_TITLE)));
         TextView pdate = (TextView) ret.findViewById(R.id.tv_post_date);
         Long pdl = feedPosts.getLong(feedPosts.getColumnIndex(FeedPostEntity.POST_DATE));
         Date pd = new Date(pdl);
         String pdt = df.format(pd);
         pdate.setText(pdt);
         TextView text = (TextView) ret.findViewById(R.id.tv_post_text);
         text.setText(Html.fromHtml(feedPosts.getString(feedPosts.getColumnIndex(FeedPostEntity.POST_TEXT))));
         Integer tag = feedPosts.getInt(feedPosts.getColumnIndex(FeedPostEntity.POST_EXTERNAL_ID));
         ret.setTag(tag);
 
         title.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(postLink));
                 startActivity(browserIntent);
             }
         });
         return ret;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.m_post, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.mi_post_share: {
                 // create the send intent
                 Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 
                 // set the type
                 shareIntent.setType("text/plain");
 
                 // add a subject
                 shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Поделиться");
 
                 // build the body of the message to be shared
                 String shareMessage = shareText();
                 // add the message
                 shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
 
                 // start the chooser for sharing
                 startActivity(Intent.createChooser(shareIntent, "Выберите приложение"));
 
             }
             return true;
         }
         return true;
     }
 
     private String shareText() {
         StringBuilder sb = new StringBuilder();
         Integer prevPos = feedPosts.getPosition();
         feedPosts.moveToPosition(currPos);
         sb.append(feedPosts.getString(feedPosts.getColumnIndex(FeedPostEntity.POST_TITLE)));
         sb.append(System.getProperty("line.separator"));
         sb.append(feedPosts.getString(feedPosts.getColumnIndex(FeedPostEntity.POST_LINK)));
         sb.append(System.getProperty("line.separator"));
         sb.append(Html.fromHtml(feedPosts.getString(feedPosts.getColumnIndex(FeedPostEntity.POST_TEXT))));
         feedPosts.moveToPosition(prevPos);
         return sb.toString();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (feedPosts != null && !feedPosts.isClosed())
             feedPosts.close();
         if (database != null && database.isOpen())
             database.close();
     }
 }
