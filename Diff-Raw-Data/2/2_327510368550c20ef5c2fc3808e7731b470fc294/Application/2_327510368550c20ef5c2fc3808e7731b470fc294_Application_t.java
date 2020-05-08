 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
        Advert frontAdvert = Advert.find("order by postedAt desc").first();
         List<Advert> olderAdverts = Advert.find(
             "order by postedAt desc"
         ).from(1).fetch(10);
         render(frontAdvert, olderAdverts);
     }
 
 }
