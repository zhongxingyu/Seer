 package webmenu.data;
 
import java.util.Date;
 import java.util.logging.Logger;
 import java.text.SimpleDateFormat;
 import javax.jdo.*;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
import webmenu.model.OneDayMenu;
 
 /// Implementation of OneDayMenuStore using Google data-store API
 public class GoogleOneDayMenuStore implements OneDayMenuStore
 {
     private static final Logger log = Logger.getLogger(GoogleOneDayMenuStore.class.getName());
     private static int NUM_RETRIES = 5;
 
     static String createId(String restaurant, Date day)
     {
         StringBuilder idBuilder = new StringBuilder();
         idBuilder.append(restaurant);
 
         idBuilder.append(":");
 
         SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
         idBuilder.append(fmt.format(day));
 
         return idBuilder.toString();
     }
 
     static PersistenceManager getPersistenceManager()
     {
         return PMF.get().getPersistenceManager();
     }
 
     static Key createKey(String id)
     {
         return KeyFactory.createKey(OneDayMenu.class.getSimpleName(), id);
     }
 
     public void updateOneDayMenu(String restaurant, OneDayMenu menu)
     {
         String id = createId(restaurant, menu.getDay());
         Key key = createKey(id);
         menu.setKey(key);
 
         for (int retry = 0; retry < NUM_RETRIES; ++retry)
         {
             PersistenceManager pm = getPersistenceManager();
             Transaction tx = pm.currentTransaction();
 
             try {
                 tx.begin();
 
                 try
                 {
                     OneDayMenu old = pm.getObjectById(OneDayMenu.class, key);
                     old.update(menu);
                 }
                 catch (JDOObjectNotFoundException e)
                 {
                     pm.makePersistent(menu);
                 }
 
                 tx.commit();
                 log.info("OneDayMenu '" + id + "' has been updated.");
                 break;
             } catch (JDOCanRetryException e) {
                 if (retry == NUM_RETRIES-1)
                     throw e;
             } finally {
                 if (tx.isActive())
                     tx.rollback();
                 pm.close();
             }
         }
     }
     
     public OneDayMenu getOneDayMenu(String restaurant, Date day)
     {
        Key key = createKey(createId(restaurant, day));
        PersistenceManager pm = getPersistenceManager();
 
        try
        {
           OneDayMenu menu = pm.getObjectById(OneDayMenu.class, key);
           OneDayMenu copy = pm.detachCopy(menu);
          log.fine("[copy] soups: " + copy.getSoupItems() + " meals: " + copy.getMenuItems());
           return copy;
        }
        catch (JDOObjectNotFoundException e)
        {
           return null;
        }
        finally
        {
           pm.close();
        }
     }
 }
