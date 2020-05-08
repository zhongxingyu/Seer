 package nz.gen.wellington.guardian.android.api.openplatfrom;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import nz.gen.wellington.guardian.android.activities.ArticleCallback;
 import nz.gen.wellington.guardian.android.api.filtering.HtmlCleaner;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 
 import org.xml.sax.SAXException;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ContentApiStyleXmlParser {
 	
 	public static final String ARTICLE_AVAILABLE = "nz.gen.wellington.guardian.android.api.ARTICLE_AVAILABLE";
 	
 	private static final String TAG = "ContentApiStyleXmlParser";
 	
 	private ContentResultsHandler handler;
 	
 	public ContentApiStyleXmlParser(Context context) {
		// TODO obviously not thread safe - complex return type will allow the handler to be pushed down to method scope
 		this.handler = new ContentResultsHandler(SingletonFactory.getSectionDAO(context), new HtmlCleaner());
 	}
 
 
 	public List<Article> parseArticlesXml(InputStream inputStream, ArticleCallback articleCallback) {
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 			handler.setArticleCallback(articleCallback);
 			saxParser.parse(inputStream, handler);
 			inputStream.close();
 			return handler.getArticles();
 
 		} catch (SAXException e) {
 			Log.e(TAG, "Error while parsing content xml: " + e.getMessage());
 		} catch (IOException e) {
 			Log.e(TAG, "Error while parsing content xml: " + e.getMessage());
 		} catch (ParserConfigurationException e) {
 			Log.e(TAG, "Error while parsing content xml: " + e.getMessage());
 		}
 		return null;
 	}
 	
 	// TODO should pass back a complex object rather then exposing the handler like this.
 	// method is only ever called straight after parse for articles.
 	public Map<String, List<ArticleSet>> getRefinements() {
 		if (handler != null) {
 			return handler.getRefinements();
 		}
 		return new HashMap<String, List<ArticleSet>>();
 	}	
 	public String getDescription() {
 		if (handler != null) {
 			return handler.getDescription();
 		}
 		return null;
 	}
 
 	
 	public String getChecksum() {
 		if (handler != null) {
 			return handler.getChecksum();
 		}
 		return null;
 	}
 	
 	public void stop() {
 		if (handler != null) {
 			handler.stop();
 		}
 	}
 	
 }
