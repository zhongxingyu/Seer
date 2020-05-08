 package jp.troter.servlet.httpsession.spi;
 
 import jp.troter.servlet.httpsession.UserHttpSession;
 import jp.troter.servlet.httpsession.UserHttpSessionHttpServletRequestWrapper;
 
 public abstract class SessionValidator {
 
     private static final String SERVICE_CLASS_NAME
         = "jp.troter.servlet.httpsession.spi.SessionValidator";
     private static final String DEFAULT_IMPLEMENTATION_NAME
         = "jp.troter.servlet.httpsession.spi.impl.DefaultSessionValidator";
 
     private static SingleServiceLoader<SessionValidator> loader;
 
     private static synchronized SingleServiceLoader<SessionValidator> getLoader() {
         if (loader == null) {
             loader = new SingleServiceLoader<SessionValidator>(SERVICE_CLASS_NAME, DEFAULT_IMPLEMENTATION_NAME);
         }
         return loader;
     }
 
     public static SessionValidator newInstance() {
         return getLoader().newService();
     }
 
     /**
      * <p>Checks if validate maker is exists.</p>
      * validate maker check
      * @param session
      * @return <code>true</code> if validate maker is exists
      */
    public abstract boolean isExistsMarker(UserHttpSession session);
 
     /**
      * <p>Checks if session is valid.</p>
      * @param session
      * @param request
      * @return <code>true</code> if session is valid
      */
     public abstract boolean isValid(UserHttpSession session, UserHttpSessionHttpServletRequestWrapper request);
 }
