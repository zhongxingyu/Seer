 package nz.gen.wellington.guardian.android.api.openplatfrom;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import nz.gen.wellington.guardian.android.activities.ArticleCallback;
 import nz.gen.wellington.guardian.android.api.filtering.HtmlCleaner;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 
 import org.xml.sax.SAXException;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ContentApiStyleXmlParser {
 	
 	public static final String ARTICLE_AVAILABLE = "nz.gen.wellington.guardian.android.api.ARTICLE_AVAILABLE";
 	
 	private static final String TAG = "ContentApiStyleXmlParser";

	private Context context;
 	private ContentResultsHandler handler;
 	
 	public ContentApiStyleXmlParser(Context context) {
		this.context = context;
 	}
 	
 	public ArticleBundle parseArticlesXml(InputStream inputStream, ArticleCallback articleCallback) {
 		try {
			handler = new ContentResultsHandler(context, new HtmlCleaner());	// TODO push to singleton factory
			
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 			handler.setArticleCallback(articleCallback);
 			saxParser.parse(inputStream, handler);
 			inputStream.close();
 			return handler.getResult();
 
 		} catch (SAXException e) {
			Log.e(TAG, "SAXException while parsing content xml: " + e.getMessage());			
 		} catch (IOException e) {
			Log.e(TAG, "IOException while parsing content xml: " + e.getMessage());
 		} catch (ParserConfigurationException e) {
			Log.e(TAG, "ParserConfigurationException while parsing content xml: " + e.getMessage());
 		}
 		return null;
 	}
 	
 	public void stop() {
 		if (handler != null) {
 			handler.stop();
 		}
 	}
 	
 }
