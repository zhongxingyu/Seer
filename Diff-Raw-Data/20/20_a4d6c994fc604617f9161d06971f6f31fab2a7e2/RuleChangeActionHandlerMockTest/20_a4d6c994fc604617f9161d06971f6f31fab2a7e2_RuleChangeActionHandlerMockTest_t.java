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
 
 import exceptions.InvalidRuleProposalException;
 
 import services.NomicService;
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventBus;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import actionHandlers.ProposeRuleChangeActionHandler;
 import actions.ProposeRuleAddition;
 import actions.ProposeRuleModification;
 import actions.ProposeRuleRemoval;
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
 		
 		String newRuleName = "Test";
 		
 		String newRule = "Test rule";
 		
 		String oldRuleName = "Old Rule";
 		
 		String oldRulePackage = "Old rule package";
 		
 		Action genericAction = context.mock(Action.class);
 		
 		ProposeRuleAddition addition = new ProposeRuleAddition(mockAgent, newRuleName, newRule);
 		
 		ProposeRuleModification modification = new ProposeRuleModification(mockAgent, newRuleName, 
 				newRule, oldRuleName, oldRulePackage);
 		
 		ProposeRuleRemoval removal = new ProposeRuleRemoval(mockAgent, oldRuleName, oldRulePackage);
 		
 		ProposeRuleChangeActionHandler handler = new ProposeRuleChangeActionHandler(serviceProvider);
 		
 		assertTrue(handler.canHandle(addition));
 		assertTrue(handler.canHandle(modification));
 		assertTrue(handler.canHandle(removal));
 		assertFalse(handler.canHandle(genericAction));
 	}
 	
 	@Test
 	public void handleRemovalTest() throws DroolsParserException, UnavailableServiceException, InvalidRuleProposalException {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final NomicService service = context.mock(NomicService.class);
 		
 		NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		final String RemoveRuleName = "Removed Rule";
 		
 		final String RemoveRulePackage = "Removed Rule Package";
 		
 		final ProposeRuleRemoval removal = new ProposeRuleRemoval(mockAgent, RemoveRuleName, RemoveRulePackage);
 		
 		context.checking(new Expectations() {{
 			oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); will(returnValue(service));
 			oneOf(service).getTurnNumber();
			oneOf(service).getSimTime();
 			oneOf(service).ProposeRuleChange(removal);
 			oneOf(service).getActiveStatefulKnowledgeSession(); will(returnValue(session));
 			oneOf(session).insert(removal);
 		}});
 		
 		ProposeRuleChangeActionHandler handler = new ProposeRuleChangeActionHandler(serviceProvider);
 		
 		try {
 			handler.handle(removal, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle rule removal");
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void handleModificationTest() throws DroolsParserException, UnavailableServiceException, InvalidRuleProposalException {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final NomicService service = context.mock(NomicService.class);
 		
 		NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		final String newRuleName = "Test";
 		
 		final String newRule = "Test rule";
 		
 		final String oldRuleName = "Old Rule";
 		
 		final String oldRulePackage = "Old rule package";
 		
 		final ProposeRuleModification modification = new ProposeRuleModification(mockAgent, newRuleName, 
 				newRule, oldRuleName, oldRulePackage);
 		
 		context.checking(new Expectations() {{
 			oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); will(returnValue(service));
 			oneOf(service).getTurnNumber();
			oneOf(service).getSimTime();
 			oneOf(service).ProposeRuleChange(modification);
 			oneOf(service).getActiveStatefulKnowledgeSession(); will(returnValue(session));
 			oneOf(session).insert(modification);
 		}});
 		
 		ProposeRuleChangeActionHandler handler = new ProposeRuleChangeActionHandler(serviceProvider);
 		
 		try {
 			handler.handle(modification, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle rule modification");
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void handleAdditionTest() throws DroolsParserException, UnavailableServiceException, InvalidRuleProposalException {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final NomicService service = context.mock(NomicService.class);
 		
 		NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		final String newRuleName = "Test";
 		
 		final String newRule = "Test rule";
 		
 		final ProposeRuleAddition addition = new ProposeRuleAddition(mockAgent, newRuleName, newRule);
 		
 		context.checking(new Expectations() {{
 			oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); will(returnValue(service));
 			oneOf(service).getTurnNumber();
			oneOf(service).getSimTime();
 			oneOf(service).ProposeRuleChange(addition);
 			oneOf(service).getActiveStatefulKnowledgeSession(); will(returnValue(session));
 			oneOf(session).insert(addition);
 		}});
 		
 		ProposeRuleChangeActionHandler handler = new ProposeRuleChangeActionHandler(serviceProvider);
 		
 		try {
 			handler.handle(addition, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle rule addition.");
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void handleBadlyFormattedActionTest() throws UnavailableServiceException {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final Action genericAction = context.mock(Action.class);
 		
 		final NomicService service = context.mock(NomicService.class);
 		
 		context.checking(new Expectations() {{
 			oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); will(returnValue(service));
 			oneOf(service).getActiveStatefulKnowledgeSession();
 		}});
 		
 		ProposeRuleChangeActionHandler handler = new ProposeRuleChangeActionHandler(serviceProvider);
 		
 		try {
 			handler.handle(genericAction, Random.randomUUID());
 			fail("Ate wrongly formatted action.");
 		} catch (ActionHandlingException e) {
 			
 		}
 		
 		context.assertIsSatisfied();
 	}
 }
