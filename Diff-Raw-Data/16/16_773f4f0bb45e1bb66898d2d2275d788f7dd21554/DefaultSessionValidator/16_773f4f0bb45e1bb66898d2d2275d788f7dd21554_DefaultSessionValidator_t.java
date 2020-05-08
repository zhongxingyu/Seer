 package jp.troter.servlet.httpsession.spi.impl;
 
 import jp.troter.servlet.httpsession.UserHttpSession;
 import jp.troter.servlet.httpsession.UserHttpSessionHttpServletRequestWrapper;
 import jp.troter.servlet.httpsession.spi.SessionValidator;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 
 public class DefaultSessionValidator extends SessionValidator {
 
     protected static final String HTTPSESSION_REMOTE_ADDR
         = "jp.troter.servlet.httpsession.spi.impl.DefaultSessionValidator.REMOTE_ADDR";
     protected static final String HTTPSESSION_USER_AGENT
         = "jp.troter.servlet.httpsession.spi.impl.DefaultSessionValidator.USER_AGENT";
 
     protected String getMakerNameRemoteAddr() {
         return HTTPSESSION_REMOTE_ADDR;
     }
 
     protected String getMakerNameUserAgent() {
         return HTTPSESSION_USER_AGENT;
     }
 
     @Override
    public boolean isExistsMarker(UserHttpSession session) {
         return session.getAttribute(getMakerNameRemoteAddr()) != null
             || session.getAttribute(getMakerNameUserAgent()) != null;
     }
 
     @Override
     public boolean isValid(UserHttpSession session, UserHttpSessionHttpServletRequestWrapper request) {
         setupMaker(session, request);
 
         String requestRemoteAddr = ObjectUtils.toString(request.getRemoteAddr(), "");
         String requestUserAgent  = ObjectUtils.toString(request.getHeader("User-Agent"), "");
         String sessionRemoteAddr = ObjectUtils.toString(session.getAttribute(getMakerNameRemoteAddr()), "");
         String sessionUserAgent  = ObjectUtils.toString(session.getAttribute(getMakerNameUserAgent()), "");
 
         boolean hasPossibilityOfSessionHijack
             =  ! StringUtils.equals(sessionRemoteAddr, requestRemoteAddr)
             || ! StringUtils.equals(sessionUserAgent, requestUserAgent);
         if (hasPossibilityOfSessionHijack) {
             trace("Hijacked session.");
             trace("Request url is " + request.getRequestURI());
             trace("Session id is " + session.getId());
             trace("REQUEST: remote_addr[" + requestRemoteAddr + "] user_agent[" + requestUserAgent + "]");
             trace("SESSION: remote_addr[" + sessionRemoteAddr + "] user_agent[" + sessionUserAgent + "]");
         }
 
         return ! hasPossibilityOfSessionHijack;
     }
 
     protected void setupMaker(UserHttpSession session, UserHttpSessionHttpServletRequestWrapper request) {
         if (session.getAttribute(getMakerNameRemoteAddr()) == null) {
             session.setAttribute(getMakerNameRemoteAddr(),
                     ObjectUtils.toString(request.getRemoteAddr(), ""));
         }
         if (session.getAttribute(getMakerNameUserAgent()) == null) {
             session.setAttribute(getMakerNameUserAgent(),
                     ObjectUtils.toString(request.getHeader("User-Agent"), ""));
         }
     }
 
     public void trace(String message) {
         //TODO
     }
 }
