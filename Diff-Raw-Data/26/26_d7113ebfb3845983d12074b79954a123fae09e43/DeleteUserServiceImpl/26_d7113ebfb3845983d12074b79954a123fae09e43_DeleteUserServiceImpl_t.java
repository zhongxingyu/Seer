 package org.timadorus.webapp.server.rpc.service;
 
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Extent;
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
 
 import org.timadorus.webapp.client.User;
 import org.timadorus.webapp.client.rpc.service.DeleteUserService;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class DeleteUserServiceImpl  extends RemoteServiceServlet implements DeleteUserService {
 
   private static final long serialVersionUID = -2215579735797066083L;
   
   public static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
 
   public String delete(User user) {
     PersistenceManager pm = PMF.getPersistenceManager();
    
    Extent<User> extent = pm.getExtent(User.class, true);

    Query query = pm.newQuery(extent, "username == name");
    query.declareParameters("String name");
    
    User found = null;

    Iterator<User> iterator = ((Collection<User>) query.execute(user.getUsername())).iterator();
    if (iterator.hasNext()) {
      found = iterator.next();      
    }
    
    pm.deletePersistent(found);
    
    System.out.println(found.getDisplayname() + " has been deleted from database!");
    
     return String.valueOf(0);
   }
 
 }
