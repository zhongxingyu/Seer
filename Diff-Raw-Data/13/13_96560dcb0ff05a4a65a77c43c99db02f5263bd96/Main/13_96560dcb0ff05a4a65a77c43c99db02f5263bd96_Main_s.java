 package crawling;
 
 import java.io.IOException;
 
 import util.Auth;
 import util.Util;
 import view.HtmlPrinter;
 import view.Visualizer;
 
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.api.services.plus.Plus;
 
 public class Main {
	public static final String WHAT = "me";
 	public static final String FILENAME = "output.network";
 	public static final String DATABASE_FILENAME = "network.db";
 
 	public static void main(String[] args) throws IOException {
 		// scan data from web
 		 Network net = scanAndWrite();
 
 		Node user = net.get(WHAT);
 		String html = new Visualizer(user).getHtml();
 		HtmlPrinter.showHtml(html);
 	}
 
 	public static Network read() throws IOException {
 		Network net = new Network();
 
 		// TODO
 
 		return net;
 	}
 
 	public static Network scanAndWrite() throws IOException {
 		// Set up the HTTP transport and JSON factory
 		HttpTransport httpTransport = new NetHttpTransport();
 		JsonFactory jsonFactory = new JacksonFactory();
 
 		// Set up OAuth 2.0 access of protected resources
 		// using the refresh and access tokens, automatically
 		// refreshing the access token when it expires
 		Auth.authorize();
 
 		GoogleAccessProtectedResource requestInitializer = new GoogleAccessProtectedResource(
 				Auth.getAccessToken(), httpTransport, jsonFactory,
 				Auth.CLIENT_ID, Auth.CLIENT_SECRET, Auth.getRefreshToken());
 
 		// Set up the main Google+ class
 		Plus plus = Plus.builder(httpTransport, jsonFactory)
 				.setHttpRequestInitializer(requestInitializer).build();
 
 		Network net = new Network();
 		net.put(new Node(WHAT));
 		net.crawl(WHAT, 0, plus);
 		Database data = new Database();
 		Util.write("importing database");
 		data.load(DATABASE_FILENAME);
 		Util.write("imported!");
 		net.write();
 		Util.write("" + net.getRatio(WHAT));
 		Util.write("" + net.getReceivedPlus(WHAT));
 		Util.write("" + net.getSentPlus(WHAT));
 		Util.write("adding network to database");
 		data.addNetwork(net);
 		Util.write("serialzing database");
 		data.serialize(DATABASE_FILENAME);
 		Util.write("serialized!");
 
 		return net;
 	}
 }
