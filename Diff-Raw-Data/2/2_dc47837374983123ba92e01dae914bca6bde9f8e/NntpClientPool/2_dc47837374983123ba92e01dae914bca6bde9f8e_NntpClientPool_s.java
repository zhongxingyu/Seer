 package au.com.samcday.bincrawl.pool;
 
 import au.com.samcday.bincrawl.configuration.NntpClientConfiguration;
 import au.com.samcday.bincrawl.misc.NNTPBandwidthMonitor;
 import au.com.samcday.jnntp.NntpClient;
 import au.com.samcday.jnntp.NntpClientBuilder;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import org.apache.commons.pool.PoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 
 import static au.com.samcday.jnntp.NntpClientBuilder.nntpClient;
 
 @Singleton
 public class NntpClientPool extends GenericObjectPool<NntpClient>  {
     @Inject
     public NntpClientPool(NntpClientConfiguration clientConfiguration, NNTPBandwidthMonitor monitor) {
         super(new ClientFactory(clientConfiguration, monitor));
 
         this.setMaxActive(clientConfiguration.getMaxConnections());
         this.setWhenExhaustedAction(WHEN_EXHAUSTED_BLOCK);
         this.setMaxIdle(-1);
         this.setTimeBetweenEvictionRunsMillis(10 * 60 * 1000); // 10 minutes.
         this.setNumTestsPerEvictionRun(-1);
         this.setTestOnBorrow(true);
 
         // TODO: configurable settings for client idle times, etc.
     }
 
     public PooledNntpClient borrow() {
         try {
             return new PooledNntpClient(this, this.borrowObject());
         }
         catch(Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static final class ClientFactory implements PoolableObjectFactory<NntpClient> {
         private NntpClientConfiguration clientConfiguration;
         private NNTPBandwidthMonitor monitor;
 
         private ClientFactory(NntpClientConfiguration clientConfiguration, NNTPBandwidthMonitor monitor) {
             this.clientConfiguration = clientConfiguration;
             this.monitor = monitor;
         }
 
         @Override
         public NntpClient makeObject() throws Exception {
             NntpClientBuilder builder = nntpClient(this.clientConfiguration.getHost())
                 .port(this.clientConfiguration.getPort())
                 .ssl(this.clientConfiguration.isSsl());
             if(this.clientConfiguration.hasAuth()) builder.auth(this.clientConfiguration.getUsername(), this.clientConfiguration.getPassword());
 
             NntpClient client = builder.build();
             client.registerBandwidthHandler(this.monitor);
 
            return builder.build();
         }
 
         @Override
         public void destroyObject(NntpClient nntpClient) throws Exception {
             nntpClient.disconnect();
         }
 
         @Override
         public boolean validateObject(NntpClient nntpClient) {
             try {
                 return nntpClient.date() != null;
             }
             catch(Exception e) {
                 return false;
             }
         }
 
         @Override
         public void activateObject(NntpClient nntpClient) throws Exception {
         }
 
         @Override
         public void passivateObject(NntpClient nntpClient) throws Exception {
         }
     }
 
 }
