 package nz.co.searchwellington.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.HibernateBackedUserDAO;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 @Component
@Scope("request")
 public class LoggedInUserFilter {
 	
 	private static Logger log = Logger.getLogger(LoggedInUserFilter.class);
 	
 	private User loggedInUser;
 	private HibernateBackedUserDAO userDAO;
 		
 	public LoggedInUserFilter() {
 	}
 
 	@Autowired
 	public LoggedInUserFilter(HibernateBackedUserDAO userDAO) {	
 		this.loggedInUser = null;
 		this.userDAO = userDAO; 
 	}
 	
 	public void loadLoggedInUser(HttpServletRequest request) {		 
 		if (request.getParameter("apikey") != null) {
 			final String apiKey = request.getParameter("apikey");
 			log.debug("Looking for an user by api key: " + apiKey);
 			User user = userDAO.getUserByApiKey(apiKey);
 			if (user != null) {
 				log.info("Found user by api key: " + user.getName());
 				loggedInUser = user;
 				return;
 			}
 		}
 		
 		log.debug("Looking for logged in user in session");
 		if (request.getSession().getAttribute("user") != null) {
 			User sessionUser =  (User) request.getSession().getAttribute("user");
 			log.info("Found user on session: " + sessionUser.getName());
 			loggedInUser = sessionUser;
 			return;
 		}
 		
 		loggedInUser = null;
 	}
 	
 	public User getLoggedInUser() {
 		return loggedInUser;
 	}
 
 	public void setLoggedInUser(HttpServletRequest request, User user) {
 		request.getSession().setAttribute("user", user);
 	}
 	
 }
