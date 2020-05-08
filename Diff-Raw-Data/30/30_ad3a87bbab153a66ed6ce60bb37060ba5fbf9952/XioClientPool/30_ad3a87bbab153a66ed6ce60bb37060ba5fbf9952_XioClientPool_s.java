 package org.xmodel.net;
 
 import java.net.InetSocketAddress;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import org.xmodel.log.Log;
 import org.xmodel.xpath.expression.IContext;
 
 public class XioClientPool
 {
   public XioClientPool( IXioClientFactory factory)
   {
     this.factory = factory;
     this.threadLocal = new ThreadLocal<ThreadClientPool>();
   }
 
   /**
    * Lease a client connection to the specified address for the specified context.
    * @param context The context.
    * @param address The host address.
    * @return Returns a connected client.
    */
   public XioClient lease( IContext context, InetSocketAddress address)
   {
     ThreadClientPool pool = threadLocal.get();
     if ( pool == null)
     {
       pool = new ThreadClientPool();
       threadLocal.set( pool);
     }
     return pool.lease( context, address);
   }
   
   /**
    * Return a client connection to the pool.
    * @param context The context.
    * @param client The XIO client connection.
    */
   public void release( IContext context, XioClient client)
   {
     ThreadClientPool pool = threadLocal.get();
     if ( pool != null) pool.release( context, client);
   }
   
   private class ThreadClientPool
   {
     public ThreadClientPool()
     {
       clients = new ConcurrentHashMap<InetSocketAddress, XioClient>();
     }
     
     /**
      * Lease a client connection to the specified address.
      * @param context The context.
      * @param address The address.
      * @return Returns a connected client.
      */
     public XioClient lease( IContext context, InetSocketAddress address)
     {
      XioClient client = getCreateClient( address);
       log.debugf( "Leasing XioClient %X, state=%s", client.hashCode(), client.isConnected()? "connected": "disconnected");
       return client;
     }
     
     /**
      * Return an XIO client connection to the pool.
      * @param context The context.
      * @param client The XIO client connection.
      */
     public void release( IContext context, XioClient client)
     {
       log.debugf( "Freeing XioClient %X, state=%s", client.hashCode(), client.isConnected()? "connected": "disconnected");
      if ( !client.isConnected() && client.getRemoteAddress() != null) 
        clients.remove( client.getRemoteAddress());
     }
     
     /**
      * Create  a new XioClient instance.
      * @param key The key.
      * @return Returns the new instance.
      */
     private synchronized XioClient getCreateClient( InetSocketAddress address)
     {
       XioClient client = clients.get( address);
       if ( client == null || !client.isConnected()) 
       {
         client = factory.newInstance( address, true);
         if ( client.isConnected()) clients.put( address, client);
       }
       return client;
     }
     
     private Map<InetSocketAddress, XioClient> clients;
   }
   
   private final static Log log = Log.getLog( XioClientPool.class);
 
   private IXioClientFactory factory;
   private ThreadLocal<ThreadClientPool> threadLocal;
 }
