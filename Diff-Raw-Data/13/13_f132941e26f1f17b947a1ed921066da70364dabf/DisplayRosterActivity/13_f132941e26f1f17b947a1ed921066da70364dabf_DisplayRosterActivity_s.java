 package directi.androidteam.training.chatclient.Roster;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import directi.androidteam.training.StanzaStore.JID;
 import directi.androidteam.training.StanzaStore.RosterGet;
 import directi.androidteam.training.chatclient.Authentication.LoginActivity;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rajat
  * Date: 9/3/12
  * Time: 1:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DisplayRosterActivity extends Activity {
     private static Context context;
     public DisplayRosterActivity(){
         context =this;
     }
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.roster);
         Intent intent = getIntent();
         String username =  intent.getStringExtra(LoginActivity.USERNAME);
         LinearLayout linearLayout = (LinearLayout)findViewById(R.id.MainLayout);
         TextView WelcomeView = new TextView(this);
        WelcomeView.setTextSize(5);
         WelcomeView.setText("Welcome " + username);
         setContentView(WelcomeView);
        linearLayout.addView(WelcomeView);
         requestForServices();
         requestForRosters();
     }
 
     private void requestForRosters() {
         Log.d("DEBUG :","entered request for ROSTER_MANAGER");
         RosterGet rosterGet = new RosterGet();
         rosterGet.setSender(JID.jid).setID("google-ROSTER_MANAGER-1").setQueryAttribute("xmlns","jabber:iq:ROSTER_MANAGER");
         PacketWriter.addToWriteQueue(rosterGet.getXml());
     }
     private void requestForServices(){
         Log.d("DEBUG :","entered request for services");
         RosterGet rosterGet = new RosterGet();
         rosterGet.setReceiver("talk.google.com").setQueryAttribute("xlmns", "http://jabber.org/protocol/disco#info");
         PacketWriter.addToWriteQueue(rosterGet.getXml());
     }
 
     public static void showAllRosters() {
         Intent intent = new Intent(context,DisplayRosterActivity.class);
         intent.putExtra("display","all");
         intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         context.startActivity(intent);
     }
     @Override
     public void onNewIntent(Intent intent){
         super.onNewIntent(intent);
         String rosterToBeDisplayed = (String)intent.getExtras().get("display");
         if(rosterToBeDisplayed.equals("all")){
             LinearLayout linearLayout1 = new LinearLayout(context);
             linearLayout1.setId(Integer.parseInt("ContactList"));
             linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
             Button chatButton1 = new Button(context);
             chatButton1.setText("Chat");
             chatButton1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
             linearLayout1.addView(chatButton1);
         }
         else {
             LinearLayout linearLayout1 = new LinearLayout(context);
             linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
             Button chatButton1 = new Button(context);
             chatButton1.setText("Chat");
             chatButton1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
             linearLayout1.addView(chatButton1);
         }
     }
 
 }
