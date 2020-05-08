 package org.sakaiproject.nakamura.lite.soak.cassandra;
 
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
 import org.sakaiproject.nakamura.lite.soak.authorizable.CreateUsersAndGroupsClient;
 import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
 import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
 import org.sakaiproject.nakamura.lite.storage.StorageClientException;
 import org.sakaiproject.nakamura.lite.storage.cassandra.CassandraClientConnectionPool;
 
 import com.google.common.collect.ImmutableMap;
 
 public class CreateUsersAndGroupsSoak extends AbstractSoakController {
     private int totalUsers;
     private ConnectionPool connectionPool;
 
     public CreateUsersAndGroupsSoak(int totalUsers, ConnectionPool connectionPool) {
         super(totalUsers);
         this.connectionPool = connectionPool;
         this.totalUsers = totalUsers;
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
