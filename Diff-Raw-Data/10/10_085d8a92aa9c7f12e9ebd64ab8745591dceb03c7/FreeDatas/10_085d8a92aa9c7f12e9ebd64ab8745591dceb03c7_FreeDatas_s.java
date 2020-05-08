 package controllers;
 
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import models.ViewInformations;
 import models.domain.Article;
 import models.domain.Statistiques;
 import models.domain.Tag;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.ListUtils;
 import play.mvc.Controller;
 import sun.misc.ASCIICaseInsensitiveComparator;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
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
 
         final ViewInformations viewInformations = new ViewInformations(articles, statistiques.get(statistiques.size() - 1), Lists.<Tag>newArrayList());
 
 
         Statistiques stats = statistiques.get(statistiques.size() - 1);
         stats.setTitleMatrix(viewInformations.getTitleMatrix());
 
 
         renderJSON(stats);
     }
 
     public static void titles() {
 
         final List<Article> articles = Article.q().filter("current", true).asList();
         List<Article> orderedList = Ordering.from(new Comparator<Article>() {
             public int compare(Article o1, Article o2) {
                 return new Long(o1.getInsertionDate()).compareTo(new Long(o2.getInsertionDate()));
             }
         }).sortedCopy(articles);
 
         renderJSON(orderedList);
     }
 
     public static void diff(final String synchro) {
 
         final List<Article> articles = Article.q().filter("current", true).asList();
 
         final HashSet<String> l = Sets.newHashSet(Splitter.on('-').split(synchro));
 
         final Collection<Article> as = Collections2.filter(articles, new Predicate<Article>() {
            public boolean apply(@Nullable Article article) {
                return !l.contains(article.getGoogleId());
             }
         });
         renderJSON(as);
     }
 
 }
