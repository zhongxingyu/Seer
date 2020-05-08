 package tw.edu.ntu.csie.mhci.tapassist.utils;
 
 import android.app.Activity;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.Window;
 
 public class Layout {
 
 	public static int getStatusBar(Activity activity) {
 		Rect rectgle = new Rect();
 		Window window = activity.getWindow();
 		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
 		int StatusBarHeight = rectgle.top;
 		int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
 				.getTop();
 		int TitleBarHeight = contentViewTop - StatusBarHeight;
 
 		Log.i("debug", "StatusBar Height= " + StatusBarHeight
 				+ " , TitleBar Height = " + TitleBarHeight);
		return TitleBarHeight;
 	}
 }
