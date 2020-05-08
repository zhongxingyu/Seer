 package com.zubisoft.matintSMS;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements UpdateListener{
 	
 	private BroadcastReceiver receptorMensajes;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Establecemos que la pantalla se mantenga activa mientras este presente esta pantalla.
         Window window = this.getWindow();
         window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
         				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         this.setContentView(R.layout.main_activity);
     }
     
     @Override
     protected void onResume() {
     	IntentFilter filtro = new IntentFilter("SMSMail.Intent.MAIN");
     	
     	receptorMensajes = new BroadcastReceiver() {
     	public void onReceive(android.content.Context context, android.content.Intent intent) {
     		Toast.makeText(context, intent.getStringExtra("enviador")+" : "+intent.getStringExtra("contenido")+"\n"+intent.getStringExtra("respuesta"), Toast.LENGTH_LONG).show();
     	}};
     	
     	this.registerReceiver(receptorMensajes,filtro);
     	super.onResume();
     }
     
     @Override
     protected void onPause() {
     	this.unregisterReceiver(this.receptorMensajes);
     	super.onPause();
     }
 
 	@Override
 	public void UpdateSMS(String sender, String content) {
 		Toast.makeText(this, sender + " envio: " + content, Toast.LENGTH_LONG).show();
 	}
 	
 	public void TestSMS(View v) {
 		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage("5554", null, "52502724168c525006000001" ,null, null);
 		Log.d("lol", "enviado SMS");
 	}
 }
