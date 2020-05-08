 package com.aciertoteam.i18n;
 
 import com.aciertoteam.common.service.EntityService;
 import com.aciertoteam.geo.IpDetector;
 import com.aciertoteam.geo.entity.Country;
 import com.aciertoteam.geo.entity.Language;
 import com.aciertoteam.geo.services.GeoIpService;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.Serializable;
 import java.util.Locale;
 
 /**
  * User session locale holder. TODO get locale from user country
  * 
  * @author ishestiporov
  */
 public class UserSessionLocale implements Serializable, InitializingBean {
 
     private static final long serialVersionUID = 1L;
 
     private Locale defaultLocale = Locale.getDefault();
 
     private Locale locale;
 
     @Autowired
     private GeoIpService geoIpService;
 
     @Autowired
     private IpDetector ipDetector;
 
     @Autowired
     private EntityService entityService;
 
     private void defineDefaultLocale(HttpServletRequest request) {
         Country country = geoIpService.defineCountry(ipDetector.getIpAddress(request));
         setLocale(getLocale(country));
     }
 
     public Locale getLocale(Country country) {
         Language defaultLanguage = entityService.findByField(Language.class, "code", "en");
         Language countryLanguage = country.getDefaultLanguage(country);
         Language language = countryLanguage == null ? defaultLanguage : countryLanguage;
         return new Locale(language.getCode(), language.getCountryCode());
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public Locale getLocale() {
         return locale;
     }
 
     /**
      * Returns user set locale or the default one if user session locale is not
      * set.
      */
     public Locale getResolvedLocale() {
         return locale != null ? locale : defaultLocale;
     }
 
     public Locale getDefaultLocale() {
         return defaultLocale;
     }
 
     public void setDefaultLocale(Locale defaultLocale) {
         this.defaultLocale = defaultLocale;
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
        if (RequestContextHolder.getRequestAttributes() != null) {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                defineDefaultLocale(request);
            }
        }    }
 }
