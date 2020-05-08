 package au.com.dius.resilience.test.unit.location;
 
import android.app.Activity;
import android.location.LocationManager;
 import au.com.dius.resilience.location.LocationBroadcaster;
 import au.com.dius.resilience.location.StopLocatingRunnable;
 import au.com.dius.resilience.test.unit.utils.ResilienceTestRunner;
 import au.com.dius.resilience.test.unit.utils.TestHelper;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
import org.mockito.Mockito;
 
import static junitx.util.PrivateAccessor.setField;
import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
 
 @RunWith(ResilienceTestRunner.class)
 public class StopLocatingRunnableTest {
 
   private StopLocatingRunnable stopLocatingRunnable;
 
   private LocationBroadcaster locationBroadcaster;
 
   @Before
   public void setup() {
    locationBroadcaster = spy(new LocationBroadcaster());
    TestHelper.setField(locationBroadcaster, "context", new Activity());
    TestHelper.setField(locationBroadcaster, "locationManager", mock(LocationManager.class));

     stopLocatingRunnable = new StopLocatingRunnable(locationBroadcaster);
     TestHelper.setField(stopLocatingRunnable, "locationBroadcaster", locationBroadcaster);
   }
 
   @Test
   public void shouldStopPollingWhenRun() {
     stopLocatingRunnable.run();
     verify(locationBroadcaster).stopPolling();
   }
 }
