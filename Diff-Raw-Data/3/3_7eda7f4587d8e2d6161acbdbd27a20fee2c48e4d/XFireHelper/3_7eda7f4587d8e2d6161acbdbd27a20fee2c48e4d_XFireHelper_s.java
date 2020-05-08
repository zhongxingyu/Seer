 package org.codehaus.xfire.aegis.inheritance.xfire;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.xfire.DefaultXFire;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.aegis.inheritance.ws1.*;
 import org.codehaus.xfire.aegis.inheritance.ws2.WS2;
 import org.codehaus.xfire.aegis.inheritance.ws2.common.ParentBean;
 import org.codehaus.xfire.aegis.inheritance.ws2.common.exception.AlreadyExistsException;
 import org.codehaus.xfire.aegis.inheritance.ws2.common.exception.NotFoundException;
 import org.codehaus.xfire.aegis.inheritance.ws2.common.pack1.ContentBean1;
 import org.codehaus.xfire.aegis.inheritance.ws2.common.pack2.ContentBean2;
 import org.codehaus.xfire.client.XFireProxyFactory;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.service.invoker.BeanInvoker;
 
 /**
  * <br/>
  * 
  * @author xfournet
  */
 public class XFireHelper
 {
     private static boolean s_handleInheritance = true;
 
     private XFire m_xfire;
 
     private ServiceFactory m_serviceFactory;
 
     private ServiceRegistry m_serviceRegistry;
 
     public XFireHelper()
     {
         this(null);
     }
 
     public XFireHelper(XFire xfire)
     {
         if (xfire == null)
         {
             m_xfire = new DefaultXFire();
         }
         else
         {
             m_xfire = xfire;
         }
         m_serviceRegistry = m_xfire.getServiceRegistry();
         m_serviceFactory = new ObjectServiceFactory(m_xfire.getTransportManager(),
                 new AegisBindingProvider());
     }
 
     public XFire getXfire()
     {
         return m_xfire;
     }
 
     public void registerService(Service service, Object svcImpl)
     {
         service.setInvoker(new BeanInvoker(svcImpl));
         m_serviceRegistry.register(service);
     }
 
     public Object createClientProxy(Service service, String xfireServletUrl)
         throws MalformedURLException
     {
         xfireServletUrl += (xfireServletUrl.endsWith("/") ? "" : "/")
                 + service.getName().getLocalPart();
         return new XFireProxyFactory().create(service, xfireServletUrl);
     }
 
     public Service createServiceWS1()
     {
         Map properties = new HashMap();
 
         if (s_handleInheritance)
         {
             properties.put(AegisBindingProvider.WRITE_XSI_TYPE_KEY, Boolean.TRUE);
 
             List overrides = new ArrayList();
             overrides.add(BeanA.class.getName());
             overrides.add(BeanB.class.getName());
             overrides.add(BeanC.class.getName());
             overrides.add(BeanD.class.getName());
             overrides.add(RootBean.class.getName());
             overrides.add(WS1Exception.class.getName());
             overrides.add(WS1ExtendedException.class.getName());
 
             properties.put(AegisBindingProvider.OVERRIDE_TYPES_KEY, overrides);
         }
 
         return m_serviceFactory.create(WS1.class, "ws1", null, properties);
     }
 
     public Service createServiceWS2()
     {
         Map properties = new HashMap();
 
         if (s_handleInheritance)
         {
             properties.put(AegisBindingProvider.WRITE_XSI_TYPE_KEY, Boolean.TRUE);
 
             List overrides = new ArrayList();
             overrides.add(ParentBean.class.getName());
             overrides.add(ContentBean1.class.getName());
             overrides.add(ContentBean2.class.getName());
             overrides.add(AlreadyExistsException.class.getName());
             overrides.add(NotFoundException.class.getName());
 
             properties.put(AegisBindingProvider.OVERRIDE_TYPES_KEY, overrides);
         }
 
         return m_serviceFactory.create(WS2.class, "ws2", null, properties);
     }
 }
