 package newscrawler;
 
 import java.sql.Connection;
 
 public class NewsFirst extends NewsPaper {
 
     public NewsFirst(Connection con) {
         super("http://www.newsfirst.lk/sinhala/node/",
             "a.active","div.panel-pane.pane-node-created", "div.field.field-name-body.field-type-text-with-summary.field-label-hidden",
            0, 30000, "newsFirst_out.txt", con, "news_first"
         );
     }
 }
