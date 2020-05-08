 package ru.rutube.RutubeApp.ui.player;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.test.ActivityInstrumentationTestCase2;
 
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubePlayer.ctrl.PlayerController;
import ru.rutube.RutubePlayer.ui.PlayerActivity;
import ru.rutube.RutubePlayer.ui.PlayerFragment;
 
 /**
  * This is a simple framework for a test of an Application.  See
  * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
  * how to write and extend Application tests.
  * <p/>
  * To run this test, you can type:
  * adb shell am instrument -w \
  * -e class ru.rutube.RutubeApp.ui.player.VitamioPlayerActivityTest \
  * ru.rutube.RutubeApp.tests/android.test.InstrumentationTestRunner
  */
public class VitamioPlayerActivityTest extends ActivityInstrumentationTestCase2<PlayerActivity> {
 
     private Intent intent;
     private Uri videoUri;
     private Uri thumbnailUri;
    private PlayerActivity mActivity;
    private PlayerFragment mFragment;
 
     public VitamioPlayerActivityTest() {
        super(PlayerActivity.class);
     }
 
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         intent = new Intent();
         videoUri = Uri.parse("http://rutube.ru/video/ed1d49491260b778418d970122ef07c8/");
         thumbnailUri = Uri.parse("http://placehold.it/640x360.jpg");
         intent.setData(videoUri);
         mActivity = getActivity();
     }
     /**
      * Проверяет, что PlayerFragment корректно обрабатывает процесс сохранения и загрузки состояния
      */
     public void testPlayerFragmentSaveLoad() {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(R.id.player_fragment));
         assertNotNull(mFragment);
         Bundle state = new Bundle();
         mFragment.onSaveInstanceState(state);
         mFragment.onActivityCreated(state);
     }
 
 
     /**
      * Проверяет, что PlayerController корректно обрабатывает процесс сохранения и загрузки состояния
      * @throws Throwable
      */
     public void testControllerSaveLoad() throws Throwable {
        mFragment = (PlayerFragment)(mActivity.getSupportFragmentManager().findFragmentById(R.id.player_fragment));
         assertNotNull(mFragment);
         final PlayerController controller = new PlayerController(videoUri, thumbnailUri);
         runTestOnUiThread(new Runnable() {
             @Override
             public void run() {
                 controller.attach(mActivity, mFragment);
             }
         });
         Bundle b = new Bundle();
         b.putParcelable("KEY", controller);
         Object res  = b.getParcelable("KEY");
         assertNotNull(res);
         assertTrue(res.equals(controller));
     }
 
     public void testActivitySaveLoad() throws Throwable {
         final Bundle state = new Bundle();
         runTestOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnSaveInstanceState(mActivity, state);
                 getInstrumentation().callActivityOnRestoreInstanceState(mActivity, state);
 
             }
         });
     }
 
 }
