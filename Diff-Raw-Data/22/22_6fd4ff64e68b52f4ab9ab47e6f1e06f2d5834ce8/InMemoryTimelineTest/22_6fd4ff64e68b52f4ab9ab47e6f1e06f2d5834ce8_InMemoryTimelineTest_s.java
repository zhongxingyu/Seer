 package com.noodlesandwich.quacker.communication;
 
 import java.time.Instant;
 import java.time.ZoneId;
 import java.time.ZonedDateTime;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import com.noodlesandwich.quacker.communication.messages.Message;
 import com.noodlesandwich.quacker.communication.timeline.InMemoryTimeline;
 import com.noodlesandwich.quacker.id.Id;
 import com.noodlesandwich.quacker.ui.TimelineRenderer;
 import com.noodlesandwich.quacker.users.User;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.Sequence;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
 
 public class InMemoryTimelineTest {
     private static final Instant NOW = Instant.from(ZonedDateTime.of(2011, 12, 25, 15, 42, 23, 0, ZoneId.of("UTC")));
 
     private final Mockery context = new Mockery();
     private final TimelineRenderer renderer = context.mock(TimelineRenderer.class);
 
     private final InMemoryTimeline timeline = new InMemoryTimeline();
 
     @Test public void
     publishes_messages_to_an_in_memory_timeline() {
         User user = context.mock(User.class);
         final Message message = new Message(new Id(72), user, "Beep beep.", NOW);
         timeline.publish(new Id(72), message);
 
         context.checking(new Expectations() {{
             oneOf(renderer).render(message);
         }});
 
         timeline.renderTo(renderer);
 
         context.assertIsSatisfied();
     }
 
     @Test public void
     messages_are_rendered_in_reverse_chronological_order() {
         User user = context.mock(User.class);
         final Message one = new Message(new Id(95), user, "One", NOW.plusSeconds(1));
         final Message two = new Message(new Id(97), user, "Two", NOW.plusSeconds(2));
         final Message three = new Message(new Id(99), user, "Three", NOW.plusSeconds(3));
 
         timeline.publish(new Id(95), one);
         timeline.publish(new Id(97), two);
         timeline.publish(new Id(99), three);
 
         context.checking(new Expectations() {{
             Sequence messages = context.sequence("messages");
             oneOf(renderer).render(three); inSequence(messages);
             oneOf(renderer).render(two); inSequence(messages);
             oneOf(renderer).render(one); inSequence(messages);
         }});
 
         timeline.renderTo(renderer);
 
         context.assertIsSatisfied();
     }
 
     @Test public void
     caps_the_number_of_messages_at_20() {
         User user = context.mock(User.class);
         final List<Message> timelineMessages = new ArrayList<>();
         for (int i = 0; i < 50; ++i) {
             Id messageId = new Id(i);
             Message message = new Message(messageId, user, "Message " + i, NOW.plusSeconds(i));
             timelineMessages.add(message);
             timeline.publish(messageId, message);
         }
         Collections.reverse(timelineMessages);
 
         context.checking(new Expectations() {{
             Sequence messages = context.sequence("messages");
             for (Message message : timelineMessages.subList(0, 20)) {
                 oneOf(renderer).render(message); inSequence(messages);
             }
         }});
 
         timeline.renderTo(renderer);
 
         context.assertIsSatisfied();
     }
 
     @SuppressWarnings("unchecked")
     @Test public void
     is_iterable() {
         User user = context.mock(User.class);
         Message one = new Message(new Id(1), user, "One", NOW.plusSeconds(1));
         Message two = new Message(new Id(2), user, "Two", NOW.plusSeconds(2));
         Message three = new Message(new Id(3), user, "Three", NOW.plusSeconds(3));
 
         timeline.publish(new Id(1), one);
         timeline.publish(new Id(2), two);
         timeline.publish(new Id(3), three);
 
         assertThat(timeline, contains(three, two, one));
     }
 }
