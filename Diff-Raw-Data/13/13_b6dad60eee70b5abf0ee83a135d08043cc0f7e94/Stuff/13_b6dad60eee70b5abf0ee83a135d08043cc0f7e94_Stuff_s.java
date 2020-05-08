 /*******************************************************
  *
  * Part of ch.blinkenlights.android.ntyfr
  *
  * (C) 2012 Adrian Ulrich
  *
  * Licensed under the GPLv2 (only)
  *
  *******************************************************/
 
 package ch.blinkenlights.android.ntyfr;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.app.Activity;
 
 import android.app.AlertDialog;
 import android.view.View.OnClickListener;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 
 import android.widget.TextView;
 import android.text.Html;
 import android.text.Spanned;
 
 import android.text.util.Linkify;
 import android.text.method.LinkMovementMethod;
 
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.app.PendingIntent;
 import android.widget.RemoteViews;
 
 
 public class Stuff extends Activity {
 	
 	private Context xCtx;
 	private static int SHORTCUT_NID = -1;
 	
 	@Override
 	public void onCreate(Bundle si) {
 		super.onCreate(si);
 	}
 	
 	public Stuff(Context c) {
 		xCtx = c;
 	}
 	
 	
 	/* total amount of all icons */
 	public final int getIconCount() {
 		return 68;
 	}
 	
 	/* total amount of 'dot' icons */
 	public final int getDotIconCount() {
 		return 4;
 	}
 	
 	public void UpdateNewNoteShortcut() {
 		NotificationManager notify_manager = (NotificationManager) xCtx.getSystemService(xCtx.NOTIFICATION_SERVICE);
 		
 		notify_manager.cancel(SHORTCUT_NID);
 		
 		if( (new ConfigUtil(xCtx)).ShowNoteShortcut() == false ) {
 			return; /* early return */
 		}
 		
		long fake_time    = android.os.Build.VERSION.SDK_INT >= 9 ? -Long.MAX_VALUE : Long.MAX_VALUE;
 		
 		RemoteViews rview = new RemoteViews(xCtx.getPackageName(), R.layout.shortcut_notification);
 		Notification n    = new Notification(R.drawable.trans, null, 0);
 		Intent xintent    = new Intent(xCtx, NewNoteDialog.class);
 		PendingIntent xpi = PendingIntent.getActivity(xCtx, 0, xintent, 0);
 		
 		rview.setImageViewResource(R.id.image, R.drawable.plus);
 		rview.setTextViewText(R.id.title, "New note");
 		rview.setTextViewText(R.id.text, "Click here to create a new note.");
 		
 		n.contentView   = rview;
 		n.contentIntent = xpi;
 		n.when          = fake_time;  // pushes notification to the right corner
		n.flags        |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
 		
 		notify_manager.notify(SHORTCUT_NID, n);
 		
 	}
 	
 	
 	public void ShowAboutDialog() {
 		Resources res = xCtx.getResources();
 		TextView msg  = new TextView(xCtx);
 		Spanned s     = Html.fromHtml(""
 			+"<h2>"+res.getString(R.string.app_name)+" "+res.getString(R.string.app_vers)+"</h2>"
 			+"<h4>(C) 2012 Adrian Ulrich</h4>"
 			+"<i><b>Icon sources:</b></i>"
 			+"<br> CC icons from <a href='http://wefunction.com/2008/07/function-free-icon-set/'>Function Icon Set</a>"
 			+"<br> Icon 56-65 from <a href='http://www.smashingmagazine.com/2010/04/15/the-ultimate-free-web-designer-s-icon-set-750-icons-incl-psd-sources/'>smashingmagazine</a>"
 			+"<br> Icon 34+45 from <a href='http://www.iconfinder.com/browse/iconset/Some_icons_png/#readme'>'Some icons' Icon Set</a>"
 			+"<br> Everything else is from <a href='http://www.oxygen-icons.org/'>the oxygen iconset</a>"
 			+"<br><br>"
 			+"<h4>Please <a href='https://market.android.com/details?id=ch.blinkenlights.android.donate'>DONATE</a> if you like this application :-)</h4>"
 		);
 		
 		msg.setText(s);
 		msg.setMovementMethod(LinkMovementMethod.getInstance());
 
 		
 		
 		AlertDialog ad = new AlertDialog.Builder(xCtx).create();
 		
 		ad.setTitle(R.string.menu_about);
 		ad.setView(msg);
 		ad.setButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface d, int w) {
 				return;
 			}});
 		
 		ad.show();
 		
 		
 	}
 }
