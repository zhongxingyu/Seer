 package st.gaw.resinfo;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.graphics.Point;
 import android.view.Display;
 
 @TargetApi(13)
 public class ApiLevel13 {
 
	public static void getScreenSize(Activity activity, Point screenSize) throws VerifyError, NoSuchMethodError {
 		Display display = activity.getWindowManager().getDefaultDisplay();
 		display.getSize(screenSize);
 	}
 
 }
