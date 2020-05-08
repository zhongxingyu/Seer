 package dk.itu.jesl.deck_code;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import java.io.IOException;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class ProcessDeckCodeServlet extends HttpServlet {
     private static final Logger log = Logger.getLogger(ProcessDeckCodeServlet.class.getName());
 
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         UserService userService = UserServiceFactory.getUserService();
         User user = userService.getCurrentUser();
 
        String name = req.getParameter("name");
         String text = req.getParameter("text");
 
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("User", user.getUserId());
        Query query = new Query("Script", userKey).
            setFilter(new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, name));
         Entity script = datastore.prepare(query).asSingleEntity();
         script.setProperty("text", text);
        datastore.put(script);
        log.info("User " + user + " posted script " + name + "\n====\n" + text + "\n====");
         resp.sendRedirect("/");
     }
 }
