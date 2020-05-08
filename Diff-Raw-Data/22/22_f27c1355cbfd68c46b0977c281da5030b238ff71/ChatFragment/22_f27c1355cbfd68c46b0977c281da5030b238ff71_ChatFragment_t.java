 package directi.androidteam.training.chatclient.Chat;
 
 
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.RosterEntry;
 import directi.androidteam.training.chatclient.Roster.RosterManager;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 11/9/12
  * Time: 6:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatFragment extends ListFragment {
     private ArrayList<ChatListItem> chatListItems;
     private ChatListAdaptor adaptor;
     private String buddyid="talk.to";
 
     private ChatFragment() {
 
     }
 
 
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         if(getArguments()!=null){
             buddyid = (String)getArguments().get("from");
             chatListItems = toChatListItemList(FragmentManager.getInstance().getFragList(buddyid));
 
         }
         else
             chatListItems = new ArrayList<ChatListItem>();
 
         MessageManager.getInstance().registerFragment(this);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState){
         super.onActivityCreated(savedInstanceState);
 
         adaptor = new ChatListAdaptor(getActivity(), chatListItems);
 
         ListView lv = getListView();
         LayoutInflater linf = getLayoutInflater(savedInstanceState);
         ViewGroup header = (ViewGroup)linf.inflate(R.layout.chatlistheader,lv,false);
         TextView tv = (TextView)(header.findViewById(R.id.chatfragment_jid));
         tv.setText(buddyid);
         TextView status = (TextView)(header.findViewById(R.id.chatfragment_status));
         RosterEntry re = RosterManager.getInstance().searchRosterEntry(buddyid);
         TextView presence = (TextView)(header.findViewById(R.id.chatheader_presence));
         ImageView closeWindow = (ImageView)(header.findViewById(R.id.chatlistheader_close));
         closeWindow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 sendGoneMsg(buddyid);
                 FragmentManager.getInstance().removeFragEntry(buddyid);
                //ChatBox.deletePage();
                 //ChatBox.recreateFragments();
                 closeFragment();
             }
         });
         if(re!=null){
             status.setText(re.getStatus());
             Log.d("statusmess",re.getPresence()+buddyid);
 
             if(re.getPresence().equals("dnd")){
                 presence.setTextColor(Color.RED);
                 Log.d("statusmess1",re.getPresence());
                 presence.setText("Busy");
             }
             else if(re.getPresence().equals("chat")){
                 presence.setTextColor(Color.GREEN);
                 presence.setText("Available");
             }
             else if(re.getPresence().equals("away")){
                 presence.setTextColor(Color.YELLOW);
                 presence.setText("away");
             }
         }
         else status.setText("null");
         lv.addHeaderView(header,null,false);
         setListAdapter(adaptor);
     }
 
     private void sendGoneMsg(String buddyid) {
         MessageStanza messageStanza = new MessageStanza(buddyid);
         messageStanza.formGoneMsg();
         messageStanza.send();
     }
 
     public static ChatFragment getInstance(String from){
         ChatFragment chatFragment = new ChatFragment();
         Bundle args = new Bundle();
         args.putString("from", from);
         chatFragment.setArguments(args);
         return chatFragment;
     }
 
     public void addChatItem(MessageStanza message, boolean b){
         ChatListItem cli = new ChatListItem(message);
         if(b && chatListItems.size()>0)
             chatListItems.remove(chatListItems.size()-1); //added  - 3/10
         chatListItems.add(cli);
         PacketStatusManager.getInstance().pushCliPacket(cli);
         ChatBox.adaptorNotify(this);
     }
 
     public void notifyAdaptor(){
 
         adaptor.notifyDataSetChanged();
         if(isVisible() || isResumed()) {  //added
         ListView lv = getListView();
         lv.setFocusable(true);
 
         if(lv.getChildCount()>0){
             lv.getChildAt(lv.getChildCount()-1).setFocusable(true);
             lv.setSelection(lv.getChildCount()-1);
         }
         }
     }
 
     @Override
     public void onResume(){
         super.onResume();
         notifyAdaptor();
     }
 
     @Override
     public void onPause(){
         super.onPause();
     }
 
     private ArrayList<ChatListItem> toChatListItemList(ArrayList<MessageStanza> list){
         ArrayList<ChatListItem> chatItemList;
         chatItemList = new ArrayList<ChatListItem>();
         for (MessageStanza s : list) {
             ChatListItem cli = new ChatListItem(s);
             chatItemList.add(cli);
         }
         return  chatItemList;
     }
 
     public void closeFragment(){
         //MessageManager.getInstance().removeEntry(buddyid);
         MessageStanza messageStanza = new MessageStanza(buddyid);
         messageStanza.formGoneMsg();
         messageStanza.send();
         if(FragmentManager.getInstance().getSizeofActiveChats()==0)
             ChatBox.finishActivity();
         ChatBox.recreateFragments();
     }
 
 }
