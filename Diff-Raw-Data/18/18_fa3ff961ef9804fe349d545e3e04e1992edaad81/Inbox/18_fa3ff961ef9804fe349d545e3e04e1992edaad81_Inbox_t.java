 package edu.nku.cs.csc440.team2.inbox;
 
 
 import java.util.ArrayList;
 
 import net.londatiga.android.ActionItem;
 import net.londatiga.android.NewQAAdapter;
 import net.londatiga.android.QuickAction;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.PopupWindow.OnDismissListener;
 import android.widget.Toast;
 import edu.nku.cs.csc440.team2.SMILCloud;
 import edu.nku.cs.csc440.team2.UIMenus.ListAllUsers;
 import edu.nku.cs.csc440.team2.mediaCloud.MessageLite;
 import edu.nku.cs.csc440.team2.player.SMILPlayer;
 import edu.nku.cs.csc440.team2.provider.MessageProvider;
 import edu.nku.cs.csc460.team2.R;
 
 public class Inbox extends Activity 
 {
 	private ArrayList<MessageLite> messages;
 
 	/**
      * @param savedInstanceState
      * @return void
      * @note Activity should run in landscape mode.
      * 
      * Call when Activity is first created
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.inbox_main);
     	
     	/*
     	 * Get the list of messages and put the names into a string
     	 */
     	this.messages = this.getMessages();
     	final String [] data = new String[this.messages.size()];
     	if( this.messages != null )
     	{
 	    	for( int i = 0; i < this.messages.size(); i++ )
 	    	{
 	    		data[i] = this.messages.get(i).getName();
 	    	}
     	}
     	
     	
     	/*
     	 * Define the list view and adapter and set the data that we
     	 * collected in the section above
     	 */
     	ListView mList = (ListView) findViewById(R.id.l_list);
     	NewQAAdapter adapter = new NewQAAdapter(this);
     	
     	adapter.setData(data);
         mList.setAdapter(adapter);
         
         /*
          * Define the actions to be used in the quick action menu
          */
         final ActionItem shrAction = new ActionItem();
 		
 		shrAction.setTitle("Share");
 		shrAction.setIcon(getResources().getDrawable(R.drawable.inbox_share));
 
 		final ActionItem plyAction = new ActionItem();
 		
 		plyAction.setTitle("Play");
 		plyAction.setIcon(getResources().getDrawable(R.drawable.inbox_play));
 		
 		final ActionItem delAction = new ActionItem();
 		
 		delAction.setTitle("Delete");
 		delAction.setIcon(getResources().getDrawable(R.drawable.inbox_delete));
 		
 		/*
 		 * Set an onClick listener for the quick action. For each action, define
 		 * a separate action
 		 */
 		mList.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				
 				final QuickAction mQuickAction 	= new QuickAction(view);
 				
 				final ImageView mMoreImage 		= (ImageView) view.findViewById(R.id.i_more);
 				
 				final String text				= data[position];
 				
 				String tempId = null;
 				for( MessageLite ml : Inbox.this.messages )
 				{
 					if( ml.getName() == text )
 					{
 						tempId = ml.getUniqueId();
 					}
 				}
 				final String messageId = tempId;
 				
 				mMoreImage.setImageResource(R.drawable.ic_list_more_selected);
 				
 				/*
 				 * Define the click listener for sharing
 				 */
 				shrAction.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						((SMILCloud)Inbox.this.getApplication()).setSharedMessageId(
 								messageId);
 				    	Intent i = new Intent(Inbox.this, ListAllUsers.class);
 				    	Inbox.this.startActivity(i);
 						mQuickAction.dismiss();
 					}
 				});
 
 				/*
 				 * Define a click listener for playing
 				 */
 				plyAction.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						mQuickAction.dismiss();
 				    	
 						((SMILCloud)Inbox.this.getApplication()).queueDocumentToPlay(messageId);
 						Intent i = new Intent(Inbox.this, SMILPlayer.class);
 						Inbox.this.startActivity(i);
 					}
 				});
 				
 				/*
 				 * Define a click listener for deletion
 				 */
 				delAction.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Toast.makeText(Inbox.this, text + " deleted.", Toast.LENGTH_SHORT).show();
 						mQuickAction.dismiss();
 						//TODO: call provider to delete this message
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								
							}
						}).start();
 					}
 				});
 				
 				mQuickAction.addActionItem(plyAction);
 				mQuickAction.addActionItem(shrAction);
 				mQuickAction.addActionItem(delAction);
 				
 				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
 				
 				mQuickAction.setOnDismissListener(new OnDismissListener() {
 					@Override
 					public void onDismiss() {
 						mMoreImage.setImageResource(R.drawable.ic_list_more);
 					}
 				});
 				
 				mQuickAction.show();
 			}
 		});
     	
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.inbox_menu, menu);
         return true;
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
     	switch(item.getItemId())
     	{
     	case R.id.create_message:
     		return true;
     	case R.id.exit_player:
     		finish();
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     
     private ArrayList<MessageLite> getMessages()
     {
     	return (new MessageProvider()).getAllMessage(
         		((SMILCloud)getApplication()).getUserId());
     }
 	
 }
