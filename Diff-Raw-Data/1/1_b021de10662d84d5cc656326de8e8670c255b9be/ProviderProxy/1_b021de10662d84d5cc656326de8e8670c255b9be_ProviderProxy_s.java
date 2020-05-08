 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2010 SorcerSoft.org.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.provider.proxy;
 
 import java.io.IOException;
 import java.io.InvalidObjectException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.jini.admin.Administrable;
 import net.jini.core.constraint.RemoteMethodControl;
 import net.jini.id.ReferentUuid;
 import net.jini.id.Uuid;
 import net.jini.security.proxytrust.SingletonProxyTrustIterator;
import sorcer.util.Log;
 
 /**
  * The Service provider should wrap up the smart proxy/stub. The
  * java.lang.reflect.Proxy returned from this object will have the following
  * features. If the server implements RemoteMethodControl, the proxy returned
  * would implement the functionality of Jini2.0(TM) Security and semantics to
  * ReferentUuid. If not it implements the semantics of ReferentUuid.
  * 
  * Functionality of Jini2.0 Security implemented by this ProviderProxy Ability to
  * set and get client constraints Ability to getProxyTrustIterator
  *
  * Functionality of ReferentUuid The unique identifier assigned to the current
  * instance of this proxy class by the lookup discovery service. This ID is used
  * to determine equality between proxies.
  */
 
 @SuppressWarnings("rawtypes")
 public class ProviderProxy implements Serializable {
 
     static final long serialVersionUID = -242006752320266252L;
 
     protected final static Logger logger = Logger.getLogger(ProviderProxy.class.getName());
 
     /**
      * Public static factory method that creates and returns an instance of
      * <code>java.lang.reflect.Proxy</code>. This proxy will implement the
      * semantics of ReferentUuid and jini2.0 security semantics if the server
      * passed onto the createServiceProxy method will implement
      * RemoteMethodControl.
      *
      * @param proxy
      *            reference to the server object through which communication
      *            occurs between the client-side and server-side of the
      *            associated service.
      * @param proxyID
      *            the unique identifier assigned by the service to each instance
      *            of this proxy
      *
      * @return an instance of <code>java.lang.reflect.Proxy</code> that
      *         implements <code>RemoteMethodControl</code> if the given
      *         <code>server</code> does.
      */
     public static Object wrapServiceProxy(Object proxy, Uuid proxyID) {
 
         if (proxy == null)
             throw new NullPointerException("Cannot have a server which is null");
 
         ReferentUuidInvocationHandler handler =
                 (proxy instanceof RemoteMethodControl) ?
                         new ConstrainableInvocationHandler(proxy, proxyID) :
                         new ReferentUuidInvocationHandler(proxy, proxyID);
 
         return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                 handler.getInterfaces(proxy),
                 handler);
     }
 
     // -------------------------------ReferentUuidInvocationHandler------------------------------
     private static class ReferentUuidInvocationHandler implements InvocationHandler, Serializable {
         private static final long serialVersionUID = 242006752320266247L;
         protected final Object proxy;
         protected final Uuid proxyID;
 
         public ReferentUuidInvocationHandler(Object proxy, Uuid proxyID) {
             this.proxy = proxy;
             this.proxyID = proxyID;
         }
 
         public Class[] getInterfaces(Object proxy, Class... additionalInterfaces) {
             Class[] interfaces = proxy.getClass().getInterfaces();
             List<Class> list = new ArrayList<Class>();
             for (Class c : interfaces) {
                 if(!list.contains(c))
                     list.add(c);
             }
             for (Class c : additionalInterfaces) {
                 if(!list.contains(c))
                     list.add(c);
             }
             list.add(ReferentUuid.class);
             return list.toArray(new Class[list.size()]);
         }
 
         public Object invoke(Object server, Method m, Object[] args) throws Throwable {
             if ("getReferentUuid".equals(m.getName()))
                 return proxyID;
             else if ("getProxy".equals(m.getName()))
                 return proxy;
             if ("hashCode".equals(m.getName())) {
                 return proxyID.hashCode();
             } else if ("equals".equals(m.getName()) && args.length == 1) {
                 return !(args.length != 1 || !(args[0] instanceof ReferentUuid)) && proxyID.equals(((ReferentUuid) args[0]).getReferentUuid());
             } else if ("toString".equals(m.getName())) {
                 return "refID=" + proxyID + " : proxy=" + proxy;
             }
             return m.invoke(proxy, args);
         }
 
         private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
             s.defaultReadObject();
 			/* Verify server */
             if (proxy == null) {
                 throw new InvalidObjectException("ServerProxy.readObject "
                         + "failure - server " + "field is null");
             }// endif
 			/* Verify proxyID */
             if (proxyID == null) {
                 throw new InvalidObjectException("ServerProxy.readObject "
                         + "failure - proxyID " + "field is null");
             }// endif
         }// end readObject
 
         private void readObjectNoData() throws InvalidObjectException {
             throw new InvalidObjectException(
                     "no data found when attempting to "
                             + "deserialize FiddlerProxy instance");
         }// end readObjectNoData
 
     }
 
     private static class ConstrainableInvocationHandler extends ReferentUuidInvocationHandler {
 
         public ConstrainableInvocationHandler(Object server, Uuid proxyID) {
             super(server, proxyID);
         }
 
         public Class[] getInterfaces(Object server) {
             return super.getInterfaces(server, RemoteMethodControl.class);
         }
 
         public Object invoke(Object server, Method m, Object[] args) throws Throwable {
             String selector = m.getName();
             if ("getReferentUuid".equals(selector)) {
                 return proxyID;
             } else if ("getProxy".equals(selector)) {
                 return proxy;
             } else  if ("hashCode".equals(selector)) {
                 return proxyID.hashCode();
             } else if ("equals".equals(selector) && args.length == 1) {
                 return args[0] instanceof ReferentUuid && proxyID.equals(((ReferentUuid) args[0]).getReferentUuid());
             } else if ("toString".equals(selector)) {
                 return "refID=" + proxyID + " : proxy=" + proxy;
             } else if ("getConstraints".equals(selector)) {
                 return ((RemoteMethodControl) proxy).getConstraints();
             } else if ("setConstraints".equals(selector)) {
                 return m.invoke(proxy, args);
             } else if ("getProxyTrustIterator".equals(selector)) {
                 return new SingletonProxyTrustIterator(server);
             } else if ("getAdmin".equals(selector)) {
                 return ((Administrable)proxy).getAdmin();
             }
             Object obj = null;
             try {
                 obj  = m.invoke(proxy, args);
             } catch (Exception e) {
                 logger.log(Level.WARNING, "proxy method: " + m + " for args: "
                         + Arrays.toString(args), e.getMessage());
                 //e.printStackTrace();
             }
             return obj;
         }
 
         private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
             s.defaultReadObject();
 			/* Verify server */
             if (proxy == null) {
                 throw new InvalidObjectException("ServerProxy.readObject "
                         + "failure - server " + "field is null");
             }// endif
 			/* Verify proxyID */
             if (proxyID == null) {
                 throw new InvalidObjectException("ServerProxy.readObject "
                         + "failure - proxyID " + "field is null");
             }// endif
         }// end readObject
 
         private void readObjectNoData() throws InvalidObjectException {
             throw new InvalidObjectException(
                     "no data found when attempting to "
                             + "deserialize FiddlerProxy instance");
         }// end readObjectNoData
     }
 
 }
