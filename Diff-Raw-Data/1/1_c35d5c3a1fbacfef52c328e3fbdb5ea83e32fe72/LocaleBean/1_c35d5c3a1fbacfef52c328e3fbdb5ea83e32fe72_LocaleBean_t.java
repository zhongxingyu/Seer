 package be.cegeka.rsvz;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpServletRequest;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 @ManagedBean
 @SessionScoped
 public class LocaleBean implements Serializable {
 
     private static final long serialVersionUID = 1L;
     private static final Logger LOG = LoggerFactory.getLogger(LocaleBean.class);
 
     private Locale locale;
     private List<Locale> locales;
 
     public LocaleBean() {
         this.locales = extractLocales();
         this.locale = extractBrowserLocale();
         LOG.info("Locale set to {}", locale);
         LOG.info("Available locales: {}", locales);
     }
 
     public Locale getLocale() {
         return locale;
     }
 
     public String getLanguage() {
         return locale.toString();
     }
 
     public List<SelectItem> getLanguages() {
         List<SelectItem> languages = new ArrayList<SelectItem>();
         for (Locale locale : locales) {
             languages.add(localeToSelectItem(locale));
         }
         return languages;
     }
 
     public void setLanguage(String language) {
         locale = extractLocale(language);
         FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
     }
 
     private Locale extractBrowserLocale() {
         Locale locale = ((HttpServletRequest) (FacesContext.getCurrentInstance().
                 getExternalContext().getRequest())).getLocale();
         if (locales.contains(locale)) {
             return locale;
         } else {
             return getDefaultLocale();
         }
     }
 
     private Locale getDefaultLocale() {
         return FacesContext.getCurrentInstance().getApplication().getDefaultLocale();
     }
 
     private Locale extractLocale(String localeCode) {
         String language = localeCode.substring(0, 2);
         String country;
         if (localeCode.length() > 2) {
             country = localeCode.substring(3, 5);
         } else {
             country = "";
         }
         LOG.info("Setting locale to language={} country={}", language, country);
         return new Locale(language, country);
     }
 
     private List<Locale> extractLocales() {
         List<Locale> locales = new ArrayList<Locale>();
         locales.add(getDefaultLocale());
         Iterator<Locale> i = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
         while (i.hasNext()) {
             locales.add(i.next());
         }
         return locales;
     }
 
     private SelectItem localeToSelectItem(Locale locale) {
         if (locale != null) {
             return new SelectItem(locale.toString(), locale.getDisplayName());
         }
         return null;
     }
 
 }
