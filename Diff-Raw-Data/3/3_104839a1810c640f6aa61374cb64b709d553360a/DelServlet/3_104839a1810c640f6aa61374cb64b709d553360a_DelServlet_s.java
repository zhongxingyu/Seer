 package soardb;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.datastore.TransactionOptions;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 @SuppressWarnings("serial")
 public class DelServlet extends HttpServlet {
 	public static final Logger log = Logger.getLogger(DelServlet.class.getName());
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		log.info("in del servlet");
 		
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		
 		
 		Key Key = KeyFactory.stringToKey(req.getParameter("key"));
 		String delete = req.getParameter("delete");
 		String newsFlag = req.getParameter("news");
 		String Name = "";
 		try {
 			Name = (String)datastore.get(Key).getProperty(N.name);
 		} catch (EntityNotFoundException e) {
 			e.printStackTrace();
 		}
 		TransactionOptions options = TransactionOptions.Builder.withXG(true);
 		Transaction tx = datastore.beginTransaction(options);
 		datastore.delete(Key);
 		
 		if(newsFlag.equals("true")){
 			Date date = new Date();
 			Entity adminNews = new Entity(N.AdminNews);
 			adminNews.setProperty(N.blurb,String.format("%s deleted %s: %s", user.getNickname(),delete,Name));
 			adminNews.setProperty(N.date,date);
 			datastore.put(adminNews);
 		}
 		tx.commit();
 		resp.setContentType("text/plain");
 		PrintWriter writer = resp.getWriter();
 		writer.println("success");
 	}
 }
