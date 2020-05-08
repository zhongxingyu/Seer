 package au.edu.adelaide.physics.opticsstatusboard;
 
 import android.app.Application;
 import org.acra.*;
 import org.acra.annotation.*;
 
 @ReportsCrashes(formKey = "", mailTo = "blokart11@gmail.com",
 				mode = ReportingInteractionMode.NOTIFICATION,
 				resNotifTickerText = R.string.crashTickerText,
                 resNotifTitle = R.string.crashTitle,
                 resNotifText = R.string.crashText,
                resDialogText = R.string.crashDialogTitle,
                 resDialogCommentPrompt = R.string.crashDialogComment)
 public class StatusBoardApplication extends Application {
 	public void onCreate() {
 		super.onCreate();
 		
 		ACRA.init(this);
 	}
 }
