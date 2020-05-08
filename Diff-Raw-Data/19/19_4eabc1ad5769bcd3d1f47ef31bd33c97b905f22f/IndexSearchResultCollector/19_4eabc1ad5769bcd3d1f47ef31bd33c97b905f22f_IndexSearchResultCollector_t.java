 package org.gridkit.gemfire.search.lucene;
 
 import com.gemstone.gemfire.cache.execute.FunctionException;
 import com.gemstone.gemfire.cache.execute.ResultCollector;
 import com.gemstone.gemfire.distributed.DistributedMember;
 import org.gridkit.gemfire.search.util.Serialization;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 
//TODO implement more general error handling (indexProcessorNotFoundMarker)
 public class IndexSearchResultCollector implements ResultCollector<Serializable, ArrayList<Object>> {
     private static Logger log = LoggerFactory.getLogger(IndexSearchResultCollector.class);
 
     private ArrayList<Object> objectKeys = new ArrayList<Object>();
 
     @Override
     public void addResult(DistributedMember memberID, Serializable optionKey) {
         if (IndexSearchFunction.indexProcessorNotFoundMarker.equals(optionKey)) {
            objectKeys.add(IndexSearchFunction.indexProcessorNotFoundMarker);
         }
         else if (String.class.isInstance(optionKey) && !IndexSearchFunction.lastResultMarker.equals(optionKey)) {
             Object objectKey;
 
             try {
                 objectKey = Serialization.toObject((String)optionKey);
             } catch (Exception e) {
                 log.warn("Exception while deserializing string key " + optionKey, e);
                 return;
             }
 
             objectKeys.add(objectKey);
         } else if (Throwable.class.isInstance(optionKey))
             log.warn("Exception on remote member " + memberID.toString(), (Throwable)optionKey);
     }
 
     @Override
     public ArrayList<Object> getResult() throws FunctionException {
         return objectKeys;
     }
 
     @Override
     public ArrayList<Object> getResult(long timeout, TimeUnit unit) throws FunctionException, InterruptedException {
         return objectKeys;
     }
 
     @Override
     public void clearResults() {}
 
     @Override
     public void endResults() {}
 }
