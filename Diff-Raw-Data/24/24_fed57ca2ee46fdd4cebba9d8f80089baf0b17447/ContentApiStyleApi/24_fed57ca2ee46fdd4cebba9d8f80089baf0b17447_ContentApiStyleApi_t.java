 package nz.gen.wellington.guardian.android.api.openplatfrom;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.android.activities.ui.ArticleCallback;
 import nz.gen.wellington.guardian.android.api.ContentSource;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.network.HttpFetcher;
 import nz.gen.wellington.guardian.android.network.LoggingBufferedInputStream;
 import nz.gen.wellington.guardian.android.usersettings.SettingsDAO;
 import android.content.Context;
 import android.util.Log;
 
 public class ContentApiStyleApi implements ContentSource {
 		
 	private static final String TAG = "ContentApiStyleApi";
 		
 	private ContentApiStyleXmlParser contentXmlParser;
 	private ContentApiStyleJSONParser contentJsonParser;
 	private HttpFetcher httpFetcher;
 	private SettingsDAO settingsDAO;
 	
 	public ContentApiStyleApi(Context context) {
 		this.contentXmlParser = new ContentApiStyleXmlParser(context);
 		this.contentJsonParser = new ContentApiStyleJSONParser();
 		this.settingsDAO = SingletonFactory.getSettingsDAO(context);
 		this.httpFetcher = new HttpFetcher(context);
 	}
 	
 	
 	@Override
 	public ArticleBundle getArticles(ArticleSet articleSet, List<Section> sections, ArticleCallback articleCallback) {
 		Log.i(TAG, "Fetching articles for: " + articleSet.getName());
 		
		final String contentApiUrl = articleSet.getSourceUrl() + "&v=" + settingsDAO.getClientVersion();		
 		LoggingBufferedInputStream input = httpFetcher.httpFetch(contentApiUrl, articleSet.getName() + " article set");	
 		if (input != null) {
 			ArticleBundle results = contentXmlParser.parseArticlesXml(input, articleCallback);
 			if (results != null && !results.getArticles().isEmpty()) {
 				String checksum = input.getEtag();
 				results.setChecksum(checksum);
 				try {
 					input.close();
 				} catch (IOException e) {
 					Log.w(TAG, "Failed to close input stream");
 				}
 				return results;
 			}
 		}
 		return null;
 	}
 	
 	
 	@Override
 	public String getRemoteChecksum(ArticleSet articleSet, int pageSize) {		
 		Log.i(TAG, "Fetching article set checksum for article set: " + articleSet.getName());		
		final String contentApiUrl = articleSet.getSourceUrl();	
 		return httpFetcher.httpEtag(contentApiUrl, articleSet.getName() + " article set checksum");
 	}
 	
 	
 	@Override
 	public List<Section> getSections() {
 		Log.i(TAG, "Fetching section list from live api");
 		ContentApiUrlService contentApiUrlService = new ContentApiUrlService(settingsDAO.getPreferedApiHost(), settingsDAO.getApiKey(), settingsDAO.getSupportedContentTypes());
 		String contentApiUrl = contentApiUrlService.getSectionsQueryUrl();
 		InputStream input = httpFetcher.httpFetch(contentApiUrl, "sections");
 		if (input != null) {
 			List<Section> results = contentJsonParser.parseSectionsJSON(input);
 			try {
 				input.close();
 			} catch (IOException e) {
 				Log.w(TAG, "Failed to close input stream");
 			}
 			return results;
 		}
 		return null;
 	}
 	
 	
 	@Override
 	public List<Tag> searchTags(String searchTerm, List<String> allowedTagSearchTypes, Map<String, Section> sections) {
 		Log.i(TAG, "Fetching tag list from live api: " + searchTerm);
 		ContentApiUrlService contentApiUrlService = new ContentApiUrlService(settingsDAO.getPreferedApiHost(), settingsDAO.getApiKey(), settingsDAO.getSupportedContentTypes());
 		
 		InputStream input = httpFetcher.httpFetch(contentApiUrlService.getTagSearchQueryUrl(searchTerm, allowedTagSearchTypes), "tag results");
 		if (input != null) {
 			List<Tag> results = contentJsonParser.parseTagsJSON(input, sections);
 			try {
 				input.close();
 			} catch (IOException e) {
 				Log.w(TAG, "Failed to close input stream");
 			}
 			return results;
 		}
 		return null;
 	}
 
 
 	@Override
 	public void stopLoading() {
 		Log.i(TAG, "Stopping content api loading");
 		contentXmlParser.stop();
 		httpFetcher.stopLoading();
 	}
 	
 }
