 package de.unidue.wiwi.tdr.kn3.rasp_home;
 
 
 import java.util.List;
 
 import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Method;
 import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Type;
 import de.unidue.wiwi.tdr.kn3.rasp_home.DatabaseClass.Room;
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 //import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	
 	boolean selector = false;
 	String[] room = new String[2];
 	
 	 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		//TODO Set localisation Observer
 
 		room[0]="Kitchen";
 		room[1]="Livingroom";
 		try{
		CommunicationClass.ResponseMessage response = MainApplication.com.client.SendRequest(new CommunicationClass.RequestMessage(Method.GET, Type.Room, null, null, null, null));
 		
 		if(response.status!=202){ Toast.makeText(getBaseContext(), "Sorry, an error occurred"  , Toast.LENGTH_SHORT).show(); 
 		return;
 		}
 		}
 		catch(Exception e){
 			Toast.makeText(getBaseContext(), "Sorry, no connectivity"  , Toast.LENGTH_SHORT).show(); 
 		}
 		
 		//MainApplication.database.rooms.
 	//	MainApplication.database.rooms.
 	//room = response.value
 		
 		
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, room);
 		
  
 		actionBar.setListNavigationCallbacks(adapter, navigationListener);
 		
 		//searchPosition();
 
 		}
 	
 	
 		public void searchPosition(){
 		
 			String residence = MainApplication.pos.lastLocation;
 			
 			 Intent nextS = new Intent(getApplicationContext(), RoomControl.class);
 	            
 	          //Intent mit den Daten fllen
 	   
 //	nextS.putExtra("currentRoom", residence);
 
 	            // Intent starten und zur zweiten Activity wechseln
 	            startActivity(nextS);    			
 			
 		};
 	
 	@Override
 		protected void onResume() {
 			// TODO Auto-generated method stub
 			super.onResume();
 			//searchPosition();
 		}
 
 	ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
 		 
         @Override
         public boolean onNavigationItemSelected(int itemPosition, long itemId) {
         	if(selector){
             Toast.makeText(getBaseContext(), "You selected : " + room[itemPosition]  , Toast.LENGTH_SHORT).show();
             
 
             Intent nextScreen = new Intent(getApplicationContext(), RoomControl.class);
             
           //Intent mit den Daten fllen
          nextScreen.putExtra("rooms", room);
          nextScreen.putExtra("currentRoom", room[itemPosition]);
 //nextScreen.putExtra("currentRoom", "test");
 
             // Intent starten und zur zweiten Activity wechseln
             startActivity(nextScreen);
             return true;
             }
         	selector=true;
      
             
             return false;
         }
     };
 		
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
