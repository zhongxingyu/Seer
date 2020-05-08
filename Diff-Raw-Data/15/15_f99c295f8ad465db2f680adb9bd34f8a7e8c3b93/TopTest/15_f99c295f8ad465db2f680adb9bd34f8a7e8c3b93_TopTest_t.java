 package ghm.followgui;
 
 public class TopTest extends AppLaunchingTestCase {
 
   public TopTest (String name) { super(name); }
 
   public void testTop () {
    assertEquals(false, app_.top_.isEnabled());
   }
 
 }
 
