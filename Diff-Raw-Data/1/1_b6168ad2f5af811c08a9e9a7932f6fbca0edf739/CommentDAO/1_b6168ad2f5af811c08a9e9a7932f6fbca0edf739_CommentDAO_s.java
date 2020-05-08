 package nz.co.searchwellington.repositories;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import nz.co.searchwellington.feeds.rss.RssHttpFetcher;
 import nz.co.searchwellington.model.Comment;
 import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.CommentImpl;
 import nz.co.searchwellington.utils.UrlFilters;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.log4j.Logger;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 public class CommentDAO {
     
     public final Logger log = Logger.getLogger(CommentDAO.class);
     
     RssHttpFetcher rssHttpFetcher;
         
     public CommentDAO(RssHttpFetcher rssHttpFetcher) {       
         this.rssHttpFetcher = rssHttpFetcher;
     }
 
     
     
     public List<Comment> loadComments(CommentFeed commentFeed) {      
         List<Comment> comments = new ArrayList<Comment>();
         
         SyndFeed syndfeed = rssHttpFetcher.httpFetch(commentFeed.getUrl());
         if (syndfeed != null) {            
             log.info("Comment feed is of type: " + syndfeed.getFeedType());
             
             List entires = syndfeed.getEntries();
             for (Iterator iter = entires.iterator(); iter.hasNext();) {
                 SyndEntry item = (SyndEntry) iter.next();
                 Comment comment = extractCommentFeedEntire(item);
                 comments.add(comment);
             }
             
         } else {
             log.error("Comment feed was null after loading attempt.");
         }
        
         log.info("Loaded " + comments.size() + " comments.");
         return comments;        
     }
 
    
     private Comment extractCommentFeedEntire(SyndEntry item) {
         Comment comment = new Comment();
         String title = item.getTitle();
         comment.setTitle(title);
                 
         if (item.getDescription() != null) {
         	String description = item.getDescription().getValue();
         	if (description != null && !description.equals("")) {
         		comment.setTitle(stripAndTrimContent(description));
         	}
         }
                 
         List<SyndContent> contents = item.getContents();        
         for (SyndContent content : contents) {
             if (content.getType().equals("html")) {            	
                 comment.setTitle(stripAndTrimContent(content.getValue()));
             }
         }
 
         return comment;
     }
 
 
 	private String stripAndTrimContent(String contentValue) {
 		String body = UrlFilters.stripHtml(StringEscapeUtils.unescapeHtml(contentValue));                
 		int clip = body.length();
 		if (body.length() > 255) {
 		    clip = 255;
 		}                
 		String finalValue = body.substring(0, clip-1);
 		return finalValue;
 	}
     
 }
