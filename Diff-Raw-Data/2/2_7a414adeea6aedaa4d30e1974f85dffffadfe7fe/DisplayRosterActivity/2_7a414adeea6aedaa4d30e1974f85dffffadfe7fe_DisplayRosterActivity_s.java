 package directi.androidteam.training.chatclient.Roster;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import directi.androidteam.training.StanzaStore.JID;
 import directi.androidteam.training.StanzaStore.PresenceS;
 import directi.androidteam.training.StanzaStore.RosterGet;
 import directi.androidteam.training.chatclient.Authentication.UserListActivity;
 import directi.androidteam.training.chatclient.Constants;
 import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 import java.util.UUID;
 
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
         ImageView myImage = (ImageView) findViewById(R.id.Roster_myimage);
         attachIcon(myImage);
         TextView textView = (TextView) findViewById(R.id.Roster_myjid);
         textView.setText(JID.jid);
         TextView textView2 = (TextView) findViewById(R.id.Roster_mystatus);
         textView2.setText(MyProfile.getInstance().getStatus());
         Spinner spinner = (Spinner) findViewById(R.id.roster_availability_spinner);
         spinner.setOnItemSelectedListener(new RosterAvailSpinnerHandler());
         Button button = (Button) findViewById(R.id.roster_availability_launch_spinner_button);
         button.setBackgroundColor(Color.GREEN);
         Log.d("XXXX", "oncreate roster : " + MyProfile.getInstance().getStatus());
         requestForRosters();
         sendInitialPresence();
     }
 
     private void sendInitialPresence() {
         PresenceS presenceS = new PresenceS();
         PacketWriter.addToWriteQueue(presenceS.getXml());
     }
 
     private int dpToPx(int dp)
     {
         float density = context.getResources().getDisplayMetrics().density;
         return Math.round((float)dp * density);
     }
     public void attachIcon(ImageView view) {
         Drawable drawing = view.getDrawable();
         Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();
         int width = bitmap.getWidth();
         int height = bitmap.getHeight();
         int bounding = dpToPx(Constants.login_icon_size);
         float xScale = ((float) bounding) / width;
         float yScale = ((float) bounding) / height;
         float scale = (xScale <= yScale) ? xScale : yScale;
         Matrix matrix = new Matrix();
         matrix.postScale(scale, scale);
         Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
         width = scaledBitmap.getWidth();
         height = scaledBitmap.getHeight();
         BitmapDrawable result = new BitmapDrawable(scaledBitmap);
         view.setImageDrawable(result);
         view.setScaleType(ImageView.ScaleType.FIT_START);
         LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
         params.width = width;
         params.height = height;
         view.setLayoutParams(params);
     }
 
     private void requestForRosters() {
         Log.d("ROSTER :","entered request for ROSTER_MANAGER");
         RosterGet rosterGet = new RosterGet();
         rosterGet.setSender(JID.jid).setID(UUID.randomUUID().toString()).setQueryAttribute("xmlns","jabber:iq:roster").setQueryAttribute("xmlns:gr","google:roster").setQueryAttribute("gr:ext","2");
         PacketWriter.addToWriteQueue(rosterGet.getXml());
         Log.d("ROSTER :", "done requesting");
     }
     private void requestForPresence(String typeVal) {
         RosterManager rosterManager = RosterManager.getInstance();
         for (RosterEntry rosterEntry : rosterManager.getRosterList()) {
             PresenceS presence = new PresenceS();
             presence.addID(UUID.randomUUID().toString());
             presence.addReceiver(rosterEntry.getJid());
             presence.addType(typeVal);
             PacketWriter.addToWriteQueue(presence.getXml());
             Log.d("ROSTER :", "entered request for presence");
         }
 
     }
     private void requestForServices(){
         Log.d("DEBUG :", "entered request for services");
         RosterGet rosterGet = new RosterGet();
         rosterGet.setReceiver("talk.google.com").setQueryAttribute("xlmns", "http://jabber.org/protocol/disco#info");
         PacketWriter.addToWriteQueue(rosterGet.getXml());
     }
 
     public static void showAllRosters() {
         Intent intent = new Intent(context,DisplayRosterActivity.class);
         Log.d("XXXX","show AllRosters Called");
         intent.putExtra("display", "all");
         intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         context.startActivity(intent);
     }
     @Override
     public void onNewIntent(Intent intent){
         super.onNewIntent(intent);
         Log.d("ROSTER INTENT :", "New Intent Started");
         ImageView myImage = (ImageView) findViewById(R.id.Roster_myimage);
         attachIcon(myImage);
         TextView textView2 = (TextView) findViewById(R.id.Roster_mystatus);
         textView2.setText(MyProfile.getInstance().getStatus());
         Button button = (Button) findViewById(R.id.roster_availability_launch_spinner_button);
         String avail = MyProfile.getInstance().getAvailability();
        if(avail.equals("available") || avail.equals("chat"))
         button.setBackgroundColor(Color.GREEN);
         else if(avail.equals("away"))
             button.setBackgroundColor(Color.YELLOW);
         else if(avail.equals("dnd"))
             button.setBackgroundColor(Color.RED);
         else
             button.setBackgroundColor(Color.GRAY);
 
         ListView rosterList = (ListView) findViewById(R.id.rosterlist);
         String rosterToBeDisplayed = (String)intent.getExtras().get("display");
         if(rosterToBeDisplayed.equals("all")){
             Log.d("ROSTER INTENT ALL :", "New Intent Started - ALL");
 
             RosterManager rosterManager = RosterManager.getInstance();
             RosterItemAdapter adapter = new RosterItemAdapter(this,rosterManager.getRosterList());
             rosterList.setAdapter(adapter);
              rosterList.setTextFilterEnabled(true);
         }
     }
 
     public void goToAccounts(View view) {
         Intent intent = new Intent(this, UserListActivity.class);
         startActivity(intent);
     }
 
     public void signOut(View view) {
     }
 
     protected Dialog onCreateDialog(int id) {
         if(id==1){
             AddRosterDialog dialog = new AddRosterDialog(context);
             dialog.setContentView(R.layout.roster_add_dialog);
             dialog.setTitle("Add Your Friend");
             return dialog;
         }
         else if(id==2) {
             AddStatusDialog dialog = new AddStatusDialog(context);
             dialog.setContentView(R.layout.rostet_add_status);
             dialog.setTitle("Set Status");
             return dialog;
         }
         else if(id==3) {
             SearchRosterEntryDialog dialog = new SearchRosterEntryDialog(context);
             dialog.setContentView(R.layout.roster_search_entry);
             dialog.setTitle("Find Ur Friend");
             return dialog;
         }
         Log.d("ROSTER : ","invalid request for dialog");
         return null;
     }
     public void addRosterEntry(View view){
         showDialog(1);
     }
     public void addStatus(View view) {
         Log.d("ROSTER UI :","add status called");
         showDialog(2);
     }
     public void searchRosterEntry(View view) {
         Log.d("ROSTER UI :","roster search called");
         showDialog(3);
     }
     public void launchSpinner(View view) {
         Spinner spinner = (Spinner) findViewById(R.id.roster_availability_spinner);
         spinner.performClick();
     }
 }
 
