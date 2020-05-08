 package nl.napauleon.sabber;
 
 
 import com.actionbarsherlock.ActionBarSherlock;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 import org.junit.runners.model.InitializationError;
 
 import java.io.File;
 
 public class CustomTestRunner extends RobolectricTestRunner {
     public CustomTestRunner(Class testClass) throws InitializationError {
         // defaults to "AndroidManifest.xml", "res" in the current directory
        super(testClass, new File("app/"));
         ActionBarSherlock.registerImplementation(ActionBarSherlockRobolectric.class);
         //addClassOrPackageToInstrument("com.actionbarsherlock.app.SherlockActivity");
     }
 
     @Override
     protected void bindShadowClasses() {
         super.bindShadowClasses();
 //        RobolectricTestRunner.setStaticValue(Build.VERSION.class, "SDK_INT", 15);
     }
 }
