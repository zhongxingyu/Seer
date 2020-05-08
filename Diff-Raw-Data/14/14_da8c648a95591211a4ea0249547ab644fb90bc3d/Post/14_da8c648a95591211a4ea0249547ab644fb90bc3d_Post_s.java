 package models;
 
 import java.util.*;
 import play.db.ebean.*;
 import play.data.validation.Constraints.*;
 import javax.persistence.*;
 
 @Entity
 public class Post extends Model {
 	@Id
 	public Long id;
   
 	@Required
 	@Column(length=4000, nullable=false)
 	public String post;
 	
 	@Required
 	public String title;
 	
 	@Required
 	public String author;
   
 	public static Finder<Long,Post> find = new Finder(
 		Long.class, Post.class);
 		
 	
 	public static List<Post> all() {
 		return find.all();
 	}

 	public static void create(Post post) {
 		post.save();
 	}
 
 	public static void delete(Long id) {
 		find.ref(id).delete();
 	}
 	
 	public static List<Post> getUserPosts(String user){
 		return find.where().eq("author", user).findList();
 	}
 	
 	public static void edit(Post post){
 		post.update();
 	}
 		
 }
