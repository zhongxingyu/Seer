 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.HashSet;
 
 public class WebPage {
 	private URI myURI;
 	private String rawURI;
 	private double linkRank;
 	private HashSet<WebPage> children;
 
 	public WebPage (String address) {
 		rawURI = address;
 		try {
 			myURI = new URI(address);
 		} catch (URISyntaxException e) {
			System.out.println("Error on: " + address);
 			try {
 				myURI = new URI(URLEncoder.encode(address, "UTF-8"));
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (URISyntaxException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
			
 		}
 		linkRank = 0;
 		children = new HashSet<WebPage>();
 	}
 	
 	public String getURL() {			return myURI.toString(); }
 	public WebPage[] getChildren() {	return (WebPage[])children.toArray(); }
 	public double getRank() {			return linkRank; }
 	
 	public int hashCode() {
 		return myURI.hashCode();
 	}
 	public boolean equals(Object other) {
		if (other instanceof WebPage && myURI.equals(((WebPage) other).getURL())) {
 			return true;
 		} else
 			return false;
 	}
 	public String toString() {
		return rawURI;
		//return myURI.toString();
 	}
 	
 }
