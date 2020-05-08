 package com.aciertoteam.i18n.interceptor;
 
 import java.util.Locale;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.aciertoteam.geo.IpDetector;
 import com.aciertoteam.geo.entity.Country;
 import com.aciertoteam.geo.services.GeoIpService;
 import com.aciertoteam.i18n.UserSessionLocale;
 import com.aciertoteam.i18n.message.LocalizedMessageSource;
 import org.springframework.util.StringUtils;
 import org.springframework.web.context.ContextLoader;
 import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class AciertoteamLocaleChangeInterceptor extends LocaleChangeInterceptor {
 
     private GeoIpService geoIpService;
 
     private IpDetector ipDetector;
 
     private LocalizedMessageSource messageSource;
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
             throws ServletException {
 
         String newLocale = request.getParameter(getParamName());
         if (newLocale != null) {
             Locale locale = StringUtils.parseLocaleString(newLocale);
             setUserLocale(locale);
         } else {
             Country country = geoIpService.defineCountry(ipDetector.getIpAddress(request));
            Locale locale = messageSource.getLocale(country);
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
 
     public void setGeoIpService(GeoIpService geoIpService) {
         this.geoIpService = geoIpService;
     }
 
     public void setIpDetector(IpDetector ipDetector) {
         this.ipDetector = ipDetector;
     }
 
     public void setMessageSource(LocalizedMessageSource messageSource) {
         this.messageSource = messageSource;
     }
 }
