 package com.shopservice;
 
 import com.shopservice.dao.*;
 import com.shopservice.domain.ClientSettings;
 
 import java.sql.SQLException;
 import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: neuser50
  * Date: 26.08.13
  * Time: 13:27
  * To change this template use File | Settings | File Templates.
  */
 public class Services {
 
     private static HashMap<String, DatabaseManager> databaseManagers = new HashMap<String, DatabaseManager>();
 
    private static Map<String, CategoryDAO> categoryDAOs = new Hashtable<String, CategoryDAO>();
    private static Map<String, ProductDAO> productDAOs = new Hashtable<String, ProductDAO>();
 
     public static final PriceListService priceListService = new PriceListService();
 
     public static final Queries queries = new Queries();
 
     public static DatabaseManager getDataBaseManager(String clientId) throws SQLException {
         if (!databaseManagers.containsKey(clientId))
             databaseManagers.put(clientId, new DatabaseManager(new StupidConnectionPool(ClientSettings.findById(clientId).databaseUrl)));
 
         return databaseManagers.get(clientId);
     }
 
     public static CategoryDAO getCategoryDAO(String clientId)
     {
        if (!categoryDAOs.containsKey(clientId))
             categoryDAOs.put( clientId, new CachedCategoryDAO(new JdbcCategoryDAO(clientId)));
 
         return categoryDAOs.get(clientId);
     }
 
     public static ProductDAO getProductDAO(String clientId)
     {
        if (!productDAOs.containsKey(clientId))
             productDAOs.put( clientId, new CachedProductDAO(new JdbcProductDAO(clientId)));
 
         return productDAOs.get(clientId);
     }
 }
