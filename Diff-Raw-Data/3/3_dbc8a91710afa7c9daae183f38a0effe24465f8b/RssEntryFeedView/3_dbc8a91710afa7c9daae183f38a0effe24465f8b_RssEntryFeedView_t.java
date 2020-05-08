 package am.ik.categolj.view;
 
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.view.AbstractView;
 
 import am.ik.categolj.entity.Entry;
 import am.ik.categolj.feed.CustomizedWireFeedOutput;
 import am.ik.categolj.util.MarkdownUtils;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 
 public class RssEntryFeedView extends AbstractView {
     private String feedTitle;
     private String feedLink;
     private String feedDescription;
 
     public RssEntryFeedView() {
         setContentType("application/rss+xml");
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void renderMergedOutputModel(Map<String, Object> model,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         response.setContentType(getContentType());
 
         SyndFeed feed = new SyndFeedImpl();
         feed.setTitle(feedTitle);
         feed.setEncoding("UTF-8");
         feed.setLink(feedLink);
         feed.setPublishedDate(new Date());
         feed.setDescription(feedDescription);
         feed.setFeedType("rss_2.0");
         List<SyndEntry> entries = new ArrayList<SyndEntry>();
         feed.setEntries(entries);
 
         for (Entry e : (List<Entry>) model.get("entryList")) {
             SyndContent description = new SyndContentImpl();
             description.setValue("<![CDATA["
                     + MarkdownUtils.markdown(e.getContent()) + "]]>");
             description.setType("text/html");
 
             SyndEntry entry = new SyndEntryImpl();
             entry.setTitle(e.getTitle());
            entry.setLink(feed.getLink() + "/entry/view/id/" + e.getId() + "/"
                    + e.getTitle());
             entry.setPublishedDate(e.getCreatedAt());
             entry.setUpdatedDate(e.getUpdatedAt());
             entry.setDescription(description);
 
             entries.add(entry);
         }
 
         CustomizedWireFeedOutput feedOutput = new CustomizedWireFeedOutput();
         ServletOutputStream out = response.getOutputStream();
         feedOutput.output(feed.createWireFeed(), new OutputStreamWriter(out,
                 feed.getEncoding()), true);
         out.flush();
     }
 
     public void setFeedTitle(String feedTitle) {
         this.feedTitle = feedTitle;
     }
 
     public void setFeedLink(String feedLink) {
         this.feedLink = feedLink;
     }
 
     public void setFeedDescription(String feedDescription) {
         this.feedDescription = feedDescription;
     }
 
 }
