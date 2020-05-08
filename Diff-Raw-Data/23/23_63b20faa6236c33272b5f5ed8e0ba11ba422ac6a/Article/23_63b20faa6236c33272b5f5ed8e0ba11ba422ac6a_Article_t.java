 package org.guiceae.main.model;
 
 import com.google.appengine.api.datastore.Text;
 import com.google.common.base.Objects;
 
 import javax.persistence.*;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: Igor Petruk
  * Date: 29.06.12
  * Time: 21:47
  */
 
 @Entity
 public class Article {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;
 
     String feed, title, permalink, author;
 
     Date created, lastUpdated;
 
     @Enumerated(EnumType.STRING)
     ArticleState state = ArticleState.PENDING;
 
     @Lob
     Text content = new Text("");
 
     @Lob
     Text shortContent = new Text("");
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getFeed() {
         return feed;
     }
 
     public void setFeed(String feed) {
         this.feed = feed;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getPermalink() {
         return permalink;
     }
 
     public void setPermalink(String permalink) {
         this.permalink = permalink;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public Date getLastUpdated() {
         return lastUpdated;
     }
 
     public void setLastUpdated(Date lastUpdated) {
         this.lastUpdated = lastUpdated;
     }
 
     public String getContent() {
         return content.getValue();
     }
 
     public void setContent(String content) {
         this.content = new Text(content);
     }
 
     public ArticleState getState() {
         return state;
     }
 
     public void setState(ArticleState state) {
         this.state = state;
     }
 
     public String getShortContent() {
         return shortContent.getValue();
     }
 
     public void setShortContent(String shortContent) {
         this.shortContent = new Text(shortContent);
     }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", feed='" + feed + '\'' +
                ", title='" + title + '\'' +
                ", permalink='" + permalink + '\'' +
                ", author='" + author + '\'' +
                ", created=" + created +
                ", lastUpdated=" + lastUpdated +
                ", state=" + state +
                ", content=" + content +
                ", shortContent=" + shortContent +
                '}';
    }
 }
