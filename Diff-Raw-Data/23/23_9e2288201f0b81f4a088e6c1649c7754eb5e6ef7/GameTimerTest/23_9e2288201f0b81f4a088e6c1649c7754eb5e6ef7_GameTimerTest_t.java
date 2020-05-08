 package dk.kleistsvendsen;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.stubbing.OngoingStubbing;
 import org.robolectric.Robolectric;
 import org.robolectric.RobolectricTestRunner;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static roboguice.RoboGuice.getInjector;
 
 @RunWith(RobolectricTestRunner.class)
 public class GameTimerTest {
     private GameTimer gameTimer;
 
     @Mock
     private ITicSource ticSource;
 
     @Before
     public void setup() {
         MockitoAnnotations.initMocks(this);
         TestGuiceModule module = new TestGuiceModule();
         module.addBinding(ITicSource.class, ticSource);
         TestGuiceModule.setUp(this, module);
         gameTimer = getInjector(Robolectric.application).getInstance(GameTimer.class);
     }
 
     @After
     public void tearDown() {
         TestGuiceModule.tearDown();
     }
 
     @Test
     public void testCallsTickOnStartTimer() {
         mockQueueTics(1);
         gameTimer.startTimer();
     }
 
     @Test
     public void timeLeftReturnsTimeSinceStart() {
         when(ticSource.tic()).thenReturn((long) 100);
         gameTimer.startTimer();
         when(ticSource.tic()).thenReturn((long) 1000);
         assertEquals(900L, gameTimer.timePlayed());
     }
 
     @Test
     public void timeLeftDoesntChangeAfterPause() {
         mockQueueTics(2);
         gameTimer.startTimer();
         gameTimer.pauseTimer();
         long time = gameTimer.timePlayed();
         assertEquals(time, gameTimer.timePlayed());
         verify(ticSource, times(2)).tic();
         verifyNoMoreInteractions(ticSource);
     }
 
     @Test
     public void pauseWhenPausedDoesNothing() {
         mockQueueTics(4);
         gameTimer.startTimer();
         gameTimer.pauseTimer();
         long time = gameTimer.timePlayed();
         gameTimer.pauseTimer();
         assertEquals(time, gameTimer.timePlayed());
     }
 
     @Test
     public void startButtonContinues() {
         mockQueueTics(4);
         gameTimer.startTimer();
         gameTimer.pauseTimer();
         gameTimer.startTimer();
         assertEquals(2, gameTimer.timePlayed());
     }
 
    @Test
    public void resetSetsTimePlayedTo0WhenStarted() {
        mockQueueTics(4);
        gameTimer.startTimer();
        gameTimer.resetTimer();
        assertEquals(0, gameTimer.timePlayed());
    }

     private void mockQueueTics(int tics) {
         OngoingStubbing<Long> stub = when(ticSource.tic());
         for (int i=0; i<tics;++i) {
             stub = stub.thenReturn((long) i);
         }
     }
 }
