 package ghm.followgui;
 
 import java.io.File;
 
 public class BottomTest extends AppLaunchingTestCase {
 
   public BottomTest (String name) { super(name); }
 
   public void testEnabled () throws Exception {
    assert(!app_.bottom_.isEnabled());
     File file = createTempFile();
     systemInterface_.setFileFromUser(file);
     invokeAction(app_.open_);
    assert(app_.bottom_.isEnabled());
     invokeAction(app_.close_);
    assert(!app_.bottom_.isEnabled());
   }
 
 }
 
