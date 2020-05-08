 package controllers;
 
 import play.*;
import play.cache.*;
import play.libs.*;
 import play.mvc.*;
 import play.data.validation.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
       Post frontPost = Post.find("order by PostedAt desc").first();
       List<Post> olderPosts = Post.find("order by postedAt desc").from(1).fetch(10);
       render(frontPost, olderPosts);
     }
 
     @Before
     public static void addDefaults() {
       renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
       renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
     }
 
     public static void show(Long id) {
       Post post = Post.findById(id);
      String randamID = Codec.UUID();
      render(post, randamID);
     }
 
    public static void postComment(
        Long postId,
        @Required(message="Author is required") String author,
        @Required(message="A message is required") String content,
        @Required(message="Please type the code") String code,
        String randamID) {
       Post post = Post.findById(postId);
      validation.equals(
          code, Cache.get(randamID)
      ).message("Invalid code. Please type it again");
       if (validation.hasErrors()){
        render("Application/show.html", post, randamID);
       }
       post.addComment(author, content);
       flash.success("Thanks for posting %s", author);
      Cache.delete(randamID);
       show(postId);
     }
 
     public static void listTagged(String tag) {
       List<Post> posts = Post.findTaggedWith(tag);
       render(tag, posts);
     }

    public static void captcha(String id) {
      Images.Captcha captcha = Images.captcha();
      String code = captcha.getText("#E4EAFD"); //captcha 文字列の色
      Cache.set(id, code, "10mn");
      renderBinary(captcha);
    }
 }
