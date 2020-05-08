 package org.touchirc.activity;
 
 import org.touchirc.R;
 import org.touchirc.adapter.ConversationPagerAdapter;
 import org.touchirc.fragments.ConnectedUsersFragment;
 import org.touchirc.fragments.ConversationFragment;
 import org.touchirc.fragments.ConnectedServersFragment;
 import org.touchirc.irc.IrcBinder;
 import org.touchirc.irc.IrcCommands;
 import org.touchirc.irc.IrcService;
 import org.touchirc.model.Conversation;
 import org.touchirc.model.Message;
 import org.touchirc.model.Server;
 
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
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
 import com.slidingmenu.lib.SlidingMenu;
 
 public class ConversationActivity extends SherlockFragmentActivity implements ServiceConnection {
 
     private IrcService ircService;
     private Server currentServer;
     private ViewPager vp;
     private SlidingMenu menu;
     private EditText inputMessage;
     private ConversationPagerAdapter cPagerAdapter;
     
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
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
         ircService = null;
         Intent intent = new Intent(this, IrcService.class);
         //  getApplicationContext().startService(intent);
         if(getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE)){
             System.out.println("Bind to Service");
         }
 
         // set the viewpager
         vp = (ViewPager) findViewById(R.id.vp);
         vp.setOnPageChangeListener(new OnPageChangeListener() {
             @Override
             public void onPageScrollStateChanged(int arg0) { }
 
             @Override
             public void onPageScrolled(int arg0, float arg1, int arg2) { }
 
             @Override
             public void onPageSelected(int position) {
                 switch (position) {
                 case 0:
                     menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                     break;
                 default:
                     menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                     break;
                 }
                 ircService.setCurrentChannel(ircService.getBot(currentServer).getChannel(currentServer.getAllConversations().get(position)));
             }
 
         });
         vp.setCurrentItem(0);
                 
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
     
     public void sendMessage() {
     	// Variables...okay it's looooonnnng
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
     	
         TextKeyListener.clear(inputMessage.getText()); // Clean the edit text, important !
     	
     	// Refresh all these things
         ircService.sendBroadcast(new Intent("org.touchirc.irc.newMessage"));
     }
     
     public void sendCommand(String command){
     	String[] args = command.split(" ");
     	String cmd = args[0].substring(1, args[0].length()).toLowerCase();
     	
     	for(String c : IrcCommands.ALL_COMMANDS){
     		if(c.equals(cmd)){
     			System.out.println("Command exists");
     			if(cmd.equals(IrcCommands.JOIN_CHANNEL)) {
     				if(args.length < 3){ // Ugly !?
     					ircService.getBot(currentServer).joinChannel(args[1]);
     				} else {
     					ircService.getBot(currentServer).joinChannel(args[1], args[2]);
     				}
     			}
     		}
     	}
     	System.out.println("Command : " + cmd);
     }
     
 
     @Override
     public void onServiceDisconnected(ComponentName name) {
         ircService = null;        
     }
 
     @Override
     public void onServiceConnected(ComponentName name, IBinder binder) {
         ircService = ((IrcBinder) binder).getService();
         
         // Retrieve the currently connected server
         currentServer = ircService.getCurrentServer();
         
         // Add the pager adapter
         cPagerAdapter = new ConversationPagerAdapter(getSupportFragmentManager(), this.currentServer);
         vp.setAdapter(cPagerAdapter);
         
         // Set the Activity title
         setTitle(currentServer.getName());
         
         // Set the current channel (by default, when launching it's 0
         ircService.setCurrentChannel(ircService.getBot(currentServer).getChannel(currentServer.getAllConversations().get(0)));
         
         final ConnectedServersFragment connectedServerFragment = new ConnectedServersFragment(ircService);
         final ConnectedUsersFragment connectedUserFragment = new ConnectedUsersFragment(ircService);
         
         // Register a new Broadcast Receiver to update the list of Fragments when channels states change
         BroadcastReceiver channelReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				String lastConv = currentServer.getLastConversationName();
 				cPagerAdapter.addFragment(new ConversationFragment(currentServer.getConversation(lastConv)));
 				connectedServerFragment.getAdapter().notifyDataSetChanged();
 				ircService.setCurrentChannel(ircService.getBot(currentServer).getChannel(lastConv));
 			}	
 		};
 		registerReceiver(channelReceiver , new IntentFilter("org.touchirc.irc.channellistUpdated"));
 		
 		// Register a new Broadcast Receiver to update the list of Fragments when userList states change
         BroadcastReceiver userReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				connectedUserFragment.getAdapter().notifyDataSetChanged();
 			}	
 		};
		registerReceiver(channelReceiver , new IntentFilter("org.touchirc.irc.userlistUpdated"));
        
         getSupportFragmentManager().beginTransaction().replace(R.id.connectedServerLayout, connectedServerFragment).commit();
         getSupportFragmentManager().beginTransaction().replace(R.id.connectedUserLayout, connectedUserFragment).commit();
     }  
     
     public void setCurrentConversation(int positon){
     	this.vp.setCurrentItem(positon);
     	menu.showContent();
     }
 
 }
