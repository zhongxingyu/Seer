 package org.alt60m.gcx;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.alt60m.cas.CASHelper;
 import org.alt60m.cas.CASUser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gcx.cas.CASProxyURLConnection;
 
 import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;
 
 public class ConnexionBar {
 	private static Log log = LogFactory.getLog(ConnexionBar.class);
 
 	private static Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
 
 	private static CASHelper helper;
 
 	//Yeah, I don't really like this...but it's kind hard to get the
 	//logoutCallback url otherwise without some refactoring I'm not willing
 	//to commit to
 	public static void setCasHelper(CASHelper helper) {
 		ConnexionBar.helper = helper;
 	}
 
 	private CASUser user;
 
 	private String logoutUrl;
 
 
 	/**
 	 * See also ProxyTicketReceptor.java
 	 * @author Matthew.Drees
 	 *
 	 */
 	private class CacheEntry {
 		public CacheEntry(String bar) {
 			this.bar = bar;
 			cacheTimestamp = new Date();
 		}
 
 		static final int validTime =  1 * 60 * 60 * 1000; //in ms
 
 		public String bar;
 
 		public Date cacheTimestamp;
 
 		public boolean expired() {
 			return (new Date().getTime() - cacheTimestamp.getTime() > validTime);
 		}
 	}
 
 	//the cache might grow kinda large, since entries are never removed, but I don't
 	//think it will pose a problem.  It's not persisted across context restarts.
 	public static void clearCache() {
 		cache.clear();
 	}
 
 	public ConnexionBar(CASUser user, HttpServletRequest request) {
 		if (user == null) {
 			throw new IllegalArgumentException("Must be initialized with a non-null user");
 		}
 		this.user = user;
 		logoutUrl = helper.getLogoutUrl(request);
 	}
 
 	public String render()
 	{
 		CacheEntry cacheEntry = cache.get(user.getGUID());
 		if (cacheEntry != null && !cacheEntry.expired()) {
 			return cacheEntry.bar;
 		}
 		String pgtIou = user.getPgtIou();
 		if (pgtIou != null) {
 			String bar = getBar(pgtIou, user.getGUID());
 			if (bar != null) {
 				bar = replaceLogoutLink(bar);
 				cache.put(user.getGUID(), new CacheEntry(bar));
 				return bar;
 			} else {
 				log.warn("Unable to fetch bar");
 			}
 		} else {
 			log.warn("user " + user.getUsername() + " has no pgtIou");
 		}
 		if (cacheEntry != null) { //better an expired bar than none at all
 			return cacheEntry.bar;
 		}
 		return null;
 	}
 
 	private String replaceLogoutLink(String bar) {
 		String wrongLogoutUrl = "&quot;https://signin.mygcx.org/cas/logout&quot";
 		return bar.replace(wrongLogoutUrl, "&quot;" + logoutUrl + "&quot;");
 	}
 	
 	public static String getBar(String pgtiou) {
 		return getBar(pgtiou, null);
 	}
 
 	public static String getBar(String pgtiou, String guid) {
 		if (pgtiou == null) {
 			throw new IllegalArgumentException("Cannot accept null pgtiou!");
 		}
 
 		String content = null;
 		//TODO: at some point, the GCX guys need to fix their system so we can request a ticket for the same URL we use to get the bar itself
 		String barTicketService = "http://www.mygcx.org/module/CampusStaff/omnibar/omnibar";
 		String barService = "http://gcx3.mygcx.org/module/CampusStaff/omnibar/omnibar"; //should be www, not working
 //		String barTicketService = "http://www.mygcx.org/module/global/omnibar/omnibarExternal";
 //		String barService = "http://gcx3.mygcx.org/module/global/omnibar/omnibarExternal";
 		// "http://gcx1.mygcx.org/module/global/omnibar/omnibarExternal";
 		String signinService = "signin.mygcx.org";
 		try {
 			content = getBar(pgtiou, barTicketService, barService, signinService);
 		} catch (IOException e) {
 			//TODO: if it's the first time a user logs in, GCX doesn't like to
 			//send the toolbar.  So rerequest the toolbar.  Remove this when GCX
 			//team fixes their server.
 			//
 			//update: this shouldnt' be necessary, since the first time a user
 			//logs in, we add them to gcx (in simplesecuritymanager).  But it
 			//doesn't hurt to leave it for now.
 			if (e.getMessage().indexOf(
 					"Server returned HTTP response code: 401") != -1) {
 				log.warn("First attempt to get bar failed due to 401; Retrying...");
 				try { //First try to add them to our community, since that might cause a failure...
 					if (guid != null) {
 						CommunityAdminInterface cai = new CommunityAdminInterface("CampusStaff");
 						if (!cai.addToGroup(guid, "Members")) {
 							log.error("User not added to CampusStaff: " + cai.getError());
 						}
 					} else {
 						log.warn("GUID is null when trying to add user to CampusStaff after getBar failure.", e);
 					}
 				} catch (IOException e2) {
 					log.error("Exception occured during attempt to add user to CampusStaff after getBar failure", e2);
 				} catch (CommunityAdminInterfaceException e3) {
 					log.error("Exception occured during attempt to add user to CampusStaff after getBar failure", e3);
 				}
 				try { //Try to get the bar again
 					content = getBar(pgtiou, barTicketService, barService, signinService);
 				} catch (IOException e1) {
 					log.error("Exception occured during second attempt to get ConneXionBar", e1);
 				}
 			} else {
 				log.warn("Exception Occured getting bar: ", e);
 			}
 		}
 
 		return content;
 	}
 
 	private static String getBar(String pgtiou, String barTicketService, String barService, String signinService) throws IOException {
 		log.debug("Getting connexion bar for pgtiou " + pgtiou);
 		String content = null;
 		String proxyticket = ProxyTicketReceptor.getProxyTicket(pgtiou,
 				barTicketService);
 		log.info("proxyticket: " + proxyticket);
 		log.info("barTicketService: " + barTicketService);
 		if (proxyticket == null)
 			log.warn("No ticket given from receptor!");
 		else {
 			CASProxyURLConnection proxyCon = new CASProxyURLConnection(
 					signinService);
 
 			String received = proxyCon.getURL(barService, proxyticket);
 			if (proxyCon.wasSuccess() && received != null) {
 				content = parseBar(received);
 //				content = received;
 			} else {
 				//TODO: retry with real service url?
 				log.error("Connection failed: " + proxyCon.getError());
 			}
 		}
 		return content;
 	}
 	
 	//Remove XML tags from beginning and end of bar, unescape characters
 	private static String parseBar(String bar) {
 		log.info(bar);
 		String resultPlusEnd = (bar.split("<reportdata>",2))[1];
 		String result = (resultPlusEnd.split("</reportdata>",2))[0];
 //		String result = bar;
 		result = result.replace("&lt;", "<");
 		result = result.replace("&gt;", ">");
 		result = result.replace("&apos;", "'");
 		result = result.replace("&quot;", "\"");
 		//We should be logging on with www but it isn't working for some reason,
 		//so the bar comes back with http/gcx3 links, when it should be https/www
 		result = result.replace("gcx3", "www");
 		result = result.replace("http", "https");
 		return result;
 	}
 }
