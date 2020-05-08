 package org.vaadin.securityauction.server;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.Query;
 import javax.servlet.annotation.WebServlet;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 import org.vaadin.securityauction.shared.AuctionItem;
 import org.vaadin.securityauction.shared.AuctionService;
 import org.vaadin.securityauction.shared.User;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 @WebServlet(urlPatterns = { "/SecurityAuction/AuctionService" })
 public class AuctionServiceImpl extends RemoteServiceServlet implements
         AuctionService {
 
    @PersistenceUnit
     private EntityManagerFactory factory;
 
     @Override
     public User authenticate(String username, String password) {
         Subject currentUser = SecurityUtils.getSubject();
         UsernamePasswordToken token = new UsernamePasswordToken(username,
                 password);
         EntityManager entityManager = null;
         try {
             currentUser.login(token);
             entityManager = factory.createEntityManager();
             Query query = entityManager
                    .createQuery("SELECT u FROM User u WHERE username = :username");
             query.setParameter("username", username);
 
             // Execute query and return result
             return (User) query.getSingleResult();
         } catch (Exception e) {
             Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
             return null;
         } finally {
             if (entityManager != null) {
                 entityManager.close();
             }
         }
     }
 
     @Override
     public List<AuctionItem> getAuctionItems(int firstItem, int itemCount) {
         // Waiting for a database
         return Arrays.asList(new AuctionItem(1, "My soul", "It's delicious"),
                 new AuctionItem(2, "My phone", "It's old"));
     }
 
     @Override
     public AuctionItem getAuctionItem(int id) {
         if (id == 1 || id == 2) {
             return getAuctionItems(0, 2).get(id - 1);
         } else {
             return null;
         }
     }
 
 }
