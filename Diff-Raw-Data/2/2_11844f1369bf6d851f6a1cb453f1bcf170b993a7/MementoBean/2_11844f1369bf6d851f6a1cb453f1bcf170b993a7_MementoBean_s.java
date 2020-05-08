 package models.memento;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import com.google.common.net.InternetDomainName;
 
 import dev.memento.Memento;
 import dev.memento.SimpleDateTime;
 
 public class MementoBean {
 	
 	/** Hash map to map Memento URLs to Wayback URLs */
 	private static HashMap<String,String> toWayback = new HashMap<String,String>();
 	static {
 		//toWayback.put("http://webarchive.nationalarchives.gov.uk/", 
 		//			  "http://webarchive.nationalarchives.gov.uk/");
 		toWayback.put("http://api.wayback.archive.org/memento/", 
 				      "http://web.archive.org/web/");
 		toWayback.put("http://www.webarchive.org.uk/wayback/memento/", 
 			   	  "http://www.webarchive.org.uk/wayback/archive/");
 		toWayback.put("http://www.webarchive.org.uk/waybacktg/memento/", 
 			   	  "http://www.webarchive.org.uk/wayback/archive/");
 		//toWayback.put("http://wayback.archive-it.org/866/",
 		//			  "http://wayback.archive-it.org/866/");
 		//toWayback.put("http://webarchive.loc.gov/lcwa0001/", 
 		//		  "http://webarchive.loc.gov/lcwa0001/");
 		//toWayback.put("http://webcitation.org/", 
 		//		  "");
 		//toWayback.put("", 
 		//		  "");
 	}
 
 	private Memento m;
 
 	public MementoBean(Memento m) {
 		this.m = m;
 	}
 
 	public String getArchiveHost() {
 		try {
 			URL uri = new URL( m.getUrl() );
			return getTopPrivateDomain(uri.getAuthority());
 		} catch (Exception e) {
 			System.err.println("Exception "+e+" when attempting to parse uri: "+m.getUrl());
 			return null;
 		}		
 	}
 	
 	private static String getTopPrivateDomain(String host) {
 	    InternetDomainName domainName = InternetDomainName.from(host);
 	    return domainName.topPrivateDomain().name();
 	  }
 
 	
 	public String getUrl() {
 		return m.getUrl();
 	}
 	
 	public String getWaybackUrl() {
 		String url = m.getUrl();
 		for( String mempre : toWayback.keySet() ) {
 			if( url.startsWith(mempre)) {
 				url = url.replaceFirst( Pattern.quote(mempre), toWayback.get(mempre));
 				break;
 			}
 		}
 		return url;
 	}
 	
 	public SimpleDateTime getDateTime() {
 		if( m == null ) return null;
 		return m.getDateTime();
 	}
 	
 	public String getRel() {
 		return m.getRel();
 	}
 }
