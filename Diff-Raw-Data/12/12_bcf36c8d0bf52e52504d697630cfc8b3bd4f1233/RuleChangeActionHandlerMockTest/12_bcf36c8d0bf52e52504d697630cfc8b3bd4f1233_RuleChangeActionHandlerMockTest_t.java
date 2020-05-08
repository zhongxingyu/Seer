 package mock;
 
 import junit.framework.TestCase;
 
 import org.drools.compiler.DroolsParserException;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import services.NomicService;
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventBus;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import actionHandlers.RuleChangeActionHandler;
 import actions.ProposeRuleAddition;
 import actions.ProposeRuleModification;
 import agents.NomicAgent;
 
 @RunWith(JMock.class)
 public class RuleChangeActionHandlerMockTest extends TestCase {
 	Mockery context = new JUnit4Mockery();
 	
 	final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
 	final EnvironmentServiceProvider serviceProvider = context.mock(EnvironmentServiceProvider.class);
 	final EventBus e = context.mock(EventBus.class);
 	final EnvironmentSharedStateAccess sharedState = context.mock(EnvironmentSharedStateAccess.class);
 	
 	@Test
 	public void canHandleTest() {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		String newRule = "Test rule";
 		
 		String oldRuleName = "Old Rule";
 		
 		String oldRulePackage = "Old rule package";
 		
 		Action genericAction = context.mock(Action.class);
 		
 		ProposeRuleAddition addition = new ProposeRuleAddition(0, mockAgent, newRule);
 		
 		ProposeRuleModification modification = new ProposeRuleModification(0, mockAgent, newRule, oldRuleName, oldRulePackage);
 		
 		RuleChangeActionHandler handler = new RuleChangeActionHandler(session, serviceProvider);
 		
 		assertTrue(handler.canHandle(addition));
 		assertTrue(handler.canHandle(modification));
 		assertFalse(handler.canHandle(genericAction));
 	}
 	
 	@Test
 	public void handleTest() throws DroolsParserException, UnavailableServiceException {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final NomicService service = context.mock(NomicService.class);
 		
 		NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		final String newRule = "Test rule";
 		
 		final String oldRuleName = "Old Rule";
 		
 		final String oldRulePackage = "Old rule package";
 		
		final Action genericAction = context.mock(Action.class);
 		
		final ProposeRuleAddition addition = new ProposeRuleAddition(0, mockAgent, newRule);
 		
		final ProposeRuleModification modification = new ProposeRuleModification(0, mockAgent, newRule, oldRuleName, oldRulePackage);
 		
 		context.checking(new Expectations() {{
 			oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); will(returnValue(service));
 			oneOf(service).addRule(newRule);
			oneOf(session).insert(addition);
 		}});
 		
 		RuleChangeActionHandler handler = new RuleChangeActionHandler(session, serviceProvider);
 		
 		try {
 			handler.handle(addition, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle rule addition.");
 		}
 		
 		context.checking(new Expectations() {{
 			oneOf(service).RemoveRule(oldRulePackage, oldRuleName);
 			oneOf(service).addRule(newRule);
			oneOf(session).insert(modification);
 		}});
 		
 		try {
 			handler.handle(modification, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle rule modification");
 		}
 		
 		try {
 			handler.handle(genericAction, Random.randomUUID());
 			fail("Ate wrongly formatted action.");
 		} catch (ActionHandlingException e) {
 			
 		}
 		
 		context.assertIsSatisfied();
 	}
 }
