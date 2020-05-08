 package org.otherobjects.cms.jcr;
 
 import javax.jcr.Credentials;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.otherobjects.cms.security.SecurityUtil;
 import org.springmodules.jcr.jackrabbit.JackrabbitSessionFactory;
 
 /**
  * This class overrides {@link JackrabbitSessionFactory} / {@link org.springmodules.jcr.JcrSessionFactory} respectively just to allow for 
  * creating sessions for different workspaces depending on the role(s) the current user has.
  * It also adds a getSession(String workspaceName) method to allow for explicitly obtaining sessions for specific workspace. 
  * @author joerg
  *
  */
 public class OtherObjectsJackrabbitSessionFactory extends JackrabbitSessionFactory
 {
 
     public static final String LIVE_WORKSPACE_NAME = "live";
     public static final String EDIT_WORKSPACE_NAME = "default";
     
     // HACK bad - this bean is getting initialised twice but it should be a singleton
     private Session liveSession;
     private Session defaultSession;
     
     private String workspaceName;
 
     private Credentials credentials;
 
     @Override
     public Session getSession() throws RepositoryException
     {
         if(SecurityUtil.isEditor()) {
            if(liveSession == null || !liveSession.isLive()) {
                liveSession = getSession(LIVE_WORKSPACE_NAME);;
            }
            return liveSession;
        }
        else {
             if(defaultSession == null || !defaultSession.isLive()) {
                 defaultSession = getSession(EDIT_WORKSPACE_NAME);
             }
             return defaultSession;
         }
     }
 
     public Session getSession(String workspaceName) throws RepositoryException
     {
         //FIXME Is this a performance issues? 
         return addListeners(getRepository().login(credentials, workspaceName));
     }
 
 
     @Override
     public String getWorkspaceName()
     {
         return workspaceName;
     }
 
     @Override
     public void setWorkspaceName(String workspaceName)
     {
         super.setWorkspaceName(workspaceName);
         this.workspaceName = workspaceName;
     }
 
     @Override
     public Credentials getCredentials()
     {
         return credentials;
     }
 
     @Override
     public void setCredentials(Credentials credentials)
     {
         super.setWorkspaceName(workspaceName);
         this.credentials = credentials;
     }
 
 }
