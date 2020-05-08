 package nz.gen.wellington.guardian.android.widgets;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.activities.widgets.mainwidget;
 import nz.gen.wellington.guardian.android.api.ArticleDAO;
 import nz.gen.wellington.guardian.android.api.ContentFetchType;
 import nz.gen.wellington.guardian.android.api.ImageDAO;
 import nz.gen.wellington.guardian.android.factories.ArticleSetFactory;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.view.View;
 import android.widget.RemoteViews;
 
 // TODO pull a common abstract class for these two
 public class TopStoriesWidget extends AppWidgetProvider {
 			
 	ArticleViews firstArticleViews = new ArticleViews(
 			R.id.WidgetFirstItem, R.id.WidgetHeadline, R.id.WidgetStandfirst,
 			R.id.WidgetImage);
 	
 	ArticleViews secondArticleViews = new ArticleViews(
 			R.id.WidgetSecondItem, R.id.WidgetSecondHeadline,
 			R.id.WidgetSecondStandfirst, R.id.WidgetSecondImage);
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {		
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 		refresh(context, appWidgetIds);
 	}
 	
 	protected ArticleSet getArticleSet(int pageSize, Context context) {
 		ArticleSetFactory articleSetFactory = SingletonFactory.getArticleSetFactory(context);
 		return articleSetFactory.getTopStoriesArticleSet();
 	}
 	
 	
 	private void refresh(Context context, int[] appWidgetIds) {
 		ArticleBundle stories = getArticles(context);
 		
 		ImageDAO imageDAO = SingletonFactory.getImageDao(context);
 		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
 		if (stories != null && stories.getArticles() != null) {
 			
 			List<Article> randomArticles = selectTwoRandomArticleWithTrailImages(stories.getArticles());
 			if (randomArticles.size() > 0) {
 				populateArticle(widgetView, imageDAO, randomArticles.get(0), context, firstArticleViews);
 				if (randomArticles.size() > 1) {
 					populateArticle(widgetView, imageDAO, randomArticles.get(1), context, secondArticleViews);
 				} else {
					hideSecondArticle(widgetView);
 				}
 				
 			} else {
 				showNoArticlesMessage(context, imageDAO, widgetView);				
 			}
 						
 		} else {			
 			showNoArticlesMessage(context, imageDAO, widgetView);
 		}
 		
 		AppWidgetManager manager = AppWidgetManager.getInstance(context);
 		if (manager != null) {
 			manager.updateAppWidget(appWidgetIds, widgetView);
 		}
 	}
 
 	
	private void showNoArticlesMessage(Context context, ImageDAO imageDAO, RemoteViews widgetView) {
 		Article errorMessage = new Article();
 		errorMessage.setTitle("No articles available");
 		errorMessage.setStandfirst(getNoArticlesExplainationText());
 		populateArticle(widgetView, imageDAO, errorMessage, context, firstArticleViews);
		
		hideSecondArticle(widgetView);
	}

	private void hideSecondArticle(RemoteViews widgetView) {
		widgetView.setViewVisibility(R.id.WidgetSecondItem, View.GONE);
 	}
 
 	protected String getNoArticlesExplainationText() {
 		return "You may need to sync this article set";
 	}
 		
 	private ArticleBundle getArticles(Context context) {
 		ArticleSet articleSet = getArticleSet(SingletonFactory.getPreferencesDAO(context).getPageSizePreference(), context);
 		if (articleSet == null) {
 			return null;
 		}
 		ArticleDAO articleDAO = SingletonFactory.getDao(context);
 		return articleDAO.getArticleSetArticles(articleSet, ContentFetchType.LOCAL_ONLY);
 	}
 	
 	
 	private List<Article> selectTwoRandomArticleWithTrailImages(List<Article> articles) {
 		List<Article> randomArticles = new ArrayList<Article>();
 		
 		List<Article> articleWithTrailImages = selectArticlesWithTrailImages(articles);
 		while (randomArticles.size() < 2 && !articleWithTrailImages.isEmpty()) {
 			int articleIndex = new Random(new Date().getTime()).nextInt(articleWithTrailImages.size()-1);
 			Article article = articleWithTrailImages.get(articleIndex);			
 			randomArticles.add(article);
 			articleWithTrailImages.remove(article);			
 		}		
 		return randomArticles;
 	}
 
 	private List<Article> selectArticlesWithTrailImages(List<Article> articles) {
 		List<Article> articleWithTrailImages = new ArrayList<Article>();
 		for (Article article : articles) {
 			if (article.getThumbnailUrl() != null) {
 				articleWithTrailImages.add(article);
 			}
 		}
 		return articleWithTrailImages;
 	}
 	
 	private void populateArticle(RemoteViews widgetView, ImageDAO imageDAO, Article article, Context context, ArticleViews articleViews) {
 		widgetView.setTextViewText(articleViews.headline, article.getTitle());
 		widgetView.setTextViewText(articleViews.standfirst, article.getStandfirst());
 		
 		if (article.getThumbnailUrl() != null && imageDAO.isAvailableLocally(article.getThumbnailUrl())) {				
 			Bitmap trailImage = imageDAO.getImage(article.getThumbnailUrl());
 			widgetView.setViewVisibility(articleViews.image, View.VISIBLE);
 			widgetView.setImageViewBitmap(articleViews.image, trailImage);
 		} else {
 			widgetView.setViewVisibility(articleViews.image, View.GONE);
 		}
 		
 		PendingIntent pendingIntent = createShowArticleIntent(context, article);
 		widgetView.setOnClickPendingIntent(articleViews.view, pendingIntent);		
 		widgetView.setViewVisibility(articleViews.view, View.VISIBLE);
 	}
 
 	protected PendingIntent createShowArticleIntent(Context context, Article article) {
 		Intent intent = new Intent(context, mainwidget.class);		
 		intent.setData(Uri.withAppendedPath(Uri.parse("content://article/id"), String.valueOf(article.getId())));
         PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
 		return pendingIntent;
 	}
 	
 	private  class ArticleViews {
 		int view;
 		int headline;
 		int standfirst;
 		int image;
 
 		public ArticleViews(int view, int headline, int standfirst, int image) {
 			this.view = view;
 			this.headline = headline;
 			this.standfirst = standfirst;
 			this.image = image;
 		}		
 	}
 	
 }
