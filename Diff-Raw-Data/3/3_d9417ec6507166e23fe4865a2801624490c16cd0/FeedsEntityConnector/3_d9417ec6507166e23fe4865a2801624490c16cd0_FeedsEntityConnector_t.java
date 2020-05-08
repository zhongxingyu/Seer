 package com.socialcomputing.wps.server.plandictionary.connectors.datastore.feeds;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.jdom.Element;
 import org.jdom.Text;
 
 import au.id.jericho.lib.html.Segment;
 import au.id.jericho.lib.html.Source;
 import au.id.jericho.lib.html.StartTag;
 
 import com.socialcomputing.wps.server.plandictionary.connectors.WPSConnectorException;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.Attribute;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.DatastoreEntityConnector;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.Entity;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.PropertyDefinition;
 import com.socialcomputing.wps.server.plandictionary.connectors.utils.UrlHelper;
 
 public class FeedsEntityConnector extends DatastoreEntityConnector {
     protected   String m_InvertedDef = null;
     protected   List<UrlHelper> m_feeds = null;
 
 	static FeedsEntityConnector readObject(org.jdom.Element element) {
 		FeedsEntityConnector connector = new FeedsEntityConnector( element.getAttributeValue("name"));
 		connector._readObject( element);
         connector.m_InvertedDef = element.getAttributeValue( "invert");
 
         List<Element> defs = (List<Element>)element.getChildren( UrlHelper.DTD_DEFINITION);
         connector.m_feeds = new ArrayList<UrlHelper>( defs.size());
         for( Element def : defs) {
             UrlHelper feed = new UrlHelper();
             feed.readObject( def);
             connector.m_feeds.add( feed);
         }
         connector.entityProperties.add( new PropertyDefinition( "poss_id", "id"));
         connector.entityProperties.add( new PropertyDefinition( "poss_name", "name"));
         connector.attributeProperties.add( new PropertyDefinition( "poss_id", "id"));
         connector.attributeProperties.add( new PropertyDefinition( "poss_name", "name"));
 
         return connector;
 	}
 
 	public FeedsEntityConnector(String name) {
 		super(name);
 	}
 
 	@Override
 	public void openConnections(int planType, Hashtable<String, Object> wpsparams) throws WPSConnectorException {
 		super.openConnections( planType, wpsparams);
 
 		List<String> titles = new ArrayList<String>();
         List<String> urls = new ArrayList<String>();
         List<String> counts = new ArrayList<String>();
         
 		m_inverted =  UrlHelper.ReplaceParameter( m_InvertedDef, wpsparams).equalsIgnoreCase( "true");
 		for( UrlHelper feed : m_feeds) {
 		    String urlLst = UrlHelper.ReplaceParameter( feed.getUrl(), wpsparams);
             for( String url : urlLst.split( ",")) {
                if( !url.startsWith( "http://") && !url.startsWith( "http://")) {
                    url = "http://" + url;
                }
                 feed.setUrl( url);
     		    feed.openConnections( planType, wpsparams);
     	        read( feed, planType, wpsparams, titles, urls, counts);
                 feed.closeConnections();
             }
             trackSite( wpsparams, urlLst);
 		}
 		wpsparams.put( "FEEDS_TITLES",  titles.toArray());
         wpsparams.put( "FEEDS_URLS",    urls.toArray());
         wpsparams.put( "FEEDS_COUNTS",  counts.toArray());
 	}
 
     private void read(UrlHelper feed, int planType, Hashtable<String, Object> wpsparams, List<String> titles, List<String> urls, List<String> counts) throws WPSConnectorException {
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
                     curFeed.openConnections( planType, wpsparams);
                     readXml( curFeed, planType, wpsparams, titles, urls, counts);
                     curFeed.closeConnections();
                 }
             }
         }
         else {
             readXml( feed, planType, wpsparams, titles, urls, counts);
         }
     }
     
 	private void readXml(UrlHelper feed, int planType, Hashtable<String, Object> wpsparams, List<String> titles, List<String> urls, List<String> counts) throws WPSConnectorException {
 	    try {
 			org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(false);
 			org.jdom.Document doc = builder.build( feed.getStream());
 			Element root = doc.getRootElement();
 			
 	        Element top = root.getChild( "channel");
 	        if( top != null) {
 	            urls.add( feed.getUrl());
 	            parseRss2( top, titles, counts);
                 track( wpsparams, urls.get( urls.size()-1), titles.get( titles.size()-1), counts.get( urls.size()-1));
 	        }
 	        else {
                 urls.add( feed.getUrl());
 	            parseAtom( root, titles, counts);
                 track( wpsparams, urls.get( urls.size()-1), titles.get( titles.size()-1), counts.get( urls.size()-1));
 	        }
 		} catch (Exception e) {
             throw new WPSConnectorException( "openConnections", e);
 		}
         for( Attribute attribute : m_Attributes.values()) {
             if( !isInverted())
                 addEntityProperties( attribute);
         }
         if( isInverted()) {
             for( Entity entity : m_Entities.values()) { 
                 addAttributeProperties( entity);
             }
         }
 	}
 	
 	private void parseAtom( Element feed, List<String> titles, List<String> counts) {
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
                     Attribute attribute = addAttribute( getAtomId( lo));
                     for( Object co : lo) {
                         if( co instanceof Element) {
                             Element contentItem = (Element) co;
                             if( contentItem.getName().equalsIgnoreCase( "title"))
                                 attribute.addProperty( "name", getAtomContent( contentItem));
                             
                             if( contentItem.getName().equalsIgnoreCase( "category")) {
                                 Entity entity = addEntity( contentItem.getAttributeValue( "term"));
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
         counts.add( String.valueOf(count));
         titles.add( title);
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
     
     private void parseRss2( Element channel, List<String> titles, List<String> counts) {
         String title = channel.getChildText( "title");
         int count = 0;
         for( Element item : (List<Element>)channel.getChildren( "item")) {
             Attribute attribute = addAttribute( item.getChildText( "link"));
             attribute.addProperty( "name", item.getChildText( "title"));
             
             for( Element category : (List<Element>)item.getChildren( "category")) {
                 Entity entity = addEntity( category.getText());
                 entity.addProperty( "name", entity.getId());
                 entity.addAttribute( attribute, 1);
                 ++ count;
             }
         }
         counts.add( String.valueOf(count));
         titles.add( title);
     }
     	
     private void track( Hashtable<String, Object> wpsparams, String url, String title, String count) {
         String track = ( String) wpsparams.get( "track");
         if( track != null && track.length() > 0) {
             UrlHelper u = new UrlHelper( UrlHelper.Type.GET, track);
             u.addParameter( "url", url);
             u.addParameter( "title", title);
             u.addParameter( "count", count);
             try {
                 u.openConnections( 0, wpsparams);
                 u.closeConnections();
             }
             catch (WPSConnectorException e) {
                 //e.printStackTrace();
             }
         }
     }
     
     private void trackSite( Hashtable<String, Object> wpsparams, String feed) {
         String track = ( String) wpsparams.get( "trackUrl");
         String site = ( String) wpsparams.get( "site");
         if( track != null && track.length() > 0 && site != null && site.length() > 0) {
             UrlHelper u = new UrlHelper( UrlHelper.Type.GET, track);
             u.addParameter( "url", site);
             u.addParameter( "feed", feed);
             try {
                 u.openConnections( 0, wpsparams);
                 u.closeConnections();
             }
             catch (WPSConnectorException e) {
                 //e.printStackTrace();
             }
         }
     }
     
 	@Override
 	public void closeConnections() throws WPSConnectorException {
 		super.closeConnections();
         for( UrlHelper feed : m_feeds) {
             feed.closeConnections();
         }
 	}
 
 }
