 package directi.androidteam.training.chatclient.Chat;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.PacketHandlers.MessageHandler;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.DisplayRosterActivity;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 3/9/12
  * Time: 7:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatBox extends FragmentActivity {
     private static Context context;
     //private final String buddy="vinayak.bhavnani@gmail.com";
     private ArrayList<String> chatlist;
     private ArrayAdapter<String> adaptor;
     private static FragmentSwipeAdaptor frag_adaptor;
     private static ViewPager viewPager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.d("chatboxcreated","cc");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.chat);
         context=this;
         //moveTaskToBack(true);
         frag_adaptor = new FragmentSwipeAdaptor(getSupportFragmentManager());
         viewPager = (ViewPager)findViewById(R.id.pager);
         viewPager.setAdapter(frag_adaptor);
         viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
             @Override
             public void onPageScrolled(int i, float v, int i1) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onPageSelected(int i) {
                 Log.d("XXX","index: "+i);
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onPageScrollStateChanged(int i) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
         String from =  (String) getIntent().getExtras().get("buddyid");
         if(getIntent().getExtras().containsKey("notification"))
             cancelNotification();
         if(from != null)
             switchFragment(from);
 
 
 
         //ListView list = (ListView) findViewById(R.id.chatlist);
         //chatlist = new ArrayList<String>();
         //adaptor = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,chatlist);
         //list.setAdapter(adaptor);
 
     }
 
     public static void openChat(String from){
 
         Intent intent = new Intent(context, ChatBox.class);
         intent.putExtra("buddyid",from);
         intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         //intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
         //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startActivity(intent);
 
     }
     public static void adaptorNotify(final ChatListAdaptor adap){
         Activity a = (Activity) context;
         //Log.d("ssss","updateroster called");
         a.runOnUiThread(new Runnable() { public void run() {
             adap.notifyDataSetChanged();
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
         String from = (String)intent.getExtras().get("buddyid");
         if(intent.getExtras().containsKey("notification"))
             cancelNotification();
         //viewPager.setCurrentItem(0);
         if(from!=null)
             switchFragment(from);
     }
 
     @Override
     public void onResume(){
         super.onResume();
         //moveTaskToBack(false);
         Log.d("Chatboxresumed","true");
     }
     public void SendChat(View view){
         EditText mess = (EditText) findViewById(R.id.message);
         String message = mess.getText().toString();
         //String buddy = ((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.chatlist)).getBuddyid();
         int currentItem = viewPager.getCurrentItem();
         Log.d("XXX","current view: "+currentItem);
         int position = currentItem;
 
 
         //ChatFragment fragment = (ChatFragment)(frag_adaptor.getItem(currentItem));
         //Log.d("XXXX", "fragment " + fragment.getArguments().get("from"));
         //String buddy = fragment.getBuddyid();
         MessageStanza messxml = new MessageStanza(MessageHandler.getInstance().FragToJid(position),message);
         PacketWriter.addToWriteQueue(messxml.getXml());
         MessageHandler.getInstance().addChat(MessageHandler.getInstance().FragToJid(position),messxml);
         viewPager.setCurrentItem(position);
         mess.setText("");
         //frag_adaptor.notifyDataSetChanged();
 
         //chatlist.add(message);
         //adaptor.notifyDataSetChanged();
         //ChatFragment fragment =  (ChatFragment)getSupportFragmentManager().findFragmentById(R.id.chatlist);
         //fragment.insertMessage(messxml);
     }
     public void GotoRoster(View view){
         Intent intent = new Intent(context, DisplayRosterActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         startActivity(intent);
     }
     private void switchFragment(String from){
         int frag = MessageHandler.getInstance().JidToFrag(from);
         Log.d("indexreturned",new Integer(frag).toString());
         viewPager.setCurrentItem(frag);
         //frag_adaptor.notifyDataSetChanged();
         //ChatFragment curfrag = (ChatFragment)(frag_adaptor.getItem(frag));
         //curfrag.notifyAdaptor();
     }
 
     public static void recreateFragments() {
         Activity a = (Activity) context;
         //Log.d("ssss","updateroster called");
         a.runOnUiThread(new Runnable() { public void run() {
             frag_adaptor.notifyDataSetChanged();
         }}
         );
         //frag_adaptor.notifyDataSetChanged();
         //To change body of created methods use File | Settings | File Templates.
     }
 }
