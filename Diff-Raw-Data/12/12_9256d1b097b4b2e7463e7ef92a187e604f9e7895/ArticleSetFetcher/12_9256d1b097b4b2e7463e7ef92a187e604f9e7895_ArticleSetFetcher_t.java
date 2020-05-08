 package nz.gen.wellington.guardian.android.content;
 
 import java.io.IOException;
 
 import nz.gen.wellington.guardian.android.activities.ui.ArticleCallback;
 import nz.gen.wellington.guardian.android.api.openplatfrom.ContentApiStyleXmlParser;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.network.HttpFetcher;
 import nz.gen.wellington.guardian.android.network.LoggingBufferedInputStream;
 import android.content.Context;
 import android.util.Log;
 
 public class ArticleSetFetcher {
 	
 	private static final String TAG = "ArticleSetFetcher";
 	
 	private HttpFetcher httpFetcher;
 	private ContentApiStyleXmlParser contentXmlParser;	
 	
 	private int clientVersion;
 	
 	public ArticleSetFetcher(Context context, int clientVersion) {
 		this.contentXmlParser = new ContentApiStyleXmlParser(context);
 		this.httpFetcher = new HttpFetcher(context);
 		this.clientVersion = clientVersion;
 	}
 		
 	public ArticleBundle getArticles(ArticleSet articleSet, ArticleCallback articleCallback) {		
 		Log.i(TAG, "Fetching article for article set: " + articleSet.getName());
 		
		final String contentApiUrl = appendClientVersion(articleSet.getSourceUrl());
 		LoggingBufferedInputStream input = httpFetcher.httpFetch(contentApiUrl, articleSet.getName());
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
 	
	private String appendClientVersion(String url) {
		// TODO somewhat rubbish implementation
		if (url.contains("?")) {
			return url + "&v=" + clientVersion;			
		} else {
			return url + "?v=" + clientVersion;
		}
	}
	
 }
