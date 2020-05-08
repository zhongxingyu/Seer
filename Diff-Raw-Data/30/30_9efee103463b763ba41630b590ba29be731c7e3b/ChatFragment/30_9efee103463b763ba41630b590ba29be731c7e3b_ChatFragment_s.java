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
 import directi.androidteam.training.StanzaStore.JID;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.PacketHandlers.MessageHandler;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Roster.RosterEntry;
 import directi.androidteam.training.chatclient.Roster.RosterManager;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 11/9/12
  * Time: 6:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatFragment extends ListFragment {
     private ArrayList<MessageStanza> sconvo;
     private ArrayList<ChatListItem> convo;
     private ChatListAdaptor adaptor;
     private String buddyid="talk.to";
 
 
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         Log.d("fragmentcreated","new fragement");
         if(getArguments()!=null){
             buddyid = (String)getArguments().get("from");
             Log.d("buddyid",buddyid);
             sconvo = MessageHandler.getInstance().getFragList(buddyid);
             convo = toChatListItemList(sconvo);
 
         }
         else
             convo = new ArrayList<ChatListItem>();
 
    //    MessageHandler.getInstance().getChatLists().get(buddyid).registerFragment(this);
         MessageManager.getInstance().registerFragment(this);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState){
         super.onActivityCreated(savedInstanceState);
         adaptor = new ChatListAdaptor(getActivity(),convo);
 
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
                 closeFragment(view);
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
         PacketWriter.addToWriteQueue(messageStanza.getXml());
     }
 
     public static ChatFragment getInstance(String from){
         ChatFragment curfrag = new ChatFragment();
         Bundle args = new Bundle();
         args.putString("from",from);
         Log.d("XXXX", "from is " + from);
         curfrag.setArguments(args);
         return curfrag;
     }
 
    public void insertMessage(MessageStanza message){
        Log.d("XXX","chat fragment for: "+buddyid);
        sconvo.add(message);
    }
     public void addChatItem(MessageStanza message){
         ChatListItem cli = new ChatListItem(message);
         convo.add(cli);
         PacketStatusManager.getInstance().pushCliPacket(cli);
         ChatBox.adaptorNotify(this);
         Log.d("chatlistitemsize",message.getBody());
     }
     public static boolean isSender(MessageStanza message){
         return message.getFrom().equals(JID.getJid().split("/")[0]);
     }
 
     public void notifyAdaptor(){
 

         adaptor.notifyDataSetChanged();
         ListView lv = getListView();
         lv.setFocusable(true);
 
        if(lv.getChildCount()!=0){
             lv.getChildAt(lv.getChildCount()-1).setFocusable(true);
             lv.setSelection(lv.getChildCount()-1);
         }
    }

    public String getBuddyid(){
        return buddyid;
     }
 
     @Override
     public void onResume(){
         super.onResume();
         notifyAdaptor();
         Log.d("fragmentresume",buddyid);
 
     }
 
     @Override
     public void onPause(){
         super.onPause();
         Log.d("fragmentpause",buddyid);
 
     }
 
     private ArrayList<ChatListItem> toChatListItemList(ArrayList<MessageStanza> list){
         ArrayList<ChatListItem> conv;
         conv = new ArrayList<ChatListItem>();
         for (MessageStanza s : list) {
             ChatListItem cli = new ChatListItem(s);
             conv.add(cli);
         }
         return  conv;
     }
 
     public void closeFragment(View view){
         Log.d("closewindow","click");
         MessageHandler.getInstance().getChatLists().remove(buddyid);
         if(MessageHandler.getInstance().getChatLists().size()==0)
             ChatBox.finishActivity();
         ChatBox.recreateFragments();
     }
 
 
 
 }
