 package org.sakaiproject.nakamura.lite.soak.cassandra;
 
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
 import org.sakaiproject.nakamura.lite.soak.authorizable.CreateUsersAndGroupsClient;
 import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
 import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
 import org.sakaiproject.nakamura.lite.storage.StorageClientException;
 import org.sakaiproject.nakamura.lite.storage.cassandra.CassandraClientConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 
 public class CreateUsersAndGroupsSoak extends AbstractSoakController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateUsersAndGroupsSoak.class);
     private int totalUsers;
     private ConnectionPool connectionPool;
 
     public CreateUsersAndGroupsSoak(int totalUsers, ConnectionPool connectionPool) {
         super(totalUsers);
         this.connectionPool = connectionPool;
         this.totalUsers = totalUsers;
     }
 
    protected void logRate(double t, int currentThreads) {
        double rate = ((double) totalUsers) / t;
        double ratePerThread = ((double) totalUsers) / (((double) currentThreads) * t);
        LOGGER.info(
                "Test complete, Threads {} time taken {} Users Per Second {} Users Per Second Per Thread {} ",
                new Object[] { currentThreads, t, rate, ratePerThread });
    }
 
     protected Runnable getRunnable(int nthreads) throws ConnectionPoolException,
             StorageClientException, AccessDeniedException {
         int usersPerThread = totalUsers / nthreads;
         return new CreateUsersAndGroupsClient(usersPerThread, connectionPool);
     }
 
     public static void main(String[] argv) throws ConnectionPoolException, StorageClientException,
             AccessDeniedException, ClassNotFoundException {
 
         int totalUsers = 1000;
         int nthreads = 10;
 
         if (argv.length > 0) {
             nthreads = StorageClientUtils.getSetting(Integer.valueOf(argv[0]), nthreads);
         }
         if (argv.length > 1) {
             totalUsers = StorageClientUtils.getSetting(Integer.valueOf(argv[1]), totalUsers);
         }
 
         CreateUsersAndGroupsSoak createUsersAndGroupsSoak = new CreateUsersAndGroupsSoak(
                 totalUsers, getConnectionPool());
         createUsersAndGroupsSoak.launchSoak(nthreads);
     }
     
     protected static ConnectionPool getConnectionPool() throws ClassNotFoundException {
         CassandraClientConnectionPool cp = new CassandraClientConnectionPool();
         cp.activate(ImmutableMap.of("test", (Object) "test"));
         return cp;
     }
 
 }
