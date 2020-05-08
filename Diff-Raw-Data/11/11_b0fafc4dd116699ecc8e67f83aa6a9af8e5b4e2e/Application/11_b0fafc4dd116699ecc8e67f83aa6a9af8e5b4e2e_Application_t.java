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
        List<Talk> talks = Talk.filter("year >", 2009).order("-plays").asList();
         Iterable<String> alleTags = collect(talks, Talk.findTags());
         List<String> tags = GuavaTools.findMostPopularElements(alleTags, 10);
        List<Integer> years = Lists.newArrayList(2010, 2011);
         render(talks, tags, years);
     }
 
 
     public static void filter(@Required String tag) {
         List<Talk> talks = Talk.filter("tags.name =", tag).order("-plays").asList();
 
         if(talks == null) {
             notFound("The talk was not found. Sorry");
         }
 
         renderTemplate("Application/index.html", talks);
     }
 
 }
