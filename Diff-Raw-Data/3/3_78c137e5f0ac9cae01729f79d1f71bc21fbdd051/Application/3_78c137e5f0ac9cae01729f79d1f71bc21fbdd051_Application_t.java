 package controllers;
 
 import interceptors.Geo;
 import interceptors.Logged;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 import model.Album;
 import model.Photo;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import others.Role;
 import play.Logger;
 import play.i18n.Messages;
 import play.api.templates.Html;
 import play.cache.Cache;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.albums;
 import views.html.albumslist;
 import views.html.main;
 import views.html.photos;
 import views.html.exif;
 import com.google.gdata.client.Query;
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.media.mediarss.MediaGroup;
 import com.google.gdata.data.photos.AlbumEntry;
 import com.google.gdata.data.photos.AlbumFeed;
 import com.google.gdata.data.photos.ExifTags;
 import com.google.gdata.data.photos.GphotoAlbumId;
 import com.google.gdata.data.photos.GphotoEntry;
 import com.google.gdata.data.photos.GphotoId;
 import com.google.gdata.data.photos.GphotoPhotosUsed;
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.data.photos.TagEntry;
 import com.google.gdata.data.photos.UserFeed;
 import com.google.gdata.data.photos.impl.ExifTag;
 import com.google.gdata.util.ParseException;
 import com.google.gdata.util.ServiceException;
 import com.google.gdata.util.ServiceForbiddenException;
 import exceptions.NoAccountsException;
 
 public class Application extends Controller {
 	
 	static final String CONFIG = "config.xml";
 	static final String THUMB_SIZE = "104c,72c,800";
 	static final String IMG_SIZE = "1600";//"d";
 	static final String API_URL = "https://picasaweb.google.com/data/entry/api/user/default";
 	static final String API_FEED_URL = "https://picasaweb.google.com/data/feed/api/user/default";
 	static public final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 	
 	static final Map<String, Object[]> local = new HashMap<String, Object[]>();
 	static public final Map<String, Object> settings = new HashMap<String, Object>();
 	static public List<PicasawebService> myServices = new ArrayList<PicasawebService>();
 	static public List<String> myServicesLogins = new ArrayList<String>();
 	static private PicasawebService myService;
 
 	/**
 	 * load configuration from config.xml
 	 * init picasa services
 	 * 
 	 * @throws NoAccountsException
 	 */
 	public static void loadServices() throws NoAccountsException {
 		
 		try {
 			Logger.info("Loading services...");
 			myServices.clear();
 			myServicesLogins.clear();
 			
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			
 			InputStream in;			
 			if(System.getProperty("config") != null && new File(System.getProperty("config")).canRead()) {
 				in =  new FileInputStream(new File(System.getProperty("config")));
 			} else {
 				if(new File(".", CONFIG).canRead()) {
 					in = new FileInputStream(new File(".", CONFIG));
 				} else {
 					in =  Application.class.getResourceAsStream("/resources/"+CONFIG);
 				}
 			}
 			Document doc = builder.parse(in);
 			in.close();
 			
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			
 			/**
 			 * picasa accounts
 			 */
 			NodeList l = (NodeList) xpath.evaluate("//picasa/account", doc, XPathConstants.NODESET);
 			for(int i=0; i < l.getLength(); i++) {
 				Node account = l.item(i);
 				String username = account.getAttributes().getNamedItem("username").getNodeValue();
 				String password = account.getAttributes().getNamedItem("password").getNodeValue();
 				
 				PicasawebService myService = new PicasawebService("testApp");
 				myService.setUserCredentials(username, password);
 				myServices.add(myService);
 				myServicesLogins.add(username);				
 			}
 			
 			/**
 			 * local accounts
 			 */
 			l = (NodeList) xpath.evaluate("//settings/local/account", doc, XPathConstants.NODESET);
 			for(int i=0; i < l.getLength(); i++) {
 				Node account = l.item(i);
 				String username = account.getAttributes().getNamedItem("login").getNodeValue();
 				String password = account.getAttributes().getNamedItem("password").getNodeValue();
 				Role role = Role.valueOf(account.getAttributes().getNamedItem("role").getNodeValue().toUpperCase());
 				local.put(username, new Object[]{password, role});
 			}
 
 			/**
 			 * settings
 			 */
 			Node n = (Node) xpath.evaluate("//settings/title", doc, XPathConstants.NODE);
 			settings.put("title", n.getTextContent());
 			
 			in.close();
 		
 			if(myServices.size() == 0) {
 				throw new NoAccountsException();
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new NoAccountsException(e);
 		}
 	}
 	
 	/**
 	 * logout
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	public static Result logout() throws IOException, ServiceException {
 		session().clear();
 		// Cache.set("albums", null);
 		return ok(albums.render(getAlbums()));
 	}
 	
 	/**
 	 * login
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	public static Result login() throws IOException, ServiceException {
 		
 		String uuid = getUUID();
 		final Map<String, String[]> values = request().body().asFormUrlEncoded();
 	    final String pass = values.get("pass")[0];
 	    final String login = values.get("login")[0];
 	    Object[] o = local.get(login);
 	    if(o != null && o[0].equals(pass)) {
 	    	session("user", login);
 	    	session("role", ((Role)o[1]).name());
 	    	Cache.set(uuid+"albums", null);
 	    	return redirect("/");
 	    }
 		flash("message", Messages.get("loginerror"));
 		return ok(albums.render(getAlbums()));
 	}
 	
 	/**
 	 * main page (root) with album covers
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 * @throws NoAccountsException
 	 */
 	public static Result albums() throws IOException, ServiceException, NoAccountsException {
 		if(request().queryString().get("lang") != null) {
 			response().setCookie("lang", request().queryString().get("lang")[0]);
 		}		
 		try {
 			return ok(albums.render(getAlbums()));
 		} catch (ServiceForbiddenException e) {
 			Logger.error(e.getMessage(), e);
 			loadServices();
 			return redirect("/");
 		} 
 	}
 	
 	/**
 	 * album list
 	 * @return
 	 * @throws ServiceException 
 	 * @throws IOException 
 	 */
 	@Geo
 	public static List<Album> getAlbums() throws IOException, ServiceException {
 				
 		String uuid = getUUID();
 		List<Album> cached = (List<Album>) Cache.get(uuid+"albums");
 		if(cached != null) {
 			Logger.debug("CACHED");
 			return cached; 
 		} else {
 			Logger.debug("NOT CACHED");
 			Logger.info("Getting albums list ("+new SimpleDateFormat("dd.MM.yyyy hh:ss:mm").format(new Date(System.currentTimeMillis()))+" | IP: "+request().remoteAddress()+")...");
 			URL feedUrl = new URL(API_FEED_URL+"?kind=album&thumbsize="+THUMB_SIZE+"&fields=entry(title,id,gphoto:id,gphoto:numphotos,media:group/media:thumbnail,media:group/media:keywords)");
 			Query albumQuery = new Query(feedUrl);
 			
 			List<Album> l = new ArrayList<Album>();		
 			int i = 0;
 			for(PicasawebService s: myServices) {
 				Logger.debug(feedUrl.toString());			
 				UserFeed feed = s.query(albumQuery, UserFeed.class);
 				for (GphotoEntry e : feed.getEntries()) {
 					// Utils.describe(e);
 					if(e.getGphotoId() != null) {
 						
 						if(session("user") != null || e.getTitle().getPlainText().endsWith("\u00A0")) {
 							String t = e.getTitle().getPlainText();
 							if(t.length() > 40) {
 								t = t.substring(0, 39)+"...";
 							}
 							l.add(new Album(e.getGphotoId(), t, e.getExtension(MediaGroup.class).getThumbnails().get(0).getUrl(), e.getExtension(GphotoPhotosUsed.class).getValue(), i, e.getTitle().getPlainText().endsWith("\u00A0"), myServicesLogins.get(i)));
 						}
 					} else {
 						// tag... (?kind=album,tag)
 						Logger.debug("album TAG: "+e.getTitle().getPlainText());
 					}
 				}
 				i++;
 			}
 			Collections.sort(l, new Comparator<Album>() {
 				@Override
 				public int compare(Album o1, Album o2) {
 					return o2.getTitle().compareTo(o1.getTitle());
 				}});
 			Cache.set(uuid+"albums", l, 3600);
 			return l;
 		}
 	}
 
 	/**
 	 * full page with opened album (by url)
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param start
 	 * @param max
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	@Geo
 	public static Result direct(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
 		return ok(main.render(albumId+"", albumslist.render(getAlbums(), albumId), photosHtml(serviceIndex, albumId, start, max)));
 	}
 	
 	/**
 	 * photos in album as Result
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param start
 	 * @param max
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	@Geo
 	public static Result photos(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
 		return ok(photosHtml(serviceIndex, albumId, start, max));
 	}
 	
 	/**
 	 * photos in album as HTML
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param start
 	 * @param max
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	private static Html photosHtml(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
 		Logger.info("Getting photos list ("+new SimpleDateFormat("dd.MM.yyyy hh:ss").format(new Date(System.currentTimeMillis()))+" | IP: "+request().remoteAddress()+")...");
 		myService = myServices.get(serviceIndex);
 		session("si", serviceIndex+"");
 		session("ai", albumId+"");
 		URL feedUrl = new URL(API_FEED_URL+"/albumid/"+albumId+"?kind=photo"+"&thumbsize="+THUMB_SIZE+"&imgmax="+IMG_SIZE+
 				(session("user") != null ?
 						"&fields=id,title,entry(title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords),openSearch:totalResults,openSearch:startIndex,openSearch:itemsPerPage"	:
 						"&fields=title,openSearch:totalResults,openSearch:startIndex,openSearch:itemsPerPage,entry[media:group/media:keywords='public'%20or%20media:group/media:keywords='public,%20picnik'%20or%20media:group/media:keywords='picnik,%20public'](title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords)")+
 				(session("user") != null ? 
 						"&max-results="+max+"&start-index="+start : 
 						"")
 				//+(session("user") != null ? "" : "&tag=public") /* to rozsortowuje kolejnosc fotek! */
 				//+,exif:tags)"*/
 				);
 		Logger.debug(feedUrl.toString());
 		Query photosQuery = new Query(feedUrl);
 		
 		// AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);		
 		AlbumFeed feed = myService.query(photosQuery, AlbumFeed.class);
 		if(feed.getTitle().getPlainText().endsWith("\u00A0")) {
 			session("pub", "1");
 		} else {
 			session().remove("pub");
 		}
 		
 		String t = feed.getTitle().getPlainText();
 		session("aname", t);
 		Logger.debug("total:"+feed.getTotalResults());
 		Logger.debug("perPage:"+feed.getItemsPerPage());
 		Logger.debug("start:"+feed.getStartIndex());
 		java.util.HashMap<String, Integer> map = new java.util.HashMap<String, Integer>();
 		map.put("total",feed.getTotalResults());
 		map.put("start",feed.getStartIndex());
 		map.put("per",feed.getItemsPerPage());
 		
 		List<Integer> pages = new ArrayList<Integer>();
 		for(int i = 1; i <= feed.getTotalResults()/feed.getItemsPerPage() + (feed.getTotalResults()%feed.getItemsPerPage() == 0 ? 0 : 1); i++) {
 			pages.add(i);
 		}
 		
 		List<Photo> lp = new ArrayList<Photo>();
 		for(GphotoEntry<PhotoEntry> e: feed.getEntries()) {
 			MediaGroup g = e.getExtension(MediaGroup.class);
 			ExifTags exif = e.getExtension(ExifTags.class);
 			
 			if(g != null) {
 				boolean pub = g.getKeywords().getKeywords().contains("public");
 				if(session("user") != null || pub) {
 					lp.add(new Photo(e.getTitle().getPlainText(), 
 							e.getExtension(GphotoId.class).getValue(), 
 						Arrays.asList(new String[]{g.getThumbnails().get(0).getUrl(), 
 								g.getThumbnails().get(1).getUrl(), 
 								g.getThumbnails().get(2).getUrl()}), 
 						g.getContents().get(0).getUrl(), 
 						e.getExtension(GphotoAlbumId.class).getValue(), 
 						g.getKeywords().getKeywords().toArray(new String[]{}), pub, exif));
 				}
 			}
 		}
 		
 		return photos.render(feed, lp, null, map, pages);
 	}
 	
 	/**
 	 * make photo public
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param photoId
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	@Logged(Role.ADMIN)
 	public static Result pub(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException {
 		URL feedUrl = new URL(API_FEED_URL+"/albumid/"+albumId+"/photoid/"+photoId);
 		Logger.debug(feedUrl+"");
 		TagEntry myTag = new TagEntry(); 
 		myTag.setTitle(new PlainTextConstruct("public"));
 		myServices.get(serviceIndex).insert(feedUrl, myTag);
 		return ok("1");
 	}
 
 	/**
 	 * make photo private
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param photoId
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	@Logged(Role.ADMIN)
 	public static Result priv(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException {
 		URL entryUrl = new URL(API_URL+"/albumid/"+albumId+"/photoid/"+photoId+"/tag/public");
 		TagEntry te = myServices.get(serviceIndex).getEntry(entryUrl, TagEntry.class);
 		te.delete();
 		return ok("0");
 	}
 
 	/**
 	 * make album public
 	 * @param serviceIndex
 	 * @param albumId
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	@Logged(Role.ADMIN)
 	public static Result pubAlbum(int serviceIndex, String albumId) throws IOException, ServiceException {
 		URL feedUrl = new URL(API_URL+"/albumid/"+albumId);
 		Logger.debug(feedUrl+"");
 		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
 		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText()+"\u00A0"));
 		ae.update();
 		return ok("1");
 	}
 	
 	/**
 	 * make album private
 	 * @param serviceIndex
 	 * @param albumId
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	@Logged(Role.ADMIN)
 	public static Result privAlbum(int serviceIndex, String albumId) throws IOException, ServiceException {
 		URL feedUrl = new URL(API_URL+"/albumid/"+albumId);
 		Logger.debug(feedUrl+"");
 		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
 		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText().replaceAll("\u00A0", "").replaceAll("\\+", "")));
 		ae.update();
 		return ok("0");
 	}
 
 	/**
 	 * get exif tags
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param photoId
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	public static Result exif(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException {
 		URL feedUrl = new URL(API_URL+"/albumid/"+albumId+"/photoid/"+photoId+"?fields=exif:tags,title");
 		PhotoEntry pe = myServices.get(serviceIndex).getEntry(feedUrl, PhotoEntry.class);
 		return ok(exif.render(pe));
 	}
 
 	public static String getUUID() {
 		String uuid = session("uuid");
 		if(uuid==null) {
 			uuid=java.util.UUID.randomUUID().toString();
 			session("uuid", uuid);
 		}
 		return session("uuid");
 	}
 	
 }
