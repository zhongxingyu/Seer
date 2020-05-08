 /**
  * 
  */
 package edu.jhu.rebar.riak;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.time.StopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.basho.riak.client.IRiakClient;
 import com.basho.riak.client.IRiakObject;
 import com.basho.riak.client.RiakException;
 import com.basho.riak.client.RiakFactory;
 import com.basho.riak.client.RiakRetryFailedException;
 import com.basho.riak.client.bucket.Bucket;
 import com.basho.riak.client.cap.UnresolvedConflictException;
 import com.basho.riak.client.convert.ConversionException;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import edu.jhu.hlt.concrete.Concrete.Vertex;
 import edu.jhu.rebar.RebarException;
 import edu.jhu.rebar.config.RiakConfiguration;
 
 /**
  * @author max
  * 
  */
 public class RiakQueryClient {
     private static final Logger logger = LoggerFactory.getLogger(RiakQueryClient.class);
     
     private final IRiakClient client;
     private List<String> keyList = null;  // "lazily" compute this. 
 
     /**
      * Get a new client.
      * 
      * @throws RiakException
      * @throws RebarException
      * 
      */
     public RiakQueryClient() throws RiakException {
         this.client = RiakFactory.newClient(RiakConfiguration.generateHTTPClusterConfig());
     }
 
     public Vertex fetchVertexById(String id) throws RebarException {
         try {
             Bucket b = this.client.fetchBucket(RiakConfiguration.getVertexBucketName()).execute();
             IRiakObject result = b.fetch(id).execute();
             return Vertex.PARSER.parseFrom(result.getValue());
         } catch (RiakRetryFailedException | UnresolvedConflictException | ConversionException | InvalidProtocolBufferException e) {
             throw new RebarException(e);
         }
     }
     
     public List<String> getTacKb09Ids() throws RebarException {
         // if we haven't computed this before, do it now.
         if (this.keyList == null) {
             try {
                 Bucket b = this.client.fetchBucket(RiakConfiguration.getFeatureBucketName()).execute();
                 IRiakObject result = b.fetch("ids").execute();
                 String bigString = new String(result.getValue());
                 String[] split = bigString.split("\n");
                 this.keyList = Arrays.asList(split);
             } catch (RiakRetryFailedException | UnresolvedConflictException | ConversionException e) {
                 throw new RebarException(e);
             }
         }
         
         // will be cached for future use.
         return this.keyList;
     }
     
     public List<String> getTacKb09Names() throws RebarException {
         try {
             Bucket b = this.client.fetchBucket(RiakConfiguration.getFeatureBucketName()).execute();
             IRiakObject result = b.fetch("names").execute();
             String bigString = new String(result.getValue());
             String[] split = bigString.split("\n");
             return Arrays.asList(split);
         } catch (RiakRetryFailedException | UnresolvedConflictException | ConversionException e) {
             throw new RebarException(e);
         }
     }
     
     public List<Vertex> sampleVertices(double fraction) throws RebarException {
         List<Vertex> vList = new ArrayList<>(1000000);
         if (fraction > 1.0) 
             throw new RebarException("Can't get over 100% of the data.");
         else if (fraction < 0.0)
             throw new RebarException("Can't get less than 0% of the data.");
         
         try {
             Bucket b = this.client.fetchBucket(RiakConfiguration.getVertexBucketName()).execute();
             int numToGet = (int)(this.keyList.size() * fraction);
             for (int i = 0; i < numToGet; i++) {
                 IRiakObject result = b.fetch(this.keyList.get(i)).execute();
                 vList.add(Vertex.PARSER.parseFrom(result.getValue()));
                 
                 if (i % 5000 == 0)
                     logger.info("Got " + i + " vertices...");
             }
             
             return vList;
         } catch (RiakRetryFailedException | UnresolvedConflictException | ConversionException | InvalidProtocolBufferException e) {
             throw new RebarException(e);
         }
     }
     
     public void close() {
         this.client.shutdown();
     }
 
     /**
      * @param args
      * @throws RiakException 
      * @throws RebarException 
      */
     public static void main(String[] args) throws RiakException, RebarException {
         if (args.length != 1) {
             logger.info("Usage: RiakQueryClient <fraction-of-data-to-get-and-measure-time>");
             System.exit(1);
         }
             
         RiakQueryClient qc = new RiakQueryClient();
         StopWatch watch = new StopWatch();
         watch.start();
         List<Vertex> vList = qc.sampleVertices(Double.parseDouble(args[0]));
         watch.suspend();
         logger.info("Took " + watch.toString() + " time to get " + vList.size() + " vertices.");
         qc.close();
     }
 }
