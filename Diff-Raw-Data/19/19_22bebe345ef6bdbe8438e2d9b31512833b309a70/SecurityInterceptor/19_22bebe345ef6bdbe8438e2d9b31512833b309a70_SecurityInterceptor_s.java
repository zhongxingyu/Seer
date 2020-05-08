 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.fi.muni.pa165.calorycounter.frontend;
 
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.AuthUserDto;
 import net.sourceforge.stripes.action.ActionBeanContext;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.controller.ExecutionContext;
 import net.sourceforge.stripes.controller.Interceptor;
 import net.sourceforge.stripes.controller.Intercepts;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Martin Bryndza
  */
 @Intercepts(LifecycleStage.HandlerResolution)
 public class SecurityInterceptor implements Interceptor {
 
     final static Logger log = LoggerFactory.getLogger(AuthenticationActionBean.class);
 
     @Override
     public Resolution intercept(ExecutionContext ec) throws Exception {
         log.debug("intercept()");
         Resolution resolution = ec.proceed();
         Class currentBean = ec.getActionBean().getClass();
         if (currentBean.isAnnotationPresent(DoesNotRequireLogin.class)) {
             return resolution;
         }
         if (isLoggedIn(ec.getActionBeanContext())) {
             return resolution;
         } else {
             ActionBeanContext currentContext = ec.getActionBean().getContext();
            currentContext.getRequest().getSession().setAttribute("authPath", ec.getActionBeanContext().getRequest().getServletPath());
             return new RedirectResolution("/authentication/login.jsp");
         }
     }
 
     protected boolean isLoggedIn(ActionBeanContext abc) {
         AuthUserDto user = (AuthUserDto) abc.getRequest().getSession().getAttribute("user");
         return user != null;
     }
 
 }
