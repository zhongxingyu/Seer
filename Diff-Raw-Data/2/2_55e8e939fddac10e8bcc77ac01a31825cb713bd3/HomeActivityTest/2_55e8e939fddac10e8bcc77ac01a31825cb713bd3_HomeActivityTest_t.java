 package dk.kleistsvendsen;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.os.RoboVibrator;
 import android.os.Vibrator;
 import android.widget.Button;
 import android.widget.TextView;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.robolectric.Robolectric;
 import org.robolectric.RobolectricTestRunner;
 import org.robolectric.shadows.ShadowAlertDialog;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.robolectric.Robolectric.shadowOf;
 
 @RunWith(RobolectricTestRunner.class)
 public class HomeActivityTest {
     private HomeActivity activity;
 
     @Mock
     private IGameTimer gameTimer;
     private TextView timePlayedText;
     private TextView timeLeftText;
     private Button resetButton;
     private Button startButton;
     private Button pauseButton;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         TestGuiceModule module = new TestGuiceModule();
         module.addBinding(IGameTimer.class, gameTimer);
         TestGuiceModule.setUp(this, module);
         activity = Robolectric.buildActivity(HomeActivity.class).create().get();
         timePlayedText = (TextView) activity.findViewById(R.id.timePlayedText);
         resetButton = (Button) activity.findViewById(R.id.reset_button);
         startButton = (Button) activity.findViewById(R.id.start_button);
         pauseButton = (Button) activity.findViewById(R.id.pause_button);
         timeLeftText = (TextView) activity.findViewById(R.id.timeLeftText);
     }
 
     @After
     public void tearDown() {
         TestGuiceModule.tearDown();
     }
 
     @Test
     public void mockShouldBeNotNull() throws Exception {
         assertNotNull(gameTimer);
     }
 
     @Test
     public void shouldHaveStartButton() throws Exception {
         assertEquals("Start", startButton.getText());
     }
 
     @Test
     public void shouldHavePauseButton() throws Exception {
         assertEquals("Pause", pauseButton.getText());
     }
 
     @Test
     public void shouldHaveResetButton() throws Exception {
         assertEquals("Reset", resetButton.getText());
     }
 
     @Test
     public void startButtonConnectedToGameTimer() throws Exception {
         startButton.performClick();
         verify(gameTimer).startTimer();
     }
 
     @Test
     public void startButtonTriggersVibrate() throws Exception {
         RoboVibrator vibrator = (RoboVibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
         startButton.performClick();
         assertTrue(vibrator.isVibrating());
     }
 
     @Test
     public void pauseButtonConnectedToGameTimer() throws Exception {
         pauseButton.performClick();
         verify(gameTimer).pauseTimer();
     }
 
     @Test
     public void pauseButtonTriggersVibrate() throws Exception {
         RoboVibrator vibrator = (RoboVibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
         pauseButton.performClick();
         assertTrue(vibrator.isVibrating());
     }
 
     @Test
     public void resetButtonTriggersDialog() throws Exception {
         resetButton.performClick();
         AlertDialog alert =
                 ShadowAlertDialog.getLatestAlertDialog();
         assertNotNull(alert);
         ShadowAlertDialog sAlert = shadowOf(alert);
         assertEquals(
                 activity.getString(R.string.reset_timer_question_title),
                 sAlert.getTitle().toString());
         alert.getButton(Dialog.BUTTON_POSITIVE).performClick();
         verify(gameTimer).resetTimer();
     }
 
     @Test
     public void resetDialogTriggersResetOnOK() throws Exception {
         resetButton.performClick();
         AlertDialog alert =
                 ShadowAlertDialog.getLatestAlertDialog();
         alert.getButton(Dialog.BUTTON_POSITIVE).performClick();
         verify(gameTimer).resetTimer();
     }
 
     @Test
     public void resetDialogTriggersNothingOnCancel() throws Exception {
         resetButton.performClick();
         AlertDialog alert =
                 ShadowAlertDialog.getLatestAlertDialog();
         alert.getButton(Dialog.BUTTON_NEGATIVE).performClick();
        verify(gameTimer, never()).resetTimer();
     }
 
     @Test
     public void shouldHaveDefaultTimeLeftText() throws Exception {
         assertEquals("20:00.000", timeLeftText.getText());
     }
 
     @Test
     public void shouldHaveDefaultTimePlayedText() throws Exception {
         assertEquals("0:00.000", timePlayedText.getText());
     }
 
     @Test
     public void updateViewSetsTimePlayed() {
         CharSequence preText = timePlayedText.getText();
         when(gameTimer.timePlayed()).thenReturn(1000L);
         activity.updateView();
         assertTrue(preText != timePlayedText.getText());
     }
 
     @Test
     public void updateViewSetsTimeLeft() {
         CharSequence preText = timeLeftText.getText();
         when(gameTimer.timePlayed()).thenReturn(1000L);
         activity.updateView();
         assertTrue(preText != timeLeftText.getText());
     }
 
     @Test
     public void updateViewFormatsTimeLeft() {
         when(gameTimer.timePlayed()).thenReturn(70321L);
         activity.updateView();
         assertEquals("18:49.679",timeLeftText.getText());
     }
 
     @Test
     public void updateViewFormatsTimePlayed() {
         when(gameTimer.timePlayed()).thenReturn(60123L);
         activity.updateView();
         assertEquals("1:00.123",timePlayedText.getText());
     }
 }
