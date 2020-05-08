 package uk.me.tom_fitzhenry.motionremote.notification;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static uk.me.tom_fitzhenry.motionremote.notification.NotificationManagerTest.TestBuilder.startTest;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InOrder;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class NotificationManagerTest {
 	
 	@InjectMocks NotificationManager notificationManager;
 	@Mock AndroidNotifier notifier;
 	
 	@Test
 	public void foo() {
 		startTest()
 			.motionAt(time(14,10,0), "Motion!", "")
			.stoppedAt(time(14,12,5), "No motion", "Past incidents: 2m5s");
 	}
 	
 	@Test
 	public void stoppedEventsWithNoCorrespondingStartEventsAreDropped() {
 		startTest()
 			.stoppedAt(arbitraryDateTime())
 			.motionAt(time(14,10,0), "Motion!", "");
 	}
 	
 	@Test
 	public void multipleIncidentsAreBatched() {
 		startTest()
 			.motionAt(time(14,10,0), "Motion!", "")
 			.stoppedAt(time(14,12,0), "No motion", "Past incidents: 2m")
 			.motionAt(time(16,10,0), "Motion!", "Past incidents: 2m")
 			.stoppedAt(time(16,14,0), "No motion", "Past incidents: 2m, 4m");
 	}
 
 	@Test
 	public void cameraDown() throws Exception {
 		startTest()
 			.motionAt(time(14,10,0), "Motion!", "")
 			.stoppedAt(time(14,12,0), "No motion", "Past incidents: 2m")
 			.cameraDownAt(time(14,20,0), "Camera down!", "Past incidents: 2m");
 	}
 	
 	@Test
 	public void clearingCausesPastIncidentsToNoLongerAppear() {
 		startTest()
 			.motionAt(time(14,10,0), "Motion!", "")
 			.stoppedAt(time(14,12,0), "No motion", "Past incidents: 2m")
 			.clear()
 			.motionAt(time(16,10,0), "Motion!", "")
 			.stoppedAt(time(16,14,0), "No motion", "Past incidents: 4m");
 	}
 	
 	public static class TestBuilder {
 		private final AndroidNotifier notifier = mock(AndroidNotifier.class);
 		private final NotificationManager notificationManager = new NotificationManager(notifier);
 		private final InOrder inOrder = inOrder(notifier);
 		
 		public static TestBuilder startTest() {
 			return new TestBuilder();
 		}
 		
 		public TestBuilder motionAt(DateTime dateTime, String title, String subTitles) {
 			return then(new MotionDetectedEvent(dateTime), title, subTitles);
 		}
 		
 		public TestBuilder stoppedAt(DateTime dateTime, String title, String subTitles) {
 			return then(new MotionStoppedEvent(dateTime), title, subTitles);
 		}
 		
 		public TestBuilder stoppedAt(DateTime dateTime) {
 			return then(new MotionStoppedEvent(dateTime));
 		}
 		
 		public TestBuilder cameraDownAt(DateTime dateTime, String title, String subTitles) {
 			return then(new CameraDownEvent(dateTime), title, subTitles);
 		}
 		
 		private TestBuilder then(Event e, String title, String subTitles) {
 			notificationManager.process(e);
 			inOrder.verify(notifier).submit(title, subTitles);
 			return this;
 		}
 		
 		public TestBuilder then(Event e) {
 			notificationManager.process(e);
 			inOrder.verify(notifier, never()).submit(anyString(), anyString());
 			return this;
 		}
 		
 		public TestBuilder clear() {
 			notificationManager.clear();
 			return this;
 		}
 	}
 
 	private static DateTime time(int hour, int minute, int second) {
 		return new DateTime(2010,1,1,hour,minute,second,0);
 	}
 	
 	private static DateTime arbitraryDateTime() {
 		return new DateTime(2013,1,2,3,4,5,6);
 	}
 
 }
