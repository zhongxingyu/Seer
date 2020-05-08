 package grabbers;
 
 import java.util.List;
 
 import org.junit.Test;
 
 public class TheTvDbGrabberTest {
 
   @Test
   public void testSearchForSeries() throws GrabberException {
     final TheTvDbGrabber grabber = new TheTvDbGrabber();
     final List<GrabberSearchMovie> searchForMovie = grabber.searchForMovie("Clone Wars");
     System.out.println("sad");
   }
 
   @Test
   public void testGetSeriesInfo() throws GrabberException {
     final TheTvDbGrabber grabber = new TheTvDbGrabber();
     final GrabberDisplayMovie displayMovie = grabber.getDisplayMovie("83268_1");
   }
 
 }
