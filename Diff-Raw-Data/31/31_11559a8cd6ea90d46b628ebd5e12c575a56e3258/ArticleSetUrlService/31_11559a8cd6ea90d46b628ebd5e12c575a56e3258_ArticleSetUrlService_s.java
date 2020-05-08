 package nz.gen.wellington.guardian.android.api;
 
 import nz.gen.wellington.guardian.android.about.AboutArticlesDAO;
 import nz.gen.wellington.guardian.android.api.openplatfrom.ContentApiUrlService;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.AboutArticleSet;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.usersettings.PreferencesDAO;
 import android.content.Context;
 
 public class ArticleSetUrlService {
 	
	private ContentApiUrlService contentApiUrlService;
 	private AboutArticlesDAO aboutArticlesDAO;
 	
 	public ArticleSetUrlService(Context context) {
		PreferencesDAO preferencesDAO = SingletonFactory.getPreferencesDAO(context);
		contentApiUrlService = new ContentApiUrlService(preferencesDAO.getPreferedApiHost(), preferencesDAO.getApiKey());
 		aboutArticlesDAO = new AboutArticlesDAO(context);
 	}
 	
 	public String getUrlForArticleSet(ArticleSet articleSet) {
 		if (articleSet instanceof AboutArticleSet) {
 			return aboutArticlesDAO.getAboutArticleSetUrl();
 		}
 		return contentApiUrlService.getContentApiUrlForArticleSet(articleSet);
 	}
 	
 }
