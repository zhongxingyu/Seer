 package model;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.mybeans.dao.DAOException;
 
 import databean.User;
 
 public class Model {
 
 	
 	private UserDAO userDAO;
 	private ItemDAO itemDAO;
 	private MessageDAO messageDAO;
 	private ExchangeDAO exchangeDAO;
 	private CommunityDAO communityDAO;
 	private TopicDAO topicDAO;
 	private PostDAO postDAO;
 	private RelationDAO relationDAO;
 	
 	public Model(ServletConfig config) throws ServletException {
 		String jdbcDriver = config.getInitParameter("jdbcDriver");
 		String jdbcURL    = config.getInitParameter("jdbcURL");
 		
 		try {
 			userDAO  = new UserDAO(jdbcDriver, jdbcURL);
 			itemDAO = new ItemDAO(jdbcDriver, jdbcURL, userDAO);
 			messageDAO = new MessageDAO(jdbcDriver, jdbcURL, userDAO);
 			exchangeDAO = new ExchangeDAO(jdbcDriver, jdbcURL, userDAO, itemDAO);
 			communityDAO = new CommunityDAO(jdbcDriver, jdbcURL, userDAO);
 			topicDAO = new TopicDAO(jdbcDriver, jdbcURL, userDAO, communityDAO);
 			postDAO = new PostDAO(jdbcDriver, jdbcURL, userDAO, topicDAO);
 			relationDAO = new RelationDAO(jdbcDriver, jdbcURL, userDAO, communityDAO);
 			
 		} catch (DAOException e) {
 			// TODO Auto-generated catch block
 			throw new ServletException(e);
 		}		
 		try {
 			User admin = new User("Admin");
 			admin.setPassword("w");
 			userDAO.create(admin);
 			
 		} catch(DAOException e) {
 		}
 	}
 
 	public UserDAO getUserDAO() {
 		return userDAO;
 	}
 
 	public ItemDAO getItemDAO() {
 		return itemDAO;
 	}
 
 	public MessageDAO getMessageDAO() {
 		return messageDAO;
 	}
 	
 	public ExchangeDAO getExchangeDAO() {
 		return exchangeDAO;
 	}
 	
 	public CommunityDAO getCommunityDAO() {
 		return communityDAO;
 	}
 	
 	public TopicDAO getTopicDAO() {
 		return topicDAO;
 	}
 	
 	public PostDAO getPostDAO() {
 		return postDAO;
 	}
 	
 	public RelationDAO getRelationDAO() {
 		return relationDAO;
 	}
 }
