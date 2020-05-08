 package org.codehaus.xfire.service.binding;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.transport.Session;
 
 /**
  * An invoker which instantiates classes automatically based on the Service's scope.
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Nov 16, 2004
  */
 public class ObjectInvoker
         implements Invoker
 {
     private static final Log logger = LogFactory.getLog(ObjectInvoker.class.getName());
 
     public final static int SCOPE_APPLICATION = 1;
 
     public final static int SCOPE_REQUEST = 3;
 
     public final static int SCOPE_SESSION = 2;
 
     public static final String SERVICE_IMPL_CLASS = "xfire.serviceImplClass";
 
     private int scope = ObjectInvoker.SCOPE_APPLICATION;
 
     /**
      * The object if the scope is SCOPE_APPLICATION.
      */
     private Object appObj;
 
     public Object invoke(final Method method, final Object[] params, final MessageContext context)
             throws XFireFault
     {
         try
         {
             final Object serviceObject = getServiceObject(context);
 
             return method.invoke(serviceObject, params);
         }
         catch (IllegalArgumentException e)
         {
             throw new XFireFault("Illegal argument.", e, XFireFault.SENDER);
         }
         catch (InvocationTargetException e)
         {
             final Throwable t = e.getTargetException();
 
             if (t instanceof XFireFault)
             {
                 throw (XFireFault) t;
             }
             else if (t instanceof Exception)
             {
                 logger.warn("Error invoking service.", t);
                 throw new XFireFault(t, XFireFault.SENDER);
             }
             else
             {
                 logger.warn("Error invoking service.", e);
                 throw new XFireRuntimeException("Error invoking service.", e);
             }
         }
         catch (IllegalAccessException e)
         {
             throw new XFireFault("Couldn't access service object.", e, XFireFault.RECEIVER);
         }
     }
 
     /**
      * Creates and returns a service object depending on the scope.
      */
     public Object getServiceObject(final MessageContext context)
             throws XFireFault
     {
         final Service service = context.getService();
         
         if (scope == ObjectInvoker.SCOPE_APPLICATION)
         {
             if (appObj == null)
             {
                 synchronized (Service.class)
                 {
                     appObj = createServiceObject(service);
                 }
             }
             return appObj;
         }
         else if (scope == ObjectInvoker.SCOPE_SESSION)
         {
             final Session session = context.getSession();
             final String key = "service." + service.getName();
 
             Object sessObj = session.get(key);
             if (sessObj == null)
             {
                 synchronized (Service.class)
                 {
                     sessObj = createServiceObject(service);
                     session.put(key, sessObj);
                 }
             }
             return sessObj;
         }
         else if (scope == ObjectInvoker.SCOPE_REQUEST)
         {
             return createServiceObject(service);
         }
         else
         {
             throw new UnsupportedOperationException("Scope " + scope + " is invalid.");
         }
     }
 
     /**
      * Override this method to control how XFire creates the service object.
      *
      * @param service
      * @return
      * @throws XFireFault
      */
     public Object createServiceObject(final Service service)
             throws XFireFault
     {
         try
         {
             Class svcClass = (Class) service.getProperty(ObjectInvoker.SERVICE_IMPL_CLASS);
 
             if (svcClass == null)
             {
                 svcClass = service.getServiceInfo().getServiceClass();
                if(svcClass.isInterface())
                {
                    throw new XFireFault("ObjectInvoker.SERVICE_IMPL_CLASS not set for interface '" + svcClass.getName() + "'", XFireFault.RECEIVER);
                }
             }
          
            if(svcClass.isInterface())
            {
                throw new XFireFault("Service class '" + svcClass.getName() + "' is an interface", XFireFault.RECEIVER);
            }
          
          if(Modifier.isAbstract(svcClass.getModifiers()))
          {
              throw new XFireFault("Service class '" + svcClass.getName() + "' is abstract", XFireFault.RECEIVER);
          }
             return svcClass.newInstance();
         }
         catch (InstantiationException e)
         {
             throw new XFireFault("Couldn't instantiate service object.", e, XFireFault.RECEIVER);
         }
         catch (IllegalAccessException e)
         {
             throw new XFireFault("Couldn't access service object.", e, XFireFault.RECEIVER);
         }
     }
 
     public int getScope()
     {
         return scope;
     }
 
     public void setScope(int scope)
     {
         this.scope = scope;
     }
 }
