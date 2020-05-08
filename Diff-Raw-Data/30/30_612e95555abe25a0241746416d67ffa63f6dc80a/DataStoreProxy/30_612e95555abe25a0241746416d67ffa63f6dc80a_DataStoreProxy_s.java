 /*
  * Copyright (C) 2012 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package fi.hoski.remote;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import fi.hoski.datastore.RemoteAppEngine;
 import fi.hoski.datastore.SMSNotConfiguredException;
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.sql.SQLException;
 import java.util.Properties;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.SynchronousQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Timo Vesalainen
  */
 public class DataStoreProxy implements Runnable, InvocationHandler
 {
 
     private Properties properties;
     private BlockingQueue<MethodCall> queue = new SynchronousQueue<MethodCall>();
     private Semaphore semaphore = new Semaphore(0);
     private Thread thread;
     private static DataStoreService proxy;
 
     public DataStoreProxy(Properties properties)
     {
         this.properties = properties;
     }
 
     public void start() throws InterruptedException
     {
         thread = new Thread(this, getClass().getName());
         thread.setDaemon(true);
         thread.start();
         proxy = (DataStoreService) Proxy.newProxyInstance(
                 DataStoreService.class.getClassLoader(),
                 new Class<?>[]
                 {
                     DataStoreService.class
                 },
                 this);
         DataStore.setDss(proxy);
     }
 
     public static DataStoreService getProxy()
     {
         return proxy;
     }
 
     public void stop()
     {
         thread.interrupt();
     }
 
     @Override
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
     {
         MethodCall mc = new MethodCall(proxy, method, args);
         queue.put(mc);
         semaphore.acquire();
         if (mc.succeeded())
         {
             return mc.getReturnValue();
         }
         else
         {
             throw mc.getThrowable();
         }
     }
 
     @Override
     public void run()
     {
         try
         {
             RemoteAppEngine rae = new RemoteAppEngine()
             {
 
                 @Override
                 protected Object run() throws IOException
                 {
                     try
                     {
                         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                         DataStoreService svc = new DataStoreServiceImpl(properties, datastore);
                         try
                         {
                             while (true)
                             {
                                 MethodCall mc = queue.take();
                                 try
                                 {
                                     Object rv = mc.invoke(svc);
                                     mc.setReturnValue(rv);
                                 }
                                 catch (InvocationTargetException ex)
                                 {
                                     mc.setThrowable(ex.getCause());
                                 }
                                 semaphore.release();
                             }
                         }
                         catch (InterruptedException ex)
                         {
                             throw new IOException(ex);
                         }
                     }
                     catch (SMSNotConfiguredException | SQLException | ClassNotFoundException | EntityNotFoundException ex)
                     {
                         throw new IOException(ex);
                     }
                 }
             };
             rae.call();
         }
         catch (IOException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     private static class MethodCall
     {
 
         private Object proxy;
         private Method method;
         private Object[] args;
         private Object returnValue;
         private Throwable throwable;
 
         public MethodCall(Object proxy, Method method, Object[] args)
         {
             this.proxy = proxy;
             this.method = method;
             this.args = args;
         }
 
         public Object invoke(Object obj) throws InvocationTargetException
         {
             try
             {
                 return method.invoke(obj, args);
             }
             catch (IllegalAccessException ex)
             {
                 throw new IllegalArgumentException(ex);
             }
         }
 
         public boolean succeeded()
         {
             return throwable == null;
         }
 
         public Object[] getArgs()
         {
             return args;
         }
 
         public Method getMethod()
         {
             return method;
         }
 
         public Object getProxy()
         {
             return proxy;
         }
 
         public Object getReturnValue()
         {
             return returnValue;
         }
 
         public void setReturnValue(Object returnValue)
         {
             this.returnValue = returnValue;
         }
 
         public Throwable getThrowable()
         {
             return throwable;
         }
 
         public void setThrowable(Throwable throwable)
         {
             this.throwable = throwable;
         }
     }
 }
