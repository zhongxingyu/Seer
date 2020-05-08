 package org.lisak.pguide.model;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import org.lisak.pguide.dao.ContentGaeDao;
 import org.lisak.pguide.exceptions.NoSuchEntity;
 
 import javax.security.auth.Subject;
 
 import java.util.Comparator;
 import java.util.Formatter;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lisak
  * Date: 06.08.13
  * Time: 17:12
  */
 @EntitySubclass(index = true)
 public class Article extends Content {
     @Index String title;
     String text;
     //parent is used for breadcrumb navigation & should be set to Id of parent article
     //if parent == null, article with id "main" is the parent
     String parent;
 
     public Article() {
         super();
     }
 
     public Article(String id, String title, String text) {
         this(id, title, text, null);
     }
 
     public Article(String id, String title, String text, String parent) {
         super(id);
         this.title = title;
         this.text = text;
         this.parent = parent;
     }
 
     public String getText() {
         return text;
     }
 
     public String getFormattedText() {
         //returns formatted text, ie. paragraphs enclosed in <div>
         String adjusted = text.replaceAll("(?m)^[ \t]*\r?\n", "</div>\n<div>");
 
         return "<div>" + adjusted + "</div>";
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public static Comparator<Article> TitleComparator = new Comparator<Article>() {
 
         @Override
         public int compare(Article article, Article article2) {
             return article.getTitle().compareTo(article2.getTitle());
         }
 
 
     };
 
     public String getParent() {
         return parent;
     }
 
     public void setParent(String parent) {
         this.parent = parent;
     }
 
     public String getBreadcrumbs() {
         Article _parent;
         Formatter _f = new Formatter();
        if(getParent() == null) {
             return "<a href='/'>Main</a> &gt; " + _f.format("<a href='/article/%s'>%s</a>", this.getId(), this.getTitle());
         } else {
             //fetch parent entity - yes, I know this is ugly and dao-specific...
             _parent = (Article) ContentGaeDao.getStaticDao().get(getParent());
             //get parent entity's breadcrumbs & append it to the result
             return _parent.getBreadcrumbs() + " &gt; " + _f.format("<a href='/article/%s'>%s</a>", this.getId(), this.getTitle());
         }
     }
 }
