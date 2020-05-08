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
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.StanzaStore.RosterGet;
 import directi.androidteam.training.chatclient.Chat.Listeners.ChatViewPageChangeListner;
 import directi.androidteam.training.chatclient.Chat.Listeners.MsgTextChangeListener;
 import directi.androidteam.training.chatclient.Constants;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.DisplayRosterActivity;
 
 
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
     private static Toast toast;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
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
             MyFragmentManager.getInstance().addFragEntry(from);
             EditText editText = (EditText) findViewById(R.id.enter_message);
             editText.addTextChangedListener(new MsgTextChangeListener(from));
             switchFragment(from);
             sendDiscoInfoQuery(from);
         }
         ActionBar ab = getActionBar();
         ab.hide();
         Log.d("DDDD","oncreate done");
     }
 
     private void sendDiscoInfoQuery(String from) {
         String queryAttr = "http://jabber.org/protocol/disco#info";
         RosterGet rosterGet = new RosterGet();
         rosterGet.setReceiver(from).setQueryAttribute("xmlns",queryAttr);
         rosterGet.send();
     }
 
     public void updateHeader(int i){
         TextView hleft = (TextView)findViewById(R.id.chatboxheader_left);
         TextView hright = (TextView)findViewById(R.id.chatboxheader_right);
 
         String left = MyFragmentManager.getInstance().getJidByFragId(i - 1);
         String right = MyFragmentManager.getInstance().getJidByFragId(i + 1);
         if(left!=null)
             hleft.setText(left.split("@")[0]);
         else
             hleft.setText("");
         if(right!=null)
             hright.setText(right.split("@")[0]);
         else
             hright.setText("");
     }
 
     public static void adaptorNotify(final ChatFragment cfrag){
         Activity a = (Activity) context;
         if(context!=null && cfrag!=null)
         a.runOnUiThread(new Runnable() {
             public void run() {
                 cfrag.notifyAdaptor();
             }
         }
         );
     }
 
     public static void notifyChat(MessageStanza ms, String from){
         if(viewPager.getCurrentItem()!= MyFragmentManager.getInstance().JidToFragId(ms.getFrom())) {
             ChatNotifier cn = new ChatNotifier(context);
             cn.notifyChat(ms);
             Log.d("xcxc","notficatn done when chatcontext is not null");
         }
         MyFragmentManager.getInstance().addFragEntry(from);
         Log.d("xcxc","before insert msg");
         MessageManager.getInstance().insertMessage(from,ms);
     }
 
     public static void cancelNotification(){
         ChatNotifier cn = new ChatNotifier(context);
         cn.cancelNotification();
     }
 
     @Override
     public void onNewIntent(Intent intent){
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
             EditText editText = (EditText) findViewById(R.id.enter_message);
             editText.addTextChangedListener(new MsgTextChangeListener(from));
             MyFragmentManager.getInstance().addFragEntry(from);
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
 
     public void onClick(View view) {
         int id = view.getId();
         if(id==R.id.sendmessage_button) {
              sendChat();
         }
         else if(id==R.id.chatlistitem_status) {
             resendMessage();
         }
         else if(id == R.id.chatboxheader_roster) {
             GotoRoster();
         }
     }
 
     private void sendChat(){
         EditText mess = (EditText) findViewById(R.id.enter_message);
         String message = mess.getText().toString();
         if(message==null || message.equals(""))
             return;
         int currentItem = viewPager.getCurrentItem();
 
         String jid = MyFragmentManager.getInstance().getJidByFragId(currentItem);
         MessageStanza messxml = new MessageStanza(jid,message);
         messxml.formActiveMsg();
         messxml.send();
 
         PacketStatusManager.getInstance().pushMsPacket(messxml);
         MyFragmentManager.getInstance().addFragEntry(jid);
         MessageManager.getInstance().insertMessage(jid, messxml);
 
         viewPager.setCurrentItem(currentItem);
 
         mess.setText("");
     }
 
     private void resendMessage(){
         TextView tv = (TextView)findViewById(R.id.chatlistitem_status);
         tv.setVisibility(0);
     }
 
     public static Context getContext() {
         return context;
     }
 
     private void GotoRoster(){
         Intent intent = new Intent(ChatApplication.getAppContext(), DisplayRosterActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         startActivity(intent);
         Object[] objects = MessageManager.getInstance().getMessageStore().keySet().toArray();
         for (Object object : objects) {
             MessageStanza messageStanza = new MessageStanza((String) object);
             messageStanza.formInActiveMsg();
             messageStanza.send();
         }
     }
     private void switchFragment(String from){
         int frag = MyFragmentManager.getInstance().JidToFragId(from);
         updateHeader(frag);
         viewPager.setCurrentItem(frag);
     }
 
     public static void notifyFragmentAdaptorInNewUIThread() {
         if(ChatBox.getContext()==null)
             return;
         Activity a = (Activity) context;
         if(context!=null)
         a.runOnUiThread(new Runnable() { public void run() {
             frag_adaptor.notifyDataSetChanged();
         }}
         );
     }
 
     public static void notifyFragmentAdaptorInSameThread() {
         frag_adaptor.notifyDataSetChanged();
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
                 if(toast!=null)
                     toast.cancel();
                 toast = Toast.makeText(ChatApplication.getAppContext(), s, Toast.LENGTH_SHORT);
                 toast.show();
             }
         }
         );
     }
 }
