 package de.age.logtool.parsing;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.age.logtool.parsing.util.TrackingLogEventListener;
 import de.age.logtool.parsing.util.WebSphereLogCreator;
 
 public class WebsphereLogParserTest {
 	
 	private WebSphereLogCreator content;
 	private WebsphereLogParser parser;
 	private TrackingLogEventListener listener;
 	
 	@Before
 	public void setUp() {
 		content = new WebSphereLogCreator();
 		parser = new WebsphereLogParser();
 		listener = new TrackingLogEventListener();
 		parser.addLogEventListener(listener);
 	}
 	
 	@After
 	public void tearDown() {
 		parser = null;
 		content = null;
 		listener = null;
 	}
 	
 	@Test
 	public void singleLineDoesNotGetParsedUntilEntryIsFinished() {
 		parser.parse(content.create().getContent());
 		assertThat(listener.getNumberOfEvents(), is(0));
 	}
 	
 	@Test
 	public void singleLineGetsParsedOnNextTimestamp() {
 		parser.parse(content.create().getContent());
 		parser.parse(content.create().getContent());
 		assertThat(listener.getNumberOfEvents(), is(1));
 	}
 	
 	@Test
 	public void timestampsGetParsedCorrectly() {
 		parser.parse(content.create().getContent());
 		parser.parse(content.create().getContent());
 		assertThat(listener.getEvents(), is(content.matcher(0, 1)));
 	}
 	
 	@Test
 	public void illegalContentAtStartGetsIgnored() {
 		parser.parse("Illegal Content");
 		parser.parse(content.create().getContent());
 		assertThat(listener.getNumberOfEvents(), is(0));
 	}
 	
 	@Test
 	public void multiLineContent() {
 	}
 	
 }
