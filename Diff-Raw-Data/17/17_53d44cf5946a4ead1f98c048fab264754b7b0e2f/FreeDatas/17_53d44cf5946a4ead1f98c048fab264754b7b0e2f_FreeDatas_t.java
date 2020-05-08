 package controllers;
 
import models.ViewInformations;
import models.domain.Article;
 import models.domain.Statistiques;
 import play.mvc.Controller;
 
 import java.util.List;
 
 /**
  * .
  *
  * @author fsznajderman
  *         date :  09/06/12
  */
 public class FreeDatas extends Controller {
 
 
     public static void freeStatGplus() {
 
        final List<Article> articles = Article.q().filter("current", true).asList();
 
         final List<Statistiques> statistiques = Statistiques.q().filter("current", true).asList();
 
        final ViewInformations viewInformations = new ViewInformations(articles, statistiques.get(statistiques.size() - 1));
 
 
        Statistiques stats = statistiques.get(statistiques.size() - 1);
          stats.setTitleMatrix(viewInformations.getTitleMatrix());
 
         renderJSON(stats);
     }
 }
