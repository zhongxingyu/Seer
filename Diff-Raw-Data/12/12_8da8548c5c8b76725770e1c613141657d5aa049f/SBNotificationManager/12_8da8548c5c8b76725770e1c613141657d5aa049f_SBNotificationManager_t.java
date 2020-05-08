 package co.shoutbreak.components;
 
 import co.shoutbreak.ui.SBContext;
 import co.shoutbreak.ui.views.SBView;
 import android.os.Bundle;
 
 public class SBNotificationManager extends SBComponent {
 	
 	public SBNotificationManager(SBContext context) {
 		super(context, "SBNotificationManager");
 	}
 	
 	public void handleNotificationExtras(Bundle extras) {
 		SBView inbox;
 		if (extras != null) {
 			if (extras.containsKey(SBView.NOTIFICATION_REFERRAL_ID)
 					&& extras.getBoolean(SBView.NOTIFICATION_REFERRAL_ID)) {
				inbox = ((SBContext) _Context).getView(SBContext.INBOX_VIEW);
				((SBContext) _Context).switchView(inbox);
 			}
 		}
 	}
 }
