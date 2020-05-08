 package org.timadorus.webapp.server.rpc.service;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 
 import org.timadorus.webapp.client.User;
 import org.timadorus.webapp.client.rpc.service.DeleteUserService;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class DeleteUserServiceImpl  extends RemoteServiceServlet implements DeleteUserService {
 
   private static final long serialVersionUID = -2215579735797066083L;
   
   public static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
 
   public String delete(User user) {
     PersistenceManager pm = PMF.getPersistenceManager();
    pm.deletePersistent(user);
    System.out.println(user + " has been deleted from database");
     return String.valueOf(0);
   }
 
 }
