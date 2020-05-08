 package nz.gen.wellington.guardian.android.services;
 
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.api.ArticleDAO;
 import nz.gen.wellington.guardian.android.api.ArticleDAOFactory;
 import nz.gen.wellington.guardian.android.api.ContentFetchType;
 import nz.gen.wellington.guardian.android.api.ImageDAO;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.ContentUpdateReport;
 import nz.gen.wellington.guardian.android.network.NetworkStatusService;
 import nz.gen.wellington.guardian.android.usersettings.PreferencesDAO;
 import android.content.Context;
 
 public class UpdateArticleSetTask implements ContentUpdateTaskRunnable {
 
 	private PreferencesDAO preferencesDAO;
 	private NetworkStatusService networkStatusService;
 	private ImageDAO imageDAO;
 	private TaskQueue taskQueue;
 	
 	protected ContentUpdateReport report;
 	protected ArticleDAO articleDAO;
 	protected boolean running = true;
 	private ArticleSet articleSet;
 		
 	public UpdateArticleSetTask(Context context, ArticleSet articleSet) {
		this.articleSet = articleSet;
		preferencesDAO = ArticleDAOFactory.getPreferencesDAO(context);
 		imageDAO = ArticleDAOFactory.getImageDao(context);
 		networkStatusService = new NetworkStatusService(context);
 		taskQueue = ArticleDAOFactory.getTaskQueue(context);
 	}

 	
 	@Override
 	public String getTaskName() {
 		return "Fetching " + articleSet.getName();
 	}
 		
 	@Override
 	public void run() {
 		ArticleBundle bundle = articleDAO.getArticleSetArticles(articleSet, ContentFetchType.CHECKSUM);
 		if (bundle != null) {
 			processArticles(bundle.getArticles());
 		}
 	}
 
 	@Override
 	final public void setReport(ContentUpdateReport report) {
 		this.report = report;
 	}
 
 	@Override
 	public void stop() {
 		articleDAO.stopLoading();
 		running = false;
 	}
 	
 		
 	private void processArticles(List<Article> articles) {
 		final boolean queueMainImagesForDownload = preferencesDAO.getLargePicturesPreference() || networkStatusService.isWifiConnection();		
 		if (articles != null) {
 			for (Article article : articles) {
 				if (article.getThumbnailUrl() != null) {
 					String description = article.getTitle() + " - trail image";					
 					queueImageDownloadIfNotAvailableLocally(article.getThumbnailUrl(), description);
 				}
 				if (queueMainImagesForDownload && article.getMainImageUrl() != null) {
 					String description = article.getTitle() + " - main image";
 					if (article.getCaption() != null) {
 						description = article.getCaption();
 					}
 					queueImageDownloadIfNotAvailableLocally(article.getMainImageUrl(), description);
 				}
 				report.setArticleCount(report.getArticleCount()+1);
 			}
 		}		
 	}
 	
 	
 	private void queueImageDownloadIfNotAvailableLocally(String imageUrl, String description) {
 		if (imageUrl != null && running) {
 			if (!imageDAO.isAvailableLocally(imageUrl)) {
 				taskQueue.addImageTask(new ImageFetchTask(imageUrl, description, imageDAO));
 			}
 		}
 	}
 	
 }
