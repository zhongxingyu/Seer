 /*
  * Team : AGF AM / OSI / SI / BO
  *
  * Copyright (c) 2001 AGF Asset Management.
  */
 package net.codjo.mad.server.plugin;
 import net.codjo.agent.AclMessage;
 import net.codjo.agent.Aid;
 import net.codjo.agent.UserId;
 import net.codjo.agent.test.BehaviourTestCase;
 import net.codjo.agent.test.DummyAgent;
 import net.codjo.mad.common.message.InstituteAmbassadorProtocol;
 import net.codjo.mad.server.handler.HandlerListener;
 import net.codjo.mad.server.handler.HandlerListenerMock;
 import net.codjo.sql.server.ConnectionPoolMock;
 import net.codjo.test.common.LogString;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 /**
  * Classe de test de {@link InstituteAmbassadorParticipant}.
  */
 public class InstituteAmbassadorParticipantTest extends BehaviourTestCase {
     private static final String EXPECTED_TEMPLATE =
           "(( Language: " + AclMessage.OBJECT_LANGUAGE + " ) AND ( Protocol: "
           + InstituteAmbassadorProtocol.ID + " ))";
     private LogString logString = new LogString();
     private InstituteAmbassadorParticipant participant;
     private SecretaryGeneralAgentMock secretaryGeneral;
     private DummyAgent president = new DummyAgent();
     private UserId userId = UserId.createId("login", "password");
     private static final String PRESIDENT_NAME = "president";
     private static final String AMBASSADOR_FOR_PRESIDENT = "AmbassadorFor" + PRESIDENT_NAME;
 
 
     public void test_action() throws Exception {
         mockConnectionPool();
 
         acceptAgent("secretaryGeneral", secretaryGeneral);
         acceptAgent(PRESIDENT_NAME, president);
 
         assertFalse(participant.done());
         containerFixture.assertNotContainsAgent(AMBASSADOR_FOR_PRESIDENT);
 
         secretaryGeneral.mockReceiveContent(createInstituteMessage(userId));
         participant.action();
         assertFalse(participant.done());
 
         UserId realUserId = secretaryGeneral.getLastSentMessage().decodeUserId();
         assertNotNull("login OK", realUserId);
 
         secretaryGeneral.getLog().assertContent(
               String.format("agent.receive(%s)", EXPECTED_TEMPLATE) + ", " +
               String.format("getUser(%s)", userId.getLogin()) + ", " +
               String.format("getPool(%s)", realUserId.encode()) + ", " +
               "createHandlerMap(UserId(" + userId.getLogin() + "), {UserId, ConnectionPoolMock})" + ", " +
               String.format("agent.send(president, null, %s)", AMBASSADOR_FOR_PRESIDENT));
 
         AclMessage lastSentMessage = secretaryGeneral.getLastSentMessage();
         assertEquals(InstituteAmbassadorProtocol.ID, lastSentMessage.getProtocol());
 
         containerFixture.assertContainsAgent(AMBASSADOR_FOR_PRESIDENT);
 
         logString.assertContent(
               String.format("declare(president, %s, %s)", AMBASSADOR_FOR_PRESIDENT, realUserId.encode()));
 
         president.die();
 
         containerFixture.assertNotContainsAgent(AMBASSADOR_FOR_PRESIDENT);
     }
 
 
     public void test_ambassadorCreationError() throws Exception {
         mockGetUserFailure(new RuntimeException("Impossible de rcuprer le user !!!"));
 
         acceptAgent("secretaryGeneral", secretaryGeneral);
         acceptAgent(PRESIDENT_NAME, president);
 
         secretaryGeneral.mockReceiveContent(createInstituteMessage(userId));
 
         participant.action();
         containerFixture.assertNotContainsAgent(AMBASSADOR_FOR_PRESIDENT);
 
         secretaryGeneral.getLog().assertContent(
               String.format("agent.receive(%s)", EXPECTED_TEMPLATE),
               "agent.send(president, null, java.lang.RuntimeException: Impossible de rcuprer le user !!!)");
 
         AclMessage lastSentMessage = secretaryGeneral.getLastSentMessage();
         assertEquals(InstituteAmbassadorProtocol.ID, lastSentMessage.getProtocol());
     }
 
 
     @Override
     protected void doSetUp() throws MalformedURLException {
         secretaryGeneral = new SecretaryGeneralAgentMock();
         AmbassadorRemovalBehaviour removalBehaviour =
               new AmbassadorRemovalBehaviour() {
                   @Override
                   public void declare(Aid presidentAID, Aid ambassadorAID, UserId poolUserId) {
                       super.declare(presidentAID, ambassadorAID, poolUserId);
                       logString.call("declare",
                                      presidentAID.getLocalName(),
                                      ambassadorAID.getLocalName(),
                                      poolUserId.encode());
                   }
               };
         secretaryGeneral.addBehaviour(removalBehaviour);
 
         BackPack backPack =
               BackPackBuilder.init()
                     .setHandlerMapBuilder(new HandlerMapBuilderMock(secretaryGeneral.getLog()))
                     .setCastorConfig(new URL("http://pipo"))
                     .setHandlerListeners(Arrays.<HandlerListener>asList(new HandlerListenerMock(logString)))
                     .get();
         backPack.setHandlerExecutorFactory(new DefaultHandlerExecutorFactory());
 
         participant = new InstituteAmbassadorParticipant(backPack, removalBehaviour);
         participant.setAgent(secretaryGeneral);
     }
 
 
     private void mockConnectionPool() throws ClassNotFoundException {
         secretaryGeneral.getJdbcServiceHelperMock().mockGetPool(new ConnectionPoolMock());
     }
 
 
     private void mockGetUserFailure(RuntimeException exception) {
         secretaryGeneral.getSecurityServiceHelperMock().mockGetUserFailure(exception);
     }
 
 
     private AclMessage createInstituteMessage(UserId aUserId) {
         AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);
         aclMessage.setSender(president.getAID());
         aclMessage.encodeUserId(aUserId);
         return aclMessage;
     }
 }
