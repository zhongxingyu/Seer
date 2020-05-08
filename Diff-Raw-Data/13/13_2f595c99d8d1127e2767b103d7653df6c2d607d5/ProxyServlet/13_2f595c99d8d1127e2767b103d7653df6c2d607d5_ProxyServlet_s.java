 package no.tornado.brap.servlet;
 
 import no.tornado.brap.auth.*;
 import no.tornado.brap.common.InvocationRequest;
 import no.tornado.brap.common.InvocationResponse;
 
 import javax.servlet.*;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * This ProxyServlet is configured from web.xml for each service you wish
  * to expose as a remoting service.
  *
  * This class provides the basic capabilities needed to instantiate and expose
  * a remoting service. Only <code>service</code> is required. The rest of
  * the parameters have default values.
  *
  * Concider subclassing to provide custom creation by overriding
  * <code>getServiceWrapper</code> or just one of
  * <code>getAuthenticationProvider</code> or <code>getAuthorizationProvider</code>.
  *
  * <p>Example of exposing a remoting service without requiring authentication:</p>
  *<pre>
  *  &lt;servlet&gt;
  *      &lt;servlet-name&gt;hello&lt;/servlet-name&gt;
  *      &lt;servlet-class&gt;no.tornado.brap.servlet.ProxyServlet&lt;/servlet-class&gt;
  *      &lt;init-param&gt;
  *          &lt;param-name&gt;service&lt;/param-name&gt;
  *          &lt;param-value&gt;class.of.the.service.to.instantiate&lt;/param-value&gt;
  *      &lt;/init-param&gt;
  *      &lt;init-param&gt;
  *          &lt;param-name&gt;authorizationProvider&lt;/param-name&gt;
  *          &lt;param-value&gt;no.tornado.brap.auth.AuthenticationNotRequiredAuthorizer&lt;/param-value&gt;
  *      &lt;/init-param&gt;
  *      &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
  *  &lt;/servlet&gt;
  *  &lt;servlet-mapping&gt;
  *      &lt;servlet-name&gt;hello&lt;/servlet-name&gt;
  *      &lt;url-pattern&gt;/remoting/hello&lt;/url-pattern&gt;
  *  &lt;/servlet-mapping&gt;
  *</pre>
  *
  *
  */
 public class ProxyServlet implements Servlet {
     public final String INIT_PARAM_AUTHENTICATION_PROVIDER = "authenticationProvider";
     public final String INIT_PARAM_AUTHORIZATION_PROVIDER = "authorizationProvider";
     public final String INIT_PARAM_SERVICE = "service";
 
     protected ServiceWrapper serviceWrapper;
     protected ServletConfig servletConfig;
 
     public void init(ServletConfig servletConfig) throws ServletException {
         this.servletConfig = servletConfig;
         try {
             createServiceWrapper();
         } catch (Exception e) {
             throw new ServletException("Failed to instantiate the serviceWrapper", e);
         }
     }
 
     /**
      * Override this method to control every detail of the creation of the service wrapper.
      *
      * Normally you would just override one or more of the methods that provide the service wrapper details.
      *
      * @see ProxyServlet#getService()
      * @see ProxyServlet#getAuthenticationProvider()
      * @see ProxyServlet#getAuthorizationProvider()
      */
     public void createServiceWrapper() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
         serviceWrapper = new ServiceWrapper();
         serviceWrapper.setService(getService());
         serviceWrapper.setAuthenticationProvider(getAuthenticationProvider());
         serviceWrapper.setAuthorizationProvider(getAuthorizationProvider());
     }
 
     /**
      * Override to configure a different Authorization Provider. The default provider
      * authorizes every authenticated invocation. In many cases, requiring Authentication and providing
      * an AuthenticationProvider is sufficient, but you can use the Authorization Provider
      * to allow/deny access to spesific method-calls based on the principal in
      * <code>AuthenticationContext#getPrincipal()</code>.
      *
      * You can either subclass or supply the "authorizationProvider" init-param to
      * change the AuthorizationProvider.
      *
      * @return the AuthorizationProvider
      */
     protected AuthorizationProvider getAuthorizationProvider() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
         if (servletConfig.getInitParameter(INIT_PARAM_AUTHORIZATION_PROVIDER) != null)
             return (AuthorizationProvider) Class.forName(servletConfig.getInitParameter(INIT_PARAM_AUTHORIZATION_PROVIDER)).newInstance();
 
         return new AuthenticationRequiredAuthorizer();
     }
 
     /**
      * Override to configure a different Authentication Provider. The default provider
      * authenticates every invocation.
      *
      * You can either subclass or supply the "authenticationProvider" init-param to
      * change the AuthenticationProvider.
      *
      * @return the AuthenticationProvider
      */
     protected AuthenticationProvider getAuthenticationProvider() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
         if (servletConfig.getInitParameter(INIT_PARAM_AUTHENTICATION_PROVIDER) != null)
             return (AuthenticationProvider) Class.forName(servletConfig.getInitParameter(INIT_PARAM_AUTHENTICATION_PROVIDER)).newInstance();
 
         return new AuthenticationNotRequiredAuthenticator();
     }
 
     /**
      * Supply the service to expose via this servlet.
      *
      * You can either subclass or supply the "service" init-param to
      * configure what service class to instantiate.
      *
      * @return
      */
     protected Object getService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
         return Class.forName(servletConfig.getInitParameter(INIT_PARAM_SERVICE)).newInstance();
     }
 
     /**
      * The service method performs the actual deserialization of the InvocationRequest and returns
      * an InvocationResponse in the body of the ServletResponse.
      *
      * Standard Java object serialization/deserialization is used to retrieve and set the invocation
      * request/response.
      *
      * The configured <code>AuthenticationProvider</code> and <code>AuthorizationProvider</code>
      * are consulted.
      *
      * A ThreadLocal in the <code>AuthenticationContext</code> holds on to any principal created during
      * authentication, so that it is available to both the AuthorizationProvider and any service
      * that whishes to get hold of the principal via <code>AuthenticationContext#getPrincipal()</code>.
      *
      * You are encouraged to use your existing domain object AllowAllAuthorizerfor authentication :)
      * 
      * @param request The ServletRequest
      * @param response the ServletResponse
      * @throws ServletException
      * @throws IOException
      *
      * @see no.tornado.brap.common.InvocationRequest
      * @see no.tornado.brap.common.InvocationResponse
      */
     public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
         AuthenticationContext.enter();
         InvocationResponse invocationResponse = null;

         try {
             invocationResponse = new InvocationResponse();
 
             InvocationRequest invocationRequest = (InvocationRequest) new ObjectInputStream(request.getInputStream()).readObject();
 
             serviceWrapper.getAuthenticationProvider().authenticate(invocationRequest);
             serviceWrapper.getAuthorizationProvider().authorize(invocationRequest);
 
            Method method = getMethod(invocationRequest.getMethodName(), invocationRequest.getParameterTypes());
 
             Serializable result = (Serializable) method.invoke(serviceWrapper.getService(), invocationRequest.getParameters());
             invocationResponse.setResult(result);
         } catch (Exception e) {
             if (e instanceof InvocationTargetException) {
                 InvocationTargetException ite = (InvocationTargetException) e;
                 invocationResponse.setException(ite.getTargetException());
             } else {
                 invocationResponse.setException(e);
             }
         } finally {
             AuthenticationContext.exit();
             new ObjectOutputStream(response.getOutputStream()).writeObject(invocationResponse);
         }
     }
 
     /**
      * Retrieves the <code>java.lang.Reflect.Method</code> to invoke on the wrapped service class.
      * The <code>methodName</code> and <code>parameterTypes</code> arguments are retrieved from the
      * <code>InvocationRequest</code> that was encapsulated in the ServletRequest body.
      *
      * @param methodName
      * @param parameterTypes
      * @return
      * @throws NoSuchMethodException
      */
     private Method getMethod(String methodName, Class[] parameterTypes) throws NoSuchMethodException {
         return serviceWrapper.getService().getClass().getMethod(methodName, parameterTypes);
     }
 
     public String getServletInfo() {
         return getClass().getCanonicalName();
     }
 
     public void destroy() {
 
     }
 
     public ServletConfig getServletConfig() {
         return servletConfig;
     }
 }
