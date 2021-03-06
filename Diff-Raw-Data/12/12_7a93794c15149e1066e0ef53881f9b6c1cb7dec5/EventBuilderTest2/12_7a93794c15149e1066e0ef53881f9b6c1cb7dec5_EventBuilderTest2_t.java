 package net.kencochrane.raven.event;
 
 import mockit.Injectable;
 import mockit.NonStrictExpectations;
 import org.testng.annotations.Test;
 
 import java.util.UUID;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.sameInstance;
 
 public class EventBuilderTest2 {
     @Test
    public void builtEventHasRandomlyGeneratedUuid(@Injectable final UUID mockUuid)
            throws Exception {
         new NonStrictExpectations(UUID.class) {{
             UUID.randomUUID();
             result = mockUuid;
         }};
         final EventBuilder eventBuilder = new EventBuilder();
 
         final Event event = eventBuilder.build();
 
         assertThat(event.getId(), is(sameInstance(mockUuid)));
     }
 
     @Test
    public void builtEventWithCustomUuidHasProperUuid(@Injectable final UUID mockUuid)
            throws Exception {
         final EventBuilder eventBuilder = new EventBuilder(mockUuid);
 
         final Event event = eventBuilder.build();
 
         assertThat(event.getId(), is(sameInstance(mockUuid)));
     }
 
     @Test
    public void builtEventWithMessageHasProperMessage(
            @Injectable("message") final String mockMessage)
            throws Exception {
         final EventBuilder eventBuilder = new EventBuilder();
         eventBuilder.setMessage(mockMessage);
 
         final Event event = eventBuilder.build();
 
         assertThat(event.getMessage(), is(sameInstance(mockMessage)));
     }
 }
