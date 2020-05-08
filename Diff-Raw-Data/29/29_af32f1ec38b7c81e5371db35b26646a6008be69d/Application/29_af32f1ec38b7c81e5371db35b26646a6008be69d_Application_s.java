 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 @With(Secure.class)
 public class Application extends Controller {
 
     public static void index() {
        List<Article> articles = Article.find(
             "order by title desc"
         ).fetch(10);
 		String user = Security.connected();
         render(articles);
     }
 
 }
