 package ca.charland.robolectric;
 
 import java.io.File;
 
 import org.junit.runners.model.InitializationError;
 
 import roboguice.RoboGuice;
 import roboguice.activity.RoboActivity;
 import android.app.Activity;
 import android.content.Context;
 import android.view.View;
 
 import com.google.inject.Injector;
 import com.xtremelabs.robolectric.Robolectric;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 import com.xtremelabs.robolectric.shadows.ShadowActivity;
 import com.xtremelabs.robolectric.shadows.ShadowResources;
 
 /**
  * Custom runner to redirect to the development folder. Specifically so the res and manifest can be found.
  * 
  * @author mcharland
  * 
  */
 public class AndroidBaseRobolectricTestRunner extends RobolectricTestRunner {
 
	private static final String ANDROID_BASE_HOME = "ANDROID_BASE_HOME";
 	private Injector injector;
 
 	public AndroidBaseRobolectricTestRunner(Class<?> testClass) throws InitializationError {
		this(testClass, getAndroidBaseFolder() + "\\AndroidBase");
	}

	private static String getAndroidBaseFolder() {
		String getenv = System.getenv(ANDROID_BASE_HOME);
		if(getenv == null) {
			throw new IllegalArgumentException(ANDROID_BASE_HOME + " not set.");
		}
		return getenv;
 	}
 
 	public AndroidBaseRobolectricTestRunner(Class<?> testClass, String configFile) throws InitializationError {
 		super(testClass, new File(configFile));
 	}
 
 	@Override
 	public void prepareTest(Object test) {
 		Context context = new RoboActivity();
 		this.injector = RoboGuice.getInjector(context);
 		this.injector.injectMembers(test);
 	}
 
 	@Override
 	protected void bindShadowClasses() {
 		Robolectric.bindShadowClass(ShadowResources.class);
 	}
 
 	public static View getViewFromShadowActivity(Activity activity, int id) {
 		ShadowActivity shadowA = Robolectric.shadowOf(activity);
 		View next = (View) shadowA.findViewById(id);
 		return next;
 	}
 }
