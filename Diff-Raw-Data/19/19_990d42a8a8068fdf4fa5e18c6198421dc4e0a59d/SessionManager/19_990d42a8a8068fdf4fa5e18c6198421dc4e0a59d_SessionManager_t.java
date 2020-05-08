 package com.mymed.controller.core.manager.session;
 
 import java.util.Map;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.model.data.user.MUserBean;
import com.mymed.utils.MyMedLogger;
 
 /**
  * Manage the session of a user
  * 
  * @author lvanni
  * @author Milo Casagrande
  * 
  */
 public class SessionManager extends AbstractManager implements ISessionManager {
 
 	private static final String SESSION_SUFFIX = "_SESSION";
 
 	public SessionManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 
 	public SessionManager(final StorageManager storageManager) throws InternalBackEndException {
 		super(storageManager);
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see ISessionManager#create(String, String)
 	 */
 	@Override
 	public void create(final String userID, final String ip) throws InternalBackEndException, IOBackEndException {
 		final MSessionBean sessionBean = new MSessionBean();
 		sessionBean.setIp(ip);
 		sessionBean.setUser(userID);
 		sessionBean.setCurrentApplications("");
 		sessionBean.setP2P(false);
 		sessionBean.setTimeout(System.currentTimeMillis());
 		create(sessionBean);
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see ISessionManager#create(String, String)
 	 */
 	public void create(final MSessionBean sessionBean) throws InternalBackEndException, IOBackEndException {
 		if (sessionBean.getId() == null) {
 			sessionBean.setId(sessionBean.getUser() + SESSION_SUFFIX);
 		}
 
		MyMedLogger.getLog().info("Creating new session with ID {} for user {}", sessionBean.getId(),
		        sessionBean.getUser());
 		storageManager.insertSlice(CF_SESSION, sessionBean.getId(), sessionBean.getAttributeToMap());
 
 		final ProfileManager profileManager = new ProfileManager(storageManager);
 		final MUserBean user = profileManager.read(sessionBean.getUser());
 
 		user.setSession(sessionBean.getId());
 		profileManager.update(user);
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see ISessionManager#read(String)
 	 */
 	@Override
 	public MSessionBean read(final String userID) throws InternalBackEndException, IOBackEndException {
 
		MyMedLogger.getLog().info("Reading session for user with ID: {}", userID);
 
 		final ProfileManager profileManager = new ProfileManager(storageManager);
 		final MUserBean user = profileManager.read(userID);
 		final MSessionBean session = new MSessionBean();
 		final Map<byte[], byte[]> args = storageManager.selectAll(CF_SESSION, user.getSession());
 
 		return (MSessionBean) introspection(session, args);
 	}
 
 	/**
 	 * @throws IOBackEndException
 	 * @see ISessionManager#update(MSessionBean)
 	 */
 	@Override
 	public void update(final MSessionBean session) throws InternalBackEndException, IOBackEndException {
 		create(session);
 	}
 
 	/**
 	 * @throws ServiceManagerException
 	 * @see ISessionManager#delete(String)
 	 */
 	@Override
 	public void delete(final String userID) throws InternalBackEndException {
		MyMedLogger.getLog().info("Deleting session for user with ID: {}", userID);
 
 		storageManager.removeAll(CF_SESSION, userID + SESSION_SUFFIX);
 	}
 }
