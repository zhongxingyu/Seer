 package nz.co.searchwellington.views;
 
 import nz.co.searchwellington.dates.DateFormatter;
 import nz.co.searchwellington.model.Geocode;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Watchlist;
 
 import com.sun.syndication.feed.module.georss.GeoRSSModule;
 import com.sun.syndication.feed.module.georss.W3CGeoModuleImpl;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 
 public class RssItemMaker {
 	
 	public SyndEntry makeRssItem(Resource content) {
 		// TODO validate - must have urls etc.
 		if (content.getType().equals("N")) {			
 			return getNewsitemRssItem((Newsitem) content);
 		}
 		if (content.getType().equals("L")) {			
 			return getWatchlistRssItem((Watchlist) content);
 		}
 		
 		return getDefaultRssItem(content);
 	}
 	
 	private SyndEntry getDefaultRssItem(Resource content) {
         SyndEntry entry = new SyndEntryImpl();      
         entry.setTitle(stripIllegalCharacters(content.getName()));
         entry.setLink(content.getUrl());
 
         SyndContent description = new SyndContentImpl();
         description.setType("text/plain");
         description.setValue(stripIllegalCharacters(content.getDescription()));
         entry.setDescription(description);
         return entry;
     }
 	
 	
 	public SyndEntry getNewsitemRssItem(Newsitem content) {
 		SyndEntry rssItem = getDefaultRssItem(content);
 		rssItem.setPublishedDate(content.getDate());
 		if (content.getPublisherName() != null) {
 			rssItem.setAuthor(content.getPublisherName());
 		}
 		final Geocode geocode = content.getGeocode();
 		if (geocode != null && geocode.isValid()) {            
 			GeoRSSModule geoRSSModule = new W3CGeoModuleImpl();
 	        geoRSSModule.getPosition().setLatitude(geocode.getLatitude());
 	        geoRSSModule.getPosition().setLongitude(geocode.getLongitude());
 	        rssItem.getModules().add(geoRSSModule);            
 		}
 		return rssItem;
 	}
 	
 	public SyndEntry getWatchlistRssItem(Watchlist content) {
 		SyndEntry rssItem = getDefaultRssItem(content);
         if (content.getLastChanged() != null) {
             // TODO this is abit odd; suggests RSS should not be on the model.
             DateFormatter dateFormatter = new DateFormatter();
             rssItem.setTitle(content.getName() + " - " + dateFormatter.formatDate(content.getLastChanged(), "d MMM yyyy"));
         }
         return rssItem;
     }
 	
 	private String stripIllegalCharacters(String input) {
 		return input.replaceAll("[^\\u0020-\\uFFFF]", "");
 	}
 	
 }
