 package awesome.app;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.util.EntityUtils;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.widget.Toast;
 
 public class NetworkManager {
 
	public static void getData(String url, DefaultHandler handler,
			Context context) {
 		getData(url, handler, new ArrayList<NameValuePair>(), context);
 	}

 	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			return cm.getActiveNetworkInfo().isAvailable();
		} catch (Exception e) {
			return false;
		}
 	}
 
	public static void getData(String url, DefaultHandler handler,
			List<NameValuePair> pairs, Context context) {
 		try {
			// HttpClient client = new DefaultHttpClient();
 			HttpClient client = SecurityHole.getNewHttpClient();
 			HttpPost post = new HttpPost(url);
 			post.setEntity(new UrlEncodedFormEntity(pairs));
 			HttpResponse response = client.execute(post);
 			HttpEntity entity = response.getEntity();
 			String results = EntityUtils.toString(entity);
 			SAXParserFactory spf = SAXParserFactory.newInstance();
 			SAXParser sp = spf.newSAXParser();
 			XMLReader xr = sp.getXMLReader();
 			xr.setContentHandler(handler);
 			xr.parse(new InputSource(new StringReader(results)));
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			Toast.makeText(context, "Parser Error", Toast.LENGTH_SHORT).show();
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
			Toast.makeText(context, "Error Fetching Data", Toast.LENGTH_SHORT)
					.show();
 			e.printStackTrace();
 		} catch (IOException e) {
			Toast.makeText(context, "I/O Exception!", Toast.LENGTH_SHORT)
					.show();
 			e.printStackTrace();
 
 		}
 	}
 
 }
