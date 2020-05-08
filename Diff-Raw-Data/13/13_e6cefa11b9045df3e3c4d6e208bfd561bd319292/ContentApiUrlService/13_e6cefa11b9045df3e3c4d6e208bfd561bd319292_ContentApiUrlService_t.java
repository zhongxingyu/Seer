 package nz.gen.wellington.guardian.android.api.openplatfrom;
 
 import java.util.List;
 
import android.util.Log;

 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.FavouriteTagsArticleSet;
 import nz.gen.wellington.guardian.android.model.SearchResultsArticleSet;
 import nz.gen.wellington.guardian.android.model.SectionArticleSet;
 import nz.gen.wellington.guardian.android.model.TagArticleSet;
 
 public class ContentApiUrlService {
 	
 	private String apiHost;
 	private String apiKey;
 	
 	public ContentApiUrlService(String apiHost, String apiKey) {
 		this.apiHost = apiHost;
 		this.apiKey = apiKey;
 	}
 
 	public String getContentApiUrlForArticleSet(ArticleSet articleSet) {
 		ContentApiStyleUrlBuilder contentApiUrlBuilder = getContentApiUrlBuilder();
 		populateContentApiUrlBuilderForArticleSet(contentApiUrlBuilder, articleSet);
 		contentApiUrlBuilder.setShowAll(true);
 		contentApiUrlBuilder.setShowRefinements(true);	
 		return contentApiUrlBuilder.toSearchQueryUrl();
 	}
 	
 	public String getContentApiUrlForArticleSetChecksum(ArticleSet articleSet) {
 		ContentApiStyleUrlBuilder contentApiUrlBuilder = getContentApiUrlBuilder();
 		populateContentApiUrlBuilderForArticleSet(contentApiUrlBuilder, articleSet);		
 		contentApiUrlBuilder.setShowAll(false);
 		contentApiUrlBuilder.setShowRefinements(false);	
 		return contentApiUrlBuilder.toSearchQueryUrl();
 	}
 	
 	public String getSectionsQueryUrl() {
 		ContentApiStyleUrlBuilder contentApiUrlBuilder = getContentApiUrlBuilder();
 		contentApiUrlBuilder.setFormat("json");
 		return contentApiUrlBuilder.toSectionsQueryUrl();
 	}
 	
 	public String getTagSearchQueryUrl(String searchTerm, List<String> allowedTypes) {
 		ContentApiStyleUrlBuilder contentApiUrlBuilder = getContentApiUrlBuilder();
 		contentApiUrlBuilder.setPageSize(20);		
 		contentApiUrlBuilder.setFormat("json");
 		contentApiUrlBuilder.setSearchTerm(searchTerm);
 		
 		for (String allowedType : allowedTypes) {
 			contentApiUrlBuilder.addType(allowedType);			
 		}
 				
 		return contentApiUrlBuilder.toTagSearchQueryUrl();
 	}
 	
 	private ContentApiStyleUrlBuilder getContentApiUrlBuilder() {	
 		return new ContentApiStyleUrlBuilder(apiHost, apiKey);
 	}	
 	
 	private void populateContentApiUrlBuilderForArticleSet(ContentApiStyleUrlBuilder contentApiUrlBuilder, ArticleSet articleSet) {
 		if (articleSet instanceof SectionArticleSet) {
 			SectionArticleSet sectionArticleSet = (SectionArticleSet) articleSet;
 			contentApiUrlBuilder.addSection(((SectionArticleSet) articleSet).getSection());
 			if (sectionArticleSet.getFromDate() != null) {
 				contentApiUrlBuilder.setFromDate(sectionArticleSet.getFromDate());
 				contentApiUrlBuilder.setToDate(sectionArticleSet.getToDate());
 			}
 		}
 		
 		if (articleSet instanceof TagArticleSet) {
 			TagArticleSet tagArticleSet = (TagArticleSet) articleSet;
 			contentApiUrlBuilder.addTag(tagArticleSet.getTag());
 			if (tagArticleSet.getFromDate() != null) {
 				contentApiUrlBuilder.setFromDate(tagArticleSet.getFromDate());
 				contentApiUrlBuilder.setToDate(tagArticleSet.getToDate());
 			}
 		}
 		
 		if (articleSet instanceof FavouriteTagsArticleSet) {
 			FavouriteTagsArticleSet favouriteStoriesArticleSet = (FavouriteTagsArticleSet) articleSet;
			for (ArticleSet favouriteArticleSet : favouriteStoriesArticleSet.getArticleSets()) {
				if (favouriteArticleSet instanceof SectionArticleSet) {
					contentApiUrlBuilder.addSection(((SectionArticleSet) favouriteArticleSet).getSection());					
				} else if (favouriteArticleSet instanceof TagArticleSet) {
					contentApiUrlBuilder.addTag(((TagArticleSet) favouriteArticleSet).getTag());
 				}
 			}
 		}
 		
 		if (articleSet instanceof SearchResultsArticleSet) {
 			contentApiUrlBuilder.setSearchTerm(((SearchResultsArticleSet) articleSet).getSearchTerm());
 		}
 		
 		contentApiUrlBuilder.addType("article");		
 		contentApiUrlBuilder.setPageSize(articleSet.getPageSize());		
 		contentApiUrlBuilder.setFormat("xml");
 	}
 	
 }
