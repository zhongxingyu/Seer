 package newscrawler;
 
 import java.sql.Connection;
 
 public class CNews extends NewsPaper{
 
     public CNews(Connection con) {
         super("http://lankacnews.com/sinhala/news/",
                 "h3", "div.date", "div.description",
                1, 100000, "cnews_out.txt", con, "c_news");
     }
 }
