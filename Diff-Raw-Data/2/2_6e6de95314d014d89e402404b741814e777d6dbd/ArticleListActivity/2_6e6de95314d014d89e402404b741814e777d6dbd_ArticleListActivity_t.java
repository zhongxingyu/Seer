 package ch.adorsaz.loungeDroid.activities;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import ch.adorsaz.loungeDroid.R;
 import ch.adorsaz.loungeDroid.article.ToDisplay;
 import ch.adorsaz.loungeDroid.article.Article;
 import ch.adorsaz.loungeDroid.servercom.ArticleListGetter;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /**
  * ArticleListActivity tasks is to get all articles from server (taking account
  * of user preference, to get all, only unread or only starred articles).
  * */
 public class ArticleListActivity extends ListActivity {
 
     private ArticleAdapter mArticleAdapter = null;
     private ToDisplay mDisplayChoice = null;
     private Article mArticleToDetail = null;
 
     protected static final String ARTICLE_KEY = "article";
     protected static final String DISPLAYCHOICE_KEY = "mDisplayChoice";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         SharedPreferences pref = getSharedPreferences(
                 SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
 
         mDisplayChoice = (ToDisplay) getIntent().getSerializableExtra(
                 DISPLAYCHOICE_KEY);
 
         if (mDisplayChoice == null) {
             if (pref.getString(SettingsActivity.DISPLAY_BEHAVIOUR_PREF,
                     "ALWAYS_PROMPT").equals("ALWAYS_PROMPT")) {
                 Log.d("ArticleList",
                         "We display dialog box with mDisplayChoice : "
                                 + mDisplayChoice);
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(getString(R.string.displayMenu));
                 builder.setSingleChoiceItems(
                         R.array.startdialog_human_toDisplay,
                         R.array.startdialog_values_toDisplay,
                         new DisplayDialogListener()).create().show();
             } else {
                 mDisplayChoice = ToDisplay.valueOf(pref.getString(
                         SettingsActivity.DISPLAY_BEHAVIOUR_PREF, "ALL"));
 
                 fetchNews();
             }
         } else {
             restoreAndUpdateData();
         }
     }
 
     /**
      * DisplayDialogListener is only used if user asked to prompt it which
      * articles he want to see.
      * */
     private class DisplayDialogListener implements
             DialogInterface.OnClickListener {
 
         @Override
         public void onClick(DialogInterface dialog, int which) {
             mDisplayChoice = ToDisplay.values()[which + 1]; // We won't show
                                                             // ALWAYS_PROMPT
             Log.d("ArticleList", "have to show : " + mDisplayChoice
                     + " (we selected " + which + ")");
             fetchNews();
             dialog.dismiss();
         }
     }
 
     /**
      * onPause() have to save data to be able to avoid reload always data from
      * server.
      * */
     @Override
     public void onPause() {
         super.onPause();
         if (mArticleToDetail == null) {
             saveData();
        } else {
            finish();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_article_list, menu);
         disableToDisplayMenu(menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         disableToDisplayMenu(menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.refresh:
                 fetchNews();
                 break;
             case R.id.showAllMenu:
                 mDisplayChoice = ToDisplay.ALL;
                 fetchNews();
                 break;
             case R.id.showUnreadMenu:
                 mDisplayChoice = ToDisplay.UNREAD;
                 fetchNews();
                 break;
             case R.id.showStarredMenu:
                 mDisplayChoice = ToDisplay.STARRED;
                 fetchNews();
                 break;
             case R.id.menu_settings:
                 SettingsActivity.setWantToEdit();
 
                 Intent intent;
                 intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 break;
         }
         return false;
     }
 
     /**
      * updateArticleList should be used by AsyncTask (especially
      * loungeDroid.server.ArticleListGetter) to update data on this activity.
      * 
      * @param articleList
      *            List of articles to display in the activity (will replace all
      *            already displayed articles)
      * */
     public void updateArticleList(List<Article> articleList) {
         mArticleAdapter = new ArticleAdapter(getApplicationContext(),
                 R.layout.articlelist_item, R.id.articleItemTitle, articleList);
         updateArticleAdapter();
     }
 
     /**
      * updateArticleAdapter updates really the displayed articles with his
      * latest ArticleAdapter field version.
      * */
     private void updateArticleAdapter() {
         ListView listView = getListView();
         listView.setScrollingCacheEnabled(false);
         listView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 mArticleToDetail = mArticleAdapter.getItem(position);
                 saveData();
             }
         });
 
         setListAdapter(mArticleAdapter);
     }
 
     /**
      * fetchNews() is an async task that will get news depending on user
      * preferences.
      * */
     private void fetchNews() {
         ArticleListGetter getter = new ArticleListGetter(this);
         getter.execute(mDisplayChoice);
     }
 
     private void disableToDisplayMenu(Menu menu) {
         MenuItem showAllItem = menu.findItem(R.id.showAllMenu);
         MenuItem showUnreadItem = menu.findItem(R.id.showUnreadMenu);
         MenuItem showStarredItem = menu.findItem(R.id.showStarredMenu);
 
         enableMenuItem(showAllItem);
         enableMenuItem(showUnreadItem);
         enableMenuItem(showStarredItem);
 
         if (mDisplayChoice != null) {
             switch (mDisplayChoice) {
                 case ALL:
                     disableMenuItem(menu, R.id.showAllMenu);
                     break;
                 case UNREAD:
                     disableMenuItem(menu, R.id.showUnreadMenu);
                     break;
                 case STARRED:
                     disableMenuItem(menu, R.id.showStarredMenu);
                     break;
                 case ALWAYS_PROMPT:
                     break;
             }
         }
 
     }
 
     /**
      * disableMenuItem will disable the item with depending on his id and in
      * which menu is it.
      * 
      * @param menu
      *            Menu where to disable some menu item.
      * @param id
      *            Id of the menuItem to disable.
      * */
     private void disableMenuItem(Menu menu, int id) {
         MenuItem menuItem = menu.findItem(id);
         menuItem.setVisible(false);
         menuItem.setEnabled(false);
     }
 
     private void enableMenuItem(MenuItem menuItem) {
         menuItem.setVisible(true);
         menuItem.setEnabled(true);
     }
 
     /**
      * saveData will save already downloaded data to avoid too many http request
      * (especially on resume).
      * */
     private void saveData() {
         Intent intent = null;
 
         if (mArticleToDetail == null) {
             intent = getIntent();
         } else {
             intent = new Intent(ArticleListActivity.this,
                     ArticleDetailActivity.class);
         }
 
         if (mArticleAdapter != null) {
             int adapterSize = mArticleAdapter.getCount();
 
             intent.putExtra("mArticleAdapterSize", adapterSize);
             intent.putExtra(DISPLAYCHOICE_KEY, mDisplayChoice);
             intent.putExtra(ARTICLE_KEY, mArticleToDetail);
 
             for (int i = 0; i < adapterSize; i++) {
                 intent.putExtra("mArticleAdapterItem" + i,
                         mArticleAdapter.getItem(i));
             }
             Log.d("ArticleList", "Saved displaychoice : " + mDisplayChoice);
         }
 
         if (mArticleToDetail != null) {
             startActivity(intent);
         }
     }
 
     /**
      * restoreAndUpdateData restores data saved in some Intent and update a
      * modified article (which is also in Intent with special key).
      * 
      * This function is used when resume from ArticleDetailActivity because this
      * activity is able to update article states (read state and starred state).
      * */
     private void restoreAndUpdateData() {
         // Restore data
         Intent intent = getIntent();
         List<Article> articleList = new LinkedList<Article>();
 
         int adapterSize = intent.getIntExtra("mArticleAdapterSize", 0);
 
         Log.d("ArticleList",
                 "Ok, we are restoring data and we have to display : "
                         + mDisplayChoice + " and we've saved " + adapterSize
                         + " articles.");
 
         for (int i = 0; i < adapterSize; i++) {
             articleList.add((Article) intent
                     .getParcelableExtra("mArticleAdapterItem" + i));
         }
 
         mArticleAdapter = new ArticleAdapter(getApplicationContext(),
                 R.layout.articlelist_item, R.id.articleItemTitle, articleList);
 
         // Update one article if needed
         Article articleUpdated = intent.getParcelableExtra(ARTICLE_KEY);
 
         if (articleUpdated != null) {
             for (int i = 0; i < mArticleAdapter.getCount(); i++) {
                 Article articleIterator = mArticleAdapter.getItem(i);
 
                 int articleIteratorId = articleIterator.getId();
                 int articleUpdatedId = articleUpdated.getId();
                 if (articleIteratorId == articleUpdatedId) {
                     mArticleAdapter.remove(articleIterator);
                     mArticleAdapter.insert(articleUpdated, i);
                 }
             }
         }
 
         // Apply restored data
         updateArticleAdapter();
     }
 
     /**
      * ArticleAdapter is the specific ArrayAdapter for this ArticleListActivity.
      * */
     private class ArticleAdapter extends ArrayAdapter<Article> {
         private Context mContext;
         private List<Article> mArticles;
 
         public ArticleAdapter(Context context, int itemLayout, int textViewId,
                 List<Article> articleList) {
             super(context, itemLayout, textViewId, articleList);
             mArticles = articleList;
             mContext = context;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             Article article = mArticles.get(position);
 
             // Find the convertView
             if (convertView == null) {
                 LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = layoutInflater.inflate(R.layout.articlelist_item,
                         null);
             }
 
             // Manage article
             RelativeLayout articleItem = null;
             if (article != null) {
                 articleItem = (RelativeLayout) convertView
                         .findViewById(R.id.itemRelativeLayout);
                 if (articleItem != null) {
                     TextView articleItemTitle = (TextView) articleItem
                             .findViewById(R.id.articleItemTitle);
                     TextView articleItemAuthor = (TextView) articleItem
                             .findViewById(R.id.articleItemAuthor);
                     TextView articleItemDate = (TextView) articleItem
                             .findViewById(R.id.articleItemDate);
 
                     if (articleItemTitle != null && articleItemAuthor != null
                             && articleItemDate != null) {
                         if (!article.isRead()) {
                             articleItem.setBackgroundColor(getResources()
                                     .getColor(R.color.isUnRead));
                         } else {
                             articleItem.setBackgroundColor(getResources()
                                     .getColor(R.color.isRead));
                         }
 
                         String title = "";
                         if (article.isStarred()) {
                             title += "\u2605 ";
                         }
 
                         title += article.getSubject();
 
                         articleItemTitle.setText(title);
                         articleItemAuthor.setText(article.getAuthor());
                         articleItemDate.setText(article.getDate());
                     }
                 }
             }
             return articleItem;
         }
     }
 }
