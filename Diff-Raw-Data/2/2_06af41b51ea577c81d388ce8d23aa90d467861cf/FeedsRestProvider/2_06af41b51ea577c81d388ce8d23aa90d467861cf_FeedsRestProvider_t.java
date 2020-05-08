 package com.socialcomputing.feeds;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.hibernate.Session;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.Text;
 
 import au.id.jericho.lib.html.Segment;
 import au.id.jericho.lib.html.Source;
 import au.id.jericho.lib.html.StartTag;
 
 import com.socialcomputing.feeds.utils.HibernateUtil;
 import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
 import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
 
 @Path("/")
 public class FeedsRestProvider {
 
     @GET
     @Path("maps/map.json")
     @Produces(MediaType.APPLICATION_JSON)
     public String kind(@Context HttpServletRequest request,  @DefaultValue("") @QueryParam("url") String urls, @DefaultValue("") @QueryParam("track") String track, @DefaultValue("") @QueryParam("site") String site) {
         HttpSession session = request.getSession(true);
         String key = urls;
         String result = ( String)session.getAttribute( key);
         if (result == null || result.length() == 0) {
            result = feeds( track, site, urls);
            session.setAttribute( key, result);
         }
         return result;
     }
 
     public String feeds( String track, String site, String urlLst) {
         StoreHelper storeHelper = new StoreHelper();
         List<String> titles = new ArrayList<String>();
         List<String> urls = new ArrayList<String>();
         List<Integer> counts = new ArrayList<Integer>();
         try {
             for( String url : urlLst.split( ",")) {
                 url = url.trim();
                 if( !url.startsWith( "http://") && !url.startsWith( "http://")) {
                     url = "http://" + url;
                 }
                 UrlHelper feed = new UrlHelper( url);
                 feed.openConnections();
                 read( track, feed, storeHelper, titles, urls, counts);
                 feed.closeConnections();
             }
             if( track.equalsIgnoreCase( "site")) {
                 trackSite( site, urlLst);
             }
         }
         catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
         }
         storeHelper.addGlobal( "FEEDS_TITLES",  titles.toArray());
         storeHelper.addGlobal( "FEEDS_URLS",    urls.toArray());
         storeHelper.addGlobal( "FEEDS_COUNTS",  counts.toArray());
         return storeHelper.toJson();
     }
 
     private void read(String track, UrlHelper feed, StoreHelper storeHelper, List<String> titles, List<String> urls, List<Integer> counts) throws WPSConnectorException {
         String content = feed.getContentType();
         if( content.contains( "text/html")) {
             // HTML ?
             Source source;
             try {
                 source = new Source( feed.getStream());
             } catch (Exception e) {
                 throw new WPSConnectorException( "openConnections", e);
             }
             List<StartTag> tags = ((Segment)source.findAllElements( "head").get( 0)).findAllStartTags( "link");
             for( StartTag tag : tags) {
                 String type = tag.getAttributeValue( "type");
                 if( type != null && type.equalsIgnoreCase( "application/rss+xml")) {
                     UrlHelper curFeed = new UrlHelper();
                     String url = tag.getAttributeValue( "href");
                     curFeed.setUrl( url.startsWith( "/") ? feed.getUrl() + url : url);
                     curFeed.openConnections( );
                     readXml( track, curFeed, storeHelper, titles, urls, counts);
                     curFeed.closeConnections();
                 }
             }
         }
         else {
             readXml( track, feed, storeHelper, titles, urls, counts);
         }
     }
     
     private void readXml(String track, UrlHelper feed, StoreHelper storeHelper, List<String> titles, List<String> urls, List<Integer> counts) throws WPSConnectorException {
         try {
             org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(false);
             org.jdom.Document doc = builder.build( feed.getStream());
             Element root = doc.getRootElement();
             
             Element top = root.getChild( "channel");
             if( top != null) {
                 parseRss2( track, feed.getUrl(), top, storeHelper, urls, titles, counts);
             }
             else {
                 parseAtom( track, feed.getUrl(), root, storeHelper, urls, titles, counts);
             }
         } catch (Exception e) {
             throw new WPSConnectorException( "openConnections", e);
         }
     }
     
     private void parseAtom( String track, String url, Element feed, StoreHelper storeHelper, List<String> urls, List<String> titles, List<Integer> counts) {
         String title = "";
         int count = 0;
         for( Object o : (List<Object>)feed.getContent()) {
             if( o instanceof Element) {
                 Element item = (Element) o;
                 if( item.getName().equalsIgnoreCase( "title")) {
                     title = getAtomContent( item);;
                 }
                 if( item.getName().equalsIgnoreCase( "entry")) {
                     List<Object> lo = item.getContent();
                     Attribute attribute = storeHelper.addAttribute( getAtomId( lo));
                     for( Object co : lo) {
                         if( co instanceof Element) {
                             Element contentItem = (Element) co;
                             if( contentItem.getName().equalsIgnoreCase( "title"))
                                 attribute.addProperty( "name", getAtomContent( contentItem));
                             
                             if( contentItem.getName().equalsIgnoreCase( "category")) {
                                 Entity entity = storeHelper.addEntity( contentItem.getAttributeValue( "term"));
                                 String label = contentItem.getAttributeValue( "label");
                                 entity.addProperty( "name", label != null ? label : entity.getId());
                                 entity.addAttribute( attribute, 1);
                                 ++count;
                             }
                         }
                     }
                 }
             }
         }
         urls.add( url);
         counts.add( count);
         titles.add( title);
         if( track.equalsIgnoreCase( "feed")) {
             trackFeeds( url, title, count);
         }
     }
 
     private String getAtomId( List<Object> content) {
         for( Object o : content) {
             if( o instanceof Element) {
                 Element item = (Element) o;
                 if( item.getName().equalsIgnoreCase( "link") && item.getAttributeValue( "rel").equalsIgnoreCase( "alternate"))
                     return item.getAttributeValue( "href");
             }
         }
         return null;
     }
     
     private String getAtomContent( Element item) {
         return ((Text)item.getContent().get( 0)).getText();
     }
     
     private void parseRss2( String track, String url, Element channel, StoreHelper storeHelper, List<String> urls, List<String> titles, List<Integer> counts) {
         String title = channel.getChildText( "title");
         int count = 0;
         for( Element item : (List<Element>)channel.getChildren( "item")) {
             Attribute attribute = storeHelper.addAttribute( item.getChildText( "link"));
             attribute.addProperty( "name", item.getChildText( "title"));
             
             // RSS2
             for( Element category : (List<Element>)item.getChildren( "category")) {
                 Entity entity = storeHelper.addEntity( category.getText());
                 entity.addProperty( "name", entity.getId());
                 entity.addAttribute( attribute, 1);
                 ++ count;
             }
             // DC
             Namespace ns = Namespace.getNamespace( "dc", "http://purl.org/dc/elements/1.1/");
             for( Element subject : (List<Element>)item.getChildren( "subject", ns)) {
                 String sbj = subject.getText();
                 Entity entity = storeHelper.addEntity( sbj);
                 entity.addProperty( "name", sbj);
                 entity.addAttribute( attribute, 1);
                 ++ count;
             }
         }
         urls.add( url);
         counts.add( count);
         titles.add( title);
         if( track.equalsIgnoreCase( "feed")) {
             trackFeeds( url, title, count);
         }
     }
         
     private void trackFeeds( String url, String title, int count) {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Feed feed = (Feed) session.get(Feed.class, url);
         if( feed == null) {
             feed = new Feed( url, title, count > 0);
             session.save( feed);
         }
         else {
             feed.incrementUpdate( title, count > 0);
             session.update( feed);
         }
     }
     
     private void trackSite( String url, String feed) throws MalformedURLException {
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         if( url.length() > 0) {
             url = normalizeUrl( url.trim());
             Site site = (Site) session.get(Site.class, url);
             if( site == null) {
                 site = new Site( url, feed);
                 session.save( site);
             }
             else {
                 site.incrementUpdate( feed);
                 session.update( site);
             }
         }
     }
     private String normalizeUrl( String url) throws MalformedURLException {
         URL u = new URL( url);
         int port = u.getPort();
         return u.getProtocol() + "://" + u.getHost()+ (port != -1 && port != 80 ? ":" + port : "") + u.getPath();
     }
     
 }
