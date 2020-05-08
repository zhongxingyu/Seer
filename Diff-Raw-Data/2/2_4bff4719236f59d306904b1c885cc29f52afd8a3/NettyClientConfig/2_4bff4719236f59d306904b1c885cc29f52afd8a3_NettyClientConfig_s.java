 package com.wixpress.fjarr.client;
 
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniels
  * Date: 3/6/12
  */
 public class NettyClientConfig
 {
     public static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 2 * 1000;
     public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 30 * 1000;
     public static final int DEFAULT_MAX_THREADS = 20;
     public static final int DEFAULT_CORE_THREADS = 4;
 
    public static final RejectedExecutionHandler DEFAULT_REJECTION_POLICY = new ThreadPoolExecutor.DiscardOldestPolicy();
 
 
     /**
      * Timeout in milliseconds for successful TCP connection to establish
      */
     private int connectionTimeoutMillis = DEFAULT_CONNECTION_TIMEOUT_MILLIS;
 
     /**
      * Socket read timeout (SO_TIMEOUT)
      */
     private int socketTimeoutMillis = DEFAULT_SOCKET_TIMEOUT_MILLIS;
 
     /**
      * Max Threads in pool
      */
     private int maxThreads = DEFAULT_MAX_THREADS;
 
     /**
      * Core Threads in pool
      */
     private int coreThreads = DEFAULT_CORE_THREADS;
 
     private RejectedExecutionHandler rejectionPolicy = DEFAULT_REJECTION_POLICY;
 
 
     private NettyClientConfig()
     {
 
     }
 
     public static NettyClientConfig defaults()
     {
         return new NettyClientConfig();
     }
 
     public NettyClientConfig withConnectionTimeoutMillis(int connectionTimeoutMillis)
     {
         setConnectionTimeoutMillis(connectionTimeoutMillis);
         return this;
     }
 
     public NettyClientConfig withSocketTimeoutMillis(int socketTimeoutMillis)
     {
         setSocketTimeoutMillis(socketTimeoutMillis);
         return this;
     }
 
     public NettyClientConfig withCoreThreads(int coreThreads)
     {
         setCoreThreads(coreThreads);
         return this;
     }
 
     public NettyClientConfig withMaxThreads(int maxThreads)
     {
         setMaxThreads(maxThreads);
         return this;
     }
 
     public void setConnectionTimeoutMillis(int connectionTimeoutMillis)
     {
         this.connectionTimeoutMillis = connectionTimeoutMillis;
     }
 
     public void setSocketTimeoutMillis(int socketTimeoutMillis)
     {
         this.socketTimeoutMillis = socketTimeoutMillis;
     }
 
     public void setCoreThreads(int coreThreads)
     {
         this.coreThreads = coreThreads;
     }
 
     public void setMaxThreads(int maxThreads)
     {
         this.maxThreads = maxThreads;
     }
 
 
     public int getConnectionTimeoutMillis()
     {
         return connectionTimeoutMillis;
     }
 
     public int getSocketTimeoutMillis()
     {
         return socketTimeoutMillis;
     }
 
     public int getMaxThreads()
     {
         return maxThreads;
     }
 
     public int getCoreThreads()
     {
         return coreThreads;
     }
 
     public RejectedExecutionHandler getRejectionPolicy()
     {
         return rejectionPolicy;
     }
 }
