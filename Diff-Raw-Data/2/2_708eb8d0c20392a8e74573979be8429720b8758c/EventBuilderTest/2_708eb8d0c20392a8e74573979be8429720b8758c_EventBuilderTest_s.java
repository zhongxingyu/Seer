 package ch.bergturbenthal.hs485.frontend.gwtfrontend.client;
 
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.client.editor.event.EventSourceManager;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.client.editor.event.EventTypeManager;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.client.editor.event.LabelGenerator;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.db.InputConnector;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.db.OutputDevice;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.db.handler.EventSource;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.event.KeyEvent;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.event.ValueEvent;
 
 public class EventBuilderTest {
 	@Test
 	public void testBuilder() {
 		final EventTypeManager panelBuilder = new EventTypeManager(new LabelGenerator() {
 
 			@Override
 			public String makeLabelForInputConnector(final InputConnector inputConnector) {
 				// TODO Auto-generated method stub
 				return "";
 			}
 
 			@Override
 			public String makeLabelForOutputDevice(final OutputDevice outputDevice) {
 				return "";
 			}
 		});
 		final Collection<EventSourceManager<ValueEvent, EventSource<ValueEvent>>> valueBuilders = panelBuilder.listInputPanelsForEvent(ValueEvent.class
 				.getName());
 		Assert.assertEquals(0, valueBuilders.size());
 		final Collection<EventSourceManager<KeyEvent, EventSource<KeyEvent>>> keyBuilders = panelBuilder
 				.listInputPanelsForEvent(KeyEvent.class.getName());
		Assert.assertEquals(2, keyBuilders.size());
 
 	}
 }
