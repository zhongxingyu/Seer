 package controllers.statistics;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import javax.validation.Valid;
 
 import com.avaje.ebean.Ebean;
 
 import play.mvc.Result;
 import play.mvc.BodyParser;
 import play.mvc.BodyParser.Json;
 
 import controllers.EController;
 
 import models.dbentities.UserModel;
 import models.data.Link;
 import models.user.AuthenticationManager;
 import models.statistics.populations.Population;
 import models.statistics.populations.SinglePopulation;
 import models.statistics.populations.PopulationFactory;
 import models.statistics.populations.PopulationChooser;
 import models.statistics.populations.PopulationFactoryException;
 import models.statistics.statistics.Statistic;
 import models.statistics.statistics.StatisticFactory;
 
 import views.html.statistics.statistics;
 
 /**
  * This class returns the statistics page for the logged in user.
  * @author Felix Van der Jeugt
  */
 public class Statistics extends EController {
 
     /* Our lovely breadcrumbs. */
     private static List<Link> breadcrumbs = new ArrayList<Link>();
     static {
         breadcrumbs.add(new Link("app.home", "/"));
         breadcrumbs.add(new Link("statistics.title", "/statistics"));
     }
 
     /** Display the main statistics page. */
     public static Result show(String questionorset, Integer id) {
         GroupForm gf = form(GroupForm.class).bindFromRequest().get();
         PopulationChooser populationChooser = getPopulationChooser();
         List<Population> populations = parsePopulations(gf);
         Statistic statistic = parseStatistic(gf);
 
         // Make sure the user doesn't view populations he shouldn't, and disable
         // those he already selected.
         populations = populationChooser.filter(populations);
 
         return ok(statistics.render(populations, populationChooser, statistic, breadcrumbs));
     }
 
     /** Return the graph data as a Json object. */
     public static Result showJson(String questionorset, Integer id) {
         GroupForm gf = form(GroupForm.class).bindFromRequest().get();
         PopulationChooser populationChooser = getPopulationChooser();
         List<Population> populations = parsePopulations(gf);
         Statistic statistic = parseStatistic(gf);
 
         // Make sure the user doesn't view populations he shouldn't, and disable
         // those he already selected.
         populations = populationChooser.filter(populations);
 
         return ok(statistic.asJson(populations));
     }
 
     private static List<Population> parsePopulations(GroupForm gf) {
         List<Population> populations = new ArrayList<Population>();
         if(gf.colours != null)
         for(int i=0; i < gf.colours.size(); i++) {
             try {
                 populations.add(PopulationFactory.instance().create(
                     gf.types.get(i),
                     gf.ids.get(i),
                     gf.colours.get(i)
                 ));
             } catch(PopulationFactoryException e) {
                 // Simply don't add the population. This happens only when the
                 // type is nonexistent due to holes in the array (when people
                 // remove populations) or when users are modifying the
                 // parameters.
             }
         }
         return populations;
     }
 
     private static PopulationChooser getPopulationChooser() {
         return PopulationChooser.getPopulationChooser(
             AuthenticationManager.getInstance().getUser()
         );
     }
 
     private static Statistic parseStatistic(GroupForm gf) {
         Statistic statistic = null;
         if(gf.statistic != null) {
             statistic = StatisticFactory.instance().create(gf.statistic);
             Statistic filter;
             if(gf.filters != null) for(int i = 0; i < gf.filters.size(); i++) {
                if(gf.filters.get(i) != null) {
                    filter = StatisticFactory.instance().create(gf.filters.get(i));
                    statistic.addFilters(filter);
                    if(gf.conditions != null) {
                        for(String j : gf.conditions.get(i)) {
                            if(j != null && !"".equals(j)) filter.addConditions(j);
                        }
                     }
                 }
             }
         }
         return statistic;
     }
 
     public static class GroupForm {
         @Valid public List<String> colours;
         @Valid public List<String> types;
         @Valid public List<String> ids;
         @Valid public String statistic;
         @Valid public List<String> filters;
         @Valid public List<List<String>> conditions;
         public GroupForm() {}
     }
 
 }
