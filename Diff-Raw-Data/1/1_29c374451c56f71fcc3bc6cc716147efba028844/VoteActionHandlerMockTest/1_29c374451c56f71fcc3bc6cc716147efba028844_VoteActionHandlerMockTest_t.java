 package mock;
 
 import junit.framework.TestCase;
 
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
 import actionHandlers.VoteActionHandler;
 import actions.Vote;
 import agents.NomicAgent;
 import enums.VoteType;
 
 @RunWith(JMock.class)
 public class VoteActionHandlerMockTest extends TestCase {
 	Mockery context = new JUnit4Mockery();
 	
 	final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
 	final EnvironmentServiceProvider serviceProvider = context.mock(EnvironmentServiceProvider.class);
 	final EventBus e = context.mock(EventBus.class);
 	final EnvironmentSharedStateAccess sharedState = context.mock(EnvironmentSharedStateAccess.class);
 	
 	@Test
 	public void canHandleTest() {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		
 		final NomicAgent mockAgent = context.mock(NomicAgent.class);
 		
 		VoteActionHandler handler = new VoteActionHandler(serviceProvider);
 		
 		Vote yes = new Vote(mockAgent, VoteType.YES);
 		
 		Vote no = new Vote(mockAgent, VoteType.NO);
 		
 		Action genericAction = context.mock(Action.class);
 		
 		assertTrue(handler.canHandle(yes));
 		assertTrue(handler.canHandle(no));
 		assertFalse(handler.canHandle(genericAction));
 	}
 	
 	@Test
 	public void handleTest() throws UnavailableServiceException {
 		context.setImposteriser(ClassImposteriser.INSTANCE);
 		final NomicAgent mockAgent = context.mock(NomicAgent.class);
 		final NomicService service = context.mock(NomicService.class);
 		
 		final Vote yes = new Vote(mockAgent, VoteType.YES);
 		
 		final Vote no = new Vote(mockAgent, VoteType.NO);
 		
 		final Action genericAction = context.mock(Action.class);
 		
 		VoteActionHandler handler = new VoteActionHandler(serviceProvider);
 		
 		context.checking(new Expectations() {{
 			oneOf(serviceProvider).getEnvironmentService(with(NomicService.class)); 
 			will(returnValue(service));
 			oneOf(service).Vote(yes);
 			oneOf(service).getTurnNumber();
			oneOf(service).getSimTime();
 			oneOf(service).getActiveStatefulKnowledgeSession(); will(returnValue(session));
 			oneOf(session).insert(yes);
 		}});
 		
 		try {
 			handler.handle(yes, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle yes vote");
 		}
 		
 		context.assertIsSatisfied();
 		
 		context.checking(new Expectations() {{
 			oneOf(service).Vote(no);
 			oneOf(service).getTurnNumber();
 			oneOf(session).insert(no);
 		}});
 		
 		try {
 			handler.handle(no, Random.randomUUID());
 		} catch (ActionHandlingException e) {
 			fail("Failed to handle no vote");
 		}
 		
 		context.assertIsSatisfied();
 		
 		try {
 			handler.handle(genericAction, Random.randomUUID());
 			fail("Ate exception for invalid action");
 		} catch (ActionHandlingException e) {
 			
 		}
 	}
 }
