 package models;
 
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.Blob;
 import play.db.jpa.Model;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 public class  Article extends Model {
 
     @Required
     @MaxSize(250)
     public String title;
 
     @Required
     public boolean approved = true;
 
     public int article_view;
 
     @Required
     @Lob
     @MaxSize(20000)
     public String content;
 
     @Required
     @ManyToOne
     public User author;
 
     @Required
     @ManyToOne
     public Category category;
 	
 	public Blob articleImage;
 
     @OneToMany(mappedBy="article", fetch = FetchType.EAGER, cascade=CascadeType.ALL)
     public List<Comment> comments;
 
     public Date submit_date;
     
     public String tags;
 
    public Article(String title, String content, User author, Category category, boolean approved, Blob articleImage, String tags){
         this.title = title;
         this.content = sanitizeContent(content);
         this.author = author;
         this.category = category;
         this.submit_date = new Date();
         this.approved = approved;
         this.comments = new ArrayList<Comment>();
         this.article_view = 0;
 		this.articleImage = articleImage;
         this.tags = tags;
     }
 
     private String sanitizeContent(String content) {
         if(content != null){
             content.replace("[\']", "&rsquo;");
             content.replace("'","&rsquo;");
 
         }
         return content;
     }
 
     public Article addComment(Long articleId, String content, String userName, String userEmail) {
         Article article = Article.findById(articleId);
         Comment newComment = new Comment(article, content, userName, userEmail, false).save();
         this.comments.add(newComment);
         this.save();
         return this;
     }
 
 }
