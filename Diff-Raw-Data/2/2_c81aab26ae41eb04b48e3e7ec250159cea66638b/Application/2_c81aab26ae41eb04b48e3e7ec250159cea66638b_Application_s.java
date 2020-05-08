 package controllers;
 
 import interceptors.Logged;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
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
 import play.api.templates.Html;
 import play.mvc.Controller;
 import play.mvc.Result;
 import scala.actors.threadpool.Arrays;
 import views.html.albums;
 import views.html.albumslist;
 import views.html.main;
 import views.html.photos;
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
 import com.google.gdata.util.ParseException;
 import com.google.gdata.util.ServiceException;
 import com.google.gdata.util.ServiceForbiddenException;
 import exceptions.NoAccountsException;
 
 public class Application extends Controller {
 	
 	static final String CONFIG = "config.xml";
 	static final String THUMB_SIZE = "104c,72c,800";
 	static final String IMG_SIZE = "1600";//"d";
 	
 	static final Map<String, Object[]> local = new HashMap<String, Object[]>();
 	static public final Map<String, Object> settings = new HashMap<String, Object>();
 	static public List<PicasawebService> myServices = new ArrayList<PicasawebService>();
 	static public List<String> myServicesLogins = new ArrayList<String>();
 	static private PicasawebService myService;
 
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
 	
 	public static Result logout() throws IOException, ServiceException {
 		session().clear();
 		return ok(albums.render(getAlbums()));
 	}
 	
 	public static Result login() throws IOException, ServiceException {
 		
 		final Map<String, String[]> values = request().body().asFormUrlEncoded();
 	    final String pass = values.get("pass")[0];
 	    final String login = values.get("login")[0];
 	    Object[] o = local.get(login);
 	    if(o != null && o[0].equals(pass)) {
 	    	session("user", login);
 	    	session("role", ((Role)o[1]).name());
 	    	return redirect("/");
 	    }
 		flash("message", "login error");
 		return ok(albums.render(getAlbums()));
 	}
 	
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
 	public static List<Album> getAlbums() throws IOException, ServiceException {
 		Logger.info("Getting albums list...");
 		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album&thumbsize="+THUMB_SIZE+"&fields=entry(title,id,gphoto:id,gphoto:numphotos,media:group/media:thumbnail,media:group/media:keywords)");
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
 		return l;
 	}
 
 	public static Result direct(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
		return ok(main.render(albumId+"", albumslist.render(getAlbums()), photosHtml(serviceIndex, albumId, start, max)));
 		// return ok("DIRECT");
 	}
 	
 	/**
 	 * photos in album list
 	 * @param serviceIndex
 	 * @param albumId
 	 * @param start
 	 * @param max
 	 * @return
 	 * @throws IOException
 	 * @throws ServiceException
 	 */
 	public static Result photos(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
 		return ok(photosHtml(serviceIndex, albumId, start, max));
 	}
 	
 	private static Html photosHtml(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
 		Logger.info("Getting photos list...");
 		myService = myServices.get(serviceIndex);
 		session("si", serviceIndex+"");
 		session("ai", albumId+"");
 		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"?kind=photo"+"&thumbsize="+THUMB_SIZE+"&imgmax="+IMG_SIZE+
 				(session("user") != null ?
 				"&fields=id,title,entry(title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords),openSearch:totalResults,openSearch:startIndex,openSearch:itemsPerPage"
 				:
 				/* tylko entry z media:keywords='public'*/
 				"&fields=title,openSearch:totalResults,openSearch:startIndex,openSearch:itemsPerPage,entry[media:group/media:keywords='public'%20or%20media:group/media:keywords='public,%20picnik'%20or%20media:group/media:keywords='picnik,%20public'](title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords)"
 				)+
 				(session("user") != null ? "&max-results="+max+"&start-index="+start : "")
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
 		for(int i = 1; i <= feed.getTotalResults()/feed.getItemsPerPage() + 1; i++) {
 			pages.add(i);
 		}
 		
 		// describe(feed.getEntries().get(0));
 		List<Photo> lp = new ArrayList<Photo>();
 		for(GphotoEntry<PhotoEntry> e: feed.getEntries()) {
 			// Utils.describe(e);
 			// Logger.debug("EXTENSIONS:" + e.getExtensions()+"");
 			MediaGroup g = e.getExtension(MediaGroup.class);
 			ExifTags exif = e.getExtension(ExifTags.class);
 			//Utils.describe(exif);
 			
 			if(g != null) {
 				/*
 				Logger.debug(g.getContents().size()+"");
 				Logger.debug(g.getThumbnails().size()+"");
 				Logger.debug("thumbs:"+g.getThumbnails().get(0).getUrl());
 				Logger.debug("thumbs:"+g.getThumbnails().get(1).getUrl());
 				Logger.debug("thumbs:"+g.getThumbnails().get(2).getUrl());
 				Logger.debug("orig:"+g.getContents().get(0).getUrl());
 				*/
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
 		// Logger.debug("TITLE:"+feed.getTitle()+"");
 		// return ok(photos.render(feed, (List<GphotoEntry<PhotoEntry>>)feed.getEntries<PhotoEntry>(), l));
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
 		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"/photoid/"+photoId);
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
 		URL entryUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId+"/photoid/"+photoId+"/tag/public");
 		TagEntry te = myServices.get(serviceIndex).getEntry(entryUrl, TagEntry.class);
 		te.delete();
 		return ok("0");
 		
 		/*
 		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"/photoid/"+photoId+"?kind=tag&tag=public");
 		Logger.debug(feedUrl+"");
 		Query photosQuery = new Query(feedUrl);
 		AlbumFeed searchResultsFeed = myServices.get(serviceIndex).query(photosQuery, AlbumFeed.class);
 		for (TagEntry tag : searchResultsFeed.getTagEntries()) {
 			if(tag.getTitle().getPlainText().equals("public")) {
 				tag.delete();
 				break;
 			}
 		}
 		return ok("0");
 		*/		
 				
 		/*
 		TagEntry myTag = myServices.get(serviceIndex).getEntry(new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"/photoid/"+photoId+"/tag/public"), TagEntry.class);
 		myTag.delete();
 		// myServices.get(serviceIndex).insert(feedUrl, myTag);
 		return ok("0");
 		*/
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
 		URL feedUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId);
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
 		URL feedUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId);
 		Logger.debug(feedUrl+"");
 		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
 		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText().replaceAll("\u00A0", "").replaceAll("\\+", "")));
 		ae.update();
 		return ok("0");
 	}
 	
 	public static Result exif(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException {
 		URL feedUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId+"/photoid/"+photoId+
 				"?fields=exif:tags,title");
 		// Logger.debug(feedUrl+"");
 		PhotoEntry pe = myServices.get(serviceIndex).getEntry(feedUrl, PhotoEntry.class);
 		return ok(exifTagsHtml(pe));
 	}
 
 	private static Html exifTagsHtml(PhotoEntry pe) throws ParseException {
 		if(pe.hasExifTags() && pe.getExifTags() != null) {
 			ExifTags e = pe.getExifTags();
 
 			// Logger.debug(e+"");
 			
 			/*
 			Utils.describe(e);
 			for(ExifTag tag: e.getExifTags()) {
 				Logger.info(tag.getName() + ":" + tag.getValue());
 			}
 			for(List<Extension> l: e.getRepeatingExtensions()) {
 				for(Extension ex: l) {
 					if(ex instanceof ExifTag) {
 						ExifTag t = (ExifTag) ex;
 						Logger.info(t.getName() + ":" + t.getValue());
 					}
 				}
 			}
 			*/
 
 			String a = null;
 			String exif = 
 				"<pre>" +
 				(e.getTime() != null ? "Create Date                     :"+ (e.getTime() != null ? new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(e.getTime()) : "") + "\n" : "") +
 				(pe != null && pe.getTitle() != null ? "File Name                       :" + pe.getTitle().getPlainText() + "\n" : "") +
 				(a != null ? "File Size                       :" + a + "\n" : "" ) +
 				(e.getCameraModel() != null ? "Camera Model Name               :" + e.getCameraModel() + "\n" : "" ) +
 				(e.getApetureFNumber() != null ? "F Number                        :" + e.getApetureFNumber() + "\n" : "" ) +
 				(e.getFocalLength() != null ? "Focal Length                    :" + e.getFocalLength() + "\n" : "" ) +
 				(a != null ? "Focal Length In 35mm Format     :" + a + "\n" : "" ) +
 				(e.getExposureTime() != null ? "Exposure Time                   :" + e.getExposureTime() + "\n" : "" ) +
 				(e.getIsoEquivalent() != null ? "ISO                             :" + e.getIsoEquivalent() + "\n" : "" ) +
 				(a != null ? "Exposure Program                :" + a + "\n" : "" ) +
 				(a != null ? "Exposure Mode                   :" + a + "\n" : "" ) +
 				(a != null ? "Metering Mode                   :" + a + "\n" : "" ) +
 				(a != null ? "White Balance                   :" + a + "\n" : "" ) +
 				(e.getFlashUsed() != null ? "Flash                           :" + e.getFlashUsed() + "\n" : "" ) +
 				(a != null ? "Light Source                    :" + a + "\n" : "" ) +
 				(a != null ? "Exposure Compensation           :" + a + "\n" : "" ) +
 				(a != null ? "Image Width                     :" + a + "\n" : "" ) +
 				(a != null ? "Image Height                    :" + a : "") +
 				"</pre>";
 			return new Html(exif);
 		} else {
 			return new Html("<pre>No EXIF tags.</pre>");
 		}
 	}
 }
