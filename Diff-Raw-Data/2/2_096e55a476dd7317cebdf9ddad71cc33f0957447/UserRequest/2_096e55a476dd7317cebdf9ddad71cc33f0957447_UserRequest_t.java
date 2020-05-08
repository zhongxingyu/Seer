 /**
  * 
  */
 package org.mklab.taskit.shared;
 
import org.mklab.taskit.server.domain.User;
 
 import com.google.web.bindery.requestfactory.shared.Request;
 import com.google.web.bindery.requestfactory.shared.RequestContext;
 import com.google.web.bindery.requestfactory.shared.Service;
 
 
 /**
  * @author ishikura
  */
 @Service(User.class)
 @SuppressWarnings("javadoc")
 public interface UserRequest extends RequestContext {
 
   Request<UserProxy> getUserByAccountId(String accountId);
 
 }
