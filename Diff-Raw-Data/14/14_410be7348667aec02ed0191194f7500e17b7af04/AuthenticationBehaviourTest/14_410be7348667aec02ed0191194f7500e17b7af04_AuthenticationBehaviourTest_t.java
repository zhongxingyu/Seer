 package net.codjo.security.server.login;
 import java.net.MalformedURLException;
 import net.codjo.agent.AclMessage;
 import net.codjo.agent.AgentMock;
 import net.codjo.agent.Aid;
 import net.codjo.agent.ContainerFailureException;
 import net.codjo.agent.UserId;
 import net.codjo.agent.test.BehaviourTestCase;
 import net.codjo.agent.test.DummyAgent;
 import net.codjo.plugin.common.session.SessionRefusedException;
 import net.codjo.security.common.BadLoginException;
 import net.codjo.security.common.SecurityLevel;
 import net.codjo.security.common.login.LoginAction;
 import net.codjo.security.common.login.LoginEvent;
 import net.codjo.security.common.login.LoginProtocol;
 import net.codjo.security.server.api.SecurityServiceHelperMock;
 import net.codjo.security.server.api.SessionManagerMock;
 import net.codjo.test.common.LogString;
 /**
  *
  */
 public class AuthenticationBehaviourTest extends BehaviourTestCase {
     private static final String EXPECTED_TEMPLATE =
           "(( Language: " + AclMessage.OBJECT_LANGUAGE + " ) AND ( Protocol: " + LoginProtocol.ID + " ))";
     private static final String LOGIN = "GABI";
     private static final String PASSWORD = "6e594b55386d41376c37593d";
     private static final String DEFAULT_SERVER_VERSION = "version-100";
     private static final String VERSION = "GABIVERSION";
     private static final String LDAP = "myLdap";
 
     private LogString logString = new LogString();
     private AuthenticationBehaviour authenticationBehaviour;
     private AgentMock serverLoginAgent = new AgentMock(logString);
     private DummyAgent clientLoginAgent = new DummyAgent();
     private SecurityServiceHelperMock serviceHelperMock;
 
 
     public void test_action() throws Exception {
         acceptServerLoginAgent();
         acceptAgent("clientLoginAgent", clientLoginAgent);
 
         assertFalse(authenticationBehaviour.done());
 
         serverLoginAgent.mockReceive(createLoginMessage(LOGIN, PASSWORD, null, VERSION, SecurityLevel.USER));
         authenticationBehaviour.action();
         assertFalse(authenticationBehaviour.done());
 
         LoginEvent loginEvent = (LoginEvent)serverLoginAgent.getLastSentMessage().getContentObject();
         UserId realUserId = loginEvent.getUserId();
         assertNotNull("login OK", realUserId);
 
         logString.assertAndClear(
               String.format("agent.receive(%s)", EXPECTED_TEMPLATE),
               "getHelper(SecurityService.NAME)",
               String.format("login(%s, %s, %s)", LOGIN, PASSWORD, SecurityLevel.USER),
               String.format("declare(clientLoginAgent, %s)", realUserId.getLogin()),
               "getHelper(SecurityService.NAME)",
               String.format("getUser(%s)", LOGIN),
               String.format("agent.send(clientLoginAgent, null, LoginEvent[userId='%s'])", realUserId.encode()));
 
         AclMessage lastSentMessage = serverLoginAgent.getLastSentMessage();
         assertEquals(LoginProtocol.ID, lastSentMessage.getProtocol());
         assertNotNull(((LoginEvent)lastSentMessage.getContentObject()).getUser());
     }
 
 
     public void test_action_withDomain() throws Exception {
         acceptServerLoginAgent();
         acceptAgent("clientLoginAgent", clientLoginAgent);
 
         assertFalse(authenticationBehaviour.done());
 
         serverLoginAgent.mockReceive(createLoginMessage(LOGIN,
                                                         PASSWORD,
                                                         LDAP,
                                                         VERSION,
                                                         SecurityLevel.SERVICE));
         authenticationBehaviour.action();
         assertFalse(authenticationBehaviour.done());
 
         LoginEvent loginEvent = (LoginEvent)serverLoginAgent.getLastSentMessage().getContentObject();
         UserId realUserId = loginEvent.getUserId();
         assertNotNull("login OK", realUserId);
 
         logString.assertAndClear(
               String.format("agent.receive(%s)", EXPECTED_TEMPLATE),
               "getHelper(SecurityService.NAME)",
               String.format("login(%s, %s, %s, %s)", LOGIN, PASSWORD, LDAP, SecurityLevel.SERVICE),
               String.format("declare(clientLoginAgent, %s)", realUserId.getLogin()), "getHelper(SecurityService.NAME)",
               String.format("getUser(%s)", LOGIN),
               String.format("agent.send(clientLoginAgent, null, LoginEvent[userId='%s'])", realUserId.encode()));
     }
 
 
     public void test_action_withoutMessage() throws Exception {
         acceptServerLoginAgent();
 
         assertFalse(authenticationBehaviour.done());
 
         authenticationBehaviour.action();
         assertFalse(authenticationBehaviour.done());
 
         logString.assertAndClear(String.format("agent.receive(%s)", EXPECTED_TEMPLATE));
     }
 
 
     private void acceptServerLoginAgent() throws ContainerFailureException {
         acceptAgent("serverLoginAgent", serverLoginAgent);
         logString.assertAndClear("setup()");
     }
 
 
     public void test_loginError() throws Exception {
         acceptServerLoginAgent();
         acceptAgent("clientLoginAgent", clientLoginAgent);
 
         serverLoginAgent.mockReceive(createLoginMessage("erreur",
                                                         "password",
                                                         null,
                                                         "version-xxx", SecurityLevel.USER));
         serviceHelperMock.mockLoginFailure(new BadLoginException("bad login"));
 
         authenticationBehaviour.action();
 
         logString.assertContent(
               String.format("agent.receive(%s)", EXPECTED_TEMPLATE),
               "getHelper(SecurityService.NAME)",
               "login(erreur, password, USER)",
               "agent.send(clientLoginAgent, null, LoginEvent[loginFailureException='bad login'])");
         AclMessage lastSentMessage = serverLoginAgent.getLastSentMessage();
         assertEquals(LoginProtocol.ID, lastSentMessage.getProtocol());
     }
 
 
     public void test_loginWithBadSecurityLevel() throws Exception {
         acceptServerLoginAgent();
         acceptAgent("clientLoginAgent", clientLoginAgent);
 
         assertFalse(authenticationBehaviour.done());
 
         try {
             serverLoginAgent.mockReceive(createLoginMessage(LOGIN, PASSWORD, null, VERSION, null));
             fail();
         }
         catch (Exception e) {
             assertEquals("Security level can't be null", e.getLocalizedMessage());
         }
     }
 
 
     public void test_clientVersionEqual() throws Exception {
         configureAgents(DEFAULT_SERVER_VERSION);
         LoginEvent loginEvent = sendMessageToServerLoginAgent(
               createLoginMessage(LOGIN, PASSWORD, null, DEFAULT_SERVER_VERSION, SecurityLevel.USER));
         assertFalse(loginEvent.hasFailed());
     }
 
 
     public void test_clientVersionLower() throws Exception {
         String clientVersion = "version-099";
         configureAgents(DEFAULT_SERVER_VERSION);
         LoginEvent loginEvent = sendMessageToServerLoginAgent(createLoginMessage(LOGIN,
                                                                                  PASSWORD,
                                                                                  null,
                                                                                  clientVersion,
                                                                                  SecurityLevel.USER));
         assertTrue(loginEvent.hasFailed());
 
         String exceptionMessage = "Version client incompatible avec le serveur.\n"
                                   + "Vous devez utiliser un client ayant une version gale ou suprieure  celle du serveur.\n"
                                   + "Version serveur : '" + DEFAULT_SERVER_VERSION + "'\n"
                                   + "Version client : '" + clientVersion + "'\n\n"
                                   + "Merci de bien vouloir contacter ASSET-SVP pour mettre  jour votre client.";
         logString.assertContent(
               String.format("agent.receive(%s)", EXPECTED_TEMPLATE),
               String.format("agent.send(clientLoginAgent, null, LoginEvent[loginFailureException='%s'])",
                             exceptionMessage));
         AclMessage lastSentMessage = serverLoginAgent.getLastSentMessage();
         assertEquals(LoginProtocol.ID, lastSentMessage.getProtocol());
     }
 
 
     public void test_clientVersionHigher() throws Exception {
         String clientVersion = "version-101";
         configureAgents(DEFAULT_SERVER_VERSION);
         LoginEvent loginEvent = sendMessageToServerLoginAgent(
               createLoginMessage(LOGIN, PASSWORD, null, clientVersion, SecurityLevel.USER));
         assertFalse(loginEvent.hasFailed());
     }
 
 
     public void test_clientWithValidIPHostname() throws Exception {
         configureAgents(DEFAULT_SERVER_VERSION);
         authenticationBehaviour.action();
         LoginEvent loginEvent = sendMessageToServerLoginAgent(
               createLoginMessage(LOGIN,
                                  PASSWORD,
                                  null,
                                  DEFAULT_SERVER_VERSION,
                                  "127.0.0.1",
                                  "localhost",
                                  SecurityLevel.USER));
         assertFalse(loginEvent.hasFailed());
     }
 
 
     public void test_clientWithInvalidIPHostname() throws Exception {
         configureAgents(DEFAULT_SERVER_VERSION);
         authenticationBehaviour.action();
         LoginEvent loginEvent = sendMessageToServerLoginAgent(
               createLoginMessage(LOGIN,
                                  PASSWORD,
                                  null,
                                  DEFAULT_SERVER_VERSION,
                                 "xxx.0.0.1",
                                  "localhost",
                                  SecurityLevel.USER));
         assertTrue(loginEvent.hasFailed());
     }
 
 
     public void test_badIpControlRejectFrenchClient() throws Exception {
         configureAgents(DEFAULT_SERVER_VERSION);
         authenticationBehaviour.setIpResolver(new AuthenticationBehaviour.IpResolver() {
             public String resolve(String ipAddress) {
                 return "A7WA284.am.agf.fr";
             }
         });
         authenticationBehaviour.action();
         LoginEvent loginEvent = sendMessageToServerLoginAgent(
               createLoginMessage(LOGIN,
                                  PASSWORD,
                                  null,
                                  DEFAULT_SERVER_VERSION,
                                  "142.12.12.12",
                                  "localhost",
                                  SecurityLevel.USER));
         assertTrue(loginEvent.hasFailed());
     }
 
 
     public void test_badIpControlAcceptForeignClient() throws Exception {
         configureAgents(DEFAULT_SERVER_VERSION);
         authenticationBehaviour.setIpResolver(new AuthenticationBehaviour.IpResolver() {
             public String resolve(String ipAddress) {
                return "183.12.12.12";
             }
         });
         authenticationBehaviour.action();
         LoginEvent loginEvent = sendMessageToServerLoginAgent(
               createLoginMessage(LOGIN,
                                  PASSWORD,
                                  null,
                                  DEFAULT_SERVER_VERSION,
                                  "183.12.12.12",
                                  "localhost",
                                  SecurityLevel.USER));
         assertFalse(loginEvent.hasFailed());
     }
 
 
     @Override
     protected void doSetUp() throws MalformedURLException {
         serviceHelperMock = new SecurityServiceHelperMock(logString);
         serverLoginAgent.mockGetHelper(serviceHelperMock);
         SessionLifecycleBehaviour sessionLifecycleBehaviour
               = new SessionLifecycleBehaviour(new SessionManagerMock(logString)) {
             @Override
             public void declare(Aid aid, UserId userId) throws SessionRefusedException {
                 logString.call("declare", aid.getLocalName(), userId.getLogin());
             }
         };
 
         authenticationBehaviour = new AuthenticationBehaviour(VERSION, sessionLifecycleBehaviour);
         authenticationBehaviour.setAgent(serverLoginAgent);
     }
 
 
     private AclMessage createLoginMessage(String login,
                                           String password,
                                           String optionalLdap,
                                           String version,
                                           SecurityLevel securityLevel) {
         return createLoginMessage(login, password, optionalLdap, version, null, null, securityLevel);
     }
 
 
     private AclMessage createLoginMessage(String login,
                                           String password,
                                           String optionalLDap, String version,
                                           String ip,
                                           String hostname,
                                           SecurityLevel securityLevel) {
         AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);
         aclMessage.setSender(clientLoginAgent.getAID());
         LoginAction action = new MyLoginAction(login, password, optionalLDap, version, ip, hostname, securityLevel);
         aclMessage.setContentObject(action);
         return aclMessage;
     }
 
 
     private void configureAgents(String serverVersion)
           throws ClassNotFoundException, ContainerFailureException {
         authenticationBehaviour.setServerVersion(serverVersion);
         acceptServerLoginAgent();
         acceptAgent("clientLoginAgent", clientLoginAgent);
     }
 
 
     private LoginEvent sendMessageToServerLoginAgent(AclMessage instituteMessage) {
         serverLoginAgent.mockReceive(instituteMessage);
         authenticationBehaviour.action();
         return (LoginEvent)serverLoginAgent.getLastSentMessage().getContentObject();
     }
 
 
     private static class MyLoginAction extends LoginAction {
         private final String hostname;
 
 
         private MyLoginAction(String login,
                               String password,
                               String optionalLdap,
                               String version,
                               String ip,
                               String hostname, SecurityLevel securityLevel) {
             super(login, password, optionalLdap, version, ip, securityLevel);
             this.hostname = hostname;
         }
 
 
         @Override
         public String getHostname() {
             return hostname;
         }
     }
 }
