 package org.touchirc.activity;
 
 import java.util.Arrays;
 
 import org.touchirc.R;
 import org.touchirc.adapter.ConversationPagerAdapter;
 import org.touchirc.fragments.ConnectedServersFragment;
 import org.touchirc.fragments.ConnectedUsersFragment;
 import org.touchirc.fragments.ConversationFragment;
 import org.touchirc.irc.IrcBinder;
 import org.touchirc.irc.IrcBot;
 import org.touchirc.irc.IrcCommands;
 import org.touchirc.irc.IrcService;
 import org.touchirc.model.Conversation;
 import org.touchirc.model.Message;
 import org.touchirc.model.Server;
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.method.TextKeyListener;
 
 import android.view.KeyEvent;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.slidingmenu.lib.SlidingMenu;
 
 public class ConversationActivity extends SherlockFragmentActivity implements ServiceConnection {
 
     private IrcService ircService;
     private Server currentServer;
     private ViewPager vp;
     private SlidingMenu menu;
     public EditText inputMessage;
     private ConversationPagerAdapter cPagerAdapter;
     private ConnectedUsersFragment connectedUserFragment;
     private ConnectedServersFragment connectedServerFragment;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setTitle(R.string.app_name);
         
         // set the content view
         setContentView(R.layout.conversation_display);
            
         // configure the SlidingMenu
         menu = new SlidingMenu(this);
         menu.setMode(SlidingMenu.LEFT_RIGHT);
         menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
         menu.setShadowWidthRes(R.dimen.sliding_shadow_width);
         menu.setShadowDrawable(R.drawable.shadow);
         menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         menu.setFadeDegree(0.35f);
         menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
         menu.setMenu(R.layout.connected_servers);
         menu.setSecondaryMenu(R.layout.connected_users);
         menu.setSecondaryShadowDrawable(R.drawable.shadow_right);
 
         // Show "home" button
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
         // Bind the service
         ircService = null;
         Intent intent = new Intent(this, IrcService.class);
         getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
 
         // Set the viewpager
         vp = (ViewPager) findViewById(R.id.vp);
         vp.setOnPageChangeListener(new OnPageChangeListener() {
             @Override
             public void onPageScrollStateChanged(int arg0) { }
 
             @Override
             public void onPageScrolled(int arg0, float arg1, int arg2) { }
 
             @Override
             public void onPageSelected(int position) {
                 ircService.setCurrentChannel(ircService.getBot(currentServer).getChannel(currentServer.getAllConversations().get(position)));
                 // Refresh right menu when changing channel
                 connectedUserFragment.getAdapter().notifyDataSetChanged();
             }
         });
         vp.setCurrentItem(0); // Default to 0
                 
         // Set the EditText
         inputMessage = (EditText) findViewById(R.id.input);
         inputMessage.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 boolean handled = false;
                 if (actionId == EditorInfo.IME_ACTION_SEND) {
                     sendMessage();
                     handled = true;
                 }
                 return handled;
             }
         });
                
     }
     
     /**
      * sendMessage
      * send the text in the EditText. If the first character is a '/'  the text is passed to sendCommand
      */
     public void sendMessage() {
         String mMessage = inputMessage.getText().toString();
         Conversation mCurrentConversation = currentServer.getConversation(ircService.getCurrentChannel().getName());
         String mAuthor = ircService.getBot(currentServer).getNick();
         
         if(mMessage.charAt(0) == '/') {
             sendCommand(mMessage);
         } else {
             // First, add message to the app
             mCurrentConversation.addMessage(new Message(mMessage, mAuthor, Message.TYPE_MESSAGE));
             // Second, send the message to the network
             ircService.getBot(currentServer).sendMessage(ircService.getCurrentChannel().getName(), inputMessage.getText().toString());
         }
         
         // Clean the edit text, important !
         TextKeyListener.clear(inputMessage.getText());
         
         // Refresh all these things
         ircService.sendBroadcast(new Intent("org.touchirc.irc.newMessage"));
     }
     
     /**
      * sendCommand
      * detect if the string is a command and send it.
      */
     public void sendCommand(String command){
         String[] args = command.split(" ");
         String cmd = args[0].substring(1, args[0].length()).toLowerCase();
         IrcBot bot = ircService.getBot(currentServer);
         args = Arrays.copyOfRange(args, 1, args.length);
         
         for(String c : IrcCommands.ALL_COMMANDS){
             if(c.equals(cmd)){
                 // Join Channel
                 if(cmd.equals(IrcCommands.JOIN_CHANNEL)) {
                     if(args.length < 2){
                         bot.joinChannel(args[0]);
                     } else {
                         bot.joinChannel(args[0], args[1]);
                     }
                 // Action (/me)
                 } else if(cmd.equals(IrcCommands.ACTION)){
                     String msg = Arrays.toString(args).replaceAll("(\\[)|(,)|(\\])", "");
                     currentServer.getConversation(ircService.getCurrentChannel().getName()).addMessage(new Message(msg, bot.getNick(), Message.TYPE_ACTION));
                     bot.sendAction(ircService.getCurrentChannel(),msg);
                 // Send a MP
                 } else if(cmd.equals(IrcCommands.QUERY)){
                     String msg = Arrays.toString(Arrays.copyOfRange(args, 1, args.length)).replaceAll("(\\[)|(,)|(\\])", "");
                     bot.getUser(args[0]).sendMessage(msg);
                     if(!currentServer.hasConversation(bot.getUser(args[0]).getNick())){
                         currentServer.addConversation(new Conversation(bot.getUser(args[0]).getNick()));
                         ircService.sendBroadcast(new Intent().setAction("org.touchirc.irc.channellistUpdated"));
                     }
                     currentServer.getConversation(bot.getUser(args[0]).getNick()).addMessage(new Message(msg, bot.getNick()));
                     ircService.sendBroadcast(new Intent().setAction("org.touchirc.irc.newMessage"));
                 // Quit a Channel
                 // XXX : BUG, PageTitle not refreshed.
                 } else if(cmd.equals(IrcCommands.PART)){
                     // Get the position of the current Fragment
                     int position = currentServer.getAllConversations().indexOf(ircService.getCurrentChannel().getName());
 
                     if(args.length < 1){
                         bot.partChannel(ircService.getCurrentChannel());
                     } else {
                         String reason = Arrays.toString(args).replaceAll("(\\[)|(,)|(\\])", "");
                         bot.partChannel(ircService.getCurrentChannel(), reason);
                     }
 
                     // Refresh ALL
                     cPagerAdapter.removeFragment(position);
                     // XXX Handle exceptions if position == 0 and there is no channel
                     if(position != 0) position --;
                     vp.setCurrentItem(position);
                     ircService.setCurrentChannel(bot.getChannel(currentServer.getAllConversations().get(position)));
                     connectedUserFragment.getAdapter().notifyDataSetChanged();
                     connectedServerFragment.getAdapter().notifyDataSetChanged();
                 }
             }
         }
     }
     
 
     @Override
     public void onServiceDisconnected(ComponentName name) {
         ircService = null;        
     }
     
     /**
      * Called after onCreate and when the service is binded
      */
     @Override
     public void onServiceConnected(ComponentName name, IBinder binder) {
         ircService = ((IrcBinder) binder).getService();
         
         // Retrieve the currently connected server
         if(ircService.getCurrentServer() == null){
             AlertDialog.Builder notConnected = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
             notConnected.setMessage(R.string.notConnected);
             notConnected.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     finish();
                 }
             });
             notConnected.setTitle(R.string.notConnectedTitle);
             notConnected.setIcon(android.R.drawable.ic_dialog_alert);
             notConnected.show();
         } else {
             currentServer = ircService.getCurrentServer();
 
             // Add the pager adapter
             cPagerAdapter = new ConversationPagerAdapter(getSupportFragmentManager(), this.currentServer);
             vp.setAdapter(cPagerAdapter);
 
             if(currentServer.getAllConversations().size()==0){
                 joinChannel(false);
             } else {
                 init();
             }
         }
     }
     
     private void init(){
         // Set the Activity title
         setTitle(currentServer.getName());
 
         // Set the current channel (by default, when launching it's 0
         ircService.setCurrentChannel(ircService.getBot(currentServer).getChannel(currentServer.getAllConversations().get(0)));
         
         connectedServerFragment = new ConnectedServersFragment(ircService);
         connectedUserFragment = new ConnectedUsersFragment(ircService);
         
         // Register a new Broadcast Receiver to update the list of Fragments when channels states change
         BroadcastReceiver channelReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 // With this, we prevent a new fragment to be added when a joinEvent is sent.
                 // Btw, we loose the focus on a channel just joined :(
                 if(cPagerAdapter.getCount() != currentServer.getAllConversations().size()) {
                     String lastConv = currentServer.getLastConversationName();
                     ConversationFragment c = new ConversationFragment(currentServer.getConversation(lastConv));
                     cPagerAdapter.addFragment(c);
                     ircService.setCurrentChannel(ircService.getBot(currentServer).getChannel(lastConv));
                 }
                 connectedServerFragment.getAdapter().notifyDataSetChanged();
                 connectedUserFragment.getAdapter().notifyDataSetChanged();                
             }    
         };
         registerReceiver(channelReceiver , new IntentFilter("org.touchirc.irc.channellistUpdated"));
         
         // Register a new Broadcast Receiver to update the list of Fragments when userList states change
         BroadcastReceiver userReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 // ((TextView)findViewById(R.id.connectedUserCount)).setText(getResources().getString(R.string.users) + " (" + ircService.getCurrentChannel().getUsers().size() + ")");
                 connectedUserFragment.getAdapter().notifyDataSetChanged();
             }    
         };
         registerReceiver(userReceiver , new IntentFilter("org.touchirc.irc.userlistUpdated"));
         
         // Replace the left and right menu of slidingMenu by fragments
         getSupportFragmentManager().beginTransaction().replace(R.id.connectedServerLayout, connectedServerFragment).commit();
         getSupportFragmentManager().beginTransaction().replace(R.id.connectedUserLayout, connectedUserFragment).commit();
 
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.conversation_menu, menu);
         return true;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         return super.onKeyDown(keyCode, event);
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         // Toggle slidingMenu left if you click on the "home" icon
         case android.R.id.home:
             menu.toggle();
             return true;
             
         case R.id.itemJoin:
             joinChannel(false);
             return true;
         }
         return super.onOptionsItemSelected((android.view.MenuItem) item);
     }
     
     /**
      * Show to fragment with the position given to the user
      * Used by ConnectedServersFragment
      */
     public void setCurrentConversation(int positon){
        this.vp.setCurrentItem(positon);
         menu.showContent();
     }
     
     public void joinChannel(final boolean exist){
         AlertDialog.Builder joinChannel = new AlertDialog.Builder(this);
         final EditText channel = new EditText(this);
         joinChannel.setView(channel)
         .setPositiveButton(R.string.joinChannelConfirm, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                 String sChannel = channel.getText().toString();
                 if(sChannel.isEmpty()) {
                     dialog.dismiss(); // For the moment dispatch the dialog window
                 }
                 ircService.getBot(currentServer).joinChannel("#" + sChannel);
                 if(currentServer.getAllConversations().size() == 0) {
                     while(currentServer.getAllConversations().size() == 0){}
                     ConversationFragment c = new ConversationFragment(currentServer.getConversation(currentServer.getLastConversationName()));
                     cPagerAdapter.addFragment(c);
                     init();
                 }
             }
         })
         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 if(exist){
                     dialog.dismiss();
                 } else {
                     finish();
                 }
             }
         });
         joinChannel.setTitle(R.string.joinChannel);
         joinChannel.show();
     }
 
 }
