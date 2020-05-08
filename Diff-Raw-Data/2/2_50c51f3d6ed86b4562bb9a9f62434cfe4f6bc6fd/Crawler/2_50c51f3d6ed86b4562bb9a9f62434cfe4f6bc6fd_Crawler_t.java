 /*
 	Programmers: An Dang + Pedro Benedicte
 	Network Fundamentals
 	Project 2 - Fakebook Web Crawler
 
 */
 
 package ccs.neu.edu.andang ;
 
 import java.util.List ;
 import java.util.LinkedList ;
 import java.util.Set ;
 import java.util.HashSet ;
 import java.util.Queue ;
 import java.util.Map ;
 import java.util.HashMap ;
 import java.util.Iterator ;
 import com.google.common.collect.Multimap ;
 import com.google.common.collect.HashMultimap ;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.Jsoup;
 import org.jsoup.select.Elements;
 import org.jsoup.nodes.Element;
 
 import java.net.UnknownHostException ;
 import java.net.SocketException ;
 import java.net.MalformedURLException;
 import java.io.IOException ;
 
 import java.net.URL ;
 
 public class Crawler {
 
 	private final String HOST = "http://cs5700.ccs.neu.edu/" ;
 	private final String LOG_IN_URL = "http://cs5700.ccs.neu.edu/accounts/login/?next=/fakebook/" ;
 	private HTTPClient client ;
 	private String id ;
 	private String password ;
 	private URL rootURL ;
 	private URL logInURL ;
 	private Set<String> visitedURL ;
 	private Queue<URL> frontierURL ;
 	private List<String> secretFlags ;
 	private Map<String, String> cookies ;
 
 	public Crawler( String id, String password ){
 		this.id = id ;
 		this.password = password ;
 		this.client = new HTTPClient() ;
 		this.visitedURL = new HashSet<String>() ;
 		this.frontierURL = new LinkedList<URL>() ;
 		this.cookies = new HashMap<String,String>() ;
 		try{
 			this.rootURL = new URL( HOST )  ;
 			this.logInURL = new URL( LOG_IN_URL ) ;
 		}
 		catch (MalformedURLException ex){
 			throw new RuntimeException( "Internal Crawler's error " + ex.toString() ) ;
 		}
 	}
 
 	// log in Fakebook and get session + csrf cookies  
 	// side-effect: change this.cookies
 	//              and propably this.visitedURL, this.frontierURL, and this.secretFlags 
 	public void login(){
 		
 		try{
 
 			HTTPRequest request = new HTTPRequest( this.logInURL ) ;
 
 			// First request to know his cookies
 			Multimap<String,String> headers = HashMultimap.create() ;
 			headers.put( "From" , "dang.an249@gmail.com" ) ;
 			request.setHeaders( headers ) ;
 
 			this.client.setRequest( request ) ;
 
 			// GETTING THE LOGIN PAGE 
 			client.doGet() ; 
 
 
 			//System.out.println( client.getRequest().toString() ) ;
 			//System.out.println( client.getResponse().toString() ) ;
 			//System.out.println("===================") ;
 
 			this.cookies = client.getResponse().getCookies() ;
 
 			request = new HTTPRequest( this.logInURL ) ;
 
 			headers.put( "Host" , this.logInURL.getHost() ) ;
 			headers.put( "Referer" , "http://cs5700.ccs.neu.edu/accounts/login/" ) ;
 			headers.put( "Connection" , "keep-alive" ) ;
 			headers.put( "Content-Type", "application/x-www-form-urlencoded" ) ;
 			
 
 			StringBuilder body = new StringBuilder(112) ;
 
 			body.append("csrfmiddlewaretoken=") ;
 			body.append( this.cookies.get( "csrftoken" )) ;
 			body.append("&username=") ;
 			body.append( this.id ) ;
 
 			body.append("&password=") ;
 			body.append( this.password ) ;
 			body.append("&next=%2Ffakebook%2F") ;
 
 			request.setRequestBody( body.toString() ) ;
 
 			headers.put( "Content-Length", Integer.toString( request.getRequestBody().length() ) ) ;
 
 			request.setHeaders( headers ) ;						
 			request.addCookies( this.cookies ) ;
 
 			client.setRequest( request ) ;
 			//System.out.println( client.getRequest().toString() ) ;
 
 			client.doPostWithRedirect() ;
 			//System.out.println( client.getResponse().toString() ) ;
 			
 			// First site added
 			addURL(HOST);
 
 		}
 		catch( UnknownHostException ex){
 			System.out.println("Unable to connect to " + client.getRequest().getURL() + ". Unknown host" ) ;
 		} 
 		catch( SocketException ex){
 			System.out.println( "Error with underlying protocol: " + ex.toString() ) ;
 		}
 		catch( IOException ex){
 			System.out.println( ex.toString() ) ;
 		}
 
 	}
 
 	// crawl the Fakebook
 	// side-effect: modify this.visitedURL, this.frontierURL, this.secretFlags
 	public void crawl(){
 
 		// check to see if cookies is set otherwise throw error
 
 		// make the GET call
 		while (!frontierURL.isEmpty()) {
 			URL site = frontierURL.remove();
 			HTTPRequest request;
 			try {
 				request = new HTTPRequest( site ) ;
 				Multimap<String,String> headers = HashMultimap.create() ;
 				headers.put( "From" , "dang.an249@gmail.com" ) ;
 				request.setHeaders( headers ) ;
 				request.addCookies( this.cookies ) ;
 				this.client.setRequest( request ) ;
 				client.doGet() ;
 			}
 			catch( UnknownHostException ex){
 				System.out.println("Unable to connect to " + client.getRequest().getURL() + ". Unknown host" ) ;
 			} 
 			catch( SocketException ex){
 				System.out.println( "Error with underlying protocol: " + ex.toString() ) ;
 			}
 			catch( IOException ex){
 				System.out.println( ex.toString() ) ;
 			}
 		
 			HTTPClient.StatusCode stat = client.getResponse().getStatusCode();
 			// If there is no permanent error
 			if (stat != HTTPClient.StatusCode.BAD_REQUEST &&
 					stat != HTTPClient.StatusCode.FORBIDDEN) {
 			
 				// Temporal error, put the URL back in the queue
 				if (stat == HTTPClient.StatusCode.INTERNAL_SERVER_ERROR) {
 					frontierURL.add(site);
 				}
 			
 				// URL moved, add new URL to the queue
 				else if (stat == HTTPClient.StatusCode.MOVED_PERMANENTLY ||
 							stat == HTTPClient.StatusCode.MOVED_TEMPORARILY) {
 					Iterator<String> iter = client.getResponse().getHeaders().get("Location").iterator();
 					if (iter.hasNext()) {
 						String newURL = iter.next();
 						addURL(newURL);
 					}
 					else
 						throw new RuntimeException("Expect a redirect URL but found none.") ;
 				}
 			
 				// Everything OK, parse HTML, find keys and add URLs
 				else if (stat == HTTPClient.StatusCode.OK) {
 					String htmlBody = client.getResponse().getResponseBody() ;
 					parseHTML(htmlBody);
 				}
 				else {
 					System.out.println("Unknown Status Code");
 				}
 			}
 		}
 	}
 	
 	// Add URLs and get keys
 	private void parseHTML(String body) {
 		Document doc = Jsoup.parse(body);
 		Element htmlBody = doc.body();
 		Elements flags = htmlBody.getElementsByTag("h2");
 		for (int i = 0; i < flags.size(); ++i) {
 			Element flag = flags.get(i);
 			if (flag.text().substring(0,6).equals("FLAG: "))
 				System.out.println(flag.text().substring(6,70));
 		}
 		Elements urls = htmlBody.getElementsByTag("a");
 		for (int i = 0; i < urls.size(); ++i) {
 			Element url = urls.get(i);
 			int index = url.toString().indexOf( '>' ) ;
			addURL(url.toString().substring(9,index-1));
 		}
 	}
 	
 	// Returns the URL including the full path
 	// Example: "/fakebook/pedro" returns:
 	//			http://cs5700.ccs.neu.edu/fakebook/pedro
 	//
 	//			"http://cs5700.ccs.neu.edu/fakebook/pedro" returns:
 	//			http://cs5700.ccs.neu.edu/fakebook/pedro
 	private URL getFullURL(String s) {
 		URL site;
 		try {
 			// Relative URL
 			if (s.charAt(0) == '/')
 				site = new URL(rootURL, s);
 			// Full path
 			else
 				site = new URL(s);
 		} catch (MalformedURLException ex){
 			throw new RuntimeException( "Could not parse URL " + ex.toString() ) ;
 		}
 		return site;
 	}
 
 	// Checks if we can crawl this URL
 	private boolean approveURL(URL s) {
 		String shost = s.getHost();
 		String fakebook = this.rootURL.toString();
 		if (shost.equals(fakebook))
 			return true;
 		else
 			return false;
 	}
 
 	// return true if we already visit this link
 	private boolean URLVisited(URL u){
 		return visitedURL.contains(u.getPath());
 	}
 	
 	// Parses the string of a URL and
 	// adds it to the queue if not visited
 	// and approved website
 	private void addURL(String s) {
 		URL site = getFullURL(s);
 		if (approveURL(site)) {
 			if (URLVisited(site)) {
 				frontierURL.add(site);
 			}
 		}
 	}
 
 	public static void main(String args[]){
 		Crawler crawler = new Crawler( args[0], args[1]) ;
 
 		crawler.login() ;
 
 		crawler.crawl() ;
 	}
 }
