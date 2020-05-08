 package com.apidump;
 
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_DATABASE_GENERATION;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.spi.PersistenceUnitTransactionType;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.egit.github.core.Comment;
 import org.eclipse.egit.github.core.Gist;
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.User;
 import org.eclipse.egit.github.core.client.PageIterator;
 import org.eclipse.egit.github.core.client.RequestException;
 import org.eclipse.egit.github.core.event.Event;
 import org.eclipse.egit.github.core.service.EventService;
 import org.eclipse.egit.github.core.service.GistService;
 import org.eclipse.egit.github.core.service.RepositoryService;
 import org.eclipse.egit.github.core.service.UserService;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.persistence.config.TargetServer;
 
 import com.apidump.models.Repositories;
 
 //TODO: implement workers/producer&consumer system
 //TODO: api rate limiting and retry on 403
 //TODO: logging (log4j)
 
 public class APIDump extends HttpServlet {
 
 	private static final long serialVersionUID = -8090330511782086752L;
 	
 	private static final String PERSISTENCE_UNIT_NAME = "apidump";
 	private static EntityManagerFactory factory;
 	
 	// Generate an event list from API.
 	private static List<Event> populateEventList() throws IOException {
 		List<Event> eventList = new ArrayList<Event>();
 		EventService es = new EventService();
 		PageIterator<Event> pages = es.pagePublicEvents();
 		while (pages.hasNext()) {
 			eventList.addAll(pages.next());
 		}
 		return eventList;
 	}
 	
 	// Generate a gist list from API.
 	private static List<Gist> populateGistList() throws IOException {
 		List<Gist> gistList = new ArrayList<Gist>();
 		GistService gs = new GistService();
 		PageIterator<Gist> pages = gs.pagePublicGists();
 		int count = 0;
 		while (pages.hasNext()) {
 			//gistList.addAll(pages.next());
 			for (Gist g : pages.next()) {
 				gistList.add(gs.getGist(g.getId()));
 			}
 			count++;
 			if (count >= 1)
 				return gistList;
 		}
 		return gistList;
 	}
 	
 	// Generate a user list from API.
 	private static List<User> populateUserList() throws IOException {
 		List<User> userList = new ArrayList<User>();
 		UserService us = new UserService();
 		for (User u : us.getFollowers("jtsay")) {
 			userList.add(us.getUser(u.getLogin()));
 		}
 		return userList;
 	}
 	
 	private static List<Repository> populateRepositoryList() throws IOException {
 		List<Repository> repoList = new ArrayList<Repository>();
 		RepositoryService rs = new RepositoryService();
 		//repoList.add(rs.getRepository("mootools", "mootools-core"));
 		repoList.add(rs.getRepository("octocat", "hello-world"));
 		return repoList;
 	}
 	
 	private static List<Comment> getGistCommentList(Gist g) throws IOException {
 		List<Comment> commentList = new ArrayList<Comment>();
 		GistService gs = new GistService();
 		commentList.addAll(gs.getComments(g.getId()));
 		return commentList;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private static Map getProperties() {
 		Map properties = new HashMap();
 		URI dbUrl = URI.create(System.getenv("DATABASE_URL"));
 		properties.put(TRANSACTION_TYPE,
 		        PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
 		properties.put(JDBC_DRIVER, "org.postgresql.Driver");
 	    properties.put(JDBC_URL, "jdbc:postgresql://" + dbUrl.getHost() + dbUrl.getPath());
 	    properties.put(JDBC_USER, dbUrl.getUserInfo().split(":")[0]);
 	    properties.put(JDBC_PASSWORD, dbUrl.getUserInfo().split(":")[1]);
 	    
 	    properties.put(DDL_DATABASE_GENERATION, "drop-and-create-tables");
 	    properties.put(TARGET_SERVER, TargetServer.None);
 	    return properties;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private static void runDump() throws IOException {
 		Map properties = getProperties();
 		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
 		EntityManager em = factory.createEntityManager();
 		
 		// Extract user model classes from API
 		//List<Event> eventList = populateEventList();
 		//List<Gist> gistList = populateGistList();
 		//List<User> userList = populateUserList();
 		List<Repository> repoList = populateRepositoryList();
 
 		//for (Event e : eventList)
 		//for (Gist g : gistList)
 		//for (User u : userList)
 		for (Repository r : repoList)
 		{
 			
 			try {
 				em.getTransaction().begin();
 				//Events ev = new Events(e);
 				//Gists gi = new Gists(g);
 				//Users us = new Users(u);
 				Repositories re = new Repositories(r);
 				
 				//em.merge(ev);
 				//em.merge(gi);
 				//em.merge(us);
 				em.merge(re);
 				
 			} catch (RequestException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				if (e1.getStatus() == 404) {
 					// continue;
 				} else if (e1.getStatus() == 403) {
 					// need to delay and retry or something
 				}
 			} finally {
 			
 				em.flush();
 				
 				if (em.getTransaction().getRollbackOnly())
 					em.getTransaction().rollback();
 				else
 					em.getTransaction().commit();
 			}
 			
 		}
 		
 		/*
 		for (APIModel m : modelList)
 			em.persist(m);
 			*/
 		
 		//System.out.println("flushing to database");
 
 		em.close();
 		
 		System.out.println("API Dump complete");
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		runDump();
 		resp.getWriter().print("API Dump complete\n");
 	}
 	
 	public static void main(String[] args) throws Exception {
 		Server server = new Server(Integer.valueOf(System.getenv("PORT")));
 		ServletContextHandler context = new ServletContextHandler(
 				ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 		server.setHandler(context);
 		context.addServlet(new ServletHolder(new APIDump()), "/*");
 		server.start();
 		server.join();
 	}
 }
