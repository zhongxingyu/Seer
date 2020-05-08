 package dev.bitcoin.bitcointalk;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.Source;
 
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.StringBody;
 
 import com.google.appengine.api.urlfetch.HTTPHeader;
 import com.google.appengine.api.urlfetch.HTTPMethod;
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 import dev.bitcoin.bitcointalk.model.Post;
 
 public class BitcoinTalkPostMan {
 
 	private static final String PM_SEND = "https://bitcointalk.org/index.php?action=pm;sa=send2";
 	private static final String PM_READ = "https://bitcointalk.org/index.php?action=pm;sa=send";
 	private static final String PM_LIST = "https://bitcointalk.org/index.php?action=pm;f=inbox";
 	private static final String LOGIN = "https://bitcointalk.org/index.php?action=login2";
 	
 	URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
 	
 	public void sendPrivateMessage(String receipient, String subject, String message, String username, String password) throws IOException {
 		HTTPHeader cookie = getLoginCookie();
 		
 		try {
 			HTTPRequest req1 = new HTTPRequest(new URL(PM_READ), HTTPMethod.GET);
 			
 			if(cookie != null)
 				req1.addHeader(cookie);
 	        List<HTTPHeader> headers = req1.getHeaders();
 	        for (HTTPHeader httpHeader : headers) {
 	        	System.out.println(httpHeader.getName() + "," + httpHeader.getValue());
 	        }
 	       
 			HTTPResponse fetch = urlFetchService.fetch(req1);
 			byte[] content = fetch.getContent();
 			String xsrfToken = getXsrfToken(new ByteArrayInputStream(content));
 			if(xsrfToken == null) {
 				System.out.println("Not logged in");
 			} else {
             
 	            MultipartEntity e2 = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 	            e2.addPart("to", new StringBody(receipient));
 	            e2.addPart("subject", new StringBody(subject));
 	            e2.addPart("bcc", new StringBody(""));
 	            e2.addPart("message", new StringBody(message));
 	            e2.addPart("sc", new StringBody(xsrfToken));
 	
 	          
 	            HTTPRequest req = new HTTPRequest(new URL(PM_SEND), HTTPMethod.POST);
 	            ServletHelper.addMultipartBodyToRequest(e2, req);
 	
 	            HTTPResponse fetch2 = urlFetchService.fetch(req);
 		        int responseCode2 = fetch2.getResponseCode();
 		        System.out.println("PM request succeeded (maybe), response code " + responseCode2);
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		
 	}
 	private HTTPHeader getLoginCookie() throws MalformedURLException,
 			UnsupportedEncodingException, IOException {
 		URL url = new URL(LOGIN);
 		HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.POST);
 		MultipartEntity e = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         e.addPart("user", new StringBody("Logik"));
         e.addPart("passwrd", new StringBody("r2mhs8wb"));
         e.addPart("cookielength", new StringBody("-1"));
         ServletHelper.addMultipartBodyToRequest(e, httpRequest);
       
 		httpRequest.getFetchOptions().doNotFollowRedirects();
 		HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);
 
 		HTTPHeader cookie = null;
 		for (HTTPHeader header : httpResponse.getHeaders() ) {
 			System.out.println("<p>Cookie returned:<br/>" + header.getName() + "<br/>" + header.getValue() + "</p>");
 			if (header.getName().equalsIgnoreCase("set-cookie")){
 				cookie = header;
 			}
 		}
 		if(cookie == null) {
 			System.err.println("No login cookie retreived");
 		}
 		return cookie;
 	}
 	
 	private String getXsrfToken(InputStream inputStream) throws IOException {
 		Source source = new Source(inputStream);
 		source.fullSequentialParse();
 		Element elementById = source.getElementById("postmodify");
 		if(elementById == null)
 			return null;
 		List<Element> inputs = elementById.getAllElements("input");
 		for (Element element : inputs) {
 			String name = element.getAttributeValue("name");
 			if(name != null && name.equals("sc")) {
 				return element.getAttributeValue("value");
 			}
 		}
 		return null;
 	}
 	
	public List<Post> getPM(String username, String password) throws IllegalStateException, IOException {
 		
 		List<Post> returnList = new ArrayList<Post>();
 		HTTPHeader cookie = getLoginCookie();
 		HTTPRequest request = new HTTPRequest(new URL(PM_LIST), HTTPMethod.GET);
 		if(cookie != null)
 			request.addHeader(cookie);
 		
 		byte[] response = urlFetchService.fetch(request).getContent();
 		Source source = new Source(new ByteArrayInputStream(response));
 		source.fullSequentialParse();
 		
 		List<Element> inputs = source.getElementById("bodyarea").getFirstElement("form").getAllElements("table").get(3).getAllElements("tr");
 		for (Element element : inputs.subList(1, inputs.size())) 
 			parsePost(returnList, element);
 		
 		return returnList;
 	}
 	
 	private void parsePost(List<Post> returnList, Element element) {
 		List<Element> allElements = element.getAllElements("a");
 		if(allElements.isEmpty() == false) {
 			String poster = allElements.get(0).getContent().toString();
			List<Element> allElementsByClass = element.getAllElementsByClass("personalmessage");
 			if(allElementsByClass.isEmpty() == false) {
 				String content = allElementsByClass.get(0).getContent().toString();
 				returnList.add(new Post(poster, content));
 			}
 		}
 	}
 		
 }
