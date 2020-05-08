 package ro.gagarin;
 
 import ro.gagarin.config.FileConfigurationManager;
 import ro.gagarin.exceptions.OperationException;
 import ro.gagarin.jdbc.JdbcDAOManager;
 import ro.gagarin.session.BasicSessionManager;
 import ro.gagarin.session.Session;
 
 /**
  * Factory class for business-logic implementation. All main sections of the
  * business logic has it's own method for retrieving the concrete implementation
  * dealing with that part of the application.
  * 
  * @author zsjoska
  * 
  */
 public class BasicManagerFactory implements ManagerFactory {
 
 	private static final BasicManagerFactory INSTANCE = new BasicManagerFactory();
 
 	private ApplicationState state = ApplicationState.INIT;
 
 	static {
 		try {
			ApplicationInitializer.init();
			INSTANCE.setApplicationState(ApplicationState.READY);
 		} catch (OperationException e) {
 			INSTANCE.setApplicationState(ApplicationState.OFFLINE);
 		}
 	}
 
 	public static ManagerFactory getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * This method returns the {@link SessionManager} implementation selected.
 	 * 
 	 * @return the configured {@link SessionManager} implementation
 	 */
 	public SessionManager getSessionManager() {
 		return BasicSessionManager.getInstance();
 	}
 
 	public DAOManager getDAOManager() {
 		return JdbcDAOManager.getInstance();
 	}
 
 	/**
 	 * This method returns the {@link ConfigurationManager} implementation
 	 * selected.
 	 * 
 	 * @return the configured {@link ConfigurationManager} implementation
 	 */
 	public ConfigurationManager getConfigurationManager(Session session) {
 		return FileConfigurationManager.getInstance();
 	}
 
 	public AuthorizationManager getAuthorizationManager(Session session) {
 		return new BasicAuthorizationManager();
 	}
 
 	public void releaseSession(Session session) {
 
 		// TODO: find a way to move this code to the sessionManager; see
 		// BasicSessionManager.destroySession(Session session)
 
 		synchronized (session) {
 			Class<?> key = BaseDAO.class;
 			Object property = session.getProperty(key);
 			session.setProperty(key, null);
 			if (property instanceof BaseDAO) {
 				((BaseDAO) property).release();
 			}
 			session.setBusy(false);
 		}
 	}
 
 	@Override
 	public ApplicationState getApplicationState() {
 		return this.state;
 	}
 
 	@Override
 	public void setApplicationState(ApplicationState state) {
 		this.state = state;
 	}
 
 }
