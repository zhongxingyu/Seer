 package unquietcode.tools.logmachine;
 
 import org.junit.Test;
 import unquietcode.tools.logmachine.core.LogEvent;
 import unquietcode.tools.logmachine.core.LogMachine;
 import unquietcode.tools.logmachine.core.formats.ShorterPlaintextFormatter;
 import unquietcode.tools.logmachine.core.topics.Topic;
 import unquietcode.tools.logmachine.impl.simple.SimpleLogMachine;
 import unquietcode.tools.logmachine.test.AbstractLoggerTest;
 
 import static org.junit.Assert.assertEquals;
 
 public class TestFormatters extends AbstractLoggerTest {
 	private static final ShorterPlaintextFormatter shortFMT = new ShorterPlaintextFormatter();
 
 	@Override
 	protected String getLoggerName() {
 		return TestFormatters.class.getName();
 	}
 
 	@Override
 	public LogMachine getLogMachine() {
 		return new SimpleLogMachine(TestFormatters.class);
 	}
 
 	@Test
 	public void testShortFormat_1() {
 		lm.fromHere().debug("hi");
 		LogEvent event = getSingleEvent();
 		StringBuilder result = shortFMT.format(event);
		assertEquals("[DEBUG] (testShortFormat_1) - hi", result.toString());
 	}
 
 	@Test
 	public void testShortFormat_2() {
 		lm.fromHere().to(TestTopics.One, TestTopics.Three).debug("hi");
 		LogEvent event = getSingleEvent();
 		StringBuilder result = shortFMT.format(event);
		assertEquals("[DEBUG] [One | Three] (testShortFormat_2) - hi", result.toString());
 	}
 
 	private enum TestTopics implements Topic {
 		One, Two, Three
 	}
 }
