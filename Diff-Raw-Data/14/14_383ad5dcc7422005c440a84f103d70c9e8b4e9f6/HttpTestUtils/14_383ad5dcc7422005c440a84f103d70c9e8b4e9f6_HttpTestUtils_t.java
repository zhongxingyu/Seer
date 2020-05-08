 package mtgscraper.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class HttpTestUtils {
 
 	private HttpTestUtils() {
 	}
 	
 	public static Document loadDocumentFromResource(String path) throws IOException {
 
 		InputStream inputStream = null;
 		try {
 			inputStream = CardProcessorTest.class.getResourceAsStream(path);
 			return Jsoup.parse(inputStream, "utf-8", "http://magiccards.info/");
 		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
 		}
 	}
 }
