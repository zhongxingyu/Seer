 package directi.androidteam.training.chatclient.Chat;
 
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.RosterItem;
 import directi.androidteam.training.chatclient.Roster.RosterManager;
 
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 11/9/12
  * Time: 6:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatFragment extends ListFragment {
     private Vector<ChatListItem> chatListItems;
     private ChatListAdaptor adaptor;
     private String buddyid="talk.to";
     private String myAccountUID;
 
     public String getMyAccountUID() {
         return myAccountUID;
     }
 
     public ChatFragment() {
     }
 
     public ChatFragment(String from) {
         this.buddyid = from;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         if(getArguments()!=null){
             buddyid = (String)getArguments().get("from");
             chatListItems = toChatListItemList(MyFragmentManager.getInstance().getFragList(buddyid));
             myAccountUID = (String)getArguments().get("accountUID");
         }
         else if(!buddyid.equals(("talk.to")))
                 chatListItems = toChatListItemList(MyFragmentManager.getInstance().getFragList(buddyid));
         else
             chatListItems = new Vector<ChatListItem>();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState){
         super.onActivityCreated(savedInstanceState);
         adaptor = new ChatListAdaptor(getActivity(), chatListItems);
 
         ListView lv = getListView();
         LayoutInflater linf = getLayoutInflater(savedInstanceState);
         ViewGroup header = (ViewGroup)linf.inflate(R.layout.chatlistheader,lv,false);
         lv.addHeaderView(header,null,false);
 
         TextView tv = (TextView)(header.findViewById(R.id.chatfragment_jid));
         tv.setText(buddyid);
         ImageView imageView = (ImageView) (header.findViewById(R.id.chatfragment_image));
         TextView status = (TextView)(header.findViewById(R.id.chatfragment_status));
         RosterItem re = RosterManager.getInstance().getRosterItem(myAccountUID, buddyid);
        imageView.setImageBitmap(re.getAvatar());
 
         ImageView presence = (ImageView)(header.findViewById(R.id.chatfragment_availability_image));
 
         ImageView closeWindow = (ImageView)(header.findViewById(R.id.chatlistheader_close));
         closeWindow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 MyFragmentManager.getInstance().removeFragEntry(buddyid);
                 sendGoneMsg();
                 closeFragment(buddyid);
             }
         });
         if(re!=null){
             status.setText(re.getStatus());
             if(re.getPresence().equals("dnd")){
                 presence.setImageResource(R.drawable.red);
             }
             else if(re.getPresence().equals("chat")){
                 presence.setImageResource(R.drawable.green);
             }
             else if(re.getPresence().equals("away")){
                 presence.setImageResource(R.drawable.yellow);
             }
         }
         else status.setText("null");
         setListAdapter(adaptor);
 
     }
 
     private void sendGoneMsg() {
         MessageStanza messageStanza = new MessageStanza(buddyid);
         messageStanza.formGoneMsg();
         messageStanza.send(getMyAccountUID());
     }
 
     public static ChatFragment getInstance(String from){
         ChatFragment chatFragment = new ChatFragment(from);
         Bundle args = new Bundle();
         args.putString("from", from);
         chatFragment.setArguments(args);
         return chatFragment;
     }
 
     public void addChatItem(MessageStanza message, boolean b){
         if(chatListItems==null) {
             return;
         }
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
 
     private synchronized Vector<ChatListItem> toChatListItemList(Vector<MessageStanza> list){
         Vector<ChatListItem> chatItemList = new Vector<ChatListItem>();
         Object[] objects = list.toArray();
         for (Object object : objects) {
             ChatListItem cli = new ChatListItem((MessageStanza) object);
             chatItemList.add(cli);
         }
 
         return  chatItemList;
     }
 
     private void closeFragment(String jid){
         if(MyFragmentManager.getInstance().getSizeofActiveChats()==0)
             ChatBox.finishActivity();
         else {
          //   ChatBox.notifyFragmentAdaptorInSameThread();
             ChatBox.removeFragmentviaFragManager(jid);
            ChatBox.notifyFragmentAdaptorInSameThread();
         }
     }
 
 }
