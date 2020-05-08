 package mock;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.drools.KnowledgeBase;
 import org.drools.compiler.DroolsParserException;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import services.NomicService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.event.EventBus;
 
 @RunWith(JMock.class)
 public class NomicServiceMockTest extends TestCase {
 	Mockery context = new JUnit4Mockery();
 	
 	@Test
 	public void NomicServiceSingleStringRuleAdditionTest() {
 		String newRule = "import agents.NomicAgent "
 				+ "rule \"Dynamic rule!\""
 				+ "when"
 				+ "	$agent : NomicAgent(SequentialID == 1)"
 				+ "then"
 				+ "	System.out.println(\"Found agent 1!\");"
 				+ "end";
 		
 		final EnvironmentSharedStateAccess ss = context.mock(EnvironmentSharedStateAccess.class);
 		final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
 		final EventBus e = context.mock(EventBus.class);
 		final KnowledgeBase base = context.mock(KnowledgeBase.class);
 		
 		context.checking(new Expectations() {{
 			oneOf(e).subscribe(with(any(NomicService.class)));
 			oneOf(session).getKnowledgeBase(); will(returnValue(base));
 			oneOf(base).addKnowledgePackages(with(any(Collection.class)));
 		}});
 		
 		final NomicService service = new NomicService(ss, session, e);
 		
 		try {
 			service.addRule(newRule);
 		} catch (DroolsParserException e1) {
 			fail("Rule was not parsed correctly.");
 		}
 		
 		context.assertIsSatisfied();
 	}
 	
 	@Test
 	public void NomicServiceMultipleStringRuleAdditionTest() {
 		final EnvironmentSharedStateAccess ss = context.mock(EnvironmentSharedStateAccess.class);
 		final StatefulKnowledgeSession session = context.mock(StatefulKnowledgeSession.class);
 		final EventBus e = context.mock(EventBus.class);
 		final KnowledgeBase base = context.mock(KnowledgeBase.class);
 		
 		context.checking(new Expectations() {{
			exactly(1).of(e).subscribe(with(any(NomicService.class)));
 			exactly(3).of(session).getKnowledgeBase(); will(returnValue(base));
 			exactly(3).of(base).addKnowledgePackages(with(any(Collection.class)));
 		}});
 		
 		final NomicService service = new NomicService(ss, session, e);
 		
 		ArrayList<String> imports = new ArrayList<String>();
 		imports.add("agents.NomicAgent");
 		
 		String ruleName = "Dynamic Rule!";
 		
 		ArrayList<String> conditions = new ArrayList<String>();
 		conditions.add("$agent : NomicAgent(SequentialID == 1)");
 		
 		ArrayList<String> actions = new ArrayList<String>();
 		actions.add("System.out.println(\"Found agent 1!\");");
 		
 		try {
 			service.addRule(imports, ruleName, conditions, actions);
 		} catch (DroolsParserException e1) {
 			fail("Simple rule was not parsed directly." + e1.getMessage());
 		}
 		
 		conditions.add("$agent2 : NomicAgent(SequentialID == 2)");
 		
 		try {
 			service.addRule(imports, ruleName, conditions, actions);
 		} catch (DroolsParserException e2) {
 			fail("Multiple condition failure.\n" + e2.getMessage());
 		}
 		
 		actions.add("System.out.println(\"Testing multiple actions\");");
 		
 		try {
 			service.addRule(imports, ruleName, conditions, actions);
 		} catch (DroolsParserException e2) {
 			fail("Multiple actions failure.\n" + e2.getMessage());
 		}
 		
 		context.assertIsSatisfied();
 	}
 }
