 package no.kantega.android.afp;
 
 import com.xtremelabs.robolectric.Robolectric;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 import org.junit.runners.model.InitializationError;
 
 import java.io.File;
 
 /**
  * Custom test runner that specifies the correct path to AndroidManifest.xml and res directory
  */
 public class MavenizedTestRunner extends RobolectricTestRunner {
 
     /**
      * Configure test runner
      *
      * @param testClass Class to test
      * @throws InitializationError Thrown if path doesn't contain required files
      */
     public MavenizedTestRunner(Class testClass) throws InitializationError {
        super(testClass, new File("./android/src/main/android"));
     }
 
     @Override
     protected void bindShadowClasses() {
         Robolectric.bindShadowClass(ShadowSQLiteDatabase.class);
     }
 }
 
 
