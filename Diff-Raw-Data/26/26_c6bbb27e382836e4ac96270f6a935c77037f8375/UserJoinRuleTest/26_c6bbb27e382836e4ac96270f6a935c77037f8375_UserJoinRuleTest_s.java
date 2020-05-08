 package de.fernuni.pi3.interactionmanager.rules.sensingeventmanager;
 
 import org.junit.Before;
 
 import de.fernuni.pi3.interactionmanager.AbstractRuleTest;
 import de.fernuni.pi3.interactionmanager.Event;
 import de.fernuni.pi3.interactionmanager.InstanceVars;
 import de.fernuni.pi3.interactionmanager.rules.Rule;
 import de.fernuni.pi3.interactionmanager.rules.sensingeventmanager.UserJoinRule;
 
 public class UserJoinRuleTest extends AbstractRuleTest {
 
	private static final String inEventJson = "{\"name\":\"participant\",\"appType\":\"SensingEventManager\",\"appInstanceId\":\"50924677bbcdaaa713000001\",\"properties\":{},\"customVars\":{\"eventType\":\"userJoin\"}}"; 
 	
 	@Override
 	@Before
 	public void setUpTestData() {
 		
 		// test a: participant count set
 		// given
 		Event givenEvent = Event.fromJson(inEventJson);
 		InstanceVars givenInstanceVars = new InstanceVars();
 		givenInstanceVars.put("PARTICIPANT_COUNT", 2);
 		
 		// expected
 		Event expectedEvent = new Event();
 		InstanceVars expectedInstanceVars = new InstanceVars();
 		expectedInstanceVars.put("PARTICIPANT_COUNT", 3);
 		
 		addTestData(givenEvent, givenInstanceVars, expectedEvent, expectedInstanceVars);
 		
 		
 		// test b: participant count not set
 		InstanceVars givenInstanceVars1 = new InstanceVars();
 		InstanceVars expectedInstanceVars1 = new InstanceVars();
 		expectedInstanceVars1.put("PARTICIPANT_COUNT", 1);
 		
 		addTestData(givenEvent, givenInstanceVars1, expectedEvent, expectedInstanceVars1);
 	}
 
 	@Override
 	protected Rule getRule() {
 		return new UserJoinRule();
 	}
 
 }
