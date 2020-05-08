 package chb.base;
 
 import chb.bean.NewsLead;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 public class NewsProxy extends DataProxy{
 
     public NewsProxy() { }
 
     public NewsProxy(String cpath) {
         setConfigPath(cpath);
     }
 
     /**
      * Query all the news.
      * @return  a list containing all the news leads
      */
     public List<NewsLead> queryALLNews() {
          String query = "SELECT authorName, authorEmail, authorTelephone, title, content, createTime, updateTime FROM news";
         String url = buildConnectionString();
         String class_name = "com.mysql.jdbc.Driver";
 
         List<NewsLead> list = new ArrayList<NewsLead>();
 
         Connection conn = getConnection(url, class_name);
         try {
             Statement stat = conn.createStatement();
             ResultSet set = stat.executeQuery(query);
 
             while(set.next()) {
                 NewsLead lead = new NewsLead();
 
                 lead.setAuthorName(set.getString("authorName"));
                 lead.setAuthorEmail(set.getString("authorEmail"));
                 lead.setAuthorTelephone(set.getString("authorTelephone"));
                 lead.setTitle(set.getString("title"));
                 lead.setContent(set.getString("content"));
                 lead.setCreateTime(set.getString("createTime"));
                 lead.setUpdateTime(set.getString("updateTime"));
 
                 list.add(lead);
             }
 
             return list;
 
         }catch (SQLException e) {
               this.errorLogFile.log(LoggingProxy.ERROR,
                       "[---]NewsProxy error. queryAllNews. Database query error. "
                               + e.getMessage());
             return null;
         }
     }
 
     /**
      * Query one single piece of news.
      * @param title the title of the news to be queried.
      * @return NewsLead instance
      */
     public NewsLead querySingleNews(String title) {
         String query = "SELECT authorName, authorEmail, authorTelephone, "
                 +"title, content, createTime, updateTime FROM news "
                 +"WHERE title=\'" + title + "\'";
         String url = buildConnectionString();
         String class_name = "com.mysql.jdbc.Driver";
 
         NewsLead lead = null;
 
         Connection conn = getConnection(url, class_name);
 
         try {
             Statement stat = conn.createStatement();
             ResultSet set = stat.executeQuery(query);
 
             while(set.next()) {
                 lead = new NewsLead();
 
                 lead.setAuthorName(set.getString("authorName"));
                 lead.setAuthorEmail(set.getString("authorEmail"));
                 lead.setAuthorTelephone(set.getString("authorTelephone"));
                 lead.setTitle(set.getString("title"));
                 lead.setContent(set.getString("content"));
                 lead.setCreateTime(set.getString("createTime"));
                 lead.setUpdateTime(set.getString("updateTime"));
             }
 
             stat.close();
             conn.close();
 
             return lead;
 
         }catch (SQLException e) {
             this.errorLogFile.log(LoggingProxy.ERROR,
                     "[---]NewsProxy error. querySingleNews. Database query error. "
                             + e.getMessage());
 
             return null;
         }
     }
 
     public boolean storeSingleNews(NewsLead lead) {
         String query = "INSERT INTO news (";
         query += "authorName,authorEmail,authorTelephone,title,content,createTime,updateTime) ";
         query += "VALUES (\'" + lead.getAuthorName() + "\',";
         query += "\'" + lead.getAuthorEmail() + "\',";
         query += "\'" + lead.getAuthorTelephone() + "\',";
         query += "\'" + lead.getTitle() + "\',";
         query += "\'" + lead.getContent() + "\',";
         query += "\'" + lead.getCreateTime() + "\',";
         query += "\'" + lead.getUpdateTime() + "\') ";
 
         query += "ON DUPLICATE KEY UPDATE ";
         query += "authorName=\'" + lead.getAuthorName() + "\',";
         query += "authorEmail=\'" + lead.getAuthorEmail() + "\',";
         query += "authorTelephone=\'" + lead.getAuthorTelephone() + "\',";
         query += "title=\'" + lead.getTitle() + "\',";
         query += "content=\'" + lead.getContent() + "\',";
         query += "updateTime=\'" + lead.getUpdateTime() + "\';";
 
         String url = buildConnectionString();
         String class_name = "com.mysql.jdbc.Driver";
         Connection conn = getConnection(url, class_name);
 
         try {
             Statement stat = conn.createStatement();
             stat.execute(query);
             stat.close();
             conn.close();
 
             return true;
         }catch (SQLException e) {
             this.errorLogFile.log(LoggingProxy.ERROR,
                     "[---]NewsProxy error. querySingleNews. Database query error. "
                             + e.getMessage() + " " + query);
             return false;
         }
     }
 }
