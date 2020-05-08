 package org.thingswedo.exchange2glass;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class SaveSettingsServlet extends HttpServlet {
 	public static final String SINGLE_SETTING_ENTOTY_NAME = "SingleSetting";
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String username = req.getParameter("username");
 		String password = req.getParameter("password");
 		String exchnage = req.getParameter("exchange");
 		String interval = req.getParameter("interval");
 
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		DatastoreService datastoreService = DatastoreServiceFactory
 				.getDatastoreService();
 		Filter f = new FilterPredicate("user", FilterOperator.EQUAL, user.getUserId());
 		Query q = new Query(SINGLE_SETTING_ENTOTY_NAME).setFilter(f);
 		Entity settings = datastoreService.prepare(q).asSingleEntity();
 
 		if (settings == null) {
 			settings = new Entity(SINGLE_SETTING_ENTOTY_NAME);
 			settings.setProperty("user", user.getUserId());
 		}
 		settings.setProperty("username", username);
 		settings.setProperty("password", Utils.encodePassword(password));
 		settings.setProperty("exchange", exchnage);
 		settings.setProperty("interval", interval);
 
 		datastoreService.put(settings);
 
 		resp.sendRedirect("/info.jsp");
 	}
 }
