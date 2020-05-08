 package sta.andswtch.gui;
 
 import sta.andswtch.R;
 import sta.andswtch.extensionLead.ExtensionLead;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.TextView;
 
 public class AndSwtch extends Activity {
 	
 	AlertDialog alert;
 	ExtensionLead extLead;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		
 		extLead = new ExtensionLead(this);
 		
 		//will be removed later, it is just a method to create a alert dialog
 		
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("dialog")
             .setCancelable(false)
             .setTitle("notifier")
             .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
                 }
             });
         alert = builder.create();
 		
 		
 		
 	}
 
 	public void onOff(View v) {
 		
 		
 		extLead.sendUpdateMessage();
 		
 		TextView tv;
 		int id = v.getId();
 		if (id == findViewById(R.id.Button01).getId()) {
 			tv = (TextView) findViewById(R.id.TextView01);
 			tv.setText("clicked pP01");
 		} else if (id == findViewById(R.id.Button02).getId()) {
 			tv = (TextView) findViewById(R.id.TextView02);
 			tv.setText("clicked pP02");
 		} else if (id == findViewById(R.id.Button03).getId()) {
 			tv = (TextView) findViewById(R.id.TextView03);
 			tv.setText("clicked pP03");
 		} else if (id == findViewById(R.id.Button04).getId()) {
 			tv = (TextView) findViewById(R.id.TextView04);
 			tv.setText("clicked eL");
 		}
 
 	}
 	
 	
 	public void updateActivity(){
         Message msg = new Message();
         msg.what = 1;
         handlerEvent.sendMessage(msg);  
 		
 		
 	}
 	
     private Handler handlerEvent = new Handler() {
 
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case 1: {
                 
             alert.setMessage(extLead.getResponse());	
             alert.show();
             }
                 break;         
             default: {
                 super.handleMessage(msg);
             }
                 break;         
             }
         }
     };
 	
 
 }
