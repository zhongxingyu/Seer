 package nz.co.searchwellington.model;
 
 import java.util.Date;
 import java.util.Set;
 
 public class FeedNewsitem extends NewsitemImpl {
 
 	private Feed feed;
 	private int itemNumber;
 	private Resource localCopy;
 	private boolean isSuppressed;
 	private Image image;
 	
     public FeedNewsitem(int id, String name, String url, String description, Date date, Set<DiscoveredFeed> discoveredFeeds) {       
         this.id = id;
         this.name = name;
         this.url = url;
         this.description = description;
         this.date = date;
         this.discoveredFeeds = discoveredFeeds;
         this.isSuppressed = false;
     }
 	
 	@Override
 	public String getType() {
 		return "FNI";
 	}
 
 	public Feed getFeed() {
 		return feed;
 	}
 
 	public void setFeed(Feed feed) {
 		this.feed = feed;
 	}
 
 	public int getItemNumber() {
 		return itemNumber;
 	}
 
 	public void setItemNumber(int itemNumber) {
 		this.itemNumber = itemNumber;
 	}
 
 	public Resource getLocalCopy() {
 		return localCopy;
 	}
 
 	public void setLocalCopy(Resource localCopy) {
 		this.localCopy = localCopy;
 	}
 
 	public boolean isSuppressed() {
 		return isSuppressed;
 	}
 
 	public void setSuppressed(boolean isSuppressed) {
 		this.isSuppressed = isSuppressed;
 	}
 
 	public Image getImage() {
 		return image;
 	}
 
 	public void setImage(Image image) {
 		this.image = image;
 	}
 			
 }
