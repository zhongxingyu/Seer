 package controllers.data;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 
import javax.validation.Valid;

 import play.mvc.Result;
 
 import controllers.EController;
 
 import models.data.Link;
 import models.data.manager.DataElement;
 import models.data.manager.DataFactory;
 import models.data.manager.DataManager;
 import models.data.links.LinkFactory;
 import models.data.links.LinkManager;
 
 import views.html.data.data_view;
 
 /**
  * Controls the managing of a dataManager.
  * @author Felix Van der Jeugt
  */
 public class DataController extends EController {
 
     protected static Map<String, DataFactory<?>> factories
         = new HashMap<String, DataFactory<?>>();
     protected static Map<String, DataManager<?>> managers
         = new HashMap<String, DataManager<?>>();
 
     static {
         factories.put("links", new LinkFactory());
         managers.put("links",  new LinkManager());
     }
 
     /**
      * Show the current list of elements.
      * @param t The type of elements, for instance "links".
      */
     public static Result show(String t) {
         return ok(data_view.render(
             managers.get(t),
             factories.get(t),
             breadcrumbs(t)
         ));
     }
 
     /**
      * This method adds a new element of T to the list.
      * @param t The type of elements, for instance "links".
      */
     //* @param fields The strings describing the new element.
    public static Result add(String t) {
        FakeForm ff = form(FakeForm.class).bindFromRequest().get();
        String[] fields = ff.fields.toArray(new String[2]);
        System.out.println(t + fields[0] + fields[1]);
         managers.get(t).add(factories.get(t).createFromStrings(fields));
         return show(t);
     }
 
     /**
      * This method removes an element from the list.
      * @param t The type of elements, for instance "links".
      * @param id The id of the element.
      */
     public static Result remove(String t, String id) {
         managers.get(t).remove(id);
         return show(t);
     }
 
     /** The breadcrumbs. */
     public static List<Link> breadcrumbs(String t) {
         List<Link> crumbs = new ArrayList<Link>();
         crumbs.add(new Link("Home", "/"));
         crumbs.add(new Link(managers.get(t).title(), "/manage/" + managers.get(t).url()));
         return crumbs;
     }
 
    public static class FakeForm {
        @Valid public List<String> fields;
        public FakeForm() {}
    }

 }
