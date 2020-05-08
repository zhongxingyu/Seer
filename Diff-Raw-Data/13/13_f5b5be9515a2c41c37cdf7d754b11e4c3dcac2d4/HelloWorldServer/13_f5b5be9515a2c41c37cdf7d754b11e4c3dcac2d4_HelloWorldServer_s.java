 package org.reefy.helloworld;
 
 import org.reefy.storesqlite.SqliteStore;
 import org.reefy.transportrest.RestContact;
 import org.reefy.transportrest.RestTransportServer;
 import org.reefy.transportrest.api.AppServerHandler;
 import org.reefy.transportrest.api.RawKey;
 import org.reefy.transportrest.api.SimpleAppServer;
 import org.reefy.transportrest.api.store.Store;
 import org.reefy.transportrest.api.store.StoreException;
 import org.reefy.transportrest.api.transport.TransportServer;
 import org.reefy.transportrest.api.transport.TransportServerFactory;
 
 /**
  * @author Paul Kernfeld <hi-entropy@gmail.com>
  */
 public class HelloWorldServer {
     public static void main(String[] args) throws StoreException {
        final Store store = new SqliteStore("hello-world");
         final TransportServerFactory transportServerFactory = new TransportServerFactory() {
             @Override
             public TransportServer build(AppServerHandler handler) {
                 return new RestTransportServer(
                     new RestContact(RawKey.pseudorandom(), "localhost", 8000), handler);
             }
         };
         final SimpleAppServer simpleAppServer = new SimpleAppServer(store, transportServerFactory);
 
         simpleAppServer.startAndWait();
         simpleAppServer.clear();
     }
 }
