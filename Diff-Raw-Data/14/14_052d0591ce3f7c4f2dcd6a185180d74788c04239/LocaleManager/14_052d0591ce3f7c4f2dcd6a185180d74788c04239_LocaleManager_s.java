 package org.rythmengine.jfinal;
 
 import com.jfinal.aop.Interceptor;
 import com.jfinal.core.ActionInvocation;
 import com.jfinal.core.Controller;
 import com.jfinal.log.Logger;
 import org.rythmengine.utils.Eval;
 import org.rythmengine.utils.S;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.Locale;
 
 /**
  * Manage locale setting as per each http request
  */
 public class LocaleManager implements Interceptor {
     private static final Logger log = Logger.getLogger(LocaleManager.class);
 
     private static ThreadLocal<Locale> locale = new ThreadLocal<Locale>(){
         @Override
         protected Locale initialValue() {
             return Locale.getDefault();
         }
     };
     
     private static String paramName = "locale";
     private static final String sessName = "rythm.locale";
 
     /**
      * Used by {@link com.jfinal.core.RythmPlugin} to init the http param name.
      * 
      * <p>Default value: "locale".</p>
      * @param paramName
      */
     public static void setLocaleParamName(String paramName) {
         if (S.notEmpty(paramName)) {
             LocaleManager.paramName = paramName;
         } else {
             log.warn("locale para name cannot be empty");
         }
     }
 
     /**
      * The API could be used by user application to override framework settings. For example,
      * instead of getting the locale information from http request, the framework could use
      * this API to set locale as per user's preference settings in the database
      * 
      * @param locale
      */
     public static void setLocale(Locale locale) {
         LocaleManager.locale.set(locale);
     }
 
     /**
      * Used by {@link RythmRender} to set locale context for the underline rendering process
      * @return the locale
      */
     public static Locale getLocale() {
         return locale.get();
     }
     
     @Override
     public void intercept(ActionInvocation ai) {
         Controller c = ai.getController();
         HttpServletRequest req = c.getRequest();
         HttpSession sess = c.getSession();
         locale.set(parse(req, sess));
         ai.invoke();
     }
 
     private Locale parse(HttpServletRequest req, HttpSession sess) {
         // check params first
         String s = req.getParameter(paramName);
         
         if (S.empty(s)) {
             // try session info then
             Locale loc = (Locale)sess.getAttribute(sessName);
             if (null != loc) {
                 return loc;
             }
             // finally try http header
             loc = req.getLocale();
            sess.setAttribute(sessName, loc);
             return loc;
         }
 
         Locale loc = Eval.locale(s);
         sess.setAttribute(sessName, loc);
         return loc;
     }
 }
