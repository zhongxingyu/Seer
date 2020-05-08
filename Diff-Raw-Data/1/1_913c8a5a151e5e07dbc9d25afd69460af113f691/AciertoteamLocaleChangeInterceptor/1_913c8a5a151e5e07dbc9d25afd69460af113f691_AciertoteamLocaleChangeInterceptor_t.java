 package com.aciertoteam.common.i18n;
 
 import java.util.Locale;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.util.StringUtils;
 import org.springframework.web.context.ContextLoader;
 import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class AciertoteamLocaleChangeInterceptor extends LocaleChangeInterceptor {
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
             throws ServletException {
         String newLocale = request.getParameter(getParamName());
         if (newLocale != null) {
             Locale locale = StringUtils.parseLocaleString(newLocale);
             setUserLocale(locale);
         }
         return super.preHandle(request, response, handler);
     }
 
     private void setUserLocale(Locale locale) {
         UserSessionLocale userSessionLocale = (UserSessionLocale) ContextLoader.getCurrentWebApplicationContext()
                 .getBean("userSessionLocale");
         if (userSessionLocale != null) {
             userSessionLocale.setLocale(locale);
         }
     }
 
 }
