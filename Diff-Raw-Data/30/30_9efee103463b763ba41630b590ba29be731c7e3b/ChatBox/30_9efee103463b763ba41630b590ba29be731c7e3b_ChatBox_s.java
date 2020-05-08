 package directi.androidteam.training.chatclient.Chat;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.bugsense.trace.BugSenseHandler;
 import directi.androidteam.training.ChatApplication;
 import directi.androidteam.training.StanzaStore.JID;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.StanzaStore.RosterGet;
 import directi.androidteam.training.chatclient.Chat.Listeners.ChatViewPageChangeListner;
 import directi.androidteam.training.chatclient.Chat.Listeners.MsgTextChangeListener;
 import directi.androidteam.training.chatclient.Constants;
 import directi.androidteam.training.chatclient.PacketHandlers.MessageHandler;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.DisplayRosterActivity;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 import java.util.UUID;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 3/9/12
  * Time: 7:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatBox extends FragmentActivity {
     private static Context context;
     private static FragmentSwipeAdaptor frag_adaptor;
     private static ViewPager viewPager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.d(Constants.DEBUG_CHATBOX,"created");
         ChatApplication.chatrunning=true;
         super.onCreate(savedInstanceState);
         BugSenseHandler.initAndStartSession(this, Constants.BUGSENSE_API_KEY);
 
         setContentView(R.layout.chat);
         context=this;
         frag_adaptor = new FragmentSwipeAdaptor(getSupportFragmentManager());
         viewPager = (ViewPager)findViewById(R.id.pager);
         viewPager.setAdapter(frag_adaptor);
         viewPager.setOnPageChangeListener(new ChatViewPageChangeListner(context));
         String from =  (String) getIntent().getExtras().get("buddyid");
         if(getIntent().getExtras().containsKey("notification"))
             cancelNotification();
         if(from != null) {
             EditText editText = (EditText) findViewById(R.id.message);
             editText.addTextChangedListener(new MsgTextChangeListener(from));
             switchFragment(from);
             sendDiscoInfoQuery(from);
         }
         ActionBar ab = getActionBar();
         ab.hide();
     }
 
     private void sendDiscoInfoQuery(String from) {
         String queryAttr = "http://jabber.org/protocol/disco#info";
         RosterGet rosterGet = new RosterGet();
         rosterGet.setSender(JID.getJid()).setReceiver(from).setQueryAttribute("xmlns",queryAttr).setID(UUID.randomUUID().toString());
         PacketWriter.addToWriteQueue(rosterGet.getXml());
     }
 
     public void updateHeader(int i){
         TextView hleft = (TextView)findViewById(R.id.chatboxheader_left);
         TextView hright = (TextView)findViewById(R.id.chatboxheader_right);
 
         String left = MessageHandler.getInstance().FragToJid(i-1);
         String right = MessageHandler.getInstance().FragToJid(i+1);
         if(left!=null)
             hleft.setText(left.split("@")[0]);
         else
             hleft.setText("");
         if(right!=null)
             hright.setText(right.split("@")[0]);
         else
             hright.setText("");
     }
 
     public static void openChat(String from){
         Intent intent = new Intent(ChatApplication.getAppContext(), ChatBox.class);
         intent.putExtra("buddyid",from);
         intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         context.startActivity(intent);
 
     }
     public static void adaptorNotify(final ChatFragment cfrag){
         Activity a = (Activity) context;
         a.runOnUiThread(new Runnable() { public void run() {
             cfrag.notifyAdaptor();
         }}
         );
     }
     public static void notifyChat(MessageStanza ms){
        if(viewPager.getCurrentItem()==MessageHandler.getInstance().JidToFrag(ms.getFrom()))
             return;
 
         ChatNotifier cn = new ChatNotifier(context);
         cn.notifyChat(ms);
     }
     public static void cancelNotification(){
         ChatNotifier cn = new ChatNotifier(context);
         cn.cancelNotification();
     }
     @Override
     public void onNewIntent(Intent intent){
         Log.d("newintent","intent");
         super.onNewIntent(intent);
         if(intent.getExtras().containsKey("error")){
             notifyConnectionError();
             return;
         }
         if (intent.getExtras().containsKey("finish")){
             this.finish();
         }
         String from = (String)intent.getExtras().get("buddyid");
         if(intent.getExtras().containsKey("notification"))
             cancelNotification();
         if(from!=null)
         {
             EditText editText = (EditText) findViewById(R.id.message);
             editText.addTextChangedListener(new MsgTextChangeListener(from));
             switchFragment(from);
             sendDiscoInfoQuery(from);
         }
     }
     public void notifyConnectionError(){
         TextView textView = (TextView)findViewById(R.id.chatbox_networknotification);
         textView.setVisibility(0);
     }
     @Override
     public void onResume(){
         super.onResume();
         Log.d("Chatboxresumed","true");
     }
     public void SendChat(View view){
         EditText mess = (EditText) findViewById(R.id.message);
         String message = mess.getText().toString();
         if(message==null || message.equals(""))
             return;
         int currentItem = viewPager.getCurrentItem();
         Log.d("XXX","current view: "+currentItem);
         int position = currentItem;
 
         MessageStanza messxml = new MessageStanza(MessageHandler.getInstance().FragToJid(position),message);
         messxml.setCreater(JID.getJid());
         messxml.formActiveMsg();
         PacketStatusManager.getInstance().pushMsPacket(messxml);
         MessageManager.getInstance().insertMessage(MessageHandler.getInstance().FragToJid(position),messxml);
         PacketWriter.addToWriteQueue(messxml.getXml());
 
         viewPager.setCurrentItem(position);
         mess.setText("");
     }
     public void resendMessage(View view){
         TextView tv = (TextView)findViewById(R.id.chatlistitem_status);
         tv.setVisibility(0);
     }
     public static Context getContext() {
         return context;
     }
 
     public void GotoRoster(View view){
         Intent intent = new Intent(ChatApplication.getAppContext(), DisplayRosterActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         startActivity(intent);
         //send inactives to chatlist
         Log.d("oncreate :","coming back to roster activity");
     }
     private void switchFragment(String from){
         int frag = MessageHandler.getInstance().JidToFrag(from);
         Log.d("indexreturned",new Integer(frag).toString());
         updateHeader(frag);
         viewPager.setCurrentItem(frag);
     }
 
     public static void recreateFragments() {
         Activity a = (Activity) context;
         if(context!=null)
         a.runOnUiThread(new Runnable() { public void run() {
             frag_adaptor.notifyDataSetChanged();
         }}
         );
     }
 
     public static void finishActivity(){
         Intent intent = new Intent(context,ChatBox.class);
         intent.putExtra("finish",true);
         context.startActivity(intent);
     }
 
     public static void composeToast(final String s) {
         Activity application = (Activity) context;
         if(context!=null)
         application.runOnUiThread(new Runnable() {
             public void run() {
                 Toast.makeText(ChatApplication.getAppContext(), s, Toast.LENGTH_LONG).show();
             }
         }
         );
     }
 }
