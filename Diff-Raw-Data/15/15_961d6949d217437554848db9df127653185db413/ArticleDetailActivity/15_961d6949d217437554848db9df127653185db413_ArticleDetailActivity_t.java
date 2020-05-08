 package ch.adorsaz.loungeDroid.activities;
 
 import ch.adorsaz.loungeDroid.R;
 import ch.adorsaz.loungeDroid.article.Article;
 import ch.adorsaz.loungeDroid.servercom.ArticleReadStateUpdater;
 import ch.adorsaz.loungeDroid.servercom.ArticleStarredStateUpdater;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.content.Intent;
 
 public class ArticleDetailActivity extends Activity implements OnClickListener {
 
     private Article mArticle = null;
     private TextView mTitle = null;
     private TextView mAuthor = null;
     private TextView mDate = null;
     private WebView mContent = null;
     private Button mReadButton = null;
     private Button mStarButton = null;
     private Button mBrowserButton = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.articlelist_detail);
         mArticle = this.getIntent().getParcelableExtra(
                 ArticleListActivity.ARTICLE_KEY);
 
         Log.d("ArticleDetail", "Article received : " + mArticle.getId());
 
         mTitle = (TextView) findViewById(R.id.newsTitle);
         mTitle.setText(mArticle.getSubject());
 
         mAuthor = (TextView) findViewById(R.id.newsAuthor);
         mAuthor.setText(mArticle.getAuthor());
 
         mDate = (TextView) findViewById(R.id.newsDate);
         mDate.setText(mArticle.getDate());
 
         mContent = (WebView) findViewById(R.id.newsContent);
         mContent.loadData(Uri.encode(mArticle.getContent()),
                 "text/html; charset=UTF-8", "UTF-8");
 
         mReadButton = (Button) findViewById(R.id.newsRead);
         mReadButton.setOnClickListener(this);
         if (mArticle.isRead()) {
             mReadButton.setText(R.string.notRead);
         } else {
             mReadButton.setText(R.string.markAsRead);
         }
 
         mStarButton = (Button) findViewById(R.id.newsStar);
         mStarButton.setOnClickListener(this);
         if (mArticle.isStarred()) {
             mStarButton.setText(R.string.unStar);
         } else {
             mStarButton.setText(R.string.starIt);
         }
 
         mBrowserButton = (Button) findViewById(R.id.newsBrowser);
         mBrowserButton.setOnClickListener(this);
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.newsRead:
                 ArticleReadStateUpdater readUpdate = new ArticleReadStateUpdater(
                         this);
                 readUpdate.execute(mArticle);
                 break;
             case R.id.newsStar:
                 ArticleStarredStateUpdater starUpdate = new ArticleStarredStateUpdater(
                         this);
                 starUpdate.execute(mArticle);
                 break;
             case R.id.newsBrowser:
                 Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                 browserIntent.setData(Uri.parse(mArticle.getLink()));
                 startActivity(browserIntent);
                 break;
         }
     }
 
    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent(ArticleDetailActivity.this,
                ArticleListActivity.class);
        intent.putExtra(ArticleListActivity.ARTICLE_KEY, mArticle);
        startActivity(intent);
        finish();
    }
 
     public void updateReadButton() {
         if (mArticle.isRead()) {
             mReadButton.setText(R.string.notRead);
             mReadButton.refreshDrawableState();
             Toast.makeText(getApplicationContext(), "Marked as read.",
                     Toast.LENGTH_SHORT).show();
         } else {
             mReadButton.setText(R.string.markAsRead);
             mReadButton.refreshDrawableState();
             Toast.makeText(getApplicationContext(), "Marked as unread.",
                     Toast.LENGTH_SHORT).show();
         }
     }
 
     public void updateStarredButton() {
         if (mArticle.isStarred()) {
             mStarButton.setText(R.string.unStar);
             mReadButton.refreshDrawableState();
             Toast.makeText(getApplicationContext(), "Starred.",
                     Toast.LENGTH_SHORT).show();
         } else {
             mStarButton.setText(R.string.starIt);
             mReadButton.refreshDrawableState();
             Toast.makeText(getApplicationContext(), "Unstarred.",
                     Toast.LENGTH_SHORT).show();
         }
     }
 }
