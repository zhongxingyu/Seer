 package org.rosenvold.spring.convention.scope;
 
 import org.rosenvold.spring.convention.ConventionContextLoader;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.test.context.ContextCacheKey;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.RequestContextListener;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * Context loader that adds the capability of adding request and session-scoped beans that
  * may run outside the web container.
  *
  */
 public class MockSessionScopeContextLoader extends ConventionContextLoader {
 
     private static final Map<ContextCacheKey, LocalAttrs> ATTR_CACHE = new HashMap<ContextCacheKey, LocalAttrs>();
 
     /**
       * Activates a scope="session" in the beanfactory allowing us to register and retrieve session-scoped
       * beans in the context. Spring 2.5
       *
       * @param context the parent scope
       */
      protected void customizeContext(final GenericApplicationContext context) {
          configureSessionInfrastructure();
          final TestSessionScope testSessionScope = new TestSessionScope();
          context.getBeanFactory().registerScope("session", testSessionScope);
          final TestRequestScope requestScope = new TestRequestScope();
          context.getBeanFactory().registerScope("request", requestScope);
      }
 
 
     @Override
     protected void prepareContext(GenericApplicationContext context) {
         super.prepareContext(context);
     }
 
     /**
      * Activates a scope="session" in the beanfactory allowing us to register and retrieve session-scoped
      * beans in the context. Spring 3.0 adapted
      *
      * @param context the parent scope
      */
     protected void customizeContext(final GenericApplicationContext context, final ContextCacheKey cacheKey) {
         customizeContext(context);
 
         final LocalAttrs value = new LocalAttrs(RequestContextHolder.getRequestAttributes(), LocaleContextHolder.getLocale());
         synchronized (ATTR_CACHE) {
             ATTR_CACHE.put(cacheKey, value);
         }
     }
 
     public void activateForThread(final ApplicationContext applicationContext, final ContextCacheKey key) {
         synchronized (ATTR_CACHE) {
         final LocalAttrs localAttrs = ATTR_CACHE.get(key);
         // We are in a later thread/invocation. Need to set up spring context for this thread.
         if (localAttrs == null) {
             System.out.println("You cannot call this method before getApplicationContext, faking it ?");
             return;
         }
         LocaleContextHolder.setLocale(localAttrs.getLocale(), true);
         RequestContextHolder.setRequestAttributes(localAttrs.getCopyOfRequestAttributes(), true);
         }
 
     }
 
     /**
      * Configures the necessary session-infrastructure needed to provide SessionScope.
      */
     private void configureSessionInfrastructure() {
         initRequest();
     }
 
     private static void initRequest() {
         final AlwaysOpenMockHttpServletRequest request = new AlwaysOpenMockHttpServletRequest();
         request.addHeader("User-Agent",
                           "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11");
         MyRequestContextListener.initRequest(request);
     }
 
     static class AlwaysOpenMockHttpServletRequest extends MockHttpServletRequest {
         private HttpSession session;
         @Override
         public HttpSession getSession(boolean create) {
             if (session == null && create) {
                 session = new MockHttpSession();
             }
             return session;
         }
         @Override
         public String getContextPath() {
             return null;
         }
         @Override
         public void close() {
         }
     }
 
     public static void requestStarted() {
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         //requestAttributes.requestCompleted();
         initRequest();
     }
 
     public static void requestCompleted() {
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         //requestAttributes.requestCompleted();
     }
 
     private static final String REQUEST_ATTRIBUTES_ATTRIBUTE = RequestContextListener.class.getName() + ".REQUEST_ATTRIBUTES";
 
     static class LocalAttrs {
         private final RequestAttributes requestAttributes;
         private final Locale locale;
 
         LocalAttrs(final RequestAttributes requestAttributes, final Locale locale) {
             this.requestAttributes = requestAttributes;
             this.locale = locale;
         }
 
         public RequestAttributes getCopyOfRequestAttributes() {
             if (requestAttributes instanceof ServletRequestAttributes) {
                 final ServletRequestAttributes original = (ServletRequestAttributes) requestAttributes;
                 ServletRequestAttributes result;
                 HttpServletRequest nextRequest;
                 if (original.getRequest() instanceof MockHttpServletRequest)  {
                    // Mock request, just clone it.
                    nextRequest =  new MockHttpServletRequest();  // Maybe need to clone.
                 } else {
                    nextRequest = original.getRequest();
                 }
 
                 result = new ServletRequestAttributes(nextRequest);
 
                 nextRequest.setAttribute(REQUEST_ATTRIBUTES_ATTRIBUTE,
                                          original.getRequest().getAttribute(REQUEST_ATTRIBUTES_ATTRIBUTE));
 
                 return result;
             }
             return requestAttributes; // Maybe
         }
 
         public Locale getLocale() {
             return locale;
         }
     }

 }
