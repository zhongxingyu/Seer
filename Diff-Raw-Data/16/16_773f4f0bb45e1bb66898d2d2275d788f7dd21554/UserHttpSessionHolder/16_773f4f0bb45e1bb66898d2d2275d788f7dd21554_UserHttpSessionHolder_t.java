 package jp.troter.servlet.httpsession;
 
 import jp.troter.servlet.httpsession.spi.SessionIdGenerator;
 import jp.troter.servlet.httpsession.spi.SessionStateManager;
 import jp.troter.servlet.httpsession.spi.SessionValidator;
 
 
 public class UserHttpSessionHolder {
 
     protected UserHttpSessionHttpServletRequestWrapper request;
 
     protected SessionStateManager sessionStateManager;
 
     protected UserHttpSession session;
 
     protected boolean sessionInvalidate = false;
 
     protected SessionValidator sessionValidator;
 
     protected SessionIdGenerator sessionIdGenerator;
 
     protected int retryLimit = 10;
 
     public UserHttpSessionHolder(UserHttpSessionHttpServletRequestWrapper request, SessionStateManager sessionStateManager) {
         this.request = request;
         this.sessionStateManager = sessionStateManager;
     }
 
     public UserHttpSession getUserHttpSession() {
         return session;
     }
 
     public UserHttpSession getSession() {
         return getSession(true);
     }
 
     public UserHttpSession getSession(boolean create) {
         // session already exists.
         if (session != null) { return session; }
 
         // session not exists, and not create
         if (!create) { return null; }
 
         boolean needNewSession = false;
         String sessionId = request.getRequestedSessionId();
         if (sessionId == null || invalidate()) {
             needNewSession = true;
             sessionId = getSessionIdGenerator().generateSessionId(request);
         }
 
         session = createHttpSession(sessionId, needNewSession);
         sessionInvalidate = false;
 
         return session;
     }
 
     protected UserHttpSession createHttpSession(final String sessionId, final boolean needNewSession) {
         String currentSessionId = sessionId;
         boolean isNew = needNewSession;
         int retry = 0;
         while (true) {
             if (retry > retryLimit) {
                 throw new RuntimeException("cannot create session.");
             }
             UserHttpSession session = new UserHttpSession(request, currentSessionId, sessionStateManager, isNew);
 
            boolean isConflictSessionId = isNew && getSessionValidator().isExistsMarker(session);
             if (isConflictSessionId || ! getSessionValidator().isValid(session, request)) {
                 session.invalidate();
                 currentSessionId = getSessionIdGenerator().generateSessionId(request, retry);
                 isNew = true;
                 retry++;
                 continue;
             }
 
             // session is valid. return.
             return session;
         }
     }
 
     /**
      * invalidate holding session.
      */
     public void sessionInvalidate() {
         session = null;
         sessionInvalidate = true;
     }
 
     public boolean invalidate() {
         return sessionInvalidate;
     }
 
     protected SessionValidator getSessionValidator() {
         if (sessionValidator == null) {
             sessionValidator = SessionValidator.newInstance();
         }
         return sessionValidator;
     }
 
     protected SessionIdGenerator getSessionIdGenerator() {
         if (sessionIdGenerator == null) {
             sessionIdGenerator = SessionIdGenerator.newInstance();
         }
         return sessionIdGenerator;
     }
 }
