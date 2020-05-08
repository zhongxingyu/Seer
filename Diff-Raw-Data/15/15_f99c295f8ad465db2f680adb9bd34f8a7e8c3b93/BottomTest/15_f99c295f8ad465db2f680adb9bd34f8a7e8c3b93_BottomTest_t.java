 package ghm.followgui;
 
 import java.io.File;
 
 public class BottomTest extends AppLaunchingTestCase {
 
   public BottomTest (String name) { super(name); }
 
   public void testEnabled () throws Exception {
    assertEquals(false, app_.bottom_.isEnabled());
     File file = createTempFile();
     systemInterface_.setFileFromUser(file);
     invokeAction(app_.open_);
    assertEquals(true, app_.bottom_.isEnabled());
     invokeAction(app_.close_);
    assertEquals(false, app_.bottom_.isEnabled());
   }
 
 }
 
