 package com.androidproductions.ics.sms;
 
 import android.app.ActionBar;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewManager;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.QuickContactBadge;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.androidproductions.ics.sms.messaging.MessageUtilities;
 import com.androidproductions.libs.sms.InternalTransaction;
 import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;
 
 import java.util.List;
 
 public class UnsentMessages extends AdSupportedActivity {
 
     private LinearLayout smsList;
 
     private IMessageView PressedMessage;
 
     /** Called when the activity is first created. */
 	@Override
     protected void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         smsList = (LinearLayout) findViewById(R.id.smsList);
         final ActionBar ab = getActionBar();
         if (ab != null) {
             ab.setHomeButtonEnabled(true);
             ab.setTitle(R.string.unsentTitle);
             ab.setDisplayHomeAsUpEnabled(true);
         }
     }
 
     public boolean onCreateOptionsMenu(final Menu menu) {
         final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.bare_menu, menu);
         return true;
     }
 
 	@Override
 	public void onResume() {
         super.onResume();
         redrawView();
         InitializeAds();
     }
 
     private void redrawView()
     {
     	final List<IMessageView> smss = MessageUtilities.GetUnsentMessages(UnsentMessages.this);
         smsList.removeAllViews();
         for (final IMessageView sms : smss)
         {
         	final View child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_summary, null);
         	final SpannableString name = new SpannableString(sms.getContactName());
         	final SpannableString body = new SpannableString(sms.getBody());
         	final SpannableString time = new SpannableString(sms.GetShortDateString());
         	((TextView)child.findViewById(R.id.contact_name)).setText(name);
         	((TextView)child.findViewById(R.id.messageContent)).setText(body);
         	((TextView)child.findViewById(R.id.messageTime)).setText(time);
             ((QuickContactBadge)child.findViewById(R.id.contact_photo)).assignContactFromPhone(sms.getAddress(),true);
         	new Thread(new Runnable() {
 				public void run() {
 					((ImageView)child.findViewById(R.id.contact_photo)).setImageBitmap(sms.getConversationContactImage());
 				}
 			}).run();
 			child.setTag(sms);
         	child.setOnClickListener(new OnClickListener() {
 				public void onClick(final View v) {
                     ShowDetails(sms);
                 }
 			});
             registerForContextMenu(child);
         	smsList.addView(child);
         }
     }
 
     @Override
     public void onCreateContextMenu(final ContextMenu menu, final View v,
                                     final ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         PressedMessage = (IMessageView)v.getTag();
         final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.unsent_action_menu,menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // app icon in action bar clicked; go home
                 final Intent intent = new Intent(this, ICSSMSActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             case R.id.settings:
             	final Intent prefintent = new Intent(this, Preferences.class);
             	prefintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(prefintent);
             	return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onContextItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
             case R.id.delete:
                 if (new InternalTransaction(UnsentMessages.this).DeleteMessage(PressedMessage))
                 {
                     final View v = smsList.findViewWithTag(PressedMessage);
                     ((ViewManager)v.getParent()).removeView(v);
                 }
                 else
                 {
                     Toast.makeText(this, getString(R.string.deleteLocked), Toast.LENGTH_SHORT).show();
                 }
                 return true;
             case R.id.details:
                 ShowDetails(PressedMessage);
                 return true;
             case R.id.resend:
                 MessageUtilities.SendMessage(UnsentMessages.this, PressedMessage.getBody(), PressedMessage.getAddress());
                 new InternalTransaction(UnsentMessages.this).DeleteMessage(PressedMessage);
                 redrawView();
                 return true;
             default:
                 return false;
         }
     }
 
     private void ShowDetails(final IMessageView message) {
         final Dialog dialog = new Dialog(UnsentMessages.this);
 
         dialog.setContentView(R.layout.sms_details);
         dialog.setTitle("Message Details");
         if (!message.IsIncoming())
         {
             ((TextView)dialog.findViewById(R.id.labelLocation)).setText("To:");
             ((TextView)dialog.findViewById(R.id.labelRecieved)).setText("Sent:");
         }
        ((TextView)dialog.findViewById(R.id.valueLocation)).setText(message.getAddress());
        ((TextView)dialog.findViewById(R.id.valueRecieved)).setText(message.GetDateString());
         dialog.show();
     }
 }
