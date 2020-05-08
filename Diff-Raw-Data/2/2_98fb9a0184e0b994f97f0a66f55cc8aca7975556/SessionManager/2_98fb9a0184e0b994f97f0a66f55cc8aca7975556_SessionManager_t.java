 package com.mymed.controller.core.manager.session;
 
 import java.util.Map;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.model.data.user.MUserBean;
 
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
   @Deprecated
   public void create(final String userID, final String ip) throws InternalBackEndException, IOBackEndException {
     final MSessionBean sessionBean = new MSessionBean();
    sessionBean.setId(userID + SESSION_SUFFIX);
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
   @Override
   public void create(final MSessionBean sessionBean) throws InternalBackEndException, IOBackEndException {
 
     if (sessionBean.getId() == null) {
       throw new InternalBackEndException("The session id is null!");
     }
 
     LOGGER.info("Creating new session with ID '{}' for user '{}'", sessionBean.getId(), sessionBean.getUser());
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
   public MSessionBean read(final String sessionID) throws InternalBackEndException, IOBackEndException {
 
     MSessionBean session = new MSessionBean();
     final Map<byte[], byte[]> args = storageManager.selectAll(CF_SESSION, sessionID);
     session = (MSessionBean) introspection(session, args);
 
     if (session.isExpired()) {
       throw new IOBackEndException("Session expired!", 404);
     }
 
     return session;
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
    * @throws IOBackEndException
    * @throws ServiceManagerException
    * @see ISessionManager#delete(String)
    */
   @Override
   public void delete(final String sessionID) throws InternalBackEndException, IOBackEndException {
     LOGGER.info("Deleting session for user with ID: {}", sessionID);
     final MSessionBean session = read(sessionID);
     session.setExpired(true);
     update(session);
 
     // Removed after 10 days
     storageManager.removeAll(CF_SESSION, sessionID + SESSION_SUFFIX);
   }
 }
