 /**
  * 
  */
 package com.majomi.zeninstants;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.majomi.zeninstants.messagescontroller.MessageManager;
 import com.majomi.zeninstants.messagesentities.MessageTextEntity;
 
 
 /**
  * 
  * Displays the historial.
  * For now, it's the initial activity
  *
  */
 public class HistoricalActivity extends Activity{
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		startService(new Intent(this,MessagesService.class));
 
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		setTheme(R.style.Theme_Sherlock);
 
 		setContentView(R.layout.historial_layout);
 
 		ListView lv = (ListView) findViewById(R.id.historial_list);
 		lv.setOnItemClickListener(mMessageClickedHandler);
 		Historial_Adapter adapter = new Historial_Adapter(this);
 		lv.setAdapter(adapter);
 
 		Button myBtn=(Button) findViewById(R.id.hl_settings_button);
 
 		myBtn.setOnClickListener(
 				new View.OnClickListener() {
 					public void onClick(View v) {
 						Intent i = new Intent(getApplicationContext(), TabsActivity.class);
 						startActivity(i);
 					}
 				});
 	}
 
 
 
 	// Create a message handling witch manage the selection an item.
 	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
 		@SuppressWarnings("rawtypes")
 		public void onItemClick(AdapterView parent, View v, int position, long id) {
 			MessageTextEntity msg = MessageManager.getMessageManager().get(position);
 
			Intent myIntent = new Intent(getApplicationContext(), msg.getClass()); 
 
 			myIntent.putExtra("MESSAGE_ID", position);
 			
 			startActivityForResult(myIntent, 0);
 		}
 	};
 }
 
 //// Saved version of the MainActivity (useful for the services stuff). Will be deleted soon..
 //
 //package com.majomi.zeninstants;
 //
 //import android.app.Activity;
 //import android.content.Intent;
 //import android.os.Bundle;
 //import android.view.Menu;
 //import android.widget.TextView;
 //
 //public class MainActivity extends Activity {
 //
 //	//public static final String KEY_121 = "http://xx.xx.xxx.xxx/hellomysql/mysqlcon.php"; //i use my real ip here
 //	public static final String KEY_121 = "http://192.168.1.138/zenManagement/index.php"; //i use my real ip here
 //	TextView txt;
 //	
 //  @Override
 //  public void onCreate(Bundle savedInstanceState) {
 //  	super.onCreate(savedInstanceState);
 //  	
 //      setContentView(R.layout.activity_main);
 //      //Hi!! 
 //      AppLog.logString("Main:Starting Service");
 //      startService(new Intent(this,MessagesService.class));
 //      AppLog.logString("Main:Service Started");
 //  }
 //
 //  @Override
 //  public boolean onCreateOptionsMenu(Menu menu) {
 //      getMenuInflater().inflate(R.menu.activity_main, menu);
 //      return true;
 //  }
 //  
 //  public void startMsgService(){
 //  	Thread t = new Thread(){
 //  		public void run(){
 //  		getApplicationContext().startService(new Intent(getApplicationContext(),MessagesService.class));
 //
 //  		}
 //  		};
 //  		t.start();
 //  }
 //  
 //}
 
 
 
