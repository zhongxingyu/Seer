 package nz.co.searchwellington.feeds;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import nz.co.searchwellington.feeds.rss.RssHttpFetcher;
 import nz.co.searchwellington.model.Comment;
 import nz.co.searchwellington.model.CommentFeed;
 import nz.co.searchwellington.utils.UrlFilters;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.log4j.Logger;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 public class CommentFeedService {
     
     public final static Logger log = Logger.getLogger(CommentFeedService.class);
     
     private RssHttpFetcher rssHttpFetcher;
         
     public CommentFeedService(RssHttpFetcher rssHttpFetcher) {       
         this.rssHttpFetcher = rssHttpFetcher;
     }
     
     public List<Comment> loadComments(CommentFeed commentFeed) {
     	List<Comment> comments = new ArrayList<Comment>();
     	// TODO this stopped working around 14 june 2011
     	//if (commentFeed.getNewsitem() == null) {
     	//	log.warn("Comment feed has no associated newsitems; no point in loading comments: " + commentFeed.getUrl());
     	//	return comments;
     	//}
    	//log.info("Loading comments from comment feed for newsitem: " + commentFeed.getNewsitem().getName());
     	
         SyndFeed syndfeed = rssHttpFetcher.httpFetch(commentFeed.getUrl());
         if (syndfeed != null) {            
             log.debug("Comment feed is of type: " + syndfeed.getFeedType());            
             List entires = syndfeed.getEntries();
             for (Iterator iter = entires.iterator(); iter.hasNext();) {
                 SyndEntry item = (SyndEntry) iter.next();
                 Comment comment = new Comment(extractCommentBody(item));
                 comments.add(comment);
             }
             
         } else {
             log.warn("Comment feed was null after loading attempt.");
         }
        
         log.info("Loaded " + comments.size() + " comments.");
         return comments;        
     }
    
     private String extractCommentBody(SyndEntry item) {
 		String commentBody = item.getTitle();
 		
         if (item.getDescription() != null) {
         	String description = item.getDescription().getValue();
         	if (description != null && !description.equals("")) {
         		commentBody = stripHtmlFromCommentItem(description);
         	}
         }
                 
         List<SyndContent> contents = item.getContents();        
         for (SyndContent content : contents) {
             if (content.getType().equals("html")) {            	
                 commentBody = stripHtmlFromCommentItem(content.getValue());
             }
         }
         return commentBody;
 	}
 
 	private String stripHtmlFromCommentItem(String contentValue) {
 		return UrlFilters.stripHtml(StringEscapeUtils.unescapeHtml(contentValue));
 	}
     
 }
