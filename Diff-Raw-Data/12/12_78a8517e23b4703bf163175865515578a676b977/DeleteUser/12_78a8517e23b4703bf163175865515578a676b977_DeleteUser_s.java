 package org.northstar.bricks.web.service;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.sitebricks.headless.Reply;
 import com.google.sitebricks.http.Get;
 import org.northstar.bricks.core.dao.UserDao;
 import org.northstar.bricks.core.domain.User;
 
 /**
  * Created with IntelliJ IDEA.
  * User: northstar
  * Date: 12-9-25
  * Time: 上午11:03
  * To change this template use File | Settings | File Templates.
  */
 public class DeleteUser {
     @Inject
     private UserDao userDao;
 
     @Get
     public Reply remove(@Named("id") Long id) {
         User user = userDao.findById(id);
         userDao.delete(user);
         return Reply.saying().redirect("/");
     }
 }
