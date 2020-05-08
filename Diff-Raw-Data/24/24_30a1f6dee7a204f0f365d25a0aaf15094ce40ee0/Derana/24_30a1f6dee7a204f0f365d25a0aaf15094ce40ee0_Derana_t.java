 package newscrawler;
 
 import java.sql.Connection;
 
 public class Derana extends NewsPaper {
 
     public Derana(Connection con) {
         super("http://sinhala.adaderana.lk/news.php?nid=",
             "h2.completeNewsTitle", "p.newsDateStamp", "div.newsContent",
            5999, 40000, "derana_out.txt", con, "ada_derana"
         );
     }
 }
