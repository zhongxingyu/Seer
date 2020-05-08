 package controllers;
 
 import com.google.common.collect.Lists;
 import models.GuavaTools;
 import models.domain.Talk;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 import java.util.List;
 
 import static models.GuavaTools.collect;
 
 public class Application extends Controller {
 
     public static void index() {
         List<Talk> talks = Talk.filter("year =", 2011).order("-plays").asList();
         Iterable<String> allTags = collect(talks, Talk.findTags());
         List<String> tags = GuavaTools.findMostPopularElements(allTags, 20);
         List<Integer> years = Lists.newArrayList(2010, 2011);
         render(talks, tags, years);
     }
 
 
     public static void filter(@Required int year) {
         List<Talk> talks = Talk.filter("year =", year).order("-plays").asList();
 
         Iterable<String> allTags = collect(talks, Talk.findTags());
         List<String> tags = GuavaTools.findMostPopularElements(allTags, 10);
         List<Integer> years = Lists.newArrayList(2010, 2011);
 
         if(talks == null) {
             notFound("No talks found for the current query. Sorry");
         }
 
        renderTemplate("Application/index.html", talks, tags, years);
     }
 
 }
