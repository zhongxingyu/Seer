 package de.newsarea.homecockpit.connector.facade.registration;
 
 import de.newsarea.homecockpit.connector.Connector;
 import de.newsarea.homecockpit.connector.facade.eventhandler.AbstractEventHandler;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.mockito.Mockito.mock;
 import static org.testng.AssertJUnit.assertEquals;
 
 public class EventHandlerRegistrationUtilsTest {
 
     @Test
     public void shouldCreateEventHandler() throws Exception {
         Connector connector = mock(Connector.class);
         Map<String, String> params = new HashMap<>();
         // zero parameters
         AbstractEventHandler abstractEventHandler = EventHandlerRegistrationUtils.createEventHandler(connector, MockEventHandler.class, params);
         assertEquals(0, abstractEventHandler.getParameters().size());
         // one parameter
         params.put("additionalKey", "additionalValue");
         abstractEventHandler = EventHandlerRegistrationUtils.createEventHandler(connector, MockEventHandler.class, params);
         assertEquals(1, abstractEventHandler.getParameters().size());
         assertEquals("additionalValue", abstractEventHandler.getParameters().get("additionalKey"));
     }
 
    public static class MockEventHandler extends AbstractEventHandler<Connector> {
 
        public MockEventHandler(Connector connector, Map<String, String> parameters) {
             super(connector, parameters);
         }
 
     }
 
 }
