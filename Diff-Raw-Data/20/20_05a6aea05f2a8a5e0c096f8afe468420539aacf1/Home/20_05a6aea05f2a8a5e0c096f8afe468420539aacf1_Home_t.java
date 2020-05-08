 package org.northstar.bricks.web.pages;
 
 import com.google.inject.Inject;
 import com.google.sitebricks.rendering.Decorated;
 import org.northstar.bricks.core.dao.UserDao;
 import org.northstar.bricks.core.domain.User;
 import org.northstar.bricks.web.components.Decorator;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 @Decorated
 public class Home extends Decorator {
 
     private List<User> pagedUsers;
     private int page;
    private Integer maxPerPage = 6;
     private final UserDao dao;
 
     @Inject
     private Logger logger;
 
     @Inject
     Home(UserDao dao) {
         this.dao = dao;
     }
 
     public int getPage() {
         return page;
     }
 
     public void setPage(int page) {
         this.page = page;
     }
 
     public List<User> getPagedUsers() {
         long startIndex = 0;
         if (page < 1) {
             page = 1;
         }
         startIndex = (page - 1) * maxPerPage - 1;
         return dao.findPagedUsers(startIndex, maxPerPage);
     }
 
     public String getPageTitle() {
         return "主页";
     }
 
     public int getMaxPerPage() {
         return maxPerPage;
     }
 
     public int getMaxResults() {
         return dao.getUserCounts();
     }
 
 }
