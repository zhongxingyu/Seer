 package unquietcode.tools.logmachine;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import unquietcode.tools.logmachine.core.LogEvent;
 import unquietcode.tools.logmachine.core.LogMachine;
 import unquietcode.tools.logmachine.core.topics.Topic;
 import unquietcode.tools.logmachine.impl.simple.SimpleLogMachine;
 import unquietcode.tools.logmachine.test.AbstractLoggerTest;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Ben Fagin
  * @version 10-22-2012
  */
 public class TestEventMetadata extends AbstractLoggerTest {
 
 	enum PrimaryColor implements Topic {
 		Blue, Red, Yellow,
 	}
 
 	enum SecondaryColor implements Topic {
 		Green, Purple, Orange
 	}
 
 	@Override
 	protected String getLoggerName() {
 		return TestEventMetadata.class.getName();
 	}
 
 	@Override
 	public LogMachine getLogMachine() {
 		return new SimpleLogMachine(TestEventMetadata.class);
 	}
 
 	@Test
 	public void testEventCategories() {
 		class Mixer {
 			void assertColor(String name, PrimaryColor...colors) {
 				LogEvent event = getSingleEvent();
 				List<Topic> groups = event.getGroups();
 				assertEquals(colors.length, groups.size());
 
 				for (PrimaryColor color : colors) {
 					assertTrue(groups.contains(color));
 				}
 
 				assertEquals(name, event.getMessage());
 			}
 		}
 
 		final Mixer mixer = new Mixer();
 
 		lm.warn().to(PrimaryColor.Red, PrimaryColor.Yellow).send("Orange");
 		mixer.assertColor("Orange", PrimaryColor.Yellow, PrimaryColor.Red);
 
 		lm.error().to(PrimaryColor.Blue, PrimaryColor.Yellow).send("Green");
 		mixer.assertColor("Green", PrimaryColor.Yellow, PrimaryColor.Blue);
 
 		lm.to(PrimaryColor.Red, PrimaryColor.Blue).info("Purple");
 		mixer.assertColor("Purple", PrimaryColor.Blue, PrimaryColor.Red);
 	}
 
 	@Test
 	public void testHeterogeneousEnums() {
 		lm.to(PrimaryColor.Red, PrimaryColor.Yellow, SecondaryColor.Orange).info("Orange");
 
 		LogEvent event = getSingleEvent();
 		List<Topic> groups = event.getGroups();
 
 		assertEquals(3, groups.size());
 		assertTrue(groups.contains(PrimaryColor.Red));
 		assertTrue(groups.contains(PrimaryColor.Yellow));
 		assertTrue(groups.contains(SecondaryColor.Orange));
 	}
 
 	@Test
 	public void testDataPoints() {
 		String s = "+";
 
 		lm.to(PrimaryColor.Red, PrimaryColor.Blue)
 		  .with("color", SecondaryColor.Purple.name())
 		  .debug("{~1} {} {~2} {=>} {:color}", s);
 
 		LogEvent event = getSingleEvent();
 		List<Topic> groups = event.getGroups();
 
 		assertEquals(2, groups.size());
 		assertTrue(groups.contains(PrimaryColor.Red));
 		assertTrue(groups.contains(PrimaryColor.Blue));
 		assertEquals(SecondaryColor.Purple.name(), event.getData().get("color"));
 		assertEquals("Red + Blue {=>} Purple", event.getFormattedMessage());
 	}
 
 	@Test
 	public void testAutomaticSource() {
 		lm.fromHere().info("Where you at dawg?");
 		LogEvent event = getSingleEvent();
		assertEquals("testAutomaticSource", event.getLocation());
 	}
 
 	// TODO
 	@Test @Ignore
 	public void ok() {
 		getURI("nope");
 	}
 
 	public URI getURI(String uri) {
 		try {
 			return new URI(uri);
 		} catch (URISyntaxException e) {
 			lm.because(e).fromHere()
 			  .with("uri", e.getInput())
 			  .warn("failed to create URI from string '{:uri}'");
 
 			return null;
 		}
 	}
 }
