 package com.ninetwozero.battlelog;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.test.suitebuilder.annotation.Suppress;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.SlidingDrawer;
 import com.jayway.android.robotium.solo.Solo;
 
 import static android.test.MoreAsserts.assertMatchesRegex;
 import static android.test.ViewAsserts.assertOnScreen;
 
public class MainIT extends ActivityInstrumentationTestCase2<Main> {
 
    private Main activity;
     private EditText fieldEmail;
     private EditText fieldPassword;
     private CheckBox checkboxSave;
     private Button buttonLogin;
     private SlidingDrawer aboutSlider;
     private Solo solo;
 
     public MainIT() {
        super("com.ninetwozero.battlelog", Main.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         activity = this.getActivity();
         solo = new Solo(getInstrumentation(), activity);
         fieldEmail = (EditText)activity.findViewById(R.id.field_email);
         fieldPassword = (EditText)activity.findViewById(R.id.field_password);
         checkboxSave = (CheckBox)activity.findViewById(R.id.checkbox_save);
         buttonLogin = (Button)activity.findViewById(R.id.button_login);
         aboutSlider = (SlidingDrawer) activity.findViewById(R.id.about_slider);
     }
 
     @Override
     public void tearDown() throws Exception {
         try{
             solo.finalize();
         }catch (Throwable ex) {
             ex.printStackTrace();
         }
 
         activity.finish();
         super.tearDown();
     }
 
     public void testActivity() {
         assertNotNull(activity);
     }
 
     public void testUILayout(){
         final View origin = decorView();
         assertOnScreen(origin, fieldEmail);
         assertOnScreen(origin, fieldPassword);
         assertOnScreen(origin, checkboxSave);
         assertOnScreen(origin, buttonLogin);
         assertOnScreen(origin, aboutSlider);
        
         String emailHint = activity.getResources().getString(R.string.info_hint_email);
         assertMatchesRegex(emailHint, fieldEmail.getHint().toString());
        
         String passwordHint = activity.getResources().getString(R.string.info_hint_password);
         assertMatchesRegex(passwordHint, fieldPassword.getHint().toString());
     }
 
     private View decorView() {
         return activity.getWindow().getDecorView();
     }
 
     //Do not run this test as so far I did not found how to check for Toast messages
     @UiThreadTest
     @Suppress
     public void testEmailValidation(){
         fieldEmail.setText("");
         fieldPassword.setText("");
         buttonLogin.performClick();
         assertTrue(solo.waitForText(resourceToString(R.string.general_invalid_email)));
     }
    
     @UiThreadTest
     public void testAboutSlidingDrawer(){
         final View origin = decorView();
         aboutSlider.performClick();
         assertOnScreen(origin, activity.findViewById(R.id.wrap_web));
         assertOnScreen(origin, activity.findViewById(R.id.wrap_twitter));
         assertOnScreen(origin, activity.findViewById(R.id.wrap_email));
         assertOnScreen(origin, activity.findViewById(R.id.wrap_forum));
         assertOnScreen(origin, activity.findViewById(R.id.wrap_xbox));
     }
 
     private String resourceToString(int id) {
         return activity.getResources().getString(id);
     }
 }
 
 
