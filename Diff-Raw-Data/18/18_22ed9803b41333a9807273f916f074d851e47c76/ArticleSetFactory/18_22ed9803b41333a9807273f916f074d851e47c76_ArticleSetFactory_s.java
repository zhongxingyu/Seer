 package nz.gen.wellington.guardian.android.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.api.ArticleSetUrlService;
 import nz.gen.wellington.guardian.android.model.AboutArticleSet;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.FavouriteStoriesArticleSet;
 import nz.gen.wellington.guardian.android.model.SavedArticlesArticleSet;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.SectionArticleSet;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.model.TagArticleSet;
 import nz.gen.wellington.guardian.android.model.TopStoriesArticleSet;
 import nz.gen.wellington.guardian.android.usersettings.PreferencesDAO;
 import android.content.Context;
 
 public class ArticleSetFactory {

	private static final String TAG = "ArticleSetFactory";
 	
 	private PreferencesDAO preferencesDAO;
 	private ArticleSetUrlService articleSetUrlService;
 	
 	public ArticleSetFactory(Context context) {
 		this.preferencesDAO = SingletonFactory.getPreferencesDAO(context);
 		this.articleSetUrlService = new ArticleSetUrlService(context);
 	}
 
 	public ArticleSet getAboutArticleSet() {
 		return addUrl(new AboutArticleSet(preferencesDAO.getPageSizePreference()));
 	}
 	
 	public ArticleSet getSavedArticlesArticleSet(List<String> articleIds) {
 		SavedArticlesArticleSet savedArticlesArticleSet = new SavedArticlesArticleSet(articleIds);
 		savedArticlesArticleSet.setSourceUrl(articleSetUrlService.getUrlForArticleSet(savedArticlesArticleSet));
 		return savedArticlesArticleSet;
 	}
 	
 	public ArticleSet getArticleSetForSection(Section section) {
 		return addUrl(new SectionArticleSet(section, preferencesDAO.getPageSizePreference()));
 	}
 
 	public ArticleSet getFavouritesArticleSetFor(List<Section> favouriteSections, List<Tag> favouriteTags) {
 		return addUrl(new FavouriteStoriesArticleSet(favouriteSections, favouriteTags, preferencesDAO.getPageSizePreference()));
 	}
 
 	public ArticleSet getTopStoriesArticleSet() {
 		return addUrl(new TopStoriesArticleSet(preferencesDAO.getPageSizePreference()));
 	}
 
 	public ArticleSet getArticleSetForTag(Tag tag) {
 		return addUrl(new TagArticleSet(tag, preferencesDAO.getPageSizePreference()));
 	}
 	
 	public List<ArticleSet> getArticleSetsForSections(List<Section> favouriteSections) {
 		List<ArticleSet> favouriteSectionsArticleSets = new ArrayList<ArticleSet>();			
 		for (Section section : favouriteSections) {
 			favouriteSectionsArticleSets.add(getArticleSetForSection(section));
 		}
 		return favouriteSectionsArticleSets;
 	}
 	
 	public List<ArticleSet> getArticleSetsForTags(List<Tag> favouriteTags) {
 		List<ArticleSet> favouriteTagsArticleSets = new ArrayList<ArticleSet>();
 		for (Tag tag : favouriteTags) {
 			
 			boolean isSectionTag = tag.isSectionKeyword();
 			if (isSectionTag) {
 				favouriteTagsArticleSets.add(getArticleSetForSection(tag.getSection()));				
 			} else {
 				favouriteTagsArticleSets.add(getArticleSetForTag(tag));
 			}
 		}
 		return favouriteTagsArticleSets;
 	}
	
	
 	private ArticleSet addUrl(ArticleSet articleSet) {
 		articleSet.setSourceUrl(articleSetUrlService.getUrlForArticleSet(articleSet));
 		return articleSet;
 	}
 	
 }
