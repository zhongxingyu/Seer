 package org.sidl.herenow;
 
 import org.sidl.herenow.db.*;
 import org.sidl.herenow.foursq.FoursquareProvider;
 import org.sidl.herenow.search.DefaultSearchEngine;
 import org.sidl.herenow.search.FileSearchMethod;
 import org.sidl.herenow.search.SearchProvider;
 import org.sidl.herenow.search.URLSearchMethod;
 import org.sidl.herenow.util.CSVFile;
 import org.sidl.herenow.util.ClientKeysFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.util.*;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The main driver class.
  */
 public class HereNow
 {
 
   public static void main(String... s)
       throws IOException
   {
     String dbConfigFile = s[0];
     String centroidFile = s[1];
     String keyFile      = s[2];
 
     config("Start time:      %d", System.currentTimeMillis());
     config("DB Config file:  %s", dbConfigFile);
     config("Centroid file:   %s", centroidFile);
     config("Client key file: %s", keyFile);
     
     CSVFile centroids = new CSVFile(centroidFile);
     config("Loaded  %d points from %s", centroids.size(), centroidFile);
     
     if (centroids.size() == 0)
     {
       LOG.warning("No search points found, ending search now...");
       System.exit(SEVERE_ERROR);
     }
 
     CSVFile keys = new CSVFile(keyFile);
     ClientKeysFile ckf = new ClientKeysFile(keys);
     config("Loaded %d client IDs from %s", ckf.size(), keyFile);
 
     if (ckf.isEmpty())
     {
       LOG.warning("No client IDs and keys found, ending search now...");
       System.exit(SEVERE_ERROR);
     }
 
 
     Map<String,String> ids = ckf.getClientKeys();
     List<SearchProvider> clientEndpoints = new ArrayList<SearchProvider>();
     
     for (Map.Entry<String,String> entry : ids.entrySet())
     {
       SearchProvider sp = new FoursquareProvider(entry.getKey(), entry.getValue());
       clientEndpoints.add(sp);
     }
 
     //(new HereNow()).addShutdownHook().runSimple(dbConfigFile, centroids);
     (new HereNow()).addShutdownHook().runWeb(dbConfigFile, centroids, clientEndpoints);
 
   }
 
   
   private void runWeb(String dbConfigFile, CSVFile centroids, List<SearchProvider> endpoints)
   {
     int maxOutstandingRequests = 100;
 
     final DBConfig config;
     
     try
     {
       config = DBConfigFactory.loadConfig(new File(dbConfigFile));
     }
     catch (IOException ioe)
     {
       LOG.log(Level.SEVERE, "Cannot load db file" + dbConfigFile, ioe);
       return;
     }
 
     final DBConnection connection = create(config);
 
     try
     {
       if (connection == null || !isRunning.get())
       {
         isRunning.set(false);
         return;
       }
 
       cleanTable(connection, config.getTableName());
       logTable(connection, config.getTableName());
 
       executeWebQueries(centroids, endpoints, connection, config.getTableName(),
           maxOutstandingRequests);
     }
     finally
     {
       if (connection != null && !connection.close())
         LOG.log(Level.WARNING, "Close error", connection.getLastException());
     }
   }
 
 
   private void executeWebQueries(CSVFile centroids, List<SearchProvider> endpoints,
                                  DBConnection connection, String tableName,
                                  int maxOutstandingRequests)
   {
     boolean backoff;
     BlockingQueue<VenueResult> queue = new ArrayBlockingQueue<VenueResult>(maxOutstandingRequests);
     DefaultSearchEngine engine = new DefaultSearchEngine();
     Random rnd = new Random(System.currentTimeMillis());
     Map<SearchProvider,AtomicInteger> map = new HashMap<SearchProvider, AtomicInteger>();
     
     for (SearchProvider sp : endpoints)
     {
       map.put(sp, new AtomicInteger(1));
     }
     
     engine.registerSearchResultQueue(queue);
 
     while (isRunning.get())
     {
       backoff = false;
       Iterator<String> locations = centroids.iterator();
 
       while (locations.hasNext() && isRunning.get())
       {
        int count = 0;
 
         while (count < maxOutstandingRequests && locations.hasNext() && isRunning.get())
         {
           count++;
           SearchProvider sp = getRandomAvailableProvider(map);
           if (sp != null)
           {
             backoff = false;
             engine.searchByCentroid(new URLSearchMethod(sp), locations.next());
           }
           else
           {
             backoff = true;
             break;
           }
         }
 
         if (isRunning.get())
         {
           processQueue(connection, tableName, map, queue);
 
           if (backoff)
           {
             LOG.info("Backing off - sleep for 1 to 3 minutes");
             randomSleep(rnd, 60000, 180000);
             for (SearchProvider sp : map.keySet())
             {
               engine.searchByCentroid(new URLSearchMethod(sp), locations.next());
             }
           }
           else
           {
             randomSleep(rnd, 10000, 30000);
           }
         }
 
        LOG.info(count + " of " + centroids.size() + " points searched");
         engine.logStats(LOG);
       }
 
     }
     
     LOG.info("Closing engine down");
     engine.deregisterSearchResultQueue(queue);
     queue.clear();
   }
 
 
   private static SearchProvider getRandomAvailableProvider(Map<SearchProvider,AtomicInteger> map)
   {
     List<SearchProvider> providers = new ArrayList<SearchProvider>(map.keySet());
     Collections.shuffle(providers);
     
     SearchProvider found = null;
     
     for (SearchProvider provider : providers)
     {
       if (map.get(provider).get() > 0)
       {
         found = provider;
         break;
       }
     }
 
     LOG.log(Level.FINE, "using provider: " + found);
     return found;
   }
 
   private static void randomSleep(Random rnd, int min, int max)
   {
     sleep(Math.max(rnd.nextInt(max), min));
   }
     
   private static void sleep(long time)
   {
     LOG.log(Level.INFO, "Sleeping for " + time);
     final long end = System.currentTimeMillis() + time;
     while (System.currentTimeMillis() < end)
     {
       try
       {
         Thread.sleep(time);
       }
       catch (InterruptedException ie)
       {
         break;
       }
     }
   }
   
   private void processQueue(DBConnection connection, String tableName,
     Map<SearchProvider,AtomicInteger> providers, BlockingQueue<VenueResult> results)
   {
     VenueResult result;
     try
     { 
       while ((result = results.poll(1, TimeUnit.SECONDS)) != null)
       {
         if (!result.isError())
           writeOneResult(connection, tableName, result);
         else
         {
           // for now log the error, but really should pull the provider out
           LOG.log(Level.WARNING, String.format("Provider: %s  Available: %d Response Msg: %s  code: %d",
               result.getSourceId(), result.getQueriesAvailable(),
               result.getResponseMessage(), result.getResponseCode()));
         }
         updateSearchAvailable(providers, result);
       }
     }
     catch (Exception e)
     {
       stop("Result processing interrupted", e);
     }
   }
 
   private void updateSearchAvailable(Map<SearchProvider, AtomicInteger> providers, VenueResult result)
   {
     for (SearchProvider provider : providers.keySet())
     {
       if (result.getSourceId().equals(provider.getId()))
       {
         LOG.log(Level.INFO, String.format("Updating %s searches left: %d",
             provider.getId(), result.getQueriesAvailable()));
         providers.get(provider).set(result.getQueriesAvailable());
       }
     }
   }
 
 
   private void stop(String message, Exception e)
   {
     isRunning.set(false);
     LOG.log(Level.SEVERE, message, e);
   }
 
   private void runSimple(String dbConfigFile, CSVFile centroids)
   {
     String location = centroids.iterator().next();
 
     final DBConfig config;
 
     try
     {
       config = DBConfigFactory.loadConfig(new File(dbConfigFile));
     }
     catch (IOException ioe)
     {
       LOG.log(Level.SEVERE, "Cannot load db file" + dbConfigFile, ioe);
       return;
     }
     
     final DBConnection connection = create(config);
 
     try
     {
       if (connection == null || !isRunning.get())
       {
         isRunning.set(false);
         return;
       }
       
       if (cleanTable(connection, config.getTableName()))
         executeFileQuery(location, connection, config.getTableName());
     }
     finally
     {
       if (connection != null && !connection.close())
         LOG.log(Level.WARNING, "Close error", connection.getLastException());
     }
   }
 
   private HereNow addShutdownHook()
   {
     Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
     {
       @Override
       public void run()
       {
         LOG.info("Shutting down");
         isRunning.set(false);
         sleep(10000);
       }
     }));
     return this;
   }
 
   private void executeFileQuery(String location, DBConnection connection, String tableName)
   {
     BlockingQueue<VenueResult> queue = new ArrayBlockingQueue<VenueResult>(1);
     DefaultSearchEngine engine = new DefaultSearchEngine();
 
     // engine delivers results to this queue
     engine.registerSearchResultQueue(queue);
 
     // kick off the engine for one search
     engine.searchByCentroid(new FileSearchMethod(new TestFSQResponse()), location);
 
     // blocking wait!
     VenueResult result;
     try
     {
       result = queue.take();
     }
     catch (InterruptedException ie)
     {
       ie.printStackTrace();
       isRunning.set(false);
       return;
     }
 
     if (result != null)
       config("Result: %s", result.toString());
     else
       config("Empty result");
 
     // write out the result to the DB connection
     try
     {  
       writeOneResult(connection, tableName, result);
     }
     catch (SQLException e)
     {
       LOG.log(Level.WARNING, "Error writing result", e);
     }
 
     // shut it down
     engine.deregisterSearchResultQueue(queue);
   }
 
   private static boolean cleanTable(DBConnection connection, String tableName)
   {
     try 
     {    
       DBStatementWriter table = new DBStatementWriter(connection, tableName);
       table.resetTable();
       table.logMetaData();
       return true;
     }
     catch (SQLException sql)
     {
       LOG.log(Level.WARNING, "Error writing to table", sql);
       return false;
     }
   }
 
   private static boolean logTable(DBConnection connection, String tableName)
   {
     try
     {
       DBStatementWriter table = new DBStatementWriter(connection, tableName);
       table.logMetaData();
       return true;
     }
     catch (SQLException sql)
     {
       LOG.log(Level.WARNING, "Error reading from table", sql);
       return false;
     }
   }
   
   private static DBConnection create(DBConfig config)
   {
     DBConnection connection;
     try
     {
       connection = new DBConnectionImpl(config);
       
       if (!connection.connect())
       {
         LOG.log(Level.SEVERE, "Failed to open DB Connection to: " + config.getURL(),
             connection.getLastException());
         connection.close();
         System.exit(SEVERE_ERROR);
       }
       
       return connection;
     }
     catch (Exception e)
     {
       LOG.log(Level.WARNING, "Error with reading/writing to db_file", e);
       return null;
     }
   }
     
   private void writeOneResult(DBConnection connection, String tableName, VenueResult result)
       throws SQLException
   {
     List<Venue> values;
     long timestamp = System.currentTimeMillis();
 
     if (result == null || result.getVenues() == null || result.getVenues().isEmpty())
     {
       return;
     }
 
     values = new ArrayList<Venue>(result.getVenues().size());
     
     for (Venue v : result.getVenues())
       if (v.getHereNowCount() > 0)
         values.add(v);
     
     if (!values.isEmpty())
     {
       int records = writeVenuesToDB(connection, tableName, timestamp, values);
       totalRecords += records;
      LOG.log(Level.INFO, "Records just written: " + records + "  Total Records: " + totalRecords);
     }
   }
 
   private static int writeVenuesToDB(DBConnection connection, String tableName,
                                      long timestamp, List<Venue> values)
       throws SQLException
   {
     int total = 0;
     DBStatementWriter writer = new DBStatementWriter(connection, tableName);
     int[] update =  writer.write(values, timestamp);
     for (int i : update) total += i;
     return total;
   }
 
   private static class TestFSQResponse extends FoursquareProvider
   {
     @Override
     public URI getURIForLocation(String location)
     {
       try
       {
         //todo - test this
         return new URI("file://" + System.getProperty("user.dir") + "/full_test_response.json");
         //return new URI("file:///Users/bryan/Code/HereNow/code/java/full_test_response.json");
       }
       catch (URISyntaxException e)
       {
         e.printStackTrace();
         return null;
       }
     }
   }
   
   private static void config(String format, Object... params)
   {
     LOG.info(String.format(format, params));
   }
   
   
  private AtomicBoolean isRunning = new AtomicBoolean(true);
 
   private int totalRecords = 0;
   
   private static Logger LOG = Logger.getLogger(HereNow.class.getName());
   
   private static int SEVERE_ERROR = 1;
 
 }
