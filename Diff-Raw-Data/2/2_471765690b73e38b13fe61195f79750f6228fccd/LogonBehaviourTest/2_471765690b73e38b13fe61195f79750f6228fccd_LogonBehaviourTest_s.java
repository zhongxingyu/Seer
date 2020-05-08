 package eugene.market.client.api.impl;
 
 import eugene.market.client.api.Session;
 import jade.core.AID;
 import jade.core.Agent;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.testng.PowerMockObjectFactory;
 import org.powermock.modules.testng.PowerMockTestCase;
 import org.testng.IObjectFactory;
 import org.testng.annotations.ObjectFactory;
 import org.testng.annotations.Test;
 
 import static org.powermock.api.mockito.PowerMockito.mock;
 
 /**
  * Tests {@link LogonBehaviour}.
  *
  * @author Jakub D Kozlowski
  * @since 0.4
  */
@PrepareForTest({Agent.class, AID.class})
 public class LogonBehaviourTest extends PowerMockTestCase {
 
     @Test(expectedExceptions = NullPointerException.class)
     public void testConstructorNullAgent() {
         new LogonBehaviour(null, mock(Session.class));
     }
 
     @Test(expectedExceptions = NullPointerException.class)
     public void testConstructorNullSession() {
         new LogonBehaviour(mock(Agent.class), null);
     }
 
     @ObjectFactory
     public IObjectFactory getObjectFactory() {
         return new PowerMockObjectFactory();
     }
 }
