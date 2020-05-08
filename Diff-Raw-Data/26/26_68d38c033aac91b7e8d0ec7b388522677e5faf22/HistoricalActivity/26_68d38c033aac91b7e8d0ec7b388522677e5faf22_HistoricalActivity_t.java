 /**
  * 
  */
 package com.majomi.zeninstants;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.majomi.zeninstants.messagesentities.MessageImageEntity;
 import com.majomi.zeninstants.messagesentities.MessageSoundEntity;
 import com.majomi.zeninstants.messagesentities.MessageTextEntity;
 import com.majomi.zeninstants.messagesentities.MessageVideoEntity;
 import com.majomi.zeninstants.messagesviews.HistorialViewsManager;
 import com.majomi.zeninstants.settingscontroller.HistorialManager;
 import com.majomi.zeninstants.utils.Utils;
 
 
 /**
  * 
  * Displays the historial.
  * For now, it's the initial activity
  *
  */
 public class HistoricalActivity extends SherlockActivity{
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		setTheme(R.style.Theme_Sherlock);
 		super.onCreate(savedInstanceState);
 		
 		Utils.setContext(this.getApplicationContext());
 
 		startService(new Intent(this,MessagesService.class));
 
 		HistorialManager.getHistorialManager().loadMessages();
 		
 		
 
 		setContentView(R.layout.historial_layout);
 
 		ListView lv = (ListView) findViewById(R.id.historial_list);
 		lv.setOnItemClickListener(mMessageClickedHandler);
 
         HistorialAdapter adapter = new HistorialAdapter(this);
 	 	lv.setAdapter(adapter);
 	 	
 	 }
 	
 	// Create a message handling witch manage the selection an item.
 	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
 		@SuppressWarnings("rawtypes")
 		public void onItemClick(AdapterView parent, View v, int position, long id) {
 			
 			MessageTextEntity msg = HistorialManager.getHistorialManager().getMessage(position);
 
 			Class c = HistorialViewsManager.getHistorialViewsManager().getViewActionFromEntity(msg);
 
 			Intent myIntent = new Intent(getApplicationContext(), c); 
 			
 			myIntent.putExtra("msg_from", "h");
 			myIntent.putExtra("msg_id", msg.getId());
 			if(msg.getClass() == MessageTextEntity.class) {
 				myIntent.putExtra("msg_type", "t");
 			} else if(msg.getClass() == MessageImageEntity.class) {
 				myIntent.putExtra("msg_type", "i");
 			} else if(msg.getClass() == MessageSoundEntity.class) {
 				myIntent.putExtra("msg_type", "s");
 			} else if(msg.getClass() == MessageVideoEntity.class) {
				myIntent.putExtra("msg_type", "v");
 			}
 			
 			//myIntent.putExtra("MESSAGE", msg);
 			
             startActivityForResult(myIntent, 0);
         }
     };
     
     @Override
     public void onResume(){
     	super.onResume();
 		Utils.setContext(this.getApplicationContext());
 		ListView lv = (ListView) findViewById(R.id.historial_list);
 		lv.setOnItemClickListener(mMessageClickedHandler);
 
         HistorialAdapter adapter = new HistorialAdapter(this);
 	 	lv.setAdapter(adapter);
     	
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
     	getSupportMenuInflater().inflate(R.menu.activity_historial, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.historial_menu_settings:
             	Intent i = new Intent(this, TabsActivity.class);
 				startActivity(i);
                 return true;
             default:
                 return true;
         }
     }
 
 }
 
 
 
 
