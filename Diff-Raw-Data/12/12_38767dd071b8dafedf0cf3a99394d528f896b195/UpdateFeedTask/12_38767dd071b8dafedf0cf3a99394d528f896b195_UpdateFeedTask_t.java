 package com.ximoneighteen.android.rssreader.util;
 
import java.util.List;

 import android.os.AsyncTask;
 
 import com.ximoneighteen.android.rssreader.model.Article;
 import com.ximoneighteen.android.rssreader.model.DbHelper;
 import com.ximoneighteen.android.rssreader.model.Feed;
 
 public class UpdateFeedTask extends AsyncTask<Feed, Void, Void> {
 	private DbHelper db;
 
 	public UpdateFeedTask(DbHelper db) {
 		this.db = db;
 	}
 	
 	@Override
 	protected Void doInBackground(Feed... params) {
 		Feed feed = params[0];
 		RssDownloader downloader = new RssDownloader(feed);
 		downloader.run();
 
		List<Article> articles = downloader.getArticles();
		if (articles != null) {
			for (Article article : articles) {
				article.setParagraphs(ArticleParagraphFetcher.fetch(article.getLink()));
				db.putArticle(article);
			}
 		}
 
 		return null;
 	}
 }
