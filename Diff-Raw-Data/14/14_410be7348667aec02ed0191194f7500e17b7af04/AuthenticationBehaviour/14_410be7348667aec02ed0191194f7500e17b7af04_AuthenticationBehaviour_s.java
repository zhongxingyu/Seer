 package net.codjo.security.server.login;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import net.codjo.agent.AclMessage;
 import net.codjo.agent.BadContentObjectException;
 import net.codjo.agent.MessageTemplate;
 import net.codjo.agent.ServiceException;
 import net.codjo.agent.UserId;
 import net.codjo.agent.behaviour.CyclicBehaviour;
 import net.codjo.security.common.api.User;
 import net.codjo.security.common.login.IncompatibleVersionException;
 import net.codjo.security.common.login.InvalidIPHostnameException;
 import net.codjo.security.common.login.LoginAction;
 import net.codjo.security.common.login.LoginEvent;
 import net.codjo.security.common.login.LoginProtocol;
 import net.codjo.security.server.api.SecurityServiceHelper;
 import org.apache.log4j.Logger;
 
 import static net.codjo.agent.AclMessage.OBJECT_LANGUAGE;
 import static net.codjo.agent.MessageTemplate.matchLanguage;
 import static net.codjo.agent.MessageTemplate.matchProtocol;
 /**
  *
  */
 class AuthenticationBehaviour extends CyclicBehaviour {
     private static final Logger LOG = Logger.getLogger(AuthenticationBehaviour.class);
 
     private String serverVersion;
     private final SessionLifecycleBehaviour sessionLifecycleBehaviour;
     private MessageTemplate requestTemplate;
     private AclMessage lastAuthenticationMessage;
     private IpResolver ipResolver = new DefaultIpResolver();
 
 
     AuthenticationBehaviour(String version, SessionLifecycleBehaviour sessionLifecycleBehaviour) {
         serverVersion = version;
         this.sessionLifecycleBehaviour = sessionLifecycleBehaviour;
 
         requestTemplate = MessageTemplate.and(matchLanguage(OBJECT_LANGUAGE),
                                               matchProtocol(LoginProtocol.ID));
     }
 
 
     @Override
     protected void action() {
         AclMessage myACLMessage = getAgent().receive(requestTemplate);
 
         if (myACLMessage == null) {
             block();
             return;
         }
         lastAuthenticationMessage = myACLMessage;
 
         LoginAction action;
         try {
             action = (LoginAction)myACLMessage.getContentObject();
         }
         catch (BadContentObjectException e) {
             LOG.error("Invalid content object", e);
             LoginEvent loginEvent = new LoginEvent(new IncompatibleVersionException(serverVersion, null));
             sendReplyMessage(myACLMessage, loginEvent);
             return;
         }
 
         String clientVersion = action.getVersion();
         if (serverVersion != null && (clientVersion == null || serverVersion.compareTo(clientVersion) > 0)) {
             Exception versionException = new IncompatibleVersionException(serverVersion, clientVersion);
             LOG.error(versionException.getMessage());
             sendReplyMessage(myACLMessage, new LoginEvent(versionException));
             return;
         }
 
         String clientIP = action.getIp();
         String clientHostname = action.getHostname();
         if (clientHostname != null && isLaptop(clientHostname)) {
             LOG.info("Laptop detected '" + action);
         }
         if (clientIP != null && clientHostname != null && !hasGoodIpResolution(clientIP, clientHostname)) {
             InvalidIPHostnameException invalidIPHostnameException
                   = new InvalidIPHostnameException(clientIP, clientHostname);
             LOG.warn(invalidIPHostnameException.getMessage());
             sendReplyMessage(myACLMessage, new LoginEvent(invalidIPHostnameException));
             return;
         }
 
         try {
             LOG.info("Trying to login with (user: " + action.getLogin() + ", securityLevel: "
                      + action.getSecurityLevel() + ")");
             UserId userId = getSecurityService().login(action.getLogin(),
                                                        action.getPassword(),
                                                        action.getLdap(), action.getSecurityLevel());
             sessionLifecycleBehaviour.declare(myACLMessage.getSender(), userId);
             User user = getSecurityService().getUser(userId);
 
             LOG.info(String.format("Login '%s' accepted", action.getLogin()));
 
             // Envoi de la rponse
             sendReplyMessage(myACLMessage, new LoginEvent(user, userId));
         }
         catch (Throwable loginFailure) {
             LOG.error(String.format("Login '%s' refused", action.getLogin()), loginFailure);
             sendReplyMessage(myACLMessage, new LoginEvent(loginFailure));
         }
     }
 
 
     public void setIpResolver(IpResolver ipResolver) {
         this.ipResolver = ipResolver;
     }
 
 
     private boolean isLaptop(String hostname) {
         return hostname.startsWith("A7L");
     }
 
 
     private boolean hasGoodIpResolution(String ip, String hostname) {
         try {
             InetAddress fromHostName = InetAddress.getByName(hostname);
             String actualHostName = fromHostName.getCanonicalHostName();
 
             String expectedHostName = ipResolver.resolve(ip);
             InetAddress fromIp = InetAddress.getByName(ip);
             boolean result = fromIp.getHostAddress().equals(ip)
                              && expectedHostName.equals(actualHostName);
             if (!result) {
                 LOG.warn("Expected host name: " + expectedHostName + ", actual host name: " + actualHostName);
                if (!expectedHostName.contains(".")) {
                     LOG.warn(
                           "Ip resolution ignored since hostname is not resolved by dns server: " + ip + ", host name: "
                           + hostname);
                     return true;
                 }
             }
             return result;
         }
         catch (Throwable throwable) {
             LOG.warn("Unattended exception", throwable);
             return false;
         }
     }
 
 
     void sendReplyMessage(AclMessage myACLMessage, LoginEvent loginEvent) {
         AclMessage aclMessage = myACLMessage.createReply();
         aclMessage.setContentObject(loginEvent);
         aclMessage.setLanguage(OBJECT_LANGUAGE);
         aclMessage.setProtocol(LoginProtocol.ID);
         getAgent().send(aclMessage);
     }
 
 
     private SecurityServiceHelper getSecurityService() throws ServiceException {
         return ((SecurityServiceHelper)getAgent().getHelper(SecurityServiceHelper.NAME));
     }
 
 
     void setServerVersion(String serverVersion) {
         this.serverVersion = serverVersion;
     }
 
 
     AclMessage getLastAuthenticationMessage() {
         return lastAuthenticationMessage;
     }
 
 
     interface IpResolver {
         String resolve(String ipAddress) throws UnknownHostException;
     }
 
     private class DefaultIpResolver implements IpResolver {
         public String resolve(String ipAddress) throws UnknownHostException {
             return InetAddress.getByName(ipAddress).getCanonicalHostName();
         }
     }
 }
