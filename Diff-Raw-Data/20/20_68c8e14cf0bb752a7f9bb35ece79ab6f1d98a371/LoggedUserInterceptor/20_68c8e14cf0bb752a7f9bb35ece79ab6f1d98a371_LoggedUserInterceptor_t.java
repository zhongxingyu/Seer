 package br.com.zeng.interceptor;
 
 import java.util.Arrays;
 
 import br.com.caelum.vraptor.InterceptionException;
 import br.com.caelum.vraptor.Intercepts;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.core.InterceptorStack;
 import br.com.caelum.vraptor.interceptor.Interceptor;
 import br.com.caelum.vraptor.resource.ResourceMethod;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 import br.com.zeng.annotation.LoggedUser;
 import br.com.zeng.controller.UserController;
 import br.com.zeng.session.UserSession;
 
 @Intercepts
 public class LoggedUserInterceptor implements Interceptor {
 
     private final UserSession userSession;
     private final Result result;
 
     public LoggedUserInterceptor(UserSession userSession, Result result) {
         this.userSession = userSession;
         this.result = result;
     }
 
     @Override
     public boolean accepts(ResourceMethod method) {
         return method.getMethod().isAnnotationPresent(LoggedUser.class);
     }
 
     @Override
     public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance)
             throws InterceptionException {
         if (!userSession.isLogged()) {
             result.include("errors", Arrays.asList(new ValidationMessage(
                     "Você precisa efetuar login para visualizar essa página", "erro")));
            result.forwardTo(UserController.class).home();
         } else {
             stack.next(method, resourceInstance);
         }
     }
 }
