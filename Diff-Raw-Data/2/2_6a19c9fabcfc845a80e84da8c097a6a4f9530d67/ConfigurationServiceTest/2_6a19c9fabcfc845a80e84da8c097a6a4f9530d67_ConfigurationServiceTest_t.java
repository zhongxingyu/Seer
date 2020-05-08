 package org.automation.dojo;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.OngoingStubbing;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNull;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Mockito.*;
 
 /**
  * @author serhiy.zelenin
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ConfigurationServiceTest {
 
     private ConfigurationService configurationService;
     @Mock private TimeService timeService;
     @Mock private ScoreService scoreService;
     @Mock private ReleaseEngine engine;
     @Captor ArgumentCaptor<Long> currentTimeTickCaptor;
     
     @Before
     public void setUp() throws Exception {
         configurationService = new ConfigurationService(timeService, scoreService, engine);
         setupCurrentTime(100);
         configurationService.init();
     }
 
     @Test
     public void shouldSkipNextReleaseWhenManual(){
         configurationService.setManualReleaseTriggering(true);
 
         configurationService.adjustChanges();
         
         assertNull(configurationService.getNextMinorReleaseTime());
     } 
     
     @Test
     public void shouldCalculateNextMinorReleaseInitially(){
         setupCurrentTime(123);
         setupReleaseParams(100, false);
 
         configurationService.adjustChanges();
         
         assertEquals(new Date(123 + 100), configurationService.getNextMinorReleaseTime());
 
         configurationService.adjustChanges();
 
         assertEquals(123 + 100, configurationService.getNextMinorReleaseTime().getTime());
     }
 
 
     @Test
     public void shouldTriggerMinorRelease() {
         setupCurrentTime(123);
         setupReleaseParams(100, false);
         configurationService.adjustChanges();
 
         setupCurrentTime(123 + 100 + 1);
         configurationService.run();
 
         verify(engine).nextMinorRelease();
         assertEquals(123 + 100 + 100, configurationService.getNextMinorReleaseTime().getTime());
     } 
 
     @Test
     public void shouldNotTriggerMinorReleaseWhenManual() {
         setupCurrentTime(123);
         setupReleaseParams(100, true);
         configurationService.adjustChanges();
 
         setupCurrentTime(123 + 100 + 1);
         configurationService.run();
 
         verify(engine, never()).nextMinorRelease();
     }
 
     @Test
     public void shouldTriggerPenaltyCalculationInitially() {
         configurationService.setPenaltyTimeOut(50);
         configurationService.adjustChanges();
 
         setupCurrentTime(currentTime() + 50  + 1);
         configurationService.run();
         
         verify(scoreService).tick(currentTimeTickCaptor.capture());
         assertEquals(currentTime(), currentTimeTickCaptor.getValue().longValue());
     }
 
     @Test
     public void shouldNotTriggerPenaltyCalculationBeforeSchedule() {
         setupReleaseParams(100, true);
         setupCurrentTime(123);
         configurationService.setPenaltyTimeOut(50);
         
         configurationService.run();
 
         setupCurrentTime(123 + 50 - 1);
 
         configurationService.run();
 
         verify(scoreService, never()).tick(anyLong());
     }
 
     
     @Test
     public void adjustChangesForTrigger() {
         configurationService.setPenaltyTimeOut(150);
         
         configurationService.adjustChanges();
         
         assertEquals(currentTime() + 150, configurationService.getNextPenaltyTickTime().getTime());
     }
 
     private long currentTime() {
         return timeService.now().getTime();
     }
 
     @Test
     public void adjustChangesForTriggerInitially() {
         assertEquals(timeService.now().getTime() + configurationService.getPenaltyTimeOut(),
                 configurationService.getNextPenaltyTickTime().getTime());
     }
 
     @Test
     public void shouldFormatReleaseTimeRemainingNullWhenManual() throws ParseException {
         setupReleaseParams(100, true);
        assertEquals("[ask trainer]", configurationService.getNextReleaseRemaining());
     }
 
     @Test
     public void shouldFormatReleaseTimeRemaining() throws ParseException {
         setupReleaseParams(10 * 60 * 1000, false); //10 min
         setupCurrentTime(0);
         configurationService.adjustChanges();
         setupCurrentTime(8 * 60 * 1000 + 15 * 1000); //8min 15sec
 
         assertEquals("01 min 45 sec", configurationService.getNextReleaseRemaining());
     }
 
 
     private OngoingStubbing<Date> setupCurrentTime(long time) {
         return when(timeService.now()).thenReturn(new Date(time));
     }
 
 
     private void setupReleaseParams(int minorReleaseFrequency, boolean manualReleaseTriggering) {
         configurationService.setMinorReleaseFrequency(minorReleaseFrequency);
         configurationService.setManualReleaseTriggering(manualReleaseTriggering);
     }
 }
