 package ch.adorsaz.loungeDroid.gui;
 
 import java.util.List;
 
 import ch.adorsaz.loungeDroid.R;
 import ch.adorsaz.loungeDroid.article.ToDisplay;
 import ch.adorsaz.loungeDroid.article.Article;
 import ch.adorsaz.loungeDroid.servercom.ArticleListGetter;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ArticleListActivity extends ListActivity {
 
     private List<Article> mArticleList = null;
     private ArticleAdapter mArticleAdapter = null;
     private ToDisplay mDisplayChoice = null;
    
    protected static final String ARTICLE_KEY = "article";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         fetchNews();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_article_list, menu);
         disableToDisplayMenu(menu);
         return true;
     }
     
     @Override
     public boolean onPrepareOptionsMenu (Menu menu) {
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
             //case R.id.mainSettings:
                 /*
                  * Intent intent; intent = new Intent(getBaseContext(),
                  * Preference.class); startActivity(intent);
                  */
               //  break;
         }
         return false;
     }
 
     public void updateArticleList(List<Article> articleList) {
         mArticleList = articleList;
         mArticleAdapter = new ArticleAdapter(getApplicationContext(),
                 R.layout.articlelist_item, R.id.articleItemTitle, mArticleList);
 
         ListView listView = getListView();
        //listView.setDividerHeight(5);
         listView.setScrollingCacheEnabled(false); // TODO : Just save some
                                                   // memory, check if needed
         listView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
 
                 Intent intent = new Intent(ArticleListActivity.this,
                         ArticleDetailActivity.class);
                intent.putExtra(ARTICLE_KEY, mArticleList.get(position));
                 startActivity(intent);
             }
         });
         
        setListAdapter(mArticleAdapter);
        //registerForContextMenu(listView);
     }
 
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
             }
         }
 
     }
 
     private void disableMenuItem(Menu menu, int id) {
         MenuItem menuItem = menu.findItem(id);
         menuItem.setVisible(false);
         menuItem.setEnabled(false);
     }
 
     private void enableMenuItem(MenuItem menuItem) {
         menuItem.setVisible(true);
         menuItem.setEnabled(true);
     }
 
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
            LinearLayout articleItem = null;
             if (article != null) {
                articleItem = (LinearLayout) convertView
                         .findViewById(R.id.itemLinearLayout);
                 if (articleItem != null) {
                     TextView articleItemTitle = (TextView) articleItem
                             .findViewById(R.id.articleItemTitle);
                     TextView articleItemAuthor = (TextView) articleItem
                             .findViewById(R.id.articleItemAuthor);
                     TextView articleItemDate = (TextView) articleItem
                             .findViewById(R.id.articleItemDate);
 
                     if (articleItemTitle != null && articleItemAuthor != null
                             && articleItemDate != null) {
                         String title = "";
                         if (!article.isRead()) {
                             title = "* ";
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
