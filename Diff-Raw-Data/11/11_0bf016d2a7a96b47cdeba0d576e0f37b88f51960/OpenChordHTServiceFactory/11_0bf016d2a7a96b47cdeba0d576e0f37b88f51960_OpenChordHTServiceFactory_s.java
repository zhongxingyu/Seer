 package org.graphdht.openchord;
 
 import org.graphdht.dht.HTService;
 import org.graphdht.hashcontainer.HTServiceFactory;
 
 import java.io.Serializable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: alex
  * Date: 8/Jun/2010
  * Time: 10:12:22
  * To change this template use File | Settings | File Templates.
  */
 public class OpenChordHTServiceFactory<V extends Serializable> implements HTServiceFactory{
     @Override
     public HTService createHTService() {
        return new DHTConnector<Long,V>("127.0.0.1", DHTConstants.GDHT_OPENCHORD_I_PORT);  
     }
 }
