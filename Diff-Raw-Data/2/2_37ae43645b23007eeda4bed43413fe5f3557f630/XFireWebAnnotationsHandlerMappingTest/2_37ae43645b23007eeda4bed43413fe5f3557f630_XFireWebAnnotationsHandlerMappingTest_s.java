 package org.codehaus.xfire.spring.remoting;
 
 /**
  * @author Arjen Poutsma
  */
 
 import java.lang.reflect.Method;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
 import org.codehaus.xfire.annotations.WebAnnotations;
 import org.codehaus.xfire.annotations.WebMethodAnnotation;
 import org.codehaus.xfire.annotations.WebServiceAnnotation;
 import org.codehaus.xfire.spring.remoting.Jsr181HandlerMapping;
 import org.codehaus.xfire.test.EchoImpl;
 import org.easymock.MockControl;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.web.context.support.StaticWebApplicationContext;
 
 public class XFireWebAnnotationsHandlerMappingTest
         extends AbstractXFireAegisTest
 {
     private Jsr181HandlerMapping handlerMapping;
     private MockControl control;
     private WebAnnotations webAnnotations;
 
 
     public void setUp()
             throws Exception
     {
         super.setUp();
         handlerMapping = new Jsr181HandlerMapping();
         control = MockControl.createControl(WebAnnotations.class);
         webAnnotations = (WebAnnotations) control.getMock();
         handlerMapping.setWebAnnotations(webAnnotations);
         handlerMapping.setXfire(getXFire());
         handlerMapping.setTypeMappingRegistry(new DefaultTypeMappingRegistry(true));
     }
 
     public void testHandler()
             throws Exception
     {
         StaticWebApplicationContext appContext = new StaticWebApplicationContext();
         appContext.registerSingleton("echo", EchoImpl.class, new MutablePropertyValues());
 
         webAnnotations.hasWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(true);
         webAnnotations.hasSOAPBindingAnnotation(EchoImpl.class);
         control.setReturnValue(false);
         webAnnotations.hasWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(true);
         WebServiceAnnotation serviceAnnotation = new WebServiceAnnotation();
         serviceAnnotation.setServiceName("EchoService");
         webAnnotations.getWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(serviceAnnotation);
         webAnnotations.getWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(serviceAnnotation);
         
         Method echoMethod = EchoImpl.class.getMethod("echo", new Class[]{String.class});
         webAnnotations.hasWebMethodAnnotation(echoMethod);
         control.setDefaultReturnValue(true);
         
         webAnnotations.hasWebMethodAnnotation(echoMethod);
         control.setDefaultReturnValue(true);
         
         WebMethodAnnotation wma = new WebMethodAnnotation();
         wma.setOperationName("echo");
         webAnnotations.getWebMethodAnnotation(echoMethod);
         control.setDefaultReturnValue(wma);
         
         webAnnotations.hasWebParamAnnotation(echoMethod, 0);
         control.setDefaultReturnValue(false);
         webAnnotations.hasWebResultAnnotation(echoMethod);
         control.setDefaultReturnValue(false);
         webAnnotations.hasOnewayAnnotation(echoMethod);
         control.setDefaultReturnValue(false);
 
         control.replay();
 
         String urlPrefix = "/services/";
         handlerMapping.setUrlPrefix(urlPrefix);
         handlerMapping.setApplicationContext(appContext);
 
         MockHttpServletRequest request = new MockHttpServletRequest("GET", urlPrefix + "EchoService");
         Object handler = handlerMapping.getHandler(request);
         assertNotNull("No valid handler is returned", handler);
 
         control.verify();
     }
 
     public void testNoAnnotation()
             throws Exception
     {
         StaticWebApplicationContext appContext = new StaticWebApplicationContext();
         appContext.registerSingleton("echo", EchoImpl.class, new MutablePropertyValues());
 
         webAnnotations.hasWebServiceAnnotation(EchoImpl.class);
         control.setReturnValue(false);
 
         control.replay();
 
         handlerMapping.setApplicationContext(appContext);
 
 
         MockHttpServletRequest request = new MockHttpServletRequest("GET", "/services/EchoService");
         Object handler = handlerMapping.getHandler(request);
         assertNull("Handler is returned", handler);
 
         control.verify();
     }
 }
