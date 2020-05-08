 package org.northstar.bricks.web.auth;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.sitebricks.headless.Reply;
 import com.google.sitebricks.headless.Request;
 import com.google.sitebricks.routing.Redirect;
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.northstar.bricks.config.URIContext;
 
 /**
  * Created with IntelliJ IDEA.
  * User: 北辰
  * Date: 12-9-20
  * Time: 下午11:01
  * To change this template use File | Settings | File Templates.
  */
 class SecureMethodInterceptor implements MethodInterceptor {
     @Inject
     private Provider<Identity> identityProvider;
    /*@Inject
    private Provider<Redirect> redirectProvider;*/
     @Inject
     private Provider<Request> requestProvider;
 
     @Override
     public Object invoke(MethodInvocation invocation) throws Throwable {
         Identity identity = identityProvider.get();
         String context = requestProvider.get().context();
         String target = requestProvider.get().uri();
         StringBuilder path = new StringBuilder(context).append(URIContext.LOGIN_PAGE).append("?target=").append(target);
         if (!identity.isAuthenticated()) {
             if (Reply.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
                 return Reply.saying().redirect(path.toString());
             }
             //return redirectProvider.get().to(LoginPage.class, ImmutableMap.of("target", uri));
             return path.toString();
         }
         return invocation.proceed();
     }
 }
